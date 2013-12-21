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

package org.yawlfoundation.yawl.cost.interfce;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.util.PasswordEncryptor;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * An API to be used by clients that want to retrieve data from the cost service.
 *
 * @author Michael Adams
 *         11/07/2011
 */

public class CostGatewayClient extends Interface_Client {

    /**
     * the uri of the YAWL Cost Service
     * a default would be "http://localhost:8080/costService/gateway"
     */
    protected String _costURI;

    /**
     * the constructors
     *
     * @param uri the uri of the YAWL Cost Service
     */
    public CostGatewayClient(String uri) {
        _costURI = uri;
    }


    public CostGatewayClient() {
        _costURI = "http://localhost:8080/costService/gateway";
    }


    public void setURI(String uri) { _costURI = uri; }


    /*******************************************************************************/

    /**
     * Connects an external entity to the cost service
     *
     * @param userID   the userid
     * @param password the corresponding password
     * @return a sessionHandle if successful, or a failure message if not
     * @throws IOException if the service can't be reached
     */
    public String connect(String userID, String password) throws IOException {
        Map<String, String> params = prepareParamMap("connect", null);
        params.put("userid", userID);
        params.put("password", PasswordEncryptor.encrypt(password, null));
        return executeGet(_costURI, params);
    }


    /**
     * Check that a session handle is active
     *
     * @param handle the session handle to check
     * @return "true" if the id is valid, "false" if otherwise
     * @throws IOException if the service can't be reached
     */
    public String checkConnection(String handle) throws IOException {
        return executeGet(_costURI, prepareParamMap("checkConnection", handle));
    }


    /**
     * Disconnects an external entity from the cost service
     *
     * @param handle the sessionHandle to disconnect
     * @throws IOException if the service can't be reached
     */
    public void disconnect(String handle) throws IOException {
        executePost(_costURI, prepareParamMap("disconnect", handle));
    }


    /**
     * Loads a cost model into the service
     *
     * @param modelXML the model to import
     * @param handle   a current sessionHandle to the cost service
     * @return a success or error message
     * @throws IOException
     */
    public String importModel(String modelXML, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("importModel", handle);
        params.put("model", modelXML);
        return executePost(_costURI, params);
    }


    /**
     * Loads a number of cost models into the service
     *
     * @param modelsXML the set of models to import, represented as an set of valid
     *                  cost mode XML child elements contained within an outer element
     *                  called 'costmodels'
     * @param handle    a current sessionHandle to the cost service
     * @return a success or error message
     * @throws IOException
     */
    public String importModels(String modelsXML, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("importModels", handle);
        params.put("models", modelsXML);
        return executePost(_costURI, params);
    }


    /**
     * Gets the cost models for a specification from the service
     *
     * @param specID the id of the specification to get the models for
     * @param handle a current sessionHandle to the cost service
     * @return a String XML representation of the models if successful or
     *         an error message if not
     * @throws IOException
     */
    public String exportModels(YSpecificationID specID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("exportModels", handle);
        params.putAll(specID.toMap());
        return executeGet(_costURI, params);
    }


    /**
     * Gets a cost model from the service
     *
     * @param specID  the id of the specification to get the model for
     * @param modelID the id of the model to get
     * @param handle  a current sessionHandle to the cost service
     * @return a String XML representation of the model if successful or
     *         an error message if not
     * @throws IOException
     */
    public String exportModel(YSpecificationID specID, String modelID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("exportModel", handle);
        params.putAll(specID.toMap());
        params.put("id", modelID);
        return executeGet(_costURI, params);
    }


    /**
     * Removes a cost model from the service
     *
     * @param specID  the id of the specification to remove the model for
     * @param modelID the id of the model to remove
     * @param handle  a current sessionHandle to the cost service
     * @return an XML success or error message
     * @throws IOException
     */
    public String removeModel(YSpecificationID specID, String modelID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("removeModel", handle);
        params.putAll(specID.toMap());
        params.put("id", modelID);
        return executeGet(_costURI, params);
    }


