/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.core;

import org.yawlfoundation.yawl.editor.core.connection.YEngineConnection;
import org.yawlfoundation.yawl.editor.core.connection.YResourceConnection;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceE.YLogGatewayClient;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.resourcing.AbstractSelector;
import org.yawlfoundation.yawl.resourcing.codelets.CodeletInfo;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * A wrapper class of static methods for the connection API
 * (to a running engine and resource service)
 * @author Michael Adams
 * @date 14/09/11
 */
public class YConnector {

    private static final YEngineConnection _engConn = new YEngineConnection();
    private static final YResourceConnection _resConn = new YResourceConnection();

    // ensure this is never instantiated - static methods only
    private YConnector() {}


    public static boolean isEngineConnected() {
        return _engConn.isConnected();
    }

    public static boolean isResourceConnected() {
        return _resConn.isConnected();
    }

    public static void disconnectEngine() { _engConn.disconnect(); }

    public static void disconnectResource() { _resConn.disconnect(); }


    public static void setUserID(String id) {
        _engConn.setUserID(id);
        _resConn.setUserID(id);
    }

    public static void setPassword(String pw) {
        _engConn.setPassword(pw);
        _resConn.setPassword(pw);
    }

    public static void setEngineURL(URL url) { _engConn.setURL(url); }

    public static void setEngineURL(String url) throws MalformedURLException {
        _engConn.setURL(url);
    }

    public static void setEngineURL(String host, int port) throws MalformedURLException {
        _engConn.setURL(host, port);
    }

    public static void setResourceURL(URL url) { _resConn.setURL(url); }

    public static void setResourceURL(String url) throws MalformedURLException {
        _resConn.setURL(url);
    }

    public static void setResourceURL(String host, int port) throws MalformedURLException {
        _resConn.setURL(host, port);
    }


    /**
     * Checks whether a valid connection can be made with the parameters passed.
     * @param host the host's base URL
     * @param port the port on the host
     * @param user the userid
     * @param password the password
     * @return true if the parameters can be used to create a valid connection
     */
    public static boolean testEngineParameters(String host, int port,
                                               String user, String password)
            throws MalformedURLException {
        YEngineConnection tempConn = new YEngineConnection();
        tempConn.setURL(host, port);
        return tempConn.testConnection(user, password);
    }

    public static boolean testEngineParameters(String url, String user, String password)
            throws MalformedURLException {
        YEngineConnection tempConn = new YEngineConnection();
        tempConn.setURL(url);
        return tempConn.testConnection(user, password);
    }

    public static boolean testEngineParameters(URL url, String user, String password)
             throws MalformedURLException {
         YEngineConnection tempConn = new YEngineConnection();
         tempConn.setURL(url);
        return tempConn.testConnection(user, password);
     }


    /**
     * Checks whether a valid connection can be made with the parameters passed.
     * @param url the connection's URL
     * @param user the userid
     * @param password the password
     * @return true if the parameters can be used to create a valid connection
     */
    public static boolean testResourceServiceParameters(String url, String user,
            String password) throws MalformedURLException {
        YResourceConnection tempConn = new YResourceConnection();
        tempConn.setURL(url);
        return tempConn.testConnection(user, password);
    }

    public static boolean testResourceServiceParameters(String host, int port,
            String user, String password) throws MalformedURLException {
        YResourceConnection tempConn = new YResourceConnection();
        tempConn.setURL(host, port);
        return tempConn.testConnection(user, password);
    }

    public static boolean testResourceServiceParameters(URL url, String user,
             String password) throws MalformedURLException {
        YResourceConnection tempConn = new YResourceConnection();
        tempConn.setURL(url);
        return tempConn.testConnection(user, password);
    }


    public static InterfaceA_EnvironmentBasedClient getInterfaceAClient() {
        return _engConn.getClient();
    }


    public static InterfaceB_EnvironmentBasedClient getInterfaceBClient() {
        return _engConn.getIbClient();
    }


    public static YLogGatewayClient getInterfaceEClient() {
        return _engConn.getIeClient();
    }


    public static ResourceGatewayClient getResourceClient() {
        return _resConn.getClient();
    }

    public static Map<String, String> getExternalDataGateways() throws IOException {
        return _engConn.getExternalDataGateways();
    }

    public static Set<YAWLServiceReference> getServices() {
        return _engConn.getRegisteredYAWLServices();
    }
    
