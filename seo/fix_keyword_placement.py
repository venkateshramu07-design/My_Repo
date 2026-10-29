# Put each post's focus keyword (focus_keywords.json) where search engines and SEO checkers look for it, only where
# it is missing: the opening "Quick answer" and the FAQ heading. Edits the source files; safe to run again.
#   python3 seo/fix_keyword_placement.py
import glob, html, json, os, re
from source_map import source_map
HERE = os.path.dirname(os.path.abspath(__file__))
FK = json.load(open(os.path.join(HERE, "focus_keywords.json"), encoding="utf-8"))
STOP = set("a an the to of in for on and or is are how what why do does can i you my your with vs it its be".split())
UP = {"emi", "gst", "fd", "rd", "ppf", "epf", "sip", "ai", "mba", "ceo", "coo", "cfo", "fire", "iq", "upi", "ssc", "upsc", "uk",
      "us", "usa", "eu", "pdf", "heic", "jpg", "cibil", "fico", "ev", "ipo", "spacex", "dv", "gdp", "nasdaq", "s&p", "oci", "pan", "rbi", "t-bills", "401k", "ira"}
PROPER = {"canadian", "australian", "british", "chinese", "japanese", "german", "indian", "usa", "uk", "us", "germany", "france", "spain", "italy", "mexico", "brazil", "ireland", "netherlands", "sweden", "norway", "denmark", "poland", "portugal", "greece", "belgium", "austria", "switzerland", "luxembourg", "korea", "africa", "estonia", "latvia", "lithuania", "slovakia", "slovenia", "czechia", "monday", "christmas", "diwali", "english", "french", "hindi", "eckhart", "sharia", "islam", "hindu", "hinduism", "buddha", "elon", "musk", "trump", "kalam", "gandhi", "einstein", "nikola", "tesla", "dubai", "uae", "america", "american", "europe", "european", "asia", "london", "paris", "eckhart", "tolle", "nassim", "taleb", "chris", "gardner", "india", "japan", "zen", "buddhism", "finland", "singapore", "australia", "canada", "kalshi", "polymarket", "aadhaar", "schengen",
          "china", "google", "bitcoin", "joe", "biden", "oxford", "credit", "karma"} - {"credit", "karma"}
def words(k): return [w for w in re.findall(r"[a-z0-9]+", k.lower()) if w not in STOP]
def has(t, k): t = t.replace("401(k)", "401k"); return all(w in t for w in words(k))
QSTART = {"what", "why", "is", "are", "can", "do", "does", "when", "which", "should", "will", "who", "where"}
HOWQ = {"to", "do", "does", "did", "is", "are", "can", "much", "many", "long", "often", "should", "much"}
def is_question(k):
    w = k.split()
    return w[0] in QSTART or (w[0] == "how" and len(w) > 1 and w[1] in HOWQ)
def cased(k, title_text):
    """The keyword with natural capitals: copied from the post's own title where the phrase appears there,
    otherwise lower case with acronyms and proper nouns fixed."""
    m = re.search(r"\b" + r"\W+".join(re.escape(w) for w in k.split()) + r"\b", title_text, re.I)
    if m and not m.group(0).isupper():
        t = m.group(0)
        # titles are in Title Case; keep capitals only where they are acronyms or proper nouns
        out = []
        for w in re.findall(r"\S+", t):
            lw = re.sub(r"[^a-z0-9&()]", "", w.lower())
            out.append(w.lower() if (len(w) == 1 and w != "I") else w if (lw in UP or lw in PROPER or w.isupper()) else w.lower())
        return fix_phrases(" ".join(out))
    return fix_phrases(" ".join(x.upper() if x in UP else (x[:1].upper() + x[1:] if x in PROPER else x) for x in k.split()))
PHRASES = ["new zealand", "south korea", "south africa", "united states", "united kingdom", "new year", "power of now", "pursuit of happiness", "fooled by randomness", "s&p 500", "nasdaq 100"]
def fix_phrases(t):
    for ph in PHRASES:
        t = re.sub(r"\b%s\b" % re.escape(ph), lambda m: " ".join(x if x.isupper() or x[:1].isdigit() else x.capitalize() for x in m.group(0).split()) if ph not in ("new year",) else m.group(0), t, flags=re.I)
    return t
SPECIAL = {"spacex": "SpaceX", "401k": "401(k)", "401(k)": "401(k)", "fdic": "FDIC", "s&p": "S&P", "iphone": "iPhone", "youtube": "YouTube", "linkedin": "LinkedIn", "chatgpt": "ChatGPT", "paypal": "PayPal"}
def special(t): return " ".join(SPECIAL.get(w.lower(), w) for w in t.split())
QFIX = {"how to be happy": "How can you be happy?", "why companies manufacture in china": "Why do companies manufacture in China?", "why politics is important": "Why is politics important?",
        "why is dubai called a fake city": "Why is Dubai called a fake city?", "how to explain gap in employment": "How do you explain a gap in employment?",
        "what happens to my 401k if i move abroad": "What happens to my 401(k) if I move abroad?"}
def question(k, kc):
    if k in QFIX: return QFIX[k]
    q = kc
    if q.lower().startswith("how to "): q = "how do you " + q[7:]
    q = re.sub(r"\bi\b", "I", q)
    return first_up(q) + "?"
def first_up(t): return t[:1].upper() + t[1:]
def title_case(t):
    small = {"a", "an", "the", "to", "of", "in", "for", "on", "and", "or", "vs", "with", "at", "by"}
    return " ".join(w.lower() if (i and w.lower() in small) else w if w.isupper() else w[:1].upper() + w[1:] for i, w in enumerate(t.split()))
M = source_map(); changed = []
for f in glob.glob(os.path.join(HERE, ".live", "[!_]*.json")):
    p = json.load(open(f)); path = re.sub(r"^https?://[^/]+", "", p["url"]); k = FK.get(path)
    if not k or path not in M: continue
    c = p["content"]; text = re.sub(r"\s+", " ", html.unescape(re.sub(r"<[^>]+>", " ", re.sub(r"(?is)<(script|style|svg)[^>]*>.*?</\1>", " ", c)))).lower()
    h2 = [re.sub(r"<[^>]+>", "", x).lower() for x in re.findall(r"(?is)<h2[^>]*>(.*?)</h2>", c)]
    src = os.path.join(HERE, M[path]); s = open(src, encoding="utf-8").read(); s0 = s
    if not has(" ".join(text.split()[:120]), k) and "<strong>Quick answer:</strong>" in s:
        kc = special(cased(k, p.get("title", "")))
        rep = ("<strong>Quick answer:</strong> %s" % question(k, kc)) if is_question(k) else "<strong>Quick answer on %s:</strong>" % re.sub(r"\s+explained$", "", kc)
        s = s.replace("<strong>Quick answer:</strong>", rep, 1)
    if not any(has(h, k) for h in h2):
        s = re.sub(r"<h2>Frequently Asked Questions</h2>", "<h2>Frequently Asked Questions: %s</h2>" % html.escape(special(title_case(cased(k, p.get("title", ""))))), s, count=1)
    if s != s0:
        open(src, "w", encoding="utf-8").write(s); changed.append((path, M[path]))
json.dump(changed, open(os.path.join(HERE, ".live", "_changed_keyword.json"), "w"))
print("changed", len(changed), "source files")
