"""Indicators used by the strategy shown in the video.

* Stochastic Momentum Index (SMI) with Yahoo Finance / ChartIQ settings
  "Stch Mtm (10,3,3,10,ema)": %K periods 10, %K smoothing 3,
  %K double smoothing 3, %D periods 10, %D moving average = exponential.
* GoNoGo Trend: an approximation (see ``gonogo_trend``).
* Swing (pivot) lows and highs, used for the "previous low vs current low" check.
"""

from __future__ import annotations

import numpy as np
import pandas as pd


def ema(series: pd.Series, span: int) -> pd.Series:
    return series.ewm(span=span, adjust=False).mean()


def smi(df: pd.DataFrame, k_period: int = 10, k_smooth: int = 3,
        k_double_smooth: int = 3, d_period: int = 10) -> pd.DataFrame:
    """Stochastic Momentum Index. Returns columns ``smi_k`` and ``smi_d``."""
    hh = df["High"].rolling(k_period).max()
    ll = df["Low"].rolling(k_period).min()
    rel = df["Close"] - (hh + ll) / 2.0           # distance from range midpoint
    rng = hh - ll                                  # full range

    rel_s = ema(ema(rel.dropna(), k_smooth), k_double_smooth)
    rng_s = ema(ema(rng.dropna(), k_smooth), k_double_smooth)
    k = 100.0 * rel_s / (rng_s / 2.0)
    k = k.replace([np.inf, -np.inf], np.nan)
    d = ema(k.dropna(), d_period)
    out = pd.DataFrame(index=df.index)
    out["smi_k"] = k
    out["smi_d"] = d
    return out


def rsi(close: pd.Series, period: int = 14) -> pd.Series:
    delta = close.diff()
    gain = delta.clip(lower=0).ewm(alpha=1 / period, adjust=False).mean()
    loss = (-delta.clip(upper=0)).ewm(alpha=1 / period, adjust=False).mean()
    rs = gain / loss.replace(0, np.nan)
    return 100 - 100 / (1 + rs)


def gonogo_trend(df: pd.DataFrame) -> pd.DataFrame:
    """Approximation of the GoNoGo Trend overlay.

    The original GoNoGo Trend (GoNoGo Charts) blends trend, momentum and
    volume into one colour per bar. Its exact weights are not public, so this
    prototype scores five common components, each +1 (bullish) or -1 (bearish):

      1. Heikin-Ashi close above / below its 21 EMA
      2. 12 EMA above / below 26 EMA
      3. MACD line above / below its 9 signal line
      4. RSI(14) above / below 50
      5. OBV above / below its 21 EMA

    score >= +3  -> "Go"    (bullish bar)
    score <= -3  -> "NoGo"  (bearish bar)
    otherwise    -> "Neutral" (amber bar)

    Compare it with the chart before trusting it with real money.
    """
    o, h, l, c, v = (df[x] for x in ("Open", "High", "Low", "Close", "Volume"))
    ha_close = (o + h + l + c) / 4.0

    ema12, ema26 = ema(c, 12), ema(c, 26)
    macd = ema12 - ema26
    signal = ema(macd, 9)
    obv = (np.sign(c.diff()).fillna(0) * v).cumsum()

    parts = [
        np.sign(ha_close - ema(ha_close, 21)),
        np.sign(ema12 - ema26),
        np.sign(macd - signal),
        np.sign(rsi(c) - 50),
        np.sign(obv - ema(obv, 21)),
    ]
    score = sum(parts).astype(float)
    state = np.where(score >= 3, "Go", np.where(score <= -3, "NoGo", "Neutral"))
    out = pd.DataFrame(index=df.index)
    out["gonogo_score"] = score
    out["gonogo"] = state
    out.loc[out.index[:26], "gonogo"] = "Neutral"   # not enough history yet
    return out


def pivots(df: pd.DataFrame, left: int = 2, right: int = 2) -> pd.DataFrame:
    """Mark swing lows / highs.

    A bar is a swing low when its low is the lowest of the ``left`` bars before
    and the ``right`` bars after it. A pivot is only known ``right`` bars later,
    so ``*_confirmed_at`` holds the index position where it becomes known
    (no look-ahead in scans or backtests).
    """
    low, high = df["Low"], df["High"]

    def strict_extreme(s: pd.Series, better) -> pd.Series:
        ok = pd.Series(True, index=s.index)
        for k in range(1, left + 1):
            ok &= better(s, s.shift(k))
        for k in range(1, right + 1):
            ok &= better(s, s.shift(-k))
        return ok.fillna(False).astype(bool)

    out = pd.DataFrame(index=df.index)
    out["pivot_low"] = strict_extreme(low, lambda a, b: a < b)
    out["pivot_high"] = strict_extreme(high, lambda a, b: a > b)
    return out


def last_two_pivots(values: np.ndarray, flags: np.ndarray, upto: int, right: int):
    """Return (previous, current) pivot values confirmed by bar ``upto``."""
    idx = np.flatnonzero(flags[: max(0, upto - right + 1)])
    if len(idx) < 2:
        return None, None
    return float(values[idx[-2]]), float(values[idx[-1]])
