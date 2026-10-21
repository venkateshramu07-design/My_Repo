# Getting Blog Digest found: search engines and AI answers

No one can guarantee a #1 ranking or force Google, ChatGPT, Perplexity or Gemini to prefer one site.
What works is making pages easy to crawl, clearly the best answer, trusted, and mentioned by other real sites.
Bought links, link farms, private blog networks and mass directory/comment links break Google's spam
policies and can get the whole blog demoted, so they are not part of this plan.

Live site check (21 October 2026):

| Item | Status |
|---|---|
| robots.txt allows Google, Bing and AI crawlers (GPTBot, OAI-SearchBot, PerplexityBot, ...) | OK |
| sitemap.xml | OK |
| Article structured data (Blogger adds BlogPosting, Breadcrumb, Organization) | OK |
| All 153 queued posts rewritten as long-form articles (3,000+ words, quick answer, FAQ, related links) | Live |
| Site redesign (`theme/redesign.xml`) | Live |
| 4 free calculators (SIP, EMI + prepayment, FIRE, credit card payoff), linked from 28 finance posts and the sitemap page | Live |
| 11 interactive tools inside 24 posts (sleep cycle, inflation, lumpsum vs SIP, debt avalanche vs snowball, 401(k), time zones, habit tracker, Eisenhower matrix, live currency converter, annuity payout, BMI with Asian cut-offs); 10 more posts link to the matching calculator | Live (21 October) |
| Post search descriptions | **Still the old comma-separated keyword lists with typos** ("postive", "awarness", "randomthougtsever"). These also appear as the text when a post is shared. The Blogger API ignores this field (tested 21 October), so it can only be changed in Blogger |
| Blog description | **Still a repeated keyword list** |
| Analytics | **Still Universal Analytics `UA-199206936-1`, which stopped recording in 2023**, so visits aren't being counted |
| About page with a real author | **About page has no author details** |
| Theme "random posts" widget | Throws a JavaScript error on every page (see 1.7) |

## Do these first (about 1 hour in total, biggest impact first)

1. Search Console: add the site, submit `sitemap.xml`, then request indexing for the 4 calculators and your 10 best guides (1.1 below).
2. Bing Webmaster Tools: import from Search Console (1.2). ChatGPT search and Copilot draw heavily on Bing.
3. Settings > Meta tags: replace the blog description with the sentence in 1.3, and either paste the new search descriptions or switch them off.
4. GA4 measurement ID (1.4), so you can see what's working.
5. About page with your name and background (1.5).
6. Share the calculators using the posts in section 5.

## 1. Owner tasks (only the blog owner can do these), in order

1. **Google Search Console** (search.google.com/search-console): add `https://randomthoughtsever.blogspot.com/`,
   submit `sitemap.xml`, then use URL Inspection > "Request indexing" for the rewritten posts, calculators first
   (about 10 a day is the practical limit). Check the Performance report monthly.
2. **Bing Webmaster Tools** (bing.com/webmasters): import the site from Search Console and submit the sitemap.
   ChatGPT search and Microsoft Copilot rely heavily on Bing's index, so this matters for AI answers.
3. **Search descriptions**: Blogger's API cannot set them. In Blogger > Posts > open a post >
   Post settings > Search description, paste the line for that URL from `seo/search_descriptions.csv`
   (every row is now a live long-form post). If that is too much work, turn off
   Settings > Meta tags > "Enable search description" so the keyword lists stop appearing at all.
   Also replace the blog-level description (Settings > Meta tags) with one sentence, e.g.
   "Practical, in-depth guides on personal finance, investing, psychology, relationships and careers."
4. **Analytics**: create a GA4 property, then put the `G-...` measurement ID in Settings > Google Analytics
   (replacing `UA-199206936-1`).
5. **Author identity (E-E-A-T)**: add your real name, photo, background and why you write on these topics to
   the About page and your Blogger profile. Google's quality guidelines and AI systems both weigh who is
   behind content, especially money and health topics. Anonymous finance advice ranks poorly.
