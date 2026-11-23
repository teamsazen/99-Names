// Draws the README's Contributors block: each contributor's avatar with their
// commit count in a badge on the top right. Writes .github/readme/contributors-{light,dark}.svg.
// Run by .github/workflows/contributors.yml on every push to main.
// Locally: GITHUB_TOKEN=$(gh auth token) node .github/scripts/contributors.mjs
import { writeFile } from 'node:fs/promises';

const repo = process.env.GITHUB_REPOSITORY ?? 'teamsazen/99-Names';
const headers = { Accept: 'application/vnd.github+json', 'User-Agent': 'contributors-svg' };
if (process.env.GITHUB_TOKEN) headers.Authorization = `Bearer ${process.env.GITHUB_TOKEN}`;

// 999 stays as is; then 1K, 1.11K, 12.5K… (cut, not rounded, so 999.999 never reads 1000K).
export const short = (n) => {
  for (const [size, unit] of [[1e9, 'B'], [1e6, 'M'], [1e3, 'K']]) {
    if (n >= size) return `${(Math.floor((n / size) * 100) / 100).toString()}${unit}`;
  }
  return String(n);
};

const res = await fetch(`https://api.github.com/repos/${repo}/contributors?per_page=100`, { headers });
if (!res.ok) throw new Error(`GitHub API: ${res.status} ${await res.text()}`);
const people = (await res.json()).filter((c) => c.type === 'User');

// Avatars are embedded: GitHub shows README SVGs as images, which can't load other URLs.
for (const p of people) {
  const img = await fetch(`${p.avatar_url}${p.avatar_url.includes('?') ? '&' : '?'}s=128`);
  const type = img.headers.get('content-type') ?? 'image/png';
  p.data = `data:${type};base64,${Buffer.from(await img.arrayBuffer()).toString('base64')}`;
}

const esc = (s) => s.replace(/[&<>"]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' })[c]);
const SIZE = 64, GAP = 26, PAD = 14, PER_ROW = 10;
const CELL = SIZE + GAP;

const draw = ({ bg, ring }) => {
  const cols = Math.min(people.length, PER_ROW) || 1;
  const rows = Math.ceil(people.length / PER_ROW) || 1;
  const width = PAD * 2 + cols * CELL - GAP + 24;
  const height = PAD + rows * CELL - GAP + 6;
  const items = people.map((p, i) => {
    const x = PAD + (i % PER_ROW) * CELL;
    const y = PAD + Math.floor(i / PER_ROW) * CELL;
    const cx = x + SIZE / 2, cy = y + SIZE / 2, r = SIZE / 2;
    const label = short(p.contributions);
    const bw = Math.max(22, 9 + label.length * 7.4), bh = 22;
    const bx = x + SIZE - bw / 2 - 4, by = y - 8;
    return `
  <g>
    <title>${esc(p.login)}: ${p.contributions} commits</title>
    <clipPath id="c${i}"><circle cx="${cx}" cy="${cy}" r="${r}"/></clipPath>
    <image href="${p.data}" x="${x}" y="${y}" width="${SIZE}" height="${SIZE}" clip-path="url(#c${i})"/>
    <circle cx="${cx}" cy="${cy}" r="${r - 1.5}" fill="none" stroke="${ring}" stroke-width="3"/>
    <rect x="${bx}" y="${by}" width="${bw}" height="${bh}" rx="${bh / 2}" fill="#E5484D" stroke="${bg}" stroke-width="3"/>
    <text x="${bx + bw / 2}" y="${by + 15.5}" text-anchor="middle" fill="#fff" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Helvetica, Arial, sans-serif" font-size="12.5" font-weight="700">${label}</text>
  </g>`;
  }).join('');
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}">${items}
</svg>
`;
};

// Badge outlines match GitHub's page background so the badge looks cut out of the avatar.
await writeFile('.github/readme/contributors-light.svg', draw({ bg: '#ffffff', ring: '#1f2328' }));
await writeFile('.github/readme/contributors-dark.svg', draw({ bg: '#0d1117', ring: '#f0f6fc' }));
console.log(people.map((p) => `${p.login} ${short(p.contributions)}`).join('\n'));
