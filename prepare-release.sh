#!/bin/sh

if ! test -e versions.sh; then
  touch versions.sh
fi

. ./versions.sh

VERSION="$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '^\[INFO\]')"

echo "Current version: $VERSION"

echo "Release version"
echo "==============="

echo "Choose what kind of release to perform:"
echo " 1. Alpha"
echo " 2. Beta"
echo " 3. Release Candidate"
echo " 4. Final"

echo "Choose variant: "
read N
KIND=$(echo alpha beta rc final| awk -v "N=$N" '{print $N}')

N_MAX=$(echo "$VERSION $KIND" | awk -f version.awk | wc -l)
echo "$VERSION $KIND" | awk -f version.awk | awk '{print ++n ". " $0} END{print ++n ". Custom" }'

echo "Choose variant: "
read N

if test "$N" -lt 1 || test "$N" -gt "$N_MAX"; then
  echo "Enter custom version number:"
  read RELEASE_VERSION
else
  RELEASE_VERSION=$(echo "$VERSION $KIND" | awk -f version.awk | head -n "$N" | tail -n 1)
fi

echo "Release version: $RELEASE_VERSION"

if test "$RELEASE_VERSION" '=' "$(echo $RELEASE_VERSION final | awk -f version.awk)"; then
  STABLE_VERSION="$RELEASE_VERSION"
  UNSTABLE_VERSION=""
else
  UNSTABLE_VERSION="$RELEASE_VERSION"
fi

NEXT_DEVELOPMENT_VERSION="${RELEASE_VERSION}-successor-SNAPSHOT"

echo "Next development version: $NEXT_DEVELOPMENT_VERSION"

echo "STABLE_VERSION=$STABLE_VERSION" > versions.sh.next
if ! test -z "$UNSTABLE_VERSION"; then
  echo "UNSTABLE_VERSION=$UNSTABLE_VERSION" >> versions.sh.next
fi

mvn release:prepare "-DreleaseVersion=$RELEASE_VERSION" "-DdevelopmentVersion=$NEXT_DEVELOPMENT_VERSION"
