# Cash you must declare when flying in or out, by country, checked against official pages in October 2026.
# Builds tools/embeds/cash-declaration-checker.html: a route checker (from, to, amount) plus a searchable table.
#   python3 seo/data_cash_limits.py
import json, os
from data_deposit_limits import ECB  # ECB euro reference rates, 23 October 2026
FX = {k: v for k, v in ECB.items()}  # units per euro
FX.update({"AED": ECB["USD"] * 3.6725, "SAR": ECB["USD"] * 3.75, "QAR": ECB["USD"] * 3.64})  # fixed US dollar pegs

EU = ["Austria", "Belgium", "Bulgaria", "Croatia", "Cyprus", "Czech Republic", "Denmark", "Estonia", "Finland", "France",
      "Germany", "Greece", "Hungary", "Ireland", "Italy", "Latvia", "Lithuania", "Luxembourg", "Malta", "Netherlands",
      "Poland", "Portugal", "Romania", "Slovakia", "Slovenia", "Spain", "Sweden"]

# id, name, region, threshold currency, arrival amount, departure amount, "gt" (more than) or "ge" (or more),
# mode ("declare", or "ask": only if customs ask), how to declare, local-currency or extra rules, source key
ROWS = [
 ("us", "United States", "Americas", "USD", 10000, 10000, "gt", "declare",
  "Tick yes on the customs declaration and file FinCEN Form 105 with CBP (on paper or online) when you arrive or leave.",
  "Covers cash, travellers' cheques, money orders and bearer cheques. Splitting money between travellers to stay under $10,000 is a crime (structuring).", "us_cbp"),
 ("ca", "Canada", "Americas", "CAD", 10000, 10000, "ge", "declare",
  "Report it to the CBSA on form E677. Leaving by air, report at the CBSA office in the airport before security.",
  "Counts Canadian and foreign cash together, plus monetary instruments.", "ca_cbsa"),
 ("mx", "Mexico", "Americas", "USD", 10000, 10000, "gt", "declare",
  "Fill in the customs cash declaration (declaración de internación o extracción de cantidades en efectivo) at the airport.",
  "Undeclared cash over the limit is fined 20% to 40% of the excess; over $30,000 undeclared can be prosecuted.", "mx_cash"),
 ("br", "Brazil", "Americas", "USD", 10000, 10000, "gt", "declare",
  "Declare it online in the e-DBV (electronic traveller's declaration) before you pass customs.",
  "Limit raised from R$10,000 to US$10,000 on 30 December 2022.", "br_rfb"),
 ("gb", "United Kingdom", "Europe", "GBP", 10000, 10000, "ge", "declare",
  "Declare online on GOV.UK up to 72 hours before you travel, or to Border Force at the airport.",
  "Applies entering or leaving Great Britain. Northern Ireland uses €10,000 for travel to or from outside the EU.", "uk_cash"),
 ("eu", "European Union (27 countries)", "Europe", "EUR", 10000, 10000, "ge", "declare",
  "Fill in the EU cash declaration form at customs (some countries also accept it online) when entering or leaving the EU.",
  "Only for travel into or out of the EU. Between EU countries there is no EU form, but several, such as Germany, make you disclose €10,000 or more if asked.", "eu_cash"),
 ("ch", "Switzerland", "Europe", "CHF", 10000, 10000, "ge", "ask",
  "No form. If a customs officer asks, you must say how much you carry, where it came from and what it is for.",
  "There is no limit on the amount; lying when asked is an offence.", "ch_bazg"),
 ("ae", "United Arab Emirates", "Middle East", "AED", 60000, 60000, "gt", "declare",
  "Declare in the iDeclare (Afseh) app or website before you fly, or at the customs desk.",
  "Precious metals and stones count. Cash carried by children under 18 is added to the parent's or guardian's total.", "ae_customs"),
 ("sa", "Saudi Arabia", "Middle East", "SAR", 40000, 40000, "ge", "declare",
  "Declare to ZATCA customs (the customs declaration office at the airport) when you arrive or leave.",
  "Lowered from SAR 60,000 to SAR 40,000 in mid-2026. Gold, precious metals, gems and fine jewellery count.", "sa_zatca"),
 ("qa", "Qatar", "Middle East", "QAR", 50000, 50000, "ge", "declare",
  "Fill in the declaration on the Qatar Customs online platform or at the customs office at the airport.",
  "Negotiable instruments, precious metals and precious stones count towards the limit.", "qa_customs"),
 ("in", "India", "Asia", "USD", 5000, None, "gt", "declare",
  "Declare on the Currency Declaration Form (CDF) to customs on arrival.",
  "Arriving: declare if foreign cash is over US$5,000, or all foreign exchange (cash plus travellers' cheques) is over US$10,000. Indian rupees: up to ₹25,000 per person in or out; foreign tourists may not bring rupees in.", "in_rbi"),
 ("pk", "Pakistan", "Asia", "USD", 10000, 5000, "gt", "declare",
  "Declare to Pakistan Customs (FBR) on the currency declaration form or app.",
  "Leaving: adults may take no more than US$5,000 per trip and US$30,000 a year (minors US$2,500), and Pakistani rupees only up to Rs 10,000.", "pk_fbr"),
 ("bd", "Bangladesh", "Asia", "USD", 10000, None, "gt", "declare",
  "Declare on form FMJ to customs on arrival.",
  "Bangladesh Bank raised the no-declaration limit to US$10,000 in 2020; the customs website still shows US$5,000, so declare above US$5,000 to be safe.", "bd_customs"),
 ("lk", "Sri Lanka", "Asia", "USD", 15000, 10000, "gt", "declare",
  "Declare to Sri Lanka Customs; keep the receipt you get on arrival.",
  "Leaving: declare over US$10,000, and declare any cash you declared on arrival even if it is less.", "lk_customs"),
 ("cn", "China", "Asia", "USD", 5000, 5000, "gt", "declare",
  "Use the red channel and fill in the customs declaration.",
  "Chinese yuan: no more than ¥20,000 in or out; the excess is not allowed. Leaving with more than US$5,000 in foreign cash needs a bank or SAFE permit.", "cn_customs"),
 ("hk", "Hong Kong", "Asia", "HKD", 120000, 120000, "gt", "declare",
  "Arriving: use the Red Channel and declare to a customs officer. Leaving: you must disclose it if an officer asks.",
  "Covers cash and bearer instruments such as travellers' cheques. Penalty up to HK$500,000 and two years in prison.", "hk_customs"),
 ("jp", "Japan", "Asia", "JPY", 1000000, 1000000, "gt", "declare",
  "Fill in the 'Declaration of Carrying of Means of Payment' and hand it to customs.",
  "Covers cash, cheques, securities and gold of 90% purity or more over 1 kg.", "jp_customs"),
 ("kr", "South Korea", "Asia", "USD", 10000, 10000, "gt", "declare",
  "Declare to customs on arrival or departure.",
  "Counts Korean won, foreign cash, travellers' cheques and bearer instruments together.", "kr_customs"),
 ("sg", "Singapore", "Asia", "SGD", 20000, 20000, "gt", "declare",
  "Declare in the MyICA app (Submit Cash (CBNI) Declaration) or on the ICA website within 72 hours before travel.",
  "There is no limit on the amount, only a reporting duty.", "sg_ica"),
 ("my", "Malaysia", "Asia", "USD", 10000, 10000, "gt", "declare",
  "Declare on the customs form when you arrive or leave.",
  "Penalty up to RM1 million, one year in prison, or both.", "my_bnm"),
 ("th", "Thailand", "Asia", "USD", 15000, 15000, "gt", "declare",
  "Declare to Thai Customs at the airport.",
  "Thai baht over ฿450,000 must also be declared. Some Thai sources still say US$20,000; declaring above US$15,000 is the safe choice.", "th_gov"),
 ("id", "Indonesia", "Asia", "IDR", 100000000, 100000000, "ge", "declare",
  "Declare to Indonesian Customs on the customs declaration.",
  "Taking out Rp100 million or more in rupiah also needs Bank Indonesia's permission beforehand.", "id_setkab"),
 ("ph", "Philippines", "Asia", "USD", 10000, 10000, "gt", "declare",
  "Declare the whole amount in the eTravel customs declaration (e-CDF), with the source and purpose.",
  "Philippine pesos: no more than ₱50,000 in or out without written permission from the central bank (BSP).", "ph_bsp"),
 ("au", "Australia", "Oceania", "AUD", 10000, 10000, "ge", "declare",
  "Tell the Australian Border Force (arriving, tick the box on the incoming passenger card) and complete a cross-border movement report.",
  "There is no upper limit. Customs can also ask about bearer instruments such as travellers' cheques of any value.", "au_austrac"),
 ("nz", "New Zealand", "Oceania", "NZD", 10000, 10000, "ge", "declare",
  "Complete a Border Cash Report online (within 72 hours before travel) or on paper.",
  "Cash includes travellers' cheques and cheques.", "nz_customs"),
 ("za", "South Africa", "Africa", "ZAR", 100000, 100000, "gt", "declare",
  "Declare it in the online SARS traveller declaration before you travel (compulsory for all travellers from 1 July 2026).",
  "The R100,000 threshold replaced the old R25,000 figure on 1 July 2026; it counts rand and foreign cash together.", "za_sars"),
 ("ng", "Nigeria", "Africa", "USD", 10000, 10000, "gt", "declare",
  "Fill in the Nigeria Customs e-Currency Declaration form before you fly, or declare at the airport.",
  "Leaving with more than US$10,000 may also need Central Bank of Nigeria approval.", "ng_customs"),
]
SRC = {
 "us_cbp": ("CBP: money and other monetary instruments", "https://cbp.gov/travel/international-visitors/kbyg/money"),
 "ca_cbsa": ("CBSA: cross-border currency and monetary instruments reporting", "https://www.cbsa-asfc.gc.ca/publications/dm-md/d19/d19-14-1-eng.html"),
 "mx_cash": ("UnoTV: how much cash you can carry into or out of Mexico (SAT rules)", "https://www.unotv.com/turismo/con-cuanto-dinero-puedo-entrar-o-salir-de-mexico/"),
 "br_rfb": ("Receita Federal: Instrução Normativa RFB 2.117/2022", "http://normas.receita.fazenda.gov.br/sijut2consulta/link.action?idAto=127627"),
 "uk_cash": ("GOV.UK: bringing cash into or out of the UK", "https://www.gov.uk/bringing-cash-into-uk"),
 "eu_cash": ("European Commission: EU cash controls", "https://taxation-customs.ec.europa.eu/customs/prohibitions-restrictions/eu-cash-controls_en"),
 "ch_bazg": ("Swiss Federal Office for Customs and Border Security: cash, foreign currency, securities", "https://www.bazg.admin.ch/en/cash-foreign-currency-securities"),
 "ae_customs": ("UAE Government portal: customs clearance for travellers", "https://u.ae/en/information-and-services/visiting-and-exploring-the-uae/Tourist-services-and-information/Customs-clearance"),
 "sa_zatca": ("ZATCA: travellers' declaration", "https://zatca.gov.sa/en/RulesRegulations/Taxes/Pages/customs-individual/Travel-pages/declare.aspx"),
 "qa_customs": ("Gulf Times: travellers with cash above QR50,000 must declare (Qatar Customs)", "https://www.gulf-times.com/article/727843/qatar/travelers-to-qatar-with-cash-or-kind-above-qr-50000-advised-to-declare-at-airport"),
 "in_rbi": ("Reserve Bank of India: FAQ on foreign exchange facilities for individuals", "https://www.rbi.org.in/scripts/FAQView.aspx?Id=66"),
 "pk_fbr": ("Federal Board of Revenue: SRO 1864 on currency declaration", "https://download1.fbr.gov.pk/SROs/2022101116105519224sro1864.pdf"),
 "bd_customs": ("Bangladesh Customs: currency regulations", "https://bangladeshcustoms.gov.bd/trade_info/currency_regulations"),
 "lk_customs": ("Sri Lanka Customs: passenger services", "https://www.customs.gov.lk/about-us/directorates-and-divisions/passenger-services-directorate/"),
 "cn_customs": ("Hunan Provincial Government: customs rules for currency", "https://enghunan.gov.cn/hneng/Services/EntryExit/IAQ/202412/t20241226_33537676.html"),
 "hk_customs": ("Hong Kong Customs: declaration requirements for currency and bearer instruments", "https://www.customs.gov.hk/en/cargo_clearance/instruments/index.html"),
 "jp_customs": ("Japan Customs: passenger procedures", "https://www.customs.go.jp/english/summary/passenger.htm"),
 "kr_customs": ("Korean Ministry of Foreign Affairs: customs information for travellers", "https://overseas.mofa.go.kr/ca-montreal-en/wpge/m_20346/contents.do"),
 "sg_ica": ("ICA Singapore: cash (CBNI) declaration", "https://go.gov.sg/cbnideclaration"),
 "my_bnm": ("Bank Negara Malaysia: declaration requirement for currency", "https://www.bnm.gov.my/-/declaration-requirement-for-currency"),
 "th_gov": ("Royal Thai Government: bringing Thai or foreign currency into or out of Thailand", "https://www.thailand.go.th/event-detail/009-010"),
 "id_setkab": ("Cabinet Secretariat of Indonesia: rules on carrying cash into and out of Indonesia", "https://setkab.go.id/en/govt-introduces-new-regulations-on-carrying-cash-into-and-outside-indonesia/"),
 "ph_bsp": ("Philippine Embassy Berne: bringing Philippine pesos and foreign currency (BSP rules)", "https://bernepe.dfa.gov.ph/other-information/frequently-asked-questions/bringing-of-phl-peso-and-foreign-currencies"),
 "au_austrac": ("AUSTRAC: cross-border movement reports", "https://www.austrac.gov.au/industry-and-business/obligations-and-guidance/your-amlctf-program/reporting-us/cross-border-movement-reports"),
 "nz_customs": ("New Zealand Customs: Border Cash Report", "https://customs.govt.nz/personal/travel-to-and-from-nz/border-cash-report"),
 "za_sars": ("SARS: excess currency policy (from 1 July 2026)", "https://www.sars.gov.za/sc-pa-01-06-excess-currency-external-policy/"),
 "ng_customs": ("BusinessDay: Nigeria Customs on declaring cash over $10,000", "https://businessday.ng/?p=1013612"),
}
SYM = {"USD": "US$", "CAD": "C$", "GBP": "£", "EUR": "€", "CHF": "CHF ", "AED": "AED ", "SAR": "SAR ", "QAR": "QAR ",
       "HKD": "HK$", "JPY": "¥", "SGD": "S$", "IDR": "Rp", "AUD": "A$", "NZD": "NZ$", "ZAR": "R"}
