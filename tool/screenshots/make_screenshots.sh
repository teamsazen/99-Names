#!/bin/sh
# Actual Flutter screens, Play Store artwork, and website assets.
set -eu
cd "$(dirname "$0")/../.."
flutter test tool/screenshots/render_test.dart
# Google Play requires 24-bit PNG files without an alpha channel.
for file in store/playstore/*.png; do
  magick "$file" -alpha off -define png:color-type=2 "$file"
done
for name in home rahman sabur favourites search grid dark settings; do
  magick "store/screenshots/$name.png" -resize 780x1688 -quality 88 "website/public/img/$name.webp"
done
# Dark versions, shown when the visitor's device is in dark mode.
mkdir -p website/public/img/dark
for name in home rahman grid settings; do
  magick "store/screenshots/dark/$name.png" -resize 780x1688 -quality 88 "website/public/img/dark/$name.webp"
done
# The Play Store artwork, for the website's gallery.
mkdir -p website/public/img/store
for file in store/playstore/0*.png; do
  n=$(basename "$file" .png)
  magick "$file" -resize 630x1120 -quality 86 "website/public/img/store/$n.webp"
  magick "$file" -resize 315x560 -quality 86 "website/public/img/store/$n-sm.webp"
done
magick store/playstore/feature_graphic.png -resize 1200x586 -background '#101820' -gravity center -extent 1200x630 website/public/og.png
cp assets/fonts/JameelNooriNastaleeq.ttf website/public/fonts/nastaleeq.ttf
