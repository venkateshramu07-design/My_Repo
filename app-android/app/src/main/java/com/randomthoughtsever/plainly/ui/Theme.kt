package com.randomthoughtsever.plainly.ui

import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Biotech
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.randomthoughtsever.plainly.R

val Navy = Color(0xFF12304F)
val NavyDeep = Color(0xFF0A1B2F)
val NavyBright = Color(0xFF24527F)
val Gold = Color(0xFFD9A75E)
val GoldLight = Color(0xFFF7D58A)

val Jakarta = FontFamily(
    Font(R.font.jakarta_medium, FontWeight.Medium),
    Font(R.font.jakarta_medium, FontWeight.Normal),
    Font(R.font.jakarta_bold, FontWeight.Bold),
    Font(R.font.jakarta_bold, FontWeight.SemiBold),
    Font(R.font.jakarta_extrabold, FontWeight.ExtraBold),
)
val Fraunces = FontFamily(Font(R.font.fraunces_semibold, FontWeight.SemiBold), Font(R.font.fraunces_bold, FontWeight.Bold))

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F4E79), onPrimary = Color.White, primaryContainer = Color(0xFFDCE8F4), onPrimaryContainer = Color(0xFF0A1B2F),
    secondary = Color(0xFFB8873E), onSecondary = Color.White, secondaryContainer = Color(0xFFF6E7CB), onSecondaryContainer = Color(0xFF3A2A0E),
    background = Color(0xFFF6F4F0), onBackground = Color(0xFF0F1B2A), surface = Color(0xFFFFFFFF), onSurface = Color(0xFF0F1B2A),
    surfaceVariant = Color(0xFFEEEAE3), onSurfaceVariant = Color(0xFF5B6576), outline = Color(0xFFD9D3C8),
    surfaceContainer = Color(0xFFFFFFFF), surfaceContainerHigh = Color(0xFFF1EEE8), surfaceContainerLow = Color(0xFFFAF8F5),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF9CC0E4), onPrimary = Color(0xFF0A1B2F), primaryContainer = Color(0xFF1E3550), onPrimaryContainer = Color(0xFFDCE8F4),
    secondary = Color(0xFFE6BE7A), onSecondary = Color(0xFF2B1E06), secondaryContainer = Color(0xFF4A3A1C), onSecondaryContainer = Color(0xFFF6E7CB),
    background = Color(0xFF0E1621), onBackground = Color(0xFFE9EDF3), surface = Color(0xFF16202D), onSurface = Color(0xFFE9EDF3),
    surfaceVariant = Color(0xFF1D2938), onSurfaceVariant = Color(0xFFA9B4C4), outline = Color(0xFF2C3A4C),
    surfaceContainer = Color(0xFF16202D), surfaceContainerHigh = Color(0xFF1D2938), surfaceContainerLow = Color(0xFF121B27),
)

private val Type = Typography().let { t ->
    Typography(
        displaySmall = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 40.sp),
        headlineLarge = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp),
        headlineMedium = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
        headlineSmall = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
        titleLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 26.sp),
        titleMedium = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp),
        titleSmall = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 19.sp),
        bodyLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 21.sp),
        bodySmall = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 17.sp),
        labelLarge = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 14.sp),
        labelMedium = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.6.sp),
        labelSmall = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 1.2.sp),
    )
}

@Composable
fun PlainlyTheme(mode: String, content: @Composable () -> Unit) {
    val dark = when (mode) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    // The colour theme the reader unlocked and chose (Levels & rewards); navy is the default.
    val accent = runCatching { AppGraph.progress.accent }.getOrDefault("navy")
    val scheme = if (dark) DarkColors else LightColors
    val c = ACCENTS[accent]
    val colors = if (c == null || accent == "navy") scheme else if (dark) scheme.copy(primary = androidx.compose.ui.graphics.lerp(c.second, Color.White, .4f),
        primaryContainer = androidx.compose.ui.graphics.lerp(c.first, Color.Black, .4f), onPrimary = Color(0xFF0A1B2F))
        else scheme.copy(primary = c.first, primaryContainer = androidx.compose.ui.graphics.lerp(c.second, Color.White, .82f))
    MaterialTheme(colorScheme = colors, typography = Type, content = content)
}

