package com.muzamil.asmaulhusna.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** An iOS-style switch: the thumb slides on a spring, the track fills with the accent. */
@Composable
fun AppSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    val p = palette
    val x by animateDpAsState(if (checked) 22.dp else 2.dp, spring(dampingRatio = .8f, stiffness = Spring.StiffnessMediumLow), label = "switch")
    val track by animateColorAsState(if (checked) p.accent else p.surfaceRaised, tween(AppMotion.FAST), label = "track")
    Box(
        Modifier
            .size(51.dp, 31.dp)
            .clip(RoundedCornerShape(50))
            .background(track)
            .semantics { role = Role.Switch; stateDescription = if (checked) "On" else "Off" }
            .clickable(remember { MutableInteractionSource() }, null) { onChange(!checked) },
    ) {
        Box(
            Modifier
                .offset(x = x, y = 2.dp)
                .size(27.dp)
                .shadow(2.dp, CircleShape)
                .background(androidx.compose.ui.graphics.Color.White, CircleShape),
        )
    }
}

/** A sliding segmented control; the thumb glides to the chosen segment. */
@Composable
fun <T> SegmentedControl(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    height: Dp = 40.dp,
    background: androidx.compose.ui.graphics.Color = palette.surfaceRaised,
    thumb: androidx.compose.ui.graphics.Color = if (palette.dark) palette.border else palette.surface,
    segment: @Composable (T, Boolean) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    BoxWithConstraints(
        Modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(11.dp)).background(background).padding(3.dp),
    ) {
        val w = maxWidth / options.size
        val index = options.indexOf(selected).coerceAtLeast(0)
        val x by animateDpAsState(w * index, spring(dampingRatio = .85f, stiffness = Spring.StiffnessMediumLow), label = "seg")
        Box(
            Modifier.offset(x = x).width(w).fillMaxHeight()
                .shadow(1.dp, RoundedCornerShape(8.dp))
                .background(thumb, RoundedCornerShape(8.dp)),
        )
        Row(Modifier.fillMaxWidth().fillMaxHeight()) {
            options.forEach { option ->
                val isSelected = option == selected
                Box(
                    Modifier.weight(1f).fillMaxHeight()
                        .semantics { this.selected = isSelected; role = Role.Tab }
                        .clickable(remember { MutableInteractionSource() }, null) {
                            if (option != selected) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelect(option)
                        },
                    contentAlignment = Alignment.Center,
                ) { segment(option, option == selected) }
            }
        }
    }
}

/**
 * A slider in [min]..[max] with [steps] stops. Touch anywhere on it and slide: the thumb
 * follows the finger smoothly, the value moves stop by stop with a tick, and on release
 * the thumb settles on the nearest stop.
 */
@Composable
fun AppSlider(value: Float, min: Float, max: Float, steps: Int, onChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    val p = palette
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    // the gesture runs once for the slider's life, so it reads the latest value through these
    val current by rememberUpdatedState(value)
    val change by rememberUpdatedState(onChange)
    var drag by remember { mutableStateOf<Float?>(null) } // finger position as 0..1 while dragging
    BoxWithConstraints(modifier.fillMaxWidth().height(36.dp)) {
        val thumb = 26.dp
        val half = with(density) { (thumb / 2).toPx() }
        val trackPx = constraints.maxWidth - 2 * half
        fun follow(x: Float) {
            val f = ((x - half) / trackPx).coerceIn(0f, 1f)
            drag = f
            val v = min + (f * steps).roundToInt() / steps.toFloat() * (max - min)
            if (v != current) { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); change(v) }
        }
        val frac = drag ?: ((value - min) / (max - min))
        val shown by animateFloatAsState(frac, if (drag != null) snap() else spring(stiffness = Spring.StiffnessMedium), label = "thumb")
        Box(
            Modifier.fillMaxWidth().fillMaxHeight().pointerInput(trackPx) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val slide = awaitHorizontalTouchSlopOrCancellation(down.id) { c, _ -> c.consume() }
                    if (slide != null) {
                        follow(slide.position.x)
                        horizontalDrag(slide.id) { c -> follow(c.position.x); c.consume() }
                    } else if (currentEvent.changes.any { it.id == down.id && !it.pressed && !it.isConsumed }) {
                        follow(down.position.x) // a tap jumps to that stop
                    }
                    drag = null
                }
            },
        ) {
            Box(Modifier.align(Alignment.CenterStart).padding(horizontal = thumb / 2).fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(p.surfaceRaised))
            Box(
                Modifier.align(Alignment.CenterStart).padding(start = thumb / 2)
                    .width(with(density) { (trackPx * shown).toDp() }).height(4.dp)
                    .clip(RoundedCornerShape(2.dp)).background(p.accent),
            )
            Box(
                Modifier.align(Alignment.CenterStart).offset(x = with(density) { (trackPx * shown).toDp() }).size(thumb)
                    .shadow(2.dp, CircleShape).background(androidx.compose.ui.graphics.Color.White, CircleShape),
            )
        }
    }
}
