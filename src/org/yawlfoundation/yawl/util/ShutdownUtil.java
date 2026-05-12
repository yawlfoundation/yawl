package org.yawlfoundation.yawl.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Michael Adams
 * @date 30/4/2026
 */
public class ShutdownUtil {

    private static final Logger _log = LogManager.getLogger(ShutdownUtil.class.getName());

    
    public static void shutdownExecutor(ExecutorService executor, String name) {
        if (executor == null) return;
        _log.debug("Stopping Executor: " + name);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    
    public static void shutdownTimer(Timer timer, String name) {
        if (timer == null) return;
        _log.debug("Stopping Timer: " + name);
        timer.cancel();
        timer.purge(); 
    }
}

