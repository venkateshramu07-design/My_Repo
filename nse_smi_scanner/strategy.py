"""Entry rules from the video, evaluated on closed bars (weekly by default).

SHORT when all hold on the latest closed bar:
  1. SMI %K (black line) < -40  AND  SMI %D (red line) < -40
  2. Lower low: the current swing low is below the previous swing low
  3. GoNoGo Trend shows "NoGo"

LONG when all hold:
  1. SMI %K > +40  AND  SMI %D > +40
  2. Higher low: the current swing low is above the previous swing low
  3. GoNoGo Trend shows "Go"

A signal is "fresh" on the first bar where all conditions become true, so a
stock is not entered again every week the conditions stay true.

Exits are not covered in the video. This prototype assumes:
  * stop loss at the current swing high (shorts) / current swing low (longs)
  * exit when %K crosses back through %D against the trade, or GoNoGo flips.
"""

from __future__ import annotations

from dataclasses import dataclass, asdict

import numpy as np
import pandas as pd

from indicators import gonogo_trend, last_two_pivots, pivots, smi


@dataclass
class Params:
    level: float = 40.0          # SMI threshold (+/-)
    pivot_left: int = 2          # bars on each side that define a swing low/high
    pivot_right: int = 2
    use_gonogo: bool = True      # require GoNoGo confirmation


def add_indicators(df: pd.DataFrame, p: Params) -> pd.DataFrame:
    df = df.join(smi(df)).join(gonogo_trend(df)).join(
        pivots(df, p.pivot_left, p.pivot_right))
    return df


def conditions_at(df: pd.DataFrame, i: int, p: Params) -> dict:
    """Evaluate every rule on bar position ``i`` without looking ahead."""
    row = df.iloc[i]
    k, d = row["smi_k"], row["smi_d"]
    prev_low, cur_low = last_two_pivots(df["Low"].values, df["pivot_low"].values,
                                        i, p.pivot_right)
    prev_high, cur_high = last_two_pivots(df["High"].values, df["pivot_high"].values,
                                          i, p.pivot_right)
    ok = not (np.isnan(k) or np.isnan(d) or cur_low is None)
    lower_low = ok and cur_low < prev_low
    higher_low = ok and cur_low > prev_low
    gng = row["gonogo"]

    short = ok and k < -p.level and d < -p.level and lower_low and \
        (gng == "NoGo" or not p.use_gonogo)
    long = ok and k > p.level and d > p.level and higher_low and \
        (gng == "Go" or not p.use_gonogo)
    return {
        "smi_k": round(float(k), 2) if not np.isnan(k) else None,
        "smi_d": round(float(d), 2) if not np.isnan(d) else None,
        "gonogo": gng,
        "prev_swing_low": prev_low, "cur_swing_low": cur_low,
        "cur_swing_high": cur_high,
        "lower_low": bool(lower_low), "higher_low": bool(higher_low),
        "long": bool(long), "short": bool(short),
    }


@dataclass
class Signal:
    symbol: str
    date: str
    side: str            # LONG / SHORT
    fresh: bool          # conditions became true on this bar
    close: float
    stop: float | None
    smi_k: float
    smi_d: float
    gonogo: str
    prev_swing_low: float
    cur_swing_low: float

    def as_dict(self):
        return asdict(self)


def evaluate(symbol: str, df: pd.DataFrame, p: Params) -> Signal | None:
    """Return a signal for the latest closed bar, or None."""
    if len(df) < 30:
        return None
    df = add_indicators(df, p)
    now = conditions_at(df, len(df) - 1, p)
    before = conditions_at(df, len(df) - 2, p)
    for side in ("long", "short"):
        if now[side]:
            stop = now["cur_swing_low"] if side == "long" else now["cur_swing_high"]
            return Signal(
                symbol=symbol, date=str(df.index[-1].date()), side=side.upper(),
                fresh=not before[side], close=round(float(df["Close"].iloc[-1]), 2),
                stop=round(stop, 2) if stop else None,
                smi_k=now["smi_k"], smi_d=now["smi_d"], gonogo=now["gonogo"],
                prev_swing_low=round(now["prev_swing_low"], 2),
                cur_swing_low=round(now["cur_swing_low"], 2))
    return None
