#!/usr/bin/env python3
"""Rank NSE stocks by how strongly they meet the entry conditions right now.

For each timeframe the stock either meets all the entry conditions (SMI %K and
%D beyond +/-40, higher/lower swing low, GoNoGo) or not. Trend strength score:
monthly 3, weekly 3, daily 2, 4h 2, 1h 1, 30m 1, 15m 1, summed over the chosen
timeframes; ties broken by how far both SMI lines are past the threshold.

Filters (all optional): price range, minimum average daily turnover, and
"structure intact" (price still above the nearest swing low for longs, below
the nearest swing high for shorts). Each pick gets its next results date with
a warning when results are within 14 days.

  python rank.py                                   # all NSE, long side, top 25
  python rank.py --universe fno --side short
  python rank.py --max-price 1000 --min-turnover 10 --top 20
  python rank.py --timeframes 15m,30m,1h,4h --min-turnover 25   # intraday
"""

from __future__ import annotations

import argparse
import logging
from concurrent.futures import ProcessPoolExecutor
from pathlib import Path

import numpy as np
import pandas as pd

from data import fno_lots, load_universe
from events import ResultsCalendar, days_until, warning
from marketdata import MarketData
from strategy import Params, add_indicators, conditions_at

HERE = Path(__file__).parent
DEFAULT_TFS = ["30m", "1h", "1d", "1wk", "1mo"]
WEIGHT = {"15m": 1, "30m": 1, "1h": 1, "4h": 2, "1d": 2, "1wk": 3, "1mo": 3}


def _rank_one(job):
    symbol, frames, side, daily = job
    p = Params()
    row = {"symbol": symbol}
    score, depth = 0, []
    for tf, df in frames:
        if df is None or len(df) < 25:
            return None
        df = add_indicators(df.iloc[-400:], p)
        now, prev = conditions_at(df, len(df) - 1, p), conditions_at(df, len(df) - 2, p)
        ok = now[side]
        row[tf] = ("NEW" if not prev[side] else "YES") if ok else "-"
        row[f"smi_{tf}"] = f"{now['smi_k']:.0f}/{now['smi_d']:.0f}"
        score += WEIGHT[tf] * ok
        lines = (now["smi_k"], now["smi_d"])
        depth.append(min(lines) if side == "long" else -max(lines))
        row[f"swing_{tf}"] = now["cur_swing_low"] if side == "long" else now["cur_swing_high"]
    if daily is None or len(daily) < 20:
        return None
    row["close"] = round(float(daily["Close"].iloc[-1]), 2)
    row["turnover_cr"] = round(float((daily["Close"] * daily["Volume"]).iloc[-20:].mean() / 1e7), 1)
    row["score"] = score
    row["strength"] = round(float(np.mean(depth)), 1)
    return row


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--universe", default="all", help="all, fno, nifty500, a file, or SYM1,SYM2")
    ap.add_argument("--side", default="long", choices=["long", "short"])
    ap.add_argument("--min-price", type=float, default=20)
    ap.add_argument("--max-price", type=float, default=None)
    ap.add_argument("--min-turnover", type=float, default=10,
                    help="minimum 20-day average traded value, in Rs crore (default 10)")
    ap.add_argument("--exclude", default=str(HERE / "universe" / "exclude.txt"),
                    help="file of symbols to skip (e.g. ones your broker blocks)")
    ap.add_argument("--timeframes", default=",".join(DEFAULT_TFS),
                    help="comma separated, from 15m,30m,1h,4h,1d,1wk,1mo")
    ap.add_argument("--top", type=int, default=25)
    ap.add_argument("--out", default=str(HERE / "rank.csv"))
    ap.add_argument("--workers", type=int, default=4)
    args = ap.parse_args()
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(message)s", datefmt="%H:%M:%S")
    logging.getLogger("yfinance").setLevel(logging.CRITICAL)

    symbols = load_universe(args.universe)
    excl = Path(args.exclude)
    if excl.exists():
        skip = {s.strip().upper() for s in excl.read_text().split() if s.strip()}
        symbols = [s for s in symbols if s not in skip]
    tfs = [t.strip() for t in args.timeframes.split(",")]
    if set(tfs) - set(WEIGHT):
        raise SystemExit(f"unknown timeframe(s); choose from {', '.join(WEIGHT)}")
    md = MarketData(symbols, sorted(set(tfs) | {"1d"}), HERE / ".cache")
    md.load_history()
    md.update()
    jobs = [(s, [(tf, md.candles(s, tf)) for tf in tfs], args.side, md.candles(s, "1d"))
            for s in md.symbols]
    with ProcessPoolExecutor(args.workers) as ex:
        rows = [r for r in ex.map(_rank_one, jobs, chunksize=25) if r]
    d = pd.DataFrame(rows)
    n_signal = int((d.score > 0).sum())

    d = d[(d.score > 0) & (d.close >= args.min_price) & (d.turnover_cr >= args.min_turnover)]
    if args.max_price:
        d = d[d.close <= args.max_price]
    # stop: the nearest swing (low for longs, high for shorts) on the right side of
    # price, looking from the shortest chosen timeframe upward
    order = [tf for tf in WEIGHT if tf in tfs]
    sw = d[[f"swing_{tf}" for tf in order]]
    ok = sw.lt(d.close, axis=0) if args.side == "long" else sw.gt(d.close, axis=0)
    d["stop"] = sw.where(ok).bfill(axis=1).iloc[:, 0]
    d = d[d.stop.notna()]
    d["stop"] = d.stop.round(2)
    d["stop_pct"] = ((d.close - d.stop).abs() / d.close * 100).round(1)
    d = d.sort_values(["score", "strength"], ascending=False).head(args.top).copy()

    lots = fno_lots()
    d["fno_lot"] = d.symbol.map(lots)
    dates = ResultsCalendar().get_many(d.symbol.tolist())
    d["results_date"] = d.symbol.map(dates)
    d["results_in_days"] = d.results_date.map(days_until)
    d["results"] = d.results_date.map(warning)

    cols = ["symbol", "score", *tfs, "close", "stop", "stop_pct", "turnover_cr", "fno_lot",
            "results", *[f"smi_{tf}" for tf in tfs], "strength", "results_date"]
    d[cols].to_csv(args.out, index=False)
    with pd.option_context("display.width", 250, "display.max_columns", 40):
        print(d[cols[:len(tfs) + 6]].to_string(index=False))
    print(f"\n{n_signal} stocks meet the {args.side} conditions on at least one timeframe; "
          f"{len(d)} shown after filters. Saved to {args.out}")


if __name__ == "__main__":
    main()
