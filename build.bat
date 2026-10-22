@echo off
setlocal
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
  echo Java was not found. Install JDK 21 from https://adoptium.net then run this again.
  pause
  exit /b 1
)

set GRADLE_VER=8.12
set GRADLE_DIR=%~dp0.gradle-local\gradle-%GRADLE_VER%

if not exist "%GRADLE_DIR%\bin\gradle.bat" (
  echo Downloading Gradle %GRADLE_VER% ...
  mkdir "%~dp0.gradle-local" 2>nul
  powershell -NoProfile -Command "[Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest https://services.gradle.org/distributions/gradle-%GRADLE_VER%-bin.zip -OutFile '%~dp0.gradle-local\gradle.zip'"
  powershell -NoProfile -Command "Expand-Archive -Force '%~dp0.gradle-local\gradle.zip' '%~dp0.gradle-local'"
)

echo Building the mod (first time takes a few minutes) ...
call "%GRADLE_DIR%\bin\gradle.bat" build
if errorlevel 1 (
  echo BUILD FAILED - copy the error above and send it to me.
  pause
  exit /b 1
)

echo.
echo DONE! Your mod is here: %~dp0build\libs\paybuttons-1.0.0.jar
explorer "%~dp0build\libs"
pause
