package com.muzamil.asmaulhusna.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muzamil.asmaulhusna.Actions
import com.muzamil.asmaulhusna.LocalNavigator
import com.muzamil.asmaulhusna.LocalThemeReveal
import com.muzamil.asmaulhusna.R
import com.muzamil.asmaulhusna.Route
import com.muzamil.asmaulhusna.StatusBarIcons
import com.muzamil.asmaulhusna.Toast
import com.muzamil.asmaulhusna.data.Settings
import com.muzamil.asmaulhusna.data.ThemeMode
import com.muzamil.asmaulhusna.ui.AppIcon
import com.muzamil.asmaulhusna.ui.AppLogo
import com.muzamil.asmaulhusna.ui.AppRadius
import com.muzamil.asmaulhusna.ui.AppSlider
import com.muzamil.asmaulhusna.ui.AppSwitch
import com.muzamil.asmaulhusna.ui.Card
import com.muzamil.asmaulhusna.ui.CircleButton
import com.muzamil.asmaulhusna.ui.GlassBar
import com.muzamil.asmaulhusna.ui.Glyph
import com.muzamil.asmaulhusna.ui.GroupedSection
import com.muzamil.asmaulhusna.ui.IconTile
import com.muzamil.asmaulhusna.ui.LargeTitle
import com.muzamil.asmaulhusna.ui.SectionLabel
import com.muzamil.asmaulhusna.ui.SegmentedControl
import com.muzamil.asmaulhusna.ui.Txt
import com.muzamil.asmaulhusna.ui.arabicStyle
import com.muzamil.asmaulhusna.ui.palette
import com.muzamil.asmaulhusna.ui.r
import com.muzamil.asmaulhusna.ui.rowButton
import com.muzamil.asmaulhusna.ui.style
import com.muzamil.asmaulhusna.ui.urduStyle

const val APP_VERSION = "2.0.1"

private data class OtherApp(val name: String, val tagline: String, val icon: Int, val packageName: String)

/** Sazen's live apps on Google Play (developer "Sazen"), Islamic apps first. */
private val otherApps = listOf(
    OtherApp("Quran Urdu Audio", "Urdu translation, word by word", R.drawable.app_quran_urdu_audio, "com.sazen.urdu.audio.quran.transcribed.shamshad.ali.khan"),
    OtherApp("Rafeeq", "Quran & prayer times", R.drawable.app_rafeeq, "com.satechdev.rafeeqapp"),
    OtherApp("SalahLog", "Salah & namaz tracker", R.drawable.app_salahlog, "com.track_prayer"),
    OtherApp("Quran Khatam Tracker", "Daily Quran reading", R.drawable.app_quran_khatam_tracker, "com.quran.khatam.tracker"),
    OtherApp("Exit Rush", "Arrow puzzle game", R.drawable.app_exit_rush, "com.sazen.exit.rush"),
    OtherApp("Bg Remover", "Background remover & 4K photo enhancer", R.drawable.app_bg_remover, "com.sazen.bg.remover"),
    OtherApp("Budget AI", "Expense tracker", R.drawable.app_budget_ai, "com.budgetai.android"),
    OtherApp("FM Crafts", "Crochet gifts", R.drawable.app_fm_crafts, "com.fmcrafts.fmcrafts"),
)

private data class FontCredit(val font: String, val usedFor: String, val note: String, val source: String, val url: String)

private val fontCredits = listOf(
    FontCredit("Jameel Noori Nastaleeq", "Urdu", "Free of charge for Urdu lovers", "urdufonts.net", "https://urdufonts.net/fonts/jameel-noori-nastaleeq-regular"),
    FontCredit("Harmattan", "Arabic", "SIL Open Font License 1.1 · SIL International", "Google Fonts", "https://fonts.google.com/specimen/Harmattan"),
    FontCredit("Google Sans", "Text", "SIL Open Font License 1.1", "Google Fonts", "https://fonts.google.com/specimen/Google+Sans"),
)

