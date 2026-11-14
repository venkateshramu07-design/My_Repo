"""Built-in replacement for the Chartink scan, using Groww market data.

Conditions (all must pass), same as the Chartink screen:
  for each of 4 hour, 1 hour, 15 minute:
      SMI %K(10,3,3) > 40,  SMI %D(10,3,3,10) > 40,  SMI %K > SMI %D
  daily volume > 50,000,  close < 1,000,  market cap > 10,000 Cr

How it works:
  * Universe: all NSE EQ stocks, filtered by market cap (refreshed weekly, cached in a file).
  * Each day: download ~60 days of 15-minute candles per stock (one request each).
  * Every scan (default 5 min): fetch today's 15-minute candles, rebuild 1h/4h candles
    aligned to 09:15, compute SMI on the latest (still forming) candle, and send the
    matches to the trading engine, exactly like a Chartink alert.
"""
from __future__ import annotations

import json
import logging
import os
import threading
from datetime import datetime, time, timedelta
from typing import Callable, Optional

from .config import Settings
from .engine import IST, Engine, ist_now
from .indicators import Candle, resample, smi

log = logging.getLogger(__name__)
PREP_TIME = time(8, 30)
TIMEFRAMES = (("4 hour", 240), ("1 hour", 60), ("15 minute", 15))


def parse_candles(rows: list) -> list[Candle]:
    out = []
    for r in rows:
        ts = r[0]
        if isinstance(ts, (int, float)):
            t = datetime.fromtimestamp(ts / 1000 if ts > 1e11 else ts, IST)
        else:
            t = datetime.fromisoformat(str(ts).replace(" ", "T"))
            t = t.astimezone(IST) if t.tzinfo else t.replace(tzinfo=IST)
        out.append(Candle(t, float(r[1]), float(r[2]), float(r[3]), float(r[4]), float(r[5] or 0)))
    return out


