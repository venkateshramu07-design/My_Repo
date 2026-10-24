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

## GDP article (published 22 October 2026)

Live at https://randomthoughtsever.blogspot.com/2026/10/what-is-gdp-its-role-in-measuring.html (Economics).
The six newest posts now carry their covers from `seo/covers/`.

## Bitcoin article (published 23 October 2026)

Live at https://randomthoughtsever.blogspot.com/2026/10/is-bitcoin-losing-its-hype-what-data.html (Cryptocurrency).
Embeds: btc-hype-cycle, btc-drawdowns, btc-risk-calculator (currency examples in CALC_EXAMPLES as `bt-cur`).
Its share description is in descriptions.json and in the theme built by `seo/theme/blogger/optimize_theme.py`,
which has to be uploaded again (Theme > Restore) for the description to reach Facebook and Google.

## Ten high-demand articles (published 24 October 2026)

1. Interest Rates Are Rising Again: 7 Money Moves to Make Now (Personal Finance): https://randomthoughtsever.blogspot.com/2026/10/interest-rates-are-rising-again-7-money.html
2. Where to Keep Your Cash When Rates Rise: Savings Accounts, T-Bills and Money Market Funds (Personal Finance): https://randomthoughtsever.blogspot.com/2026/10/where-to-keep-your-cash-when-rates-rise.html
3. Why Is the 10-Year Treasury Yield Above 5%? What It Means for Your Money (Economics): https://randomthoughtsever.blogspot.com/2026/10/why-is-10-year-treasury-yield-above-5.html
4. Jobs AI Is Replacing in 2026, and the Skills That Keep You Employed (Career & Workplace): https://randomthoughtsever.blogspot.com/2026/10/jobs-ai-is-replacing-in-2026-and-skills.html
5. Stablecoins Explained: Can You Really Earn Interest on Digital Dollars? (Cryptocurrency): https://randomthoughtsever.blogspot.com/2026/10/stablecoins-explained-can-you-really.html
6. Should You Buy a House With Mortgage Rates Above 7%? (Rent vs Buy Calculator) (Personal Finance): https://randomthoughtsever.blogspot.com/2026/10/should-you-buy-house-with-mortgage.html
7. How Prediction Markets Like Kalshi and Polymarket Work, and Are They Legal? (Economics): https://randomthoughtsever.blogspot.com/2026/10/how-prediction-markets-like-kalshi-and.html
8. How to Afford a Car in 2026 When the Average Payment Is $777 (Calculator) (Personal Finance): https://randomthoughtsever.blogspot.com/2026/10/how-to-afford-car-in-2026-when-average.html
9. How Small Investors Can Buy IPO Shares: Lessons From the SpaceX Listing (Investing): https://randomthoughtsever.blogspot.com/2026/10/how-small-investors-can-buy-ipo-shares.html
10. Is Gold Still a Safe Haven After Its Big 2026 Fall? (Investing): https://randomthoughtsever.blogspot.com/2026/10/is-gold-still-safe-haven-after-its-big.html

Each has a cover, an animated chart and a calculator; currency examples are in CALC_EXAMPLES. Their share
descriptions are in descriptions.json and the regenerated theme, which has to be uploaded again (Theme > Restore).
The Bitcoin article was republished with the corrected gold figure.

## Batch 2: ten global question articles (published 24 October 2026)

1. Why Is My Credit Score Different on Every App? Which One Do Lenders Actually Use? (Personal Finance): https://randomthoughtsever.blogspot.com/2026/10/why-is-my-credit-score-different-on.html
2. What Happens to Your Money If Your Bank or Fintech App Goes Bust? (Personal Finance): https://randomthoughtsever.blogspot.com/2026/10/what-happens-to-your-money-if-your-bank.html
3. Do You Pay Tax When You Swap One Crypto for Another? The Rules Explained (Cryptocurrency): https://randomthoughtsever.blogspot.com/2026/10/do-you-pay-tax-when-you-swap-one-crypto.html
4. What Happens to Your Retirement Savings When You Move Abroad? (Countries & Living Abroad): https://randomthoughtsever.blogspot.com/2026/10/what-happens-to-your-retirement-savings.html
5. Why Are Prices Still So High When Inflation Has Come Down? (Economics): https://randomthoughtsever.blogspot.com/2026/10/why-are-prices-still-so-high-when.html
6. How to Explain a Layoff or Career Gap in an Interview (Without Lying) (Career & Workplace): https://randomthoughtsever.blogspot.com/2026/10/how-to-explain-layoff-or-career-gap-in.html
7. What Is a Fair Pay Raise in 2026? How to Work Out Your Real Raise After Inflation (Career & Workplace): https://randomthoughtsever.blogspot.com/2026/10/what-is-fair-pay-raise-in-2026-how-to.html
8. Lump Sum or Monthly Pension: Which Should You Take? (Break-Even Calculator) (Personal Finance): https://randomthoughtsever.blogspot.com/2026/10/lump-sum-or-monthly-pension-which.html
9. What Happens to Your Online Accounts, Crypto and Money When You Die? (Technology & AI): https://randomthoughtsever.blogspot.com/2026/10/what-happens-to-your-online-accounts.html
10. Why Did My Insurance Premium Go Up When I Never Made a Claim? (Personal Finance): https://randomthoughtsever.blogspot.com/2026/10/why-did-my-insurance-premium-go-up-when.html

Calculators in this batch start in dollars and carry `data-cur` on their currency select, so the design layer
switches them to euro or pound for readers there (amounts stay the same). Embed text uses <div>, not <p>, so
AdSense auto ads do not land inside a chart, and chart bar colours are inline so dark mode keeps them.
Share descriptions are in descriptions.json and the regenerated theme, which has to be uploaded again (Theme > Restore).
