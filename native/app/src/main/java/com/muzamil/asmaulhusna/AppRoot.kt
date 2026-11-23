package com.muzamil.asmaulhusna

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.muzamil.asmaulhusna.data.DivineName
import com.muzamil.asmaulhusna.data.Settings
import com.muzamil.asmaulhusna.data.ThemeMode
import com.muzamil.asmaulhusna.screens.HomeScreen
import com.muzamil.asmaulhusna.screens.LicencesScreen
import com.muzamil.asmaulhusna.screens.NameScreen
import com.muzamil.asmaulhusna.screens.SettingsScreen
import com.muzamil.asmaulhusna.ui.AppFonts
import com.muzamil.asmaulhusna.ui.AppMotion
import com.muzamil.asmaulhusna.ui.LocalPalette
import com.muzamil.asmaulhusna.ui.Palette
import com.muzamil.asmaulhusna.ui.Txt
import com.muzamil.asmaulhusna.ui.palette
import com.muzamil.asmaulhusna.ui.pill
import com.muzamil.asmaulhusna.ui.style
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.max

// ---------- Routes ----------

sealed interface Route {
    val key: String
    data object Home : Route { override val key = "home" }
    data class Name(val list: List<DivineName>, val index: Int, val prefix: String) : Route {
        override val key = "name-$prefix-${list[index].number}"
    }
    data object Settings : Route { override val key = "settings" }
    data object Licences : Route { override val key = "licences" }
}

/** A tiny back stack: push goes deeper, back pops. No navigation library needed. */
class Navigator {
    val stack = mutableStateListOf<Route>(Route.Home)
    var forward by mutableStateOf(true); private set
    fun push(route: Route) { forward = true; stack.add(route); Analytics.screen(route) }
    fun pop() { if (stack.size > 1) { forward = false; stack.removeAt(stack.lastIndex); Analytics.screen(stack.last()) } }
}

val LocalNavigator = compositionLocalOf<Navigator> { error("No navigator") }

// ---------- Hero (shared element) scopes ----------

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/** Lets a name's Arabic fly between the card it was tapped on and its page. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.hero(key: String): Modifier {
    val shared = LocalSharedScope.current ?: return this
    val anim = LocalAnimScope.current ?: return this
    return with(shared) {
        this@hero.sharedBounds(
            rememberSharedContentState(key),
            anim,
            enter = fadeIn(tween(AppMotion.PAGE)),
            exit = fadeOut(tween(AppMotion.PAGE)),
            boundsTransform = { _, _ -> tween(AppMotion.PAGE, easing = AppMotion.drawer) },
            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(ContentScale.Fit, Alignment.Center),
        )
    }
}

// ---------- Theme reveal ----------

/**
 * Animates a light/dark switch: a snapshot of the old theme sits on top while a circle
 * of the new theme grows out from where the user last tapped.
 */
class ThemeReveal {
    var snapshot by mutableStateOf<ImageBitmap?>(null)
    var origin = Offset.Zero
    val progress = Animatable(0f)
}

val LocalThemeReveal = compositionLocalOf<(ThemeMode) -> Unit> { { Settings.setThemeMode(it) } }

// ---------- Toast ----------

object Toast {
    var message by mutableStateOf<String?>(null); private set
    var id by mutableStateOf(0); private set
    fun show(text: String) { message = text; id++ }
    fun clear() { message = null }
}

