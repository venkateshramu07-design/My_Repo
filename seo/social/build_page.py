# Build pack.html (the social pack page published as an artifact) from pack.json and the search check-up below.
#   python3 seo/social/build_page.py
import json, os
HERE = os.path.dirname(os.path.abspath(__file__))
DESCS = json.load(open(os.path.join(HERE, "..", "theme", "descriptions.json"), encoding="utf-8"))
pack = json.load(open(os.path.join(HERE, "pack.json"), encoding="utf-8"))
SHORT = {"gdp": "GDP", "efund": "Emergency fund", "ikigai": "Ikigai", "sleep": "Sleep", "burnout": "Burnout"}
for a in pack["articles"]:
    a["short"] = SHORT[a["id"]]
BLOG_DESC = "Clear, in-depth guides to money, economics, careers, psychology and well-being, for readers everywhere."
seo = {
 "summary": "I checked all 140 articles on the live site. The writing itself is in good shape. The problems are in Blogger settings that only you can change, and they hurt search and social sharing at the same time: a shared link currently shows an old keyword list, and no visits are being counted.",
 "findings": [
  {"title": "Shared links show an old keyword list", "status": "you",
   "text": "When anyone shares an article on Facebook, WhatsApp or LinkedIn, the preview text comes from Blogger's search description. Older posts still have the old comma-separated keywords (with typos), and new posts fall back to the blog description, which is also a keyword list. Google is less affected because the site's design layer fixes it for browsers, but social apps don't run that code. Fix this before posting the social pack.",
   "steps": ["Blogger → <b>Settings</b> → <b>Meta tags</b>: turn on <b>Enable search description</b> and paste the blog description below.",
             "For each of the five articles in this pack: open the post → <b>Post settings</b> → <b>Search description</b> → paste its line below → <b>Update</b>.",
             "Then open <b>developers.facebook.com/tools/debug</b>, paste each article link and tap <b>Scrape again</b>, so Facebook forgets the old preview."],
   "copies": [{"label": "Blog description", "text": BLOG_DESC}] + [
       {"label": a["short"], "text": DESCS[a["url"].replace("https://randomthoughtsever.blogspot.com", "")]} for a in pack["articles"]]},
  {"title": "Visits aren't being counted", "status": "you",
   "text": "The blog still sends visits to an old Universal Analytics ID (UA-199206936-1). Google switched those off in 2023, so there's no record of which articles people read or where they come from. Without that, nobody can tell which posts are worth promoting.",
   "steps": ["Go to <b>analytics.google.com</b> → <b>Admin</b> → <b>Create</b> → <b>Property</b>, name it Blog Digest, then add a <b>Web</b> data stream for randomthoughtsever.blogspot.com.",
             "Copy the <b>Measurement ID</b> (it starts with G-).",
             "Blogger → <b>Settings</b> → <b>Google Analytics Measurement ID</b> → paste it → <b>Save</b>.",
             "Send me the G- ID too. If the theme doesn't pick it up within a day, I'll add it through the design gadget."]},
  {"title": "Find the posts that are almost on page 1", "status": "you",
   "text": "This needs Google Search Console, which only you can open. Posts sitting at positions 8 to 20 are the quickest wins: a better title, a clearer answer near the top and a few internal links can move them onto page 1.",
   "steps": ["Open <b>search.google.com/search-console</b> and add <code>https://randomthoughtsever.blogspot.com/</code> as a URL-prefix property. Blogger usually verifies it automatically for the blog owner.",
             "<b>Sitemaps</b> → submit <code>sitemap.xml</code>.",
             "After 3 or 4 days: <b>Performance</b> → <b>Search results</b> → tick <b>Average position</b> → open the <b>Pages</b> tab, then the <b>Queries</b> tab.",
             "Send me screenshots of both tabs, or use <b>Export</b>. I'll rework the posts that are close.",
             "While you're there: <b>bing.com/webmasters</b> → <b>Import from Google Search Console</b>. Bing also feeds DuckDuckGo, Yahoo and ChatGPT search."]},
  {"title": "Six titles that undersell their articles", "status": "you",
   "text": "These titles are short or vague, so they get fewer clicks in search results than the articles deserve. The new titles are ready and saved; I need a fresh Blogger token to publish them. Web addresses stay the same, so no links break.",
   "titles": [["How to Handle Work Pressure", "How to Handle Work Pressure Without Burning Out"],
              ["Signs You Need a Career Break", "Signs You Need a Career Break (and How to Plan One)"],
              ["Top Secrets to Become Rich", "How to Become Rich: The Habits That Really Build Wealth"],
              ["Steps to Win in Life", "20 Steps to Win in Life: A Practical Guide to Success"],
              ["The Grammar of a Good Leader", "The Grammar of a Good Leader: 12 Rules for Leading Well"],
              ["Zen Quotes and Their Meaning", "Zen Quotes and Their Meaning: Classic Sayings Explained"]]},
  {"title": "Article quality: healthy", "status": "done",
   "text": "All 140 articles were checked. Every one has at least two links to other articles, alt text on every image and a quick answer at the top. All but the three Start Here hubs have an FAQ section. The only short pages are the calculators and hubs, which is expected for tools. The GDP article's summary has been rewritten for a global reader."}
 ]}
tpl = open(os.path.join(HERE, "page_template.html"), encoding="utf-8").read()
dump = lambda o: json.dumps(o, ensure_ascii=False).replace("</", "<\\/")
open(os.path.join(HERE, "pack.html"), "w", encoding="utf-8").write(tpl.replace("__PACK__", dump(pack)).replace("__SEO__", dump(seo)))
print("wrote pack.html")
