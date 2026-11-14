# Groww MTF trading bot (built-in screener, Chartink optional)

Scans the market itself using Groww data, buys a stock on Groww (MTF) as soon as it meets
your screener conditions, and sells it once profit is **greater than 7 % of the total order value**.

```
built-in scanner (every 5 min, Groww candles) ──┐
Chartink webhook (optional)  ───────────────────┴─► engine ──► Groww Trade API
                                                      └─ every 5 s: track fills, check LTP vs target, sell
```

## Built-in screener (replaces Chartink)

`SIGNAL_SOURCE=groww` (default). Conditions, all required:

| Check | Timeframes |
|---|---|
| SMI %K(10,3,3) > 40, SMI %D(10,3,3,10) > 40, %K > %D | 4 hour, 1 hour, 15 minute (latest, still-forming candle) |
| Today's volume > 50,000 · close < 1,000 · market cap > 10,000 Cr | – |

How: once a week it checks the market cap of all ~2,700 NSE stocks (`get_quote`, takes ~10 min,
cached in `universe.json`). At 08:30 each trading day it loads 60 days of 15-minute candles
per stock. Every 5 minutes from 09:20 to 15:15 it fetches today's 15-minute candles, rebuilds
1-hour and 4-hour candles aligned to 09:15 (4h = 09:15–13:15, 13:15–15:30), computes SMI,
and buys the matches.

SMI = 100 × EMA(EMA(close − mid, 3), 3) ÷ (0.5 × EMA(EMA(HH − LL, 3), 3)) over 10 bars;
%D = EMA(%K, 10). **Compare with Chartink once** before going live:
`python -m bot.scan SBIN TATASTEEL` prints the values per timeframe, and
`python -m bot.scan --all` runs a full scan (no orders) to compare with Chartink's results.
Small differences are normal (data vendor, candle timing); the pass/fail list should mostly agree.

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

## Setup from a phone (easiest)

On a server, run this one command as root. It installs everything (Python, HTTPS via
Caddy + a free `<ip>.sslip.io` address, a systemd service, and a firewall), asks for your
Groww key, then prints your Chartink webhook URL and a phone dashboard link:

```bash
bash <(curl -fsSL https://raw.githubusercontent.com/venkateshramu07-design/My_Repo/ccr-8f94bfa5-5nixv7/trading_bot/deploy/setup.sh)
```

Re-running it updates the code and keeps your `.env` and trade history.
Dashboard: `https://<ip>.sslip.io/dashboard?token=<WEBHOOK_TOKEN>` (status, trades, pause button).

## Manual setup

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
