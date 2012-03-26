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

package org.yawlfoundation.yawl.worklet.support;

import org.jdom.Element;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.PasswordEncryptor;
import org.yawlfoundation.yawl.worklet.rdr.RdrNode;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;

import java.io.IOException;
import java.util.Map;

/**
 * An API to be used by clients that want to retrieve data from the cost service.
 *
 *  @author Michael Adams
 *  11/07/2011
 */

public class WorkletGatewayClient extends Interface_Client {

    private static final String DEFAULT_URI = "http://localhost:8080/workletService/gateway";
    
    /** the uri of the Worklet Service Gateway
     */
    protected String _wsURI;

    /** the constructors
     * @param uri the uri of the YAWL Cost Service
     */
    public WorkletGatewayClient(String uri) {
        _wsURI = uri ;
    }


    public WorkletGatewayClient() {
        this(DEFAULT_URI);
    }


    public void setURI(String uri)  { _wsURI = uri; }


    /*******************************************************************************/

    /**
     * Connects an external entity to the cost service
     * @param userID the userid
     * @param password the corresponding password
     * @return a sessionHandle if successful, or a failure message if not
     * @throws java.io.IOException if the service can't be reached
     */
    public String connect(String userID, String password) throws IOException {
        Map<String, String> params = prepareParamMap("connect", null);
        params.put("userid", userID);
        params.put("password", PasswordEncryptor.encrypt(password, null));
        return executeGet(_wsURI, params) ;
    }


    /**
     * Check that a session handle is active
     * @param handle the session handle to check
     * @return "true" if the id is valid, "false" if otherwise
     * @throws java.io.IOException if the service can't be reached
     */
    public String checkConnection(String handle) throws IOException {
        return executeGet(_wsURI, prepareParamMap("checkConnection", handle)) ;
    }


    /**
     * Disconnects an external entity from the cost service
     * @param handle the sessionHandle to disconnect
     * @throws java.io.IOException if the service can't be reached
     */
    public void disconnect(String handle) throws IOException {
        executePost(_wsURI, prepareParamMap("disconnect", handle));
    }


    /**
     *
     * @param listenerURI
     * @param handle a current sessionhandle to the cost service
     * @return
     * @throws java.io.IOException
     */
    public String addListener(String listenerURI, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addListener", handle);
        params.put("uri", listenerURI);
        return executePost(_wsURI, params);
    }


    public String removeListener(String listenerURI, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("removeListener", handle);
        params.put("uri", listenerURI);
        return executePost(_wsURI, params);
    }


    public String refresh(YSpecificationID specID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("refresh", handle);
        params.putAll(specID.toMap());
        return executePost(_wsURI, params);
    }


    public String evaluate(WorkItemRecord wir, Element data, RuleType rType,
                                  String handle) throws IOException {
        Map<String, String> params = prepareParamMap("evaluate", handle);
        params.put("wir", wir.toXML());
        params.put("data", JDOMUtil.elementToString(data));
        params.put("rtype", rType.name());
        return executePost(_wsURI, params);
    }

    public String evaluate(YSpecificationID specID, String taskID, Element data,
                                  RuleType rType, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("evaluate", handle);
        params.putAll(specID.toMap());
        if (taskID != null) params.put("taskid", taskID);
        params.put("data", JDOMUtil.elementToString(data));
        params.put("rtype", rType.name());
        return executePost(_wsURI, params);
    }

    public String evaluate(String processName, String taskID, Element data,
                                  RuleType rType, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("evaluate", handle);
        params.put("name", processName);
        if (taskID != null) params.put("taskid", taskID);
        params.put("data", JDOMUtil.elementToString(data));
        params.put("rtype", rType.name());
        return executePost(_wsURI, params);
    }

    public String addNode(WorkItemRecord wir, RuleType rType, RdrNode node,
                          String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addNode", handle);
        params.put("wir", wir.toXML());
        params.put("node", node.toXML());
        params.put("rtype", rType.name());
        return executePost(_wsURI, params);
    }

    public String addNode(YSpecificationID specID, String taskID, RuleType rType,
                           RdrNode node, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addNode", handle);
        params.putAll(specID.toMap());
        if (taskID != null) params.put("taskid", taskID);
        params.put("node", node.toXML());
        params.put("rtype", rType.name());
        return executePost(_wsURI, params);
    }

    public String addNode(String processName, String taskID, RuleType rType,
                           RdrNode node, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addNode", handle);
        params.put("name", processName);
        if (taskID != null) params.put("taskid", taskID);
        params.put("node", node.toXML());
        params.put("rtype", rType.name());
        return executePost(_wsURI, params);
    }

    public String getNode(WorkItemRecord wir, RuleType rType, int nodeID, String handle) 
            throws IOException {
        Map<String, String> params = prepareParamMap("getNode", handle);
        params.put("wir", wir.toXML());
        params.put("nodeid", String.valueOf(nodeID));
        params.put("rtype", rType.name());
        return executeGet(_wsURI, params);
    }

    public String getNode(YSpecificationID specID, String taskID, RuleType rType, 
                          int nodeID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getNode", handle);
        params.putAll(specID.toMap());
        if (taskID != null) params.put("taskid", taskID);
        params.put("nodeid", String.valueOf(nodeID));
        params.put("rtype", rType.name());
        return executeGet(_wsURI, params);
    }

    public String getNode(String processName, String taskID, RuleType rType, int nodeID,
                          String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getNode", handle);
        params.put("name", processName);
        if (taskID != null) params.put("taskid", taskID);
        params.put("nodeid", String.valueOf(nodeID));
        params.put("rtype", rType.name());
        return executeGet(_wsURI, params);
    }

    public String getRdrTree(WorkItemRecord wir, RuleType rType, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getRdrTree", handle);
        params.put("wir", wir.toXML());
        params.put("rtype", rType.name());
        return executeGet(_wsURI, params);
    }

    public String getRdrTree(YSpecificationID specID, String taskID, RuleType rType,
                              String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getRdrTree", handle);
        params.putAll(specID.toMap());
        if (taskID != null) params.put("taskid", taskID);
        params.put("rtype", rType.name());
        return executeGet(_wsURI, params);
    }

    public String getRdrTree(String processName, String taskID, RuleType rType,
                              String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getRdrTree", handle);
        params.put("name", processName);
        if (taskID != null) params.put("taskid", taskID);
        params.put("rtype", rType.name());
        return executeGet(_wsURI, params);
    }

    public String getRdrSet(YSpecificationID specID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getRdrSet", handle);
        params.putAll(specID.toMap());
        return executeGet(_wsURI, params);
    }

    public String getRdrSet(String processName, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getRdrSet", handle);
        params.put("name", processName);
        return executeGet(_wsURI, params);
    }

}