#!/bin/bash

set -euo pipefail

echo "--- :rubygems: Setting up Gems"
install_gems

echo "--- :closed_lock_with_key: Installing Secrets"
bundle exec fastlane run configure_apply

echo "--- 🛠 Download Mobile App Dependencies [Assemble Mobile App]"
./gradlew :WooCommerce:assembleJalapenoDebug
echo ""

echo "--- 🛠 Download Wear App Dependencies [Assemble Wear App]"
./gradlew :WooCommerce-Wear:assembleJalapenoDebug
echo ""

echo "--- 🧹 Download Lint Dependencies [Lint Mobile App]"
./gradlew :WooCommerce:lintJalapenoDebug
echo ""

echo "--- 🧪 Download Unit Test Dependencies [Assemble Unit Tests]"
./gradlew assembleJalapenoDebugUnitTest assembleDebugUnitTest
echo ""

echo "--- 🧪 Download Android Test Dependencies [Assemble Android Tests]"
./gradlew assembleJalapenoDebugAndroidTest
echo ""

echo "--- 💾 Save Cache"
save_gradle_dependency_cache
