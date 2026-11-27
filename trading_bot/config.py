"""Settings, read from environment variables (or a .env file next to this one)."""
import os
from dataclasses import dataclass
from datetime import time
from pathlib import Path

try:
    from dotenv import load_dotenv

    load_dotenv(Path(__file__).with_name(".env"))
except ImportError:  # python-dotenv is optional
    pass


def _bool(name, default):
    return os.getenv(name, str(default)).strip().lower() in ("1", "true", "yes", "on")


def _time(name, default):
    h, m = os.getenv(name, default).strip().split(":")
    return time(int(h), int(m))


@dataclass
class Config:
    dry_run: bool
    trading_enabled: bool
    webhook_secret: str
    groww_api_key: str
    groww_totp_secret: str
    groww_api_secret: str
    side: str
    margin_per_trade: float
    leverage: float
    exchange: str
    entry_start: time
    entry_end: time
    square_off_time: time
    signal_timeout_minutes: float
    telegram_bot_token: str
    telegram_chat_id: str
    db_path: str

    @classmethod
    def from_env(cls):
        db = os.getenv("DB_PATH", "data/trades.db")
        if not os.path.isabs(db):
            db = str(Path(__file__).parent / db)
        cfg = cls(
            dry_run=_bool("DRY_RUN", True),
            trading_enabled=_bool("TRADING_ENABLED", True),
            webhook_secret=os.getenv("WEBHOOK_SECRET", ""),
            groww_api_key=os.getenv("GROWW_API_KEY", ""),
            groww_totp_secret=os.getenv("GROWW_TOTP_SECRET", ""),
            groww_api_secret=os.getenv("GROWW_API_SECRET", ""),
            side=os.getenv("SIDE", "SELL").strip().upper(),
            margin_per_trade=float(os.getenv("MARGIN_PER_TRADE", "25000")),
            leverage=float(os.getenv("LEVERAGE", "5")),
            exchange=os.getenv("EXCHANGE", "NSE").strip().upper(),
            entry_start=_time("ENTRY_START", "11:00"),
            entry_end=_time("ENTRY_END", "15:00"),
            square_off_time=_time("SQUARE_OFF_TIME", "15:15"),
            signal_timeout_minutes=float(os.getenv("SIGNAL_TIMEOUT_MINUTES", "16")),
            telegram_bot_token=os.getenv("TELEGRAM_BOT_TOKEN", ""),
            telegram_chat_id=os.getenv("TELEGRAM_CHAT_ID", ""),
            db_path=db,
        )
        cfg.validate()
        return cfg

    def validate(self):
        if len(self.webhook_secret) < 16 or self.webhook_secret.startswith("change-me"):
            raise ValueError("Set WEBHOOK_SECRET to a random string of at least 16 characters")
        if self.side not in ("BUY", "SELL"):
            raise ValueError("SIDE must be BUY or SELL")
        if self.exchange not in ("NSE", "BSE"):
            raise ValueError("EXCHANGE must be NSE or BSE")
        if self.margin_per_trade <= 0 or self.leverage <= 0:
            raise ValueError("MARGIN_PER_TRADE and LEVERAGE must be positive")
        if self.signal_timeout_minutes <= 0:
            raise ValueError("SIGNAL_TIMEOUT_MINUTES must be positive")
        if not self.dry_run and not self.groww_api_key:
            raise ValueError("GROWW_API_KEY is required when DRY_RUN=false")
        if not self.dry_run and not (self.groww_totp_secret or self.groww_api_secret):
            raise ValueError("Set GROWW_TOTP_SECRET or GROWW_API_SECRET when DRY_RUN=false")
