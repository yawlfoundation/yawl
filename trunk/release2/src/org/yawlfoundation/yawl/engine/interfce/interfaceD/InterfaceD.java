/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine.interfce.interfaceD;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;


/**
 * The interface that the YAWL worklist uses for connecting to 3rd-party applications using HTTP.
 * @author Guy Redding 15/05/2005
 */
public class InterfaceD {

    private boolean debug = false;

    private URL url;
    private HttpURLConnection connection;


    /**
     * Empty constructor.
     */
    public InterfaceD() {
    }


    /**
     * Opens a new HttpURLConnection for the given URL.
     * @param urlStr
     * @throws java.io.IOException
     */
    public void connect(String urlStr) throws IOException {

        url = new URL(urlStr);

        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST"); // include an option for "get"
        connection.setRequestProperty("Content-Type", "text/xml");
    }


    /**
     * Adds the parameters found in the parameter map as properties to the request.
     * @param _parameters
     */
    private void setParameters(Map _parameters) {

        Map parameters = Collections.synchronizedMap(new TreeMap(_parameters));
        Set s = _parameters.keySet();
        Iterator it = s.iterator();

        while (it.hasNext()) {
            Object key = it.next();

            if (debug) {
                Logger.getLogger(InterfaceD.class).debug("Parameters Map key: " +
                        key.toString() + ", Map value: " + parameters.get(key));
            }

            connection.setRequestProperty(key.toString(), parameters.get(key).toString());
        }
    }


    /**
     * Posts the given string of data to the URL.  A URL connection must already exist.
     * Any parameters included in the map are added as request properties on the connection.
     * @param data
     * @param parameters
     * @throws java.io.IOException
     */
    public void postData(String data, Map parameters) throws IOException {

        setParameters(parameters);

//send query
        PrintWriter out = new PrintWriter(connection.getOutputStream());
        out.print(data);
        out.flush();

        StringBuffer result = new StringBuffer();

//retrieve reply
        InputStream inputStr = connection.getInputStream();
        InputStreamReader isReader = new InputStreamReader(inputStr);
        BufferedReader in = new BufferedReader(isReader);
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            result.append(inputLine);
        }

//clean up
        in.close();
        out.close();
        connection.disconnect();
    }


    /**
     * Sends a post to the URL connection (a connection must already exist).
     * Any parameters included in the map are added as request properties on the connection.
     * This type of post sends an empty output stream.  It is meant as a command to activate
     * some type of activity at the recipient.
     * @param parameters
     * @throws java.io.IOException
     */
    public void postCommand(Map parameters) throws IOException {

        setParameters(parameters);

        PrintWriter out = new PrintWriter(connection.getOutputStream());
        out.print("");
        out.flush();

        StringBuffer result = new StringBuffer();

//retrieve reply
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            result.append(inputLine);
        }

//clean up
        in.close();
        out.close();
        connection.disconnect();
    }
}