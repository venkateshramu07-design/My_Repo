# Builds the four illustrations for each batch-3 article. Run: python3 make_images.py  (writes svg/*.svg and specs.json)
import json, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from lib import *

S = []  # (post, n, after-heading, alt, caption, svg)
def add(post, n, after, alt, cap, svg): S.append(dict(post=post, n=n, after=after, alt=alt, cap=cap, svg=svg))
usd = lambda v: "$" + format(int(round(v)), ",")
pct = lambda v: ("%.1f%%" % v).replace(".0%", "%")
BLUE, ORANGE, GREEN, PURPLE, AMBER, RED, TEAL, BROWN = "#2f7de1", "#e0662f", "#0f9d76", "#7a5cd6", "#e0a52f", "#c2410c", "#0e8fa3", "#b45309"
GOLD = "#f2b33d"

# ---------------- 1. Minimum wage ----------------
P = "minimum-wage-by-country-2026"
b = card(56, 190, 1088, 420)
for i, (ic, top, mid, bot) in enumerate([("clock", "Hourly", "£12.71 × 40 h", "× 52 ÷ 12"), ("cal", "Daily", "฿400 × 26", "working days"), ("doc", "Monthly", "€2,771", "as published")]):
    x = 56 + 40 + i * 350
    b += card(x, 220, 310, 360, tint(BLUE, .08))
    b += (clock(x + 155, 300, 50, BLUE) if ic == "clock" else calendar(x + 155, 300, .8, BLUE, "26", "DAYS") if ic == "cal" else coins(x + 155, 330, 5, GOLD, 52))
    b += t(x + 155, 405, top, 34, 800, INK, "middle", "serif") + t(x + 155, 460, mid, 32, 700, BLUE, "middle") + t(x + 155, 505, bot, 26, 500, MUTED, "middle")
    b += t(x + 155, 555, "= one month of full-time pay", 21, 600, MUTED, "middle")
add(P, 1, "How Minimum Wages Are Compared", "Hourly, daily and monthly minimum wages converted to a month of full-time pay",
    "Every rate is turned into a month of full-time work so countries can be compared.",
    frame(BLUE, "Minimum wage 2026", "Three ways countries set the minimum", b, "Method used in our table"))

b = card(56, 190, 1088, 420) + hbars(96, 222, 820, [("Luxembourg", 3102), ("Australia", 3058), ("United Kingdom", 2945), ("Germany", 2623), ("US (federal)", 1257, ORANGE)], 3300, usd, BLUE, rowh=74, labw=250)
b += coins(1010, 560, 7, GOLD, 64) + coins(1090, 575, 4, GOLD, 44)
add(P, 2, "The Highest Minimum Wages", "Bar chart of the highest monthly minimum wages in US dollars: Luxembourg, Australia, the UK and Germany, with the US federal rate",
    "A month of full-time work at the minimum wage, in US dollars.",
    frame(BLUE, "Minimum wage 2026", "The highest minimum wages, per month", b, "Eurostat and governments; ECB rates"))

b = card(56, 190, 1088, 420) + vbars(140, 250, 600, 270, [("Federal\nsince 2009", 7.25, ORANGE), ("Washington\nstate", 17.13, BLUE), ("Seattle", 21.30, shade(BLUE, .2))], 22, lambda v: "$%.2f" % v, BLUE, valsize=34)
b += calendar(920, 330, 1.1, ORANGE, "2009", "LAST FEDERAL RISE") + t(920, 470, "30 states and DC", 30, 800, INK, "middle") + t(920, 510, "pay more than $7.25", 26, 500, MUTED, "middle")
add(P, 3, "The United States: Low Federal Floor, High State Rates", "Chart comparing the US federal minimum wage of $7.25 with Washington state's $17.13 and Seattle's $21.30",
    "The federal floor has not changed since 2009, but most states and many cities set much higher rates.",
    frame(BLUE, "United States", "Two minimum wages in one country", b, "US Department of Labor; 2026 rates"))

b = card(56, 190, 1088, 420)
for i, (title, sub, col) in enumerate([("Minimum wage", "The legal floor", BLUE), ("Living wage", "What basic costs need", GREEN), ("Real wage", "After inflation", PURPLE)]):
    x = 56 + 40 + i * 350
    b += card(x, 220, 310, 360, tint(col, .08))
    b += (shield(x + 155, 320, 1.0, col) if i == 0 else house(x + 155, 330, 1.05, col) if i == 1 else thermo(x + 155, 320, 1.1, col))
    b += t(x + 155, 455, title, 34, 800, INK, "middle", "serif") + t(x + 155, 500, sub, 26, 500, MUTED, "middle")
add(P, 4, "Minimum Wage, Living Wage and Real Wages", "Three cards explaining the difference between the minimum wage, a living wage and the real wage",
    "The minimum wage is a legal floor; a living wage reflects local costs; the real wage adjusts for inflation.",
    frame(BLUE, "Minimum wage 2026", "Minimum, living and real wages", b))

