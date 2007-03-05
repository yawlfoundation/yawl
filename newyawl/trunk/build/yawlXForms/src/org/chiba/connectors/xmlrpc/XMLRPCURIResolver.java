package org.chiba.connectors.xmlrpc;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.chiba.xml.xforms.connector.AbstractConnector;
import org.chiba.xml.xforms.connector.URIResolver;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Document;

import java.net.URI;
import java.util.Hashtable;
import java.util.Vector;


/**
 * This class resolves <code>xmlrpc</code> URIs. It treats the denoted
 * <code>xmlrpc</code> resource as a XML-RPC function, with parameters
 * passed as a query string to the URI. URIs look like this:
 * <code>xmlrpc://server:port/path/[xmlrpcQuery]?[xmlrpcParameters]#frag.</code> <p>
 * The function is called with the parameters in a Hashtable, and an
 * xml Document is returned.
 * <p/>
 * If the specified URI contains a fragment part, the specified element
 * is looked up via the <code>getElementById</code>. Thus, the parsed
 * response must have an internal DTD subset specifying all ID attribute.
 * Otherwise the element would not be found.
 *
 * @author Chris Picton;
 */
public class XMLRPCURIResolver extends AbstractConnector implements URIResolver {
    private static Logger log = Logger.getLogger(XMLRPCURIResolver.class);

    /**
     * Decodes the <code>xmlrpc</code> URI, runs the indicated function
     * and returns the result as a DOM document.
     *
     * @return a DOM node parsed from the <code>xmlrpc</code> URI.
     * @throws XFormsException if any error occurred.
     */
    public Object resolve() throws XFormsException {
        try {
            log.setLevel(Level.DEBUG);

            URI uri = new URI(getURI());
            log.debug("Getting URI: '" + uri + "'");

            Vector v = parseURI(uri);
            String rpcURL = (String) v.get(0);
            String function = (String) v.get(1);
            Hashtable params = (Hashtable) v.get(2);

            org.chiba.connectors.xmlrpc.RPCClient rpc = new org.chiba.connectors.xmlrpc.RPCClient(rpcURL);

            Document document = rpc.getDocument(function, params);

            if (uri.getFragment() != null) {
                return document.getElementById(uri.getFragment());
            }

            return document;
        } catch (Exception e) {
            throw new XFormsException(e);
        }
    }

    /**
     * Parses the URI into three elements.
     * The xmlrpc URL, the function and the params
     *
     * @return a Vector
     */
    private Vector parseURI(URI uri) {
        String host = uri.getHost();
        int port = uri.getPort();
        String path = uri.getPath();
        String query = uri.getQuery();
            
        /* Split the path up into basePath and function */
        int finalSlash = path.lastIndexOf('/');
        String basePath = "";
        if (finalSlash > 0) {
            basePath = path.substring(1, finalSlash);
        }
        String function = path.substring(finalSlash + 1, path.length());

        String rpcURL = "http://" + host + ":" + port + "/" + basePath;

        log.debug("New URL  = " + rpcURL);
        log.debug("Function = " + function);

        Hashtable paramHash = new Hashtable();

        if (query != null) {
            String[] params = query.split("&");

            for (int i = 0; i < params.length; i++) {
                log.debug("params[" + i + "] = " + params[i]);
                String[] keyval = params[i].split("=");
                log.debug("\t" + keyval[0] + " -> " + keyval[1]);
                paramHash.put(keyval[0], keyval[1]);
            }
        }

        Vector ret = new Vector();
        ret.add(rpcURL);
        ret.add(function);
        ret.add(paramHash);
        return ret;
    }
}
