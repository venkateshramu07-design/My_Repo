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

    # Signal source: "groww" = built-in scanner (no Chartink), "chartink" = webhook, or "both"
    signal_source: str = "groww"
    scan_interval_minutes: int = 5
    scan_history_days: int = 60
    universe_file: str = "universe.json"
    universe_max_age_days: int = 7
    api_rate_per_sec: float = 8.0
    # Screener conditions
    min_market_cap_cr: float = 10000.0
    max_close: float = 1000.0
    min_volume: float = 50000.0
    smi_k_len: int = 10
    smi_s1: int = 3
    smi_s2: int = 3
    smi_d_len: int = 10
    smi_threshold: float = 40.0

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
            signal_source=_env("SIGNAL_SOURCE", "groww").lower(),
            scan_interval_minutes=int(_env("SCAN_INTERVAL_MINUTES", "5")),
            scan_history_days=int(_env("SCAN_HISTORY_DAYS", "60")),
            universe_file=_env("UNIVERSE_FILE", "universe.json"),
            universe_max_age_days=int(_env("UNIVERSE_MAX_AGE_DAYS", "7")),
            api_rate_per_sec=float(_env("API_RATE_PER_SEC", "8")),
            min_market_cap_cr=float(_env("MIN_MARKET_CAP_CR", "10000")),
            max_close=float(_env("MAX_CLOSE", "1000")),
            min_volume=float(_env("MIN_VOLUME", "50000")),
            smi_k_len=int(_env("SMI_K_LEN", "10")),
            smi_s1=int(_env("SMI_S1", "3")),
            smi_s2=int(_env("SMI_S2", "3")),
            smi_d_len=int(_env("SMI_D_LEN", "10")),
            smi_threshold=float(_env("SMI_THRESHOLD", "40")),
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
        if self.signal_source not in ("groww", "chartink", "both"):
            raise ValueError("SIGNAL_SOURCE must be groww, chartink or both")
        if self.signal_source != "chartink" and not self.groww_api_key:
            raise ValueError("GROWW_API_KEY is required for the built-in scanner")
        if not self.webhook_token or len(self.webhook_token) < 16:
            raise ValueError("WEBHOOK_TOKEN must be set (16+ characters)")
        if not self.dry_run and not self.groww_api_key:
            raise ValueError("GROWW_API_KEY is required when DRY_RUN=false")
