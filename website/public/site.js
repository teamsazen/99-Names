// Page motion, as on urduquran.teamsazen.com: smooth scroll, headlines revealing line by
// line, sections fading in, phones drifting with the scroll. Plus the live "Name of the day" card (the same
// Name the app shows today), the 99 Names search and the
// privacy page's contents highlight. Without JS (or with Reduce Motion) everything is
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

  // Name of the day: the same Name the app opens on today, with arrows to try others.
  const today = document.querySelector('[data-today]');
  const data = document.getElementById('names-data');
  if (today && data) {
    const names = JSON.parse(data.textContent);
    const d = new Date();
    let i = Math.floor(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()) / 864e5) % 99;
    const first = i;
    const $ = (sel) => today.querySelector(sel);
    const paint = () => {
      const n = names[i];
      $('[data-n]').textContent = n.n;
      $('[data-ar]').textContent = n.ar;
      $('[data-tr]').textContent = n.tr;
      $('[data-en]').textContent = n.en;
      $('[data-ur]').textContent = n.ur;
      $('[data-rf]').textContent = n.rf;
      const link = $('[data-link]');
      link.href = n.path;
      link.textContent = `Read about ${n.tr} →`;
      $('[data-label]').textContent = i === first ? 'Name of the day' : `Name ${n.n} of 99`;
    };
    let busy = false;
    const go = (step) => {
      if (busy) return;
      i = (i + step + 99) % 99;
      if (reduce) { paint(); return; }
      busy = true;
      today.classList.add('out');
      setTimeout(() => {
        paint();
        today.classList.remove('out');
        busy = false;
      }, 260);
    };
    today.querySelectorAll('[data-step]').forEach((b) => b.addEventListener('click', () => go(Number(b.dataset.step))));
    paint();
  }

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