# ---------------- 2. Electricity ----------------
P = "electricity-prices-by-country-2026"
b = card(56, 190, 1088, 420)
segs = [("Energy and supply", 45, AMBER), ("Network charges", 25, BLUE), ("Taxes and levies", 30, ORANGE)]
x0 = 110; tot = 980
for lab, v, col in segs:
    w = tot * v / 100
    b += f'<rect x="{x0}" y="250" width="{w-6}" height="90" rx="16" fill="{col}"/>' + t(x0 + w / 2, 395, lab, 28, 800, INK, "middle") + t(x0 + w / 2, 435, ["follows wholesale prices", "regulated grids", "VAT, energy taxes"][segs.index((lab, v, col))], 22, 500, MUTED, "middle")
    x0 += w
b += bolt(150, 520, .7, AMBER) + t(210, 530, "In most of Europe, networks and taxes are more than half the bill", 26, 600, INK)
add(P, 1, "What These Prices Include", "Diagram of the three parts of an electricity bill: energy and supply, network charges, and taxes and levies",
    "What you pay per kWh is made up of energy, networks and taxes. The split shown is illustrative.",
    frame(AMBER, "Electricity 2026", "What is inside a kilowatt-hour", b, "Illustrative split"))

b = card(56, 190, 1088, 420) + vbars(110, 250, 980, 270, [("Ireland", .433), ("Germany", .405), ("Belgium", .40), ("UK", .352), ("EU avg", .317, shade(AMBER, .25)), ("US", .183, BLUE), ("Türkiye", .074, GREEN)], .48, lambda v: "%d¢" % round(v * 100), AMBER, labsize=24)
add(P, 2, "The Most Expensive Countries", "Bar chart of household electricity prices in US cents per kWh, from Ireland and Germany to the US and Türkiye",
    "Household electricity, US cents per kWh including taxes (UK unit rate excludes the standing charge).",
    frame(AMBER, "Electricity 2026", "Price per kWh, in US cents", b, "Eurostat H1 2026, Ofgem, EIA"))

b = card(56, 190, 1088, 420)
b += t(100, 240, "European gas (TTF), € per MWh", 26, 800, ORANGE) + line(100, 320, 460, 200, [32, 62, 45, 42, 61, 82, 90], 0, 100, ORANGE, labels=(0, 1, 6), fmt="€%d", xlabels=((0, "Feb"), (3, "Jun"), (6, "Oct")))
b += t(650, 240, "EU household electricity, € per kWh", 26, 800, AMBER) + line(650, 320, 440, 200, [.290, .286, .283], 0, .4, AMBER, labels=(0, 2), fmt="€%.3f", xlabels=((0, "H2 2025"), (2, "H1 2026")))
add(P, 3, "Why Bills Have Not Followed the War, Yet", "Two charts: European wholesale gas prices nearly tripled in 2026 while household electricity prices were flat",
    "Wholesale gas jumped after February, but household prices lag because suppliers buy energy months ahead.",
    frame(AMBER, "Electricity 2026", "Wholesale gas soared; bills lagged", b, "Market reports; Eurostat"))

b = card(56, 190, 1088, 420)
for i, (who, kwh, cost, col, s) in enumerate([("EU household", "3,500 kWh", "≈ €990 a year", AMBER, .9), ("UK household", "2,500 kWh", "≈ £860 a year", PURPLE, .8), ("US household", "10,500 kWh", "≈ $1,920 a year", BLUE, 1.2)]):
    x = 56 + 40 + i * 350
    b += card(x, 220, 310, 360, tint(col, .08)) + house(x + 155, 330, s, col)
    b += t(x + 155, 450, who, 30, 800, INK, "middle", "serif") + t(x + 155, 495, kwh, 28, 700, col, "middle") + t(x + 155, 540, cost, 28, 800, INK, "middle")
add(P, 4, "What a Kilowatt-Hour Means for Your Bill", "Typical yearly electricity use and cost for households in the EU, UK and US",
    "Americans pay less per unit but use about three times as much electricity.",
    frame(AMBER, "Electricity 2026", "Typical yearly use and cost", b, "Our calculation at October 2026 prices"))

# ---------------- 3. Income tax ----------------
P = "income-tax-by-country-take-home-pay"
b = card(56, 190, 1088, 420) + doc(110, 215, 520, 370, RED, [("Gross pay", "100"), ("Income tax", "− 15"), ("Social security", "− 10"), ("Take-home pay", "75", 1)], "PAYSLIP (OECD AVERAGE)")
b += donut(880, 400, 160, [("tax", 25.1, RED), ("kept", 74.9, tint(RED, .25))], 64) + t(880, 395, "75%", 64, 800, INK, "middle", "serif") + t(880, 440, "kept", 28, 600, MUTED, "middle")
add(P, 1, "What the Numbers Measure", "Payslip showing an average worker in the OECD keeps about 75% of gross pay after income tax and social security",
    "On average across the OECD, a single worker keeps about three-quarters of gross pay. The split between tax and contributions varies by country.",
    frame(RED, "Taxes on wages", "How a typical payslip shrinks", b, "OECD Taxing Wages 2026; split illustrative"))

