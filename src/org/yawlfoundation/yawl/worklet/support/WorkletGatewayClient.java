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

import org.jdom2.Element;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.*;
import org.yawlfoundation.yawl.worklet.rdr.RdrConclusion;
import org.yawlfoundation.yawl.worklet.rdr.RdrNode;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;
import org.yawlfoundation.yawl.worklet.selection.WorkletRunner;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
     * if a rule node is satisfied raises an exception and executes the exception handler
     * for the node, if one is defined. A raised exception will be announced to listeners
     * as a byproduct of this method call.
     * @param wir the workitem containing specification and task identifiers. PRE: the
     *            workitem must currently exist in the engine
     * @param data the data set to use in the evaluation
     * @param rType the type of rule tree to evaluate. NOTE: Only ItemAbort and
     *              ItemConstraintViolation rules can be used with this method
     * @param handle a current sessionhandle to the worklet service
     * @return a success or error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String process(WorkItemRecord wir, Element data, RuleType rType,
                                  String handle) throws IOException {
        Map<String, String> params = prepareParamMap("process", handle);
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
     * Raises an exception and executes the exception handler defined in an RdrConclusion.
     * A raised exception will be announced to listeners as a byproduct of this method call.
     * @param wir the workitem containing specification and task identifiers. PRE: the
     *            workitem must currently exist in the engine
     * @param rType the type of rule tree to evaluate. NOTE: Case-level exception types
     *              cannot be used with this method
     * @param conclusion the RdrConclusion object that defines the exlet to execute
     * @param handle a current sessionhandle to the worklet service
     * @return a success or error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String execute(WorkItemRecord wir, RuleType rType, RdrConclusion conclusion,
                                  String handle) throws IOException {
        Map<String, String> params = prepareParamMap("process", handle);
        params.put("wir", wir.toXML());
        params.put("rtype", rType.name());
        params.put("conclusion", conclusion.toXML());
        return executePost(_wsURI, params);
    }


    /**
     * Raises an exception and executes the exception handler defined in an RdrConclusion.
     * A raised exception will be announced to listeners as a byproduct of this method call.
     * @param wir the workitem containing specification and task identifiers. PRE: the
     *            workitem must currently exist in the engine
     * @param rType the type of rule tree to evaluate. NOTE: Case-level exception types
     *              cannot be used with this method
     * @param conclusion the RdrConclusion object that defines the exlet to execute
     * @param workletSet a set of worklet specifications to be loaded into the engine
     *                   for use as compensation handlers during the exception handling
     * @param handle a current sessionhandle to the worklet service
     * @return a success or error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String execute(WorkItemRecord wir, RuleType rType, RdrConclusion conclusion,
                          Set<String> workletSet, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("process", handle);
        params.put("wir", wir.toXML());
        params.put("rtype", rType.name());
        params.put("workletset", StringUtil.setToXML(workletSet));
        params.put("conclusion", conclusion.toXML());
        return executePost(_wsURI, params);
    }


    /**
     * Raises an exception and executes the exception handler defined in an RdrConclusion.
     * A raised exception will be announced to listeners as a byproduct of this method call.
     * @param wir the workitem containing specification and task identifiers. PRE: the
     *            workitem must currently exist in the engine
     * @param rType the type of rule tree to evaluate. NOTE: Case-level exception types
     *              cannot be used with this method
     * @param conclusion the RdrConclusion object that defines the exlet to execute
     * @param workletXML a worklet specification to be loaded into the engine
     *                   for use as a compensation handler during the exception handling
     * @param handle a current sessionhandle to the worklet service
     * @return a success or error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String execute(WorkItemRecord wir, RuleType rType, RdrConclusion conclusion,
                          String workletXML, String handle) throws IOException {
        Set<String> workletSet = new HashSet<String>();
        workletSet.add(workletXML);
        return execute(wir, rType, conclusion, workletSet, handle);
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


    /**
     * Adds a complete (legacy) rule set, expressed as xml
     * @param ruleSetXML the rule set to add
     * @param handle a current sessionhandle to the worklet service
     * @return a success or error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String addRdrSet(YSpecificationID specID, String ruleSetXML, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("addRdrSet", handle);
        params.putAll(specID.toMap());
        params.put("ruleset", ruleSetXML);
        return executePost(_wsURI, params);
    }


    /**
     * Gets the specified worklet, if loaded in the service
     * @param specID the Worklet specification id
     * @param handle a current sessionhandle to the worklet service
     * @return the worklet xml, if found, or an error message if not
     * @throws java.io.IOException if the service can't be reached
     */
    public String getWorklet(YSpecificationID specID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getWorklet", handle);
        params.putAll(specID.toMap());
        return executeGet(_wsURI, params);
    }


    /**
     * Adds a worklet to the service repertoire
     * @param specID the Worklet specification id
     * @param workletXML the worklet specification (as XML) to add
     * @param handle a current sessionhandle to the worklet service
     * @return a success or error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String addWorklet(YSpecificationID specID, String workletXML, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("addWorklet", handle);
        params.putAll(specID.toMap());
        params.put("worklet", workletXML);
        return executePost(_wsURI, params);
    }


    /**
     * Gets a list of the names of all the worklets  in the repository
     * @param handle a current sessionhandle to the worklet service
     * @return an XML representation of the list of worklet names
     * @throws java.io.IOException if the service can't be reached
     */
    public String getWorkletNames(String handle) throws IOException {
        return executeGet(_wsURI, prepareParamMap("getWorkletNames", handle));
    }


    /**
     * Gets a list of specification ids of all the worklets in the repository
     * @param handle a current sessionhandle to the worklet service
     * @return an XML representation of the list of worklet specification ids
     * @throws java.io.IOException if the service can't be reached
     */
    public String getWorkletIdList(String handle) throws IOException {
        return executeGet(_wsURI, prepareParamMap("getWorkletIdList", handle));
    }


    /**
     * Gets a info set of all currently running worklets
     * @param handle a current sessionhandle to the worklet service
     * @return an XML representation of the list of worklet file names
     * @throws java.io.IOException if the service can't be reached
     */
    public Set<WorkletRunner> getRunningWorklets(String handle) throws IOException {
        String result = executeGet(_wsURI, prepareParamMap("getRunningWorklets", handle));
        if (! successful(result)) {
            throw new IOException(result);
        }
        XNode root = new XNodeParser().parse(result);
        if (root == null) {
            throw new IOException("Malformed result string:" + result);
        }
        Set<WorkletRunner> runners = new HashSet<WorkletRunner>();
        for (XNode node : root.getChildren()) {
            runners.add(new WorkletRunner(node));
        }
        return runners;
    }

}