@echo off
echo ========================================
echo   Quiz Leaderboard — Build and Run
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH.
    echo Please install Java 11+ from https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

REM Check if javac is available
javac -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: javac not found. You may have JRE instead of JDK.
    echo Please install JDK 11+ from https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

echo [1/2] Compiling QuizLeaderboard.java ...
javac QuizLeaderboard.java
if %errorlevel% neq 0 (
    echo ERROR: Compilation failed. Check the source file.
    pause
    exit /b 1
)
echo       Compilation successful!
echo.

echo [2/2] Running QuizLeaderboard ...
echo       (This will take ~50 seconds due to 5s delays between 10 polls)
echo.
java QuizLeaderboard

echo.
echo ========================================
echo   Done! Check output above for result.
echo ========================================
pause