b = card(56, 190, 1088, 420) + hbars(96, 222, 1000, [("Belgium", 60.5), ("Germany", 61.3), ("Lithuania", 61.3), ("Slovenia", 63.8), ("Denmark", 64.7)], 100, lambda v: "%.1f%% kept" % v, RED, rowh=74, labw=230, valw=210)
add(P, 2, "Where Workers Keep the Least", "Bar chart: average workers keep about 60% to 65% of pay in Belgium, Germany, Lithuania, Slovenia and Denmark",
    "Share of gross pay kept by a single worker on the average wage, 2025.",
    frame(RED, "Taxes on wages", "Where workers keep the least", b, "OECD Taxing Wages 2026"))

b = card(56, 190, 1088, 420) + hbars(96, 222, 1000, [("Colombia", 100), ("Chile", 92.9), ("Costa Rica", 90.2), ("Mexico", 86.8), ("South Korea", 83.5)], 100, lambda v: "%.1f%% kept" % v if v < 100 else "100% kept", GREEN, rowh=74, labw=230, valw=210)
add(P, 3, "Where Workers Keep the Most", "Bar chart: average workers keep the most pay in Colombia, Chile, Costa Rica, Mexico and South Korea",
    "Share of gross pay kept by a single worker on the average wage, 2025.",
    frame(GREEN, "Taxes on wages", "Where workers keep the most", b, "OECD Taxing Wages 2026"))

b = card(56, 190, 1088, 420)
b += card(96, 220, 480, 360, tint(RED, .08)) + person(336, 360, 1.1, RED) + t(336, 470, "Single, no children", 28, 700, INK, "middle") + t(336, 535, "25.1% in tax", 44, 800, RED, "middle", "serif")
b += card(624, 220, 480, 360, tint(GREEN, .08)) + person(780, 360, 1.0, GREEN) + person(880, 360, 1.0, GREEN) + person(830, 395, .6, shade(GREEN, .2)) + person(930, 395, .6, shade(GREEN, .2))
b += t(864, 470, "Couple, two children", 28, 700, INK, "middle") + t(864, 535, "14.7% in tax", 44, 800, GREEN, "middle", "serif")
add(P, 4, "Families With Children Pay Much Less", "A single worker pays 25.1% of pay in tax on average in the OECD, a one-earner family with two children 14.7%",
    "OECD averages at the average wage, after child benefits and tax credits.",
    frame(RED, "Taxes on wages", "Families pay far less", b, "OECD Taxing Wages 2026"))

# ---------------- 4. Pensions ----------------
P = "state-pension-amounts-by-country-2026"
b = card(56, 190, 1088, 420) + vbars(110, 260, 980, 260, [("Germany", 2140), ("US avg", 2071), ("Australia", 1880), ("UK", 1400), ("Canada", 1170), ("Japan*", 450)], 2400, usd, PURPLE, labsize=25)
add(P, 1, "Headline State Pension Amounts in 2026", "Bar chart of 2026 state pension amounts per month in US dollars for Germany, the US, Australia, the UK, Canada and Japan",
    "Main state pension per month in US dollars, 2026. *Japan's basic pension only; see the article for each definition.",
    frame(PURPLE, "State pensions 2026", "What the main state pension pays a month", b, "National pension agencies; ECB rates"))

b = card(56, 190, 1088, 420) + doc(110, 225, 380, 300, PURPLE, [("Take-home pay", "100"), ("Working years", "22 to 67"), ("Pension", "63", 1)], "BEFORE RETIREMENT")
b += arrow(530, 380, 690, 380, PURPLE, 10) + t(610, 350, "full career", 22, 600, MUTED, "middle")
b += donut(880, 390, 160, [("p", 63, PURPLE), ("gap", 37, tint(PURPLE, .2))], 64) + t(880, 395, "63%", 64, 800, INK, "middle", "serif") + t(880, 438, "of pay", 24, 600, MUTED, "middle")
add(P, 2, "What the Replacement Rate Means", "Diagram: across the OECD, mandatory pensions replace about 63% of an average earner's take-home pay",
    "The OECD's net replacement rate for an average earner after a full career, mandatory schemes.",
    frame(PURPLE, "State pensions 2026", "How much of your pay a pension replaces", b, "OECD Pensions at a Glance 2025"))

b = card(56, 190, 1088, 420) + vbars(200, 250, 800, 260, [("Half the\naverage wage", 75, PURPLE), ("Average\nwage", 63, shade(PURPLE, .15)), ("Twice the\naverage", 53, shade(PURPLE, .3))], 90, lambda v: "%d%%" % v, PURPLE, labsize=25)
add(P, 3, "Low and High Earners Are Treated Very Differently", "Chart: pensions replace about 75% of pay for low earners, 63% for average earners and 53% for high earners across the OECD",
    "OECD averages for mandatory pensions, by earnings level.",
    frame(PURPLE, "State pensions 2026", "Pensions favour lower earners", b, "OECD Pensions at a Glance 2025"))

