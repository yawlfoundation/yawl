package org.yawlfoundation.yawl.worklet.support;

import org.yawlfoundation.yawl.worklet.exception.CaseMonitor;
import org.yawlfoundation.yawl.worklet.exception.ExletRunner;
import org.yawlfoundation.yawl.worklet.selection.WorkletRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 8/01/2016
 */
public class StateRestorer {


    /** restores active HandlerRunner instances */
    public Map<String, ExletRunner> restoreRunners() {
        Map<String, ExletRunner> runnerMap = new HashMap<String, ExletRunner>();

        // retrieve persisted runner objects from database
        List items = loadClassesByName(ExletRunner.class.getName());

        if (items != null) {
            for (Object o : items) {
                ExletRunner runner = (ExletRunner) o;
                runnerMap.put(runner.getCaseID(), runner);
            }
        }
        return runnerMap ;
    }


    /** Restores active CaseMonitor instances
     * @param runnerMap - the set of restored HandlerRunner instances
     * @return the set of restored CaseMonitor instances
     */
    public Map<String, CaseMonitor> restoreMonitoredCases(
            Map<String, ExletRunner> runnerMap, Map<String, ExletRunner> compensatorsMap) {
        Map<String, CaseMonitor> monitorMap = new HashMap<String, CaseMonitor>();

        // retrieve persisted monitor objects from database
        List items = loadClassesByName(CaseMonitor.class.getName());

        if (items != null) {
            for (Object o : items) {
                CaseMonitor monitor = (CaseMonitor) o;

                // 'reattach' relevant runners to this case monitor
                List<ExletRunner> restoredRunners = monitor.restoreRunners(
                        runnerMap);
                compensatorsMap.putAll(rebuildHandlersStarted(restoredRunners));

                monitor.initNonPersistedItems();            // finish the reconstitution
                monitorMap.put(monitor.getCaseID(), monitor);
            }
        }
        return monitorMap ;
    }


    /** add the runners with active worklet instances to handlersStarted */
    private Map<String, ExletRunner> rebuildHandlersStarted(List<ExletRunner> runners) {
        Map<String, ExletRunner> handlersStarted = new HashMap<String, ExletRunner>();
        for (ExletRunner runner : runners) {
            if (runner.hasRunningWorklet()) {
                for (WorkletRunner wRunner : runner.getWorkletRunners()) {
                    handlersStarted.put(wRunner.getCaseID(), runner);
                }
            }
        }
        return handlersStarted;
    }



    private List loadClassesByName(String className) {
        return Persister.getInstance().getObjectsForClass(className);
    }

}
