"""Settings loaded from environment variables (see .env.example)."""
from __future__ import annotations

import os
from dataclasses import dataclass, field
from datetime import time


def _env(name: str, default: str) -> str:
    # Strip inline "# comments" (systemd EnvironmentFile keeps them verbatim).
    return os.getenv(name, default).split("#", 1)[0].strip()


def _bool(name: str, default: bool) -> bool:
    return _env(name, str(default)).lower() in ("1", "true", "yes", "on")


def _time(name: str, default: str) -> time:
    hh, mm = _env(name, default).split(":")
    return time(int(hh), int(mm))


@dataclass
class Settings:
    groww_api_key: str = ""
    groww_totp_secret: str = ""
    groww_api_secret: str = ""
    webhook_token: str = ""

    dry_run: bool = True
    product: str = "MTF"
    sizing_mode: str = "fixed_qty"  # fixed_qty | capital
    quantity: int = 25
    capital_per_trade: float = 10000.0
    leverage: float = 3.05
    entry_limit_buffer_pct: float = 0.5
    target_profit_pct: float = 7.0

    max_open_trades: int = 10
    max_new_trades_per_day: int = 10
    max_order_value: float = 50000.0
    allow_reentry_same_day: bool = False

    entry_start: time = field(default_factory=lambda: time(9, 20))
    entry_end: time = field(default_factory=lambda: time(15, 15))
    poll_seconds: float = 5.0
    db_path: str = "trades.db"

    @classmethod
    def from_env(cls) -> "Settings":
        s = cls(
            groww_api_key=_env("GROWW_API_KEY", ""),
            groww_totp_secret=_env("GROWW_TOTP_SECRET", ""),
            groww_api_secret=_env("GROWW_API_SECRET", ""),
            webhook_token=_env("WEBHOOK_TOKEN", ""),
            dry_run=_bool("DRY_RUN", True),
            product=_env("PRODUCT", "MTF").upper(),
            sizing_mode=_env("SIZING_MODE", "fixed_qty").lower(),
            quantity=int(_env("QUANTITY", "25")),
            capital_per_trade=float(_env("CAPITAL_PER_TRADE", "10000")),
            leverage=float(_env("LEVERAGE", "3.05")),
            entry_limit_buffer_pct=float(_env("ENTRY_LIMIT_BUFFER_PCT", "0.5")),
            target_profit_pct=float(_env("TARGET_PROFIT_PCT", "7")),
            max_open_trades=int(_env("MAX_OPEN_TRADES", "10")),
            max_new_trades_per_day=int(_env("MAX_NEW_TRADES_PER_DAY", "10")),
            max_order_value=float(_env("MAX_ORDER_VALUE", "50000")),
            allow_reentry_same_day=_bool("ALLOW_REENTRY_SAME_DAY", False),
            entry_start=_time("ENTRY_START", "09:20"),
            entry_end=_time("ENTRY_END", "15:15"),
            poll_seconds=float(_env("POLL_SECONDS", "5")),
            db_path=_env("DB_PATH", "trades.db"),
        )
        s.validate()
        return s

    def validate(self) -> None:
        if self.product not in ("MTF", "CNC", "MIS"):
            raise ValueError("PRODUCT must be MTF, CNC or MIS")
        if self.sizing_mode not in ("fixed_qty", "capital"):
            raise ValueError("SIZING_MODE must be fixed_qty or capital")
        if not self.webhook_token or len(self.webhook_token) < 16:
            raise ValueError("WEBHOOK_TOKEN must be set (16+ characters)")
        if not self.dry_run and not self.groww_api_key:
            raise ValueError("GROWW_API_KEY is required when DRY_RUN=false")
