package com.muzamil.asmaulhusna.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muzamil.asmaulhusna.R
import kotlin.math.min

/**
 * Every colour the app paints with. One accent, locked: bronze-gold. It marks the
 * sacred text, the active state and the primary action. Neutrals carry the rest, and
 * the ink [hero] surface (the colour of the app icon) is the brand moment.
 */
@Immutable
data class Palette(
    val dark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val border: Color,
    val accent: Color,
    val onAccent: Color,
    val accentDeep: Color,
    val title: Color,
    val body: Color,
    val muted: Color,
    val faint: Color,
    val shadow: Color,
    val hero: Color,
    val onHero: Color,
    val heroMuted: Color,
    val logoInk: Color = Color(0xFFD9BC89),
    val logoBackground: Color = Color(0xFF101820),
) {
    val gold get() = accent

    companion object {
        val Light = Palette(
            dark = false,
            background = Color(0xFFF6F3EC),
            surface = Color(0xFFFFFFFF),
            surfaceRaised = Color(0xFFECE7DC),
            border = Color(0xFFE6E0D4),
            accent = Color(0xFF86622B),
            onAccent = Color(0xFFFFFFFF),
            accentDeep = Color(0xFFF3EADB),
            title = Color(0xFF1A1712),
            body = Color(0xFF4A453D),
            muted = Color(0xFF7A7368),
            faint = Color(0xFFA9A195),
            shadow = Color(0x141A1712),
            hero = Color(0xFF101820),
            onHero = Color(0xFFF7F3EA),
            heroMuted = Color(0xFFA9B2B9),
        )
        val Dark = Palette(
            dark = true,
            background = Color(0xFF0A0E12),
            surface = Color(0xFF151B21),
            surfaceRaised = Color(0xFF1F272E),
            border = Color(0xFF262F37),
            accent = Color(0xFFD9BC89),
            onAccent = Color(0xFF14100A),
            accentDeep = Color(0xFF2A241A),
            title = Color(0xFFF4F1EA),
            body = Color(0xFFC9C4BA),
            muted = Color(0xFF9A958B),
            faint = Color(0xFF6B675F),
            shadow = Color(0x00000000),
            hero = Color(0xFF16202A),
            onHero = Color(0xFFF7F3EA),
            heroMuted = Color(0xFFA9B2B9),
        )
    }
}

val LocalPalette = staticCompositionLocalOf { Palette.Light }

val palette: Palette
    @Composable @ReadOnlyComposable get() = LocalPalette.current

object AppFonts {
    val sans = FontFamily(
        Font(R.font.google_sans_regular, FontWeight.Normal),
        Font(R.font.google_sans_regular, FontWeight.Medium),
        Font(R.font.google_sans_semibold, FontWeight.SemiBold),
        Font(R.font.google_sans_bold, FontWeight.Bold),
    )
    val arabic = FontFamily(Font(R.font.harmattan))
    val urdu = FontFamily(Font(R.font.nastaleeq))
}

/** Shape lock: actions and inputs are pills, cards 24, thumbnails 16, icon squares 8. */
object AppRadius {
    val card = 24.dp
    val tile = 16.dp
    val icon = 8.dp
}

/** Strong ease-out for things entering or responding; short and interruptible. */
object AppMotion {
    val easeOut = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
    val drawer = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
    const val PRESS = 100
    const val RELEASE = 180
    const val FAST = 200
    const val PAGE = 380
}

/** Scales a design value (authored at 390×844) to the current screen. */
@Composable
@ReadOnlyComposable
fun scaleFactor(): Float {
    val c = LocalConfiguration.current
    return min(c.screenWidthDp / 390f, c.screenHeightDp / 844f).coerceIn(.82f, 1.2f)
}

@Composable @ReadOnlyComposable fun r(v: Float): Dp = (v * scaleFactor()).dp
@Composable @ReadOnlyComposable fun rs(v: Float): TextUnit = (v * scaleFactor()).sp
