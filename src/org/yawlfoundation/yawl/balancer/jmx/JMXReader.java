/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.balancer.jmx;

import org.json.JSONException;
import org.json.JSONObject;
import org.yawlfoundation.yawl.engine.interfce.*;
import org.yawlfoundation.yawl.util.XNode;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Adams
 * @date 21/6/17
 */
public class JMXReader extends Interface_Client {

    private static final String JOLOKIA_URL_TEMPLATE = "http://%s:%d/jolokia/read";

    private static final String THREAD_BODY_TEMPLATE = buildParams(
            "Catalina:name=\"http-bio-%d\",type=ThreadPool",
            Arrays.asList("currentThreadsBusy", "maxThreads",
                    "currentThreadCount", "connectionCount")
    );

    private static final String MEM_BODY = buildParams("java.lang:type=Memory",
            null);

    private static final String REQ_BODY_TEMPLATE = buildParams("Catalina:type=GlobalRequestProcessor," +
            "name=\"http-nio-%d\"", null);
//    "name=\"http-bio-%d\"", null);


    private static final String EXEC_BODY = buildParams("Catalina:type=Executor," +
            "name=tomcatThreadPool", null);

    private static final String SYS_BODY = buildParams("java.lang:type=OperatingSystem",
            null);


    private final String _jolokiaURL;
    private final String _threadBody;
    private final String _reqBody;


    public JMXReader(String host, int port) {
        _jolokiaURL = String.format(JOLOKIA_URL_TEMPLATE, host, port);
        _threadBody = String.format(THREAD_BODY_TEMPLATE, port);
        _reqBody = String.format(REQ_BODY_TEMPLATE, port);
    }


    public List<JMXStatistics> getAll() throws IOException, JSONException {
        List<JMXStatistics> statsList = new ArrayList<JMXStatistics>(5);
        addIfNotNull(statsList, getThreads());
        addIfNotNull(statsList, getMemory());
        addIfNotNull(statsList, getRequests());
        addIfNotNull(statsList, getExecutor());
        addIfNotNull(statsList, getSystem());
        return statsList;
    }


    public String getAllToXML() throws IOException, JSONException {
        XNode root = new XNode("jmx");
        for (JMXStatistics stats : getAll()) {
            root.addChild(stats.toXNode());
        }
        return root.toPrettyString();
    }


    public JMXMemoryStatistics getMemory() throws IOException, JSONException {
        JSONObject o = parse(execute(MEM_BODY));
        return (o != null) ? new JMXMemoryStatistics((JSONObject) o.get("value"),
                "memory", o.getLong("timestamp")) : null;
    }


    public JMXStatistics getThreads() throws IOException, JSONException {
        return getStatistics("threadpool", _threadBody);
    }


    public JMXStatistics getRequests() throws IOException, JSONException {
        return getStatistics("requests", _reqBody);
    }


    public JMXStatistics getExecutor() throws IOException, JSONException {
        return getStatistics("executor", EXEC_BODY);
    }


    public JMXStatistics getSystem() throws IOException, JSONException {
        return getStatistics("system", SYS_BODY);
    }


    private JMXStatistics getStatistics(String title, String body)
            throws IOException, JSONException {
        JSONObject o = parse(execute(body));
        return (o != null) ? new JMXStatistics((JSONObject) o.get("value"),
                title, o.getLong("timestamp")) : null;
    }


    protected String execute(String body) throws IOException {
        HttpURLConnection connection = initPostConnection(_jolokiaURL);
        connection.setReadTimeout(500);
        connection.setConnectTimeout(500);
        connection.setRequestProperty("content-type", "application/json;charset:UTF-8");
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        out.write(body);
        out.close();
        String result = getReply(connection.getInputStream());
        connection.disconnect();
        return result;
    }


    private void addIfNotNull(List<JMXStatistics> statsList, JMXStatistics stats) {
        if (stats != null) statsList.add(stats);
    }

    
    protected static String buildParams(String mbean, List<String> attributes) {
        JSONObject json = new JSONObject();
        try {
            json.put("mbean", mbean);
            json.put("type", "read");
            if (!(attributes == null || attributes.isEmpty())) {
                json.put("attribute", attributes);
            }
            return json.toString();
        }
        catch (JSONException je) {
            je.printStackTrace();
            return "";
        }
    }


    private JSONObject parse(String s) throws JSONException {
        JSONObject o = new JSONObject(s);
        int status = o.getInt("status");
        return status == 200 ? o : null;
    }

}
