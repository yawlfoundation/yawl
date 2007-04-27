###HOW TO BUILD THE INSTALLER FOR YAWL BETA 8.2###

In order to build the installer, you need the following files to be copied in folder build\Installer:

- admintool.war
- PDFforms.war
- timeService.war
- workletService.war
- worklist.war
- yawl.war
- yawlSMSInvoker.war
- yawlWSInvoker.war
- yawlXForms.war

(download them from http://sourceforge.net/project/yawl - YAWL Engine/Beta 8.2/YAWLBeta8.2-Executable-WebServerVersion.zip or build from scratch)

- YAWLBeta8.2-Standalone.jar (download from http://sourceforge.net/project/yawl - YAWL Engine/Beta8.2/YAWLBeta8.2_Standalone.jar or build from scratch)
- YAWLEditorLite1.4.5.jar (download from http://sourceforge.net/project/yawl - YAWL Editor/1.4.5/YAWLEditorLite1.4.5.jar or build from scarch)

- apache-tomcat-5.5.23.exe (download from http://tomcat.apache.org)
- postgresql-8.0-int.msi   (download from http://www.postgresql.org)

Then build the target "create_standard_installer" of file build\installer.xml. This will create "installer.jar" in folder build\Installer.
You can then use Launch4j to give the icon "yawl.ico" to the installer. You can find the icon in build\Installer\icons.


@ 2007, The YAWL Foundation
Last update: 27/04/2007 - Marcello La Rosa