# Publish the calculator posts in seo/tools/ and the topic hubs in seo/hubs/.
# New posts are created once; later runs update them in place.
# In hub pages, [[year/month/slug]] becomes a link with the post's current title, [[tool:N]] a link to
# CALCULATORS[N] in apply.py, [[hub:name]] a link to another published hub, [[post:name]] a link to a post
# published from here, and [[embed:name]] inserts the widget seo/tools/embeds/<name>.html.
#   python3 seo/publish_tools.py build <name>     # print the assembled post HTML
#   python3 seo/publish_tools.py publish [name ...]
import sys, os, json, re
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
TOOLS = os.path.join(os.path.dirname(os.path.abspath(__file__)), "tools")
IDS = os.path.join(TOOLS, "published.json")

POSTS = {
    "sip-calculator": ("SIP Calculator: Estimate Your Mutual Fund Returns (With Step-Up)", ["Personal Finance", "Investing", "Calculators"]),
    "emi-calculator": ("Home Loan EMI Calculator With Prepayment Savings", ["Personal Finance", "Calculators"]),
    "fire-calculator": ("FIRE Calculator: How Much You Need to Retire Early", ["Personal Finance", "Investing", "Calculators"]),
    "credit-card-payoff-calculator": ("Credit Card Payoff Calculator: How Long Will Your Debt Take?", ["Personal Finance", "Calculators"]),
    "hubs/money-start-here": ("Personal Finance for Beginners: A Step-by-Step Guide (Start Here)",
                              ["Personal Finance", "Investing", "Start Here"]),
    "hubs/career-start-here": ("Career Guide: Work Smarter, Avoid Burnout and Grow (Start Here)",
                               ["Career & Workplace", "Leadership", "Start Here"]),
    "hubs/mind-start-here": ("Mental Well-Being and Self-Improvement: A Practical Guide (Start Here)",
                             ["Self Improvement", "Psychology", "Health & Wellness", "Start Here"]),
}
POSTS.update({
    "articles/prepay-home-loan-or-invest": ("Prepay Home Loan or Invest? How to Decide (With Examples)", ["Personal Finance"]),
    "articles/emergency-fund-guide": ("How Much Emergency Fund Do You Need? (With Calculator)", ["Personal Finance"]),
    "articles/good-credit-score-cibil": ("What Is a Good CIBIL Score? How to Check and Improve It", ["Personal Finance"]),
    "articles/gdp-measuring-economy": ("What Is GDP? Its Role in Measuring an Economy, Explained", ["Economics"]),
    "articles/bitcoin-losing-its-hype": ("Is Bitcoin Losing Its Hype? What the Data Says in 2026", ["Cryptocurrency"]),
    "articles/retire-abroad-on-2000-a-month": ("Best Countries to Retire Abroad on $2,000 a Month: Costs, Visas and Taxes Compared", ["Countries & Living Abroad"]),
    "articles/life-insurance-cost-by-age-calculator": ("How Much Does Life Insurance Cost by Age? Term Cover Prices Compared (Calculator)", ["Personal Finance"]),
    "articles/debt-consolidation-loans-when-they-save": ("Debt Consolidation Loans: When They Save You Money and When They Don't (Calculator)", ["Personal Finance"]),
    "articles/ev-vs-petrol-running-costs-2026": ("Is It Cheaper to Charge an EV or Fill Up With Petrol Now? (Cost Calculator)", ["Personal Finance"]),
    "articles/inheritance-tax-by-country-2026": ("Inheritance Tax by Country 2026: Thresholds, Rates and Who Pays", ["Personal Finance"]),
    "articles/mortgage-rates-by-country-2026": ("Mortgage Rates by Country 2026: Where Buying a Home Costs the Most", ["Personal Finance"]),
    "articles/state-pension-amounts-by-country-2026": ("State Pension Amounts by Country 2026: How Much Retirees Actually Get", ["Personal Finance"]),
    "articles/income-tax-by-country-take-home-pay": ("Income Tax by Country 2026: How Much of Your Salary You Keep in 38 Countries", ["Personal Finance"]),
    "articles/electricity-prices-by-country-2026": ("Electricity Prices by Country 2026: What Households Pay per kWh, and What the Iran War Means for Bills", ["Economics"]),
    "articles/minimum-wage-by-country-2026": ("Minimum Wage by Country 2026: Hourly and Monthly Rates in US Dollars", ["Career & Workplace"]),
    "articles/retirement-age-by-country-2026": ("Retirement Age by Country 2026: State Pension Ages in 39 Countries", ["Personal Finance"]),
    "articles/petrol-prices-since-iran-war-by-country": ("Petrol Prices Since the Iran War: How Much They Have Risen in 35 Countries", ["Economics"]),
    "articles/deposit-insurance-limits-by-country-2026": ("Deposit Insurance Limits by Country 2026: How Much of Your Money Is Protected", ["Personal Finance"]),
    "articles/salary-increase-forecast-2027-by-country": ("Salary Increase Forecasts 2027 by Country: Every Major Survey in One Table", ["Career & Workplace"]),
    "articles/insurance-premium-up-no-claims": ("Why Did My Insurance Premium Go Up When I Never Made a Claim?", ["Personal Finance"]),
    "articles/online-accounts-crypto-when-you-die": ("What Happens to Your Online Accounts, Crypto and Money When You Die?", ["Technology & AI"]),
    "articles/lump-sum-or-monthly-pension": ("Lump Sum or Monthly Pension: Which Should You Take? (Break-Even Calculator)", ["Personal Finance"]),
    "articles/fair-pay-raise-2026-real-raise": ("What Is a Fair Pay Raise in 2026? How to Work Out Your Real Raise After Inflation", ["Career & Workplace"]),
    "articles/explain-layoff-career-gap-interview": ("How to Explain a Layoff or Career Gap in an Interview (Without Lying)", ["Career & Workplace"]),
    "articles/why-prices-still-high-inflation-down": ("Why Are Prices Still So High When Inflation Has Come Down?", ["Economics"]),
    "articles/retirement-savings-moving-abroad": ("What Happens to Your Retirement Savings When You Move Abroad?", ["Countries & Living Abroad"]),
    "articles/crypto-swap-tax-rules": ("Do You Pay Tax When You Swap One Crypto for Another? The Rules Explained", ["Cryptocurrency"]),
    "articles/bank-fintech-app-goes-bust": ("What Happens to Your Money If Your Bank or Fintech App Goes Bust?", ["Personal Finance"]),
    "articles/credit-score-different-every-app": ("Why Is My Credit Score Different on Every App? Which One Do Lenders Actually Use?", ["Personal Finance"]),
    "articles/gold-safe-haven-2026-fall": ("Is Gold Still a Safe Haven After Its Big 2026 Fall?", ["Investing"]),
    "articles/how-to-buy-ipo-shares": ("How Small Investors Can Buy IPO Shares: Lessons From the SpaceX Listing", ["Investing"]),
    "articles/afford-car-2026-calculator": ("How to Afford a Car in 2026 When the Average Payment Is $777 (Calculator)", ["Personal Finance"]),
    "articles/prediction-markets-kalshi-polymarket": ("How Prediction Markets Like Kalshi and Polymarket Work, and Are They Legal?", ["Economics"]),
    "articles/buy-house-mortgage-rates-above-7": ("Should You Buy a House With Mortgage Rates Above 7%? (Rent vs Buy Calculator)", ["Personal Finance"]),
    "articles/stablecoins-explained-interest": ("Stablecoins Explained: Can You Really Earn Interest on Digital Dollars?", ["Cryptocurrency"]),
    "articles/jobs-ai-is-replacing-skills": ("Jobs AI Is Replacing in 2026, and the Skills That Keep You Employed", ["Career & Workplace"]),
    "articles/10-year-treasury-yield-above-5": ("Why Is the 10-Year Treasury Yield Above 5%? What It Means for Your Money", ["Economics"]),
    "articles/where-to-keep-cash-rates-rise": ("Where to Keep Your Cash When Rates Rise: Savings Accounts, T-Bills and Money Market Funds", ["Personal Finance"]),
    "articles/interest-rates-rising-money-moves": ("Interest Rates Are Rising Again: 7 Money Moves to Make Now", ["Personal Finance"]),
})
# Everyday tools (no upload, no sign-up), published 2026-10.
TOOL_POSTS = {
    "articles/free-tools-no-upload-no-signup": ("Free Online Tools That Don't Upload Your Files, Make You Sign Up or Add Watermarks", ["Technology & AI", "Tools"]),
    "articles/qr-code-generator-never-expires": ("Free QR Code Generator That Never Expires (No Sign-Up, No Trial)", ["Technology & AI", "Tools"]),
    "articles/merge-split-pdf-without-uploading": ("Merge, Split and Reorder PDFs Free Without Uploading Them (No Daily Limit)", ["Technology & AI", "Tools"]),
    "articles/sign-pdf-free-no-account": ("How to Sign a PDF for Free Without an Account (No 3-Document Limit)", ["Technology & AI", "Tools"]),
    "articles/heic-to-jpg-remove-photo-location": ("Convert HEIC to JPG, Shrink Photos and Remove Location Data, Without Uploading", ["Technology & AI", "Tools"]),
    "articles/free-resume-builder-free-pdf-download": ("Free Resume Builder With a Free PDF Download (No Paywall at the End)", ["Career & Workplace", "Tools"]),
    "articles/free-invoice-generator-no-watermark": ("Free Invoice Generator: No Sign-Up, No Watermark, PDF in Seconds", ["Career & Workplace", "Tools"]),
    "articles/subscription-tracker-cancel-reminders": ("Subscription Tracker: Find Forgotten Subscriptions and Get Reminded Before Renewals", ["Personal Finance", "Tools"]),
    "articles/meeting-time-zone-planner": ("Meeting Time Zone Planner: Find a Time That Works in Every City", ["Career & Workplace", "Tools"]),
    "articles/password-passphrase-generator-breach-check": ("Strong Password and Passphrase Generator, Plus a Safe Breach Check", ["Technology & AI", "Tools"]),
    "articles/word-counter-character-limits": ("Word Counter With Character Limits for X, LinkedIn, SMS and SEO (Plus Text Clean-Up)", ["Career & Workplace", "Tools"]),
}
POSTS.update(TOOL_POSTS)
# Country money-facts pages and the compare-countries hub, generated by seo/countries/build_countries.py.
_CF = os.path.join(os.path.dirname(os.path.abspath(__file__)), "countries", "countries.json")
if os.path.exists(_CF):
    POSTS.update({k: tuple(v) for k, v in json.load(open(_CF, encoding="utf-8")).items()})
