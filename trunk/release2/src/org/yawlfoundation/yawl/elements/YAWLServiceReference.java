/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.util.YVerificationMessage;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 18/03/2004
 * Time: 15:10:09
 * 
 */
public class YAWLServiceReference implements YVerifiable{
    //todo split this class into two - one "partner link class with wsdlLoc and opName and more
    //todo another class for a registered yawl service
    public String _yawlServiceID;
    private YAWLServiceGateway _webServiceGateway;
    public String _documentation;
    private String _serviceName ;
    public boolean _assignable = true ;            // default: can be assigned to a task

    /*****************************
      INSERTED FOR PERSISTANCE
     */
    public YAWLServiceReference() {
    }

    public void set_yawlServiceID(String id) {
	this._yawlServiceID = id;
    }

    public String get_yawlServiceID() {
	return _yawlServiceID;
    }

    public void set_documentation(String id) {
	this._documentation = id;
    }

    public String get_documentation() {
	return _documentation;
    }

    public String get_serviceName() {
        return _serviceName;
    }

    public void set_serviceName(String name) {
        _serviceName = name;
    }

    public void set_assignable(boolean b) { _assignable = b ; }

    public boolean get_assignable() { return _assignable ; }

    public boolean isAssignable() { return _assignable ; }

    /***************************************/

    public YAWLServiceReference(String yawlServiceID, YAWLServiceGateway webServiceGateway) {
        this._yawlServiceID = yawlServiceID;
        this._webServiceGateway = webServiceGateway;
    }

    public YAWLServiceReference(String yawlServiceID, YAWLServiceGateway webServiceGateway,
                                String serviceName) {
        this(yawlServiceID, webServiceGateway);
        _serviceName = serviceName ;
    }

    public String getURI() {
        return _yawlServiceID;
    }

    /**
     * AJH: Extended message to conditionally include WSGateway ID.
     */ 
    public List verify() {
        List messages = new ArrayList();
        YEngine engine = YEngine.getInstance();
        YAWLServiceReference service = engine.getRegisteredYawlService(_yawlServiceID);
        if(service == null){
            messages.add(
                    new YVerificationMessage(
                            this,
                            "YAWL service[" + _yawlServiceID + "] " +
                            (_webServiceGateway != null
                             ? "at WSGateway[" + _webServiceGateway.getID() + "] "
                             : " ") + "is not registered with engine.",
                            YVerificationMessage.WARNING_STATUS));
        }
        return messages;
    }

    // called during spec output to xml
    public String toXML() {
        StringBuffer result = new StringBuffer();
        result.append("<yawlService id=\"").append(_yawlServiceID).append("\">");
        if (_documentation != null) {
            result.append("<documentation>").append(_documentation)
                  .append("</documentation>");
        }
        result.append("</yawlService>");
        return result.toString();
    }

    // called when adding, editing, or getting serivce across interfaces A & B
    public String toXMLComplete() {
        // trim off closing tag
        String simpleXML = toXML();
        simpleXML = simpleXML.substring(0, simpleXML.lastIndexOf("<"));
        
        StringBuffer result = new StringBuffer(simpleXML);
        result.append("<servicename>").append(_serviceName).append("</servicename>");
        result.append("<assignable>").append(_assignable).append("</assignable>");
        result.append("</yawlService>");
        return result.toString();
    }


    /**
     * Returns a YAWL service from XML (if valid).
     * @param serialisedService
     * @return
     */
    public static YAWLServiceReference unmarshal(String serialisedService){
        SAXBuilder builder = new SAXBuilder();
        try {
            Document doc = builder.build(new StringReader(serialisedService));
            String uri = doc.getRootElement().getAttributeValue("id");
            String name = doc.getRootElement().getChildText("servicename");
            String docStr = doc.getRootElement().getChildText("documentation");
            String assignable = doc.getRootElement().getChildText("assignable");
            YAWLServiceReference service =  new YAWLServiceReference(uri, null, name);
            service.setDocumentation(docStr);
            if (assignable != null)
                service.set_assignable(assignable.equalsIgnoreCase("true"));
            return service;
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    /**
     * gets the documentation
     * @return
     */
    public String getDocumentation() {
        return _documentation;
    }

    /**
     * sets the documentation
     * @param documentation
     */
    public void setDocumentation(String documentation) {
        this._documentation = documentation;
    }


    public boolean canBeAssignedToTask() { return _assignable ; }


    /**
     * Returns the scheme (i.e. the protocol) component of the URI (assuming standard URI format).
     * If no protocol defined, Null is returned.<P>
     *
     * @return  The scheme component of the URI
     */
    public String getScheme()
    {
        try
        {
            URI uri = new URI(getURI());
            return uri.getScheme();
        }
        catch (URISyntaxException e)
        {
            return null;
        }
    }
}
