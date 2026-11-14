"""HTTP server: receives Chartink webhook alerts and runs the exit monitor.

Run:  python -m bot.app          (listens on 127.0.0.1:8000; put HTTPS in front)
"""
from __future__ import annotations

import hmac
import html
import json
import logging
import os
import threading
from contextlib import asynccontextmanager
from urllib.parse import parse_qs

from fastapi import BackgroundTasks, FastAPI, HTTPException, Request
from fastapi.responses import HTMLResponse, RedirectResponse

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

    def _set_paused(request: Request, value: bool):
        check(request)
        engine.paused = value
        if request.query_params.get("ui"):
            return RedirectResponse(f"/dashboard?token={token}", status_code=303)
        return {"paused": value}

    @app.post("/pause")
    def pause(request: Request):
        return _set_paused(request, True)

    @app.post("/resume")
    def resume(request: Request):
        return _set_paused(request, False)

    @app.get("/dashboard", response_class=HTMLResponse)
    def dashboard(request: Request):
        check(request)
        return render_dashboard(engine, token)

    return app


def render_dashboard(engine: Engine, token: str) -> str:
    """Small phone-friendly status page."""
    e = html.escape
    trades = engine.store.recent(50)
    closed = [t for t in trades if t.status == "CLOSED" and t.sell_price]
    pnl = sum((t.sell_price - t.avg_price) * t.quantity for t in closed)
    rows = []
    for t in trades:
        ltp_note = f"{t.target_price:.2f}" if t.target_price else "-"
        rows.append(
            f"<tr><td>{e(t.created_at[5:16].replace('T', ' '))}</td><td><b>{e(t.symbol)}</b></td>"
            f"<td>{t.quantity}</td><td>{t.avg_price or '-'}</td><td>{ltp_note}</td>"
            f"<td>{t.sell_price or '-'}</td><td class='{e(t.status)}'>{e(t.status)}</td></tr>")
    mode = "PAPER (no real orders)" if engine.s.dry_run else "LIVE"
    state = "PAUSED" if engine.paused else "RUNNING"
    action = "resume" if engine.paused else "pause"
    return f"""<!doctype html><html><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1"><title>Trading bot</title>
<style>
body{{font-family:system-ui,sans-serif;margin:0;padding:16px;background:#f6f7f9;color:#111}}
.card{{background:#fff;border-radius:12px;padding:14px;margin-bottom:12px;box-shadow:0 1px 3px #0002}}
.big{{font-size:20px;font-weight:700}} .muted{{color:#666;font-size:13px}}
button{{width:100%;padding:14px;font-size:17px;border:0;border-radius:10px;color:#fff;
background:{'#16a34a' if engine.paused else '#dc2626'}}}
table{{width:100%;border-collapse:collapse;font-size:12px}} td,th{{padding:6px 4px;border-bottom:1px solid #eee;text-align:left}}
.OPEN{{color:#2563eb}} .CLOSED{{color:#16a34a}} .FAILED{{color:#dc2626}} .BUY_PENDING,.SELL_PENDING{{color:#d97706}}
.wrap{{overflow-x:auto}}
</style></head><body>
<div class="card"><div class="big">{state} &middot; {mode}</div>
<div class="muted">Product {e(engine.s.product)} &middot; qty {engine.s.quantity} &middot; target &gt;{engine.s.target_profit_pct}%</div>
<div class="muted">Closed trades shown: {len(closed)} &middot; gross P&amp;L &#8377;{pnl:,.2f}</div></div>
<form class="card" method="post" action="/{action}?token={e(token)}&ui=1">
<button type="submit">{'Resume new buys' if engine.paused else 'Pause new buys'}</button>
<div class="muted" style="margin-top:6px">Pausing stops new entries; exits keep running.</div></form>
<div class="card wrap"><table><tr><th>Time</th><th>Stock</th><th>Qty</th><th>Buy</th><th>Target</th><th>Sold</th><th>Status</th></tr>
{''.join(rows) or '<tr><td colspan=7>No trades yet</td></tr>'}</table></div>
<div class="muted">Refresh the page to update.</div></body></html>"""


def main() -> None:
    import uvicorn

    load_dotenv()
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s",
                        handlers=[logging.StreamHandler(), logging.FileHandler("bot.log")])
    settings = Settings.from_env()
    app = create_app(build_engine(settings))
    uvicorn.run(app, host=os.getenv("HOST", "127.0.0.1"), port=int(os.getenv("PORT", "8000")),
                access_log=False)  # keep the secret token out of logs


if __name__ == "__main__":
    main()
