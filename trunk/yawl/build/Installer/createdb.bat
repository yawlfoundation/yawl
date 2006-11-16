c:\postgres\bin\psql.exe -l -U postgres | find "yawl"
if errorlevel 1 c:\postgres\bin\createdb.exe yawl -U postgres