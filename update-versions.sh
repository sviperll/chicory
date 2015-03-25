#!/bin/sh

if test -e versions.sh.next; then
  mv versions.sh.next versions.sh

  ./generate-readmes.sh
fi
