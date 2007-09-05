/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.rsInterface;

import au.edu.qut.yawl.engine.interfce.Interface_Client;
import au.edu.qut.yawl.util.JDOMConversionTools;

import java.io.IOException;

import org.jdom.Element;

/**
 * An interface to be used by clients that want to converse with the Resource Service.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  14/08/2007
 */

public class ResourceInterfaceClient extends Interface_Client {

    private String _serviceURI ;

    public ResourceInterfaceClient(String uri) {
        _serviceURI = uri ;
    }


    public Element getConstraints() throws IOException {
        return performGet("getResourceConstraints");
    }

    public Element getFilters() throws IOException {
        return performGet("getResourceFilters") ;
    }

    public Element getAllocators() throws IOException {
        return performGet("getResourceAllocators") ;
    }

    public Element getAllSelectors() throws IOException {
        return performGet("getAllSelectors") ;
    }

    public String getAllParticipantNames() throws IOException {
        return executeGet(_serviceURI + "&action=getAllParticipantNames") ;
    }

    public String getAllRoleNames() throws IOException {
        return executeGet(_serviceURI + "&action=getAllRoleNames") ;
    }

    public Element getParticipants() throws IOException {
        return performGet(_serviceURI + "&action=getParticipants") ;
    }

    public Element getRoles() throws IOException {
        return performGet(_serviceURI + "&action=getRoles") ;
    }

    private Element performGet(String action) throws IOException {
        String resultStr = executeGet(_serviceURI + "&action=" + action) ;
        return JDOMConversionTools.stringToElement(resultStr) ;
    }
    
}
