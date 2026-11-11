"""Shared helpers for the November 2026 US batch 3 articles. Every number in a worked example is computed here."""
import json, os
HERE = os.path.dirname(os.path.abspath(__file__))
BLOG = "https://randomthoughtsever.blogspot.com"
UPDATED = "November 2026"

def money(x, d=0):
    return f"${x:,.{d}f}"

def pmt(principal, apr, months):
    r = apr / 100 / 12
    return principal / months if r == 0 else principal * r / (1 - (1 + r) ** -months)

def table(head, rows, cls=""):
    h = "".join(f"<th>{c}</th>" for c in head)
    b = "\n".join("<tr>" + "".join(f"<td>{c}</td>" for c in r) + "</tr>" for r in rows)
    return f"<table>\n<thead><tr>{h}</tr></thead>\n<tbody>\n{b}\n</tbody>\n</table>"

def ul(items):
    return "<ul>\n" + "\n".join(f"<li>{i}</li>" for i in items) + "\n</ul>"

def faq(pairs):
    return "\n".join(f"<h3>{q}</h3>\n<p>{a}</p>" for q, a in pairs)

def sources(items):
    return "<h2>Sources</h2>\n" + ul([f'<a href="{u}" rel="noopener" target="_blank">{t}</a>' for t, u in items])

def related(items):
    return "<h2>Related Guides</h2>\n" + ul([f'<a href="{BLOG}{u}">{t}</a>' for t, u in items])

def states(occ):
    """BLS OEWS May 2025 state rows for one occupation: the 50 states and DC only, numeric medians only."""
    d = json.load(open(os.path.join(HERE, "data", "bls_oews_may2025_states.json")))
    out = []
    for o in d:
        if o["OCC_CODE"] != occ or o["AREA_TITLE"] in ("Guam", "Puerto Rico", "Virgin Islands"):
            continue
        try:
            o = {**o, **{k: float(o[k]) for k in ("A_MEDIAN", "H_MEDIAN", "A_PCT10", "A_PCT90", "A_MEAN")}, "TOT_EMP": int(o["TOT_EMP"])}
        except (TypeError, ValueError):
            continue
        out.append(o)
    return out

def nat(occ):
    """BLS OEWS May 2025 national row."""
    d = json.load(open(os.path.join(HERE, "data", "bls_oews_may2025_national.json")))
    r = next(o for o in d if o["OCC_CODE"] == occ)
    f = lambda k: float(r[k]) if r[k] not in ("*", "#", None) else None
    return dict(title=r["OCC_TITLE"], emp=int(r["TOT_EMP"]), h_mean=f("H_MEAN"), a_mean=f("A_MEAN"), h_med=f("H_MEDIAN"),
                p10=f("A_PCT10"), p25=f("A_PCT25"), p50=f("A_MEDIAN"), p75=f("A_PCT75"), p90=f("A_PCT90"))

def state_table(S, ALL):
    S = sorted(S, key=lambda s: -s["A_MEDIAN"])
    rows = [[i, s["AREA_TITLE"], f"${s['H_MEDIAN']:.2f}", money(s["A_MEDIAN"]), money(s["A_PCT10"]), money(s["A_PCT90"]),
             f"{s['TOT_EMP']:,}", f"{s['A_MEDIAN'] / float(ALL[s['AREA_TITLE']]['A_MEDIAN']):.1f}x"] for i, s in enumerate(S, 1)]
    return table(["#", "State", "Median hourly", "Median annual", "Low (10%)", "High (90%)", "Employed", "Vs typical job in state"], rows)

def save(slug, title, labels, body):
    body = f"<p><em>Updated {UPDATED}</em></p>\n<!--cover-->\n\n" + body.strip() + "\n"
    body = body.replace(" in District of Columbia", " in the District of Columbia")
    open(os.path.join(HERE, "html", slug + ".html"), "w").write(body)
    meta_p = os.path.join(HERE, "articles.json")
    meta = json.load(open(meta_p)) if os.path.exists(meta_p) else {}
    meta[slug] = {**meta.get(slug, {}), "title": title, "labels": labels}
    json.dump(meta, open(meta_p, "w"), indent=1)
    import re, html as H
    words = len(H.unescape(re.sub(r"<[^>]+>", " ", body)).split())
    print(f"{slug}: {words} words, title {len(title)} chars")

