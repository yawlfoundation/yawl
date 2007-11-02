/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.interfce;


/**
 * 
 * @author Lachlan Aldred
 * Date: 15/03/2004
 * Time: 12:11:08
 * 
 */
public class AuthenticationConfig {
    private static String userName;
    private String _password;
    private String _proxyHost;
    private String _proxyPort;


    private static AuthenticationConfig _myInstance;


    private AuthenticationConfig() {
    }


    public static boolean isSetForAuthentication() {
        return userName != null;
    }


    public static AuthenticationConfig getInstance() {
        if (_myInstance == null) {
            _myInstance = new AuthenticationConfig();
        }
        return _myInstance;
    }


    public void setProxyAuthentication(String userName, String password,
                                       String proxyHost, String proxyPort) {
        this.userName = userName;
        this._password = password;
        this._proxyHost = proxyHost;
        this._proxyPort = proxyPort;
    }


    public String getPassword() {
        return _password;
    }


    public String getProxyHost() {
        return _proxyHost;
    }


    public String getUserName() {
        return userName;
    }


    public String getProxyPort() {
        return _proxyPort;
    }
}
