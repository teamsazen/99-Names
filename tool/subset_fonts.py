"""Trims the fonts to what the app and the website actually draw.

Run after changing any text in the app (native/) or the website's Arabic or Urdu:
    pip install fonttools uharfbuzz brotli
    python3 tool/subset_fonts.py

Full source fonts are not committed; download them into tool/fonts_src/
(see tool/fonts_src/README.md). Writes:
- native/app/src/main/res/font/: google_sans_*.ttf, harmattan.ttf, nastaleeq.ttf
- website/public/fonts/: harmattan.woff2, nastaleeq.ttf
(the website's google-sans.woff2 is a full copy and is left alone)
"""
import pathlib
import re
import sys

import uharfbuzz as hb
from fontTools import subset
from fontTools.ttLib import TTFont
from fontTools.varLib import instancer

root = pathlib.Path(__file__).resolve().parent.parent
src = root / "tool" / "fonts_src"
app_fonts = root / "native" / "app" / "src" / "main" / "res" / "font"
web_fonts = root / "website" / "public" / "fonts"

kotlin = "".join(
    p.read_text(encoding="utf-8")
    for p in (root / "native" / "app" / "src" / "main" / "java").rglob("*.kt")
)
literals = [m.replace('\\"', '"') for m in re.findall(r'"((?:[^"\\\n]|\\.)*)"', kotlin)]
web_text = "".join(p.read_text(encoding="utf-8") for p in (root / "website" / "src").glob("*.html"))

ascii_ = "".join(chr(c) for c in range(0x20, 0x7F))
latin = ascii_ + "".join(sorted({c for c in kotlin if 0x80 <= ord(c) < 0x600})) + "—’…©°"

# Arabic: the letters and marks of the 99 Names (the field after the number).
names_kt = (root / "native/app/src/main/java/com/muzamil/asmaulhusna/data/Names.kt").read_text(encoding="utf-8")
arabic = "".join(re.findall(r'DivineName\(\s*\d+,\s*"([^"]*)"', names_kt)) + " "

# Urdu strings are the ones using Urdu-only letters (ے ہ ی ک گ ں ٹ ڈ ڑ ھ).
urdu_letters = "[ےہیکگںٹڈڑھ]"
urdu_app = [s for s in literals if re.search(urdu_letters, s)]
urdu_web = re.findall(r'lang="ur"[^>]*>([^<]+)<', web_text)


def options():
    o = subset.Options()
    o.layout_features = ["*"]  # keep shaping, ligatures and mark positioning
    o.name_IDs = ["*"]
    o.notdef_outline = True
    o.hinting = False
    return o


def save(font, out, src_size, flavor=None):
    font.flavor = flavor
    font.save(str(out))
    print(f"{out.relative_to(root)}: {src_size // 1024} KB -> {out.stat().st_size // 1024} KB")
    return out


def subset_chars(path, chars, out, flavor=None):
    o = options()
    font = subset.load_font(str(path), o)
    s = subset.Subsetter(o)
    s.populate(text=chars)
    s.subset(font)
    return save(font, out, path.stat().st_size, flavor)


def shape(path, strings, gids=None):
    """Glyph outlines and positions HarfBuzz produces for each string and
    word. Outlines stand in for glyph names, which some fonts leave blank."""
    blob = hb.Blob.from_file_path(str(path))
    font = hb.Font(hb.Face(blob))
    tt = TTFont(str(path))
    order, glyf = tt.getGlyphOrder(), tt["glyf"]
    outline = lambda i: glyf[order[i]].compile(glyf)
    runs = []
    for s in strings:
        for piece in [s, *s.split()]:
            buf = hb.Buffer()
            buf.add_str(piece)
            buf.guess_segment_properties()
            if gids is not None:
                # Nastaliq ligatures form in stages; keep the glyphs seen
                # between lookups too, or the rules that use them are dropped.
                def seen(_message, buf=buf):
                    gids.update(i.codepoint for i in buf.glyph_infos)
                    return True

                buf.set_message_func(seen)
            hb.shape(font, buf)
            if gids is not None:
                gids.update(i.codepoint for i in buf.glyph_infos)
            runs.append([
                (outline(i.codepoint), p.x_advance, p.x_offset, p.y_offset)
                for i, p in zip(buf.glyph_infos, buf.glyph_positions)
            ])
    return runs


def subset_shaped(path, strings, out):
    """Keeps only the glyphs the given strings shape to. Nastaliq fonts hold
    thousands of precomposed ligatures, so this is far smaller than keeping
    every glyph reachable from the characters."""
    gids = {0}
    before = shape(path, strings, gids)
    o = options()
    o.layout_closure = False
    font = subset.load_font(str(path), o)
    s = subset.Subsetter(o)
    s.populate(gids=gids, text="".join(strings) + " ۔")
    s.subset(font)
    save(font, out, path.stat().st_size)
    if shape(out, strings) != before:
        sys.exit(f"{out.name}: shaping changed after trimming; keep more glyphs")


sans = src / "GoogleSans[GRAD,opsz,wght].ttf"
for weight, style in [(400, "regular"), (600, "semibold"), (700, "bold")]:
    static = instancer.instantiateVariableFont(
        TTFont(str(sans)), {"wght": weight, "opsz": 18, "GRAD": 0}
    )
    s = subset.Subsetter(options())
    s.populate(text=latin)
    s.subset(static)
    save(static, app_fonts / f"google_sans_{style}.ttf", sans.stat().st_size)

harmattan = src / "Harmattan-Regular.ttf"
subset_chars(harmattan, arabic, app_fonts / "harmattan.ttf")
subset_chars(harmattan, arabic, web_fonts / "harmattan.woff2", "woff2")

nastaleeq = src / "JameelNooriNastaleeq.ttf"
subset_shaped(nastaleeq, urdu_app, app_fonts / "nastaleeq.ttf")
subset_shaped(nastaleeq, urdu_app + urdu_web, web_fonts / "nastaleeq.ttf")
