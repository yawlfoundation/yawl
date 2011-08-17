if not exist "$tomcat_home" goto end
if exist "$tomcat_home\webapps\yawl.war" goto end
copy "$INSTALL_PATH\*.war" "$tomcat_home\webapps"

:end






