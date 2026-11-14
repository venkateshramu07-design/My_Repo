#!/usr/bin/env bash
# One-command installer for an Ubuntu 22.04/24.04 server. Safe to re-run (updates code,
# keeps your .env and trades.db). Usage, as root:
#   bash <(curl -fsSL https://raw.githubusercontent.com/venkateshramu07-design/My_Repo/ccr-8f94bfa5-5nixv7/trading_bot/deploy/setup.sh)
set -euo pipefail
REPO_URL=${REPO_URL:-https://github.com/venkateshramu07-design/My_Repo.git}
BRANCH=${BRANCH:-ccr-8f94bfa5-5nixv7}
APP_DIR=/opt/trading_bot

[ "$(id -u)" -eq 0 ] || { echo "Please run as root (type: sudo -i  then run again)"; exit 1; }
export DEBIAN_FRONTEND=noninteractive

echo "==> Installing packages (takes 1-3 minutes)"
apt-get update -qq
apt-get install -y -qq git python3-venv python3-pip caddy ufw curl >/dev/null

echo "==> Downloading bot code"
id bot &>/dev/null || useradd --system --home-dir "$APP_DIR" --shell /usr/sbin/nologin bot
tmp=$(mktemp -d)
git clone -q --depth 1 -b "$BRANCH" "$REPO_URL" "$tmp/repo"
mkdir -p "$APP_DIR"
cp -r "$tmp/repo/trading_bot/." "$APP_DIR/"
rm -rf "$tmp"
python3 -m venv "$APP_DIR/.venv"
"$APP_DIR/.venv/bin/pip" install -q --upgrade pip
"$APP_DIR/.venv/bin/pip" install -q -r "$APP_DIR/requirements.txt"

if [ ! -f "$APP_DIR/.env" ]; then
  echo "==> Groww credentials (from groww.in > Trading APIs > Generate TOTP key)"
  read -rp  "Groww TOTP API key: " GKEY </dev/tty
  read -rsp "Groww TOTP secret (typing is hidden): " GSECRET </dev/tty; echo
  GKEY="$GKEY" GSECRET="$GSECRET" python3 - "$APP_DIR" <<'PY'
import os, secrets, sys
d = sys.argv[1]
vals = {"GROWW_API_KEY": os.environ["GKEY"].strip(),
        "GROWW_TOTP_SECRET": os.environ["GSECRET"].strip().replace(" ", ""),
        "WEBHOOK_TOKEN": secrets.token_hex(24)}
out = []
for line in open(f"{d}/.env.example"):
    k = line.split("=", 1)[0]
    out.append(f"{k}={vals[k]}\n" if k in vals else line)
open(f"{d}/.env", "w").write("".join(out))
PY
fi
chown -R bot:bot "$APP_DIR"
chmod 600 "$APP_DIR/.env"

IP=$(curl -4 -fsS https://api.ipify.org)
DOMAIN=${DOMAIN:-${IP//./-}.sslip.io}

echo "==> Configuring HTTPS for $DOMAIN"
cat > /etc/caddy/Caddyfile <<CADDY
$DOMAIN {
    reverse_proxy 127.0.0.1:8000
}
CADDY
systemctl enable -q caddy
systemctl restart caddy

echo "==> Installing the bot service"
cat > /etc/systemd/system/trading-bot.service <<UNIT
[Unit]
Description=Chartink to Groww trading bot
After=network-online.target
Wants=network-online.target

[Service]
User=bot
WorkingDirectory=$APP_DIR
ExecStart=$APP_DIR/.venv/bin/python -m bot.app
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
UNIT
systemctl daemon-reload
systemctl enable -q trading-bot
systemctl restart trading-bot

ufw allow OpenSSH >/dev/null; ufw allow 80/tcp >/dev/null; ufw allow 443/tcp >/dev/null
ufw --force enable >/dev/null

echo "==> Checking Groww login (places no orders)"
(cd "$APP_DIR" && sudo -u bot .venv/bin/python -m bot.check 2>&1 | grep -E "Login OK|LTP|Config|Error|error" ) \
  || echo "!! Groww login failed. Fix the key/secret with:  nano $APP_DIR/.env  then:  systemctl restart trading-bot"

TOKEN=$(grep '^WEBHOOK_TOKEN=' "$APP_DIR/.env" | cut -d= -f2 | cut -d'#' -f1 | tr -d ' ')
sleep 3
cat <<DONE

================================================================
 DONE. Save these somewhere private (they contain your secret token):

 Server static IP (register with Groww if asked):  $IP

 Phone dashboard (bookmark it):
   https://$DOMAIN/dashboard?token=$TOKEN

 Signals: $(grep '^SIGNAL_SOURCE=' "$APP_DIR/.env" | cut -d' ' -f1 || echo 'SIGNAL_SOURCE=groww') (groww = built-in scanner, no Chartink needed)
 Chartink webhook URL (only if SIGNAL_SOURCE=chartink or both):
   https://$DOMAIN/webhook/chartink?token=$TOKEN

 Mode: $(grep '^DRY_RUN=' "$APP_DIR/.env" | cut -d' ' -f1)  (paper trading until you change it)
================================================================
DONE
