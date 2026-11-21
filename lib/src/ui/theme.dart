import 'dart:math' as math;

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

/// Every colour the app paints with. Light and dark share one shape, so each
/// widget reads `context.palette` and both themes keep the same look.
///
/// One accent, locked: bronze-gold. It marks the sacred text, the active
/// state and the primary action, nothing else. Neutrals carry the rest, and
/// the ink [hero] surface (the colour of the app icon) is the brand moment.
@immutable
class AppPalette extends ThemeExtension<AppPalette> {
  const AppPalette({
    required this.background,
    required this.surface,
    required this.surfaceRaised,
    required this.border,
    required this.accent,
    required this.onAccent,
    required this.accentDeep,
    required this.gold,
    required this.title,
    required this.body,
    required this.muted,
    required this.faint,
    required this.patternInk,
    required this.shadow,
    required this.logoBackground,
    required this.logoInk,
    required this.hero,
    required this.onHero,
    required this.heroMuted,
  });

  final Color background;
  final Color surface;
  final Color surfaceRaised;
  final Color border;

  /// The one accent. [gold] is the same colour, kept for readability where
  /// the meaning is "sacred" rather than "interactive".
  final Color accent;
  final Color onAccent;

  /// Accent tint for selected backgrounds and badges.
  final Color accentDeep;
  final Color gold;
  final Color title;
  final Color body;
  final Color muted;
  final Color faint;

  /// Base colour of the geometric line patterns.
  final Color patternInk;
  final Color shadow;
  final Color logoBackground;
  final Color logoInk;

  /// Deep ink brand surface, used for hero blocks in both themes.
  final Color hero;
  final Color onHero;
  final Color heroMuted;

  static const dark = AppPalette(
    logoBackground: Color(0xFF101820),
    logoInk: Color(0xFFD9BC89),
    background: Color(0xFF0A0E12),
    surface: Color(0xFF151B21),
    surfaceRaised: Color(0xFF1F272E),
    border: Color(0xFF262F37),
    accent: Color(0xFFD9BC89),
    onAccent: Color(0xFF14100A),
    accentDeep: Color(0xFF2A241A),
    gold: Color(0xFFD9BC89),
    title: Color(0xFFF4F1EA),
    body: Color(0xFFC9C4BA),
    muted: Color(0xFF9A958B),
    faint: Color(0xFF6B675F),
    patternInk: Color(0xFFD9BC89),
    shadow: Color(0x00000000),
    hero: Color(0xFF16202A),
    onHero: Color(0xFFF7F3EA),
    heroMuted: Color(0xFFA9B2B9),
  );

  static const light = AppPalette(
    logoBackground: Color(0xFF101820),
    logoInk: Color(0xFFD9BC89),
    background: Color(0xFFF6F3EC),
    surface: Color(0xFFFFFFFF),
    surfaceRaised: Color(0xFFECE7DC),
    border: Color(0xFFE6E0D4),
    accent: Color(0xFF86622B),
    onAccent: Colors.white,
    accentDeep: Color(0xFFF3EADB),
    gold: Color(0xFF86622B),
    title: Color(0xFF1A1712),
    body: Color(0xFF4A453D),
    muted: Color(0xFF7A7368),
    faint: Color(0xFFA9A195),
    patternInk: Color(0xFF86622B),
    shadow: Color(0x141A1712),
    hero: Color(0xFF101820),
    onHero: Color(0xFFF7F3EA),
    heroMuted: Color(0xFFA9B2B9),
  );

  @override
  AppPalette copyWith() => this;

  @override
  AppPalette lerp(AppPalette? other, double t) {
    if (other == null) return this;
    Color l(Color a, Color b) => Color.lerp(a, b, t)!;
    return AppPalette(
      background: l(background, other.background),
      surface: l(surface, other.surface),
      surfaceRaised: l(surfaceRaised, other.surfaceRaised),
      border: l(border, other.border),
      accent: l(accent, other.accent),
      onAccent: l(onAccent, other.onAccent),
      accentDeep: l(accentDeep, other.accentDeep),
      gold: l(gold, other.gold),
      title: l(title, other.title),
      body: l(body, other.body),
      muted: l(muted, other.muted),
      faint: l(faint, other.faint),
      patternInk: l(patternInk, other.patternInk),
      shadow: l(shadow, other.shadow),
      logoBackground: l(logoBackground, other.logoBackground),
      logoInk: l(logoInk, other.logoInk),
      hero: l(hero, other.hero),
      onHero: l(onHero, other.onHero),
      heroMuted: l(heroMuted, other.heroMuted),
    );
  }
}

