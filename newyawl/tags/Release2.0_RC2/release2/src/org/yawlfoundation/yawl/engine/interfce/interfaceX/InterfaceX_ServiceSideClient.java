/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */
package org.yawlfoundation.yawl.engine.interfce.interfaceX;

import org.jdom.Element;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.IOException;
import java.util.Map;

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
 *  @version 0.8, 04/07/2006
 */

public class InterfaceX_ServiceSideClient extends Interface_Client {
    private String _backEndURIStr;


    /**
     * Constructor.
     * @param backEndURIStr the back end uri of where to find
     * the engine.  A default deployment this value is
     * http://localhost:8080/yawl/ib
     */
    public InterfaceX_ServiceSideClient(String backEndURIStr) {
        _backEndURIStr = backEndURIStr;
    }


    public String setExceptionObserver(String observerURI) throws IOException {
        Map<String, String> params = prepareParamMap("setExceptionObserver", null);
        params.put("observerURI", observerURI);
        return executePost(_backEndURIStr, params);
    }


    public void removeExceptionObserver() throws IOException {
        Map<String, String> params = prepareParamMap("removeExceptionObserver", null);
        executePost(_backEndURIStr, params);
    }


    public void updateWorkItemData(WorkItemRecord wir, Element data,
                                     String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("updateWorkItemData", sessionHandle);
        params.put("workitemID", wir.getID());
        params.put("data", JDOMUtil.elementToString(data));
        executePost(_backEndURIStr, params);
    }


    public void updateCaseData(String caseID, Element data, String sessionHandle)
                                                               throws IOException {
        Map<String, String> params = prepareParamMap("updateCaseData", sessionHandle);
        params.put("caseID", caseID);
        params.put("data", JDOMUtil.elementToString(data));
        executePost(_backEndURIStr, params);
    }


    public void forceCompleteWorkItem(WorkItemRecord wir, Element data,
                                 String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("completeWorkItem", sessionHandle);
        params.put("workitemID", wir.getID());
        params.put("data", JDOMUtil.elementToString(data));
        params.put("force", "true");
        executePost(_backEndURIStr, params);
    }


    public WorkItemRecord continueWorkItem(String workItemID, String sessionHandle)
                                                           throws IOException {
        Map<String, String> params = prepareParamMap("continueWorkItem", sessionHandle);
        params.put("workitemID", workItemID);
        String result = executePost(_backEndURIStr, params);

        // process result
        return Marshaller.unmarshalWorkItem(stripOuterElement(result));
    }


    public WorkItemRecord unsuspendWorkItem(String workItemID, String sessionHandle)
                                                           throws IOException {
        Map<String, String> params = prepareParamMap("unsuspendWorkItem", sessionHandle);
        params.put("workitemID", workItemID);
        String result = executePost(_backEndURIStr, params);

        // process result
        return Marshaller.unmarshalWorkItem(stripOuterElement(result));
    }

    public void restartWorkItem(String workItemID, String sessionHandle)
                                                           throws IOException {
        Map<String, String> params = prepareParamMap("restartWorkItem", sessionHandle);
        params.put("workitemID", workItemID);
        executePost(_backEndURIStr, params);
    }


    public void startWorkItem(String workItemID, String sessionHandle)
                                                           throws IOException {
        Map<String, String> params = prepareParamMap("startWorkItem", sessionHandle);
        params.put("workitemID", workItemID);
        executePost(_backEndURIStr, params);
    }


    public void cancelWorkItem(String workItemID, boolean fail, String sessionHandle)
                                                           throws IOException {
        Map<String, String> params = prepareParamMap("cancelWorkItem", sessionHandle);
        params.put("workitemID", workItemID);
        params.put("fail", String.valueOf(fail));
        executePost(_backEndURIStr, params);
    }

}