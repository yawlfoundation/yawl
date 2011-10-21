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

package org.yawlfoundation.yawl.cost;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;

import java.io.IOException;
import java.util.Map;

/**
 * An API to be used by clients that want to retrieve data from the cost service.
 *
 *  @author Michael Adams
 *  11/07/2011
 */

public class CostGatewayClient extends Interface_Client {

    /** the uri of the YAWL Cost Service
     * a default would be "http://localhost:8080/costService/"
     */
    private String _costURI;

    /** the only constructor
     * @param uri the uri of the YAWL Resource Service's log gateway
     */
    public CostGatewayClient(String uri) {
        _costURI = uri ;
    }


    /*******************************************************************************/

    // GET METHODS - returning String //

    private String executeGet(String action, YSpecificationID specID, String taskID)
            throws IOException {
        return executeGet(_costURI, prepareParams(action, specID, taskID));
    }


    private Map<String, String> prepareParams(String action, YSpecificationID specID,
                                              String taskID) {
        Map<String, String> params = prepareParamMap(action, null);
        params.putAll(specID.toMap());
        params.put("taskid", taskID);
        return params;
    }


    /*******************************************************************************/

    /**
     * Gets an XML list of all cost functions for the specified specification - task
     * combination.
     * @param specID the specification identifier
     * @param taskID the task identifier (may be null, in which case only the case level
     * functions are required)
     * @return an XML list of the cost functions requested, or an appropriate failure
     * message.
     * @throws IOException if there's a problem connecting to the service
     */
    public String getFunctionList(YSpecificationID specID, String taskID) throws IOException {
        return executeGet("getFunctionList", specID, taskID);
    }


    /**
      * Gets an XML list of all the fixed costs for the specified specification - task
      * combination.
      * @param specID the specification identifier
      * @param taskID the task identifier (may be null, in which case only the case level
      * costs are required)
      * @return an XML list of the costs requested, or an appropriate failure message.
     * @throws IOException if there's a problem connecting to the service
     */
    public String getFixedCosts(YSpecificationID specID, String taskID) throws IOException {
        return executeGet("getFixedCosts", specID, taskID);
    }


    /**
     * Calculates the cost of an activity.
     * @param specID the specification identifier
     * @param taskID the task identifier (may be null, in which case only the case level
     * costs are required)
     * @param costParams an XML document containing data variables to be used in the
     * calculation (eg. resources, rates, time durations etc.)
     * @return an XML document containing the actual result of applying the costParams
     * to the relevant cost functions.
     * @throws IOException if there's a problem connecting to the service
     */
    public String calcCost(YSpecificationID specID, String taskID, String costParams)
            throws IOException {
        Map<String, String> params = prepareParams("calcCost", specID, taskID);
        params.put("costparams", costParams);
        return executeGet(_costURI, params);
    }

}