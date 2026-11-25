@echo off
setlocal enabledelayedexpansion
title QueueLess - Automated Windows Environment Setup
echo =====================================================================
echo           QueueLess Intelligent Queue Management System
echo           Automated Windows 11 Home Setup and Build Script
echo =====================================================================
echo.

:: 1. Auto-detect Java 17+
set "JAVA_FOUND="
for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-17*" "C:\Program Files\Java\jdk-17*" "C:\Program Files\Java\jdk-21*" "C:\Program Files (x86)\Java\jdk-17*") do (
    if exist "%%D\bin\java.exe" (
        set "JAVA_HOME=%%D"
        set "JAVA_CMD=%%D\bin\java.exe"
        set "JAVA_FOUND=1"
        goto :FoundJava
    )
)

if not defined JAVA_FOUND (
    where java >nul 2>&1
    if !errorlevel! equ 0 (
        set "JAVA_CMD=java"
        set "JAVA_FOUND=1"
    )
)

:FoundJava
if not defined JAVA_FOUND (
    echo [ERROR] Java 17 or higher was not detected on this machine.
    echo Please install Eclipse Adoptium Temurin 17 or Oracle JDK 17.
    pause
    exit /b 1
)

echo [OK] Detected Java Runtime at: %JAVA_CMD%
echo.

:: 2. Locate Maven
set "MVN_CMD=..\apache-maven-3.9.9\bin\mvn.cmd"
if not exist "%MVN_CMD%" (
    where mvn >nul 2>&1
    if !errorlevel! equ 0 set "MVN_CMD=mvn"
)

echo [OK] Using Maven Build Tool: %MVN_CMD%
echo.

:: 3. Run JUnit algorithm tests and build package
echo [*] Executing JUnit algorithm tests...
call "%MVN_CMD%" test
if !errorlevel! neq 0 (
    echo [ERROR] Unit tests failed. Please review compiler logs.
    pause
    exit /b 1
)

echo.
echo [*] Packaging executable standalone Spring Boot JAR...
call "%MVN_CMD%" -DskipTests package
if !errorlevel! neq 0 (
    echo [ERROR] JAR packaging failed.
    pause
    exit /b 1
)

echo.
echo =====================================================================
echo [SUCCESS] QueueLess setup and build completed successfully!
echo Executable JAR: target\queueless-1.0.0.jar
echo.
echo To run the application, double-click RUN_QUEUELESS.bat
echo =====================================================================
pause
