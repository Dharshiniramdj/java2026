@echo off
setlocal enabledelayedexpansion
title QueueLess - One-Click Application Launcher
echo =====================================================================
echo           QueueLess Intelligent Queue Management System
echo           SIMATS Engineering - Capstone Project Review 1
echo =====================================================================
echo.

:: 1. Search and detect Java 17
set "JAVA_CMD=java"
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
)

if not exist "%JAVA_CMD%" (
    for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-17*" "C:\Program Files\Java\jdk-17*" "C:\Program Files\Java\jdk-21*" "C:\Program Files (x86)\Java\jdk-17*") do (
        if exist "%%D\bin\java.exe" (
            set "JAVA_CMD=%%D\bin\java.exe"
            set "JAVA_HOME=%%D"
            goto :JavaFound
        )
    )
)

:JavaFound
echo [OK] Using Java Runtime: %JAVA_CMD%
"%JAVA_CMD%" -version
echo.

:: 2. Check if JAR exists, else build it
set "JAR_PATH=target\queueless-1.0.0.jar"
if not exist "%JAR_PATH%" (
    echo [*] Executable JAR not found. Building with Maven...
    call "..\apache-maven-3.9.9\bin\mvn.cmd" -DskipTests package
)

if not exist "%JAR_PATH%" (
    echo [ERROR] Build failed or JAR file missing.
    pause
    exit /b 1
)

echo.
echo [*] Starting QueueLess Spring Boot Application on http://localhost:8080 ...
echo [*] Embedded H2 Database: ./data/queueless.mv.db
echo [*] Default Demo Accounts:
echo     - Customer : customer@demo.com / Customer@123
echo     - Staff    : staff@demo.com    / Staff@123
echo     - Admin    : admin@demo.com    / Admin@123
echo.

start "QueueLess Server" "%JAVA_CMD%" -jar "%JAR_PATH%"

echo [*] Waiting 6 seconds for server initialization...
timeout /t 6 /nobreak >nul

echo [*] Launching QueueLess in default web browser...
start http://localhost:8080

echo.
echo =====================================================================
echo QueueLess is running at http://localhost:8080
echo Close the "QueueLess Server" window or run STOP_QUEUELESS.bat to exit.
echo =====================================================================
pause
