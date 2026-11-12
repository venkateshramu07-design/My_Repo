package com.batchmates.app.util

/** Throwback questions: one tap, one memory. They work for any school or college, anywhere. */
object Prompts {
    val all = listOf(
        "Who was the strictest teacher?",
        "Best canteen / cafeteria snack?",
        "Who sat next to you?",
        "Funniest thing that happened in class?",
        "Your most embarrassing moment at school?",
        "Which teacher do you still remember fondly?",
        "Best school trip ever?",
        "Who was always late — and what was the excuse?",
        "The song everyone was obsessed with that year?",
        "Who was the class clown?",
        "Your favourite subject then — and now?",
        "The punishment you'll never forget?",
        "Most memorable annual day / sports day moment?",
        "Who had the best handwriting?",
        "Where did the gang hang out after class?",
        "The exam you were sure you'd fail?",
        "A rumour that turned out to be true?",
        "What did you want to become back then?",
        "Who do you most want to see at the reunion?",
        "Best excuse for not doing homework?",
        "The nickname everyone had for you?",
        "Your first crush in class? (initials only 😉)",
        "What would you tell your younger self?",
        "Which classmate inspired you the most?",
        "The game you played every break?",
        "The gadget / toy everyone wanted that year?",
        "Most legendary bunk / skipped class?",
        "The teacher's catchphrase you still hear in your head?",
        "One thing about school you miss the most?",
        "Describe our batch in three words.",
    )

    fun random(except: String? = null): String = all.filter { it != except }.random()
}
