package org.yawlfoundation.yawl.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Michael Adams
 * @date 30/4/2026
 *
 * Registers and runs shutdown tasks 
 */
public class ShutdownTaskHandler {

    private static final List<ShutdownTask> tasks = new CopyOnWriteArrayList<>();


    public static void register(ShutdownTask task) {
        tasks.add(task);
    }


    public static void performShutdown() {

        for (ShutdownTask task : tasks) {
            try {
                task.runShutdown();
            } catch (Exception e) {
                // Log and continue to the next one
            }
        }
    }
}
