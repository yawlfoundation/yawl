/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */


package org.yawlfoundation.yawl.authentication;

import org.jdom.Element;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.PasswordEncryptor;


/**
 * A simple class used to store the credentials of an external application that connects
 * to the Engine via the various interfaces (as opposed to a custom service).
 *
 * Note that the generic user "admin" is represented by an instance of this class
 *
 * @author Michael Adams
 * Date: 23/11/2009
 *
 */

public class YExternalClient {

    String _userID;
    String _password;
    String _documentation;


    public YExternalClient() {}

    public YExternalClient(String userID, String password, String documentation) {
        _userID = userID;
        setPassword(password);
        _documentation = documentation;
    }

    public YExternalClient(Element xml) {
        _userID = xml.getChildText("username");
        setPassword(xml.getChildText("password"));
        _documentation = xml.getChildText("documentation");
    }

    
    public String getUserID() { return _userID; }

    public void setUserID(String userID) { _userID = userID; }


    public String getPassword() { return _password; }

    public void setPassword(String password) {
        _password = PasswordEncryptor.encrypt(password, null);
    }


    public String getDocumentation() { return _documentation; }

    public void setDocumentation(String documentation) { _documentation = documentation; }


    public String toXML() {
        StringBuilder xml = new StringBuilder("<client>");
        xml.append(StringUtil.wrap(_userID, "username"))
           .append(StringUtil.wrap(_password, "password"))
           .append(StringUtil.wrap(_documentation, "documentation"))
           .append("</client>");
        return xml.toString();
    }


    // For JSF table

    public String get_userid() { return _userID; }

    public String get_documentation() { return _documentation; }



}