package com.muzamil.asmaulhusna.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.ColorUtils
import androidx.compose.ui.graphics.toArgb

private val wordmark = wordmarkPath()

private fun shade(c: Color, dl: Float): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(c.toArgb(), hsl)
    hsl[2] = (hsl[2] + dl).coerceIn(0f, 1f)
    return Color(ColorUtils.HSLToColor(hsl))
}

/**
 * The app icon in its "Glow" frame: ink that brightens toward the middle, a fine
 * gold ring and the Urdu wordmark اسماء الحسنیٰ in a soft gold gradient.
 */
@Composable
fun AppLogo(size: Dp, radius: Dp, modifier: Modifier = Modifier) {
    val p = palette
    val bg = p.logoBackground
    val ink = p.logoInk
    Canvas(
        modifier.size(size).clip(RoundedCornerShape(radius))
            .semantics { contentDescription = "99 Names — Asmaul Husna" },
    ) {
        val w = this.size.width
        drawRect(
            Brush.radialGradient(
                0f to shade(bg, .08f), .62f to bg, 1f to shade(bg, -.035f),
                center = Offset(w / 2, w * .44f), radius = w * .74f,
            ),
        )
        scale(w / 256f, pivot = Offset.Zero) {
            drawCircle(ink.copy(alpha = .5f), 100f, Offset(128f, 128f), style = Stroke(2.2f))
            drawCircle(ink.copy(alpha = .18f), 93f, Offset(128f, 128f), style = Stroke(1f))
            translate(128f, 128f) {
                scale(.8f, pivot = Offset.Zero) {
                    translate(-128f, -128f) {
                        drawPath(
                            wordmark,
                            Brush.verticalGradient(
                                0f to shade(ink, .12f), .5f to ink, 1f to shade(ink, -.12f),
                                startY = 60f, endY = 200f,
                            ),
                        )
                    }
                }
            }
        }
    }
}
