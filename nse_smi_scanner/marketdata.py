"""Live candle store for many NSE stocks across timeframes, fed by Yahoo Finance.

Timeframes: 30m, 1h, 1d, 1wk, 1mo (nothing shorter than 30 minutes).

Start-up downloads (cached on disk, refreshed once per day):
  * daily bars, 5 years      -> 1d, and resampled to 1wk and 1mo
  * 15-minute bars, 60 days  -> resampled to 30m and 1h

Every cycle only today's 15-minute bars are downloaded (one small request per
100 stocks). They update the 15-minute store and today's daily bar, so every
timeframe moves in step.

Candles follow NSE hours (09:15-15:30 IST). Hourly bars run 09:15, 10:15 ...
15:15 (the last one is 15 minutes long), the same as most Indian charting sites.
"""

from __future__ import annotations

import logging
import pickle
import time
from pathlib import Path

import pandas as pd
import yfinance as yf

log = logging.getLogger("marketdata")

INTRADAY = {"30m": 30, "1h": 60}
FEED_MINUTES = 15          # price feed resolution; candles are built from it
HIGHER = ("1d", "1wk", "1mo")
ALL_TIMEFRAMES = tuple(INTRADAY) + HIGHER
SESSION_OPEN = pd.Timedelta(hours=9, minutes=15)
SESSION_CLOSE = pd.Timedelta(hours=15, minutes=30)
COLS = ["Open", "High", "Low", "Close", "Volume"]
AGG = {"Open": "first", "High": "max", "Low": "min", "Close": "last", "Volume": "sum"}


def now_ist() -> pd.Timestamp:
    return pd.Timestamp.now(tz="Asia/Kolkata").tz_localize(None)


def _download(symbols: list[str], period: str, interval: str, chunk: int = 100,
              retries: int = 4, suffix: str = ".NS") -> dict[str, pd.DataFrame]:
    """Batch download from Yahoo; returns naive-IST OHLCV frames per symbol."""
    out: dict[str, pd.DataFrame] = {}
    for start in range(0, len(symbols), chunk):
        batch = symbols[start:start + chunk]
        tickers = [f"{s}{suffix}" for s in batch]
        raw = None
        for attempt in range(retries):
            try:
                raw = yf.download(tickers, period=period, interval=interval,
                                  group_by="ticker", auto_adjust=False,
                                  progress=False, threads=True)
                break
            except Exception as e:  # network / rate limit
                wait = 5 * 2 ** attempt
                log.warning("download failed (%s), retrying in %ss", e, wait)
                time.sleep(wait)
        if raw is None or raw.empty:
            continue
        for s, t in zip(batch, tickers):
            try:
                df = raw[t] if isinstance(raw.columns, pd.MultiIndex) else raw
                df = df[COLS].dropna(subset=["Open", "High", "Low", "Close"])
            except KeyError:
                continue
            if df.empty:
                continue
            if df.index.tz is not None:
                df.index = df.index.tz_convert("Asia/Kolkata").tz_localize(None)
            out[s] = df.astype("float64")
    return out


def _resample_intraday(df: pd.DataFrame, minutes: int) -> pd.DataFrame:
    # Bins anchored at 00:15 line up with the 09:15 open for 15m, 30m and 1h.
    return df.resample(f"{minutes}min", label="left", closed="left",
                       offset="15min").agg(AGG).dropna(subset=["Open"])


def _resample_daily(df: pd.DataFrame, tf: str) -> pd.DataFrame:
    rule = "W-MON" if tf == "1wk" else "MS"
    return df.resample(rule, label="left", closed="left").agg(AGG).dropna(subset=["Open"])


def _merge(old: pd.DataFrame | None, new: pd.DataFrame) -> pd.DataFrame:
    if old is None or old.empty:
        return new
    return pd.concat([old[~old.index.isin(new.index)], new]).sort_index()


