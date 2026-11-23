#!/usr/bin/env python3
"""Generates the native app's data from the Flutter sources, so both apps share one truth.

- lib/src/data/names.dart  -> native/app/src/main/java/com/muzamil/asmaulhusna/data/Names.kt
- lib/src/ui/wordmark_path.dart -> native/app/src/main/java/com/muzamil/asmaulhusna/ui/Wordmark.kt

Run from the project root: python3 tool/native/gen_kotlin.py
"""
import re
from pathlib import Path

root = Path(__file__).resolve().parents[2]
pkg = root / "native/app/src/main/java/com/muzamil/asmaulhusna"

dart = (root / "lib/src/data/names.dart").read_text(encoding="utf-8")
lit = r"""'(?:[^'\\]|\\.)*'|"(?:[^"\\]|\\.)*\""""
entry = re.compile(
    rf"DivineName\(\s*(\d+),\s*({lit}),\s*({lit}),\s*({lit}),\s*({lit}),\s*({lit}),?\s*\)"
)
def unq(s):
    return re.sub(r"\\(.)", r"\1", s[1:-1])
def kt(s):
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$") + '"'
names = [m.groups() for m in entry.finditer(dart)]
assert len(names) == 99, len(names)
lines = ",\n".join(
    f"    DivineName({n}, {kt(unq(a))}, {kt(unq(t))}, {kt(unq(e))}, {kt(unq(u))}, {kt(unq(r))})"
    for n, a, t, e, u, r in names
)
(pkg / "data").mkdir(parents=True, exist_ok=True)
(pkg / "data/Names.kt").write_text(f"""// Generated from lib/src/data/names.dart by tool/native/gen_kotlin.py. Do not edit.
package com.muzamil.asmaulhusna.data

/** One of the 99 beautiful names of Allah. */
data class DivineName(
    val number: Int,
    val arabic: String,
    val transliteration: String,
    val english: String,
    val urdu: String,
    val explanation: String,
)

/** The 99 names in the order narrated by at-Tirmidhi. */
val names: List<DivineName> = listOf(
{lines},
)
""", encoding="utf-8")

wm = (root / "lib/src/ui/wordmark_path.dart").read_text(encoding="utf-8")
ops = []
for m in re.finditer(r"\.\.(moveTo|lineTo|quadraticBezierTo|cubicTo|close)\(([^)]*)\)", wm):
    op, args = m.group(1), m.group(2)
    nums = [a.strip() for a in args.split(",") if a.strip()]
    name = {"quadraticBezierTo": "quadraticTo", "close": "close"}.get(op, op)
    ops.append(f"    {name}({', '.join(n + 'f' for n in nums)})")
(pkg / "ui").mkdir(parents=True, exist_ok=True)
(pkg / "ui/Wordmark.kt").write_text("""// Generated from lib/src/ui/wordmark_path.dart by tool/native/gen_kotlin.py. Do not edit.
// Urdu: اسماء الحسنیٰ, outlined Nastaleeq on a 256-unit canvas.
package com.muzamil.asmaulhusna.ui

import androidx.compose.ui.graphics.Path

fun wordmarkPath(): Path = Path().apply {
""" + "\n".join(ops) + "\n}\n", encoding="utf-8")
print(f"Names.kt: 99 names; Wordmark.kt: {len(ops)} path operations")
