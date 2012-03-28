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
 * An API to be used by clients that want to interact with the worklet service.
 *
 *  @author Michael Adams
 *  11/07/2011
 */

public class WorkletGatewayClient extends Interface_Client {

    private static final String DEFAULT_URI = "http://localhost:8080/workletService/gateway";

    protected String _wsURI;                   // the uri of the Worklet Service Gateway

    /**
     * Constructor
     * @param uri the uri of the Worklet Service Gateway
     */
    public WorkletGatewayClient(String uri) {
        _wsURI = uri ;
    }

    /**
     * Constructor - uses default uri on localhost
     */
    public WorkletGatewayClient() {
        this(DEFAULT_URI);
    }


    /**
     * Sets the uri of the Worklet Service Gateway
     * @param uri the uri to set
     */
    public void setURI(String uri)  { _wsURI = uri; }


    /*******************************************************************************/

    /**
     * Connects an external entity to the worklet service
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
     * Disconnects an external entity from the worklet service
     * @param handle the sessionHandle to disconnect
     * @throws java.io.IOException if the service can't be reached
     */
    public void disconnect(String handle) throws IOException {
        executePost(_wsURI, prepareParamMap("disconnect", handle));
    }


    /**
     * Adds a listener for worklet events (selection, exception raising)
     * @param listenerURI the URI of the listener to add
     * @param handle a current sessionhandle to the worklet service
     * @return a message denoting success or describing an error
     * @throws java.io.IOException if the service can't be reached
     */
    public String addListener(String listenerURI, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addListener", handle);
        params.put("uri", listenerURI);
        return executePost(_wsURI, params);
    }


    /**
     * Removes a listener for worklet events
     * @param listenerURI the URI of the listener to remove
     * @param handle a current sessionhandle to the worklet service
     * @return a message denoting success or describing an error
     * @throws java.io.IOException if the service can't be reached
     */
    public String removeListener(String listenerURI, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("removeListener", handle);
        params.put("uri", listenerURI);
        return executePost(_wsURI, params);
    }


    /**
     * Refreshes the internal rules cache from storage for a specification
     * @param specID the specification id to refresh the rule set for
     * @param handle a current sessionhandle to the worklet service
     * @return a message denoting success or describing an error
     * @throws java.io.IOException if the service can't be reached
     */
    public String refresh(YSpecificationID specID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("refresh", handle);
        params.putAll(specID.toMap());
        return executePost(_wsURI, params);
    }


    /**
     * Evaluates a data set against the rule tree for a specification and task, and
     * returns the conclusion, if any. Note that for a conclusion to be returned, a rule
     * tree must exist for the specification/task/ruletype, and a rule in that tree
     * must be satisfied.
     * @param wir the workitem containing specification and task identifiers
     * @param data the data set to use in the evaluation
     * @param rType the type of rule tree to evaluate
     * @param handle a current sessionhandle to the worklet service
     * @return a conclusion XML string, or an error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String evaluate(WorkItemRecord wir, Element data, RuleType rType,
                                  String handle) throws IOException {
        Map<String, String> params = prepareParamMap("evaluate", handle);
        params.put("wir", wir.toXML());
        params.put("data", JDOMUtil.elementToString(data));
        params.put("rtype", rType.name());
        return executePost(_wsURI, params);
    }


    /**
     * Evaluates a data set against the rule tree for a specification and task, and
     * returns the conclusion, if any. Note that for a conclusion to be returned, a rule
     * tree must exist for the specification/task/ruletype, and a rule in that tree
     * must be satisfied.
     * @param specID the specification identifier
     * @param taskID the task identifier (may be null for case-level rule types)
     * @param data the data set to use in the evaluation
     * @param rType the type of rule tree to evaluate
     * @param handle a current sessionhandle to the worklet service
     * @return a conclusion XML string, or an error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String evaluate(YSpecificationID specID, String taskID, Element data,
                                  RuleType rType, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("evaluate", handle);
        params.putAll(specID.toMap());
        if (taskID != null) params.put("taskid", taskID);
        params.put("data", JDOMUtil.elementToString(data));
        params.put("rtype", rType.name());
        return executePost(_wsURI, params);
    }


    /**
     * Evaluates a data set against the rule tree for a process and task, and
     * returns the conclusion, if any. Note that for a conclusion to be returned, a rule
     * tree must exist for the process/task/ruletype, and a rule in that tree
     * must be satisfied.
     * @param processName the process identifier, or unique ruleset name
     * @param taskID the task identifier (may be null for case-level rule types)
     * @param data the data set to use in the evaluation
     * @param rType the type of rule tree to evaluate
     * @param handle a current sessionhandle to the worklet service
     * @return a conclusion XML string, or an error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String evaluate(String processName, String taskID, Element data,
                                  RuleType rType, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("evaluate", handle);
        params.put("name", processName);
        if (taskID != null) params.put("taskid", taskID);
        params.put("data", JDOMUtil.elementToString(data));
        params.put("rtype", rType.name());
        return executePost(_wsURI, params);
    }


    /**
     * Adds a node to a ruleSet. If the appropriate rule tree exists, the node is added
     * to the correct place in the tree; if the rule tree does not yet exist, it is
     * created and the node added as its first rule node (after the root); the there
     * is no ruleSet for the parameters, one is created, and a new tree inserted as above.
     * @param wir the workitem containing specification and task identifiers
     * @param rType the type of rule tree to add the node to
     * @param node the node to add. It is not required that values are given for id,
     *             parent, true child or false child - these will be calculated and
     *             incorporated when the node is added. It <b>is</b> necessary that
     *             values are given for condition, conclusion and particularly
     *             cornerstone data, which is used to determine the correct place to
     *             insert the node within the tree. Not supplying these values will
     *             produce unpredictable results
     * @param handle a current sessionhandle to the worklet service
     * @return the added node, updated with parent and child ids as appropriate
     * @throws java.io.IOException if the service can't be reached
     */
    public String addNode(WorkItemRecord wir, RuleType rType, RdrNode node,
                          String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addNode", handle);
        params.put("wir", wir.toXML());
        params.put("node", node.toXML());
        params.put("rtype", rType.name());
        return executePost(_wsURI, params);
    }


