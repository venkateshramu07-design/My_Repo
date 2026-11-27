"""Talks to Groww. PaperBroker has the same methods but only logs (used when DRY_RUN=true)."""
import logging
import threading
import time
from datetime import datetime
from zoneinfo import ZoneInfo

log = logging.getLogger("broker")
IST = ZoneInfo("Asia/Kolkata")

FILLED = {"EXECUTED", "COMPLETED"}
DEAD = {"REJECTED", "FAILED", "CANCELLED"}


def round_tick(price, tick=0.05):
    return round(round(price / tick) * tick, 2)


class BrokerError(Exception):
    pass


class GrowwBroker:
    def __init__(self, cfg):
        self.cfg = cfg
        self._client = None
        self._token_day = None
        self._lock = threading.Lock()

    # Groww access tokens expire every day (around 6 AM), so log in again each trading day.
    def client(self):
        with self._lock:
            today = datetime.now(IST).date()
            if self._client is None or self._token_day != today:
                from growwapi import GrowwAPI

                if self.cfg.groww_totp_secret:
                    import pyotp

                    token = GrowwAPI.get_access_token(
                        api_key=self.cfg.groww_api_key,
                        totp=pyotp.TOTP(self.cfg.groww_totp_secret).now(),
                    )
                else:
                    token = GrowwAPI.get_access_token(
                        api_key=self.cfg.groww_api_key, secret=self.cfg.groww_api_secret
                    )
                self._client = GrowwAPI(token)
                self._token_day = today
                log.info("Logged in to Groww")
            return self._client

    def ltp(self, symbol):
        g = self.client()
        key = f"NSE_{symbol}"
        data = g.get_ltp(exchange_trading_symbols=(key,), segment=g.SEGMENT_CASH, timeout=5)
        value = data.get(key) if isinstance(data, dict) else None
        return float(value) if value else None

    def market_order(self, symbol, side, qty, ref):
        g = self.client()
        resp = g.place_order(
            trading_symbol=symbol,
            quantity=qty,
            validity=g.VALIDITY_DAY,
            exchange=g.EXCHANGE_NSE,
            segment=g.SEGMENT_CASH,
            product=g.PRODUCT_MIS,
            order_type=g.ORDER_TYPE_MARKET,
            transaction_type=side,
            order_reference_id=ref,
            timeout=10,
        )
        order_id = resp.get("groww_order_id")
        if not order_id:
            raise BrokerError(f"No order id in Groww response: {resp}")
        return order_id

    def wait_for_fill(self, order_id, seconds=15):
        """Returns (filled_qty, avg_price) once the order is filled; raises if rejected/timeout."""
        g = self.client()
        deadline = time.time() + seconds
        last = {}
        while time.time() < deadline:
            last = g.get_order_status(segment=g.SEGMENT_CASH, groww_order_id=order_id, timeout=5)
            status = str(last.get("order_status", "")).upper()
            if status in FILLED:
                detail = g.get_order_detail(segment=g.SEGMENT_CASH, groww_order_id=order_id, timeout=5)
                qty = int(last.get("filled_quantity") or detail.get("filled_quantity") or 0)
                price = float(detail.get("average_fill_price") or detail.get("price") or 0)
                return qty, price
            if status in DEAD:
                raise BrokerError(f"Order {order_id} {status}: {last.get('remark', '')}")
            time.sleep(1)
        raise BrokerError(f"Order {order_id} not filled within {seconds}s (last status: {last})")

    def place_oco(self, symbol, exit_side, qty, net_position_qty, target, stop, ref):
        """One-cancels-other exit: target LIMIT + stop-loss SL-M. Returns the smart order id."""
        g = self.client()
        resp = g.create_smart_order(
            smart_order_type=g.SMART_ORDER_TYPE_OCO,
            reference_id=ref,
            segment=g.SEGMENT_CASH,
            trading_symbol=symbol,
            quantity=qty,
            product_type=g.PRODUCT_MIS,
            exchange=g.EXCHANGE_NSE,
            duration=g.VALIDITY_DAY,
            net_position_quantity=net_position_qty,
            transaction_type=exit_side,
            target={"trigger_price": f"{target:.2f}", "order_type": g.ORDER_TYPE_LIMIT, "price": f"{target:.2f}"},
            stop_loss={"trigger_price": f"{stop:.2f}", "order_type": g.ORDER_TYPE_STOP_LOSS_MARKET},
            timeout=10,
        )
        oco_id = resp.get("smart_order_id")
        if not oco_id:
            raise BrokerError(f"No smart_order_id in Groww response: {resp}")
        return oco_id

    def oco_status(self, oco_id):
        g = self.client()
        resp = g.get_smart_order(
            segment=g.SEGMENT_CASH, smart_order_type=g.SMART_ORDER_TYPE_OCO, smart_order_id=oco_id, timeout=5
        )
        return str(resp.get("status", "")).upper()

    def cancel_oco(self, oco_id):
        g = self.client()
        g.cancel_smart_order(
            segment=g.SEGMENT_CASH, smart_order_type=g.SMART_ORDER_TYPE_OCO, smart_order_id=oco_id, timeout=10
        )

    def net_position(self, symbol):
        """Net intraday quantity (+long / -short), or None if it can't be read."""
        g = self.client()
        resp = g.get_position_for_trading_symbol(trading_symbol=symbol, segment=g.SEGMENT_CASH, timeout=5)
        positions = resp.get("positions") if isinstance(resp, dict) else None
        if positions is None:
            return None
        total = 0
        for p in positions:
            if str(p.get("product", "MIS")).upper() != "MIS":
                continue
            q = p.get("quantity", p.get("net_quantity"))
            if q is None:
                return None
            total += int(q)
        return total


class PaperBroker:
    """Pretends to trade. Fills at the alert price so the whole flow can be tested safely."""

    def __init__(self, cfg):
        self.cfg = cfg
        self.orders = []
        self.positions = {}
        self.ocos = {}
        self._n = 0

    def _id(self, prefix):
        self._n += 1
        return f"{prefix}-{self._n}"

    def ltp(self, symbol):
        return None  # caller falls back to Chartink's trigger price

    def market_order(self, symbol, side, qty, ref):
        oid = self._id("PAPER")
        self.orders.append({"id": oid, "symbol": symbol, "side": side, "qty": qty})
        self.positions[symbol] = self.positions.get(symbol, 0) + (qty if side == "BUY" else -qty)
        log.info("[PAPER] %s %s x%s", side, symbol, qty)
        return oid

    def wait_for_fill(self, order_id, seconds=15):
        o = next(o for o in self.orders if o["id"] == order_id)
        return o["qty"], 0.0  # 0 = unknown; the engine then uses the alert/LTP price

    def place_oco(self, symbol, exit_side, qty, net_position_qty, target, stop, ref):
        oid = self._id("OCO")
        self.ocos[oid] = "ACTIVE"
        log.info("[PAPER] OCO %s %s x%s target=%.2f stop=%.2f", exit_side, symbol, qty, target, stop)
        return oid

    def oco_status(self, oco_id):
        return self.ocos.get(oco_id, "")

    def cancel_oco(self, oco_id):
        self.ocos[oco_id] = "CANCELLED"

    def net_position(self, symbol):
        return self.positions.get(symbol, 0)


def make_broker(cfg):
    return PaperBroker(cfg) if cfg.dry_run else GrowwBroker(cfg)


def now_ist():
    return datetime.now(IST)

