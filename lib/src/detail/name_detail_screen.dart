import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../analytics/analytics.dart';
import '../data/names.dart';
import '../settings/settings_store.dart';
import '../ui/icons.dart';
import '../ui/motion.dart';
import '../ui/theme.dart';
import '../ui/widgets.dart';

/// One name per page; swipe between the names shown on the home screen.
class NameDetailScreen extends StatefulWidget {
  const NameDetailScreen({
    super.key,
    required this.names,
    required this.initialIndex,
    this.heroPrefix = 'all',
  });

  final List<DivineName> names;
  final int initialIndex;

  /// Which home surface the name was opened from, so its Arabic flies back
  /// to the same place.
  final String heroPrefix;

  @override
  State<NameDetailScreen> createState() => _NameDetailScreenState();
}

class _NameDetailScreenState extends State<NameDetailScreen> {
  late final _pages = PageController(initialPage: widget.initialIndex);
  late int _index = widget.initialIndex;

  DivineName get _name => widget.names[_index];

  @override
  void dispose() {
    _pages.dispose();
    super.dispose();
  }

  void _go(int delta) => _pages.animateToPage(
    _index + delta,
    duration: AppMotion.page,
    curve: AppMotion.drawer,
  );

  void _copy() {
    final n = _name;
    HapticFeedback.selectionClick();
    Clipboard.setData(
      ClipboardData(
        text:
            '${n.number}. ${n.arabic}\n'
            '${n.transliteration} — ${n.english}\n'
            '${n.urdu}\n\n'
            '${n.explanation}',
      ),
    );
    showToast(context, 'Copied ${n.transliteration}');
  }

