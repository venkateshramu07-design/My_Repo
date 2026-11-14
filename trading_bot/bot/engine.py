"""Trading logic: enter on Chartink signals, exit at the profit target."""
from __future__ import annotations

import logging
import math
import threading
from datetime import datetime, time
from decimal import ROUND_CEILING, ROUND_FLOOR, Decimal
from typing import Callable, Iterable, Optional
from zoneinfo import ZoneInfo

from .broker import DEAD, FILLED, Broker
from .config import Settings
from .store import Store, Trade

log = logging.getLogger(__name__)
IST = ZoneInfo("Asia/Kolkata")
TICK = Decimal("0.05")  # a multiple of every NSE equity tick size
MARKET_OPEN, MARKET_CLOSE = time(9, 15), time(15, 30)


def ist_now() -> datetime:
    return datetime.now(IST)


def round_tick_up(price: float) -> float:
    return float((Decimal(str(price)) / TICK).to_integral_value(ROUND_CEILING) * TICK)


def target_price(avg_price: float, profit_pct: float) -> float:
    """Smallest tick price whose profit is strictly greater than `profit_pct` %."""
    raw = Decimal(str(avg_price)) * (1 + Decimal(str(profit_pct)) / 100)
    return float((raw / TICK).to_integral_value(ROUND_FLOOR) * TICK + TICK)