# Cover images (seo/covers/, drawn by make_covers.py), served from a fixed commit. The first image in a post is
# what Blogger uses for its share preview (og:image), and these posts have no image of their own.
COVER_BASE = "https://cdn.jsdelivr.net/gh/venkateshramu07-design/My_Repo@5343548c94530fa78e6c90aa9c9c4df230417087/seo/covers/"
COVERS = {"hubs/money-start-here", "hubs/mind-start-here", "hubs/career-start-here",
          "articles/emergency-fund-guide", "articles/good-credit-score-cibil", "articles/prepay-home-loan-or-invest",
          "articles/gdp-measuring-economy", "articles/bitcoin-losing-its-hype", "articles/interest-rates-rising-money-moves", "articles/where-to-keep-cash-rates-rise", "articles/10-year-treasury-yield-above-5", "articles/jobs-ai-is-replacing-skills", "articles/stablecoins-explained-interest", "articles/buy-house-mortgage-rates-above-7", "articles/prediction-markets-kalshi-polymarket", "articles/afford-car-2026-calculator", "articles/how-to-buy-ipo-shares", "articles/gold-safe-haven-2026-fall", "articles/credit-score-different-every-app", "articles/bank-fintech-app-goes-bust", "articles/crypto-swap-tax-rules", "articles/retirement-savings-moving-abroad", "articles/why-prices-still-high-inflation-down", "articles/explain-layoff-career-gap-interview", "articles/fair-pay-raise-2026-real-raise", "articles/lump-sum-or-monthly-pension", "articles/online-accounts-crypto-when-you-die", "articles/insurance-premium-up-no-claims", "articles/salary-increase-forecast-2027-by-country", "articles/deposit-insurance-limits-by-country-2026", "articles/petrol-prices-since-iran-war-by-country", "articles/retirement-age-by-country-2026", "articles/minimum-wage-by-country-2026", "articles/electricity-prices-by-country-2026", "articles/income-tax-by-country-take-home-pay", "articles/state-pension-amounts-by-country-2026", "articles/mortgage-rates-by-country-2026", "articles/inheritance-tax-by-country-2026", "articles/ev-vs-petrol-running-costs-2026", "articles/debt-consolidation-loans-when-they-save", "articles/life-insurance-cost-by-age-calculator", "articles/retire-abroad-on-2000-a-month"}
