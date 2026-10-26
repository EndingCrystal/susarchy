@echo off
setlocal
cd /d "%~dp0"
set "JAVA_HOME=C:\Users\85198\AppData\Roaming\runtime\java-runtime-delta"
set "PATH=%JAVA_HOME%\bin;%PATH%"
call "%~dp0gradlew.bat" build --offline --no-daemon --console=plain %*
if errorlevel 1 exit /b %errorlevel%
echo.
echo Built:
dir /b "%~dp0build\libs\susarchy-*.jar"
exit /b 0
