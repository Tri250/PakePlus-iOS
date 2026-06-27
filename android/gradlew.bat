@echo off
REM Gradle wrapper fallback for CI / local builds.
REM To generate the full wrapper (gradle-wrapper.jar), run:
REM   gradle wrapper --gradle-version 8.7
REM This script delegates to the system Gradle when the full wrapper is not present.

if "%GRADLE_BIN%"=="" set GRADLE_BIN=gradle

where /q %GRADLE_BIN%
if errorlevel 1 (
    echo ERROR: Gradle not found. Install Gradle 8.7 or run 'gradle wrapper' to generate the full wrapper.
    exit /b 1
)

%GRADLE_BIN% %*
