@REM @echo off
@REM echo ========================================
@REM echo Building HSB Backend...
@REM echo ========================================

@REM cd /d "%~dp0"

@REM REM Clean and build the project
@REM call mvn clean package -DskipTests

@REM IF %ERRORLEVEL% NEQ 0 (
@REM     echo.
@REM     echo ========================================
@REM     echo BUILD FAILED!
@REM     echo ========================================
@REM     pause
@REM     exit /b 1
@REM )

@REM echo.
@REM echo ========================================
@REM echo Build successful! Deploying to WildFly...
@REM echo ========================================

@REM REM Copy WAR to WildFly deployments
@REM copy /Y "target\HSB.war" "C:\wildfly-33.0.2.Final\standalone\deployments\"

@REM IF %ERRORLEVEL% NEQ 0 (
@REM     echo.
@REM     echo ========================================
@REM     echo DEPLOYMENT COPY FAILED!
@REM     echo ========================================
@REM     pause
@REM     exit /b 1
@REM )

@REM echo.
@REM echo ========================================
@REM echo Deployment complete!
@REM echo HSB.war copied to WildFly deployments
@REM echo ========================================
@REM pause
