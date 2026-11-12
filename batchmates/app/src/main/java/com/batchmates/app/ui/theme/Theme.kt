package com.batchmates.app.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object Brand {
    val Night = Color(0xFF0E0B2B)
    val Deep = Color(0xFF17123F)
    val Violet = Color(0xFF7C4DFF)
    val Orchid = Color(0xFFB24DDB)
    val Coral = Color(0xFFFF6B6B)
    val Sun = Color(0xFFFFC857)
    val Mint = Color(0xFF4DE1C1)
    val Sky = Color(0xFF5AB8FF)
    val Glass = Color(0x1AFFFFFF)
    val GlassBorder = Color(0x33FFFFFF)
    val TextDim = Color(0xB3FFFFFF)
    val WhatsApp = Color(0xFF25D366)
    val Facebook = Color(0xFF1877F2)

    val Primary = Brush.linearGradient(listOf(Violet, Orchid, Coral))
    val Warm = Brush.linearGradient(listOf(Coral, Sun))
    val Instagram = Brush.linearGradient(listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045)))

    /** Stable, friendly colour pair per person for avatars. */
    fun pair(seed: String): List<Color> {
        val palettes = listOf(
            listOf(Violet, Sky), listOf(Coral, Sun), listOf(Mint, Sky), listOf(Orchid, Coral),
            listOf(Sun, Mint), listOf(Sky, Violet), listOf(Color(0xFFFF8FAB), Orchid),
        )
        return palettes[(seed.hashCode() and 0x7fffffff) % palettes.size]
    }
}

private val scheme = darkColorScheme(
    primary = Brand.Violet,
    onPrimary = Color.White,
    secondary = Brand.Coral,
    tertiary = Brand.Sun,
    background = Brand.Night,
    onBackground = Color.White,
    surface = Brand.Deep,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF241D55),
    onSurfaceVariant = Brand.TextDim,
    surfaceContainer = Brand.Deep,
    surfaceContainerHigh = Color(0xFF221B52),
    surfaceContainerHighest = Color(0xFF2B2363),
    outline = Brand.GlassBorder,
    error = Color(0xFFFF8A80),
)

private val base = Typography()
private val display = FontFamily.SansSerif

private val typography = Typography(
    displaySmall = base.displaySmall.copy(fontFamily = display, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
    headlineLarge = base.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
    headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp),
    bodyLarge = base.bodyLarge,
    bodyMedium = base.bodyMedium,
    bodySmall = base.bodySmall,
)

val Eyebrow = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp, color = Brand.Sun)

@Composable
fun BatchMatesTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, typography = typography) {
        // Screens draw on our own aurora background, not a Surface, so set the default text colour here.
        CompositionLocalProvider(LocalContentColor provides Color.White, content = content)
    }
}
