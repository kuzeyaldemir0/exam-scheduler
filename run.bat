@echo off
REM Exam Scheduler Launcher for Windows
REM This script runs the exam scheduling application

echo ========================================
echo    Exam Scheduler Launcher
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo.
    echo Please install Java 21 or higher from:
    echo https://adoptium.net/
    echo.
    pause
    exit /b 1
)

echo Java detected successfully!
echo.
echo Starting Exam Scheduler...
echo This may take a moment on first run...
echo.

REM Navigate to examschd directory and run the application
cd examschd
if errorlevel 1 (
    echo ERROR: Could not find examschd directory
    echo Please make sure you're running this from the project root
    pause
    exit /b 1
)

REM Run the application using Gradle
call gradlew.bat run

if errorlevel 1 (
    echo.
    echo ========================================
    echo Application exited with an error
    echo ========================================
    pause
    exit /b 1
)

echo.
echo Application closed successfully
pause
