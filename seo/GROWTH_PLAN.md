# Organic growth plan (October 2026 to April 2027)

## Diagnosis

- **Content quality is no longer the constraint.** There are 193 articles. 39 of them are deep guides with tools,
  data tables, sources, FAQ data and citations.
- **The constraint is discovery.** The site has almost no inbound links and is barely indexed. A new blogspot site
  with no links is crawled rarely and ranks for little, however good the pages are.
- **Organic traffic therefore has to come from three places,** in this order:
  1. Long-tail search pages that are easy to rank: country-level money facts, where competition is weak.
  2. Linkable assets that other sites cite and embed: open data, embeddable tables, share buttons.
  3. Freshness signals that bring crawlers back: monthly data updates and "updated" dates.

## Workstreams

| # | Workstream | Why it brings organic visitors | Status |
|---|---|---|---|
| 1 | **Open data**: every data table published as CSV (via GitHub and jsDelivr), with a "Download the data" link on each page | Journalists, students and bloggers cite and link a source they can download. GitHub pages are crawled quickly, so the CSVs act as discovery paths back to the site | **Live**: 11 CSVs in `seo/data/`, download button on every data guide |
| 2 | **Embeddable tables**: a one-line "Embed this table" code on every data page; each embed carries a credit link | Every blog that embeds a table creates a permanent backlink and referral traffic | **Live**: `seo/widgets/table.js`, "Embed this table" on every data guide, tested on an outside page |
| 3 | **Share bar on every article**: WhatsApp, X, Facebook, LinkedIn, Reddit, Telegram, email, copy link | Each reader becomes a distributor at no cost. WhatsApp and Telegram drive a lot of sharing in India and emerging markets | **Live** on all articles |
| 4 | **Country money-facts pages**: one page per country (about 25) combining all 11 datasets (minimum wage, take-home pay, mortgage rate, electricity, petrol, pension age and amount, deposit insurance, inheritance tax, salary outlook, retirement visa), with rankings against other countries | Targets thousands of low-competition searches such as "Germany minimum wage 2026", "retirement age in Japan" and "Spain mortgage rate 2026". Each page has unique data and rankings, so it is useful rather than thin | **Built**: 35 pages in `seo/countries/` with covers, FAQ data and rankings; publish with `seo/countries/publish_countries.py` |
| 5 | **Compare-countries hub**: an index of all country pages plus side-by-side comparisons | Links the cluster together and ranks for "compare countries cost of living" style searches | **Built**: compare-countries hub, linked from all 11 data guides |
| 6 | **Monthly data refresh**: petrol (weekly official data), mortgage rates and electricity updated monthly, with the date shown | Fresh dates and changing numbers make search engines and AI tools revisit and prefer the page | Monthly, from November 2026 |
| 7 | **Search titles and snippets**: year and key number in every title; quick answers written in the shape of featured snippets | Higher click-through from the same rankings | Done for 39 guides; applied to every new page |
| 8 | **Topic clusters and internal links**: hubs, autolinks, related guides and country pages all linking to each other | Spreads authority from any page that earns links to the rest of the site | Ongoing |
| 9 | **AI readability**: static tables, FAQ and Dataset data, and a "Cite this page" line | AI answer engines cite pages they can read. Each citation is a visit, and builds the brand | Done |
| 10 | **Seasonal content calendar**: November (tax-year planning, Black Friday credit card debt), December (budgets, minimum wage changes in January), January (new tax rates, pay rises), April (UK tax year, US tax deadline) | Publishing two to four weeks before the searches peak lets new pages rank in time | Calendar below |

## Content calendar (2 to 3 new pages a week)

- **November 2026:** country pages wave 1 (US, UK, Canada, Australia, Germany, France, Spain, Italy, Netherlands,
  Ireland, Japan, South Korea); "Minimum wage increases for January 2027"; "Credit card interest rates by
  country"; "Black Friday buy now, pay later: what it really costs".
- **December 2026:** country pages wave 2 (Portugal, Greece, Poland, Belgium, Austria, Switzerland, Mexico, Brazil,
  India, China, Thailand, Philippines, South Africa); "2027 income tax changes by country"; "Savings interest rates
  by country"; "Cost of a Christmas dinner by country".
- **January 2027:** refresh every data table with January rates (minimum wages, pensions and tax thresholds change on
  1 January); "Pay rises 2027: what employers actually gave"; "Average rent by country".
- **February–April 2027:** "Tax-free allowances by country"; "Childcare costs by country"; "Student loan interest by
  country"; "Inflation by country (monthly)"; "Best savings accounts by country (guide)".

## Measures of success

| Measure | Now | 3 months | 6 months |
|---|---|---|---|
| Indexed pages (Google and Bing) | very few | 150+ | 230+ |
| Organic visitors a day | under 20 | 150 | 600+ |
| Referring domains | about 0 | 10 | 40 |
| Pages with downloads or embeds | 0 | 11 | 25+ |
| AdSense RPM | n/a | $2–5 | $3–8 |

## Not used, because it damages accounts or rankings

Buying links, traffic exchanges, bots, clicking ads, auto-generated thin pages, and copying content from other
sites. Each of these is detected by Google or AdSense and costs far more than it gains.
