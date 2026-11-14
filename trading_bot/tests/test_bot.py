from datetime import datetime

import pytest
from fastapi.testclient import TestClient

from bot.app import create_app, parse_chartink
from bot.broker import PaperBroker
from bot.config import Settings
from bot.engine import IST, Engine, round_tick_up, target_price
from bot.store import Store

TOKEN = "t" * 32


class Clock:
    def __init__(self):
        self.t = datetime(2026, 11, 16, 10, 0, tzinfo=IST)  # a Monday

    def __call__(self):
        return self.t


@pytest.fixture
def env(tmp_path):
    clock = Clock()
    s = Settings(webhook_token=TOKEN, db_path=str(tmp_path / "t.db"))
    broker = PaperBroker()
    eng = Engine(s, Store(s.db_path), broker, now=clock)
    return eng, broker, clock


def test_target_strictly_above_7pct():
    assert target_price(100.0, 7) == 107.05
    assert target_price(500.0, 7) > 535.0
    t = target_price(812.35, 7)
    assert t > 812.35 * 1.07 and t - 812.35 * 1.07 <= 0.05
    assert round_tick_up(100.01) == 100.05


def test_parse_chartink_json_and_form():
    body = b'{"stocks":"SBIN, infy","trigger_prices":"625.5,1650","scan_name":"x"}'
    assert parse_chartink(body) == [("SBIN", 625.5), ("INFY", 1650.0)]
    assert parse_chartink(b"stocks=TCS&trigger_prices=3900") == [("TCS", 3900.0)]
    assert parse_chartink(b"") == []


def test_full_lifecycle_25_shares_mtf(env):
    eng, broker, clock = env
    [r] = eng.handle_signal([("SBIN", 600.0)])
    assert r["action"] == "buy_placed" and r["qty"] == 25
    assert r["limit"] == 603.0  # LTP + 0.5% buffer

    eng.poll()  # LTP 600 <= limit -> filled at 600
    [t] = eng.store.by_status("OPEN")
    assert (t.product, t.quantity, t.avg_price, t.target_price) == ("MTF", 25, 600.0, 642.05)

    broker.note_price("SBIN", 641.0)
    eng.poll()
    assert eng.store.by_status("OPEN")  # 6.8% profit: hold

    broker.note_price("SBIN", 643.0)
    eng.poll()  # target hit -> sell placed
    eng.poll()  # sell fills
    [t] = eng.store.by_status("CLOSED")
    profit = (t.sell_price - t.avg_price) * t.quantity
    assert profit > 0.07 * t.avg_price * t.quantity


def test_dedupe_and_reentry(env):
    eng, broker, clock = env
    eng.handle_signal([("SBIN", 600.0)])
    [r] = eng.handle_signal([("SBIN", 600.0)])
    assert r["reason"] == "already holding / order pending"


def test_entry_window_and_weekend(env):
    eng, broker, clock = env
    clock.t = clock.t.replace(hour=9, minute=16)
    assert eng.handle_signal([("SBIN", 600.0)])[0]["reason"] == "outside entry window"
    clock.t = datetime(2026, 11, 14, 11, 0, tzinfo=IST)  # Saturday
    assert eng.handle_signal([("SBIN", 600.0)])[0]["reason"] == "outside entry window"


def test_risk_limits(env):
    eng, broker, clock = env
    eng.s.max_open_trades = 2
    res = eng.handle_signal([("A", 100.0), ("B", 100.0), ("C", 100.0)])
    assert [r["action"] for r in res] == ["buy_placed", "buy_placed", "skipped"]
    eng.s.max_order_value = 1000
    eng.s.max_open_trades = 10
    assert "MAX_ORDER_VALUE" in eng.handle_signal([("D", 100.0)])[0]["reason"]


def test_capital_sizing_with_leverage(env):
    eng, broker, clock = env
    eng.s.sizing_mode, eng.s.capital_per_trade, eng.s.leverage = "capital", 10000, 3.05
    [r] = eng.handle_signal([("X", 500.0)])
    assert r["qty"] == 60  # floor(10000 * 3.05 / 502.5)


def test_paused(env):
    eng, broker, clock = env
    eng.paused = True
    assert eng.handle_signal([("SBIN", 600.0)])[0]["reason"] == "bot paused"


def test_rejected_buy_marks_failed(env):
    eng, broker, clock = env
    eng.handle_signal([("SBIN", 600.0)])
    broker._orders["PAPER000001"]["status"] = "REJECTED"
    eng.poll()
    assert eng.store.by_status("FAILED")


def test_webhook_auth_and_flow(env):
    eng, broker, clock = env
    eng.s.signal_source = "chartink"
    client = TestClient(create_app(eng, start_monitor=False))
    assert client.post("/webhook/chartink?token=bad", json={}).status_code == 403
    r = client.post(f"/webhook/chartink?token={TOKEN}",
                    json={"stocks": "SBIN", "trigger_prices": "600"})
    assert r.status_code == 200 and r.json() == {"received": ["SBIN"]}
    assert eng.store.by_status("BUY_PENDING")
    assert client.get("/health").json()["dry_run"] is True


def test_dashboard_and_pause_buttons(env):
    eng, broker, clock = env
    eng.handle_signal([("SBIN", 600.0)])
    client = TestClient(create_app(eng, start_monitor=False))
    assert client.get("/dashboard?token=bad").status_code == 403
    page = client.get(f"/dashboard?token={TOKEN}").text
    assert "SBIN" in page and "PAPER" in page and "Pause new buys" in page
    r = client.post(f"/pause?token={TOKEN}&ui=1", follow_redirects=False)
    assert r.status_code == 303 and eng.paused
    assert "Resume new buys" in client.get(f"/dashboard?token={TOKEN}").text
    assert client.post(f"/resume?token={TOKEN}").json() == {"paused": False}


def test_webhook_ignored_when_using_builtin_scanner(env):
    eng, broker, clock = env
    client = TestClient(create_app(eng, start_monitor=False))
    r = client.post(f"/webhook/chartink?token={TOKEN}", json={"stocks": "SBIN"})
    assert "ignored" in r.json() and not eng.store.by_status("BUY_PENDING")
