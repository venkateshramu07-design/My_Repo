package com.randomthoughtsever.plainly

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.io.File

/**
 * If the app ever crashes, the error is saved on the phone (nothing is sent anywhere), and the next start shows it once
 * with Copy and Share buttons, so the reader (or a tester) can pass it on. Uses plain Android views only, so the report
 * screen still works when the crash came from the app's own start-up.
 */
object CrashReport {
    private fun file(c: Context) = File(c.filesDir, "last-crash.txt")

    private fun stages(c: Context) = File(c.filesDir, "start-stages.txt")
    private const val SHOWN = "shown"

    /**
     * First thing at start-up (before Play's split loader). If the previous run never reached the screen, builds a report
     * from Android's own record of why that process ended (Android 11+: crashes, native crashes, start-up failures, kills),
     * which works even when the app's own code never got to run. Then starts a new stage log.
     */
    fun beginRun(c: Context) {
        runCatching {
            val log = stages(c)
            val previous = if (log.exists()) log.readText().trim() else null
            if (previous != null && !previous.endsWith(SHOWN) && !file(c).exists()) {
                file(c).writeText(buildString {
                    appendLine("Plainly ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                    appendLine(environment(c))
                    appendLine("Last start reached: ${previous.lines().joinToString(" > ")}")
                    appendLine()
                    append(exitReason(c))
                })
            }
            log.writeText("start")
        }
    }

    /** Records how far start-up got (cheap; a few short writes per start). */
    fun stage(c: Context, name: String) { runCatching { stages(c).appendText("\n$name") } }
    fun shown(c: Context) = stage(c, SHOWN)

    private fun environment(c: Context): String = buildString {
        append("Phone: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}), ABIs ${Build.SUPPORTED_ABIS.joinToString()}")
        val info = c.applicationInfo
        append("\nSplits: ${info.splitNames?.joinToString() ?: "none"}")
        val installer = runCatching {
            if (Build.VERSION.SDK_INT >= 30) c.packageManager.getInstallSourceInfo(c.packageName).installingPackageName
            else @Suppress("DEPRECATION") c.packageManager.getInstallerPackageName(c.packageName)
        }.getOrNull()
        append("\nInstalled by: ${installer ?: "unknown"}")
    }

    private fun exitReason(c: Context): String {
        if (Build.VERSION.SDK_INT < 30) return "Android's exit record needs Android 11 or newer."
        return runCatching {
            val am = c.getSystemService(android.app.ActivityManager::class.java)
            val list = am.getHistoricalProcessExitReasons(c.packageName, 0, 3)
            if (list.isEmpty()) "Android has no exit record for the app." else list.joinToString("\n\n") { e ->
                val trace = runCatching { e.traceInputStream?.use { it.readBytes().decodeToString().take(8_000) } }.getOrNull()
                buildString {
                    append("Exit: reason ${e.reason} (${reasonName(e.reason)}), status ${e.status}, importance ${e.importance}, ")
                    append("at ${java.util.Date(e.timestamp)}, process ${e.processName}\n")
                    append("Description: ${e.description}")
                    if (!trace.isNullOrBlank()) append("\nTrace:\n$trace")
                }
            }
        }.getOrElse { "Couldn't read Android's exit record: $it" }
    }

    private fun reasonName(r: Int) = when (r) {
        1 -> "exited by itself"; 2 -> "signal"; 3 -> "low memory"; 4 -> "crash"; 5 -> "native crash"; 6 -> "not responding"
        7 -> "start-up failure"; 8 -> "permission change"; 9 -> "excessive resources"; 10 -> "user request"; 11 -> "user stopped"
        12 -> "dependency died"; 13 -> "other"; 14 -> "frozen"; 15 -> "package state change"; 16 -> "package updated"; else -> "unknown"
    }

    /** Call as early as possible (Application.attachBaseContext). Keeps the system's handler, so Android still closes the app. */
    fun install(c: Context) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { file(c).writeText(describe(c, thread.name, error)) }
            previous?.uncaughtException(thread, error)
        }
    }

    internal fun describe(c: Context, thread: String, error: Throwable): String {
        return buildString {
            appendLine("Plainly ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine(environment(c))
            appendLine("Thread: $thread; start reached: ${runCatching { stages(c).readText().trim().lines().joinToString(" > ") }.getOrDefault("?")}")
            appendLine()
            append(error.stackTraceToString().take(12_000))
        }
    }

    /** The saved report, if the last run crashed. */
    fun pending(c: Context): String? = file(c).takeIf { it.exists() }?.let { runCatching { it.readText() }.getOrNull() }

    fun clear(c: Context) { file(c).delete() }

    /** Shows the report (plain views). [onContinue] starts the app normally. */
    fun show(a: Activity, report: String, onContinue: () -> Unit) {
        val pad = (16 * a.resources.displayMetrics.density).toInt()
        val root = LinearLayout(a).apply {
            orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.WHITE); setPadding(pad, pad * 3, pad, pad * 2)
        }
        root.addView(TextView(a).apply {
            text = "Sorry, Plainly closed unexpectedly last time"
            textSize = 20f; setTextColor(Color.BLACK); setTypeface(typeface, Typeface.BOLD)
        })
        root.addView(TextView(a).apply {
            text = "Please tap Copy (or Share) and send this to the developer. It only contains the error and your phone model."
            textSize = 15f; setTextColor(Color.DKGRAY); setPadding(0, pad / 2, 0, pad / 2)
        })
        val buttons = LinearLayout(a).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.START }
        buttons.addView(Button(a).apply {
            text = "Copy"
            setOnClickListener {
                (a.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Plainly error", report))
                Toast.makeText(a, "Copied", Toast.LENGTH_SHORT).show()
            }
        })
        buttons.addView(Button(a).apply {
            text = "Share"
            setOnClickListener {
                runCatching {
                    a.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain")
                        .putExtra(Intent.EXTRA_SUBJECT, "Plainly error report").putExtra(Intent.EXTRA_TEXT, report), "Send the error"))
                }
            }
        })
        buttons.addView(Button(a).apply {
            text = "Continue"
            setOnClickListener { clear(a); onContinue() }
        })
        root.addView(buttons)
        root.addView(ScrollView(a).apply {
            addView(TextView(a).apply {
                text = report; textSize = 12f; setTextColor(Color.BLACK); typeface = Typeface.MONOSPACE; setTextIsSelectable(true)
                contentDescription = "Error report"
            })
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        a.setContentView(root)
    }
}