  @override
  Widget build(BuildContext context) {
    final s = SettingsStore.instance;
    final p = context.palette;
    final last = widget.names.length - 1;
    // The ink hero sits under the status bar in both themes.
    return AnnotatedRegion<SystemUiOverlayStyle>(
      value: SystemUiOverlayStyle.light.copyWith(
        statusBarColor: Colors.transparent,
        systemNavigationBarColor: p.background,
      ),
      child: ListenableBuilder(
        listenable: s,
        builder: (context, _) => Scaffold(
          body: Column(
            children: [
              ColoredBox(
                color: p.hero,
                child: SafeArea(
                  bottom: false,
                  child: Padding(
                    padding: const EdgeInsets.fromLTRB(16, 8, 16, 10),
                    child: Row(
                      children: [
                        CircleButton(
                          glyph: AppGlyph.back,
                          tooltip: 'Back',
                          onHero: true,
                          onTap: () => Navigator.of(context).pop(),
                        ),
                        Expanded(child: _Progress(number: _name.number)),
                        CircleButton(
                          glyph: AppGlyph.copy,
                          tooltip: 'Copy',
                          iconSize: 19,
                          onHero: true,
                          onTap: _copy,
                        ),
                        const SizedBox(width: 8),
                        HeartButton(
                          favourite: s.isFavourite(_name.number),
                          onHero: true,
                          onChanged: () {
                            Analytics.favourite(
                              _name.number,
                              added: !s.isFavourite(_name.number),
                            );
                            s.toggleFavourite(_name.number);
                          },
                        ),
                      ],
                    ),
                  ),
                ),
              ),
              Expanded(
                child: PageView.builder(
                  controller: _pages,
                  itemCount: widget.names.length,
                  onPageChanged: (i) {
                    HapticFeedback.selectionClick();
                    setState(() => _index = i);
                  },
                  itemBuilder: (_, i) => _NamePage(
                    name: widget.names[i],
                    heroTag: '${widget.heroPrefix}-${widget.names[i].number}',
                  ),
                ),
              ),
              SafeArea(
                top: false,
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(16, 8, 16, 10),
                  child: Row(
                    children: [
                      Expanded(
                        child: PillButton(
                          glyph: AppGlyph.back,
                          label: _index > 0
                              ? widget.names[_index - 1].transliteration
                              : 'Previous',
                          onTap: _index > 0 ? () => _go(-1) : null,
                        ),
                      ),
                      const SizedBox(width: 10),
                      Expanded(
                        child: PillButton(
                          glyph: AppGlyph.forward,
                          trailingIcon: true,
                          filled: true,
                          label: _index < last
                              ? widget.names[_index + 1].transliteration
                              : 'Next',
                          onTap: _index < last ? () => _go(1) : null,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

/// "12 of 99" with a thin gold bar showing how far through the names you
/// are; the number rolls as you swipe.
class _Progress extends StatelessWidget {
  const _Progress({required this.number});
  final int number;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Swap(
          value: number,
          child: Text(
            '$number of 99',
            style: TextStyle(
              fontSize: 13,
              fontWeight: FontWeight.w600,
              fontFeatures: const [FontFeature.tabularFigures()],
              color: p.heroMuted,
            ),
          ),
        ),
        const SizedBox(height: 6),
        Container(
          width: 64,
          height: 3,
          decoration: ShapeDecoration(
            color: p.onHero.withValues(alpha: .14),
            shape: const StadiumBorder(),
          ),
          alignment: Alignment.centerLeft,
          child: TweenAnimationBuilder<double>(
            tween: Tween(end: number / 99),
            duration: AppMotion.page,
            curve: AppMotion.easeOut,
            builder: (_, v, _) => FractionallySizedBox(
              widthFactor: v,
              child: DecoratedBox(
                decoration: ShapeDecoration(
                  color: p.logoInk,
                  shape: const StadiumBorder(),
                ),
                child: const SizedBox.expand(),
              ),
            ),
          ),
        ),
      ],
    );
  }
}

class _NamePage extends StatelessWidget {
  const _NamePage({required this.name, required this.heroTag});
  final DivineName name;
  final String heroTag;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    final s = SettingsStore.instance;
    final side = context.r(16);
    const shoulders = BorderRadius.vertical(bottom: Radius.circular(36));
    return ListView(
      padding: EdgeInsets.only(bottom: context.r(20)),
      children: [
        // Ink hero continuing seamlessly from the header.
        DecoratedBox(
          decoration: ShapeDecoration(
            color: p.hero,
            shape: const RoundedSuperellipseBorder(borderRadius: shoulders),
          ),
          child: Padding(
            padding: EdgeInsets.fromLTRB(24, 0, 24, context.r(30)),
            child: Column(
              children: [
                SizedBox(
                  width: double.infinity,
                  height: context.r(260),
                  child: Stack(
                    alignment: Alignment.center,
                    children: [
                      RosetteIn(
                        size: context.r(240),
                        color: p.logoInk.withValues(alpha: .34),
                      ),
                      Hero(
                        tag: heroTag,
                        flightShuttleBuilder: arabicFlight,
                        child: FittedBox(
                          fit: BoxFit.scaleDown,
                          child: Text(
                            name.arabic,
                            maxLines: 1,
                            textDirection: TextDirection.rtl,
                            style: TextStyle(
                              fontFamily: AppFonts.arabic,
                              fontSize: context.r(80) * s.arabicScale,
                              height: 1.5,
                              color: p.logoInk,
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
                Reveal(
                  delay: const Duration(milliseconds: 120),
                  child: Column(
                    children: [
                      if (s.showTransliteration)
                        Text(
                          name.transliteration,
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            fontSize: context.r(30),
                            height: 1.15,
                            fontWeight: FontWeight.w700,
                            letterSpacing: -.9,
                            color: p.onHero,
                          ),
                        ),
                      if (s.showEnglish) ...[
                        const SizedBox(height: 6),
                        Text(
                          name.english,
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            fontSize: context.r(17),
                            fontWeight: FontWeight.w500,
                            color: p.heroMuted,
                          ),
                        ),
                      ],
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
        SizedBox(height: context.r(24)),
        Padding(
          padding: EdgeInsets.symmetric(horizontal: side),
          child: Reveal(
            delay: const Duration(milliseconds: 220),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                if (s.showUrdu) ...[
                  GroupedSection(
                    label: 'Urdu meaning',
                    children: [
                      Padding(
                        padding: const EdgeInsets.fromLTRB(20, 10, 20, 12),
                        child: Text(
                          name.urdu,
                          textAlign: TextAlign.center,
                          textDirection: TextDirection.rtl,
                          style: TextStyle(
                            fontFamily: AppFonts.urdu,
                            fontSize: context.r(30),
                            height: 1.7,
                            color: p.title,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),
                ],
                GroupedSection(
                  label: 'Reflection',
                  labelColor: p.gold,
                  children: [
                    Padding(
                      padding: const EdgeInsets.fromLTRB(20, 18, 20, 20),
                      child: Text(
                        name.explanation,
                        style: TextStyle(
                          fontSize: 17,
                          height: 1.6,
                          color: p.body,
                        ),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}
