# Prompt for a new Blog Digest article

Copy everything in the box into a new Claude Code session on this repository (branch `ccr-ee047f91-rbyv4x`),
change the first two lines, and send. Get a fresh Blogger token first (see the end).

```
Write and publish a new article for my Blogger blog Blog Digest (randomthoughtsever.blogspot.com).

Title: <your title here>
Topic label: <one of: Personal Finance, Investing, Economics, Psychology, Self Improvement, Love & Relationships,
Career & Workplace, Health & Wellness, Technology & AI, Education, Cryptocurrency, Countries & Living Abroad, Politics>

Work in the repo on branch ccr-ee047f91-rbyv4x. Read seo/README.md and seo/REWRITE_STATUS.md first, and follow the
same model as seo/articles/gdp-measuring-economy.html and the other files in seo/articles/.

Content
- At least 3,000 words, original, written from scratch in your own words (no copying or close paraphrase of sources).
- Voice: an experienced, measured expert explaining to an intelligent general reader. Concrete examples, real
  history and real cases, plain judgements, varied sentence length. No filler, no hype, no "In conclusion".
  Byline stays "Blog Digest"; do not claim any person's name or credentials.
- Research before writing: use web search for current figures and check them against official sources
  (for India: MoSPI, RBI, PIB; global: IMF, World Bank). Use today's latest data, give dates for figures, and
  re-check every number and calculation in your draft before publishing. List the sources to me at the end.
- India-first examples where relevant (₹, lakh/crore), with international comparisons.
- Structure, as in the existing articles: <p><em>Updated <today's date></em></p>, a "Quick answer:" paragraph,
  a two-paragraph intro, 12-16 <h2> sections with <h3> subsections where useful, a "Frequently Asked Questions"
  section (6+ questions as <h3>), "Final Thoughts", "Related Guides" with 4-5 links to my existing posts on the
  topic as [[year/month/slug]] (find them in seo/plan.py), and a one-line note on data dates / not advice.

Visuals
- 2-3 interactive or animated embeds in seo/tools/embeds/ placed with [[embed:name]], matching the existing ones:
  e.g. an animated SVG diagram (SMIL or CSS, respects prefers-reduced-motion), an animated chart that fills
  when scrolled into view, and a calculator that uses window.bdCalc (see seo/tools/calc.js and
  seo/tools/embeds/gdp-calculator.html). Must work on phones (no sideways scrolling) and in dark mode.
- A 1200x630 cover: add the post to POSTS in seo/covers/make_covers.py, render it (Fraunces variable font from
  github.com/google/fonts ofl/fraunces), commit, point COVER_BASE in seo/publish_tools.py at that commit,
  and add the post to COVERS.
- The design layer adds the five animated section illustrations automatically; nothing to do for those.

Publish
- Add the post to POSTS in seo/publish_tools.py with only the one topic label.
- Before publishing, preview it inside a live article page in Playwright on desktop and a Pixel 7: check the
  embeds render, the calculator gives correct results, no page errors, no horizontal overflow. Fix and re-check.
- Publish with: python3 seo/publish_tools.py publish articles/<name>
- Add a one-sentence description for its path to seo/theme/descriptions.json, release it (commit, write the
  commit hash to seo/theme/version.txt, commit, push), then check the live page as a new visitor on phone and
  desktop and report the URL.
- Do not save my Blogger token in any file. Commit and push all other work to ccr-ee047f91-rbyv4x.

Blogger token: <paste a fresh access token here, or set BLOGGER_TOKEN in the environment settings>
```

## Getting a Blogger token (valid for one hour)

1. Open https://developers.google.com/oauthplayground
2. Choose **Blogger API v3**, tick `https://www.googleapis.com/auth/blogger`, tap **Authorize APIs** and sign in
   with the blog's Google account.
3. Tap **Exchange authorization code for tokens** and copy the **Access token**.
4. Safer than pasting it in chat: save it as `BLOGGER_TOKEN` in the cloud environment's settings and start a
   new session. Revoke it afterwards at myaccount.google.com > Security > Third-party connections.
