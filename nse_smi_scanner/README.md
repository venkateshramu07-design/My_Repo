# SMI + GoNoGo scanner and real-time alerts: NSE stocks, forex, gold (prototype)

This prototype scans NSE stocks for the setup described in the screen recording of 25 Nov 2026 (SWIGGY.NS on Yahoo Finance, weekly chart). It can also enter the trades it finds.

## The rules from the video

Indicators on the weekly chart:
* **Stochastic Momentum Index (10, 3, 3, 10, EMA)**: %K is the black line and %D is the red line.
* **GoNoGo Trend**: the coloured candles.

| | SHORT | LONG |
|---|---|---|
| SMI | %K **and** %D both below **-40** | %K **and** %D both above **+40** |
| Swing lows | current low **below** previous low (downtrend) | current low **above** previous low (uptrend) |
| GoNoGo Trend | NoGo | Go |

A signal is **fresh** on the first closed bar where all three conditions become true. Only fresh signals are traded, so a stock isn't re-entered every week while the setup holds.

The SMI formula matches Yahoo's chart: on SWIGGY's weekly bars it gives -87.44 / -64.63 for the latest week and -71.55 / -49.13 for 12 Jan 2026, the same values shown in the video.

## Assumptions (the video doesn't cover these)
* **GoNoGo Trend is approximated.** The original formula isn't public. `indicators.gonogo_trend` scores five parts: Heikin-Ashi close vs its 21 EMA, 12/26 EMA, MACD vs signal, RSI vs 50, and OBV vs its 21 EMA. Check a few signals against the chart. Use `--no-gonogo` to drop this filter.
* **Swing low** means a weekly low that is lower than the 2 bars on each side of it. A swing low is only confirmed 2 bars later, so the scanner never looks ahead.
* **Stop loss**: the current swing low for longs, the current swing high for shorts.
* **Backtest exit**: the stop is hit, %K crosses %D against the trade, or GoNoGo flips.
* **Closed bars only**: the unfinished current week is ignored until Friday's 15:30 IST close. Use `--include-open-bar` to include it.

## Real-time alerts (NSE stocks, forex, gold)
`realtime.py` runs all day and watches **30m, 1h, 1d, 1wk and 1mo** candles (nothing under 30 minutes) for:

| Market | Instruments | Hours (IST) |
|---|---|---|
| NSE | every stock, about 2,600 (EQ and BE series) | 09:15-15:30, Mon-Fri |
| Forex | EURUSD, GBPUSD, USDJPY, AUDUSD, USDCAD, USDCHF, NZDUSD, EURGBP, EURJPY, GBPJPY, USDINR, EURINR, GBPINR, JPYINR (`universe/forex.txt`) | about 24 h, Mon 03:30 to Sat 03:30 |
| Gold | COMEX gold futures `GC=F` in $/oz (see note below) | about 24 h, Mon-Fri | Whenever a candle closes and the rules above have just become true, it sends an alert.

