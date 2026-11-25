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
    date: str            # start time of the bar that produced the signal
    side: str            # LONG / SHORT
    fresh: bool          # conditions became true on this bar
    close: float
    stop: float | None
    smi_k: float
    smi_d: float
    gonogo: str
    prev_swing_low: float
    cur_swing_low: float

    timeframe: str = "1wk"
    market: str = "NSE"
    currency: str = "₹"

    def as_dict(self):
        return asdict(self)


MIN_BARS = 25        # SMI(10,3,3) + %D(10) need about this many bars
MAX_BARS = 400       # older bars barely change the EMAs; trimming keeps scans fast


def evaluate(symbol: str, df: pd.DataFrame, p: Params,
             timeframe: str = "1wk") -> Signal | None:
    """Return a signal for the last bar of ``df`` (pass closed bars only), or None."""
    if len(df) < MIN_BARS:
        return None
    df = add_indicators(df.iloc[-MAX_BARS:], p)
    now = conditions_at(df, len(df) - 1, p)
    before = conditions_at(df, len(df) - 2, p)
    for side in ("long", "short"):
        if now[side]:
            stop = now["cur_swing_low"] if side == "long" else now["cur_swing_high"]
            return Signal(
                symbol=symbol, date=_fmt(df.index[-1], timeframe), side=side.upper(),
                fresh=not before[side], close=px(float(df["Close"].iloc[-1])),
                stop=px(stop) if stop else None,
                smi_k=now["smi_k"], smi_d=now["smi_d"], gonogo=now["gonogo"],
                prev_swing_low=px(now["prev_swing_low"]),
                cur_swing_low=px(now["cur_swing_low"]), timeframe=timeframe)
    return None


def px(v: float) -> float:
    """Round a price sensibly: 2 decimals for stocks and gold, 5 for EURUSD-style quotes."""
    a = abs(v)
    return round(v, 2 if a >= 100 else 3 if a >= 10 else 5)


def _fmt(ts: pd.Timestamp, timeframe: str) -> str:
    return str(ts.date()) if timeframe in ("1d", "1wk", "1mo") else ts.strftime("%Y-%m-%d %H:%M")