    /**
     * Adds a node to a ruleSet. If the appropriate rule tree exists, the node is added
     * to the correct place in the tree; if the rule tree does not yet exist, it is
     * created and the node added as its first rule node (after the root); the there
     * is no ruleSet for the parameters, one is created, and a new tree inserted as above.
     * @param specID the specification identifier
     * @param taskID the task identifier (may be null for case-level rule types)
     * @param rType the type of rule tree to add the node to
     * @param node the node to add. It is not required that values are given for id,
     *             parent, true child or false child - these will be calculated and
     *             incorporated when the node is added. It <b>is</b> necessary that
     *             values are given for condition, conclusion and particularly
     *             cornerstone data, which is used to determine the correct place to
     *             insert the node within the tree. Not supplying these values will
     *             produce unpredictable results
     * @param handle a current sessionhandle to the worklet service
     * @return the added node, updated with parent and child ids as appropriate
     * @throws java.io.IOException if the service can't be reached
     */
    public String addNode(YSpecificationID specID, String taskID, RuleType rType,
                           RdrNode node, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addNode", handle);
        params.putAll(specID.toMap());
        if (taskID != null) params.put("taskid", taskID);
        params.put("node", node.toXML());
        params.put("rtype", rType.name());
        return executePost(_wsURI, params);
    }


    /**
     * Adds a node to a ruleSet. If the appropriate rule tree exists, the node is added
     * to the correct place in the tree; if the rule tree does not yet exist, it is
     * created and the node added as its first rule node (after the root); the there
     * is no ruleSet for the parameters, one is created, and a new tree inserted as above.
     * @param processName the process identifier, or unique ruleset name
     * @param taskID the task identifier (may be null for case-level rule types)
     * @param rType the type of rule tree to add the node to
     * @param node the node to add. It is not required that values are given for id,
     *             parent, true child or false child - these will be calculated and
     *             incorporated when the node is added. It <b>is</b> necessary that
     *             values are given for condition, conclusion and particularly
     *             cornerstone data, which is used to determine the correct place to
     *             insert the node within the tree. Not supplying these values will
     *             produce unpredictable results
     * @param handle a current sessionhandle to the worklet service
     * @return the added node, updated with parent and child ids as appropriate
     * @throws java.io.IOException if the service can't be reached
     */
    public String addNode(String processName, String taskID, RuleType rType,
                           RdrNode node, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addNode", handle);
        params.put("name", processName);
        if (taskID != null) params.put("taskid", taskID);
        params.put("node", node.toXML());
        params.put("rtype", rType.name());
        return executePost(_wsURI, params);
    }


