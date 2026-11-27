# Play Store listing

Eight 1620×2880 screenshots and the 1024×500 feature graphic, made from real-phone
screenshots inside a photo phone frame (the same frames as the Budget-AI listing). Only the first shot has a hand; no other people.

1. **Capture** on a connected phone (1080×2400, e.g. the S21): install with
   `cd native && ./gradlew :app:installDebug`, open each screen and
   `adb exec-out screencap -p > raw.png`.
2. **Clean the status bar** (Samsung ignores demo mode, so it is painted over: 09:41, wifi, signal, battery 100; the app moves 28 px down under a band of its own colour, captures stay untouched in `device/`):
   `python3 tool/cleanbar.py device/<name>.png ready/<name>.png`
3. **Put it in a frame**: `python3 tool/composite.py composed/<out>.png frames/<frame>.png ready/<shot>.png [ready/<back-shot>.png]`
   Frames: `hand-phone` (first shot), `phone-tilt-left`, `phone-tilt-right`, `phone-front`, `phone-pair` (two screens: front first).
4. **Render**: `./render.sh` → `out/`, then copy to `../playstore/` (`feature-graphic.png` → `feature_graphic.png`).

The Python tools need `numpy opencv-python-headless pillow`.
