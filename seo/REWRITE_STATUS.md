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

## Hubs and new articles (published 21 October 2026)

- Live: the three finance articles (prepay vs invest, emergency fund, CIBIL score) and the money, mind and
  career "Start here" hubs. Post IDs and URLs are in `seo/tools/published.json`.
- Every labelled post (154) shows a "Start here" link to its topic hub in the related block. The hub is
  chosen from the post's main label via `HUB_LABELS` in `apply.py`; Technology & AI, Cryptocurrency,
  Countries & Living Abroad, Politics and Parenting & Family have no hub yet.
- The sitemap page lists the hubs under "Start Here: Topic Guides" and the new articles under their topic.
- Emergency fund calculator and credit utilisation checker tested on the live pages: both work.
- Blogger limits how many new posts can be created in a short time (429 `rateLimitExceeded`);
  wait a few minutes and run the command again.
- Console errors on live pages come from the theme's random posts widget (`random_posts`), not from post content.

## Pending: share covers (prepared 22 October 2026, waiting for a valid token)

The six posts published from `seo/hubs` and `seo/articles` have no image, so their WhatsApp, Facebook and X
previews show none. `publish_tools.py` now puts a cover from `seo/covers/` after their "Updated" line.
Republish them (keeps their dates):

    python3 seo/publish_tools.py publish articles/prepay-home-loan-or-invest articles/emergency-fund-guide articles/good-credit-score-cibil hubs/money-start-here hubs/mind-start-here hubs/career-start-here

Then check that each page's `og:image` meta tag names the cover.

