package org.yawlfoundation.yawl.simulation;

import java.util.Random;

// per task resource configs
class TaskResourceSettings {

    private int maxTime;
    private int minTime;
    private int concurrent = 1;
    private static final Random RANDOM = new Random();


    TaskResourceSettings() { }


    void addTiming(int time, int deviation) {
        maxTime = time + deviation;
        minTime = time - deviation;
    }

    int getTiming() {
        return RANDOM.nextInt((maxTime - minTime) + 1) + minTime;
    }


    int getConcurrent() { return concurrent; }

    void setConcurrent(int c) { concurrent = c; }
}
