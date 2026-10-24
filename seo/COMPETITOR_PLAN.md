# Competitor analysis and winning plan (24 October 2026)

## Who we compete with

We searched the questions our articles answer and looked at who ranks for them. No single site owns every topic.
Five types of site keep appearing, and we measured one representative page from each against our page on the same
question (phone, same browser, same day):

| Competitor | Type | Their page | Our page |
|---|---|---|---|
| NerdWallet | Market leader, calculators | Emergency fund calculator | Emergency fund guide |
| SmartAsset | Calculators plus programmatic pages ("$150k lump sum or $1,200 a month?") | Lump sum vs annuity | Lump sum or monthly pension |
| Gerald "Learn" (joingerald.com) | Long-tail question articles; appeared most often in our results | What is a good raise | Fair pay raise in 2026 |
| Indeed Career Guide | Careers | How to explain a career gap | Layoff or career gap |
| Koinly | Crypto tax specialist | Crypto-to-crypto tax | Crypto swap tax |

Investopedia also ranks for many of our topics but blocks automated access, so it is not in the measurements.
Blog Digest does not yet appear in the top results for any of these searches.

## Scorecard

| Measure | Blog Digest | Competitors |
|---|---|---|
| Words per article | 3,000–3,500 | 750–1,000 |
| Sections (H2) | 16–18 | 1–4 |
| Quick answer, contents list, FAQ | All | Mostly none |
| Interactive tools per article | 1–2 (every article) | NerdWallet 1, others 0 |
| Up-to-date data | Data to September/October 2026 | Gerald's raise guide still uses 2.8% inflation (actual: 3.7%) |
| Structured data | BlogPosting, breadcrumbs | Similar; Gerald none |
| **Links to sources** | **0** | NerdWallet 12, Gerald 12, Koinly 2 |
| **Internal links in the article** | **6–9** | NerdWallet 41, Koinly 12 |
| **Named people, review process** | **None shown, no editorial policy** | NerdWallet and Koinly name authors; NerdWallet has editorial standards |
| **Ad slots per page** | **13–16 (AdSense auto ads)** | Almost none (they earn from affiliate links) |
| **Time to main content (LCP)** | **2.1–2.4 s** | 1.2–2.0 s |
| **Site authority** | **Blogspot subdomain, few backlinks** | Strong domains with years of links |

We already win clearly on depth, freshness, tools, global coverage and answer-first structure. We lose on trust
signals, internal linking, ad load, speed and above all authority.

## The plan

### 1. Trust signals (done or ready, no action needed from you)
- **Linked sources on every recent article.** 25 articles now end with a "Sources" section linking to the official
  pages behind their figures (central banks, statistics offices, tax authorities, regulators). Addresses were checked.
  Source list: `seo/article_sources.py`.
- **Trust line under every headline:** "6 official sources · How we research", linking to the sources and the policy.
- **Editorial policy page** (`seo/pages/editorial-policy.html`): how we choose topics, research, write, use AI,
  update and correct, and stay independent of advertisers.
- **About page rewritten to 1,020 words** (`seo/pages/about-us.html`): what we cover, who it is for, how guides are
  made, calculators, independence, history, promise to readers, contact.
- We do not invent people or credentials. The policy says plainly that AI tools help research and draft, and that
  every claim is checked against the linked sources.

### 2. Internal linking (done in the design layer)
- The first mention of 45 topics we have guides on ("emergency fund", "credit score", "inflation", "mortgage rate",
  "annuity", "burnout" and more) now links to that guide, up to six links per article, on all ~190 posts, including
  the old ones. Map: `seo/theme/autolinks.json`. Combined with Related Guides, "More in this topic" and the inline
  "Read next" cards, every article links to 12+ others.

### 3. Rich results (done in the design layer)
- FAQ structured data is generated from each article's "Frequently Asked Questions" section, so search engines can
  read the questions and answers directly.

### 4. Speed and ads (partly done; one setting for you)
- Cover images are now marked to load first (`fetchpriority="high"`), applied as articles are republished.
- **You:** in AdSense, go to Ads → By site → randomthoughtsever.blogspot.com → Auto ads, and move the **Ad load**
  slider two steps towards "fewer ads", and turn off vignette ads. 13–16 ad slots per page is far more than any
  competitor shows; fewer, better-placed ads usually keep most of the revenue while improving speed, time on page and
  rankings. Review revenue after 30 days.

### 5. Authority (the biggest gap; needs you, takes months)
- **Custom domain** (about $10–15 a year, e.g. blogdigest-guides.com): Blogger supports it with free HTTPS and
  redirects every blogspot address automatically, so no links are lost. A real domain is a trust signal for readers,
  advertisers and search engines, and every backlink then builds an asset you own. Settings → Publishing → Custom domain.
- **Google Search Console and Bing Webmaster Tools:** verify the site and submit `/sitemap.xml`, so new articles are
  found within days and we can see which searches we appear for.
- **Earn links:** our calculators and data charts are the most linkable things on the site. Answer questions on
  Reddit, Quora and forums where a calculator genuinely helps; respond to journalist requests on Qwoted and Featured;
  offer our charts to bloggers who cite them. Aim for 5–10 genuine links a month.

