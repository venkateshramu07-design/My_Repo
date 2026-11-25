#!/usr/bin/env python3
"""Real-time scanner: every NSE stock, every timeframe, alert on each new setup.

Runs all day. During market hours (09:15-15:30 IST, Mon-Fri) it refreshes
prices every 5 minutes. Whenever a candle closes on any timeframe, it checks
the video's rules on that candle and sends an alert for each stock where they
have just become true.

  python realtime.py                              # all ~2,600 NSE stocks, all timeframes
  python realtime.py --timeframes 15m,1h,1d,1wk   # fewer timeframes
  python realtime.py --universe nifty500
  python realtime.py --early                      # also alert while a candle is still forming
  python realtime.py --once                       # one scan now, then exit (testing)
"""

from __future__ import annotations

import argparse
import logging
import os
import time
from concurrent.futures import ProcessPoolExecutor
from pathlib import Path

import pandas as pd

from alerts import Alerter
from data import load_universe
from marketdata import ALL_TIMEFRAMES, INTRADAY, SESSION_CLOSE, SESSION_OPEN, MarketData, now_ist
from strategy import Params, evaluate

HERE = Path(__file__).parent
log = logging.getLogger("realtime")


def _scan_one(job):
    """Runs in a worker process: evaluate one stock on several timeframes."""
    symbol, frames, params = job
    out = []
    for tf, df, provisional in frames:
        s = evaluate(symbol, df, params, timeframe=tf)
        if s and s.fresh:
            out.append((s, provisional))
    return out


class Scanner:
    def __init__(self, args):
        self.args = args
        self.params = Params(level=args.level, use_gonogo=not args.no_gonogo)
        self.timeframes = [t.strip() for t in args.timeframes.split(",")]
        bad = set(self.timeframes) - set(ALL_TIMEFRAMES)
        if bad:
            raise SystemExit(f"unknown timeframe(s) {bad}; choose from {ALL_TIMEFRAMES}")
        symbols = load_universe(args.universe)
        self.md = MarketData(symbols, self.timeframes, HERE / ".cache")
        self.alerter = Alerter(HERE / "alerts_log.csv", HERE / ".cache" / "sent.json")
        self.last_bar: dict[tuple[str, str, bool], pd.Timestamp] = {}
        self.last_boundary: dict[str, tuple] = {}
        self.first_scan = True
        self.pool = ProcessPoolExecutor(max_workers=args.workers)

    def scan(self) -> None:
        t0 = time.time()
        jobs = []
        modes = [False, True] if self.args.early else [False]
        # Skip timeframes where no new candle has closed since the last scan.
        tfs = []
        for tf in self.timeframes:
            b = self._boundary(tf)
            if self.args.early or self.last_boundary.get(tf) != b:
                tfs.append(tf)
            self.last_boundary[tf] = b
        if not tfs:
            return
        for sym in self.md.symbols:
            frames = []
            for tf in tfs:
                for provisional in modes:
                    df = self.md.candles(sym, tf, include_open_bar=provisional)
                    if df is None or df.empty:
                        continue
                    bar = df.index[-1]
                    key = (sym, tf, provisional)
                    # closed candles: check each one once; forming candles: every cycle
                    if not provisional and self.last_bar.get(key) == bar:
                        continue
                    if provisional and self.md.is_closed(bar, tf):
                        continue   # already covered by the closed-candle check
                    self.last_bar[key] = bar
                    frames.append((tf, df, provisional))
            if frames:
                jobs.append((sym, frames, self.params))
        found = []
        for res in self.pool.map(_scan_one, jobs, chunksize=20):
            found.extend(res)
        n_checks = sum(len(j[1]) for j in jobs)
        new = [x for x in found if self.alerter.new(*x)]
        log.info("checked %d candles (%s) on %d stocks in %.0fs: %d setup(s), %d new",
                 n_checks, ",".join(tfs), len(jobs), time.time() - t0, len(found), len(new))
        # The first scan after start-up finds every setup that is still "fresh" on
        # the latest candle, including old ones (e.g. last month's monthly candle).
        # Send one summary for those instead of hundreds of messages.
        self.alerter.send(found, summary_only=self.first_scan and not self.args.once)
        self.first_scan = False

    def _boundary(self, tf: str) -> tuple:
        """Changes value exactly when a new candle of ``tf`` closes."""
        de = self.md.data_end
        at_close = de >= de.normalize() + SESSION_CLOSE
        if tf in INTRADAY:
            m = INTRADAY[tf]
            return ((de - pd.Timedelta(minutes=15)).floor(f"{m}min"), at_close)
        return (de.date(), at_close)

    def run(self) -> None:
        self.md.load_history()
        self.alerter.post(f"NSE SMI scanner started: {len(self.md.symbols)} stocks, "
                          f"timeframes {', '.join(self.timeframes)}")
        if self.args.once:
            self.md.update()
            self.scan()
            return
        first = True
        while True:
            now = now_ist()
            day = now.normalize()
            in_session = now.weekday() < 5 and \
                day + SESSION_OPEN <= now <= day + SESSION_CLOSE + pd.Timedelta(minutes=10)
            if in_session or first:
                if now.date() != getattr(self, "_hist_day", now.date()):
                    self.md.load_history()   # new day: refresh history cache
                self._hist_day = now.date()
                try:
                    n = self.md.update()
                    log.info("prices updated for %d stocks (data up to %s)", n,
                             self.md.data_end.strftime("%H:%M"))
                    self.scan()
                except Exception:
                    log.exception("cycle failed; will retry next cycle")
                first = False
            # wake 30s after each 5-minute mark so Yahoo has the finished candle
            nxt = (now.floor("5min") + pd.Timedelta(minutes=5, seconds=30))
            time.sleep(max(5.0, (nxt - now_ist()).total_seconds()))


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--universe", default="all",
                    help="all (every NSE stock), nifty50/100/500, a file, or SYM1,SYM2")
    ap.add_argument("--timeframes", default=",".join(ALL_TIMEFRAMES))
    ap.add_argument("--level", type=float, default=40.0)
    ap.add_argument("--no-gonogo", action="store_true")
    ap.add_argument("--early", action="store_true",
                    help="also alert on candles that have not closed yet")
    ap.add_argument("--once", action="store_true", help="scan once and exit")
    ap.add_argument("--workers", type=int, default=max(1, (os.cpu_count() or 2) - 1))
    args = ap.parse_args()
    os.environ["TZ"] = "Asia/Kolkata"       # log times in IST
    if hasattr(time, "tzset"):
        time.tzset()
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s",
                        datefmt="%H:%M:%S")
    logging.getLogger("yfinance").setLevel(logging.CRITICAL)
    Scanner(args).run()


if __name__ == "__main__":
    main()
