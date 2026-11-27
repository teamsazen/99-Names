# Play Store listing: 99 Names – Asmaul Husna 2.0.1

Package `com.muzamil.asmaulhusna` · versionCode 4 · versionName 2.0.1 (the native Kotlin app
in `native/`, signed with the same upload key as 1.0.0).

## Privacy policy URL

```
https://99.teamsazen.com/privacy/
```

## App name (max 30)

```
99 Names - Asmaul Husna
```

## Short description (max 80)

```
The 99 Names of Allah in Arabic, with English & Urdu meanings. Works offline.
```

## Full description (max 4000)

```
99 Names brings you the 99 Beautiful Names of Allah in a calm, simple app that you can open anytime, anywhere. Each Name is shown in clear Arabic with full harakat, with its transliteration, English meaning, Urdu meaning and a short reflection.

"To Allah belong the most beautiful names, so call upon Him by them." (Al-A'raf 7:180)

ALL 99 NAMES IN ONE PLACE
• Every Name in the order narrated by at-Tirmidhi
• Clear Arabic with full harakat
• Transliteration, so you can say each Name correctly
• English and Urdu meanings side by side

FROM TRUSTED SOURCES
• Arabic and order from Jami at-Tirmidhi 3507
• English and Urdu meanings from its published translations, not machine translated

A NEW NAME EVERY DAY
• The Name of the day greets you each time you open the app
• The same Name for everyone, through all 99 in turn

LEARN AND REFLECT
• A page for every Name, with a short reflection
• Swipe to the next or previous Name
• Copy any Name with its meanings to share with family and friends

FIND ANY NAME QUICKLY
• Search by transliteration, English or Urdu meaning, Arabic or number

FAVOURITES
• Tap the heart to keep the Names close to your heart
• See all your favourites in one tap

MADE FOR YOU
• Light and dark themes, or follow your phone's setting
• Choose what each card shows: transliteration, English meaning, Urdu meaning
• Make the Arabic larger or smaller to suit your eyes

LIGHT, PRIVATE AND OFFLINE
• A small download of about 2 MB
• Every Name works without internet
• No account, no sign-up and no ads
• Your favourites and settings stay on your phone

Whether you are memorising the 99 Names, teaching them to your children or simply want to remember Allah through His Names during the day, 99 Names keeps them with you, beautifully and quietly.
```

## Release notes, en-US (max 500)

2.0.1:

```
• The Arabic text size slider is smooth now: touch it and slide in one go
• Settings: a link to our website, with all our apps and games
```

2.0.0:

```
A brand-new 99 Names, rebuilt from the ground up:
• A fresh design with light and dark themes
• The Name of the day on the home screen
• Arabic, English and Urdu from trusted sources (Jami at-Tirmidhi and its translations)
• Search in English, Urdu or Arabic
• Smoother, with gentle animations
• Now only about 2 MB
Your favourites and settings are kept.
```

## Graphics

| Play Console field | File | Size |
|---|---|---|
| App icon | `app_icon.png` | 512×512 PNG |
| Feature graphic | `playstore/feature_graphic.png` | 1024×500 PNG |
| Phone screenshots (8) | `playstore/01.png` … `08.png` | 1620×2880 PNG |

`listing/render.sh` writes them to `listing/out/` (not committed); copy them to `playstore/`.

## App content: Data safety

The Android release build sends anonymous usage statistics to Firebase Analytics
(see the privacy policy). Answer:

- Does your app collect or share any of the required user data types? **Yes**
- Is all of the user data collected by your app encrypted in transit? **Yes**
- Do you provide a way for users to request that their data is deleted? **No** (nothing is
  linked to a person; uninstalling clears the on-device data)
- Data types collected (not shared, processed ephemerally: **No**, required: **Yes**, purpose
  **Analytics** for each):
  - **App activity → App interactions** (screens and Names opened, favourites, taps on Share/Rate)
  - **App info and performance → Diagnostics**: not collected (no Crashlytics)
  - **Device or other IDs** (the Firebase app-instance ID)
- Advertising ID: **not used** (`AD_ID` permission removed from the manifest).

## Other App content answers

- Ads: **No ads**
- Target audience: 13+ (or as already set for 1.0.0); not designed for children
- Content rating: unchanged (reference app, no user content)
