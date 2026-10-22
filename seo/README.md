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

A design layer over the current SoraTemplates theme. It only restyles the theme: posts, pages, gadgets and
URLs are left as they are. Version 2 adds:
- a monogram logo, a sticky blurred header, a one-row mobile header with a dropdown menu, and a search overlay
  (search icon or the `/` key)
- a homepage hero with search, stats and a "Start here" panel linking the money, mind and career hubs
- "Explore by topic" tiles with icons, a full-width featured first post, and a "Browse all articles" banner
- topic-coloured cover art for posts without an image (cards, ticker, widgets, related posts), in place of
  the theme's grey "no image" picture
- articles: an "Updated" pill, a quick-answer callout, a drop cap on the first real paragraph, correct list
  numbering, FAQ question badges, restyled share buttons, tags and newer/older links
- tidier widgets: sentence-case titles, no "0 Comments" lines, a cleaner Recent Posts list; the empty
  Report Abuse and "Powered by Blogger" boxes are hidden, and Recent Posts is hidden on the homepage
  (it repeats the main list)
- reading progress bar, reading time, back-to-top button, scroll-reveal motion and a dark mode toggle
- engagement: instant search results as you type; on articles a reading bar (current section, contents panel,
  next article), an "Up next" card at 60% read and a "Keep reading" card halfway through; a "Continue reading"
  link on the homepage and "Read" chips on finished posts (history is kept in the reader's browser only);
  topic page headers that link to the matching "Start here" guide; a 404 page with search, guides and latest
  articles; lazy-loaded article images and external links that open in a new tab

Animations are turned off for visitors whose system asks for reduced motion.

The Blogger API cannot edit themes, so this step is done by hand in Blogger:
1. Open **Theme**, click the arrow next to **Customise**, and choose **Backup** to download the current theme.
2. Choose **Edit HTML** and search for `bd-redesign` (Ctrl+F). If the earlier version is there, select from
   the `<link href='https://fonts.googleapis.com' rel='preconnect'/>` line above it down to the closing
   `//]]></script>` and delete it.
3. Search for `</head>` and paste the whole contents of `theme/redesign.xml` on the line directly above it.
4. Click **Save**.

To undo, delete the pasted block, or restore the backup from step 1.

**Easier option (works from a phone): gadget.** `theme/bd-redesign.js` is the same layer as a single script,
built with `python3 seo/theme/build_loader.py` after editing `redesign.xml`. It also removes the first version's
output, so the old block in the theme can stay. In Blogger, open **Layout**, click **Add a Gadget** at the top of
the sidebar (it must sit above the theme's random-posts widgets), choose **HTML/JavaScript**, leave the title
empty, paste the contents of `theme/gadget.html` and click **Save**. The gadget hides its own box. To undo,
delete the gadget.

`gadget.html` loads the script from the exact commit named in `theme/version.txt`. jsDelivr's branch URLs are
cached for up to 7 days in browsers and its branch copies can lag by days, so commit URLs are the only reliable
ones. To release a change:
1. Edit `redesign.xml`, run `build_loader.py`, commit and push.
2. Put that commit's full hash in `theme/version.txt` (`git rev-parse HEAD > seo/theme/version.txt`), commit and push.
Readers pick up the new version on their next page view after GitHub's 5-minute cache.

## Blog Digest theme (`theme/blog-digest-theme.xml`)

A complete Blogger theme of its own, replacing the SoraTemplates theme and its required credit line. It renders
the header, menus, post cards, articles, comments, pager and footer, loads AdSense (Auto ads), Google Analytics 4
(G-2P00N18249) and the design layer directly, so no gadget is needed. Article structured data comes from the
design layer.

To install: Theme > arrow next to Customise > Backup (download the current theme first), then Restore > upload
`blog-digest-theme.xml`. To undo, Restore the backup. Gadgets from the old theme's sidebar and footer are not
kept; the footer has an empty section for new ones.

## Publishing rewritten articles

Rewritten posts live in `rewrites/<year>_<month>_<slug>.html`. When `apply.py` finds a rewrite for a post, it uses it as the post text. It keeps the post's original images, spaced through the article, then adds the contents box, alt text and related articles.

    python3 seo/apply.py apply --only 2021/06/how-to-be-happy --dry-run
    python3 seo/apply.py apply --only 2021/06/how-to-be-happy

## Interactive tools

Standalone calculator posts live in `tools/*.html` and are published with `python3 seo/publish_tools.py publish`.
Widgets embedded inside existing posts live in `tools/embeds/*.html`; `EMBEDS` and `CTAS` in `apply.py` choose
which posts get them. They are inserted before the first section whenever a post is republished with
`publish_rewrites.py`. Non-ASCII characters inside scripts are written as `\u` escapes because Blogger
rewrites them otherwise.
