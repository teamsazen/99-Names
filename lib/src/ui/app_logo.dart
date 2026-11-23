import 'dart:ui' as ui;

import 'package:flutter/material.dart';

import 'theme.dart';
import 'wordmark_path.dart';

/// Layers shared by the in-app logo and native launcher icon exports.
enum LogoLayer { full, background, foreground, monochrome }

/// Approved stacked Urdu wordmark: اسماء الحسنیٰ, in the "Glow" frame.
/// Outlined Nastaleeq keeps the calligraphy identical at every size.
class AppLogo extends StatelessWidget {
  const AppLogo({
    super.key,
    this.size = 64,
    this.layer = LogoLayer.full,
    this.radius,
    this.wordmarkScale = 1,
  });

  final double size;
  final LogoLayer layer;
  final double? radius;

  /// Adaptive launcher layers use a smaller mark to stay inside crop masks.
  final double wordmarkScale;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    Widget logo = Semantics(
      label: '99 Names — Asmaul Husna',
      image: true,
      child: SizedBox.square(
        dimension: size,
        child: CustomPaint(
          painter: _WordmarkPainter(
            background: p.logoBackground,
            ink: layer == LogoLayer.monochrome ? p.surface : p.logoInk,
            layer: layer,
            scale: wordmarkScale,
          ),
        ),
      ),
    );
    if (radius != null) {
      logo = ClipRRect(
        borderRadius: BorderRadius.circular(radius!),
        child: logo,
      );
    }
    return logo;
  }
}

class _WordmarkPainter extends CustomPainter {
  _WordmarkPainter({
    required this.background,
    required this.ink,
    required this.layer,
    required this.scale,
  });

  final Color background;
  final Color ink;
  final LogoLayer layer;
  final double scale;
  static final Path _wordmark = createWordmarkPath();

  /// "Glow" design: ink that brightens toward the middle, a fine gold ring and
  /// the wordmark in a soft gold gradient. Every tone comes from the palette's
  /// two logo colours, only lighter or darker.
  static Color _shade(Color c, double dl) {
    final hsl = HSLColor.fromColor(c);
    return hsl.withLightness((hsl.lightness + dl).clamp(0, 1)).toColor();
  }

  /// The ring and wordmark sit inside this share of the 256-unit canvas.
  static const _mark = .8;

  @override
  void paint(Canvas canvas, Size size) {
    if (layer == LogoLayer.full || layer == LogoLayer.background) {
      canvas.drawRect(
        Offset.zero & size,
        Paint()
          ..shader = ui.Gradient.radial(
            Offset(size.width / 2, size.height * .44),
            size.width * .74,
            [_shade(background, .08), background, _shade(background, -.035)],
            [0, .62, 1],
          ),
      );
    }
    if (layer == LogoLayer.background) return;
    final mono = layer == LogoLayer.monochrome;
    canvas.save();
    canvas.scale(size.width / 256, size.height / 256);
    canvas.translate(128, 128);
    canvas.scale(scale);
    // Fine double ring.
    final ring = Paint()..style = PaintingStyle.stroke;
    canvas.drawCircle(
      Offset.zero,
      100,
      ring
        ..color = ink.withValues(alpha: mono ? 1 : .5)
        ..strokeWidth = 2.2,
    );
    if (!mono) {
      canvas.drawCircle(
        Offset.zero,
        93,
        ring
          ..color = ink.withValues(alpha: .18)
          ..strokeWidth = 1,
      );
    }
    canvas.scale(_mark);
    canvas.translate(-128, -128);
    canvas.drawPath(
      _wordmark,
      Paint()
        ..color = ink
        ..shader = mono
            ? null
            : ui.Gradient.linear(
                const Offset(0, 60),
                const Offset(0, 200),
                [_shade(ink, .12), ink, _shade(ink, -.12)],
                [0, .5, 1],
              ),
    );
    canvas.restore();
  }

  @override
  bool shouldRepaint(_WordmarkPainter old) =>
      old.background != background ||
      old.ink != ink ||
      old.layer != layer ||
      old.scale != scale;
}