b = card(56, 190, 1088, 420)
b += f'<rect x="140" y="260" width="900" height="90" rx="20" fill="{tint(PURPLE,.15)}"/><rect x="140" y="260" width="{900*.63:.0f}" height="90" rx="20" fill="{PURPLE}"/>'
b += f'<rect x="{140+900*.63:.0f}" y="260" width="{900*.14:.0f}" height="90" fill="{AMBER}"/>' + f'<line x1="{140+900*.75:.0f}" y1="235" x2="{140+900*.75:.0f}" y2="375" stroke="{INK}" stroke-width="4" stroke-dasharray="8 6"/>'
b += t(140 + 900 * .31, 318, "State and mandatory pension 63%", 26, 800, "#fff", "middle") + t(140 + 900 * .75, 225, "Common target 70–80%", 24, 800, INK, "middle")
b += t(140 + 900 * .70, 410, "the gap: workplace pension,", 26, 700, shade(AMBER, .35), "middle") + t(140 + 900 * .70, 445, "savings or working longer", 26, 700, shade(AMBER, .35), "middle")
b += coins(250, 560, 4, GOLD, 50) + house(400, 560, .55, PURPLE)
add(P, 4, "Will My State Pension Be Enough?", "Bar showing state pensions replace about 63% of pay, short of the 70% to 80% many planners suggest, with the gap filled by savings",
    "For most average and higher earners, savings or a workplace pension are needed on top of the state pension.",
    frame(PURPLE, "State pensions 2026", "Mind the retirement gap", b, "OECD; planner rule of thumb"))

# ---------------- 5. Mortgages ----------------
P = "mortgage-rates-by-country-2026"
b = card(56, 190, 1088, 420)
for i, (lab, sub) in enumerate([("Central bank", "policy rate"), ("Inflation", "lenders' risk"), ("Fixed period", "30 years costs more"), ("Competition", "and regulation")]):
    x = 96 + i * 262
    b += card(x, 225, 236, 350, tint(BLUE, .08))
    b += [bank(x + 118, 340, .95, BLUE), thermo(x + 118, 335, 1.0, ORANGE), calendar(x + 118, 335, .9, BLUE, "30", "YEARS"), house(x + 118, 345, .8, GREEN)][i]
    b += t(x + 118, 470, lab, 30, 800, INK, "middle", "serif") + t(x + 118, 512, sub, 23, 500, MUTED, "middle")
add(P, 1, "Why Mortgage Rates Differ So Much", "Four factors behind mortgage rates: central bank rates, inflation, the fixed period and competition",
    "The four main reasons the same loan costs more in one country than another.",
    frame(BLUE, "Mortgages 2026", "Why rates differ between countries", b))

pm = [5.98, 6.38, 6.23, 6.51, 6.49, 6.58, 6.65, 6.95, 7.32]  # Freddie Mac, roughly monthly: 26 Feb ... 22 Oct
b = card(56, 190, 1088, 420) + line(120, 240, 960, 280, pm, 5.0, 8.0, BLUE, labels=(0, 3, 7, 8), xlabels=((0, "Feb"), (2, "Apr"), (4, "Jun"), (6, "Aug"), (8, "Oct")))
add(P, 2, "The United States: Above 7% Again", "Line chart of the US 30-year fixed mortgage rate rising from 5.98% in February 2026 to 7.32% in October",
    "Freddie Mac's average 30-year fixed rate, roughly monthly readings; it peaked at 7.40% in mid-October.",
    frame(BLUE, "Mortgages 2026", "US 30-year fixed rate since the war began", b, "Freddie Mac PMMS"))

b = card(56, 190, 1088, 420) + vbars(100, 250, 1000, 260, [("Malta", 1.94, GREEN), ("Bulgaria", 2.42, GREEN), ("Spain", 3.04), ("France", 3.20), ("Euro avg", 3.60, shade(BLUE, .25)), ("Germany", 4.00, ORANGE), ("Latvia", 4.35, ORANGE)], 4.8, lambda v: "%.2f%%" % v, BLUE, labsize=24, valsize=26)
add(P, 3, "The Euro Area: Lower, but Rising", "Bar chart of August 2026 mortgage rates across the euro area, from 1.94% in Malta to 4.35% in Latvia",
    "Cost of borrowing for house purchase, new loans, August 2026.",
    frame(BLUE, "Mortgages 2026", "One currency, very different rates", b, "European Central Bank"))

b = card(56, 190, 1088, 420) + vbars(110, 250, 760, 260, [("1.5%", 400, GREEN), ("3.6%", 506), ("6.0%", 644), ("7.3%*", 687), ("11.5%", 1016, ORANGE)], 1150, lambda v: format(v, ","), BLUE, labsize=26)
b += house(990, 380, 1.0, BLUE) + tag(990, 500, .9, ORANGE, "100,000")
add(P, 4, "What a Rate Means for Your Monthly Payment", "Monthly payment on 100,000 borrowed at rates from 1.5% to 11.5%",
    "Monthly payment per 100,000 borrowed over 25 years (*30 years, as in the US).",
    frame(BLUE, "Mortgages 2026", "Monthly payment per 100,000 borrowed", b, "Our calculation"))

