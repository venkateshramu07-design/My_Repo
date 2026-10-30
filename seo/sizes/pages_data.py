# Content for the exact-size pages built by build_sizes.py. Facts about portals come from the sources in SRC; the
# "our test" numbers come from measurements.json, recorded by running the tools in a browser on real files.
UPDATED = "30 October 2026"

SRC = {
 "ssc": ("Staff Selection Commission: official website and notices", "https://ssc.gov.in/"),
 "pw_ssc": ("Physics Wallah: SSC CGL photo and signature size 2026", "https://www.pw.live/ssc/exams/ssc-cgl-photo-and-signature-size-2026"),
 "ibps": ("IBPS: official website and recruitment notifications", "https://www.ibps.in/"),
 "upsc": ("UPSC online applications (upsconline.nic.in)", "https://upsconline.nic.in/"),
 "visionias": ("Vision IAS: UPSC photo and signature rules 2026", "https://blog.visionias.in/blog/featured/upsc-new-photo-signature-rules-2026-dos-and-donts"),
 "ssbcrack": ("SSBCrack: how to apply for UPSC CDS online (document sizes)", "https://ssbcrackexams.com/how-to-apply-upsc-cds-exam-online-2/"),
 "neet": ("NTA: NEET (UG) official website", "https://neet.nta.nic.in/"),
 "pw_neet": ("Physics Wallah: NEET 2026 photo and signature guidelines", "https://www.pw.live/neet/exams/neet-2026-photo-and-signature-guideline"),
 "pw_neetdocs": ("Physics Wallah: documents required for the NEET UG 2026 application form", "https://www.pw.live/vp-blogs/exams/documents-required-for-neet-ug-2026-application-form"),
 "protean": ("Protean (NSDL): online PAN application with digital signature, scanning specifications", "https://tin.tin.nsdl.com/pan/form49Adsc.html"),
 "pan_photo": ("Passport-Photo.Online: PAN card photo size", "https://passport-photo.online/en-in/pan-card-photo"),
 "state": ("US Department of State: digital image requirements for visa photos", "https://travel.state.gov/content/travel/en/us-visas/visa-information-resources/photos/digital-image-requirements.html"),
 "oci": ("OCI Services, Ministry of Home Affairs", "https://ociservices.gov.in/"),
 "eoibogota": ("Embassy of India, Bogota: guidelines for uploading photograph, signature and documents", "https://www.eoibogota.gov.in/content/Guidelines-for-uploading-of-ICAO-photograph-signature-and-Documents-which-are-mandatory-at-applicants-level_1.pdf"),
 "evisa": ("Embassy of India, Paris: e-Visa photo and passport upload requirements", "https://eoiparis.gov.in/page/e-visa"),
 "gst_kk": ("Kanakkupillai: document size for GST registration", "https://www.kanakkupillai.com/learn/document-size-for-gst-registration/"),
 "gst_pob": ("StudyCafe: GSTN raises upload size for place-of-business documents", "https://studycafe.in/gstn-portal-update-increase-in-document-upload-size-for-place-of-business-in-new-registration-327614.html"),
 "epfo": ("Housing.com: EPFO e-nomination, step by step", "https://housing.com/news/epfo-e-nomination"),
 "nsp": ("National Scholarship Portal: standard operating procedure (document upload sizes)", "https://moridhalcollege.in/scholar/NSP_SOP.pdf"),
 "uidai": ("UIDAI: commonly asked questions on online document update", "https://uidai.gov.in/images/doc_update/en/Commonly_Asked_Questions.pdf"),
 "ircc": ("IRCC Help Centre: file size limit for documents uploaded to your account", "https://ircc.canada.ca/english/helpcentre/answer.asp?qnum=1123"),
 "nist": ("NIST: prefixes for binary multiples (KB, KiB, MB, MiB)", "https://physics.nist.gov/cuu/Units/binary.html"),
 "mdn_blob": ("MDN Web Docs: HTMLCanvasElement.toBlob() and JPEG quality", "https://developer.mozilla.org/en-US/docs/Web/API/HTMLCanvasElement/toBlob"),
 "pdflib": ("pdf-lib: the open-source library the PDF tool uses to rebuild files", "https://pdf-lib.js.org/"),
 "pdfjs": ("PDF.js (Mozilla): the open-source library the PDF tool uses to read pages", "https://mozilla.github.io/pdf.js/"),
 "ms_units": ("Microsoft Learn: how Windows reports file and disk sizes", "https://learn.microsoft.com/en-us/troubleshoot/windows-client/backup-and-storage/hard-disk-drive-capacity"),
}

PHOTO_SIZES = ["resize-image-to-10kb", "resize-image-to-15kb", "resize-image-to-20kb", "resize-image-to-30kb", "resize-image-to-50kb",
               "resize-image-to-100kb", "resize-image-to-200kb", "resize-image-to-500kb"]
PDF_SIZES = ["compress-pdf-to-50kb", "compress-pdf-to-100kb", "compress-pdf-to-150kb", "compress-pdf-to-200kb", "compress-pdf-to-300kb",
             "compress-pdf-to-500kb", "compress-pdf-to-1mb", "compress-pdf-to-2mb"]


def near(slug, pool, n=2):
    i = pool.index(slug)
    return [pool[j] for j in (i - 1, i + 1, i - 2, i + 2) if 0 <= j < len(pool)][:n]


def img_related(slug):
    return near(slug, PHOTO_SIZES, 3) + ["kb-to-mb-converter", "image-and-pdf-size-tools"]


def pdf_related(slug):
    return near(slug, PDF_SIZES, 3) + ["jpg-to-pdf-under-200kb", "image-and-pdf-size-tools"]


def steps_img(k, extra=""):
    return [
        "<strong>Choose your photo</strong> in the tool above (or several at once). iPhone HEIC photos and PNG screenshots are fine; the result is always a real JPG.",
        "<strong>Check the limit:</strong> the tool is set to %s. If the form also gives a minimum, such as &ldquo;%s&rdquo;, type it in the second box so the file is not too small either." % (k[0], k[1]),
        "<strong>Set the dimensions only if the form gives them.</strong> Leave &ldquo;Keep shape&rdquo; selected otherwise: the tool shrinks the photo just enough to stay sharp. %s" % extra,
        "<strong>Download</strong> the JPG (or all of them as a .zip) and upload it. The result line shows the exact KB, the size in pixels and the JPEG quality it used.",
    ]


COMMON_IMG_SECTION = ("How the Tool Picks the Size",
    "<p>A JPEG's file size depends on two things: how many pixels it has and how strongly it is compressed (the &ldquo;quality&rdquo; setting). "
    "Most compressors only lower the quality, which is why their 20 KB results look blocky. This tool searches for the highest quality that fits and, "
    "if that would fall below about 60%, makes the photo slightly smaller in pixels instead, because a smaller sharp photo looks far better on a form than a large blurry one. "
    "It also writes the resolution (dpi) into the file, which some government portals check, and counts 1 KB as 1,000 bytes so the file passes whichever way the portal measures.</p>")

IMAGE = []


def add_img(**p):
    p.setdefault("labels", [])
    p["embed"] = p["slug"].replace("resize-image-to-", "image-to-") + "-tool"
    p["related"] = p.get("related") or img_related(p["slug"])
    IMAGE.append(p)


add_img(slug="resize-image-to-10kb", kb=10, kw="resize image to 10kb",
 title="Resize Image to 10KB Online Free (Photo or Signature Under 10 KB)",
 tool_title="Resize image to 10 KB",
 desc="Resize any photo or signature to under 10 KB in your browser. Keeps it sharp, sets pixels or cm, works on iPhone photos. Nothing is uploaded.",
 quick="Choose the image in the free tool below and download it: it comes out as a JPG under 10 KB, usually around 400 pixels wide for a photo or 800 pixels wide for a black-and-white signature, still sharp enough to read. A 10 KB limit is mostly asked for signatures, for example the Protean (NSDL) online PAN form, so tick &ldquo;Black and white&rdquo; for a signature and the tool can keep more pixels.",
 intro="Ten kilobytes is very small: about one three-hundredth of a typical phone photo. Squeezing a photo that far with a normal compressor ruins it, which is why portals often reject the result as &ldquo;not clear&rdquo;. The tool below finds the best balance of pixel size and quality that fits, on your own device, without uploading your signature or photo anywhere.",
 how_h2="How to Resize an Image to 10KB in 4 Steps",
 steps=steps_img(("10 KB", "between 4 KB and 10 KB"), "For signatures, also tick &ldquo;Black and white&rdquo;: grey paper turns into plain white, which saves a lot of space."),
 uses_intro="A file this small is almost always a signature. Forms that set a minimum of 10 KB (such as SSC and IBPS signatures, 10&ndash;20 KB) will reject a file under 10 KB, so read both numbers before you shrink anything.",
 uses=[("Protean (NSDL) online PAN application", "Signature, 200 dpi, black and white", "Up to 10 KB"),
       ("NTA (NEET, JEE Main, CUET)", "Signature", "4&ndash;30 KB"),
       ("SSC and IBPS forms", "Signature (minimum)", "At least 10 KB, at most 20 KB"),
       ("Protean (NSDL) online PAN application", "Photograph, 200 dpi, colour", "Up to 20 KB")],
 test_note="So a 10 KB photo is roughly {cw} &times; {ch} pixels: fine for a small passport-style photo on screen, not for printing. If a form wants a 10 KB <em>photo</em> with fixed dimensions, set them in pixels and the tool will only lower the quality.",
 sections=[("Signature Under 10 KB: What Works",
   "<p>Sign with a black gel pen on plain white paper, photograph it flat in daylight and crop close to the ink. In our test, a signature photographed on paper (380 KB) came out at <strong>9.7 KB and 838 &times; 377 pixels</strong> in black and white, which is sharp enough to zoom into. In colour, the same file had to drop to about half the width, because the grey paper texture takes up space.</p>"
   "<p>If the form fixes the signature at, say, 140 &times; 60 pixels, choose &ldquo;Set width and height in pixels&rdquo;. At that size a clean signature is often only 3&ndash;6 KB even at top quality, and the tool can add an invisible blank block to reach a minimum such as 4 KB.</p>"),
   COMMON_IMG_SECTION],
 faq_title="Resize Image to 10KB",
 faq=[("How do I reduce a photo to 10 KB?", "Use the tool above: choose the photo, keep the limit at 10 KB and download. On a phone without the tool, you would have to crop the photo, then shrink it to about 400 pixels wide in an editing app and save it at medium quality, checking the size each time."),
      ("Is 10 KB enough for a photo?", "For a small on-screen photo, yes: about 400 &times; 300 pixels at good quality, or around 270 &times; 350 for a passport-style crop. It is too small to print. Check that the form really asks for 10 KB for the photo and not only for the signature."),
      ("How many pixels is a 10 KB image?", "It depends on the picture. In our tests, the detailed camera photo fitted at about 270 &times; 220 pixels, the smoother 12-megapixel image at about 400 &times; 300, and a black-and-white signature at about 840 &times; 380, all at 65&ndash;70% JPEG quality."),
      ("Why does the portal say my 10 KB file is too large?", "Windows counts 1 KB as 1,024 bytes; some portals count 1,000. A file Windows shows as 9.9 KB can be 10,140 bytes, which a 10,000-byte limit rejects. This tool keeps files under 10,000 bytes, so they pass either way."),
      ("Can I resize a signature to 10 KB without losing quality?", "Mostly, yes: tick &ldquo;Black and white&rdquo; and crop tightly. A clean black signature on white compresses very well, so it keeps far more detail than a colour photo of the same size.")],
 src=["protean", "neet", "pw_neet", "ssc", "ibps", "nist", "mdn_blob"])

