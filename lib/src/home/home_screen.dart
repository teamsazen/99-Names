import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../data/names.dart';
import '../detail/name_detail_screen.dart';
import '../settings/settings_screen.dart';
import '../settings/settings_store.dart';
import '../ui/app_logo.dart';
import '../ui/icons.dart';
import '../ui/motion.dart';
import '../ui/theme.dart';
import '../ui/widgets.dart';
import 'name_cards.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final _search = TextEditingController();
  String _query = '';
  bool _favouritesOnly = false;
  bool _raised = false;

  @override
  void dispose() {
    _search.dispose();
    super.dispose();
  }

  List<DivineName> _visible(SettingsStore s) {
    final q = normalizeForSearch(_query);
    return [
      for (final n in names)
        if ((!_favouritesOnly || s.isFavourite(n.number)) &&
            (q.isEmpty || _matches(n, q)))
          n,
    ];
  }

  static bool _matches(DivineName n, String q) =>
      '${n.number}' == q ||
      normalizeForSearch(n.transliteration).contains(q) ||
      normalizeForSearch(n.english).contains(q) ||
      normalizeForSearch(n.arabic).contains(q) ||
      normalizeForSearch(n.urdu).contains(q);

  void _open(List<DivineName> list, int index, String heroPrefix) {
    FocusScope.of(context).unfocus();
    Navigator.of(context).push(
      CupertinoPageRoute(
        builder: (_) => NameDetailScreen(
          names: list,
          initialIndex: index,
          heroPrefix: heroPrefix,
        ),
      ),
    );
  }

  void _openSettings() => Navigator.of(
    context,
  ).push(CupertinoPageRoute(builder: (_) => const SettingsScreen()));

  /// The glass bar takes over once the ink hero has scrolled away.
  bool _onScroll(ScrollNotification n) {
    if (n.depth == 0) {
      final raised = n.metrics.pixels > context.r(430);
      if (raised != _raised) setState(() => _raised = raised);
    }
    return false;
  }

  @override
  Widget build(BuildContext context) => ListenableBuilder(
    listenable: SettingsStore.instance,
    builder: (context, _) => _build(context, SettingsStore.instance),
  );

  Widget _build(BuildContext context, SettingsStore s) {
    final list = _visible(s);
    final side = context.r(16);
    final browsing = _query.isNotEmpty || _favouritesOnly;
    final favourites = [
      for (final n in names)
        if (s.isFavourite(n.number)) n,
    ];
    final today = nameOfTheDay(DateTime.now());
    final toggleView = CircleButton(
      glyph: s.gridLayout ? AppGlyph.list : AppGlyph.grid,
      tooltip: s.gridLayout ? 'List view' : 'Grid view',
      onTap: () {
        HapticFeedback.selectionClick();
        s.setGridLayout(!s.gridLayout);
      },
    );
    final settings = CircleButton(
      glyph: AppGlyph.sliders,
      tooltip: 'Settings',
      onTap: _openSettings,
    );
    // Light status bar over the ink hero, then the theme's own once raised.
    final overlay = !_raised || context.isDark
        ? SystemUiOverlayStyle.light
        : SystemUiOverlayStyle.dark;
    return AnnotatedRegion<SystemUiOverlayStyle>(
      value: overlay.copyWith(statusBarColor: Colors.transparent),
      child: Scaffold(
        body: Stack(
          children: [
            GestureDetector(
              onTap: () => FocusScope.of(context).unfocus(),
              child: NotificationListener<ScrollNotification>(
                onNotification: _onScroll,
                child: CustomScrollView(
                  keyboardDismissBehavior:
                      ScrollViewKeyboardDismissBehavior.onDrag,
                  slivers: [
                    SliverToBoxAdapter(
                      child: _HomeHero(
                        today: today,
                        onOpen: () => _open(names, today.number - 1, 'day'),
                        onToggleView: () {
                          HapticFeedback.selectionClick();
                          s.setGridLayout(!s.gridLayout);
                        },
                        gridLayout: s.gridLayout,
                        onSettings: _openSettings,
                      ),
                    ),
                    SliverPadding(
                      padding: EdgeInsets.symmetric(horizontal: side),
                      sliver: const SliverToBoxAdapter(
                        child: Reveal(
                          delay: Duration(milliseconds: 360),
                          child: _Verse(),
                        ),
                      ),
                    ),
                    if (favourites.isNotEmpty)
                      SliverToBoxAdapter(
                        child: Padding(
                          padding: EdgeInsets.only(top: context.r(30)),
                          child: _FavouritesShelf(
                            favourites: favourites,
                            side: side,
                            onOpen: (i) => _open(favourites, i, 'fav'),
                          ),
                        ),
                      ),
                    SliverPadding(
                      padding: EdgeInsets.fromLTRB(
                        side,
                        context.r(30),
                        side,
                        0,
                      ),
                      sliver: SliverList.list(
                        children: [
                          SectionTitle(
                            _favouritesOnly ? 'Favourites' : 'All names',
                            trailing: browsing ? '${list.length}' : '99',
                          ),
                          _SearchField(
                            controller: _search,
                            onChanged: (v) => setState(() => _query = v),
                          ),
                          const SizedBox(height: 10),
                          _ScopeControl(
                            favouritesOnly: _favouritesOnly,
                            favourites: s.favourites.length,
                            onChanged: (v) =>
                                setState(() => _favouritesOnly = v),
                          ),
                          const SizedBox(height: 16),
                        ],
                      ),
                    ),
                    if (list.isEmpty)
                      SliverToBoxAdapter(
                        child: _EmptyState(
                          favourites: _favouritesOnly && _query.isEmpty,
                        ),
                      )
                    else
                      SliverPadding(
                        padding: EdgeInsets.fromLTRB(
                          side,
                          0,
                          side,
                          context.r(28) + MediaQuery.paddingOf(context).bottom,
                        ),
                        sliver: s.gridLayout
                            ? SliverGrid.builder(
                                gridDelegate:
                                    SliverGridDelegateWithFixedCrossAxisCount(
                                      crossAxisCount: 2,
                                      mainAxisSpacing: 12,
                                      crossAxisSpacing: 12,
                                      mainAxisExtent: context.r(200),
                                    ),
                                itemCount: list.length,
                                itemBuilder: (_, i) => NameGridCard(
                                  name: list[i],
                                  onTap: () => _open(list, i, 'all'),
                                ),
                              )
                            : SliverList.builder(
                                itemCount: list.length,
                                itemBuilder: (_, i) => NameListRow(
                                  name: list[i],
                                  first: i == 0,
                                  last: i == list.length - 1,
                                  onTap: () => _open(list, i, 'all'),
                                ),
                              ),
                      ),
                  ],
                ),
              ),
            ),
            Positioned(
              top: 0,
              left: 0,
              right: 0,
              child: GlassBar(
                raised: _raised,
                hideUntilRaised: true,
                title: '99 Names',
                leading: const SizedBox(width: 92),
                trailing: [toggleView, settings],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// The same name for everyone on a given day, cycling through all 99.
DivineName nameOfTheDay(DateTime date) {
  final day = DateTime.utc(date.year, date.month, date.day);
  return names[day.millisecondsSinceEpoch ~/ Duration.millisecondsPerDay % 99];
}

/// Full-bleed ink hero: the brand row, the title and today's name in gold
/// over a rosette. Its blocks rise in one after another on launch.
class _HomeHero extends StatelessWidget {
  const _HomeHero({
    required this.today,
    required this.onOpen,
    required this.onToggleView,
    required this.gridLayout,
    required this.onSettings,
  });

  final DivineName today;
  final VoidCallback onOpen;
  final VoidCallback onToggleView;
  final bool gridLayout;
  final VoidCallback onSettings;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    final s = SettingsStore.instance;
    const lip = 28.0;
    return ColoredBox(
      color: p.hero,
      child: Column(
        children: [
          SafeArea(
            bottom: false,
            child: Padding(
              padding: EdgeInsets.fromLTRB(20, 8, 16, context.r(10)),
              child: Row(
                children: [
                  const AppLogo(size: 34, radius: AppRadius.icon),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Text(
                      'Asmaul Husna',
                      style: TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.w700,
                        color: p.onHero,
                      ),
                    ),
                  ),
                  CircleButton(
                    glyph: gridLayout ? AppGlyph.list : AppGlyph.grid,
                    tooltip: gridLayout ? 'List view' : 'Grid view',
                    onHero: true,
                    onTap: onToggleView,
                  ),
                  const SizedBox(width: 8),
                  CircleButton(
                    glyph: AppGlyph.sliders,
                    tooltip: 'Settings',
                    onHero: true,
                    onTap: onSettings,
                  ),
                ],
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Reveal(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      LargeTitle('The 99 Names', color: p.onHero),
                      const SizedBox(height: 4),
                      Text(
                        'of Allah, with meanings in English and Urdu',
                        style: TextStyle(fontSize: 15, color: p.heroMuted),
                      ),
                    ],
                  ),
                ),
                SizedBox(height: context.r(22)),
                Reveal(
                  delay: const Duration(milliseconds: 90),
                  child: Row(
                    children: [
                      Eyebrow('NAME OF THE DAY', color: p.logoInk),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Divider(
                          color: p.onHero.withValues(alpha: .14),
                          thickness: 1,
                        ),
                      ),
                      const SizedBox(width: 10),
                      NumberRing(today.number, size: 32, color: p.logoInk),
                    ],
                  ),
                ),
                Pressable(
                  onTap: onOpen,
                  semanticLabel: 'Open ${today.transliteration}',
                  child: SizedBox(
                    height: context.r(210),
                    child: Stack(
                      alignment: Alignment.center,
                      children: [
                        RosetteIn(
                          size: context.r(200),
                          color: p.logoInk.withValues(alpha: .3),
                        ),
                        Reveal(
                          delay: const Duration(milliseconds: 180),
                          child: Hero(
                            tag: 'day-${today.number}',
                            flightShuttleBuilder: arabicFlight,
                            child: FittedBox(
                              fit: BoxFit.scaleDown,
                              child: Text(
                                today.arabic,
                                maxLines: 1,
                                textDirection: TextDirection.rtl,
                                style: TextStyle(
                                  fontFamily: AppFonts.arabic,
                                  fontSize: context.r(70) * s.arabicScale,
                                  height: 1.6,
                                  color: p.logoInk,
                                ),
                              ),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                Reveal(
                  delay: const Duration(milliseconds: 260),
                  child: Column(
                    children: [
                      Text(
                        today.transliteration,
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          fontSize: context.r(26),
                          fontWeight: FontWeight.w700,
                          letterSpacing: -.7,
                          color: p.onHero,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        today.english,
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          fontSize: context.r(16),
                          fontWeight: FontWeight.w500,
                          color: p.heroMuted,
                        ),
                      ),
                      SizedBox(height: context.r(18)),
                      Center(
                        child: PillButton(
                          label: 'Read reflection',
                          glyph: AppGlyph.arrowRight,
                          trailingIcon: true,
                          filled: true,
                          color: p.logoInk,
                          foreground: p.hero,
                          height: 46,
                          onTap: onOpen,
                        ),
                      ),
                    ],
                  ),
                ),
                SizedBox(height: context.r(30)),
              ],
            ),
          ),
          // The page rises over the hero with rounded shoulders.
          Container(
            height: lip,
            decoration: ShapeDecoration(
              color: p.background,
              shape: const RoundedSuperellipseBorder(
                borderRadius: BorderRadius.vertical(top: Radius.circular(lip)),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

/// Al-A'raf 7:180, set as a quiet pull quote on the page itself.
class _Verse extends StatelessWidget {
  const _Verse();

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8),
      child: Column(
        children: [
          Text(
            'اور اللہ کے سب نام اچھے ہی اچھے ہیں، تو اسے انہی ناموں سے پکارو',
            textAlign: TextAlign.center,
            textDirection: TextDirection.rtl,
            style: TextStyle(
              fontFamily: AppFonts.urdu,
              fontSize: context.r(27),
              height: 1.65,
              color: p.body,
            ),
          ),
          const SizedBox(height: 6),
          Row(
            children: [
              Expanded(child: Divider(color: p.border, thickness: 1)),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 12),
                child: Text(
                  "AL-A'RAF 7:180",
                  style: TextStyle(
                    fontSize: 11.5,
                    fontWeight: FontWeight.w700,
                    letterSpacing: 1.4,
                    color: p.gold,
                  ),
                ),
              ),
              Expanded(child: Divider(color: p.border, thickness: 1)),
            ],
          ),
        ],
      ),
    );
  }
}

/// Horizontal shelf of saved names, so favourites are one tap from home.
class _FavouritesShelf extends StatelessWidget {
  const _FavouritesShelf({
    required this.favourites,
    required this.side,
    required this.onOpen,
  });

  final List<DivineName> favourites;
  final double side;
  final ValueChanged<int> onOpen;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Padding(
          padding: EdgeInsets.symmetric(horizontal: side),
          child: SectionTitle(
            'Your favourites',
            trailing: '${favourites.length}',
          ),
        ),
        SizedBox(
          height: 132,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            padding: EdgeInsets.symmetric(horizontal: side),
            itemCount: favourites.length,
            separatorBuilder: (_, _) => const SizedBox(width: 10),
            itemBuilder: (_, i) {
              final n = favourites[i];
              return Surface(
                onTap: () => onOpen(i),
                radius: AppRadius.tile,
                color: p.hero,
                padding: const EdgeInsets.fromLTRB(12, 8, 12, 12),
                child: SizedBox(
                  width: 108,
                  child: Column(
                    children: [
                      Expanded(
                        child: Center(
                          child: Hero(
                            tag: 'fav-${n.number}',
                            flightShuttleBuilder: arabicFlight,
                            child: FittedBox(
                              fit: BoxFit.scaleDown,
                              child: Text(
                                n.arabic,
                                maxLines: 1,
                                textDirection: TextDirection.rtl,
                                style: TextStyle(
                                  fontFamily: AppFonts.arabic,
                                  fontSize: 34,
                                  height: 1.6,
                                  color: p.logoInk,
                                ),
                              ),
                            ),
                          ),
                        ),
                      ),
                      Text(
                        n.transliteration,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(
                          fontSize: 13.5,
                          fontWeight: FontWeight.w700,
                          color: p.onHero,
                        ),
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
        ),
      ],
    );
  }
}

/// Lowercases and strips punctuation, Arabic diacritics and alif variants so
/// "ar rahman", "Ar-Rahman" and "الرحمن" all match.
String normalizeForSearch(String s) => s
    .toLowerCase()
    .replaceAll(RegExp('[ً-ٰٟۖ-ۭـ]'), '')
    .replaceAll(RegExp('[ٱآأإ]'), 'ا')
    .replaceAll(RegExp(r"[\s\-'’ʿ،,.]"), '');

/// Filled pill search field.
class _SearchField extends StatelessWidget {
  const _SearchField({required this.controller, required this.onChanged});
  final TextEditingController controller;
  final ValueChanged<String> onChanged;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    return ListenableBuilder(
      listenable: controller,
      builder: (context, _) => Container(
        height: 46,
        decoration: ShapeDecoration(
          color: p.surfaceRaised,
          shape: const StadiumBorder(),
        ),
        child: Row(
          children: [
            const SizedBox(width: 16),
            AppIcon(AppGlyph.search, size: 19, color: p.muted),
            const SizedBox(width: 10),
            Expanded(
              child: TextField(
                controller: controller,
                onChanged: onChanged,
                textInputAction: TextInputAction.search,
                style: TextStyle(fontSize: 16, color: p.title),
                decoration: InputDecoration(
                  isCollapsed: true,
                  border: InputBorder.none,
                  hintText: 'Search name, meaning or number',
                  hintStyle: TextStyle(fontSize: 16, color: p.muted),
                ),
              ),
            ),
            AnimatedSwitcher(
              duration: AppMotion.fast,
              switchInCurve: AppMotion.easeOut,
              transitionBuilder: (child, a) => FadeTransition(
                opacity: a,
                child: ScaleTransition(
                  scale: Tween(begin: .8, end: 1.0).animate(a),
                  child: child,
                ),
              ),
              child: controller.text.isEmpty
                  ? const SizedBox(width: 14)
                  : Pressable(
                      onTap: () {
                        controller.clear();
                        onChanged('');
                      },
                      semanticLabel: 'Clear search',
                      child: Container(
                        margin: const EdgeInsets.only(right: 10),
                        width: 26,
                        height: 26,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          color: p.faint.withValues(alpha: .35),
                        ),
                        child: Center(
                          child: AppIcon(
                            AppGlyph.close,
                            size: 14,
                            color: p.title,
                          ),
                        ),
                      ),
                    ),
            ),
          ],
        ),
      ),
    );
  }
}

/// All names / favourites, as a native sliding segmented control.
class _ScopeControl extends StatelessWidget {
  const _ScopeControl({
    required this.favouritesOnly,
    required this.favourites,
    required this.onChanged,
  });

  final bool favouritesOnly;
  final int favourites;
  final ValueChanged<bool> onChanged;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    TextStyle style(bool selected) => TextStyle(
      fontFamily: AppFonts.sans,
      fontSize: 14,
      fontWeight: selected ? FontWeight.w700 : FontWeight.w500,
      color: selected ? p.title : p.muted,
    );
    return SizedBox(
      width: double.infinity,
      child: CupertinoSlidingSegmentedControl<bool>(
        groupValue: favouritesOnly,
        backgroundColor: p.surfaceRaised,
        thumbColor: context.isDark ? p.border : p.surface,
        padding: const EdgeInsets.all(3),
        onValueChanged: (v) {
          if (v != null) onChanged(v);
        },
        children: {
          false: Padding(
            padding: const EdgeInsets.symmetric(vertical: 7),
            child: Text('All 99', style: style(!favouritesOnly)),
          ),
          true: Padding(
            padding: const EdgeInsets.symmetric(vertical: 7),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                AppIcon(AppGlyph.heartFilled, size: 14, color: p.accent),
                const SizedBox(width: 6),
                Flexible(
                  child: Text(
                    'Favourites  $favourites',
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: style(favouritesOnly),
                  ),
                ),
              ],
            ),
          ),
        },
      ),
    );
  }
}

class _EmptyState extends StatelessWidget {
  const _EmptyState({required this.favourites});
  final bool favourites;

  @override
  Widget build(BuildContext context) {
    final p = context.palette;
    return Reveal(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(32, 32, 32, 64),
        child: Column(
          children: [
            Container(
              width: 60,
              height: 60,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: p.accentDeep,
              ),
              child: Center(
                child: AppIcon(
                  favourites ? AppGlyph.heart : AppGlyph.search,
                  size: 26,
                  color: p.accent,
                ),
              ),
            ),
            const SizedBox(height: 14),
            Text(
              favourites ? 'No favourites yet' : 'No matching names',
              style: TextStyle(
                fontSize: 17,
                fontWeight: FontWeight.w700,
                letterSpacing: -.2,
                color: p.title,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              favourites
                  ? 'Open a name and tap the heart to keep it here.'
                  : 'Try a transliteration, an English meaning or a number.',
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 14, height: 1.4, color: p.muted),
            ),
          ],
        ),
      ),
    );
  }
}
