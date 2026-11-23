import 'dart:math' as math;

import 'package:flutter/widgets.dart';

/// The app's own line icons, drawn on a 24×24 grid with one stroke weight
/// and round caps, so every glyph shares a single visual family.
enum AppGlyph {
  back,
  forward,
  chevronRight,
  grid,
  list,
  sliders,
  search,
  close,
  heart,
  heartFilled,
  copy,
  share,
  star,
  link,
  external,
  sun,
  moon,
  auto,
  arrowRight,
  shield,
  document,
}

class AppIcon extends StatelessWidget {
  const AppIcon(
    this.glyph, {
    super.key,
    this.size = 22,
    required this.color,
    this.semanticLabel,
  });

  final AppGlyph glyph;
  final double size;
  final Color color;
  final String? semanticLabel;

  @override
  Widget build(BuildContext context) => Semantics(
    label: semanticLabel,
    excludeSemantics: semanticLabel == null,
    child: SizedBox.square(
      dimension: size,
      child: CustomPaint(painter: _GlyphPainter(glyph, color)),
    ),
  );
}

class _GlyphPainter extends CustomPainter {
  const _GlyphPainter(this.glyph, this.color);
  final AppGlyph glyph;
  final Color color;

  @override
  void paint(Canvas canvas, Size size) {
    canvas.scale(size.width / 24);
    final line = Paint()
      ..color = color
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.9
      ..strokeCap = StrokeCap.round
      ..strokeJoin = StrokeJoin.round;
    final fill = Paint()..color = color;

    void poly(List<Offset> pts, {bool close = false}) {
      final p = Path()..moveTo(pts.first.dx, pts.first.dy);
      for (final o in pts.skip(1)) {
        p.lineTo(o.dx, o.dy);
      }
      if (close) p.close();
      canvas.drawPath(p, line);
    }

    RRect rr(double l, double t, double r, double b, double rad) =>
        RRect.fromLTRBR(l, t, r, b, Radius.circular(rad));

    switch (glyph) {
      case AppGlyph.back:
        poly(const [Offset(15, 5), Offset(8, 12), Offset(15, 19)]);
      case AppGlyph.forward:
        poly(const [Offset(9, 5), Offset(16, 12), Offset(9, 19)]);
      case AppGlyph.chevronRight:
        poly(const [Offset(10, 7), Offset(15, 12), Offset(10, 17)]);
      case AppGlyph.shield:
        final shield = Path()
          ..moveTo(12, 3.5)
          ..lineTo(19, 6.2)
          ..lineTo(19, 11.5)
          ..cubicTo(19, 15.8, 16.1, 19, 12, 20.5)
          ..cubicTo(7.9, 19, 5, 15.8, 5, 11.5)
          ..lineTo(5, 6.2)
          ..close();
        canvas.drawPath(shield, line);
        poly(const [Offset(9, 12), Offset(11.2, 14.2), Offset(15.2, 10)]);
      case AppGlyph.document:
        poly(const [
          Offset(14, 3.5),
          Offset(7, 3.5),
          Offset(5.5, 5),
          Offset(5.5, 19),
          Offset(7, 20.5),
          Offset(17, 20.5),
          Offset(18.5, 19),
          Offset(18.5, 8),
          Offset(14, 3.5),
        ], close: true);
        poly(const [Offset(14, 3.5), Offset(14, 8), Offset(18.5, 8)]);
        poly(const [Offset(9, 12.5), Offset(15, 12.5)]);
        poly(const [Offset(9, 16), Offset(13, 16)]);
      case AppGlyph.arrowRight:
        poly(const [Offset(5, 12), Offset(19, 12)]);
        poly(const [Offset(13, 6), Offset(19, 12), Offset(13, 18)]);
      case AppGlyph.grid:
        for (final (x, y) in const [(4.0, 4.0), (13.5, 4.0), (4.0, 13.5)]) {
          canvas.drawRRect(rr(x, y, x + 6.5, y + 6.5, 2), line);
        }
        canvas.drawRRect(rr(13.5, 13.5, 20, 20, 2), line);
      case AppGlyph.list:
        canvas.drawRRect(rr(4, 4.5, 20, 10.5, 2.2), line);
        canvas.drawRRect(rr(4, 13.5, 20, 19.5, 2.2), line);
      case AppGlyph.sliders:
        poly(const [Offset(4, 8), Offset(20, 8)]);
        poly(const [Offset(4, 16), Offset(20, 16)]);
        canvas.drawCircle(const Offset(9, 8), 2.6, Paint()..color = color);
        canvas.drawCircle(const Offset(15, 16), 2.6, Paint()..color = color);
      case AppGlyph.search:
        canvas.drawCircle(const Offset(10.5, 10.5), 6, line);
        poly(const [Offset(15, 15), Offset(20, 20)]);
      case AppGlyph.close:
        poly(const [Offset(6.5, 6.5), Offset(17.5, 17.5)]);
        poly(const [Offset(17.5, 6.5), Offset(6.5, 17.5)]);
      case AppGlyph.heart:
        canvas.drawPath(_heart, line);
      case AppGlyph.heartFilled:
        canvas.drawPath(_heart, fill);
        canvas.drawPath(_heart, line);
      case AppGlyph.copy:
        // Front sheet, and the back sheet peeking out above and to the left.
        canvas.drawRRect(rr(9, 9, 20, 20, 3), line);
        canvas.save();
        canvas.clipPath(
          Path.combine(
            PathOperation.difference,
            Path()..addRect(const Rect.fromLTWH(0, 0, 24, 24)),
            Path()..addRRect(rr(7, 7, 22, 22, 4)),
          ),
        );
        canvas.drawRRect(rr(4, 4, 15, 15, 3), line);
        canvas.restore();
      case AppGlyph.share:
        poly(const [Offset(12, 3.5), Offset(12, 14.5)]);
        poly(const [Offset(8, 7.5), Offset(12, 3.5), Offset(16, 7.5)]);
        poly(const [
          Offset(8, 11),
          Offset(5.5, 11),
          Offset(5.5, 20.5),
          Offset(18.5, 20.5),
          Offset(18.5, 11),
          Offset(16, 11),
        ]);
      case AppGlyph.star:
        final p = Path();
        for (var i = 0; i < 10; i++) {
          final r = i.isEven ? 8.8 : 3.9;
          final a = -math.pi / 2 + i * math.pi / 5;
          final o = Offset(12 + r * math.cos(a), 12.6 + r * math.sin(a));
          i == 0 ? p.moveTo(o.dx, o.dy) : p.lineTo(o.dx, o.dy);
        }
        canvas.drawPath(p..close(), fill);
        canvas.drawPath(p, line..strokeWidth = 1.2);
      case AppGlyph.link:
        canvas.save();
        canvas.translate(12, 12);
        canvas.rotate(-math.pi / 4);
        canvas.drawRRect(rr(-9.5, -3.2, 0.5, 3.2, 3.2), line);
        canvas.drawRRect(rr(-0.5, -3.2, 9.5, 3.2, 3.2), line);
        canvas.restore();
      case AppGlyph.external:
        poly(const [
          Offset(11, 5),
          Offset(7, 5),
          Offset(5, 7),
          Offset(5, 17),
          Offset(7, 19),
          Offset(17, 19),
          Offset(19, 17),
          Offset(19, 13),
        ]);
        poly(const [Offset(13, 5), Offset(19, 5), Offset(19, 11)]);
        poly(const [Offset(19, 5), Offset(11.5, 12.5)]);
      case AppGlyph.sun:
        canvas.drawCircle(const Offset(12, 12), 4, line);
        for (var i = 0; i < 8; i++) {
          final a = i * math.pi / 4;
          final d = Offset(math.cos(a), math.sin(a));
          poly([const Offset(12, 12) + d * 7, const Offset(12, 12) + d * 9]);
        }
      case AppGlyph.moon:
        final moon = Path.combine(
          PathOperation.difference,
          Path()
            ..addOval(Rect.fromCircle(center: const Offset(12, 12), radius: 8)),
          Path()
            ..addOval(Rect.fromCircle(center: const Offset(17, 8), radius: 7)),
        );
        canvas.drawPath(moon, fill);
      case AppGlyph.auto:
        canvas.drawCircle(const Offset(12, 12), 8, line);
        canvas.drawPath(
          Path()
            ..moveTo(12, 4)
            ..arcToPoint(const Offset(12, 20), radius: const Radius.circular(8))
            ..close(),
          fill,
        );
    }
  }

  static final _heart = Path()
    ..moveTo(12, 20)
    ..cubicTo(5.2, 15.6, 3, 12.2, 3, 8.9)
    ..cubicTo(3, 6.1, 5.1, 4, 7.7, 4)
    ..cubicTo(9.5, 4, 11, 5, 12, 6.6)
    ..cubicTo(13, 5, 14.5, 4, 16.3, 4)
    ..cubicTo(18.9, 4, 21, 6.1, 21, 8.9)
    ..cubicTo(21, 12.2, 18.8, 15.6, 12, 20)
    ..close();

  @override
  bool shouldRepaint(covariant _GlyphPainter old) =>
      old.glyph != glyph || old.color != color;
}