@Composable
fun SettingsScreen() {
    val p = palette
    val nav = LocalNavigator.current
    val context = LocalContext.current
    val switchTheme = LocalThemeReveal.current
    val list = rememberLazyListState()
    val raised by remember { derivedStateOf { list.firstVisibleItemIndex > 1 || (list.firstVisibleItemIndex == 1 && list.firstVisibleItemScrollOffset > 60) } }
    val side = r(16f)
    val gap: @Composable () -> Unit = { Spacer(Modifier.height(28.dp)) }

    StatusBarIcons(lightIcons = p.dark)

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), list, contentPadding = PaddingValues(horizontal = side)) {
            item { Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars)); Spacer(Modifier.height(58.dp)) }
            item { Box(Modifier.padding(start = 4.dp, bottom = 20.dp)) { LargeTitle("Settings") } }
            item { BrandCard(); gap() }
            item {
                SectionLabel("Appearance")
                SegmentedControl(
                    ThemeMode.entries, Settings.themeMode, { switchTheme(it) },
                    height = 68.dp, background = p.surface, thumb = p.accentDeep,
                ) { mode, selected ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AppIcon(
                            when (mode) { ThemeMode.System -> Glyph.Auto; ThemeMode.Light -> Glyph.Sun; ThemeMode.Dark -> Glyph.Moon },
                            if (selected) p.accent else p.muted, 20.dp,
                        )
                        Spacer(Modifier.height(4.dp))
                        Txt(mode.name, style(13.5.sp, if (selected) p.title else p.muted, if (selected) FontWeight.Bold else FontWeight.Medium))
                    }
                }
                gap()
            }
            item {
                GroupedSection("Show on cards", rows = listOf(
                    { SwitchRow("Transliteration", "Ar-Rahman", false, Settings.showTransliteration, Settings::setShowTransliteration) },
                    { SwitchRow("English meaning", "The Most Merciful", false, Settings.showEnglish, Settings::setShowEnglish) },
                    { SwitchRow("Urdu meaning", "بہت رحم کرنے والا", true, Settings.showUrdu, Settings::setShowUrdu) },
                ))
                gap()
            }
            item {
                GroupedSection("Arabic text size", rows = listOf(
                    {
                        Box(Modifier.fillMaxWidth().height(84.dp), contentAlignment = Alignment.Center) {
                            Txt("الرَّحْمٰنُ", arabicStyle((32 * Settings.arabicScale).sp, p.title))
                        }
                    },
                    {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Txt("A", style(13.sp, p.muted))
                            AppSlider(Settings.arabicScale, .8f, 1.4f, 6, Settings::setArabicScale, Modifier.weight(1f).padding(horizontal = 8.dp))
                            Txt("A", style(20.sp, p.muted))
                        }
                    },
                ))
                gap()
            }
            item {
                GroupedSection("Support the app", dividerIndent = 60.dp, rows = listOf(
                    { LinkRow("Share app", "Share 99 Names with friends and family", { IconTile(Glyph.Share, p.accent) }) { Actions.share(context) } },
                    { LinkRow("Rate on Google Play", "Leave a review on the Play Store", { IconTile(Glyph.Star, p.accent) }) { Actions.rate(context) } },
                ))
                gap()
            }
            item {
                GroupedSection("Our other apps", dividerIndent = 72.dp, rows = otherApps.map { app ->
                    @Composable {
                        LinkRow(app.name, app.tagline, {
                            Image(
                                painterResource(app.icon), null,
                                Modifier.size(42.dp).clip(RoundedCornerShape(AppRadius.icon)),
                                contentScale = ContentScale.Crop,
                            )
                        }, Glyph.External) { Actions.openStore(context, app.packageName) }
                    }
                } + listOf<@Composable () -> Unit>({
                    LinkRow("Our website", "All our apps and games · teamsazen.com", { SazenTile(42.dp) }, Glyph.External) {
                        Actions.openSazen(context, "settings_row")
                    }
                }))
                gap()
            }
            item {
                GroupedSection("Legal", dividerIndent = 60.dp, rows = listOf(
                    { LinkRow("Privacy Policy", "What the app keeps and why", { IconTile(Glyph.Shield, p.accent) }, Glyph.External) { Actions.openPage(context, "/privacy/") } },
                    { LinkRow("Terms & Conditions", "Using the app and its content", { IconTile(Glyph.Document, p.accent) }, Glyph.External) { Actions.openPage(context, "/terms/") } },
                ))
                gap()
            }
            item {
                GroupedSection("Fonts & credits", rows = fontCredits.map { c ->
                    @Composable { CreditRow(c) }
                } + listOf<@Composable () -> Unit>({ LinkRow("Open Source Licences", null, null) { nav.push(Route.Licences) } }))
            }
            item { PoweredBy { Actions.openSazen(context, "powered_by") } }
            item { Spacer(Modifier.height(24.dp)); Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
        }
        GlassBar(raised, "Settings", leading = { CircleButton(Glyph.Back, "Back") { nav.pop() } })
    }
}