add_img(slug="resize-image-to-15kb", kb=15, kw="resize image to 15kb",
 title="Resize Image to 15KB Online Free (Photo Under 15 KB, No Upload)",
 tool_title="Resize image to 15 KB",
 desc="Resize a photo or signature to under 15 KB, sharp and in JPG format. Set pixels, cm or a minimum KB. Works on iPhone photos; nothing is uploaded.",
 quick="Add your photo to the tool below and download the JPG: it will be under 15 KB, typically 340&ndash;600 pixels wide for a photo at 68&ndash;73% quality. A 15 KB target sits safely inside the most common signature range (10&ndash;20 KB for SSC and IBPS) and under the 20 KB limit for the photo on the online PAN form.",
 intro="Fifteen kilobytes is a popular choice because it is safe: it is above the 10 KB minimum many forms set for signatures and below the 20 KB maximum, so it passes both. The tool does the work on your own device, so your photo or signature is never uploaded to a server.",
 how_h2="How to Resize an Image to 15KB in 4 Steps",
 steps=steps_img(("15 KB", "between 10 KB and 20 KB"), "For a signature, tick &ldquo;Black and white&rdquo; too."),
 uses_intro="These are common places where a file of about 15 KB is the safe answer. Aim for the middle of a range rather than the edge: it leaves room if the portal measures KB differently.",
 uses=[("SSC (CGL, CHSL, MTS, GD)", "Signature", "10&ndash;20 KB"),
       ("IBPS, SBI, RRB, LIC", "Signature, 140 &times; 60 px", "10&ndash;20 KB"),
       ("Protean (NSDL) online PAN application", "Photograph, 200 dpi", "Up to 20 KB"),
       ("NTA (NEET, JEE Main, CUET)", "Signature", "4&ndash;30 KB")],
 test_note="A 15 KB photo is therefore about {cw} &times; {ch} pixels from a camera and {pw} &times; {phh} from the smoother 12 MP image: enough for a clear head-and-shoulders photo on any online form.",
 sections=[("Photo or Signature at 15 KB With Exact Pixels",
   "<p>Many bank and exam forms give exact pixels as well as KB. For an IBPS signature (140 &times; 60 pixels, 10&ndash;20 KB), choose &ldquo;Set width and height in pixels&rdquo;, type 140 and 60, set the minimum to 10 and the maximum to 20. At only 8,400 pixels, a clean signature is usually smaller than 10 KB even at the highest quality, so the tool adds an invisible blank block inside the file to reach 10 KB. In our test it produced a 10.1 KB file at 140 &times; 60 pixels and 95% quality, which the portal accepts.</p>"),
   COMMON_IMG_SECTION],
 faq_title="Resize Image to 15KB",
 faq=[("How do I make a photo 15 KB?", "Choose the photo in the tool above, keep the limit at 15 KB and download. If the form also fixes the pixels, select &ldquo;Set width and height in pixels&rdquo; and enter them first."),
      ("What is the size of a 15 KB photo in pixels?", "There is no fixed answer, because detail compresses differently, but at good quality it is roughly 340 &times; 280 pixels for a camera photo and up to 600 &times; 450 for a simpler image."),
      ("Is 15 KB good for a signature?", "Yes. It is in the middle of the common 10&ndash;20 KB signature range, so it passes forms that measure KB either as 1,000 or 1,024 bytes."),
      ("How do I resize a photo to between 10 and 20 KB?", "Set the maximum to 20 and the minimum to 10 in the tool. It aims as close to 20 KB as it can for sharpness and pads the file if the picture is too simple to reach 10 KB."),
      ("Does resizing to 15 KB reduce the quality?", "Some, unavoidably, but the tool keeps JPEG quality at 60% or higher by reducing the pixel size a little instead, which looks much better than a heavily compressed full-size photo.")],
 src=["ssc", "pw_ssc", "ibps", "protean", "neet", "nist", "mdn_blob"])

add_img(slug="resize-image-to-20kb", kb=20, kw="resize image to 20kb",
 title="Resize Image to 20KB Online Free (Compress Photo to 20 KB)",
 tool_title="Resize image to 20 KB",
 desc="Resize or compress any photo to under 20 KB as a sharp JPG. Set pixels, cm or a 10-20 KB range for signatures. Free, no upload, no sign-up.",
 quick="Choose your photo in the free tool below and download the JPG: it will be under 20 KB at the best quality that fits, about 400&ndash;800 pixels wide for a typical photo. You need 20 KB most often for the photo on the online PAN form (up to 20 KB) and for SSC and IBPS signatures (10&ndash;20 KB); for exam photos with a 20&ndash;50 KB range, 20 KB is the <em>minimum</em>, so set the tool to 50 KB instead.",
 intro="&ldquo;Reduce to 20 KB&rdquo; is one of the most common instructions on Indian application forms, and one of the most common reasons uploads fail: a phone photo is 3&ndash;5 MB, more than 150 times too big. The tool below compresses it on your own device, keeping it as sharp as 20 KB allows, and never uploads your photo.",
 how_h2="How to Resize an Image to 20KB in 4 Steps",
 steps=steps_img(("20 KB", "10 to 20 KB"), "For a passport-style photo, &ldquo;Set width and height in cm&rdquo; with 3.5 &times; 4.5 cm at 200 dpi gives the usual 276 &times; 354 pixels."),
 uses_intro="Read the form carefully: 20 KB is a maximum for some uploads and a minimum for others.",
 uses=[("Protean (NSDL) online PAN application", "Photograph, 200 dpi, colour", "Up to 20 KB"),
       ("SSC (CGL, CHSL, MTS, GD)", "Signature", "10&ndash;20 KB"),
       ("IBPS, SBI, RRB, LIC", "Signature, 140 &times; 60 px", "10&ndash;20 KB"),
       ("SSC, IBPS (photo)", "Photograph", "20&ndash;50 KB: 20 KB is the minimum"),
       ("UPSC", "Photograph", "20&ndash;200 KB: 20 KB is the minimum")],
 test_note="So a sharp 20 KB photo is about {cw} &times; {ch} pixels from a camera and {pw} &times; {phh} from the smoother 12 MP image. That is plenty for a form photo, which the portal shows at a few hundred pixels anyway.",
 sections=[("How to Reduce a Photo to 20 KB Without a Tool",
   "<p><strong>On Windows (Paint):</strong> open the photo, choose Resize, select Pixels, untick nothing and type about 600 for the width; save as JPEG and check the size in the file's Properties. If it is still above 20 KB, resize to 450 pixels and save again. Paint does not let you set the JPEG quality, so this takes a few tries.</p>"
   "<p><strong>On a phone:</strong> crop the photo to the part you need first (cropping is the biggest saving), then share it to yourself on WhatsApp as a photo, which compresses it to about 100&ndash;300 KB; that still needs a second step to reach 20 KB. The tool above does all of this in one go and shows the exact size before you download.</p>"),
   COMMON_IMG_SECTION],
 faq_title="Resize Image to 20KB",
 faq=[("How do I reduce a photo to 20 KB?", "Choose it in the tool above with the limit at 20 KB and download. The tool keeps the photo's shape, finds the highest quality that fits under 20 KB and shows the final size, pixels and quality."),
      ("What is a 20 KB photo size in pixels?", "At good quality, about 400 &times; 330 pixels for a detailed camera photo and up to 800 &times; 600 for a simpler image. For a passport-style photo, 276 &times; 354 pixels (3.5 &times; 4.5 cm at 200 dpi) fits under 20 KB easily."),
      ("How do I make a photo between 20 KB and 50 KB?", "Set the maximum to 50 and the minimum to 20, or use the [[post:articles/resize-image-to-50kb|50 KB version of this tool]]. That is the photo range for SSC, IBPS, SBI and RRB forms."),
      ("How do I resize a photo to 20 KB in Paint?", "Open it in Paint, choose Resize, pick Pixels, set the width to about 600 (keep the aspect ratio ticked), save as JPEG and check the size; repeat with a smaller width until it is under 20 KB."),
      ("Why is my 20 KB photo rejected?", "Usually because the form also wants exact pixels or a minimum size, or because it counts 1 KB as 1,000 bytes. Set the pixels in the tool if the form gives them; the tool always stays under 20,000 bytes.")],
 src=["protean", "pan_photo", "ssc", "pw_ssc", "ibps", "upsc", "nist", "mdn_blob"])

add_img(slug="resize-image-to-30kb", kb=30, kw="resize image to 30kb",
 title="Resize Image to 30KB Online Free (Photo or Signature Under 30 KB)",
 tool_title="Resize image to 30 KB",
 desc="Resize a photo or signature to under 30 KB as a sharp JPG, in your browser. Set pixels, cm or a minimum. Free, no upload, no watermark.",
 quick="Add the photo to the tool below and download: the JPG will be under 30 KB, roughly 580&ndash;1,000 pixels wide for a photo at 60&ndash;75% quality. A 30 KB limit is common for signatures, such as NTA forms for NEET, JEE Main and CUET (4&ndash;30 KB), and it is a comfortable size for a passport-style photo on most online forms.",
 intro="Thirty kilobytes is enough for a clear passport-style photo and more than enough for a signature, so most of the work is making sure the file lands under the limit without being blurred. The tool does it on your device; nothing is uploaded.",
 how_h2="How to Resize an Image to 30KB in 4 Steps",
 steps=steps_img(("30 KB", "4 to 30 KB"), "For an NTA signature, tick &ldquo;Black and white&rdquo; as well."),
 uses_intro="Common forms where 30 KB is the limit, or a safe size inside the allowed range:",
 uses=[("NTA (NEET, JEE Main, CUET)", "Signature", "4&ndash;30 KB"),
       ("SSC and IBPS", "Photograph", "20&ndash;50 KB (30 KB is safely inside)"),
       ("IBPS, SBI, RRB", "Left thumb impression", "20&ndash;50 KB"),
       ("Many state recruitment portals", "Photograph or signature", "Often 20&ndash;50 KB; check the notice")],
 test_note="So a 30 KB photo is about {cw} &times; {ch} pixels from a camera photo and {pw} &times; {phh} from the smoother 12 MP image: sharp enough for any on-screen use.",
 sections=[("30 KB vs 20 KB vs 50 KB: Which Should I Pick?",
   "<p>Always follow the form. When it gives a range, aim a little below the top: a 20&ndash;50 KB photo is safest at about 40&ndash;45 KB, and a 4&ndash;30 KB signature at about 25 KB. Being right at the maximum risks a rejection if the portal counts 1 KB as 1,000 bytes, while being close to the minimum risks &ldquo;image not clear&rdquo;. The tool keeps every file under the limit in bytes, so a 30 KB setting gives a file of at most 30,000 bytes.</p>"),
   COMMON_IMG_SECTION],
 faq_title="Resize Image to 30KB",
 faq=[("How do I make a photo 30 KB?", "Choose it in the tool above with the limit at 30 KB and download. The result line shows the final KB, pixels and quality."),
      ("What is a 30 KB photo?", "Simply a JPG file of 30 kilobytes or less, about 30,000 bytes. At good quality that is roughly 580 &times; 480 pixels for a detailed photo or close to 1,000 pixels wide for a simpler one."),
      ("How do I resize a signature to 30 KB for NEET?", "Tick &ldquo;Black and white&rdquo;, keep the limit at 30 KB and download. NTA accepts 4&ndash;30 KB for the signature, so the result passes."),
      ("Can I reduce several photos to 30 KB at once?", "Yes. Choose them all together; each one is compressed separately and you can download them one by one or as a .zip."),
      ("Will a 30 KB photo look blurry?", "Not if it is the right pixel size. The tool shrinks the pixel size slightly instead of crushing the quality, so the photo stays sharp.")],
 src=["neet", "pw_neet", "ssc", "ibps", "nist", "mdn_blob"])

