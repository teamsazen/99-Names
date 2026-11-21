import 'dart:math' as math;
import 'dart:ui' show ImageFilter;

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'icons.dart';
import 'motion.dart';
import 'theme.dart';

/// iOS-style large title: heavy, tightly tracked Google Sans.
class LargeTitle extends StatelessWidget {
  const LargeTitle(this.text, {super.key, this.color});
  final String text;
  final Color? color;

  @override
  Widget build(BuildContext context) => Text(
    text,
    style: TextStyle(
      fontSize: context.r(34),
      height: 1.12,
      fontWeight: FontWeight.w700,
      letterSpacing: -1.2,
      color: color ?? context.palette.title,
    ),
  );
}

class Eyebrow extends StatelessWidget {
  const Eyebrow(this.text, {super.key, this.color});
  final String text;
  final Color? color;

  @override
  Widget build(BuildContext context) => Text(
    text,
    style: TextStyle(
      color: color ?? context.palette.accent,
      fontSize: context.r(12),
      height: 1.3,
      fontWeight: FontWeight.w700,
      letterSpacing: 1.6,
    ),
  );
}

/// Shrinks a touch on press and springs back on release, so every tap is
/// acknowledged the moment the finger lands. Respects reduced motion.
class Pressable extends StatefulWidget {
  const Pressable({
    super.key,
    required this.child,
    required this.onTap,
    this.scale = .97,
    this.semanticLabel,
  });

  final Widget child;
  final VoidCallback? onTap;
  final double scale;
  final String? semanticLabel;

  @override
  State<Pressable> createState() => _PressableState();
}

class _PressableState extends State<Pressable> {
  bool _down = false;

  void _set(bool v) {
    if (_down != v) setState(() => _down = v);
  }

  @override
  Widget build(BuildContext context) {
    final enabled = widget.onTap != null;
    final still = MediaQuery.disableAnimationsOf(context);
    return Semantics(
      button: true,
      enabled: enabled,
      label: widget.semanticLabel,
      child: GestureDetector(
        behavior: HitTestBehavior.opaque,
        onTapDown: enabled ? (_) => _set(true) : null,
        onTapUp: enabled ? (_) => _set(false) : null,
        onTapCancel: enabled ? () => _set(false) : null,
        onTap: widget.onTap,
        child: AnimatedScale(
          scale: _down && !still ? widget.scale : 1,
          duration: _down ? AppMotion.press : AppMotion.release,
          curve: AppMotion.easeOut,
          child: widget.child,
        ),
      ),
    );
  }
}

/// Grouped-list row that tints its background on press, like iOS Settings.
class RowButton extends StatefulWidget {
  const RowButton({super.key, required this.child, required this.onTap});
  final Widget child;
  final VoidCallback? onTap;

  @override
  State<RowButton> createState() => _RowButtonState();
}

class _RowButtonState extends State<RowButton> {
  bool _down = false;

  void _set(bool v) {
    if (_down != v) setState(() => _down = v);
  }

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    final enabled = widget.onTap != null;
    return Semantics(
      button: enabled,
      child: GestureDetector(
        behavior: HitTestBehavior.opaque,
        onTapDown: enabled ? (_) => _set(true) : null,
        onTapUp: enabled ? (_) => _set(false) : null,
        onTapCancel: enabled ? () => _set(false) : null,
        onTap: widget.onTap,
        child: AnimatedContainer(
          // Instant on press, gentle fade on release.
          duration: _down ? Duration.zero : AppMotion.release,
          curve: AppMotion.easeOut,
          color: _down ? p.surfaceRaised : p.surface.withValues(alpha: 0),
          child: widget.child,
        ),
      ),
    );
  }
}

/// Plain rounded surface. Depth comes from the step between background and
/// surface, not from borders or glows.
class Surface extends StatelessWidget {
  const Surface({
    super.key,
    required this.child,
    this.padding = const EdgeInsets.all(16),
    this.radius = AppRadius.card,
    this.color,
    this.onTap,
  });

  final Widget child;
  final EdgeInsetsGeometry padding;
  final double radius;
  final Color? color;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    final box = DecoratedBox(
      decoration: ShapeDecoration(
        color: color ?? p.surface,
        shape: RoundedSuperellipseBorder(
          borderRadius: BorderRadius.circular(radius),
        ),
        shadows: [
          BoxShadow(color: p.shadow, blurRadius: 3, offset: const Offset(0, 1)),
        ],
      ),
      child: ClipRSuperellipse(
        borderRadius: BorderRadius.circular(radius),
        child: Padding(padding: padding, child: child),
      ),
    );
    return onTap == null ? box : Pressable(onTap: onTap, child: box);
  }
}

