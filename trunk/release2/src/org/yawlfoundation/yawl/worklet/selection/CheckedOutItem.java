/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.worklet.support.DBManager;
import org.yawlfoundation.yawl.worklet.support.Library;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// import org.apache.log4j.Logger;



/** The CheckedOutItem class maintains, for a workitem that has been 
 *  checked-out of the engine, a dynamic set of data for each of the child 
 *  items of which this workitem is a 'parent'.
 *
 *  Note that all YAWL workitems have at least one child.
 *
 *  ==================        =======================
 *  | CheckedOutItem | 1----M | CheckedOutChildItem |
 *  ==================        =======================
 *         ^^^
 *
 *  @author Michael Adams
 *  v0.8, 04-09/2006
 */

public class CheckedOutItem {

    private WorkItemRecord _wir ;          // the id of the 'parent' workitem
    private ArrayList _myChildren ;        // list of checked out children
    private YSpecificationID _specId ;               // specification that task is in
    private CheckedOutItem _me ;           // reference to self
    private int _spawnCount ;              // original number of items spawned for task
    private int _miThreshold ;             // Threshold of MI Task (if this is a MI task)
    private int _completions ;             // count of completed items (used if MI task)

    private String _persistID ;            // unique id field for persistence
    private String _wirStr ;               // intermediate string needed for persistence

    //   private static Logger _log ;                  // log file for debug messages

    public CheckedOutItem() {}             // required for persistence

    /** Constructs a CheckedOutItem
     *  @param w - the WorkItemRecord of the 'parent' workitem
     */
    public CheckedOutItem(WorkItemRecord w) {
        _wir = w ;
        _persistID = _wir.getID();

        initNonPersistedItems();
    }

//===========================================================================//

    // ACCESSORS & MUTATORS NEEDED FOR PERSISTENCE //

    public void set_persistID(String id) {
        _persistID = id ;
    }

    public void set_wirStr(String s) {
        _wirStr = s;
    }

    public String get_wirStr() {
        if (_wirStr == null) _wirStr = _wir.toXML() ;
        return _wirStr ;
    }

    public String get_persistID() {
        return _persistID ;
    }

    // initialise the data members that are not persisted (after a restore)
    public void initNonPersistedItems() {
        _specId = new YSpecificationID(_wir) ;
        _myChildren = new ArrayList() ;
        _me = this ;
        //  _log = Logger.getLogger("org.yawlfoundation.yawl.worklet.selection.CheckedOutItem");
    }

    // update the persisted object
    public void persistThis() {
        DBManager dbMgr = DBManager.getInstance(false);
        if ((dbMgr != null) && dbMgr.isPersisting())
            dbMgr.persist(this, DBManager.DB_UPDATE);
    }



//===========================================================================//


    // SETTERS //

    public void setItem(WorkItemRecord w) {
        _wir = w ;
        _persistID = _wir.getID();
        initNonPersistedItems();
        persistThis();
    }


    public void setChildren(List c) {
        _myChildren = (ArrayList) c ;
    }


    public void setSpawnCount(int count) {
        _spawnCount = count ;
    }

    public void setThreshold(int thres) {
        _miThreshold = thres ;
    }


    public boolean isMultiTask() { return _spawnCount > 1 ; }

    public void incCompletedItems() { _completions++ ; }

    public boolean thresholdReached() { return _completions == _miThreshold ; }

//===========================================================================//

    // GETTERS //

    public WorkItemRecord getItem() {
        return _wir ;
    }


    public YSpecificationID getSpecId() {
        return _specId ;
    }


    public List getChildren() {
        return _myChildren ;
    }


    public String getParentID() {
        return _persistID ;
    }

    public int getSpawnCount() {
        return _spawnCount ;
    }


    public int getThreshold() {
        return _miThreshold ;
    }


    /** returns the WorkItemRecord of the index childitem */
    public WorkItemRecord getChildWorkItem(int i) {
        if (_myChildren.size() < i) return null ;                  // no kids!
        else {
            CheckedOutChildItem c = getCheckedOutChildItem(i) ;
            if (c != null) return c.getItem() ;
            else return null ;
        }
    }

    /** returns the CheckedOutChildItem object of the index childitem */
    public CheckedOutChildItem getCheckedOutChildItem(int i) {
        if (_myChildren.size() < i) return null ;                  // no kids!
        return (CheckedOutChildItem) _myChildren.get(i) ;
    }


    /** returns the CheckedOutChildItem object of the childitem with the id */
    public CheckedOutChildItem getCheckedOutChildItem(String itemId) {
        Iterator itr = _myChildren.iterator();
        while (itr.hasNext()) {                              // for each child
            CheckedOutChildItem c = (CheckedOutChildItem) itr.next() ;
            if (itemId.equals(c.getItem().getID())) return c ;
        }
        return null ;
    }

//===========================================================================//

    // CHILD MAINTENANCE //

    /** adds a new child item to this parent */
    public void addChild(WorkItemRecord w) {
        addChild(new CheckedOutChildItem(w)) ;
    }


    /** adds child from ChildItem (used when restoring from persistence) */
    public void addChild(CheckedOutChildItem child) {
        child.setParent(_me) ;
        _myChildren.add(child) ;                           // keep track of children
    }


    /** returns the number of children of this parent */
    public int getChildCount() {
        return _myChildren.size() ;
    }


    /** removes the indexed child from the list of children */
    public void removeChild(int idx) {
        if (idx < _myChildren.size())
            _myChildren.remove(idx) ;
    }


    /**
     *  removes the child referenced by the CheckedOutChildItem passed
     *  @return the (former) index of the removed child
     */
    public int removeChild(CheckedOutChildItem childToRemove) {

        // for each child in list of children
        for (int i = 0; i < _myChildren.size(); i++) {
            CheckedOutChildItem c = (CheckedOutChildItem) _myChildren.get(i) ;

            // if this object matches the one passed, remove it
            if (childToRemove.getItem().getID().equals(c.getItem().getID())) {
                _myChildren.remove(i) ;
                return i ;
            }
        }
        return -1 ;
    }

    /** removes all child records from this parent */
    public void removeAllChildren() {
        _myChildren.clear();
    }


    /** returns true if this parent has children */
    public boolean hasCheckedOutChildItems() {
        return (_myChildren.size() > 0) ;
    }


    /** returns true if this parent has the child passed */
    public boolean hasCheckedOutChildItem(CheckedOutChildItem child) {
        return (_myChildren.contains(child));
    }

//===========================================================================//


    /** returns String representation of  current CheckedOutItem */
    public String toString() {
        StringBuilder s = new StringBuilder("##### CHECKEDOUTITEM RECORD #####") ;
        String n = Library.newline ;

        // write out the parent wir
        s.append(n);
        Library.appendLine(s, "CHECKED OUT ITEM", toStringSub());

        // build this toString by calling the toStringSub of each child
        s.append("CHILD ITEM(S): ");
        s.append(n);
        Iterator itr = _myChildren.iterator();
        while (itr.hasNext()) {
            CheckedOutChildItem child = (CheckedOutChildItem) itr.next() ;
            s.append(child.toStringSub());
            s.append(n);
        }

        return s.toString() ;
    }


    public String toStringSub() {
        StringBuilder s = new StringBuilder();
        s.append(_wir.toXML());
        s.append(Library.newline);
        return s.toString() ;
    }

//===========================================================================//
//===========================================================================//

}
	
