from common import *
contract = 400000
sba_fee = contract * 0.006
rates = [0.01, 0.02, 0.03]
prem_rows = [[money(contract), f"{r:.0%}", money(contract * r)] for r in rates]
lic = 15000
lic_rows = [[money(lic), f"{r:.0%}", money(lic * r)] for r in (0.01, 0.03, 0.05, 0.10)]
body = f"""
<p><strong>Quick answer:</strong> A surety bond is a three-party guarantee. A surety company promises a third party (the <strong>obligee</strong>, often a government agency or project owner) that you (the <strong>principal</strong>) will meet an obligation, such as finishing a contract, following licensing law or paying subcontractors. If you don't, the surety pays valid claims and then recovers the money from you. That's the key difference from insurance. You pay a yearly premium, typically a small percentage of the bond amount, set mainly by your credit and finances. Federal construction contracts over $150,000 require performance and payment bonds.</p>

<h2>How a Surety Bond Works</h2>
{table(["Party", "Role"], [
 ["Principal (you, the business)", "Buys the bond and must perform the obligation"],
 ["Obligee (government, project owner, court)", "Requires the bond and is protected by it"],
 ["Surety (bonding company)", "Guarantees performance; pays valid claims and recovers them from the principal under an indemnity agreement"],
])}
<p><strong>Surety bond vs insurance:</strong> insurance protects you and expects to pay losses. A surety bond protects someone else, and expects no losses, because you are liable to repay every claim. That's why sureties underwrite much like lenders, looking at credit, financial statements and experience.</p>

<h2>Main Types of Surety Bonds</h2>
{table(["Type", "Examples", "Who needs it"], [
 ["Contract bonds", "Bid, performance, payment and maintenance bonds", "Contractors on public and many private construction projects"],
 ["License and permit bonds", "Contractor, auto dealer, mortgage broker, freight broker, notary, collection agency bonds", "Businesses that state or federal law requires to be bonded before licensing"],
 ["Court bonds", "Appeal, injunction, probate (executor/administrator), guardianship bonds", "Parties to lawsuits and people managing estates"],
 ["Public official bonds", "Treasurer, tax collector bonds", "Officials handling public money"],
 ["Fidelity bonds (related)", "Employee dishonesty, ERISA plan bonds", "Employers; strictly a form of insurance, but often sold alongside surety"],
])}

<h2>Contract Bonds on Construction Projects</h2>
{ul([
 "<strong>Bid bond:</strong> guarantees you'll sign the contract and provide the other bonds if you win the bid.",
 "<strong>Performance bond:</strong> guarantees you'll complete the work according to the contract.",
 "<strong>Payment bond:</strong> guarantees you'll pay subcontractors, workers and suppliers. On public jobs they can't put liens on public property, so this is their protection.",
])}
<p>The federal Miller Act requires performance and payment bonds on federal construction contracts over $150,000. For contracts of $35,000 to $150,000, the contracting officer must choose two or more alternative payment protections (FAR 28.102-1, as of March 2026). Most states have similar "Little Miller Act" laws for state and local public works, with their own thresholds.</p>

<h2>What a Surety Bond Costs</h2>
<p>You pay a premium, not the bond amount. Rates depend on the bond type, the amount, your personal and business credit, financial strength and experience. Small license bonds for applicants with strong credit are often around 1% to 3% of the bond amount a year, and much more with poor credit. Contract bond premiums are typically a few percent of the contract value, and lower for large contracts and established firms.</p>
<h3>Example: a $15,000 contractor license bond</h3>
{table(["Bond amount", "Premium rate", "Yearly premium"], lic_rows)}
<h3>Example: performance and payment bonds on a $400,000 job</h3>
{table(["Contract value", "Premium rate", "Premium"], prem_rows)}
<p>Rates here are for illustration; get quotes from licensed surety agents or companies. The Treasury's list of approved sureties (Circular 570) shows which companies can write bonds on federal contracts, and up to what amount.</p>

<h2>Can't Get Bonded? The SBA Surety Bond Guarantee</h2>
<p>The U.S. Small Business Administration guarantees bonds for small contractors who can't get them through regular channels. Key terms:</p>
{ul([
 "Contracts up to <strong>$9 million</strong> (non-federal) and <strong>$14 million</strong> (federal).",
 f"The small business pays SBA a fee of <strong>0.6% of the contract price</strong> for performance and payment bond guarantees ({money(sba_fee)} on a {money(contract)} contract), plus the surety's own premium. There's no SBA fee for bid bond guarantees.",
 "You apply through an SBA-authorized surety agent.",
])}

<h2>How to Get a Surety Bond</h2>
<ol>
<li><strong>Find out exactly which bond you need:</strong> the obligee (licensing board, agency or project owner) sets the bond type, amount and form.</li>
<li><strong>Apply with a surety company or licensed surety agent.</strong> Small license bonds often need only a short application and a credit check. Contract bonds need financial statements, work history, and details of the job and your backlog.</li>
<li><strong>Sign the indemnity agreement,</strong> which makes you (and often your spouse and the company's owners) personally liable to reimburse the surety.</li>
<li><strong>Pay the premium</strong> and file the bond with the obligee.</li>
<li><strong>Renew</strong> each term as required, and keep it in force while the licence or contract obligation lasts.</li>
</ol>

<h2>What Happens if Someone Makes a Claim</h2>
<p>The surety investigates. If the claim is valid, it pays the obligee or claimant, up to the bond amount, or arranges for the work to be completed. It then seeks full repayment from you under the indemnity agreement, including its costs. An unresolved claim can make it hard or impossible to get bonded again, so dealing with disputes early is cheaper.</p>

<h2>Frequently Asked Questions</h2>
{faq([
 ("What is a surety bond in simple terms?", "A guarantee bought from a surety company that you will meet a legal or contractual obligation; if you don't, the surety pays the injured party and you repay the surety."),
 ("How much does a $10,000 surety bond cost?", "Often about $100 to $300 a year for applicants with good credit, and more with weaker credit, depending on the bond type."),
 ("Is a surety bond the same as insurance?", "No. Insurance protects you; a surety bond protects the obligee, and you must repay any claim."),
 ("Do I get the bond premium back?", "No. The premium is the cost of the guarantee, not a deposit."),
 ("What does it mean to be bonded and insured?", "The business carries a surety bond (often a license bond) and liability insurance; customers can claim against the bond for licence-law violations and against the insurance for accidental damage."),
 ("Can you get a surety bond with bad credit?", "Often yes, especially for small license bonds, at higher rates. For contract bonds, the SBA guarantee program helps small contractors who can't qualify otherwise."),
])}

{sources([
 ("U.S. Small Business Administration: Surety bonds", "https://www.sba.gov/funding-programs/surety-bonds"),
 ("FAR 28.102-1: Bonds and other financial protections, general (Miller Act thresholds)", "https://www.acquisition.gov/far/28.102-1"),
 ("40 U.S.C. § 3131: Bonds of contractors of public buildings or works (Miller Act)", "https://www.law.cornell.edu/uscode/text/40/3131"),
 ("U.S. Treasury: Circular 570, surety companies acceptable on federal bonds", "https://www.fiscal.treasury.gov/surety-bonds/circular-570.html"),
])}

{related([L["consolidation"], L["score"], L["career"], L["pf"]])}
<p><em>Premium examples are illustrative; thresholds as published (checked November 2026). This article is for information and isn't legal or financial advice.</em></p>
"""
save("surety-bonds", "Surety Bonds Explained: Types, Costs and How to Get Bonded", ["Personal Finance", "Career & Workplace"], body)
