/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.wsif;

import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.jdom.Element;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 *
 * 
 * @author Lachlan Aldred
 * Date: 19/03/2004
 * Time: 11:40:59
 * 
 */
public class WSIFController extends InterfaceBWebsideController {

    private String _sessionHandle = null;

    private static final String WSDL_LOCATION_PARAMNAME = "YawlWSInvokerWSDLLocation";
    private static final String WSDL_PORTNAME_PARAMNAME = "YawlWSInvokerPortName";
    private static final String WSDL_OPERATIONNAME_PARAMNAME = "YawlWSInvokerOperationName";


    /**
     * Implements InterfaceBWebsideController.  It recieves messages from the engine
     * notifying an enabled task and acts accordingly.  In this case it takes the message,
     * tries to check out the work item, and if successful it begins to start up a web service
     * invokation.
     * @param enabledWorkItem
     */
    public void handleEnabledWorkItemEvent(WorkItemRecord enabledWorkItem) {
        try {
            if (!checkConnection(_sessionHandle)) {
                _sessionHandle = connect(DEFAULT_ENGINE_USERNAME, DEFAULT_ENGINE_PASSWORD);
            }
            if (successful(_sessionHandle)) {
                List executingChildren = checkOutAllInstancesOfThisTask(enabledWorkItem, _sessionHandle);

                for (int i = 0; i < executingChildren.size(); i++) {
                    WorkItemRecord itemRecord = (WorkItemRecord) executingChildren.get(i);
                    Element inputData = itemRecord.getDataList();
                    String wsdlLocation = inputData.getChildText(WSDL_LOCATION_PARAMNAME);
                    String portName = inputData.getChildText(WSDL_PORTNAME_PARAMNAME);
                    String operationName = inputData.getChildText(WSDL_OPERATIONNAME_PARAMNAME);

                    Element webServiceArgsData = (Element) inputData.clone();
                    webServiceArgsData.removeChild(WSDL_LOCATION_PARAMNAME);
                    webServiceArgsData.removeChild(WSDL_PORTNAME_PARAMNAME);
                    webServiceArgsData.removeChild(WSDL_OPERATIONNAME_PARAMNAME);

                    Map replyFromWebServiceBeingInvoked =
                            WSIFInvoker.invokeMethod(
                                    wsdlLocation,
                                    portName,
                                    operationName,
                                    webServiceArgsData,
                                    getAuthenticationConfig());

                    System.out.println("\n\nReply from Web service being " +
                            "invoked is :" + replyFromWebServiceBeingInvoked);

                    Element caseDataBoundForEngine = prepareReplyRootElement(enabledWorkItem, _sessionHandle);

                    for (Iterator iterator = replyFromWebServiceBeingInvoked.keySet().iterator(); iterator.hasNext();) {
                        String varName = (String) iterator.next();
                        Object replyMsg = replyFromWebServiceBeingInvoked.get(varName);
                        System.out.println("replyMsg class = " + replyMsg.getClass().getName());
                        String varVal = replyMsg.toString();

                        Element content = new Element(varName);
                        content.setText(varVal);
                        caseDataBoundForEngine.addContent(content);
                    }

                    _logger.debug("\nResult of item [" +
                            itemRecord.getID() + "] checkin is : " +
                            checkInWorkItem(
                                    itemRecord.getID(),
                                    inputData,
                                    caseDataBoundForEngine,
                                    _sessionHandle));
                }
            }

        } catch (Exception e) {
            _logger.error(e.getMessage(), e);
        }
    }


    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {

    }

    public YParameter[] describeRequiredParams() {
        YParameter[] params = new YParameter[3];
        YParameter param;

        param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        param.setDataTypeAndName(XSD_ANYURI_TYPE, WSDL_LOCATION_PARAMNAME, XSD_NAMESPACE);
        param.setDocumentation("This is the location of the WSDL for the Web Service");
        params[0] = param;

        param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        param.setDataTypeAndName(XSD_NCNAME_TYPE, WSDL_PORTNAME_PARAMNAME, XSD_NAMESPACE);
        param.setDocumentation("This is the port name of the Web service - inside the WSDL.");
        params[1] = param;

        param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        param.setDataTypeAndName(XSD_NCNAME_TYPE, WSDL_OPERATIONNAME_PARAMNAME, XSD_NAMESPACE);
        param.setDocumentation("This is the operation name of the Web service - inside the WSDL.");
        params[2] = param;

        return params;
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        RequestDispatcher dispatcher =
                request.getRequestDispatcher("/authServlet");

        dispatcher.forward(request, response);
    }


}

