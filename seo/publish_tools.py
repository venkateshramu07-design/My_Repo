# Publish the calculator posts in seo/tools/ and the topic hubs in seo/hubs/.
# New posts are created once; later runs update them in place.
# In hub pages, [[year/month/slug]] becomes a link with the post's current title, [[tool:N]] a link to
# CALCULATORS[N] in apply.py, [[hub:name]] a link to another published hub, [[post:name]] a link to a post
# published from here, and [[embed:name]] inserts the widget seo/tools/embeds/<name>.html.
#   python3 seo/publish_tools.py build <name>     # print the assembled post HTML
#   python3 seo/publish_tools.py publish [name ...]
import sys, os, json
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
# Cover images (seo/covers/, drawn by make_covers.py), served from a fixed commit. The first image in a post is
# what Blogger uses for its share preview (og:image), and these posts have no image of their own.
COVER_BASE = "https://cdn.jsdelivr.net/gh/venkateshramu07-design/My_Repo@b1945fde66acaac91b8910ba0627498c06efa458/seo/covers/"
COVERS = {"hubs/money-start-here", "hubs/mind-start-here", "hubs/career-start-here",
          "articles/emergency-fund-guide", "articles/good-credit-score-cibil", "articles/prepay-home-loan-or-invest",
          "articles/gdp-measuring-economy", "articles/bitcoin-losing-its-hype", "articles/interest-rates-rising-money-moves"}
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
            return f'<a href="{ids[key[5:]]["url"]}">{html.escape(POSTS[key[5:]][0])}</a>'
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
    if name in COVERS:  # after the opening "Updated" line
        img = (f'<img src="{COVER_BASE}{name.split("/")[1]}.jpg" alt="{html.escape(POSTS[name][0], quote=True)}" '
               'width="1200" height="630" style="width:100%;height:auto;border-radius:16px"/>')
        body = body.replace("</p>", "</p>\n" + img, 1)
    return escape_scripts(f"{tool_assets()}\n<div class=\"seo-post\">\n{body}</div>")


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
        ok = len(r.get("content", "")) > 5000 and "[[" not in r["content"]
        print("OK " if ok else "CHECK", name, "|", r["title"], "|", r["url"])


if __name__ == "__main__":
    if sys.argv[1] == "build":
        print(build(sys.argv[2]))
    else:
        publish(sys.argv[2:] or list(POSTS))
