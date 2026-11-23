package com.muzamil.asmaulhusna.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxWidth as fmw
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muzamil.asmaulhusna.Actions
import com.muzamil.asmaulhusna.Analytics
import com.muzamil.asmaulhusna.LocalNavigator
import com.muzamil.asmaulhusna.Route
import com.muzamil.asmaulhusna.StatusBarIcons
import com.muzamil.asmaulhusna.Toast
import com.muzamil.asmaulhusna.data.DivineName
import com.muzamil.asmaulhusna.data.Settings
import com.muzamil.asmaulhusna.hero
import com.muzamil.asmaulhusna.ui.AppMotion
import com.muzamil.asmaulhusna.ui.CircleButton
import com.muzamil.asmaulhusna.ui.Glyph
import com.muzamil.asmaulhusna.ui.GroupedSection
import com.muzamil.asmaulhusna.ui.HeartButton
import com.muzamil.asmaulhusna.ui.PillButton
import com.muzamil.asmaulhusna.ui.Reveal
import com.muzamil.asmaulhusna.ui.glow
import com.muzamil.asmaulhusna.ui.Swap
import com.muzamil.asmaulhusna.ui.FitTxt
import com.muzamil.asmaulhusna.ui.Txt
import com.muzamil.asmaulhusna.ui.arabicStyle
import com.muzamil.asmaulhusna.ui.palette
import com.muzamil.asmaulhusna.ui.r
import com.muzamil.asmaulhusna.ui.rs
import com.muzamil.asmaulhusna.ui.style
import com.muzamil.asmaulhusna.ui.urduStyle
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

/** One name per page; swipe between the names shown on the home screen. */
@Composable
fun NameScreen(route: Route.Name) {
    val p = palette
    val nav = LocalNavigator.current
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val pager = rememberPagerState(route.index) { route.list.size }
    val name = route.list[pager.currentPage]
    val last = route.list.lastIndex

    LaunchedEffect(pager) {
        snapshotFlow { pager.currentPage }.drop(1).collect { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
    }
    fun go(delta: Int) = scope.launch {
        pager.animateScrollToPage(pager.currentPage + delta, animationSpec = tween(AppMotion.PAGE, easing = AppMotion.drawer))
    }

    StatusBarIcons(lightIcons = true)

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().background(p.hero).statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleButton(Glyph.Back, "Back", onHero = true) { nav.pop() }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Progress(name.number) }
            CircleButton(Glyph.Copy, "Copy", onHero = true, iconSize = 19.dp) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                Actions.copy(
                    context, name.transliteration,
                    "${name.number}. ${name.arabic}\n${name.transliteration} — ${name.english}\n${name.urdu}\n\n${name.explanation}",
                )
                Toast.show("Copied ${name.transliteration}")
            }
            Spacer(Modifier.width(8.dp))
            HeartButton(Settings.isFavourite(name.number), onHero = true) {
                Analytics.favourite(name.number, added = !Settings.isFavourite(name.number))
                Settings.toggleFavourite(name.number)
            }
        }
        HorizontalPager(pager, Modifier.weight(1f).fillMaxWidth(), key = { route.list[it].number }) { page ->
            val n = route.list[page]
            NamePage(n, "${route.prefix}-${n.number}")
        }
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val page = pager.currentPage
            PillButton(
                if (page > 0) route.list[page - 1].transliteration else "Previous",
                if (page > 0) ({ go(-1) }) else null,
                Modifier.weight(1f), glyph = Glyph.Back,
            )
            PillButton(
                if (page < last) route.list[page + 1].transliteration else "Next",
                if (page < last) ({ go(1) }) else null,
                Modifier.weight(1f), glyph = Glyph.Forward, trailingIcon = true, filled = true,
            )
        }
    }
}

/** "12 of 99" with a thin gold bar; the number rolls as you swipe. */
@Composable
private fun Progress(number: Int) {
    val p = palette
    val frac by animateFloatAsState(number / 99f, tween(AppMotion.PAGE, easing = AppMotion.easeOut), label = "progress")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Swap(number) {
            Txt("$it of 99", style(13.sp, p.heroMuted, FontWeight.SemiBold).copy(fontFeatureSettings = "tnum"))
        }
        Spacer(Modifier.height(6.dp))
        Box(Modifier.width(64.dp).height(3.dp).clip(RoundedCornerShape(50)).background(p.onHero.copy(alpha = .14f))) {
            Box(Modifier.fillMaxHeight().fmw(frac).clip(RoundedCornerShape(50)).background(p.logoInk))
        }
    }
}

@Composable
private fun NamePage(name: DivineName, heroKey: String) {
    val p = palette
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = r(20f))) {
        // Ink hero continuing seamlessly from the header.
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)).background(p.hero)
                .padding(start = 24.dp, end = 24.dp, bottom = r(30f)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.fillMaxWidth().height(r(260f)), contentAlignment = Alignment.Center) {
                Box(Modifier.size(r(250f)).glow(p.logoInk, .18f))
                FitTxt(name.arabic, arabicStyle(rs(60f) * Settings.arabicScale, p.logoInk), Modifier.hero(heroKey), min = 20.sp)
            }
            Reveal(120) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (Settings.showTransliteration) {
                        Txt(name.transliteration, style(rs(30f), p.onHero, FontWeight.Bold, letterSpacing = (-.9).sp, lineHeight = rs(35f), align = TextAlign.Center))
                    }
                    if (Settings.showEnglish) {
                        Spacer(Modifier.height(6.dp))
                        Txt(name.english, style(rs(17f), p.heroMuted, FontWeight.Medium, align = TextAlign.Center))
                    }
                }
            }
        }
        Spacer(Modifier.height(r(24f)))
        Reveal(220, modifier = Modifier.padding(horizontal = r(16f))) {
            Column {
                if (Settings.showUrdu) {
                    GroupedSection("Urdu meaning", rows = listOf {
                        Txt(name.urdu, urduStyle(rs(30f), p.title, lineHeight = 1.8f), Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 12.dp))
                    })
                    Spacer(Modifier.height(20.dp))
                }
                GroupedSection("Reflection", labelColor = p.accent, rows = listOf {
                    Txt(name.explanation, style(17.sp, p.body, lineHeight = 27.sp), Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 20.dp))
                })
            }
        }
    }
}
