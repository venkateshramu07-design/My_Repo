"""Trading rules.

* A Chartink alert containing a stock = signal is TRUE for that stock.
* Enter (one trade at a time) when a signal arrives between ENTRY_START and ENTRY_END.
* Stay in while Chartink keeps sending the same stock.
* Exit as soon as an alert from the same scan arrives WITHOUT the stock, or no alert has
  included it for SIGNAL_TIMEOUT_MINUTES.
* At SQUARE_OFF_TIME, exit no matter what.
"""
import logging
import math
import threading
import uuid
from datetime import datetime, timedelta

import requests

from broker import now_ist

log = logging.getLogger("engine")


class Engine:
    def __init__(self, cfg, broker, store):
        self.cfg = cfg
        self.broker = broker
        self.store = store
        self.lock = threading.RLock()  # alerts and the timer never act at the same moment
        self._warned_exit_fail = set()

    def notify(self, text):
        log.info(text)
        if self.cfg.telegram_bot_token and self.cfg.telegram_chat_id:
            try:
                requests.post(
                    f"https://api.telegram.org/bot{self.cfg.telegram_bot_token}/sendMessage",
                    json={"chat_id": self.cfg.telegram_chat_id, "text": text},
                    timeout=5,
                )
            except requests.RequestException as e:
                log.warning("Telegram failed: %s", e)

    @staticmethod
    def parse_alert(payload):
        """Chartink sends comma-separated 'stocks' and 'trigger_prices'. Returns [(symbol, price)]."""
        stocks = [s.strip().upper() for s in str(payload.get("stocks", "")).split(",") if s.strip()]
        prices = [p.strip() for p in str(payload.get("trigger_prices", "")).split(",")]
        out = []
        for i, sym in enumerate(stocks):
            try:
                price = float(prices[i])
            except (IndexError, ValueError):
                price = None
            out.append((sym, price))
        return out

    # ---------- alerts ----------
    def handle_alert(self, payload):
        now = now_ist()
        scan = str(payload.get("scan_name") or payload.get("alert_name") or "")
        signals = self.parse_alert(payload)
        symbols = [s for s, _ in signals]

        with self.lock:
            active = self.store.active_trade()
            if active:
                same_scan = not active["scan_name"] or not scan or active["scan_name"] == scan
                if not same_scan:
                    return f"ignored: alert from another scan ({scan})"
                if active["symbol"] in symbols:
                    self.store.update_trade(active["id"], last_signal_at=now.isoformat(timespec="seconds"))
                    return f"holding {active['symbol']}: signal still true"
                self.exit(active, "signal no longer true", dict(signals))
                if self.store.active_trade():  # exit failed, still in the trade
                    return f"exit of {active['symbol']} failed"

            reason = self._entry_blocked(now)
            if reason:
                return f"no entry: {reason}"
            if not signals:
                return "no stocks in alert"
            symbol, price = signals[0]  # one trade at a time: take the first stock in the alert
            return self.enter(symbol, price, scan, now)

    def _entry_blocked(self, now):
        if not self.cfg.trading_enabled:
            return "trading disabled"
        if now.weekday() >= 5:
            return "weekend"
        if not (self.cfg.entry_start <= now.time() < self.cfg.entry_end):
            return f"outside entry window {self.cfg.entry_start:%H:%M}-{self.cfg.entry_end:%H:%M}"
        return None

    def quantity(self, price):
        """Fixed margin per trade: qty = margin x leverage / price, rounded down."""
        return math.floor(self.cfg.margin_per_trade * self.cfg.leverage / price)

    def enter(self, symbol, alert_price, scan, now):
        side = self.cfg.side
        try:
            price = self.broker.ltp(symbol) or alert_price
        except Exception as e:
            log.warning("Live price failed for %s (%s); using Chartink price", symbol, e)
            price = alert_price
        if not price:
            return f"no entry: no price for {symbol}"
        qty = self.quantity(price)
        if qty < 1:
            return f"no entry: margin too small for 1 share of {symbol} @ {price}"
        try:
            order_id = self.broker.market_order(symbol, side, qty, uuid.uuid4().hex[:12])
            filled, avg = self.broker.wait_for_fill(order_id)
        except Exception as e:
            self.store.add_trade(
                day=now.date().isoformat(), symbol=symbol, side=side, qty=qty, scan_name=scan,
                entry_time=now.isoformat(timespec="seconds"), last_signal_at=now.isoformat(timespec="seconds"),
                status="FAILED", note=str(e)[:500],
            )
            self.notify(f"Entry FAILED for {symbol}: {e}")
            return f"entry failed: {e}"
        entry = avg or price
        self.store.add_trade(
            day=now.date().isoformat(), symbol=symbol, side=side, qty=filled, scan_name=scan,
            entry_time=now.isoformat(timespec="seconds"), entry_price=entry,
            last_signal_at=now.isoformat(timespec="seconds"), status="OPEN",
        )
        word = "SHORT SOLD" if side == "SELL" else "BOUGHT"
        margin = entry * filled / self.cfg.leverage
        self.notify(f"{word} {symbol} x{filled} @ {entry:.2f} (margin ~ ₹{margin:,.0f})" + (" [PAPER]" if self.cfg.dry_run else ""))
        return f"entered {side} {symbol} x{filled}"

    # ---------- exits ----------
    def exit(self, trade, reason, prices=None):
        symbol, side = trade["symbol"], trade["side"]
        exit_side = "BUY" if side == "SELL" else "SELL"
        now = now_ist()
        try:
            net = self.broker.net_position(symbol)
            if net is None:  # couldn't read it: close what the bot opened
                qty = trade["qty"]
            elif (side == "SELL" and net < 0) or (side == "BUY" and net > 0):
                qty = min(abs(net), trade["qty"])  # never flip into a new position
            else:
                qty = 0  # already flat (e.g. closed by hand in the app)
            avg = None
            if qty:
                order_id = self.broker.market_order(symbol, exit_side, qty, uuid.uuid4().hex[:12])
                _, avg = self.broker.wait_for_fill(order_id)
        except Exception as e:
            log.exception("Exit failed for %s", symbol)
            if trade["id"] not in self._warned_exit_fail:
                self._warned_exit_fail.add(trade["id"])
                self.notify(f"URGENT: could not exit {symbol} ({e}). Retrying; close it in the Groww app if needed.")
            return False
        exit_price = avg or (prices or {}).get(symbol)
        self.store.update_trade(
            trade["id"], status="CLOSED", exit_time=now.isoformat(timespec="seconds"),
            exit_price=exit_price, exit_reason=reason,
        )
        pnl = ""
        if exit_price and trade["entry_price"]:
            sign = -1 if side == "SELL" else 1
            pnl = f" | P&L ~ ₹{sign * (exit_price - trade['entry_price']) * trade['qty']:.0f}"
        self.notify(f"EXITED {symbol} ({reason}){pnl}" + (" [PAPER]" if self.cfg.dry_run else ""))
        return True

    def tick(self):
        """Run every few seconds: signal timeout and the end-of-day exit."""
        with self.lock:
            active = self.store.active_trade()
            if not active:
                return
            now = now_ist()
            if now.time() >= self.cfg.square_off_time or active["day"] != now.date().isoformat():
                self.exit(active, f"{self.cfg.square_off_time:%H:%M} square-off")
                return
            last = datetime.fromisoformat(active["last_signal_at"])
            if now - last > timedelta(minutes=self.cfg.signal_timeout_minutes):
                self.exit(active, f"no signal for {self.cfg.signal_timeout_minutes:g} min")
