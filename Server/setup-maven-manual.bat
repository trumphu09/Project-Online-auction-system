@echo off
REM Maven Auto-Setup Script for Windows
REM This script downloads and installs Maven

setlocal enabledelayedexpansion

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║        Maven Auto-Setup Script for Windows               ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

REM Check if Maven already installed
mvn -version >nul 2>&1
if not errorlevel 1 (
    echo ✅ Maven is already installed!
    mvn -version
    exit /b 0
)

echo ❌ Maven not found. Please install manually:
echo.
echo 📝 Installation Steps:
echo    1. Go to: https://maven.apache.org/download.cgi
echo    2. Download: apache-maven-3.9.6-bin.zip (or latest)
echo    3. Extract to: C:\maven
echo    4. Add to PATH: C:\maven\apache-maven-3.9.6\bin
echo.
echo 🔗 Quick Install (if PowerShell available):
echo    powershell.exe -ExecutionPolicy Bypass -Command ^
   "$client = new-object System.Net.WebClient; ^
    $client.DownloadFile('https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip', 'C:\maven.zip'); ^
    Expand-Archive -Path C:\maven.zip -DestinationPath C:\maven"
echo.
echo 📌 After installation, run:
echo    cd C:\Users\admin\Documents\Project-Online-auction-system\Server
echo    mvn test -Dtest=DatabaseOperationTest
echo.
pause
