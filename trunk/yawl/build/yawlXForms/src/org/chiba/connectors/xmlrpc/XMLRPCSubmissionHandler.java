package org.chiba.connectors.xmlrpc;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.chiba.xml.xforms.Submission;
import org.chiba.xml.xforms.connector.AbstractConnector;
import org.chiba.xml.xforms.connector.SubmissionHandler;
import org.chiba.xml.xforms.connector.serializer.XMLSerializer;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Node;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

public class XMLRPCSubmissionHandler extends AbstractConnector implements SubmissionHandler {
    private static Logger log = Logger.getLogger(XMLRPCSubmissionHandler.class);


    public XMLRPCSubmissionHandler() throws XFormsException {
        super();
        registerSerializer("xmlrpc", "put", "*", new XMLSerializer());
    }

    /**
     * Serializes and submits the given instance data to an xmlrpc function protocol.
     *
     * @param submission the submission issuing the request.
     * @param instance   the instance data to be serialized and submitted.
     * @return <code>null</code>.
     * @throws XFormsException if any error occurred during submission.
     */
    public Map submit(Submission submission, Node instance) throws XFormsException {

        if (!submission.getReplace().equals("none")) {
            throw new XFormsException("submission mode '" + submission.getReplace() + "' not supported");
        }

        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            serialize(submission, instance, stream);

            log.setLevel(Level.DEBUG);

            URI uri = new URI(getURI());
            log.debug("Getting URI: '" + uri + "'");

            Vector v = parseURI(uri);
            String rpcURL = (String) v.get(0);
            String function = (String) v.get(1);
            Hashtable params = (Hashtable) v.get(2);

            params.put("doc", stream.toByteArray());

            org.chiba.connectors.xmlrpc.RPCClient rpc = new org.chiba.connectors.xmlrpc.RPCClient(rpcURL);

            Hashtable result = rpc.runFunc(function, params);

            if (((String) result.get("status")).equals("error")) {
                throw new XFormsException((String) result.get("error"));
            }
        } catch (Exception e) {
            throw new XFormsException(e);
        }
        return null;
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
