"""Upcoming quarterly results dates, from Yahoo Finance (cached for the day).

Yahoo's future dates are often estimates based on last year's pattern; the
company confirms the board-meeting date only 1-2 weeks ahead (NSE: Corporate
Filings -> Event Calendar). Treat a date as a warning, not a fact.
"""

from __future__ import annotations

import datetime as dt
import json
import logging
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

import pandas as pd
import yfinance as yf

log = logging.getLogger("events")
CACHE_DIR = Path(__file__).parent / ".cache"
WARN_DAYS = 14          # warn when results are this close


def _today() -> dt.date:
    return pd.Timestamp.now(tz="Asia/Kolkata").date()


def _lookup(symbol: str, ticker: str) -> str | None:
    """Next results date on or after today as YYYY-MM-DD, or None if unknown.

    When Yahoo lists no future date but the company reported within the last
    ~4 months, the next date is estimated as last results + 91 days and marked
    with a trailing "*" (e.g. "2027-01-28*").
    """
    today = _today()
    try:
        cal = yf.Ticker(ticker).calendar or {}
        dates = [d for d in cal.get("Earnings Date") or [] if d >= today]
        if dates:
            return str(min(dates))
    except Exception:
        pass
    try:
        ed = yf.Ticker(ticker).get_earnings_dates(limit=8)
        if ed is not None and len(ed):
            all_d = [i.tz_convert("Asia/Kolkata").date() for i in ed.index]
            fut = [d for d in all_d if d >= today]
            if fut:
                return str(min(fut))
            last = max(all_d)
            if (today - last).days <= 125:
                est = last + dt.timedelta(days=91)
                while est < today:
                    est += dt.timedelta(days=91)
                return f"{est}*"
    except Exception:
        pass
    return None


class ResultsCalendar:
    def __init__(self, suffix: str = ".NS"):
        self.suffix = suffix
        CACHE_DIR.mkdir(exist_ok=True)
        self.path = CACHE_DIR / f"results_{_today():%Y%m%d}.json"
        try:
            self.data: dict[str, str | None] = json.loads(self.path.read_text())
        except (OSError, ValueError):
            self.data = {}

    def get_many(self, symbols: list[str]) -> dict[str, str | None]:
        todo = [s for s in symbols if s not in self.data]
        if todo:
            with ThreadPoolExecutor(8) as ex:
                for s, d in zip(todo, ex.map(lambda s: _lookup(s, s + self.suffix), todo)):
                    self.data[s] = d
            self.path.write_text(json.dumps(self.data))
        return {s: self.data.get(s) for s in symbols}

    def get(self, symbol: str) -> str | None:
        return self.get_many([symbol])[symbol]


def days_until(date: str | None) -> int | None:
    if not isinstance(date, str) or not date:
        return None
    return (dt.date.fromisoformat(date.rstrip("*")) - _today()).days


def warning(date: str | None) -> str:
    """Short text for reports and alerts."""
    n = days_until(date)
    if n is None:
        return "results date unknown"
    est = " estimated" if date.endswith("*") else ""
    day = date.rstrip("*")
    if n <= WARN_DAYS:
        return f"⚠️ results in {n} day{'s' if n != 1 else ''} ({day}{est})"
    return f"results {day}{est} (in {n} days)"
