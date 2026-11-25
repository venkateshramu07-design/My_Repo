"""NSE price data from Yahoo Finance (symbols get the ``.NS`` suffix)."""

from __future__ import annotations

import time
from pathlib import Path

import pandas as pd
import yfinance as yf

UNIVERSE_DIR = Path(__file__).parent / "universe"


def load_universe(name_or_symbols: str) -> list[str]:
    """``nifty50`` / ``nifty100`` / ``nifty500``, a path to a text file,
    or a comma-separated list such as ``SWIGGY,RELIANCE``."""
    f = UNIVERSE_DIR / f"{name_or_symbols}.txt"
    if not f.exists():
        f = Path(name_or_symbols)
    if f.exists():
        return [s.strip().upper() for s in f.read_text().split() if s.strip()]
    return [s.strip().upper() for s in name_or_symbols.split(",") if s.strip()]


def _drop_open_bar(df: pd.DataFrame, interval: str) -> pd.DataFrame:
    """Drop the current, still-forming bar so signals use closed bars only.

    Weekly: the bar for this week is complete after Friday's close (15:30 IST).
    """
    if df.empty:
        return df
    now = pd.Timestamp.now(tz="Asia/Kolkata")
    last = df.index[-1]
    if interval == "1wk":
        week_end = (last + pd.Timedelta(days=4)).tz_localize(None).normalize() \
            + pd.Timedelta(hours=15, minutes=30)
        if now.tz_localize(None) < week_end:
            return df.iloc[:-1]
    elif interval == "1d":
        close_time = last.tz_localize(None).normalize() + pd.Timedelta(hours=15, minutes=30)
        if now.tz_localize(None) < close_time:
            return df.iloc[:-1]
    return df


def fetch(symbols: list[str], interval: str = "1wk", period: str = "5y",
          include_open_bar: bool = False, chunk: int = 50) -> dict[str, pd.DataFrame]:
    out: dict[str, pd.DataFrame] = {}
    for start in range(0, len(symbols), chunk):
        batch = symbols[start:start + chunk]
        tickers = [f"{s}.NS" for s in batch]
        raw = yf.download(tickers, period=period, interval=interval, group_by="ticker",
                          auto_adjust=False, progress=False, threads=True)
        for s, t in zip(batch, tickers):
            try:
                df = raw[t] if isinstance(raw.columns, pd.MultiIndex) else raw
                df =df[["Open", "High", "Low", "Close", "Volume"]].dropna()
            except (KeyError, ValueError):
                continue
            if df.index.tz is not None:
                df.index = df.index.tz_localize(None)
            if not include_open_bar:
                df = _drop_open_bar(df, interval)
            if len(df):
                out[s] = df
        if start + chunk < len(symbols):
            time.sleep(1)
    return out
