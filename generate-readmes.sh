#!/bin/sh

. ./versions.sh

export STABLE_VERSION
if ! test -z "$UNSTABLE_VERSION"; then
  export UNSTABLE_VERSION
fi

for R in README.md.mustache */README.md.mustache; do
  RR=$(echo "$R" | sed 's/.mustache$//g')
  cat "$R" | ./mo > "$RR"
done
