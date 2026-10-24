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
open(os.path.join(HERE, "bd-redesign.js"), "w", encoding="utf-8").write(out)
print("wrote bd-redesign.js", len(out), "bytes")
