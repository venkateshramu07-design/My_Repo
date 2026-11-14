"""HTTP server: receives Chartink webhook alerts and runs the exit monitor.

Run:  python -m bot.app          (listens on 127.0.0.1:8000; put HTTPS in front)
"""
from __future__ import annotations

import hmac
import json
import logging
import os
import threading
from contextlib import asynccontextmanager
from urllib.parse import parse_qs

from fastapi import BackgroundTasks, FastAPI, HTTPException, Request

from .broker import GrowwBroker, PaperBroker
from .config import Settings
from .engine import Engine
from .store import Store

log = logging.getLogger("bot")


def load_dotenv(path: str = ".env") -> None:
    """Minimal .env loader (does not override variables already set)."""
    if not os.path.exists(path):
        return
    for line in open(path, encoding="utf-8"):
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            k, v = line.split("=", 1)
            os.environ.setdefault(k.strip(), v.strip())


def parse_chartink(body: bytes, content_type: str = "") -> list[tuple[str, float | None]]:
    """Chartink sends e.g. {"stocks": "SBIN,INFY", "trigger_prices": "625.5,1650", ...}."""
    try:
        data = json.loads(body or b"{}")
    except ValueError:
        data = {k: v[0] for k, v in parse_qs(body.decode("utf-8", "replace")).items()}
    if not isinstance(data, dict):
        return []
    stocks = [s.strip().upper() for s in str(data.get("stocks") or "").split(",") if s.strip()]
    prices = [p.strip() for p in str(data.get("trigger_prices") or "").split(",")]
    out: list[tuple[str, float | None]] = []
    for i, sym in enumerate(stocks):
        try:
            px = float(prices[i]) if i < len(prices) and prices[i] else None
        except ValueError:
            px = None
        out.append((sym, px))
    return list(dict.fromkeys(out))


def build_engine(settings: Settings) -> Engine:
    live = None
    if settings.groww_api_key:
        live = GrowwBroker(settings.groww_api_key, settings.groww_totp_secret,
                           settings.groww_api_secret)
    if settings.dry_run:
        broker = PaperBroker(price_source=live.ltp if live else None)
        log.warning("DRY_RUN=true: paper trading, NO real orders will be placed")
    else:
        broker = live
        log.warning("DRY_RUN=false: LIVE trading with product %s", settings.product)
    return Engine(settings, Store(settings.db_path), broker)


def create_app(engine: Engine, start_monitor: bool = True) -> FastAPI:
    token = engine.s.webhook_token
    stop = threading.Event()

    @asynccontextmanager
    async def lifespan(_app):
        if start_monitor:
            threading.Thread(target=engine.run_forever, args=(stop,), daemon=True).start()
        yield
        stop.set()

    app = FastAPI(title="Chartink -> Groww bot", lifespan=lifespan)

    def check(request: Request) -> None:
        given = request.query_params.get("token", "")
        if not hmac.compare_digest(given.encode(), token.encode()):
            raise HTTPException(status_code=403, detail="bad token")

    @app.post("/webhook/chartink")
    async def chartink(request: Request, background: BackgroundTasks):
        check(request)
        body = await request.body()
        signals = parse_chartink(body, request.headers.get("content-type", ""))
        log.info("Chartink alert: %s", signals)
        # Reply to Chartink immediately; place orders in the background.
        background.add_task(engine.handle_signal, signals)
        return {"received": [s for s, _ in signals]}

    @app.get("/health")
    def health():
        return {"ok": True, "dry_run": engine.s.dry_run, "paused": engine.paused,
                "product": engine.s.product}

    @app.get("/trades")
    def trades(request: Request, limit: int = 100):
        check(request)
        return [t.__dict__ for t in engine.store.recent(limit)]

    @app.post("/pause")
    def pause(request: Request):
        check(request)
        engine.paused = True
        return {"paused": True}

    @app.post("/resume")
    def resume(request: Request):
        check(request)
        engine.paused = False
        return {"paused": False}

    return app


def main() -> None:
    import uvicorn

    load_dotenv()
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s",
                        handlers=[logging.StreamHandler(), logging.FileHandler("bot.log")])
    settings = Settings.from_env()
    app = create_app(build_engine(settings))
    uvicorn.run(app, host=os.getenv("HOST", "127.0.0.1"), port=int(os.getenv("PORT", "8000")))


if __name__ == "__main__":
    main()
