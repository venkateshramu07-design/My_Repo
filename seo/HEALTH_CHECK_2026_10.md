# Blog health check, October 2026

Data: the public feed (274 posts), Google results and keyword data from DataForSEO (about $0.49 of API credit).

## What is fine
- All 274 posts are crawlable: no noindex, correct canonical URLs, sitemap lists all 274, robots.txt allows everything.
- Content depth: 271 posts have 700+ words; every post has section headings; only 3 images lack alt text (tool previews).
- Structured data (BlogPosting, Breadcrumb) is present; pages work on phones.

## What stops posts ranking
1. **Google barely indexes the site.** A site: search shows about 77 of 274 posts, and DataForSEO finds no
   keyword where the site ranks in the top 100 (US or India). Most posts are invisible, whatever their quality.
   Likely causes: a young site, 270+ posts rewritten or published in one month, and no Search Console sitemap
   submission or indexing requests (no sign Search Console is set up yet).
2. **170 of 274 titles matched no measurable search.** They were phrased the way an essay is titled
   ("The Genesis of Petrodollars...", "Spain in Numbers 2026..."), not the way people search ("petrodollar",
   "spain minimum wage"). Fixed below for 132 posts.
3. **Every post has the same meta description in its HTML** (the blog's general description). The per-post
   descriptions are added by JavaScript, which Google does not always use for snippets.

## Changes made
- **132 titles rewritten** to lead with a phrase people search, chosen by volume and difficulty
  (`title_changes_2026_10.json`, applied with `apply_titles.py`). Examples:
  - "How to Fall Asleep Fast and Sleep Peacefully" → lead phrase "how to fall asleep fast" (74,000 US searches a month, difficulty 26)
  - "Spain in Numbers 2026..." → "Spain Minimum Wage 2026..." (and 34 other country pages; "uk minimum wage" 135,000 a month)
  - "How to Mask Your Aadhaar..." → "Masked Aadhaar: ..." (40,500 India searches, difficulty 0)
  - "Merge, Split and Reorder PDFs..." → "Merge PDF and Split PDF Free..." ("split pdf" 673,000 India searches, difficulty 26)
- Titles are also changed in plan.py, publish_tools.py and countries/build_countries.py, so republishing keeps them.
- URLs are unchanged, so existing links and any indexed pages keep working.

## Needs the owner (cannot be done through the API)
1. **Google Search Console** (free): add the site, submit `sitemap.xml`, then request indexing for the 20-30 most
   important posts (URL Inspection, about 10 a day). This is the single biggest fix.
2. **Search descriptions in HTML:** Blogger Settings, Meta tags, enable "Search description". Then per-post
   descriptions can be set (to test through the API's customMetaData field once a token is available).
