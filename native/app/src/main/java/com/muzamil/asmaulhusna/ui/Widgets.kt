package com.muzamil.asmaulhusna.ui

import android.provider.Settings.Global
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ---------- Text ----------

fun style(
    size: TextUnit,
    color: Color,
    weight: FontWeight = FontWeight.Normal,
    family: FontFamily = AppFonts.sans,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    align: TextAlign = TextAlign.Unspecified,
) = TextStyle(
    fontSize = size, color = color, fontWeight = weight, fontFamily = family,
    letterSpacing = letterSpacing, lineHeight = lineHeight, textAlign = align,
)

@Composable
fun Txt(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
) = BasicText(text, modifier, style, overflow = TextOverflow.Ellipsis, maxLines = maxLines)

/** One line that shrinks (down to [min]) to fit its width, never cut off. */
@Composable
fun FitTxt(text: String, style: TextStyle, modifier: Modifier = Modifier, min: TextUnit = 12.sp) =
    BasicText(
        text, modifier, style, maxLines = 1, softWrap = false,
        autoSize = TextAutoSize.StepBased(minFontSize = min, maxFontSize = style.fontSize, stepSize = 1.sp),
    )

@Composable
fun LargeTitle(text: String, color: Color = palette.title) =
    Txt(text, style(rs(34f), color, FontWeight.Bold, letterSpacing = (-1.2).sp, lineHeight = rs(38f)))

@Composable
fun Eyebrow(text: String, color: Color = palette.accent) =
    Txt(text, style(rs(12f), color, FontWeight.Bold, letterSpacing = 1.6.sp))

/** Arabic (Harmattan) and Urdu (Nastaleeq, which draws small, so its sizes are large). */
fun arabicStyle(size: TextUnit, color: Color, align: TextAlign = TextAlign.Center) =
    style(size, color, family = AppFonts.arabic, lineHeight = size * 1.7f, align = align)

fun urduStyle(size: TextUnit, color: Color, align: TextAlign = TextAlign.Center, lineHeight: Float = 1.6f) =
    style(size, color, family = AppFonts.urdu, lineHeight = size * lineHeight, align = align)

// ---------- Motion helpers ----------

/** True when the system's animator scale is off ("Remove animations"). */
@Composable
fun reducedMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        runCatching { Global.getFloat(context.contentResolver, Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
            .getOrDefault(false)
    }
}

/**
 * Shrinks a touch while pressed and springs back on release, so every tap is
 * acknowledged the moment the finger lands. No ripple.
 */
@Composable
fun Modifier.pressable(
    onClick: (() -> Unit)?,
    scale: Float = .97f,
    description: String? = null,
): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val still = reducedMotion()
    val s by animateFloatAsState(
        if (pressed && !still) scale else 1f,
        tween(if (pressed) AppMotion.PRESS else AppMotion.RELEASE, easing = AppMotion.easeOut),
        label = "press",
    )
    return this
        .graphicsLayer { scaleX = s; scaleY = s }
        .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier)
        .clickable(source, indication = null, enabled = onClick != null, role = Role.Button) { onClick?.invoke() }
}

/** Grouped-list row: tints on press, like iOS Settings. */
@Composable
fun Modifier.rowButton(onClick: (() -> Unit)?): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val p = palette
    val bg by animateColorAsState(
        if (pressed) p.surfaceRaised else p.surface.copy(alpha = 0f),
        tween(if (pressed) 0 else AppMotion.RELEASE, easing = AppMotion.easeOut),
        label = "row",
    )
    return this
        .background(bg)
        .clickable(source, indication = null, enabled = onClick != null, role = Role.Button) { onClick?.invoke() }
}

/** Fades and lifts its content into place once, after [delayMs]. */
@Composable
fun Reveal(delayMs: Int = 0, offset: Dp = 18.dp, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    // Previews and screenshot renders show the settled state.
    val still = reducedMotion() || LocalInspectionMode.current
    val t = remember { Animatable(if (still) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (t.value < 1f) {
            delay(delayMs.toLong())
            t.animateTo(1f, tween(520, easing = AppMotion.easeOut))
        }
    }
    Box(modifier.graphicsLayer {
        alpha = t.value
        translationY = offset.toPx() * (1 - t.value)
    }) { content() }
}

/** Swaps its content with a short vertical slide and fade when [value] changes. */
@Composable
fun <T> Swap(value: T, modifier: Modifier = Modifier, content: @Composable (T) -> Unit) {
    AnimatedContent(
        value,
        modifier,
        transitionSpec = {
            (slideInVertically(tween(AppMotion.FAST, easing = AppMotion.easeOut)) { it / 2 } +
                fadeIn(tween(AppMotion.FAST))) togetherWith
                (slideOutVertically(tween(AppMotion.FAST, easing = AppMotion.easeOut)) { -it / 2 } +
                    fadeOut(tween(AppMotion.FAST / 2)))
        },
        contentAlignment = Alignment.Center,
        label = "swap",
    ) { content(it) }
}

// ---------- Surfaces ----------

/** Plain rounded surface; depth comes from the step between background and surface. */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    radius: Dp = AppRadius.card,
    color: Color = palette.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    val p = palette
    Box(
        modifier
            .then(if (onClick != null) Modifier.pressable(onClick) else Modifier)
            .then(if (p.dark) Modifier else Modifier.shadow(1.dp, shape, ambientColor = p.shadow, spotColor = p.shadow))
            .clip(shape)
            .background(color),
        content = content,
    )
}

