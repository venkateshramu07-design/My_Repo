# Why Google isn't indexing the blog: diagnosis, 10 November 2026

Asked: get every page indexed. Short answer: nothing technical is blocking Google. Google has made a site-wide
judgement about the blog, it predates the October work, and it can't be overridden from our side.

## Numbers (Search Console URL Inspection, all 305 live URLs, `2026-11-10.json`)

| Status | 3 Nov | 10 Nov |
|---|---|---|
| Indexed | 6 | 6 |
| Crawled, not indexed | 142 | 142 |
| Discovered, not indexed | 128 | 107 |
| Unknown to Google | 23 | 48 |
| Redirect error | 1 | 2 |

- Google crawled **1** URL between 3 and 10 November. Almost every "last crawled" date is 20-29 October.
- The **homepage** is "Crawled - currently not indexed", last crawled **27 July 2026**.
- Search Console performance: **6 impressions in total from July 2025 to September 2026**, 192 in October (peak 80 on
  26 Oct), 11 so far in November. The blog had no Google visibility long before the October rewrite.
- A live `site:` search (Google India) returns the same 6 pages. A search for the blog's own address does not return it.
- The "236 pages" in Search Console's report counts each address twice (with and without `?m=1`).
- **Bing: 244 pages in its index**, crawling 36-100 pages a day, with impressions for queries such as
  "free pdf to jpg convert" and "kokorozashi".

## Ruled out (checked on 10 Nov)

- Pages return 200 with `x-robots-tag: all`; no `noindex` anywhere (raw HTML or after scripts run); canonicals point to
  the clean address; Google's chosen canonical matches on every crawled page.
- Mobile Googlebot gets one 302 to `?m=1`, then 200 (normal for Blogger). No script redirects in the theme, the
  design layer or the posts.
- Rendered as Googlebot Smartphone in Chromium: the article text is visible, title/description/canonical are right.
- The theme's 263 KB obfuscated script is the standard SoraTemplates credit check. Its footer link is present and
  visible, so it never redirects.
- `robots.txt`, `sitemap.xml` (301 URLs, read by Google on 9 Nov with no errors), jsDelivr and raw.githubusercontent
  (no robots blocks). Intermittent 503s seen from the cloud machine also happen on Google's own Blogspot blogs
  (rate limiting of the machine's address), so they aren't a site fault.
- Structured data parses. Only fault: the breadcrumb on the 6 posts without a label (theme template; no effect on indexing).

## What the evidence points to

1. **A long-standing site-wide classification.** Homepage not indexed, zero impressions for 15 months, no ranking for
   its own name. That pattern is a site-level quality or spam judgement (or a manual action, which only the Search
   Console UI shows), not page-level faults.
2. **October made the site look like scaled content.** 139 new posts in about ten days, and 159 older posts rewritten
   to a near-uniform length (2022-2024 posts are all 3,157-4,560 words, median about 3,270), all "updated" on 30 Oct.
   62 posts still share more than 25% of their text with three or more others (country pages and PDF/photo size pages,
   up to 49%). Google's crawl burst of 20-29 Oct was followed by a near-total stop.
3. **No outside signals.** 5 referring domains, none editorial. With no links there is almost no crawl demand.

Bing indexing 80% of the same pages confirms the pages are reachable and readable; the hold-back is Google's assessment.

## What can and can't be done

- No API or setting forces indexing. The Indexing API is only for job and livestream pages; using it for articles
  breaks Google's rules. Paid "instant indexing" services and bought links risk a real penalty.
- More site-wide edits would add to the churn that item 2 describes. The site should stay stable while Google
  re-crawls for the validation the owner started on 10 Nov (usually 2-4 weeks).
- Levers that exist, all the owner's call: check Manual actions in Search Console; real links (the outreach list in
  `seo/outreach/`, the Play Store listing once the app is public); a named author; pruning or merging the most
  templated pages; a custom domain (needs an app update, since the app is tied to the blogspot address).

## Done on 10 Nov

- Fresh snapshot of all 305 URLs.
- Bing: found that the daily routine couldn't save its log, so it re-sent the same 95 URLs each day. Corrected the log
  and sent the 95 URLs Bing had never been given (16 left for the next run).
- Replaced the failing daily routine with a weekly check that runs in the main session (which can save results).
