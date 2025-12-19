#!/bin/bash
# Exam Scheduler Launcher for Mac
# Double-click this file to run the application

echo "========================================"
echo "   Exam Scheduler Launcher"
echo "========================================"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo ""
    echo "Please install Java 21 or higher:"
    echo "  Mac: brew install openjdk@21"
    echo "  Or download from: https://adoptium.net/"
    echo ""
    read -p "Press Enter to exit..."
    exit 1
fi

echo "Java detected successfully!"
echo ""
echo "Starting Exam Scheduler..."
echo "This may take a moment on first run..."
echo ""

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Navigate to examschd directory and run the application
cd "$SCRIPT_DIR/examschd" || {
    echo "ERROR: Could not find examschd directory"
    echo "Please make sure the project structure is intact"
    read -p "Press Enter to exit..."
    exit 1
}

# Run the application using Gradle
./gradlew run

exit_code=$?

if [ $exit_code -ne 0 ]; then
    echo ""
    echo "========================================"
    echo "Application exited with an error"
    echo "========================================"
    read -p "Press Enter to exit..."
    exit $exit_code
fi

echo ""
echo "Application closed successfully"
echo ""
read -p "Press Enter to close this window..."
