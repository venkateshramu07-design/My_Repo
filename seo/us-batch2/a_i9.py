from common import *
body = f"""
<p><strong>Quick answer:</strong> Form I-9, Employment Eligibility Verification, is the form every US employer must complete for every person it hires, citizen or not, to confirm identity and permission to work. The employee fills in Section 1 <strong>no later than the first day of work</strong> and shows original documents; the employer examines them and completes Section 2 <strong>within three business days</strong>. The current form carries the expiry date 05/31/2027. It is never sent to the government: the employer keeps it and must produce it within three business days if inspected. Since March 2026, more paperwork errors are fined without a chance to correct them, at $288 to $2,861 per form.</p>

<h2>Form I-9 at a Glance</h2>
{table(["Question", "Answer"], [
 ["Who needs one?", "Every employee hired in the US after 6 November 1986, including US citizens. Not independent contractors or unpaid volunteers."],
 ["Who fills it in?", "Section 1: the employee. Section 2: the employer or its authorised representative."],
 ["Deadlines", "Section 1 by the end of the first day of work. Section 2 within 3 business days of the first day."],
 ["Which version?", "The edition showing expiry date 05/31/2027 (edition dates 08/01/23 and 01/20/25 are both in use)."],
 ["Where is it filed?", "Nowhere. The employer stores it."],
 ["How long is it kept?", "3 years after the hire date or 1 year after employment ends, whichever is later."],
 ["Cost", "Free, from uscis.gov/i-9. Nobody should charge you for the form."],
 ["Not the same as", "W-4 (tax withholding) or W-2 (year-end wage statement)."],
])}

<h2>Which Documents Count</h2>
<p>The employee chooses what to show: <strong>one document from List A, or one from List B plus one from List C</strong>. Documents must be original and unexpired. The employer may not ask for a particular document or for more than the form requires.</p>
{table(["List A: identity and work authorisation", "List B: identity only", "List C: work authorisation only"], [
 ["US passport or passport card", "Driver's licence or state ID card with photo", "Social Security card without a work restriction printed on it"],
 ["Permanent Resident Card (green card, Form I-551)", "Federal, state or local government ID with photo", "Certified birth certificate issued by a state, county or territory"],
 ["Employment Authorization Document with photo (Form I-766)", "School ID with photo", "Consular Report of Birth Abroad (FS-240, FS-545, DS-1350)"],
 ["Foreign passport with a temporary I-551 stamp or printed notation", "Voter registration card", "US Citizen ID Card (I-197) or resident citizen card (I-179)"],
 ["Foreign passport with Form I-94 showing a work-authorised nonimmigrant status (valid only for the sponsoring employer)", "US military card, draft record or military dependant's ID", "Native American tribal document"],
 ["Passport of Micronesia or the Marshall Islands with Form I-94", "Native American tribal document; Canadian driver's licence", "Employment authorisation document issued by the Department of Homeland Security (other than those in List A)"],
])}
<p>Under-18s who have none of the List B documents can use a school record or report card, a clinic, doctor or hospital record, or a day-care or nursery school record.</p>
<h3>Common document questions</h3>
{ul([
 "<strong>Laminated Social Security card:</strong> acceptable unless it says \"not valid if laminated\". A card stamped \"valid for work only with DHS authorization\" or \"not valid for employment\" is not a List C document.",
 "<strong>Lost a document?</strong> A receipt for a replacement is accepted for 90 days. The actual document must then be shown.",
 "<strong>Photocopies or phone photos:</strong> not acceptable, except a certified copy of a birth certificate.",
 "<strong>Hospital birth certificates</strong> with footprints are souvenirs, not certified copies.",
 "<strong>Expired documents:</strong> not acceptable, with narrow exceptions for automatic extensions that the employee can show by notice.",
])}

<h2>Step by Step</h2>
<h3>Employee: Section 1</h3>
<ol>
<li>Enter your legal name, other last names used, address and date of birth.</li>
<li>Social Security number, email and phone are optional, unless your employer uses E-Verify, in which case the Social Security number is required.</li>
<li>Tick one status box: US citizen, noncitizen national, lawful permanent resident, or noncitizen authorised to work. Give the USCIS or A-number, I-94 number or foreign passport number the box asks for, and the date your authorisation expires if it has one.</li>
<li>Sign and date. Do this on or before your first day of paid work, and not before you have accepted the job.</li>
<li>If someone helps you complete or translate it, they complete Supplement A.</li>
</ol>
<h3>Employer: Section 2</h3>
<ol>
<li>Examine the original documents in the employee's presence, or by live video under the remote procedure below.</li>
<li>Accept them if they reasonably appear genuine and to relate to the person. You are not expected to be a document expert.</li>
<li>Record the document title, issuing authority, number and expiry date.</li>
<li>Enter the employee's first day of employment, then your name, title, signature, date, and the business name and address.</li>
<li>Finish within three business days of the first day. For a job lasting under three days, finish on day one.</li>
</ol>
<h3>Worked deadline example</h3>
<p>An employee starts on Monday. Section 1 is due by the end of Monday. Section 2 is due by the end of Thursday. If the employee starts on a Thursday, Section 2 is due the following Tuesday, because Saturday and Sunday are not business days for a Monday-to-Friday business.</p>

<h2>Remote Hires</h2>
<p>There are two lawful routes.</p>
{ul([
 "<strong>An authorised representative.</strong> The employer may appoint anyone, such as a notary, an HR contractor or a trusted adult near the employee, to examine the documents in person and sign Section 2. The employer stays liable for their mistakes.",
 "<strong>The alternative procedure.</strong> Employers enrolled in E-Verify and in good standing may examine documents over a live video call. The employee first sends copies (front and back); the employer keeps those copies with the form and ticks the alternative-procedure box in Section 2. It must be offered consistently to all employees at a site, or to all remote employees.",
])}
<p>An employee can never complete Section 2 for themselves.</p>

<h2>Rehires and Reverification (Supplement B)</h2>
{ul([
 "<strong>Reverify</strong> on or before the date an employee's work authorisation expires. The employee again chooses which List A or List C document to show.",
 "<strong>Never reverify</strong> US citizens, noncitizen nationals, or lawful permanent residents who presented a green card, even when the card expires. Never reverify List B identity documents.",
 "<strong>Rehire within three years</strong> of the original form: you may update the old form with Supplement B, or complete a new one.",
 "<strong>Name changes:</strong> recording them is recommended, not required.",
])}

<h2>Storing and Producing I-9s</h2>
{ul([
 "<strong>Retention:</strong> the later of three years after the date of hire or one year after employment ends. Someone who worked for five years: keep for one year after they leave. Someone who worked for six months: keep until three years after their hire date.",
 "<strong>Format:</strong> paper, scanned or electronic. Electronic systems need audit trails, secure access and a reliable electronic signature process; from March 2026 gaps in these are treated as substantive violations.",
 "<strong>Keep them apart</strong> from personnel files. In an inspection you hand over I-9s only, and a separate binder or folder avoids disclosing anything else.",
 "<strong>Copies of documents</strong> are optional, unless you use E-Verify with certain documents or the remote procedure. If you copy, copy for everyone.",
 "<strong>Inspection:</strong> after a Notice of Inspection from ICE, you have at least three business days to produce the forms.",
 "<strong>Destroy on schedule.</strong> Forms kept past the retention date can still be fined if they contain errors.",
])}

<h2>Penalties, and What Changed in March 2026</h2>
{table(["Violation", "Civil fine (current inflation-adjusted amounts)"], [
 ["Paperwork violations (missing, late or incorrect forms)", "$288 to $2,861 per form"],
 ["Knowingly hiring or continuing to employ an unauthorised worker, first order", "$716 to $5,724 per worker"],
 ["Second order", "$5,724 to $14,308 per worker"],
 ["Third or later order", "$8,586 to $28,619 per worker"],
])}
<p>Fines for paperwork violations scale with the share of an employer's forms that are defective, then move up or down for business size, good faith, seriousness, any unauthorised workers found and history. Document fraud and discrimination carry separate penalties.</p>
<p><strong>The March 2026 change.</strong> For nearly 30 years, many errors were classed as "technical", and an employer given notice had at least 10 business days to correct them before any fine. On 16 March 2026, Immigration and Customs Enforcement revised its inspection guidance and moved more than ten categories of error into the "substantive" class, which is fined without a correction period. Reported examples include:</p>
{ul([
 "a missing work-authorisation expiry date in Section 1",
 "a missing employee date of birth",
 "missing dates next to signatures",
 "a missing name or title of the employer's representative in Section 2",
 "using the Spanish-language form outside Puerto Rico",
 "electronic I-9 systems without adequate audit trails or signature controls",
])}
<p>Using an out-of-date edition of the form is still treated as a technical error. The practical point is that self-audits now matter more: errors you find and correct properly before any inspection show good faith; errors an inspector finds first may simply be fined.</p>

<h2>How to Correct a Mistake Properly</h2>
<ol>
<li>Never use correction fluid, never backdate and never throw away the original.</li>
<li>Section 1 errors are corrected by the employee: one line through the error, the right information beside it, initials and today's date.</li>
<li>Section 2 errors are corrected by the employer in the same way.</li>
<li>If the form is beyond repair, complete a new one, attach it to the old one, and add a signed, dated note explaining why.</li>
<li>If a form was never completed, complete it now with the true current date.</li>
</ol>

<h2>What Employers May Not Do</h2>
{ul([
 "Ask for specific documents (\"I need to see your green card\") or reject documents that look genuine.",
 "Ask for documents before a job offer is accepted, or use the I-9 to screen applicants.",
 "Treat people differently because of citizenship status or national origin. Complaints go to the Justice Department's Immigrant and Employee Rights Section.",
 "Charge employees for the form or the process.",
])}

<h2>I-9 and E-Verify</h2>
<p>E-Verify is the federal online system that checks I-9 information against government records. It does not replace the I-9. It is voluntary for most employers, required for federal contractors with the E-Verify clause, and required for some or all employers by a number of states, including Arizona, Alabama, Mississippi and South Carolina, and Florida for private employers with 25 or more employees. A case must be opened within three business days of the start date.</p>

<h2>Frequently Asked Questions</h2>
{faq([
 ("What is an I-9 form for employment?", "The federal form on which a new employee attests to being authorised to work in the US and the employer records the documents that prove identity and authorisation."),
 ("Do I fill out the I-9 or does my employer?", "Both. You complete Section 1; your employer completes Section 2 after examining your documents."),
 ("Is an I-9 the same as a W-2 or a W-4?", "No. The I-9 verifies the right to work. The W-4 tells the employer how much tax to withhold. The W-2 reports your wages and tax after the year ends."),
 ("What documents do I need for an I-9?", "Either one List A document, such as a US passport or green card, or one List B document plus one List C document, such as a driver's licence and an unrestricted Social Security card."),
 ("Can I do my own I-9 verification?", "No. Section 2 must be completed by the employer or someone it authorises. A self-employed person with no employer does not need an I-9 at all."),
 ("Where do I find an I-9 form online?", "On the US Citizenship and Immigration Services website, uscis.gov/i-9. It is free."),
 ("What if I don't have my documents on the first day?", "You have three business days after starting to present them. A receipt for a replacement of a lost, stolen or damaged document is accepted for 90 days. If you cannot present acceptable documents or a receipt in time, the employer may end your employment."),
 ("Should I-9 forms be filed separately?", "Yes. Keep them separate from personnel files so they can be produced on their own within three business days."),
 ("Do independent contractors need an I-9?", "No, but a business may not use a contractor it knows is not authorised to work."),
 ("Does an I-9 expire?", "A completed I-9 does not. The blank form has an expiry date, and employers must use a current edition for new hires."),
])}

{sources([
 ("USCIS: Form I-9, Employment Eligibility Verification", "https://www.uscis.gov/i-9"),
 ("USCIS: Handbook for Employers (M-274)", "https://www.uscis.gov/i-9-central/form-i-9-resources/handbook-for-employers-m-274"),
 ("USCIS: Form I-9 acceptable documents", "https://www.uscis.gov/i-9-central/form-i-9-acceptable-documents"),
 ("USCIS: remote examination of documents (alternative procedure)", "https://www.uscis.gov/i-9-central/remote-examination-of-documents"),
 ("ICE: Form I-9 inspection fact sheet", "https://www.ice.gov/factsheets/i9-inspection"),
 ("Department of Justice: Immigrant and Employee Rights Section", "https://www.justice.gov/crt/immigrant-and-employee-rights-section"),
])}

{related([L["layoff"], L["career"], L["raise"], L["second"]])}
<p><em>Rules and penalty amounts as published by USCIS, ICE and the Department of Homeland Security, checked in November 2026. This article is for information and isn't legal advice.</em></p>
"""
save("form-i-9", "Form I-9 in 2026: Accepted Documents, Deadlines and the New Fine Rules", ["Career & Workplace"], body)
