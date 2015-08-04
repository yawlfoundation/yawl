/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */
package au.edu.qut.yawl.engine.interfce.interfaceX;

import au.edu.qut.yawl.engine.interfce.Interface_Client;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import au.edu.qut.yawl.worklist.model.Marshaller;
import au.edu.qut.yawl.util.JDOMConversionTools;

import java.io.IOException;
import java.util.*;

import org.jdom.Element;

/**
 *  InterfaceX_ServiceSideClient posts method calls from an exception service to the
 *  YAWL engine.
 *
 *  This class is a member class of Interface X, which provides an interface
 *  between the YAWL Engine and a Custom YAWL Service that manages exception
 *  handling at the process level.
 *
 *  InterfaceB_EnvironmentBasedClient was used as a template for this class.
 *
 *  Schematic of Interface X:
 *                                          |
 *                           EXCEPTION      |                              INTERFACE X
 *                            GATEWAY       |                                SERVICE
 *                  (implements) |          |                       (implements) |
 *                               |          |                                    |
 *  +==========+   ----->   ENGINE-SIDE  ---|-->   SERVICE-SIDE  ----->   +=============+
 *  || YAWL   ||              CLIENT        |        SERVER               || EXCEPTION ||
 *  || ENGINE ||                            |                             ||  SERVICE  ||
 *  +==========+   <-----   ENGINE-SIDE  <--|---   SERVICE-SIDE  <-----   +=============+
 *                            SERVER        |         CLIENT
 *                                          |
 *  @author Michael Adams                   |
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  @version 0.8, 04/07/2006
 */

public class InterfaceX_ServiceSideClient extends Interface_Client {
    private String _backEndURIStr;


    /**
     * Constructor.
     * @param backEndURIStr the back end uri of where to find
     * the engine.  A default deployment this value is
     * http://131.181.70.9:8080/yawl/ib
     */
    public InterfaceX_ServiceSideClient(String backEndURIStr) {
        _backEndURIStr = backEndURIStr;
    }


    public String setExceptionObserver(String observerURI) throws IOException {
        HashMap params = new HashMap();
        params.put("observerURI", observerURI);
        return executePost(_backEndURIStr + "/setExceptionObserver", params);
    }


    public void removeExceptionObserver() throws IOException {
        HashMap params = new HashMap();
        executePost(_backEndURIStr + "/removeExceptionObserver", params);
    }


    public void updateWorkItemData(WorkItemRecord wir, Element data,
                                     String sessionHandle) throws IOException {
        HashMap params = new HashMap() ;
        params.put("workitemID", wir.getID());
        params.put("data", JDOMConversionTools.elementToString(data));
        params.put("sessionHandle", sessionHandle);
        executePost(_backEndURIStr + "/updateWorkItemData", params);
    }


    public void updateCaseData(String caseID, Element data, String sessionHandle)
                                                               throws IOException {
        HashMap params = new HashMap() ;
        params.put("caseID", caseID);
        params.put("data", JDOMConversionTools.elementToString(data));
        params.put("sessionHandle", sessionHandle);
        executePost(_backEndURIStr + "/updateCaseData", params);
    }


    public void forceCompleteWorkItem(WorkItemRecord wir, Element data,
                                 String sessionHandle) throws IOException {
        HashMap params = new HashMap() ;
        params.put("workitemID", wir.getID());
        params.put("data", JDOMConversionTools.elementToString(data));
        params.put("force", "true");
        params.put("sessionHandle", sessionHandle);
        executePost(_backEndURIStr + "/completeWorkItem", params);
    }


    public WorkItemRecord continueWorkItem(String workItemID, String sessionHandle)
                                                           throws IOException {
        HashMap params = new HashMap() ;
        params.put("workitemID", workItemID);
        params.put("sessionHandle", sessionHandle);
        String result = executePost(_backEndURIStr + "/continueWorkItem", params);

        // process result
        result = stripOuterElement(result);
        return Marshaller.unmarshalWorkItem(result);
    }


    public WorkItemRecord unsuspendWorkItem(String workItemID, String sessionHandle)
                                                           throws IOException {
        HashMap params = new HashMap() ;
        params.put("workitemID", workItemID);
        params.put("sessionHandle", sessionHandle);
        String result = executePost(_backEndURIStr + "/unsuspendWorkItem", params);

        // process result
        result = stripOuterElement(result);
        return Marshaller.unmarshalWorkItem(result);
    }

    public void restartWorkItem(String workItemID, String sessionHandle)
                                                           throws IOException {
        HashMap params = new HashMap() ;
        params.put("workitemID", workItemID);
        params.put("sessionHandle", sessionHandle);
        executePost(_backEndURIStr + "/restartWorkItem", params);
    }


    public void startWorkItem(String workItemID, String sessionHandle)
                                                           throws IOException {
        HashMap params = new HashMap() ;
        params.put("workitemID", workItemID);
        params.put("sessionHandle", sessionHandle);
        executePost(_backEndURIStr + "/restartWorkItem", params);
    }


    public void cancelWorkItem(String workItemID, boolean fail, String sessionHandle)
                                                           throws IOException {
        HashMap params = new HashMap() ;
        params.put("workitemID", workItemID);
        params.put("fail", String.valueOf(fail));
        params.put("sessionHandle", sessionHandle);
        executePost(_backEndURIStr + "/cancelWorkItem", params);
    }

}
