# NSE SMI + GoNoGo swing scanner (prototype)

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
* `scan.py`: scanner and order entry
* `broker.py`: paper broker, Zerodha Kite broker, position sizing
* `backtest.py`: walk-forward backtest
* `universe/`: Nifty 50/100/500 symbol lists from NSE archives
