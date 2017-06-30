package org.yawlfoundation.yawl.engine.interfce;

import org.yawlfoundation.yawl.util.XNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 11/6/17
 */
public class PerfReporter {

    private static final Map<String, List<TimeSpan>> _requestMap = new HashMap<String, List<TimeSpan>>();
    private static long _previousTimestamp = 0;


    private PerfReporter() { }


    public static void add(String request, long start) {
        getOrAdd(request).add(new TimeSpan(start, System.nanoTime()));
    }


    // read and clear
    public static String poll() {
        String resp = read();
        _requestMap.clear();
        return resp;
    }


    public static String read() {
        XNode root = new XNode("requeststats");
        long timestamp = System.nanoTime();
        root.addAttribute("timestamp", timestamp);
        root.addAttribute("previous", _previousTimestamp);
        _previousTimestamp = timestamp;
        root.addChild(getRequests());
        return root.toString();
    }


    private static XNode getRequests() {
        XNode root = new XNode("requests");
        for (String action : _requestMap.keySet()) {
            XNode actionNode = root.addChild("action");
            actionNode.addAttribute("name", action);
            for (TimeSpan span : _requestMap.get(action)) {
                actionNode.addChild(span.toXNode());
            }
        }
        return root;
    }


    private static List<TimeSpan> getOrAdd(String action) {
        if (! _requestMap.containsKey(action)) {
            _requestMap.put(action, new ArrayList<TimeSpan>());
        }
        return _requestMap.get(action);
    }

    /***/

    static class TimeSpan {

        private final long nanoBegin;
        private final long nanoEnd;

        protected TimeSpan(long begin, long end) {
            nanoBegin = begin;
            nanoEnd = end;
        }

        protected XNode toXNode() {
            XNode node = new XNode("span");
            node.addAttribute("begin", nanoBegin);
            node.addAttribute("end", nanoEnd);
            return node;
        }
    }
}
