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

    /** Call as early as possible (Application.attachBaseContext). Keeps the system's handler, so Android still closes the app. */
    fun install(c: Context) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { file(c).writeText(describe(c, thread.name, error)) }
            previous?.uncaughtException(thread, error)
        }
    }

    internal fun describe(c: Context, thread: String, error: Throwable): String {
        val installer = runCatching {
            if (Build.VERSION.SDK_INT >= 30) c.packageManager.getInstallSourceInfo(c.packageName).installingPackageName
            else @Suppress("DEPRECATION") c.packageManager.getInstallerPackageName(c.packageName)
        }.getOrNull()
        return buildString {
            appendLine("Plainly ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("Phone: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("Installed by: ${installer ?: "unknown"}; thread: $thread")
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
