# Ranking kit: the parts that need you (24 October 2026)

Everything on the site that Google can measure has been improved: depth, freshness, linked sources on all 179
articles, an editorial policy, FAQ data, internal links, faster covers, a calculators hub. What is left is the one
signal no website can give itself: **other websites linking to it**. That is where NerdWallet's lead comes from.
This kit makes it as quick as possible. Ten minutes a day is enough.

## 1. Fix robots.txt (5 minutes, once) — do this first

Blogger's default robots.txt blocks every address starting with `/search`, which includes your topic pages
(`/search/label/Personal Finance` and the rest). Those pages are the hubs that tie the site together.

Blogger → Settings → Crawlers and indexing → **Enable custom robots.txt** → paste:

```
User-agent: Mediapartners-Google
Disallow:

User-agent: *
Disallow: /search?
Allow: /search/label/
Allow: /

Sitemap: https://randomthoughtsever.blogspot.com/sitemap.xml
Sitemap: https://randomthoughtsever.blogspot.com/sitemap-pages.xml
```

This keeps search-result pages blocked (they are endless duplicates), opens the topic pages, and adds the pages
sitemap (Editorial Policy, Free Calculators). Leave "custom robots header tags" off.

## 2. Google Search Console and Bing (15 minutes, once)

- https://search.google.com/search-console → Add property → URL prefix
  `https://randomthoughtsever.blogspot.com/` → Blogger sites usually verify automatically when you are signed in
  with the blog's Google account. Then Sitemaps → submit `sitemap.xml` and `sitemap-pages.xml`.
- Use **URL Inspection → Request indexing** for the 10 most important pages: the calculators hub, the
  editorial policy and the eight strongest guides.
- https://www.bing.com/webmasters → **Import from Google Search Console** (one click; Bing's index also feeds Yahoo
  and DuckDuckGo results).
- After four weeks, Search Console shows which searches you appear for. Send me that list and I will rewrite
  titles and opening paragraphs for the queries where you rank 5–20, which is the quickest ranking win there is.

## 3. Journalist requests (10 minutes a day) — the best free source of strong links

Journalists post questions daily and link to the experts they quote. Sign up as "Blog Digest" with your email:

| Platform | Cost | Why |
|---|---|---|
| Source of Sources (sourceofsources.com) | Free | Started by HARO's founder; up to three digests a day |
| HARO (helpareporter.com, now run by Featured) | Free tier | Relaunched in 2025 |
| Featured (featured.com) | Free tier | Expert Q&A answers published on partner sites |
| Qwoted (qwoted.com) | Free tier | Business and finance outlets |
| SourceBottle (Australia), ResponseSource (UK) | Free / low cost | Regional press |
| #journorequest on X and Bluesky | Free | Requests appear here first |

How to win: answer within the first hour, only on topics we have a guide on, in 100–150 words of specific,
quotable advice with one number, and include the link to the matching guide or calculator. Never send generic or
AI-sounding answers; platforms ban them. Send me any request and I will draft the answer in minutes.

## 4. Library and education resource pages (one email each)

These guides list free personal-finance tools for students and the public. Our calculators hub is exactly what
they link to.

| Target | Page |
|---|---|
| South College Library: personal finance | https://library.south.edu/personalfinance |
| Indiana University Southeast: personal finance guide | https://libguides.ius.edu/personal_finance |
| Sullivan University Library: personal finance web resources | https://libguides.sullivan.edu/personalfinance/web |
| Library guide: smart investing, online resources | https://arlibrary.libguides.com/smart_investing/online_resources |
| Qatar National Library: business tools | https://libguides.qnl.qa/bus/11/en |

Find more by searching Google for: `"personal finance" libguides calculators`, `"financial literacy" resources
"calculators" site:.edu`, `"money management" "useful links" library`. Aim for five emails a week.

**Email template (send from your Blog Digest address, personalise the first line):**

