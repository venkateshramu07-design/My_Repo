# Next session: start here

1. **Bing submission.** The user added `BING_API_KEY` to the environment on 30 October 2026.
   - Run `python3 seo/bing_submit.py`. It submits up to Bing's daily quota in priority order and records progress in `seo/bing_submitted.json`.
   - Re-run it on later days until every address is sent.
   - After each publish, run `python3 seo/bing_submit.py <changed URLs>`.
2. After every publish, also run `python3 seo/ping_feeds.py`, which notifies Google's WebSub hub. After refreshing `.live`, run `python3 seo/build_index.py` to update the public README index.
3. **Search Console:** with a token that has the `webmasters` scope (passed as `GSC_TOKEN`, never saved), run `python3 seo/gsc_inspect.py` to get Google's index status for every address.
   - On 30 October: 18 indexed, 132 crawled but not indexed (125 of them last crawled before the 29 October rewrites), 126 discovered, 24 unknown, 1 redirect error (the KB to MB converter, probably a one-off).
   - All sitemaps were resubmitted on 30 October.
4. **Never ask the user to do manual work.** The Blogger token (`BLOGGER_TOKEN`, passed only as an environment variable and never saved) is the only exception.
5. **About 13 November 2026:** re-measure Google indexing and rankings with DataForSEO.
   - Use per-URL `site:` checks (see `seo/URL_INSPECTION.xlsx` for the priority list) and ranked_keywords.
   - On 30 October, about 7% of sampled posts were indexed.
6. Status and scripts:
   - `seo/sizes/STATUS.md`: exact-size pages.
   - `seo/publish_u3.py`, `seo/publish_pending.py`: republish batches.
   - `seo/fetch_live.py` + `seo/seo_audit.py`: live audit. On 30 October, 290 of 290 posts passed.
