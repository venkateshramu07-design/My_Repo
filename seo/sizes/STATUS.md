# Exact-size pages: published 30 October 2026

## Why
DataForSEO shows 0 Google rankings on record for the site. Small tool sites (pdf.pi7.org, photoresizer.net) win India with one page
per exact need. Target searches (India, monthly volume and KD) were checked against their SERPs: the top 10 is full of tiny sites and
YouTube, Instagram and Facebook posts. Examples:
- compress pdf to 200kb: 246k, KD 20
- resize image to 20kb: 110k, KD 0
- signature resize: 90.5k, KD 0
- kb to mb: 165k, KD 4
- photo resize for pan card: 40.5k, KD 0

Bank FD pages were dropped because banks and big fintech sites own those SERPs.

## Done
- tools/templates/image-to-kb.html: any photo to N KB.
  - Batches, HEIC, pixels or cm, min and max KB, 1 KB = 1,000 bytes (safe), dpi written, padding.
  - Tested.
- tools/templates/pdf-to-kb.html: PDF or photos to N KB.
  - Lossless squeeze first, then a 1-bit black-and-white XObject (crisp, about 16 KB per page at 150 dpi), then JPEG pages.
  - Tested.
- tools/embeds/kb-mb-converter.html: shows both 1,000 and 1,024 conventions.
- sizes/build_sizes.py: generator. Writes articles/<slug>.html, embeds, pages.json, descriptions.json and focus_keywords.json.
- sizes/pages_data.py: content for 8 image pages (10, 15, 20, 30, 50, 100, 200, 500 KB) and 8 PDF pages (50 KB to 2 MB).
- sizes/measurements.json: browser test results used in the "Our test" tables.
  - The PDF part is partial; re-run test/m2.js after the final code.

## Published (30 October 2026)
- All 22 pages are live and the Free Tools and Free Calculators hubs list the new tools.
- Five existing tool posts link into the group, and the design layer was released (commit 130804b) with the descriptions.
- Live browser check: descriptions, covers and tools all work.
- Republish: BLOGGER_TOKEN=... python3 seo/sizes/publish_sizes.py. Pre-publish audit: python3 seo/sizes/audit_sizes.py.

## Next
- About 2 weeks after publishing, check rankings for the 22 target keywords with DataForSEO (about $0.05).
- U3: retarget existing articles to winnable long-tail keywords and add answer sections.
