import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../ui/app_logo.dart';
import '../ui/icons.dart';
import '../ui/theme.dart';
import '../ui/widgets.dart';
import 'settings_store.dart';

const appVersion = '1.0.0';
const _appActions = MethodChannel('com.muzamil.asmaulhusna/app_actions');

Future<void> _openAppAction(
  BuildContext context,
  String action, [
  String? packageName,
]) async {
  try {
    await _appActions.invokeMethod<void>(
      action,
      packageName == null ? null : {'packageName': packageName},
    );
  } on PlatformException {
    if (context.mounted) {
      showToast(context, 'Could not open this action. Please try again.');
    }
  } on MissingPluginException {
    if (context.mounted) {
      showToast(context, 'This action is available on Android and iOS.');
    }
  }
}

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  bool _raised = false;

  bool _onScroll(ScrollNotification n) {
    if (n.depth == 0) {
      final raised = n.metrics.pixels > context.r(36);
      if (raised != _raised) setState(() => _raised = raised);
    }
    return false;
  }

  @override
  Widget build(BuildContext context) {
    final s = SettingsStore.instance;
    return ListenableBuilder(
      listenable: s,
      builder: (context, _) {
        final p = context.palette;
        const gap = SizedBox(height: 28);
        return Scaffold(
          body: Stack(
            children: [
              NotificationListener<ScrollNotification>(
                onNotification: _onScroll,
                child: ListView(
                  padding: EdgeInsets.fromLTRB(
                    context.r(16),
                    MediaQuery.paddingOf(context).top + GlassBar.height,
                    context.r(16),
                    32 + MediaQuery.paddingOf(context).bottom,
                  ),
                  children: [
                    const Padding(
                      padding: EdgeInsets.only(left: 4, bottom: 20),
                      child: LargeTitle('Settings'),
                    ),
                    const _BrandCard(),
                    gap,
                    const SectionLabel('Appearance'),
                    _ThemePicker(mode: s.themeMode, onChanged: s.setThemeMode),
                    gap,
                    GroupedSection(
                      label: 'Show on cards',
                      children: [
                        _SwitchRow(
                          title: 'Transliteration',
                          subtitle: 'Ar-Rahman',
                          value: s.showTransliteration,
                          onChanged: s.setShowTransliteration,
                        ),
                        _SwitchRow(
                          title: 'English meaning',
                          subtitle: 'The Most Gracious',
                          value: s.showEnglish,
                          onChanged: s.setShowEnglish,
                        ),
                        _SwitchRow(
                          title: 'Urdu meaning',
                          subtitle: 'بے حد مہربان',
                          urduSubtitle: true,
                          value: s.showUrdu,
                          onChanged: s.setShowUrdu,
                        ),
                      ],
                    ),
                    gap,
                    GroupedSection(
                      label: 'Arabic text size',
                      children: [
                        SizedBox(
                          height: 84,
                          child: Center(
                            child: Text(
                              'ٱلرَّحْمَٰنُ',
                              textDirection: TextDirection.rtl,
                              style: TextStyle(
                                fontFamily: AppFonts.arabic,
                                fontSize: 42 * s.arabicScale,
                                height: 1.5,
                                color: p.title,
                              ),
                            ),
                          ),
                        ),
                        Padding(
                          padding: const EdgeInsets.fromLTRB(16, 4, 16, 6),
                          child: Row(
                            children: [
                              Text(
                                'A',
                                style: TextStyle(fontSize: 13, color: p.muted),
                              ),
                              Expanded(
                                child: Slider.adaptive(
                                  value: s.arabicScale,
                                  min: .8,
                                  max: 1.4,
                                  divisions: 6,
                                  onChanged: s.setArabicScale,
                                ),
                              ),
                              Text(
                                'A',
                                style: TextStyle(fontSize: 20, color: p.muted),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                    gap,
                    GroupedSection(
                      label: 'Support the app',
                      dividerIndent: 60,
                      children: [
                        _LinkRow(
                          leading: _IconTile(AppGlyph.share, p.accent),
                          title: 'Share app',
                          subtitle: 'Share 99 Names with friends and family',
                          onTap: () => _openAppAction(context, 'share'),
                        ),
                        _LinkRow(
                          leading: _IconTile(AppGlyph.star, p.accent),
                          title: 'Rate on Google Play',
                          subtitle: 'Leave a review on the Play Store',
                          onTap: () => _openAppAction(context, 'rate'),
                        ),
                      ],
                    ),
                    gap,
                    GroupedSection(
                      label: 'Our other apps',
                      dividerIndent: 72,
                      children: [
                        for (final app in _otherApps)
                          _LinkRow(
                            leading: ClipRSuperellipse(
                              borderRadius: BorderRadius.circular(
                                AppRadius.icon,
                              ),
                              child: Image.asset(
                                app.icon,
                                width: 42,
                                height: 42,
                                fit: BoxFit.cover,
                                cacheWidth: 168,
                                excludeFromSemantics: true,
                              ),
                            ),
                            title: app.name,
                            subtitle: 'View on Google Play',
                            trailing: AppGlyph.external,
                            onTap: () => _openAppAction(
                              context,
                              'openStore',
                              app.packageName,
                            ),
                          ),
                      ],
                    ),
                    gap,
                    GroupedSection(
                      label: 'Fonts & credits',
                      children: [
                        for (final credit in _fontCredits) _CreditRow(credit),
                        _LinkRow(
                          title: 'Open Source Licences',
                          onTap: () => showLicensePage(
                            context: context,
                            applicationName: '99 Names',
                            applicationVersion: appVersion,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              Positioned(
                top: 0,
                left: 0,
                right: 0,
                child: GlassBar(
                  raised: _raised,
                  title: 'Settings',
                  leading: CircleButton(
                    glyph: AppGlyph.back,
                    tooltip: 'Back',
                    onTap: () => Navigator.of(context).pop(),
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}

typedef _FontCredit = ({
  String font,
  String usedFor,
  String note,
  String source,
  String url,
});

const List<_FontCredit> _fontCredits = [
  (
    font: 'Jameel Noori Nastaleeq',
    usedFor: 'Urdu',
    note: 'Free of charge for Urdu lovers',
    source: 'urdufonts.net',
    url: 'https://urdufonts.net/fonts/jameel-noori-nastaleeq-regular',
  ),
  (
    font: 'PDMS Saleem Quran',
    usedFor: 'Arabic',
    note: '© 2001 Pakistan Data Management Services',
    source: 'urdunigaar.com',
    url:
        'https://urdunigaar.com/download/pdms-saleem-quran-font-ttf-file-download/',
  ),
  (
    font: 'Google Sans',
    usedFor: 'Text',
    note: 'SIL Open Font License 1.1',
    source: 'Google Fonts',
    url: 'https://fonts.google.com/specimen/Google+Sans',
  ),
];

/// Ink brand card: the app icon, name, version and promise.
class _BrandCard extends StatelessWidget {
  const _BrandCard();

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    return Surface(
      color: p.hero,
      padding: EdgeInsets.zero,
      child: Stack(
        children: [
          Positioned(
            right: -50,
            bottom: -60,
            child: Rosette(size: 200, color: p.logoInk.withValues(alpha: .12)),
          ),
          Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    const AppLogo(size: 56, radius: AppRadius.tile),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            '99 Names',
                            style: TextStyle(
                              fontSize: 20,
                              fontWeight: FontWeight.w700,
                              letterSpacing: -.5,
                              color: p.onHero,
                            ),
                          ),
                          Text(
                            'Version $appVersion',
                            style: TextStyle(
                              fontSize: 13,
                              fontFeatures: const [
                                FontFeature.tabularFigures(),
                              ],
                              color: p.heroMuted,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 14),
                Text(
                  'The 99 beautiful names of Allah with transliteration and '
                  'English and Urdu meanings.',
                  style: TextStyle(
                    fontSize: 14.5,
                    height: 1.5,
                    color: p.onHero,
                  ),
                ),
                const SizedBox(height: 12),
                Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: [
                    for (final label in const [
                      'Offline',
                      'No account',
                      'No tracking',
                    ])
                      Container(
                        padding: const EdgeInsets.fromLTRB(10, 6, 12, 6),
                        decoration: ShapeDecoration(
                          color: p.onHero.withValues(alpha: .08),
                          shape: const StadiumBorder(),
                        ),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Container(
                              width: 6,
                              height: 6,
                              decoration: BoxDecoration(
                                shape: BoxShape.circle,
                                color: p.logoInk,
                              ),
                            ),
                            const SizedBox(width: 7),
                            Text(
                              label,
                              style: TextStyle(
                                fontSize: 12.5,
                                fontWeight: FontWeight.w600,
                                color: p.onHero,
                              ),
                            ),
                          ],
                        ),
                      ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

/// One font with its source. The app is offline, so tapping copies the link
/// instead of opening it.
class _CreditRow extends StatelessWidget {
  const _CreditRow(this.credit);
  final _FontCredit credit;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    return RowButton(
      onTap: () {
        Clipboard.setData(ClipboardData(text: credit.url));
        showToast(context, 'Link to ${credit.source} copied');
      },
      child: Padding(
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 12),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    '${credit.font} · ${credit.usedFor}',
                    style: TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w600,
                      color: p.title,
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    credit.note,
                    style: TextStyle(fontSize: 13, color: p.muted),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    'Source: ${credit.source}',
                    style: TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w600,
                      color: p.accent,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 12),
            AppIcon(AppGlyph.link, size: 18, color: p.faint),
          ],
        ),
      ),
    );
  }
}

/// Small rounded square holding a white glyph, like an iOS Settings icon.
class _IconTile extends StatelessWidget {
  const _IconTile(this.icon, this.color);
  final AppGlyph icon;
  final Color color;

  @override
  Widget build(BuildContext context) => Container(
    width: 30,
    height: 30,
    decoration: ShapeDecoration(
      color: color,
      shape: RoundedSuperellipseBorder(
        borderRadius: BorderRadius.circular(AppRadius.icon),
      ),
    ),
    child: Center(
      child: AppIcon(icon, size: 17, color: context.palette.onAccent),
    ),
  );
}

/// Tappable grouped row: optional leading tile, title, subtitle, trailing icon.
class _LinkRow extends StatelessWidget {
  const _LinkRow({
    required this.title,
    required this.onTap,
    this.subtitle,
    this.leading,
    this.trailing = AppGlyph.chevronRight,
  });

  final String title;
  final String? subtitle;
  final Widget? leading;
  final AppGlyph trailing;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    return RowButton(
      onTap: onTap,
      child: ConstrainedBox(
        constraints: const BoxConstraints(minHeight: 52),
        child: Padding(
          padding: const EdgeInsets.fromLTRB(16, 10, 14, 10),
          child: Row(
            children: [
              if (leading != null) ...[leading!, const SizedBox(width: 14)],
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: TextStyle(
                        fontSize: 15.5,
                        fontWeight: FontWeight.w600,
                        color: p.title,
                      ),
                    ),
                    if (subtitle != null) ...[
                      const SizedBox(height: 1),
                      Text(
                        subtitle!,
                        style: TextStyle(fontSize: 13, color: p.muted),
                      ),
                    ],
                  ],
                ),
              ),
              const SizedBox(width: 8),
              AppIcon(trailing, size: 20, color: p.faint),
            ],
          ),
        ),
      ),
    );
  }
}

/// System / Light / Dark as a native sliding segmented control.
class _ThemePicker extends StatelessWidget {
  const _ThemePicker({required this.mode, required this.onChanged});
  final ThemeMode mode;
  final ValueChanged<ThemeMode> onChanged;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    Widget segment(ThemeMode m, AppGlyph icon, String label) {
      final selected = m == mode;
      final color = selected ? p.title : p.muted;
      return Padding(
        padding: const EdgeInsets.symmetric(vertical: 10),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            AppIcon(icon, size: 20, color: selected ? p.accent : p.muted),
            const SizedBox(height: 4),
            Text(
              label,
              style: TextStyle(
                fontFamily: AppFonts.sans,
                fontSize: 13.5,
                fontWeight: selected ? FontWeight.w700 : FontWeight.w500,
                color: color,
              ),
            ),
          ],
        ),
      );
    }

    return SizedBox(
      width: double.infinity,
      child: CupertinoSlidingSegmentedControl<ThemeMode>(
        groupValue: mode,
        backgroundColor: p.surface,
        thumbColor: p.accentDeep,
        padding: const EdgeInsets.all(4),
        onValueChanged: (m) {
          if (m != null) onChanged(m);
        },
        children: {
          ThemeMode.system: segment(ThemeMode.system, AppGlyph.auto, 'System'),
          ThemeMode.light: segment(ThemeMode.light, AppGlyph.sun, 'Light'),
          ThemeMode.dark: segment(ThemeMode.dark, AppGlyph.moon, 'Dark'),
        },
      ),
    );
  }
}

class _SwitchRow extends StatelessWidget {
  const _SwitchRow({
    required this.title,
    required this.subtitle,
    required this.value,
    required this.onChanged,
    this.urduSubtitle = false,
  });

  final String title;
  final String subtitle;
  final bool value;
  final ValueChanged<bool> onChanged;
  final bool urduSubtitle;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    return RowButton(
      onTap: () => onChanged(!value),
      child: Padding(
        padding: const EdgeInsets.fromLTRB(16, 8, 14, 8),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: TextStyle(
                      fontSize: 15.5,
                      fontWeight: FontWeight.w600,
                      color: p.title,
                    ),
                  ),
                  Text(
                    subtitle,
                    style: urduSubtitle
                        ? TextStyle(
                            fontFamily: AppFonts.urdu,
                            fontSize: 18,
                            height: 1.5,
                            color: p.muted,
                          )
                        : TextStyle(fontSize: 13, color: p.muted),
                  ),
                ],
              ),
            ),
            CupertinoSwitch(
              value: value,
              activeTrackColor: p.accent,
              onChanged: onChanged,
            ),
          ],
        ),
      ),
    );
  }
}

const _otherApps = [
  (
    name: 'Rafeeq',
    icon: 'assets/other_apps/rafeeq.png',
    packageName: 'com.satechdev.rafeeqapp',
  ),
  (
    name: 'FM Crafts',
    icon: 'assets/other_apps/fm_crafts.png',
    packageName: 'com.fmcrafts.fmcrafts',
  ),
  (
    name: 'Budget AI',
    icon: 'assets/other_apps/budget_ai.png',
    packageName: 'com.budgetai.android',
  ),
  (
    name: 'Quran Khatam Tracker',
    icon: 'assets/other_apps/quran_khatam_tracker.png',
    packageName: 'com.quran.khatam.tracker',
  ),
];
