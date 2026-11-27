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
    default_side: str
    capital_per_trade: float
    stop_loss_pct: float
    target_pct: float
    max_trades_per_day: int
    min_price: float
    max_price: float
    entry_start: time
    entry_end: time
    square_off_time: time
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
            default_side=os.getenv("DEFAULT_SIDE", "BUY").strip().upper(),
            capital_per_trade=float(os.getenv("CAPITAL_PER_TRADE", "10000")),
            stop_loss_pct=float(os.getenv("STOP_LOSS_PCT", "1.0")),
            target_pct=float(os.getenv("TARGET_PCT", "2.0")),
            max_trades_per_day=int(os.getenv("MAX_TRADES_PER_DAY", "5")),
            min_price=float(os.getenv("MIN_PRICE", "20")),
            max_price=float(os.getenv("MAX_PRICE", "5000")),
            entry_start=_time("ENTRY_START", "09:20"),
            entry_end=_time("ENTRY_END", "14:45"),
            square_off_time=_time("SQUARE_OFF_TIME", "15:05"),
            telegram_bot_token=os.getenv("TELEGRAM_BOT_TOKEN", ""),
            telegram_chat_id=os.getenv("TELEGRAM_CHAT_ID", ""),
            db_path=db,
        )
        cfg.validate()
        return cfg

    def validate(self):
        if len(self.webhook_secret) < 16 or self.webhook_secret.startswith("change-me"):
            raise ValueError("Set WEBHOOK_SECRET to a random string of at least 16 characters")
        if self.default_side not in ("BUY", "SELL"):
            raise ValueError("DEFAULT_SIDE must be BUY or SELL")
        if not self.dry_run and not self.groww_api_key:
            raise ValueError("GROWW_API_KEY is required when DRY_RUN=false")
        if not self.dry_run and not (self.groww_totp_secret or self.groww_api_secret):
            raise ValueError("Set GROWW_TOTP_SECRET or GROWW_API_SECRET when DRY_RUN=false")
        if self.stop_loss_pct <= 0 or self.target_pct <= 0:
            raise ValueError("STOP_LOSS_PCT and TARGET_PCT must be positive")
