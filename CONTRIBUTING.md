# Contributing to 99 Names

Thank you for wanting to help. 99 Names is a small, free app about the Names of
Allah, so every change is checked carefully. Please read this page before you
open an issue or a pull request.

## Contribute when you have something that makes the app better

Please contribute when you bring something real, for example:

- **A correction to the content**: an Arabic spelling, a transliteration, or an
  English or Urdu meaning or reflection, **with a reliable source**: a book,
  a hadith collection or a published translation, with the page or reference
- **Better data**: a more accurate translation, or a new language, from a known
  and trusted source
- **A bug you found and fixed**: something that is wrong, crashes or looks
  broken, on a phone you can name
- **Accessibility**: screen readers, large text, contrast
- **A clear improvement to how the app feels or works**, which you can show
  before and after

Please do **not** send:

- Changes only for the sake of change: renaming, reformatting or rewriting code
  that already works
- Content without a source, or machine-translated text
- New libraries or SDKs, ads, trackers or anything that needs an account
- Changes to the name, logo, icon or branding (see [BRANDING.md](BRANDING.md))

Not sure? [Open an issue](https://github.com/teamsazen/99-names/issues) first
and ask. That saves your time and ours.

## Rules for a pull request

1. **It must not bring new problems.** No crashes, no broken screens, no lost
   favourites or settings, no new warnings. A pull request that fixes one thing
   and breaks another is not accepted.
2. **It must be tested by you**, on a real phone or an emulator, before you send it:
   - `cd native && ./gradlew testDebugUnitTest :app:assembleDebug` passes
   - you used the change in the app yourself
   - every screen you touched looks right in **both light and dark**
3. **Keep these as they are:**
   - exactly **99 Names, in their order** (`data/Names.kt`; the tests check it)
   - the way settings and favourites are saved, so updates keep each user's data
   - colours from the app's palette and the app's own fonts
4. **Changed any text?** Run `python3 tool/subset_fonts.py` (see
   [`tool/fonts_src/README.md`](tool/fonts_src/README.md)), or new letters will
   be missing from the fonts.
5. **One change per pull request**, small and focused.

## What to write in your pull request

A pull request without these details will not be reviewed. The pull request
form asks for them:

- **What** you changed, in a few lines
- **Why**: the problem it solves or what it improves
- **Source** for any content change (book, hadith number, translation, page)
- **How you tested it**: phone or emulator, Android version, light and dark
- **Screenshots** (before and after) for anything you can see
- **Issue** it fixes, if there is one

## Reporting a problem

Open an [issue](https://github.com/teamsazen/99-names/issues) with what
happened, what you expected, the steps to make it happen again, your phone and
Android version, and a screenshot if you can.

Thank you, and may Allah reward you for helping others learn His Names.
