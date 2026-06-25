@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0simulator-missing-tool.ps1"
