package org.yawlfoundation.yawl.editor.core.connection;

import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A base wrapper class for connecting to a running YAWL instance.
 * @author Michael Adams
 * @date 08/09/2011
 */
public abstract class YConnection {

    // some default values
    public static final String DEFAULT_USERID = "editor";
    public static final String DEFAULT_PASSWORD = "yEditor";

    protected URL _url;                                        // the connection's url
    protected String _handle;                                  // a session handle
    protected String _userid = DEFAULT_USERID;
    protected String _password = DEFAULT_PASSWORD;

    private static final int MAX_WAIT = 2000;                  // max ping response time


    // Constructors - called only from subclasses
    protected YConnection() {  }

    protected YConnection(String urlStr) {
        try {
            setURL(urlStr);
        }
        catch (MalformedURLException mue) {
            setURL((URL) null);
        }
    }

    protected YConnection(URL url) { setURL(url); }


    // Abstract method implemented by subclasses to handle their own initialisation tasks
    // when a connection URL is set or changed.
    protected abstract void init();

    protected abstract String getURLFilePath();

    protected abstract Interface_Client getClient();

    /***********************************************************************/

    /**
     * Sets the URL for this connection object
     * @param urlStr a String representation of the URL to connect to.
     * @throws MalformedURLException if the String is an malformed URL.
     */
    public void setURL(String urlStr) throws MalformedURLException {
        setURL(new URL(urlStr));
    }


    /**
     * Sets the URL for this connection object
     * @param url the URL to connect to.
     */
    public void setURL(URL url) {
        if (url != null) {
            _url = url;
            init();
        }
    }

    public void setURL(String host, int port) throws MalformedURLException {
        setURL(new URL("http", host, port, getURLFilePath()));
    }


    /**
     * Gets the current URL for this connection object.
     * @return the current URL.
     */
    public URL getURL() { return _url; }

    public String getHost() { return _url.getHost(); }

    public int getPort() { return _url.getPort(); }


    /**
     * Sets the user id for the connection.
     * @param id the user id (cannot be null).
     */
    public void setUserID(String id) {
        if (id != null) {
            _userid = id;
            disconnect();
        }
    }


    /**
     * Sets the password for the connection.
     * @param pw the password (cannot be null).
     */
    public void setPassword(String pw) {
        if (pw != null) {
            _password = pw;
            disconnect();
        }
    }


    /**
     * Drop the current connection - if any
     */
    public void disconnect() {
        _handle = null;
    }


    /**
     * Attempts to connect to a YAWL instance via an API client. Called by subclasses.
     * @param client the client object to connect
     * @return true if the connection is successful
     * @throws IOException if the client or URL is null, if the URL is not reachable,
     * or if there is some problem connecting to a YAWL instance at the given URL
     */
    protected boolean connect(Interface_Client client) throws IOException {
        checkParameters(client);
        if (_handle == null) {
            _handle = connectClient(client);
            if (! client.successful(_handle)) {
                _handle = null;
                return false;
            }
        }
        return true;
    }


    /**
     * Checks that the client currently has a valid connection to a running YAWL
     * instance, and if not attempts to establish a connection.
     * @param client the client object to check and/or connect
     * @return true if the connection is currently established
     */
    public boolean isConnected(Interface_Client client) {
        try {
            checkParameters(client);
            if (_handle == null) {
                return connect(client);
            }
            else {
                boolean success = checkConnection(client);
                if (! success) _handle = null;
                return success;
            }
        }
        catch (IOException ioe) {
            return false;
        }
    }


    public boolean testConnection(String user, String password) {
        setUserID(user);
        setPassword(password);
        return isConnected(getClient());
    }

    /***********************************************************************/

    /**
     * Ensures all connections parameters are valid
     * @param client the client object to check
     * @throws IOException if the client or URL is null, or if the URL is not reachable.
     */
    private void checkParameters(Interface_Client client) throws IOException {
        if (_url == null) {
            throw new IOException("URL is null");
        }
        if (! isReachable()) {
            throw new IOException("URL is not reachable");
        }
        if (client == null) {
            throw new IOException("Interface client is null");
        }
    }


    /**
     * Attempts to connect to a URL and receive a response
     * @return true if a response was received
     */
    private boolean isReachable() {
        try {
            HttpURLConnection httpConnection = (HttpURLConnection) _url.openConnection();
            httpConnection.setRequestMethod("HEAD");
            httpConnection.setConnectTimeout(MAX_WAIT);

            // if the URL is down, the next line throws an exception
            httpConnection.getResponseCode();

            // if it gets here, the connection is ok, the URI is up
            return true;
        }
        catch (IOException ioe) {
            return false;
        }
    }


    /**
     * Establishes a connection to a YAWL instance via a client object.
     * @param client the client to connect
     * @return a session handle if successful, or a diagnostic message if not
     * @throws IOException if there is some problem connecting to the YAWL instance
     */
    private String connectClient(Interface_Client client) throws IOException {

        // this construct was required because Interface_Client does not have a
        // generic 'connect' method
        if (client instanceof InterfaceA_EnvironmentBasedClient) {
            return ((InterfaceA_EnvironmentBasedClient) client).connect(_userid, _password);
        }
        if (client instanceof ResourceGatewayClient) {
            return ((ResourceGatewayClient) client).connect(_userid, _password);
        }
        return null;
    }


    /**
     * Checks to see if there is a current connection to a YAWL instance via a client object.
     * @param client the client to check
     * @return true if the current session handle is valid
     * @throws IOException if there is some problem connecting to the YAWL instance
     */
   private boolean checkConnection(Interface_Client client) throws IOException  {

        // this construct was required because Interface_Client does not have a
        // generic 'checkConnection' method
        if (client instanceof InterfaceA_EnvironmentBasedClient) {
            InterfaceA_EnvironmentBasedClient iaClient =
                    (InterfaceA_EnvironmentBasedClient) client;
            return iaClient.successful(iaClient.checkConnection(_handle));
        }
        if (client instanceof ResourceGatewayClient) {
            ResourceGatewayClient irClient = (ResourceGatewayClient) client;
            return irClient.successful(irClient.checkConnection(_handle));
        }
        return false;
    }

}
