# Do you pay tax on money family or friends send you from abroad? By the recipient's country, checked October 2026.
# Builds tools/embeds/money-from-abroad-tax-checker.html from tools/templates/money-from-abroad-tax-checker.html.
#   python3 seo/data_money_from_abroad.py
import json, os, html

# Status: none (no tax), allow (tax-free up to an allowance), tax (taxable), report (no tax, but report it)
# id, name, region, (family status, text), (others status, text), what to report, source key
ROWS = [
 ("us", "United States", "Americas", ("report", "Gifts are not income for the person receiving them, so there is no tax to pay."),
  ("report", "Same: no tax for the recipient. Any gift tax is the giver's matter."),
  "Report gifts from foreign individuals totalling more than $100,000 a year on Form 3520. If someone in the US sends money abroad paying with cash or a money order, the sender pays a 1% remittance tax (from 2026); bank and card transfers are exempt.", "us_irs"),
 ("ca", "Canada", "Americas", ("none", "No tax: Canada has no gift tax and gifts are not income."), ("none", "No tax on gifts."), "Nothing to report for a cash gift. Income from investing it is taxable as usual.", "pwc_ca"),
 ("mx", "Mexico", "Americas", ("none", "Gifts from a spouse, parents, grandparents, children or grandchildren are exempt from income tax."),
  ("tax", "Gifts from others above a small yearly exemption are taxed as income."), "Report exempt gifts over MXN 600,000 in your annual tax return.", "pwc_mx"),
 ("gb", "United Kingdom", "Europe", ("none", "No tax for the person receiving a gift."), ("none", "No tax for the recipient."),
  "Nothing to report as the recipient. Inheritance tax can apply to a UK giver's estate if they die within 7 years of the gift.", "uk_gift"),
 ("ie", "Ireland", "Europe", ("allow", "Capital Acquisitions Tax of 33% applies above lifetime thresholds: €420,000 from a parent (from 7 October 2026), lower for other relatives. Each giver can also give you €3,000 a year tax-free."),
  ("allow", "33% above a much lower lifetime threshold for non-relatives, after the €3,000 a year small-gift exemption."), "File a CAT return if you receive more than 80% of your threshold.", "ie_cat"),
 ("de", "Germany", "Europe", ("allow", "Gift tax (Schenkungsteuer) is paid by the recipient above 10-year allowances: €500,000 from a spouse, €400,000 from a parent, €200,000 from a grandparent, €20,000 from your children (to parents), siblings and others."),
  ("allow", "€20,000 tax-free over 10 years, then 30% to 50%."), "Report every gift to the tax office within 3 months, even if no tax is due.", "de_erbstg"),
 ("fr", "France", "Europe", ("allow", "Gift duty (droits de donation) is paid by the recipient if they have lived in France for 6 of the last 10 years. Allowances over 15 years: €100,000 from each parent, plus up to €31,865 extra for a cash gift from a parent or grandparent under 80; €15,932 from a brother or sister."),
  ("allow", "Only €1,594 tax-free, then 60%."), "Declare a cash gift (don manuel) to the tax office online within a month of it being revealed, using form 2735 or the online service.", "fr_don"),
 ("es", "Spain", "Europe", ("allow", "Gift tax (Impuesto sobre Donaciones) is paid by the recipient and set by each region. Most regions cut it to almost nothing for gifts from parents, children and spouses when the gift is properly documented, often in front of a notary."),
  ("tax", "Taxed at the regional rates, which can be high for non-relatives."), "File the gift tax return within 30 working days of the gift.", "es_gift"),
 ("it", "Italy", "Europe", ("allow", "Gift tax is 4% above €1 million for each child or spouse, 6% above €100,000 for siblings. Gifts from someone living abroad are taxed only on assets located in Italy."),
  ("tax", "8% with no allowance for non-relatives, if the tax applies."), "Large gifts should be made by notarial deed.", "it_gift"),
 ("nl", "Netherlands", "Europe", ("allow", "Gift tax (schenkbelasting) is paid by the recipient above yearly allowances: €6,908 from your parents in 2026, or a one-off €33,129 if you are 18 to 40."),
  ("allow", "€2,769 a year tax-free, then 30% to 40%."), "File a gift tax return if a gift goes over the allowance.", "nl_gift"),
 ("pl", "Poland", "Europe", ("report", "Gifts from the closest family (spouse, children, parents, siblings, step-family) are fully exempt, but only if you report them on form SD-Z2 within 6 months and the money arrives by bank transfer."),
  ("tax", "Taxed above modest allowances, at 3% to 20%."), "Miss the 6-month SD-Z2 deadline and the family exemption is lost.", "pl_gift"),
 ("in", "India", "Asia", ("none", "Gifts from relatives (parents, children, spouse, siblings and their spouses, and other relatives defined in the law) are fully exempt, whatever the amount."),
  ("tax", "If gifts from non-relatives total more than ₹50,000 in a year, the whole amount is taxed as income, not just the excess."), "Show exempt gifts in your income tax return. Money an NRI sends to their own account in India is not a gift and is not taxed.", "in_gift"),
 ("pk", "Pakistan", "Asia", ("none", "Foreign remittances received through banks or licensed exchange companies are not taxed and, within the legal limit, cannot be treated as unexplained income."),
  ("none", "Same protection for remittances through official channels."), "Keep the bank's remittance proof (PRC) for your tax return.", "pk_fbr"),
 ("bd", "Bangladesh", "Asia", ("none", "No gift tax, and remittances through official channels are not taxed."), ("none", "No gift tax."), "Use official channels so the money is documented.", "pwc_bd"),
 ("ph", "Philippines", "Asia", ("none", "Money received from family abroad is not taxed for the recipient."), ("none", "No tax for the recipient; donor's tax (6%) is the giver's matter."), "Nothing to report as the recipient.", "pwc_ph"),
 ("vn", "Vietnam", "Asia", ("none", "Remittances from abroad are exempt from personal income tax."), ("allow", "Cash remittances are exempt; a 10% tax applies to gifts of property, shares and some other assets."), "Nothing to report for cash remittances.", "pwc_vn"),
 ("id", "Indonesia", "Asia", ("none", "No gift tax. Gifts from immediate family are not income."), ("tax", "Gifts from people outside the immediate family can count as taxable income."), "List large gifts in your annual tax return.", "pwc_id"),
 ("th", "Thailand", "Asia", ("allow", "Gifts from parents, children or a spouse are tax-free up to ฿20 million a year; 5% on the excess."),
  ("allow", "Gifts from others are tax-free up to ฿10 million a year; 5% on the excess."), "Since 2024, residents' own foreign income is taxed when brought into Thailand, so keep proof that money is a gift and not your earnings.", "th_gift"),
 ("my", "Malaysia", "Asia", ("none", "No gift tax; foreign money received by resident individuals is exempt from income tax."), ("none", "No gift tax."), "Nothing to report for a gift.", "pwc_my"),
 ("sg", "Singapore", "Asia", ("none", "No gift tax; foreign-sourced income received by individuals is generally exempt."), ("none", "No gift tax."), "Nothing to report for a gift.", "sg_iras"),
 ("cn", "China", "Asia", ("none", "No gift tax, and family support from abroad is not income."), ("none", "No gift tax."), "Converting foreign currency to yuan is limited to US$50,000 per person per year without extra paperwork.", "pwc_cn"),
 ("hk", "Hong Kong", "Asia", ("none", "No gift tax or tax on money received from abroad."), ("none", "No gift tax."), "Nothing to report.", "pwc_hk"),
 ("jp", "Japan", "Asia", ("allow", "Gift tax is paid by the recipient on gifts above ¥1.1 million a year, at 10% to 55%."), ("allow", "Same ¥1.1 million a year allowance, at higher rates."),
  "File a gift tax return by 15 March for gifts over ¥1.1 million in the previous year. Banks report incoming foreign transfers over ¥1 million to the tax office.", "jp_gift"),
 ("kr", "South Korea", "Asia", ("allow", "Gift tax is paid by the recipient above 10-year deductions: ₩50 million from parents to an adult child (₩20 million for a minor), ₩600 million from a spouse, plus up to ₩100 million more around a marriage or birth."),
  ("allow", "₩10 million over 10 years from other relatives; nothing from non-relatives. Rates 10% to 50%."), "File a gift tax return within 3 months of the end of the month you receive the gift.", "kr_gift"),
 ("ae", "United Arab Emirates", "Middle East", ("none", "No income tax or gift tax for individuals."), ("none", "No tax."), "Nothing to report.", "pwc_ae"),
 ("sa", "Saudi Arabia", "Middle East", ("none", "No income tax or gift tax for individuals."), ("none", "No tax."), "Nothing to report.", "pwc_sa"),
 ("eg", "Egypt", "Africa", ("none", "No gift or inheritance tax; remittances are not taxed."), ("none", "No gift tax."), "Nothing to report.", "pwc_eg"),
 ("ng", "Nigeria", "Africa", ("none", "No gift or inheritance tax; remittances are not taxed."), ("none", "No gift tax."), "Nothing to report.", "pwc_ng"),
 ("za", "South Africa", "Africa", ("none", "The recipient pays nothing. Donations tax (20%, 25% on large amounts) is charged on South African resident givers only."), ("none", "Same: no tax for the recipient."), "Nothing to report as the recipient.", "za_sars"),
 ("au", "Australia", "Oceania", ("none", "No gift tax, and genuine gifts are not income."), ("none", "No gift tax."), "Keep evidence that large amounts are gifts, not loans or payment for work; the ATO may ask.", "pwc_au"),
 ("nz", "New Zealand", "Oceania", ("none", "No gift tax since 2011."), ("none", "No gift tax."), "Nothing to report.", "pwc_nz"),
]
PWC = "https://taxsummaries.pwc.com/%s/individual/other-taxes"
SRC = {
 "us_irs": ("IRS: gifts from foreign persons (Form 3520)", "https://www.irs.gov/businesses/gifts-from-foreign-person"),
 "uk_gift": ("GOV.UK: inheritance tax on gifts", "https://www.gov.uk/inheritance-tax/gifts"),
 "ie_cat": ("Revenue: Capital Acquisitions Tax thresholds", "https://www.revenue.ie/en/gains-gifts-and-inheritance/cat-thresholds-rates-and-aggregation-rules/cat-thresholds.aspx"),
 "ie_b27": ("Grant Thornton: Budget 2027 analysis (CAT thresholds)", "https://www.grantthornton.ie/insights/budget/budget-2027-analysis/"),
 "de_erbstg": ("German Inheritance and Gift Tax Act, section 16 (allowances)", "https://www.gesetze-im-internet.de/erbstg_1974/__16.html"),
 "fr_don": ("Service-Public.fr: tax on a manual gift (don manuel)", "https://www.service-public.fr/particuliers/vosdroits/F14203"),
 "es_gift": ("Pellicer & Heredia: gift tax in Spain 2026", "https://www.pellicerheredia.com/en/gifts-tax-spain/"),
 "it_gift": ("Italy Handbook: gift tax rules in Italy", "https://italyhandbook.com/gift-tax-rules-in-italy-when-you-need-to-pay/"),
 "nl_gift": ("ABN AMRO: gift tax allowances and rates 2026", "https://www.abnamro.nl/nl/prive/themas/schenken/tarieven-vrijstellingen-schenkbelasting.html"),
 "pl_gift": ("Polish Ministry of Finance: inheritance and gift tax", "https://podatki-arch.mf.gov.pl/en/inheritance/"),
 "in_gift": ("Business Standard: gifts from non-relatives above ₹50,000 are taxable", "https://www.business-standard.com/article/pf/gifts-received-from-non-relatives-of-above-rs-50-000-a-year-are-taxable-122101801086_1.html"),
 "pk_fbr": ("Federal Board of Revenue: circular on foreign remittances", "https://download1.fbr.gov.pk/Docs/20218311282133965CircularNO.05ofForeignRemittances.pdf"),
 "th_gift": ("HLB Thailand: foreign income tax changes in force", "https://www.hlbthai.com/?p=158982"),
 "th_gift2": ("Taxes for Expats: Thailand (gift exemptions)", "https://www.taxesforexpats.com/country_guides/thailand.html"),
 "sg_iras": ("IRAS: income received from overseas", "https://www.iras.gov.sg/taxes/individual-income-tax/basics-of-individual-income-tax/what-is-taxable-what-is-not/income-received-from-overseas"),
 "jp_gift": ("Ministry of Finance Japan: gift tax", "https://www.mof.go.jp/english/tax_policy/tax_system/asset/03_3.pdf"),
 "kr_gift": ("Financial News: gift tax deductions for children (2026)", "https://en.fnnews.com/news/202605121659105425"),
 "za_sars": ("SARS: donations tax", "https://www.sars.gov.za/types-of-tax/donations-tax/"),
 "pwc_chart": ("PwC Worldwide Tax Summaries: inheritance and gift tax rates", "https://taxsummaries.pwc.com/quick-charts/inheritance-and-gift-tax-rates"),
}
for k, c, n in [("pwc_ca", "canada", "Canada"), ("pwc_mx", "mexico", "Mexico"), ("pwc_bd", "bangladesh", "Bangladesh"), ("pwc_ph", "philippines", "Philippines"),
                ("pwc_vn", "vietnam", "Vietnam"), ("pwc_id", "indonesia", "Indonesia"), ("pwc_my", "malaysia", "Malaysia"), ("pwc_cn", "peoples-republic-of-china", "China"),
                ("pwc_hk", "hong-kong-sar", "Hong Kong"), ("pwc_ae", "united-arab-emirates", "United Arab Emirates"), ("pwc_sa", "saudi-arabia", "Saudi Arabia"),
                ("pwc_eg", "egypt", "Egypt"), ("pwc_ng", "nigeria", "Nigeria"), ("pwc_au", "australia", "Australia"), ("pwc_nz", "new-zealand", "New Zealand")]:
    SRC[k] = ("PwC Worldwide Tax Summaries: %s, other taxes" % n, PWC % c)
