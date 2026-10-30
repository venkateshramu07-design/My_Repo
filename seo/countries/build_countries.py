# Build one "money facts" page per country from the open datasets in seo/data/, plus a compare-countries hub.
# Every figure comes from the CSVs behind our data guides; each section links to the guide with sources.
#   python3 seo/countries/build_countries.py      # writes seo/countries/<slug>.html and countries.json
import csv, json, os, re, html

HERE = os.path.dirname(os.path.abspath(__file__))
DATA = os.path.join(HERE, "..", "data")
UPDATED = "25 October 2026"
EU = {"Austria", "Belgium", "Bulgaria", "Croatia", "Cyprus", "Czechia", "Denmark", "Estonia", "Finland", "France", "Germany",
      "Greece", "Hungary", "Ireland", "Italy", "Latvia", "Lithuania", "Luxembourg", "Malta", "Netherlands", "Poland", "Portugal",
      "Romania", "Slovakia", "Slovenia", "Spain", "Sweden"}
NO_MIN_WAGE = {"Italy", "Denmark", "Finland", "Sweden", "Austria", "Norway"}  # wages set by collective agreements
NOT_COUNTRY = re.compile(r"average|European Union", re.I)


def load(name):
    out = {}
    for r in csv.DictReader(open(os.path.join(DATA, name + ".csv"), encoding="utf-8")):
        c = r[next(iter(r))]
        r["_label"] = c  # e.g. "United States (federal)"
        out[{"Türkiye": "Turkey"}.get(c, re.sub(r"\s*\(.*\)$", "", c))] = r
    return out


def num(v):
    try: return float(str(v).replace(",", ""))
    except ValueError: return None


def ordinal(n):
    return "%d%s" % (n, "th" if 10 <= n % 100 <= 20 else {1: "st", 2: "nd", 3: "rd"}.get(n % 10, "th"))


def rank(table, col, country, high_first=True):
    vals = sorted(((num(r[col]), c) for c, r in table.items() if not NOT_COUNTRY.search(c) and num(r[col]) is not None),
                  key=lambda x: -x[0] if high_first else x[0])
    for i, (_, c) in enumerate(vals):
        if c == country: return i + 1, len(vals)
    return None, len(vals)


def pos(r, n, hi="highest", lo="lowest"):
    if r == 1: return f"the {hi} of {n} countries we compare"
    if r == n: return f"the {lo} of {n} countries we compare"
    if r <= n / 2: return f"{ordinal(r)} {hi} of {n} countries"
    return f"{ordinal(n - r + 1)} {lo} of {n} countries"


def short(r, n, hi="highest", lo="lowest"):
    if r is None: return "—"
    if r == 1: return f"{hi.capitalize()} of {n}"
    if r == n: return f"{lo.capitalize()} of {n}"
    return f"{ordinal(r)} {hi} of {n}" if r <= n / 2 or r == 1 else f"{ordinal(n - r + 1)} {lo} of {n}"


def lc(t):
    return t[0].lower() + t[1:] if t.split()[0] in {"New", "Bank", "Average", "Variable"} else t


def clean(t):
    return re.sub(r"\s+\.", ".", (t or "").strip())


D = {k: load(k) for k in ["minimum-wage-2026", "take-home-pay-average-wage-2025", "mortgage-rates-2026", "household-electricity-prices-2026",
                          "petrol-prices-since-feb-2026", "retirement-age-2026", "pension-net-replacement-rates", "deposit-insurance-limits-2026",
                          "inheritance-tax-2026", "retirement-visa-income-rules-2026", "salary-increase-forecasts-2027"]}
GUIDE = {"minimum-wage-2026": "articles/minimum-wage-by-country-2026", "take-home-pay-average-wage-2025": "articles/income-tax-by-country-take-home-pay",
         "mortgage-rates-2026": "articles/mortgage-rates-by-country-2026", "household-electricity-prices-2026": "articles/electricity-prices-by-country-2026",
         "petrol-prices-since-feb-2026": "articles/petrol-prices-since-iran-war-by-country", "retirement-age-2026": "articles/retirement-age-by-country-2026",
         "pension-net-replacement-rates": "articles/state-pension-amounts-by-country-2026", "deposit-insurance-limits-2026": "articles/deposit-insurance-limits-by-country-2026",
         "inheritance-tax-2026": "articles/inheritance-tax-by-country-2026", "retirement-visa-income-rules-2026": "articles/retire-abroad-on-2000-a-month",
         "salary-increase-forecasts-2027": "articles/salary-increase-forecast-2027-by-country"}
