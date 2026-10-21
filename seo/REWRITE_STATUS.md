# Article rewrite status

Goal: rewrite every post in `rewrite_queue.txt` (153 posts) in the style of
`rewrites/2021_06_how-to-be-happy.html`, at **more than 3,000 words** each, then publish.

Rules agreed with the blog owner:
- Keep each post's original publish date; start the article with `<p><em>Updated <date></em></p>`.
- Structure: quick answer, intro, numbered/H2 sections with practical steps, FAQ, final thoughts.
- Publish in batches of about 10 and check each batch is live.
- When updating a post through the API, always send the full post (title, labels, content, published).
  Sending only one field blanks the post body.

Publish (refuses anything under 3,000 words or that would lose images):

    python3 seo/publish_rewrites.py <slug> [<slug> ...]

## Progress

- Queue lines 1–39: rewritten at 3,000+ words and published (verified live on 2026-10-20).
- Queue lines 40–149: rewritten at 3,000+ words and published in batches of 10 (verified live on 2026-10-21).
- Line 35 (`2021/09/how-much-does-google-know-about-me`): edited version republished (verified live on 2026-10-21).
- Queue lines 150–153: rewritten at 3,000+ words and published (verified live on 2026-10-21).

All 153 queued posts are rewritten and live.
- `2021/06/how-to-be-happy` is live (its publish date was moved to 2026-10-20 at the owner's request).
- Blogger API calls need `BLOGGER_TOKEN` (an OAuth Playground access token, valid for one hour).

## Pending publish (prepared 21 October 2026, waiting for a valid token)

Run in this order (later steps link to URLs created by earlier ones):

    python3 seo/publish_tools.py publish articles/prepay-home-loan-or-invest articles/emergency-fund-guide articles/good-credit-score-cibil
    python3 seo/publish_tools.py publish hubs/money-start-here hubs/mind-start-here
    python3 seo/publish_tools.py publish hubs/career-start-here

Then:
- add a "Start here" link per topic to `related_block` in `apply.py` (hub URLs are in `seo/tools/published.json`)
  and republish all posts with `publish_rewrites.py` in batches of 10;
- add the hubs and new articles to the sitemap page (`2021/06/sitemap`);
- test the emergency fund calculator and utilisation checker on the live pages.
