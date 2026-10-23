#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
if [[ -z "${JAVA_HOME:-}" ]]; then
  if /usr/libexec/java_home -v 21 >/dev/null 2>&1; then
    JAVA_HOME="$(/usr/libexec/java_home -v 21)"
  else
    JAVA_HOME="$(find "$HOME/.local/jdks" -type d -name Home -print -quit 2>/dev/null || true)"
  fi
  export JAVA_HOME
fi
if ! command -v mvn >/dev/null 2>&1; then
  if [[ -x "$HOME/.local/maven/apache-maven-3.9.9/bin/mvn" ]]; then
    export PATH="$HOME/.local/maven/apache-maven-3.9.9/bin:$PATH"
  fi
fi
export PATH="${JAVA_HOME:+$JAVA_HOME/bin:}$PATH"
cd "$ROOT/backend"
exec mvn spring-boot:run "$@"
