package org.yawlfoundation.yawl.balancer;

import java.util.List;

/**
 * @author Michael Adams
 * @date 17/6/17
 */
public class ResponseStats {

    StatSet _overall;
    StatSet _previous;
    StatSet _current;
    long _timestamp;


    ResponseStats() {
        _overall = new StatSet();
        _previous = new StatSet();
        _current = new StatSet();
    }


    void add(double min, double max, double mean, int count, long timestamp,
             double timespan, List<ResponseTimes> timings) {
        _previous.copyFrom(_current);

        _current.min = min;
        _current.max = max;
        _current.mean = mean;
        _current.count = count;
        _current.timespan = timespan / 1000000.0;
        _current.rawTimes = timings;

        updateOverall(_current);

        _timestamp = timestamp;
    }


    void add(List<ResponseTimes> timings, long timestamp, long previous) {
        double timespan = (timestamp > 0 && previous > 0) ? timestamp - previous : 0;
        int count = timings.size();
        if (timings.isEmpty()) {
            add(0, 0, 0, 0, timestamp, timespan, timings);
        }
        else {
            double min = Double.MAX_VALUE;
            double max = 0;
            double sum = 0;

            for (ResponseTimes t : timings) {
                double duration = t.getDurationMsecs();
                min = Math.min(min, duration);
                max = Math.max(max, duration);
                sum += duration;
            }
            double mean = sum / count;
            add(min, max, mean, count, timestamp, timespan, timings);
        }
    }


    void updateOverall(StatSet update) {
        if (update.min < _overall.min) _overall.min = update.min;
        if (update.max > _overall.max) _overall.max = update.max;
        updateOverallMean(update);
        _overall.count += update.count;
        _overall.timespan += update.timespan;
    }


    void updateOverallMean(StatSet update) {
        double total = _overall.mean * _overall.count;
        double updateTotal = update.mean * update.count;
        _overall.mean = (total + updateTotal) / (_overall.count + update.count);
    }


    String report(String name) {
        StringBuilder sb = new StringBuilder(name);
        sb.append(", ");
        sb.append(_current.report()).append(",");
        sb.append(_previous.report()).append(",");
        sb.append(_overall.report());
        return sb.toString();
    }


    class StatSet {
        List<ResponseTimes> rawTimes;
        double min = 0;                                     // msecs
        double max = 0;                                     // msecs
        double mean = 0;                                    // msecs
        int count = 0;
        double timespan = 0;                                // msecs

        void copyFrom(StatSet set) {
            min = set.min;
            max = set.max;
            mean = set.mean;
            count = set.count;
            timespan = set.timespan;
            rawTimes = set.rawTimes;
        }
        
        double getResponsesPerSec() {
            return count > 0 ? count / (timespan / 1000) : 0;
        }

        String report() {
            return String.format("%.3f, %.3f, %.3f, %d, %.3f",
                    min, max, mean, count, getResponsesPerSec());
        }

    }
}
