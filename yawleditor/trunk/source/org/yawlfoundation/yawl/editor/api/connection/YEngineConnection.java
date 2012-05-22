package org.yawlfoundation.yawl.editor.api.connection;

import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EngineBasedClient;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

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

    // default url
    public static final String DEFAULT_URL = "http://localhost:8080/yawl/ia";


    /*********************************************************************************/

    // Constructors
    public YEngineConnection() {
        super();
        try {
            setURL(DEFAULT_URL);
        }
        catch (MalformedURLException mue) {
            setURL((URL) null);
        }
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
    public Set<YAWLServiceReference> getRegisteredYAWLServices() throws IOException {
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
     * @throws IOException if the service is not currently cached, and there's a
     * problem connection to the the YAWL engine.
     */
    public YAWLServiceReference getService(String uri) throws IOException {
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



    /**
     * Called from the super class to do any initialisation tasks
     */
    protected void init() {
        _client = new InterfaceA_EnvironmentBasedClient(_url.toExternalForm());
    }

}
