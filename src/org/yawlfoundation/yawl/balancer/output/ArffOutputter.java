package org.yawlfoundation.yawl.balancer.output;

import org.yawlfoundation.yawl.balancer.config.Config;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.IOException;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 21/8/17
 */
public class ArffOutputter extends AbstractLoadOutputter {


    public ArffOutputter() {
        super("busyness", "all", ".arff");
        setWriteTime(false);
    }


    protected synchronized void writeValues(Map<String, String> values) throws IOException {
        _out.write(values.get("cpu_process"));
        _out.write(',');
        _out.write(values.get("cpu_system"));
        _out.write(',');
        _out.write(values.get("threads_busy"));
        _out.write(',');
        _out.write(values.get("threads_free"));
        _out.write(',');
        _out.write(values.get("process_time"));
        _out.write(',');
        _out.write(values.get("requests_count"));
        _out.write(',');
        _out.write(getBusyLabel(values.get("busyness")));
    }


    protected String getHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("@RELATION engine_busyness\n");
        sb.append("@ATTRIBUTE cpu_process REAL\n");
        sb.append("@ATTRIBUTE cpu_system REAL\n");
        sb.append("@ATTRIBUTE threads_busy numeric\n");
        sb.append("@ATTRIBUTE threads_free numeric\n");
        sb.append("@ATTRIBUTE process_time REAL\n");
        sb.append("@ATTRIBUTE requests_count REAL\n");
        sb.append("@ATTRIBUTE class {busy, not-busy}\n");

        sb.append("@DATA\n");
        return sb.toString();
    }


    protected void finalise() throws IOException {
        for (int i=0; i<3; i++) {
            _out.write('%');
            _out.write('\n');
        }
    }


    private String getBusyLabel(String busyValue) {
        double busyness = StringUtil.strToDouble(busyValue, -1);
        return busyness >= Config.getBusynessLimit() ? "busy" : "not-busy";
    }

}
