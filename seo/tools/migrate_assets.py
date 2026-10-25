# Move every public asset (design layer, covers, illustrations, data, widget) to a separate, neutrally named
# repository, so no page, image link or download shows this repository or its owner, and this repository can then
# be made private.
#   python3 seo/tools/migrate_assets.py ORG/REPO /path/to/clone-of-ORG-REPO
# Then: commit and tag v1 in the asset clone and push it; commit here; republish every post (publish_tools.py publish,
# publish_rewrites.py, countries/publish_countries.py, tools/publish_tool_posts.py, publish_pages.py); paste
# seo/theme/loader-snippet.html into the theme in place of the old loader; make this repository private.
import os, re, shutil, sys

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
OLD = re.compile(r"(cdn\.jsdelivr\.net/gh/)venkateshramu07-design/My_Repo@[^/\"' )]+/seo/")
OLD_RAW = "raw.githubusercontent.com/venkateshramu07-design/My_Repo/ccr-ee047f91-rbyv4x/seo/"
ASSETS = ["seo/covers", "seo/images", "seo/data", "seo/widgets", "seo/theme/bd-redesign.js", "seo/theme/descriptions.json",
          "seo/theme/follow.json", "seo/theme/version.txt"]


def main(new, clone):
    tag = "v1"
    for a in ASSETS:  # same layout under the asset repo, without the seo/ prefix
        src, dst = os.path.join(ROOT, a), os.path.join(clone, a[4:])
        if os.path.isdir(src): shutil.copytree(src, dst, dirs_exist_ok=True, ignore=shutil.ignore_patterns("*.py", "*.js.map"))
        elif os.path.exists(src): os.makedirs(os.path.dirname(dst), exist_ok=True); shutil.copy2(src, dst)
    changed = 0
    for d, _, files in os.walk(os.path.join(ROOT, "seo")):
        for f in files:
            if not f.endswith((".py", ".html", ".xml", ".js", ".json", ".md", ".txt")): continue
            p = os.path.join(d, f); s = open(p, encoding="utf-8").read()
            t = OLD.sub(r"\g<1>" + new + "@" + tag + "/", s).replace(OLD_RAW, "raw.githubusercontent.com/" + new + "/main/")
            t = t.replace("venkateshramu07-design/My_Repo", new)
            if t != s: open(p, "w", encoding="utf-8").write(t); changed += 1
    snippet = """<script>//<![CDATA[
(function () {
  var a = 'https://cdn.jsdelivr.net/gh/""" + new + """@', ver = 'v1', c = null, now = Date.now();
  try { c = JSON.parse(localStorage.getItem('bd-ver3') || 'null'); } catch (e) {}
  if (c && now - c.t < 6e5) ver = c.v;
  else try {
    var x = new XMLHttpRequest(); x.open('GET', 'https://raw.githubusercontent.com/""" + new + """/main/theme/version.txt', false); x.send();
    var t = (x.responseText || '').trim();
    if (x.status === 200 && /^[0-9a-f]{40}$/.test(t)) { ver = t; localStorage.setItem('bd-ver3', JSON.stringify({ v: t, t: now })); } else if (c) ver = c.v;
  } catch (e) { if (c) ver = c.v; }
  document.write('<script src="' + a + ver + '/theme/bd-redesign.js" crossorigin="anonymous"><\\/script>');
})();
//]]></script>
"""
    open(os.path.join(ROOT, "seo", "theme", "loader-snippet.html"), "w", encoding="utf-8").write(snippet)
    print("copied assets to", clone, "and rewrote", changed, "files to", new + "@" + tag)


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