# ---------------- 6. Inheritance ----------------
P = "inheritance-tax-by-country-2026"
b = card(56, 190, 1088, 420)
for i, (ttl, sub, col) in enumerate([("Estate tax", "Whole estate taxed, then shared", BROWN), ("Inheritance tax", "Each heir taxed on their share", GREEN)]):
    x = 96 + i * 524
    b += card(x, 220, 488, 360, tint(col, .07)) + t(x + 244, 272, ttl, 36, 800, INK, "middle", "serif") + t(x + 244, 314, sub, 23, 600, MUTED, "middle")
    b += house(x + 244, 400, .7, col)
    if i == 0:
        b += tag(x + 244, 470, .8, RED, "TAX") + "".join(person(x + 140 + k * 104, 560, .5, col) for k in range(3))
    else:
        b += "".join(person(x + 140 + k * 104, 520, .5, col) + tag(x + 150 + k * 104, 560, .42, GREEN, "") for k in range(3)) + t(x + 244, 470, "each heir has an allowance", 22, 700, shade(col, .3), "middle")
add(P, 1, "Inheritance Tax and Estate Tax: What Is the Difference?", "Diagram comparing an estate tax on the whole estate with an inheritance tax charged on each heir",
    "The UK and US tax the estate; France, Germany and Japan tax each heir's share, with separate allowances.",
    frame(BROWN, "Inheritance tax 2026", "Estate tax or inheritance tax?", b))

b = card(56, 190, 1088, 420) + vbars(100, 250, 1000, 260, [("Japan", 55), ("S. Korea", 50), ("France", 45), ("UK", 40), ("US", 40), ("Spain", 34), ("Ireland", 33), ("Germany", 30)], 62, lambda v: "%d%%" % v, BROWN, labsize=23, valsize=28)
add(P, 2, "The Highest Inheritance Taxes", "Bar chart of top inheritance tax rates for children: Japan 55%, South Korea 50%, France 45%, UK and US 40%",
    "Top rate on assets left to children. Thresholds differ hugely: the US taxes only estates above $15 million.",
    frame(BROWN, "Inheritance tax 2026", "Top inheritance tax rates for children", b, "National tax authorities"))

b = card(56, 190, 1088, 420) + globe(220, 400, 140, GREEN)
b += chips(420, 230, 700, ["Canada*", "Australia", "New Zealand", "Sweden", "Norway", "Austria", "India", "China", "Mexico", "Singapore", "Hong Kong", "UAE"], GREEN, 27)
b += t(420, 560, "*Capital gains tax is due at death instead", 23, 600, MUTED)
add(P, 3, "Countries With No Inheritance Tax", "Globe with a list of countries that have no inheritance tax, including Canada, Australia, Sweden, India and Singapore",
    "Countries with no inheritance or estate tax. Some, such as Canada, tax capital gains at death.",
    frame(GREEN, "Inheritance tax 2026", "No inheritance tax here", b))

b = card(56, 190, 1088, 420) + hbars(96, 222, 1000, [("US, Italy", 0, GREEN), ("Germany", 22000), ("Ireland", 52800), ("France", 156000), ("UK (with home)", 200000, RED)], 220000, lambda v: "0" if v == 0 else format(int(v), ","), BROWN, rowh=74, labw=270, valw=170)
add(P, 4, "Worked Example: A 1 Million Estate Left to Two Children", "Approximate inheritance tax on a 1 million estate left to two children in the US, Italy, Germany, Ireland, France and the UK",
    "Approximate tax on 1 million in local currency left to two adult children, no spouse or special reliefs.",
    frame(BROWN, "Inheritance tax 2026", "Tax on a 1 million estate, two children", b, "Our calculation under 2026 rules"))

# ---------------- 7. EV ----------------
P = "ev-vs-petrol-running-costs-2026"
b = card(56, 190, 1088, 420) + pump(200, 360, 1.4, ORANGE) + charger(1000, 360, 1.4, GREEN)
rows = [("United States", "$4.28 / gal", "18.3¢ / kWh"), ("United Kingdom", "174.6p / litre", "26.3p / kWh"), ("EU average", "€1.99 / litre", "28¢ / kWh")]
for i, (c_, a, e) in enumerate(rows):
    y = 270 + i * 100
    b += t(600, y, c_, 28, 800, INK, "middle", "serif") + t(470, y + 40, a, 27, 800, ORANGE, "middle") + t(730, y + 40, e, 27, 800, GREEN, "middle")
add(P, 1, "Fuel and Electricity Prices in October 2026", "Petrol pump and EV charger with October 2026 prices of petrol and household electricity in the US, UK and EU",
    "Petrol per gallon or litre against household electricity per kWh, October 2026.",
    frame(GREEN, "EV vs petrol", "The four prices that decide it", b, "EIA, DESNZ, European Commission, Ofgem"))

b = card(56, 190, 1088, 420)
for i, (c_, pv, ev, sym) in enumerate([("United States", 1830, 630, "$"), ("United Kingdom", 1260, 570, "£"), ("Germany", 2130, 980, "€")]):
    x = 96 + i * 350
    b += vbars(x, 250, 300, 240, [("Petrol", pv, ORANGE), ("EV", ev, GREEN)], 2300, lambda v, s=sym: s + format(v, ","), GREEN, valsize=27)
    b += t(x + 150, 575, c_, 27, 800, INK, "middle", "serif")
