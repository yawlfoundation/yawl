package org.yawlfoundation.yawl.worklet.exception;

import org.yawlfoundation.yawl.worklet.rdr.RuleType;
import org.yawlfoundation.yawl.worklet.selection.WorkletRunner;
import org.yawlfoundation.yawl.worklet.support.Persister;

import java.util.*;

/**
 * @author Michael Adams
 * @date 8/02/2016
 */
public class ExletRunnerCache {

    private final Set<ExletRunner> _runners = new HashSet<ExletRunner>();


    public boolean add(ExletRunner runner) {
        return runner != null && _runners.add(runner);
    }


    public boolean remove(ExletRunner runner) {
        return runner != null && _runners.remove(runner);
    }



    public Set<ExletRunner> getRunnersForCase(String caseID) {
        if (caseID != null) {
            Set<ExletRunner> caseRunners = new HashSet<ExletRunner>();
            for (ExletRunner runner : _runners) {
                if (caseID.equals(runner.getCaseID())) {
                    caseRunners.add(runner);
                }
            }
            return caseRunners;
        }
        return Collections.emptySet();
    }


    public ExletRunner getRunnerForItem(String itemID) {
        if (itemID != null) {
            for (ExletRunner runner : _runners) {
                if (itemID.equals(runner.getWorkItemID())) {
                    return runner;
                }
            }
        }
        return null;
    }


    public ExletRunner getRunner(RuleType xType, String caseID, String itemID) {
        return itemID != null ? getRunnerForItem(itemID) : getRunner(xType, caseID);
    }


    public ExletRunner getRunner(RuleType xType, String caseID) {
        if (caseID != null) {
            for (ExletRunner runner : getRunnersForCase(caseID)) {
                if (runner.getRuleType() == xType) {
                    return runner;
                }
            }
        }
        return null;
    }


    public ExletRunner getRunnerForWorklet(String caseID) {
        for (ExletRunner runner : _runners) {
            for (WorkletRunner worklet : runner.getWorkletRunners()) {
                if (worklet.getCaseID().equals(caseID)) {
                    return runner;
                }
            }
        }
        return null;
    }


    public Set<WorkletRunner> getWorkletsForCase(String caseID) {
        if (caseID != null) {
            Set<WorkletRunner> worklets = new HashSet<WorkletRunner>();
            for (ExletRunner runner : getRunnersForCase(caseID)) {
                worklets.addAll(runner.getWorkletRunners());
            }
            return worklets;
        }
        return Collections.emptySet();
    }


    public Set<WorkletRunner> getWorkletsForItem(String itemID) {
        return getWorklets(getRunnerForItem(itemID));
    }


    public Set<WorkletRunner> getAllWorklets() {
        Set<WorkletRunner> worklets = new HashSet<WorkletRunner>();
        for (ExletRunner runner : _runners) {
            worklets.addAll(runner.getWorkletRunners());
        }
        return worklets;
    }



    public boolean isCompensationWorklet(String caseID) {
        return getWorkletRunner(caseID) != null;
    }


    public WorkletRunner getWorkletRunner(String caseID) {
        for (ExletRunner runner : _runners) {
            for (WorkletRunner worklet : runner.getWorkletRunners()) {
                if (worklet.getCaseID().equals(caseID)) {
                    return worklet;
                }
            }
        }
        return null;
    }


    /** restores active ExletRunner instances */
    public void restore() {
        List items = Persister.getInstance().getObjectsForClass(
                ExletRunner.class.getName());

        for (Object o : items) _runners.add((ExletRunner) o);
        Persister.getInstance().commit();
    }


    private Set<WorkletRunner> getWorklets(ExletRunner runner) {
        return runner != null ? runner.getWorkletRunners() : null;
    }



}
