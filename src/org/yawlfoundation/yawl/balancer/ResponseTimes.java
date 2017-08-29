package org.yawlfoundation.yawl.balancer;

/**
 * @author Michael Adams
 * @date 19/6/17
 */
public class ResponseTimes {

    final long start;
    final long end;

    ResponseTimes(long s, long e) {
        start = s;
        end = e;
    }


    long getStartTime() { return start; }

    long getEndTime() { return end; }

    double getDurationMsecs() { return (end - start) / 1000000.0; }

}