add(P, 2, "How Much an EV Saves: Three Examples", "Yearly fuel cost of a petrol car against home charging an EV in the US, UK and Germany",
    "Yearly running cost: typical mileage, home charging on a standard tariff.",
    frame(GREEN, "EV vs petrol", "Yearly fuel cost: petrol vs EV at home", b, "Our calculation at October 2026 prices"))

b = card(56, 190, 1088, 420) + hbars(96, 222, 1000, [("Off-peak EV tariff", 160, GREEN), ("Home, standard tariff", 570, GREEN), ("20% rapid charging", 800, AMBER), ("Petrol car", 1260, ORANGE), ("All rapid charging", 1710, RED)], 1800, lambda v: "£" + format(v, ","), GREEN, rowh=74, labw=330, valw=130)
add(P, 3, "When an EV Does Not Save Money", "UK yearly charging costs from £160 off-peak to £1,710 using only rapid chargers, against £1,260 for petrol",
    "UK, 12,000 km a year. Relying on rapid chargers can cost more than petrol.",
    frame(RED, "EV vs petrol", "UK: how you charge changes everything", b, "Zapmap Sept 2026, Ofgem, DESNZ"))

b = card(56, 190, 1088, 420) + vbars(150, 250, 560, 270, [("Before the war\npetrol $2.94", 630, tint(GREEN, .55)), ("October 2026\npetrol $4.28", 1200, GREEN)], 1400, lambda v: "$" + format(v, ",") + "/yr", GREEN, labsize=25)
b += car(930, 380, 1.2, GREEN) + bolt(930, 250, .6, AMBER) + t(930, 520, "EV saving roughly doubled", 27, 800, INK, "middle")
add(P, 4, "How the War Changed the Sums", "The yearly saving from charging an EV at home in the US roughly doubled from about $630 to $1,200 as petrol prices rose",
    "US driver, 12,000 miles a year, 28 mpg petrol car vs an EV charged at home.",
    frame(GREEN, "EV vs petrol", "The war doubled the EV saving", b, "Our calculation; EIA prices"))

# ---------------- 8. Debt consolidation ----------------
P = "debt-consolidation-loans-when-they-save"
b = card(56, 190, 1088, 420)
for i, (lab, col) in enumerate([("24%", RED), ("22%", ORANGE), ("29%", shade(RED, .2))]):
    y = 225 + i * 120
    b += card_icon(110, y, 170, col, lab) + arrow(310, y + 52, 620, 395, tint(INK, .35), 6)
b += card(640, 300, 440, 190, tint(GREEN, .1), stroke=GREEN) + t(860, 370, "One loan", 38, 800, INK, "middle", "serif") + t(860, 425, "12% fixed, 3 years", 30, 800, GREEN, "middle")
add(P, 1, "What Debt Consolidation Is", "Three credit cards at 22% to 29% combined into a single loan at 12%",
    "Consolidation replaces several expensive debts with one loan and one payment.",
    frame(GREEN, "Debt consolidation", "Several debts, one payment", b))

b = card(56, 190, 1088, 420) + hbars(96, 222, 1000, [("Cards, 3 years", 4550, RED), ("Loan 12%, 3 years", 2150, GREEN), ("Loan + 5% fee", 2810, AMBER), ("Loan 12%, 5 years", 3680, ORANGE)], 5000, lambda v: "$" + format(v, ","), GREEN, rowh=74, labw=320, valw=140)
b += t(96, 565, "Minimum payments only: about 26 years and over $21,000 in interest", 25, 700, RED)
add(P, 2, "A Worked Example", "Total interest on $11,000 of card debt: $4,550 over three years on cards against $2,150 with a 12% loan",
    "Interest and fees on $11,000 of credit card debt under different repayment plans.",
    frame(GREEN, "Debt consolidation", "Interest paid on $11,000 of card debt", b, "Our calculation"))

b = card(56, 190, 1088, 420)
for i, (c_, cr, lr) in enumerate([("United States", 22.15, 11.86), ("United Kingdom", 24.7, 6.5)]):
    x = 96 + i * 524
    b += vbars(x + 40, 250, 400, 250, [("Credit card", cr, RED), ("Personal loan", lr, GREEN)], 28, lambda v: "%.1f%%" % v, GREEN, labsize=25)
    b += t(x + 240, 580, c_, 28, 800, INK, "middle", "serif")
add(P, 3, "Current Rates in 2026", "Average credit card rates of about 22% to 25% against personal loan rates of about 6.5% to 12% in the US and UK",
    "US: Federal Reserve, Q2 2026. UK: Bank of England card rate; loan rate for £7,500 to £10,000 with good credit.",
    frame(GREEN, "Debt consolidation", "Cards cost two to four times more", b, "Federal Reserve G.19; Bank of England"))

