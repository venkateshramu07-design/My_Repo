# Google indexing: action plan (started 10 November 2026)

Goal: get the blog's pages into Google's index. Baseline on 10 Nov: 6 of 305 indexed, homepage not indexed,
1 URL crawled in the previous week. Bing: 244 indexed. Evidence is in `index-status/DIAGNOSIS_2026-11-10.md`.

Everything in this plan is done by Claude through the Blogger, Search Console and Bing APIs. Nothing in it is a
task for the owner; the only owner input is a yes or no to the phase 3 proposal in December. Steps that would need the owner's own login, name, mailbox or money are left out (listed once at the end).

## Phase 1: clean and stable for Google's re-crawl (10 Nov - early Dec)

The owner started "Validate fix" on 10 Nov, so Google will re-crawl the 236 listed addresses over about 2-4 weeks.
The site must be fault-free and must not change underneath that re-crawl.

| # | Action | Status |
|---|---|---|
| 1.1 | Inspect all 305 URLs; save snapshot | Done 10 Nov |
| 1.2 | Technical audit as Googlebot: status codes, robots, canonicals, redirects, rendered page, theme scripts, sitemap | Done 10 Nov, no faults |
| 1.3 | Audit all 301 post bodies: dead links, robots-blocked links, missing alt text, stray H1s, broken JSON-LD, placeholders | Done 10 Nov: 0 dead links, 0 missing alts, 0 invalid JSON-LD |
| 1.4 | Fix duplicate title (two posts were both "FIRE Movement: Financial Independence, Retire Early Explained") | Done 10 Nov: the 2021/07 post is now "How to Reach FIRE: Your FIRE Number, Savings Rate and Withdrawal Plan" (backup in `backup/dedupe/`) |
| 1.5 | Resubmit `sitemap.xml`, `sitemap-pages.xml` and the updated-posts feed; ping the feed hub | Done 10 Nov |
| 1.6 | Bing: correct the submission log, send the 95 never-sent URLs | Done 10 Nov (16 left, sent by the next weekly run) |
| 1.7 | Weekly check (Mondays 9:37 IST): inspect every URL, compare, submit to Bing, report | Scheduled; first run 16 Nov |
| 1.8 | Freeze: no bulk edits, republishing, new batches of posts or theme changes until the validation finishes | In force |

## Phase 2: weekly watch (every Monday from 16 Nov)

Each run inspects all URLs, compares with the week before, sends Bing its next batch, commits the snapshot and reports
four numbers: pages indexed, URLs Google crawled in the week, homepage status, Bing pages indexed. If posts become
indexed, `crawl_boost.py` adds the one-time "More guides" link box to them, pointing at posts Google hasn't crawled.

## Phase 3: act on the validation result

Decided at the weekly check on **7 December** (or earlier if Search Console's validation has clearly finished):

- **Homepage indexed, or 30+ pages indexed:** propose starting 2 researched guides a week (never batches), each
  fact-checked against primary sources; start on the owner's "yes". Keep the weekly watch.
- **Still under 10 pages:** propose folding the 35 country pages into the existing by-country hubs, keeping the
  countries that earned impressions; the rest would go back to draft (same address if republished later; backups
  kept). Pages come down only on the owner's "yes". The PDF/photo size pages stay, because the Android app's tools
  load them. Re-measure four weeks after.
- **Between 10 and 29 pages:** keep the freeze and re-decide four weeks later.

## Not doing

- Google's Indexing API for articles, paid "instant indexing" services, bought or exchanged links, link directories:
  against Google's spam rules and able to turn a soft hold-back into a penalty.
- Unpublishing the PDF/photo size pages: the Android app's tools load them.
- Adding a label to the six legal pages to fix their breadcrumb: the app would start listing them as articles. The
  fix belongs in the theme template (no effect on indexing).

## Outside Claude's reach (not part of this plan)

Sending email, the manual buttons in Search Console (Manual actions page, Request indexing), buying a domain, and
putting a person's name on the site. `outreach/2026-11-03-batch1.md` stays in the repo as written.

## Tracking

Each weekly run appends to `index-status/`. Numbers to watch: pages indexed (6), URLs crawled in the week (1),
homepage status (not indexed), Bing pages indexed (244).
