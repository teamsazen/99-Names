import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';

/// Firebase Analytics, on Android release builds only. Everywhere else
/// (iOS, debug, profile, tests) every call is a no-op and Firebase is never
/// started. The native side is gated the same way through the
/// `firebase_analytics_collection_enabled` manifest flag.
abstract final class Analytics {
  static FirebaseAnalytics? _fa;

  static bool get _supported =>
      kReleaseMode &&
      !kIsWeb &&
      defaultTargetPlatform == TargetPlatform.android;

  /// Starts Firebase in the background; the first frame never waits for it.
  static Future<void> init() async {
    if (!_supported || _fa != null) return;
    try {
      // Options come from android/app/google-services.json (not in the
      // public repo); without it this throws and analytics stays off.
      await Firebase.initializeApp();
      _fa = FirebaseAnalytics.instance;
    } on Object catch (e) {
      debugPrint('Analytics unavailable: $e');
    }
  }

  /// Logs a screen view for every named route the user lands on.
  static final NavigatorObserver observer = _ScreenObserver();

  static void log(String name, [Map<String, Object>? parameters]) {
    _fa?.logEvent(name: name, parameters: parameters);
  }

  static void openName(int number, String source) =>
      log('open_name', {'number': number, 'source': source});

  static void favourite(int number, {required bool added}) =>
      log(added ? 'favourite_add' : 'favourite_remove', {'number': number});
}

class _ScreenObserver extends NavigatorObserver {
  void _screen(Route<dynamic>? route) {
    final name = route?.settings.name;
    if (name == null || route is! PageRoute) return;
    Analytics._fa?.logScreenView(screenName: name == '/' ? 'home' : name);
  }

  @override
  void didPush(Route<dynamic> route, Route<dynamic>? previousRoute) =>
      _screen(route);

  @override
  void didPop(Route<dynamic> route, Route<dynamic>? previousRoute) =>
      _screen(previousRoute);

  @override
  void didReplace({Route<dynamic>? newRoute, Route<dynamic>? oldRoute}) =>
      _screen(newRoute);
}
