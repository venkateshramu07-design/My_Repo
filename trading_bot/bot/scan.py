"""Run the built-in scanner by hand (places NO orders).

  python -m bot.scan SBIN TATASTEEL     show SMI values for these stocks (compare with Chartink)
  python -m bot.scan --all              one full scan, prints the stocks that match
"""
import logging
import sys

from .app import load_dotenv
from .broker import GrowwBroker
from .config import Settings
from .scanner import TIMEFRAMES, Scanner


def main() -> None:
    load_dotenv()
    logging.basicConfig(level=logging.INFO, format="%(message)s")
    s = Settings.from_env()
    data = GrowwBroker(s.groww_api_key, s.groww_totp_secret, s.groww_api_secret,
                       rate_per_sec=s.api_rate_per_sec)
    sc = Scanner(s, data, engine=None)
    args = sys.argv[1:]
    if not args:
        print(__doc__)
        return
    if args == ["--all"]:
        matches = sc.scan()
        print(f"\n{len(matches)} of {len(sc.universe)} stocks match:")
        for sym, px in matches:
            print(f"  {sym:12} {px:10.2f}")
        return
    for sym in (a.upper() for a in args):
        sc.universe = {sym: 0}
        sc._load_history()
        r = sc.evaluate(sym, sc._refresh(sym))
        print(f"\n{sym}: {'PASS' if r['pass'] else 'no'}  {r.get('reason', '')}")
        if "close" in r:
            print(f"  close {r['close']:.2f}   today's volume {r['volume']:,.0f}")
        for name, _ in TIMEFRAMES:
            if name in r:
                print(f"  {name:9}  SMI %K {r[name]['K']:7.2f}   %D {r[name]['D']:7.2f}")
        try:
            q = data.quote(sym)
            print(f"  market cap {q['market_cap_cr']:,.0f} Cr")
        except Exception as exc:
            print(f"  market cap: unavailable ({exc})")


if __name__ == "__main__":
    main()
