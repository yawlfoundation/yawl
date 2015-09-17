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

package org.yawlfoundation.yawl.worklet.support;

import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;
import org.yawlfoundation.yawl.worklet.selection.LaunchEvent;

import java.util.Map;
import java.util.Set;

/** The WorkletRecord class maintains a generic dataset for classes
 *  that manage a currently running worklet for a 'parent' process.
 *
 * It is extended by selection.CheckedOutChildItem and exception.HandlerRunner
 *
 *  @author Michael Adams
 *  @version 0.8, 04-09/2006
 */


public class WorkletRecord {

    protected CaseMap _runners = new CaseMap() ;  // executing caseid <==> name mapping
    protected WorkItemRecord _wir ;          // the child workitem
    protected Element _datalist ;            // data passed to this workitem
    protected RuleType _reasonType ;         // why the worklet was raised
    protected static Logger _log ;           // log file for debug messages

    protected String _persistID ;            // unique id field for persistence
    protected boolean _hasPersisted = false; // set to true when row is inserted
    protected String _wirStr ;               // intermediate str for persistence
    protected String _runningCaseIdStr ;     // intermediate str for persistence
    protected String _runningWorkletStr ;     // intermediate str for persistence


    /**
     *  Constructs a basic WorkletRecord
     */
    public WorkletRecord() {}

//===========================================================================//

    // SETTERS //

    public void setItem(WorkItemRecord w) {
        _wir = w ;
        persistThis();
    }


    public void setDatalist(Element d) {
        _datalist = d ;
    }


    public void addRunner(String caseID, String wName) {
     //   _runners.addCase(caseID, wName);
        persistThis();
    }


    public void setExType(RuleType xType) {
        _reasonType = xType;
        persistThis();
    }


    public void removeRunnerByCaseID(String caseID) {
        _runners.removeCase(caseID) ;
        persistThis();
    }

    public void removeRunnerByWorkletName(String wName) {
        _runners.removeWorklet(wName);
        persistThis();
    }

    public void removeAllCases() {
        _runners.removeAllCases();
    }


 //===========================================================================//

    //*** GETTERS ***//

    public String getWorkletName(String caseID) {
        return _runners.getWorkletName(caseID) ;
    }

    public String getWorkletCaseID(String wName) {
        return _runners.getCaseID(wName) ;
    }

    public Set<String> getWorkletList() {
        return _runners.getAllWorkletNames() ;
    }

    public Map<String, String> getCaseMapAsCSVList() {
        return _runners.getCaseMapAsCSVLists();
    }


     public WorkItemRecord getItem() {
        return _wir ;
    }


    public String getItemId() {
        return _wir.getID() ;
    }


    public Set<String> getRunningCaseIds() {
        return _runners.getAllCaseIDs() ;                      // the worklet case ids
    }


    public Element getDatalist() {
        return _datalist ;
    }


    public String getCaseID() {
        return _wir.getCaseID();            // the originating workitem's case id
    }

    public YSpecificationID getSpecID() {
        return new YSpecificationID(_wir);      // i.e. of the originating workitem
    }

    public RuleType getRuleType() {
        return _reasonType ;
    }

    public boolean hasRunningWorklet() {
        return _runners.hasRunningWorklets();
    }

//===========================================================================//

    // ACCESSORS & MUTATORS FOR PERSISTENCE //

    protected void set_wirStr(String s) {
        _wirStr = s;
    }

    public String get_wirStr() {
        if (_wirStr == null)

            // only if there's a workitem for this record, create its string
            if (_wir != null) _wirStr = _wir.toXML() ;
        return _wirStr ;
    }


    protected void set_runningCaseIdStr(String s) {
        _runningCaseIdStr = s;
    }

    protected String get_runningCaseIdStr() {
        if (_runners != null) {
            Map<String, String> cases = _runners.getCaseMapAsCSVLists();
            _runningCaseIdStr = cases.get("caseIDs");
        }
         return _runningCaseIdStr ;
    }


    protected void set_runningWorkletStr(String s) {
        _runningWorkletStr = s ;
    }

    protected void set_reasonType(int i) {
        _reasonType = RuleType.values()[i] ;
    }

    protected String get_runningWorkletStr() {
        if (_runners != null) {
            Map<String, String> cases = _runners.getCaseMapAsCSVLists();
            _runningWorkletStr = cases.get("workletNames");
        }
        return _runningWorkletStr ;
    }

    protected int get_reasonType() {
        return _reasonType.ordinal() ;
    }


    protected void set_searchPairStr(String s) { }

    public String get_searchPairStr() { return ""; }

    public void rebuildSearchPair(YSpecificationID specID, String taskID) { }


    public void restoreCaseMap() {
        _runners.restore(_runningCaseIdStr, _runningWorkletStr) ;
    }

    public void ObjectPersisted() {
        _hasPersisted = true ;
    }


    protected void persistThis() {
        if (_hasPersisted) Persister.update(this);
    }


//===========================================================================//

    //*** SAVE METHODS **//

    /**
     * writes the node id's for the nodes returned from the rdr search
     * and the data for the current workitem, to a file for later
     * input into the 'add rule' process, if required
     */
    public void logLaunchEvent() {

        // add the worklet names and case ids
        for (String wName : _runners.getAllWorkletNames()) {
            LaunchEvent event = new LaunchEvent(_wir, _reasonType,
                    _runners.getCaseID(wName), JDOMUtil.elementToString(_datalist));
            Persister.insert(event);
        }

     }


//===========================================================================//

    /** returns a String representation of current WorkletRecord */
    public String toString() {
        StringBuilder s = new StringBuilder("##### WORKLET RECORD #####") ;
        s.append(Library.newline);
        s.append(toStringSub());

        return s.toString() ;
    }

    public String toStringSub() {
        StringBuilder s = new StringBuilder() ;
        String wirStr = (_wir != null)? _wir.toXML() : "null";
        Library.appendLine(s, "WORKITEM", wirStr);

        String data = JDOMUtil.elementToStringDump(_datalist);
        Library.appendLine(s, "DATALIST", data);

        Library.appendLine(s, "WORKLET NAMES", _runners.getWorkletCSVList());
        Library.appendLine(s, "RUNNING CASE IDs", _runners.getCaseIdCSVList());

        s.append(Library.newline);
        return s.toString() ;
    }

//===========================================================================//
//===========================================================================//

}
