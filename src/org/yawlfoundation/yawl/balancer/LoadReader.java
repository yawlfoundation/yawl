package org.yawlfoundation.yawl.balancer;

import org.json.JSONException;
import org.yawlfoundation.yawl.balancer.jmx.JMXReader;
import org.yawlfoundation.yawl.balancer.jmx.JMXStatistics;
import org.yawlfoundation.yawl.balancer.output.BusynessOutputter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 22/6/17
 */
public class LoadReader {

    private final JMXReader _jmxReader;
    private double _requestLimit = 500;
    private double _procTimeLimit = 70;
    private double _threadsLimit = 16;
    private int _prevReqCount = 0;
    private double _prevProcTime = 0;
    private BusynessOutputter _outputter;
    private final String _engineName;


    public LoadReader() {
        this("localhost", 8080);
    }


    public LoadReader(String host, int port) {
        _jmxReader = new JMXReader(host, port);
        _engineName = host + ":" + port;
    }


    public void setLimits(int requestsPerSec, int meanProcessingTimeInMsecs,
                          int busyThreads) {
        _requestLimit = requestsPerSec;
        _procTimeLimit = meanProcessingTimeInMsecs;
        _threadsLimit = busyThreads;
    }


    public double getBusyNess() throws IOException, JSONException {
        return getBusyness(false);
    }


    public void close() {
        if (_outputter != null) _outputter.closeFile();
    }

    public double getBusyness(boolean verbose) throws IOException, JSONException {
        JMXStatistics requestStats = getRequests();
        int reqCount = requestStats.getIntValue("requestCount");
        int procTime = requestStats.getIntValue("processingTime");
        double timeFactor = 0;
        double reqFactor = 0;
        if (_prevReqCount > 0) {
            int netReqCount = reqCount - _prevReqCount;
            double meanTime = (procTime - _prevProcTime) / (double) (netReqCount);
            timeFactor = meanTime / _procTimeLimit;
            reqFactor = netReqCount / _requestLimit;
        }
        _prevReqCount = reqCount;
        _prevProcTime = procTime;

        JMXStatistics threadStats = getThreads();
        int busy = threadStats.getIntValue("currentThreadsBusy");
        double threadFactor = busy / _threadsLimit;

        double score = ((timeFactor + reqFactor + threadFactor) / 3) * 100;

        if (verbose) {
            int count = threadStats.getIntValue("currentThreadCount");
            int max = threadStats.getIntValue("maxThreads");

            JMXStatistics sysStats = getSystem();
            double procLoad = 100 * sysStats.getDoubleValue("ProcessCpuLoad");
            double sysLoad = 100 * sysStats.getDoubleValue("SystemCpuLoad");

            Map<String, String> verboseValues = new HashMap<String, String>();
            verboseValues.put("cpu_process", String.format("%.3f", procLoad));
            verboseValues.put("cpu_system", String.format("%.3f", sysLoad));
            verboseValues.put("thread_count", String.format("%d", count));
            verboseValues.put("threads_busy", String.format("%d", busy));
            verboseValues.put("threads_free", String.format("%d", (max - busy)));
            verboseValues.put("process_time_factor", String.format("%.3f", timeFactor));
            verboseValues.put("requests_factor", String.format("%.3f", reqFactor));
            verboseValues.put("threads_factor", String.format("%.3f", threadFactor));
            verboseValues.put("busyness", String.format("%.3f", score));
            getOutputter().add(verboseValues);

//            StringBuilder sb = new StringBuilder();
//            sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").
//                    format(new Date()));
//            sb.append(" :- Load statistics for engine ").append(_host).append(":")
//                    .append(_port).append('\n');
//            sb.append(String.format("CPU %% (process/system): %.3f / %.3f\n",
//                    procLoad, sysLoad));
//            sb.append(String.format("Threads (count/busy/free): %d / %d / %d\n",
//                    count, busy, (max - busy)));
//            sb.append(String.format("Factors (proctime/reqs/threads): %.3f / %.3f / %.3f\n",
//                    timeFactor, reqFactor, threadFactor));
//            sb.append(String.format("Overall Busyness Factor: %.1f%%\n\n", score));
//            System.out.println(sb.toString());
        }

        return score;
    }


    private BusynessOutputter getOutputter() {
        if (_outputter == null) {
            _outputter = new BusynessOutputter(_engineName);
        }
        return _outputter;
    }
    
    private JMXStatistics getThreads() throws IOException, JSONException {
        return _jmxReader.getThreads();
    }

    private JMXStatistics getRequests() throws IOException, JSONException {
        return _jmxReader.getRequests();
    }

    private JMXStatistics getSystem() throws IOException, JSONException {
        return _jmxReader.getSystem();
    }

}
