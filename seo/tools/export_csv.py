# Exports each data-table embed's rows to seo/data/<name>.csv (served at cdn.jsdelivr.net/gh/<repo>@<branch>/seo/data/).
#   python3 seo/tools/export_csv.py
import csv, html, json, os, re
HERE = os.path.dirname(os.path.abspath(__file__)); EMB = os.path.join(HERE, "embeds"); OUT = os.path.join(HERE, "..", "data")
# embed -> (csv name, article key, column headers for c, g, s, l, u, n, value unit)
MAP = {
 "deposit-limits-by-country-table": ("deposit-insurance-limits-2026", "articles/deposit-insurance-limits-by-country-2026", "Scheme", "Limit (local currency)", "Limit (US$)"),
 "minimum-wage-by-country-table": ("minimum-wage-2026", "articles/minimum-wage-by-country-2026", "", "Minimum wage (local)", "Monthly full-time (US$)"),
 "electricity-prices-by-country-table": ("household-electricity-prices-2026", "articles/electricity-prices-by-country-2026", "Basis", "Price per kWh (local)", "Price per kWh (US$)"),
 "income-tax-take-home-by-country-table": ("take-home-pay-average-wage-2025", "articles/income-tax-by-country-take-home-pay", "Average wage", "Share of pay kept", "Income tax + employee social security (% of gross)"),
 "mortgage-rates-by-country-table": ("mortgage-rates-2026", "articles/mortgage-rates-by-country-2026", "", "Type of loan", "Interest rate (%)"),
 "pension-replacement-rate-by-country-table": ("pension-net-replacement-rates", "articles/state-pension-amounts-by-country-2026", "", "Pension", "Net replacement rate, average earner (%)"),
 "inheritance-tax-by-country-table": ("inheritance-tax-2026", "articles/inheritance-tax-by-country-2026", "Basis", "Tax-free amount (children)", "Top rate for children (%)"),
 "retire-abroad-visa-cost-table": ("retirement-visa-income-rules-2026", "articles/retire-abroad-on-2000-a-month", "Visa", "Income rule", "Approx. income needed (US$ a month)"),
}
def strip(s): return re.sub(r"\s+", " ", html.unescape(re.sub(r"<[^>]+>", " ", str(s or "")))).strip()
ids = json.load(open(os.path.join(HERE, "published.json")))
index = []
os.makedirs(OUT, exist_ok=True)
for emb, (name, art, sh, lh, uh) in MAP.items():
    s = open(os.path.join(EMB, emb + ".html"), encoding="utf-8").read()
    i = s.index("var D=") + 6; j = s.index(";\n", i); D = json.loads(s[i:j])
    cols = ["Country", "Region"] + ([sh] if sh else []) + [lh, uh, "Notes"]
    with open(os.path.join(OUT, name + ".csv"), "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f); w.writerow(cols)
        for d in D:
            w.writerow([d.get("c"), d.get("g")] + ([strip(d.get("s"))] if sh else []) + [strip(d.get("l")), "" if d.get("u") is None else d.get("u"), strip(d.get("n"))])
    index.append((name, art, len(D)))
# Hand-built tables with their own shapes
def raw(emb):
    s = open(os.path.join(EMB, emb + ".html"), encoding="utf-8").read(); i = s.index("var D=") + 6; return json.loads(s[i:s.index(";\n", i)])
P = raw("petrol-price-rise-by-country-table")
with open(os.path.join(OUT, "petrol-prices-since-feb-2026.csv"), "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f); w.writerow(["Country", "Price week of 19 Oct 2026", "Price week of 23 Feb 2026", "Peak (week)", "Approx. US$ per litre", "Change (%)"])
    for d in P: w.writerow([d["c"], d["now"], d["pre"], d["pk"], d["u"], d["x"]])
index.append(("petrol-prices-since-feb-2026", "articles/petrol-prices-since-iran-war-by-country", len(P)))
R = raw("retirement-age-by-country-table")
with open(os.path.join(OUT, "retirement-age-2026.csv"), "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f); w.writerow(["Country", "Region", "Pension age (sort value)", "Pension age 2026", "Earliest claim", "Legislated change", "Notes"])
    for d in R: w.writerow([d["c"], d["g"], d["a"], d["d"], d["e"], d["f"], d["n"]])
index.append(("retirement-age-2026", "articles/retirement-age-by-country-2026", len(R)))
SF = raw("salary-forecast-2027-table")
keys = list(SF[0].keys())
NAMES = {"c": "Country", "g": "Region", "f": "2027 raise forecast (%)", "p26": "2026 raise (%)", "s": "Forecasts by survey (%)", "i": "Latest inflation (%)", "n": "Inflation month", "r": "Real raise vs inflation (points)"}
with open(os.path.join(OUT, "salary-increase-forecasts-2027.csv"), "w", newline="", encoding="utf-8") as f:
    w = csv.writer(f); w.writerow([NAMES.get(k, k) for k in keys])
    for d in SF: w.writerow([strip(d[k]) if isinstance(d[k], str) else json.dumps(d[k]) if isinstance(d[k], (list, dict)) else d[k] for k in keys])
index.append(("salary-increase-forecasts-2027", "articles/salary-increase-forecast-2027-by-country", len(SF)))
json.dump([{"csv": n, "url": ids[a]["url"], "rows": r} for n, a, r in index], open(os.path.join(OUT, "index.json"), "w"), indent=1)
for n, a, r in index: print(n, r)