add_img(slug="resize-image-to-50kb", kb=50, kw="resize image to 50kb",
 title="Resize Image to 50KB Online Free (Photo Between 20 and 50 KB)",
 tool_title="Resize image to 50 KB",
 desc="Resize or compress a photo to under 50 KB, or into the 20-50 KB range exam forms ask for. Sharp JPG, pixels or cm, no upload.",
 quick="Choose the photo in the tool below and download it: the JPG will be under 50 KB, about 750&ndash;1,300 pixels wide at 60&ndash;75% quality. For SSC, IBPS, SBI and RRB photos the allowed range is 20&ndash;50 KB, and the tool is already set to that: a maximum of 50 KB and a minimum of 20 KB.",
 intro="Fifty kilobytes is the standard upper limit for the photograph on Indian government exam and bank recruitment forms, and its minimum (usually 20 KB) catches people out just as often as the maximum. The tool below hits the range on your own device, without uploading your photo.",
 how_h2="How to Resize an Image to 50KB in 4 Steps",
 steps=steps_img(("50 KB, with a minimum of 20 KB", "20 to 50 KB"), "For an SSC photo, choose cm and 3.5 &times; 4.5; for IBPS, choose pixels and 200 &times; 230."),
 uses_intro="The most common forms with a 50 KB limit:",
 uses=[("SSC (CGL, CHSL, MTS, GD, CPO)", "Photograph, 3.5 &times; 4.5 cm", "20&ndash;50 KB"),
       ("IBPS, SBI, RRB, LIC", "Photograph, 200 &times; 230 px", "20&ndash;50 KB"),
       ("IBPS, SBI, RRB", "Left thumb impression, 240 &times; 240 px", "20&ndash;50 KB"),
       ("Protean (NSDL) PAN", "Photograph", "Up to 20 KB, so use the 20 KB tool instead")],
 preset={"min": 20},
 test_note="So 50 KB allows about {cw} &times; {ch} pixels for a camera photo and {pw} &times; {phh} for the smoother 12 MP image, far more than a form needs. With exact dimensions such as 200 &times; 230 pixels the photo usually fits at 90% quality or more.",
 sections=[("Photo in the 20&ndash;50 KB Range at Exact Pixels",
   "<p>In our test with &ldquo;Set width and height in cm&rdquo; at 3.5 &times; 4.5 cm and 200 dpi (the SSC photo size), the tool produced a 276 &times; 354 pixel photo of <strong>40.9 KB at 95% quality</strong> from the camera photo, and 20.5 KB from a simpler image. Both are inside 20&ndash;50 KB, so both upload first time. If a picture is so simple that it stays under 20 KB even at top quality, the tool adds an invisible blank block to reach the minimum, without changing the photo.</p>"),
   COMMON_IMG_SECTION],
 faq_title="Resize Image to 50KB",
 faq=[("How do I reduce a photo to 50 KB?", "Choose it in the tool above and download; the limit is already 50 KB with a 20 KB minimum. Set the pixels or centimetres first if your form gives them."),
      ("How do I make a photo between 20 KB and 50 KB?", "Keep the tool's settings (maximum 50, minimum 20). It aims close to 50 KB for sharpness and pads the file if it would come out under 20 KB."),
      ("What is a 50 KB photo size in pixels?", "At good quality, about 750 &times; 615 pixels for a detailed camera photo and up to 1,300 pixels wide for a simpler one. Exam photos are much smaller (200 &times; 230 or 276 &times; 354 pixels), so they fit at high quality."),
      ("How do I compress an image to 50 KB on a phone?", "Open this page on your phone, tap Choose photos, pick the photo from your gallery or take one, and download the result. It works on Android and iPhone, including HEIC photos."),
      ("Why does the SSC site reject my 50 KB photo?", "Check the minimum (20 KB) and the dimensions, and make sure the file is a real JPG. Also note that many SSC forms now capture a live photo, so read the current notice.")],
 src=["ssc", "pw_ssc", "ibps", "protean", "nist", "mdn_blob"])

add_img(slug="resize-image-to-100kb", kb=100, kw="resize image to 100kb",
 title="Resize Image to 100KB Online Free (Compress JPG to 100 KB)",
 tool_title="Resize image to 100 KB",
 desc="Resize or compress any photo to under 100 KB as a sharp JPG, in your browser. For GST, EPFO and exam forms. No upload, no sign-up.",
 quick="Add your photo to the tool below and download it as a JPG under 100 KB, which at good quality is about 1,000&ndash;1,800 pixels wide: sharp enough for any online form. Typical 100 KB uploads are the photo for GST registration, the nominee photo for EPFO e-nomination and the handwritten declaration on IBPS forms (50&ndash;100 KB).",
 intro="A 100 KB limit is generous: a photo that size can still be 1,000 pixels or more across. The trouble is that phones save photos at 2&ndash;5 MB, so it still needs to shrink 20 to 50 times. The tool does that on your device, keeping as much detail as fits, and never uploads the photo.",
 how_h2="How to Resize an Image to 100KB in 4 Steps",
 steps=steps_img(("100 KB", "50 to 100 KB"), ""),
 uses_intro="Where you commonly meet a 100 KB limit:",
 uses=[("GST registration (GST portal)", "Photograph of proprietor, partners, directors or signatory (JPEG)", "Up to 100 KB"),
       ("EPFO e-nomination", "Nominee photograph", "Up to 100 KB"),
       ("IBPS, SBI, RRB", "Handwritten declaration, 800 &times; 400 px", "50&ndash;100 KB"),
       ("UPSC", "Signature", "Up to 100 KB")],
 test_note="So 100 KB holds about {cw} &times; {ch} pixels from a camera photo and {pw} &times; {phh} from the smoother 12 MP image: high enough to print a small photo.",
 sections=[("JPG, PNG or HEIC to 100 KB",
   "<p>The tool accepts JPG, PNG, WebP and iPhone HEIC files and always saves a JPG, because almost every form asks for JPG or JPEG (they are the same format). A PNG screenshot of a document can be several megabytes; as a JPG under 100 KB it stays perfectly readable. If you need a PDF instead, use the [[post:articles/jpg-to-pdf-under-200kb|JPG to PDF tool]] and set 100 KB.</p>"),
   COMMON_IMG_SECTION],
 faq_title="Resize Image to 100KB",
 faq=[("How do I compress a JPG to 100 KB?", "Choose it in the tool above and download. The limit is set to 100 KB; the result line shows the final size, pixels and quality."),
      ("What is the size of a 100 KB photo in pixels?", "At good quality, roughly 1,000 &times; 870 pixels for a detailed camera photo and up to 1,800 pixels wide for a simpler image."),
      ("How do I upload a photo for GST registration?", "The GST portal asks for a JPEG of up to 100 KB for each photograph. Compress it with the tool above, then upload it in the Promoters or Authorised Signatory tab."),
      ("How do I reduce a photo to 100 KB for EPFO?", "Use the tool with the limit at 100 KB and upload the JPG as the nominee photo in the e-nomination step on the EPFO member portal."),
      ("Is 100 KB a good size for a photo?", "For online forms, yes. It keeps a photo sharp on screen and loads fast. For printing, use 500 KB or more.")],
 src=["gst_kk", "epfo", "ibps", "upsc", "visionias", "nist", "mdn_blob"])

add_img(slug="resize-image-to-200kb", kb=200, kw="resize image to 200kb",
 title="Resize Image to 200KB Online Free (Compress Photo to 200 KB)",
 tool_title="Resize image to 200 KB",
 desc="Compress or resize a photo to under 200 KB as a high-quality JPG. For UPSC, NEET, OCI and visa forms. Free, works offline, nothing uploaded.",
 quick="Choose your photo in the tool below and download the JPG: under 200 KB, a photo can stay 1,500&ndash;2,500 pixels wide at about 70% quality, so it looks as good as the original on screen. You need 200 KB for the photo on UPSC (20&ndash;200 KB) and NTA forms such as NEET (10&ndash;200 KB), for OCI photos, and for documents on many National Scholarship Portal schemes.",
 intro="Two hundred kilobytes is a high-quality limit: the trick is only to get a 4 MB phone photo below it without wrecking the colours. The tool does it on your device and never uploads the photo.",
 how_h2="How to Resize an Image to 200KB in 4 Steps",
 steps=steps_img(("200 KB", "20 to 200 KB"), "OCI and US visa photos must be square: choose pixels and 600 &times; 600."),
 uses_intro="Common forms with a 200 KB limit:",
 uses=[("UPSC (Civil Services, NDA, CDS)", "Photograph", "20&ndash;200 KB"),
       ("NTA (NEET, JEE Main, CUET)", "Photograph", "10&ndash;200 KB"),
       ("OCI (Overseas Citizen of India)", "Square photograph", "Up to 200 KB at most missions"),
       ("National Scholarship Portal", "Documents such as a bank passbook", "Up to 200 KB on many schemes"),
       ("US visa (DS-160)", "Square photograph, 600 &times; 600 px", "Up to 240 KB (200 KB is safely under)")],
 test_note="So 200 KB keeps about {cw} &times; {ch} pixels from a camera photo and {pw} &times; {phh} from the smoother 12-megapixel image: more than any form displays.",
 sections=[("Photo vs Document at 200 KB",
   "<p>A photo of a person compresses easily. A photo of a <em>document</em> (marksheet, certificate, passbook) has sharp text and paper texture, which makes the file bigger. For documents, crop to the edges of the paper and tick &ldquo;Black and white&rdquo; if the form allows it: the paper turns white and the text stays crisp. If the form asks for a PDF rather than a JPG, use the [[post:articles/compress-pdf-to-200kb|compress PDF to 200 KB tool]].</p>"),
   COMMON_IMG_SECTION],
 faq_title="Resize Image to 200KB",
 faq=[("How do I reduce a photo to 200 KB?", "Choose it in the tool above with the limit at 200 KB and download. The result keeps the shape and as many pixels as fit at good quality."),
      ("What is a 200 KB photo in pixels?", "At about 70% quality, roughly 1,500 &times; 1,230 pixels for a detailed camera photo and 2,500 pixels wide for a simpler one."),
      ("How do I resize a photo to 200 KB for UPSC?", "UPSC accepts a JPG photo of 20&ndash;200 KB. Use the tool with the limit at 200 KB; check the pixel size in the current notification and set it if one is given."),
      ("How do I compress a photo to 200 KB on iPhone?", "Open this page in Safari, tap Choose photos and pick the photo (HEIC is fine). The tool converts it to JPG and compresses it to 200 KB on the phone."),
      ("Is 200 KB the same as 0.2 MB?", "Almost: 200 KB is 0.2 MB if you count 1,000 KB per MB, and about 0.195 MB if you count 1,024. Our [[post:articles/kb-to-mb-converter|KB to MB converter]] shows both.")],
 src=["upsc", "visionias", "neet", "pw_neet", "oci", "nsp", "state", "nist", "mdn_blob"])

