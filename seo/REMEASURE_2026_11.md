# Google indexing and rankings re-measure, 13 November 2026

Compared with the 30 October 2026 baseline (`HEALTH_CHECK_2026_10.md`, `gsc_inspection_2026-10-30.json`, `URL_INSPECTION.xlsx`).

**Short version:** indexing went down, not up. Search Console now counts **6 indexed pages out of 309 (1.9%)**, against **18 of 301 (6.0%)** on 30 October. No post was newly indexed. No keyword ranks in the top 100 in the US or India. The new problem is **33 "Redirect error" pages**: almost every page Google crawled on 10 and 11 November failed with a redirect error.

## How it was measured

| Check | Source | Cost |
|---|---|---|
| Per-URL `site:` search for all 301 addresses (the 266 in the priority list plus the 35 other baseline addresses, including the 18 indexed ones) | DataForSEO Google organic SERP, standard queue, US / English | $0.90 |
| Ranked keywords for the domain, US and India | DataForSEO Labs `ranked_keywords` | $0.02 |
| Check of the `site:` method (6 live queries, including a domain-wide `site:` query in the US and India) | DataForSEO live SERP | $0.06 |
| Google's own index status for every address in the sitemap (309) | Search Console URL Inspection API (`gsc_inspect.py`) | free |
| **Total DataForSEO spend** | balance $34.92 → $33.94 | **$0.98** |

**The `site:` results can't be trusted this time.** The per-URL `site:` checks found only 1 of 300 addresses (the 2023 post "Cryptocurrency Regulation: Is It Possible?"). But Google returned no results at all for the domain-wide `site:randomthoughtsever.blogspot.com` query, in both the US and India. It also returned nothing for two pages that Search Console says are indexed. So 1 in 300 is only a lower bound. The Search Console figures below are the real measure. (The 30 October `site:` figures, about 77 posts on 28 October and 8 to 10 on 30 October, were already unstable.)

## Indexing: then and now (Search Console)

| Status | 30 Oct | 13 Nov |
|---|---:|---:|
| Submitted and indexed | **18** | **6** |
| Crawled, currently not indexed | 132 | 140 |
| Discovered, currently not indexed | 126 | 108 |
| URL is unknown to Google | 24 | 21 |
| Redirect error | 1 | **34** |
| Addresses inspected | 301 | 309 (8 new November posts) |
| **Indexed share** | **6.0%** | **1.9%** |

(NEXT_SESSION.md gave "about 7% of sampled posts" for 30 October. That came from a sample; the full Search Console count was 18 of 301, 6.0%.)

### Still indexed (6; none are new)
- Crypto Swap Tax: Do You Pay Tax Swapping One Crypto for Another? (article)
- Gold Price Fall 2026: Is Gold Still a Safe Haven? (article)
- Self-Improvement and Mental Well-Being: A Practical Guide (hub article)
- How Much Emergency Fund Do You Need? (With Calculator) (calculator)
- Cryptocurrency Regulation: Is It Possible? (2023 article)
- Bad Boss Signs: How to Tell You Work for a Toxic Boss (2023 article)

### Dropped out of the index (12)
All 12 are now "Crawled, currently not indexed". Their last crawls were between 20 October and 2 November, so Google dropped them after re-reading them. The redirect errors did not cause this.
- France Minimum Wage 2026 (country data)
- Debt Consolidation Loans: When They Save Money (calculator)
- Credit Card Payoff Calculator (calculator)
- Prepay Home Loan or Invest? (article)
- Why Did My Car Insurance Go Up When I Never Made a Claim?
- Why Are Groceries and Prices So Expensive When Inflation Is Down?
- What Happens If a Bank Fails? Your Money if a Bank or App Goes Bust
- How to Deal With Being Ignored (2023)
- Sleep Hacks (2023)
- Pursuit of Happiness (2023)
- Zen Quotes and Sayings (2023)
- Why Is the US So Rich? (2021)

### Newly indexed
None.

