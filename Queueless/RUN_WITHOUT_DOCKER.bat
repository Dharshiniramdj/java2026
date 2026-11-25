@echo off
cd /d "%~dp0"
if not exist data mkdir data
where java >nul 2>&1 || (echo Java not found. Install JDK 17 and reopen terminal. & pause & exit /b 1)
where mvn >nul 2>&1 || (echo Maven not found. Install Maven and reopen terminal. & pause & exit /b 1)
echo Starting QueueLess at http://localhost:8080
mvn spring-boot:run
pause
