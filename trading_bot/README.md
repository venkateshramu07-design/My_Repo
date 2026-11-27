# Chartink → Groww intraday bot

## The rules it follows
1. **Signal = Chartink alert.** When your Chartink alert sends a stock, the signal is true for that stock.
2. **Entry:** when a signal arrives between **11:00 and 15:00 IST**, the bot immediately **short sells** (intraday/MIS, market order). Quantity comes from a fixed margin per trade (see below).
3. **One trade at a time.** While a trade is open, alerts for other stocks are ignored. If one alert has several stocks, the first one is taken.
4. **Hold** for as long as Chartink keeps sending the same stock.
5. **Exit as soon as the signal stops** (buy back at market). That happens when either:
   - an alert from the same scan arrives **without** the stock, or
   - no alert has included the stock for `SIGNAL_TIMEOUT_MINUTES` (default 6).
6. **No new trades from 15:00.** A trade opened before 15:00 keeps following the signal.
7. **15:15 IST:** any open trade is closed, whatever the signal says.

After an exit, the next signal (before 15:00) opens a new trade, including the same stock again.

It starts in **paper mode** (`DRY_RUN=true`). Nothing goes to Groww until you change that.

> Use at your own risk. There is no stop-loss: the exit depends on the signal and the 15:15 cut-off. Test in paper mode, then with a small margin, before trading full size.

## Quantity and margin: fixed margin per trade
Each trade uses the same amount of margin, `MARGIN_PER_TRADE` (default ₹25,000), at Groww's intraday leverage, `LEVERAGE` (default 5x):

```
quantity = (MARGIN_PER_TRADE × LEVERAGE) ÷ share price   (rounded down)
```

| Stock price | Quantity (₹25,000 × 5) | Order value |
|---|---|---|
| ₹1,327.60 (Adani Green) | 94 | ≈ ₹1,24,800 |
| ₹200 | 625 | ₹1,25,000 |
| ₹5,000 | 25 | ₹1,25,000 |

In live mode the price is Groww's latest traded price at the moment of entry. If that can't be read, it uses Chartink's trigger price. If the margin isn't enough for even 1 share, the trade is skipped.
Groww's real margin rate differs slightly by stock (your screenshot works out to about 5.2x). So the margin actually blocked will be close to, not exactly, `MARGIN_PER_TRADE`. Keep enough balance above it.

## Important: Chartink alert setup
The "stay in while the signal is true" rule only works if Chartink **sends the alert repeatedly**: every candle or every few minutes, with all the stocks currently in the scan. When you create the alert:
- Set the alert to run on your scan's candle timeframe (e.g. every 5 minutes).
- Set `SIGNAL_TIMEOUT_MINUTES` to that interval + 1 (5-min alerts → `6`, 1-min alerts → `2`, 15-min alerts → `16`).
- Webhook URL: `http://<server-ip>:8000/webhook/<WEBHOOK_SECRET>`

You can check this in paper mode: `/status/<secret>` shows `last_signal_at` updating each time Chartink sends the stock again.

## What you need
- **Chartink premium plan** (needed for webhook alerts).
- **Groww Trade API subscription.** Enable it in Groww → Profile → Settings → Trading APIs. Generate an **API key and TOTP secret** there; this lets the bot log in every morning on its own.
- **A small cloud server with a static IP**, e.g. AWS Lightsail, DigitalOcean or Oracle Cloud, running Ubuntu. Add the server's IP in Groww's API settings; under SEBI's rules, API orders must come from a whitelisted static IP.

## Setup on the server
```bash
sudo apt update && sudo apt install -y python3-venv git
git clone <your repo> && cd My_Repo/trading_bot
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env && nano .env          # fill in WEBHOOK_SECRET etc.
python -m pytest -q tests                  # should say all passed
gunicorn -w 1 --threads 8 -b 0.0.0.0:8000 'app:create_app()'
```
Generate a webhook secret with `python3 -c "import secrets; print(secrets.token_urlsafe(24))"`.
Open port 8000 in the server firewall. Keep **one worker** (`-w 1`).

To keep it running after reboots, create a systemd service:
```ini
# /etc/systemd/system/tradebot.service
[Unit]
Description=Chartink Groww bot
After=network.target
[Service]
WorkingDirectory=/home/ubuntu/My_Repo/trading_bot
ExecStart=/home/ubuntu/My_Repo/trading_bot/.venv/bin/gunicorn -w 1 --threads 8 -b 0.0.0.0:8000 app:create_app()
Restart=always
[Install]
WantedBy=multi-user.target
```
Then run `sudo systemctl enable --now tradebot`. Logs: `journalctl -u tradebot -f`.

## Useful URLs
| URL | What it does |
|---|---|
| `GET /status/<secret>` | Paper/live mode, the active trade, today's trades with entry/exit and reason |
| `POST /exit/<secret>` | Panic button: close the active trade now (`curl -X POST ...`) |
| `GET /health` | Returns `ok` |

## Settings (`.env`)
| Setting | Default | Meaning |
|---|---|---|
| `SIDE` | SELL | SELL = short sell on signal; BUY = buy on signal |
| `MARGIN_PER_TRADE` | 25000 | ₹ margin used per trade |
| `LEVERAGE` | 5 | Intraday leverage; qty = margin × leverage ÷ price |
| `EXCHANGE` | NSE | NSE or BSE (Chartink scans NSE data) |
| `ENTRY_START` / `ENTRY_END` | 11:00 / 15:00 | New trades only in this window |
| `SQUARE_OFF_TIME` | 15:15 | Forced exit |
| `SIGNAL_TIMEOUT_MINUTES` | 6 | Exit if the stock isn't re-sent within this time |
| `TRADING_ENABLED` | true | false = log alerts only (kill switch) |
| `TELEGRAM_BOT_TOKEN` / `TELEGRAM_CHAT_ID` | – | Optional: a message on every entry, exit and error |

## Going live
1. Paper mode for a few days. Check that entries, holds and exits in `/status` match what you see on Chartink.
2. `DRY_RUN=false` with a small `MARGIN_PER_TRADE` (e.g. 1000). Confirm in the Groww app that the short sell, the buy-back and the margin blocked look right.
3. Then set `MARGIN_PER_TRADE` to your real amount.

If an exit order fails, the bot keeps retrying every 5 seconds and sends you an URGENT message once. If the server is down, Groww's own intraday auto square-off is the last backstop.
