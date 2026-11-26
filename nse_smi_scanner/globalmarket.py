"""Candle store for 24-hour markets: forex pairs and gold, from Yahoo Finance.

* Forex: Yahoo tickers such as ``EURUSD=X`` and ``USDINR=X``. Yahoo reports no
  volume for forex, so the OBV part of GoNoGo is neutral there.
* Gold: Yahoo has no gold *spot* series, so ``GC=F`` (COMEX gold futures, in
  USD per troy ounce) stands in for it. Futures track spot to within a small,
  slowly changing premium, so SMI and swing-low signals are effectively the same.

These markets trade from Sunday evening to Friday evening (New York time), so
candles follow UTC hours. Times are shown in IST, where each UTC hour starts at
:30 (an hourly candle runs 10:30-11:30 IST, for example; 4-hour candles start at
01:30, 05:30, 09:30, 13:30, 17:30 and 21:30 IST).

Every cycle downloads the last two days of 15-minute bars and the last five
daily bars for each instrument; there are only a few, so this is quick.
"""

from __future__ import annotations

import logging
import pickle
import time
from pathlib import Path

import pandas as pd

from marketdata import AGG, INTRADAY, _download, _merge, _resample_daily, now_ist

log = logging.getLogger("globalmarket")
IST_UTC = pd.Timedelta(hours=5, minutes=30)
FEED = pd.Timedelta(minutes=15)

NAMES = {"GC=F": "GOLD"}


def _resample_utc_hours(df: pd.DataFrame, minutes: int) -> pd.DataFrame:
    # Data is in IST (UTC+5:30). Offset 30 minutes puts 15m/30m/1h edges on
    # whole UTC hours; offset 90 minutes puts 4h edges on 00, 04, 08 ... UTC.
    if minutes == 15:
        return df
    offset = "90min" if minutes == 240 else "30min"
    return df.resample(f"{minutes}min", label="left", closed="left",
                       offset=offset).agg(AGG).dropna(subset=["Open"])


class GlobalMarketData:
    def __init__(self, name: str, tickers: list[str], timeframes: list[str],
                 cache_dir: Path, currency: str = ""):
        self.name = name
        self.currency = currency
        self.symbols = tickers
        self.timeframes = timeframes
        self.cache_dir = cache_dir
        self.cache_dir.mkdir(exist_ok=True)
        self.daily: dict[str, pd.DataFrame] = {}
        self.m15: dict[str, pd.DataFrame] = {}

    # ---------- data ----------
    def _get(self, period: str, interval: str) -> dict[str, pd.DataFrame]:
        data = _download(self.symbols, period, interval, suffix="")
        if interval == "1d":
            for df in data.values():
                df.index = df.index.normalize()
        return data

    def load_history(self) -> None:
        f = self.cache_dir / f"{self.name.lower()}.pkl"
        if f.exists():
            stamp, syms, daily, m15 = pickle.loads(f.read_bytes())
            if stamp == now_ist().date() and set(self.symbols) <= syms:
                self.daily, self.m15 = daily, m15
                log.info("%s history: loaded %d instruments from cache", self.name, len(daily))
                return
        t0 = time.time()
        self.daily = self._get("10y", "1d")
        self.m15 = self._get("60d", "15m")
        f.write_bytes(pickle.dumps((now_ist().date(), set(self.symbols), self.daily, self.m15)))
        log.info("%s history: %d instruments in %.0fs", self.name, len(self.daily),
                 time.time() - t0)

    def update(self) -> int:
        fresh15 = self._get("2d", "15m")
        for s, df in fresh15.items():
            self.m15[s] = _merge(self.m15.get(s), df)
        for s, df in self._get("5d", "1d").items():
            self.daily[s] = _merge(self.daily.get(s), df)
        return len(fresh15)

    # ---------- candles ----------
    def candles(self, symbol: str, tf: str, include_open_bar: bool = False) -> pd.DataFrame | None:
        if tf in INTRADAY:
            df = self.m15.get(symbol)
            if df is not None:
                df = _resample_utc_hours(df, INTRADAY[tf])
        else:
            df = self.daily.get(symbol)
            if df is not None and tf != "1d":
                df = _resample_daily(df, tf)
        if df is None or df.empty:
            return None
        if not include_open_bar and not self.is_closed(symbol, df.index[-1], tf):
            df = df.iloc[:-1]
        return df

    def is_closed(self, symbol: str, start: pd.Timestamp, tf: str) -> bool:
        now = now_ist()
        if tf in INTRADAY:
            feed = self.m15.get(symbol)
            if feed is None or feed.empty:
                return False
            data_end = min(now, feed.index[-1] + FEED)
            return start + pd.Timedelta(minutes=INTRADAY[tf]) <= data_end
        # Daily and longer: closed once a bar of a later period exists ...
        daily = self.daily.get(symbol)
        if daily is None or daily.empty:
            return False
        if tf == "1d":
            period_end = start + pd.Timedelta(days=1)
        elif tf == "1wk":
            period_end = start + pd.Timedelta(days=7)
        else:
            period_end = start + pd.offsets.MonthBegin(1)
        if daily.index[-1] >= period_end:
            return True
        # ... or once the weekend close has passed with no later bar yet.
        if self._weekend(now):
            if tf == "1mo":
                next_monday = (now + pd.offsets.Week(weekday=0)).normalize()
                return next_monday >= period_end
            return True
        return False

    @staticmethod
    def _weekend(now_ist_: pd.Timestamp) -> bool:
        """Forex/gold weekend: Friday 22:00 to Sunday 22:00 UTC (approx.)."""
        utc = now_ist_ - IST_UTC
        wd, h = utc.weekday(), utc.hour
        return (wd == 4 and h >= 22) or wd == 5 or (wd == 6 and h < 22)

    # ---------- scanner interface ----------
    def display(self, symbol: str) -> str:
        return NAMES.get(symbol, symbol.replace("=X", ""))

    def in_session(self, now: pd.Timestamp) -> bool:
        # Keep updating for 30 minutes after the weekend close to catch the last candles.
        return not self._weekend(now - pd.Timedelta(minutes=30))

    def boundary(self, tf: str):
        # Only a handful of instruments: check every timeframe every cycle.
        # Each candle is still evaluated once (the scanner remembers it).
        return time.time()

    def status(self) -> str:
        last = [df.index[-1] for df in self.m15.values() if len(df)]
        return f"data up to {max(last):%d %b %H:%M}" if last else "no data"