    /**
     * Gets a copy of a particular node from a rule set
     * @param wir the workitem containing specification and task identifiers
     * @param rType the type of rule tree to get the node from
     * @param nodeID the (integer) node id
     * @param handle a current sessionhandle to the worklet service
     * @return the node, if found within the specification/task/rule-type combination
     * @throws java.io.IOException if the service can't be reached
     */
    public String getNode(WorkItemRecord wir, RuleType rType, int nodeID, String handle) 
            throws IOException {
        Map<String, String> params = prepareParamMap("getNode", handle);
        params.put("wir", wir.toXML());
        params.put("nodeid", String.valueOf(nodeID));
        params.put("rtype", rType.name());
        return executeGet(_wsURI, params);
    }


    /**
     * Gets a copy of a particular node from a rule set
     * @param specID the specification identifier
     * @param taskID the task identifier (may be null for case-level rule types)
     * @param rType the type of rule tree to get the node from
     * @param nodeID the (integer) node id
     * @param handle a current sessionhandle to the worklet service
     * @return the node, if found within the specification/task/rule-type combination
     * @throws java.io.IOException if the service can't be reached
     */
    public String getNode(YSpecificationID specID, String taskID, RuleType rType,
                          int nodeID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getNode", handle);
        params.putAll(specID.toMap());
        if (taskID != null) params.put("taskid", taskID);
        params.put("nodeid", String.valueOf(nodeID));
        params.put("rtype", rType.name());
        return executeGet(_wsURI, params);
    }


    /**
     * Gets a copy of a particular node from a rule set
     * @param processName the process identifier, or unique ruleset name
     * @param taskID the task identifier (may be null for case-level rule types)
     * @param rType the type of rule tree to get the node from
     * @param nodeID the (integer) node id
     * @param handle a current sessionhandle to the worklet service
     * @return the node, if found within the process/task/rule-type combination
     * @throws java.io.IOException if the service can't be reached
     */
    public String getNode(String processName, String taskID, RuleType rType, int nodeID,
                          String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getNode", handle);
        params.put("name", processName);
        if (taskID != null) params.put("taskid", taskID);
        params.put("nodeid", String.valueOf(nodeID));
        params.put("rtype", rType.name());
        return executeGet(_wsURI, params);
    }


    /**
     * Gets a copy of a particular rule tree
     * @param wir the workitem containing specification and task identifiers
     * @param rType the type of rule tree to get the node from
     * @param handle a current sessionhandle to the worklet service
     * @return the tree, if found for the specification/task/rule-type combination
     * @throws java.io.IOException if the service can't be reached
     */
    public String getRdrTree(WorkItemRecord wir, RuleType rType, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getRdrTree", handle);
        params.put("wir", wir.toXML());
        params.put("rtype", rType.name());
        return executeGet(_wsURI, params);
    }


    /**
     * Gets a copy of a particular rule tree
     * @param specID the specification identifier
     * @param taskID the task identifier (may be null for case-level rule types)
     * @param rType the type of rule tree to get the node from
     * @param handle a current sessionhandle to the worklet service
     * @return the tree, if found for the specification/task/rule-type combination
     * @throws java.io.IOException if the service can't be reached
     */
    public String getRdrTree(YSpecificationID specID, String taskID, RuleType rType,
                              String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getRdrTree", handle);
        params.putAll(specID.toMap());
        if (taskID != null) params.put("taskid", taskID);
        params.put("rtype", rType.name());
        return executeGet(_wsURI, params);
    }


    /**
      * Gets a copy of a particular rule tree
      * @param processName the process identifier, or unique ruleset name
      * @param taskID the task identifier (may be null for case-level rule types)
      * @param rType the type of rule tree to get the node from
      * @param handle a current sessionhandle to the worklet service
      * @return the tree, if found for the process/task/rule-type combination
      * @throws java.io.IOException if the service can't be reached
      */
    public String getRdrTree(String processName, String taskID, RuleType rType,
                              String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getRdrTree", handle);
        params.put("name", processName);
        if (taskID != null) params.put("taskid", taskID);
        params.put("rtype", rType.name());
        return executeGet(_wsURI, params);
    }


    /**
     * Gets a copy of a particular rule set
     * @param specID the specification identifier
     * @param handle a current sessionhandle to the worklet service
     * @return the rule set, if found for the specification
     * @throws java.io.IOException if the service can't be reached
     */
    public String getRdrSet(YSpecificationID specID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getRdrSet", handle);
        params.putAll(specID.toMap());
        return executeGet(_wsURI, params);
    }


    /**
     * Gets a copy of a particular rule set
     * @param processName the process identifier, or unique ruleset name
     * @param handle a current sessionhandle to the worklet service
     * @return the rule set, if found for the process
     * @throws java.io.IOException if the service can't be reached
     */
    public String getRdrSet(String processName, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getRdrSet", handle);
        params.put("name", processName);
        return executeGet(_wsURI, params);
    }

}