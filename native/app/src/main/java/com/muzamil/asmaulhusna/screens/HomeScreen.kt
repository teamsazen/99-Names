package com.muzamil.asmaulhusna.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muzamil.asmaulhusna.Analytics
import com.muzamil.asmaulhusna.LocalNavigator
import com.muzamil.asmaulhusna.Route
import com.muzamil.asmaulhusna.StatusBarIcons
import com.muzamil.asmaulhusna.data.DivineName
import com.muzamil.asmaulhusna.data.Settings
import com.muzamil.asmaulhusna.data.matches
import com.muzamil.asmaulhusna.data.nameOfTheDay
import com.muzamil.asmaulhusna.data.names
import com.muzamil.asmaulhusna.data.normalizeForSearch
import com.muzamil.asmaulhusna.hero
import com.muzamil.asmaulhusna.ui.AppIcon
import com.muzamil.asmaulhusna.ui.AppLogo
import com.muzamil.asmaulhusna.ui.AppMotion
import com.muzamil.asmaulhusna.ui.AppRadius
import com.muzamil.asmaulhusna.ui.CircleButton
import com.muzamil.asmaulhusna.ui.GlassBar
import com.muzamil.asmaulhusna.ui.Glyph
import com.muzamil.asmaulhusna.ui.Reveal
import com.muzamil.asmaulhusna.ui.SegmentedControl
import com.muzamil.asmaulhusna.ui.FitTxt
import com.muzamil.asmaulhusna.ui.Txt
import com.muzamil.asmaulhusna.ui.arabicStyle
import com.muzamil.asmaulhusna.ui.glow
import com.muzamil.asmaulhusna.ui.palette
import com.muzamil.asmaulhusna.ui.pressable
import com.muzamil.asmaulhusna.ui.r
import com.muzamil.asmaulhusna.ui.rs
import com.muzamil.asmaulhusna.ui.style
import com.muzamil.asmaulhusna.ui.urduStyle
import kotlinx.coroutines.launch

/**
 * Home: a compact ink header (with today's name as one tappable card, and search
 * opening right under it), then the All / Favourites filter and the grid of names
 * straight away. The verse closes the list.
 */
@Composable
fun HomeScreen(
    // Starting state; only screenshot renders pass these.
    initialQuery: String = "",
    initialFavourites: Boolean = false,
) {
    val nav = LocalNavigator.current
    val p = palette
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf(initialQuery) }
    var searching by rememberSaveable { mutableStateOf(initialQuery.isNotEmpty()) }
    var favouritesOnly by rememberSaveable { mutableStateOf(initialFavourites) }
    val listState = rememberLazyListState()
    // The glass bar takes over once the ink header has scrolled away.
    val raised by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    val favs = Settings.favourites
    val q = normalizeForSearch(query)
    val list = names.filter { (!favouritesOnly || it.number in favs) && (q.isEmpty() || it.matches(q)) }
    val today = remember { nameOfTheDay() }
    val side = r(16f)

    fun open(from: List<DivineName>, index: Int, prefix: String) {
        focus.clearFocus()
        Analytics.openName(from[index].number, prefix)
        nav.push(Route.Name(from, index, prefix))
    }
    fun startSearch() {
        searching = true
        scope.launch { listState.animateScrollToItem(0) }
    }
    fun endSearch() {
        searching = false
        query = ""
        focus.clearFocus()
        keyboard?.hide()
    }
    BackHandler(searching) { endSearch() }

    StatusBarIcons(lightIcons = !raised || p.dark)

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), listState) {
            item("header") {
                Header(
                    today = today,
                    searching = searching,
                    query = query,
                    onQuery = { query = it },
                    onSearch = { if (searching) endSearch() else startSearch() },
                    onSettings = { nav.push(Route.Settings) },
                    onOpenToday = { open(names, today.number - 1, "day") },
                )
            }
            item("scope") {
                Box(Modifier.padding(start = side, end = side, top = 4.dp, bottom = 14.dp)) {
                    SegmentedControl(listOf(false, true), favouritesOnly, { favouritesOnly = it }) { fav, selected ->
                        val st = style(14.sp, if (selected) p.title else p.muted, if (selected) FontWeight.Bold else FontWeight.Medium)
                        if (!fav) {
                            Txt(if (q.isEmpty()) "All 99" else "Results  ${list.size}", st, maxLines = 1)
                        } else Row(verticalAlignment = Alignment.CenterVertically) {
                            AppIcon(Glyph.HeartFilled, p.accent, 14.dp)
                            Spacer(Modifier.width(6.dp))
                            Txt("Favourites  ${favs.size}", st, maxLines = 1)
                        }
                    }
                }
            }
            if (list.isEmpty()) {
                item("empty") { EmptyState(favouritesOnly && query.isEmpty()) }
            } else {
                items(list.chunked(2), key = { row -> "g${row.first().number}" }) { row ->
                    Row(
                        Modifier.padding(horizontal = side).padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        row.forEach { n -> NameGridCard(n, Modifier.weight(1f)) { open(list, list.indexOf(n), "all") } }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            if (!searching) item("verse") { Verse(Modifier.padding(start = side, end = side, top = r(18f))) }
            item("end") {
                Spacer(Modifier.height(r(28f)))
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }

        GlassBar(
            raised = raised,
            title = "99 Names",
            hideUntilRaised = true,
            trailing = {
                CircleButton(Glyph.Search, "Search") { startSearch() }
                CircleButton(Glyph.Sliders, "Settings") { nav.push(Route.Settings) }
            },
        )
    }
}

/** The ink header: brand row, search (when open) and today's name as one compact card. */
@Composable
private fun Header(
    today: DivineName,
    searching: Boolean,
    query: String,
    onQuery: (String) -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onOpenToday: () -> Unit,
) {
    val p = palette
    Column(Modifier.fillMaxWidth().background(p.hero)) {
        Row(
            Modifier.statusBarsPadding().padding(start = 20.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppLogo(34.dp, AppRadius.icon)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Txt("99 Names", style(18.sp, p.onHero, FontWeight.Bold, letterSpacing = (-.3).sp))
                Txt("Asmaul Husna", style(12.5.sp, p.heroMuted))
            }
            CircleButton(if (searching) Glyph.Close else Glyph.Search, if (searching) "Close search" else "Search", onHero = true, onClick = onSearch)
            Spacer(Modifier.width(8.dp))
            CircleButton(Glyph.Sliders, "Settings", onHero = true, onClick = onSettings)
        }

        AnimatedVisibility(
            searching,
            enter = expandVertically(tween(AppMotion.FAST, easing = AppMotion.easeOut)) + fadeIn(tween(AppMotion.FAST)),
            exit = shrinkVertically(tween(160)) + fadeOut(tween(120)),
        ) {
            SearchField(query, onQuery, Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp))
        }

        AnimatedVisibility(
            !searching,
            enter = expandVertically(tween(AppMotion.FAST, easing = AppMotion.easeOut)) + fadeIn(tween(AppMotion.FAST)),
            exit = shrinkVertically(tween(160)) + fadeOut(tween(120)),
        ) {
            Reveal(60, modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                TodayCard(today, onOpenToday)
            }
        }

        // The page rises over the header with rounded shoulders.
        Box(Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).background(p.background))
    }
}

