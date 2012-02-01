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

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.util.PasswordEncryptor;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 11/10/2010
 */
public class ResourceCalendarGatewayClient extends Interface_Client {

    /** the uri of the resource service's __ calendar gateway__
     * a default would be "http://localhost:8080/resourceService/calendarGateway"
     */
    private String _serviceURI ;

    /** empty constructor */
    public ResourceCalendarGatewayClient() {
        super();
    }

    /** constructor
     * @param uri the uri of the resourceService's gateway
     */
    public ResourceCalendarGatewayClient(String uri) {
        super();
        _serviceURI = uri ;
    }

    /*******************************************************************************/

    /**
     * Connects an external entity to the resource service
     * @param userID the userid
     * @param password the corresponding password
     * @return a sessionHandle if successful, or a failure message if not
     * @throws java.io.IOException if the service can't be reached
     */
    public String connect(String userID, String password) throws IOException {
        Map<String, String> params = prepareParamMap("connect", null);
        params.put("userid", userID);
        params.put("password", PasswordEncryptor.encrypt(password, null));
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Check that a session handle is active
     * @param handle the session handle to check
     * @return "true" if the id is valid, "false" if otherwise
     * @throws IOException if the service can't be reached
     */
    public String checkConnection(String handle) throws IOException {
        return executeGet(_serviceURI, prepareParamMap("checkConnection", handle)) ;
    }


    /**
     * Disconnects an external entity from the resource service
     * @param handle the sessionHandle to disconnect
     * @throws IOException if the service can't be reached
     */
    public void disconnect(String handle) throws IOException {
        executePost(_serviceURI, prepareParamMap("disconnect", handle));
    }


    /**
     * Registers a URI as a listener for status change events. Note that only changes
     * that affect the owner of the handle will receive events via the URI.
     * @param uri the uri to which status change announcements are made
     * @param handle a valid session handle
     * @return a diagnostic message
     * @throws IOException if the service can't be reached
     */
    public String registerStatusChangeListener(String uri, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("registerStatusChangeListener", handle);
        params.put("uri", uri);
        return executeGet(_serviceURI, params);
    }


    /**
     * Removes a URI as a listener for status change events.
     * @param uri the uri to remove as a listener for status change announcements
     * @param handle a valid session handle
     * @throws IOException if the service can't be reached
     */
    public void removeStatusChangeListener(String uri, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("removeStatusChangeListener", handle);
        params.put("uri", uri);
        executePost(_serviceURI, params);
    }


    /******************************************************************************/

    /**
     * Gets the periods (timeslots) between 'from' and 'to' when the resource
     * described by the resource record xml, is available
     * @param resourceXML the resource record to check for availability
     * @param from the start of the date/time range
     * @param to the end of the date/time range
     * @param handle a valid session handle
     * @return an xml string of the resource's available timeslots
     * @throws IOException if the service can't be reached
     */
    public String getAvailability(String resourceXML, long from, long to,
                                          String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getAvailability", handle);
        params.put("resourceXML", resourceXML);
        params.put("from", String.valueOf(from));
        params.put("to", String.valueOf(to));
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets the periods (timeslots) between 'from' and 'to' when the resource
     * described by the resource record xml, is available
     * @param resourceXML the resource record to check for availability
     * @param fromDate the start of the date/time range
     * @param toDate the end of the date/time range
     * @param handle a valid session handle
     * @return an xml string of the resource's available timeslots
     * @throws IOException if the service can't be reached
     */
    public String getAvailability(String resourceXML, Date fromDate, Date toDate,
                                          String handle) throws IOException {
        long from = (fromDate != null) ? fromDate.getTime() : -1;
        long to = (toDate != null) ? toDate.getTime() : -1;
        return getAvailability(resourceXML, from, to, handle);
    }


    /**
     * Gets the periods (timeslots) between 'from' and 'to' when the resource is
     * available
     * @param resourceID the resource to check for availability
     * @param from the start of the date/time range
     * @param to the end of the date/time range
     * @param handle a valid session handle
     * @return an xml string of the resource's available timeslots
     * @throws IOException if the service can't be reached
     */
    public String getResourceAvailability(String resourceID, long from, long to,
                                          String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getResourceAvailability", handle);
        params.put("id", resourceID);
        params.put("from", String.valueOf(from));
        params.put("to", String.valueOf(to));
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets the periods (timeslots) between 'from' and 'to' when the resource is
     * available
     * @param resourceID the resource to check for availability
     * @param fromDate the start of the date/time range
     * @param toDate the end of the date/time range
     * @param handle a valid session handle
     * @return an xml string of the resource's available timeslots
     * @throws IOException if the service can't be reached
     */
    public String getResourceAvailability(String resourceID, Date fromDate, Date toDate,
                                          String handle) throws IOException {
        long from = (fromDate != null) ? fromDate.getTime() : -1;
        long to = (toDate != null) ? toDate.getTime() : -1;
        return getResourceAvailability(resourceID, from, to, handle);
    }


    /**
     * Gets all the current reservations held for a resource within a time period
     * @param resourceXML the resource(s) to get the reservations for
     * @param from the start of the date/time range
     * @param to the end of the date/time range
     * @param handle a valid session handle
     * @return an xml string of the resource's reservations
     * @throws IOException if the service can't be reached
     */
    public String getReservations(String resourceXML, long from, long to,
                                          String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getReservations", handle);
        params.put("resource", resourceXML);
        params.put("from", String.valueOf(from));
        params.put("to", String.valueOf(to));
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets all the current reservations held for a resource within a time period
     * @param resourceXML the resource(s) to get the reservations for
     * @param fromDate the start of the date/time range
     * @param toDate the end of the date/time range
     * @param handle a valid session handle
     * @return an xml string of the resource's reservations
     * @throws IOException if the service can't be reached
     */
    public String getReservations(String resourceXML, Date fromDate, Date toDate,
                                          String handle) throws IOException {
        long from = (fromDate != null) ? fromDate.getTime() : -1;
        long to = (toDate != null) ? toDate.getTime() : -1;
        return getReservations(resourceXML, from, to, handle);
    }


    /**
     * Saves all the reservations within a UtilisationPlan (where possible)
     * @param planXML the UtilisationPlan containing the reservations
     * @param checkOnly if true, will report on the effects only - won't update the
     * reservations
     * @param handle a valid session handle
     * @return an passed xml plan, updated with messages where appropriate
     * @throws IOException if the service can't be reached
     */
    public String saveReservations(String planXML, boolean checkOnly, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("saveReservations", handle);
        params.put("plan", planXML);
        params.put("checkOnly", String.valueOf(checkOnly));
        return executeGet(_serviceURI, params) ;
    }







}
