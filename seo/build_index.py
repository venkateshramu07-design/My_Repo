# Write the repository README: a public, crawlable list of every Blog Digest article (from seo/.live, refreshed by
# fetch_live.py), grouped by topic, so search engines that crawl GitHub can discover every post.
#   python3 seo/build_index.py
import collections, glob, json, os, re
HERE = os.path.dirname(os.path.abspath(__file__))
sizes = {json.load(open(os.path.join(HERE, "tools", "published.json")))[n]["url"] for n in json.load(open(os.path.join(HERE, "sizes", "pages.json")))}
G = collections.OrderedDict((k, []) for k in ["Free tools: photo and PDF size", "Free tools and calculators", "Money and investing", "Countries and living abroad",
                                              "Career and work", "Mind, well-being and relationships", "Technology and science", "More articles"])
RULES = [("Countries and living abroad", r"in-numbers-2026|compare-countries|passport|visa|abroad|retire-abroad|cash-can-i-carry|codeine|country|countries|oci-photo"),
         ("Free tools and calculators", r"calculator|generator|builder|converter|maker|resizer|counter|planner|tracker|merge-split|sign-pdf|mask-your|heic|free-tools"),
         ("Money and investing", r"money|tax|loan|emi|sip|fund|invest|stock|gold|bitcoin|crypto|stablecoin|inflation|gdp|rate|bank|debt|credit|cibil|insurance|pension|401k|annuit|fire|financ|rich|ipo|dollar|currency|forex|petrodollar|hedging|recession|economy|econom|wage|salary|mortgage|house|electricity|petrol|deposit|treasury|fed |federal reserve|inflation|interest|portfolio|nasdaq|s&p|altcoin|401|scam|groceries|prices|rich|car can i afford"),
         ("Career and work", r"career|job|boss|work|mba|interview|layoff|leader|ceo|company|companies|gig|fashion|data-science|team leader|leadership|corporate|career break|time management|criticism|focus"),
         ("Technology and science", r"ai-|artificial|data-structures|neural|google|space|warm-hole|wormhole|digital|technology|online|internet|hacking|password|time zones"),
         ("Mind, well-being and relationships", r"happy|happi|love|mind|zen|calm|stress|emotion|sleep|habit|confiden|shy|success|positiv|fate|karma|truth|innocence|thought|ikigai|grief|resilien|anxiety|lonel|relationship|parent|friend|morning|consisten|self|quote|iq|intelligence|psycholog|courage|rejection|letting-go|meditation|mental|uncertainty|ignored|socialize|someone|relationship|lifestyle|diabetes|present moment|life lessons|kokorozashi|power of now|resolution|children|learn anything|compassion|alpha male|fooled by randomness|smarter")]
for f in sorted(glob.glob(os.path.join(HERE, ".live", "[!_]*.json"))):
    p = json.load(open(f)); u = p["url"]; slug = u.rsplit("/", 1)[1]
    if re.search(r"/(about-us|contact-us|privacy-policy|disclaimer|terms-and-conditions|sitemap)\.html$", u): continue
    g = "Free tools: photo and PDF size" if u in sizes else next((name for name, rx in RULES if re.search(rx, (slug + " " + p["title"]).lower())), "More articles")
    G[g].append((p["title"].strip(), u))
out = ["# Blog Digest", "", "Source and publishing scripts for **[Blog Digest](https://randomthoughtsever.blogspot.com/)**: practical, sourced guides to money, work, well-being and living abroad, plus free tools that run in your browser without uploading your files.", "",
       "Start here: [Free Tools](https://randomthoughtsever.blogspot.com/p/free-tools.html) · [Free Calculators](https://randomthoughtsever.blogspot.com/p/free-calculators.html) · [Reduce image or PDF size in KB](https://randomthoughtsever.blogspot.com/2026/10/reduce-image-size-in-kb-online-free.html)", ""]
for g, items in G.items():
    if not items: continue
    out += ["## %s (%d)" % (g, len(items)), ""] + ["- [%s](%s)" % (t.replace("[", "(").replace("]", ")"), u) for t, u in sorted(items)] + [""]
open(os.path.join(HERE, "..", "README.md"), "w", encoding="utf-8").write("\n".join(out))
print({g: len(v) for g, v in G.items()}, sum(len(v) for v in G.values()))
