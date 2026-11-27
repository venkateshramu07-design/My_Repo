"""Webhook server. Chartink posts alerts to /webhook/<WEBHOOK_SECRET>.

Run with ONE worker (the square-off timer and trade limits live in this process):
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


def create_app(cfg=None, broker=None, store=None, start_scheduler=True, background=True):
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
        side = request.args.get("side")
        if side and side.upper() not in ("BUY", "SELL"):
            abort(400)
        log.info("Alert %s: %s", payload.get("scan_name"), payload.get("stocks"))
        if background:  # answer Chartink immediately, trade in the background
            threading.Thread(target=engine.handle_alert, args=(payload, side), daemon=True).start()
            return jsonify(ok=True)
        return jsonify(ok=True, results=engine.handle_alert(payload, side))

    @app.get("/status/<secret>")
    def status(secret):
        check_secret(secret)
        return jsonify(
            dry_run=cfg.dry_run,
            trading_enabled=cfg.trading_enabled,
            now=now_ist().isoformat(timespec="seconds"),
            trades_today=store.trades_on(now_ist().date().isoformat()),
        )

    @app.post("/squareoff/<secret>")
    def squareoff(secret):
        """Manual panic button: close all of today's bot positions now."""
        check_secret(secret)
        engine.square_off_all()
        return jsonify(ok=True)

    @app.get("/health")
    def health():
        return "ok"

    if start_scheduler:
        threading.Thread(target=_square_off_loop, args=(engine,), daemon=True).start()
    return app


def _square_off_loop(engine):
    while True:
        now = now_ist()
        day = now.date().isoformat()
        if now.weekday() < 5 and now.time() >= engine.cfg.square_off_time and engine.squared_off_day != day:
            log.info("Square-off time")
            try:
                engine.square_off_all()
            except Exception:
                log.exception("Square-off loop error")
        time.sleep(20)


if __name__ == "__main__":
    create_app().run(host="0.0.0.0", port=8000)
