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

package org.yawlfoundation.yawl.editor.core.connection;

import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EngineBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceE.YLogGatewayClient;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * A wrapper class for getting data from the YAWL engine, with added caching
 * to handle when the connection becomes unavailable.
 * @author Michael Adams
 * @date 8/09/11
 */
public class YEngineConnection extends YConnection {

    // engine API client
    private InterfaceA_EnvironmentBasedClient _client;

    // caches for each of the various resource entities
    private Map<String, YAWLServiceReference> _serviceCache =
            new Hashtable<String, YAWLServiceReference>();
    private Map<String, YParameter[]> _paramCache = new Hashtable<String, YParameter[]>();
    private Map<String, String> _externalDbCache = new Hashtable<String, String>();

    // default url parts
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 8080;
    public static final String IA_PATH = "/yawl/ia";
    public static final String IB_PATH = "/yawl/ib";
    public static final String IE_PATH = "/yawl/ie";


    /*********************************************************************************/

    // Constructors
    public YEngineConnection() { }

    public YEngineConnection(String host, int port) throws MalformedURLException {
        super();
        setURL(new URL("http", host, port, IA_PATH));
    }

    public YEngineConnection(String urlStr) { super(urlStr); }

    public YEngineConnection(URL url) { super(url); }


    /**
     * Opens a connection to the engine. Usually not required to be called
     * directly, as connections are managed internally.
     * @return true if the connection is successfully opened.
     * @throws IOException if there's a problem connection to the engine.
     */
    public boolean connect() throws IOException {
        return super.connect(_client);
    }


    /**
     * Checks that a connection is open. Usually not required to be called
     * directly, as connections are managed internally.
     * @return true if the connection is open.
     */
    public boolean isConnected() {
        return super.isConnected(_client);
    }


    /**
     * Gets the client object maintaining the current connection. Usually not required
     * to be called directly, as connections are managed internally.
     * @return the current client object.
     */
    public InterfaceA_EnvironmentBasedClient getClient() { return _client; }


    /**
     * Gets the set of currently registered custom services from the engine.
     * @return If there is a current connection to the engine, a fresh set is
     * retrieved from the engine, the cache is updated, and the set is returned.
     * If a connection can't be established, or there is some problem retrieving a fresh
     * set, then the cached set is returned.
     * @throws IOException If there is some problem retrieving a fresh set, and the
     * cache is empty.
     */
    public Set<YAWLServiceReference> getRegisteredYAWLServices() {
        if (isConnected()) {
            Set<YAWLServiceReference> services = _client.getRegisteredYAWLServices(_handle);
            if (! services.isEmpty()) {
                updateServicesCache(services);
            }
            return services;
        }

        // couldn't connect, return cached
        return new HashSet<YAWLServiceReference>(_serviceCache.values());
    }


    /**
     * Gets the corresponding service object for the URI passed
     * @param uri the URI of the service to get
     * @return the service object, if found
     * problem connection to the the YAWL engine.
     */
    public YAWLServiceReference getService(String uri) {
        YAWLServiceReference service = _serviceCache.get(uri);
        if (service == null) {
            getRegisteredYAWLServices();                     // update cache from engine
            service = _serviceCache.get(uri);
        }
        return service;
    }


    /**
     * Gets a map of currently installed external data gateways from the engine, in
     * the form of [name, description]
     * @return If there is a current connection to the engine, a fresh map is
     * retrieved from the engine, the cache is updated, and the map is returned.
     * If a connection can't be established, or there is some problem retrieving a fresh
     * map, then the cached map is returned.
     * @throws IOException If there is some problem retrieving a fresh map, and the
     * cache is empty.
     */
    public Map<String, String> getExternalDataGateways() throws IOException {
        if (isConnected()) {
            String gatewayXML = _client.getExternalDBGateways(_handle);
            if (successful(gatewayXML)) {
                XNode root = new XNodeParser().parse(gatewayXML);      // <response>
                if (root != null) {
                    updateExternalDbCache(root);
                }
            }
        }
        return _externalDbCache;
    }



    /**
     * Gets an array of required task-level parameters for a service from the engine.
     * @param serviceURI the URI of the service to get the parameters for.
     * @return If there is a current connection to the engine, a fresh array is
     * retrieved from the engine, the cache is updated, and the array is returned.
     * If a connection can't be established, or there is some problem retrieving a fresh
     * array, then the cached array is returned.
     * @throws IOException If there is some problem retrieving a fresh array, and the
     * cache is empty.
     */
    public YParameter[] getParametersForService(String serviceURI)
            throws IOException {
        YAWLServiceReference service = getService(serviceURI);
        if (service == null) throw new IOException("Unable to locate service");

        YParameter[] parameters;
        try {
            parameters = new InterfaceB_EngineBasedClient().getRequiredParamsForService(service);
            _paramCache.put(serviceURI, parameters);
        }
        catch (Exception e) {
            parameters = _paramCache.get(serviceURI);        // fallback - try the cache
            if (parameters == null) throw new IOException(e.getMessage());
        }

        return parameters;
    }


    public String uploadSpecification(YSpecification specification) throws IOException {
        if (isConnected()) {
            return _client.uploadSpecification(
                    getSpecificationXML(specification), _handle);
        }
        throw new IOException("Cannot connect to YAWL Engine");
    }


