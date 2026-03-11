@echo off
setlocal

set BASE_URL=%1
if "%BASE_URL%"=="" set BASE_URL=http://localhost:8080/api

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0seed-and-verify.ps1" -BaseUrl "%BASE_URL%"
if errorlevel 1 (
  echo Integration seed/verify failed.
  exit /b 1
)

echo Integration seed/verify finished successfully.
exit /b 0
