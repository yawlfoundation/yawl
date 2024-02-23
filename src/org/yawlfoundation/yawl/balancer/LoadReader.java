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

import org.json.JSONException;
import org.yawlfoundation.yawl.balancer.config.Config;
import org.yawlfoundation.yawl.balancer.jmx.JMXReader;
import org.yawlfoundation.yawl.balancer.jmx.JMXStatistics;
import org.yawlfoundation.yawl.balancer.output.ArffOutputter;
import org.yawlfoundation.yawl.balancer.output.BusynessOutputter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 22/6/17
 */
public class LoadReader {

    private JMXReader _jmxReader;
    private int _prevReqCount = 0;
    private double _prevProcTime = 0;
    private BusynessOutputter _outputter;
    private ArffOutputter _arffWriter;
    private final String _engineName;


    public LoadReader() {
        this("localhost", 8080);
    }


    public LoadReader(String host, int port) {
        _jmxReader = new JMXReader(host, port);
        _engineName = host + ":" + port;
    }

    public void setJMXReader(JMXReader reader) { _jmxReader = reader; }


    public double getBusyNess() throws IOException, JSONException {
        return getBusyness(false);
    }


    public void close() {
        if (_outputter != null) _outputter.closeFile();
        if (_arffWriter != null) _arffWriter.closeFile();
    }


    public void setArffWriter(ArffOutputter writer) {
        _arffWriter = writer;
    }

    public double getBusyness(boolean verbose) throws IOException, JSONException {
        JMXStatistics requestStats = getRequests();
        int reqCount = requestStats.getIntValue("requestCount");
        int procTime = requestStats.getIntValue("processingTime");
        double timeFactor = 0;
        double reqFactor = 0;
        int netReqCount = 0;
        double meanTime = 0;

        if (_prevReqCount > 0) {
            netReqCount = reqCount - _prevReqCount;
            meanTime = (procTime - _prevProcTime) / (double) (netReqCount);
            timeFactor = (meanTime / Config.getWeightedProcessTimeLimit());
            reqFactor = netReqCount / Config.getWeightedRequestLimitPerPollInterval();
        }
        _prevReqCount = reqCount;
        _prevProcTime = procTime;

        JMXStatistics threadStats = getThreads();
        int busy = threadStats.getIntValue("currentThreadsBusy");
        double threadFactor = busy / Config.getWeightedThreadsLimit();

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
            verboseValues.put("process_time", String.format("%.3f", meanTime));
            verboseValues.put("requests_count", String.format("%d", netReqCount));
            getOutputter().add(verboseValues);

            if (_arffWriter != null) {
                verboseValues.put("process_time", String.format("%.3f", meanTime));
                verboseValues.put("requests_count", String.format("%d", netReqCount));
                _arffWriter.add(verboseValues);
            }
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
