package org.yawlfoundation.yawl.balancer.output;

import java.io.IOException;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 21/8/17
 */
public class BusynessOutputter extends AbstractLoadOutputter {


    public BusynessOutputter(String engineName) {
        super("busyness", engineName, ".log");
    }


    protected void writeValues(Map<String, String> values) throws IOException {
        _out.write(values.get("cpu_process"));
        _out.write(',');
        _out.write(values.get("cpu_system"));
        _out.write(',');
        _out.write(values.get("thread_count"));
        _out.write(',');
        _out.write(values.get("threads_busy"));
        _out.write(',');
        _out.write(values.get("threads_free"));
        _out.write(',');
        _out.write(values.get("process_time_factor"));
        _out.write(',');
        _out.write(values.get("requests_factor"));
        _out.write(',');
        _out.write(values.get("threads_factor"));
        _out.write(',');
        _out.write(values.get("busyness"));
    }


    protected String getHeader() {
        return "time,timestamp,cpu_process,cpu_system,thread_count," +
               "threads_busy,threads_free,process_time_factor,requests_factor," +
               "threads_factor,busyness";
    }


    protected void finalise() { }

}
