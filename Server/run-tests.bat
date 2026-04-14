@echo off
REM ==========================================
REM AUCTION SYSTEM - Test Runner Script
REM ==========================================

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║     AUCTION SYSTEM - TEST DATABASE SETUP & RUN            ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

REM Check if Maven is installed
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Maven not found. Installing Maven...
    REM Download and setup Maven (simplified - manual path needed)
    echo Please install Maven manually from: https://maven.apache.org/download.cgi
    echo Add Maven bin folder to Windows PATH environment variable
    pause
    exit /b 1
)

echo ✅ Maven found!
echo.

REM Go to Server directory
cd /d "C:\Users\admin\Documents\Project-Online-auction-system\Server"

echo 🔨 Step 1: Cleaning and compiling project...
mvn clean compile -q
if errorlevel 1 (
    echo ❌ Compilation failed!
    pause
    exit /b 1
)
echo ✅ Compilation successful!
echo.

echo 🧪 Step 2: Running database tests...
echo ────────────────────────────────────────────────────────────
mvn test -Dtest=DatabaseOperationTest
echo ────────────────────────────────────────────────────────────
echo.

echo ✅ Test execution completed!
echo.
pause