/// Small caps label that sits above a grouped section.
class SectionLabel extends StatelessWidget {
  const SectionLabel(this.text, {super.key, this.color});
  final String text;
  final Color? color;

  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.fromLTRB(16, 0, 16, 8),
    child: Text(
      text.toUpperCase(),
      style: TextStyle(
        fontSize: 12.5,
        fontWeight: FontWeight.w600,
        letterSpacing: .6,
        color: color ?? context.palette.muted,
      ),
    ),
  );
}

/// iOS-style inset grouped list: a label, then rows on one surface separated
/// by hairlines that start where the row text starts.
class GroupedSection extends StatelessWidget {
  const GroupedSection({
    super.key,
    this.label,
    required this.children,
    this.dividerIndent = 16,
    this.labelColor,
  });

  final String? label;
  final List<Widget> children;
  final double dividerIndent;
  final Color? labelColor;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        if (label != null) SectionLabel(label!, color: labelColor),
        Surface(
          padding: EdgeInsets.zero,
          child: Column(
            children: [
              for (final (i, child) in children.indexed) ...[
                if (i > 0)
                  Divider(
                    height: .5,
                    thickness: .5,
                    indent: dividerIndent,
                    color: p.border,
                  ),
                child,
              ],
            ],
          ),
        ),
      ],
    );
  }
}

/// Section title with a quiet trailing count that rolls when it changes.
class SectionTitle extends StatelessWidget {
  const SectionTitle(this.text, {super.key, this.trailing});
  final String text;
  final String? trailing;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    return Padding(
      padding: const EdgeInsets.fromLTRB(4, 0, 4, 12),
      child: Row(
        children: [
          Expanded(
            child: Swap(
              value: text,
              child: Align(
                alignment: Alignment.centerLeft,
                child: Text(
                  text,
                  style: TextStyle(
                    fontSize: 22,
                    fontWeight: FontWeight.w700,
                    letterSpacing: -.6,
                    color: p.title,
                  ),
                ),
              ),
            ),
          ),
          if (trailing != null)
            Swap(
              value: trailing!,
              child: Text(
                trailing!,
                style: TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w600,
                  fontFeatures: const [FontFeature.tabularFigures()],
                  color: p.muted,
                ),
              ),
            ),
        ],
      ),
    );
  }
}

/// Round icon button. [onHero] styles it for the ink hero.
class CircleButton extends StatelessWidget {
  const CircleButton({
    super.key,
    required this.glyph,
    required this.onTap,
    required this.tooltip,
    this.onHero = false,
    this.iconSize = 20,
  });

  final AppGlyph glyph;
  final VoidCallback onTap;
  final String tooltip;
  final bool onHero;
  final double iconSize;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    return Tooltip(
      message: tooltip,
      child: Pressable(
        onTap: onTap,
        scale: .9,
        semanticLabel: tooltip,
        child: Container(
          width: 42,
          height: 42,
          alignment: Alignment.center,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: onHero ? p.onHero.withValues(alpha: .1) : p.surface,
            boxShadow: [
              if (!onHero)
                BoxShadow(
                  color: p.shadow,
                  blurRadius: 3,
                  offset: const Offset(0, 1),
                ),
            ],
          ),
          child: Swap(
            value: glyph,
            child: AppIcon(
              glyph,
              size: iconSize,
              color: onHero ? p.onHero : p.title,
            ),
          ),
        ),
      ),
    );
  }
}

/// Heart toggle. Adding a favourite pops the heart with a slight overshoot
/// and sends a ring outward; removing it just settles.
class HeartButton extends StatefulWidget {
  const HeartButton({
    super.key,
    required this.favourite,
    required this.onChanged,
    this.onHero = false,
  });

  final bool favourite;
  final VoidCallback onChanged;
  final bool onHero;

  @override
  State<HeartButton> createState() => _HeartButtonState();
}

