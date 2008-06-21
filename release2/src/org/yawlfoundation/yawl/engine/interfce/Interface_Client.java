/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */

package org.yawlfoundation.yawl.engine.interfce;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used by clients and servers to execute GET and POST requests
 * across the YAWL interfaces
 *
 * @author Lachlan Aldred
 * Date: 22/03/2004
 * Time: 17:49:42
 *
 * @author Michael Adams (refactored for v2.0, 06/2008)
 */

public class Interface_Client {

    /**
     * Executes a HTTP POST request on the url specified.
     *
     * @param urlStr the URL to send the POST to
     * @param paramsMap a set of attribute-value pairs that make up the posted data
     * @return the result of the POST request
     * @throws IOException when there's some kind of communication problem
     */
    public static String executePost(String urlStr, Map<String, String> paramsMap)
            throws IOException {

        // create and setup connection
        HttpURLConnection connection = initPostConnection(urlStr);

        // encode data and send query
        sendData(connection, encodeData(paramsMap)) ;

        //retrieve reply
        String result = getReply(connection.getInputStream());
        connection.disconnect();

        return stripOuterElement(result);
    }

    /**
     * Executes a HTTP POST request on the url specified, sending the contents of
     * a specification file.
     *
     * @param urlStr the URL to send the POST to
     * @param specAsXML the spec xml as a string
     * @param filename the name of the specification
     * @param handle an active session handle
     * @return the result of the POST request (success/fail)
     * @throws IOException when there's some kind of communication problem
     */
    protected String executeUpload(String urlStr, String specAsXML, String filename,
                                   String handle) throws IOException {

        // create and setup connection
        HttpURLConnection connection = initPostConnection(urlStr);
        connection.setRequestProperty("YAWLSessionHandle", handle);
        connection.setRequestProperty("filename", filename);
        connection.setRequestProperty("Content-Type", "text/xml");

        //send query
        sendData(connection, specAsXML) ;

        //retrieve reply
        String result = getReply(connection.getInputStream());
        connection.disconnect();

        return stripOuterElement(result);
    }


    /**
     * Executes a HTTP GET request on the specified URL
     *
     * @param urlStr the URL to send the GET to
     * @param paramsMap a set of attribute-value pairs that make up the posted data
     * @return the result of the GET request
     * @throws IOException when there's some kind of communication problem
     */
    public static String executeGet(String urlStr, Map<String, String> paramsMap)
            throws IOException {

        // prepare & encode data
        if (paramsMap != null) {
            urlStr += "?" + encodeData(paramsMap) ;
        }

        // do the GET
        return completeGet(urlStr);
    }


    /**
     * Executes a HTTP GET request on the specified URL
     *
     * @param urlStr the URL to send the GET to, combined with data
     * @return the result of the GET request
     * @throws IOException when there's some kind of communication problem
     */
    public static String executeGet(String urlStr) throws IOException {

        // prepare & encode data
        String[] urlParts = urlStr.split("\\?");
        if (urlParts.length > 1) {
            urlStr = urlParts[0] + "?" + encodeData(urlParts[1]);
        }

        return completeGet(urlStr);
    }


    /**
     * Initialises a map for transporting parameters - used by extending classes
     * @param action the name of the action to take
     * @param handle the current engine session handle
     * @return the initialised Map
     */
    protected Map<String, String> prepareParamMap(String action, String handle) {
        Map<String, String> result = new HashMap<String, String>();
        result.put("action", action) ;
        if (handle != null) result.put("sessionHandle", handle) ;
        return result;
    }


    /**
     * Removes an outer set of xml tags from an xml string, if possible
     * @param inputXML the xml string to strip
     * @return the stripped xml string
     */
    protected static String stripOuterElement(String inputXML) {
        if (inputXML != null) {
            int beginClipping = inputXML.indexOf(">") + 1;
            int endClipping = inputXML.lastIndexOf("<");
            if (beginClipping >= 0 && endClipping >= 0) {
                inputXML = inputXML.substring(beginClipping, endClipping);
            }
        }
        return inputXML;
    }


    /**
     * Tests a response message for success or failure
     * @param message the response message to test
     * @return true if the response represents success
     */
    public static boolean successful(String message) {
        return message != null
                &&
                message.indexOf("<failure>") == -1
                &&
                message.length() > 0;
    }


    /*******************************************************************************/

    // PRIVATE METHODS //

    /**
     * Executes a HTTP GET request with the combined encoded data on the specified URL
     *
     * @param encodedURLStr the URL to send the GET request to, combined with an
     *        encoded query
     * @return the result of the GET request
     * @throws IOException when there's some kind of communication problem
     */
    private static String completeGet(String encodedURLStr) throws IOException {

        // create connection
        URL url = new URL(encodedURLStr);
        HttpURLConnection connection = initConnection(url);

        //retrieve reply
        String result = getReply(url.openStream());
        connection.disconnect();

        return result;
    }


    /**
     * Initialises a HTTP connection
     * @param url the url to connect to
     * @return an generic initialised connection (for GET or POST requests)
     * @throws IOException when there's some kind of communication problem
     */
    private static HttpURLConnection initConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        return connection ;
    }

    
    private static HttpURLConnection initPostConnection(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = initConnection(url);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        return connection ;
    }


    private static String encodeData(String data) {
        Map<String, String> map = new HashMap<String, String>();
        String[] params = data.split("&");
        for (String param : params) {
            String[] parts = param.split("=");
            String value = (parts.length == 2) ? parts[1] : "" ;
            map.put(parts[0], value);
        }
        return encodeData(map) ;
    }


    private static String encodeData(Map<String, String> params) {
        StringBuilder result = new StringBuilder("");
        for (String param : params.keySet()) {
            if (result.length() > 0) result.append("&");
            result.append(param)
                  .append("=")
                  .append(ServletUtils.urlEncode(params.get(param)));
        }
        return result.toString();
    }


    private static void sendData(HttpURLConnection connection, String data)
            throws IOException {
        PrintWriter out = new PrintWriter(connection.getOutputStream());
        out.print(data);
        out.flush();
        out.close();
    }


    private static String getReply(InputStream is) throws IOException {
        String result = "";
        InputStreamReader isr = new InputStreamReader(is);
        int data = 0 ;
        int chrsRead = 0;
        char[] buf = new char[512];
        while (data != -1) {
            data = isr.read(buf);
            result += new String(buf) ;
            chrsRead += data;
        } 

        isr.close();
        return result.substring(0, chrsRead + 1);
    }


}
