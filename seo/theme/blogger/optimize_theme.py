# Apply the Blog Digest head clean-up to a downloaded Blogger theme (Theme > Backup > Download) and write a new
# file to upload with Theme > Restore. Every change must match exactly once, or nothing is written.
#   python3 seo/theme/blogger/optimize_theme.py <theme.xml> <out.xml>
import re, sys

src = open(sys.argv[1], encoding="utf-8").read()
import os
# The loader's fallback, used only if GitHub can't be reached on a first visit: the current release.
CURRENT = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "version.txt")).read().strip()
s = src

HEAD = """<!-- Blog Digest: the design layer loads first, so pages render in the final design without the old theme flashing. -->
    <link crossorigin='anonymous' href='https://cdn.jsdelivr.net' rel='preconnect'/>
    <link href='https://fonts.googleapis.com' rel='preconnect'/>
    <link crossorigin='anonymous' href='https://fonts.gstatic.com' rel='preconnect'/>
    <script>//<![CDATA[
(function () {
  var repo = 'venkateshramu07-design/My_Repo', ver = '3e8cd196ed9f95a7b0791e1aa3818ee65eae6967', c = null, now = Date.now();
  try { c = JSON.parse(localStorage.getItem('bd-ver2') || 'null'); } catch (e) {}
  if (c && now - c.t < 6e5) ver = c.v;
  else {
    try {
      var x = new XMLHttpRequest();
      x.open('GET', 'https://raw.githubusercontent.com/' + repo + '/ccr-ee047f91-rbyv4x/seo/theme/version.txt', false);
      x.send();
      var t = (x.responseText || '').trim();
      if (x.status === 200 && /^[0-9a-f]{40}$/.test(t)) { ver = t; localStorage.setItem('bd-ver2', JSON.stringify({ v: t, t: now })); }
      else if (c) ver = c.v;
    } catch (e) { if (c) ver = c.v; }
  }
  document.write('<script src="https://cdn.jsdelivr.net/gh/' + repo + '@' + ver + '/seo/theme/bd-redesign.js"><\\/script>');
})();
//]]></script>""".replace("3e8cd196ed9f95a7b0791e1aa3818ee65eae6967", CURRENT)

# Blogger's all-head-content include writes og:description and the meta description from each post's search
# description, which on this blog are still old comma-separated keyword lists, and the API can't change them. So the
# theme writes those tags itself: every article gets its one-sentence summary from descriptions.json, and anything
# else falls back to the post's own search description or the site sentence. The rest of what the include wrote
# (canonical, feeds, favicon, og:url, og:title, og:image) is written out here too.
import html, json
DESCS = json.load(open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "descriptions.json"), encoding="utf-8"))
BLOG = "https://randomthoughtsever.blogspot.com"
SITE = "Clear, in-depth guides to money, economics, careers, psychology and well-being, for readers everywhere."
HOME_IMAGE = "https://cdn.jsdelivr.net/gh/venkateshramu07-design/My_Repo@e85c61307eba213563a3c2f5fec931a8b9a15cd3/seo/covers/home.jpg"
attr = lambda t: html.escape(t, quote=True).replace("&#x27;", "&#39;")
def desc_tags(text):
    return "<meta content='%s' name='description'/><meta content='%s' property='og:description'/>" % (attr(text), attr(text))
# Blogger compares against its internal address, which is http:// (it rewrites links to https:// only on output),
# so each article is listed under both forms.
CASES = "\n".join("      <b:case value='%s'/>%s" % (attr(scheme + BLOG[len("https"):] + path), desc_tags(text))
                   for path, text in sorted(DESCS.items()) for scheme in ("http", "https"))
META = """<meta content='text/html; charset=UTF-8' http-equiv='Content-Type'/>
    <meta content='blogger' name='generator'/>
    <link expr:href='data:blog.homepageUrl + &quot;favicon.ico&quot;' rel='icon' type='image/x-icon'/>
    <link expr:href='data:blog.canonicalUrl' rel='canonical'/>
    <link expr:href='data:blog.homepageUrl + &quot;feeds/posts/default&quot;' rel='alternate' title='Blog Digest - Atom' type='application/atom+xml'/>
    <link expr:href='data:blog.homepageUrl + &quot;feeds/posts/default?alt=rss&quot;' rel='alternate' title='Blog Digest - RSS' type='application/rss+xml'/>
    <meta expr:content='data:blog.canonicalUrl' property='og:url'/>
    <b:if cond='data:view.isHomepage'><meta content='Blog Digest: Practical Guides to Money, Mind and Work' property='og:title'/><b:elseif cond='data:view.isLabelSearch'/><meta expr:content='data:view.search.label + &quot; | Blog Digest&quot;' property='og:title'/><b:elseif cond='data:view.isSingleItem'/><meta expr:content='data:view.title' property='og:title'/><b:else/><meta content='Blog Digest' property='og:title'/></b:if>
    <b:if cond='data:view.isSingleItem and data:view.featuredImage'><meta expr:content='resizeImage(data:view.featuredImage, 1200, &quot;1200:630&quot;)' property='og:image'/><b:else/><meta content='""" + HOME_IMAGE + """' property='og:image'/></b:if>
    <b:switch var='data:blog.canonicalUrl'>
""" + CASES + """
      <b:default/><b:if cond='data:view.isPost and data:view.description'><meta expr:content='data:view.description' name='description'/><meta expr:content='data:view.description' property='og:description'/><b:else/>""" + desc_tags(SITE) + """</b:if>
    </b:switch>
    <meta content='Blog Digest' property='og:site_name'/>
    <b:if cond='data:view.isSingleItem'><meta content='article' property='og:type'/><b:else/><meta content='website' property='og:type'/></b:if>
    <meta content='summary_large_image' name='twitter:card'/>
    <b:if cond='data:view.isArchive'><meta content='noindex, follow' name='robots'/></b:if>"""

