# Restoring Blog Digest on a new domain (if Blogger or Google is unavailable)

The backup lives on the `site-backup` branch of this repository (folder `site-backup/`) and can be rebuilt at any time
with `python3 seo/backup/backup_blog.py`. Everything in it is independent of Google: images are full-size originals
downloaded from Blogger's image servers, and every library, font and data file the posts use is a local copy.

## What is in the backup

| Path | Contents |
|---|---|
| `site/` | The whole blog as a static website at the same addresses as today (`/2026/10/slug.html`, `/p/page.html`), with a home page, a page per topic, `/topics.html`, `sitemap.xml`, `feed.xml` (RSS), `robots.txt`, `ads.txt`, `404.html` and `_redirects` (old `/search/label/...` addresses). All tools and calculators work, because their code is inside the posts and their libraries are in `site/files/`. |
| `wordpress.xml` | Every post, page, label and comment in WordPress export format. |
| `data/posts.json` | Every post and page: title, address, dates, labels, author, original HTML and HTML with local file paths, and its comments. |
| `data/feed-*.json` | The raw Blogger feeds for posts, pages and comments, exactly as Blogger returned them. |
| `data/file-map.json` | Original address of every downloaded file and its local name. |
| `data/blogger-*` | The live `robots.txt`, `ads.txt` and sitemaps; `homepage-snapshot.html` is the rendered home page. |
| `files/` | Every image and file used by the posts. |
| `manifest.json` | Counts, sizes and SHA-256 checksums of every file, plus lists of anything that failed (should be empty). |

The rest of this repository is also part of the backup: article sources (`seo/articles/`, `seo/countries/`), the tools
(`seo/tools/embeds/`), covers and illustrations (`seo/covers/`, `seo/images/`), open data (`seo/data/`), the design
layer (`seo/theme/`) and the Blogger theme (`seo/theme/blog-digest-theme.xml`).

## Option 1: static hosting (fastest, free, about 15 minutes)

Works on Cloudflare Pages, Netlify, GitHub Pages or any web host.

1. Buy or choose the new domain.
2. In `site-backup/site/`, replace `NEWDOMAIN` in `sitemap.xml`, `robots.txt` and `feed.xml` with the domain:
   `grep -rl NEWDOMAIN site | xargs sed -i 's/NEWDOMAIN/www.example.com/g'`
3. Upload the `site/` folder:
   - **Cloudflare Pages:** Create project, then Direct Upload, then drag in the `site` folder. `_redirects` and `404.html` work automatically.
   - **Netlify:** drag the folder onto app.netlify.com/drop. Same.
   - **GitHub Pages:** push the folder's contents to a repository and turn on Pages.
4. Point the domain at the host (the host shows the DNS records to add).
5. Submit `https://<domain>/sitemap.xml` in Google Search Console and Bing Webmaster Tools.

Addresses stay the same, so every internal link, bookmark and backlink path still works on the new domain.

## Option 2: WordPress

1. Install WordPress on the new domain.
2. Tools, then Import, then WordPress: upload `wordpress.xml`. Choose "Download and import file attachments" if the old
   images are still online; otherwise upload `site/files/` to the server and run a search-and-replace on post content
   from `/files/` to `https://<domain>/files/`.
3. Settings, then Permalinks, then Custom structure: `/%year%/%monthnum%/%postname%.html` keeps Blogger's addresses.
   Each post also stores its original path in the `blogger_permalink` field.

## If Blogger is still running

You can keep the same content and simply move to a custom domain: Blogger, Settings, Custom domain. Blogger then
redirects the old blogspot address to the new domain automatically, which keeps search rankings.

## AdSense on the new domain

Add the new domain in AdSense (Sites, Add site). `ads.txt` is already in the static site. Ads only show after AdSense
approves the domain.

## Keeping the backup current

Run `python3 seo/backup/backup_blog.py` after publishing new posts; it replaces `site-backup/` completely and checks
that every post, page, comment and file was saved (see `manifest.json`).