class Scanner:
    def __init__(self, settings: Settings, data, engine: Optional[Engine],
                 now: Callable[[], datetime] = ist_now):
        self.s = settings
        self.data = data  # GrowwBroker (equity_symbols, quote, candles_15m)
        self.engine = engine
        self.now = now
        self.universe: dict[str, float] = {}  # symbol -> market cap (Cr)
        self.history: dict[str, list[Candle]] = {}
        self._history_day = None
        self.last_scan: Optional[str] = None
        self.last_matches: list[str] = []
        self.last_error: Optional[str] = None

    # ------------------------------------------------------------------ universe
    def load_universe(self, force: bool = False) -> None:
        path = self.s.universe_file
        if not force and os.path.exists(path):
            try:
                saved = json.load(open(path))
                built = datetime.fromisoformat(saved["built"])
                if self.now() - built < timedelta(days=self.s.universe_max_age_days) and saved["symbols"]:
                    self.universe = saved["symbols"]
                    log.info("universe: %d stocks (cached %s)", len(self.universe), saved["built"][:10])
                    return
            except Exception as exc:
                log.warning("universe cache unreadable, rebuilding: %s", exc)

        symbols = self.data.equity_symbols()
        log.info("universe: checking market cap of %d NSE stocks (takes several minutes)",
                 len(symbols))
        keep: dict[str, float] = {}
        for i, sym in enumerate(symbols, 1):
            try:
                q = self.data.quote(sym)
            except Exception as exc:
                log.debug("quote %s failed: %s", sym, exc)
                continue
            if q["market_cap_cr"] > self.s.min_market_cap_cr:
                keep[sym] = round(q["market_cap_cr"], 1)
            if i % 250 == 0:
                log.info("universe: %d/%d checked, %d kept", i, len(symbols), len(keep))
        if not keep:
            raise RuntimeError("universe build returned no stocks (check Groww access)")
        self.universe = keep
        tmp = path + ".tmp"
        json.dump({"built": self.now().isoformat(), "symbols": keep}, open(tmp, "w"))
        os.replace(tmp, path)
        log.info("universe: %d stocks with market cap > %.0f Cr", len(keep), self.s.min_market_cap_cr)

    # ------------------------------------------------------------------ candles
    def _fetch(self, sym: str, start: datetime) -> list[Candle]:
        return parse_candles(self.data.candles_15m(sym, start, self.now()))

    def _load_history(self) -> None:
        start = (self.now() - timedelta(days=self.s.scan_history_days)).replace(
            hour=9, minute=0, second=0, microsecond=0)
        log.info("loading %d days of 15m candles for %d stocks",
                 self.s.scan_history_days, len(self.universe))
        for sym in self.universe:
            try:
                self.history[sym] = self._fetch(sym, start)
            except Exception as exc:
                log.debug("history %s failed: %s", sym, exc)
        self._history_day = self.now().date()

    def _refresh(self, sym: str) -> list[Candle]:
        """Replace today's candles in the cache with fresh ones (incl. the forming candle)."""
        today_open = self.now().replace(hour=9, minute=15, second=0, microsecond=0)
        fresh = self._fetch(sym, today_open)
        old = [c for c in self.history.get(sym, []) if c.ts < today_open]
        self.history[sym] = old + [c for c in fresh if c.ts >= today_open]
        return self.history[sym]

    # ------------------------------------------------------------------ rules
    def evaluate(self, sym: str, c15: list[Candle]) -> dict:
        """Check every condition for one stock. Returns details + "pass" flag."""
        s = self.s
        today = self.now().date()
        res: dict = {"symbol": sym, "pass": False}
        if len(c15) < 60 or c15[-1].ts.date() != today:
            res["reason"] = "not enough / stale data"
            return res
        res["close"] = c15[-1].close
        res["volume"] = sum(c.volume for c in c15 if c.ts.date() == today)
        ok = res["close"] < s.max_close and res["volume"] > s.min_volume
        for name, minutes in TIMEFRAMES:
            bars = c15 if minutes == 15 else resample(c15, minutes)
            k, d = smi(bars, s.smi_k_len, s.smi_s1, s.smi_s2, s.smi_d_len)
            res[name] = {"K": round(k[-1], 2), "D": round(d[-1], 2)}
            ok = ok and k[-1] > s.smi_threshold and d[-1] > s.smi_threshold and k[-1] > d[-1]
        res["pass"] = ok
        return res

    # ------------------------------------------------------------------ scanning
    def prepare(self) -> None:
        """Make sure the universe and today's candle history are loaded."""
        if not self.universe or self._universe_stale():
            self.load_universe()
        if self._history_day != self.now().date():
            self._load_history()

    def _universe_stale(self) -> bool:
        try:
            built = datetime.fromisoformat(json.load(open(self.s.universe_file))["built"])
            return self.now() - built >= timedelta(days=self.s.universe_max_age_days)
        except Exception:
            return True

    def scan(self) -> list[tuple[str, float]]:
        self.prepare()
        matches = []
        for sym in self.universe:
            try:
                r = self.evaluate(sym, self._refresh(sym))
            except Exception as exc:
                log.debug("scan %s failed: %s", sym, exc)
                continue
            if r["pass"]:
                matches.append((sym, r["close"]))
        self.last_scan = self.now().strftime("%Y-%m-%d %H:%M:%S")
        self.last_matches = [m[0] for m in matches]
        log.info("scan done: %d/%d stocks match %s", len(matches), len(self.universe),
                 self.last_matches)
        return matches

    def run_forever(self, stop: threading.Event) -> None:
        log.info("built-in scanner started (every %d min)", self.s.scan_interval_minutes)
        while not stop.is_set():
            n = self.now()
            if n.weekday() < 5 and self.s.entry_start <= n.time() <= self.s.entry_end:
                try:
                    matches = self.scan()
                    self.last_error = None
                    if matches and self.engine:
                        self.engine.handle_signal(matches)
                except Exception as exc:
                    self.last_error = str(exc)[:300]
                    log.exception("scan failed")
                stop.wait(max(5.0, self.s.scan_interval_minutes * 60
                              - (self.now() - n).total_seconds()))
            else:
                # Pre-market (08:30 onwards): get the stock list and history ready.
                if n.weekday() < 5 and n.time() >= PREP_TIME and n.time() < self.s.entry_start:
                    try:
                        self.prepare()
                    except Exception as exc:
                        self.last_error = str(exc)[:300]
                        log.exception("pre-market preparation failed")
                stop.wait(30)

    def status(self) -> dict:
        return {"universe": len(self.universe), "last_scan": self.last_scan,
                "last_matches": self.last_matches, "last_error": self.last_error}