### Redirect errors (1 → 34)
- 33 of the 37 addresses Google crawled after 30 October now show "Redirect error". All of them were crawled by the mobile Googlebot on 10 and 11 November.
- The affected pages include 5 of the 8 new November posts, the GST and income tax calculators, the passport-photo and Aadhaar tools, and 15 older articles. The KB to MB converter has failed in the same way since 30 October, so the "one-off" explanation no longer holds.
- I couldn't reproduce the error from here. With Googlebot's smartphone user agent, a page gets Blogger's usual single 302 redirect to `?m=1`, which then returns 200. That `?m=1` page has the correct canonical, and neither the theme nor `bd-redesign.js` redirects on load.
- The likely suspects are Blogger's mobile `?m=1` redirect (redirecting back and forth, or temporary errors on Blogger's side during the crawl) or a short outage on 10 November.
- Either way, it blocks the pages Google is actively recrawling, which are the ones that matter most.

### Movement in the priority list (266 addresses)
- 203 are unchanged: 118 still "crawled, not indexed" and 85 still "discovered".
- 14 moved forward from "unknown" to "discovered". 17 moved backward from "discovered" to "unknown", so Google lost track of them.
- 27 now have redirect errors.
- None became indexed.

## By kind of post

| Kind | Addresses | Indexed 30 Oct | Indexed 13 Nov | Crawled, not indexed (13 Nov) | Redirect errors (13 Nov) |
|---|---:|---:|---:|---:|---:|
| Articles | 179 | 13 | 5 (2.8%) | 90 | 18 |
| Calculators | 15 | 3 | 1 (6.7%) | 11 | 2 |
| Country data pages (minimum wage, "by country") | 53 | 2 | 0 | 26 | 3 |
| Tools (PDF/image resizers, photo makers, generators) | 43 | 0 | 0 | 9 | 5 |
| Site pages (about, policy, hubs) | 11 | 0 | 0 | 4 | 1 |

- **Calculators** still have the best indexed rate (1 of 15). But they lost 2 of their 3 indexed pages (debt consolidation, credit card payoff).
- **Articles** hold 5 of the 6 indexed pages, mostly older (2023) posts and posts that answer a specific question.
- **Tools** have never had a page indexed. Most are still only "discovered", meaning Google hasn't fetched them. The site has about 20 near-identical "Resize Image to N KB" and "Compress PDF to N KB" pages.
- **Country data pages** lost the one that was indexed (France). There are 35 near-identical per-country templates.

## Rankings (DataForSEO Labs, ranked_keywords)

| Market | Ranking keywords (top 100) | Estimated traffic |
|---|---:|---:|
| United States | 0 | 0 |
| India | 0 | 0 |

There is no change from 30 October. Labs found no keyword where the domain appears in Google's top 100 in either market. With only 6 indexed pages, this is expected.

## Recommended next steps
1. **Fix the redirect errors first.**
   - Re-run `python3 seo/gsc_inspect.py` (free) in 3 to 4 days to see whether the 10 to 11 November errors were temporary.
   - If they persist, remove the `?m=1` mobile redirect. In Blogger, under Theme, then the mobile settings, set "Show desktop theme on mobile devices" to Yes, so phones get the responsive theme with no redirect. Blogger's API can't change this setting, so it needs the owner, and only if they agree.
   - Then resubmit the sitemaps.
2. **Pause bulk publishing and republishing, and reduce near-duplicates.**
   - Google crawled 12 indexed pages and then dropped them, and 140 pages are "crawled, not indexed". That points to a site-wide quality or duplication judgement, not a discovery problem.
   - Merge the roughly 20 "Resize Image / Compress PDF to N KB" pages into a few strong pages, with the size variants as sections or parameters.
   - Consider `noindex` for the thinnest of the 35 per-country templates.
   - Fix the duplicate "FIRE Movement: Financial Independence, Retire Early Explained" (two addresses with the same title).
3. **Earn links to a few strong pages instead of publishing more.**
   - Promote the emergency fund calculator, the GST and income tax calculators, and the passport-photo maker (see `OUTREACH.md` and `PROMO_KIT.md`).
   - Use the Bing submissions already made as a second channel. Without links, Google is crawling the site less, not more.
4. **Measure more cheaply.**
   - Use Search Console URL Inspection (free) as the main indexing measure, plus ranked_keywords ($0.02).
   - Drop the per-URL DataForSEO `site:` checks: they cost $0.90 this time and weren't reliable.

**Next check:** about **27 November 2026**: `gsc_inspect.py` plus ranked_keywords (US and India). Also do a quick `gsc_inspect.py` run around 17 November to see whether the redirect errors have cleared.
