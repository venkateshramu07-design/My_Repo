import json
from datetime import datetime, timedelta

import pytest

from bot.config import Settings
from bot.engine import IST, Engine
from bot.indicators import Candle, ema, resample, smi
from bot.broker import PaperBroker
from bot.scanner import Scanner, parse_candles
from bot.store import Store

NOW = datetime(2026, 11, 16, 11, 50, tzinfo=IST)  # Monday


def session_bars(day, start_price, step, n=25):
    """n 15-minute candles from 09:15 on `day`, price moving by `step` each bar."""
    out, p = [], start_price
    t = datetime.combine(day, datetime.min.time(), IST).replace(hour=9, minute=15)
    for i in range(n):
        out.append(Candle(t + timedelta(minutes=15 * i), p, p + abs(step) + 1, p - 1, p + step, 10000))
        p += step
    return out


def test_ema_matches_pandas():
    pd = pytest.importorskip("pandas")
    vals = [1, 5, 3, 8, 2, 9, 4, 7]
    assert ema(vals, 3) == pytest.approx(pd.Series(vals).ewm(span=3, adjust=False).mean().tolist())


def test_smi_matches_independent_pandas_version():
    pd = pytest.importorskip("pandas")
    import random
    random.seed(1)
    bars, p = [], 500.0
    t = datetime(2026, 1, 1, 9, 15, tzinfo=IST)
    for i in range(300):
        o = p; p = max(1, p + random.uniform(-5, 5.2))
        bars.append(Candle(t + timedelta(minutes=15 * i), o, max(o, p) + random.random() * 2,
                           min(o, p) - random.random() * 2, p, 1000))
    df = pd.DataFrame({"h": [b.high for b in bars], "l": [b.low for b in bars], "c": [b.close for b in bars]})
    hh = df.h.rolling(10, min_periods=1).max(); ll = df.l.rolling(10, min_periods=1).min()
    e = lambda s, n: s.ewm(span=n, adjust=False).mean()
    k_ref = 100 * e(e(df.c - (hh + ll) / 2, 3), 3) / (0.5 * e(e(hh - ll, 3), 3))
    d_ref = e(k_ref, 10)
    k, d = smi(bars)
    assert k == pytest.approx(k_ref.tolist()) and d == pytest.approx(d_ref.tolist())


def test_smi_extremes():
    up = session_bars(NOW.date(), 100, 1, 60)
    down = session_bars(NOW.date(), 200, -1, 60)
    assert smi(up)[0][-1] > 70 and smi(down)[0][-1] < -70


def test_resample_aligned_to_0915():
    bars = session_bars(NOW.date(), 100, 1, 25)  # 09:15 .. 15:15 (full session)
    h1 = resample(bars, 60)
    assert [b.ts.strftime("%H:%M") for b in h1] == ["09:15", "10:15", "11:15", "12:15", "13:15", "14:15", "15:15"]
    h4 = resample(bars, 240)
    assert [b.ts.strftime("%H:%M") for b in h4] == ["09:15", "13:15"]
    assert h4[0].volume == 16 * 10000 and h4[1].volume == 9 * 10000
    assert h4[0].open == bars[0].open and h4[0].close == bars[15].close
    assert h4[0].high == max(b.high for b in bars[:16])


def test_parse_candles_formats():
    rows = [["2026-11-16T09:15:00", 1, 2, 0.5, 1.5, 100, None],
            ["2026-11-16 09:30:00", 1, 2, 0.5, 1.5, 100],
            [1794800700, 1, 2, 0.5, 1.5, None]]
    c = parse_candles(rows)
    assert c[0].ts.hour == 9 and c[1].ts.minute == 30 and c[2].volume == 0
    assert all(x.ts.tzinfo is not None for x in c)


class FakeData:
    """Stand-in for GrowwBroker market data.

    Every stock trades sideways for weeks; for the last week UP trends up, DOWN trends down,
    BIGPX trends up but trades above 1000, TINY has a small market cap.
    """

    BASE = {"UP": 300.0, "DOWN": 300.0, "BIGPX": 1500.0, "TINY": 300.0}

    def equity_symbols(self):
        return list(self.BASE)

    def quote(self, sym):
        return {"last_price": self.BASE[sym], "market_cap_cr": 500 if sym == "TINY" else 50000}

    def candles_15m(self, sym, start, end):
        import math
        rows, i, p = [], 0, self.BASE[sym]
        day = datetime(2026, 9, 1).date()
        while day <= end.date():
            if day.weekday() < 5:
                n = 25 if day < end.date() else 11  # today: 09:15..11:45
                for j in range(n):
                    t = datetime.combine(day, datetime.min.time(), IST).replace(hour=9, minute=15) \
                        + timedelta(minutes=15 * j)
                    o = p
                    if (end.date() - day).days <= 8:  # trend over the last week
                        p *= 0.997 if sym == "DOWN" else 1.003
                    else:
                        p = self.BASE[sym] * (1 + 0.02 * math.sin(i / 9))
                    i += 1
                    if start <= t <= end:
                        rows.append([t.isoformat(), o, max(o, p) * 1.002, min(o, p) * 0.998, p, 10000])
            day += timedelta(days=1)
        return rows


@pytest.fixture
def scanner(tmp_path):
    s = Settings(webhook_token="t" * 32, db_path=str(tmp_path / "t.db"),
                 universe_file=str(tmp_path / "u.json"))
    eng = Engine(s, Store(s.db_path), PaperBroker(), now=lambda: NOW)
    return Scanner(s, FakeData(), eng, now=lambda: NOW)


def test_universe_filters_market_cap_and_is_cached(scanner):
    scanner.load_universe()
    assert set(scanner.universe) == {"UP", "DOWN", "BIGPX"}
    saved = json.load(open(scanner.s.universe_file))
    assert set(saved["symbols"]) == {"UP", "DOWN", "BIGPX"}
    scanner.data.equity_symbols = lambda: pytest.fail("should use cache")
    scanner.universe = {}
    scanner.load_universe()
    assert "UP" in scanner.universe


def test_scan_finds_only_matching_stock_and_trades_it(scanner):
    matches = scanner.scan()
    assert [m[0] for m in matches] == ["UP"]  # DOWN: SMI negative; BIGPX: close >= 1000
    r = scanner.evaluate("UP", scanner.history["UP"])
    assert r["volume"] == 11 * 10000
    for tf in ("4 hour", "1 hour", "15 minute"):
        assert r[tf]["K"] > 40 and r[tf]["D"] > 40 and r[tf]["K"] >= r[tf]["D"]
    assert r["pass"]
    scanner.engine.handle_signal(matches)
    assert scanner.engine.store.by_status("BUY_PENDING")[0].symbol == "UP"


def test_low_volume_fails(scanner):
    scanner.s.min_volume = 200000
    assert scanner.scan() == []


def test_stale_data_is_rejected(scanner):
    old = session_bars(NOW.date() - timedelta(days=3), 100, 1, 80)
    assert scanner.evaluate("X", old)["reason"] == "not enough / stale data"


def test_market_cap_unit_detection():
    from unittest import mock
    from bot.broker import GrowwBroker
    b = GrowwBroker.__new__(GrowwBroker)
    b._G = mock.Mock()
    for raw, expected in ((1.9e13, 1.9e6), (1.9e6, 1.9e6)):  # rupees vs crores
        b._call = lambda fn, raw=raw: {"last_price": 1400.0, "market_cap": raw}
        assert b.quote("RELIANCE")["market_cap_cr"] == pytest.approx(expected)