class MarketData:
    """NSE equities: 09:15-15:30 IST, Monday to Friday."""
    name = "NSE"
    currency = "₹"

    def __init__(self, symbols: list[str], timeframes: list[str], cache_dir: Path):
        self.symbols = symbols
        self.timeframes = timeframes
        self.cache_dir = cache_dir
        self.cache_dir.mkdir(exist_ok=True)
        self.daily: dict[str, pd.DataFrame] = {}
        self.m15: dict[str, pd.DataFrame] = {}
        self.data_end = pd.Timestamp.min   # time up to which candles are complete

    # ---------- start-up history ----------
    def _cached(self, name: str, period: str, interval: str) -> dict[str, pd.DataFrame]:
        f = self.cache_dir / f"{name}.pkl"
        if f.exists():
            stamp, syms, data = pickle.loads(f.read_bytes())
            if stamp == now_ist().date() and set(self.symbols) <= syms:
                log.info("%s history: loaded %d stocks from cache", name, len(data))
                return {s: data[s] for s in self.symbols if s in data}
        t0 = time.time()
        log.info("%s history: downloading %d stocks (%s of %s bars) ...",
                 name, len(self.symbols), period, interval)
        data = _download(self.symbols, period, interval)
        f.write_bytes(pickle.dumps((now_ist().date(), set(self.symbols), data)))
        log.info("%s history: %d stocks in %.0fs", name, len(data), time.time() - t0)
        return data

    def load_history(self) -> None:
        tfs = set(self.timeframes)
        if tfs & set(HIGHER):
            self.daily = self._cached("daily", "5y", "1d")
        # 15-minute bars are always needed: they are the live feed.
        self.m15 = self._cached("m15", "60d", "15m")
        self._set_data_end()

    # ---------- every cycle ----------
    def update(self) -> int:
        """Pull today's 15-minute bars and roll them into every timeframe."""
        fresh = _download(self.symbols, "1d", f"{FEED_MINUTES}m")
        for s, df in fresh.items():
            self.m15[s] = _merge(self.m15.get(s), df)
            day = df.index[-1].normalize()
            today = self.m15[s][self.m15[s].index >= day]
            if s in self.daily:
                bar = pd.DataFrame({"Open": today["Open"].iloc[0],
                                    "High": today["High"].max(), "Low": today["Low"].min(),
                                    "Close": today["Close"].iloc[-1],
                                    "Volume": today["Volume"].sum()}, index=[day])
                self.daily[s] = _merge(self.daily[s], bar)
        self._set_data_end()
        return len(fresh)

    def _set_data_end(self) -> None:
        last = [df.index[-1] for df in self.m15.values() if len(df)]
        if last:
            end = max(last) + pd.Timedelta(minutes=FEED_MINUTES)
            # Yahoo's newest 15m bar may still be forming; it counts as done only
            # once the clock has passed its end.
            self.data_end = min(end, now_ist())

    # ---------- candles ----------
    def candles(self, symbol: str, tf: str, include_open_bar: bool = False) -> pd.DataFrame | None:
        if tf in INTRADAY:
            df = self.m15.get(symbol)
            if df is not None:
                df = _resample_intraday(df, INTRADAY[tf])
        else:
            df = self.daily.get(symbol)
            if df is not None and tf != "1d":
                df = _resample_daily(df, tf)
        if df is None or df.empty:
            return None
        if not include_open_bar and not self.is_closed(symbol, df.index[-1], tf):
            df = df.iloc[:-1]
        return df

    def display(self, symbol: str) -> str:
        return symbol

    def in_session(self, now: pd.Timestamp) -> bool:
        day = now.normalize()
        return now.weekday() < 5 and \
            day + SESSION_OPEN <= now <= day + SESSION_CLOSE + pd.Timedelta(minutes=10)

    def boundary(self, tf: str) -> tuple:
        """Changes value exactly when a new candle of ``tf`` closes."""
        de = self.data_end
        at_close = de >= de.normalize() + SESSION_CLOSE
        if tf in INTRADAY:
            return ((de - pd.Timedelta(minutes=15)).floor(f"{INTRADAY[tf]}min"), at_close)
        return (de.date(), at_close)

    def status(self) -> str:
        return f"data up to {self.data_end:%d %b %H:%M}"

    def is_closed(self, symbol: str, start: pd.Timestamp, tf: str) -> bool:
        end_data = self.data_end
        day = start.normalize()
        if tf in INTRADAY:
            end = min(start + pd.Timedelta(minutes=INTRADAY[tf]), day + SESSION_CLOSE)
            return end_data >= end
        today_close = end_data.normalize() + SESSION_CLOSE
        if tf == "1d":
            return start < end_data.normalize() or end_data >= today_close
        if tf == "1wk":   # start is the Monday of the week
            friday_close = day + pd.Timedelta(days=4) + SESSION_CLOSE
            return end_data >= friday_close
        # 1mo: closed once the month's last weekday has closed
        last_bday = (day + pd.offsets.BMonthEnd(0)).normalize()
        return end_data >= last_bday + SESSION_CLOSE
