@echo off
echo Building WooCommerce Android Release APK in stages to avoid memory issues

REM Clean the project first
call gradlew clean

REM Kill any running Gradle daemons
taskkill /F /IM java.exe

REM Set environment variables for the build
set GRADLE_OPTS=-Xmx8g -XX:MaxMetaspaceSize=1g -XX:+HeapDumpOnOutOfMemoryError -XX:+UseParallelGC

REM Compile the code without minification
echo Step 1: Compiling the code without minification...
call gradlew compileVanillaReleaseKotlin compileVanillaReleaseJavaWithJavac

REM Process resources and generate R files
echo Step 2: Processing resources...
call gradlew processVanillaReleaseResources

REM Build the final APK skipping lint checks
echo Step 3: Building the final APK (skipping lint)...
call gradlew assembleVanillaRelease -x lint

echo Build completed!
if exist "WooCommerce\build\outputs\apk\vanilla\release\WooCommerce-vanilla-release.apk" (
    echo APK built successfully at WooCommerce\build\outputs\apk\vanilla\release\WooCommerce-vanilla-release.apk
) else (
    echo Build may have failed, check the logs.
)

pause 