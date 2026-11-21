import 'package:flutter/material.dart';

import '../data/names.dart';
import '../settings/settings_store.dart';
import '../ui/icons.dart';
import '../ui/motion.dart';
import '../ui/theme.dart';
import '../ui/widgets.dart';

/// The Arabic of a name, ready to fly into the detail screen.
class _ArabicHero extends StatelessWidget {
  const _ArabicHero({
    required this.name,
    required this.size,
    required this.color,
  });
  final DivineName name;
  final double size;
  final Color color;

  @override
  Widget build(BuildContext context) => Hero(
    tag: 'all-${name.number}',
    flightShuttleBuilder: arabicFlight,
    child: FittedBox(
      fit: BoxFit.scaleDown,
      child: Text(
        name.arabic,
        maxLines: 1,
        textDirection: TextDirection.rtl,
        style: TextStyle(
          fontFamily: AppFonts.arabic,
          fontSize: size,
          height: 1.5,
          color: color,
        ),
      ),
    ),
  );
}

/// Compact card for the two-column grid: number, Arabic, transliteration and
/// one line of meaning.
class NameGridCard extends StatelessWidget {
  const NameGridCard({super.key, required this.name, required this.onTap});
  final DivineName name;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    final s = SettingsStore.instance;
    final favourite = s.isFavourite(name.number);
    return Surface(
      onTap: onTap,
      padding: const EdgeInsets.fromLTRB(12, 12, 12, 14),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Row(
            children: [
              NumberRing(name.number, size: 30),
              const Spacer(),
              AnimatedOpacity(
                duration: AppMotion.fast,
                opacity: favourite ? 1 : 0,
                child: AppIcon(AppGlyph.heartFilled, size: 16, color: p.accent),
              ),
            ],
          ),
          Expanded(
            child: Center(
              child: _ArabicHero(
                name: name,
                size: context.r(42) * s.arabicScale,
                color: p.title,
              ),
            ),
          ),
          if (s.showTransliteration)
            Text(
              name.transliteration,
              textAlign: TextAlign.center,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: TextStyle(
                fontSize: context.r(15.5),
                fontWeight: FontWeight.w700,
                letterSpacing: -.3,
                color: p.title,
              ),
            ),
          if (s.showEnglish) ...[
            const SizedBox(height: 2),
            Text(
              name.english,
              textAlign: TextAlign.center,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: TextStyle(fontSize: context.r(12.5), color: p.muted),
            ),
          ] else if (s.showUrdu)
            Text(
              name.urdu,
              textAlign: TextAlign.center,
              textDirection: TextDirection.rtl,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: TextStyle(
                fontFamily: AppFonts.urdu,
                fontSize: context.r(18),
                height: 1.5,
                color: p.muted,
              ),
            ),
        ],
      ),
    );
  }
}

/// One row of the inset grouped list: number and meanings on the left,
/// Arabic on the right. [first] and [last] round the group's corners.
class NameListRow extends StatelessWidget {
  const NameListRow({
    super.key,
    required this.name,
    required this.onTap,
    this.first = false,
    this.last = false,
  });

  final DivineName name;
  final VoidCallback onTap;
  final bool first;
  final bool last;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    final s = SettingsStore.instance;
    final favourite = s.isFavourite(name.number);
    const r = Radius.circular(AppRadius.card);
    return ClipRSuperellipse(
      borderRadius: BorderRadius.vertical(
        top: first ? r : Radius.zero,
        bottom: last ? r : Radius.zero,
      ),
      child: ColoredBox(
        color: p.surface,
        child: RowButton(
          onTap: onTap,
          child: Column(
            children: [
              if (!first)
                Divider(height: .5, thickness: .5, indent: 64, color: p.border),
              Padding(
                padding: const EdgeInsets.fromLTRB(16, 10, 16, 10),
                child: Row(
                  children: [
                    NumberRing(name.number, size: 34),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          if (s.showTransliteration)
                            Row(
                              children: [
                                Flexible(
                                  child: Text(
                                    name.transliteration,
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis,
                                    style: TextStyle(
                                      fontSize: 16,
                                      fontWeight: FontWeight.w700,
                                      letterSpacing: -.2,
                                      color: p.title,
                                    ),
                                  ),
                                ),
                                if (favourite) ...[
                                  const SizedBox(width: 6),
                                  AppIcon(
                                    AppGlyph.heartFilled,
                                    size: 13,
                                    color: p.accent,
                                  ),
                                ],
                              ],
                            ),
                          if (s.showEnglish)
                            Padding(
                              padding: const EdgeInsets.only(top: 1),
                              child: Text(
                                name.english,
                                style: TextStyle(
                                  fontSize: 13.5,
                                  color: p.muted,
                                ),
                              ),
                            ),
                          if (s.showUrdu)
                            Text(
                              name.urdu,
                              textDirection: TextDirection.rtl,
                              style: TextStyle(
                                fontFamily: AppFonts.urdu,
                                fontSize: 19,
                                height: 1.5,
                                color: p.muted,
                              ),
                            ),
                        ],
                      ),
                    ),
                    const SizedBox(width: 10),
                    ConstrainedBox(
                      constraints: BoxConstraints(maxWidth: context.r(130)),
                      child: _ArabicHero(
                        name: name,
                        size: 34 * s.arabicScale,
                        color: p.title,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
