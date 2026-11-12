package com.batchmates.app.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.batchmates.app.BuildConfig
import com.batchmates.app.data.Group

enum class ShareTarget(val label: String, val packages: List<String>) {
    WHATSAPP("WhatsApp", listOf("com.whatsapp", "com.whatsapp.w4b")),
    INSTAGRAM("Instagram", listOf("com.instagram.android")),
    FACEBOOK("Facebook", listOf("com.facebook.orca", "com.facebook.katana")),
    MORE("More", emptyList()),
}

object Invites {
    fun link(code: String) = "https://${BuildConfig.INVITE_HOST}/j/$code"

    fun message(group: Group): String =
        "🎓 Remember us? I just found our batch on BatchMates!\n\n" +
            "🏫 ${group.schoolName}\n" +
            "📚 ${group.track.label} · Class of ${group.year} · ${group.sectionLabel}\n" +
            (group.milestone(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR))
                ?.let { "🎉 It's our $it-year reunion year!\n" } ?: "") +
            "👥 ${group.memberCount} classmate${if (group.memberCount == 1) "" else "s"} already in\n\n" +
            "Join our reunion 👉 ${link(group.inviteCode)}\n" +
            "or open BatchMates and enter code ${group.inviteCode}"

    /** A personal note for someone on the "missing" list: names and faces pull people in far better than a link. */
    fun missingMessage(group: Group, person: String, from: String, others: List<String>): String {
        val first = person.trim().substringBefore(' ')
        val names = others.filter { !it.equals(from, true) }.take(3)
        val crew = when {
            names.isEmpty() -> "the gang"
            group.memberCount > names.size + 1 -> names.joinToString(", ") + " and ${group.memberCount - names.size - 1} others"
            else -> names.joinToString(" and ")
        }
        return "Hey $first! 👋 It's $from from ${group.schoolName}, Class of ${group.year}.\n\n" +
            "$crew are back together on BatchMates and everyone keeps asking about you 😄\n\n" +
            "Join us 👉 ${link(group.inviteCode)}  (code ${group.inviteCode})"
    }

    fun reunionMessage(group: Group, r: com.batchmates.app.data.Reunion, dateText: String): String =
        "🥳 ${group.schoolName} · Class of ${group.year} REUNION!\n\n" +
            (if (r.lockedDate != null) "📅 $dateText\n" else "📅 Vote for a date: $dateText\n") +
            (if (r.place.isNotBlank()) "📍 ${r.place}\n" else "") +
            "✅ ${r.rsvpCount(com.batchmates.app.data.Rsvp.YES)} coming so far\n\n" +
            "RSVP in BatchMates 👉 ${link(group.inviteCode)}"

    /** Plain text to a specific app (or the share sheet). */
    fun shareText(context: Context, target: ShareTarget, text: String) {
        val base = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val installed = target.packages.firstOrNull { isInstalled(context, it) }
        if (installed != null && runCatching { context.startActivity(Intent(base).setPackage(installed)) }.isSuccess) return
        if (target == ShareTarget.WHATSAPP) { openUrl(context, "https://wa.me/?text=" + Uri.encode(text)); return }
        context.startActivity(Intent.createChooser(base, "Share").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** Shares straight into the chosen app; gracefully falls back to the web or the system sheet. */
    fun share(context: Context, target: ShareTarget, group: Group) {
        val text = message(group)
        val base = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val installed = target.packages.firstOrNull { isInstalled(context, it) }
        if (installed != null) {
            try {
                context.startActivity(Intent(base).setPackage(installed)); return
            } catch (_: ActivityNotFoundException) { /* fall through */ }
        }
        when (target) {
            ShareTarget.WHATSAPP -> openUrl(context, "https://wa.me/?text=" + Uri.encode(text))
            ShareTarget.FACEBOOK -> openUrl(context, "https://www.facebook.com/sharer/sharer.php?u=" + Uri.encode(link(group.inviteCode)))
            else -> context.startActivity(
                Intent.createChooser(base, "Invite your batchmates").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
        if (target == ShareTarget.INSTAGRAM) copy(context, text) // Instagram DMs paste more reliably
    }

    fun copy(context: Context, text: String) {
        val cm = context.getSystemService(ClipboardManager::class.java)
        cm.setPrimaryClip(ClipData.newPlainText("BatchMates invite", text))
    }

    fun openUrl(context: Context, url: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    private fun isInstalled(context: Context, pkg: String) =
        runCatching { context.packageManager.getPackageInfo(pkg, 0); true }.getOrDefault(false)
}
