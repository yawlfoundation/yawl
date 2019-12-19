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
