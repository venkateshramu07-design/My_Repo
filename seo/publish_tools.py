# Publish the calculator posts in seo/tools/. New posts are created once; later runs update them in place.
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
}


def build(name):
    from apply import escape_scripts, tool_assets
    body = open(os.path.join(TOOLS, name + ".html"), encoding="utf-8").read()
    return escape_scripts(f"{tool_assets()}\n<div class=\"seo-post\">\n{body}</div>")


def publish(names):
    import apply as a
    bid = a.blog_id()
    ids = json.load(open(IDS)) if os.path.exists(IDS) else {}
    for name in names:
        title, labels = POSTS[name]
        body = {"title": title, "labels": labels, "content": build(name)}
        if name in ids:
            old = a.call("GET", f"/blogs/{bid}/posts/{ids[name]}")
            body["published"] = old["published"]
            r = a.call("PATCH", f"/blogs/{bid}/posts/{ids[name]}", {"fetchBody": "true"}, body)
        else:
            r = a.call("POST", f"/blogs/{bid}/posts/", None, body)
            ids[name] = r["id"]
            json.dump(ids, open(IDS, "w"), indent=1)
        ok = "<script>" in r.get("content", "") and len(r.get("content", "")) > 5000
        print("OK " if ok else "CHECK", name, "|", r["title"], "|", r["url"])


if __name__ == "__main__":
    if sys.argv[1] == "build":
        print(build(sys.argv[2]))
    else:
        publish(sys.argv[2:] or list(POSTS))
