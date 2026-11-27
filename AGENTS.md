# Asmaul Husna — agent notes

- Native Android app in `native/` (Kotlin + Jetpack Compose, no Material), package `com.muzamil.asmaulhusna`. Android only; there is no iOS or Flutter app any more (1.0.0 was Flutter; see git history). Content is offline. Libraries: AndroidX, Firebase Analytics (release builds only: manifest flag `analyticsEnabled` in `native/app/build.gradle.kts`; `Analytics.kt`) and Play in-app updates. Update `docs/privacy-policy/` when what is logged changes. Don't add dependencies without asking.
- Settings are stored in the Flutter app's format (`FlutterSharedPreferences`, keys `flutter.*`, see `data/Settings.kt`) so updates from 1.0.0 keep favourites and settings. Never change that.
- Colours only via `palette` (`Palette` in `ui/Theme.kt`); every screen must look right in light and dark.
- Fonts via `AppFonts`: Google Sans (UI and titles), Harmattan (Arabic), Jameel Noori Nastaleeq (Urdu). Arabic/Urdu fonts draw small, so their sizes are large on purpose.
- Content: `data/Names.kt` — exactly 99 names in order (tested). The website reads it too.
- Secrets stay out of the repo: `native/key.properties` (upload key) and `native/app/google-services.json` are gitignored. Builds work without them.

## tool/ and store/
- `tool/subset_fonts.py` — the fonts in `native/app/src/main/res/font/` and `website/public/fonts/` are trimmed to the text drawn. **After changing any text, run it** (`pip install fonttools uharfbuzz brotli`; full fonts go in `tool/fonts_src/`, not committed — see its README). Skipping it shows missing glyphs.
- Play listing: real-phone screenshots (`adb`) → `store/listing/device/` → `tool/cleanbar.py` paints the status bar → `ready/` → photo phone frames in `composed/` → `store/listing/render.sh` (8 shots 1620×2880 + feature graphic in `out/`) → copy to `store/playstore/`. `render-post.sh` makes 4:5 social posts (`post.html`) from the same phones. Steps in `store/listing/README.md`. Only a hand holding the phone (first shot); no other people.
- Website screenshots: `native/…/StoreShots.kt` renders the real screens with Paparazzi (`cd native && ./gradlew :app:recordPaparazziDebug` → `app/src/test/snapshots/images/`).
- Icon masters are in `design/brand/`; the launcher icons are exports of `app-icon.svg`.

## Rules
- New font → also credit it in Settings (`fontCredits` in `SettingsScreen.kt`) and README.
- Before committing: `cd native && ./gradlew testDebugUnitTest :app:assembleDebug`; for website changes `cd website && node build.mjs`.
- Small conventional commits (`feat:`, `style:`, `build:`…); push to `teamsazen/99-Names` (`main`).
