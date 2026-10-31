// Builds the Asmaul Husna website into dist/: copies public/, fills src/index.html
// with all 99 Names read straight from the app's lib/src/data/names.dart (so the
// site never drifts from the app), writes one page per Name from src/name.html at
// /names/<name>, and a sitemap listing every page.
// Run: node build.mjs
import { createHash } from 'node:crypto';
import { cp, mkdir, readFile, rm, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const here = path.dirname(fileURLToPath(import.meta.url));
const dist = path.join(here, 'dist');
const SITE = 'https://99.teamsazen.com';

// Dart string literal, single or double quoted, with escapes.
const literal = String.raw`'(?:[^'\\]|\\.)*'|"(?:[^"\\]|\\.)*"`;
const unquote = (s) => s.slice(1, -1).replace(/\\(.)/g, '$1');

const dart = await readFile(path.join(here, '..', 'lib/src/data/names.dart'), 'utf8');
const entry = new RegExp(
  String.raw`DivineName\(\s*(\d+),\s*(${literal}),\s*(${literal}),\s*(${literal}),\s*(${literal}),\s*(${literal}),?\s*\)`,
  'g',
);
const names = [...dart.matchAll(entry)].map((m) => ({
  number: Number(m[1]),
  arabic: unquote(m[2]),
  transliteration: unquote(m[3]),
  english: unquote(m[4]),
  urdu: unquote(m[5]),
  reflection: unquote(m[6]),
}));
if (names.length !== 99) throw new Error(`Expected 99 names in names.dart, found ${names.length}`);

// URL slug from the transliteration ("Al-Mu'min" -> "al-mumin"). Two Names share
// the transliteration Al-Majid, so a slug used more than once gets the number too.
const slugOf = (t) => t.normalize('NFKD').replace(/[̀-ͯ]/g, '').toLowerCase()
  .replace(/['’‘]/g, '').replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
const counts = {};
for (const n of names) counts[slugOf(n.transliteration)] = (counts[slugOf(n.transliteration)] ?? 0) + 1;
for (const n of names) {
  const s = slugOf(n.transliteration);
  n.slug = counts[s] > 1 ? `${s}-${n.number}` : s;
  n.path = `/names/${n.slug}`;
}

const esc = (s) => String(s).replace(/[&<>"]/g, (c) => `&#${c.charCodeAt(0)};`);
const grid = names
  .map(
    (n) => `<li class="name" data-search="${esc(`${n.number} ${n.transliteration} ${n.english}`.toLowerCase())}">
  <span class="num">${n.number}</span>
  <span class="ar" lang="ar" dir="rtl">${esc(n.arabic)}</span>
  <a class="tr" href="${n.path}">${esc(n.transliteration)}</a>
  <span class="en">${esc(n.english)}</span>
  <span class="ur" lang="ur" dir="rtl">${esc(n.urdu)}</span>
  <span class="rf">${esc(n.reflection)}</span>
</li>`,
  )
  .join('\n');

// Cache-busting: the stylesheet and script versions follow their content.
const version = async (file) => createHash('sha256').update(await readFile(path.join(here, 'public', file))).digest('hex').slice(0, 8);
const cssv = await version('styles.css');
const jsv = await version('site.js');
const bust = (html) => html.replace(/\/styles\.css\?v=[^"]+/g, `/styles.css?v=${cssv}`).replace(/\/site\.js\?v=[^"]+/g, `/site.js?v=${jsv}`);

const template = await readFile(path.join(here, 'src/index.html'), 'utf8');
await rm(dist, { recursive: true, force: true });
await mkdir(dist, { recursive: true });
await cp(path.join(here, 'public'), dist, { recursive: true });
// The privacy policy has one source, docs/privacy-policy/, published with the Cloudflare website.
await mkdir(path.join(dist, 'privacy'), { recursive: true });
const privacy = await readFile(path.join(here, '..', 'docs/privacy-policy/index.html'), 'utf8');
await writeFile(path.join(dist, 'privacy/index.html'), bust(privacy));
const home = bust(template.replace('{{NAMES}}', grid));
await writeFile(path.join(dist, 'index.html'), home);

// Name pages share the home page's icon symbols, header and footer; header links
// that point at home sections get a leading slash.
const take = (re) => home.match(re)?.[0] ?? (() => { throw new Error(`Missing ${re} in index.html`); })();
const symbols = take(/<svg width="0" height="0"[\s\S]*?<\/svg>/);
const header = take(/<header class="pill-wrap">[\s\S]*?<\/header>/).replace(/href="#/g, 'href="/#');
const footer = take(/<footer class="foot">[\s\S]*?<\/footer>/);
const page = await readFile(path.join(here, 'src/name.html'), 'utf8');
await mkdir(path.join(dist, 'names'), { recursive: true });
const clip = (s, max) => (s.length <= max ? s : `${s.slice(0, s.lastIndexOf(' ', max - 1))}…`);
for (const [i, n] of names.entries()) {
  const prev = names[(i + 98) % 99], next = names[(i + 1) % 99];
  const url = `${SITE}${n.path}`;
  const ld = [
    {
      '@context': 'https://schema.org', '@type': 'DefinedTerm', name: n.transliteration, alternateName: n.arabic,
      description: `${n.english}. ${n.reflection}`, termCode: String(n.number), url,
      inDefinedTermSet: { '@type': 'DefinedTermSet', name: '99 Names of Allah (Asmaul Husna)', url: `${SITE}/#names` },
    },
    {
      '@context': 'https://schema.org', '@type': 'BreadcrumbList',
      itemListElement: [['99 Names - Asmaul Husna', `${SITE}/`], ['The 99 Names', `${SITE}/#names`], [n.transliteration, url]]
        .map(([name, item], k) => ({ '@type': 'ListItem', position: k + 1, name, item })),
    },
  ].map((data) => `<script type="application/ld+json">${JSON.stringify(data)}</script>`).join('\n');
  const fill = {
    TITLE: `${n.transliteration} meaning: ${n.english} · 99 Names of Allah`,
    DESC: clip(`${n.transliteration} (${n.english}) is Name ${n.number} of the 99 Names of Allah: its Arabic, transliteration, English and Urdu meaning, and a short reflection. ${n.reflection}`, 160),
    URL: url, CSSV: cssv, JSV: jsv, LD: ld, SYMBOLS: symbols, HEADER: header, FOOTER: footer,
    N: n.number, AR: n.arabic, TR: n.transliteration, EN: n.english, UR: n.urdu, RF: n.reflection,
    PREV_URL: prev.path, PREV_N: prev.number, PREV_TR: prev.transliteration, PREV_EN: prev.english,
    NEXT_URL: next.path, NEXT_N: next.number, NEXT_TR: next.transliteration, NEXT_EN: next.english,
  };
  const raw = new Set(['LD', 'SYMBOLS', 'HEADER', 'FOOTER']);
  const html = page.replace(/\{\{([A-Z_]+)\}\}/g, (_, key) => {
    if (!(key in fill)) throw new Error(`Unknown placeholder {{${key}}} in name.html`);
    return raw.has(key) ? fill[key] : esc(fill[key]);
  });
  await writeFile(path.join(dist, 'names', `${n.slug}.html`), html);
}

const urls = ['/', '/privacy/', ...names.map((n) => n.path)];
await writeFile(path.join(dist, 'sitemap.xml'), `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
${urls.map((u) => `  <url><loc>${SITE}${u}</loc></url>`).join('\n')}
</urlset>
`);
console.log(`dist/ built with ${names.length} Names and ${names.length} Name pages`);
