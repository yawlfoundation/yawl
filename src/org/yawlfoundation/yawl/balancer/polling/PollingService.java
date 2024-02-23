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

package org.yawlfoundation.yawl.balancer.polling;

import org.yawlfoundation.yawl.balancer.config.Config;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author Michael Adams
 * @date 18/9/17
 */
public class PollingService {

    private static final ScheduledExecutorService EXECUTOR =
            Executors.newScheduledThreadPool(4);

    private static final Set<Pollable> POLLABLE_SET = new HashSet<Pollable>();
    
    private static final Set<ScheduledFuture<?>> SCHEDULED_FUTURES =
            new HashSet<ScheduledFuture<?>>();



    public static void add(Pollable pollable) {
        POLLABLE_SET.add(pollable);
    }


    public static void remove(Pollable pollable) {
        POLLABLE_SET.remove(pollable);
    }


    public static void schedule() {
        schedule(Config.getPollInterval());
    }


    public static void reschedule() {
        clearSchedule();
        schedule(Config.getPollInterval());
    }


    public static void shutdown() {
        EXECUTOR.shutdownNow();
    }

    
    public static void schedule(int pollPeriodAsSeconds) {
        if (pollPeriodAsSeconds > 0) {
            for (final Pollable p : POLLABLE_SET) {
                SCHEDULED_FUTURES.add(EXECUTOR.scheduleAtFixedRate(
                        new Runnable() {
                            @Override
                            public void run() {
                                p.scheduledEvent();
                            }
                        }, 0, pollPeriodAsSeconds, TimeUnit.SECONDS
                ));
            }
        }
    }


    private static void clearSchedule() {
        for (ScheduledFuture future : SCHEDULED_FUTURES) {
            future.cancel(true);
        }
        SCHEDULED_FUTURES.clear();
    }

}