COUNTRY_COVER_BASE = "https://cdn.jsdelivr.net/gh/venkateshramu07-design/My_Repo@f454e428cfeba8f707641b3c3ce3ad0e603c3b37/seo/covers/"
COVERS |= {n for n in POSTS if n.startswith("countries/")}
COVERS |= set(TOOL_POSTS)
TOOL_COVER_BASE = "https://cdn.jsdelivr.net/gh/venkateshramu07-design/My_Repo@1ac2d86551d465d2cc58492b22f9e91a22ce6c8d/seo/covers/"


def cover_base(name):
    return COUNTRY_COVER_BASE if name.startswith("countries/") else TOOL_COVER_BASE if name in TOOL_POSTS else COVER_BASE

HUB_NAMES = {"money-start-here": "personal finance guide", "career-start-here": "career guide",
             "mind-start-here": "well-being guide"}


def load_ids():
    return json.load(open(IDS)) if os.path.exists(IDS) else {}


def expand(body):
    import re, html
    from apply import BACKUP, CALCULATORS
    from plan import PLAN
    ids = load_ids()

    def link(m):
        key = m.group(1)
        if key.startswith("tool:"):
            name, url = CALCULATORS[int(key[5:])]
            return f'<a href="{url}">{html.escape(name[0].upper() + name[1:])}</a>'
        if key.startswith("hub:"):
            return f'<a href="{ids["hubs/" + key[4:]]["url"]}">{HUB_NAMES[key[4:]]}</a>'
        if key.startswith("post:"):
            name, _, label = key[5:].partition("|")
            text = html.escape(label or POSTS[name][0])
            if name not in ids and (name.startswith("countries/") or name.startswith("articles/") and name in TOOL_POSTS):  # not created yet: plain text until the next run
                return text
            return f'<a href="{ids[name]["url"]}">{text}</a>'
        if key.startswith("embed:"):
            return open(os.path.join(TOOLS, "embeds", key[6:] + ".html"), encoding="utf-8").read()
        p = json.load(open(os.path.join(BACKUP, key.replace("/", "_") + ".json"), encoding="utf-8"))
        title = (PLAN.get(key) or (None,))[0] or p["title"].strip()
        return f'<a href="{p["url"]}">{html.escape(title)}</a>'
    return re.sub(r"\[\[([^\]]+)\]\]", link, body)


