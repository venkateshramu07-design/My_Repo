#!/usr/bin/env python3
"""Scan NSE stocks for the SMI + swing-low + GoNoGo setup and (optionally) enter.

Examples
  python scan.py                                # Nifty 50, weekly, report only
  python scan.py --universe nifty500            # wider list
  python scan.py --universe SWIGGY,RELIANCE     # specific stocks
  python scan.py --trade                        # record paper trades for fresh signals
  python scan.py --trade --live                 # real orders via Zerodha Kite (careful)
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path

import pandas as pd

from broker import KiteBroker, PaperBroker, position_size
from data import fetch, load_universe
from strategy import Params, evaluate

HERE = Path(__file__).parent


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--universe", default="nifty50")
    ap.add_argument("--interval", default="1wk", choices=["1wk", "1d"],
                    help="timeframe for the signal (video uses weekly)")
    ap.add_argument("--level", type=float, default=40.0, help="SMI threshold, default 40")
    ap.add_argument("--no-gonogo", action="store_true",
                    help="skip the GoNoGo Trend confirmation")
    ap.add_argument("--include-open-bar", action="store_true",
                    help="also use the unfinished current bar (signals may change)")
    ap.add_argument("--all", action="store_true",
                    help="list stocks where the setup is still active, not only fresh ones")
    ap.add_argument("--trade", action="store_true", help="enter fresh signals")
    ap.add_argument("--live", action="store_true", help="real orders via Zerodha Kite")
    ap.add_argument("--allow-intraday-shorts", action="store_true")
    ap.add_argument("--capital-per-trade", type=float, default=50_000)
    ap.add_argument("--risk-per-trade", type=float, default=1_000,
                    help="max rupee loss to the stop per trade")
    ap.add_argument("--out", default=str(HERE / "signals.csv"))
    args = ap.parse_args()

    p = Params(level=args.level, use_gonogo=not args.no_gonogo)
    symbols = load_universe(args.universe)
    print(f"Scanning {len(symbols)} NSE stocks on {args.interval} bars ...")
    data = fetch(symbols, interval=args.interval, include_open_bar=args.include_open_bar)
    missing = sorted(set(symbols) - set(data))
    if missing:
        print(f"No data for {len(missing)}: {', '.join(missing[:15])}"
              f"{' ...' if len(missing) > 15 else ''}")

    signals = [s for s in (evaluate(sym, df, p) for sym, df in data.items()) if s]
    if not args.all:
        signals = [s for s in signals if s.fresh]

    if not signals:
        print("No stock meets all conditions on the latest closed bar.")
        return
    table = pd.DataFrame([s.as_dict() for s in signals]).sort_values(["side", "symbol"])
    print(table.to_string(index=False))
    table.to_csv(args.out, index=False)
    print(f"\nSaved {len(table)} signal(s) to {args.out}")

    if not args.trade:
        return
    broker = KiteBroker(args.allow_intraday_shorts) if args.live \
        else PaperBroker(HERE / "paper_trades.csv")
    for s in signals:
        if not s.fresh:
            continue
        qty = position_size(args.capital_per_trade, args.risk_per_trade, s.close, s.stop)
        if qty == 0:
            print(f"{s.symbol}: quantity 0, skipped")
            continue
        result = broker.place(s.symbol, s.side, qty, s.close, s.stop, s.date)
        print(f"{s.side:5} {s.symbol:12} qty {qty:5}  @ ~{s.close}  stop {s.stop}  -> {result}")


if __name__ == "__main__":
    main()
