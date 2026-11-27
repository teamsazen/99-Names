// Page motion, as on urduquran.teamsazen.com: smooth scroll, headlines revealing line by
// line, sections fading in, phones drifting with the scroll. Plus the theme switch, the
// hero phones trading places, the 99 Names search and the privacy page's contents highlight. Without JS (or with Reduce Motion) everything is
// simply visible.
(() => {
  const reduce = matchMedia('(prefers-reduced-motion: reduce)').matches;
  const hasIO = 'IntersectionObserver' in window;

  if (!reduce && window.Lenis) {
    const lenis = new Lenis({ lerp: 0.09, anchors: { offset: -90 } });
    const raf = (t) => { lenis.raf(t); requestAnimationFrame(raf); };
    requestAnimationFrame(raf);
  }

  const onceVisible = (selector, onIn, opts) => {
    const els = document.querySelectorAll(selector);
    if (!hasIO) { els.forEach(onIn); return; }
    const io = new IntersectionObserver((entries) => {
      for (const e of entries) {
        if (!e.isIntersecting) continue;
        onIn(e.target);
        io.unobserve(e.target);
      }
    }, opts || { rootMargin: '0px 0px -8% 0px', threshold: 0.08 });
    els.forEach((el) => io.observe(el));
  };

  // Theme: the system decides until the visitor flips the switch; their choice is remembered.
  const root = document.documentElement;
  const toggle = document.querySelector('[data-theme-toggle]');
  const themeMeta = document.querySelector('meta[name="theme-color"]');
  const stored = () => { try { return localStorage.getItem('theme'); } catch (e) { return null; } };
  const apply = (t) => {
    root.dataset.theme = t;
    if (themeMeta) themeMeta.content = t === 'dark' ? '#0A0E12' : '#F6F3EC';
    toggle?.setAttribute('aria-pressed', String(t === 'dark'));
  };
  apply(root.dataset.theme || (matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'));
  matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => { if (!stored()) apply(e.matches ? 'dark' : 'light'); });
  toggle?.addEventListener('click', () => {
    const next = root.dataset.theme === 'dark' ? 'light' : 'dark';
    try { localStorage.setItem('theme', next); } catch (e) {}
    // The icon turns first, then the new theme sweeps across the page from the button's side.
    toggle.setAttribute('aria-pressed', String(next === 'dark'));
    if (!document.startViewTransition) { setTimeout(() => apply(next), 80); return; }
    setTimeout(() => {
      const fromRight = toggle.getBoundingClientRect().left > innerWidth / 2;
      root.classList.add('theme-vt');
      const vt = document.startViewTransition(() => apply(next));
      vt.ready.then(() => root.animate(
        { clipPath: [fromRight ? 'inset(0 0 0 100%)' : 'inset(0 100% 0 0)', 'inset(0 0 0 0)'] },
        { duration: 360, easing: 'cubic-bezier(.65, 0, .35, 1)', pseudoElement: '::view-transition-new(root)' }));
      vt.finished.finally(() => root.classList.remove('theme-vt'));
    }, 80);
  });

  // Hero phones: tap one at the back and it trades places with the one in front.
  const phones = [...document.querySelectorAll('.hero-phones .phone')];
  const slots = ['ph-left', 'ph-main', 'ph-right'];
  const slotOf = (el) => slots.find((c) => el.classList.contains(c));
  const syncPhones = () => phones.forEach((el) => {
    const front = slotOf(el) === 'ph-main';
    el.setAttribute('role', front ? 'img' : 'button');
    el.tabIndex = front ? -1 : 0;
    el.setAttribute('aria-label', front ? 'In front' : 'Bring this screen to the front');
  });
  const bringFront = (el) => {
    const main = phones.find((p) => slotOf(p) === 'ph-main');
    if (!main || main === el) return;
    const slot = slotOf(el);
    el.closest('.hero-phones')?.classList.add('settled');
    main.classList.replace('ph-main', slot);
    el.classList.replace(slot, 'ph-main');
    syncPhones();
  };
  phones.forEach((el) => {
    el.addEventListener('click', () => bringFront(el));
    el.addEventListener('keydown', (e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); bringFront(el); } });
  });
  syncPhones();
  setTimeout(() => document.querySelector('.hero-phones')?.classList.add('settled'), 2000);

  // Search the 99 Names; tap a Name to show its reflection (its name links to its page).
  const q = document.getElementById('q');
  const cards = [...document.querySelectorAll('.name')];
  const empty = document.getElementById('empty');
  q?.addEventListener('input', () => {
    const term = q.value.trim().toLowerCase();
    let shown = 0;
    for (const c of cards) {
      const hit = !term || c.dataset.search.includes(term);
      c.hidden = !hit;
      if (hit) shown++;
    }
    empty.hidden = shown > 0;
  });
  for (const c of cards) c.addEventListener('click', (e) => { if (!e.target.closest('a')) c.classList.toggle('open'); });

  // Privacy page: highlight the section being read in the contents list.
  const tocLinks = [...document.querySelectorAll('.toc a')];
  if (tocLinks.length && hasIO) {
    const tio = new IntersectionObserver((entries) => {
      for (const e of entries) {
        if (!e.isIntersecting) continue;
        tocLinks.forEach((a) => a.classList.toggle('on', a.getAttribute('href') === `#${e.target.id}`));
      }
    }, { rootMargin: '-30% 0px -60% 0px' });
    document.querySelectorAll('.legal-body section[id]').forEach((s) => tio.observe(s));
  }

  if (hasIO && !reduce) {
    // Headlines: wrap each line (split on <br>) so it can slide up on its own.
    document.querySelectorAll('h1, main h2').forEach((h) => {
      if (h.closest('.legal-body')) return;
      const parts = h.innerHTML.split(/<br\s*\/?>/i);
      h.innerHTML = parts.map((p) => `<span class="ln"><span>${p.trim()}</span></span>`).join('');
      h.classList.add('lines');
    });

    const items = document.querySelectorAll('.head, .row-copy > :not(h2), .adv-list article, .deep-copy > :not(h2), .deep-art, .names .grid, .final .wrap > :not(h2), .legal-body section, .toc, .name-body .wrap > *, .name-pager');
    items.forEach((el, i) => {
      el.classList.add('reveal');
      el.style.transitionDelay = `${(i % 3) * 70}ms`;
    });
    onceVisible('.reveal', (el) => el.classList.add('in'));
    onceVisible('.lines', (el) => el.classList.add('in'));

    // Phone pairs drift against the scroll for a little depth.
    const drifters = [...document.querySelectorAll('[data-drift]')];
    const drift = () => {
      const vh = innerHeight;
      for (const el of drifters) {
        const r = el.getBoundingClientRect();
        if (r.bottom < 0 || r.top > vh) continue;
        const k = (r.top + r.height / 2 - vh / 2) / vh;
        el.style.transform = `translateY(${k * -46}px)`;
      }
    };
    addEventListener('scroll', drift, { passive: true });
    drift();
  }
})();
