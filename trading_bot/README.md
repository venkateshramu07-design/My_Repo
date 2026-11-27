# Chartink → Groww intraday bot

When your Chartink scan alert fires, Chartink calls this bot's webhook. For each stock, the bot:

1. Checks the safety rules: entry time window, max trades per day, one trade per stock per day, price range.
2. Places an **intraday (MIS) market order** on Groww, sized by `CAPITAL_PER_TRADE`.
3. Right after the fill, places an **OCO exit order** with a stop-loss (SL-M) and a target (LIMIT). When one fills, Groww cancels the other.
   If the OCO can't be placed, the bot **closes the position immediately**, so you're never left without a stop-loss.
4. At `SQUARE_OFF_TIME` (default 15:05 IST), cancels any open OCOs and closes the remaining positions.

It starts in **paper mode** (`DRY_RUN=true`). Nothing goes to Groww until you change that.

> Use at your own risk. Automated trading can lose money quickly. Test in paper mode, then with 1 share, before trading real size.

## What you need
- **Chartink premium plan** (needed for webhook alerts).
- **Groww Trade API subscription.** Enable it in Groww → Profile → Settings → Trading APIs. Generate an **API key and TOTP secret** there; this lets the bot log in every morning on its own.
- **A small cloud server with a static IP**, e.g. AWS Lightsail, DigitalOcean or Oracle Cloud free tier, running Ubuntu. Under SEBI's retail algo rules, Groww only accepts API orders from a **whitelisted static IP**. Add the server's IP in Groww's API settings.

## Setup on the server
```bash
sudo apt update && sudo apt install -y python3-venv git
git clone <your repo> && cd My_Repo/trading_bot
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env && nano .env          # fill in WEBHOOK_SECRET, risk settings
python -m pytest -q tests                  # should say all passed
gunicorn -w 1 --threads 8 -b 0.0.0.0:8000 'app:create_app()'
```
Generate a webhook secret with `python3 -c "import secrets; print(secrets.token_urlsafe(24))"`.
Open port 8000 in the server's firewall. Keep it to **one worker** (`-w 1`), because the trade limits and square-off timer live in that process.

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

Make sure the server clock is right (`timedatectl`). The bot converts all times to IST itself.

## Connect Chartink
In your scan, click **Create Alert**, choose **Webhook**, and use this URL:
```
http://<server-ip>:8000/webhook/<WEBHOOK_SECRET>
```
For a bearish/short scan, add `?side=SELL`:
`http://<server-ip>:8000/webhook/<WEBHOOK_SECRET>?side=SELL`

## Useful URLs
| URL | What it does |
|---|---|
| `GET /status/<secret>` | Today's trades and whether paper mode is on |
| `POST /squareoff/<secret>` | Panic button: close all of today's bot positions now (`curl -X POST ...`) |
| `GET /health` | Returns `ok` |

## Going live, step by step
1. **Paper mode for a few days** (`DRY_RUN=true`). Check `/status/<secret>` and the logs: are the right stocks, quantities, SL and targets showing up?
2. **Live with tiny size:** `DRY_RUN=false`, `CAPITAL_PER_TRADE` around the price of 1 share, `MAX_TRADES_PER_DAY=1`. In the Groww app, confirm the entry order, the OCO (stop-loss + target) and the 15:05 square-off all appear correctly.
3. Only then raise the size.

Kill switch: set `TRADING_ENABLED=false` and restart. Alerts are still logged but not traded.

## Settings (`.env`)
| Setting | Default | Meaning |
|---|---|---|
| `CAPITAL_PER_TRADE` | 10000 | ₹ per trade (before intraday leverage); qty = capital ÷ price |
| `STOP_LOSS_PCT` / `TARGET_PCT` | 1.0 / 2.0 | % from the fill price |
| `MAX_TRADES_PER_DAY` | 5 | Worst-case daily loss ≈ trades × capital × SL% |
| `ENTRY_START` / `ENTRY_END` | 09:20 / 14:45 | No new trades outside this window |
| `SQUARE_OFF_TIME` | 15:05 | Closes everything (before Groww's own auto square-off, which charges extra) |
| `MIN_PRICE` / `MAX_PRICE` | 20 / 5000 | Skip stocks outside this price range |
| `TELEGRAM_BOT_TOKEN` / `TELEGRAM_CHAT_ID` | – | Optional: get a message on every trade and error |

## Limitations
- Chartink alerts arrive a little after the candle that triggered them, so fills can differ from the trigger price. The bot uses the live price (LTP) to size the order and the actual fill price to set SL and target.
- Each stock is traded at most once per day, even if the scan fires again.
- If the server is down at 15:05, Groww's own auto square-off is the backstop. Check `/status` or Telegram for `NEEDS_ATTENTION` / `URGENT` messages.
