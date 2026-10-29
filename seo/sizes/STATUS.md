# Exact-size pages: where the work stopped (30 October 2026)

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

## Next
1. Add EXTRA pages in pages_data.py:
   - jpg-to-pdf-under-200kb
   - kb-to-mb-converter
   - signature-resize (preset: gray, 10-20 KB)
   - pan-card-photo-resize (Protean: photo up to 20 KB and signature up to 10 KB at 200 dpi; 2.5x3.5 cm = 197x276 px)
   - resize-image-in-cm (data-dim=cm)
   - image-and-pdf-size-tools (hub linking all)
2. Re-run the PDF measurements with the final pdf-to-kb code (bw list now starts at 300 dpi), then check each PDF page's test_note against the numbers.
3. Run build_sizes.py, register SIZE_POSTS in publish_tools.py (in TOOL_POSTS so unpublished links fall back to text), add covers,
   descriptions and focus keywords, and run seo_audit.py.
4. Browser-check the pages, then publish twice (to resolve the links) with a fresh Blogger token, and release the layer.
5. U3: link the existing tool posts (exam resizer, PDF toolkit, passport photo) into this cluster; retarget long-tail keywords.

DataForSEO spend so far: about $1.2 of $51.