add_img(slug="resize-image-to-500kb", kb=500, kw="resize image to 500kb",
 title="Resize Image to 500KB Online Free (Compress JPG Under 500 KB)",
 tool_title="Resize image to 500 KB",
 desc="Compress a large photo to under 500 KB while keeping it sharp and full size. For GST, OCI and e-visa uploads. Free, private, no upload.",
 quick="Choose the photo in the tool below and download a JPG under 500 KB. At that size a photo usually keeps 2,400&ndash;4,000 pixels across at 65&ndash;80% quality, so it looks just like the original. 500 KB limits appear on GST registration for place-of-business proof, on some Indian missions' OCI forms and wherever a portal wants a &ldquo;high-quality&rdquo; photo.",
 intro="Half a megabyte is plenty for a photo, and the challenge is only getting a 5&ndash;10 MB phone picture below it. The tool keeps the full size where it can and lowers the quality only as far as needed. Everything happens on your device.",
 how_h2="How to Resize an Image to 500KB in 4 Steps",
 steps=steps_img(("500 KB", "100 to 500 KB"), ""),
 uses_intro="Where a 500 KB limit is common:",
 uses=[("GST registration", "Principal or additional place-of-business proof", "Up to 500 KB"),
       ("OCI (some Indian missions)", "Photograph", "Up to 500 KB"),
       ("Indian e-Visa", "Photograph (JPEG)", "10 KB&ndash;1 MB (500 KB is safely inside)"),
       ("Job portals and email", "Photos attached to applications", "Smaller files open faster for the reader")],
 test_note="Our 6-megapixel camera photo kept {cw} &times; {ch} pixels; the smoother 12-megapixel image kept its full {pw} &times; {phh} at 65% quality.",
 sections=[("500 KB or 1 MB?",
   "<p>If a form allows 1 MB, a 500 KB photo still passes and uploads twice as fast on a slow connection. Going lower than you need has no benefit, though: set the tool to the form's actual limit for the best quality. The chips under the size box switch instantly between 10 KB and 1 MB and re-compress every photo you have added.</p>"),
   COMMON_IMG_SECTION],
 faq_title="Resize Image to 500KB",
 faq=[("How do I compress a photo to 500 KB?", "Add it to the tool above with the limit at 500 KB and download the JPG."),
      ("Is 500 KB high quality?", "Yes, for almost any photo: a 500 KB JPEG can keep a 12-megapixel photo at its full size with only light compression."),
      ("How do I reduce an image from MB to KB?", "Choose the image and set the KB you need; the tool handles a 10 MB photo the same way as a 1 MB one. Use the chips for common sizes."),
      ("How many KB is half an MB?", "500 KB if you count 1,000 KB per MB (phones and Macs), or 512 KB if you count 1,024 (Windows)."),
      ("Can I keep the original pixel size?", "Usually, yes, at 500 KB. The result line shows the pixels; if they are smaller than the original, the photo needed it to stay sharp under 500 KB.")],
 src=["gst_pob", "gst_kk", "oci", "eoibogota", "evisa", "nist", "mdn_blob"])


# ---------------------------------------------------------------- PDF pages
PDF = []
PDF_TESTS = [("scan3-colour", ("3-page certificate scan, 300 dpi (9.8 MB)", "Keep colour")),
             ("scan3-bw", ("3-page certificate scan, 300 dpi (9.8 MB)", "Black and white")),
             ("digital5", ("5-page text PDF with a photo (3.9 MB)", "Keep colour")),
             ("digital5-bw", ("5-page text PDF with a photo (3.9 MB)", "Black and white"))]
PDF_TEST_INTRO = ("We ran the tool above on two files: a three-page certificate scanned at 300 dpi with paper texture (9.8 MB), the typical "
                  "&ldquo;my scan is too big&rdquo; case, and a five-page PDF of typed text with one photo (3.9 MB). These are the results it produced:")

COMMON_PDF_SECTION = ("What Happens to My PDF When It Is Compressed?",
    "<p>The tool first tries a <strong>lossless</strong> squeeze: it rebuilds the PDF with compressed internal structures, which keeps text selectable and changes nothing you can see. "
    "That is often enough for PDFs made on a computer. If the file is still too big, usually because it is a scan or contains photos, it redraws every page as an image: "
    "in colour as a JPEG, using the highest resolution and quality that fit, or as a pure black-and-white image, "
    "which is how fax and document scanners store text and is often ten times smaller than colour. In <strong>Auto</strong> mode the tool keeps colour when it stays clear and uses black and white when colour pages would be blurry. The page size stays the same (A4 stays A4), it prints the same, "
    "and the preview shows the first pages so you can check every line before you download. Nothing is uploaded: the PDF is read and rebuilt inside your browser by the "
    "open-source PDF.js and pdf-lib libraries.</p>")


def steps_pdf(k, extra=""):
    return [
        "<strong>Choose your PDF</strong> in the tool above. You can also choose photos of the pages (JPG or PNG); they become one PDF, one photo per A4 page.",
        "<strong>Check the size:</strong> the tool is set to %s. Type a different number, or tap one of the size chips, if your form says otherwise." % k,
        "<strong>Leave Auto selected</strong> unless you need something specific. %s" % (extra or "Auto keeps colour when it stays clear and switches to black and white when colour pages would be blurry. Choose Keep colour for photos, coloured stamps and seals, or Black and white for the sharpest text."),
        "<strong>Check the preview, then download.</strong> The result shows the new size, the number of pages and how it was saved.",
    ]


def add_pdf(**p):
    p.setdefault("labels", [])
    p["embed"] = p["slug"].replace("compress-pdf-to-", "pdf-to-") + "-tool"
    p["related"] = p.get("related") or pdf_related(p["slug"])
    p.setdefault("tests", PDF_TESTS)
    p.setdefault("test_intro", PDF_TEST_INTRO)
    PDF.append(p)


add_pdf(slug="compress-pdf-to-50kb", kb=50, kw="compress pdf to 50kb",
 title="Compress PDF to 50KB Online Free (Sharp Text, No Upload)",
 tool_title="Compress PDF to 50 KB",
 desc="Compress a PDF or scanned document to under 50 KB with sharp, readable text. Works in your browser; the file is never uploaded. No sign-up, no watermark.",
 quick="Choose the PDF in the tool below, select <strong>Black and white</strong> and download: a one- to three-page document fits in 50 KB with text still sharp (our 3-page, 9.8 MB scan came out at 48 KB, saved at 150 dpi). In colour, 50 KB is only enough for about one page at low resolution, so use colour only for a single page with a coloured stamp or photo.",
 intro="Fifty kilobytes is a tiny allowance for a PDF: a single phone scan is often 1&ndash;3 MB. Most online compressors reach it by blurring the page until it is unreadable. The tool below keeps text crisp by saving pages in pure black and white, and it does everything on your own device.",
 how_h2="How to Compress a PDF to 50KB in 4 Steps",
 steps=steps_pdf("50 KB", "At 50 KB Auto will almost always choose black and white; choose Keep colour only for a single page with a photo or a coloured seal."),
 uses_intro="A 50 KB PDF is rarely a maximum on its own; more often it is a size you need to stay inside a 10&ndash;300 KB range, or to send a document over a weak connection. Note that NEET documents have a 50 KB <em>minimum</em>:",
 uses=[("Indian e-Visa", "Passport bio page (PDF)", "10&ndash;300 KB"),
       ("UPSC", "Photo identity document (PDF)", "20&ndash;300 KB"),
       ("NTA (NEET UG)", "Category, PwBD and other certificates (PDF)", "50&ndash;300 KB: do not go under 50 KB"),
       ("Email and messaging on slow networks", "Any document", "Small files send instantly")],
 test_note="In black and white, 50 KB held three full A4 pages at 150 dpi, which is as sharp as an office scanner's default. Colour pages had to drop to 50 dpi, where small print starts to blur.",
 sections=[("How Many Pages Fit in 50 KB?",
   "<p>From our tests, a typed or printed A4 page in pure black and white takes about 10&ndash;16 KB at 150 dpi, so <strong>3 to 4 pages fit in 50 KB</strong> with sharp text. Dense pages (small print on every line) take more; mostly blank pages take less. In colour, one A4 page needs around 50 KB even at a low resolution. For more pages, check whether the form accepts a larger file or several uploads, or split the PDF with our free [[post:articles/merge-split-pdf-without-uploading|PDF splitter]].</p>"),
   COMMON_PDF_SECTION],
 faq_title="Compress PDF to 50KB",
 faq=[("How do I compress a PDF to 50 KB?", "Choose it in the tool above, select Black and white and download. The result line shows the final size and how it was saved."),
      ("Can I compress a PDF to 50 KB without losing quality?", "For a typed or computer-made PDF, sometimes: the tool tries a lossless squeeze first. For scans, some loss is unavoidable, but pure black and white keeps text sharp at 150 dpi."),
      ("Why is my PDF still more than 50 KB?", "It probably has many pages or photos. Try Black and white, remove pages you do not need, or crop the scan to the paper's edges before making the PDF."),
      ("How do I convert a JPG to a PDF under 50 KB?", "Choose the JPG in the tool above instead of a PDF; it becomes a one-page PDF under the size you set. Black and white works best for documents."),
      ("Is it safe to compress a certificate online?", "With this tool, yes: the PDF is processed in your browser and never uploaded. Many other sites send your file to their servers.")],
 src=["evisa", "upsc", "ssbcrack", "neet", "pw_neetdocs", "pdfjs", "pdflib"])

add_pdf(slug="compress-pdf-to-100kb", kb=100, kw="compress pdf to 100kb",
 title="Compress PDF to 100KB Online Free (Scans and Certificates)",
 tool_title="Compress PDF to 100 KB",
 desc="Reduce a PDF or scanned certificate to under 100 KB with readable text. Private: processed in your browser, never uploaded. Free, no limit, no watermark.",
 quick="Choose the PDF in the tool below and download it: in <strong>Black and white</strong> a 3&ndash;6 page document fits in 100 KB at 150&ndash;200 dpi (our 3-page, 9.8 MB certificate scan came out at 87 KB at 200 dpi). In colour, 100 KB holds one or two pages at medium resolution. 100 KB sits comfortably inside the 10&ndash;300 KB and 20&ndash;300 KB ranges used by the Indian e-Visa and UPSC forms.",
 intro="A 100 KB PDF limit trips up almost everyone with a phone scan, because a single photographed page is often 2 MB. The tool below gets under the limit on your own device and shows a preview so you can check the text is still readable.",
 how_h2="How to Compress a PDF to 100KB in 4 Steps",
 steps=steps_pdf("100 KB"),
 uses_intro="Common uploads where 100 KB is the limit or a safe size inside the range:",
 uses=[("Indian e-Visa", "Passport bio page (PDF)", "10&ndash;300 KB"),
       ("UPSC", "Photo identity document (PDF)", "20&ndash;300 KB"),
       ("NTA (NEET UG)", "Certificates (PDF)", "50&ndash;300 KB"),
       ("Protean (NSDL) online PAN application", "Supporting documents", "Up to 300 KB")],
 test_note="Black and white beat colour by a wide margin on text: at 100 KB the scan kept 200 dpi (print quality) instead of 50 dpi in colour.",
 sections=[("Colour or Black and White at 100 KB?",
   "<p>Use black and white for typed letters, printed forms, marksheets and most certificates: the text stays as sharp as the original and the file is often a fifth of the limit. Use colour only when colour carries meaning, such as a photo on an ID card, a coloured seal, or a signature in blue ink that must look blue. If a colour page comes out soft at 100 KB, crop the scan to the edges of the paper first; empty margins cost as much space as text.</p>"),
   COMMON_PDF_SECTION],
 faq_title="Compress PDF to 100KB",
 faq=[("How do I reduce a PDF to 100 KB?", "Choose it in the tool above and download; the limit is set to 100 KB. Select Black and white for text documents."),
      ("How can I reduce a PDF from 1 MB to 100 KB?", "The tool handles that in one step. A 1 MB PDF is usually a scan or contains photos, so it redraws the pages at the best resolution that fits under 100 KB."),
      ("Can I compress a PDF to 100 KB on my phone?", "Yes. Open this page on your phone, tap Choose a PDF or photos and pick the file from your downloads or files app. It works offline once the page has loaded."),
      ("How do I make a PDF under 100 KB from photos?", "Choose the photos instead of a PDF. Each one becomes an A4 page and the whole PDF is compressed to under 100 KB."),
      ("Will the text still be selectable?", "Only if the lossless step was enough (the result line says so). If pages were redrawn as images, the text looks the same but can no longer be selected or searched.")],
 src=["evisa", "upsc", "ssbcrack", "neet", "pw_neetdocs", "protean", "pdfjs", "pdflib"])

