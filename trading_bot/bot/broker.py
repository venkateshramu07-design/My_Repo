"""Broker adapters: the real Groww API, and a paper-trading simulator.

Both expose the same four methods the engine needs:
    place_order(symbol, side, qty, price, product, ref) -> order_id
    order_detail(order_id) -> {"status", "filled_quantity", "average_fill_price"}
    ltp(symbols) -> {symbol: last_traded_price}
"""
from __future__ import annotations

import itertools
import logging
import threading
from datetime import datetime, timedelta
from typing import Callable, Iterable, Optional, Protocol

log = logging.getLogger(__name__)

FILLED = {"EXECUTED", "DELIVERY_AWAITED", "COMPLETED"}
DEAD = {"REJECTED", "FAILED", "CANCELLED"}


class Broker(Protocol):
    def place_order(self, symbol: str, side: str, qty: int, price: float,
                    product: str, ref: str) -> str: ...
    def order_detail(self, order_id: str) -> dict: ...
    def ltp(self, symbols: Iterable[str]) -> dict[str, float]: ...


class GrowwBroker:
    """Thin wrapper over the official `growwapi` SDK (v1.5.0).

    Uses the TOTP flow when GROWW_TOTP_SECRET is set (no daily manual approval),
    otherwise the API key + secret flow. Groww access tokens expire at 06:00 IST
    daily, so the token is refreshed after that time and on any 401.
    """

    def __init__(self, api_key: str, totp_secret: str = "", api_secret: str = "",
                 now: Callable[[], datetime] = datetime.now):
        from growwapi import GrowwAPI  # imported lazily so tests don't need it

        self._G = GrowwAPI
        self._api_key = api_key
        self._totp_secret = totp_secret
        self._api_secret = api_secret
        self._now = now
        self._client = None
        self._token_time: Optional[datetime] = None
        self._lock = threading.Lock()

    # ---------- auth ----------
    def _login(self) -> None:
        if self._totp_secret:
            import pyotp
            token = self._G.get_access_token(
                api_key=self._api_key, totp=pyotp.TOTP(self._totp_secret).now())
        else:
            token = self._G.get_access_token(api_key=self._api_key, secret=self._api_secret)
        self._client = self._G(token)
        self._token_time = self._now()
        log.info("Groww access token refreshed")

    def _api(self):
        with self._lock:
            now = self._now()
            six_am = now.replace(hour=6, minute=0, second=0, microsecond=0)
            if now < six_am:
                six_am -= timedelta(days=1)
            if self._client is None or self._token_time < six_am:
                self._login()
            return self._client

    def _call(self, fn: Callable):
        from growwapi.groww.exceptions import GrowwAPIAuthenticationException
        try:
            return fn(self._api())
        except GrowwAPIAuthenticationException:
            with self._lock:
                self._client = None
            return fn(self._api())

    # ---------- trading ----------
    def place_order(self, symbol, side, qty, price, product, ref) -> str:
        G = self._G
        resp = self._call(lambda c: c.place_order(
            trading_symbol=symbol,
            quantity=int(qty),
            price=float(price),
            validity=G.VALIDITY_DAY,
            exchange=G.EXCHANGE_NSE,
            segment=G.SEGMENT_CASH,
            product=product,  # "MTF" / "CNC" / "MIS"
            order_type=G.ORDER_TYPE_LIMIT,
            transaction_type=G.TRANSACTION_TYPE_BUY if side == "BUY" else G.TRANSACTION_TYPE_SELL,
            order_reference_id=ref,
            timeout=10,
        ))
        return resp["groww_order_id"]

    def order_detail(self, order_id: str) -> dict:
        d = self._call(lambda c: c.get_order_detail(
            segment=self._G.SEGMENT_CASH, groww_order_id=order_id, timeout=10))
        return {
            "status": d.get("order_status"),
            "filled_quantity": int(d.get("filled_quantity") or 0),
            "average_fill_price": float(d.get("average_fill_price") or 0),
            "remark": d.get("remark"),
        }

    def ltp(self, symbols: Iterable[str]) -> dict[str, float]:
        out: dict[str, float] = {}
        symbols = list(dict.fromkeys(symbols))
        for i in range(0, len(symbols), 50):  # API max 50 per call
            keys = tuple(f"NSE_{s}" for s in symbols[i:i + 50])
            resp = self._call(lambda c: c.get_ltp(
                exchange_trading_symbols=keys, segment=self._G.SEGMENT_CASH, timeout=10))
            for k, v in resp.items():
                if v is not None:
                    out[k.split("_", 1)[1]] = float(v)
        return out


class PaperBroker:
    """Simulated broker for DRY_RUN. Never sends orders.

    Prices come from `price_source` (the real Groww LTP when credentials are set),
    falling back to prices learned from Chartink alerts. A BUY limit fills when
    LTP <= limit; a SELL limit fills when LTP >= limit.
    """

    def __init__(self, price_source: Optional[Callable[[Iterable[str]], dict]] = None):
        self._price_source = price_source
        self._known: dict[str, float] = {}
        self._orders: dict[str, dict] = {}
        self._ids = itertools.count(1)

    def note_price(self, symbol: str, price: float) -> None:
        self._known[symbol] = price

    def ltp(self, symbols):
        symbols = list(symbols)
        prices = {s: self._known[s] for s in symbols if s in self._known}
        if self._price_source:
            try:
                prices.update(self._price_source(symbols))
            except Exception as exc:  # paper mode should keep running
                log.warning("paper: live price fetch failed: %s", exc)
        self._known.update(prices)
        return prices

    def place_order(self, symbol, side, qty, price, product, ref) -> str:
        oid = f"PAPER{next(self._ids):06d}"
        self._orders[oid] = dict(symbol=symbol, side=side, qty=qty, price=price,
                                 status="OPEN", filled=0, avg=0.0)
        log.info("PAPER %s %s x%d @ %.2f (%s) ref=%s -> %s",
                 side, symbol, qty, price, product, ref, oid)
        return oid

    def order_detail(self, order_id: str) -> dict:
        o = self._orders[order_id]
        if o["status"] == "OPEN":
            ltp = self.ltp([o["symbol"]]).get(o["symbol"])
            crosses = ltp is not None and (
                ltp <= o["price"] if o["side"] == "BUY" else ltp >= o["price"])
            if crosses:
                # Buys fill at the better of LTP/limit; sells at the limit.
                fill = min(ltp, o["price"]) if o["side"] == "BUY" else o["price"]
                o.update(status="EXECUTED", filled=o["qty"], avg=fill)
        return {"status": o["status"], "filled_quantity": o["filled"],
                "average_fill_price": o["avg"], "remark": "paper"}
