@echo off
where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  mvn %*
  exit /b %ERRORLEVEL%
)
echo Maven is required to use this lightweight wrapper. Install Maven or run this project in Render's Java environment. 1>&2
exit /b 1