    /**
     * Clears all the cost model for a particular specification from the service
     *
     * @param specID the id of the specification to remove the models for
     * @param handle a current sessionHandle to the cost service
     * @return an XML success or error message
     * @throws IOException
     */
    public String clearModels(YSpecificationID specID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("clearModels", handle);
        params.putAll(specID.toMap());
        return executeGet(_costURI, params);
    }


    /**
     * @param specID
     * @param withData
     * @param handle   a current sessionhandle to the cost service
     * @return
     * @throws IOException
     */
    public String getAnnotatedLog(YSpecificationID specID, boolean withData, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getAnnotatedLog", handle);
        params.putAll(specID.toMap());
        params.put("withData", String.valueOf(withData));
        return executeGet(_costURI, params);
    }


    public boolean evaluate(YSpecificationID specID, String caseID, String predicate,
                            String handle) throws IOException {
        Map<String, String> params = prepareParamMap("evaluate", handle);
        params.putAll(specID.toMap());
        params.put("id", caseID);
        params.put("predicate", predicate);
        return executeGet(_costURI, params).equals("true");
    }

    public double calculate(YSpecificationID specID, String caseID, String predicate,
                            String handle) throws IOException {
        Map<String, String> params = prepareParamMap("calculate", handle);
        params.putAll(specID.toMap());
        params.put("id", caseID);
        params.put("predicate", predicate);
        String result = executeGet(_costURI, params);
        try {
            return Double.parseDouble(result);
        }
        catch (NumberFormatException nfe) {
            return -1;
        }
    }

    /**
     * Gets an XML list of all cost functions for the specified specification - task
     * combination.
     *
     * @param specID   the specification identifier
     * @param taskName the task identifier (may be null, in which case only the case level
     *                 functions are required)
     * @param handle   a current sessionhandle to the cost service
     * @return an XML list of the cost functions requested, or an appropriate failure
     *         message.
     * @throws IOException if there's a problem connecting to the service
     */
    public String getFunctionList(YSpecificationID specID, String taskName, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getFunctionList", handle);
        params.putAll(specID.toMap());
        params.put("taskname", taskName);
        return executePost(_costURI, params);
    }


    /**
     * Gets an XML list of all the fixed costs for the specified specification - task
     * combination.
     *
     * @param specID   the specification identifier
     * @param taskName the task identifier (may be null, in which case only the case level
     *                 costs are required)
     * @param handle   a current sessionhandle to the cost service
     * @return an XML list of the costs requested, or an appropriate failure message.
     * @throws IOException if there's a problem connecting to the service
     */
    public String getFixedCosts(YSpecificationID specID, String taskName, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getFixedCosts", handle);
        params.putAll(specID.toMap());
        params.put("taskname", taskName);
        return executePost(_costURI, params);
    }


    /**
     * Gets the cost of performing an activity for each resource in a Set.
     *
     * @param specID    the specification identifier
     * @param taskName  the task name (may be null, in which case only the case level
     *                  costs are required)
     * @param resources an XML document containing participant ids
     * @param handle    a current sessionhandle to the cost service
     * @return an XML document containing the actual result of applying the costParams
     *         to the relevant cost functions.
     * @throws IOException if there's a problem connecting to the service
     */
    public String getResourceCosts(YSpecificationID specID, String taskName,
                                   String resources, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getResourceCosts", handle);
        params.putAll(specID.toMap());
        params.put("taskname", taskName);
        params.put("resources", resources);
        return executeGet(_costURI, params);
    }


    /**
     * @param specID    the specification identifier
     * @param taskName  the task name
     * @param resources the set of participant ids to get costs for
     * @param handle    a current sessionhandle to the cost service
     * @return
     * @throws IOException
     */
    public String getResourceCosts(YSpecificationID specID, String taskName,
                                   Set<String> resources, String handle) throws IOException {
        XNode node = new XNode("resources");
        for (String resource : resources) {
            node.addChild("id", resource);
        }
        return getResourceCosts(specID, taskName, node.toString(), handle);
    }


}