CSV_URL = "https://cdn.jsdelivr.net/gh/venkateshramu07-design/My_Repo@ccr-ee047f91-rbyv4x/seo/data/"


def the(c):
    return "the " + c if c in {"United States", "United Kingdom", "Netherlands", "Philippines", "Czech Republic"} else c


def slug(c):
    return re.sub(r"[^a-z0-9]+", "-", c.lower()).strip("-")


def country_page(c):
    T = the(c); Tc = T[0].upper() + T[1:]
    rows, secs, faq, quick, used = [], [], [], [], []

    def add(key, measure, value, rk, h2, paras, q=None):
        used.append(key)
        rows.append((measure, value, rk or "—"))
        secs.append(f"<h2>{h2}</h2>\n" + "\n".join(f"<p>{p}</p>" for p in paras) +
                    f"\n<p>Full table for every country, with sources: [[post:{GUIDE[key]}]]. "
                    f'<a href="{CSV_URL}{key}.csv">Download the data (CSV)</a>.</p>')
        if q: faq.append(q)

    t = D["minimum-wage-2026"]
    if c in t:
        r = t[c]; rk, n = rank(t, "Monthly full-time (US$)", c); note = clean(r["Notes"])
        quick.append(f"the minimum wage is {r['Minimum wage (local)']} (about ${int(num(r['Monthly full-time (US$)'])):,} a month full time)")
        add("minimum-wage-2026", "Minimum wage", f"{r['Minimum wage (local)']} (≈ ${int(num(r['Monthly full-time (US$)'])):,}/month)", short(rk, n),
            f"Minimum Wage in {T} (2026)",
            [f"The legal minimum wage in {T} is <strong>{r['Minimum wage (local)']}</strong>. For a month of full-time work that is about "
             f"<strong>${int(num(r['Monthly full-time (US$)'])):,}</strong> at current exchange rates, {pos(rk, n)} in our comparison."
             + (f" {note}." if note and not note.endswith(".") else (" " + note if note else ""))],
            (f"What is the minimum wage in {T} in 2026?", f"The minimum wage in {T} is {r['Minimum wage (local)']}, about ${int(num(r['Monthly full-time (US$)'])):,} a month for full-time work. "
             f"That is {pos(rk, n)}." + (f" {note.rstrip('.')}." if note else "")))
    elif c in NO_MIN_WAGE:
        quick.append("there is no national minimum wage (pay floors are set by collective agreements)")
        add("minimum-wage-2026", "Minimum wage", "None by law (collective agreements)", "—", f"Minimum Wage in {T} (2026)",
            [f"{Tc} has <strong>no statutory national minimum wage</strong>. Pay floors are set sector by sector in collective agreements "
             "between unions and employers, which cover most workers and are often higher than the legal minimums of neighbouring countries."],
            (f"Does {T} have a minimum wage?", f"No. {T} has no national minimum wage set by law; minimum pay is agreed sector by sector in collective agreements."))

    t = D["take-home-pay-average-wage-2025"]
    if c in t:
        r = t[c]; rk, n = rank(t, "Income tax + employee social security (% of gross)", c); tax = num(r["Income tax + employee social security (% of gross)"])
        avg = num(t.get("OECD average", {}).get("Income tax + employee social security (% of gross)"))
        wage = r["Average wage"].replace("Average wage: ", "").replace(" (PPP)", " at purchasing-power parity")
        quick.append(f"an average earner keeps {r['Share of pay kept'].replace(' kept', '')} of their pay after income tax and social security")
        add("take-home-pay-average-wage-2025", "Tax on an average wage", f"{tax:g}% (keeps {r['Share of pay kept'].replace(' kept', '')})", short(rk, n),
            f"Income Tax and Take-Home Pay in {T}",
            [f"A single worker on the average wage in {T} ({wage}) pays <strong>{tax:g}%</strong> of gross pay in income tax and employee social security, "
             f"so they keep <strong>{r['Share of pay kept'].replace(' kept', '')}</strong>. That is {pos(rk, n)}"
             + (f", against an OECD average of {avg:g}%." if avg else "."), clean(r["Notes"])],
            (f"How much tax do you pay in {T}?", f"On the average wage, income tax plus employee social security in {T} take {tax:g}% of gross pay, so a single worker keeps "
             f"{r['Share of pay kept'].replace(' kept', '')}" + (f" (OECD average: {avg:g}%)." if avg else ".")))

    t = D["salary-increase-forecasts-2027"]
    if c in t:
        r = t[c]; rk, n = rank(t, "2027 raise forecast (%)", c); real = num(r["Real raise vs inflation (points)"])
        add("salary-increase-forecasts-2027", "Pay rise forecast 2027", f"{r['2027 raise forecast (%)']}% (inflation {r['Latest inflation (%)']}%)", short(rk, n),
            f"Pay Rises in {T}: 2027 Forecast",
            [f"Employers in {T} expect to raise salaries by <strong>{r['2027 raise forecast (%)']}%</strong> in 2027 (median of the major pay surveys), after "
             f"{r['2026 raise (%)']}% in 2026. With inflation at {r['Latest inflation (%)']}% ({r['Inflation month']}), that is "
             + (f"a real rise of about {real:g} points." if real and real > 0 else f"a real cut of about {abs(real):g} points unless inflation falls." if real and real < 0 else "roughly flat in real terms.")],
            (f"What is the average pay rise in {T} for 2027?", f"Pay surveys forecast an average salary increase of {r['2027 raise forecast (%)']}% in {T} for 2027, against inflation of "
             f"{r['Latest inflation (%)']}%."))

    t = D["mortgage-rates-2026"]
    if c in t:
        r = t[c]; rk, n = rank(t, "Interest rate (%)", c); avg = num(t.get("Euro area average", {}).get("Interest rate (%)"))
        quick.append(f"mortgage rates are about {num(r['Interest rate (%)']):g}%")
        add("mortgage-rates-2026", "Mortgage rate", f"{num(r['Interest rate (%)']):g}% ({lc(r['Type of loan'])})", short(rk, n),
            f"Mortgage Rates in {T} (2026)",
            [f"The typical mortgage rate in {T} is <strong>{num(r['Interest rate (%)']):g}%</strong> ({lc(r['Type of loan'])}), {pos(rk, n)}"
             + (f"; the euro area average is {avg:g}%." if avg and c in EU else "."), clean(r["Notes"])],
            (f"What are mortgage rates in {T} right now?", f"Mortgage rates in {T} are about {num(r['Interest rate (%)']):g}% ({lc(r['Type of loan'])}). " + clean(r["Notes"])))

    t = D["household-electricity-prices-2026"]
    if c in t:
        r = t[c]; rk, n = rank(t, "Price per kWh (US$)", c); eu = t.get("EU average")
        quick.append(f"household electricity costs {r['Price per kWh (local)']} per kWh")
        add("household-electricity-prices-2026", "Electricity (household)", f"{r['Price per kWh (local)']}/kWh (≈ ${num(r['Price per kWh (US$)']):.2f})", short(rk, n),
            f"Electricity Prices in {T}",
            [f"Households in {T} pay about <strong>{r['Price per kWh (local)']} per kWh</strong> of electricity (about ${num(r['Price per kWh (US$)']):.2f}), "
             f"{pos(rk, n, 'most expensive', 'cheapest')}" + (f"; the EU average is {eu['Price per kWh (local)']}." if eu and c in EU else ".")
             + (f" Change: {r['Basis']}." if "vs" in r["Basis"] else f" Basis: {r['Basis']}."), clean(r["Notes"])],
            (f"How much does electricity cost in {T}?", f"Household electricity in {T} costs about {r['Price per kWh (local)']} per kWh (about ${num(r['Price per kWh (US$)']):.2f}), "
             f"{pos(rk, n, 'most expensive', 'cheapest')}."))

    t = D["petrol-prices-since-feb-2026"]
    if c in t:
        r = t[c]; rk, n = rank(t, "Approx. US$ per litre", c); ch = num(r["Change (%)"]); crk, _ = rank(t, "Change (%)", c)
        quick.append(f"petrol is {r['Price week of 19 Oct 2026']}, up {ch:g}% since February")
        add("petrol-prices-since-feb-2026", "Petrol price", f"{r['Price week of 19 Oct 2026']} (+{ch:g}% since Feb)", short(rk, n),
            f"Petrol Prices in {T} Since the Iran War",
            [f"Petrol in {T} costs <strong>{r['Price week of 19 Oct 2026']}</strong> (week of 19 October 2026), up <strong>{ch:g}%</strong> from "
             f"{r['Price week of 23 Feb 2026']} in the week before the Iran war began. It peaked at {r['Peak (week)']}. "
             f"At about ${num(r['Approx. US$ per litre']):.2f} a litre it is {pos(rk, n, 'most expensive', 'cheapest')}, and the rise is "
             f"{pos(crk, n, 'largest', 'smallest')}."],
            (f"How much has petrol gone up in {T}?", f"Petrol in {T} has risen {ch:g}% since February 2026, from {r['Price week of 23 Feb 2026']} to {r['Price week of 19 Oct 2026']}."))

    t = D["retirement-age-2026"]
    if c in t:
        r = t[c]; rk, n = rank(t, "Pension age (sort value)", c)
        quick.append(f"the state pension age is {r['Pension age 2026']}")
        chg = r["Legislated change"]
        add("retirement-age-2026", "State pension age", r["Pension age 2026"], short(rk, n),
            f"Retirement Age in {T}",
            [f"The state pension age in {T} is <strong>{r['Pension age 2026']}</strong> for people reaching it in 2026; the earliest you can claim is "
             f"{r['Earliest claim']}. Future change: {chg[0].lower() + chg[1:] if chg.startswith('None') else chg}. {clean(r['Notes'])}"],
            (f"What is the retirement age in {T}?", f"The state pension age in {T} is {r['Pension age 2026']} in 2026, with the earliest claim at {r['Earliest claim']}. "
             f"Legislated change: {chg}."))

    t = D["pension-net-replacement-rates"]
    if c in t:
        r = t[c]; rk, n = rank(t, "Net replacement rate, average earner (%)", c); oecd = t.get("OECD average")
        rate = num(r["Net replacement rate, average earner (%)"])
        add("pension-net-replacement-rates", "Pension (share of pay)", f"{rate:g}% of net pay", short(rk, n),
            f"How Much the State Pension Pays in {T}",
            [f"A full-career average earner in {T} can expect a mandatory pension worth about <strong>{rate:g}% of their previous net pay</strong>, "
             f"{pos(rk, n, 'most generous', 'least generous')}" + (f" (OECD average {num(oecd['Net replacement rate, average earner (%)']):g}%)." if oecd else ".")
             + " " + clean(r["Notes"])],
            (f"How much is the state pension in {T}?", f"In {T}, mandatory pensions replace about {rate:g}% of an average earner's net pay after a full career"
             + (f", against an OECD average of {num(oecd['Net replacement rate, average earner (%)']):g}%." if oecd else ".")))

    t = D["deposit-insurance-limits-2026"]
    r = t.get(c) or (t.get("European Union") if c in EU else None)
    if r:
        add("deposit-insurance-limits-2026", "Bank deposit protection", r["Limit (local currency)"] + " per person, per bank", "—",
            f"How Much of Your Money Is Protected in {T}",
            [f"Bank deposits in {T} are protected up to <strong>{r['Limit (local currency)']}</strong> per depositor, per bank"
             + (f" (about ${int(num(r['Limit (US$)'])):,})" if num(r["Limit (US$)"]) and not r["Limit (local currency)"].startswith("$") else "")
             + (f", under the {r['Scheme']} scheme." if r["Scheme"] != "National schemes" else ", the limit set by EU law for every member state.")
             + (" " + clean(r["Notes"]) + ("" if clean(r["Notes"]).endswith(".") else ".") if clean(r["Notes"]) else "")
             + " Money in payment apps and e-money wallets is usually not covered."],
            (f"How much money is insured in a bank in {T}?", f"Deposits in {T} are insured up to {r['Limit (local currency)']} per person, per bank."))

    t = D["inheritance-tax-2026"]
    if c in t:
        r = t[c]; top = num(r["Top rate for children (%)"])
        if top:
            rk, n = rank(t, "Top rate for children (%)", c)
            p = (f"{Tc} has <strong>{r['Basis'].lower()}</strong>. Children can receive <strong>{r['Tax-free amount (children)']}</strong> tax-free, and the top rate for "
                 f"children is <strong>{top:g}%</strong>, {pos(rk, n)}. {clean(r['Notes'])}")
            fa = f"Yes. Children can inherit {r['Tax-free amount (children)']} tax-free in {T}; above that the top rate for children is {top:g}%."
        else:
            p = f"{Tc} has <strong>no inheritance tax</strong> for children. {clean(r['Notes'])}"
            fa = f"No. {T} does not tax inheritances passed to children. {clean(r['Notes'])}"
        add("inheritance-tax-2026", "Inheritance tax (children)", f"top rate {top:g}%" if top else "None", "—",
            f"Inheritance Tax in {T}", [p], (f"Is there inheritance tax in {T}?", fa))

    t = D["retirement-visa-income-rules-2026"]
    if c in t:
        r = t[c]
        add("retirement-visa-income-rules-2026", "Retirement visa income", r["Income rule"], "—", f"Retiring to {T} From Abroad",
            [f"Foreign retirees usually move to {T} on the <strong>{r['Visa']}</strong>, which requires <strong>{r['Income rule']}</strong>"
             + (f" (about ${int(num(r['Approx. income needed (US$ a month)'])):,} a month)" if num(r['Approx. income needed (US$ a month)']) else "") + f". {clean(r['Notes'])}"],
            (f"How much income do you need to retire in {T}?", f"The {r['Visa']} asks for {r['Income rule']}."))

    title_bits = [m for m in ["Minimum Wage", "Tax", "Mortgage Rates", "Pension Age", "Energy Prices"]
                  if {"Minimum Wage": "minimum-wage-2026", "Tax": "take-home-pay-average-wage-2025", "Mortgage Rates": "mortgage-rates-2026",
                      "Pension Age": "retirement-age-2026", "Energy Prices": "household-electricity-prices-2026"}[m] in used]
    # Lead with the phrase people search ("<country> minimum wage"), or income tax where there is no minimum wage.
    tname = {"The United States": "US", "The United Kingdom": "UK"}.get(Tc, Tc[4:] if Tc.startswith("The ") else Tc)
    lead, rest = ("Minimum Wage" if title_bits[0] == "Minimum Wage" else "Income Tax"), title_bits[1:]
    sh = {"Tax": "Tax", "Mortgage Rates": "Mortgages", "Pension Age": "Pensions", "Energy Prices": "Energy"}
    rest = [sh[x] for x in rest]
    title = (f"{tname} {lead} 2026" if not rest else f"{tname} {lead} 2026 and {rest[0]}" if len(rest) == 1
             else f"{tname} {lead} 2026: " + ", ".join(rest[:-1]) + " and " + rest[-1])
    table = ('<table class="bd-cf"><thead><tr><th>Measure</th><th>' + html.escape(c) + "</th><th>Rank</th></tr></thead><tbody>\n" +
             "\n".join(f"<tr><td>{m}</td><td>{html.escape(v)}</td><td>{k}</td></tr>" for m, v, k in rows) + "\n</tbody></table>")
    qa = "\n".join(f"<h3>{html.escape(q)}</h3>\n<p>{html.escape(a)}</p>" for q, a in faq)
    q = "; ".join(quick[:5])
    out = f"""<p><em>Updated {UPDATED}</em></p>

<p><strong>{"Quick answer on " + tname + " " + lead.lower() + ":" if tname != T else "Quick answer:"}</strong> In {T} in 2026, {q}. The table below puts {len(rows)} everyday money measures for {T} side by side and ranks each one against the other countries in our data, using official figures from national statistics offices, central banks, the OECD and Eurostat.</p>

<p>Most comparisons of {T} look at one number at a time. This page gathers the figures that matter for working, buying a home, paying bills and retiring in {T}, so you can see them together and see how {T} compares. Each section links to our full guide for that topic, which covers every country and lists the original sources, and each dataset can be downloaded free.</p>

<h2>{Tc} at a Glance</h2>
{table}
<p><small>Rank compares {T} with the other countries in each dataset; "highest" means the largest value. "—" means the measure isn't ranked.</small></p>

{chr(10).join(secs)}

<h2>Frequently Asked Questions: {tname} {lead} 2026</h2>
{qa}

<h2>Compare {T} With Other Countries</h2>
<p>See every country side by side in [[post:countries/compare-countries-money-facts-2026]], or open a country page: {"{{SIBLINGS}}"}.</p>

<h2>Related Guides</h2>
<ul>
{chr(10).join(f"<li>[[post:{GUIDE[k]}]]</li>" for k in used)}
<li>[[post:articles/emergency-fund-guide]]</li>
</ul>
<p><em>Figures checked against the official sources listed in each linked guide in October 2026. Exchange-rate conversions are approximate. This page is for information and isn't financial or legal advice.</em></p>
"""
    out = re.sub(r"\) \(about ([^()]*)\)", r"), about \1", out)  # "¥1,177/hour (average), about $1,301 ..."
    return title, out, len(used)


