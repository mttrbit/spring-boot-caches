#!/usr/bin/env bash

JAVADOC_SITE_PATH=api/latest

# Fail the script if one command fails
set -e

cp -f CHANGELOG.md docs/CHANGELOG.md
cp -f CONTRIBUTING.md docs/CONTRIBUTING.md

# Clean our site directory
rm -rf site

# Make the necessary files locatable by MkDocs
mkdir -p docs/providers
cp -f backends/caffeine-cache-provider/README.md docs/providers/caffeine.md
cp -f backends/simple-cache-provider/README.md docs/providers/simple.md
cp -f backends/memcache-cache-provider/README.md docs/providers/memcache.md
cp -f README.md docs/index.md

# Ensure MkDocs & used theme are installed
pip install mkdocs-material

# Generate docs
mkdocs build
./gradlew clean rootJavadoc

# Copy generated Javadoc site to main site directory
mkdir -p site/$JAVADOC_SITE_PATH
cp -Rf build/docs/javadoc/* site/$JAVADOC_SITE_PATH

rm -f docs/CHANGELOG.md
rm -f docs/CONTRIBUTING.md

rm -rf docs/providers
rm -rf docs/index.md