WORD = {"none": "No tax", "allow": "Tax-free up to an allowance", "tax": "Can be taxed", "report": "No tax, but you may need to report it"}
SHORT = {"none": "No tax", "allow": "Allowance", "tax": "Taxed", "report": "Report"}

def build():
    e = html.escape
    data = [dict(id=r[0], n=r[1], f=list(r[3]), o=list(r[4]), r=r[5], s=SRC[r[6]][1], sn=SRC[r[6]][0]) for r in ROWS]
    opts = "".join('<option value="%s">%s</option>' % (r[0], e(r[1])) for r in sorted(ROWS, key=lambda r: r[1]))
    rows = "".join('<tr><td><b>%s</b></td><td><span class="mfa-s mfa-%s">%s</span></td><td><span class="mfa-s mfa-%s">%s</span></td></tr>'
                   % (e(r[1]), r[3][0], SHORT[r[3][0]], r[4][0], SHORT[r[4][0]]) for r in ROWS)
    t = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "tools", "templates", "money-from-abroad-tax-checker.html"), encoding="utf-8").read()
    t = (t.replace("__OPTS__", opts).replace("__ROWS__", rows).replace("__N__", str(len(ROWS)))
          .replace("__DATA__", json.dumps(data, ensure_ascii=False, separators=(",", ":"))).replace("__WORD__", json.dumps(WORD)))
    out = os.path.join(os.path.dirname(os.path.abspath(__file__)), "tools", "embeds", "money-from-abroad-tax-checker.html")
    open(out, "w", encoding="utf-8").write(t)
    print("wrote", out, len(t), "bytes,", len(ROWS), "countries")

if __name__ == "__main__":
    build()
