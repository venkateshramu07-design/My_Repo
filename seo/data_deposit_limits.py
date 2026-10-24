# Deposit insurance limits per depositor per bank, verified October 2026.
# USD values use ECB euro reference rates of 23 October 2026 (Oman: fixed peg). None = no ECB reference rate.
ECB = {"USD": 1.1193, "JPY": 175.51, "GBP": 0.83728, "CHF": 0.9227, "NOK": 10.4665, "TRY": 55.6715, "AUD": 1.5939,
       "BRL": 5.6485, "CAD": 1.5665, "CNY": 7.4660, "HKD": 8.7793, "IDR": 20268.06, "INR": 106.2720, "KRW": 1482.46,
       "MXN": 21.0226, "MYR": 4.5729, "NZD": 1.9682, "PHP": 71.235, "SGD": 1.4310, "THB": 37.771, "ZAR": 18.5777, "EUR": 1.0}
def usd(amount, cur):
    if cur == "OMR": return amount * 2.6008
    if cur not in ECB: return None
    return amount / ECB[cur] * ECB["USD"]
ROWS = [  # country, region, scheme, amount, currency, display, note
 ("United States", "Americas", "FDIC", 250000, "USD", "$250,000", "Per ownership category; joint and retirement accounts insured separately"),
 ("Canada", "Americas", "CDIC", 100000, "CAD", "C$100,000", "Per eligible category"),
 ("Mexico", "Americas", "IPAB", 3500000, "MXN", "400,000 UDIs (≈ MX$3.5m)", "Set in inflation-linked UDIs; peso value changes daily"),
 ("Brazil", "Americas", "FGC", 250000, "BRL", "R$250,000", ""),
 ("Argentina", "Americas", "SEDESA", 50000000, "ARS", "ARS 50 million", "From 1 April 2026"),
 ("United Kingdom", "Europe", "FSCS", 120000, "GBP", "£120,000", "From 1 December 2025; temporary high balances up to £1.4m"),
 ("European Union (all 27)", "Europe", "National schemes", 100000, "EUR", "€100,000", "Harmonised by EU law; local-currency equivalent outside the euro"),
 ("Switzerland", "Europe", "esisuisse", 100000, "CHF", "CHF 100,000", ""),
 ("Norway", "Europe", "Bankenes sikringsfond", 2000000, "NOK", "NOK 2 million", "For banks in the Norwegian scheme"),
 ("Turkey", "Europe", "TMSF", 1200000, "TRY", "TRY 1.2 million", "Limit for 2026, raised from TRY 950,000"),
 ("Russia", "Europe", "DIA", 1400000, "RUB", "RUB 1.4 million", "Proposals to raise it are under discussion"),
 ("South Africa", "Africa", "CODI", 100000, "ZAR", "R100,000", "Scheme launched April 2024"),
 ("Nigeria", "Africa", "NDIC", 5000000, "NGN", "₦5 million", "Commercial banks; ₦2m for microfinance and mortgage banks"),
 ("Kenya", "Africa", "KDIC", 500000, "KES", "KES 500,000", ""),
 ("Oman", "Middle East", "Bank Deposits Protection", 20000, "OMR", "OMR 20,000", "Law in force from 2025"),
 ("Japan", "Asia-Pacific", "DICJ", 10000000, "JPY", "¥10 million plus interest", "Non-interest settlement accounts fully protected"),
 ("China", "Asia-Pacific", "Deposit insurance fund", 500000, "CNY", "¥500,000 (RMB)", "Principal and interest"),
 ("Hong Kong", "Asia-Pacific", "DPS", 800000, "HKD", "HK$800,000", ""),
 ("Taiwan", "Asia-Pacific", "CDIC", 3000000, "TWD", "NT$3 million", "Principal and interest"),
 ("South Korea", "Asia-Pacific", "KDIC", 100000000, "KRW", "₩100 million", "Doubled from ₩50m on 1 September 2025"),
 ("Singapore", "Asia-Pacific", "SDIC", 100000, "SGD", "S$100,000", ""),
 ("Malaysia", "Asia-Pacific", "PIDM", 250000, "MYR", "RM250,000", ""),
 ("Thailand", "Asia-Pacific", "DPA", 1000000, "THB", "฿1 million", ""),
 ("Indonesia", "Asia-Pacific", "LPS", 2000000000, "IDR", "Rp2 billion", ""),
 ("Philippines", "Asia-Pacific", "PDIC", 1000000, "PHP", "₱1 million", "Doubled from ₱500,000 on 15 March 2025"),
 ("Vietnam", "Asia-Pacific", "DIV", 125000000, "VND", "₫125 million", ""),
 ("India", "Asia-Pacific", "DICGC", 500000, "INR", "₹5 lakh (₹500,000)", ""),
 ("Pakistan", "Asia-Pacific", "DPC", 1000000, "PKR", "PKR 1 million", "Doubled from PKR 500,000 in October 2024"),
 ("Australia", "Asia-Pacific", "Financial Claims Scheme", 250000, "AUD", "A$250,000", ""),
 ("New Zealand", "Asia-Pacific", "Depositor Compensation Scheme", 100000, "NZD", "NZ$100,000", "Scheme started 1 July 2025"),
]
if __name__ == "__main__":
    for r in sorted(ROWS, key=lambda r: -(usd(r[3], r[4]) or 0)):
        u = usd(r[3], r[4]); print(f"{r[0]:26} {r[5]:30} {('$%s' % format(round(u, -2), ',.0f')) if u else '—'}")
