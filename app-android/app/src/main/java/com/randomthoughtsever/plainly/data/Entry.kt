package com.randomthoughtsever.plainly.data

/** One guide in the full index (from the light summary feed): enough for Explore, collections and Quick reads. */
data class Entry(
    val id: String, val title: String, val path: String, val url: String, val labels: List<String>, val published: String,
    val quick: String, val image: String?,
) {
    val label: String get() = labels.firstOrNull { it != "Start Here" } ?: "Guide"
    val isToolPage: Boolean get() = isToolPage(labels, title)
    val ref get() = Ref(id, title, image, label, 4)
}

private val TOOL_TITLE = Regex("\\b(calculator|converter|generator|maker|tracker|planner|counter|checker|resizer?|compress|photo size)\\b", RegexOption.IGNORE_CASE)

/**
 * A page that is mainly a tool: labelled Tools or Calculators, or named after one ("Raise Calculator: ...",
 * "Rent vs Buy Calculator", "... (Calculator)"). Those live in the Tools tab, not in Today or Reads.
 */
fun isToolPage(labels: List<String>, title: String): Boolean =
    labels.any { it == "Tools" || it == "Calculators" } || TOOL_TITLE.containsMatchIn(title)
