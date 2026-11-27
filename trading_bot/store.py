"""Small SQLite journal of every alert and trade (survives restarts)."""
import os
import sqlite3
import threading

SCHEMA = """
CREATE TABLE IF NOT EXISTS trades (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    day TEXT NOT NULL,
    created_at TEXT NOT NULL,
    symbol TEXT NOT NULL,
    side TEXT NOT NULL,
    qty INTEGER NOT NULL,
    entry_price REAL,
    stop_price REAL,
    target_price REAL,
    entry_order_id TEXT,
    oco_id TEXT,
    status TEXT NOT NULL,   -- PENDING, OPEN, EXITED, SQUARED_OFF, FAILED, NEEDS_ATTENTION
    note TEXT,
    scan_name TEXT
);
CREATE TABLE IF NOT EXISTS alerts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    received_at TEXT NOT NULL,
    payload TEXT NOT NULL
);
"""


class Store:
    def __init__(self, path):
        if path != ":memory:":
            os.makedirs(os.path.dirname(path), exist_ok=True)
        self.db = sqlite3.connect(path, check_same_thread=False)
        self.db.row_factory = sqlite3.Row
        self.db.executescript(SCHEMA)
        self.lock = threading.RLock()

    def log_alert(self, when, payload):
        with self.lock:
            self.db.execute("INSERT INTO alerts (received_at, payload) VALUES (?, ?)", (when, payload))
            self.db.commit()

    def add_trade(self, **f):
        with self.lock:
            cols = ", ".join(f)
            cur = self.db.execute(f"INSERT INTO trades ({cols}) VALUES ({', '.join('?' * len(f))})", tuple(f.values()))
            self.db.commit()
            return cur.lastrowid

    def update_trade(self, trade_id, **f):
        with self.lock:
            sets = ", ".join(f"{k} = ?" for k in f)
            self.db.execute(f"UPDATE trades SET {sets} WHERE id = ?", (*f.values(), trade_id))
            self.db.commit()

    def trades_on(self, day):
        with self.lock:
            return [dict(r) for r in self.db.execute("SELECT * FROM trades WHERE day = ? ORDER BY id", (day,))]

    def count_entries(self, day):
        with self.lock:
            row = self.db.execute(
                "SELECT COUNT(*) FROM trades WHERE day = ? AND status != 'FAILED'", (day,)
            ).fetchone()
            return row[0]

    def traded_symbol(self, day, symbol):
        with self.lock:
            row = self.db.execute(
                "SELECT 1 FROM trades WHERE day = ? AND symbol = ? AND status != 'FAILED'", (day, symbol)
            ).fetchone()
            return row is not None
