/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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
import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.PasswordEncryptor;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.worklet.rdr.RdrConclusion;
import org.yawlfoundation.yawl.worklet.rdr.RdrNode;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;

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
     * @return a session handle if successful, or a failure message if not
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
     * @param handle the session handle to disconnect
     * @throws java.io.IOException if the service can't be reached
     */
    public void disconnect(String handle) throws IOException {
        executePost(_wsURI, prepareParamMap("disconnect", handle));
    }


    /**
     * Adds a listener for worklet events (selection, exception raising)
     * @param listenerURI the URI of the listener to add
     * @param handle a current session handle to the worklet service
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
     * @param handle a current session handle to the worklet service
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
     * @param handle a current session handle to the worklet service
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
     * @param handle a current session handle to the worklet service
     * @return a success or error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String process(WorkItemRecord wir, Element data, RuleType rType,
                                  String handle) throws IOException {
        Map<String, String> params = prepareParamMap("process", handle);
        params.put("wir", wir.toXML());
        params.put("data", JDOMUtil.elementToString(data));
        params.put("rtype", rType.toString());
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
     * @param handle a current session handle to the worklet service
     * @return a conclusion XML string, or an error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String evaluate(WorkItemRecord wir, Element data, RuleType rType,
                                  String handle) throws IOException {
        Map<String, String> params = prepareParamMap("evaluate", handle);
        params.put("wir", wir.toXML());
        params.put("data", JDOMUtil.elementToString(data));
        params.put("rtype", rType.toString());
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
     * @param handle a current session handle to the worklet service
     * @return a conclusion XML string, or an error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String evaluate(YSpecificationID specID, String taskID, Element data,
                                  RuleType rType, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("evaluate", handle);
        params.putAll(specID.toMap());
        if (taskID != null) params.put("taskid", taskID);
        params.put("data", JDOMUtil.elementToString(data));
        params.put("rtype", rType.toString());
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
     * @param handle a current session handle to the worklet service
     * @return a conclusion XML string, or an error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String evaluate(String processName, String taskID, Element data,
                                  RuleType rType, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("evaluate", handle);
        params.put("name", processName);
        if (taskID != null) params.put("taskid", taskID);
        params.put("data", JDOMUtil.elementToString(data));
        params.put("rtype", rType.toString());
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
     * @param handle a current session handle to the worklet service
     * @return a success or error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String execute(WorkItemRecord wir, RuleType rType, RdrConclusion conclusion,
                                  String handle) throws IOException {
        Map<String, String> params = prepareParamMap("process", handle);
        params.put("wir", wir.toXML());
        params.put("rtype", rType.toString());
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
     * @param handle a current session handle to the worklet service
     * @return a success or error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String execute(WorkItemRecord wir, RuleType rType, RdrConclusion conclusion,
                          Set<String> workletSet, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("process", handle);
        params.put("wir", wir.toXML());
        params.put("rtype", rType.toString());
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
     * @param handle a current session handle to the worklet service
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
     * @param handle a current session handle to the worklet service
     * @return the added node, updated with parent and child ids as appropriate
     * @throws java.io.IOException if the service can't be reached
     */
    public String addNode(WorkItemRecord wir, RuleType rType, RdrNode node,
                          String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addNode", handle);
        params.put("wir", wir.toXML());
        params.put("node", node.toXML());
        params.put("rtype", rType.toString());
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
     * @param handle a current session handle to the worklet service
     * @return the added node, updated with parent and child ids as appropriate
     * @throws java.io.IOException if the service can't be reached
     */
    public String addNode(YSpecificationID specID, String taskID, RuleType rType,
                           RdrNode node, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addNode", handle);
        params.putAll(specID.toMap());
        if (taskID != null) params.put("taskid", taskID);
        params.put("node", node.toXML());
        params.put("rtype", rType.toString());
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
     * @param handle a current session handle to the worklet service
     * @return the added node, updated with parent and child ids as appropriate
     * @throws java.io.IOException if the service can't be reached
     */
    public String addNode(String processName, String taskID, RuleType rType,
                           RdrNode node, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addNode", handle);
        params.put("name", processName);
        if (taskID != null) params.put("taskid", taskID);
        params.put("node", node.toXML());
        params.put("rtype", rType.toString());
        return executePost(_wsURI, params);
    }


    /**
     * Gets a copy of a particular node from a rule set
     * @param nodeID the (integer) node id
     * @param handle a current session handle to the worklet service
     * @return the node, if found
     * @throws java.io.IOException if the service can't be reached
     */
    public String getNode(long nodeID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getNode", handle);
        params.put("nodeid", String.valueOf(nodeID));
        return executeGet(_wsURI, params);
    }


    /**
     * Removes a rule node from a rule tree, triggers a tree restructure
     * @param specID the specification identifier
     * @param taskID the task identifier (may be null for case-level rule types)
     * @param rType the type of rule tree to get the node from
     * @param handle a current session handle to the worklet service
     * @return a success or failure message
     * @throws java.io.IOException if the service can't be reached
     */
    public String removeNode(YSpecificationID specID, String taskID, RuleType rType,
                             long nodeID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("removeNode", handle);
        params.putAll(specID.toMap());
        if (taskID != null) params.put("taskid", taskID);
        params.put("rtype", rType.toString());
        params.put("nodeid", String.valueOf(nodeID));
        return executeGet(_wsURI, params);
    }


    /**
     * Gets a copy of a particular rule tree
     * @param wir the workitem containing specification and task identifiers
     * @param rType the type of rule tree to get the node from
     * @param handle a current session handle to the worklet service
     * @return the tree, if found for the specification/task/rule-type combination
     * @throws java.io.IOException if the service can't be reached
     */
    public String getRdrTree(WorkItemRecord wir, RuleType rType, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getRdrTree", handle);
        params.put("wir", wir.toXML());
        params.put("rtype", rType.toString());
        return executeGet(_wsURI, params);
    }


    /**
     * Gets a copy of a particular rule tree
     * @param specID the specification identifier
     * @param taskID the task identifier (may be null for case-level rule types)
     * @param rType the type of rule tree to get the node from
     * @param handle a current session handle to the worklet service
     * @return the tree, if found for the specification/task/rule-type combination
     * @throws java.io.IOException if the service can't be reached
     */
    public String getRdrTree(YSpecificationID specID, String taskID, RuleType rType,
                              String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getRdrTree", handle);
        params.putAll(specID.toMap());
        if (taskID != null) params.put("taskid", taskID);
        params.put("rtype", rType.toString());
        return executeGet(_wsURI, params);
    }


    /**
      * Gets a copy of a particular rule tree
      * @param processName the process identifier, or unique ruleset name
      * @param taskID the task identifier (may be null for case-level rule types)
      * @param rType the type of rule tree to get the node from
      * @param handle a current session handle to the worklet service
      * @return the tree, if found for the process/task/rule-type combination
      * @throws java.io.IOException if the service can't be reached
      */
    public String getRdrTree(String processName, String taskID, RuleType rType,
                              String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getRdrTree", handle);
        params.put("name", processName);
        if (taskID != null) params.put("taskid", taskID);
        params.put("rtype", rType.toString());
        return executeGet(_wsURI, params);
    }


    /**
     * Gets a copy of a particular rule set
     * @param specID the specification identifier
     * @param handle a current session handle to the worklet service
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
     * @param handle a current session handle to the worklet service
     * @return the rule set, if found for the process
     * @throws java.io.IOException if the service can't be reached
     */
    public String getRdrSet(String processName, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getRdrSet", handle);
        params.put("name", processName);
        return executeGet(_wsURI, params);
    }


    /**
     * Gets the identifiers for all rule sets
     * @param handle a current session handle to the worklet service
     * @return the set of all rule set ids stored by the service (as XML). Specification
     * ids will be of the form "identifier:version:uri" (i.e. its 'full string')
     * @throws java.io.IOException if the service can't be reached
     */
    public String getRdrSetIDs(String handle) throws IOException {
        return executeGet(_wsURI, prepareParamMap("getRdrSetIDs", handle));
    }


    /**
     * Adds a complete (legacy) rule set, expressed as xml
     * @param specID the id of the specification the rule set is defined for
     * @param ruleSetXML the rule set to add
     * @param handle a current session handle to the worklet service
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
     * Adds a complete (legacy) rule set, expressed as xml
     * @param processName the name of the process the rule set is defined for
     * @param ruleSetXML the rule set to add
     * @param handle a current session handle to the worklet service
     * @return a success or error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String addRdrSet(String processName, String ruleSetXML, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("addRdrSet", handle);
        params.put("name", processName);
        params.put("ruleset", ruleSetXML);
        return executePost(_wsURI, params);
    }


    /**
      * Removes a rule set
      * @param specID the specification id of the rule set to remove
      * @param handle a current session handle to the worklet service
      * @return a success or error message
      * @throws java.io.IOException if the service can't be reached
      */
    public String removeRdrSet(YSpecificationID specID, String handle)
                throws IOException {
        return removeRdrSet(specID.toFullString(), handle);
    }


    /**
      * Removes a rule set
      * @param identifier the id of the rule set to remove
      * @param handle a current session handle to the worklet service
      * @return a success or error message
      * @throws java.io.IOException if the service can't be reached
      */
    public String removeRdrSet(String identifier, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("removeRdrSet", handle);
        params.put("identifier", identifier);
        return executePost(_wsURI, params);
    }


    /**
     * Gets the specified worklet, if loaded in the service
     * @param specID the Worklet specification id
     * @param handle a current session handle to the worklet service
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
     * @param handle a current session handle to the worklet service
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
     * Replaces a worklet instance (or instances) started for a work item with a new
     * instance (or instances), based on a 'pre' updated rule set
     * @param itemID the item id of the parent that launched the worklet(s)
     * @param handle a current session handle to the worklet service
     * @return a list of case ids of the new instances, or an error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String replaceWorklet(String itemID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("replace", handle);
        params.put("itemID", itemID);
        params.put("exType", RuleType.ItemSelection.toString());
        return executePost(_wsURI, params);
    }


    /**
     * Replaces a worklet instance (or instances) started for an exlet with a new
     * instance (or instances), based on a 'pre' updated rule set
     * @param caseID the parent case id
     * @param itemID the item id of the parent that launched the worklet(s). May be null
     *               (if case level exception)
     * @param handle a current session handle to the worklet service
     * @return a list of case ids of the new instances, or an error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String replaceWorklet(String caseID, String itemID, RuleType ruleType,
                                 String handle) throws IOException {
        Map<String, String> params = prepareParamMap("replace", handle);
        params.put("caseID", caseID);
        params.put("itemID", itemID);
        params.put("exType", ruleType.toString());
        return executePost(_wsURI, params);
    }


    /**
     * Gets the specified worklet, if loaded in the service
     * @param specKey the Worklet specification id key
     * @param handle a current session handle to the worklet service
     * @return the worklet xml, if found, or an error message if not
     * @throws java.io.IOException if the service can't be reached
     */
    public String removeWorklet(String specKey, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("removeWorklet", handle);
        params.put("key", specKey);
        return executeGet(_wsURI, params);
    }


    /**
     * Gets the specified worklet, if loaded in the service
     * @param specID the Worklet specification id
     * @param handle a current session handle to the worklet service
     * @return the worklet xml, if found, or an error message if not
     * @throws java.io.IOException if the service can't be reached
     */
    public String removeWorklet(YSpecificationID specID, String handle) throws IOException {
        return removeWorklet(specID.getKey(), handle);
    }


    /**
     * Gets a list of the names of all the worklets in the repository
     * @param handle a current session handle to the worklet service
     * @return an XML representation of the list of worklet names
     * @throws java.io.IOException if the service can't be reached
     */
    public String getWorkletNames(String handle) throws IOException {
        return executeGet(_wsURI, prepareParamMap("getWorkletNames", handle));
    }


    /**
     * Gets a list of specification descriptors of all the worklets in the repository
     * @param handle a current session handle to the worklet service
     * @return an XML representation of the list of worklet specifications
     * @throws java.io.IOException if the service can't be reached
     */
    public String getWorkletInfoList(String handle) throws IOException {
        return executeGet(_wsURI, prepareParamMap("getWorkletInfoList", handle));
    }


    /**
     * Gets an info set of all currently running worklets
     * @param handle a current session handle to the worklet service
     * @return an XML representation of the list of running worklets
     * @throws java.io.IOException if the service can't be reached
     */
    public String getRunningWorklets(String handle) throws IOException {
        return executeGet(_wsURI, prepareParamMap("getRunningWorklets", handle));
    }


    /**
     * Gets an info set of all stored worklets that are not referenced by any rule
     * @param handle a current session handle to the worklet service
     * @return an XML representation of the list of orphaned worklets
     * @throws java.io.IOException if the service can't be reached
     */
    public String getOrphanedWorklets(String handle) throws IOException {
        return executeGet(_wsURI, prepareParamMap("getOrphanedWorklets", handle));
    }


    /**
     * Loads a file or files into the worklet repository. If the path is a directory,
     * will load all files in that directory and its sub-directories of the specified
     * type.
     * @param path the file path to load
     * @param extn one of '.xrs' (for a rules file) or '.yawl' (for a worklet file)
     * @param handle a current session handle to the worklet service
     * @return a success message or a list of error messages
     * @throws java.io.IOException if the service can't be reached
     */
    public String loadFile(String path, String extn, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("loadFile", handle);
        params.put("path", path);
        params.put("type", extn);
        return executePost(_wsURI, params);
    }


    /**
     * Updates the task ids in a rule set for those they have been changed to via
     * a specification edit
     * @param specID the specification id for the rule set to update
     * @param updateMap the map of changes [oldID -> newID]
     * @param handle a current session handle to the worklet service
     * @return a success or error message
     * @throws java.io.IOException if the service can't be reached
     */
    public String updateRdrSetTaskIDs(YSpecificationID specID,
              Map<String, String> updateMap, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("updateRdrSetTaskIDs", handle);
        params.putAll(specID.toMap());
        String mapXML = StringUtil.wrap(new YAttributeMap(updateMap).toXMLElements(),
                "updates");
        params.put("updates", mapXML);
        return executePost(_wsURI, params);
    }


    public String getAdministrationTask(int id, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getAdministrationTask", handle);
        params.put("id", String.valueOf(id));
        return executeGet(_wsURI, params);
    }


    public String getAdministrationTasks(String handle) throws IOException {
        return executeGet(_wsURI, prepareParamMap("getAdministrationTasks", handle));
    }


    public String addAdministrationTask(String caseID, String itemID, String title,
                                        String scenario, String process, int taskType,
                                        String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addAdministrationTask", handle);
        params.put("caseid", caseID);
        if (itemID != null) {
            params.put("itemid", itemID);
        }
        params.put("title", title);
        params.put("scenario", scenario);
        params.put("process", process);
        params.put("tasktype", String.valueOf(taskType));
        return executePost(_wsURI, params);
    }


    public String removeAdministrationTask(int id, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("removeAdministrationTask", handle);
        params.put("id", String.valueOf(id));
        return executePost(_wsURI, params);
    }


    public String raiseCaseExternalException(String caseID, String trigger, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("raiseExternalException", handle);
        params.put("caseid", caseID);
        params.put("trigger", trigger);
        return executePost(_wsURI, params);
    }


    public String raiseItemExternalException(String itemID, String trigger, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("raiseExternalException", handle);
        params.put("itemid", itemID);
        params.put("trigger", trigger);
        return executePost(_wsURI, params);
    }


    public String getExternalTriggersForCase(String caseID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getExternalTriggers", handle);
        params.put("caseid", caseID);
        return executeGet(_wsURI, params);
    }


    public String getExternalTriggersForItem(String itemID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getExternalTriggers", handle);
        params.put("itemid", itemID);
        return executeGet(_wsURI, params);
    }


}