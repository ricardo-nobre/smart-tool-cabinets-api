@echo off
setlocal
echo This will remove the local PostgreSQL Docker volume.
set /p CONFIRM=Type RESET to continue: 
if /I not "%CONFIRM%"=="RESET" (
  echo Cancelled.
  exit /b 1
)
pushd "%~dp0..\.."
docker compose down -v
docker compose up -d
popd
