$INSTALL_PATH\postgres\bin\psql.exe -l -U postgres | find "yawl"
if errorlevel 1 $INSTALL_PATH\postgres\bin\createdb.exe yawl -U postgres