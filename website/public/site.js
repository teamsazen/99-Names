// Page motion (same as the other Sazen app sites): smooth scroll (the header pill always
// stays in view), headlines reveal line by line, sections fade in, numbers count up,
// cards tilt, phone pairs drift and the hero coverflow advances. Plus the 99 Names
// search, and tapping a Name shows its reflection.
// Without JS (or with Reduce Motion) everything is simply visible.
(() => {
  const reduce = matchMedia('(prefers-reduced-motion: reduce)').matches;
  const fine = matchMedia('(pointer: fine)').matches;
  const hasIO = 'IntersectionObserver' in window;

  // Smooth scrolling (Lenis drives native scroll, so scroll events still fire).
  if (!reduce && window.Lenis) {
    const lenis = new Lenis({ lerp: 0.09, anchors: { offset: -90 } });
    const raf = (t) => { lenis.raf(t); requestAnimationFrame(raf); };
    requestAnimationFrame(raf);
  }

  // Search the 99 Names; tap a Name to show its reflection.
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
  for (const c of cards) c.addEventListener('click', () => c.classList.toggle('open'));

  // Giant footer wordmark: fill the width exactly.
  const fit = () => document.querySelectorAll('[data-fit]').forEach((el) => {
    el.style.fontSize = '100px';
    const natural = [...el.children].reduce((w, c) => w + c.getBoundingClientRect().width, 0);
    if (natural) el.style.fontSize = `${(el.clientWidth / natural) * 100 * 0.995}px`;
  });
  (document.fonts ? document.fonts.ready : Promise.resolve()).then(fit);
  addEventListener('resize', fit);

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

  // Hero showcase: coverflow of the app's screens, auto-advancing until the visitor takes over.
  const show = document.querySelector('[data-show]');
  if (show) {
    const slides = [...show.querySelectorAll('.slide')];
    const infos = [...show.querySelectorAll('.info')];
    const ghost = show.querySelector('.show-ghost span');
    const count = show.querySelector('[data-count]');
    const stage = show.querySelector('.show-stage');
    const n = slides.length;
    const DWELL = 4200;
    let active = Number(show.dataset.active) || 0;
    let timer = 0, paused = false, visible = true;
    const place = (raw) => {
      const d = ((raw % n) + n + 4) % n - 4;
      const a = Math.abs(d);
      return { x: [0, .78, 1.45, 1.95, 2.3][a] * Math.sign(d), ry: -d * 16, s: [1, .84, .7, .6, .5][a], o: [1, .92, .5, 0, 0][a], z: 10 - a, far: a >= 2 };
    };
    const restart = () => {
      clearTimeout(timer);
      show.classList.remove('ticking');
      if (reduce || paused || !visible) return;
      show.style.setProperty('--dwell', `${DWELL}ms`);
      void show.offsetWidth;
      show.classList.add('ticking');
      timer = setTimeout(() => go(active + 1), DWELL);
    };
    const go = (next) => {
      active = (next + n) % n;
      slides.forEach((slide, i) => {
        const p = place(i - active);
        slide.style.cssText = `--x:${p.x};--ry:${p.ry}deg;--s:${p.s};--o:${p.o};z-index:${p.z}`;
        slide.toggleAttribute('data-far', p.far);
        slide.classList.toggle('is-active', i === active);
        if (!p.far) slide.querySelector('img')?.setAttribute('loading', 'eager');
      });
      infos.forEach((info, i) => { info.hidden = i !== active; });
      ghost.textContent = infos[active].querySelector('.eyebrow').textContent;
      if (!reduce) ghost.animate([{ opacity: 0, transform: 'translateY(12%)' }, { opacity: 1, transform: 'none' }], { duration: 900, easing: 'cubic-bezier(.16,1,.3,1)' });
      count.textContent = String(active + 1).padStart(2, '0');
      restart();
    };
    show.querySelector('[data-prev]').addEventListener('click', () => go(active - 1));
    show.querySelector('[data-next]').addEventListener('click', () => go(active + 1));
    slides.forEach((slide, i) => slide.addEventListener('click', () => { if (i !== active) go(i); }));
    show.addEventListener('pointerenter', (e) => { if (e.pointerType === 'mouse') { paused = true; restart(); } });
    show.addEventListener('pointerleave', () => { paused = false; restart(); });
    show.addEventListener('focusin', () => { paused = true; restart(); });
    show.addEventListener('focusout', () => { paused = false; restart(); });
    show.addEventListener('keydown', (e) => {
      if (e.key === 'ArrowLeft') go(active - 1);
      if (e.key === 'ArrowRight') go(active + 1);
    });
    let sx = 0, sy = 0;
    stage.addEventListener('pointerdown', (e) => { sx = e.clientX; sy = e.clientY; });
    stage.addEventListener('pointerup', (e) => {
      const dx = e.clientX - sx;
      if (Math.abs(dx) > 40 && Math.abs(dx) > Math.abs(e.clientY - sy)) go(active + (dx < 0 ? 1 : -1));
    });
    if (hasIO) new IntersectionObserver(([e]) => { visible = e.isIntersecting; restart(); }, { threshold: 0.2 }).observe(show);
    go(active);
  }

  if (!hasIO || reduce) return;

  // Headlines: wrap each line (split on <br>) so it can slide up on its own.
  document.querySelectorAll('h1, main h2').forEach((h) => {
    if (h.closest('.legal-body')) return;
    const parts = h.innerHTML.split(/<br\s*\/?>/i);
    h.innerHTML = parts.map((p) => `<span class="ln"><span>${p.trim()}</span></span>`).join('');
    h.classList.add('lines');
  });

  const once = (selector, onIn, opts) => {
    const io = new IntersectionObserver((entries) => {
      for (const e of entries) {
        if (!e.isIntersecting) continue;
        onIn(e.target);
        io.unobserve(e.target);
      }
    }, opts || { rootMargin: '0px 0px -8% 0px', threshold: 0.08 });
    document.querySelectorAll(selector).forEach((el) => io.observe(el));
  };

  // Entrance. The Names grid is left out: on a phone it is far taller than the screen,
  // so a share-of-element threshold would never be met and it would stay hidden.
  const items = document.querySelectorAll('.head, .stats > div, .tile, .adv-list article, .dark-copy, .dark-card .phone, .row-copy, .pair, .search, .faq, .final .wrap > *, .legal-body section, .toc');
  items.forEach((el, i) => {
    el.classList.add('reveal');
    el.style.transitionDelay = `${(i % 3) * 70}ms`;
  });
  once('.reveal', (el) => el.classList.add('in'));
  once('.lines', (el) => el.classList.add('in'));
  once('.foot .wordmark', (el) => el.classList.add('in'), { threshold: 0.2 });

  // Numbers count up the first time they appear.
  once('.stats dt', (dt) => {
    const end = Number(dt.textContent);
    if (!Number.isFinite(end) || end === 0) return;
    const t0 = performance.now(), dur = 1400;
    const step = (t) => {
      const k = Math.min(1, (t - t0) / dur);
      dt.textContent = String(Math.round(end * (1 - Math.pow(1 - k, 3))));
      if (k < 1) requestAnimationFrame(step);
    };
    dt.textContent = '0';
    requestAnimationFrame(step);
  }, { threshold: 0.6 });

  // Feature cards tilt under the pointer.
  if (fine) {
    document.querySelectorAll('.tile, .stats > div').forEach((el) => {
      el.addEventListener('pointermove', (e) => {
        const r = el.getBoundingClientRect();
        const x = (e.clientX - r.left) / r.width - 0.5, y = (e.clientY - r.top) / r.height - 0.5;
        el.style.transition = 'transform .15s ease-out, opacity 1s';
        el.style.transform = `rotateX(${-y * 6}deg) rotateY(${x * 8}deg)`;
      });
      el.addEventListener('pointerleave', () => { el.style.transition = ''; el.style.transform = ''; });
    });
  }

  // Phone pairs drift against the scroll for a little depth.
  const drifters = [...document.querySelectorAll('.pair')];
  const drift = () => {
    const vh = innerHeight;
    for (const el of drifters) {
      const r = el.getBoundingClientRect();
      if (r.bottom < 0 || r.top > vh) continue;
      const k = (r.top + r.height / 2 - vh / 2) / vh;
      el.style.transform = `translateY(${k * -50}px)`;
    }
  };
  addEventListener('scroll', drift, { passive: true });
  drift();
})();
