package org.yawlfoundation.yawl.balancer;

import org.yawlfoundation.yawl.balancer.output.RequestStatOutputter;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EnvironmentBasedClient;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Michael Adams
 * @date 16/6/17
 */
public class Poller {

    private static final String YAWL_URL_TEMPLATE = "http://%s:%d/yawl/ib";
    private final InterfaceB_EnvironmentBasedClient client;
    private final Map<String, ResponseStats> statMap;
    private String handle;
    private ScheduledExecutorService _executor;
    private RequestStatOutputter _outputter;
    private final String _engineName;

    public Poller() {
        this("localhost", 8080);
    }

    
    public Poller(String host, int port) {
        String yawlURL = String.format(YAWL_URL_TEMPLATE, host, port);
        client = new InterfaceB_EnvironmentBasedClient(yawlURL);
        statMap = new TreeMap<String, ResponseStats>();
        _engineName = host + ':' + port;
    }

    
    public void start() { start(10, false); }


    public void start(int pollPeriodAsSeconds, final boolean verbose) {
        _executor = Executors.newScheduledThreadPool(1);
        _executor.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            parse(pollRequests());
                            if (verbose) {
                                dump();
                            }
                        }
                        catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }

                }, 0, pollPeriodAsSeconds, TimeUnit.SECONDS
        );
    }

    public void stop() {
        if (_executor != null) _executor.shutdownNow();
        if (_outputter != null) _outputter.closeFile();
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
