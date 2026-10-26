@echo off
setlocal
cd /d "%~dp0"
set "JAVA_HOME=C:\Users\85198\AppData\Roaming\runtime\java-runtime-delta"
set "PATH=%JAVA_HOME%\bin;%PATH%"
if not exist "%~dp0run-gametest" mkdir "%~dp0run-gametest"
if not exist "%~dp0run-gametest\eula.txt" (
    > "%~dp0run-gametest\eula.txt" echo eula=true
)
if not exist "%~dp0build\moddev" mkdir "%~dp0build\moddev"
(
    echo assets_root=C:/Users/85198/Downloads/PCL2/.minecraft/assets
    echo asset_index=17
) > "%~dp0build\moddev\minecraft_assets.properties"
call "%~dp0gradlew.bat" runGameTestServer --no-daemon --offline --console=plain -x downloadAssets -PlocalNeoForgeConfig="%~dp0local-neoforge\net\neoforged\neoforge\21.1.234\neoforge-21.1.234-moddev-config.json" -PlocalDevLaunch="%~dp0local-neoforge\net\neoforged\DevLaunch\1.0.2\DevLaunch-1.0.2.jar"
exit /b %errorlevel%
