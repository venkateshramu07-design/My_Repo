# SMI multi-timeframe EA (MetaTrader 5, Octa)

Buys when **all** are true; closes the trade as soon as **any** is false:

| Timeframe | Conditions |
|---|---|
| Weekly, 4 Hour, 1 Hour | SMI %K(10,3,3) > 40 · SMI %D(10,3,3,10) > 40 · %K > %D |

Checks once a minute on the current (forming) candle, like Chartink's `[0]`. Long trades only.
The chart shows live %K/%D values per timeframe so you can compare with Chartink.

## Install
1. In MT5 (desktop, on the VPS): **Tools → MetaQuotes Language Editor**.
2. **File → New → Expert Advisor (template)** → name `SMI_MultiTF_EA` → Finish.
3. Select everything in the new file, delete it, paste the contents of `SMI_MultiTF_EA.mq5`
   (open the "Raw" link on GitHub, select all, copy).
4. Press **Compile** (F7): it should say `0 errors`.
5. Back in MT5: open a chart (e.g. EURUSD), drag **SMI_MultiTF_EA** from Navigator → Expert Advisors
   onto it, tick **Allow Algo Trading**, set **Lots**, OK. Turn on the **Algo Trading** button in the toolbar.

## Settings
| Input | Default | Meaning |
|---|---|---|
| Lots | 0.01 | Trade size |
| UseClosedCandles | false | true = use last closed candles (fewer in/out flips) |
| CheckEverySeconds | 60 | How often conditions are checked |
| StopLossPoints | 0 | Safety stop-loss in points (0 = none). Strongly recommended |

One EA per chart: attach it to one chart per pair you want to trade.
