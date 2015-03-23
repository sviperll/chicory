#!/bin/sh

VERSION="$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '^\[INFO\]')"

echo "Current version: $VERSION"

echo "Release version"
echo "==============="

N_MAX=$(echo "$VERSION release" | awk -f version.awk | wc -l)
echo "$VERSION release" | awk -f version.awk | awk '{print ++n ". " $0} END{print ++n ". Custom" }'

echo "Choose variant: "
read N

if test "$N" -lt 1 || test "$N" -gt "$N_MAX"; then
  echo "Enter custom version number:"
  read RELEASE_VERSION
else
  RELEASE_VERSION=$(echo "$VERSION release" | awk -f version.awk | head -n "$N" | tail -n 1)
fi

echo "Release version: $RELEASE_VERSION"

NEXT_DEVELOPMENT_VERSION=$(echo "$RELEASE_VERSION next development" | awk -f version.awk)

echo "Next development version: $NEXT_DEVELOPMENT_VERSION"

mvn release:prepare -DdryRun=true "-DreleaseVersion=$RELEASE_VERSION" "-DdevelopmentVersion=$NEXT_DEVELOPMENT_VERSION"