def once(pattern, repl, name, flags=re.S):
    global s
    n = len(re.findall(pattern, s, flags))
    if n != 1:
        sys.exit("expected one match for %s, found %d; nothing written" % (name, n))
    s = re.sub(pattern, lambda m: repl if isinstance(repl, str) else repl(m), s, count=1, flags=flags)

# 1. The first version of the design layer (CSS and script pasted into the head). The current layer removes
#    everything it adds, so it only costs download and a flash of the old hero. Replaced by the head loader.
once(r"<!-- Blog Digest redesign\. Paste this whole block.*?<style id='bd-redesign'>.*?<script>//<!\[CDATA\[.*?//\]\]></script>", HEAD, "v1 block")
# 2. Universal Analytics stopped collecting data in July 2023; the tag still downloads about 150 KB.
once(r"<!-- Global site tag \(gtag\.js\) - Google Analytics -->\s*<script async='async' src='https://www\.googletagmanager\.com/gtag/js\?id=UA-199206936-1'/>\s*<script>.*?</script>\s*", "", "UA tag")
# 3. OneSignal push notifications were removed from the site; the SDK still loaded on every page.
once(r"<script async='async' src='https://cdn\.onesignal\.com/sdks/OneSignalSDK\.js'/>\s*<script>.*?OneSignal\.init.*?</script>\s*", "", "OneSignal")
# 4. Let readers zoom (accessibility; also flagged by Lighthouse).
once(r"<meta content='width=device-width, initial-scale=1, minimum-scale=1, maximum-scale=1' name='viewport'/>",
     "<meta content='width=device-width, initial-scale=1' name='viewport'/>", "viewport")
# 5. Homepage title: the blog's name in Settings is still "Good vibes".
once(r"<title><data:view.title.escaped/></title>",
     "<b:if cond='data:view.isHomepage'><title>Blog Digest: Practical Guides to Money, Mind and Work</title>"
     "<b:elseif cond='data:view.isLabelSearch'/><title><data:view.search.label.escaped/> | Blog Digest</title>"
     "<b:elseif cond='data:view.isSingleItem'/><title><data:view.title.escaped/></title><b:else/><title>Blog Digest</title></b:if>", "title")
# 6. Sharing and indexing tags Blogger doesn't add.
once(r"<b:include data='blog' name='all-head-content'/>", META, "head content")
# 7. Open Sans and Arimo are overridden by the design layer's fonts; Arimo is metric-identical to Arial anyway.
once(r"<link href='//fonts\.googleapis\.com/css\?family=Open\+Sans[^>]*/>\s*<link href='//fonts\.googleapis\.com/css\?family=Arimo[^>]*/>\s*", "", "old fonts")
# 8. Font Awesome from jsDelivr (StackPath's CDN is being retired), without blocking the first paint.
once(r"<link href='https://stackpath\.bootstrapcdn\.com/font-awesome/4\.7\.0/css/font-awesome\.min\.css' rel='stylesheet'/>",
     "<link href='https://cdn.jsdelivr.net/npm/font-awesome@4.7.0/css/font-awesome.min.css' media='print' onload='this.media=&quot;all&quot;' rel='stylesheet'/>", "font awesome")
# 9. The sidebar gadget that used to load the design layer; the head loader replaces it.
once(r"(<b:widget id='HTML10'[^>]*>\s*<b:widget-settings>\s*<b:widget-setting name='content'><!\[CDATA\[)<script>.*?bd-redesign\.js.*?</script>(\]\]>)",
     lambda m: m.group(1) + "<!-- Blog Digest design now loads from the theme head. -->" + m.group(2), "gadget")
# 10. Site name in the homepage's WebSite structured data (it used the blog title, still "Good vibes").
once(r"&quot;@type&quot;:&quot;WebSite&quot;,&quot;name&quot;:&quot;<data:view.title.escaped/>&quot;",
     "&quot;@type&quot;:&quot;WebSite&quot;,&quot;name&quot;:&quot;Blog Digest&quot;", "WebSite name")
# 11. Oswald, loaded over http by the old recent-posts widget (blocked as mixed content; the layer restyles the list).
once(r"<link href='http://fonts\.googleapis\.com/css\?family=Oswald' rel='stylesheet' type='text/css'/>\s*", "", "Oswald")

for must_keep in ("id='mycontent'", "SoraTemplates", "<b:widget id='Blog1'", "data-ad-client='ca-pub-9105247488116580'",
                  "name='p:domain_verify'", "name='yandex-verification'"):
    if must_keep not in s:
        sys.exit("lost %s; nothing written" % must_keep)
open(sys.argv[2], "w", encoding="utf-8").write(s)
print("wrote", sys.argv[2], len(src), "->", len(s), "bytes")