/** Today's name in one line: gold Arabic, its name and meaning, and a way in. */
@Composable
private fun TodayCard(today: DivineName, onOpen: () -> Unit) {
    val p = palette
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(AppRadius.card)).background(p.onHero.copy(alpha = .07f))
            .pressable(onOpen, description = "Name of the day: ${today.transliteration}")
            .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(r(96f)).glow(p.logoInk, .2f).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
            FitTxt(today.arabic, arabicStyle(rs(30f) * Settings.arabicScale, p.logoInk), Modifier.hero("day-${today.number}"))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Txt("NAME OF THE DAY · ${today.number}", style(11.sp, p.logoInk, FontWeight.Bold, letterSpacing = 1.2.sp))
            Spacer(Modifier.height(3.dp))
            Txt(today.transliteration, style(rs(20f), p.onHero, FontWeight.Bold, letterSpacing = (-.4).sp), maxLines = 1)
            Txt(today.english, style(rs(14f), p.heroMuted), maxLines = 1)
        }
        AppIcon(Glyph.ChevronRight, p.heroMuted, 22.dp)
    }
}

/** Al-A'raf 7:180, set as a quiet closing quote. */
@Composable
private fun Verse(modifier: Modifier) {
    val p = palette
    Column(modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Txt("اور اللہ کے سب نام اچھے ہی اچھے ہیں، تو اسے انہی ناموں سے پکارو", urduStyle(rs(25f), p.body, lineHeight = 1.7f))
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f).height(1.dp).background(p.border))
            Txt("AL-A'RAF 7:180", style(11.5.sp, p.accent, FontWeight.Bold, letterSpacing = 1.4.sp), Modifier.padding(horizontal = 12.dp))
            Box(Modifier.weight(1f).height(1.dp).background(p.border))
        }
    }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit, modifier: Modifier) {
    val p = palette
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { focus.requestFocus(); keyboard?.show() }
    Row(
        modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(50)).background(p.onHero.copy(alpha = .1f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(16.dp))
        AppIcon(Glyph.Search, p.heroMuted, 19.dp)
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Txt("Name, meaning or number", style(16.sp, p.heroMuted), maxLines = 1)
            BasicTextField(
                value, onChange,
                textStyle = style(16.sp, p.onHero),
                singleLine = true,
                cursorBrush = SolidColor(p.logoInk),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
            )
        }
        AnimatedVisibility(value.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier.padding(end = 10.dp).size(26.dp).clip(CircleShape).background(p.onHero.copy(alpha = .14f))
                    .pressable({ onChange("") }, description = "Clear search"),
                contentAlignment = Alignment.Center,
            ) { AppIcon(Glyph.Close, p.onHero, 14.dp) }
        }
        if (value.isEmpty()) Spacer(Modifier.width(14.dp))
    }
}

@Composable
private fun EmptyState(favourites: Boolean) {
    val p = palette
    Reveal {
        Column(Modifier.fillMaxWidth().padding(start = 32.dp, end = 32.dp, top = 32.dp, bottom = 64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(60.dp).clip(CircleShape).background(p.accentDeep), contentAlignment = Alignment.Center) {
                AppIcon(if (favourites) Glyph.Heart else Glyph.Search, p.accent, 26.dp)
            }
            Spacer(Modifier.height(14.dp))
            Txt(if (favourites) "No favourites yet" else "No matching names", style(17.sp, p.title, FontWeight.Bold, letterSpacing = (-.2).sp))
            Spacer(Modifier.height(6.dp))
            Txt(
                if (favourites) "Open a name and tap the heart to keep it here."
                else "Try a transliteration, an English meaning or a number.",
                style(14.sp, p.muted, lineHeight = 20.sp, align = TextAlign.Center),
            )
        }
    }
}
