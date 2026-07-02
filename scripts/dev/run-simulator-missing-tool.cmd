@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0simulator.ps1" -Scenario missing-tool
