#!/bin/sh

if test -e versions.sh.next; then
  mv versions.sh.next versions.sh
  git add versions.sh

  ./generate-readmes.sh
  git add README.md */README.md

  git commit -m "Update version information"
fi