def main():
    counts = {}
    for t in D.values():
        for c in t:
            if not NOT_COUNTRY.search(c): counts[c] = counts.get(c, 0) + 1
    countries = sorted(c for c, n in counts.items() if n >= 6)
    pages = {}
    for c in countries:
        title, body, n = country_page(c)
        pages[c] = (title, body)
    names = {c: f"countries/{slug(c)}-money-facts-2026" for c in countries}
    meta = {}
    for c, (title, body) in pages.items():
        sib = ", ".join(f"[[post:{names[o]}|{the(o)}]]" for o in countries if o != c)
        open(os.path.join(HERE, names[c].split("/")[1] + ".html"), "w", encoding="utf-8").write(body.replace("{{SIBLINGS}}", sib))
        meta[names[c]] = [title, ["Countries & Living Abroad"]]
    hub(countries, names)
    meta["countries/compare-countries-money-facts-2026"] = ["Compare Countries 2026: Wages, Tax, Mortgages and Pensions",
                                                           ["Countries & Living Abroad"]]
    json.dump(meta, open(os.path.join(HERE, "countries.json"), "w"), indent=1, ensure_ascii=False)
    print(len(countries), "country pages:", ", ".join(countries))


def cell(key, c, col, fmt):
    r = D[key].get(c)
    if r is None and key == "deposit-insurance-limits-2026" and c in EU: r = D[key]["European Union"]
    if r is None: return "None" if key == "minimum-wage-2026" and c in NO_MIN_WAGE else "—"
    return fmt(r[col]) if r[col] not in ("", None) else "—"