/** Light status bar icons over the ink hero, the theme's own elsewhere. */
@Composable
fun StatusBarIcons(lightIcons: Boolean) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !lightIcons
            isAppearanceLightNavigationBars = !lightIcons
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppRoot() {
    val systemDark = isSystemInDarkTheme()
    val dark = when (Settings.themeMode) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val p = if (dark) Palette.Dark else Palette.Light
    val nav = remember { Navigator() }
    val holder = rememberSaveableStateHolder()
    val reveal = remember { ThemeReveal() }
    val layer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val reduce = com.muzamil.asmaulhusna.ui.reducedMotion()

    val switchTheme: (ThemeMode) -> Unit = { mode ->
        val next = when (mode) { ThemeMode.System -> systemDark; ThemeMode.Light -> false; ThemeMode.Dark -> true }
        if (next == dark || reveal.snapshot != null) {
            Settings.setThemeMode(mode)
        } else {
            scope.launch {
                reveal.snapshot = runCatching { layer.toImageBitmap() }.getOrNull()
                Settings.setThemeMode(mode)
                reveal.progress.snapTo(0f)
                reveal.progress.animateTo(1f, tween(if (reduce) 250 else 620, easing = AppMotion.easeOut))
                reveal.snapshot = null
            }
        }
    }

    LaunchedEffect(Unit) { Analytics.screen(Route.Home) }
    BackHandler(nav.stack.size > 1) { nav.pop() }

    CompositionLocalProvider(
        LocalPalette provides p,
        LocalNavigator provides nav,
        LocalThemeReveal provides switchTheme,
        LocalTextSelectionColors provides TextSelectionColors(p.accent, p.accent.copy(alpha = .3f)),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(p.background)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val e = awaitPointerEvent(PointerEventPass.Initial)
                            e.changes.firstOrNull()?.let { if (it.pressed) reveal.origin = it.position }
                        }
                    }
                },
        ) {
            Box(
                Modifier.fillMaxSize().drawWithContent {
                    layer.record { this@drawWithContent.drawContent() }
                    drawLayer(layer)
                },
            ) {
                SharedTransitionLayout {
                    AnimatedContent(
                        nav.stack.last(),
                        transitionSpec = {
                            val dur = AppMotion.PAGE
                            if (nav.forward) {
                                slideInHorizontally(tween(dur, easing = AppMotion.drawer)) { it } togetherWith
                                    slideOutHorizontally(tween(dur, easing = AppMotion.drawer)) { -it / 3 } +
                                    fadeOut(tween(dur), .6f)
                            } else {
                                slideInHorizontally(tween(dur, easing = AppMotion.drawer)) { -it / 3 } +
                                    fadeIn(tween(dur), .6f) togetherWith
                                    slideOutHorizontally(tween(dur, easing = AppMotion.drawer)) { it }
                            }.apply { targetContentZIndex = if (nav.forward) 1f else -1f }
                        },
                        contentKey = { it.key },
                        label = "nav",
                    ) { route ->
                        CompositionLocalProvider(LocalSharedScope provides this@SharedTransitionLayout, LocalAnimScope provides this) {
                            holder.SaveableStateProvider(route.key) {
                                Box(Modifier.fillMaxSize().background(palette.background)) {
                                    when (route) {
                                        Route.Home -> HomeScreen()
                                        is Route.Name -> NameScreen(route)
                                        Route.Settings -> SettingsScreen()
                                        Route.Licences -> LicencesScreen()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // The old theme, with a growing circular hole showing the new one.
            reveal.snapshot?.let { image ->
                Canvas(Modifier.fillMaxSize()) {
                    val t = reveal.progress.value
                    val o = reveal.origin
                    val far = listOf(Offset.Zero, Offset(size.width, 0f), Offset(0f, size.height), Offset(size.width, size.height))
                        .maxOf { hypot(it.x - o.x, it.y - o.y) }
                    val hole = Path().apply {
                        fillType = PathFillType.EvenOdd
                        addRect(androidx.compose.ui.geometry.Rect(Offset.Zero, size))
                        addOval(androidx.compose.ui.geometry.Rect(o, max(far * t, .01f)))
                    }
                    if (reduce) {
                        drawImage(image, alpha = 1 - t)
                    } else {
                        clipPath(hole) { drawImage(image) }
                    }
                }
            }

            ToastHost(Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun ToastHost(modifier: Modifier) {
    val p = palette
    val text = Toast.message
    LaunchedEffect(Toast.id) {
        if (text != null) { delay(2200); Toast.clear() }
    }
    AnimatedVisibility(
        text != null,
        modifier.navigationBarsPadding().padding(bottom = 24.dp, start = 24.dp, end = 24.dp),
        enter = slideInVertically(tween(260, easing = AppMotion.easeOut)) { it } + fadeIn(),
        exit = slideOutVertically(tween(180)) { it / 2 } + fadeOut(tween(180)),
    ) {
        Box(Modifier.background(p.title, pill()).padding(horizontal = 20.dp, vertical = 12.dp)) {
            Txt(text ?: "", style(14.sp, p.background, androidx.compose.ui.text.font.FontWeight.Medium, AppFonts.sans))
        }
    }
}
