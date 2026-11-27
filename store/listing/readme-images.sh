#!/bin/sh
# README copies of the store images with rounded corners (GitHub drops CSS, so the corners
# are cut into the images). The Play Store files in store/playstore/ stay square.
# The banner shows at about 860 px wide and the screenshots at 180 px, so both corners
# come out at about 8 px on the page. Needs ImageMagick.
set -e
cd "$(dirname "$0")/../.."
out=.github/readme
round() { # in out radius [width]
  magick "$1" ${4:+-resize "$4"x} \( +clone -alpha extract -fill black -colorize 100 -fill white \
    -draw "roundrectangle 0,0 %[fx:w-1],%[fx:h-1] $3,$3" \) -alpha off -compose CopyOpacity -composite \
    -strip "$2"
}
round store/playstore/feature_graphic.png "$out/banner.png" 10
mkdir -p "$out/screens"
for i in 1 2 3 4 5 6 7 8; do round "store/playstore/0$i.png" "$out/screens/0$i.png" 16 360; done
ls -la "$out" "$out/screens"