> Subject: A free calculator collection for your personal finance guide
>
> Hello [name],
>
> I came across your [guide name] guide while looking for financial literacy resources, and found the section on
> [budgeting / credit / saving] really useful.
>
> I run Blog Digest, a free, ad-supported site with 38 calculators that run in the browser and store nothing:
> emergency fund, debt payoff, loan prepayment, retirement, inflation, pay raise after inflation and others. Each
> sits in a plain-language guide that links to its official sources (central banks, statistics offices, tax
> authorities). The full list is here: https://randomthoughtsever.blogspot.com/p/free-calculators.html
>
> If you think it would help your students, I'd be grateful if you considered adding it. Either way, thank you for
> putting the guide together.
>
> Best wishes,
> [Your name], Blog Digest

## 5. Answer questions where people ask them (15 minutes, three times a week)

Reddit, Quora and forums rank in Google themselves, and good answers send readers for years. Rules: answer the
question fully in the post itself; link only when a calculator or guide genuinely helps; never post the same text
twice; follow each community's self-promotion rules (many Reddit communities ban links from new accounts, so build
a history of helpful answers first).

Good places: Quora topics on personal finance, credit scores, retirement and careers; r/personalfinance,
r/povertyfinance, r/UKPersonalFinance, r/PersonalFinanceCanada, r/AusFinance, r/IndiaInvestments (read each
sidebar first); Stack Exchange's Personal Finance & Money site (no self-promotion; links only as supporting
references).

## 6. Make the calculators shareable (ongoing)

- Pin each calculator to Pinterest with its cover image (the social pack automation already creates the text).
- Share one guide a day on LinkedIn with a two-line takeaway, not just a link.
- When you see a site citing an out-of-date figure we have current (for example 2.8% inflation in a raise guide),
  email the author: "Your article cites X; the latest figure is Y (source). We explain it here if useful."

## 7. Be found and cited by AI assistants (ChatGPT, Perplexity, Google AI Overviews, Copilot, Claude)

**Already done on the site (October 2026):**
- Every data table now ships its rows as plain HTML, so AI crawlers that don't run JavaScript can read the figures.
- Every guide carries static FAQ data (FAQPage), and every data page carries Dataset data listing its official sources.
- Every article ends with a "Cite this page" line (title, date, address), which makes the page easy to quote.
- Each article opens with a direct "Quick answer" paragraph, the format AI assistants extract most often.

**Your steps (10 minutes):**
1. **Bing Webmaster Tools** (bing.com/webmasters): import the site from Google Search Console and submit both sitemaps.
   ChatGPT search, Microsoft Copilot and DuckDuckGo rely heavily on Bing's index; this is the single biggest AI step.
2. **Google Search Console:** submit the sitemaps (Google AI Overviews and Gemini use Google's index).
3. **Optional, robots.txt with AI crawlers named explicitly** (replaces the text in section 1; everything stays open):

```
User-agent: Mediapartners-Google
Disallow:

User-agent: GPTBot
User-agent: OAI-SearchBot
User-agent: ChatGPT-User
User-agent: PerplexityBot
User-agent: ClaudeBot
User-agent: Claude-SearchBot
User-agent: Google-Extended
User-agent: Applebot-Extended
User-agent: Bingbot
Allow: /

User-agent: *
Disallow: /search?
Allow: /search/label/
Allow: /

Sitemap: https://randomthoughtsever.blogspot.com/sitemap.xml
Sitemap: https://randomthoughtsever.blogspot.com/sitemap-pages.xml
```

4. **Get mentioned where AI tools look:** answers on Reddit, Quora and Stack Exchange, and links from news sites
   (section 3), are among the sources AI assistants cite most. One good, sourced answer that links a data page is
   worth more than many posts.

## What to expect

New sites typically need three to six months of steady links and content before rankings move a lot. Search
Console will show impressions growing first, then positions, then clicks. Keep publishing two guides a week, do the
ten minutes a day above, and send me the Search Console query report each month: I will turn it into title,
content and internal-link improvements.
