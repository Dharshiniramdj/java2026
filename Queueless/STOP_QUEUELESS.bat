@echo off
title QueueLess - Stop Application
echo =====================================================================
echo                 Stopping QueueLess Server
echo =====================================================================
echo.

echo [*] Finding process listening on port 8080...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080" ^| findstr "LISTENING"') do (
    set "PID=%%a"
    echo [*] Terminating process PID !PID! ...
    taskkill /F /PID !PID! >nul 2>&1
)

echo.
echo [OK] QueueLess server on port 8080 stopped successfully.
pause
