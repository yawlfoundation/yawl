package org.yawlfoundation.yawl.editor.api.connection;

import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.resourcing.AbstractSelector;
import org.yawlfoundation.yawl.resourcing.codelets.CodeletInfo;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayClient;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayClientAdapter;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper class for getting data from the YAWL resource service, with added caching
 * to handle when the connection becomes unavailable.
 * @author Michael Adams
 * @date 8/09/11
 */
public class YResourceConnection extends YConnection {

    // resource service API clients
    private ResourceGatewayClient _client;
    private ResourceGatewayClientAdapter _adapter;

    // caches for each of the various resource entities
    private List<Participant> _participantCache = new ArrayList<Participant>();
    private List<Role> _roleCache = new ArrayList<Role>();
    private List<Capability> _capabilityCache = new ArrayList<Capability>();
    private List<Position> _positionCache = new ArrayList<Position>();
    private List<OrgGroup> _orgGroupCache = new ArrayList<OrgGroup>();
    private List<NonHumanResource> _nonHumanCache = new ArrayList<NonHumanResource>();
    private List<NonHumanCategory> _nhCategoryCache = new ArrayList<NonHumanCategory>();
    private List<AbstractSelector> _allocatorCache = new ArrayList<AbstractSelector>();
    private List<AbstractSelector> _filterCache = new ArrayList<AbstractSelector>();
    private List<CodeletInfo> _codeletCache = new ArrayList<CodeletInfo>();

    // default URL
    private static final String DEFAULT_URL = "http://localhost:8080/resourceService/gateway";


    /*********************************************************************************/

    // Constructors
    public YResourceConnection() {
        super();
        try {
            setURL(DEFAULT_URL);
        }
        catch (MalformedURLException mue) {
            setURL((URL) null);
        }
    }

    public YResourceConnection(String urlStr) { super(urlStr); }

    public YResourceConnection(URL url) { super(url); }


    /**
     * Opens a connection to the resource service. Usually not required to be called
     * directly, as connections are managed internally.
     * @return true if the connection is successfully opened.
     * @throws IOException if there's a problem connection to the service.
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
    public ResourceGatewayClient getClient() { return _client; }


    /**
     * Get the current list of Participants from the resource service.
     * @return If there is a current connection to the service, a fresh list is
     * retrieved from the service, the cache is updated, and the list is returned.
     * If a connection can't be established, or there is some problem retrieving a fresh
     * list, then the cached list is returned.
     * @throws IOException If there is some problem retrieving a fresh list, and the
     * cache is empty.
     */
    public List<Participant> getParticipants() throws IOException {
        if (isConnected()) {
            try {
                _participantCache = _adapter.getParticipants(_handle);
            }
            catch (ResourceGatewayException rge) {
                if (_participantCache.isEmpty()) throw new IOException(rge.getMessage());
            }
            catch (IOException ioe) {
                if (_participantCache.isEmpty()) throw new IOException(ioe.getMessage());
            }
        }
        return _participantCache;
    }


    /**
     * Get the current list of Roles from the resource service.
     * @return If there is a current connection to the service, a fresh list is
     * retrieved from the service, the cache is updated, and the list is returned.
     * If a connection can't be established, or there is some problem retrieving a fresh
     * list, then the cached list is returned.
     * @throws IOException If there is some problem retrieving a fresh list, and the
     * cache is empty.
     */
    public List<Role> getRoles() throws IOException {
        if (isConnected()) {
            try {
                _roleCache = _adapter.getRoles(_handle);
            }
            catch (ResourceGatewayException rge) {
                if (_roleCache.isEmpty()) throw new IOException(rge.getMessage());
            }
            catch (IOException ioe) {
                if (_roleCache.isEmpty()) throw new IOException(ioe.getMessage());
            }
        }
        return _roleCache;
    }


    /**
     * Get the current list of Capabilities from the resource service.
     * @return If there is a current connection to the service, a fresh list is
     * retrieved from the service, the cache is updated, and the list is returned.
     * If a connection can't be established, or there is some problem retrieving a fresh
     * list, then the cached list is returned.
     * @throws IOException If there is some problem retrieving a fresh list, and the
     * cache is empty.
     */
    public List<Capability> getCapabilities() throws IOException {
        if (isConnected()) {
            try {
                _capabilityCache = _adapter.getCapabilities(_handle);
            }
            catch (ResourceGatewayException rge) {
                if (_capabilityCache.isEmpty()) throw new IOException(rge.getMessage());
            }
            catch (IOException ioe) {
                if (_capabilityCache.isEmpty()) throw new IOException(ioe.getMessage());
            }
        }
        return _capabilityCache;
    }


