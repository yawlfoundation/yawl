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

    private Map<String, WorkletRunner> _runners = new HashMap<String, WorkletRunner>();


    public WorkletRunner add(WorkletRunner runner) {
        WorkletRunner added = _runners.put(runner.getCaseID(), runner);
        Persister.insert(runner);
        return added;
    }


    public boolean addAll(Set<WorkletRunner> runners) {
        for (WorkletRunner runner : runners) {
            add(runner);
        }
        return true;
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
            String runnerParentID = runner.getParentWorkItemID();
            if (runnerParentID != null && runnerParentID.equals(parentID)) {
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
            String runnerParentID = runner.getParentWorkItemID();
            if (runnerParentID != null && runnerParentID.equals(parentID)) {
                return true;
            }
        }
        return false;
    }


    public Set<WorkletRunner> getRunnersForCase(String caseID) {
        Set<WorkletRunner> runners = new HashSet<WorkletRunner>();
        for (WorkletRunner runner : _runners.values()) {
            String runnerParentID = runner.getParentCaseID();
            if (runnerParentID != null && runnerParentID.equals(caseID)) {
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


    public void restore(String caseID) {
        int selectionType = RuleType.ItemSelection.ordinal();
        String clause = "wr._ruleType" + (caseID == null ? "=" + selectionType :
                "!=" + selectionType + " and wr._parentCaseID='" + caseID + "'");
        Query query = Persister.getInstance().createQuery(
                "from WorkletRunner as wr where " + clause);
        Iterator it = query.iterate();
        if (it.hasNext()) {
            while (it.hasNext()) {
                WorkletRunner runner = (WorkletRunner) it.next();
                _runners.put(runner.getCaseID(), runner);
            }
        }
        Persister.getInstance().commit();
    }

}