/** Ink brand card: the app icon, name, version and promise. */
@Composable
private fun BrandCard() {
    val p = palette
    Card(Modifier.fillMaxWidth(), color = p.hero) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppLogo(56.dp, AppRadius.tile)
                Spacer(Modifier.width(14.dp))
                Column {
                    Txt("99 Names", style(20.sp, p.onHero, FontWeight.Bold, letterSpacing = (-.5).sp))
                    Txt("Version $APP_VERSION", style(13.sp, p.heroMuted).copy(fontFeatureSettings = "tnum"))
                }
            }
            Spacer(Modifier.height(14.dp))
            Txt("The 99 beautiful names of Allah with transliteration and English and Urdu meanings.", style(14.5.sp, p.onHero, lineHeight = 22.sp))
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (label in listOf("Works offline", "No account", "No ads")) {
                    Row(
                        Modifier.clip(RoundedCornerShape(50)).background(p.onHero.copy(alpha = .08f)).padding(start = 10.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(p.logoInk))
                        Spacer(Modifier.width(7.dp))
                        Txt(label, style(12.5.sp, p.onHero, FontWeight.SemiBold))
                    }
                }
            }
        }
    }
}

/** The Sazen "S" on an ink tile, the same shape as the other apps' icons. */
@Composable
private fun SazenTile(size: Dp) {
    val p = palette
    Box(Modifier.size(size).clip(RoundedCornerShape(AppRadius.icon)).background(p.hero), contentAlignment = Alignment.Center) {
        Image(painterResource(R.drawable.sazen_logo), null, Modifier.size(size * .56f), colorFilter = ColorFilter.tint(p.onHero))
    }
}

/** "Powered by Sazen" at the foot of Settings; tapping it opens Sazen's website. */
@Composable
private fun PoweredBy(onClick: () -> Unit) {
    val p = palette
    Box(Modifier.fillMaxWidth().padding(top = 28.dp), contentAlignment = Alignment.Center) {
        Row(
            Modifier.clip(RoundedCornerShape(50)).rowButton(onClick).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Txt("Powered by", style(13.sp, p.muted))
            Spacer(Modifier.width(8.dp))
            Image(painterResource(R.drawable.sazen_logo), null, Modifier.size(16.dp), colorFilter = ColorFilter.tint(p.title))
            Spacer(Modifier.width(5.dp))
            Txt("Sazen", style(14.sp, p.title, FontWeight.Bold, letterSpacing = (-.2).sp))
        }
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, urdu: Boolean, value: Boolean, onChange: (Boolean) -> Unit) {
    val p = palette
    Row(
        Modifier.fillMaxWidth().rowButton { onChange(!value) }.padding(start = 16.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Txt(title, style(15.5.sp, p.title, FontWeight.SemiBold))
            Txt(subtitle, if (urdu) urduStyle(18.sp, p.muted, androidx.compose.ui.text.style.TextAlign.Start, 1.5f) else style(13.sp, p.muted))
        }
        AppSwitch(value, onChange)
    }
}

@Composable
private fun LinkRow(
    title: String,
    subtitle: String?,
    leading: (@Composable () -> Unit)?,
    trailing: Glyph = Glyph.ChevronRight,
    onClick: () -> Unit,
) {
    val p = palette
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp).rowButton(onClick).padding(start = 16.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) { leading(); Spacer(Modifier.width(14.dp)) }
        Column(Modifier.weight(1f)) {
            Txt(title, style(15.5.sp, p.title, FontWeight.SemiBold))
            if (subtitle != null) Txt(subtitle, style(13.sp, p.muted), Modifier.padding(top = 1.dp))
        }
        Spacer(Modifier.width(8.dp))
        AppIcon(trailing, p.faint, 20.dp)
    }
}

