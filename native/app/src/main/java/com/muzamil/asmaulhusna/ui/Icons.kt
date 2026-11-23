package com.muzamil.asmaulhusna.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The app's own line icons, drawn on a 24×24 grid with one stroke weight and round
 * caps, so every glyph shares a single visual family (no icon font in the app).
 */
enum class Glyph {
    Back, Forward, ChevronRight, Grid, List, Sliders, Search, Close, Heart, HeartFilled,
    Copy, Share, Star, Link, External, Sun, Moon, Auto, ArrowRight, Shield, Document,
}

@Composable
fun AppIcon(glyph: Glyph, color: Color, size: Dp = 22.dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) { drawGlyph(glyph, color) }
}

private val heart = Path().apply {
    moveTo(12f, 20f)
    cubicTo(5.2f, 15.6f, 3f, 12.2f, 3f, 8.9f)
    cubicTo(3f, 6.1f, 5.1f, 4f, 7.7f, 4f)
    cubicTo(9.5f, 4f, 11f, 5f, 12f, 6.6f)
    cubicTo(13f, 5f, 14.5f, 4f, 16.3f, 4f)
    cubicTo(18.9f, 4f, 21f, 6.1f, 21f, 8.9f)
    cubicTo(21f, 12.2f, 18.8f, 15.6f, 12f, 20f)
    close()
}

fun DrawScope.drawGlyph(glyph: Glyph, color: Color) {
    scale(size.width / 24f, pivot = Offset.Zero) {
        val line = Stroke(width = 1.9f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun poly(vararg pts: Float, close: Boolean = false) {
            val p = Path().apply {
                moveTo(pts[0], pts[1])
                var i = 2
                while (i < pts.size) { lineTo(pts[i], pts[i + 1]); i += 2 }
                if (close) close()
            }
            drawPath(p, color, style = line)
        }
        fun rr(l: Float, t: Float, r: Float, b: Float, rad: Float) =
            Path().apply { addRoundRect(RoundRect(l, t, r, b, CornerRadius(rad))) }

        when (glyph) {
            Glyph.Back -> poly(15f, 5f, 8f, 12f, 15f, 19f)
            Glyph.Forward -> poly(9f, 5f, 16f, 12f, 9f, 19f)
            Glyph.ChevronRight -> poly(10f, 7f, 15f, 12f, 10f, 17f)
            Glyph.ArrowRight -> { poly(5f, 12f, 19f, 12f); poly(13f, 6f, 19f, 12f, 13f, 18f) }
            Glyph.Grid -> for ((x, y) in listOf(4f to 4f, 13.5f to 4f, 4f to 13.5f, 13.5f to 13.5f)) {
                drawPath(rr(x, y, x + 6.5f, y + 6.5f, 2f), color, style = line)
            }
            Glyph.List -> {
                drawPath(rr(4f, 4.5f, 20f, 10.5f, 2.2f), color, style = line)
                drawPath(rr(4f, 13.5f, 20f, 19.5f, 2.2f), color, style = line)
            }
            Glyph.Sliders -> {
                poly(4f, 8f, 20f, 8f); poly(4f, 16f, 20f, 16f)
                drawCircle(color, 2.6f, Offset(9f, 8f)); drawCircle(color, 2.6f, Offset(15f, 16f))
            }
            Glyph.Search -> { drawCircle(color, 6f, Offset(10.5f, 10.5f), style = line); poly(15f, 15f, 20f, 20f) }
            Glyph.Close -> { poly(6.5f, 6.5f, 17.5f, 17.5f); poly(17.5f, 6.5f, 6.5f, 17.5f) }
            Glyph.Heart -> drawPath(heart, color, style = line)
            Glyph.HeartFilled -> { drawPath(heart, color); drawPath(heart, color, style = line) }
            Glyph.Copy -> {
                drawPath(rr(9f, 9f, 20f, 20f, 3f), color, style = line)
                val hole = Path.combine(
                    PathOperation.Difference,
                    Path().apply { addRect(androidx.compose.ui.geometry.Rect(0f, 0f, 24f, 24f)) },
                    rr(7f, 7f, 22f, 22f, 4f),
                )
                clipPath(hole, ClipOp.Intersect) { drawPath(rr(4f, 4f, 15f, 15f, 3f), color, style = line) }
            }
            Glyph.Share -> {
                poly(12f, 3.5f, 12f, 14.5f); poly(8f, 7.5f, 12f, 3.5f, 16f, 7.5f)
                poly(8f, 11f, 5.5f, 11f, 5.5f, 20.5f, 18.5f, 20.5f, 18.5f, 11f, 16f, 11f)
            }
            Glyph.Star -> {
                val p = Path()
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) 8.8f else 3.9f
                    val a = -PI / 2 + i * PI / 5
                    val x = 12f + r * cos(a).toFloat(); val y = 12.6f + r * sin(a).toFloat()
                    if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
                }
                p.close()
                drawPath(p, color); drawPath(p, color, style = Stroke(1.2f, join = StrokeJoin.Round))
            }
            Glyph.Link -> rotate(-45f, pivot = Offset(12f, 12f)) {
                drawPath(rr(2.5f, 8.8f, 12.5f, 15.2f, 3.2f), color, style = line)
                drawPath(rr(11.5f, 8.8f, 21.5f, 15.2f, 3.2f), color, style = line)
            }
            Glyph.External -> {
                poly(11f, 5f, 7f, 5f, 5f, 7f, 5f, 17f, 7f, 19f, 17f, 19f, 19f, 17f, 19f, 13f)
                poly(13f, 5f, 19f, 5f, 19f, 11f); poly(19f, 5f, 11.5f, 12.5f)
            }
            Glyph.Sun -> {
                drawCircle(color, 4f, Offset(12f, 12f), style = line)
                for (i in 0 until 8) {
                    val a = i * PI / 4
                    val dx = cos(a).toFloat(); val dy = sin(a).toFloat()
                    poly(12 + dx * 7, 12 + dy * 7, 12 + dx * 9, 12 + dy * 9)
                }
            }
            Glyph.Moon -> {
                val moon = Path.combine(
                    PathOperation.Difference,
                    Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(12f, 12f), 8f)) },
                    Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(17f, 8f), 7f)) },
                )
                drawPath(moon, color)
            }
            Glyph.Auto -> {
                drawCircle(color, 8f, Offset(12f, 12f), style = line)
                drawArc(color, -90f, 180f, true, Offset(4f, 4f), Size(16f, 16f))
            }
            Glyph.Shield -> {
                val p = Path().apply {
                    moveTo(12f, 3.5f); lineTo(19f, 6.2f); lineTo(19f, 11.5f)
                    cubicTo(19f, 15.8f, 16.1f, 19f, 12f, 20.5f)
                    cubicTo(7.9f, 19f, 5f, 15.8f, 5f, 11.5f); lineTo(5f, 6.2f); close()
                }
                drawPath(p, color, style = line); poly(9f, 12f, 11.2f, 14.2f, 15.2f, 10f)
            }
            Glyph.Document -> {
                poly(14f, 3.5f, 7f, 3.5f, 5.5f, 5f, 5.5f, 19f, 7f, 20.5f, 17f, 20.5f, 18.5f, 19f, 18.5f, 8f, 14f, 3.5f, close = true)
                poly(14f, 3.5f, 14f, 8f, 18.5f, 8f); poly(9f, 12.5f, 15f, 12.5f); poly(9f, 16f, 13f, 16f)
            }
        }
    }
}