add_pdf(slug="compress-pdf-to-150kb", kb=150, kw="compress pdf to 150kb",
 title="Compress PDF to 150KB Online Free (Private, No Upload)",
 tool_title="Compress PDF to 150 KB",
 desc="Compress any PDF or scan to under 150 KB in your browser, with sharp text and a preview. Free, no upload, no sign-up, no watermark.",
 quick="Add your PDF to the tool below and download a copy under 150 KB. In <strong>Black and white</strong>, 150 KB holds about 5&ndash;10 typed pages at 150&ndash;200 dpi; in colour, two or three pages at a readable resolution (our 3-page colour scan came out at 146 KB). 150 KB is a safe middle value for portals that accept anything from 10&ndash;50 KB up to 300 KB.",
 intro="When a form allows 100&ndash;200 KB, aiming at 150 KB leaves a margin on both sides. The tool below gets there on your own device without uploading your document.",
 how_h2="How to Compress a PDF to 150KB in 4 Steps",
 steps=steps_pdf("150 KB"),
 uses_intro="Uploads where a 150 KB file is inside the allowed range:",
 uses=[("Indian e-Visa", "Passport bio page (PDF)", "10&ndash;300 KB"),
       ("UPSC", "Photo identity document (PDF)", "20&ndash;300 KB"),
       ("NTA (NEET UG)", "Certificates (PDF)", "50&ndash;300 KB"),
       ("National Scholarship Portal", "Documents on many schemes", "Up to 200 KB")],
 test_note="Colour pages reached only 60 dpi at 150 KB, while black and white kept full 300 dpi sharpness in 124 KB.",
 sections=[("Getting a Clean Scan in the First Place",
   "<p>The smaller the limit, the more the original scan matters. Photograph the page flat, in daylight, from directly above, and crop to the paper's edges. A phone's &ldquo;document scan&rdquo; mode (in Google Drive, the iPhone Notes and Files apps, or most camera apps) straightens the page and whitens the background, which can halve the compressed size. Then add the result to the tool.</p>"),
   COMMON_PDF_SECTION],
 faq_title="Compress PDF to 150KB",
 faq=[("How do I compress a PDF to 150 KB?", "Choose it in the tool above with the limit at 150 KB and download."),
      ("Is 150 KB enough for a scanned certificate?", "Yes. A single certificate page fits at good quality in colour, and several pages fit in black and white."),
      ("How do I compress a PDF to between 100 and 150 KB?", "Set the tool to 150 KB. It aims close to the limit for the best quality, so the result is usually between 140 and 150 KB."),
      ("Can I compress password-protected PDFs?", "Not directly. Open the PDF with its password, save or print it to a new PDF without the password, then compress that copy."),
      ("Does compressing reduce the page size?", "No. A4 pages stay A4 and print the same size; only the file gets smaller.")],
 src=["evisa", "upsc", "neet", "pw_neetdocs", "nsp", "pdfjs", "pdflib"])

add_pdf(slug="compress-pdf-to-200kb", kb=200, kw="compress pdf to 200kb",
 title="Compress PDF to 200KB Online Free (Scholarship and Exam Uploads)",
 tool_title="Compress PDF to 200 KB",
 desc="Compress a PDF or scanned document to under 200 KB with sharp text, for NSP scholarship and exam forms. Free, private, never uploaded.",
 quick="Choose your PDF in the tool below and download a copy under 200 KB. In <strong>Black and white</strong>, 200 KB holds about six typed pages at 200 dpi, or twelve at 150 dpi; in colour, our 3-page, 9.8 MB certificate scan came out at 193 KB. 200 KB is the document limit on many National Scholarship Portal (NSP) schemes and is safely inside the 300 KB maximum on NEET, UPSC and e-Visa forms.",
 intro="&ldquo;Compress PDF to 200 KB&rdquo; is the most searched PDF size in India, mostly because of scholarship and exam applications. The tool below gets any PDF or set of photos under the limit on your own device, and shows a preview before you download.",
 how_h2="How to Compress a PDF to 200KB in 4 Steps",
 steps=steps_pdf("200 KB"),
 uses_intro="Where you will meet a 200 KB PDF limit, or a range that includes it:",
 uses=[("National Scholarship Portal (NSP)", "Bank passbook and other documents on many schemes", "Up to 200 KB"),
       ("NTA (NEET UG)", "Category, PwBD and other certificates", "50&ndash;300 KB"),
       ("UPSC", "Photo identity document", "20&ndash;300 KB"),
       ("Indian e-Visa", "Passport bio page", "10&ndash;300 KB"),
       ("State scholarship and university portals", "Marksheets, income and caste certificates", "Often 200 KB; check the notice")],
 test_note="So 200 KB is enough for three full-colour scanned pages at a readable resolution, or six to seven black-and-white pages at print quality (200 dpi).",
 sections=[("NSP Scholarship Documents Under 200 KB",
   "<p>Scholarship portals typically ask for several documents (income certificate, caste certificate, marksheet, bank passbook, fee receipt), each under 200 KB. Scan or photograph each one separately, add it to the tool, choose Black and white for printed certificates and download. Name each file clearly before uploading (for example <em>income-certificate.pdf</em>), because portals often show only the file name. The exact limit depends on the scheme, so read the line under each upload button.</p>"),
   COMMON_PDF_SECTION],
 faq_title="Compress PDF to 200KB",
 faq=[("How do I compress a PDF to 200 KB?", "Choose it in the tool above and download; the limit is set to 200 KB."),
      ("How do I compress a PDF to 200 KB for a scholarship?", "Scan each document separately, compress it with the tool (Black and white for printed certificates) and upload it in the matching field. Each file must be under the portal's limit, usually 200 KB."),
      ("How can I reduce the size of a PDF to under 200 KB on my phone?", "Open this page in your phone's browser, tap Choose a PDF or photos, select the file and download the result. Nothing is uploaded."),
      ("How do I make a PDF under 200 KB from photos?", "Choose the photos in the tool instead of a PDF. Or use the dedicated [[post:articles/jpg-to-pdf-under-200kb|JPG to PDF under 200 KB]] page."),
      ("Why is my compressed PDF blurry?", "The original may have many pages or a lot of colour. Choose Black and white for text, crop the scan, or check whether the portal accepts a larger file.")],
 src=["nsp", "neet", "pw_neetdocs", "upsc", "ssbcrack", "evisa", "pdfjs", "pdflib"])

add_pdf(slug="compress-pdf-to-300kb", kb=300, kw="compress pdf to 300kb",
 title="Compress PDF to 300KB Online Free (NEET, UPSC, e-Visa, PAN)",
 tool_title="Compress PDF to 300 KB",
 desc="Compress a PDF to under 300 KB for NEET, UPSC, Indian e-Visa or PAN uploads. Sharp text, preview, processed in your browser and never uploaded.",
 quick="Choose your PDF in the tool below and download it under 300 KB, which is the maximum for NEET certificates (50&ndash;300 KB), the UPSC photo ID (20&ndash;300 KB), the Indian e-Visa passport page (10&ndash;300 KB) and PAN supporting documents on the Protean (NSDL) online form. At 300 KB a 3-page colour scan stays clear at 72&ndash;100 dpi, and a black-and-white document can keep full 300 dpi print quality.",
 intro="Three hundred kilobytes is the most common PDF ceiling on Indian government forms. It is also the edge of the range, so the tool keeps files under 300,000 bytes to pass whichever way the portal counts.",
 how_h2="How to Compress a PDF to 300KB in 4 Steps",
 steps=steps_pdf("300 KB"),
 uses_intro="Forms where 300 KB is the maximum:",
 uses=[("NTA (NEET UG)", "Category, PwBD, citizenship and other certificates", "50&ndash;300 KB"),
       ("UPSC (CDS, NDA, Civil Services)", "Photo identity document", "20&ndash;300 KB"),
       ("Indian e-Visa", "Passport bio page", "10&ndash;300 KB"),
       ("Protean (NSDL) online PAN application", "Supporting documents", "Up to 300 KB")],
 test_note="Colour scans kept 72 dpi at 300 KB; black and white kept the full resolution with most of the allowance unused.",
 sections=[("Also Watch the Minimum",
   "<p>NEET certificates must be at least 50 KB and UPSC documents at least 20 KB. A clean black-and-white page can come out smaller than that, which the portal rejects as too small. If that happens, choose Keep colour instead: the file will be larger (and still under 300 KB), and the page looks exactly like the original scan.</p>"),
   COMMON_PDF_SECTION],
 faq_title="Compress PDF to 300KB",
 faq=[("How do I compress a PDF to 300 KB?", "Choose it in the tool above and download; the limit is set to 300 KB."),
      ("How do I upload a category certificate for NEET under 300 KB?", "Scan or photograph the certificate, add it to the tool and download. Keep colour usually gives a file between 50 and 300 KB, which is the NEET range."),
      ("What size should the passport PDF be for an Indian e-Visa?", "Between 10 KB and 300 KB. Scan the bio page only, crop it, and compress it with the tool."),
      ("How do I reduce a PDF from 2 MB to 300 KB?", "Add it to the tool; it tries a lossless squeeze first and, if that is not enough, redraws the pages at the best resolution that fits."),
      ("Is 300 KB the same as 0.3 MB?", "Yes if you count 1,000 KB per MB; with 1,024 it is 0.29 MB. Our [[post:articles/kb-to-mb-converter|KB to MB converter]] shows both.")],
 src=["neet", "pw_neetdocs", "upsc", "ssbcrack", "evisa", "protean", "pdfjs", "pdflib"])

add_pdf(slug="compress-pdf-to-500kb", kb=500, kw="compress pdf to 500kb",
 title="Compress PDF to 500KB Online Free (GST, Job and Visa Uploads)",
 tool_title="Compress PDF to 500 KB",
 desc="Compress a PDF to under 500 KB with high quality, for GST registration and other uploads. Free, private, works in your browser with no upload.",
 quick="Choose the PDF in the tool below and download it under 500 KB. That is generous: a 3-page colour scan keeps about 120 dpi (clear on screen and in print), and about 15 typed pages fit in black and white at 200 dpi. 500 KB is the limit for place-of-business proof in GST registration.",
 intro="Half a megabyte is enough for most single documents, so the aim is to keep as much quality as possible while getting under the line. The tool tries a lossless squeeze first and only redraws pages if it has to.",
 how_h2="How to Compress a PDF to 500KB in 4 Steps",
 steps=steps_pdf("500 KB", "At 500 KB Auto usually keeps colour. Choose Black and white for long text documents."),
 uses_intro="Where 500 KB limits are common:",
 uses=[("GST registration", "Principal or additional place-of-business proof (rent agreement, electricity bill)", "Up to 500 KB"),
       ("GST registration", "Most other documents", "Up to 1 MB"),
       ("Job and university portals", "Certificates and transcripts", "Often 500 KB&ndash;2 MB"),
       ("Email to offices with small inbox limits", "Any document", "Smaller files are less likely to bounce")],
 test_note="At 500 KB colour pages reached 120 dpi, which reads like the original on a phone or laptop.",
 sections=[("GST Place-of-Business Proof Under 500 KB",
   "<p>The rent agreement or electricity bill for your principal place of business is often several pages long and scanned in colour, which easily runs to a few megabytes. Remove pages the portal does not need, keep colour if the bill has coloured stamps, and compress it with the tool. A long agreement usually fits better in black and white at 200 dpi, which keeps every clause readable.</p>"),
   COMMON_PDF_SECTION],
 faq_title="Compress PDF to 500KB",
 faq=[("How do I compress a PDF to 500 KB?", "Choose it in the tool above and download; the limit is set to 500 KB."),
      ("What is the GST document upload size?", "Photographs are JPEG up to 100 KB, place-of-business proof up to 500 KB, and most other documents PDF or JPEG up to 1 MB. Check the line under each upload button on the GST portal."),
      ("How do I reduce a PDF from 5 MB to 500 KB?", "Add it to the tool. It will tell you whether the lossless step was enough or the pages were redrawn, and show a preview."),
      ("Will my PDF lose quality at 500 KB?", "Very little for most documents. If it is a typed PDF made on a computer, the lossless step often gets there with no change at all."),
      ("Can I compress several PDFs into one under 500 KB?", "Merge them first with our free [[post:articles/merge-split-pdf-without-uploading|PDF toolkit]], then compress the merged file here.")],
 src=["gst_pob", "gst_kk", "pdfjs", "pdflib"])