### 6. Content engine (ongoing)
- Two new in-depth guides a week on questions with high demand and weak answers, using the same standards.
- A monthly refresh of the figures that change (inflation, interest rates, yields, gold, mortgage rates) in the
  articles that use them, with the updated date shown.
- Topic clusters: each new guide links to and from its hub ("Start here") and 3–5 related guides.
- Where SmartAsset wins with programmatic pages, we answer with calculators people can use for their own numbers
  rather than thin pages per number, which Google's spam policies now penalise.

## What "300% more effective" means here

No one can honestly guarantee beating NerdWallet or Investopedia in search, or promise that a competitor can never
win a ranking: their domains have years of authority. What we can do is be measurably better on everything we control
and close the gaps we can't fix overnight. Targets for the next six months:

| Measure | Now | Target | Best competitor |
|---|---|---|---|
| Depth (words) | 3,000+ | 3,000+ | ~1,000 (we are 3×) |
| Linked sources per article | 4–11 (from 0) | 5+ | 12 |
| Internal links per article | 12+ (from 6–9) | 15+ | 41 |
| Tools per article | 1–2 | 1–2 | 0–1 |
| Time to main content | 2.1–2.4 s | under 1.8 s | 1.2 s |
| Ad slots per page | 13–16 | 6–8 | 0 |
| Referring domains | few | +30 | thousands |
| Search clicks | baseline from Search Console | 3× baseline | — |

### 7. Built since the first plan (ready to publish with the next token)
- **Free calculators hub** (`seo/pages/free-calculators.html`, page `/p/free-calculators.html`): all 38 tools on the
  site, grouped by topic, each linked to its guide. Linked from the footer and the homepage tool bar. This is the
  page outreach should point to; no competitor we measured has a hub like it.
- **WebP covers:** every cover now also exists as 800 px and 1200 px WebP files (about 15 KB instead of 57 KB on
  phones), served with srcset and loaded first. The cover is the largest element on article pages, so this is the
  biggest single speed gain available to us.
- **New privacy policy** (`seo/pages/privacy-policy.html`) with the disclosures AdSense requires: Google's advertising
  cookies, third-party vendors, opt-out through Google's Ads Settings and aboutads.info, Analytics, the newsletter and
  the calculators' browser storage.
- **You:** in AdSense, go to Privacy & messaging → European regulations and publish the consent message (Google's
  certified consent tool, free). Google requires it for ads to serve fully to visitors in the EEA, UK and Switzerland,
  and the privacy policy describes it.

### 8. Full-site audit (196 pages, 24 October 2026)
- Median article length 3,183 words; no images without alt text; no duplicate titles; no broken internal links
  (all 179 internal targets load; the only 404 is a draft "sitemap_pages" post nothing links to).
- **Thin pages fixed:** three image-only "thought" posts (51–61 words) get companion articles of 700–900 words under
  their original images (`seo/thoughts/`), the 37-word contact page is rewritten (`seo/pages/contact-us.html`),
  and the About and Privacy pages are replaced. Thin pages are the most common reason AdSense rejects a site for
  "low-value content", so this matters before any domain move or review.
- **Remaining gap:** about 170 older posts still cite no outside sources. Next step: add linked Sources sections to
  them in batches, starting with the most-visited finance posts.

## Status (24 October 2026, live)

- **Live and verified on phone:** linked sources on 25 articles; trust line under every headline; FAQ structured
  data; up to six internal links per article on all posts; WebP covers; About (1,037 words), Contact, Privacy,
  Editorial Policy and Free Calculators pages; three image-only posts expanded; descriptions for all of them.
- **Needs you:** AdSense consent message (Privacy & messaging → European regulations), AdSense ad load, Search
  Console and Bing verification, and later the custom domain.
- **Done:** all 154 older posts now end with "Sources and further reading" (2–4 checked links each, matched to the
  post's subject), so every article on the site links to outside sources.
- **Next:** two new in-depth guides a week; monthly data refresh; speed work on the theme's blocking scripts.

## Data pages (24 October 2026, live)

Strategy change: instead of more guides on crowded questions, we now publish pages that compile official data
nobody else puts in one place. Each has an interactive table, a full guide and linked official sources, and each is
the kind of page journalists and bloggers cite.

| Page | Coverage | Sources |
|---|---|---|
| [Salary increase forecasts 2027](https://randomthoughtsever.blogspot.com/2026/10/salary-increase-forecasts-2027-by.html) | 29 countries, 6 employer surveys | 14 |
| [Deposit insurance limits 2026](https://randomthoughtsever.blogspot.com/2026/10/deposit-insurance-limits-by-country.html) | 30 countries and the EU, local currency and US$ | 28 |
| [Petrol price rises since the Iran war](https://randomthoughtsever.blogspot.com/2026/10/petrol-prices-since-iran-war-how-much.html) | 29 countries on official weekly data, plus 6 | 15 |
| [Retirement age by country 2026](https://randomthoughtsever.blogspot.com/2026/10/retirement-age-by-country-2026-state.html) | 39 countries, earliest age and legislated changes | 13 |

Datasets: `seo/data_*.py` / `seo/data_petrol_2026.json`. The petrol table should be refreshed monthly from the EU
Weekly Oil Bulletin, EIA and DESNZ files; the others when limits or ages change. These are the pages to offer in
outreach (OUTREACH.md, section 3) for journalist requests on fuel prices, pensions and bank safety.
