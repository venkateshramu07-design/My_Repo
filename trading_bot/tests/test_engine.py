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
DAY = "2026-11-27"  # a Friday


def t(s):
    return datetime.strptime(s, "%H:%M").time()


def make_cfg(**over):
    base = dict(
        dry_run=True, trading_enabled=True, webhook_secret=SECRET, groww_api_key="", groww_totp_secret="",
        groww_api_secret="", side="SELL", quantity=100, exchange="NSE",
        entry_start=t("11:00"), entry_end=t("15:00"), square_off_time=t("15:15"), signal_timeout_minutes=6,
        telegram_bot_token="", telegram_chat_id="", db_path=":memory:",
    )
    base.update(over)
    return Config(**base)


class Bot:
    def __init__(self, monkeypatch, **over):
        self.mp = monkeypatch
        cfg = make_cfg(**over)
        self.broker, self.store = PaperBroker(cfg), Store(":memory:")
        self.app = create_app(cfg, self.broker, self.store, start_timer=False, background=False)
        self.client = self.app.test_client()
        self.at("11:00")

    def at(self, hhmm, day=27):
        h, m = map(int, hhmm.split(":"))
        self.mp.setattr(engine_mod, "now_ist", lambda: datetime(2026, 11, day, h, m, tzinfo=IST))

    def alert(self, stocks, prices=None, scan="Short scan"):
        prices = prices or ",".join("1000" for _ in stocks.split(",")) if stocks else ""
        r = self.client.post(f"/webhook/{SECRET}", json={"stocks": stocks, "trigger_prices": prices, "scan_name": scan})
        return r.get_json()["result"]

    def tick(self):
        self.app.engine.tick()

    @property
    def sides(self):
        return [(o["side"], o["symbol"], o["qty"]) for o in self.broker.orders]

    @property
    def active(self):
        return self.store.active_trade()


@pytest.fixture
def bot(monkeypatch):
    return Bot(monkeypatch)


def test_wrong_secret_rejected(bot):
    assert bot.client.post("/webhook/wrong", json={"stocks": "INFY"}).status_code == 404
    assert bot.broker.orders == []


def test_enters_short_100_and_holds_while_signal_repeats(bot):
    bot.at("11:05")
    assert bot.alert("ADANIGREEN", "1327.6") == "entered SELL ADANIGREEN x100"
    for minute in ("11:10", "11:15", "11:20"):
        bot.at(minute)
        assert "holding ADANIGREEN" in bot.alert("ADANIGREEN,INFY")
        bot.tick()
    assert bot.sides == [("SELL", "ADANIGREEN", 100)]


def test_exits_when_alert_no_longer_contains_stock(bot):
    bot.at("11:05")
    bot.alert("ADANIGREEN", "1327.6")
    bot.at("11:10")
    bot.alert("INFY", "1500")  # same scan, ADANIGREEN gone -> exit, then INFY is a fresh signal
    assert bot.sides == [("SELL", "ADANIGREEN", 100), ("BUY", "ADANIGREEN", 100), ("SELL", "INFY", 100)]
    closed = bot.store.trades_on(DAY)[0]
    assert closed["exit_reason"] == "signal no longer true"


def test_exits_when_signals_stop_arriving(bot):
    bot.at("11:05")
    bot.alert("ADANIGREEN")
    bot.at("11:11")
    bot.tick()
    assert bot.active is not None  # exactly 6 min: still in
    bot.at("11:12")
    bot.tick()
    assert bot.active is None
    assert bot.sides[-1] == ("BUY", "ADANIGREEN", 100)


def test_only_one_trade_at_a_time(bot):
    bot.at("11:05")
    bot.alert("ADANIGREEN,INFY,TCS")
    assert bot.sides == [("SELL", "ADANIGREEN", 100)]
    bot.alert("ADANIGREEN,TCS")
    assert len(bot.sides) == 1


def test_entry_window_11_to_3(bot):
    bot.at("10:59")
    assert "outside entry window" in bot.alert("ADANIGREEN")
    bot.at("15:00")
    assert "outside entry window" in bot.alert("ADANIGREEN")
    bot.at("11:00", day=28)  # Saturday
    assert "weekend" in bot.alert("ADANIGREEN")
    assert bot.broker.orders == []


def test_trade_opened_before_3_held_after_3_and_closed_at_315(bot):
    bot.at("14:58")
    bot.alert("ADANIGREEN")
    for minute in ("15:03", "15:08", "15:13"):
        bot.at(minute)
        assert "holding" in bot.alert("ADANIGREEN")
        bot.tick()
    assert bot.active is not None
    bot.at("15:15")
    bot.tick()
    assert bot.active is None
    assert bot.store.trades_on(DAY)[0]["exit_reason"] == "15:15 square-off"


def test_after_exit_no_new_entry_after_3pm(bot):
    bot.at("14:58")
    bot.alert("ADANIGREEN")
    bot.at("15:05")
    assert "no entry" in bot.alert("INFY")
    assert bot.sides == [("SELL", "ADANIGREEN", 100), ("BUY", "ADANIGREEN", 100)]


def test_alert_from_other_scan_does_not_close_trade(bot):
    bot.at("11:05")
    bot.alert("ADANIGREEN", scan="Short scan")
    assert "another scan" in bot.alert("INFY", scan="Some other scan")
    assert bot.active["symbol"] == "ADANIGREEN"


def test_does_not_buy_back_if_closed_by_hand(bot):
    bot.at("11:05")
    bot.alert("ADANIGREEN")
    bot.broker.positions["ADANIGREEN"] = 0  # you closed it in the Groww app
    bot.at("11:10")
    bot.alert("INFY")
    assert ("BUY", "ADANIGREEN", 100) not in bot.sides


def test_failed_exit_keeps_trade_open_and_retries(bot, monkeypatch):
    bot.at("11:05")
    bot.alert("ADANIGREEN")
    real = bot.broker.market_order
    monkeypatch.setattr(bot.broker, "market_order", lambda *a: (_ for _ in ()).throw(RuntimeError("network")))
    bot.at("11:30")
    bot.tick()
    assert bot.active is not None
    monkeypatch.setattr(bot.broker, "market_order", real)
    bot.tick()
    assert bot.active is None


def test_manual_exit_endpoint(bot):
    bot.at("11:05")
    bot.alert("ADANIGREEN")
    assert bot.client.post(f"/exit/{SECRET}").get_json() == {"ok": True}
    assert bot.active is None


def test_buy_side_also_works(monkeypatch):
    b = Bot(monkeypatch, side="BUY", quantity=10)
    b.at("12:00")
    b.alert("INFY")
    b.at("12:05")
    b.alert("TCS")
    assert b.sides[:2] == [("BUY", "INFY", 10), ("SELL", "INFY", 10)]