@Composable
fun SectionLabel(text: String, color: Color = palette.muted) =
    Txt(
        text.uppercase(),
        style(12.5.sp, color, FontWeight.SemiBold, letterSpacing = .6.sp),
        Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
    )

/** Inset grouped list: rows on one surface, with hairlines from where the text starts. */
@Composable
fun GroupedSection(
    label: String? = null,
    labelColor: Color = palette.muted,
    dividerIndent: Dp = 16.dp,
    rows: List<@Composable () -> Unit>,
) {
    val p = palette
    Column(Modifier.fillMaxWidth()) {
        if (label != null) SectionLabel(label, labelColor)
        Card(Modifier.fillMaxWidth()) {
            Column {
                rows.forEachIndexed { i, row ->
                    if (i > 0) Box(Modifier.padding(start = dividerIndent).fillMaxWidth().height(.5.dp).background(p.border))
                    row()
                }
            }
        }
    }
}

/** Section title with a quiet trailing count that rolls when it changes. */
@Composable
fun SectionTitle(text: String, trailing: String? = null, modifier: Modifier = Modifier) {
    val p = palette
    Row(modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Swap(text, Modifier.weight(1f)) {
            Txt(it, style(22.sp, p.title, FontWeight.Bold, letterSpacing = (-.6).sp), Modifier.fillMaxWidth())
        }
        if (trailing != null) Swap(trailing) { Txt(it, style(15.sp, p.muted, FontWeight.SemiBold)) }
    }
}

// ---------- Buttons ----------

/** Round icon button; [onHero] styles it for the ink hero. */
@Composable
fun CircleButton(glyph: Glyph, description: String, onHero: Boolean = false, iconSize: Dp = 20.dp, onClick: () -> Unit) {
    val p = palette
    Box(
        Modifier
            .size(42.dp)
            .pressable(onClick, .9f, description)
            .then(if (!onHero && !p.dark) Modifier.shadow(1.dp, CircleShape, ambientColor = p.shadow, spotColor = p.shadow) else Modifier)
            .clip(CircleShape)
            .background(if (onHero) p.onHero.copy(alpha = .1f) else p.surface),
        contentAlignment = Alignment.Center,
    ) {
        Swap(glyph) { AppIcon(it, if (onHero) p.onHero else p.title, iconSize) }
    }
}

/**
 * Heart toggle. Adding a favourite pops the heart with a soft overshoot and sends a
 * gold ring outward; removing it just settles.
 */
