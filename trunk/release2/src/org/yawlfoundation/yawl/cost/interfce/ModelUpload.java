/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.cost.interfce;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 11/02/12
 */
public class ModelUpload {

    private String url = "http://localhost:8080/costService/gateway";
    private Map<String, String> params = new Hashtable<String, String>();

    public ModelUpload() {}


    public String add(String fileName) {
        String model = loadModel(fileName);
        String handle = getSession();
        String result = upload(model, handle);
        disconnect(handle);
        return successful(result) ? "Model successfully added to cost service" : result;
    }


    public String getLog(String id, String version, String uri) {
        String handle = getSession();
        String result = getLog(id, version, uri, handle);
        disconnect(handle);
        return result;
    }


    private String loadModel(String fileName) {
        return fileToString(fileName);
    }


    private String getLog(String id, String version, String uri, String handle) {
        params.clear();
        params.put("action", "getAnnotatedLog");
        params.put("specidentifier", id);
        params.put("specversion", version);
        params.put("specuri", uri);
        params.put("withData", "true");
        params.put("sessionHandle", handle);
        String log = post();
        String logFileName = uri + version + ".xeslog";
        stringToFile(logFileName, log);
        return "Successful generation of log: " + logFileName;
    }


    private String getSession() {
        params.clear();
        params.put("action", "connect");
        params.put("userid", "admin");
        params.put("password", "Se4tMaQCi9gr0Q2usp7P56Sk5vM=");
        return post();
    }


    private String upload(String model, String handle) {
        params.clear();
        params.put("action", "importModel");
        params.put("sessionHandle", handle);
        params.put("model", model);
        return post();
    }

    private void disconnect(String handle) {
        params.clear();
        params.put("action", "disconnect");
        params.put("sessionHandle", handle);
    }


    private String post() {
        String result = null;
        try {
            result = send(url, params);
            if (!successful(result)) {
                abort(stripOuterElement(result));
            }
        } catch (IOException ioe) {
            abort(ioe.getMessage());
        }
        return result;

    }

    private void abort(String msg) {
        System.err.println("ERROR: " + msg);
        System.exit(0);
    }

    private String send(String urlStr, Map<String, String> paramsMap)
            throws IOException {

        // create and setup connection
        HttpURLConnection connection = initPostConnection(urlStr);

        // encode data and send query
        sendData(connection, encodeData(paramsMap));

        //retrieve reply
        String result = readStream(connection.getInputStream(), -1);
        connection.disconnect();

        return result;
    }


    /**
     * Initialises a HTTP POST connection
     *
     * @param urlStr the url to connect to
     * @return an initialised POST connection
     * @throws IOException when there's some kind of communication problem
     */
    private HttpURLConnection initPostConnection(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Accept-Charset", "UTF-8");

        // required to ensure the connection is not reused. When not set, spurious
        // intermittent problems (double posts, missing posts) occur under heavy load.
        connection.setRequestProperty("Connection", "close");
        return connection;
    }


    /**
     * Encodes parameter values for HTTP transport
     *
     * @param params a map of the data parameter values, of the form
     *               [param1=value1],[param2=value2]...
     * @return a formatted http data string with the data values encoded
     */
    private String encodeData(Map<String, String> params) {
        StringBuilder result = new StringBuilder("");
        for (String param : params.keySet()) {
            String value = params.get(param);
            if (value != null) {
                if (result.length() > 0) result.append("&");
                result.append(param)
                        .append("=")
                        .append(urlEncode(value));
            }
        }
        return result.toString();
    }

    private String urlEncode(String s) {
        if (s == null) return s;
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            return s;
        }
    }


    /**
     * Submits data on a HTTP connection
     *
     * @param connection a valid, open HTTP connection
     * @param data       the data to submit
     * @throws IOException when there's some kind of communication problem
     */
    private void sendData(HttpURLConnection connection, String data)
            throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        out.write(data);
        out.close();
    }


    /**
     * Receives a reply from a HTTP submission
     *
     * @param is the InputStream of a URL or Connection object
     * @return the stream's contents (ie. the HTTP reply)
     * @throws IOException when there's some kind of communication problem
     */
    private String readStream(InputStream is, int size) throws IOException {
        final int BUF_SIZE = size > 0 ? size : 16384;

        // read reply into a buffered byte stream - to preserve UTF-8
        BufferedInputStream inStream = new BufferedInputStream(is);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(BUF_SIZE);
        byte[] buffer = new byte[BUF_SIZE];

        // read chunks from the input stream and write them out
        int bytesRead;
        while ((bytesRead = inStream.read(buffer, 0, BUF_SIZE)) > 0) {
            outStream.write(buffer, 0, bytesRead);
        }

        outStream.flush();
        outStream.close();
        inStream.close();

        // convert the bytes to a UTF-8 string
        return outStream.toString("UTF-8");
    }


    private void stringToFile(String fileName, String contents) {
        File f = new File(fileName);
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(f));
            buf.write(contents, 0, contents.length());
            buf.close();
        } catch (IOException ioe) {
            abort(ioe.getMessage());
        }
    }


    /**
     * Removes the outermost set of xml tags from a string, if any
     *
     * @param xml the xml string to strip
     * @return the stripped xml string
     */
    protected String stripOuterElement(String xml) {
        if (xml != null) {
            int start = xml.indexOf('>') + 1;
            int end = xml.lastIndexOf('<');
            if (end > start) {
                return xml.substring(start, end);
            }
        }
        return xml;
    }


    /**
     * Tests a response message for success or failure
     *
     * @param message the response message to test
     * @return true if the response represents success
     */
    public boolean successful(String message) {
        return (message != null) &&
                (message.length() > 0) &&
                (!message.contains("<failure>"));
    }


    private String fileToString(File f) {
        String result = null;
        if (f.exists()) {
            try {
                int bufsize = (int) f.length();
                result = readStream(new FileInputStream(f), bufsize);
            } catch (Exception e) {
                abort(e.getMessage());
            }
        } else abort("File not found: " + f.getAbsolutePath());

        return result;
    }


    private String fileToString(String filename) {
        return fileToString(new File(filename));
    }


    private static void usage() {
        System.out.println();
        System.out.println("ModelUpload uploads cost models to the Cost Service, and gets cost annotated logs.");
        System.out.println();
        System.out.println("USAGE: ModelUpload f");
        System.out.println("   where 'f' is the name of the cost model file to upload");
        System.out.println();
        System.out.println("       ModelUpload -l specid specversion specuri");
        System.out.println("   to generate a log file");
        System.out.println();
        System.out.println("NOTE: The Cost Service must be running locally.");
        System.out.println();
    }


    public static void main(String[] args) {
        if (args.length == 1) {
            String result = new ModelUpload().add(args[0]);
            System.out.println(result);
        } else if (args.length == 4 && args[0].equals("-l")) {
            String result = new ModelUpload().getLog(args[1], args[2], args[3]);
            System.out.println(result);
        } else usage();
    }

}
