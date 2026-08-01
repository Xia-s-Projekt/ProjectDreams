#!/bin/zsh
set -e
cd "$(dirname "$0")"

./gradlew :app:assembleDebug -q
adb install -r app/build/outputs/apk/debug/app-debug.apk
echo "Deployed"
