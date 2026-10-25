# National minimum wages, October 2026. EU/candidate countries and the US: Eurostat earn_mw_cur, July 2026 (monthly, EUR).
# Others: official rates; monthly = hourly x 40 x 52 / 12, weekly x 52 / 12, daily x 26 (Thailand, India) or x 30.4 (Mexico).
# USD uses ECB reference rates of 23 October 2026. None = no ECB rate (local value only).
ECB = {"USD": 1.1193, "JPY": 175.51, "GBP": 0.83728, "AUD": 1.5939, "BRL": 5.6485, "CAD": 1.5665, "CNY": 7.4660,
       "IDR": 20268.06, "INR": 106.2720, "KRW": 1482.46, "MXN": 21.0226, "NZD": 1.9682, "THB": 37.771, "ZAR": 18.5777, "EUR": 1.0}
H = 40 * 52 / 12
def usd(m, cur): return None if cur not in ECB else round(m / ECB[cur] * ECB["USD"])
EURO = {"Luxembourg": 2771, "Ireland": 2391, "Netherlands": 2338, "Germany": 2343, "Belgium": 2234, "France": 1867,
        "Slovenia": 1482, "Spain": 1425, "Lithuania": 1153, "Poland": 1119, "Cyprus": 1088, "Greece": 1073, "Portugal": 1073,
        "Croatia": 1050, "Malta": 994, "Estonia": 946, "Czechia": 923, "Slovakia": 915, "Hungary": 906, "Romania": 825,
        "Latvia": 780, "Bulgaria": 620, "Türkiye": 621, "Serbia": 743, "United States (federal)": 1103}
ROWS = []  # country, region, local display, monthly local, currency, note
for c, v in EURO.items():
    reg = "Americas" if c.startswith("United States") else "Europe"
    note = {"Germany": "€13.90 an hour from January 2026", "United States (federal)": "$7.25 an hour, unchanged since 2009; 30 states and DC set higher rates, up to $17.13 in Washington state",
            "France": "SMIC; raised in 2026", "Türkiye": "Converted at falling exchange rates; set in lira", "Ireland": "€14.15 an hour from January 2026"}.get(c, "")
    disp = "$7.25/hour" if reg == "Americas" else "€%s/month" % format(v, ",")
    ROWS.append((c, reg, disp, v, "EUR", note))
ROWS += [
 ("United Kingdom", "Europe", "£12.71/hour", 12.71 * H, "GBP", "National Living Wage for 21+ from 1 April 2026"),
 ("Australia", "Asia-Pacific", "A$26.44/hour", 1004.90 * 52 / 12, "AUD", "A$1,004.90 a week (38 hours) from 1 July 2026, up about 6%"),
 ("New Zealand", "Asia-Pacific", "NZ$23.95/hour", 23.95 * H, "NZD", "Adult rate from 1 April 2026"),
 ("Canada (federal)", "Americas", "C$18.15/hour", 18.15 * H, "CAD", "Federally regulated sectors from 1 April 2026; provinces set their own rates"),
 ("Japan", "Asia-Pacific", "¥1,177/hour (average)", 1177 * H, "JPY", "National weighted average for fiscal 2026; set by prefecture, from ¥1,280 in Tokyo"),
 ("South Korea", "Asia-Pacific", "₩10,320/hour", 10320 * H, "KRW", "2026 rate"),
 ("Brazil", "Americas", "R$1,621/month", 1621, "BRL", "From 1 January 2026, up 6.8%"),
 ("Mexico", "Americas", "MX$315.04/day", 315.04 * 30.4, "MXN", "General rate from 1 January 2026, up 13%; higher in the northern border zone"),
 ("South Africa", "Africa", "R30.23/hour", 30.23 * H, "ZAR", "From 1 March 2026"),
 ("China (Shanghai)", "Asia-Pacific", "¥2,740/month", 2740, "CNY", "Highest provincial rate; set by province"),
 ("Indonesia (Jakarta)", "Asia-Pacific", "Rp5,729,876/month", 5729876, "IDR", "Highest provincial rate, 2026"),
 ("Thailand (Bangkok)", "Asia-Pacific", "฿400/day", 400 * 26, "THB", "Highest rate; THB 337–400 by province"),
 ("India", "Asia-Pacific", "₹178/day (national floor)", 178 * 26, "INR", "Non-binding national floor; states set their own, often higher, rates"),
 ("Colombia", "Americas", "COP 1,750,905/month", 1750905, "COP", "Up 23% from January 2026, plus a COP 249,095 transport allowance"),
 ("Chile", "Americas", "CLP 539,000/month", 539000, "CLP", "From January 2026"),
 ("Vietnam (Region I)", "Asia-Pacific", "₫5,310,000/month", 5310000, "VND", "Highest of four regions, from 1 January 2026"),
]
if __name__ == "__main__":
    for r in sorted(ROWS, key=lambda r: -(usd(r[3], r[4]) or 0)): print(f"{r[0]:26} {r[2]:28} {usd(r[3], r[4])}")
