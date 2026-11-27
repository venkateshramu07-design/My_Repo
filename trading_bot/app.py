"""Webhook server. Chartink posts alerts to /webhook/<WEBHOOK_SECRET>.

Run with ONE worker (the one-trade rule and the timers live in this process):
    gunicorn -w 1 --threads 8 -b 0.0.0.0:8000 'app:create_app()'
"""
import hmac
import json
import logging
import threading
import time

from flask import Flask, abort, jsonify, request

from broker import make_broker, now_ist
from config import Config
from engine import Engine
from store import Store

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")
log = logging.getLogger("app")


def create_app(cfg=None, broker=None, store=None, start_timer=True, background=True):
    cfg = cfg or Config.from_env()
    store = store or Store(cfg.db_path)
    engine = Engine(cfg, broker or make_broker(cfg), store)
    app = Flask(__name__)
    app.engine = engine

    def check_secret(secret):
        if not hmac.compare_digest(secret, cfg.webhook_secret):
            abort(404)

    @app.post("/webhook/<secret>")
    def webhook(secret):
        check_secret(secret)
        payload = request.get_json(silent=True)
        if payload is None:  # some senders post form data
            payload = request.form.to_dict() or {}
        store.log_alert(now_ist().isoformat(timespec="seconds"), json.dumps(payload))
        log.info("Alert %s: %s", payload.get("scan_name"), payload.get("stocks"))
        if background:  # answer Chartink immediately, trade in the background
            threading.Thread(target=_safe, args=(engine.handle_alert, payload), daemon=True).start()
            return jsonify(ok=True)
        return jsonify(ok=True, result=engine.handle_alert(payload))

    @app.get("/status/<secret>")
    def status(secret):
        check_secret(secret)
        return jsonify(
            dry_run=cfg.dry_run,
            trading_enabled=cfg.trading_enabled,
            now=now_ist().isoformat(timespec="seconds"),
            active_trade=store.active_trade(),
            trades_today=store.trades_on(now_ist().date().isoformat()),
        )

    @app.post("/exit/<secret>")
    def manual_exit(secret):
        """Panic button: close the active trade now."""
        check_secret(secret)
        with engine.lock:
            active = store.active_trade()
            ok = engine.exit(active, "manual exit") if active else True
        return jsonify(ok=ok)

    @app.get("/health")
    def health():
        return "ok"

    if start_timer:
        threading.Thread(target=_timer_loop, args=(engine,), daemon=True).start()
    return app


def _safe(fn, *args):
    try:
        log.info("Result: %s", fn(*args))
    except Exception:
        log.exception("Alert handling failed")


def _timer_loop(engine):
    while True:
        try:
            engine.tick()
        except Exception:
            log.exception("Timer error")
        time.sleep(5)


if __name__ == "__main__":
    create_app().run(host="0.0.0.0", port=8000)
