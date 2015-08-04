/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */


package org.yawlfoundation.yawl.authentication;

import org.jdom.Element;


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

public class YExternalClient extends YClient {

    public YExternalClient() { super(); }

    public YExternalClient(String userID, String password, String documentation) {
        super(userID, password, documentation);
    }

    public YExternalClient(Element xml) {
        super(xml);
    }


    // For JSF table

    public String get_userid() { return _userName; }

    public String get_documentation() { return _documentation; }

}