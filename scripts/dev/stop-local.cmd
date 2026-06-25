@echo off
setlocal
pushd "%~dp0..\.."
docker compose down
popd
