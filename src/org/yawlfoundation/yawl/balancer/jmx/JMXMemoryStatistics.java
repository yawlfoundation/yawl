package org.yawlfoundation.yawl.balancer.jmx;

import org.json.JSONException;
import org.json.JSONObject;
import org.yawlfoundation.yawl.util.XNode;

/**
 * @author Michael Adams
 * @date 13/6/17
 */
public class JMXMemoryStatistics extends JMXStatistics {

    private Memory heap;
    private Memory nonHeap;


    public JMXMemoryStatistics(JSONObject object, String title, long timestamp) {
        super(object, title, timestamp);
        parse(object);
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getTimestampFormatted()).append('\n');
        if (heap != null) sb.append(heap.toString());
        if (nonHeap != null) sb.append(nonHeap.toString());
        return sb.toString();
    }


    public XNode toXNode() {
        XNode root = new XNode("memory");
        root.addAttribute("timestamp", getTimestamp());
        if (heap != null) root.addChild(heap.toXnode());
        if (nonHeap != null) root.addChild(nonHeap.toXnode());
        return root;
    }


    private void parse(JSONObject obj) {
        try {
            heap = new Memory((JSONObject) obj.get("HeapMemoryUsage"), "Heap");
            nonHeap = new Memory((JSONObject) obj.get("NonHeapMemoryUsage"), "NonHeap");
        }
        catch (JSONException je) {
            //
        }
    }


    class Memory {
        long init;
        long committed;
        long max;
        long used;
        String type;

        Memory(JSONObject obj, String t) {
            init = getLong(obj, "init");
            committed = getLong(obj, "committed");
            max = getLong(obj, "max");
            used = getLong(obj, "used");
            type = t;
        }

        long getLong(JSONObject obj, String key) {
            try {
                return obj.getLong(key);
            }
            catch (JSONException je) {               
                return -1;
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(type).append(" Usage\n");
            sb.append("\tinit:").append(init).append('\n');
            sb.append("\tcommitted:").append(committed).append('\n');
            sb.append("\tmax:").append(max).append('\n');
            sb.append("\tused:").append(used).append('\n');
            return sb.toString();
        }

        public XNode toXnode() {
            XNode root = new XNode(type);
            root.addChild("init", init);
            root.addChild("committed", committed);
            root.addChild("max", max);
            root.addChild("used", used);
            return root;
        }
    }
}