/** One font with its source; tapping copies the link. */
@Composable
private fun CreditRow(c: FontCredit) {
    val p = palette
    val context = LocalContext.current
    Row(
        Modifier.fillMaxWidth().rowButton {
            Actions.copy(context, c.source, c.url)
            Toast.show("Link to ${c.source} copied")
        }.padding(16.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Txt("${c.font} · ${c.usedFor}", style(15.sp, p.title, FontWeight.SemiBold))
            Spacer(Modifier.height(2.dp))
            Txt(c.note, style(13.sp, p.muted))
            Spacer(Modifier.height(2.dp))
            Txt("Source: ${c.source}", style(13.sp, p.accent, FontWeight.SemiBold))
        }
        Spacer(Modifier.width(12.dp))
        AppIcon(Glyph.Link, p.faint, 18.dp)
    }
}

/** Open source licences: the bundled font's licence and the libraries the app uses. */
@Composable
fun LicencesScreen() {
    val p = palette
    val nav = LocalNavigator.current
    val context = LocalContext.current
    val ofl = remember { runCatching { context.assets.open("GoogleSans-OFL.txt").bufferedReader().readText() }.getOrDefault("") }
    val oflArabic = remember { runCatching { context.assets.open("Harmattan-OFL.txt").bufferedReader().readText() }.getOrDefault("") }
    val list = rememberLazyListState()
    val raised by remember { derivedStateOf { list.firstVisibleItemIndex > 0 } }
    StatusBarIcons(lightIcons = p.dark)
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), list, contentPadding = PaddingValues(horizontal = r(16f))) {
            item { Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars)); Spacer(Modifier.height(58.dp)) }
            item { Box(Modifier.padding(start = 4.dp, bottom = 20.dp)) { LargeTitle("Licences") } }
            item {
                GroupedSection("Libraries", rows = listOf(
                    "Jetpack Compose, AndroidX" to "Apache License 2.0 · The Android Open Source Project",
                    "Firebase Analytics" to "Apache License 2.0 · Google",
                    "Play In-App Update" to "Play Core Software Development Kit Terms of Service · Google",
                    "Kotlin" to "Apache License 2.0 · JetBrains",
                ).map { (a, b) -> @Composable { LicenceRow(a, b) } })
                Spacer(Modifier.height(28.dp))
            }
            item {
                GroupedSection("Google Sans", rows = listOf {
                    Txt(ofl, style(12.5.sp, p.body, lineHeight = 18.sp), Modifier.padding(16.dp))
                })
                Spacer(Modifier.height(28.dp))
            }
            item {
                GroupedSection("Harmattan", rows = listOf {
                    Txt(oflArabic, style(12.5.sp, p.body, lineHeight = 18.sp), Modifier.padding(16.dp))
                })
            }
            item { Spacer(Modifier.height(32.dp)); Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
        }
        GlassBar(raised, "Licences", leading = { CircleButton(Glyph.Back, "Back") { nav.pop() } })
    }
}

@Composable
private fun LicenceRow(title: String, note: String) {
    val p = palette
    Column(Modifier.fillMaxWidth().padding(16.dp, 12.dp)) {
        Txt(title, style(15.sp, p.title, FontWeight.SemiBold))
        Txt(note, style(13.sp, p.muted), Modifier.padding(top = 2.dp))
    }
}

