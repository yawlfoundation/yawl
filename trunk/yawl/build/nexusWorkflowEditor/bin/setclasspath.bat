REM setClasspath.bat
@echo off
set LIB_CLASSPATH=classes
rem FOR %%f IN (lib\*.jar) DO echo %%f
REM ******************************************************
REM Environment variables within a FOR loop are expanded
REM at the beginning of the loop and won't change until
REM AFTER the end of the DO section.
REM Need to use the CALL :subroutine mechanism
REM ******************************************************
REM FOR %%f IN (lib\*.jar) DO (call :append_classpath "%%f")
FOR  %%f IN (lib\*.jar) DO (call :append_classpath "%%f")
echo %LIB_CLASSPATH%
:append_classpath
rem echo LIB_CLASSPATH:%LIB_CLASSPATH% - arg:%1
set LIB_CLASSPATH=%LIB_CLASSPATH%;%1
GOTO :eof