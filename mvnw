#!/bin/sh
set -e

if command -v mvn >/dev/null 2>&1; then
  exec mvn "$@"
fi

echo "Maven is required to use this lightweight wrapper. Install Maven or run this project in Render's Java environment." >&2
exit 1