    public boolean unloadSpecification(YSpecificationID specID) throws IOException {
        if (isConnected()) {
            String result = _client.unloadSpecification(specID, _handle);
            if (! successful(result)) {
                throw new IOException(StringUtil.unwrap(result));
            }
            return true;
        }
        throw new IOException("Cannot connect to YAWL Engine");
    }


    public List<SpecificationData> getLoadedSpecificationList() throws IOException {
        if (isConnected()) {
            return getIbClient().getSpecificationList(_handle);
        }
        throw new IOException("Cannot connect to YAWL Engine");
    }


    public String getSpecification(YSpecificationID specID) throws IOException {
        if (isConnected()) {
            return getIbClient().getSpecification(specID, _handle);
        }
        throw new IOException("Cannot connect to YAWL Engine");
    }


    public Set<YSpecificationID> getAllLoadedVersions(YSpecificationID specID)
            throws IOException {
        if (isConnected()) {
            Set<YSpecificationID> versions = new HashSet<YSpecificationID>();
            for (SpecificationData specData : getLoadedSpecificationList()) {
                YSpecificationID thisID = specData.getID();
                if (thisID.getIdentifier().equals(specID.getIdentifier())) {
                    versions.add(thisID);
                }
            }
            return versions;
        }
        throw new IOException("Cannot connect to YAWL Engine");
    }


    public Set<String> getAllRunningCases(YSpecificationID specID)
            throws IOException {
        if (isConnected()) {
            String casesXML = getIbClient().getCases(specID, _handle);
            if (successful(casesXML)) {
                XNode casesNode = new XNodeParser().parse(casesXML);
                if (casesNode != null) {
                    Set<String> cases = new HashSet<String>();
                    for (XNode caseNode : casesNode.getChildren()) {
                        cases.add(caseNode.getText());
                    }
                    return cases;
                }
                throw new IOException("Invalid cases list returned from YAWL Engine");
            }
            throw new IOException(StringUtil.unwrap(casesXML));
        }
        throw new IOException("Cannot connect to YAWL Engine");
    }


    public String cancelAllCases(YSpecificationID specID) throws IOException {
        StringBuilder result = new StringBuilder("Cases cancelled: ");
        Set<String> cases = getAllRunningCases(specID);
        for (String caseID : cases) {
            if (cancelCase(caseID)) {
                result.append(caseID).append(" ");
            }
        }
        return result.toString();
    }


    public void unloadAllVersions(YSpecificationID specID, boolean cancelCases)
            throws IOException {
        for (YSpecificationID thisID : getAllLoadedVersions(specID)) {
            if (cancelCases) {
                cancelAllCases(thisID);
            }
            unloadSpecification(thisID);
        }
    }

    public String launchCase(YSpecificationID specID, String caseParams,
                           YLogDataItemList logList) throws IOException {
        if (isConnected()) {
            return getIbClient().launchCase(specID, caseParams, logList, _handle);
        }
        throw new IOException("Cannot connect to YAWL Engine");
    }


    public boolean cancelCase(String caseID) throws IOException {
        if (isConnected()) {
            return successful(getIbClient().cancelCase(caseID, _handle));
        }
        throw new IOException("Cannot connect to YAWL Engine");
    }


    public InterfaceB_EnvironmentBasedClient getIbClient() {
        String iaUri = _client.getBackEndURI();
        String ibUri = iaUri.replace(IA_PATH, IB_PATH);
        return new InterfaceB_EnvironmentBasedClient(ibUri);
    }


    public YLogGatewayClient getIeClient() {
        String iaUri = _client.getBackEndURI();
        String ieUri = iaUri.replace(IA_PATH, IE_PATH);
        return new YLogGatewayClient(ieUri);
    }


    /**
     * Checks an xml string returned via the APi for error messages
     * @param xml the returned xml string to check
     * @return true if the xml contains no error messages
     */
    private boolean successful(String xml) {
        return (_client != null) && _client.successful(xml);
    }


    /**
     * Rebuilds the services cache with a fresh set of services
     * @param services the set of services to cache
     */
    private void updateServicesCache(Set<YAWLServiceReference> services) {
        _serviceCache.clear();
        for (YAWLServiceReference service : services) {
             _serviceCache.put(service.getURI(), service);
        }
    }


    /**
     * Rebuilds the external data gateway cache with a fresh set of gateways
     * @param root the set of gateway elements to cache
     */
    private void updateExternalDbCache(XNode root) {
        _externalDbCache.clear();
        XNode gatewayNodes = root.getChild();              // <gateways>
        for (XNode gateway : gatewayNodes.getChildren()) {
            String name = gateway.getChildText("name");
            String subName = name.substring(name.lastIndexOf('.') +1);
            _externalDbCache.put(subName, gateway.getChildText("description"));
        }
    }


    private String getSpecificationXML(YSpecification specification) {
        return YMarshal.marshal(specification);
    }


    /**
     * Called from the super class to do any initialisation tasks
     */
    protected void init() {
        _client = new InterfaceA_EnvironmentBasedClient(_url.toExternalForm());
    }

    protected String getURLFilePath() {
        return IA_PATH;
    }

}
