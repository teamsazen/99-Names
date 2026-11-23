#!/bin/sh
# Renders index.html into out/: eight 1620x2880 Play screenshots and the 1024x500 feature
# graphic. Needs Google Chrome. Refresh the screens first with
#   (cd native && ./gradlew :app:recordPaparazziDebug) and copy them into screens/.
set -e
cd "$(dirname "$0")"
CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
mkdir -p out
shoot() { # id width height scale file
  "$CHROME" --headless=new --disable-gpu --hide-scrollbars --allow-file-access-from-files \
    --run-all-compositor-stages-before-draw --virtual-time-budget=4000 \
    --force-device-scale-factor="$4" --window-size="$2,$3" \
    --screenshot="$PWD/out/$5" "file://$PWD/index.html#$1" >/dev/null 2>&1
}
for i in 1 2 3 4 5 6 7 8; do shoot "s$i" 1080 1920 1.5 "0$i.png"; done
shoot fg 1024 500 1 feature-graphic.png
ls out
