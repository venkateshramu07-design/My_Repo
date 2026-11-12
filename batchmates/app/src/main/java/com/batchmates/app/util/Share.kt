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
