# Build bd-redesign.js from redesign.xml, for sites that load the layer with one <script src> line
# (Layout > Add a Gadget > HTML/JavaScript) instead of pasting it into the theme.
#   python3 seo/theme/build_loader.py
import json, os, re
HERE = os.path.dirname(os.path.abspath(__file__))
src = open(os.path.join(HERE, "redesign.xml"), encoding="utf-8").read()
fonts = re.search(r"<link href='(https://fonts\.googleapis\.com/css2[^']+)'", src).group(1).replace("&amp;", "&")
css = re.search(r"<style id='bd-redesign-v2'>/\*<!\[CDATA\[\*/(.*?)/\*\]\]>\*/</style>", src, re.S).group(1)
js = re.search(r"<script>//<!\[CDATA\[\n(.*?)//\]\]></script>", src, re.S).group(1)
# Article summaries from descriptions.json, so a new description goes live with the next release, not a theme upload.
descs = json.load(open(os.path.join(HERE, "descriptions.json"), encoding="utf-8"))
assert js.count("var BD_DESCS = {};") == 1
js = js.replace("var BD_DESCS = {};", "var BD_DESCS = " + json.dumps(descs, ensure_ascii=True, separators=(",", ":")) + ";")
# Covers for articles whose cover image is hosted on jsDelivr: post path -> cover base URL.
import sys, re as _re
sys.path.insert(0, os.path.join(HERE, ".."))
import publish_tools as _pt
_ids = json.load(open(os.path.join(HERE, "..", "tools", "published.json"), encoding="utf-8"))
_cb = lambda n: _pt.cover_base(n) + n.split("/")[1]
covers = {_re.sub(r"^https?://[^/]+", "", _ids[n]["url"]): _cb(n) for n in _pt.COVERS if n in _ids}
covers.update({"t:" + _pt.POSTS[n][0]: _cb(n) for n in _pt.COVERS if n in _ids and n in _pt.POSTS})
assert js.count("var BD_COVERS = {};") == 1
js = js.replace("var BD_COVERS = {};", "var BD_COVERS = " + json.dumps(covers, separators=(",", ":")) + ";")
_dat = json.load(open(os.path.join(HERE, "..", "data", "index.json"), encoding="utf-8"))
dmap = {_re.sub(r"^https?://[^/]+", "", d["url"]): d["csv"] for d in _dat}
assert js.count("var BD_DATA = {};") == 1
js = js.replace("var BD_DATA = {};", "var BD_DATA = " + json.dumps(dmap, separators=(",", ":")) + ";")
_trend = json.load(open(os.path.join(HERE, "trending.json"), encoding="utf-8"))
assert js.count("var BD_TRENDING = {};") == 1
js = js.replace("var BD_TRENDING = {};", "var BD_TRENDING = " + json.dumps(_trend, separators=(",", ":"), ensure_ascii=False) + ";")
_cty = {_pt.POSTS[n][0].split(" in Numbers")[0].replace("The ", ""): _re.sub(r"^https?://[^/]+", "", _ids[n]["url"]) for n in _ids if n.startswith("countries/") and "in Numbers" in _pt.POSTS.get(n, ("",))[0]}
if "countries/compare-countries-money-facts-2026" in _ids: _cty[" hub"] = _re.sub(r"^https?://[^/]+", "", _ids["countries/compare-countries-money-facts-2026"]["url"])
_tk = {"hub": "free-tools-no-upload-no-signup", "qr": "qr-code-generator-never-expires", "pdf": "merge-split-pdf-without-uploading", "sign": "sign-pdf-free-no-account",
       "photo": "heic-to-jpg-remove-photo-location", "resume": "free-resume-builder-free-pdf-download", "invoice": "free-invoice-generator-no-watermark",
       "subs": "subscription-tracker-cancel-reminders", "tz": "meeting-time-zone-planner", "pw": "password-passphrase-generator-breach-check", "words": "word-counter-character-limits"}
_tools = {k: _re.sub(r"^https?://[^/]+", "", _ids["articles/" + v]["url"]) for k, v in _tk.items() if "articles/" + v in _ids}
if "pages/free-tools" in _ids: _tools["page"] = _re.sub(r"^https?://[^/]+", "", _ids["pages/free-tools"]["url"])
# One list of every tool and calculator, for the homepage tool finder, article tool cards and app schema.
_all = []
for _sec in json.load(open(os.path.join(HERE, "..", "pages", "free-tools.json"), encoding="utf-8"))["sections"]:
    for _it in _sec["items"]: _all.append([_it["title"], _it["desc"], _it["href"], "t", _sec["name"]])
for _sec in json.load(open(os.path.join(HERE, "..", "pages", "free-calculators.json"), encoding="utf-8")):
    for _it in _sec["items"]:
        if not _it.get("retired"): _all.append([_it["title"], _it["desc"], _it["href"], "d" if _sec["name"].startswith("Data") else "c", _sec["name"]])
assert js.count("var BD_ALLTOOLS = [];") == 1
js = js.replace("var BD_ALLTOOLS = [];", "var BD_ALLTOOLS = " + json.dumps(_all, separators=(",", ":"), ensure_ascii=False) + ";")
assert js.count("var BD_TOOLS = {};") == 1
js = js.replace("var BD_TOOLS = {};", "var BD_TOOLS = " + json.dumps(_tools, separators=(",", ":")) + ";")
assert js.count("var BD_COUNTRIES = {};") == 1
js = js.replace("var BD_COUNTRIES = {};", "var BD_COUNTRIES = " + json.dumps(_cty, separators=(",", ":"), ensure_ascii=False) + ";")
links = json.load(open(os.path.join(HERE, "autolinks.json"), encoding="utf-8"))
assert js.count("var BD_LINKS = {};") == 1
js = js.replace("var BD_LINKS = {};", "var BD_LINKS = " + json.dumps(links, ensure_ascii=True, separators=(",", ":")) + ";")
out = f"""/* Blog Digest redesign, built from seo/theme/redesign.xml by build_loader.py. Do not edit by hand. */
(function () {{
  var me = document.currentScript, box = me && me.closest('.widget');
  window.bdBase = me && me.src ? me.src.replace(/bd-redesign\\.js.*$/, '') : '';
  if (box) box.style.display = 'none';
  if (window.bdRedesign) return;
  window.bdRedesign = true;
  var f = document.createElement('link'); f.rel = 'stylesheet'; f.href = {json.dumps(fonts)};
  var s = document.createElement('style'); s.id = 'bd-redesign-v2'; s.textContent = {json.dumps(css, ensure_ascii=True)};
  // The first version of this layer, if still in the theme, already loads Fraunces; reuse it instead of a second copy.
  if (!document.querySelector('link[href*="family=Fraunces:opsz"]')) document.head.appendChild(f);
  document.head.appendChild(s);
  {js}}})();"""
path = os.path.join(HERE, "bd-redesign.js")
open(path, "w", encoding="utf-8").write(out)
# Minify when terser is available (TERSER=/path/to/terser); the readable source stays in redesign.xml.
import shutil, subprocess
terser = os.environ.get("TERSER") or shutil.which("terser")
if terser:
    subprocess.run([terser, path, "-c", "-m", "--comments", "/Do not edit/", "-o", path], check=True)
    out = open(path, encoding="utf-8").read()
print("wrote bd-redesign.js", len(out), "bytes")
