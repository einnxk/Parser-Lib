#!/usr/bin/env bash
set -euo pipefail
VERSION_FILE="version-info.json"
CURRENT_VERSION=$(jq -r '.version' "$VERSION_FILE")
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"
NEW_VERSION="${MAJOR}.${MINOR}.$((PATCH + 1))"
TMP_FILE=$(mktemp)
jq --arg v "$NEW_VERSION" '.version = $v' "$VERSION_FILE" > "$TMP_FILE"
mv "$TMP_FILE" "$VERSION_FILE"
echo "$NEW_VERSION"