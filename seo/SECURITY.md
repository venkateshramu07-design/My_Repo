# Site security (October 2026)

## Done

- **No secrets in the repository.** The full git history was scanned for Blogger/Google OAuth tokens, API keys, GitHub
  tokens and private keys: none. Blogger tokens are only ever passed as the `BLOGGER_TOKEN` environment variable and
  expire within an hour. Commit authors use no-reply addresses.
- **Locked third-party code.** Every library the tools load (pdf-lib, pdf.js and its worker, JSZip, qrcode-generator,
  heic-to) is pinned to an exact version and loaded with a Subresource Integrity hash, so the browser refuses it if a
  CDN ever serves anything different.
- **Everything else from our own code is pinned to a commit.** Images, data and the design layer load from
  `cdn.jsdelivr.net/gh/...@<commit>`, which can't change after publishing.
- **Anti-framing (clickjacking).** Blogger sends no `X-Frame-Options` header. The theme already navigates framed copies
  away; the design layer also hides the page and shows only a link when another site frames it (Blogger's editor,
  Google and Translate are allowed).
- **Content Security Policy (partial).** `object-src 'none'; base-uri 'self'; upgrade-insecure-requests`, added by the
  layer. A full script policy isn't possible on Blogger because AdSense and Blogger load scripts from many hosts.
- **Safer links.** Every link that opens a new tab gets `rel="noopener"`, so the opened page can't control this one.
- **No user data leaves the device.** All tools run in the browser; the only optional network call (password breach
  check) uses k-anonymity and sends 5 characters of a hash.
- **Output escaping.** Everything the layer and tools put on the page from the URL, the search box, feeds or user input
  is HTML-escaped.
- **HTTPS.** `http://` redirects to `https://` (301), and Blogger sends `X-Content-Type-Options: nosniff`.
- **GitHub link removed from the menu.** "Open data" now goes to the downloads section of the compare-countries page.

## Hiding this repository and its owner from the site (one step left)

Every image, download and the design layer are served from jsDelivr's GitHub CDN, whose addresses always contain
`owner/repository`. No page can avoid that while the files live in this repository, and only the account owner can
create a neutral host. Steps:

1. Create a free GitHub organization with a neutral name (github.com/account/organizations/new, Free plan), for example
   `blogdigest-cdn`, and in it a public repository `assets` with a README.
2. Install the Claude GitHub app on that organization (github.com/apps/claude) so the session can push to it.
3. Then `python3 seo/tools/migrate_assets.py blogdigest-cdn/assets <clone>` copies the assets, rewrites every address,
   and writes `seo/theme/loader-snippet.html`. Every post is republished, the loader snippet replaces the old one in
   the theme (Blogger: Theme, Edit HTML), and this repository can then be made **private**.

Do not make this repository private before the migration: the live site loads its files from here.

## Account safety (owner)

- Turn on two-step verification for the Google account that owns the blog and AdSense, and for GitHub.
- Review Google account access (myaccount.google.com/permissions) and remove apps you don't use.
- The Blogger tokens pasted into chat expire after about an hour; you can also revoke them at
  myaccount.google.com/permissions (OAuth 2.0 Playground).
- In Blogger settings, keep "HTTPS redirect" on and comment moderation on.