L_EXTRA = {}
# Internal links (real titles and paths of live posts)
L = {
 "raise": ("Raise Calculator: What Is a Fair Pay Raise in 2026 After Inflation?", "/2026/10/what-is-fair-pay-raise-in-2026-how-to.html"),
 "salary2027": ("Salary Increase 2027 Forecasts by Country: Every Major Survey", "/2026/10/salary-increase-forecasts-2027-by.html"),
 "ai_jobs": ("What Jobs Will AI Replace in 2026, and Which Skills Keep You Safe?", "/2026/10/jobs-ai-is-replacing-in-2026-and-skills.html"),
 "layoff": ("How to Explain a Gap in Employment or a Layoff in an Interview", "/2026/10/how-to-explain-layoff-or-career-gap-in.html"),
 "career": ("Career Guide: Work Smarter, Avoid Burnout and Grow", "/2026/10/career-guide-work-smarter-avoid-burnout.html"),
 "efund": ("How Much Emergency Fund Do You Need? (With Calculator)", "/2026/10/how-much-emergency-fund-do-you-need.html"),
 "score": ("Credit Karma vs FICO: Why Is My Credit Score Different on Every App?", "/2026/10/why-is-my-credit-score-different-on.html"),
 "payoff": ("Credit Card Payoff Calculator: How Long Will Your Debt Take?", "/2026/10/credit-card-payoff-calculator-how-long.html"),
 "consolidation": ("Debt Consolidation Loans: When They Save Money (Calculator)", "/2026/10/debt-consolidation-loans-when-they-save.html"),
 "rentbuy": ("Rent or Buy? Rent vs Buy Calculator With Mortgage Rates Above 7%", "/2026/10/should-you-buy-house-with-mortgage.html"),
 "rates": ("Will Interest Rates Go Up? 7 Money Moves to Make Now", "/2026/10/interest-rates-are-rising-again-7-money.html"),
 "cash": ("Best Place to Keep Cash When Rates Rise: Savings, T-Bills or Funds", "/2026/10/where-to-keep-your-cash-when-rates-rise.html"),
 "badcar": ("Car Loan With Terrible Credit: What Lenders Approve and the Real Rates", "/2026/11/car-loan-with-terrible-credit-what.html"),
 "afford_car": ("How Much Car Can I Afford? Car Affordability Calculator for 2026", "/2026/10/how-to-afford-car-in-2026-when-average.html"),
 "ev": ("Electric Car vs Petrol Cost: Is an EV Cheaper to Run? (Calculator)", "/2026/10/is-it-cheaper-to-charge-ev-or-fill-up.html"),
 "us_numbers": ("US Minimum Wage 2026: Tax, Mortgages, Pensions and Energy", "/2026/10/the-united-states-in-numbers-2026.html"),
 "minwage_world": ("Minimum Wage by Country 2026: Hourly and Monthly Rates in US Dollars", "/2026/10/minimum-wage-by-country-2026-hourly-and.html"),
 "life_ins": ("Life Insurance Calculator: How Much Does Life Insurance Cost by Age?", "/2026/10/how-much-does-life-insurance-cost-by.html"),
 "car_ins": ("Why Did My Car Insurance Go Up When I Never Made a Claim?", "/2026/10/why-did-my-insurance-premium-go-up-when.html"),
 "student": ("Subsidized vs Unsubsidized Student Loans: Which Costs Less", "/2026/11/subsidized-vs-unsubsidized-student.html"),
 "aotc": ("American Opportunity Tax Credit 2026: Who Gets $2,500 and How to Claim It", "/2026/11/american-opportunity-tax-credit-2026.html"),
 "second": ("Second Chance Bank Accounts 2026: Every Option Compared", "/2026/11/second-chance-bank-accounts-2026-every.html"),
 "dental_loans": ("Dental Implant Loans: Real Financing Options and What They Cost", "/2026/11/dental-implant-loans-real-financing.html"),
 "lump": ("Lump Sum or Monthly Pension: Which Should You Take? (Calculator)", "/2026/10/lump-sum-or-monthly-pension-which.html"),
 "retire_age": ("Retirement Age by Country 2026, Including Retirement Age in Europe", "/2026/10/retirement-age-by-country-2026-state.html"),
 "pf": ("Personal Finance for Beginners: A Step-by-Step Guide", "/2026/10/personal-finance-for-beginners-step-by.html"),
}
