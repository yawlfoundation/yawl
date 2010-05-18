package org.yawlfoundation.yawl.authentication;

import org.jdom.Element;
import org.yawlfoundation.yawl.util.XNode;

/**
 * The base class for a YAWL external client app or service.
 * Known child classes: YExternalClient, YAWLServiceReference
 *
 * Author: Michael Adams
 * Creation Date: 11/05/2010
 */
public class YClient {

    protected String _userName;
    protected String _password;
    protected String _documentation;

    public YClient() {}

    public YClient(String userID, String password, String documentation) {
        _userName = userID;
        _password = password;
        _documentation = documentation;
    }

    public YClient(Element xml) {
        _userName = xml.getChildText("username");
        _password = (xml.getChildText("password"));
        _documentation = xml.getChildText("documentation");
    }


    public String getUserName() { return _userName; }

    public void setUserName(String userID) { _userName = userID; }


    public String getPassword() { return _password; }

    public void setPassword(String password) { _password = password; }


    public String getDocumentation() { return _documentation; }

    public void setDocumentation(String documentation) { _documentation = documentation; }


    public String toXML() {
        XNode root = new XNode("client");
        root.addChild("username", _userName);
        root.addChild("password", _password);
        root.addChild("documentation", _documentation);
        return root.toString();
    }


}
