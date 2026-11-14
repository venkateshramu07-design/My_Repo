# Chartink → Groww MTF trading bot

Buys a stock on Groww (MTF) as soon as your Chartink scan alerts on it, and sells it once
profit is **greater than 7 % of the total order value**.

```
Chartink scan alert ──HTTPS webhook──► this bot ──► Groww Trade API
                                          │
                                          └─ every 5 s: track fills, check LTP vs target, sell
```

## Trading rules (all configurable in `.env`)

| Rule | Default |
|---|---|
| Product | `MTF` (Groww decides the actual leverage per stock; 3.05x is not something the API lets you set) |
| Size | 25 shares per signal (`SIZING_MODE=fixed_qty`, `QUANTITY=25`). Or `SIZING_MODE=capital`: qty = ⌊capital × 3.05 ÷ price⌋ |
| Entry | LIMIT buy at LTP + 0.5 % (fills immediately, caps slippage), between 09:20 and 15:15 IST |
| Exit | When LTP ≥ target, LIMIT sell at target. Target = smallest 0.05 price > avg × 1.07, so profit > 7 % of `qty × avg buy price` |
| Repeats | Never buys a stock already held/pending; no re-entry in the same stock the same day |
| Limits | Max 10 open trades, max 10 new trades/day, max ₹50,000 per order |
| Safety | `DRY_RUN=true` paper-trades by default. `POST /pause` stops new entries |

The 7 % is **gross**: brokerage, taxes and MTF interest come out of it.
There is **no stop-loss**, as requested. A stock that never rises 7 % is held indefinitely,
with MTF interest accruing daily and possible margin calls if it falls. Consider adding one.

## Setup

1. **Groww**: subscribe to the Trading API, then on the Cloud API Keys page create a
   **TOTP** key. Put the key and TOTP secret in `.env`.
2. **Server**: a small Linux VPS with a **static IP** (SEBI's retail algo rules require API
   orders to come from a registered static IP; register it with Groww if asked), plus a
   domain with HTTPS (e.g. Caddy: `bot.example.com { reverse_proxy 127.0.0.1:8000 }`).
3. Install and configure:
   ```bash
   python3 -m venv .venv && . .venv/bin/activate
   pip install -r requirements.txt
   cp .env.example .env    # edit: credentials, WEBHOOK_TOKEN (random 32+ chars, no '#')
   python -m bot.check     # logs in, shows margin and an LTP; places no orders
   python -m bot.app       # starts the server on 127.0.0.1:8000
   ```
4. **Chartink**: open your scan → *Create Alert* → frequency *every candle / 5 min*
   (market hours) → enable **Webhook** and set the URL to
   `https://bot.example.com/webhook/chartink?token=<WEBHOOK_TOKEN>`.
   Webhook alerts need a Chartink premium plan.
5. Run it as a service so it restarts on reboot (`systemd` unit with
   `WorkingDirectory=` this folder and `ExecStart=.venv/bin/python -m bot.app`).

## Going live

1. Paper-trade at least 1–2 weeks with `DRY_RUN=true`. Paper fills use real Groww LTPs.
   Check `bot.log` and `GET /trades?token=...`.
2. **Test MTF once by hand**: Groww's SDK (v1.5.0) defines `PRODUCT_MTF = "MTF"`, but the
   public docs list only CNC/MIS/NRML. Set `DRY_RUN=false`, `MAX_NEW_TRADES_PER_DAY=1`,
   and confirm the first real MTF buy and sell succeed in the Groww app. If Groww rejects
   `MTF`, the log shows the reason; switch to `PRODUCT=CNC` (no leverage) or contact Groww.
3. Raise the limits gradually.

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/webhook/chartink?token=…` | Chartink alert (`stocks`, `trigger_prices`) |
| GET | `/health` | status, dry-run flag |
| GET | `/trades?token=…` | recent trades |
| POST | `/pause?token=…` / `/resume?token=…` | stop / restart new entries (exits keep running) |

## Notes

- The bot must be running during market hours: exits are checked by polling, not by an
  order resting at the exchange.
- Restarts are safe: trades are stored in `trades.db` and resumed.
- Tests: `pip install -r requirements-dev.txt && pytest -q`.

This is software, not investment advice. Leveraged trading can lose more than your margin.
