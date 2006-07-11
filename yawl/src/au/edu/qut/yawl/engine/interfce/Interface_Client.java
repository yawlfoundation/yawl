/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.interfce;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * 
 * @author Lachlan Aldred
 * Date: 22/03/2004
 * Time: 17:49:42
 * 
 */
public class Interface_Client {

    // handles all of the Http Client code
    public static String executePost(String urlStr, Map paramsMap) throws IOException {
        StringBuffer result = new StringBuffer();
        HttpURLConnection connection = null;

        URL url = new URL(urlStr);
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        //send query
        PrintWriter out = new PrintWriter(connection.getOutputStream());
        Iterator paramKeys = paramsMap.keySet().iterator();
        while (paramKeys.hasNext()) {
            String paramName = (String) paramKeys.next();
            out.print(paramName + "=" + paramsMap.get(paramName));
            if (paramKeys.hasNext()) {
                out.print('&');
            }
        }
        out.flush();
        //retrieve reply
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        String newline = System.getProperty("line.separator");
        while ((inputLine = in.readLine()) != null) {
            result.append(inputLine).append(newline);
        }
        //clean up
        in.close();
        out.close();
        connection.disconnect();

        String msg = result.toString();
        return stripOuterElement(msg);
    }


    // handles all of the Http Client code
    public static String executeGet(String urlStr) throws IOException {
        int index = urlStr.indexOf("?");
        if (index != -1) {
            String firstHalf = urlStr.substring(0, urlStr.indexOf('?') + 1);
            String query = urlStr.substring(index + 1);
            StringBuffer encodedQuery = new StringBuffer();
            StringTokenizer tokens = new StringTokenizer(query, "&");
            while (tokens.hasMoreTokens()) {
                String paramNVal = tokens.nextToken();
                StringTokenizer tokens2 = new StringTokenizer(paramNVal, "=");
                while (tokens2.hasMoreTokens()) {
                    String param = tokens2.nextToken();
                    String val = tokens2.nextToken();
                    try {
                        val = URLEncoder.encode(val, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    encodedQuery.append(param + "=" + val);
                }
                if (tokens.hasMoreTokens()) {
                    encodedQuery.append("&");
                }
            }
            urlStr = firstHalf + encodedQuery;
        }
        StringBuffer result = new StringBuffer();

        URL url = new URL(urlStr);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        //retrieve reply
        String inputLine;
        String newline = System.getProperty("line.separator");
        while ((inputLine = in.readLine()) != null) {
            result.append(inputLine).append(newline);
        }
        //clean up
        in.close();
        connection.disconnect();

        return result.toString();
    }


    protected static String stripOuterElement(String inputXML) {
        if (inputXML != null) {
            int beginClipping = inputXML.indexOf(">") + 1;
            int endClipping = inputXML.lastIndexOf("<");
            if (beginClipping >= 0 && endClipping >= 0) {
                inputXML = inputXML.substring(beginClipping, endClipping);
                return inputXML;
            }
        }
        return inputXML;
    }

    public static boolean successful(String message) {
        return message != null
                &&
                message.indexOf("<failure>") == -1
                &&
                message.length() > 0;
    }
}
