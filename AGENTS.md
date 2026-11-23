# Asmaul Husna — agent notes

- Flutter app `com.muzamil.asmaulhusna`, Android + iOS. Content is offline. Packages: `shared_preferences`, `in_app_update_flutter` (store update check on launch, `lib/src/update/app_update.dart`), and `firebase_core` + `firebase_analytics` — Android release builds only (`lib/src/analytics/analytics.dart`; manifest flag `analyticsEnabled` in `android/app/build.gradle.kts`). Never start Firebase on iOS or in debug. Update `docs/privacy-policy/` when what is logged changes. Don't add dependencies without asking.
- Colours only via `context.palette` (`AppPalette` in `lib/src/ui/theme.dart`); every screen must look right in light and dark.
- Fonts via `AppFonts`: Google Sans (UI and titles), PDMS Saleem Quran (Arabic), Jameel Noori Nastaleeq (Urdu). Arabic/Urdu fonts draw small, so their sizes are large on purpose.
- Content: `lib/src/data/names.dart` — exactly 99 names in order (tested).

## tool/
- `tool/subset_fonts.py` — `assets/fonts/` are trimmed to the text in `lib/`. **After changing any text, run it** (`pip install fonttools uharfbuzz`; full fonts go in `tool/fonts_src/`, not committed — see its README). Skipping it shows missing glyphs.
- Store and website screenshots: `native/…/StoreShots.kt` renders the real screens with Paparazzi (`cd native && ./gradlew :app:recordPaparazziDebug` → `app/src/test/snapshots/images/`). Copy them to `store/listing/screens/`, run `store/listing/render.sh` (8 Play shots 1620×2880 + feature graphic in `out/`). No people in the listing.
- `tool/icon/make_icons.sh` — icon = `AppLogo` widget (`lib/src/ui/app_logo.dart`). After changing it, run this to regenerate all iOS/Android icons (needs ImageMagick).

## Rules
- New font → also credit it in Settings (`_fontCredits`) and README.
- Before committing: `dart format lib`, `flutter analyze`, `flutter test`.
- Small conventional commits (`feat:`, `style:`, `build:`…); push to `teamsazen/99-Names` (`main`).
