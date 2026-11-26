#!/usr/bin/env python3
"""Real-time scanner: NSE stocks, forex and gold. Alerts on each new setup.

Timeframes: 30m, 1h, 1d, 1wk, 1mo. Every 15 minutes it refreshes prices for
each market that is open:
  * NSE F&O stocks (about 220) 09:15-15:30 IST, Monday to Friday
  * forex, gold about 24 hours a day, Monday to Friday (Sunday evening to Friday
    evening New York time; roughly 03:30 Monday to 03:30 Saturday IST)
Whenever a candle closes it checks the video's rules on it and sends an alert
for each instrument where they have just become true.

  python realtime.py                              # NSE F&O stocks + forex + gold
  python realtime.py --universe all               # every NSE stock, not only F&O
  python realtime.py --markets forex,gold         # only forex and gold
  python realtime.py --timeframes 1h,1d,1wk       # fewer timeframes
  python realtime.py --universe nifty500          # fewer NSE stocks
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
from data import fno_lots, load_universe, refresh_fno
from events import ResultsCalendar
from globalmarket import GlobalMarketData
from marketdata import ALL_TIMEFRAMES, MarketData, now_ist
from strategy import Params, evaluate

HERE = Path(__file__).parent
log = logging.getLogger("realtime")
MARKETS = ("nse", "forex", "gold")


def _scan_one(job):
    """Runs in a worker process: evaluate one instrument on several timeframes."""
    name, market, currency, lot, frames, params = job
    out = []
    for tf, df, provisional in frames:
        s = evaluate(name, df, params, timeframe=tf)
        if s and s.fresh:
            s.market, s.currency, s.lot = market, currency, lot
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
        markets = [m.strip().lower() for m in args.markets.split(",")]
        if set(markets) - set(MARKETS):
            raise SystemExit(f"unknown market(s); choose from {MARKETS}")
        cache = HERE / ".cache"
        self.stores = []
        if "nse" in markets and args.universe == "fno":
            log.info("F&O list: %s", "updated from NSE" if refresh_fno()
                     else "using the saved list (NSE download failed)")
        self.lots = fno_lots()
        if "nse" in markets:
            skip = set(load_universe(args.exclude)) if Path(args.exclude).exists() else set()
            nse = [s for s in load_universe(args.universe) if s not in skip]
            self.stores.append(MarketData(nse, self.timeframes, cache))
        if "forex" in markets:
            self.stores.append(GlobalMarketData("FOREX", load_universe(args.forex),
                                                self.timeframes, cache, currency=""))
        if "gold" in markets:
            self.stores.append(GlobalMarketData("GOLD", load_universe(args.gold),
                                                self.timeframes, cache, currency="$"))
        self.alerter = Alerter(HERE / "alerts_log.csv", cache / "sent.json")
        self.results = ResultsCalendar()
        self.last_bar: dict[tuple, pd.Timestamp] = {}
        self.last_boundary: dict[tuple, object] = {}
        self.pool = ProcessPoolExecutor(max_workers=args.workers)

    def scan(self, store) -> list:
        t0 = time.time()
        modes = [False, True] if self.args.early else [False]
        # Skip timeframes where no new candle has closed since the last scan.
        tfs = []
        for tf in self.timeframes:
            b = store.boundary(tf)
            if self.args.early or self.last_boundary.get((store.name, tf)) != b:
                tfs.append(tf)
            self.last_boundary[(store.name, tf)] = b
        jobs = []
        for sym in store.symbols:
            frames = []
            for tf in tfs:
                for provisional in modes:
                    df = store.candles(sym, tf, include_open_bar=provisional)
                    if df is None or df.empty:
                        continue
                    bar = df.index[-1]
                    key = (store.name, sym, tf, provisional)
                    # closed candles: check each one once; forming candles: every cycle
                    if not provisional and self.last_bar.get(key) == bar:
                        continue
                    if provisional and store.is_closed(sym, bar, tf):
                        continue   # already covered by the closed-candle check
                    self.last_bar[key] = bar
                    frames.append((tf, df, provisional))
            if frames:
                lot = self.lots.get(sym) if store.name == "NSE" else None
                jobs.append((store.display(sym), store.name, store.currency, lot, frames,
                             self.params))
        found = []
        if jobs:
            for res in self.pool.map(_scan_one, jobs, chunksize=20):
                found.extend(res)
            new = [x for x in found if self.alerter.new(*x)]
            log.info("%s: checked %d candles (%s) on %d instruments in %.0fs: "
                     "%d setup(s), %d new", store.name, sum(len(j[4]) for j in jobs),
                     ",".join(tfs), len(jobs), time.time() - t0, len(found), len(new))
        return found

    def cycle(self, first: bool) -> None:
        now = now_ist()
        found = []
        for store in self.stores:
            if not (first or store.in_session(now)):
                continue
            try:
                if now.date() != getattr(store, "_hist_day", now.date()):
                    store.load_history()   # new day: refresh the history cache
                store._hist_day = now.date()
                n = store.update()
                log.info("%s: prices updated for %d instruments (%s)", store.name, n,
                         store.status())
                found += self.scan(store)
            except Exception:
                log.exception("%s cycle failed; will retry next cycle", store.name)
        # Look up results dates only for NSE stocks about to be alerted.
        nse_new = sorted({s.symbol for s, p in found if s.market == "NSE" and self.alerter.new(s, p)})
        if nse_new and not (first and not self.args.once):
            try:
                dates = self.results.get_many(nse_new)
                for s, _ in found:
                    if s.market == "NSE":
                        s.results = dates.get(s.symbol)
            except Exception:
                log.exception("results-date lookup failed; alerts sent without it")
        # The first scan after start-up finds every setup that is still "fresh" on
        # the latest candle, including old ones (e.g. last month's monthly candle).
        # Send one summary for those instead of hundreds of messages.
        self.alerter.send(found, summary_only=first and not self.args.once)

    def run(self) -> None:
        for store in self.stores:
            store.load_history()
        self.alerter.post("SMI scanner started: " + ", ".join(
            f"{s.name} ({len(s.symbols)})" for s in self.stores) +
            f"; timeframes {', '.join(self.timeframes)}")
        self.cycle(first=True)
        if self.args.once:
            return
        while True:
            # wake 30s after each 15-minute mark so Yahoo has the finished candle;
            # 30m and 1h candles close on these marks for every market.
            now = now_ist()
            nxt = now.floor("15min") + pd.Timedelta(minutes=15, seconds=30)
            time.sleep(max(5.0, (nxt - now).total_seconds()))
            self.cycle(first=False)


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--markets", default="nse,forex,gold",
                    help="any of nse, forex, gold (comma separated)")
    ap.add_argument("--exclude", default=str(HERE / "universe" / "exclude.txt"),
                    help="file of NSE symbols to skip (e.g. ones your broker blocks)")
    ap.add_argument("--universe", default="fno",
                    help="NSE stocks: fno (F&O stocks, default), all, nifty50/100/500, "
                         "a file, or SYM1,SYM2")
    ap.add_argument("--forex", default="forex",
                    help="forex pairs: a file in universe/ or Yahoo tickers like EURUSD=X,USDINR=X")
    ap.add_argument("--gold", default="gold", help="gold ticker(s), default GC=F")
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
