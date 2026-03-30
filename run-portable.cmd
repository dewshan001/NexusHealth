@echo off
setlocal

REM Always run from the project root so ${user.dir} points here
cd /d "%~dp0"

call mvnw.cmd spring-boot:run

endlocal
