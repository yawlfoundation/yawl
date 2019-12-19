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

package org.yawlfoundation.yawl.balancer;

import org.yawlfoundation.yawl.balancer.config.Config;
import org.yawlfoundation.yawl.balancer.output.RequestStatOutputter;
import org.yawlfoundation.yawl.balancer.polling.Pollable;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EnvironmentBasedClient;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.IOException;
import java.util.*;

/**
 * @author Michael Adams
 * @date 16/6/17
 */
public class RequestCollator implements Pollable {

    private static final String YAWL_URL_TEMPLATE = "http://%s:%d/yawl/ib";
    private final InterfaceB_EnvironmentBasedClient client;
    private final Map<String, ResponseStats> statMap;
    private String handle;
    private RequestStatOutputter _outputter;
    private final String _engineName;

    
    public RequestCollator() {
        this("localhost", 8080);
    }

    
    public RequestCollator(String host, int port) {
        String yawlURL = String.format(YAWL_URL_TEMPLATE, host, port);
        client = new InterfaceB_EnvironmentBasedClient(yawlURL);
        statMap = new TreeMap<String, ResponseStats>();
        _engineName = host + ':' + port;
    }


    public void scheduledEvent() {
        try {
            parse(pollRequests());
            if (Config.isWriteLog()) {
                dump();
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    
    public Map<String, ResponseStats> getStatisics() { return statMap; }


    public double getItemsPerSec() {
        return (getResponsesPerSec("checkout") +
                getResponsesPerSec("checkin")) / 2.0;
    }


    public double getResponsesPerSec(String request) {
        ResponseStats stats = getResponseStats(request);
        return stats != null ? stats._current.getResponsesPerSec() : 0;
    }


    public void dump() {
        StringBuilder sb = new StringBuilder();
        for (String name : statMap.keySet()) {
            ResponseStats stats = statMap.get(name);
            sb.append(stats.report(name)).append('\n');
        }
        if (sb.length() > 0) {
            getOutputter().add(sb.toString());
        }
    }


    private String pollRequests() throws IOException {
        if (handle == null) handle = client.connect("admin", "YAWL");
        return client.pollPerfStats(handle);
    }

    
    private ResponseStats getResponseStats(String name) {
        ResponseStats stats = statMap.get(name);
        if (stats == null) {
            stats = new ResponseStats();
            statMap.put(name, stats);
        }
        return stats;
    }


    private void parse(String resp) {
        XNode root = new XNodeParser().parse(resp);
        long timestamp = StringUtil.strToLong(root.getAttributeValue("timestamp"), 0);
        long previous = StringUtil.strToLong(root.getAttributeValue("previous"), 0);

        parseRequests(root.getChild("requests"), timestamp, previous);
    }


    private void parseRequests(XNode reqs, long timestamp, long previous) {
        List<String> actions = new ArrayList<String>();
        for (XNode action : reqs.getChildren()) {
            List<ResponseTimes> times = new ArrayList<ResponseTimes>();
            for (XNode span : action.getChildren()) {
                long b = StringUtil.strToLong(span.getAttributeValue("begin"), 0);
                long e = StringUtil.strToLong(span.getAttributeValue("end"), 0);
                times.add(new ResponseTimes(b, e));
            }

            String name = action.getAttributeValue("name");
            actions.add(name);
            getResponseStats(name).add(times, timestamp, previous);
        }

        for (String name : statMap.keySet()) {
            if (! actions.contains(name)) {
                getResponseStats(name).add(Collections.<ResponseTimes>emptyList(),
                        timestamp, previous);
            }
        }
    }


    private RequestStatOutputter getOutputter() {
        if (_outputter == null) {
            _outputter = new RequestStatOutputter(_engineName);
        }
        return _outputter;
    }

}
