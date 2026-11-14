"""Pre-flight check: logs in to Groww and reads margin + one LTP. Places NO orders.

Run:  python -m bot.check
"""
from .app import load_dotenv
from .broker import GrowwBroker
from .config import Settings


def main() -> None:
    load_dotenv()
    s = Settings.from_env()
    b = GrowwBroker(s.groww_api_key, s.groww_totp_secret, s.groww_api_secret)
    api = b._api()
    print("Login OK")
    print("Margin:", api.get_available_margin_details())
    print("LTP:", b.ltp(["SBIN"]))
    print(f"Config: product={s.product} sizing={s.sizing_mode} qty={s.quantity} "
          f"target={s.target_profit_pct}% dry_run={s.dry_run}")


if __name__ == "__main__":
    main()
