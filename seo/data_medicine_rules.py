# Taking common restricted medicines abroad, by destination, checked against official pages in October 2026.
# Builds tools/embeds/medicine-travel-checker.html from tools/templates/medicine-travel-checker.html.
#   python3 seo/data_medicine_rules.py
import json, os, html

MEDS = [  # key, label, examples
 ("pain", "Strong painkillers", "codeine, tramadol, oxycodone, morphine"),
 ("sleep", "Sleeping pills and anxiety medicine", "zolpidem, diazepam, alprazolam, lorazepam"),
 ("adhd", "ADHD medicine", "Adderall, Vyvanse, Ritalin, Concerta"),
 ("cold", "Cold and flu tablets", "pseudoephedrine, such as Sudafed and Actifed"),
 ("cbd", "CBD oil and cannabis medicine", "CBD oil, gummies, medical cannabis"),
 ("rx", "Other prescription medicine", "blood pressure, diabetes, antibiotics, the pill"),
]
# Status: ban (not allowed), permit (approval before you fly), docs (prescription, declare), ok (usually fine), check
# id, name, {med: (status, text)}, general allowance, source key
ROWS = [
 ("jp", "Japan", {
   "pain": ("permit", "Opioid painkillers such as morphine and oxycodone are narcotics: you need a permit from a Regional Narcotics Control Department before you travel. Over-the-counter medicines containing codeine are not allowed."),
   "sleep": ("docs", "Allowed for personal use with your prescription, within Japan's limits (about a month's supply); larger amounts need an Import Confirmation (Yunyu Kakunin-sho) before you fly."),
   "adhd": ("ban", "Adderall, Vyvanse, Dexedrine and other amphetamine medicines are banned, even with a prescription. For Ritalin or Concerta (methylphenidate), check with the Ministry of Health before you travel."),
   "cold": ("ban", "Cold medicines with pseudoephedrine, such as Sudafed and Actifed, are banned, along with Vicks inhalers."),
   "cbd": ("check", "Only products with no THC at all are legal; anything containing THC is treated as cannabis."),
   "rx": ("ok", "Up to one month's supply with a copy of your prescription. More than that needs an Import Confirmation before you leave home."),
 }, "jp_mhlw"),
 ("ae", "United Arab Emirates", {
   "pain": ("permit", "Controlled medicine. Get free prior approval online from the Ministry of Health (MOHAP) before you travel, or declare it on arrival with your prescription and a medical report. No more than 3 months' supply."),
   "sleep": ("permit", "Controlled medicine. Get MOHAP prior approval online, or declare it on arrival with your prescription and a medical report. No more than 3 months' supply."),
   "adhd": ("permit", "Controlled medicine. Get MOHAP prior approval online before you travel, with your prescription and a medical report. No more than 3 months' supply."),
   "cold": ("docs", "Carry the prescription or packaging; some cold medicines are controlled, so check the active ingredient."),
   "cbd": ("ban", "CBD and any cannabis product are illegal, even if bought legally elsewhere."),
   "rx": ("ok", "Up to 3 months' supply with your prescription. Passengers only changing planes do not need approval."),
 }, "ae_mohap"),
 ("sa", "Saudi Arabia", {
   "pain": ("permit", "Apply for a clearance permit in the SFDA Controlled Drugs System before you travel, with a prescription or medical report from the last 6 months. No more than 30 days' supply or the length of your stay."),
   "sleep": ("permit", "Controlled: apply for an SFDA clearance permit before you travel. No more than 30 days' supply or the length of your stay."),
   "adhd": ("permit", "Controlled: apply for an SFDA clearance permit before you travel. No more than 30 days' supply or the length of your stay."),
   "cold": ("check", "Check the active ingredient with the SFDA; some cold medicines are controlled."),
   "cbd": ("ban", "Cannabis products, including CBD, are illegal."),
   "rx": ("ok", "Allowed with a recent prescription or medical report; no permit needed if the medicine is not controlled."),
 }, "sa_sfda"),
 ("qa", "Qatar", {
   "pain": ("permit", "Apply on the Ministry of Public Health's online permit system at least 5 working days before you travel (in use since August 2026)."),
   "sleep": ("permit", "Apply on the Ministry of Public Health's online permit system at least 5 working days before you travel."),
   "adhd": ("permit", "Apply on the Ministry of Public Health's online permit system at least 5 working days before you travel."),
   "cold": ("check", "Check the active ingredient on the Ministry of Public Health's list of controlled medicines."),
   "cbd": ("ban", "Cannabis products, including CBD, are illegal."),
   "rx": ("ok", "Carry a prescription; bring no more than about 30 days' supply."),
 }, "qa_moph"),
 ("sg", "Singapore", {
   "pain": ("permit", "Medicines with controlled ingredients need approval from the Health Sciences Authority (HSA) before you travel."),
   "sleep": ("permit", "Controlled ingredients need HSA approval before you travel. Check your medicine with HSA's online checker."),
   "adhd": ("permit", "Controlled: HSA approval before you travel. Approval is not guaranteed, so apply early."),
   "cold": ("check", "Check the ingredient with HSA's online checker before packing."),
   "cbd": ("ban", "CBD and cannabis products are illegal."),
   "rx": ("ok", "Up to 3 months' supply without approval if nothing in it is on HSA's controlled list."),
 }, "sg_hsa"),
 ("th", "Thailand", {
   "pain": ("permit", "Narcotic medicines need a Thai FDA permit, applied for online at least 15 days before you travel. No more than 30 days' supply; declare it at customs."),
   "sleep": ("docs", "Bring your prescription, no more than 30 days' supply, and declare it at customs."),
   "adhd": ("check", "Thailand treats amphetamine as a Category 1 narcotic, which is not allowed. Check methylphenidate (Ritalin, Concerta) on the Thai FDA's traveller permit site before you go."),
   "cold": ("docs", "Personal amounts with the original packaging."),
   "cbd": ("check", "THC is prohibited for travellers; check CBD products with the Thai FDA."),
   "rx": ("ok", "Personal supply, no more than 30 days of controlled medicine."),
 }, "th_fda"),
 ("kr", "South Korea", {
   "pain": ("permit", "Approval from the Ministry of Food and Drug Safety (MFDS) before you arrive, applied for online. A foreign prescription alone is not enough."),
   "sleep": ("permit", "Approval from MFDS before you arrive, whatever your nationality."),
   "adhd": ("permit", "Approval from MFDS before you arrive. Amphetamines are tightly controlled, so apply early; approval is not guaranteed."),
   "cold": ("check", "Check with MFDS; pseudoephedrine products are restricted."),
   "cbd": ("ban", "CBD and cannabis products are illegal."),
   "rx": ("ok", "Up to 6 bottles or 3 months' supply of non-controlled medicine."),
 }, "kr_mfds"),
 ("cn", "China", {
   "pain": ("docs", "Allowed within one prescription's maximum dose for your own use. Carry the prescription or medical records and declare the medicine on arrival."),
   "sleep": ("docs", "Allowed within one prescription's maximum dose. Carry the prescription and declare it on arrival."),
   "adhd": ("check", "Amphetamine medicines are not approved in China and may be refused at customs. Ask a Chinese embassy before you travel."),
   "cold": ("docs", "A reasonable personal amount in the original packaging."),
   "cbd": ("ban", "CBD and cannabis products are illegal."),
   "rx": ("ok", "A reasonable quantity for personal use; carry the prescription."),
 }, "cn_customs"),
 ("hk", "Hong Kong", {
   "pain": ("docs", "A reasonable quantity in your own baggage for personal use is exempt from a licence. Carry a doctor's letter or prescription."),
   "sleep": ("docs", "A reasonable personal quantity in your baggage, with a doctor's letter or prescription."),
   "adhd": ("docs", "A reasonable personal quantity in your baggage, with a doctor's letter or prescription."),
   "cold": ("ok", "Personal amounts are fine."),
   "cbd": ("ban", "CBD has been a dangerous drug since 1 February 2023; bringing in even a small amount can lead to prison."),
   "rx": ("ok", "A reasonable personal quantity."),
 }, "hk_gov"),
 ("id", "Indonesia", {
   "pain": ("check", "Morphine, codeine and similar medicines can be treated as illegal drugs. Carry a prescription and doctor's letter, and confirm with an Indonesian embassy first."),
   "sleep": ("check", "Sleeping pills can be treated as illegal drugs without paperwork. Carry a prescription and doctor's letter, and confirm with an Indonesian embassy."),
   "adhd": ("ban", "ADHD medicines such as dexamphetamine are illegal in Indonesia; travellers have been detained at Bali airport."),
   "cold": ("check", "Check with an Indonesian embassy."),
   "cbd": ("ban", "Cannabis products, including CBD, are illegal, with very heavy penalties."),
   "rx": ("ok", "Personal supply with a prescription."),
 }, "id_smart"),
 ("ph", "Philippines", {
   "pain": ("permit", "Medicines classed as dangerous drugs need authority from the Philippine Drug Enforcement Agency (PDEA) before you travel."),
   "sleep": ("permit", "Dangerous-drug medicines need PDEA authority before you travel; carry the prescription and a doctor's letter."),
   "adhd": ("permit", "Amphetamine and methylphenidate medicines need PDEA authority before you travel."),
   "cold": ("docs", "Personal amounts in the original packaging."),
   "cbd": ("ban", "Cannabis products are illegal."),
   "rx": ("ok", "Personal amounts in original packaging, with a prescription."),
 }, "ph_pdea"),
 ("gb", "United Kingdom", {
   "pain": ("docs", "Carry your prescription or a doctor's letter. Up to 3 months' supply; more than that needs a Home Office personal licence."),
   "sleep": ("docs", "Carry your prescription or a doctor's letter; up to 3 months' supply."),
   "adhd": ("docs", "Carry your prescription or a doctor's letter; up to 3 months' supply."),
   "cold": ("ok", "Personal amounts are fine."),
   "cbd": ("check", "Legal only with no controlled cannabinoids such as THC; many products fail this test."),
   "rx": ("ok", "Up to 3 months' supply."),
 }, "uk_med"),
 ("eu", "Schengen area (Europe)", {
   "pain": ("docs", "Travelling from another Schengen country: carry a Schengen certificate (Article 75), valid for up to 30 days. From outside: a prescription or doctor's letter; some countries have stricter national rules."),
   "sleep": ("docs", "From another Schengen country: Schengen certificate for up to 30 days. From outside: prescription or doctor's letter."),
   "adhd": ("docs", "From another Schengen country: Schengen certificate for up to 30 days; some countries, such as Finland, treat lisdexamfetamine (Vyvanse) as a narcotic with national rules."),
   "cold": ("ok", "Personal amounts are fine."),
   "cbd": ("check", "Rules differ by country; THC limits vary."),
   "rx": ("ok", "Personal supply for your stay with a prescription."),
 }, "eu_schengen"),
 ("us", "United States", {
   "pain": ("docs", "Declare it to CBP, keep it in the original container and carry your prescription. Personal amounts only (a guide is up to 90 days)."),
   "sleep": ("docs", "Declare it to CBP, in the original container, with your prescription."),
   "adhd": ("docs", "Declare it to CBP, in the original container, with your prescription."),
   "cold": ("ok", "Personal amounts are fine."),
   "cbd": ("check", "Federal and state rules differ; products with THC can be seized."),
   "rx": ("ok", "Up to about 90 days' supply in original containers."),
 }, "us_fda"),
]
SRC = {
 "jp_mhlw": ("Ministry of Health, Labour and Welfare of Japan: bringing medicines for personal use", "https://www.mhlw.go.jp/english/policy/health-medical/pharmaceuticals/01.html"),
 "jp_emb": ("Embassy of Japan in the USA: bringing medications into Japan", "https://www.us.emb-japan.go.jp/itpr_en/bringing-medications-to-japan.html"),
 "ae_mohap": ("UAE Ministry of Health and Prevention: permit to import medicines for personal use", "https://mohap.gov.ae/en/services/issue-of-permit-to-import-medicines-for-personal-use"),
 "sa_sfda": ("Saudi Food and Drug Authority: rules for carrying medicines", "https://www.sfda.gov.sa/en/news/5521869"),
 "qa_moph": ("Gulf Times: travellers can now apply online to carry controlled medicines to Qatar", "https://www.gulf-times.com/article/730569/qatar/travellers-can-now-apply-online-to-carry-controlled-medicines"),
 "sg_hsa": ("Health Sciences Authority Singapore: travelling with medication", "https://www.hsa.gov.sg/travelling-with-medication-and-medical-devices/personal-medications/"),
 "th_fda": ("Thai FDA: guidance for travellers carrying medication containing narcotic drugs", "https://en.fda.moph.go.th/entrepreneurs-narcotic-drugs-and-psychotropic-substances/guidance-for-travelers-under-treatment-carrying-01"),
 "kr_mfds": ("Embassy of Denmark in Korea: bringing prescription drugs into Korea (MFDS approval)", "https://sydkorea.um.dk/en/travel-and-residence/other-inquiries/Bringing-prescription-drugs-into-the-Republic-of-Korea"),
 "cn_customs": ("Shanghai Municipal Government: bringing medication for personal use", "https://english.shanghai.gov.cn/en-ImmigrationCustoms/20240516/632357dc3fbe483c923a5c296795b618.html"),
 "hk_gov": ("Hong Kong Government: CBD to be a dangerous drug, and rules for controlled medicines", "https://www.info.gov.hk/gia/general/202301/06/P2023010600476.htm"),
 "id_smart": ("Smartraveller (Australian Government): Indonesia travel advice", "https://www.smartraveller.gov.au/destinations/asia/indonesia"),
 "ph_pdea": ("Respicio & Co.: bringing prescription stimulants to the Philippines (PDEA permit)", "https://www.respicio.ph/commentaries/bringing-prescription-stimulants-to-the-philippines-pdea-permit-and-airport-rules"),
 "uk_med": ("GOV.UK: take medicine in or out of the UK", "https://www.gov.uk/take-medicine-in-or-out-uk"),
 "eu_schengen": ("EUR-Lex: Schengen certificate for carrying narcotic and psychotropic medicines (Article 75)", "https://eur-lex.europa.eu/eli/dec/1994/28(3)/oj"),
 "us_fda": ("US FDA: traveling with prescription medications", "https://www.fda.gov/drugs/fda-drug-info-rounds-video/traveling-prescription-medications"),
 "incb": ("International Narcotics Control Board: country rules for travellers", "https://www.incb.org/incb/en/travellers/index.html"),
 "cdc_med": ("CDC Yellow Book: traveling with prohibited or restricted medications", "https://www.cdc.gov/yellow-book/hcp/travelers-with-additional-considerations/traveling-with-prohibited-or-restricted-medications.html"),
}
SHORT = {"ban": "Banned", "permit": "Permit", "docs": "Paperwork", "ok": "OK", "check": "Check"}
WORD = {"ban": "Not allowed", "permit": "Permit before you fly", "docs": "Allowed with paperwork", "ok": "Usually fine", "check": "Check first"}

