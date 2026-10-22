#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
command -v java >/dev/null || { echo "Install JDK 21 first (https://adoptium.net)"; exit 1; }
V=8.12
D=".gradle-local/gradle-$V"
if [ ! -x "$D/bin/gradle" ]; then
  mkdir -p .gradle-local
  curl -L "https://services.gradle.org/distributions/gradle-$V-bin.zip" -o .gradle-local/gradle.zip
  unzip -q -o .gradle-local/gradle.zip -d .gradle-local
fi
"$D/bin/gradle" build
echo "DONE: build/libs/paybuttons-1.0.0.jar"