class _HeartButtonState extends State<HeartButton>
    with SingleTickerProviderStateMixin {
  late final _burst = AnimationController(
    vsync: this,
    duration: const Duration(milliseconds: 520),
  );

  @override
  void dispose() {
    _burst.dispose();
    super.dispose();
  }

  void _tap() {
    if (!widget.favourite) {
      HapticFeedback.lightImpact();
      if (!MediaQuery.disableAnimationsOf(context)) _burst.forward(from: 0);
    } else {
      HapticFeedback.selectionClick();
    }
    widget.onChanged();
  }

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    final on = widget.favourite;
    final idle = widget.onHero ? p.onHero : p.title;
    return Tooltip(
      message: on ? 'Remove favourite' : 'Add favourite',
      child: Pressable(
        onTap: _tap,
        scale: .9,
        semanticLabel: on ? 'Remove favourite' : 'Add favourite',
        child: AnimatedContainer(
          duration: AppMotion.fast,
          curve: AppMotion.easeOut,
          width: 42,
          height: 42,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: on
                ? p.accent
                : widget.onHero
                ? p.onHero.withValues(alpha: .1)
                : p.surface,
          ),
          child: AnimatedBuilder(
            animation: _burst,
            builder: (context, _) {
              final t = _burst.value;
              final e = Curves.easeOut.transform(t);
              // Pop: 1 → 1.28 → 1, with the ring travelling outward.
              final pop = 1 + .28 * math.sin(math.pi * e);
              return Stack(
                alignment: Alignment.center,
                clipBehavior: Clip.none,
                children: [
                  if (t > 0 && t < 1)
                    OverflowBox(
                      maxWidth: 80,
                      maxHeight: 80,
                      child: Container(
                        width: 42 + 16 * e,
                        height: 42 + 16 * e,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          border: Border.all(
                            color: p.accent.withValues(alpha: 1 - t),
                            width: 2.5 * (1 - t) + .5,
                          ),
                        ),
                      ),
                    ),
                  Transform.scale(
                    scale: pop,
                    child: AppIcon(
                      on ? AppGlyph.heartFilled : AppGlyph.heart,
                      size: 20,
                      color: on ? p.onAccent : idle,
                    ),
                  ),
                ],
              );
            },
          ),
        ),
      ),
    );
  }
}

/// Pill-shaped action. [filled] is the screen's primary action in the
/// accent; otherwise a quiet surface pill.
class PillButton extends StatelessWidget {
  const PillButton({
    super.key,
    required this.label,
    required this.onTap,
    this.glyph,
    this.filled = false,
    this.trailingIcon = false,
    this.color,
    this.foreground,
    this.height = 50,
  });

  final String label;
  final VoidCallback? onTap;
  final AppGlyph? glyph;
  final bool filled;
  final bool trailingIcon;
  final Color? color;
  final Color? foreground;
  final double height;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    final fg = foreground ?? (filled ? p.onAccent : p.title);
    final i = glyph == null
        ? null
        : AppIcon(glyph!, size: 18, color: filled ? fg : p.accent);
    return Pressable(
      onTap: onTap,
      child: AnimatedOpacity(
        duration: AppMotion.fast,
        opacity: onTap == null ? .35 : 1,
        child: Container(
          height: height,
          padding: const EdgeInsets.symmetric(horizontal: 18),
          decoration: ShapeDecoration(
            color: color ?? (filled ? p.accent : p.surface),
            shape: const StadiumBorder(),
            shadows: [
              if (!filled && color == null)
                BoxShadow(
                  color: p.shadow,
                  blurRadius: 3,
                  offset: const Offset(0, 1),
                ),
            ],
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              if (i != null && !trailingIcon) ...[i, const SizedBox(width: 8)],
              Flexible(
                child: Swap(
                  value: label,
                  child: Text(
                    label,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w700,
                      color: fg,
                    ),
                  ),
                ),
              ),
              if (i != null && trailingIcon) ...[const SizedBox(width: 8), i],
            ],
          ),
        ),
      ),
    );
  }
}

/// Floating header made of translucent material. Content scrolls beneath it;
/// once [raised], the blur, hairline and title fade in. With
/// [hideUntilRaised] the whole bar waits, for screens whose hero carries its
/// own controls.
class GlassBar extends StatelessWidget {
  const GlassBar({
    super.key,
    required this.raised,
    this.leading,
    this.title,
    this.trailing = const [],
    this.hideUntilRaised = false,
  });

  final bool raised;
  final Widget? leading;
  final String? title;
  final List<Widget> trailing;
  final bool hideUntilRaised;

