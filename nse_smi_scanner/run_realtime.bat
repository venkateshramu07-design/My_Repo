@echo off
REM Double-click to start the real-time NSE scanner. Leave this window open.
REM Put your Telegram details below (see README.md, "Telegram setup").
set TELEGRAM_BOT_TOKEN=
set TELEGRAM_CHAT_ID=
cd /d "%~dp0"
python -m pip install -q -r requirements.txt
python realtime.py
pause