@Composable
fun HeartButton(favourite: Boolean, onHero: Boolean = false, onToggle: () -> Unit) {
    val p = palette
    val haptics = LocalHapticFeedback.current
    val still = reducedMotion()
    val burst = remember { Animatable(0f) }
    var go by remember { mutableStateOf(0) }
    LaunchedEffect(go) {
        if (go > 0) { burst.snapTo(0f); burst.animateTo(1f, tween(520)) }
    }
    val bg by animateColorAsState(
        when {
            favourite -> p.accent
            onHero -> p.onHero.copy(alpha = .1f)
            else -> p.surface
        },
        tween(AppMotion.FAST), label = "heart",
    )
    Box(
        Modifier.size(42.dp).pressable({
            if (!favourite) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                if (!still) go++
            } else {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            onToggle()
        }, .9f, if (favourite) "Remove favourite" else "Add favourite"),
        contentAlignment = Alignment.Center,
    ) {
        val t = burst.value
        val e = AppMotion.easeOut.transform(t)
        if (t > 0f && t < 1f) {
            Canvas(Modifier.requiredSize(58.dp)) {
                drawCircle(
                    p.accent.copy(alpha = 1 - t),
                    radius = (21.dp + 8.dp * e).toPx(),
                    style = Stroke((2.5f * (1 - t) + .5f).dp.toPx()),
                )
            }
        }
        Box(Modifier.size(42.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
            val pop = 1f + .28f * sin(PI * e).toFloat()
            AppIcon(
                if (favourite) Glyph.HeartFilled else Glyph.Heart,
                if (favourite) p.onAccent else if (onHero) p.onHero else p.title,
                20.dp,
                Modifier.graphicsLayer { scaleX = pop; scaleY = pop },
            )
        }
    }
}

/** Pill-shaped action; [filled] is the screen's primary action in the accent. */
@Composable
fun PillButton(
    label: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    glyph: Glyph? = null,
    filled: Boolean = false,
    trailingIcon: Boolean = false,
    color: Color? = null,
    foreground: Color? = null,
    height: Dp = 50.dp,
) {
    val p = palette
    val fg = foreground ?: if (filled) p.onAccent else p.title
    val icon: @Composable () -> Unit = { if (glyph != null) AppIcon(glyph, if (filled) fg else p.accent, 18.dp) }
    val shape = RoundedCornerShape(50)
    val enabledAlpha by animateFloatAsState(if (onClick == null) .35f else 1f, tween(AppMotion.FAST), label = "pill")
    Row(
        modifier
            .height(height)
            .alpha(enabledAlpha)
            .pressable(onClick)
            .then(if (!filled && color == null && !p.dark) Modifier.shadow(1.dp, shape, ambientColor = p.shadow, spotColor = p.shadow) else Modifier)
            .clip(shape)
            .background(color ?: if (filled) p.accent else p.surface)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (glyph != null && !trailingIcon) { icon(); Spacer(Modifier.width(8.dp)) }
        Swap(label, Modifier.weight(1f, fill = false)) {
            Txt(it, style(15.sp, fg, FontWeight.Bold), maxLines = 1)
        }
        if (glyph != null && trailingIcon) { Spacer(Modifier.width(8.dp)); icon() }
    }
}

// ---------- Bars ----------

/**
 * Floating header. Content scrolls beneath it; once [raised], a translucent surface,
 * hairline and title fade in. With [hideUntilRaised] the whole bar waits.
 */
@Composable
fun GlassBar(
    raised: Boolean,
    title: String,
    hideUntilRaised: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val p = palette
    val t by animateFloatAsState(if (raised) 1f else 0f, tween(AppMotion.FAST, easing = AppMotion.easeOut), label = "bar")
    // Hidden, it must not catch taps meant for the header beneath it.
    if (hideUntilRaised && !raised && t < .01f) return
    Box(
        Modifier
            .fillMaxWidth()
            .alpha(if (hideUntilRaised) t else 1f)
            .drawBehind {
                drawRect(p.background.copy(alpha = .94f * t))
                drawRect(p.border.copy(alpha = t), Offset(0f, size.height - .5.dp.toPx()))
            }
            .statusBarsPadding()
            .height(58.dp)
            .padding(horizontal = 16.dp),
    ) {
        Row(Modifier.fillMaxWidth().align(Alignment.Center), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(92.dp)) { leading?.invoke() }
            Txt(
                title,
                style(16.sp, p.title, FontWeight.Bold, letterSpacing = (-.2).sp, align = TextAlign.Center),
                Modifier.weight(1f).alpha(t),
                maxLines = 1,
            )
            Row(Modifier.width(92.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) { trailing?.invoke(this) }
        }
    }
}

// ---------- Ornaments ----------

/** A name's number in a thin ring, in tabular figures. */
@Composable
fun NumberRing(number: Int, size: Dp = 34.dp, color: Color = palette.accent) {
    Box(
        Modifier.size(size).border(1.dp, color.copy(alpha = .45f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Txt(
            "$number",
            style((size.value * if (number > 9) .34f else .38f).sp, color, FontWeight.Bold)
                .copy(fontFeatureSettings = "tnum"),
        )
    }
}

/** A soft gold light behind a name's Arabic: no pattern, just warmth. */
fun Modifier.glow(color: Color, strength: Float = .16f): Modifier = drawBehind {
    drawCircle(
        androidx.compose.ui.graphics.Brush.radialGradient(
            listOf(color.copy(alpha = strength), color.copy(alpha = 0f)),
            center = center, radius = size.minDimension / 2,
        ),
    )
}

/** Small rounded square holding a glyph, like an iOS Settings icon. */
@Composable
fun IconTile(glyph: Glyph, color: Color) {
    val p = palette
    Box(Modifier.size(30.dp).clip(RoundedCornerShape(AppRadius.icon)).background(color), contentAlignment = Alignment.Center) {
        AppIcon(glyph, p.onAccent, 17.dp)
    }
}

fun pill(): Shape = RoundedCornerShape(50)

