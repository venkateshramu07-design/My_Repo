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
- Queue lines 40–149: rewritten at 3,000+ words, not yet published (token expired).
- Line 35 (`2021/09/how-much-does-google-know-about-me`) was edited after publishing (an invented example is now labelled illustrative); republish it with the next batch.
- Queue lines 150–153: not started.
- `2021/06/how-to-be-happy` is live (its publish date was moved to 2026-10-20 at the owner's request).
- Blogger API calls need `BLOGGER_TOKEN` (an OAuth Playground access token, valid for one hour).