6. **Fix the theme's random-posts widget**: in Theme > Edit HTML, find `function random_posts(json)` and change
   `for (var i = 0; i < randomposts_number; i++) {` to
   `for (var i = 0; i < Math.min(randomposts_number, (json.feed.entry || []).length); i++) {`.
   The widget currently crashes when the feed returns fewer than 5 posts.
7. **Custom domain** (optional, recommended): e.g. `blogdigest.in`, via Settings > Custom domain.
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

AI answer engines quote pages that state the answer clearly, early, and with specifics. All 153 rewrites
follow this; keep doing it for new posts:

- Open with a 2–4 sentence direct answer ("Quick answer").
- Use question-style H2/H3 headings that match how people ask (and an FAQ section).
- Include specific numbers, dates, worked examples and named sources (RBI, SEBI, IRS, studies).
- Keep facts current: add the "Updated <date>" line and revise figures when rules change
  (tax slabs, repo rate, contribution limits).
- One topic per page, and link related guides to each other (the "Related articles" box does this).
- Cover a topic cluster fully (e.g. all home loan questions) so the blog becomes the go-to source for it.

## 4. Ready-to-post shares for the calculators

Post each one where the audience is; answer replies yourself. Don't post the same text to many groups at once,
because that looks like spam and gets removed.

**LinkedIn / X**
> I built 4 free personal finance calculators: SIP with yearly step-up, home loan EMI with prepayment savings,
> FIRE number, and credit card payoff (with 18% GST on interest, which most calculators skip).
> No sign-up, works on mobile, and shows the year-by-year numbers.
> One surprise from building them: a ₹1 lakh card balance paid at the 5% minimum due takes over 30 years to clear.
> https://randomthoughtsever.blogspot.com/2026/10/credit-card-payoff-calculator-how-long.html

**Reddit (r/personalfinanceindia, r/IndiaInvestments; read each subreddit's self-promotion rules first)**
Use it to answer a real question, e.g. "should I prepay my home loan or invest?":
> Rough numbers for a ₹50L loan at 8.5% over 20 years: EMI ~₹43,400, total interest ~₹54L. An extra ₹5k a month clears it
> ~4.5 years early and saves ~₹14L of interest. Whether that beats investing depends on your post-tax return and how much
> you value being debt-free. I made a calculator that shows the year-by-year schedule if you want to try your own numbers: <link>

**Quora**: search "SIP calculator", "how much do I need to retire early in India", "how to pay off credit card debt",
and write a full answer with a worked example; link the matching calculator at the end.

**Tools that suit wider audiences** (share the post link, not just the tool):
- Sleep cycle calculator (`2021/06/how-to-sleep-fastly-and-peacefully`): Pinterest and Instagram ("what time should I sleep to wake at 6:30?").
- 30-day habit tracker with printable sheet (`2024/01/how-to-be-consistent`): Pinterest, productivity subreddits, and a New Year / new month post.
- Eisenhower matrix (`2023/10/the-importance-of-balance-on-time`): LinkedIn, with a screenshot of your own matrix.
- Debt avalanche vs snowball planner (`2023/06/methods-to-efficiently-pay-off-credit`): answers on Reddit and Quora debt questions.
- 401(k) calculator with employer match (`2021/10/a-comprehensive-guide-401k`): r/personalfinance answers about match ("am I leaving free money on the table?").
- Live currency converter (`2023/11/why-currency-value-varies-with-each`): student and NRI groups; the rupee passed 90 per dollar, which is a timely hook.

**WhatsApp / family groups**: the SIP calculator with a single line: "Try this: ₹5,000 a month from age 25 vs 35."

## 5. Measuring progress

Check monthly in Search Console and Bing Webmaster Tools: indexed pages, impressions, clicks, average
position per query, and top linking sites. Expect 3–6 months before rankings move noticeably for a
blogspot site with few links; new posts and steady links speed this up.
