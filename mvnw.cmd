@REM --------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.
@REM --------------------------------------------------------------------
@REM Maven start up batch script
@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir
@REM --------------------------------------------------------------------

@echo off
@setlocal

set WRAPPER_JAR="%~dp0\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

if not exist %WRAPPER_JAR% (
    echo Downloading Maven Wrapper...
    powershell -Command "Invoke-WebRequest -Uri %WRAPPER_URL% -OutFile %WRAPPER_JAR%"
)

"%JAVA_HOME%\bin\java.exe" %MAVEN_OPTS% ^
  -jar %WRAPPER_JAR% %*
