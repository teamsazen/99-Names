# 99 Names — Urdu wordmark

Approved direction: **A, stacked Nastaleeq**, spelling **اسماء الحسنیٰ**.
Derived from the app's existing Jameel Noori Nastaleeq font, with a custom stacked
arrangement and detached flourish. Font credits remain in Settings and README.

## Masters

- `wordmark.svg`: black, transparent background; source of truth for geometry.
- `wordmark-white.svg`: reversed lettering.
- `wordmark-bronze.svg`: brand lettering on a transparent background.
- `app-icon.svg`: bronze lettering on the ink-blue square.

Brand colours: ink blue **#101820**, muted bronze **#D9BC89**.
The icon uses the same colours in light and dark mode.

Keep the proportions and the whitespace in the 256 × 256 master. Do not stretch,
rotate, add shadows, separate the diacritics, or add competing patterns. Give the
standalone lettering at least 16 master units of clear space. Prefer 64 px or
larger for reading the full phrase; at favicon sizes it serves as a recognisable
silhouette. Android foreground and monochrome layers use 66% scale to protect
lettering under launcher masks.

## Where it is used

- In the app, `AppLogo` (`native/…/ui/AppLogo.kt`) draws the lettering from
  `Wordmark.kt`, the outlines of `wordmark.svg` as Compose paths. No SVG library
  and no font loading at startup.
- The launcher icons (`native/app/src/main/res/mipmap-*`), the Play Store icon
  (`store/app_icon.png`), the website icons and `.github/readme/icon.png` are
  exports of `app-icon.svg`. After changing it, export them again at the same
  sizes.

The square source deliberately has no rounded corners; launcher masks and UI
widgets apply their own corner treatment. Third-party font terms and the
project's branding policy continue to apply.