add_pdf(slug="compress-pdf-to-1mb", kb=1000, kw="compress pdf to 1mb",
 title="Compress PDF to 1MB Online Free (Under 1 MB, No Upload)",
 tool_title="Compress PDF to 1 MB",
 desc="Reduce a large PDF to under 1 MB for GST, OCI or any portal. Keeps text sharp, shows a preview and never uploads your file. Free, no sign-up.",
 quick="Choose your PDF in the tool below and download it under 1 MB. At 1 MB a 3-page colour scan keeps 150 dpi (good print quality), and a typed document of 30 or more pages fits in black and white. 1 MB is the limit for most GST registration documents and, at Indian missions, for each OCI supporting document.",
 intro="A 1 MB limit is reached easily by phone scans and PDFs with photos: a five-page scan is often 10 MB. The tool gets it under 1 MB on your own device while keeping as much resolution as possible.",
 how_h2="How to Compress a PDF to 1MB in 4 Steps",
 steps=steps_pdf("1 MB (1,000 KB)", "At 1 MB Auto keeps colour. Choose Black and white for long text documents."),
 uses_intro="Common uploads with a 1 MB limit:",
 uses=[("GST registration", "PAN, Aadhaar, bank and business proof (PDF or JPEG)", "Up to 1 MB each"),
       ("OCI application (Indian missions)", "Each supporting document", "Up to 1,000 KB"),
       ("Indian e-Visa", "Photograph (JPEG)", "10 KB&ndash;1 MB"),
       ("Many job and admission portals", "Resume, certificates", "Often 1&ndash;2 MB")],
 test_note="At 1 MB the colour scan kept 150 dpi; the 5-page document kept 120 dpi in colour and its full sharpness in black and white.",
 sections=[("1 MB, 1,000 KB or 1,024 KB?",
   "<p>Portals do not always say which they mean. Windows counts 1 MB as 1,024 KB; phones, Macs and many web servers count 1,000 KB. The tool keeps every file under 1,000,000 bytes, so a PDF it makes for a &ldquo;1 MB&rdquo; limit passes either way. If your file shows as 0.98 MB on one device and 1.02 MB on another, that is why.</p>"),
   COMMON_PDF_SECTION],
 faq_title="Compress PDF to 1MB",
 faq=[("How do I compress a PDF to 1 MB?", "Choose it in the tool above and download; the limit is set to 1 MB."),
      ("How do I reduce a PDF from 10 MB to 1 MB?", "Add it to the tool. A 10 MB PDF is almost always a scan, so the pages are redrawn at the best resolution that fits; check the preview before downloading."),
      ("Can I compress a PDF to 1 MB without losing quality?", "If it was made on a computer, often yes: the lossless step keeps it identical. Scans lose a little resolution, but 1 MB keeps them at print quality."),
      ("What is the OCI document size limit?", "Indian missions' upload guidelines give 1 MB (1,000 KB) per document. Compress each one separately with the tool."),
      ("How do I compress a PDF to 1 MB on a Mac or iPhone?", "Open this page in Safari, choose the PDF and download the result. On a Mac, Preview's Reduce File Size option also works but often blurs text much more.")],
 src=["gst_kk", "eoibogota", "oci", "evisa", "nist", "pdfjs", "pdflib"])

add_pdf(slug="compress-pdf-to-2mb", kb=2000, kw="compress pdf to 2mb",
 title="Compress PDF to 2MB Online Free (Aadhaar Update, Job Portals)",
 tool_title="Compress PDF to 2 MB",
 desc="Compress a big PDF to under 2 MB at high quality, for Aadhaar document updates, job and admission portals. Free and private: nothing is uploaded.",
 quick="Choose the PDF in the tool below and download it under 2 MB. At 2 MB even a multi-page colour scan keeps 150 dpi at high quality, and computer-made PDFs often fit with the lossless step alone, keeping text selectable. 2 MB is the limit for each document on the myAadhaar online update, and a common limit on job and admission portals.",
 intro="Two megabytes is enough for almost any normal document. PDFs go over it when they are long scans or full of photos; the tool brings them under with as little loss as possible, on your own device.",
 how_h2="How to Compress a PDF to 2MB in 4 Steps",
 steps=steps_pdf("2 MB (2,000 KB)", "Auto keeps colour at 2 MB, which is right for almost everything."),
 uses_intro="Common 2 MB limits:",
 uses=[("myAadhaar online document update (UIDAI)", "Proof of identity or address (PDF, JPEG or PNG)", "Up to 2 MB"),
       ("Job portals", "Resume and certificates", "Often 2 MB"),
       ("IRCC (Canada) secure account", "Each document", "Up to 4 MB (5 MB on the IRCC Portal)"),
       ("University admission portals", "Transcripts and certificates", "Often 1&ndash;5 MB")],
 test_note="At 2 MB the 3-page colour scan kept 150 dpi at 88% quality: indistinguishable from the original on screen.",
 sections=[("Aadhaar Document Update Under 2 MB",
   "<p>On the myAadhaar portal you upload one proof of identity and one proof of address, each as a PDF, JPEG or PNG of up to 2 MB. A phone photo of a document is usually 2&ndash;5 MB, so it needs a small squeeze: add it to the tool (a photo becomes a one-page PDF), keep colour, and download. Make sure every corner of the document is visible and the text is readable in the preview, because blurred proofs are the most common reason updates are rejected.</p>"),
   COMMON_PDF_SECTION],
 faq_title="Compress PDF to 2MB",
 faq=[("How do I compress a PDF to 2 MB?", "Choose it in the tool above and download; the limit is set to 2 MB."),
      ("What is the file size limit for an Aadhaar update?", "UIDAI's myAadhaar portal accepts PDF, JPEG or PNG documents of up to 2 MB each."),
      ("How do I reduce a PDF from 20 MB to 2 MB?", "Add it to the tool. Large PDFs take a little longer because every page is processed on your device."),
      ("Will compressing to 2 MB change how my PDF looks?", "Usually not visibly. Typed PDFs often fit losslessly, and scans keep 150 dpi or more."),
      ("Is there a page limit?", "No fixed limit, but very long PDFs (hundreds of pages) can be slow on older phones. A laptop is faster for those.")],
 src=["uidai", "ircc", "nist", "pdfjs", "pdflib"])


# ---------------------------------------------------------------- special pages and the hub
EXTRA = []


def px(cm, dpi):
    return round(cm / 2.54 * dpi)


CM_ROWS = [("Exam and visa photo (SSC, many forms)", 3.5, 4.5), ("PAN card photo", 2.5, 3.5), ("Square photo (US visa, OCI)", 5.08, 5.08),
           ("Stamp-size photo", 2.0, 2.5), ("SSC signature", 6.0, 2.0), ("PAN card signature", 4.5, 2.0), ("Postcard photo (NEET, 4 &times; 6 in)", 10.16, 15.24)]
CM_TABLE = ("<table>\n<thead><tr><th>Size</th><th>Centimetres</th><th>100 dpi</th><th>200 dpi</th><th>300 dpi</th></tr></thead>\n<tbody>\n" +
            "".join("<tr><td>%s</td><td>%g &times; %g cm</td><td>%d &times; %d</td><td>%d &times; %d</td><td>%d &times; %d</td></tr>\n" % (
                n, w, h, px(w, 100), px(h, 100), px(w, 200), px(h, 200), px(w, 300), px(h, 300)) for n, w, h in CM_ROWS) + "</tbody>\n</table>\n")

KBMB = [(10, ""), (20, ""), (50, ""), (100, ""), (200, ""), (300, ""), (500, ""), (1000, "1 MB on a phone"), (1024, "1 MB on Windows"), (2048, "2 MB on Windows"), (5120, "5 MB on Windows")]
KBMB_TABLE = ("<table>\n<thead><tr><th>KB</th><th>MB (1 MB = 1,000 KB)</th><th>MB (1 MB = 1,024 KB)</th><th>Bytes (1 KB = 1,024)</th></tr></thead>\n<tbody>\n" +
              "".join("<tr><td>%s KB%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n" % (
                  format(k, ","), (" (%s)" % note) if note else "", ("%.3f" % (k / 1000)).rstrip("0").rstrip("."), ("%.4f" % (k / 1024)).rstrip("0").rstrip("."), format(k * 1024, ",")) for k, note in KBMB) +
              "</tbody>\n</table>\n")

SIZE_LIST = ("<ul>\n" + "".join("<li>[[post:articles/%s]]</li>\n" % n for n in PHOTO_SIZES) + "</ul>\n")
PDF_LIST = ("<ul>\n" + "".join("<li>[[post:articles/%s]]</li>\n" % n for n in PDF_SIZES + ["jpg-to-pdf-under-200kb"]) + "</ul>\n")

EXTRA.append(dict(slug="signature-resize", kb=20, kw="signature resize", template="image-to-kb", embed="signature-resize-tool",
 preset={"min": 10, "gray": 1},
 title="Signature Resize Online Free: 10-20 KB for SSC, IBPS, PAN and NEET",
 tool_title="Signature resizer: crisp black ink, exact KB",
 desc="Resize your signature to 10-20 KB, exact pixels or cm, as a crisp black-on-white JPG for SSC, IBPS, RRB, PAN and NEET forms. No upload.",
 quick="Photograph your signature on white paper, add it to the free tool below and download: it comes out as a crisp black-on-white JPG between 10 and 20 KB, which is the signature range for SSC, IBPS, SBI and RRB forms. For other forms, change the numbers: NEET and JEE accept 4&ndash;30 KB, the online PAN form up to 10 KB, and UPSC up to 100 KB. If the form gives exact pixels or centimetres, set those too.",
 intro="The signature is the upload that fails most often, because a photo of a signature is mostly grey paper: too big in colour, too small (under the minimum) when cropped tight, and blurry if a compressor crushes it. The tool below turns the paper pure white, keeps the ink sharp and hits the KB range, all on your own device.",
 sections=[("How to Resize a Signature in 4 Steps",
   "<ol>\n<li><strong>Sign on plain white paper</strong> with a black gel or ball pen (blue only if the form allows it). Sign as you normally would; some forms reject signatures in capital letters.</li>\n"
   "<li><strong>Photograph it flat</strong> in daylight, from directly above, without your shadow. Crop close to the ink in your phone's photo editor if you can.</li>\n"
   "<li><strong>Add it to the tool above.</strong> &ldquo;Black and white&rdquo; is already ticked and the range is set to 10&ndash;20 KB. For a fixed size, choose pixels (IBPS: 140 &times; 60) or cm (SSC: 6 &times; 2 cm).</li>\n"
   "<li><strong>Download the JPG</strong> and upload it. The result line confirms the KB and pixel size.</li>\n</ol>\n"),
  ("Signature Sizes for Popular Forms",
   "<table>\n<thead><tr><th>Form</th><th>Signature size</th><th>File size</th></tr></thead>\n<tbody>\n"
   "<tr><td>SSC (CGL, CHSL, MTS, GD)</td><td>6 &times; 2 cm (about 236 &times; 79 px at 100 dpi)</td><td>10&ndash;20 KB</td></tr>\n"
   "<tr><td>IBPS, SBI, RRB, LIC</td><td>140 &times; 60 px</td><td>10&ndash;20 KB</td></tr>\n"
   "<tr><td>NTA (NEET, JEE Main, CUET)</td><td>Clear signature on white paper</td><td>4&ndash;30 KB</td></tr>\n"
   "<tr><td>UPSC</td><td>Signed three times, one below the other</td><td>20&ndash;100 KB</td></tr>\n"
   "<tr><td>Protean (NSDL) online PAN application</td><td>200 dpi, black and white</td><td>Up to 10 KB</td></tr>\n"
   "</tbody>\n</table>\n<p>These are the sizes in recent notifications; they change between years, so check yours and type the exact numbers into the tool.</p>\n"),
  ("Too Small? Why a Signature Can Be Under 10 KB",
   "<p>A clean signature at 140 &times; 60 pixels has very little in it: even at the highest JPEG quality it is often only 3&ndash;6 KB, and the portal says &ldquo;file size less than minimum&rdquo;. Making it blurry or adding colour is the wrong fix. The tool instead adds an invisible blank block inside the file to reach the minimum, without changing the picture. In our test, a 140 &times; 60 signature came out at <strong>10.1 KB at 95% quality</strong>, accepted by a 10&ndash;20 KB form, and a full-size signature in black and white fitted in 9.7 KB at 838 &times; 377 pixels for a form with a 10 KB maximum.</p>\n"),
  ("Is It Safe to Resize My Signature Online?",
   "<p>Your signature, together with your photo and ID number, is what someone needs to fill in forms in your name. Most resizer sites upload the image to their server. This one never does: the image is opened, cleaned and compressed inside your browser, and disappears when you close the page.</p>\n")],
 faq_title="Signature Resize",
 faq=[("How do I resize my signature to 10&ndash;20 KB?", "Add the photo of your signature to the tool above; it is already set to 10&ndash;20 KB in black and white. Download the JPG and upload it."),
      ("How do I resize a signature to 140 &times; 60 pixels?", "Choose &ldquo;Set width and height in pixels&rdquo; in the tool and type 140 and 60. That is the IBPS, SBI and RRB signature size; the tool pads the file to 10 KB if needed."),
      ("What is 6 cm &times; 2 cm in pixels for the SSC signature?", "About 236 &times; 79 pixels at 100 dpi, or 472 &times; 157 at 200 dpi. Choose cm in the tool, enter 6 and 2, and pick the dpi."),
      ("Can I sign in blue ink?", "Only if the form allows it. Most Indian exam forms ask for black ink; the tool's black-and-white setting makes blue ink black anyway, which is what those forms want."),
      ("How do I resize a signature on my phone?", "Open this page in your phone's browser, tap Choose photos and take a picture of your signature or pick one from the gallery. Everything runs on the phone.")],
 src=["ssc", "pw_ssc", "ibps", "neet", "pw_neet", "upsc", "visionias", "protean", "mdn_blob"],
 related=["resize-image-to-20kb", "resize-image-to-10kb", "pan-card-photo-resize", "exam-photo-signature-resizer", "image-and-pdf-size-tools"]))

