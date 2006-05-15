/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.interfce.interfaceD_WorkItemExecution;

import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import au.edu.qut.yawl.worklist.model.YParametersSchema;
import au.edu.qut.yawl.engine.interfce.Interface_Client;
import au.edu.qut.yawl.unmarshal.YMarshal;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import org.jdom.JDOMException;

/**
 * Sends workitem related messages to a
 *
 * 
 * @author Lachlan Aldred
 * Date: 16/09/2005
 * Time: 15:34:59
 */
public class InterfaceD_Client extends Interface_Client{

    private String _interfaceDServerURI;

    public InterfaceD_Client(String interfaceDServerURI) {
        _interfaceDServerURI = interfaceDServerURI;
    }

    public String sendWorkItem(WorkItemRecord workitem) throws IOException, JDOMException {
        Map queryMap = new HashMap();
        queryMap.put("workitem", workitem.toXML());
        return  executePost(_interfaceDServerURI + "", queryMap);
    }



}
