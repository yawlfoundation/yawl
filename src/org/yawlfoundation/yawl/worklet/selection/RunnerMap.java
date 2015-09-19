package org.yawlfoundation.yawl.worklet.selection;

import org.hibernate.Query;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;
import org.yawlfoundation.yawl.worklet.support.Persister;

import java.util.*;

/**
 * @author Michael Adams
 * @date 4/09/15
 */
public class RunnerMap {

    public static final boolean SELECTION_RUNNERS = true;
    public static final boolean EXCEPTION_RUNNERS = false;

    private Map<String, WorkletRunner> _runners = new HashMap<String, WorkletRunner>();


    public WorkletRunner add(WorkletRunner runner) {
        WorkletRunner added = _runners.put(runner.getCaseID(), runner);
        Persister.insert(runner);
        return added;
    }

    public WorkletRunner remove(WorkletRunner runner) {
        return remove(runner.getCaseID());
    }


    public WorkletRunner remove(String caseID) {
        WorkletRunner removed = _runners.remove(caseID);
        if (removed != null) {
            Persister.delete(removed);
        }
        return removed;
    }


    public boolean isWorklet(String caseID) {
        return _runners.containsKey(caseID);
    }


    public boolean isEmpty() {
        return _runners.isEmpty();
    }


    public WorkletRunner getWorkletRunner(String caseID) {
        return _runners.get(caseID);
    }


    public Set<WorkletRunner> getRunnersForWorkItem(String wirID) {
        Set<WorkletRunner> runners = new HashSet<WorkletRunner>();
        for (WorkletRunner runner : _runners.values()) {
            if (runner.getWorkItemID().equals(wirID)) {
                runners.add(runner);
            }
        }
        return runners;
    }


    public Set<WorkletRunner> getRunnersForParentWorkItem(String parentID) {
        Set<WorkletRunner> runners = new HashSet<WorkletRunner>();
        for (WorkletRunner runner : _runners.values()) {
            if (runner.getParentWorkItemID().equals(parentID)) {
                runners.add(runner);
            }
        }
        return runners;
    }


    public Set<WorkItemRecord> getCheckedOutWorkItemsForParent(String parentID) {
        Set<WorkItemRecord> checkedOutItems = new HashSet<WorkItemRecord>();
        for (WorkletRunner runner : getRunnersForParentWorkItem(parentID)) {
            checkedOutItems.add(runner.getWir());
        }
        return checkedOutItems;
    }

    public void removeRunners(Set<WorkletRunner> runners) {
        Persister.getInstance().beginTransaction();
        for (WorkletRunner runner : runners) {
            if (remove(runner.getCaseID()) != null) {
                Persister.delete(runner, false);
            }
        }
        Persister.getInstance().commit();
    }


    public boolean hasRunnersForWorkItem(String wirID) {
        if (wirID == null) return false;
        for (WorkletRunner runner : _runners.values()) {
            if (runner.getWorkItemID().equals(wirID)) {
                return true;
            }
        }
        return false;
    }


    public boolean hasRunnersForParentWorkItem(String parentID) {
        if (parentID == null) return false;
        for (WorkletRunner runner : _runners.values()) {
            if (runner.getParentWorkItemID().equals(parentID)) {
                return true;
            }
        }
        return false;
    }


    public Set<WorkletRunner> getRunnersForCase(String caseID) {
        Set<WorkletRunner> runners = new HashSet<WorkletRunner>();
        for (WorkletRunner runner : _runners.values()) {
            if (runner.getParentCaseID().equals(caseID)) {
                runners.add(runner);
            }
        }
        return runners;
    }


    public Set<WorkletRunner> getRunnersForAncestorCase(String caseID) {
        Set<WorkletRunner> runners = getRunnersForCase(caseID);
        return getChildRunners(runners);
    }


    public Set<WorkletRunner> getChildRunners(Set<WorkletRunner> runners) {
        if (! runners.isEmpty()) {
            for (WorkletRunner runner : new HashSet<WorkletRunner>(runners)) {
                Set<WorkletRunner> childRunners = getRunnersForCase(runner.getCaseID());
                runners.addAll(getChildRunners(childRunners));
            }
        }
        return runners;
    }


    public Set<WorkletRunner> getAll() {
        return new HashSet<WorkletRunner>(_runners.values());
    }


    public void restore(boolean selectionOnly) {
        int selectionType = RuleType.ItemSelection.ordinal();
        String op = selectionOnly ? "=" : "!=";
        Query query = Persister.getInstance().createQuery(
                "from WorkletRunner as wr where wr._ruleType" + op + selectionType);
        Iterator it = query.iterate();
        if (it.hasNext()) {
            while (it.hasNext()) {
                WorkletRunner runner = (WorkletRunner) it.next();
                _runners.put(runner.getCaseID(), runner);
            }
        }
    }

}
