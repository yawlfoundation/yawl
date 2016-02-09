/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.worklet.exception;

import org.jdom2.Element;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.worklet.rdr.RdrConclusion;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;
import org.yawlfoundation.yawl.worklet.selection.AbstractRunner;
import org.yawlfoundation.yawl.worklet.selection.RunnerMap;
import org.yawlfoundation.yawl.worklet.selection.WorkletRunner;
import org.yawlfoundation.yawl.worklet.support.Persister;

import java.util.HashSet;
import java.util.Set;

/**
 * The ExletRunner class manages an exception handling process. An instance
 *  of this class is created for each exception process raised by a case.
 *  This class also manages running worklet instances for a 'parent' case
 *  when required.
 *
 *  The key data member is the RdrConclusion _conclusion, which contains the
 *  sequential set of exception handling primitives for this particular handler.
 *
 *  @author Michael Adams
 *  @version 0.8, 04-09/2006; updated for v4 9/2015
 */

public class ExletRunner extends AbstractRunner {

    private RdrConclusion _conclusion = null ;      // steps to run for this handler
    private int _actionIndex = 1 ;                  // index to 'primitives' set
    private boolean _isItemSuspended;               // has excepted item been suspended?
    private boolean _isCaseSuspended;               // has case been suspended?
    private final RunnerMap _worklets = new RunnerMap();  // set of running compensations

    // list of suspended items - can be child items, or for whole case
    private Set<String> _suspendedItems = null;


    /**
     * This constructor is used when an exception is raised at the case level
     * @param rdrConc the RdrConclusion of a rule that represents the handling process
     */
    public ExletRunner(YSpecificationID specID, String caseID, RdrConclusion rdrConc,
                       RuleType xType) {
        _conclusion = rdrConc ;
        _ruleType = xType ;
        _caseID = caseID;
        _parentSpecID = specID;
    }


    /** This constructor is used when an exception is raised at the workitem level */
    public ExletRunner(WorkItemRecord wir, RdrConclusion rdrConc, RuleType xType) {
        this(new YSpecificationID(wir), wir.getRootCaseID(), rdrConc, xType);
        _wir = wir ;
        _wirID = wir.getID();
    }


    /** This one's for persistence */
    private ExletRunner() {}



   /** @return the action for the current index from the RdrConclusion */
    public String getNextAction() {
        return _conclusion.getAction(_actionIndex);
    }


    /** @return the target for the current index from the RdrConclusion */
    public String getNextTarget() {
        return _conclusion.getTarget(_actionIndex);
    }


    /** @return the current index in the set of actions */
    public int getActionIndex() {
        return _actionIndex ;
    }


    /** @return the total number of primitives in the conclusion */
    public int getActionCount() { return _conclusion.getCount(); }


    /** @return the list of currently suspended workitems for this runner */
    public Set<String> getSuspendedItems() { return _suspendedItems ; }


    /** @return the data params for the parent workitem/case */
    public Element getWorkItemDatalist() { return getWorkItemData(); }


    public Element getWorkItemUpdatedData() { return getWir().getUpdatedData(); }


    public void addWorklet(WorkletRunner runner) { _worklets.add(runner); }

    public WorkletRunner removeWorklet(WorkletRunner runner) {
        return _worklets.remove(runner);
    }

    public WorkletRunner removeWorklet(String caseID) {
        return _worklets.remove(caseID);
    }

    public boolean hasRunningWorklet() { return ! _worklets.isEmpty(); }

    public Set<WorkletRunner> getWorkletRunners() { return _worklets.getAll(); }

    protected void addWorkletRunners(Set<WorkletRunner> runners) {
        if (! runners.isEmpty()) {
            _worklets.addAll(runners);
//            if (_ruleType.isCaseLevelType()) {
//                for (WorkletRunner runner : runners) {
//                    runner.setParentCaseID(getCaseID());
//                    runner.setParentSpecID(getSpecID());
//                }
//            }
        }
    }


    public Set<WorkletRunner> restoreWorkletRunners() {
        _worklets.restore(getCaseID());
        return _worklets.getAll();
    }


    public void setItem(WorkItemRecord item) {
        _wir = item;
        _wirID = item.getID();
    }

    /** called when an action suspends the workitem of this ExletRunner */
    public void setItemSuspended() {
        _isItemSuspended = true ;
        persistThis();
    }


    /** called when an action unsuspends the workitem of this ExletRunner */
    public void unsetItemSuspended() {
        _isItemSuspended = false ;
        clearSuspendedItems();
    }

    /** called when an action suspends the case of this ExletRunner */
    public void setCaseSuspended() {
        _isCaseSuspended = true ;
        persistThis();
    }


    /** called when an action unsuspends the case of this ExletRunner */
    public void clearCaseSuspended() {
        _isCaseSuspended = false ;
        clearSuspendedItems();
    }


     /** called when an action suspends the item or parent case of this ExletRunner */
    public void setSuspendedItems(Set<WorkItemRecord> items) {
        _suspendedItems = new HashSet<String>();
        for (WorkItemRecord wir : items) {
            _suspendedItems.add(wir.getID());
        }
        persistThis();
    }


    /** called when an action unsuspends the item or parent case of this ExletRunner */
    public void clearSuspendedItems() {
        _suspendedItems = null ;
        persistThis();
    }


    public int incActionIndex() {
        ++_actionIndex;
        persistThis();
        return _actionIndex;
    }

     /** @return true if there is another action to run in the set */
    public boolean hasNextAction() {
        return (_actionIndex <= getActionCount());
    }

    public boolean isItemSuspended() {
        return _isItemSuspended;
    }

    public boolean isCaseSuspended() {
        return _isCaseSuspended;
    }


    // PERSISTENCE METHODS //

    private void setActionIndex(int i) { _actionIndex = i; }

    private void setItemSuspended(boolean b) { _isItemSuspended = b; }

    private void setCaseSuspended(boolean b) { _isCaseSuspended = b; }

    private void persistThis() {
        Persister.update(this);
    }

}  // end ExletRunner class


