# SEO for randomthoughtsever.blogspot.com

## Audit findings (October 2026, all 164 public posts)

| Problem | Posts affected |
|---|---|
| Search descriptions are comma-separated keyword lists (Google ignores or rewrites these) | 153 |
| Images with no alt text | 857 images |
| Extra `<h1>` headings inside post bodies (the theme already uses `<h1>` for the title) | 147 |
| No internal links to other posts | 134 |
| Generic labels (Internet, world, interesting, Trends, news) on almost every post, so topics are unclear | ~140 |
| Titles with typos or weak wording ("How To Forgot Someone?", "Warm Hole", "Is Invest In Gold Is Good Idea?") | ~100 |
| "sitemap" pages show raw JavaScript code to visitors | 2 |
| Image-only posts with no text (thin content) | 3 |
| URLs Google still lists that now return 404 (`2021/10/wall-street-influence-in-world.html`, `2021/09/what-is-better-investment-bitcoin-or.html`) | 2 |
| Analytics uses Universal Analytics `UA-199206936-1`, which Google shut down in 2023, so no traffic is recorded | whole site |
| Homepage title is just "Blog Digest" and the blog description is a repeated keyword list | homepage |

## What `apply.py` changes (via the Blogger API)

For every post listed in `plan.py`:
- a clearer, keyword-focused title (the URL stays the same, so existing links keep working)
- a search description written as one readable sentence
- topic labels (Personal Finance, Investing, Psychology, Love & Relationships, Career & Workplace, ...) in place of the generic ones
- alt text on every image
- in-body `<h1>` headings changed to `<h2>`
- a "Related articles" box with four links to posts on the same topic, plus a link to the sitemap page

Also:
- `2021/06/sitemap` is rebuilt as a real HTML list of all articles grouped by topic
- `2021/06/sitemappages` (a broken duplicate) is moved back to draft
- the three quote image posts get a title, the quote as text, a short reflection and alt text

Every post is saved to `seo/backup/` before anything is changed, and `python3 seo/apply.py restore <slug>` restores a single post.

## Running it

1. Get a Blogger access token: open https://developers.google.com/oauthplayground, choose
   "Blogger API v3" > `https://www.googleapis.com/auth/blogger`, click "Authorize APIs", sign in with the
   blog owner's Google account, then click "Exchange authorization code for tokens".
2. Store the access token as the environment variable `BLOGGER_TOKEN` in this cloud environment's settings,
   then start a new session. The token is valid for one hour.
3. `python3 seo/apply.py backup`, then `python3 seo/apply.py apply --dry-run`, then `python3 seo/apply.py apply`.

## Site redesign (`theme/redesign.xml`)

A design layer over the current SoraTemplates theme. It doesn't remove any posts, pages, gadgets or URLs.
It adds:
- a homepage hero with search, stats and floating cards for the latest posts
- "Explore by topic" tiles linking to the topic label pages
- card-style post listings with full-size images
- editorial typography (Fraunces and Inter) and a sticky, blurred header
- a reading progress bar, reading-time label and drop cap on articles
- styled contents and related-articles boxes, a back-to-top button, scroll-reveal motion and a dark mode toggle

Animations are turned off for visitors whose system asks for reduced motion.

To install:
1. In Blogger, open **Theme**, click the arrow next to **Customise**, and choose **Backup** to download the current theme.
2. Choose **Edit HTML**, search for `</head>` (Ctrl+F), and paste the whole contents of `theme/redesign.xml` on the line directly above it.
3. Click **Save**.

To undo, delete the pasted block, or restore the backup from step 1.

Also recommended under **Settings > Posts**: set "Max posts shown on main page" to 10 so the post list is about as long as the sidebar.

## Publishing rewritten articles

Rewritten posts live in `rewrites/<year>_<month>_<slug>.html`. When `apply.py` finds a rewrite for a post, it uses it as the post text. It keeps the post's original images, spaced through the article, then adds the contents box, alt text and related articles.

    python3 seo/apply.py apply --only 2021/06/how-to-be-happy --dry-run
    python3 seo/apply.py apply --only 2021/06/how-to-be-happy