b = card(56, 190, 1088, 420)
b += f'<rect x="110" y="300" width="700" height="80" rx="18" fill="{GREEN}"/><rect x="810" y="300" width="280" height="80" rx="18" fill="{RED}"/>'
b += t(460, 352, "0% for 18 months: $286 a month clears $5,000", 25, 800, "#fff", "middle") + t(950, 352, "then 20%+", 26, 800, "#fff", "middle")
b += hourglass(150, 480, .8, AMBER) + t(220, 470, "Transfer fee 3% = $150", 28, 800, INK) + t(220, 512, "Clear it before the 0% period ends", 25, 500, MUTED)
add(P, 4, "Balance Transfer Cards vs Personal Loans", "Timeline of a 0% balance transfer: 18 interest-free months, then the rate jumps above 20%",
    "A balance transfer is cheapest if you clear the debt before the 0% period ends.",
    frame(GREEN, "Debt consolidation", "How a 0% balance transfer works", b))

# ---------------- 9. Life insurance ----------------
P = "life-insurance-cost-by-age-calculator"
ages = [25, 30, 35, 40, 45, 50, 55, 60]; M = [32, 38, 47, 59, 90, 137, 231, 395]; Fm = [30, 32, 37, 47, 69, 102, 168, 286]
b = card(56, 190, 1088, 420)
slot = 980 / 8
for i, a in enumerate(ages):
    x = 110 + i * slot
    for j, (v, col) in enumerate(((M[i], PURPLE), (Fm[i], tint(PURPLE, .5)))):
        h = 260 * v / 400; bx = x + 16 + j * 44
        b += f'<rect x="{bx}" y="{530-h:.0f}" width="40" height="{h:.0f}" rx="8" fill="{col}"/>'
    b += t(x + 58, 530 - 260 * M[i] / 400 - 12, "$%d" % M[i], 24, 800, INK, "middle") + t(x + 58, 568, str(a), 26, 700, INK, "middle")
b += f'<rect x="110" y="225" width="24" height="24" rx="6" fill="{PURPLE}"/>' + t(144, 245, "Men", 24, 700, INK) + f'<rect x="230" y="225" width="24" height="24" rx="6" fill="{tint(PURPLE,.5)}"/>' + t(264, 245, "Women", 24, 700, INK)
add(P, 1, "Average Term Life Insurance Cost by Age", "Bar chart of average monthly premiums for $500,000 of 20-year term cover by age for men and women",
    "Average monthly premium, $500,000 20-year term, non-smoker in average health. Labels show men's prices.",
    frame(PURPLE, "Life insurance 2026", "Monthly premium by age", b, "MoneyGeek, 21 US insurers, Oct 2026"))