  static const height = 58.0;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    final top = MediaQuery.paddingOf(context).top;
    return IgnorePointer(
      ignoring: hideUntilRaised && !raised,
      child: TweenAnimationBuilder<double>(
        tween: Tween(end: raised ? 1 : 0),
        duration: AppMotion.fast,
        curve: AppMotion.easeOut,
        builder: (context, t, _) => Opacity(
          opacity: hideUntilRaised ? t : 1,
          child: ClipRect(
            child: BackdropFilter(
              filter: ImageFilter.blur(sigmaX: 24 * t, sigmaY: 24 * t),
              child: Container(
                height: top + height,
                padding: EdgeInsets.fromLTRB(16, top, 16, 0),
                decoration: BoxDecoration(
                  color: p.background.withValues(alpha: .8 * t),
                  border: Border(
                    bottom: BorderSide(
                      color: p.border.withValues(alpha: t),
                      width: .5,
                    ),
                  ),
                ),
                child: Row(
                  children: [
                    ?leading,
                    Expanded(
                      child: Opacity(
                        opacity: t,
                        child: Text(
                          title ?? '',
                          textAlign: TextAlign.center,
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
                    ),
                    for (final (i, w) in trailing.indexed) ...[
                      if (i > 0) const SizedBox(width: 8),
                      w,
                    ],
                    if (leading != null && trailing.isEmpty)
                      const SizedBox(width: 42),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

/// A name's number in a thin ring, set in tabular figures.
class NumberRing extends StatelessWidget {
  const NumberRing(this.number, {super.key, this.size = 34, this.color});
  final int number;
  final double size;
  final Color? color;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    final c = color ?? p.accent;
    return Container(
      width: size,
      height: size,
      alignment: Alignment.center,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        border: Border.all(color: c.withValues(alpha: .45), width: 1),
      ),
      child: Text(
        '$number',
        style: TextStyle(
          fontSize: size * (number > 9 ? .34 : .38),
          fontWeight: FontWeight.w700,
          fontFeatures: const [FontFeature.tabularFigures()],
          color: c,
        ),
      ),
    );
  }
}

/// Geometric rosette drawn in fine lines: a circle, two eight-pointed stars
/// and an inner ring. The art behind each name.
class Rosette extends StatelessWidget {
  const Rosette({super.key, required this.size, required this.color});
  final double size;
  final Color color;

  @override
  Widget build(BuildContext context) => SizedBox.square(
    dimension: size,
    child: CustomPaint(painter: _RosettePainter(color)),
  );
}

class _RosettePainter extends CustomPainter {
  const _RosettePainter(this.color);
  final Color color;

  @override
  void paint(Canvas canvas, Size size) {
    final c = size.center(Offset.zero);
    final r = size.width / 2;
    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.1;
    canvas.drawCircle(c, r * .98, paint);
    canvas.drawPath(_star(c, r * .92, 0), paint);
    canvas.drawPath(_star(c, r * .7, math.pi / 8), paint);
    canvas.drawCircle(c, r * .42, paint);
    for (var i = 0; i < 16; i++) {
      final a = i * math.pi / 8;
      final d = Offset(math.cos(a), math.sin(a));
      canvas.drawLine(c + d * r * .42, c + d * r * .52, paint);
    }
  }

  /// Eight-pointed star: two squares rotated 45° apart.
  static Path _star(Offset c, double radius, double rotation) {
    Path square(double angle) {
      final p = Path();
      for (var i = 0; i < 4; i++) {
        final a = rotation + angle + i * math.pi / 2;
        final pt = c + Offset(math.cos(a), math.sin(a)) * radius;
        i == 0 ? p.moveTo(pt.dx, pt.dy) : p.lineTo(pt.dx, pt.dy);
      }
      return p..close();
    }

    return Path.combine(PathOperation.union, square(0), square(math.pi / 4));
  }

  @override
  bool shouldRepaint(covariant _RosettePainter old) => old.color != color;
}

/// Turns and grows the rosette into place when a name first appears.
class RosetteIn extends StatelessWidget {
  const RosetteIn({super.key, required this.size, required this.color});
  final double size;
  final Color color;

  @override
  Widget build(BuildContext context) {
    if (MediaQuery.disableAnimationsOf(context)) {
      return Rosette(size: size, color: color);
    }
    return TweenAnimationBuilder<double>(
      tween: Tween(begin: 0, end: 1),
      duration: const Duration(milliseconds: 900),
      curve: AppMotion.easeOut,
      builder: (context, t, child) => Opacity(
        opacity: t,
        child: Transform.rotate(
          angle: -math.pi / 8 * (1 - t),
          child: Transform.scale(scale: .9 + .1 * t, child: child),
        ),
      ),
      child: Rosette(size: size, color: color),
    );
  }
}