INPUT_CUR = ["USD", "EUR", "GBP", "INR", "AED", "SAR", "QAR", "CAD", "AUD", "NZD", "SGD", "HKD", "JPY", "CNY", "KRW",
             "MYR", "THB", "IDR", "PHP", "CHF", "ZAR", "BRL", "MXN"]

def fmt(a, cur):
    s = "{:,}".format(a)
    if cur == "IDR" and a >= 1000000: s = "{:,} million".format(a // 1000000)
    return SYM.get(cur, cur + " ") + s

def limit_text(r):
    _, _, _, cur, a, d, op, mode = r[:8]
    word = "" if op == "gt" else " or more"
    pre = "more than " if op == "gt" else ""
    if mode == "ask": return "If asked: %s%s" % (fmt(a, cur), word)
    if a == d: return "%s%s%s" % (pre, fmt(a, cur), word)
    out = "%s%s%s" % (pre, fmt(d, cur), word) if d else "see notes"
    return "In: %s%s%s · Out: %s" % (pre, fmt(a, cur), word, out)

def build():
    data = [dict(id=r[0], n=r[1], g=r[2], c=r[3], a=r[4], d=r[5], op=r[6], m=r[7], h=r[8], x=r[9], s=SRC[r[10]][1], sn=SRC[r[10]][0])
            for r in ROWS]
    opts = sorted([(r[1], r[0]) for r in ROWS if r[0] != "eu"] + [(e, "eu:" + e) for e in EU])
    sel = "".join('<option value="%s">%s</option>' % (v, n) for n, v in opts)
    cur = "".join('<option>%s</option>' % c for c in INPUT_CUR)
    rows = "".join('<tr data-q="%s"><td><b>%s</b></td><td>%s</td><td>%s</td></tr>'
                   % ((r[1] + " " + (" ".join(EU) if r[0] == "eu" else "")).lower(), r[1], limit_text(r), r[8]) for r in ROWS)
    fx = {k: round(v, 6) for k, v in FX.items() if k in INPUT_CUR or k in {r[3] for r in ROWS}}
    h = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "tools", "templates", "cash-declaration-checker.html"), encoding="utf-8").read()
    h = (h.replace("__FROM__", sel).replace("__CUR__", cur).replace("__ROWS__", rows).replace("__N__", str(len(ROWS) - 1 + len(EU)))
          .replace("__DATA__", json.dumps(data, ensure_ascii=False, separators=(",", ":")))
          .replace("__FX__", json.dumps(fx, separators=(",", ":"))))
    out = os.path.join(os.path.dirname(os.path.abspath(__file__)), "tools", "embeds", "cash-declaration-checker.html")
    open(out, "w", encoding="utf-8").write(h)
    print("wrote", out, len(h), "bytes,", len(ROWS), "rules")

if __name__ == "__main__":
    build()
