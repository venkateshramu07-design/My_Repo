package com.randomthoughtsever.plainly.data

import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * "A Year of Money": a 12-month simulation played with the reader's own numbers (or typical ones). Each month brings
 * pay, bills, interest and the markets, then an event with a choice. Choices have consequences for money and
 * wellbeing, and each event links to the guide that explains it. Pure logic, so it's tested without a screen.
 */
object LifeSim {
    data class State(
        val month: Int, val income: Double, val spend: Double, val savings: Double, val invested: Double, val debt: Double,
        val debtRate: Double, val wellbeing: Int, val history: List<String> = emptyList(),
    ) {
        val netWorth get() = savings + invested - debt
    }

    data class Choice(val label: String, val result: String, val apply: (State) -> State)
    data class Event(val key: String, val emoji: String, val title: String, val text: (State) -> String, val choices: List<Choice>, val learn: Regex)

    fun start(n: Numbers): State {
        val income = if (n.income > 0) n.income else 3000.0
        val spend = if (n.spend > 0) n.spend.coerceAtMost(income * 1.1) else income * 0.8
        return State(0, income, spend, if (n.isSet) n.savings else income * 0.4, if (n.isSet) n.invested else 0.0,
            if (n.isSet) n.debt else income * 1.3, 0.22, 60)
    }

    /** One month: pay in, spending out, interest on debt, market move on investments, then the minimum debt payment. */
    fun month(s: State, market: Double): State {
        val interest = s.debt * s.debtRate / 12
        val minPay = if (s.debt > 0) maxOf(s.debt * 0.03, minOf(s.debt + interest, s.income * 0.02)) else 0.0
        var savings = s.savings + s.income - s.spend - minPay
        var debt = (s.debt + interest - minPay).coerceAtLeast(0.0)
        if (savings < 0) { debt += -savings * 1.0; savings = 0.0 } // short months go on the card
        return s.copy(month = s.month + 1, savings = savings, debt = debt, invested = s.invested * (1 + market))
    }

    /** The year's market moves (monthly), the same for everyone playing the same seed. */
    fun markets(seed: Long): List<Double> { val r = Random(seed); return List(12) { (r.nextDouble() * 0.06 - 0.022) } }
    fun events(seed: Long): List<Event> = EVENTS.shuffled(Random(seed)).take(12)

    fun score(start: State, end: State): Int {
        val gain = (end.netWorth - start.netWorth) / start.income // months of pay gained
        return (50 + gain * 12 + (end.wellbeing - 50) * 0.6).roundToInt().coerceIn(0, 100)
    }
    fun grade(score: Int) = when { score >= 85 -> "A"; score >= 70 -> "B"; score >= 55 -> "C"; score >= 40 -> "D"; else -> "E" }
    fun verdict(score: Int) = when {
        score >= 85 -> "Money master: you grew your wealth and kept your head."
        score >= 70 -> "Strong year: good choices outweighed the bumps."
        score >= 55 -> "Steady: you held your ground. A few choices cost you."
        score >= 40 -> "Tough year: debt or stress got the better of you."
        else -> "Rough ride. Try again and use the guides between months."
    }

    private fun wb(s: State, d: Int) = (s.wellbeing + d).coerceIn(0, 100)
    private fun State.pay(x: Double) = if (savings >= x) copy(savings = savings - x) else copy(savings = 0.0, debt = debt + (x - savings))
    private fun State.note(t: String) = copy(history = history + t)