EXTRA.append(dict(slug="pan-card-photo-resize", kb=20, kw="resize photo for pan card", template="image-to-kb", embed="pan-photo-resize-tool",
 preset={"dim": "cm", "wc": 2.5, "hc": 3.5, "dpi": 200},
 title="Resize Photo for PAN Card Online Free (20 KB, 200 DPI, No Upload)",
 tool_title="PAN card photo resizer: 2.5 x 3.5 cm, 200 dpi, under 20 KB",
 desc="Resize your photo and signature for the online PAN application: 2.5 x 3.5 cm at 200 dpi under 20 KB, signature under 10 KB. Free, no upload.",
 quick="Add your photo to the free tool below and download it: it is already set to the PAN card photo size, 2.5 &times; 3.5 cm at 200 dpi (197 &times; 276 pixels), as a colour JPG under 20 KB. For the signature, switch the limit to 10 KB and tick &ldquo;Black and white&rdquo;. These are the scanning specifications on Protean's (formerly NSDL) online PAN form; UTIITSL's form shows its own limits on the upload screen.",
 intro="The online PAN application rejects photos that are too big, the wrong shape or saved at the wrong resolution. A phone photo is 3&ndash;5 MB; the PAN photo has to be under 20 KB. The tool below crops, resizes and compresses it to that size on your own device, and writes 200 dpi into the file.",
 sections=[("PAN Card Photo and Signature Size",
   "<table>\n<thead><tr><th>Upload</th><th>Size</th><th>Resolution</th><th>File</th></tr></thead>\n<tbody>\n"
   "<tr><td>Photograph</td><td>2.5 &times; 3.5 cm (197 &times; 276 px)</td><td>200 dpi, colour</td><td>JPEG, up to 20 KB</td></tr>\n"
   "<tr><td>Signature</td><td>4.5 &times; 2 cm (354 &times; 157 px)</td><td>200 dpi, black and white</td><td>JPEG, up to 10 KB</td></tr>\n"
   "<tr><td>Supporting documents</td><td>Scanned pages</td><td>200 dpi</td><td>Up to 300 KB</td></tr>\n"
   "</tbody>\n</table>\n<p>Source: Protean's scanning specifications for the online PAN application. The forms have been revised more than once, so if the upload screen shows different numbers, type those into the tool instead.</p>\n"),
  ("How to Resize Your Photo for PAN in 4 Steps",
   "<ol>\n<li><strong>Take a recent colour photo</strong> against a plain white or light background, face straight to the camera, both ears visible, no cap or dark glasses.</li>\n"
   "<li><strong>Crop it to head and shoulders</strong> in your phone's editor, so your face fills most of the picture. The tool keeps the 2.5 &times; 3.5 cm shape and fills the frame from the centre.</li>\n"
   "<li><strong>Add it to the tool above</strong> and download the JPG. It will be 197 &times; 276 pixels, 200 dpi and under 20 KB.</li>\n"
   "<li><strong>For the signature,</strong> sign on white paper, photograph it, set the limit to 10 KB, choose cm and enter 4.5 &times; 2, tick Black and white and download. Our [[post:articles/signature-resize|signature resizer]] does the same with more options.</li>\n</ol>\n"),
  ("Why the PAN Portal Rejects Photos",
   "<ul>\n<li><strong>Over 20 KB:</strong> the most common error. The tool always stays under 20,000 bytes.</li>\n"
   "<li><strong>Wrong resolution:</strong> the portal reads the dpi stored inside the JPEG. The tool writes 200 dpi.</li>\n"
   "<li><strong>Not a real JPG:</strong> a renamed PNG or iPhone HEIC file fails. The tool always saves a genuine JPG and opens HEIC photos.</li>\n"
   "<li><strong>Signature in colour or on grey paper:</strong> tick Black and white so the background becomes pure white.</li>\n</ul>\n"),
  ("Keep Your PAN Documents Private",
   "<p>Your photo, signature and PAN details are exactly what identity fraud needs. This tool works entirely in your browser and never uploads them. Before sharing a copy of your PAN or Aadhaar anywhere else, you can also hide the numbers with our [[post:articles/mask-aadhaar-pan-before-sharing|Aadhaar and PAN masking tool]].</p>\n")],
 faq_title="Resize Photo for PAN Card",
 faq=[("What is the photo size for a PAN card?", "On Protean's online form: 2.5 &times; 3.5 cm at 200 dpi, colour, JPEG of up to 20 KB. At 200 dpi that is 197 &times; 276 pixels."),
      ("What is the signature size for a PAN card?", "4.5 cm wide by 2 cm high at 200 dpi, black and white, JPEG of up to 10 KB (about 354 &times; 157 pixels)."),
      ("How do I reduce my photo to 20 KB for PAN?", "Use the tool above; it is preset to the PAN photo size and 20 KB. Add the photo and download."),
      ("What is 2.5 &times; 3.5 cm in pixels?", "197 &times; 276 pixels at 200 dpi, 98 &times; 138 at 100 dpi and 295 &times; 413 at 300 dpi."),
      ("Can I use the same photo for the UTIITSL PAN form?", "Usually, but check the limits shown on UTIITSL's upload screen and type them into the tool if they differ.")],
 src=["protean", "pan_photo", "mdn_blob", "nist"],
 related=["signature-resize", "resize-image-to-20kb", "resize-image-to-10kb", "mask-aadhaar-pan-before-sharing", "image-and-pdf-size-tools"]))

EXTRA.append(dict(slug="resize-image-in-cm", kb=100, kw="resize image to cm", template="image-to-kb", embed="image-cm-tool",
 preset={"dim": "cm", "wc": 3.5, "hc": 4.5, "dpi": 200},
 title="Resize Image to CM Online Free (cm, mm or Inches to Pixels)",
 tool_title="Resize image to centimetres",
 desc="Resize a photo to an exact size in cm (or mm) at 100, 200 or 300 dpi, with a KB limit. Includes a cm to pixel table for common photo sizes. No upload.",
 quick="Choose your photo in the free tool below, enter the width and height in centimetres and pick the resolution (200 dpi is what most Indian forms mean), then download. The tool converts centimetres to pixels for you: 3.5 &times; 4.5 cm is 276 &times; 354 pixels at 200 dpi. For millimetres, divide by ten (35 &times; 45 mm is 3.5 &times; 4.5 cm).",
 intro="A digital image has no size in centimetres on its own: it has pixels, plus a resolution (dots per inch) that says how big to print them. That is why &ldquo;resize to 3.5 &times; 4.5 cm&rdquo; really means &ldquo;resize to the right number of pixels and store the right dpi&rdquo;. The tool does both, and keeps the file under a KB limit if the form has one.",
 sections=[("How to Convert cm to Pixels",
   "<p>Pixels = centimetres &divide; 2.54 &times; dpi. So 3.5 cm at 200 dpi is 3.5 &divide; 2.54 &times; 200 = 276 pixels. The table below has the common sizes worked out; the tool does the same sum when you choose &ldquo;Set width and height in cm&rdquo;.</p>\n" + CM_TABLE +
   "<p>When a form gives only centimetres and no dpi, 200 dpi is a safe choice for an on-screen upload. Use 300 dpi if the picture will be printed, for example on a card.</p>\n"),
  ("How to Resize an Image to cm in 3 Steps",
   "<ol>\n<li><strong>Choose the photo</strong> in the tool above. It is preset to 3.5 &times; 4.5 cm at 200 dpi.</li>\n"
   "<li><strong>Type the width and height in cm</strong> and pick 100, 200 or 300 dpi. Set the KB limit if the form gives one.</li>\n"
   "<li><strong>Download the JPG.</strong> It is cropped from the centre to the exact shape, resized to the matching pixels, and has the dpi written inside it, so it prints at exactly that size.</li>\n</ol>\n"),
  ("Printing at the Right Size",
   "<p>When you print, choose &ldquo;Actual size&rdquo; or 100% scale, not &ldquo;Fit to page&rdquo;. Because the dpi is stored in the file, a 276 &times; 354 pixel photo saved at 200 dpi prints at exactly 3.5 &times; 4.5 cm. For a sheet of passport photos, use our [[post:articles/passport-photo-maker|passport photo maker]], which lays out several copies on one page.</p>\n")],
 faq_title="Resize Image to CM",
 faq=[("How do I resize an image to 3.5 &times; 4.5 cm?", "Use the tool above: it is preset to 3.5 &times; 4.5 cm at 200 dpi (276 &times; 354 pixels). Add the photo and download."),
      ("How many pixels is 1 cm?", "It depends on the dpi: about 39 pixels at 100 dpi, 79 at 200 dpi and 118 at 300 dpi."),
      ("How do I resize an image in mm?", "Divide the millimetres by ten and enter them as centimetres: 35 &times; 45 mm is 3.5 &times; 4.5 cm."),
      ("What dpi should I use?", "200 dpi for most online forms, 300 dpi for anything that will be printed. 100 dpi gives a very small file but looks soft when printed."),
      ("Can I resize to inches?", "Multiply inches by 2.54 to get centimetres: a 2 &times; 2 inch photo is 5.08 &times; 5.08 cm, or 600 &times; 600 pixels at 300 dpi.")],
 src=["nist", "state", "ssc", "protean", "mdn_blob"],
 related=["pan-card-photo-resize", "signature-resize", "resize-image-to-50kb", "passport-photo-maker", "image-and-pdf-size-tools"]))

