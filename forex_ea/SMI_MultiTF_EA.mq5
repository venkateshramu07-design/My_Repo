//+------------------------------------------------------------------+
//| SMI_MultiTF_EA.mq5                                               |
//| Buys when ALL of these are true, closes as soon as ANY is false: |
//|   on Weekly, 4 Hour and 1 Hour:                                  |
//|     SMI %K(10,3,3) > 40                                          |
//|     SMI %D(10,3,3,10) > 40                                       |
//|     SMI %K > SMI %D                                              |
//| Same rules as the Chartink screen ([0] = current candle).        |
//+------------------------------------------------------------------+
#property copyright "SMI multi-timeframe EA"
#property version   "1.00"
#property description "Enter long when SMI %K/%D conditions hold on W1, H4 and H1; exit when they stop holding."

#include <Trade/Trade.mqh>

//--- inputs (shown in the EA settings window)
input double Lots              = 0.01;   // Lot size per trade
input double Level             = 40.0;   // SMI level (%K and %D must be above)
input int    SmiLength         = 10;     // SMI %K length
input int    Smooth1           = 3;      // SMI first smoothing
input int    Smooth2           = 3;      // SMI second smoothing
input int    SignalLength      = 10;     // SMI %D (signal) length
input bool   UseClosedCandles  = false;  // false = current candle like Chartink [0]; true = last closed candle (fewer flip-flops)
input int    CheckEverySeconds = 60;     // How often to re-check the conditions
input int    StopLossPoints    = 0;      // Safety stop-loss in points (0 = none). Strongly recommended.
input int    HistoryBars       = 300;    // Candles used to calculate SMI
input ulong  MagicNumber       = 240518; // ID so the EA only manages its own trades

CTrade   trade;
datetime lastCheck = 0;

//+------------------------------------------------------------------+
//| Stochastic Momentum Index on any timeframe.                      |
//| %K = 100 * EMA(EMA(close - mid, s1), s2) / (0.5 * EMA(EMA(HH-LL, s1), s2))
//| %D = EMA(%K, SignalLength)                                       |
//+------------------------------------------------------------------+
bool CalcSMI(ENUM_TIMEFRAMES tf, double &k, double &d)
{
   MqlRates r[];
   ArraySetAsSeries(r, false);                       // r[0] = oldest candle
   int shift = UseClosedCandles ? 1 : 0;
   int got = CopyRates(_Symbol, tf, shift, HistoryBars, r);
   if(got < SmiLength + 3 * SignalLength)            // not enough history loaded yet
      return false;

   double a1 = 2.0 / (Smooth1 + 1), a2 = 2.0 / (Smooth2 + 1), as = 2.0 / (SignalLength + 1);
   double rel1 = 0, rel2 = 0, rng1 = 0, rng2 = 0, sig = 0, kv = 0;

   for(int i = 0; i < got; i++)
   {
      double hh = r[i].high, ll = r[i].low;
      for(int j = MathMax(0, i - SmiLength + 1); j <= i; j++)
      {
         hh = MathMax(hh, r[j].high);
         ll = MathMin(ll, r[j].low);
      }
      double rel = r[i].close - (hh + ll) / 2.0;
      double rng = hh - ll;

      if(i == 0)
      {
         rel1 = rel; rel2 = rel; rng1 = rng; rng2 = rng;
      }
      else
      {
         rel1 = a1 * rel  + (1 - a1) * rel1;
         rel2 = a2 * rel1 + (1 - a2) * rel2;
         rng1 = a1 * rng  + (1 - a1) * rng1;
         rng2 = a2 * rng1 + (1 - a2) * rng2;
      }
      kv  = (rng2 != 0) ? 100.0 * rel2 / (0.5 * rng2) : 0;
      sig = (i == 0) ? kv : as * kv + (1 - as) * sig;
   }
   k = kv;
   d = sig;
   return true;
}

//+------------------------------------------------------------------+
//| Checks one timeframe. Returns false if data isn't ready.         |
//+------------------------------------------------------------------+
bool CheckTF(ENUM_TIMEFRAMES tf, string label, bool &ok, string &text)
{
   double k, d;
   if(!CalcSMI(tf, k, d))
      return false;
   ok = (k > Level && d > Level && k > d);
   text += StringFormat("%s  %%K %7.2f  %%D %7.2f  %s\n", label, k, d, ok ? "OK" : "--");
   return true;
}

//+------------------------------------------------------------------+
bool HasPosition()
{
   for(int i = PositionsTotal() - 1; i >= 0; i--)
   {
      ulong ticket = PositionGetTicket(i);
      if(ticket > 0 && PositionSelectByTicket(ticket)
         && PositionGetString(POSITION_SYMBOL) == _Symbol
         && (ulong)PositionGetInteger(POSITION_MAGIC) == MagicNumber)
         return true;
   }
   return false;
}

void CloseAll()
{
   for(int i = PositionsTotal() - 1; i >= 0; i--)
   {
      ulong ticket = PositionGetTicket(i);
      if(ticket > 0 && PositionSelectByTicket(ticket)
         && PositionGetString(POSITION_SYMBOL) == _Symbol
         && (ulong)PositionGetInteger(POSITION_MAGIC) == MagicNumber)
      {
         if(!trade.PositionClose(ticket))
            Print("Close failed: ", trade.ResultRetcode(), " ", trade.ResultRetcodeDescription());
      }
   }
}

//+------------------------------------------------------------------+
int OnInit()
{
   trade.SetExpertMagicNumber(MagicNumber);
   trade.SetTypeFillingBySymbol(_Symbol);
   trade.SetDeviationInPoints(30);
   return INIT_SUCCEEDED;
}

void OnDeinit(const int reason)
{
   Comment("");
}

void OnTick()
{
   if(TimeCurrent() - lastCheck < CheckEverySeconds)
      return;
   lastCheck = TimeCurrent();

   bool okW1 = false, okH4 = false, okH1 = false;
   string text = "SMI EA  (" + _Symbol + ")\n";
   if(!CheckTF(PERIOD_W1, "W1", okW1, text) || !CheckTF(PERIOD_H4, "H4", okH4, text)
      || !CheckTF(PERIOD_H1, "H1", okH1, text))
   {
      Comment(text + "Waiting for chart history to load...");
      return;   // never trade or close on incomplete data
   }

   bool signal = okW1 && okH4 && okH1;
   bool inTrade = HasPosition();
   Comment(text + (signal ? "SIGNAL: ALL TRUE" : "SIGNAL: not all true")
                + (inTrade ? "   |  IN TRADE" : "   |  no trade"));

   if(signal && !inTrade)
   {
      double ask = SymbolInfoDouble(_Symbol, SYMBOL_ASK);
      double sl  = StopLossPoints > 0 ? NormalizeDouble(ask - StopLossPoints * _Point, _Digits) : 0;
      if(trade.Buy(Lots, _Symbol, ask, sl, 0, "SMI EA"))
         Print("BUY ", Lots, " ", _Symbol, " at ", ask);
      else
         Print("Buy failed: ", trade.ResultRetcode(), " ", trade.ResultRetcodeDescription());
   }
   else if(!signal && inTrade)
   {
      Print("Conditions no longer true: closing ", _Symbol);
      CloseAll();
   }
}
//+------------------------------------------------------------------+
