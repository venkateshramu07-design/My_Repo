# Map each live post path to the source file it is built from (rewrites/, articles/, countries/, hubs/, tools/).
import json, os, re
HERE = os.path.dirname(os.path.abspath(__file__))
def source_map():
    m = {}
    for f in os.listdir(os.path.join(HERE, "rewrites")):
        if f.endswith(".html"):
            y, mo, slug = f[:-5].split("_", 2); m["/%s/%s/%s.html" % (y, mo, slug)] = "rewrites/" + f
    ids = json.load(open(os.path.join(HERE, "tools", "published.json"), encoding="utf-8"))
    for name, v in ids.items():
        url = v.get("url") if isinstance(v, dict) else None
        if not url: continue
        for cand in (name + ".html", "tools/" + name + ".html"):
            if os.path.exists(os.path.join(HERE, cand)):
                m[re.sub(r"^https?://[^/]+", "", url)] = cand; break
    for path, f in (("/2026/10/sip-calculator-estimate-your-mutual.html", "tools/sip-calculator.html"),
                    ("/2026/10/fire-calculator-how-much-you-need-to.html", "tools/fire-calculator.html"),
                    ("/2026/10/credit-card-payoff-calculator-how-long.html", "tools/credit-card-payoff-calculator.html")):
        m[path] = f
    return m
if __name__ == "__main__":
    import glob
    M = source_map(); live = [json.load(open(f))["url"] for f in glob.glob(os.path.join(HERE, ".live", "*.json"))]
    paths = [re.sub(r"^https?://[^/]+", "", u) for u in live]
    miss = [p for p in paths if p not in M]
    print("mapped", len(paths) - len(miss), "of", len(paths)); print("\n".join(miss))
