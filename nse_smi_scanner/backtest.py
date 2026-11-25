#!/usr/bin/env python3
"""Walk-forward backtest of the video's rules on past data.

Entry: next bar's open after a fresh signal on a closed bar.
Exit (assumption, not from the video): stop at the swing low/high, or on the
close of the bar where %K crosses %D against the trade or GoNoGo flips.

  python backtest.py --universe nifty50 --period 10y
"""

from __future__ import annotations

import argparse

import pandas as pd

from data import fetch, load_universe
from strategy import Params, add_indicators, conditions_at


def backtest_symbol(sym: str, df: pd.DataFrame, p: Params) -> list[dict]:
    df = add_indicators(df, p)
    trades, pos = [], None
    prev = {"long": False, "short": False}
    for i in range(30, len(df) - 1):
        bar = df.iloc[i]
        if pos:
            side, entry, stop = pos["side"], pos["entry"], pos["stop"]
            exit_px = None
            if side == "LONG" and stop and bar["Low"] <= stop:
                exit_px = min(bar["Open"], stop)
            elif side == "SHORT" and stop and bar["High"] >= stop:
                exit_px = max(bar["Open"], stop)
            elif side == "LONG" and (bar["smi_k"] < bar["smi_d"] or bar["gonogo"] == "NoGo"):
                exit_px = bar["Close"]
            elif side == "SHORT" and (bar["smi_k"] > bar["smi_d"] or bar["gonogo"] == "Go"):
                exit_px = bar["Close"]
            if exit_px is not None:
                ret = (exit_px / entry - 1) * (1 if side == "LONG" else -1)
                trades.append({**pos, "exit_date": df.index[i].date(),
                               "exit": round(exit_px, 2), "return_pct": round(100 * ret, 2)})
                pos = None
        c = conditions_at(df, i, p)
        if pos is None:
            for side in ("long", "short"):
                if c[side] and not prev[side]:
                    stop = c["cur_swing_low"] if side == "long" else c["cur_swing_high"]
                    pos = {"symbol": sym, "side": side.upper(),
                           "entry_date": df.index[i + 1].date(),
                           "entry": round(float(df["Open"].iloc[i + 1]), 2), "stop": stop}
                    break
        prev = {"long": c["long"], "short": c["short"]}
    return trades


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--universe", default="nifty50")
    ap.add_argument("--interval", default="1wk", choices=["1wk", "1d"])
    ap.add_argument("--period", default="10y")
    ap.add_argument("--no-gonogo", action="store_true")
    ap.add_argument("--out", default="backtest_trades.csv")
    args = ap.parse_args()

    p = Params(use_gonogo=not args.no_gonogo)
    data = fetch(load_universe(args.universe), interval=args.interval, period=args.period)
    trades = [t for s, df in data.items() for t in backtest_symbol(s, df, p)]
    if not trades:
        print("No trades.")
        return
    t = pd.DataFrame(trades)
    t.to_csv(args.out, index=False)
    for side, g in [("ALL", t), *t.groupby("side")]:
        r = g["return_pct"]
        print(f"{side:5} trades {len(g):4}  win rate {100 * (r > 0).mean():5.1f}%  "
              f"avg {r.mean():6.2f}%  median {r.median():6.2f}%  "
              f"best {r.max():7.2f}%  worst {r.min():7.2f}%")
    print(f"Saved trade list to {args.out}")


if __name__ == "__main__":
    main()