b = card(56, 190, 1088, 420)
items = [(calendar, "Age"), (heart, "Health"), (cig, "Smoking"), (shield, "Cover, term"), (plane, "Job, hobbies"), (person, "Sex")]
for i, (fn, lab) in enumerate(items):
    x = 96 + (i % 3) * 350; y = 220 + (i // 3) * 190
    b += card(x, y, 320, 170, tint(PURPLE, .07))
    if fn is calendar: b += calendar(x + 62, y + 85, .6, PURPLE, "40", "AGE")
    elif fn is person: b += person(x + 62, y + 110, .7, PURPLE)
    else: b += fn(x + 62, y + 85, .55, PURPLE)
    b += t(x + 122, y + 95, lab, 25, 800, INK)
add(P, 2, "What Determines Your Premium", "Six factors that set a life insurance premium: age, health, smoking, cover and term, job and hobbies, and sex",
    "Age matters most; smoking typically doubles or triples the price.",
    frame(PURPLE, "Life insurance 2026", "What sets your premium", b))

b = card(56, 190, 1088, 420) + doc(110, 210, 560, 390, PURPLE, [("Income × 10 years", "$600,000"), ("Mortgage and debts", "$250,000"), ("Future costs", "$50,000"), ("Savings and cover", "− $40,000"), ("Suggested cover", "$850,000", 1)], "HOW MUCH COVER")
b += shield(900, 360, 1.5, PURPLE) + t(900, 520, "≈ $80 a month", 36, 800, INK, "middle", "serif") + t(900, 560, "at 35, non-smoker", 24, 500, MUTED, "middle")
add(P, 3, "How Much Cover Do You Need?", "Worked example adding income, debts and future costs minus savings to reach $850,000 of life cover",
    "The cover calculation from the article's example, with a typical premium for a 35-year-old man.",
    frame(PURPLE, "Life insurance 2026", "Working out how much cover you need", b, "MoneyGeek averages"))

b = card(56, 190, 1088, 420)
b += t(110, 270, "Term", 34, 800, INK, "middle" if False else "start", "serif") + f'<rect x="300" y="240" width="500" height="44" rx="22" fill="{PURPLE}"/><rect x="800" y="240" width="290" height="44" rx="22" fill="{tint(PURPLE,.15)}"/>' + t(550, 271, "20 years of cover", 24, 800, "#fff", "middle") + t(300, 330, "Cheapest; no cash value", 25, 500, MUTED)
b += t(110, 450, "Whole life", 34, 800, INK, "start", "serif") + f'<rect x="300" y="420" width="790" height="44" rx="22" fill="{shade(PURPLE,.25)}"/>' + t(695, 451, "Lifetime cover plus cash value", 24, 800, "#fff", "middle") + t(300, 510, "Often 5 to 15 times the cost", 25, 500, MUTED)
add(P, 4, "Term or Whole Life?", "Comparison of term life insurance for a fixed period with whole life insurance that lasts a lifetime and costs much more",
    "Term cover suits most families; whole life costs far more for the same payout.",
    frame(PURPLE, "Life insurance 2026", "Term or whole life?", b))

# ---------------- 10. Retire abroad ----------------
P = "retire-abroad-on-2000-a-month"
b = card(56, 190, 1088, 420) + hbars(96, 215, 1000, [("Thailand", 6470), ("Colombia", 5280), ("Panama", 3910), ("Portugal", 3310), ("United States", 2000, MUTED)], 7000, lambda v: "$" + format(v, ","), TEAL, rowh=74, labw=240, valw=130)
add(P, 1, "How to Read the Table", "What $2,000 a month buys in Thailand, Colombia, Panama and Portugal measured in US prices",
    "What $2,000 a month buys, expressed in US prices (World Bank price levels, 2024).",
    frame(TEAL, "Retiring abroad", "What $2,000 a month is worth abroad", b, "World Bank, our calculation"))

b = card(56, 190, 1088, 420) + vbars(100, 250, 1000, 260, [("Philippines", 800), ("Panama", 1000), ("Costa Rica", 1000), ("Portugal", 1030), ("Colombia", 1450), ("Dom. Rep.", 1500), ("Thailand", 1930)], 2700, usd, TEAL, labsize=22, valsize=26)
b += f'<line x1="100" y1="{250+260-260*2000/2700:.0f}" x2="1100" y2="{250+260-260*2000/2700:.0f}" stroke="{RED}" stroke-width="4" stroke-dasharray="10 8"/>' + t(100, 250 + 260 - 260 * 2000 / 2700 - 14, "Your $2,000 a month", 24, 800, RED)
add(P, 2, "Destinations That Work on $2,000 a Month", "Retirement visa income requirements below $2,000 a month in seven countries, from $800 in the Philippines to about $1,930 in Thailand",
    "Monthly income a main applicant must show, 2026 (approximate US dollars).",
    frame(TEAL, "Retiring abroad", "Visa income needed: under $2,000", b, "Governments and consulates"))

b = card(56, 190, 1088, 420) + vbars(200, 250, 700, 260, [("Spain", 2690, ORANGE), ("Greece", 3920, ORANGE), ("Mexico", 4500, RED)], 5000, usd, ORANGE, valsize=30)
b += f'<line x1="200" y1="{510-260*2000/5000:.0f}" x2="900" y2="{510-260*2000/5000:.0f}" stroke="{TEAL}" stroke-width="4" stroke-dasharray="10 8"/>' + t(905, 510 - 260 * 2000 / 5000 + 8, "$2,000", 24, 800, TEAL)
b += plane(1030, 330, .9, ORANGE)
add(P, 3, "Destinations That Need More Than $2,000 a Month", "Visa income needed in Spain, Greece and Mexico, all above $2,000 a month",
    "Spain about $2,690, Greece about $3,920 and Mexico about $4,500 a month, unless you qualify with savings.",
    frame(ORANGE, "Retiring abroad", "Popular but harder on $2,000", b, "Governments and consulates"))

parts = [("Rent", 650, TEAL), ("Food", 350, GREEN), ("Health", 250, PURPLE), ("Leisure", 300, AMBER), ("Utilities", 150, BLUE), ("Transport", 100, ORANGE), ("Buffer", 200, "#9aa0ad")]
b = card(56, 190, 1088, 420) + donut(330, 400, 170, parts, 70) + t(330, 400, "$2,000", 50, 800, INK, "middle", "serif") + t(330, 440, "a month", 24, 600, MUTED, "middle")
for i, (lab, v, col) in enumerate(parts):
    y = 235 + i * 52
    b += f'<rect x="600" y="{y}" width="28" height="28" rx="7" fill="{col}"/>' + t(645, y + 24, lab, 27, 700, INK) + t(1080, y + 24, "$%d" % v, 27, 800, INK, "end")
add(P, 4, "A Sample $2,000 Monthly Budget", "Doughnut chart of a sample $2,000 monthly retirement budget: rent, food, health, leisure, utilities, transport and a buffer",
    "A sample budget for a single retiree in a mid-sized city, using midpoints of the ranges in the article.",
    frame(TEAL, "Retiring abroad", "A sample $2,000 monthly budget", b, "Illustrative midpoints"))

if __name__ == "__main__":
    d = os.path.dirname(os.path.abspath(__file__)); os.makedirs(os.path.join(d, "svg"), exist_ok=True)
    for s in S:
        open(os.path.join(d, "svg", "%s-%d.svg" % (s["post"], s["n"])), "w").write(s["svg"])
    json.dump([{k: v for k, v in s.items() if k != "svg"} for s in S], open(os.path.join(d, "specs.json"), "w"), indent=1, ensure_ascii=False)
    print(len(S), "images")
