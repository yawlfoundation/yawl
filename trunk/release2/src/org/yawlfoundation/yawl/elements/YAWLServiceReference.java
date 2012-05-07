/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.elements;

import org.jdom2.Element;
import org.yawlfoundation.yawl.authentication.YClient;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;
import org.yawlfoundation.yawl.util.YVerificationHandler;

/**
 * Represents a server-side reference to a YAWL Custom Service.
 * @author Lachlan Aldred
 * @author Michael Adams (for 2.0-2.1)
 * @since 0.1
 * @date 18/03/2004
 */
public class YAWLServiceReference extends YClient implements YVerifiable {

    private String _yawlServiceID;                   // the (unique) uri
    private YAWLServiceGateway _webServiceGateway;
    private boolean _assignable = true ;            // default: can be assigned to a task

    /********************************************/

    /**
     * Constructs and empty service reference (default constructor).
     */
    public YAWLServiceReference() {}


    public YAWLServiceReference(String serviceID, YAWLServiceGateway webGateway) {
        _yawlServiceID = serviceID;
        _webServiceGateway = webGateway;
    }

    public YAWLServiceReference(String serviceID, YAWLServiceGateway webGateway,
                                String serviceName) {
        this(serviceID, webGateway);
        setUserName(serviceName);
    }


    public YAWLServiceReference(String serviceID, YAWLServiceGateway webGateway,
                                String serviceName, String servicePassword) {
        this(serviceID, webGateway, serviceName);
        setServicePassword(servicePassword) ;
    }


    public YAWLServiceReference(String serviceID, YAWLServiceGateway webGateway,
                                String serviceName, String servicePassword, String doco) {
        this(serviceID, webGateway, serviceName, servicePassword);
        setDocumentation(doco) ;
    }

    /*******************************************/


    public String getServiceID() { return _yawlServiceID; }

    public String getServiceName() { return _userName; }

    public void setServiceName(String name) { setUserName(name); }

    public String getServicePassword() { return _password; }

    public void setServicePassword(String password) { setPassword(password); }

    public void setAssignable(boolean b) { _assignable = b ; }

    public boolean isAssignable() { return _assignable ; }

    public boolean canBeAssignedToTask() { return _assignable ; }

    public String getURI() { return _yawlServiceID; }


    /***************************************************/

    // Hibernate mappings //

    public void set_yawlServiceID(String id) { _yawlServiceID = id; }

    public String get_yawlServiceID() { return _yawlServiceID; }

    public void set_documentation(String id) { _documentation = id; }

    public String get_documentation() { return _documentation; }

    public String get_serviceName() { return _userName; }

    public void set_serviceName(String name) { _userName = name; }

    public String get_servicePassword() { return _password; }

    public void set_servicePassword(String password) { _password = password; }

    public void set_assignable(boolean b) { _assignable = b ; }

    public boolean get_assignable() { return _assignable ; }


    /***************************************************/

    public boolean equals(Object other) {
        return (other instanceof YAWLServiceReference) &&
                ((getServiceID() != null) ?
                getServiceID().equals(((YAWLServiceReference) other).getServiceID()) :
                super.equals(other));
    }

    public int hashCode() {
        return (getServiceID() != null) ? getServiceID().hashCode() : super.hashCode();
    }
    

    public void verify(YVerificationHandler handler) {
        try {
            if (YEngine.isRunning()) {
                YEngine engine = YEngine.getInstance();
                YAWLServiceReference service = engine.getRegisteredYawlService(_yawlServiceID);
                if (service == null) {
                    handler.warn(this,
                            "YAWL service [" + _yawlServiceID + "] " +
                            (_webServiceGateway != null
                                    ? "at WSGateway [" + _webServiceGateway.getID() + "] "
                                    : " ") + "is not registered with engine.");
                }
            }
        }
        catch (NoClassDefFoundError e) {
            // may occur if called in standalone mode (eg. from the editor), caused by
            // the call to a static YEngine which attempts to create a
            // YPersistenceManager object - ok to ignore the verify check in these instances
        }
    }


    // called during spec output to xml
    public String toXML() {
        return toBasicXNode().toString();
    }

    
    // called when adding, editing, or getting service across interfaces A & B
    public String toXMLComplete() {
        XNode root = toBasicXNode();
        root.addChild("servicename", _userName);
        root.addChild("servicepassword", _password);
        root.addChild("assignable", _assignable);
        return root.toString();
    }

    
    public void fromXML(String xml) {
        XNode node = new XNodeParser().parse(xml);
        if (node != null) {
            _yawlServiceID = node.getAttributeValue("id");
            _documentation = node.getChildText("documentation");
            _userName = node.getChildText("servicename");
            _password = node.getChildText("servicepassword");
            String assignStr = node.getChildText("assignable");
            _assignable = (assignStr != null) && assignStr.equalsIgnoreCase("true");
        }
    }

    private XNode toBasicXNode() {
        XNode root = new XNode("yawlService");
        root.addAttribute("id", _yawlServiceID);
        if (_documentation != null) {
            root.addChild("documentation", _documentation);
        }
        return root;
    }


    /**
     * Returns a YAWL service from XML (if valid).
     * @param serialisedService
     * @return an instantiated YAWL Service, or null if the XML was invalid
     */
    public static YAWLServiceReference unmarshal(String serialisedService) {
        YAWLServiceReference service = null;
        Element serviceElem = JDOMUtil.stringToElement(serialisedService);
        if (serviceElem != null) {
            String uri = serviceElem.getAttributeValue("id");
            String name = serviceElem.getChildText("servicename");
            String password = serviceElem.getChildText("servicepassword");
            String docStr = serviceElem.getChildText("documentation");
            String assignable = serviceElem.getChildText("assignable");

            service =  new YAWLServiceReference(uri, null, name, password);
            service.setDocumentation(docStr);
            if (assignable != null)
                service.setAssignable(assignable.equalsIgnoreCase("true"));
        }
        return service;

    }


    /**
     * Returns the scheme (i.e. the protocol) component of the URI (assuming standard URI format).
     * If no protocol defined, null is returned.<P>
     *
     * @return  The scheme component of the URI
     */
    public String getScheme() {
        int pos = _yawlServiceID.indexOf(':');
        return pos > -1 ? _yawlServiceID.substring(0, pos) : null;
    }
}
