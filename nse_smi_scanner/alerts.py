"""Alert delivery: console, a CSV log, and Telegram.

Telegram setup (free, about 2 minutes):
  1. In Telegram, open @BotFather, send /newbot, and copy the token it gives you.
  2. Send any message to your new bot, then open
     https://api.telegram.org/bot<TOKEN>/getUpdates and copy "chat":{"id": ...}.
  3. Set TELEGRAM_BOT_TOKEN and TELEGRAM_CHAT_ID before starting the scanner.
"""

from __future__ import annotations

import csv
import json
import logging
import os
import urllib.parse
import urllib.request
from pathlib import Path

from events import WARN_DAYS, days_until
from strategy import Signal

log = logging.getLogger("alerts")
TF_ORDER = ["1mo", "1wk", "1d", "1h", "30m"]


def format_signal(s: Signal, provisional: bool) -> str:
    arrow = "🟢 LONG" if s.side == "LONG" else "🔴 SHORT"
    tag = " (bar still forming)" if provisional else ""
    market = "" if s.market == "NSE" else f" · {s.market}"
    c = s.currency
    return (f"{arrow} {s.symbol} [{s.timeframe}{market}]{tag}\n"
            f"  bar {s.date}{' IST' if ':' in s.date else ''} | close {c}{s.close} | stop {c}{s.stop}"
            f"{f' | lot {s.lot}' if s.lot else ''}\n"
            f"  SMI %K {s.smi_k} / %D {s.smi_d} | GoNoGo {s.gonogo}\n"
            f"  swing low {s.prev_swing_low} → {s.cur_swing_low}"
            f"{_results_line(s)}")


def _results_line(s: Signal) -> str:
    n = days_until(s.results)
    if n is not None and 0 <= n <= WARN_DAYS:
        return f"\n  ⚠️ results in {n} day{'s' if n != 1 else ''} ({s.results.rstrip('*')})"
    return ""


class Alerter:
    def __init__(self, log_path: Path, state_path: Path):
        self.log_path = log_path
        self.state_path = state_path
        self.token = os.environ.get("TELEGRAM_BOT_TOKEN")
        self.chat_id = os.environ.get("TELEGRAM_CHAT_ID")
        try:
            self.sent: set[str] = set(json.loads(state_path.read_text()))
        except (OSError, ValueError):
            self.sent = set()
        if not (self.token and self.chat_id):
            log.warning("Telegram not configured: alerts go to the console and %s only",
                        log_path.name)

    @staticmethod
    def key(s: Signal, provisional: bool) -> str:
        return f"{s.market}|{s.symbol}|{s.timeframe}|{s.side}|{s.date}|{'P' if provisional else 'C'}"

    def new(self, s: Signal, provisional: bool) -> bool:
        return self.key(s, provisional) not in self.sent

    def send(self, items: list[tuple[Signal, bool]], summary_only: bool = False) -> None:
        items = [(s, p) for s, p in items if self.new(s, p)]
        if not items:
            return
        items.sort(key=lambda x: (TF_ORDER.index(x[0].timeframe), x[0].side, x[0].symbol))
        self._log_csv(items)
        if summary_only:
            counts: dict[str, list[int]] = {}
            for s, _ in items:
                c = counts.setdefault(f"{s.market} {s.timeframe}", [0, 0])
                c[0 if s.side == "LONG" else 1] += 1
            text = "Setups already active at start-up (saved in alerts_log.csv):\n" + \
                "\n".join(f"  {tf}: {c[0]} long, {c[1]} short" for tf, c in counts.items()) + \
                "\nFrom now on you get one alert per new setup."
            print(text, flush=True)
            self.post(text)
        else:
            blocks = [format_signal(s, p) for s, p in items]
            for b in blocks:
                print(b, flush=True)
            if self.token and self.chat_id:
                self._telegram(blocks)
        for s, p in items:
            self.sent.add(self.key(s, p))
        self.state_path.write_text(json.dumps(sorted(self.sent)[-20000:]))

    def _log_csv(self, items) -> None:
        new = not self.log_path.exists()
        with self.log_path.open("a", newline="") as f:
            w = csv.writer(f)
            if new:
                w.writerow(["market", "timeframe", "symbol", "side", "bar", "provisional", "close",
                            "stop", "smi_k", "smi_d", "gonogo", "prev_swing_low",
                            "cur_swing_low", "lot"])
            for s, p in items:
                w.writerow([s.market, s.timeframe, s.symbol, s.side, s.date, p, s.close, s.stop,
                            s.smi_k, s.smi_d, s.gonogo, s.prev_swing_low, s.cur_swing_low,
                            s.lot])

    def _telegram(self, blocks: list[str]) -> None:
        # Telegram allows 4096 characters per message.
        msgs, cur = [], ""
        for b in blocks:
            if len(cur) + len(b) + 2 > 3800:
                msgs.append(cur)
                cur = ""
            cur += b + "\n\n"
        if cur:
            msgs.append(cur)
        for m in msgs:
            self.post(m)

    def post(self, text: str) -> None:
        if not (self.token and self.chat_id):
            return
        url = f"https://api.telegram.org/bot{self.token}/sendMessage"
        data = urllib.parse.urlencode({"chat_id": self.chat_id, "text": text}).encode()
        try:
            urllib.request.urlopen(url, data=data, timeout=20).read()
        except Exception as e:
            log.error("Telegram send failed: %s", e)