    val EVENTS = listOf(
        Event("car", "🚗", "The car breaks down", { "The repair bill is ${fmt(it.income * 0.4)}." }, listOf(
            Choice("Pay from savings", "Ouch, but no interest.") { it.pay(it.income * 0.4).copy(wellbeing = wb(it, -3)) },
            Choice("Put it on the credit card", "It's paid, but the card charges interest.") { it.copy(debt = it.debt + it.income * 0.4, wellbeing = wb(it, -1)) },
            Choice("Take the bus for now", "Cheaper, but life gets harder.") { it.copy(spend = it.spend + it.income * 0.03, wellbeing = wb(it, -8)) },
        ), Regex("(?i)emergency fund|emergency")),
        Event("raise", "📈", "Time for your review", { "Your manager asks what you think you deserve." }, listOf(
            Choice("Ask for 8% with evidence", "You prepared, and got 6%.") { it.copy(income = it.income * 1.06, wellbeing = wb(it, 4)) },
            Choice("Accept whatever they offer", "You got the standard 3%.") { it.copy(income = it.income * 1.03) },
            Choice("Start looking for a new job", "Stressful, but you found 12% more.") { it.copy(income = it.income * 1.12, wellbeing = wb(it, -6)) },
        ), Regex("(?i)raise|salary")),
        Event("sale", "🛍️", "A big online sale", { "Your basket is worth ${fmt(it.income * 0.25)}." }, listOf(
            Choice("Buy it all", "Fun for a week.") { it.pay(it.income * 0.25).copy(wellbeing = wb(it, 3)) },
            Choice("Wait 48 hours, then decide", "You bought one thing you really wanted.") { it.pay(it.income * 0.05).copy(wellbeing = wb(it, 2)) },
            Choice("Close the app", "Nothing bought, a little FOMO.") { it.copy(wellbeing = wb(it, -1)) },
        ), Regex("(?i)budget|spend|subscription")),
        Event("market", "📉", "Markets drop 15%", { "Your investments are down. Everyone's talking about it." }, listOf(
            Choice("Sell everything", "You locked in the loss.") { it.copy(savings = it.savings + it.invested * 0.85, invested = 0.0, wellbeing = wb(it, -4)) },
            Choice("Hold and keep investing", "Markets usually recover. You kept going.") { it.copy(invested = it.invested * 0.85 + it.income * 0.05, savings = it.savings - it.income * 0.05) },
            Choice("Buy more with savings", "Bold. You bought cheap.") { val x = minOf(it.savings * 0.3, it.income); it.copy(savings = it.savings - x, invested = it.invested * 0.85 + x * 1.1) },
        ), Regex("(?i)invest|stock|market|sip")),
        Event("invest", "🌱", "Start investing?", { "A friend shows you an index fund. You could invest ${fmt(it.income * 0.1)} a month." }, listOf(
            Choice("Set up a monthly investment", "Small, automatic, for the long run.") { it.copy(spend = it.spend + it.income * 0.1, invested = it.invested + it.income * 0.6) },
            Choice("Not yet", "Your money stays in savings.") { it },
            Choice("Buy a hot crypto coin instead", "It halved.") { val x = minOf(it.savings, it.income * 0.3); it.copy(savings = it.savings - x + x * 0.5, wellbeing = wb(it, -3)) },
        ), Regex("(?i)invest|fund|crypto|bitcoin")),
        Event("card", "💳", "Your card's interest rate", { "You're paying ${"%.0f".format(it.debtRate * 100)}% on ${fmt(it.debt)}." }, listOf(
            Choice("Call and ask for a lower rate", "They cut it by 4 points.") { it.copy(debtRate = (it.debtRate - 0.04).coerceAtLeast(0.05), wellbeing = wb(it, 2)) },
            Choice("Pay an extra chunk off", "Less debt, less interest.") { val x = minOf(it.savings * 0.4, it.debt); it.copy(savings = it.savings - x, debt = it.debt - x, wellbeing = wb(it, 3)) },
            Choice("Ignore it", "The interest keeps running.") { it },
        ), Regex("(?i)credit card|debt|payoff")),
        Event("burnout", "😮‍💨", "You're exhausted", { "Work has been relentless for weeks." }, listOf(
            Choice("Take a long weekend off", "Rested, and sharper at work.") { it.pay(it.income * 0.08).copy(wellbeing = wb(it, 12)) },
            Choice("Push through", "You got it done, but you're running on empty.") { it.copy(wellbeing = wb(it, -12)) },
            Choice("Daily 10-minute walks and earlier nights", "Free, and it helped.") { it.copy(wellbeing = wb(it, 8)) },
        ), Regex("(?i)burnout|stress|sleep")),
        Event("subs", "📺", "Subscription check", { "You have 7 subscriptions costing ${fmt(it.income * 0.04)} a month." }, listOf(
            Choice("Cancel the ones you don't use", "You cut the bill in half.") { it.copy(spend = it.spend - it.income * 0.02, wellbeing = wb(it, 1)) },
            Choice("Keep them all", "Nothing changes.") { it },
        ), Regex("(?i)subscription|budget")),
        Event("friend", "🤝", "A friend needs a loan", { "They ask to borrow ${fmt(it.income * 0.3)}." }, listOf(
            Choice("Lend it", "They paid back half so far.") { it.pay(it.income * 0.15).copy(wellbeing = wb(it, 2)) },
            Choice("Give a smaller gift instead", "Kind, and limited.") { it.pay(it.income * 0.05).copy(wellbeing = wb(it, 4)) },
            Choice("Say no kindly", "Awkward, but your plan is safe.") { it.copy(wellbeing = wb(it, -3)) },
        ), Regex("(?i)gift|friend|family|relationship")),
        Event("side", "💡", "A side hustle idea", { "You could freelance a few hours a week." }, listOf(
            Choice("Go for it", "Extra money, less free time.") { it.copy(income = it.income + it.income * 0.12, wellbeing = wb(it, -5)) },
            Choice("Learn a new skill instead", "No money yet, but a better CV.") { it.copy(wellbeing = wb(it, 3), income = it.income * 1.02) },
            Choice("Rest instead", "Weekends stay free.") { it.copy(wellbeing = wb(it, 4)) },
        ), Regex("(?i)career|skill|side")),
        Event("rent", "🏠", "Rent goes up", { "Your landlord adds ${fmt(it.income * 0.05)} a month." }, listOf(
            Choice("Negotiate", "You agreed on half the rise.") { it.copy(spend = it.spend + it.income * 0.025, wellbeing = wb(it, -1)) },
            Choice("Move somewhere cheaper", "Moving costs now, savings later.") { it.pay(it.income * 0.3).copy(spend = it.spend - it.income * 0.05, wellbeing = wb(it, -5)) },
            Choice("Just pay it", "Your budget is tighter.") { it.copy(spend = it.spend + it.income * 0.05) },
        ), Regex("(?i)rent|buy|mortgage")),
        Event("bonus", "🎉", "A surprise bonus!", { "You get ${fmt(it.income * 0.5)}." }, listOf(
            Choice("Pay down debt", "Less debt means less interest to pay.") { val x = it.income * 0.5; val d = minOf(x, it.debt); it.copy(debt = it.debt - d, savings = it.savings + x - d, wellbeing = wb(it, 4)) },
            Choice("Invest it", "It's working for you now.") { it.copy(invested = it.invested + it.income * 0.5, wellbeing = wb(it, 2)) },
            Choice("Treat yourself", "A great holiday.") { it.copy(wellbeing = wb(it, 10)) },
            Choice("Split: half saved, half fun", "Balanced.") { it.copy(savings = it.savings + it.income * 0.25, wellbeing = wb(it, 6)) },
        ), Regex("(?i)bonus|invest|debt")),
        Event("medical", "🩺", "An unexpected medical bill", { "The bill is ${fmt(it.income * 0.3)}." }, listOf(
            Choice("Pay it from savings", "Covered. This is what savings are for.") { it.pay(it.income * 0.3) },
            Choice("Ask for a payment plan", "Spread out, no interest.") { it.copy(spend = it.spend + it.income * 0.03) },
        ), Regex("(?i)emergency|health|insurance")),
        Event("habit", "🏃", "New year, new habit?", { "You want to get healthier." }, listOf(
            Choice("Expensive gym membership", "You went twice.") { it.copy(spend = it.spend + it.income * 0.03, wellbeing = wb(it, 1)) },
            Choice("Free: a 10-minute daily walk", "Tiny, and it stuck.") { it.copy(wellbeing = wb(it, 6)) },
        ), Regex("(?i)habit|health")),
        Event("phone", "📱", "Your phone is old", { "The newest model costs ${fmt(it.income * 0.35)}." }, listOf(
            Choice("Buy it on monthly payments", "Shiny, and a new monthly bill.") { it.copy(spend = it.spend + it.income * 0.03, wellbeing = wb(it, 3)) },
            Choice("Buy last year's model outright", "Half the price.") { it.pay(it.income * 0.17).copy(wellbeing = wb(it, 2)) },
            Choice("Fix the battery and keep it", "Cheap and fine.") { it.pay(it.income * 0.03) },
        ), Regex("(?i)credit card|subscription|debt")),
        Event("tax", "🧾", "Tax time", { "You could claim credits you're owed if you check." }, listOf(
            Choice("Spend an evening checking", "You found a refund.") { it.copy(savings = it.savings + it.income * 0.15, wellbeing = wb(it, -1)) },
            Choice("Skip it", "Nothing gained.") { it },
        ), Regex("(?i)tax|credit")),
        Event("autosave", "🤖", "Automate your savings?", { "Move ${fmt(it.income * 0.05)} to savings on payday, automatically." }, listOf(
            Choice("Set it up", "You stop noticing it, and it adds up.") { it.copy(spend = it.spend - it.income * 0.03, wellbeing = wb(it, 2)) },
            Choice("I'll save what's left", "There's rarely anything left.") { it.copy(spend = it.spend + it.income * 0.01) },
        ), Regex("(?i)save|budget|automat")),
        Event("ai", "🧠", "AI arrives at work", { "Your team starts using AI tools." }, listOf(
            Choice("Learn them early", "You became the go-to person.") { it.copy(income = it.income * 1.04, wellbeing = wb(it, 2)) },
            Choice("Avoid them", "You fell behind a bit.") { it.copy(wellbeing = wb(it, -3)) },
        ), Regex("(?i)ai |career")),
        Event("wedding", "💒", "A friend's wedding abroad", { "Flights and hotel: ${fmt(it.income * 0.45)}." }, listOf(
            Choice("Go", "Unforgettable.") { it.pay(it.income * 0.45).copy(wellbeing = wb(it, 10)) },
            Choice("Send a gift and video call", "Kind and cheaper.") { it.pay(it.income * 0.05).copy(wellbeing = wb(it, -2)) },
        ), Regex("(?i)travel|budget|abroad")),
        Event("sleep", "🌙", "Late-night scrolling", { "You're sleeping 5 hours a night." }, listOf(
            Choice("Phone out of the bedroom", "Hard for three days, then great.") { it.copy(wellbeing = wb(it, 9)) },
            Choice("Coffee will fix it", "It didn't.") { it.copy(spend = it.spend + it.income * 0.01, wellbeing = wb(it, -6)) },
        ), Regex("(?i)sleep|habit")),
    )

    var symbol = "$"
    fun fmt(x: Double) = (if (x.roundToInt() < 0) "-" else "") + "$symbol${"%,d".format(kotlin.math.abs(x.roundToInt()))}"
}