EXTRA.append(dict(slug="jpg-to-pdf-under-200kb", kb=200, kw="jpg to pdf under 200kb", template="pdf-to-kb", embed="jpg-pdf-200kb-tool",
 title="JPG to PDF Under 200KB Online Free (Also 100KB, 300KB, 500KB)",
 tool_title="JPG to PDF under 200 KB",
 desc="Convert JPG photos of documents to one PDF under 200 KB (or any size) with sharp text. Free, private, works on phones and never uploads your files.",
 quick="Choose your JPG photos in the free tool below; they become one PDF, one photo per A4 page, under 200 KB. Change the number for 100, 300 or 500 KB. In our test, two phone photos of printed pages (5.2 MB) became an <strong>83 KB PDF</strong> in black and white at 300 dpi, or 193 KB in colour.",
 intro="Most portals want documents as a PDF under a certain size, but phones save documents as large JPG photos. The tool below converts and compresses in one step, on your own device.",
 sections=[("How to Convert JPG to PDF Under 200 KB",
   "<ol>\n<li><strong>Choose the photos</strong> of your document pages in the tool above, in page order (select them together).</li>\n"
   "<li><strong>Keep Auto selected.</strong> It keeps colour when it stays clear and switches to black and white when colour would be blurry at 200 KB.</li>\n"
   "<li><strong>Check the preview and download</strong> the PDF.</li>\n</ol>\n"
   "<p>Each photo is placed on an A4 page with a small margin, turned to landscape if it is wider than tall. For best results, crop each photo to the edges of the paper first.</p>\n"),
  ("What Our Test Produced",
   "<table>\n<thead><tr><th>Limit</th><th>Keep colour</th><th>Black and white</th></tr></thead>\n<tbody>\n"
   "<tr><td>100 KB</td><td>96 KB, 60 dpi</td><td>83 KB, 300 dpi</td></tr>\n"
   "<tr><td>200 KB</td><td>193 KB, 72 dpi</td><td>83 KB, 300 dpi</td></tr>\n"
   "<tr><td>300 KB</td><td>290 KB, 100 dpi</td><td>83 KB, 300 dpi</td></tr>\n"
   "<tr><td>500 KB</td><td>484 KB, 150 dpi</td><td>83 KB, 300 dpi</td></tr>\n"
   "</tbody>\n</table>\n<p>Two photographed A4 pages of printed text (5.2 MB as JPGs). Black and white stays sharp at every limit; colour needs about 500 KB for two pages to look like the original.</p>\n"),
  ("Tips for Photographing Documents",
   "<ul>\n<li><strong>Lay the page flat</strong> on a dark table, so its edges stand out, and shoot from directly above.</li>\n"
   "<li><strong>Use daylight</strong> from a window instead of the flash, which leaves a bright spot in the middle of the page.</li>\n"
   "<li><strong>Fill the frame</strong> with the page and crop away the table afterwards: margins cost as many bytes as text.</li>\n"
   "<li><strong>Use your phone's document scan mode</strong> if it has one (Google Drive, the iPhone Notes and Files apps, most camera apps). It straightens the page and whitens the paper, which roughly halves the PDF size.</li>\n"
   "<li><strong>Check every corner is visible.</strong> Portals reject certificates with a cut-off seal, signature or serial number.</li>\n</ul>\n"),
  ("Where You Need It",
   "<p>National Scholarship Portal schemes typically ask for documents under 200 KB; NEET certificates must be 50&ndash;300 KB; the Indian e-Visa passport page 10&ndash;300 KB. For a single photo that must stay a JPG instead, use [[post:articles/resize-image-to-200kb|resize image to 200 KB]].</p>\n")],
 faq_title="JPG to PDF Under 200KB",
 faq=[("How do I convert a JPG to a PDF under 200 KB?", "Choose the JPG in the tool above and download the PDF; the limit is set to 200 KB."),
      ("Can I combine several JPGs into one PDF?", "Yes. Select them all at once and they become one PDF, in the order you selected them."),
      ("How do I make a JPG to PDF under 100 KB?", "Type 100 in the size box or tap the 100 KB chip; black and white keeps text sharp at that size."),
      ("Will my photo stay in colour?", "Choose Keep colour if it must. Auto keeps colour unless it would make the page blurry."),
      ("Does it work on a phone?", "Yes, on Android and iPhone, and nothing is uploaded.")],
 src=["nsp", "neet", "pw_neetdocs", "evisa", "pdfjs", "pdflib"],
 related=["compress-pdf-to-200kb", "compress-pdf-to-100kb", "compress-pdf-to-300kb", "resize-image-to-200kb", "image-and-pdf-size-tools"]))

EXTRA.append(dict(slug="kb-to-mb-converter", kw="kb to mb", embed="kb-mb-converter",
 title="KB to MB Converter (and MB to KB): Both 1,000 and 1,024 Answers",
 desc="Convert KB to MB, MB to KB, bytes and GB instantly, with both the 1,000 and 1,024 answers, and learn which one your upload form means.",
 quick="Divide by 1,024 to turn KB into MB the way Windows and most upload forms count (500 KB = 0.488 MB), or by 1,000 the way phones and Macs count (500 KB = 0.5 MB). The converter below shows both at once. To stay safe on a form that says &ldquo;under 200 KB&rdquo;, keep your file under 195 KB as Windows shows it.",
 intro="Two systems are in everyday use. The international standard (SI) and storage makers use 1 KB = 1,000 bytes; Windows and many programmers use 1 KB = 1,024 bytes, which the standards body calls a kibibyte (KiB). The difference is small for kilobytes and grows with each step: 2.4% for KB, 4.9% for MB, 7.4% for GB.",
 sections=[("KB to MB Conversion Table", KBMB_TABLE),
  ("Which One Does My Upload Form Use?",
   "<p>Forms rarely say. Many websites check the file in bytes and compare it with 200 &times; 1,024 = 204,800 bytes for &ldquo;200 KB&rdquo;; others use 200,000. A file Windows shows as 199 KB is 203,776 bytes, which passes the first and fails the second. The safe rule is to stay under the limit &times; 1,000 bytes, which is what our [[post:articles/image-and-pdf-size-tools|resize and compress tools]] do automatically.</p>\n"),
  ("Why My Phone and Laptop Show Different Sizes",
   "<p>The same file can show as 1.05 MB on an iPhone or Mac and 1,000 KB (0.98 MB) on Windows. Nothing changed: Apple's systems count 1 MB as 1,000,000 bytes and Windows counts 1,048,576. The same thing explains why a new 128 GB phone or memory card shows only about 119 GB in Windows: the maker counts in thousands, Windows in multiples of 1,024. When an upload form rejects a file that &ldquo;looks&rdquo; small enough, check the exact size in bytes, shown in brackets in Windows Properties.</p>\n"),
  ("How to Check a File's Size",
   "<ul>\n<li><strong>Windows:</strong> right-click the file, choose Properties and read &ldquo;Size&rdquo; (in 1,024-based KB) and the exact bytes in brackets.</li>\n"
   "<li><strong>Mac:</strong> select the file and press Command-I. Finder uses 1,000-based KB.</li>\n"
   "<li><strong>Android:</strong> open the Files app, tap and hold the file, then the &#9432; details icon.</li>\n"
   "<li><strong>iPhone:</strong> in the Files app, touch and hold the file and choose Get Info. For photos, open the photo and swipe up.</li>\n</ul>\n")],
 faq_title="KB to MB",
 faq=[("How many KB are in 1 MB?", "1,024 KB in the binary system (Windows, most forms) or 1,000 KB in the decimal system (phones, Macs, storage makers)."),
      ("How do I convert KB to MB?", "Divide by 1,024 (binary) or 1,000 (decimal). 300 KB is 0.293 MB or 0.3 MB."),
      ("How do I convert MB to KB?", "Multiply by 1,024 or 1,000. 2 MB is 2,048 KB or 2,000 KB."),
      ("Is 500 KB bigger than 1 MB?", "No. 500 KB is about half of 1 MB in either system."),
      ("What is a KiB?", "A kibibyte, exactly 1,024 bytes. The standards say KB should mean 1,000 bytes and KiB 1,024, but Windows still labels KiB as KB.")],
 src=["nist", "ms_units"],
 related=["image-and-pdf-size-tools", "resize-image-to-200kb", "compress-pdf-to-1mb", "compress-pdf-to-200kb"],
 footer="Checked in October 2026. The converter runs in your browser."))

EXTRA.append(dict(slug="image-and-pdf-size-tools", kb=100, kw="reduce image size in kb", template="image-to-kb", embed="image-kb-hub-tool",
 title="Reduce Image Size in KB Online Free: Every Photo and PDF Size",
 tool_title="Reduce image size in KB",
 desc="Reduce photo size in KB to any limit, or pick a ready tool for 10 KB to 500 KB photos and 50 KB to 2 MB PDFs. Free, private, nothing uploaded.",
 quick="Type the KB you need into the free tool below, add your photo and download: it comes out as a sharp JPG under that size. Or open one of the ready-made pages below for a specific size, a PDF, a signature or a PAN card photo; each has the tool preset and lists which forms use that size.",
 intro="Indian application forms ask for files of every size from 4 KB to 2 MB, and each one has its own trap: a minimum as well as a maximum, exact pixels, centimetres, a dpi, or a PDF instead of a JPG. Every tool here runs on your own device and never uploads your files, has no daily limit and adds no watermark.",
 sections=[("Photo Size Tools", SIZE_LIST),
  ("PDF Size Tools", PDF_LIST),
  ("Signature, PAN and Exact-Size Tools",
   "<ul>\n<li>[[post:articles/signature-resize]]</li>\n<li>[[post:articles/pan-card-photo-resize]]</li>\n<li>[[post:articles/resize-image-in-cm]]</li>\n"
   "<li>[[post:articles/exam-photo-signature-resizer]]</li>\n<li>[[post:articles/passport-photo-maker]]</li>\n<li>[[post:articles/kb-to-mb-converter]]</li>\n</ul>\n"),
  ("How to Reduce Image Size in KB Without Losing Quality",
   "<ol>\n<li><strong>Crop first.</strong> Cutting away background you do not need is the biggest saving and costs no quality.</li>\n"
   "<li><strong>Use the right pixel size.</strong> A form shows your photo at a few hundred pixels; a 4,000-pixel original wastes space. The tool reduces pixels before quality.</li>\n"
   "<li><strong>Use black and white for signatures and text.</strong> It removes grey paper texture, which is most of the file.</li>\n"
   "<li><strong>Save as JPG, not PNG,</strong> for photos. PNG is lossless and often ten times bigger.</li>\n"
   "<li><strong>Stay a little under the limit.</strong> Forms count KB differently; the tools keep files under the limit in bytes so they pass either way.</li>\n</ol>\n")],
 faq_title="Reduce Image Size in KB",
 faq=[("How do I reduce image size in KB?", "Type the limit into the tool above, add the photo and download. It keeps the photo as sharp as the limit allows."),
      ("How do I reduce a photo from MB to KB?", "The same way: a 5 MB photo becomes, for example, 50 KB in one step. Pick the size or type it in."),
      ("How can I reduce image size in KB on my phone?", "Open this page in your phone's browser and use the tool; it works on Android and iPhone without an app."),
      ("Which is the best free image size reducer?", "One that keeps your photo private, shows the exact result and lets you set the pixels and the minimum as well as the maximum. That is what these tools are built for."),
      ("Can I reduce the size of a PDF too?", "Yes: open one of the PDF size tools above, or [[post:articles/compress-pdf-to-200kb|compress a PDF to 200 KB]].")],
 src=["ssc", "ibps", "upsc", "neet", "protean", "gst_kk", "nsp", "uidai", "nist", "mdn_blob", "pdflib"],
 related=["resize-image-to-20kb", "resize-image-to-50kb", "compress-pdf-to-200kb", "signature-resize", "kb-to-mb-converter"]))
