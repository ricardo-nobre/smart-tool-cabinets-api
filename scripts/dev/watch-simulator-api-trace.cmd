@echo off
setlocal
if not exist "%~dp0simulator-api-trace.log" type nul > "%~dp0simulator-api-trace.log"
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-Content -Path '%~dp0simulator-api-trace.log' -Wait"