def build(name):
    import html
    from apply import escape_scripts, tool_assets
    body = expand(open(os.path.join(TOOLS, "..", name + ".html") if "/" in name
                       else os.path.join(TOOLS, name + ".html"), encoding="utf-8").read())
    body = body.replace("IMGBASE/", COVER_BASE.replace("/seo/covers/", "/seo/images/"))  # article illustrations
    if name in COVERS:  # after the opening "Updated" line
        c = cover_base(name) + name.split("/")[1]
        img = (f'<img src="{c}-800.webp" srcset="{c}-800.webp 800w, {c}-1200.webp 1200w" sizes="(max-width: 780px) 92vw, 680px" '
               f'alt="{html.escape(POSTS[name][0], quote=True)}" '
               'width="1200" height="630" fetchpriority="high" decoding="async" style="width:100%;height:auto;border-radius:16px"/>')
        body = body.replace("</p>", "</p>\n" + img, 1)
    raw = open(os.path.join(TOOLS, "..", name + ".html") if "/" in name else os.path.join(TOOLS, name + ".html"), encoding="utf-8").read()
    body = machine_readable(name, raw, body)
    return escape_scripts(f"{tool_assets()}\n<div class=\"seo-post\">\n{body}</div>")


def _text(h):
    import html as _h
    return re.sub(r"\s+", " ", _h.unescape(re.sub(r"<[^>]+>", " ", re.sub(r"\[\[[^\]]+\]\]", "", h)))).strip()