    /**
     * Get the current list of Positions from the resource service.
     * @return If there is a current connection to the service, a fresh list is
     * retrieved from the service, the cache is updated, and the list is returned.
     * If a connection can't be established, or there is some problem retrieving a fresh
     * list, then the cached list is returned.
     * @throws IOException If there is some problem retrieving a fresh list, and the
     * cache is empty.
     */
    public List<Position> getPositions() throws IOException {
        if (isConnected()) {
            try {
                _positionCache = _adapter.getPositions(_handle);
            }
            catch (ResourceGatewayException rge) {
                if (_positionCache.isEmpty()) throw new IOException(rge.getMessage());
            }
            catch (IOException ioe) {
                if (_positionCache.isEmpty()) throw new IOException(ioe.getMessage());
            }
        }
        return _positionCache;
    }


    /**
     * Get the current list of OrgGroups from the resource service.
     * @return If there is a current connection to the service, a fresh list is
     * retrieved from the service, the cache is updated, and the list is returned.
     * If a connection can't be established, or there is some problem retrieving a fresh
     * list, then the cached list is returned.
     * @throws IOException If there is some problem retrieving a fresh list, and the
     * cache is empty.
     */
    public List<OrgGroup> getOrgGroups() throws IOException {
        if (isConnected()) {
            try {
                _orgGroupCache = _adapter.getOrgGroups(_handle);
            }
            catch (ResourceGatewayException rge) {
                if (_orgGroupCache.isEmpty()) throw new IOException(rge.getMessage());
            }
            catch (IOException ioe) {
                if (_orgGroupCache.isEmpty()) throw new IOException(ioe.getMessage());
            }
        }
        return _orgGroupCache;
    }


    /**
     * Get the current list of NonHumanResources from the resource service.
     * @return If there is a current connection to the service, a fresh list is
     * retrieved from the service, the cache is updated, and the list is returned.
     * If a connection can't be established, or there is some problem retrieving a fresh
     * list, then the cached list is returned.
     * @throws IOException If there is some problem retrieving a fresh list, and the
     * cache is empty.
     */
    public List<NonHumanResource> getNonHumanResources() throws IOException {
        if (isConnected()) {
            try {
                _nonHumanCache = _adapter.getNonHumanResources(_handle);
            }
            catch (ResourceGatewayException rge) {
                if (_nonHumanCache.isEmpty()) throw new IOException(rge.getMessage());
            }
            catch (IOException ioe) {
                if (_nonHumanCache.isEmpty()) throw new IOException(ioe.getMessage());
            }
        }
        return _nonHumanCache;
    }


    /**
     * Get the current list of NonHumanCategories from the resource service.
     * @return If there is a current connection to the service, a fresh list is
     * retrieved from the service, the cache is updated, and the list is returned.
     * If a connection can't be established, or there is some problem retrieving a fresh
     * list, then the cached list is returned.
     * @throws IOException If there is some problem retrieving a fresh list, and the
     * cache is empty.
     */
    public List<NonHumanCategory> getNonHumanCategories() throws IOException {
        if (isConnected()) {
            try {
                _nhCategoryCache = _adapter.getNonHumanCategories(_handle);
            }
            catch (ResourceGatewayException rge) {
                if (_nhCategoryCache.isEmpty()) throw new IOException(rge.getMessage());
            }
            catch (IOException ioe) {
                if (_nhCategoryCache.isEmpty()) throw new IOException(ioe.getMessage());
            }
        }
        return _nhCategoryCache;
    }


    /**
     * Get the current list of Filters from the resource service.
     * @return If there is a current connection to the service, a fresh list is
     * retrieved from the service, the cache is updated, and the list is returned.
     * If a connection can't be established, or there is some problem retrieving a fresh
     * list, then the cached list is returned.
     * @throws IOException If there is some problem retrieving a fresh list, and the
     * cache is empty.
     */
    public List<AbstractSelector> getFilters() throws IOException {
        if (isConnected()) {
            try {
                _filterCache = _adapter.getFilters(_handle);
            }
            catch (ResourceGatewayException rge) {
                if (_filterCache.isEmpty()) throw new IOException(rge.getMessage());
            }
            catch (IOException ioe) {
                if (_filterCache.isEmpty()) throw new IOException(ioe.getMessage());
            }
        }
        return _filterCache;
    }