abstract final class AppFonts {
  static const sans = 'Google Sans';
  static const arabic = 'PDMS Saleem Quran';
  static const urdu = 'Jameel Noori Nastaleeq';
}

/// Shape lock: actions and inputs are pills, cards are [card], small
/// thumbnails are [tile], icon squares are [icon]. Nothing else.
abstract final class AppRadius {
  static const card = 24.0;
  static const tile = 16.0;
  static const icon = 8.0;
}

/// Motion tokens. Strong ease-out for things entering or responding, short
/// durations, and a faster exit than entry.
abstract final class AppMotion {
  static const easeOut = Cubic(0.23, 1, 0.32, 1);
  static const easeInOut = Cubic(0.77, 0, 0.175, 1);

  /// iOS-like sheet / page curve.
  static const drawer = Cubic(0.32, 0.72, 0, 1);

  static const press = Duration(milliseconds: 100);
  static const release = Duration(milliseconds: 180);
  static const fast = Duration(milliseconds: 200);
  static const page = Duration(milliseconds: 380);
}

ThemeData buildTheme(Brightness brightness) {
  final dark = brightness == Brightness.dark;
  final p = dark ? AppPalette.dark : AppPalette.light;
  const cupertinoText = CupertinoTextThemeData();
  return ThemeData(
    useMaterial3: true,
    brightness: brightness,
    fontFamily: AppFonts.sans,
    scaffoldBackgroundColor: p.background,
    extensions: [p],
    colorScheme:
        ColorScheme.fromSeed(
          seedColor: p.accent,
          brightness: brightness,
        ).copyWith(
          primary: p.accent,
          onPrimary: p.onAccent,
          secondary: p.gold,
          surface: p.surface,
          onSurface: p.title,
          outline: p.border,
        ),
    cupertinoOverrideTheme: CupertinoThemeData(
      brightness: brightness,
      primaryColor: p.accent,
      scaffoldBackgroundColor: p.background,
      textTheme: CupertinoTextThemeData(
        textStyle: cupertinoText.textStyle.copyWith(fontFamily: AppFonts.sans),
      ),
    ),
    textTheme: TextTheme(
      bodyMedium: TextStyle(fontSize: 14, color: p.body, height: 1.4),
    ),
    appBarTheme: AppBarTheme(
      backgroundColor: p.background,
      foregroundColor: p.title,
      surfaceTintColor: Colors.transparent,
      elevation: 0,
      scrolledUnderElevation: 0,
      centerTitle: true,
      titleTextStyle: TextStyle(
        fontFamily: AppFonts.sans,
        fontSize: 17,
        fontWeight: FontWeight.w700,
        color: p.title,
      ),
    ),
    snackBarTheme: SnackBarThemeData(
      behavior: SnackBarBehavior.floating,
      backgroundColor: p.title,
      elevation: 0,
      shape: const StadiumBorder(),
      contentTextStyle: TextStyle(
        fontFamily: AppFonts.sans,
        fontSize: 14,
        fontWeight: FontWeight.w500,
        color: p.background,
      ),
    ),
    sliderTheme: SliderThemeData(
      activeTrackColor: p.accent,
      thumbColor: p.accent,
      inactiveTrackColor: p.surfaceRaised,
    ),
    splashFactory: NoSplash.splashFactory,
    highlightColor: Colors.transparent,
    textSelectionTheme: TextSelectionThemeData(
      cursorColor: p.accent,
      selectionColor: p.accent.withValues(alpha: .3),
      selectionHandleColor: p.accent,
    ),
    dividerColor: p.border,
  );
}

extension AppContext on BuildContext {
  AppPalette get palette => Theme.of(this).extension<AppPalette>()!;

  bool get isDark => Theme.of(this).brightness == Brightness.dark;

  /// Scales a design value (authored at 390×844) to the current screen.
  double r(double value) {
    final size = MediaQuery.sizeOf(this);
    final k = math.min(size.width / 390, size.height / 844).clamp(.82, 1.2);
    return value * k;
  }
}

void showToast(BuildContext context, String text) {
  ScaffoldMessenger.of(context)
    ..hideCurrentSnackBar()
    ..showSnackBar(SnackBar(content: Text(text)));
}
