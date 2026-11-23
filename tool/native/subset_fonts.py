#!/usr/bin/env python3
"""Trims the Arabic font for the Android app and the website to what they draw.

- Harmattan (Arabic, SIL OFL): the letters and marks of the 99 Names, for
  native/ and website/public/fonts/.
Without hinting, which modern screens do not use. Jameel Noori Nastaleeq and Google
Sans are trimmed by tool/subset_fonts.py (into assets/fonts/), then copied here.

Needs the full fonts in tool/fonts_src/ (see its README) and
`pip install fonttools brotli`. Run from the project root:
    python3 tool/native/subset_fonts.py
"""
import re
from pathlib import Path

from fontTools import subset
from fontTools.ttLib import TTFont

root = Path(__file__).resolve().parents[2]
src = root / "tool/fonts_src"
res = root / "native/app/src/main/res/font"
dart = (root / "lib/src/data/names.dart").read_text(encoding="utf-8")

lit = r"""['"]([^'"]*)['"]"""
fields = re.findall(rf"DivineName\(\s*\d+,\s*{lit},\s*{lit},\s*{lit},\s*{lit},", dart)
arabic = "".join(f[0] for f in fields) + " "


def trim(font_in, text, out, flavor=None, variations=None):
    font = TTFont(font_in, lazy=False)
    opts = subset.Options()
    opts.layout_features = ["*"]
    opts.name_IDs = ["*"]
    opts.notdef_outline = True
    opts.hinting = False
    opts.flavor = flavor
    s = subset.Subsetter(opts)
    s.populate(text=text)
    s.subset(font)
    if variations:
        from fontTools.varLib import instancer  # noqa: F401
        font = instancer.instantiateVariableFont(font, variations)
    font.flavor = flavor
    font.save(out)
    print(f"{out.relative_to(root)}: {out.stat().st_size // 1024} KB")


trim(src / "Harmattan-Regular.ttf", arabic, res / "harmattan.ttf")
trim(src / "Harmattan-Regular.ttf", arabic, root / "website/public/fonts/harmattan.woff2", "woff2")
# Urdu (Jameel Noori Nastaleeq) and Google Sans come from tool/subset_fonts.py, whose
# harfbuzz-based trimming keeps Nastaleeq's ligatures small; copy them from assets/fonts/.
