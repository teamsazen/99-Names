#!/bin/sh
# Renders post.html into out/: eight 1080x1350 (4:5) social posts with whole phones.
# Needs Google Chrome. The framed phones come from composed/ (see README.md).
set -e
cd "$(dirname "$0")"
CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
mkdir -p out
for i in 1 2 3 4 5 6 7 8; do
  "$CHROME" --headless=new --disable-gpu --hide-scrollbars --allow-file-access-from-files \
    --run-all-compositor-stages-before-draw --virtual-time-budget=4000 \
    --force-device-scale-factor=1 --window-size=1080,1350 \
    --screenshot="$PWD/out/post-$i.png" "file://$PWD/post.html#p$i" >/dev/null 2>&1
done
ls out/post-*.png
