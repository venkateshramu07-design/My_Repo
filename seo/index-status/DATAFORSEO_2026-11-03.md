# DataForSEO check: 3 November 2026

Cost about $0.15 of the $44.69 balance.

## Google results (SERP API)
- `site:randomthoughtsever.blogspot.com` in Google US shows about 10 results (8 on 30 Oct). Google India returned none.
  The endpoint was flaky: some identical queries failed and others returned 0 before succeeding, so treat `site:` as rough.
- The blog does not appear for its own name ("randomthoughtsever", "randomthoughtsever.blogspot.com") or for the exact
  title of an indexed post, in Google US or India.

## Rankings (DataForSEO Labs)
- Ranked keywords in the top 100: **0** in the US and **0** in India. Domain rank overview: no data.
- Search Console shows the few impressions at positions 70-100 for competitive money terms (life insurance rates by
  age, CIBIL score, crypto swap tax).

## Backlinks
- 5 referring domains, 56 backlinks, rank 66, backlink spam score 35.
- 50 of the 56 links are from blogarama.com (a feed directory, "Read More" links to 2023 posts). The rest are
  Steemit profile links. All 52 target URLs are live.
- No editorial links from real sites.

## Technical crawl (On-Page API, 322 URLs)
- 0 broken links, 0 broken resources, 0 redirect loops, 0 duplicate titles. On-page score 97.65.
- 61 URLs answered 302 to `google.com/sorry` (Google's anti-bot page for the crawler's IP), not a site problem.
  The same URLs return 200 normally.
- **106 pages share the blog-wide meta description** ("Clear, in-depth guides to money..."): 88 posts, the homepage,
  /p/ pages and label pages. These posts have no search description in Blogger. The Blogger API cannot set that
  field, and the design layer adds descriptions only in the browser.
- Pages are about 390 KB of HTML each. A post has about 357 KB of inline script, most of it from the theme.
- 33 titles are over 65 characters. Label pages have no `<h1>`.

## What this means
Google dropped 12 of 18 indexed pages between 30 Oct and 3 Nov, 8 of them without crawling them again.
Together with zero rankings and almost no outside links, that points to a site-level judgement: a young,
anonymous finance site that published or rewrote nearly 300 long articles in October.
On-page changes have reached diminishing returns. What moves this now:
1. **Stop the churn.** No more site-wide republishes. Let Google re-crawl a stable site.
2. **Real links and mentions** from other sites (OUTREACH.md on `ccr-ee047f91-rbyv4x`).
3. **A real, named author with credentials** on the About page and article bylines. This matters most for money
   and health topics.
