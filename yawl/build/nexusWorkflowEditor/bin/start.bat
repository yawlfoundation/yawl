@echo off
setlocal
rem
rem Wrapper script to run [JavaApplicationWithMainMethod]
rem
if "%OS%"=="Windows_NT" goto nt
echo This script only works with NT-based versions of Windows.
goto :eof

:nt
rem
rem Find the application home.
rem
rem %~dp0 is location of current script under NT
set _REALPATH=%~dp0
cd ..
set APPLICATION_HOME=%cd%

echo %APPLICATION_HOME%
echo _REALPATH == %_REALPATH%

if exist "%APPLICATION_HOME%\bin\setClasspath.bat" call "%APPLICATION_HOME%\bin\setClasspath.bat"
set CLASSPATH=%LIB_CLASSPATH%
echo LIB_CLASSPATH = %LIB_CLASSPATH%

echo CLASSPATH = %CLASSPATH%

rem Get remaining unshifted command line arguments and save them in the
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs

:doneSetArgs
rem
rem Start java program
rem
:startup
javaw -cp %CLASSPATH% com.nexusbpm.editor.WorkflowEditor %CMD_LINE_ARGS%
rem The setlocal command also sets the errorlevel value
rem when it is passed an argument.
rem The errorlevel value is set to zero (0) if one of the
rem two valid arguments is given and set to one (1)
rem otherwise.
rem if not errorlevel 1 goto :eof
:eof