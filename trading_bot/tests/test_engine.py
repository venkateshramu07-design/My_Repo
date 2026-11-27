import sys
from datetime import datetime
from pathlib import Path

import pytest

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

import engine as engine_mod  # noqa: E402
from app import create_app  # noqa: E402
from broker import IST, PaperBroker  # noqa: E402
from config import Config  # noqa: E402
from store import Store  # noqa: E402

SECRET = "test-secret-1234567890"


def make_cfg(**over):
    base = dict(
        dry_run=True, trading_enabled=True, webhook_secret=SECRET, groww_api_key="", groww_totp_secret="",
        groww_api_secret="", default_side="BUY", capital_per_trade=10000, stop_loss_pct=1.0, target_pct=2.0,
        max_trades_per_day=2, min_price=20, max_price=5000,
        entry_start=datetime.strptime("09:20", "%H:%M").time(),
        entry_end=datetime.strptime("14:45", "%H:%M").time(),
        square_off_time=datetime.strptime("15:05", "%H:%M").time(),
        telegram_bot_token="", telegram_chat_id="", db_path=":memory:",
    )
    base.update(over)
    return Config(**base)


@pytest.fixture
def at(monkeypatch):
    def set_time(hh, mm, day=27):  # 27 Nov 2026 is a Friday
        monkeypatch.setattr(engine_mod, "now_ist", lambda: datetime(2026, 11, day, hh, mm, tzinfo=IST))
    set_time(10, 30)
    return set_time


def setup(**over):
    cfg = make_cfg(**over)
    broker, store = PaperBroker(cfg), Store(":memory:")
    app = create_app(cfg, broker, store, start_scheduler=False, background=False)
    return app, app.test_client(), broker, store


ALERT = {"stocks": "TATASTEEL,INFY,RELIANCE", "trigger_prices": "152.3,1490.5,2900", "scan_name": "Breakout"}


def test_wrong_secret_rejected(at):
    _, c, broker, _ = setup()
    assert c.post("/webhook/wrong", json=ALERT).status_code == 404
    assert broker.orders == []


def test_buys_with_sl_and_target_and_respects_daily_limit(at):
    _, c, broker, store = setup()
    r = c.post(f"/webhook/{SECRET}", json=ALERT).get_json()
    assert r["results"][0] == ["TATASTEEL", "BUY 65 @ 152.30"]
    assert r["results"][1] == ["INFY", "BUY 6 @ 1490.50"]
    assert "max trades" in r["results"][2][1]
    t = store.trades_on("2026-11-27")[0]
    assert (t["stop_price"], t["target_price"], t["status"]) == (150.8, 155.35, "OPEN")


def test_duplicate_alert_same_stock_skipped(at):
    _, c, broker, _ = setup(max_trades_per_day=10)
    c.post(f"/webhook/{SECRET}", json={"stocks": "INFY", "trigger_prices": "1490"})
    r = c.post(f"/webhook/{SECRET}", json={"stocks": "INFY", "trigger_prices": "1495"}).get_json()
    assert "already traded" in r["results"][0][1]
    assert len(broker.orders) == 1


def test_short_side_levels(at):
    _, c, _, store = setup()
    c.post(f"/webhook/{SECRET}?side=SELL", json={"stocks": "INFY", "trigger_prices": "1000"})
    t = store.trades_on("2026-11-27")[0]
    assert (t["side"], t["stop_price"], t["target_price"]) == ("SELL", 1010.0, 980.0)


def test_no_entries_outside_window_or_weekend(at):
    _, c, broker, _ = setup()
    at(14, 50)
    assert "outside entry window" in c.post(f"/webhook/{SECRET}", json=ALERT).get_json()["results"][0][1]
    at(11, 0, day=28)  # Saturday
    assert "weekend" in c.post(f"/webhook/{SECRET}", json=ALERT).get_json()["results"][0][1]
    assert broker.orders == []


def test_square_off_closes_open_and_skips_hit_oco(at, monkeypatch):
    app, c, broker, store = setup()
    c.post(f"/webhook/{SECRET}", json=ALERT)
    trades = store.trades_on("2026-11-27")
    # pretend INFY's target already hit and the position is flat
    broker.ocos[trades[1]["oco_id"]] = "TRIGGERED"
    broker.positions["INFY"] = 0
    at(15, 6)
    app.engine.square_off_all()
    exits = [o for o in broker.orders if o["side"] == "SELL"]
    assert exits == [{"id": exits[0]["id"], "symbol": "TATASTEEL", "side": "SELL", "qty": 65}]
    statuses = {t["symbol"]: t["status"] for t in store.trades_on("2026-11-27")}
    assert statuses == {"TATASTEEL": "SQUARED_OFF", "INFY": "EXITED"}
    # running again does nothing more, and new alerts are refused
    app.engine.square_off_all()
    assert len([o for o in broker.orders if o["side"] == "SELL"]) == 1


def test_oco_failure_exits_immediately(at, monkeypatch):
    _, c, broker, store = setup()
    monkeypatch.setattr(broker, "place_oco", lambda *a, **k: (_ for _ in ()).throw(RuntimeError("boom")))
    c.post(f"/webhook/{SECRET}", json={"stocks": "INFY", "trigger_prices": "1000"})
    assert [o["side"] for o in broker.orders] == ["BUY", "SELL"]
    assert store.trades_on("2026-11-27")[0]["status"] == "EXITED"


def test_trading_disabled(at):
    _, c, broker, _ = setup(trading_enabled=False)
    c.post(f"/webhook/{SECRET}", json=ALERT)
    assert broker.orders == []
