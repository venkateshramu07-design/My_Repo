"""Order placement.

PaperBroker (default) records trades in ``paper_trades.csv``.
KiteBroker places real orders through Zerodha Kite Connect and is only used
with ``--live``. It needs ``KITE_API_KEY`` and ``KITE_ACCESS_TOKEN`` set.

Note on shorts: in the NSE cash segment a short sale must be squared off the
same day (MIS). A weekly short held for days needs futures or options, so
live SHORT orders are refused unless you pass ``--allow-intraday-shorts``.
"""

from __future__ import annotations

import csv
import datetime as dt
import math
import os
from pathlib import Path


def position_size(capital_per_trade: float, risk_per_trade: float,
                  entry: float, stop: float | None) -> int:
    """Quantity limited both by capital and by the rupee risk to the stop."""
    qty_cap = math.floor(capital_per_trade / entry)
    if stop and abs(entry - stop) > 0:
        qty_risk = math.floor(risk_per_trade / abs(entry - stop))
        return max(0, min(qty_cap, qty_risk))
    return max(0, qty_cap)


class PaperBroker:
    def __init__(self, path: Path):
        self.path = path

    def _open_keys(self) -> set[tuple[str, str]]:
        if not self.path.exists():
            return set()
        with self.path.open() as f:
            return {(r["symbol"], r["side"]) for r in csv.DictReader(f)
                    if r["status"] == "OPEN"}

    def place(self, symbol: str, side: str, qty: int, price: float,
              stop: float | None, signal_date: str) -> str:
        if (symbol, side) in self._open_keys():
            return "skipped: already open"
        new = not self.path.exists()
        with self.path.open("a", newline="") as f:
            w = csv.writer(f)
            if new:
                w.writerow(["placed_at", "signal_date", "symbol", "side", "qty",
                            "entry", "stop", "status"])
            w.writerow([dt.datetime.now().isoformat(timespec="seconds"), signal_date,
                        symbol, side, qty, price, stop, "OPEN"])
        return "paper order recorded"


class KiteBroker:
    def __init__(self, allow_intraday_shorts: bool = False):
        from kiteconnect import KiteConnect  # pip install kiteconnect
        self.kite = KiteConnect(api_key=os.environ["KITE_API_KEY"])
        self.kite.set_access_token(os.environ["KITE_ACCESS_TOKEN"])
        self.allow_shorts = allow_intraday_shorts

    def place(self, symbol: str, side: str, qty: int, price: float,
              stop: float | None, signal_date: str) -> str:
        if side == "SHORT" and not self.allow_shorts:
            return "refused: positional shorts are not allowed in NSE cash"
        k = self.kite
        product = k.PRODUCT_CNC if side == "LONG" else k.PRODUCT_MIS
        order_id = k.place_order(
            variety=k.VARIETY_REGULAR, exchange=k.EXCHANGE_NSE, tradingsymbol=symbol,
            transaction_type=k.TRANSACTION_TYPE_BUY if side == "LONG" else k.TRANSACTION_TYPE_SELL,
            quantity=qty, product=product, order_type=k.ORDER_TYPE_MARKET)
        msg = f"order {order_id}"
        if stop:
            sl_id = k.place_order(
                variety=k.VARIETY_REGULAR, exchange=k.EXCHANGE_NSE, tradingsymbol=symbol,
                transaction_type=k.TRANSACTION_TYPE_SELL if side == "LONG" else k.TRANSACTION_TYPE_BUY,
                quantity=qty, product=product, order_type=k.ORDER_TYPE_SLM,
                trigger_price=round(stop, 1))
            msg += f", stop-loss order {sl_id}"
        return msg