    /**
     * Get the current list of Allocators from the resource service.
     * @return If there is a current connection to the service, a fresh list is
     * retrieved from the service, the cache is updated, and the list is returned.
     * If a connection can't be established, or there is some problem retrieving a fresh
     * list, then the cached list is returned.
     * @throws IOException If there is some problem retrieving a fresh list, and the
     * cache is empty.
     */
    public List<AbstractSelector> getAllocators() throws IOException {
        if (isConnected()) {
            try {
                _allocatorCache = _adapter.getAllocators(_handle);
            }
            catch (ResourceGatewayException rge) {
                if (_allocatorCache.isEmpty()) throw new IOException(rge.getMessage());
            }
            catch (IOException ioe) {
                if (_allocatorCache.isEmpty()) throw new IOException(ioe.getMessage());
            }
        }
        return _allocatorCache;
    }


    /**
     * Get the current list of Codelets from the resource service (as CodeletInfo objects).
     * @return If there is a current connection to the service, a fresh list is
     * retrieved from the service, the cache is updated, and the list is returned.
     * If a connection can't be established, or there is some problem retrieving a fresh
     * list, then the cached list is returned.
     * @throws IOException If there is some problem retrieving a fresh list, and the
     * cache is empty.
     */
    public List<CodeletInfo> getCodelets() throws IOException {
        if (isConnected()) {
            try {
                _codeletCache = _adapter.getCodelets(_handle);
            }
            catch (ResourceGatewayException rge) {
                if (_codeletCache.isEmpty()) throw new IOException(rge.getMessage());
            }
            catch (IOException ioe) {
                if (_codeletCache.isEmpty()) throw new IOException(ioe.getMessage());
            }
        }
        return _codeletCache;
    }


    /**
     * Gets the required task-level parameters for the specified codelet
     * @param codeletName the name of the codelet to get the parameters for
     * @return a fresh list of required parameters from the resource service, or a list
     * retrieved from the cached codelets, if any
     * @throws IOException if the resource service connection is down, and the cache
     * is empty
     */
    public List<YParameter> getCodeletParameters(String codeletName) throws IOException {
        if (isConnected()) {
            try {
                return _adapter.getCodeletParameters(codeletName, _handle);
            }
            catch (ResourceGatewayException rge) {
                // fall through to call below
            }
            catch (IOException ioe) {
                // fall through to call below
            }
        }
        return getCodeletParametersFromCache(codeletName);
    }


    /**
     * Iterates the cache for a codelet, and if found returns its parameters
     * @param codeletName the name of the codelet to get the parameters for
     * @return a list of required parameters for the named codelet, retrieved from the
     * cached codelets, if possible, or an empty list if not found
     * @throws IOException if the codelet cache is empty
     */
    private List<YParameter> getCodeletParametersFromCache(String codeletName)
            throws IOException {
        if (_codeletCache.isEmpty()) throw new IOException("Codelet Parameters Unavailable");
        for (CodeletInfo codelet : _codeletCache) {
            if (codelet.getName().equals(codeletName)) {
                return codelet.getRequiredParams();
            }
        }
        return new ArrayList<YParameter>();
    }


    /**
     * Gets a list of ids for all participants
     * @return the list of ids if successful, or an empty list if not
     */
    public List<String> getParticipantIDs() {
        List<String> ids = new ArrayList<String>();
        try {
            for (Participant p : getParticipants()) {
                 ids.add(p.getID());
            }
        }
        catch (IOException ioe) {
           // nothing to do - returns empty list
        }
        return ids;
    }


    /**
     * Gets a list of ids for all roles
     * @return the list of ids if successful, or an empty list if not
     */
    public List<String> getRoleIDs() {
        List<String> ids = new ArrayList<String>();
        try {
            for (Role r : getRoles()) {
                 ids.add(r.getID());
            }
        }
        catch (IOException ioe) {
           // nothing to do - returns empty list
        }
        return ids;
    }


    /**
     * Called from the super class to do any initialisation tasks
     */
    protected void init() {
        _client = new ResourceGatewayClient(_url.toExternalForm());
        _adapter = new ResourceGatewayClientAdapter(_url.toExternalForm());
    }


    /**********************************************************************************/

    // test class
    public static void main(String[] args) {
        YResourceConnection conn =
                new YResourceConnection("http://localhost:8080/resourceService/gateway");
        try {
            List<Participant> ps = conn.getParticipants();
            System.out.println(ps.size());
            List<Role> rs = conn.getRoles();
            System.out.println(rs.size());
            List<CodeletInfo> cs = conn.getCodelets();
            System.out.println(cs.size());
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
