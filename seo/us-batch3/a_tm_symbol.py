from common import *
body = f"""
<p><strong>Quick answer:</strong> <strong>™</strong> (trademark) can be used by anyone, on any brand name, logo or slogan they claim as their own. No filing is needed. <strong>®</strong> (registered) may be used only after the U.S. Patent and Trademark Office has registered the mark, and only for the goods or services listed in that registration. To type them: on Windows, <strong>Alt+0153</strong> gives ™ and <strong>Alt+0174</strong> gives ® (using the number pad); on a Mac, <strong>Option+2</strong> gives ™ and <strong>Option+R</strong> gives ®.</p>

<h2>TM vs ® vs SM vs ©</h2>
{table(["Symbol", "Means", "Who can use it", "Legal effect"], [
 ["™", "Trademark (goods)", "Anyone claiming rights in a brand for products, registered or not", "Puts the public on notice of your claim; doesn't create rights by itself"],
 ["℠", "Service mark (services)", "Anyone claiming rights in a brand for services", "Same as ™, for services"],
 ["®", "Federally registered mark", "Only the owner of a USPTO registration, only for the listed goods/services", "Lets you recover profits and damages from infringers without proving they knew of the registration"],
 ["©", "Copyright", "Owner of a creative work (text, images, music, software)", "Copyright notice; blocks the &quot;innocent infringer&quot; defence"],
])}

<h2>When You Can Use ® (and When You Can't)</h2>
{ul([
 "<strong>Only after registration</strong> is issued. A pending application isn't enough: use ™ or ℠ until the registration certificate arrives.",
 "<strong>Only for the goods or services in the registration.</strong> If your mark is registered for clothing, you can't put ® on the same name when used on coffee.",
 "<strong>Only for federal registration.</strong> A state trademark registration doesn't entitle you to use ®.",
 "<strong>Stop using it</strong> if the registration lapses or is cancelled.",
])}
<p>Using ® on an unregistered mark is improper and can count against you: it can be treated as an attempt to mislead, and can harm your own application or a later lawsuit.</p>
<h3>Why the ® is worth having</h3>
<p>Under the Lanham Act (15 U.S.C. § 1111), a registrant gives notice of registration by showing ®, or the words "Registered in U.S. Patent and Trademark Office" or "Reg. U.S. Pat. & Tm. Off.". If you don't, then in an infringement suit you can't recover the infringer's profits or your damages unless the infringer actually knew the mark was registered.</p>

<h2>How to Type ™ and ®</h2>
{table(["Device or app", "™", "®", "©"], [
 ["Windows (number pad)", "Alt + 0153", "Alt + 0174", "Alt + 0169"],
 ["Windows (any keyboard)", "Windows key + . (period), then Symbols", "Same", "Same"],
 ["Microsoft Word AutoCorrect", "Type (tm)", "Type (r)", "Type (c)"],
 ["Mac", "Option + 2", "Option + R", "Option + G"],
 ["iPhone / iPad", "Emoji keyboard: search &quot;trademark&quot;, or add a text replacement", "Emoji keyboard: search &quot;registered&quot;", "Emoji keyboard: search &quot;copyright&quot;"],
 ["Android (Gboard)", "?123, then =\\&lt; page", "Same page", "Same page"],
 ["HTML", "&amp;trade;", "&amp;reg;", "&amp;copy;"],
 ["Unicode", "U+2122", "U+00AE", "U+00A9"],
 ["Google Docs", "Insert > Special characters, search &quot;trade&quot;", "Search &quot;registered&quot;", "Search &quot;copyright&quot;"],
])}
<h3>Where to put the symbol</h3>
<p>Place it right after the mark, usually as superscript (raised and smaller): Brand™ or Brand®. You don't need it on every mention. Common practice is the first or most prominent use on a page, a package or an ad, and on the logo itself.</p>

<h2>How to Get the ®: Registering a Trademark</h2>
<ol>
<li><strong>Search</strong> the USPTO's trademark database for identical or similar marks used on related goods or services.</li>
<li><strong>File online</strong> with the USPTO: a base fee of $350 per class of goods or services. Surcharges apply if your application is missing information ($100 per class), if you write your own description instead of choosing one from the USPTO's ID Manual ($200 per class), and for each further 1,000 characters of your own description ($200).</li>
<li><strong>Examination:</strong> a USPTO examining attorney reviews the application and may issue an office action you must answer.</li>
<li><strong>Publication:</strong> if approved, the mark is published so others can oppose it within 30 days.</li>
<li><strong>Registration</strong> (if you're already using the mark) or a notice of allowance (if you filed on an intent-to-use basis, in which case you file proof of use before registration).</li>
</ol>
<h3>Keeping it registered</h3>
{table(["When", "Filing", "Fee (per class, online)"], [
 ["Between years 5 and 6 after registration", "Section 8 declaration of use", "$325"],
 ["Every 10 years", "Combined Section 8 declaration and Section 9 renewal", "$650"],
])}
<p>Fees: USPTO fee schedule, effective January 19, 2025 (revised November 1, 2026).</p>

<h2>Frequently Asked Questions</h2>
{faq([
 ("Can I use TM without registering?", "Yes. Anyone can use ™ (or ℠ for services) to claim a brand. It costs nothing and doesn't need a filing."),
 ("Is it illegal to use ® without registration?", "It's improper and can be treated as false marking. Courts and the USPTO have held it against owners, for example by refusing relief. Use ® only once the federal registration is issued."),
 ("What is the difference between ™ and ®?", "™ claims rights in a mark. ® shows the mark is federally registered, and gives you stronger remedies in court."),
 ("Do I need to put ® every time I use my brand name?", "No. Using it once prominently on each page, package or ad is common practice."),
 ("What does ℠ mean?", "Service mark: the ™ equivalent for brands of services (banking, consulting, restaurants) rather than goods."),
 ("How much does it cost to register a trademark?", "From $350 per class of goods or services with the USPTO when filed online using the ID Manual's descriptions, plus attorney fees if you hire one."),
 ("Is © the same as ®?", "No. © is for copyright in creative works; ® is for registered trademarks (brands)."),
])}

{sources([
 ("USPTO: What is a trademark?", "https://www.uspto.gov/trademarks/basics/what-trademark"),
 ("15 U.S.C. § 1111: Notice of registration", "https://www.law.cornell.edu/uscode/text/15/1111"),
 ("USPTO fee schedule (effective January 19, 2025)", "https://www.uspto.gov/learning-and-resources/fees-and-payment/uspto-fee-schedule"),
 ("USPTO: Trademark Search", "https://www.uspto.gov/trademarks/search"),
 ("The Unicode Standard: Letterlike Symbols (U+2122)", "https://www.unicode.org/charts/PDF/U2100.pdf"),
])}

{related([L["career"], L["pf"], L["ai_jobs"], L["raise"]])}
<p><em>Checked November 2026. This is general information, not legal advice; a trademark attorney can advise on a specific mark.</em></p>
"""
save("tm-symbol", "TM vs ® Symbol: When You Can Use Each and How to Type Them", ["Technology & AI", "Career & Workplace"], body)
