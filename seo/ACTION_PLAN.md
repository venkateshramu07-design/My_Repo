# Google indexing: action plan (started 10 November 2026)

Goal: get the blog's pages into Google's index. Baseline on 10 Nov: 6 of 305 indexed, homepage not indexed,
1 URL crawled in the previous week. Bing: 244 indexed. Evidence is in `index-status/DIAGNOSIS_2026-11-10.md`.

Owner key: **C** = Claude does it (through the Blogger, Search Console and Bing APIs). **O** = needs the owner's
own login, name or money; Claude has no way to do it.

## Phase 1: clean and stable for Google's re-crawl (10 Nov - early Dec)

The owner started "Validate fix" on 10 Nov, so Google will re-crawl the 236 listed addresses over about 2-4 weeks.
The site must be fault-free and must not change underneath that re-crawl.

| # | Action | Owner | Status |
|---|---|---|---|
| 1.1 | Inspect all 305 URLs; save snapshot | C | Done 10 Nov |
| 1.2 | Technical audit as Googlebot: status codes, robots, canonicals, redirects, rendered page, theme scripts, sitemap | C | Done 10 Nov, no faults |
| 1.3 | Audit all 301 post bodies: dead links, robots-blocked links, missing alt text, stray H1s, broken JSON-LD, placeholders | C | Done 10 Nov: 0 dead links, 0 missing alts, 0 invalid JSON-LD |
| 1.4 | Fix duplicate title (two posts were both "FIRE Movement: Financial Independence, Retire Early Explained") | C | Done 10 Nov: the 2021/07 post is now "How to Reach FIRE: Your FIRE Number, Savings Rate and Withdrawal Plan" (backup in `backup/dedupe/`) |
| 1.5 | Resubmit `sitemap.xml`, `sitemap-pages.xml` and the updated-posts feed; ping the feed hub | C | Done 10 Nov |
| 1.6 | Bing: correct the submission log, send the 95 never-sent URLs | C | Done 10 Nov (16 left, sent by the next weekly run) |
| 1.7 | Weekly check (Mondays 9:37 IST): inspect every URL, compare, submit to Bing, report | C | Scheduled; first run 16 Nov |
| 1.8 | Freeze: no bulk edits, republishing, new batches of posts or theme changes until the validation finishes | C + O | In force |
| 1.9 | Check Search Console > Security & Manual actions > Manual actions. If anything is listed, Claude writes the fix list and the reconsideration request | O (10 seconds) | Open |
| 1.10 | Request indexing in Search Console (about 10 a day) for the priority list below | O (optional) | Open |

Priority list for 1.10, in order: the homepage, `/p/trending-posts_53.html`, `/2021/06/sitemap.html`,
`/p/free-tools.html`, `/p/free-calculators.html`, then the 12 posts Google indexed in October and dropped
(listed in `index-status/2026-11-03.md`).

## Phase 2: reputation (can start now; runs alongside phase 1)

| # | Action | Owner | Status |
|---|---|---|---|
| 2.1 | Outreach batch 1: 14 researched pages that link to upload-based resizers, each with a written pitch (`outreach/2026-11-03-batch1.md`) | O sends (Claude has no mail access) | Ready to send |
| 2.2 | Outreach batches 2 and 3 (calculators; country data tables with CSV downloads), 15 prospects each | C prepares | After batch 1 goes out, so pitches can use what got replies |
| 2.3 | Play Store listing: put the blog address in Store listing > Contact details > Website. The listing already links the privacy policy on the blog | O | When the app goes to production |
| 2.4 | Named author: a real name and two lines of background on the About page and article bylines | O supplies the name; C applies it | Open |

## Phase 3: after the validation result (early December)

Decision on the first weekly check after Search Console shows the validation as finished:

- **Homepage indexed, or 30+ pages indexed:** resume publishing at 2 researched guides a week (never batches), and
  re-inspect weekly. Add the "More guides" link box to each newly indexed post (`crawl_boost.py`).
- **Still under 10 pages:** the site-wide judgement is holding. Then, in this order:
  1. Thin the programmatic set where the app doesn't need the page: fold the 35 country pages into the existing
     by-country hubs and keep only the countries that earned impressions (C, with the owner's go-ahead because pages
     come down).
  2. Move the blog to a custom domain (about ₹800 a year; Blogger redirects every old address). Needs an app update
     first, because the app's link handling and tool pages are tied to the blogspot address (O buys; C does the app
     and Search Console work).
  3. Re-measure 4 weeks later.

## Not doing

- Google's Indexing API for articles, paid "instant indexing" services, bought or exchanged links, link directories:
  against Google's spam rules and able to turn a soft hold-back into a penalty.
- Unpublishing the PDF/photo size pages: the Android app's tools load them.
- Adding a label to the six legal pages to fix their breadcrumb: the app would start listing them as articles. The
  fix belongs in the theme template (no effect on indexing).

## Tracking

Each weekly run appends to `index-status/`. Numbers to watch: pages indexed (6), URLs crawled in the week (1),
homepage status (not indexed), Bing pages indexed (244).
