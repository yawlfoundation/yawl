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
import org.yawlfoundation.yawl.worklet.support.WorkletConstants;
import org.yawlfoundation.yawl.worklet.support.Persister;

import java.util.Set;

/**
 * The HandlerRunner class manages an exception handling process. An instance
 *  of this class is created for each exception process raised by a case. The CaseMonitor
 *  class maintains the current set of HandlerRunners for a case (amongst other things).
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
    private CaseMonitor _parentMonitor = null ;     // case that generated this instance
    private int _actionIndex = 1 ;                  // index to 'primitives' set
    private boolean _isItemSuspended;               // has excepted item been suspended?
    private boolean _isCaseSuspended;               // has case been suspended?
    private String _trigger;                        // for external exceptions
    private final RunnerMap _worklets = new RunnerMap();  // set of running compensations

    // list of suspended items - can be child items, or for whole case
    private Set<WorkItemRecord> _suspendedItems = null;


   /**
     * This constructor is used when an exception is raised at the case level
     * @param monitor the CaseMonitor for the case the generated the exception
     * @param rdrConc the RdrConclusion of a rule that represents the handling process
     */
    public ExletRunner(CaseMonitor monitor, RdrConclusion rdrConc, RuleType xType) {
        _parentMonitor = monitor ;
        _conclusion = rdrConc ;
        _ruleType = xType ;
        _caseID = monitor.getCaseID();
        _trigger = monitor.getTrigger();                 // may be null
    }


    /** This constructor is used when an exception is raised at the workitem level */
    public ExletRunner(CaseMonitor monitor, WorkItemRecord wir,
                       RdrConclusion rdrConc, RuleType xType) {
        this(monitor, rdrConc, xType);
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


    /** @return the id of the spec of the case that raised the exception */
   public YSpecificationID getSpecID() {
       return _parentMonitor.getSpecID();
   }


    /** @return the CaseMonitor that is the container for this HandlerRunner */
    public CaseMonitor getOwnerCaseMonitor() {
        return _parentMonitor ;
    }


    /** @return the list of currently suspended workitems for this runner */
    public Set<WorkItemRecord> getSuspendedItems() {
       return _suspendedItems ;
    }


    /** @return the data params for the parent workitem/case */
    public Element getDatalist() {
        return _ruleType.isCaseLevelType() ?_parentMonitor.getCaseData() :
            getWorkItemData();
    }

    public Element getUpdatedData() {
        return _ruleType.isCaseLevelType() ? _parentMonitor.getCaseData() :
                getWir().getUpdatedData();
    }


    public void addWorklet(WorkletRunner runner) { _worklets.add(runner); }

    public void removeWorklet(WorkletRunner runner) { _worklets.remove(runner); }

    public void removeWorklet(String caseID) { _worklets.remove(caseID); }

    public boolean hasRunningWorklet() { return ! _worklets.isEmpty(); }

    public Set<WorkletRunner> getWorkletRunners() { return _worklets.getAll(); }

    protected void addWorkletRunners(Set<WorkletRunner> runners) {
        if (! runners.isEmpty()) {
            _worklets.addAll(runners);
            if (_ruleType.isCaseLevelType()) {
                for (WorkletRunner runner : runners) {
                    runner.setParentCaseID(getCaseID());
                    runner.setParentSpecID(_parentMonitor.getSpecID());
                    runner.setData(_parentMonitor.getNetLevelData());
                    runner.setTrigger(_parentMonitor.getTrigger());
                }
            }
        }
    }


    public void setItem(WorkItemRecord item) {
        _wir = item;
        _wirID = item.getID();
    }

    /** called when an action suspends the workitem of this HandlerRunner */
    public void setItemSuspended() {
        _isItemSuspended = true ;
        persistThis();
    }


    /** called when an action unsuspends the workitem of this HandlerRunner */
    public void unsetItemSuspended() {
        _isItemSuspended = false ;
        clearSuspendedItems();
    }

    /** called when an action suspends the case of this HandlerRunner */
    public void setCaseSuspended() {
        _isCaseSuspended = true ;
        persistThis();
    }


    /** called when an action unsuspends the case of this HandlerRunner */
    public void clearCaseSuspended() {
        _isCaseSuspended = false ;
        clearSuspendedItems();
    }


    public void setOwnerCaseMonitor(CaseMonitor monitor) {
         _parentMonitor = monitor ;
     }


     /** called when an action suspends the item or parent case of this HandlerRunner */
    public void setSuspendedItems(Set<WorkItemRecord> items) {
        _suspendedItems = items ;
         persistThis();
    }


    /** called when an action unsuspends the item or parent case of this HandlerRunner */
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


    public String dump() {
        StringBuilder s = new StringBuilder("HandlerRunner record:") ;
        s.append(WorkletConstants.newline);
        s.append(super.toString());

        String conc = (_conclusion == null) ? "null" : _conclusion.toString();
        String parent = (_parentMonitor == null)? "null" :
                         _parentMonitor.getSpecID() + ": " + _parentMonitor.getCaseID();
        String index = String.valueOf(_actionIndex);
        String count = String.valueOf(getActionCount());
        String itemSusp = String.valueOf(_isItemSuspended);
        String caseSusp = String.valueOf(_isCaseSuspended);
        String wirs = "";
        if (_suspendedItems != null) {
            for (WorkItemRecord wir : _suspendedItems) {
               wirs += wir.toXML() + WorkletConstants.newline ;
            }
        }

        s = WorkletConstants.appendLine(s, "RDRConclusion", conc);
        s = WorkletConstants.appendLine(s, "Parent Monitor", parent);
        s = WorkletConstants.appendLine(s, "Action Index", index);
        s = WorkletConstants.appendLine(s, "Action Count", count);
        s = WorkletConstants.appendLine(s, "Item Suspended?", itemSusp);
        s = WorkletConstants.appendLine(s, "Case Suspended?", caseSusp);
        s = WorkletConstants.appendLine(s, "Suspended Items", wirs);

        return s.toString();
    }


    // PERSISTENCE METHODS //

    private void setActionIndex(int i) { _actionIndex = i; }

    private void setItemSuspended(boolean b) { _isItemSuspended = b; }

    private void setCaseSuspended(boolean b) { _isCaseSuspended = b; }

    private void persistThis() {
        Persister.update(this);
    }

}  // end HandlerRunner class


