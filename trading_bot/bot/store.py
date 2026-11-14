"""SQLite persistence for trades, so the bot survives restarts.

Trade lifecycle:
    BUY_PENDING -> OPEN -> SELL_PENDING -> CLOSED
    BUY_PENDING -> FAILED            (buy rejected / cancelled with no fill)
    SELL_PENDING -> OPEN             (sell rejected / cancelled, retried later)
"""
from __future__ import annotations

import sqlite3
import threading
from dataclasses import dataclass
from datetime import datetime
from typing import Optional

ACTIVE = ("BUY_PENDING", "OPEN", "SELL_PENDING")


@dataclass
class Trade:
    id: int
    symbol: str
    quantity: int
    product: str
    status: str
    signal_price: Optional[float]
    buy_order_id: Optional[str]
    buy_ref: Optional[str]
    avg_price: Optional[float]
    target_price: Optional[float]
    sell_order_id: Optional[str]
    sell_ref: Optional[str]
    sell_price: Optional[float]
    created_at: str
    updated_at: str
    note: Optional[str]

    @property
    def order_value(self) -> Optional[float]:
        return None if self.avg_price is None else self.avg_price * self.quantity


_SCHEMA = """
CREATE TABLE IF NOT EXISTS trades (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    symbol TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    product TEXT NOT NULL,
    status TEXT NOT NULL,
    signal_price REAL,
    buy_order_id TEXT,
    buy_ref TEXT,
    avg_price REAL,
    target_price REAL,
    sell_order_id TEXT,
    sell_ref TEXT,
    sell_price REAL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    note TEXT
);
CREATE INDEX IF NOT EXISTS idx_trades_status ON trades(status);
CREATE INDEX IF NOT EXISTS idx_trades_symbol ON trades(symbol);
"""

_FIELDS = (
    "quantity status buy_order_id buy_ref avg_price target_price "
    "sell_order_id sell_ref sell_price note"
).split()


class Store:
    def __init__(self, path: str):
        self._lock = threading.Lock()
        self._db = sqlite3.connect(path, check_same_thread=False)
        self._db.row_factory = sqlite3.Row
        self._db.executescript(_SCHEMA)

    def _rows(self, sql: str, args=()) -> list[Trade]:
        with self._lock:
            return [Trade(**dict(r)) for r in self._db.execute(sql, args).fetchall()]

    def create(self, symbol: str, quantity: int, product: str,
               signal_price: Optional[float], now: datetime) -> Trade:
        ts = now.isoformat()
        with self._lock, self._db:
            cur = self._db.execute(
                "INSERT INTO trades (symbol, quantity, product, status, signal_price,"
                " created_at, updated_at) VALUES (?, ?, ?, 'BUY_PENDING', ?, ?, ?)",
                (symbol, quantity, product, signal_price, ts, ts),
            )
            trade_id = cur.lastrowid
        return self.get(trade_id)

    def get(self, trade_id: int) -> Trade:
        return self._rows("SELECT * FROM trades WHERE id = ?", (trade_id,))[0]

    def update(self, trade_id: int, now: datetime, **fields) -> Trade:
        bad = set(fields) - set(_FIELDS)
        if bad:
            raise ValueError(f"unknown fields {bad}")
        cols = ", ".join(f"{k} = ?" for k in fields)
        with self._lock, self._db:
            self._db.execute(
                f"UPDATE trades SET {cols}, updated_at = ? WHERE id = ?",
                (*fields.values(), now.isoformat(), trade_id),
            )
        return self.get(trade_id)

    def by_status(self, *statuses: str) -> list[Trade]:
        marks = ",".join("?" * len(statuses))
        return self._rows(f"SELECT * FROM trades WHERE status IN ({marks}) ORDER BY id", statuses)

    def active_for_symbol(self, symbol: str) -> list[Trade]:
        marks = ",".join("?" * len(ACTIVE))
        return self._rows(
            f"SELECT * FROM trades WHERE symbol = ? AND status IN ({marks})", (symbol, *ACTIVE)
        )

    def created_on(self, day: str, symbol: Optional[str] = None) -> list[Trade]:
        """Trades created on `day` (YYYY-MM-DD) that actually reached the broker."""
        sql = "SELECT * FROM trades WHERE substr(created_at, 1, 10) = ? AND buy_order_id IS NOT NULL"
        args: tuple = (day,)
        if symbol:
            sql += " AND symbol = ?"
            args += (symbol,)
        return self._rows(sql, args)

    def recent(self, limit: int = 100) -> list[Trade]:
        return self._rows("SELECT * FROM trades ORDER BY id DESC LIMIT ?", (limit,))
