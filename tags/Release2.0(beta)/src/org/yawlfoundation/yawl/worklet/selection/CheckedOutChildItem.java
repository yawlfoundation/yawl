/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.worklet.selection;

import org.yawlfoundation.yawl.worklet.support.*;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import org.apache.log4j.Logger;


/** The CheckedOutChildItem class maintains a dataset for a 'child' workitem
 *  that has been checked-out of the engine (via its parent).
 *
 *  Note that an atomic task YAWL workitem has one child, and a multiple atomic
 *  task has several, each with its own discrete datalist.
 *
 *  A number of CheckedOutChildItems (representing the child workitems) are 
 *  maintained by one CheckOutItem (representing the parent workitem).
 *
 *  ==================        =======================
 *  | CheckedOutItem | 1----M | CheckedOutChildItem |
 *  ==================        =======================
 *                                      ^^^
 *  @author Michael Adams
 *  v0.8, 04-09/2006
 */

public class CheckedOutChildItem extends WorkletRecord {

    private CheckedOutItem _parent ;       // the 'parent' workitem
    private String _parentID ;             // for persistence link back to parent

    public CheckedOutChildItem() {}        // required for persistence

    /**
     *  Constructs a CheckedOutChildItem
     *  @param w - the WorkItemRecord that describes the child workitem
     */
    public CheckedOutChildItem(WorkItemRecord w) {
        super();
        _wir = w ;
        _persistID = _wir.getID();
        _datalist = _wir.getDataList();
        _wirStr = _wir.toXML();
        _log = Logger.getLogger("org.yawlfoundation.yawl.worklet.selection.CheckedOutChildItem");
    }

//===========================================================================//

    //*** SETTER ***//

    public void setParent(CheckedOutItem p) {
        boolean update = (_parent != null);   // if parent already set, this is an update
        _parent = p ;
        _parentID = p.getParentID();
        if (update) persistThis();            // so persist the change
    }

//===========================================================================//

    //*** GETTER ***//

    public CheckedOutItem getParent() {
        return _parent ;
    }

//===========================================================================//

    // ACCESSORS & MUTATORS FOR PERSISTENCE //

    public String get_persistID() { return _persistID; }

    public void set_persistID(String s) { _persistID = s ; }


    public String get_parentID() { return _parentID; }

    public void set_parentID(String s) { _parentID = s ; }


    // restore stringified objects
    public void initNonPersistedItems() {
        _wir = RdrConversionTools.xmlStringtoWIR(_wirStr);
        _datalist = _wir.getDataList();
    }


//===========================================================================//

    /** returns String representation of current CheckedOutChildItem */
    public String toString() {

        StringBuffer s = new StringBuffer("##### CHECKEDOUTCHILDITEM RECORD #####") ;
        s.append(Library.newline);
        s.append(toStringSub());

        String parent = (_parent == null)? "null" : _parent.toStringSub();
        Library.appendLine(s, "PARENT CHECKEDOUTITEM", parent);

        return s.toString() ;
    }


//===========================================================================//
//===========================================================================//

}
	