/** The topics shown in Explore, with a colour pair and an icon each. Labels match the blog's labels. */
data class Topic(val label: String, val title: String, val blurb: String, val icon: ImageVector, val from: Color, val to: Color)

val Topics = listOf(
    Topic("Personal Finance", "Personal Finance", "Budgets, debt, credit and saving", Icons.Rounded.AccountBalance, Color(0xFF1F4E79), Color(0xFF3B7BB8)),
    Topic("Investing", "Investing", "Funds, gold, IPOs and risk", Icons.Rounded.ShowChart, Color(0xFF0F5F55), Color(0xFF22A08C)),
    Topic("Economics", "Economics", "Inflation, rates and the news", Icons.Rounded.TrendingUp, Color(0xFF4B3A8C), Color(0xFF7B67D6)),
    Topic("Career & Workplace", "Career", "Pay, interviews and burnout", Icons.Rounded.Work, Color(0xFF8A4B1E), Color(0xFFD08844)),
    Topic("Psychology", "Mind", "How the mind works", Icons.Rounded.Psychology, Color(0xFF7A2E55), Color(0xFFC25A8C)),
    Topic("Self Improvement", "Self Improvement", "Habits, focus and growth", Icons.Rounded.SelfImprovement, Color(0xFF2E5E2A), Color(0xFF5FA35A)),
    Topic("Health & Wellness", "Health", "Sleep, stress and body", Icons.Rounded.Spa, Color(0xFF145A6B), Color(0xFF3AA5BD)),
    Topic("Technology & AI", "Technology & AI", "AI, data and the web", Icons.Rounded.Memory, Color(0xFF243B6B), Color(0xFF4F74C9)),
    Topic("Cryptocurrency", "Crypto", "Bitcoin, stablecoins, tax", Icons.Rounded.CurrencyBitcoin, Color(0xFF7A4E0C), Color(0xFFD69A2E)),
    Topic("Countries & Living Abroad", "Countries", "Money facts for 35+ countries", Icons.Rounded.Public, Color(0xFF0E4A6E), Color(0xFF2D8FC4)),
    Topic("Love & Relationships", "Relationships", "Love, family and people", Icons.Rounded.Favorite, Color(0xFF7D2333), Color(0xFFCC4D63)),
    Topic("Zen & Mindfulness", "Zen", "Calm, stories and stillness", Icons.Rounded.AutoAwesome, Color(0xFF3F4A2A), Color(0xFF8A9A5B)),
    Topic("Leadership", "Leadership", "Leading people and teams", Icons.Rounded.Flag, Color(0xFF5A2B1A), Color(0xFFA9583A)),
    Topic("Education", "Education", "Learning and student money", Icons.Rounded.School, Color(0xFF2A3F5F), Color(0xFF5A80B3)),
).distinctBy { it.title }.filter { it.title != "Wellbeing" }

/** Labels used for tool posts: they have their own look but aren't listed as reading topics. */
private val ToolTopics = listOf(
    Topic("Tools", "Tools", "Free tools that run on your phone", Icons.Rounded.Build, Color(0xFF1F4E79), Color(0xFF3B79B5)),
    Topic("Calculators", "Calculators", "Money calculators", Icons.Rounded.Calculate, Color(0xFF0F5F55), Color(0xFF2E9C88)),
)

fun topicFor(label: String): Topic = Topics.firstOrNull { it.label == label } ?: ToolTopics.firstOrNull { it.label == label } ?: Topics.first()
fun isReadingTopic(label: String) = Topics.any { it.label == label }
