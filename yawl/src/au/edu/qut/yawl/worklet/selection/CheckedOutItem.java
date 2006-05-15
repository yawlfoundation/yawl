/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.worklet.selection;

import au.edu.qut.yawl.worklist.model.*;
import au.edu.qut.yawl.worklet.support.*;
import au.edu.qut.yawl.worklet.rdr.*;

import org.jdom.*;
import org.jdom.output.* ;
                             
import java.util.*;
import java.io.* ;


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
 *  v0.7, 10/12/2005
 */

public class CheckedOutItem {
	
	private WorkItemRecord _wir ;          // the id of the 'parent' workitem
	private ArrayList _myChildren ;        // list of checked out children
//	private ArrayList _runningCases ;      // list of running worklet cases 
	private String _specId ;               // specification that task is in 
    private Logger _log ;                  // log file for debug messages 
    private CheckedOutItem _me ;           // reference to self
	
	
	/** Constructs a CheckedOutItem 
	 *  @param w - the WorkItemRecord of the 'parent' workitem
	 */	
	public CheckedOutItem(WorkItemRecord w) {
		_wir = w ;                              
		_myChildren = new ArrayList() ;
	    _specId = w.getSpecificationID() ;
	    _log = new Logger("checkedOutItem.log");
	    _me = this ;
	}
	
//===========================================================================//
	
	// SETTERS //
	
	public void setItem(WorkItemRecord w) {
		_wir = w ;
	}
	
	
	public void setChildren(List c) {
		_myChildren = (ArrayList) c ;
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
		CheckedOutChildItem child = new CheckedOutChildItem(w);
		child.setParent(_me) ;
		_myChildren.add(child) ;                    // keep track of children
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
	
	/** returns true if this parent has children */
	public boolean hasCheckedOutChildItems() {   
		return (_myChildren.size() > 0) ;
	}
	
//===========================================================================//
	
   
    /** returns String representation of  current CheckedOutItem */  
    public String toString() {
    	StringBuffer s = new StringBuffer("CheckedOutItem record:") ;
    	String n = Library.newline ; 	
    	int i ;
    	
    	s.append(n); 
    	s.append("PARENT: ");
    	s.append(_wir.toXML());
    	s.append(n); 

    	// build this toString by calling the toString of each child
    	s.append("CHILDREN: ");
    	Iterator itr = _myChildren.iterator();
	    while (itr.hasNext()) {
    		CheckedOutChildItem c = (CheckedOutChildItem) itr.next() ;
    		s.append(c.toString());
        }
    	
    	return s.toString() ;
    }
    
//===========================================================================//
//===========================================================================//
   
}
	