```bash
pip install -r requirements.txt
set TELEGRAM_BOT_TOKEN=...   &  set TELEGRAM_CHAT_ID=...     (Windows cmd)
export TELEGRAM_BOT_TOKEN=... TELEGRAM_CHAT_ID=...          (Mac/Linux)
python realtime.py
```
Alert example:
```
🟢 LONG BAJFINANCE [1d]
  bar 2026-11-25 | close ₹1131.35 | stop ₹1078.15
  SMI %K 56.95 / %D 67.62 | GoNoGo Go
  swing low 899.15 → 1078.15
```
* **Gold spot:** Yahoo Finance has no gold spot series (`XAUUSD=X` returns nothing), so the scanner uses COMEX gold futures (`GC=F`). Futures trade a few dollars above spot, and that gap moves slowly, so SMI and swing-low signals are effectively the same as on a spot chart. For true spot XAU/USD or MCX gold, the data source needs changing (for example, a Twelve Data key or your broker's feed).
* **Forex:** Yahoo reports no volume for forex, so the volume (OBV) part of GoNoGo counts as neutral, and the other four parts decide Go/NoGo. Forex and gold candles follow UTC hours, so hourly candles run 10:30-11:30 IST and so on. A daily forex candle closes at 05:30 IST (midnight UTC); the weekly candle closes at the Friday close (about 03:30 Saturday IST).
* **Telegram setup:** in Telegram, open @BotFather and send `/newbot` to get a token. Send your bot any message, then open `https://api.telegram.org/bot<TOKEN>/getUpdates` and copy the `chat` → `id`. Without Telegram, alerts still print on screen and go to `alerts_log.csv`.
* **Timing:** prices refresh every 15 minutes while each market is open (NSE 09:15-15:40 IST; forex and gold around the clock on weekdays), just after 30m and 1h candles close (09:45, 10:15, 10:45 ...). Alerts arrive within about 2 minutes of the close. Daily alerts come at 15:30, weekly alerts on Friday at 15:30, and monthly alerts on the month's last trading day.
* **Start-up:** the first run each day downloads history for every stock (about 5 minutes, then cached in `.cache/`). The first scan sends **one summary** of the setups already active, not hundreds of messages. After that you get one alert per new setup, and an alert is never repeated, even after a restart.
* **Options:** `--markets forex,gold` (choose any of nse, forex, gold; default all three), `--forex EURUSD=X,USDINR=X` (your own pairs, as Yahoo tickers), `--timeframes 1h,1d,1wk` (fewer timeframes; choose from 30m, 1h, 1d, 1wk, 1mo), `--universe nifty500`, `--early` (also alert while a candle is still forming; it may disappear by the close), `--once` (one scan, then exit).
* **Load:** in testing on 4 CPU cores, one refresh of all 2,600 stocks took about 90 seconds, and a check of every candle on all timeframes took about 90 seconds. A normal 15-minute cycle checks only the timeframes whose candle just closed.

### Where to run it
The computer must stay on while the markets you watch are open. Forex and gold trade through the night, so a small cloud server suits them better than a PC:
* **Your own PC:** double-click `run_realtime.bat` (Windows) before 09:00 and leave it open.
* **A small cloud server** (e.g. Oracle Cloud free tier, AWS Lightsail, DigitalOcean, ₹400–800/month), so it runs without your PC: `nohup python3 realtime.py &`, or set it up as a service.

### Data limits (read this)
* Prices come from Yahoo Finance, which is free but not an official live feed. Bars can arrive late, and Yahoo may rate-limit heavy use. The scanner retries, but a cycle can still be missed.
* For tick-accurate real-time data, use your broker's live feed (Zerodha Kite Connect is about ₹500/month and streams up to 3,000 stocks on one connection). Swapping `marketdata.py` to that feed is the natural next step.
* Alerts are a prompt to look at the chart, not advice. Check the setup on the chart before you trade.

## Usage
```bash
pip install -r requirements.txt

python scan.py                              # fresh signals, Nifty 50, weekly
python scan.py --universe nifty500 --all    # every stock where the setup is active
python scan.py --universe SWIGGY,RELIANCE   # chosen stocks
python scan.py --trade                      # record fresh signals in paper_trades.csv
python backtest.py --universe nifty50 --period 10y
```
Sizing: `--capital-per-trade` (default ₹50,000) and `--risk-per-trade` (default ₹1,000 loss to the stop) together set the quantity.

### Live orders (Zerodha Kite Connect)
```bash
pip install kiteconnect
export KITE_API_KEY=...  KITE_ACCESS_TOKEN=...   # the access token expires daily
python scan.py --trade --live
```
Longs are placed as CNC market orders with an SL-M stop. **Shorts:** NSE cash shorts must be squared off the same day, so holding a weekly short needs futures or options. Live short orders are refused unless you pass `--allow-intraday-shorts`.

## Backtest snapshot (weekly bars, 10 years, today's index members, no costs)
| Universe | Trades | Win rate | Avg / trade | Longs avg | Shorts avg |
|---|---|---|---|---|---|
| Nifty 50 | 818 | 37.4% | +0.19% | +1.65% | -2.52% |
| Nifty 500 | 6,717 | 39.2% | +0.64% | +2.90% | -1.88% |

Longs have a small edge because a few big winners pay for many small losses. Shorts lose money on average with these exit rules. The numbers have survivorship bias because they use today's index members, and they include no costs or slippage. Treat them as a sanity check, not as evidence that the strategy works.

## Files
* `indicators.py`: SMI, GoNoGo approximation, swing pivots
* `strategy.py`: entry rules and signal object
* `data.py`: Yahoo Finance NSE data (`.NS`) and symbol lists
* `realtime.py`: real-time scanner for all stocks and timeframes, with alerts
* `marketdata.py`: live candle store (30m/1h/1d/1wk/1mo, built from a 15-minute price feed)
* `globalmarket.py`: candle store for forex and gold (24-hour markets)
* `alerts.py`: Telegram, console and CSV alerts
* `scan.py`: one-off weekly scanner and order entry
* `broker.py`: paper broker, Zerodha Kite broker, position sizing
* `backtest.py`: walk-forward backtest
* `universe/`: NSE symbol lists from NSE archives (all stocks, Nifty 50/100/500), plus `forex.txt` and `gold.txt`
