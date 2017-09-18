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
