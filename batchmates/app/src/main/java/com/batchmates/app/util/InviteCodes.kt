package com.batchmates.app.util

import java.security.SecureRandom

/** Short, human-friendly invite codes (no 0/O/1/I to avoid typos over the phone). */
object InviteCodes {
    private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    private const val LENGTH = 6
    private val random = SecureRandom()

    fun generate(): String = buildString { repeat(LENGTH) { append(ALPHABET[random.nextInt(ALPHABET.length)]) } }

    /** Accepts "ab3-k9z", a full invite link, or a pasted WhatsApp message containing the link. */
    fun normalize(input: String): String {
        val fromLink = Regex("""/j/([A-Za-z0-9]{4,12})""").find(input)?.groupValues?.get(1)
        val raw = fromLink ?: input
        return raw.uppercase().filter { it.isLetterOrDigit() }.take(12)
    }

    fun isPlausible(input: String) = normalize(input).length == LENGTH
}
