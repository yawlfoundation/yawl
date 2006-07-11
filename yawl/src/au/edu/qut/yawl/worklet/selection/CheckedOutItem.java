/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */
package au.edu.qut.yawl.worklet.selection;

import au.edu.qut.yawl.worklist.model.*;
import au.edu.qut.yawl.worklet.support.*;

import java.util.*;

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
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.8, 04/07/2006
 */

public class CheckedOutItem {
	
	private WorkItemRecord _wir ;          // the id of the 'parent' workitem
	private ArrayList<CheckedOutChildItem> _myChildren ;  // list of checked out children
	private String _specId ;               // specification that task is in
    private CheckedOutItem _me ;           // reference to self

    private String _persistID ;            // unique id field for persistence
    private String _wirStr ;               // intermediate string needed for persistence

 //   private static Logger _log ;         // log file for debug messages
	
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
        _specId = _wir.getSpecificationID() ;
        _myChildren = new ArrayList<CheckedOutChildItem>() ;
        _me = this ;
        //  _log = Logger.getLogger("au.edu.qut.yawl.worklet.selection.CheckedOutItem");
    }

    // update the persisted object
    private void persistThis() {
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
	
	
	public void setChildren(List<CheckedOutChildItem> c) {
		_myChildren = (ArrayList<CheckedOutChildItem>) c ;
	}
	
//===========================================================================//
	
	// GETTERS //
	
	public WorkItemRecord getItem() {
		return _wir ;
	}
	

	public String getSpecId() {
		return _specId ;
	}
	
	
	public List getChildren() {
		return _myChildren ;
	}


    public String getParentID() {
       return _persistID ;
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
	   return _myChildren.get(i) ;
	}
	
	
	/** returns the CheckedOutChildItem object of the childitem with the id */
	public CheckedOutChildItem getCheckedOutChildItem(String itemId) {
        for (Object a_myChildren : _myChildren)
        {                              // for each child
            CheckedOutChildItem c = (CheckedOutChildItem) a_myChildren;
            if (itemId.equals(c.getItem().getID())) return c;
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
    		CheckedOutChildItem c = _myChildren.get(i) ;
    		
    		// if this object matches the one passed, remove it
	        if (childToRemove.getItem().getID().equals(c.getItem().getID())) {
	           _myChildren.remove(i) ;
	           return i ;
	        }   
        }
        return -1 ;		
	}
	
	/** returns true if this parent has children */
	public boolean hasCheckedOutChildItems() {   
		return (_myChildren.size() > 0) ;
	}
	
//===========================================================================//
	
   
    /** returns String representation of  current CheckedOutItem */  
    public String toString() {
        System.out.println("**** in coi.toString");
        StringBuffer s = new StringBuffer("##### CHECKEDOUTITEM RECORD #####") ;
    	String n = Library.newline ;

        // write out the parent wir
        s.append(n);
        Library.appendLine(s, "CHECKED OUT ITEM", toStringSub());

    	// build this toString by calling the toStringSub of each child
    	s.append("CHILD ITEM(S): ");
        s.append(n);
        for (CheckedOutChildItem child : _myChildren) {
            s.append(child.toStringSub());
            s.append(n);
        }

    	return s.toString() ;
    }


    public String toStringSub() {
        StringBuffer s = new StringBuffer();
        s.append(_wir.toXML());
        s.append(Library.newline);
    	return s.toString() ;
    }

//===========================================================================//
//===========================================================================//
   
}
	
