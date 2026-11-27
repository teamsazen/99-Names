#!/bin/sh
# The website's phones: real captures from store/listing/device/ without status bar or
# gesture handle (cleanbar.py --bare, see store/listing/README.md) put in the real front phone frame, cropped to the phone and
# saved as webp: public/img/<name>.webp (light) and public/img/dark/<name>.webp (dark).
# A screen with only a light capture shows it in both themes.
# Needs ImageMagick and python3 with numpy, opencv-python-headless and pillow.
set -e
cd "$(dirname "$0")/.."
L=../store/listing
TMP=$(mktemp -d)
frame() { # name capture out-dir
  python3 "$L/tool/cleanbar.py" --bare "$L/device/$2.png" "$TMP/bare-$2.png"
  python3 "$L/tool/composite.py" "$TMP/$1.png" "$L/frames/phone-front.png" "$TMP/bare-$2.png" >/dev/null
  magick "$TMP/$1.png" -crop 1510x3040+265+30 +repage -resize 780x -quality 84 -define webp:alpha-quality=90 "$3/$1.webp"
}
mkdir -p public/img/dark
for pair in home:home rahman:name-rahman grid:name-wadud; do
  n=${pair%%:*}; c=${pair#*:}
  frame "$n" "$c-light" public/img
  frame "$n" "$c-dark" public/img/dark
done
frame search search-giv public/img; rm -f public/img/dark/search.webp
frame favourites fav-light public/img; rm -f public/img/dark/favourites.webp
rm -rf "$TMP"
ls public/img public/img/dark
