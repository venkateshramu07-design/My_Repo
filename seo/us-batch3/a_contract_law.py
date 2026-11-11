from common import *
body = f"""
<p><strong>Quick answer:</strong> A contract is an agreement the law will enforce. It needs four things: <strong>mutual assent</strong> (an offer and an acceptance), <strong>consideration</strong> (each side gives or promises something of value), <strong>capacity</strong> (the parties are adults of sound mind), and <strong>legality</strong> (a lawful purpose). Most contracts can be oral. Some, such as land sales, contracts that can't be completed within a year, and sales of goods worth $500 or more, must be in writing and signed to be enforced. In the US, contract law is mostly state law, built on court decisions. The Uniform Commercial Code (UCC) governs sales of goods.</p>

<h2>The Elements of a Valid Contract</h2>
{table(["Element", "What it means", "Example of a problem"], [
 ["Offer", "A definite proposal showing willingness to be bound on specific terms", "An advert or a price list is usually an invitation to make offers, not an offer"],
 ["Acceptance", "Unconditional agreement to the offer's terms, communicated as the offer requires", "A reply that changes the terms is a counter-offer, not an acceptance (with UCC exceptions between merchants)"],
 ["Consideration", "Each side gives or promises something of value in exchange", "A promise of a gift, or a promise to do what you already must do, usually isn't enforceable"],
 ["Capacity", "Parties are of legal age and mentally competent", "Contracts with minors are generally voidable by the minor"],
 ["Legality", "The purpose and terms are lawful", "An agreement to do something illegal is void"],
])}
<p>Together, offer and acceptance are called <em>mutual assent</em>, or a "meeting of the minds". Courts judge it objectively, by what the parties said and did, not by what they privately intended.</p>

<h2>Contract vs Agreement</h2>
<p>Every contract is an agreement, but not every agreement is a contract. Agreeing to meet a friend for dinner lacks consideration and any intention to create legal obligations, so neither side can sue. An agreement becomes a contract when it has all five elements above and, where required, is in writing.</p>

<h2>When a Contract Must Be in Writing (Statute of Frauds)</h2>
<p>Every state has a statute of frauds requiring certain contracts to be written and signed by the party you want to enforce them against. The common categories:</p>
{ul([
 "Sale or transfer of an interest in land (including most leases longer than one year).",
 "Contracts that can't be performed within one year of being made.",
 "Sales of goods for $500 or more (UCC § 2-201).",
 "Promises to pay someone else's debt (a guarantee).",
 "Contracts in consideration of marriage, such as prenuptial agreements.",
 "Promises by an executor to pay an estate's debts from their own money.",
])}
<p>Electronic signatures and records satisfy writing requirements under the federal E-SIGN Act and the Uniform Electronic Transactions Act, adopted in almost every state.</p>

<h2>Types of Contracts</h2>
{table(["Type", "Meaning"], [
 ["Express", "Terms stated in words, oral or written"],
 ["Implied in fact", "Formed by conduct (you order at a restaurant and are expected to pay)"],
 ["Bilateral", "A promise for a promise (most commercial contracts)"],
 ["Unilateral", "A promise in exchange for an act (a reward for finding a lost dog)"],
 ["Void", "Never enforceable (for example, an illegal purpose)"],
 ["Voidable", "Valid, but one party can cancel it (minors, fraud, duress)"],
 ["Unenforceable", "Valid but can't be enforced in court (for example, an oral contract the statute of frauds says must be written)"],
 ["Adhesion (standard form)", "Take-it-or-leave-it terms such as software terms of service; enforced, but unfair terms can be struck down as unconscionable"],
])}

<h2>Defenses: When a Contract Won't Be Enforced</h2>
{ul([
 "<strong>Lack of capacity</strong> (minors, mental incapacity, sometimes intoxication).",
 "<strong>Misrepresentation or fraud</strong> about a material fact that the other side relied on.",
 "<strong>Duress or undue influence:</strong> agreement obtained by improper threats or pressure.",
 "<strong>Mistake:</strong> both parties wrong about a basic fact; a one-sided mistake rarely helps.",
 "<strong>Unconscionability:</strong> terms so one-sided and unfairly obtained that a court won't enforce them.",
 "<strong>Illegality or public policy</strong>, for example some non-compete clauses, depending on the state.",
 "<strong>Statute of frauds:</strong> a required writing is missing.",
])}

<h2>Breach of Contract and Remedies</h2>
<p>A breach is a failure to perform without a legal excuse. A <em>material</em> breach (one that defeats the purpose of the deal) lets the other side end the contract and sue; a minor breach only gives a right to damages. To win a breach case you generally prove that there was a contract, that you performed or were excused, that the other party breached, and that you suffered loss as a result.</p>
{table(["Remedy", "What it does"], [
 ["Expectation damages", "Puts you where you would have been if the contract had been performed (the usual measure)"],
 ["Reliance damages", "Repays what you spent relying on the contract"],
 ["Restitution", "Returns any benefit you gave the other side"],
 ["Consequential damages", "Covers foreseeable knock-on losses (for example, lost profits), if they were foreseeable when the contract was made"],
 ["Liquidated damages", "An amount fixed in the contract, enforced if it's a reasonable estimate and not a penalty"],
 ["Specific performance", "A court order to perform; used when money isn't enough, typically for land or unique goods"],
])}
<p>Punitive damages are generally not available for a breach of contract alone. You must also act reasonably to limit your losses, which is known as the duty to mitigate.</p>

<h2>Where Contract Law Comes From</h2>
{ul([
 "<strong>Common law</strong> (judge-made rules in each state) governs services, employment, real estate and most other contracts.",
 "<strong>UCC Article 2</strong>, adopted in every state except Louisiana, governs sales of goods and relaxes several common-law rules for merchants.",
 "<strong>The Restatement (Second) of Contracts</strong> isn't law, but courts rely on it heavily as a summary of the common law.",
])}

<h2>Statute of Limitations</h2>
<p>The time limit to sue for breach depends on the state and the type of contract, commonly 3 to 6 years for written contracts and less for oral ones. For sales of goods the UCC sets 4 years (§ 2-725), which the parties can shorten to no less than one year. Check your state's statute, because missing the deadline usually ends the claim.</p>

<h2>Frequently Asked Questions</h2>
{faq([
 ("Is a verbal agreement a legally binding contract?", "Often yes, if it has all the elements and isn't in a category the statute of frauds requires to be written. It's harder to prove, so put important deals in writing."),
 ("What are the 4 elements of a contract?", "Mutual assent (offer and acceptance), consideration, capacity and legality. Some lists show offer and acceptance separately, making five."),
 ("Is a text message or email a contract?", "It can be. Emails and texts can show offer and acceptance, and can satisfy writing and signature requirements under the E-SIGN Act and state electronic transaction laws."),
 ("Can you back out of a contract after signing?", "Only if the contract or the law gives you a right to cancel (for example, the FTC's three-day rule for some door-to-door sales), if the other side agrees, or if a defense such as fraud applies."),
 ("What makes a contract void?", "An illegal purpose, or the absence of a basic element such as consideration or mutual assent."),
 ("Does a contract need to be notarized?", "Usually not. Notarization proves who signed; it's required only for specific documents, such as some real estate deeds."),
])}

{sources([
 ("Cornell Law School LII, Wex: Contract", "https://www.law.cornell.edu/wex/contract"),
 ("Cornell LII, Wex: Statute of frauds", "https://www.law.cornell.edu/wex/statute_of_frauds"),
 ("UCC § 2-201: Formal requirements; statute of frauds", "https://www.law.cornell.edu/ucc/2/2-201"),
 ("UCC § 2-725: Statute of limitations in contracts for sale", "https://www.law.cornell.edu/ucc/2/2-725"),
 ("15 U.S.C. § 7001: Electronic signatures in global and national commerce (E-SIGN)", "https://www.law.cornell.edu/uscode/text/15/7001"),
 ("FTC: Buyer's Remorse: The FTC's Cooling-Off Rule", "https://consumer.ftc.gov/articles/buyers-remorse-ftcs-cooling-rule-may-help"),
])}

<p>Next: <a href="__POST:tort-law__">tort law explained</a>, the other half of civil liability.</p>
{related([L["career"], L["pf"], L["layoff"], L["rentbuy"]])}
<p><em>Checked November 2026. This is general information about US law, not legal advice; rules differ by state.</em></p>
"""
save("contract-law", "Contract Law Basics: What Makes an Agreement Legally Binding", ["Career & Workplace", "Personal Finance"], body)
