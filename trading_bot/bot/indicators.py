"""Indicators and candle resampling for the built-in scanner (pure Python)."""
from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from typing import Sequence

SESSION_START_MIN = 9 * 60 + 15  # NSE opens 09:15 IST; Chartink aligns candles to it


@dataclass
class Candle:
    ts: datetime  # candle start time, IST
    open: float
    high: float
    low: float
    close: float
    volume: float


def ema(values: Sequence[float], period: int) -> list[float]:
    """Exponential moving average (alpha = 2/(n+1)), seeded with the first value."""
    if not values:
        return []
    alpha = 2 / (period + 1)
    out = [float(values[0])]
    for v in values[1:]:
        out.append(alpha * v + (1 - alpha) * out[-1])
    return out


def smi(candles: Sequence[Candle], k_len: int = 10, s1: int = 3, s2: int = 3,
        d_len: int = 10) -> tuple[list[float], list[float]]:
    """Stochastic Momentum Index (Blau): returns (%K, %D) series.

    %K = 100 * EMA(EMA(close - mid, s1), s2) / (0.5 * EMA(EMA(HH - LL, s1), s2))
    where HH/LL are the highest high / lowest low of the last k_len bars and
    mid = (HH + LL) / 2.   %D = EMA(%K, d_len).
    Chartink: Smi %K(10,3,3) and Smi %D(10,3,3,10).
    """
    rel, rng = [], []
    for i, c in enumerate(candles):
        window = candles[max(0, i - k_len + 1): i + 1]
        hh = max(x.high for x in window)
        ll = min(x.low for x in window)
        rel.append(c.close - (hh + ll) / 2)
        rng.append(hh - ll)
    num = ema(ema(rel, s1), s2)
    den = ema(ema(rng, s1), s2)
    k = [100 * a / (0.5 * b) if b else 0.0 for a, b in zip(num, den)]
    return k, ema(k, d_len)


def resample(candles: Sequence[Candle], minutes: int) -> list[Candle]:
    """Combine 15-minute candles into `minutes`-long candles aligned to 09:15.

    60 -> 09:15, 10:15, ... 15:15     240 -> 09:15-13:15, 13:15-15:30
    """
    out: list[Candle] = []
    key_prev = None
    for c in candles:
        m = c.ts.hour * 60 + c.ts.minute - SESSION_START_MIN
        key = (c.ts.date(), m // minutes)
        if key != key_prev:
            start = c.ts.replace(hour=(SESSION_START_MIN + key[1] * minutes) // 60,
                                 minute=(SESSION_START_MIN + key[1] * minutes) % 60)
            out.append(Candle(start, c.open, c.high, c.low, c.close, c.volume))
            key_prev = key
        else:
            last = out[-1]
            last.high = max(last.high, c.high)
            last.low = min(last.low, c.low)
            last.close = c.close
            last.volume += c.volume
    return out