    public static YAWLServiceReference getService(String uri) {
        return _engConn.getService(uri);
    }

    public static String uploadSpecification(YSpecification specification)
            throws IOException {
        return _engConn.uploadSpecification(specification);
    }

    public static boolean unloadSpecification(YSpecificationID specID)
            throws IOException {
        return _engConn.unloadSpecification(specID);
    }

    public static Set<YSpecificationID> getAllLoadedVersions(YSpecificationID specID)
            throws IOException {
        return _engConn.getAllLoadedVersions(specID);
    }

    public static Set<String> getAllRunningCases(YSpecificationID specID)
            throws IOException {
        return _engConn.getAllRunningCases(specID);
    }

    public static String cancelAllCases(YSpecificationID specID) throws IOException {
        return _engConn.cancelAllCases(specID);
    }

    public static void unloadAllVersions(YSpecificationID specID, boolean cancelCases)
            throws IOException {
        _engConn.unloadAllVersions(specID, cancelCases);
    }


    public static String launchCase(YSpecificationID specID, String caseParams,
                                    YLogDataItemList logList) throws IOException {
        return _engConn.launchCase(specID, caseParams, logList);
    }

    public static boolean cancelCase(String caseID) throws IOException {
        return _engConn.cancelCase(caseID);
    }


    public static List<String> getParticipantIDs() {
        return _resConn.getParticipantIDs();
    }

    public static List<String> getRoleIDs() {
        return _resConn.getRoleIDs();
    }

    public static List<CodeletInfo> getCodelets() throws IOException {
        return _resConn.getCodelets();
    }

    public static List<Capability> getCapabilities() throws IOException {
        return _resConn.getCapabilities();
    }

    public static List<Position> getPositions() throws IOException {
        return _resConn.getPositions();
    }

    public static List<OrgGroup> getOrgGroups() throws IOException {
        return _resConn.getOrgGroups();
    }

    public static List<NonHumanResource> getNonHumanResources() throws IOException {
        return _resConn.getNonHumanResources();
    }

    public static List<NonHumanCategory> getNonHumanCategories() throws IOException {
        return _resConn.getNonHumanCategories();
    }

    public static List<Participant> getParticipants() throws IOException {
        return _resConn.getParticipants();
    }

    public static List<Role> getRoles() throws IOException {
        return _resConn.getRoles();
    }

    public static boolean hasResources() {
        try {
            return ! (getParticipants().isEmpty() && getRoles().isEmpty());
        }
        catch (IOException ioe) {
            return false;
        }
    }


    public static Map<String, YAWLServiceReference> getServicesMap() {
        Map<String, YAWLServiceReference> map = new Hashtable<String, YAWLServiceReference>();
        for (YAWLServiceReference service : getServices()) {
            map.put(service.getServiceID(), service);
        }
        return map;
    }


    public static Map<String, Participant> getParticipantMap() {
        Map<String, Participant> map = new Hashtable<String, Participant>();
        try {
            for (Participant participant : getParticipants()) {
                 map.put(participant.getID(), participant);
            }
        }
        catch (IOException ioe) {
            // fall though to empty map
        }
        return map;
    }


    public static Map<String, Role> getRoleMap() {
        Map<String, Role> map = new Hashtable<String, Role>();
        try {
            for (Role role : getRoles()) {
                 map.put(role.getID(), role);
            }
        }
        catch (IOException ioe) {
            // fall though to empty map
        }
        return map;
    }


    public static Map<String, NonHumanResource> getNonHumanResourceMap() {
        Map<String, NonHumanResource> map = new Hashtable<String, NonHumanResource>();
        try {
            for (NonHumanResource resource : getNonHumanResources()) {
                 map.put(resource.getID(), resource);
            }
        }
        catch (IOException ioe) {
            // fall though to empty map
        }
        return map;
    }


    public static List<AbstractSelector> getAllocators() throws IOException {
        return _resConn.getAllocators();
    }


    public static List<AbstractSelector> getFilters() throws IOException {
        return _resConn.getFilters();
    }

    public static List<AbstractSelector> getConstraints() throws IOException {
        return _resConn.getConstraints();
    }


    public static List<YParameter> getCodeletParameters(String codeletName)
            throws IOException {
        return _resConn.getCodeletParameters(codeletName);
    }


    public static List<YParameter> getServiceParameters(String serviceURI)
            throws IOException {
        return Arrays.asList(_engConn.getParametersForService(serviceURI));
    }

}
