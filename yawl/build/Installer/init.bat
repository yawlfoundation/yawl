if not exist "$tomcat_home" goto end
if exist "$tomcat_home\webapps\yawl.war" goto end
copy "$INSTALL_PATH\library\*" "$tomcat_home\common\lib\"
copy "$INSTALL_PATH\*.war" "$tomcat_home\webapps"
copy "$INSTALL_PATH\*-servlet.xml" "$tomcat_home\common\classes"
copy "$INSTALL_PATH\*.properties" "$tomcat_home\common\classes"


:end






