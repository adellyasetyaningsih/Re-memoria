@echo off
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
echo ===================================================
echo [1/3] Compiling and packaging APK...
echo ===================================================
call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Gradle assembleDebug failed.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ===================================================
echo [2/3] Installing APK directly to device via ADB...
echo ===================================================
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] ADB install failed. Please check if your device screen is unlocked.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ===================================================
echo [3/3] Launching App...
echo ===================================================
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" shell am start -n com.example.finalproject/.ui.splash.SplashActivity
echo.
echo [DONE] App launched successfully!
