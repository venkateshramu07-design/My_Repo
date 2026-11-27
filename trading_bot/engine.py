"""Trading logic: turn a Chartink alert into intraday orders, and square off before close."""
import logging
import math
import threading
import uuid

import requests

from broker import now_ist, round_tick

log = logging.getLogger("engine")


class Engine:
    def __init__(self, cfg, broker, store):
        self.cfg = cfg
        self.broker = broker
        self.store = store
        self.entry_lock = threading.Lock()  # one entry decision at a time, so limits can't be raced
        self.squared_off_day = None

    # ---------- notifications ----------
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

    # ---------- alerts ----------
    @staticmethod
    def parse_alert(payload):
        """Chartink sends comma-separated 'stocks' and 'trigger_prices'."""
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

    def handle_alert(self, payload, side=None):
        side = (side or self.cfg.default_side).upper()
        scan = str(payload.get("scan_name") or payload.get("alert_name") or "")
        results = []
        for symbol, price in self.parse_alert(payload):
            try:
                results.append(self.enter(symbol, price, side, scan))
            except Exception as e:  # never let one stock break the rest
                log.exception("Entry failed for %s", symbol)
                self.notify(f"ERROR entering {symbol}: {e}")
                results.append((symbol, f"error: {e}"))
        return results

    def _skip_reason(self, symbol, price, now):
        day = now.date().isoformat()
        if not self.cfg.trading_enabled:
            return "trading disabled"
        if now.weekday() >= 5:
            return "weekend"
        if not (self.cfg.entry_start <= now.time() <= self.cfg.entry_end):
            return f"outside entry window {self.cfg.entry_start:%H:%M}-{self.cfg.entry_end:%H:%M}"
        if self.squared_off_day == day:
            return "already squared off for the day"
        if self.store.traded_symbol(day, symbol):
            return "already traded today"
        if self.store.count_entries(day) >= self.cfg.max_trades_per_day:
            return "max trades per day reached"
        if not price:
            return "no price"
        if not (self.cfg.min_price <= price <= self.cfg.max_price):
            return f"price {price} outside {self.cfg.min_price}-{self.cfg.max_price}"
        return None

    def levels(self, entry, side):
        sl, tg = self.cfg.stop_loss_pct / 100, self.cfg.target_pct / 100
        if side == "BUY":
            return round_tick(entry * (1 - sl)), round_tick(entry * (1 + tg))
        return round_tick(entry * (1 + sl)), round_tick(entry * (1 - tg))

    def enter(self, symbol, alert_price, side, scan=""):
        now = now_ist()
        day = now.date().isoformat()
        exit_side = "SELL" if side == "BUY" else "BUY"

        with self.entry_lock:
            try:
                price = self.broker.ltp(symbol) or alert_price
            except Exception as e:
                log.warning("LTP failed for %s (%s); using alert price", symbol, e)
                price = alert_price
            reason = self._skip_reason(symbol, price, now)
            if reason:
                log.info("SKIP %s: %s", symbol, reason)
                return (symbol, f"skipped: {reason}")
            qty = math.floor(self.cfg.capital_per_trade / price)
            if qty < 1:
                return (symbol, "skipped: capital too small for 1 share")
            trade_id = self.store.add_trade(
                day=day, created_at=now.isoformat(timespec="seconds"), symbol=symbol, side=side,
                qty=qty, status="PENDING", scan_name=scan,
            )

        ref = uuid.uuid4().hex[:12]
        try:
            order_id = self.broker.market_order(symbol, side, qty, ref)
            self.store.update_trade(trade_id, entry_order_id=order_id)
            filled, avg = self.broker.wait_for_fill(order_id)
        except Exception as e:
            self.store.update_trade(trade_id, status="FAILED", note=str(e)[:500])
            raise
        if filled < 1:
            self.store.update_trade(trade_id, status="FAILED", note="filled 0")
            return (symbol, "failed: nothing filled")

        entry = avg or price
        stop, target = self.levels(entry, side)
        self.store.update_trade(trade_id, qty=filled, entry_price=entry, stop_price=stop, target_price=target)

        net = filled if side == "BUY" else -filled
        try:
            oco_id = self.broker.place_oco(symbol, exit_side, filled, net, target, stop, ref + "x")
        except Exception as e:
            # Never sit in a position without a stop-loss: get out immediately.
            log.exception("OCO failed for %s, exiting", symbol)
            try:
                self.broker.market_order(symbol, exit_side, filled, ref + "e")
                self.store.update_trade(trade_id, status="EXITED", note=f"stop-loss order failed, exited: {e}"[:500])
                self.notify(f"{symbol}: could not place stop-loss/target ({e}). Position closed at market.")
            except Exception as e2:
                self.store.update_trade(trade_id, status="NEEDS_ATTENTION", note=f"OCO failed and exit failed: {e2}"[:500])
                self.notify(f"URGENT {symbol}: position open WITHOUT stop-loss. Close it in the Groww app now. ({e2})")
            return (symbol, "exited: stop-loss order failed")

        self.store.update_trade(trade_id, oco_id=oco_id, status="OPEN")
        self.notify(
            f"{'BOUGHT' if side == 'BUY' else 'SHORTED'} {symbol} x{filled} @ {entry:.2f} | "
            f"SL {stop:.2f} | Target {target:.2f}" + (" [PAPER]" if self.cfg.dry_run else "")
        )
        return (symbol, f"{side} {filled} @ {entry:.2f}")

    # ---------- end of day ----------
    def square_off_all(self):
        """Cancel open OCOs and close whatever is still open. Safe to run more than once."""
        day = now_ist().date().isoformat()
        self.squared_off_day = day
        for t in self.store.trades_on(day):
            if t["status"] not in ("OPEN", "PENDING"):
                continue
            symbol, side, qty = t["symbol"], t["side"], t["qty"]
            exit_side = "SELL" if side == "BUY" else "BUY"
            try:
                if t["oco_id"]:
                    status = self.broker.oco_status(t["oco_id"])
                    if status in ("TRIGGERED", "COMPLETED"):
                        self.store.update_trade(t["id"], status="EXITED", note=f"OCO {status}")
                        continue
                    if status == "ACTIVE":
                        self.broker.cancel_oco(t["oco_id"])
                net = self.broker.net_position(symbol)
                if net is None:
                    self.store.update_trade(t["id"], status="NEEDS_ATTENTION", note="could not read position")
                    self.notify(f"CHECK {symbol}: couldn't read the position at square-off. Check the Groww app.")
                    continue
                remaining = min(abs(net), qty)
                # only close in the direction of our own trade (never flip into a new position)
                if remaining and ((side == "BUY" and net > 0) or (side == "SELL" and net < 0)):
                    self.broker.market_order(symbol, exit_side, remaining, uuid.uuid4().hex[:12])
                    self.store.update_trade(t["id"], status="SQUARED_OFF", note=f"closed {remaining} at square-off")
                    self.notify(f"Squared off {symbol} x{remaining}")
                else:
                    self.store.update_trade(t["id"], status="EXITED", note="already flat at square-off")
            except Exception as e:
                log.exception("Square-off failed for %s", symbol)
                self.store.update_trade(t["id"], status="NEEDS_ATTENTION", note=str(e)[:500])
                self.notify(f"URGENT square-off failed for {symbol}: {e}. Close it in the Groww app.")
