# Getting Blog Digest found: search engines and AI answers

No one can guarantee a #1 ranking or force Google, ChatGPT, Perplexity or Gemini to prefer one site.
What works is making pages easy to crawl, clearly the best answer, trusted, and mentioned by other real sites.
Bought links, link farms, private blog networks and mass directory/comment links break Google's spam
policies and can get the whole blog demoted, so they are not part of this plan.

Live site check (20 October 2026):

| Item | Status |
|---|---|
| robots.txt allows Google, Bing and AI crawlers (GPTBot, OAI-SearchBot, PerplexityBot, ...) | OK |
| sitemap.xml | OK |
| Article structured data (Blogger adds BlogPosting, Breadcrumb, Organization) | OK |
| 40 long-form articles (3,000+ words, quick answer, FAQ, related links) | Live |
| Post search descriptions | **Still the old comma-separated keyword lists** (also used as the social share text) |
| Analytics | **Still Universal Analytics `UA-199206936-1`, which stopped recording in 2023** |
| About page with a real author | **About page has no author details** |

## 1. Owner tasks (only the blog owner can do these), in order

1. **Google Search Console** (search.google.com/search-console): add `https://randomthoughtsever.blogspot.com/`,
   submit `sitemap.xml`, then use URL Inspection > "Request indexing" for the 40 rewritten posts
   (about 10 a day is the practical limit). Check the Performance report monthly.
2. **Bing Webmaster Tools** (bing.com/webmasters): import the site from Search Console and submit the sitemap.
   ChatGPT search and Microsoft Copilot rely heavily on Bing's index, so this matters for AI answers.
3. **Search descriptions**: Blogger's API cannot set them. In Blogger > Posts > open a post >
   Post settings > Search description, paste the line for that URL from `seo/search_descriptions.csv`
   (the 40 live long-form posts are at the top). If that is too much work, turn off
   Settings > Meta tags > "Enable search description" so the keyword lists stop appearing at all.
   Also replace the blog-level description (Settings > Meta tags) with one sentence, e.g.
   "Practical, in-depth guides on personal finance, investing, psychology, relationships and careers."
4. **Analytics**: create a GA4 property, then put the `G-...` measurement ID in Settings > Google Analytics
   (replacing `UA-199206936-1`).
5. **Author identity (E-E-A-T)**: add your real name, photo, background and why you write on these topics to
   the About page and your Blogger profile. Google's quality guidelines and AI systems both weigh who is
   behind content, especially money and health topics. Anonymous finance advice ranks poorly.
6. **Custom domain** (optional, recommended): e.g. `blogdigest.in`, via Settings > Custom domain.
   Blogger redirects the old blogspot URLs, so existing links keep working. A brand domain is easier
   to remember, share and earn links to.

## 2. Earning backlinks (legitimate)

Aim for a few links a month from relevant, real sites, not hundreds from irrelevant ones.

| Tactic | How |
|---|---|
| Expert quotes for journalists | Sign up to Qwoted, Featured.com, SourceBottle or Help a B2B Writer; answer finance, career and psychology requests. Published quotes usually include a link. |
| Guest articles | Pitch 2–3 original topics to Indian personal finance, career and wellbeing blogs that accept contributors; include one link back to the matching in-depth guide. |
| Linkable assets | Turn the strongest guides into resources others cite: e.g. an EMI vs prepayment calculator, a "SIP vs lumpsum" returns table, a printable 30-day credit card debt plan. |
| Broken-link building | Find dead links on finance and psychology resource pages (Ahrefs free broken-link checker, Check My Links extension) and suggest the matching guide as a replacement. |
| Community answers | Answer real questions on Quora, Reddit (r/IndiaInvestments, r/personalfinanceindia, r/relationships) and LinkedIn with a full answer; link only when the guide genuinely adds more. No link-dropping. |
| Republishing | Post shortened versions on Medium and LinkedIn Articles with "Originally published at ..." and a link to the full guide. |
| Visual platforms | Pinterest pins for each guide (finance and self-improvement do well there); short YouTube/Instagram videos summarising the quick answer, with the link in the description. |
| Unlinked mentions | Set a Google Alert for "Blog Digest" and "randomthoughtsever"; ask sites that mention the blog without a link to add one. |

Avoid: paid link packages, "1,000 backlinks" gigs, link exchanges at scale, auto-generated comments,
and spammy directories.

## 3. Content that search engines and AI cite

AI answer engines quote pages that state the answer clearly, early, and with specifics. The rewrites
already follow this; keep doing it for lines 40–153 of `rewrite_queue.txt`:

- Open with a 2–4 sentence direct answer ("Quick answer").
- Use question-style H2/H3 headings that match how people ask (and an FAQ section).
- Include specific numbers, dates, worked examples and named sources (RBI, SEBI, IRS, studies).
- Keep facts current: add the "Updated <date>" line and revise figures when rules change
  (tax slabs, repo rate, contribution limits).
- One topic per page, and link related guides to each other (the "Related articles" box does this).
- Cover a topic cluster fully (e.g. all home loan questions) so the blog becomes the go-to source for it.

## 4. Measuring progress

Check monthly in Search Console and Bing Webmaster Tools: indexed pages, impressions, clicks, average
position per query, and top linking sites. Expect 3–6 months before rankings move noticeably for a
blogspot site with few links; new posts and steady links speed this up.
