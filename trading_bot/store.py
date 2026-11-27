"""Small SQLite journal of every alert and trade (survives restarts)."""
import os
import sqlite3
import threading

SCHEMA = """
CREATE TABLE IF NOT EXISTS trades (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    day TEXT NOT NULL,
    symbol TEXT NOT NULL,
    side TEXT NOT NULL,
    qty INTEGER NOT NULL,
    scan_name TEXT,
    entry_time TEXT NOT NULL,
    entry_price REAL,
    last_signal_at TEXT NOT NULL,
    exit_time TEXT,
    exit_price REAL,
    exit_reason TEXT,
    status TEXT NOT NULL,   -- OPEN, CLOSED, FAILED
    note TEXT
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
            cur = self.db.execute(
                f"INSERT INTO trades ({', '.join(f)}) VALUES ({', '.join('?' * len(f))})", tuple(f.values())
            )
            self.db.commit()
            return cur.lastrowid

    def update_trade(self, trade_id, **f):
        with self.lock:
            sets = ", ".join(f"{k} = ?" for k in f)
            self.db.execute(f"UPDATE trades SET {sets} WHERE id = ?", (*f.values(), trade_id))
            self.db.commit()

    def active_trade(self):
        with self.lock:
            row = self.db.execute("SELECT * FROM trades WHERE status = 'OPEN' ORDER BY id DESC LIMIT 1").fetchone()
            return dict(row) if row else None

    def trades_on(self, day):
        with self.lock:
            return [dict(r) for r in self.db.execute("SELECT * FROM trades WHERE day = ? ORDER BY id", (day,))]
