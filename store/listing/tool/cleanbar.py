"""Replace the phone's real status bar with a clean one: 09:41, wifi, full signal and a
battery pill reading 100. Samsung ignores Android demo mode, so the bar is painted over.
The photo frames round off the screen's top corners, so the bar sits lower and further in
than a real one, and the app is moved EXTRA px down under a taller band of its own top
colour so it keeps its breathing room. The app itself is not changed: the rows that fall
off the bottom are behind the gesture handle, which is put back where it was.
With --bare there is no status bar and no gesture handle: just the band (for the website).
usage: cleanbar.py [--bare] device/x.png ready/x.png   (1080x2400 capture; never run it on its own output)"""
import sys
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont

BAR = 92  # status bar height in px on a 1080x2400 / 480dpi screen
EXTRA = 28  # extra band above the app
HANDLE = (351, 2372, 729, 2384)  # gesture handle pill: x0, y0, x1, y1
LEFT, RIGHT, MID = 64, 1016, 72  # time's left edge, battery's right edge, vertical centre
FONT = Path(__file__).resolve().parents[3] / "tool/fonts_src/GoogleSans[GRAD,opsz,wght].ttf"
S = 4  # draw supersampled, then shrink for clean edges


def font(px, wght):
    f = ImageFont.truetype(str(FONT), px * S)
    try:
        f.set_variation_by_axes([0, 18, wght])  # GRAD, opsz, wght
    except Exception:
        pass
    return f


def draw_bar(width):
    """An alpha mask (L) of the whole bar at width x BAR."""
    m = Image.new("L", (width * S, BAR * S), 0)
    d = ImageDraw.Draw(m)
    s = lambda *v: [round(x * S) for x in v]

    d.text(s(LEFT, MID), "09:41", font=font(40, 720), fill=255, anchor="lm")

    # battery: a filled pill with "100" cut out of it, and a small cap
    x = RIGHT
    bw, bh = 62, 32
    d.rounded_rectangle(s(x + 2, MID - 6, x + 6, MID + 6), radius=2 * S, fill=150)
    d.rounded_rectangle(s(x - bw, MID - bh / 2, x, MID + bh / 2), radius=11 * S, fill=255)
    d.text(s(x - bw / 2, MID + 0.5), "100", font=font(22, 800), fill=0, anchor="mm")
    x -= bw + 20

    # signal: four rounded bars rising to the right
    bar_w, gap, tall = 7, 5, 30
    left = x - (4 * bar_w + 3 * gap)
    base = MID + tall / 2
    for i in range(4):
        h = tall * (0.4 + 0.2 * i)
        bx = left + i * (bar_w + gap)
        d.rounded_rectangle(s(bx, base - h, bx + bar_w, base), radius=2.5 * S, fill=255)
    x = left - 18

    # wifi: a fan of three bands with a filled tip, cut from solid wedges
    r, cx = 26, x - 26 * 0.72
    cy = MID + 13
    box = lambda rr: s(cx - rr, cy - rr, cx + rr, cy + rr)
    for rr, fill in ((r, 255), (r - 7, 0), (r - 11, 255), (r - 18, 0), (r - 22, 255)):
        d.pieslice(box(rr), 225, 315, fill=fill)
    return m.resize((width, BAR), Image.LANCZOS)


def handle_mask(h, w):
    m = Image.new("L", (w * S, h * S), 0)
    x0, y0, x1, y1 = HANDLE
    ImageDraw.Draw(m).rounded_rectangle([x0 * S, y0 * S, x1 * S, y1 * S], radius=(y1 - y0) / 2 * S, fill=255)
    return np.asarray(m.resize((w, h), Image.LANCZOS)).astype(np.float32)[..., None] / 255


def main():
    args = [a for a in sys.argv[1:] if a != "--bare"]
    bare = "--bare" in sys.argv
    src, out = args[0], args[1]
    orig = np.asarray(Image.open(src).convert("RGB"))
    h, w = orig.shape[:2]
    top = BAR + EXTRA
    a = np.empty_like(orig)
    a[top:] = orig[BAR: h - EXTRA]  # the app, moved down
    a[:top] = orig[BAR + 2]  # a band of the app's own top colour
    # put the gesture handle back at the bottom, over the moved app
    a = a.astype(np.float32)
    if bare:  # the handle fell off the bottom with the move; no bar either
        Image.fromarray(np.clip(a, 0, 255).astype(np.uint8)).save(out)
        return
    hm = handle_mask(h, w)
    a = a * (1 - hm) + orig * hm

    bg = orig[BAR + 2].mean(0)
    light_bg = (0.299 * bg[0] + 0.587 * bg[1] + 0.114 * bg[2]) > 140
    fg = (26, 23, 18) if light_bg else (247, 243, 234)
    mask = np.asarray(draw_bar(w)).astype(np.float32)[..., None] / 255
    a[:BAR] = a[:BAR] * (1 - mask) + np.array(fg, np.float32) * mask
    Image.fromarray(np.clip(a, 0, 255).astype(np.uint8)).save(out)


if __name__ == "__main__":
    main()
