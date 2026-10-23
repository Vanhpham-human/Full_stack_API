#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
if [[ -x "$ROOT/.tools/node/bin/npm" ]]; then
  export PATH="$ROOT/.tools/node/bin:$PATH"
fi
cd "$ROOT/frontend"
if [[ ! -d node_modules ]]; then
  npm install
fi
exec npm run dev
