import 'dart:math' as math;
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';

import '../settings/settings_store.dart';
import 'theme.dart';

/// Fades and lifts its child into place once, after [delay]. Used for the
/// first appearance of a screen's main blocks, staggered a few frames apart.
/// With Reduce Motion it appears at once.
class Reveal extends StatefulWidget {
  const Reveal({
    super.key,
    required this.child,
    this.delay = Duration.zero,
    this.offset = 18,
    this.duration = const Duration(milliseconds: 520),
  });

  final Widget child;
  final Duration delay;

  /// How far below its place the child starts, in logical pixels.
  final double offset;
  final Duration duration;

  @override
  State<Reveal> createState() => _RevealState();
}

class _RevealState extends State<Reveal> with SingleTickerProviderStateMixin {
  late final _c = AnimationController(
    vsync: this,
    duration: widget.delay + widget.duration,
  );
  late final Animation<double> _t = CurvedAnimation(
    parent: _c,
    // The delay lives inside the controller, so no timers are left pending.
    curve: Interval(
      widget.delay.inMicroseconds /
          (widget.delay + widget.duration).inMicroseconds,
      1,
      curve: AppMotion.easeOut,
    ),
  );

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_c.isDismissed) {
      MediaQuery.disableAnimationsOf(context) ? _c.value = 1 : _c.forward();
    }
  }

  @override
  void dispose() {
    _c.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => AnimatedBuilder(
    animation: _t,
    builder: (context, child) => Opacity(
      opacity: _t.value,
      child: Transform.translate(
        offset: Offset(0, widget.offset * (1 - _t.value)),
        child: child,
      ),
    ),
    child: widget.child,
  );
}

/// Swaps its child with a short vertical slide and fade whenever [value]
/// changes: counters, toggled icons, labels.
class Swap extends StatelessWidget {
  const Swap({
    super.key,
    required this.value,
    required this.child,
    this.up = true,
  });

  final Object value;
  final Widget child;
  final bool up;

  @override
  Widget build(BuildContext context) => AnimatedSwitcher(
    duration: AppMotion.fast,
    switchInCurve: AppMotion.easeOut,
    switchOutCurve: AppMotion.easeOut,
    layoutBuilder: (current, previous) =>
        Stack(alignment: Alignment.center, children: [...previous, ?current]),
    transitionBuilder: (c, a) {
      final entering = c.key == ValueKey(value);
      final from = Offset(0, (up ? .45 : -.45) * (entering ? 1 : -1));
      return FadeTransition(
        opacity: a,
        child: SlideTransition(
          position: Tween(begin: from, end: Offset.zero).animate(a),
          child: c,
        ),
      );
    },
    child: KeyedSubtree(key: ValueKey(value), child: child),
  );
}

/// Animates a light/dark switch: a snapshot of the old theme sits on top
/// while a circle of the new theme grows out from where the user last
/// tapped. With Reduce Motion it is a plain cross-fade.
class ThemeReveal extends StatefulWidget {
  const ThemeReveal({super.key, required this.child});
  final Widget child;

  @override
  State<ThemeReveal> createState() => _ThemeRevealState();
}

class _ThemeRevealState extends State<ThemeReveal>
    with SingleTickerProviderStateMixin {
  final _boundary = GlobalKey();
  final _store = SettingsStore.instance;
  late ThemeMode _mode = _store.themeMode;
  late final _c =
      AnimationController(
        vsync: this,
        duration: const Duration(milliseconds: 620),
      )..addStatusListener((s) {
        if (s == AnimationStatus.completed) _clear();
      });
  ui.Image? _snapshot;
  Offset _origin = Offset.zero;

  @override
  void initState() {
    super.initState();
    _store.addListener(_onStore);
  }

  @override
  void dispose() {
    _store.removeListener(_onStore);
    _c.dispose();
    _snapshot?.dispose();
    super.dispose();
  }

  void _clear() {
    final old = _snapshot;
    setState(() => _snapshot = null);
    old?.dispose();
  }

  Brightness _resolve(ThemeMode m) => switch (m) {
    ThemeMode.light => Brightness.light,
    ThemeMode.dark => Brightness.dark,
    ThemeMode.system => MediaQuery.platformBrightnessOf(context),
  };

  void _onStore() {
    final next = _store.themeMode;
    final prev = _mode;
    if (next == prev) return;
    _mode = next;
    if (_resolve(prev) == _resolve(next)) return;
    final box =
        _boundary.currentContext?.findRenderObject() as RenderRepaintBoundary?;
    if (box == null || !box.hasSize || box.debugNeedsPaint) return;
    // The app rebuilds on the next frame, so the boundary still holds the
    // old theme's pixels here.
    final ui.Image image;
    try {
      image = box.toImageSync(
        pixelRatio: MediaQuery.devicePixelRatioOf(context),
      );
    } on Object {
      return;
    }
    _snapshot?.dispose();
    setState(() => _snapshot = image);
    _c.forward(from: 0);
  }

  @override
  Widget build(BuildContext context) {
    final reduce = MediaQuery.disableAnimationsOf(context);
    return Listener(
      behavior: HitTestBehavior.translucent,
      onPointerDown: (e) => _origin = e.localPosition,
      child: Stack(
        fit: StackFit.expand,
        children: [
          RepaintBoundary(key: _boundary, child: widget.child),
          if (_snapshot != null)
            IgnorePointer(
              child: AnimatedBuilder(
                animation: _c,
                builder: (context, child) {
                  final t = AppMotion.easeOut.transform(_c.value);
                  if (reduce) return Opacity(opacity: 1 - t, child: child);
                  return ClipPath(
                    clipper: _HoleClipper(_origin, t),
                    child: child,
                  );
                },
                child: RawImage(image: _snapshot, fit: BoxFit.fill),
              ),
            ),
        ],
      ),
    );
  }
}

/// The old-theme snapshot with a growing circular hole in it.
class _HoleClipper extends CustomClipper<Path> {
  _HoleClipper(this.center, this.t);
  final Offset center;
  final double t;

  @override
  Path getClip(Size size) {
    final far = [
      Offset.zero,
      Offset(size.width, 0),
      Offset(0, size.height),
      Offset(size.width, size.height),
    ].map((c) => (c - center).distance).reduce(math.max);
    return Path()
      ..fillType = PathFillType.evenOdd
      ..addRect(Offset.zero & size)
      ..addOval(Rect.fromCircle(center: center, radius: far * t));
  }

  @override
  bool shouldReclip(_HoleClipper old) => old.t != t || old.center != center;
}

/// Hero shuttle for a name's Arabic: always flies the larger (detail)
/// version, scaled to the moving rect, so it grows and shrinks smoothly.
Widget arabicFlight(
  BuildContext flightContext,
  Animation<double> animation,
  HeroFlightDirection direction,
  BuildContext fromContext,
  BuildContext toContext,
) {
  final big =
      (direction == HeroFlightDirection.push ? toContext : fromContext).widget
          as Hero;
  // Material, so the text doesn't pick up the debug underline in flight.
  return Material(
    type: MaterialType.transparency,
    child: FittedBox(fit: BoxFit.contain, child: big.child),
  );
}
