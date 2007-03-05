@echo off
if "%1"=="" goto usage
if "%2"=="" goto usage
if "%3"=="" goto usage
if "%4"=="" goto usage
goto run
:usage
echo JKS2PFX
echo Copyright © 2005 TJ (tj@tjworld.net)
echo The GNU General Public License version 2 or later applies.
echo http://www.gnu.org/licenses/gpl.html
echo Converts Java JKS keystore private key and public certificate to PFX format
echo which can be imported into Microsoft keystores.
echo Creates the file "certificate.pfx" in the current directory.
echo usage: JKS2PFX keystore password alias javaclasspath
echo javaclasspath = the directory where ExportPrvKey.class is
goto end
:run
set KEYSTORE=%1
set PASSWORD=%2
set ALIAS=%3
set JAVACLASSPATH=%4
set FIRSTNAME=%5
set LASTNAME=%6
set PKEY_8=privatekey.pkcs8
set PKEY_64=privatekey.b64
set CERT_64=certificate.b64
set CERT_P12=certificate.pfx
%JAVACLASSPATH%\keytool -genkey -dname "CN=%FIRSTNAME% %LASTNAME%, OU=YAWL, O=QUT, L=Brisbane, S=QLD, C=AU" -alias %ALIAS% -keystore %KEYSTORE% -keypass %PASSWORD% -storepass %PASSWORD%
%JAVACLASSPATH%\keytool -export -rfc -keystore %KEYSTORE% -storepass %PASSWORD% -alias %ALIAS% > %CERT_64%
java -classpath %JAVACLASSPATH% ExportPrvKey %KEYSTORE% %PASSWORD% %ALIAS% > %PKEY_8%
echo -----BEGIN PRIVATE KEY----- > %PKEY_64%
%JAVACLASSPATH%\openssl enc -in %PKEY_8% -a >> %PKEY_64%
echo -----END PRIVATE KEY----- >> %PKEY_64%
echo Generating new PFX Key/Certificate pair, please enter a password
%JAVACLASSPATH%\openssl pkcs12 -inkey %PKEY_64% -in %CERT_64% -out %JAVACLASSPATH%\%CERT_P12% -export -password pass:%PASSWORD%
%JAVACLASSPATH%\keytool -storepass %PASSWORD% -keystore %KEYSTORE% -export -alias %ALIAS% -file %JAVACLASSPATH%\pubcertfile.cer
del /q %PKEY_8% %PKEY_64% %CERT_64% %KEYSTORE%
echo Created new PFX key+certificate: %CERT_P12%
:end
exit