class Engine:
    def __init__(self, settings: Settings, store: Store, broker: Broker,
                 now: Callable[[], datetime] = ist_now):
        self.s = settings
        self.store = store
        self.broker = broker
        self.now = now
        self.paused = False  # kill switch for new entries
        self._lock = threading.RLock()

    # ------------------------------------------------------------------ helpers
    def _in_window(self, start: time, end: time) -> bool:
        n = self.now()
        return n.weekday() < 5 and start <= n.time() <= end

    def _ref(self, prefix: str, trade: Trade) -> str:
        # Groww: 8-20 alphanumeric chars. e.g. B251114093512000042 (19 chars)
        return f"{prefix}{self.now():%y%m%d%H%M%S}{trade.id % 1_000_000:06d}"

    def _quantity(self, price: float) -> int:
        if self.s.sizing_mode == "capital":
            return math.floor(self.s.capital_per_trade * self.s.leverage / price)
        return self.s.quantity

    # ------------------------------------------------------------------ entries
    def handle_signal(self, signals: Iterable[tuple[str, Optional[float]]]) -> list[dict]:
        """Process the stocks from one Chartink alert. Returns one result per stock."""
        with self._lock:
            return [self._enter(sym, px) for sym, px in signals]

    def _enter(self, symbol: str, signal_price: Optional[float]) -> dict:
        def skip(reason: str) -> dict:
            log.info("SKIP %s: %s", symbol, reason)
            return {"symbol": symbol, "action": "skipped", "reason": reason}

        if self.paused:
            return skip("bot paused")
        if not self._in_window(self.s.entry_start, self.s.entry_end):
            return skip("outside entry window")
        if self.store.active_for_symbol(symbol):
            return skip("already holding / order pending")
        today = f"{self.now():%Y-%m-%d}"
        if not self.s.allow_reentry_same_day and self.store.created_on(today, symbol):
            return skip("already traded today")
        if len(self.store.by_status("BUY_PENDING", "OPEN", "SELL_PENDING")) >= self.s.max_open_trades:
            return skip("max open trades reached")
        if len(self.store.created_on(today)) >= self.s.max_new_trades_per_day:
            return skip("max new trades per day reached")

        if signal_price and hasattr(self.broker, "note_price"):
            self.broker.note_price(symbol, signal_price)
        try:
            ltp = self.broker.ltp([symbol]).get(symbol) or signal_price
        except Exception as exc:
            log.warning("LTP fetch failed for %s: %s", symbol, exc)
            ltp = signal_price
        if not ltp:
            return skip("no price available")

        limit = round_tick_up(ltp * (1 + self.s.entry_limit_buffer_pct / 100))
        qty = self._quantity(limit)
        if qty < 1:
            return skip("quantity rounds to 0")
        if qty * limit > self.s.max_order_value:
            return skip(f"order value {qty * limit:.0f} > MAX_ORDER_VALUE")

        trade = self.store.create(symbol, qty, self.s.product, signal_price, self.now())
        ref = self._ref("B", trade)
        try:
            oid = self.broker.place_order(symbol, "BUY", qty, limit, self.s.product, ref)
        except Exception as exc:
            log.error("BUY %s failed: %s", symbol, exc)
            self.store.update(trade.id, self.now(), status="FAILED", buy_ref=ref, note=str(exc)[:500])
            return {"symbol": symbol, "action": "error", "reason": str(exc)}
        self.store.update(trade.id, self.now(), buy_order_id=oid, buy_ref=ref)
        log.info("BUY %s x%d limit %.2f (%s) order=%s", symbol, qty, limit, self.s.product, oid)
        return {"symbol": symbol, "action": "buy_placed", "qty": qty, "limit": limit, "order_id": oid}

    # ------------------------------------------------------------------ monitor
    def poll(self) -> None:
        """One monitoring cycle: track fills and fire profit-target exits."""
        with self._lock:
            for t in self.store.by_status("BUY_PENDING"):
                self._safe(self._check_buy, t)
            for t in self.store.by_status("SELL_PENDING"):
                self._safe(self._check_sell, t)
            if self._in_window(MARKET_OPEN, MARKET_CLOSE):
                self._check_targets()

    def _safe(self, fn, t: Trade) -> None:
        try:
            fn(t)
        except Exception:
            log.exception("error processing trade %s (%s)", t.id, t.symbol)

    def _check_buy(self, t: Trade) -> None:
        if not t.buy_order_id:
            return
        d = self.broker.order_detail(t.buy_order_id)
        filled = d["filled_quantity"]
        if d["status"] in FILLED and filled >= t.quantity:
            self._mark_open(t, filled, d["average_fill_price"])
        elif d["status"] in DEAD:
            if filled > 0:  # partially filled, rest cancelled
                self._mark_open(t, filled, d["average_fill_price"])
            else:
                self.store.update(t.id, self.now(), status="FAILED",
                                  note=f"buy {d['status']}: {d.get('remark') or ''}"[:500])
                log.warning("BUY %s %s: %s", t.symbol, d["status"], d.get("remark"))

    def _mark_open(self, t: Trade, qty: int, avg: float) -> None:
        tgt = target_price(avg, self.s.target_profit_pct)
        self.store.update(t.id, self.now(), status="OPEN", quantity=qty,
                          avg_price=avg, target_price=tgt)
        log.info("FILLED %s x%d @ %.2f, order value %.2f -> target %.2f",
                 t.symbol, qty, avg, qty * avg, tgt)

    def _check_targets(self) -> None:
        open_trades = self.store.by_status("OPEN")
        if not open_trades:
            return
        try:
            prices = self.broker.ltp(t.symbol for t in open_trades)
        except Exception as exc:
            log.warning("LTP fetch failed: %s", exc)
            return
        for t in open_trades:
            ltp = prices.get(t.symbol)
            if ltp is not None and ltp >= t.target_price:
                self._safe(lambda tr: self._sell(tr, ltp), t)

    def _sell(self, t: Trade, ltp: float) -> None:
        # Limit at the target: marketable now (LTP >= target) and never below +7%.
        ref = self._ref("S", t)
        oid = self.broker.place_order(t.symbol, "SELL", t.quantity, t.target_price, t.product, ref)
        self.store.update(t.id, self.now(), status="SELL_PENDING", sell_order_id=oid, sell_ref=ref)
        log.info("TARGET HIT %s ltp %.2f >= %.2f: SELL x%d order=%s",
                 t.symbol, ltp, t.target_price, t.quantity, oid)

    def _check_sell(self, t: Trade) -> None:
        d = self.broker.order_detail(t.sell_order_id)
        filled = d["filled_quantity"]
        if d["status"] in FILLED and filled >= t.quantity:
            px = d["average_fill_price"]
            self.store.update(t.id, self.now(), status="CLOSED", sell_price=px)
            log.info("CLOSED %s x%d: bought %.2f sold %.2f, gross P&L %.2f",
                     t.symbol, t.quantity, t.avg_price, px, (px - t.avg_price) * t.quantity)
        elif d["status"] in DEAD:
            # Sell expired/rejected: go back to OPEN and retry on the next target hit.
            self.store.update(t.id, self.now(), status="OPEN", quantity=t.quantity - filled,
                              note=f"sell {d['status']} (filled {filled}): {d.get('remark') or ''}"[:500])
            log.warning("SELL %s %s, filled %d; will retry", t.symbol, d["status"], filled)

    def run_forever(self, stop: threading.Event) -> None:
        log.info("monitor started (poll every %.0fs)", self.s.poll_seconds)
        while not stop.is_set():
            try:
                self.poll()
            except Exception:
                log.exception("poll cycle failed")
            stop.wait(self.s.poll_seconds)
