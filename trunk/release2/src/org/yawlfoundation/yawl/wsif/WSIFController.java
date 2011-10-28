/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.wsif;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.TaskInformation;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.YParametersSchema;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Hashtable;
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
    private Logger _log = Logger.getLogger(this.getClass());

    private static final String WSDL_LOCATION_PARAMNAME = "YawlWSInvokerWSDLLocation";
    private static final String WSDL_PORTNAME_PARAMNAME = "YawlWSInvokerPortName";
    private static final String WSDL_OPERATIONNAME_PARAMNAME = "YawlWSInvokerOperationName";


    /**
     * Implements InterfaceBWebsideController.  It receives messages from the engine
     * notifying an enabled task and acts accordingly.  In this case it takes the message,
     * tries to check out the work item, and if successful it begins to start up a web service
     * invocation.
     * @param enabledWorkItem
     */
    public void handleEnabledWorkItemEvent(WorkItemRecord enabledWorkItem) {
        try {
            if (!checkConnection(_sessionHandle)) {
                _sessionHandle = connect(engineLogonName, engineLogonPassword);
            }
            if (successful(_sessionHandle)) {
                List<WorkItemRecord> executingChildren =
                        checkOutAllInstancesOfThisTask(enabledWorkItem, _sessionHandle);
                for (WorkItemRecord itemRecord : executingChildren) {
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

                    _log.warn("\n\nReply from Web service being invoked is :" +
                            replyFromWebServiceBeingInvoked);

                    Element caseDataBoundForEngine = prepareReplyRootElement(enabledWorkItem, _sessionHandle);
                    Map<String, String> outputDataTypes = getOutputDataTypes(enabledWorkItem);

                    for (Object o : replyFromWebServiceBeingInvoked.keySet()) {
                        String varName = (String) o;
                        Object replyMsg = replyFromWebServiceBeingInvoked.get(varName);
                        System.out.println("replyMsg class = " + replyMsg.getClass().getName());
                        String varVal = replyMsg.toString();
                        String varType = outputDataTypes.get(varName);
                        if ((varType != null) && (! varType.endsWith("string"))) {
                            varVal = validateValue(varType, varVal);
                        }
                        Element content = new Element(varName);
                        content.setText(varVal);
                        caseDataBoundForEngine.addContent(content);
                    }

                    _logger.debug("\nResult of item [" +
                            itemRecord.getID() + "] checkin is : " +
                            checkInWorkItem(
                                    itemRecord.getID(),
                                    inputData,
                                    caseDataBoundForEngine, null,
                                    _sessionHandle));
                }
            }

        } catch (Throwable e) {
            _logger.error(e.getMessage(), e);
        }
    }

    private Map<String, String> getOutputDataTypes(WorkItemRecord wir) throws IOException {
        Map<String, String> dataTypes = new Hashtable<String, String>();
        TaskInformation taskInfo = this.getTaskInformation(
                new YSpecificationID(wir), wir.getTaskID(), _sessionHandle);
        if (taskInfo != null) {
            YParametersSchema schema = taskInfo.getParamSchema();
            if (schema != null) {
                for (YParameter param : schema.getOutputParams()) {
                    dataTypes.put(param.getPreferredName(), param.getDataTypeName());
                }
            }
        }
        return dataTypes;
    }

    private String validateValue(String type, String value) {
        if (type.endsWith("boolean")) {
            return String.valueOf(value.equalsIgnoreCase("true"));
        }
        try {
            if (type.endsWith("integer")) {
               return String.valueOf(new Integer(value));
            }
            else if (type.endsWith("double")) {
               return String.valueOf(new Double(value));
            }
            else if (type.endsWith("float")) {
               return String.valueOf(new Float(value));
            }
            else return value;    // we tried!
        }
        catch (NumberFormatException nfe) {
            if (type.endsWith("integer")) {
                return "0";
            }
            else {
                return "0.0";
            }
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

