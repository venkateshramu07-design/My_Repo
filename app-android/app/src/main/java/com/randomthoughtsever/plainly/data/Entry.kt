package com.randomthoughtsever.plainly.data

/** One guide in the full index (from the light summary feed): enough for Explore, collections and Quick reads. */
data class Entry(
    val id: String, val title: String, val path: String, val url: String, val labels: List<String>, val published: String,
    val quick: String, val image: String?,
) {
    val label: String get() = labels.firstOrNull { it != "Start Here" } ?: "Guide"
    val ref get() = Ref(id, title, image, label, 4)
}
