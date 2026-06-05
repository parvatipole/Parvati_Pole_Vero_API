@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup script for Windows.
@REM ----------------------------------------------------------------------------

@echo off
@setlocal

set ERROR_CODE=0

@REM Resolve the project base directory
set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_PROPERTIES="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"

@REM Locate Java
if not "%JAVA_HOME%"=="" goto OkJHome
set JAVACMD=java.exe
goto OkJavaCmd

:OkJHome
set JAVACMD="%JAVA_HOME%\bin\java.exe"
if not exist %JAVACMD% (
    echo Error: JAVA_HOME is set to an invalid directory: "%JAVA_HOME%" >&2
    set ERROR_CODE=1
    goto End
)

:OkJavaCmd

@REM Download the wrapper jar if it is missing
if exist %WRAPPER_JAR% goto RunWrapper

for /f "tokens=2 delims==" %%a in ('findstr /b "wrapperUrl=" %WRAPPER_PROPERTIES%') do set WRAPPER_URL=%%a
powershell -NoProfile -Command "& { Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile %WRAPPER_JAR% }"

:RunWrapper
%JAVACMD% %MAVEN_OPTS% -classpath %WRAPPER_JAR% "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
if ERRORLEVEL 1 set ERROR_CODE=%ERRORLEVEL%

:End
@endlocal & set ERROR_CODE=%ERROR_CODE%
exit /B %ERROR_CODE%