def build():
    e = html.escape
    data = [dict(id=r[0], n=r[1], m={k: list(v) for k, v in r[2].items()}, s=SRC[r[3]][1], sn=SRC[r[3]][0]) for r in ROWS]
    dest = "".join('<option value="%s">%s</option>' % (r[0], e(r[1])) for r in sorted(ROWS, key=lambda r: r[1]))
    meds = "".join('<option value="%s">%s (%s)</option>' % (k, e(l), e(x)) for k, l, x in MEDS)
    head = "".join("<th>%s</th>" % e(l.split(" and ")[0] if k != "cbd" else "CBD") for k, l, x in MEDS[:5])
    rows = "".join("<tr><td><b>%s</b></td>%s</tr>" % (e(r[1]), "".join('<td><span class="med-s med-%s">%s</span></td>' % (r[2][k][0], SHORT[r[2][k][0]]) for k, l, x in MEDS[:5])) for r in ROWS)
    t = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "tools", "templates", "medicine-travel-checker.html"), encoding="utf-8").read()
    t = (t.replace("__DEST__", dest).replace("__MEDS__", meds).replace("__HEAD__", head).replace("__ROWS__", rows)
          .replace("__DATA__", json.dumps(data, ensure_ascii=False, separators=(",", ":"))).replace("__WORD__", json.dumps(WORD)))
    out = os.path.join(os.path.dirname(os.path.abspath(__file__)), "tools", "embeds", "medicine-travel-checker.html")
    open(out, "w", encoding="utf-8").write(t)
    print("wrote", out, len(t), "bytes,", len(ROWS), "destinations")

if __name__ == "__main__":
    build()
