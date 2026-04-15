@echo off
setlocal enabledelayedexpansion

set "ROOT=%~dp0"
set "LWJGL_DIR=C:\Program Files\lwjgl-release-3.3.4-custom"
set "BUILD_DIR=%ROOT%out"
set "NATIVE_DIR=%ROOT%.natives"
set "SOURCE_DIR=%ROOT%src\main\java"

if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"
if not exist "%NATIVE_DIR%" mkdir "%NATIVE_DIR%"

pushd "%NATIVE_DIR%"
for %%J in ("%LWJGL_DIR%\*-natives-windows.jar") do (
    jar xf "%%~fJ"
)
popd

set "SOURCE_FILES="
for /f "delims=" %%F in ('dir /s /b "%SOURCE_DIR%\*.java"') do (
    set "SOURCE_FILES=!SOURCE_FILES! "%%F""
)

if not defined SOURCE_FILES (
    echo No Java source files were found in %SOURCE_DIR%
    exit /b 1
)

javac -encoding UTF-8 -cp "%LWJGL_DIR%\*;%SOURCE_DIR%" -d "%BUILD_DIR%" !SOURCE_FILES!
if errorlevel 1 exit /b 1

java -Dorg.lwjgl.librarypath="%NATIVE_DIR%" -cp "%BUILD_DIR%;%LWJGL_DIR%\*" app.Main