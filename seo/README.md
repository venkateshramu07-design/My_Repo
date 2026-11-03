# SEO for randomthoughtsever.blogspot.com

## Audit findings (October 2026, all 164 public posts)

| Problem | Posts affected |
|---|---|
| Search descriptions are comma-separated keyword lists (Google ignores or rewrites these) | 153 |
| Images with no alt text | 857 images |
| Extra `<h1>` headings inside post bodies (the theme already uses `<h1>` for the title) | 147 |
| No internal links to other posts | 134 |
| Generic labels (Internet, world, interesting, Trends, news) on almost every post, so topics are unclear | ~140 |
| Titles with typos or weak wording ("How To Forgot Someone?", "Warm Hole", "Is Invest In Gold Is Good Idea?") | ~100 |
| "sitemap" pages show raw JavaScript code to visitors | 2 |
| Image-only posts with no text (thin content) | 3 |
| URLs Google still lists that now return 404 (`2021/10/wall-street-influence-in-world.html`, `2021/09/what-is-better-investment-bitcoin-or.html`) | 2 |
| Analytics uses Universal Analytics `UA-199206936-1`, which Google shut down in 2023, so no traffic is recorded | whole site |
| Homepage title is just "Blog Digest" and the blog description is a repeated keyword list | homepage |

## What `apply.py` changes (via the Blogger API)

For every post listed in `plan.py`:
- a clearer, keyword-focused title (the URL stays the same, so existing links keep working)
- a search description written as one readable sentence
- topic labels (Personal Finance, Investing, Psychology, Love & Relationships, Career & Workplace, ...) in place of the generic ones
- alt text on every image
- in-body `<h1>` headings changed to `<h2>`
- a "Related articles" box with four links to posts on the same topic, plus a link to the sitemap page

Also:
- `2021/06/sitemap` is rebuilt as a real HTML list of all articles grouped by topic
- `2021/06/sitemappages` (a broken duplicate) is moved back to draft
- the three quote image posts get a title, the quote as text, a short reflection and alt text

Every post is saved to `seo/backup/` before anything is changed, and `python3 seo/apply.py restore <slug>` restores a single post.

## Running it

1. Get a Blogger access token: open https://developers.google.com/oauthplayground, choose
   "Blogger API v3" > `https://www.googleapis.com/auth/blogger`, click "Authorize APIs", sign in with the
   blog owner's Google account, then click "Exchange authorization code for tokens".
2. Store the access token as the environment variable `BLOGGER_TOKEN` in this cloud environment's settings,
   then start a new session. The token is valid for one hour.
3. `python3 seo/apply.py backup`, then `python3 seo/apply.py apply --dry-run`, then `python3 seo/apply.py apply`.

## Crawling and indexing (November 2026)

The 3 Nov 2026 check (`index-status/2026-11-03.md`) found only 6 of 300 URLs indexed and 151 never crawled.
Main cause: the homepage shows only the 4 newest posts and its "Older posts" link points to `/search?...`,
which robots.txt blocks, so Googlebot could reach only about 25 posts by following links.

- `crawl_boost.py` turns the sitewide "Trending Posts" page and `2021/06/sitemap` into full topic directories,
  so every post is 2 clicks from any page. It also adds a "More Guides" box to indexed posts that links to
  posts not yet crawled, and resubmits the sitemaps. Every changed item is backed up to `seo/backup/crawl-boost/`.
  To undo a change, run `python3 seo/crawl_boost.py restore <file>`.
- `index_check.py` inspects every URL and writes `index-status/<date>.json` and `.md`, comparing each run with the previous one.
- `bing_submit.py` submits the daily Bing quota (100 URLs), rotating through the sitemap.

`detemplate.py` (run 3 Nov 2026) cut the shared text on the 35 country pages and 16 PDF/photo size pages:
country pages lost the generic intro (now a "where it stands out" paragraph from the page's own rankings), the nine
"Full table ... Download CSV" lines (now one short link per section), the FAQ that repeated every figure (and its
FAQPage JSON-LD), the identical 34-country list (now nearby countries) and two empty-value bugs ("inflation at % ()",
"after % in 2026"). Each country page also has a comparison table against its nearest neighbours. The tool pages'
identical "how it works" and "test files" text now lives once on the Reduce Image Size hub. Text shared with 5+
other posts fell from about 50-57% to about 30-46% per page.

Before that fix: 55 templated posts (35 "<country> in numbers 2026" pages and about 20
"compress PDF / resize image to X KB" variants) share 50-57% of their text with each other. Also, 137 posts
were published between 20 and 30 Oct (60 on 25 Oct alone). Google tends to hold back a young site that
grows this fast until it has built some trust.
