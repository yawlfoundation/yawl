package org.yawlfoundation.yawl.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// per task resource configs
class TaskResourceSettings {

    private List<Integer> timings;
    private int concurrent = 1;
    private static final int DEFAULT_PROCESSING_TIME = 4000;


    TaskResourceSettings() { timings = new ArrayList<Integer>(); }


    void addTiming(int timing) { timings.add(timing); }

    int getTiming() {
        switch (timings.size()) {
            case 0  : return DEFAULT_PROCESSING_TIME;
            case 1  : return timings.get(0);
            default : return timings.get(new Random().nextInt(timings.size()));
        }
    }


    int getConcurrent() { return concurrent; }

    void setConcurrent(int c) { concurrent = c; }
}