def hub(countries, names):
    cols = [("Minimum wage (US$/month)", "minimum-wage-2026", "Monthly full-time (US$)", lambda v: f"${int(num(v)):,}"),
            ("Tax on average wage", "take-home-pay-average-wage-2025", "Income tax + employee social security (% of gross)", lambda v: f"{num(v):g}%"),
            ("Mortgage rate", "mortgage-rates-2026", "Interest rate (%)", lambda v: f"{num(v):g}%"),
            ("Electricity (US$/kWh)", "household-electricity-prices-2026", "Price per kWh (US$)", lambda v: f"${num(v):.2f}"),
            ("Petrol rise since Feb", "petrol-prices-since-feb-2026", "Change (%)", lambda v: f"+{num(v):g}%"),
            ("Pension age", "retirement-age-2026", "Pension age 2026", lambda v: v),
            ("Pension (% of net pay)", "pension-net-replacement-rates", "Net replacement rate, average earner (%)", lambda v: f"{num(v):g}%")]
    head = "<tr><th>Country</th>" + "".join(f"<th>{h}</th>" for h, *_ in cols) + "</tr>"
    body = "\n".join("<tr><td>[[post:%s|%s]]</td>%s</tr>" % (names[c], c, "".join(f"<td>{html.escape(cell(k, c, col, f))}</td>" for _, k, col, f in cols))
                     for c in countries)

    def top(key, col, hi=True, k=3):
        vals = sorted(((num(D[key][c][col]), c) for c in countries if c in D[key] and num(D[key][c][col]) is not None), key=lambda x: -x[0] if hi else x[0])
        names = [the(c) for _, c in vals[:k]]
        return ", ".join(names[:-1]) + " and " + names[-1]
    out = f"""<p><em>Updated {UPDATED}</em></p>

<p><strong>Quick answer on how to compare countries:</strong> Among the {len(countries)} countries compared here, the highest minimum wages (in US dollars a month) are in {top("minimum-wage-2026", "Monthly full-time (US$)")}; the average worker pays the most tax in {top("take-home-pay-average-wage-2025", "Income tax + employee social security (% of gross)")}; mortgages are dearest in {top("mortgage-rates-2026", "Interest rate (%)")}; household electricity is most expensive in {top("household-electricity-prices-2026", "Price per kWh (US$)")}; and state pension ages are highest in {top("retirement-age-2026", "Pension age (sort value)")}. The table puts seven measures side by side, and each country links to a page with the full figures.</p>

<p>Deciding where to work, buy a home or retire means comparing several numbers at once. This hub brings together the figures from our country-by-country data guides, all from official sources (national statistics offices, central banks, the OECD and Eurostat), and every dataset is free to download.</p>

<h2>Compare Countries Side by Side</h2>
<div style="overflow-x:auto;-webkit-overflow-scrolling:touch"><table class="bd-cf"><thead>{head}</thead><tbody>
{body}
</tbody></table></div>
<p><small>"None": no statutory minimum wage (collective agreements). "—": not in that dataset.</small></p>

<h2 id="downloads">Download the Data</h2>
<p>Every table behind these pages is free to download as a CSV file and reuse with credit to Blog Digest (CC BY 4.0):</p>
<ul>
{chr(10).join(f'<li><a href="{CSV_URL}{k}.csv">{html.escape(k.replace("-", " ").capitalize())}</a> (from [[post:{g}]])</li>' for k, g in GUIDE.items())}
</ul>

<h2>How to Use These Comparisons</h2>
<ul>
<li><strong>Wages and tax together:</strong> a high minimum wage can come with high tax. Compare the US-dollar minimum wage with the share of pay the average worker keeps.</li>
<li><strong>Homes:</strong> the mortgage rate sets the monthly payment. A 2-point difference adds about 20% to the payment on a 25-year loan.</li>
<li><strong>Bills:</strong> electricity prices vary more than threefold. Petrol rises since February show where the Iran war hit hardest.</li>
<li><strong>Retirement:</strong> look at the pension age and the share of pay the pension replaces together; a later age with a generous pension can beat an early age with a small one.</li>
</ul>

<h2>Frequently Asked Questions: Compare Countries</h2>
<h3>Which country has the highest minimum wage in 2026?</h3>
<p>Converted to US dollars for a month of full-time work, the highest minimum wages are in {top("minimum-wage-2026", "Monthly full-time (US$)")}.</p>
<h3>Which country takes the most tax from an average worker?</h3>
<p>Counting income tax and employee social security on the average wage, the highest are {top("take-home-pay-average-wage-2025", "Income tax + employee social security (% of gross)")}; the lowest are {top("take-home-pay-average-wage-2025", "Income tax + employee social security (% of gross)", False)}.</p>
<h3>Where are mortgage rates lowest?</h3>
<p>Among these countries, the lowest typical mortgage rates are in {top("mortgage-rates-2026", "Interest rate (%)", False)}.</p>
<h3>Which country has the cheapest electricity?</h3>
<p>The cheapest household electricity, in US dollars per kWh, is in {top("household-electricity-prices-2026", "Price per kWh (US$)", False)}.</p>
<h3>Where is the retirement age lowest?</h3>
<p>The lowest state pension ages among these countries are in {top("retirement-age-2026", "Pension age (sort value)", False)}.</p>

<h2>Related Guides</h2>
<ul>
{chr(10).join(f"<li>[[post:{g}]]</li>" for g in GUIDE.values())}
</ul>
<p><em>Figures from the official sources listed in each linked guide, checked in October 2026. Exchange-rate conversions are approximate. For information only; not financial advice.</em></p>
"""
    open(os.path.join(HERE, "compare-countries-money-facts-2026.html"), "w", encoding="utf-8").write(out)


if __name__ == "__main__":
    main()
