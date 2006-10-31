/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.util.YVerificationMessage;

 /**
 * 
 * @author Lachlan Aldred
 * Date: 18/03/2004
 * Time: 15:10:09
 * 
 */
@Entity
public class YAWLServiceReference implements YVerifiable, Serializable {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
	
    //todo split this class into two - one "partner link class with wsdlLoc and opName and more
    //todo another class for a registered yawl service
    public String _yawlServiceID;
    private YAWLServiceGateway _webServiceGateway;
    public String _documentation;

    /*****************************
      INSERTED FOR PERSISTANCE
     */
    public YAWLServiceReference() {
    }

    @ManyToOne(fetch = FetchType.EAGER)
    public YAWLServiceGateway getYawlServiceGateway() {
    	return _webServiceGateway;
    }
    public void setYawlServiceGateway(YAWLServiceGateway serviceGateway) {
    	_webServiceGateway = serviceGateway;
    }

    public void setYawlServiceID(String id) {
	this._yawlServiceID = id;
    }

    private boolean enabled = true;
    
    @Basic
    public boolean getEnabled() {
    	return enabled;
    }
    public void setEnabled(boolean enablement) {
    	enabled = enablement;
    }
    
    @Id
    public String getYawlServiceID() {
    	return _yawlServiceID;
    }
    
    /***************************************/

    public YAWLServiceReference(String yawlServiceID, YAWLServiceGateway webServiceGateway) {
        this._yawlServiceID = yawlServiceID;
        this._webServiceGateway = webServiceGateway;
    }


    @Transient
    public String getURI() {
        return _yawlServiceID;
    }

    /**
     * AJH: Extended message to conditionally include WSGateway ID.
     */ 
    public List<YVerificationMessage> verify() {
        List<YVerificationMessage> messages = new ArrayList<YVerificationMessage>();
        AbstractEngine engine = null;
        try {
		engine = EngineFactory.createYEngine();
	  } catch (Exception e) {
            messages.add(new YVerificationMessage(this,"CANNOT ACCESS ENGINE",YVerificationMessage.WARNING_STATUS));
		return messages;
	  }
        YAWLServiceReference service = engine.getRegisteredYawlService(_yawlServiceID);
        if(service == null){
            messages.add(
                    new YVerificationMessage(
                            this,
                            "YAWL service[" + _yawlServiceID + "] " +
                            (_webServiceGateway != null
                             ? "at WSGateway[" + _webServiceGateway.getId() + "] "
                             : " ") + "is not registered with engine.",
                            YVerificationMessage.WARNING_STATUS));
        }
        return messages;
    }

    public String toXML() {
        StringBuffer result = new StringBuffer();
        result.append("<yawlService id=\"" + _yawlServiceID + "\">");
        if(_documentation != null) {
            result.append("<documentation>");
            result.append(_documentation);
            result.append("</documentation>");
        }
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
            String docStr = doc.getRootElement().getChildText("documentation");
            YAWLServiceReference service =  new YAWLServiceReference(uri, null);
            service.setDocumentation(docStr);
            return service;
        } catch (JDOMException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return null;
    }



    /**
     * gets the documentation
     */
    @Column(name="documentation")
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


    /**
     * Returns the scheme (i.e. the protocol) component of the URI (assuming standard URI format).
     * If no protocol defined, Null is returned.<P>
     *
     * @return  The scheme component of the URI
     */
    @Transient
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
    
    public boolean equals(Object o) {
    	if (!(o instanceof YAWLServiceReference)) {
    		return false;
    	}
    	YAWLServiceReference check = (YAWLServiceReference) o;
    	if (check.getURI().equals(getURI())) {
    		return true;
    	}
    	return false;
    }
}

