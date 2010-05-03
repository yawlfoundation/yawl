/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements;

import org.jdom.Element;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 18/03/2004
 * Time: 15:10:09
 *
 * Modified for v2.1 by Michael Adams 04/09
 * 
 */
public class YAWLServiceReference implements YVerifiable {

    private String _yawlServiceID;                   // the (unique) uri
    private YAWLServiceGateway _webServiceGateway;
    private String _documentation;
    private String _serviceName ;
    private String _servicePassword;
    private boolean _assignable = true ;            // default: can be assigned to a task

    /********************************************/

    public YAWLServiceReference() {}

    public YAWLServiceReference(String serviceID, YAWLServiceGateway webGateway) {
        _yawlServiceID = serviceID;
        _webServiceGateway = webGateway;
    }

    public YAWLServiceReference(String serviceID, YAWLServiceGateway webGateway,
                                String serviceName) {
        this(serviceID, webGateway);
        _serviceName = serviceName ;
    }


    public YAWLServiceReference(String serviceID, YAWLServiceGateway webGateway,
                                String serviceName, String servicePassword) {
        this(serviceID, webGateway, serviceName);
        setServicePassword(servicePassword) ;
    }


    public YAWLServiceReference(String serviceID, YAWLServiceGateway webGateway,
                                String serviceName, String servicePassword, String doco) {
        this(serviceID, webGateway, serviceName, servicePassword);
        _documentation = doco ;
    }

    /*******************************************/

    public String getDocumentation() { return _documentation; }

    public void setDocumentation(String id) { _documentation = id; }

    public String getServiceID() { return _yawlServiceID; }

    public String getServiceName() { return _serviceName; }

    public void setServiceName(String name) { _serviceName = name; }

    public String getServicePassword() { return _servicePassword; }

    public void setServicePassword(String password) {
        _servicePassword = password;
    }

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

    public String get_serviceName() { return _serviceName; }

    public void set_serviceName(String name) { _serviceName = name; }

    public String get_servicePassword() { return _servicePassword; }

    public void set_servicePassword(String password) { _servicePassword = password; }

    public void set_assignable(boolean b) { _assignable = b ; }

    public boolean get_assignable() { return _assignable ; }


    /***************************************************/


    public List<YVerificationMessage> verify() {
        List<YVerificationMessage> messages = new ArrayList<YVerificationMessage>();
        if (YEngine.isRunning()) {
            YEngine engine = YEngine.getInstance();
            YAWLServiceReference service = engine.getRegisteredYawlService(_yawlServiceID);
            if (service == null) {
                messages.add(new YVerificationMessage(
                                this,
                                "YAWL service[" + _yawlServiceID + "] " +
                                 (_webServiceGateway != null
                                     ? "at WSGateway[" + _webServiceGateway.getID() + "] "
                                     : " ") + "is not registered with engine.",
                                YVerificationMessage.WARNING_STATUS));
            }
        }
        return messages;
    }


    // called during spec output to xml
    public String toXML() {
        StringBuilder result = new StringBuilder();
        result.append("<yawlService id=\"").append(_yawlServiceID).append("\">");
        if (_documentation != null) {
            result.append("<documentation>").append(_documentation)
                  .append("</documentation>");
        }
        result.append("</yawlService>");
        return result.toString();
    }

    
    // called when adding, editing, or getting service across interfaces A & B
    public String toXMLComplete() {
        String simpleXML = toXML();

        // trim off closing tag
        simpleXML = simpleXML.substring(0, simpleXML.lastIndexOf("<"));
        
        StringBuilder result = new StringBuilder(simpleXML);
        result.append(StringUtil.wrap(_serviceName, "servicename"));
        result.append(StringUtil.wrap(_servicePassword, "servicepassword"));
        result.append(StringUtil.wrap(String.valueOf(_assignable), "assignable"));
        result.append(StringUtil.wrap(_servicePassword, "password"));
        result.append("</yawlService>");
        return result.toString();
    }


    /**
     * Returns a YAWL service from XML (if valid).
     * @param serialisedService
     * @return
     */
    public static YAWLServiceReference unmarshal(String serialisedService){
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
        try {
            URI uri = new URI(getURI());
            return uri.getScheme();
        }
        catch (URISyntaxException e) {
            return null;
        }
    }
}
