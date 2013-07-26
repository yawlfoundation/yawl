package org.yawlfoundation.yawl.editor.core;

import org.yawlfoundation.yawl.editor.core.connection.YEngineConnection;
import org.yawlfoundation.yawl.editor.core.connection.YResourceConnection;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient;
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

    public static void setEngineUserID(String id) { _engConn.setUserID(id); }
    public static void setEnginePassword(String pw) { _engConn.setPassword(pw); }
    public static void setEngineURL(URL url) { _engConn.setURL(url); }
    public static void setEngineURL(String url) { _engConn.setURL(makeURL(url)); }

    public static void setResourceUserID(String id) { _resConn.setUserID(id); }
    public static void setResourcePassword(String pw) { _resConn.setPassword(pw); }
    public static void setResourceURL(URL url) { _resConn.setURL(url); }
    public static void setResourceURL(String url) { _resConn.setURL(makeURL(url)); }


    /**
     * Checks whether a valid connection can be made with the parameters passed.
     * @param url the connection's URL
     * @param userid the userid
     * @param password the password
     * @return true if the parameters can be used to create a valid connection
     */
    public static boolean testEngineParameters(String url, String userid, String password) {
        YEngineConnection tempConn = new YEngineConnection(url);
        tempConn.setUserID(userid);
        tempConn.setPassword(password);
        return tempConn.isConnected();
    }


    /**
     * Checks whether a valid connection can be made with the parameters passed.
     * @param url the connection's URL
     * @param userid the userid
     * @param password the password
     * @return true if the parameters can be used to create a valid connection
     */
    public static boolean testResourceServiceParameters(String url, String userid,
                                                        String password) {
        YResourceConnection tempConn = new YResourceConnection(url);
        tempConn.setUserID(userid);
        tempConn.setPassword(password);
        return tempConn.isConnected();
    }


    public static InterfaceA_EnvironmentBasedClient getEngineClient() {
        return _engConn.getClient();
    }

    public static ResourceGatewayClient getResourceClient() {
        return _resConn.getClient();
    }

    public static Map<String, String> getExternalDataGateways() throws IOException {
        return _engConn.getExternalDataGateways();
    }

    public static Set<YAWLServiceReference> getServices() throws IOException {
        return _engConn.getRegisteredYAWLServices();
    }
    
    public static YAWLServiceReference getService(String uri) throws IOException {
        return _engConn.getService(uri);
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
        try {
            for (YAWLServiceReference service : getServices()) {
                 map.put(service.getServiceID(), service);
            }
        }
        catch (IOException ioe) {
            // fall though to empty map
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


    private static URL makeURL(String url) {
        if (url != null) {
            try {
                return new URL(url);
            }
            catch (MalformedURLException mue) {
                // fallthrough to null
            }
        }
        return null;
    }

}
