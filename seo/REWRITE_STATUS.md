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

- Queue lines 1–30: rewritten and expanded to 3,000+ words, except
  `2021/07/qualities-of-good-team-lead` (line 27, ~2,100 words), `2021/08/how-to-control-emotions` (line 29) and
  `2021/08/how-to-manage-credit-card-loans` (line 30), which still need expanding.
  The other 27 were republished in their 3,000+ word versions on 2026-10-20 and verified live.
  Lines 27, 29 and 30 are still live in their earlier, shorter versions.
- Queue lines 31–35: drafted but still short (about 1,000 words); expand before publishing.
- Queue lines 36–153: not started.
- `2021/06/how-to-be-happy` is live (its publish date was moved to 2026-10-20 at the owner's request).
