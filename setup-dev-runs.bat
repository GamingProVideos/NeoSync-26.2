@echo off
setlocal
cd /d "%~dp0"
echo Regenerating NeoForge / ModDevGradle IDE run files...
call gradlew.bat neoForgeIdeSync prepareClientRun
if errorlevel 1 (
  echo.
  echo Failed to regenerate the run files.
  exit /b 1
)

echo.
if exist "build\moddev\clientRunVmArgs.txt" (
  echo OK: build\moddev\clientRunVmArgs.txt
) else (
  echo ERROR: build\moddev\clientRunVmArgs.txt was not generated.
  exit /b 2
)
if exist "build\moddev\clientRunProgramArgs.txt" (
  echo OK: build\moddev\clientRunProgramArgs.txt
) else (
  echo ERROR: build\moddev\clientRunProgramArgs.txt was not generated.
  exit /b 3
)

echo.
echo Refresh the Gradle project in IntelliJ, then run the newly generated Minecraft Client configuration.
endlocal