def machine_readable(name, raw, body):
    """Static structured data and a citation line, readable by crawlers that don't run JavaScript (most AI crawlers):
    FAQPage from the FAQ section, Dataset for data-table articles, and a 'Cite this page' box before Related Guides."""
    import html as _h
    ids = load_ids(); url = (ids.get(name) or {}).get("url")
    title = POSTS[name][0] if name in POSTS else ""
    org = {"@type": "Organization", "name": "Blog Digest", "url": "https://randomthoughtsever.blogspot.com/"}
    blocks = []
    m = re.search(r"<h2[^>]*>Frequently Asked Questions</h2>(.*?)(?=<h2)", body, re.S)
    if m:
        qa = re.findall(r"<h3[^>]*>(.*?)</h3>\s*<p>(.*?)</p>", m.group(1), re.S)
        if qa:
            blocks.append(('bd-faq-ld', {"@context": "https://schema.org", "@type": "FAQPage", "mainEntity": [
                {"@type": "Question", "name": _text(q), "acceptedAnswer": {"@type": "Answer", "text": _text(a)}} for q, a in qa]}))
    upd = re.search(r"Updated (\d{1,2} \w+ 20\d\d)", raw)
    quick = re.search(r"<strong>Quick answer:</strong>(.*?)</p>", raw, re.S)
    if url and re.search(r"\[\[embed:[a-z0-9-]*(table)\]\]", raw):
        srcs = re.findall(r'<ul class="bd-sources">(.*?)</ul>', body, re.S)
        cites = re.findall(r'href="([^"]+)"', srcs[-1]) if srcs else []
        ds = {"@context": "https://schema.org", "@type": "Dataset", "name": title, "url": url,
              "description": (_text(quick.group(1)) if quick else title)[:4900], "creator": org, "publisher": org,
              "isAccessibleForFree": True, "spatialCoverage": "Worldwide", "temporalCoverage": "2026",
              "keywords": [w for w in re.split(r"[:,?]| and ", title) if w.strip()][:6]}
        if upd: ds["dateModified"] = __import__("datetime").datetime.strptime(upd.group(1), "%d %B %Y").date().isoformat()
        if cites: ds["citation"] = cites[:30]
        blocks.append(("bd-ds-ld", ds))
    ld = "".join('<script type="application/ld+json" id="%s">%s</script>\n' % (i, json.dumps(d, ensure_ascii=False)) for i, d in blocks)
    if url and "<h2>Related Guides</h2>" in body:
        when = upd.group(1) if upd else "2026"
        cite = ('<div class="bd-cite" style="margin:1.6em 0;padding:14px 16px;border-radius:14px;border:1px solid rgba(120,120,140,.25);font:500 14px/1.55 system-ui,sans-serif">'
                '<b>Cite this page:</b> Blog Digest, &ldquo;%s&rdquo;, updated %s, %s</div>\n' % (_h.escape(title), when, url))
        body = body.replace("<h2>Related Guides</h2>", cite + "<h2>Related Guides</h2>", 1)
    return body + ("\n" + ld if ld else "")


def publish(names):
    import apply as a
    bid = a.blog_id()
    ids = load_ids()
    for name in names:
        title, labels = POSTS[name]
        body = {"title": title, "labels": labels, "content": build(name)}
        pid = ids[name]["id"] if isinstance(ids.get(name), dict) else ids.get(name)
        if pid:
            old = a.call("GET", f"/blogs/{bid}/posts/{pid}")
            body["published"] = old["published"]
            r = a.call("PATCH", f"/blogs/{bid}/posts/{pid}", {"fetchBody": "true"}, body)
        else:
            r = a.call("POST", f"/blogs/{bid}/posts/", None, body)
        ids[name] = {"id": r["id"], "url": r["url"]}
        json.dump(ids, open(IDS, "w"), indent=1)
        ok = len(r.get("content", "")) > 5000 and not re.search(r"\[\[(?:post|embed|tool|hub|\d{4}/)", r["content"])
        print("OK " if ok else "CHECK", name, "|", r["title"], "|", r["url"])


if __name__ == "__main__":
    if sys.argv[1] == "build":
        print(build(sys.argv[2]))
    else:
        publish(sys.argv[2:] or list(POSTS))
