package com.muzamil.asmaulhusna

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.muzamil.asmaulhusna.data.Settings
import com.muzamil.asmaulhusna.data.names
import com.muzamil.asmaulhusna.screens.HomeScreen
import com.muzamil.asmaulhusna.screens.NameScreen
import com.muzamil.asmaulhusna.screens.SettingsScreen
import com.muzamil.asmaulhusna.ui.LocalPalette
import com.muzamil.asmaulhusna.ui.Palette
import org.junit.Rule
import org.junit.Test

/**
 * Renders the real screens on the JVM, for the Play Store listing and the website:
 * ./gradlew :app:recordPaparazziDebug writes them to app/src/test/snapshots/images/.
 */
class StoreShots {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5, useDeviceResolution = true)

    private fun shot(name: String, dark: Boolean, content: @Composable () -> Unit) {
        Settings.load(paparazzi.context)
        if (Settings.favourites.isEmpty()) listOf(1, 3, 14, 47).forEach(Settings::toggleFavourite)
        paparazzi.snapshot(name) {
            val nav = remember { Navigator() }
            val p = if (dark) Palette.Dark else Palette.Light
            CompositionLocalProvider(LocalPalette provides p, LocalNavigator provides nav, LocalInspectionMode provides true) {
                Box(Modifier.fillMaxSize().background(p.background)) { content() }
            }
        }
    }

    @Test fun homeLight() = shot("home-light", false) { HomeScreen() }
    @Test fun homeDark() = shot("home-dark", true) { HomeScreen() }
    @Test fun nameLight() = shot("name-light", false) { NameScreen(Route.Name(names, 0, "all")) }
    @Test fun nameDark() = shot("name-dark", true) { NameScreen(Route.Name(names, 46, "all")) }
    @Test fun searchLight() = shot("search-light", false) { HomeScreen(initialQuery = "دینے") }
    @Test fun favouritesDark() = shot("favourites-dark", true) { HomeScreen(initialFavourites = true) }
    @Test fun nameUrdu() = shot("name-light-wadud", false) { NameScreen(Route.Name(names, 46, "all")) }
    @Test fun nameDarkRahman() = shot("name-dark-rahman", true) { NameScreen(Route.Name(names, 0, "all")) }
    @Test fun searchDark() = shot("search-dark", true) { HomeScreen(initialQuery = "دینے") }
    @Test fun favouritesLight() = shot("favourites-light", false) { HomeScreen(initialFavourites = true) }
    @Test fun settingsLight() = shot("settings-light", false) { SettingsScreen() }
    @Test fun settingsDark() = shot("settings-dark", true) { SettingsScreen() }
}
