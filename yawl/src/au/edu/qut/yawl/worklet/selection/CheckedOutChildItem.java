/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.worklet.selection;

import au.edu.qut.yawl.worklist.model.*;
import au.edu.qut.yawl.worklet.support.*;
import au.edu.qut.yawl.worklet.rdr.*;

import org.jdom.*;
import org.jdom.output.* ;

import org.apache.log4j.Logger;
                             
import java.util.*;
import java.io.* ;


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
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.7, 10/12/2005
 */

public class CheckedOutChildItem {
	
	private WorkItemRecord _wir ;          // the child workitem
	private CheckedOutItem _parent ;       // the 'parent' workitem
	private Element _datalist ;            // data passed to this workitem
	private String _runningCaseId ;        // the caseid of the running worklet 
	private String _workletName ;          // name of worklet used
	private RdrNode[] _searchPair ;        // rule pair returned from search
    private static Logger _log ;           // log file for debug messages
	
	
	/** 
	 *  Constructs a CheckedOutChildItem
	 *  @param w - the WorkItemRecord the describes the child workitem
	 */	
	public CheckedOutChildItem(WorkItemRecord w) {
		_wir = w ;                              
		_datalist = w.getWorkItemData();
        _log = Logger.getLogger("au.edu.qut.yawl.worklet.selection.CheckedOutChildItem");           
    }
	
//===========================================================================//
	
	// SETTERS //
	
	public void setItem(WorkItemRecord w) {
		_wir = w ;
	}
	
	
	public void setParent(CheckedOutItem p) {
		_parent = p ;
	}
	
	
	public void setDatalist(Element d) {
		_datalist = d ;
	}
		
	
	public void setWorkletName(String wName) {
		_workletName = wName ;
	}
	
	
	public void setSearchPair(RdrNode[] pair) {
		_searchPair = pair ;
	}
	
	
	public void setRunningCaseId(String caseId) {
		_runningCaseId = caseId ;
	}
	
//===========================================================================//
	
	//*** GETTERS ***//
	
	public String getWorkletName() {
		return _workletName ;
	}
	
	
	public WorkItemRecord getItem() {
		return _wir ;
	}
	
	
	public String getItemId() {
		return _wir.getID() ;
	}
	
		
	public CheckedOutItem getParent() {
		return _parent ;
	}
	

	public RdrNode[] getSearchPair() {
		return _searchPair ;
	}
	
	
	public String getRunningCaseId() {
		return _runningCaseId ;
	}
	
	
	public Element getDatalist() {
		return _datalist ;
	}

//===========================================================================//
	
	//*** SAVE METHODS **//

    /**
     * writes the node id's for the nodes returned from the rdr search
     * and the data for the current workitem, to a file for later
     * input into the 'add rule' process, if required
     */ 
    public void saveSearchResults() {
    	
    	// build file name from workitem and worklet identifiers
    	String fName = _wir.getID() + " - " + _workletName + ".xws" ;
    	fName = fName.replace(':', '-') ;
    	String fPath = Library.wsSelectedDir ; 
    	
    	// create the required components for the output file
    	Document doc = new Document(new Element("searchResult")) ;
    	Element eLastNode = new Element("lastNode") ;
    	Element eSatisfied = new Element("satisfied") ;
    	Element eTested = new Element("tested") ;
    	Element eId = new Element("id") ;
    	Element eSpecid = new Element("specid") ;
    	Element eTaskid = new Element("taskid") ;
    	Element eCaseid = new Element("caseid") ;
    	Element eWorklet = new Element("worklet") ;
    	Element eRunningCaseId = new Element("runningcaseid") ;
    	Element eCaseData = new Element("casedata") ;

    	try { 
    	    // transfer the workitem's data items to the file  	
		    List dataItems = _datalist.getChildren() ;  
		    Iterator itr = dataItems.iterator();
		    while (itr.hasNext()) {
	    		Element e = (Element) itr.next() ;
		        eCaseData.addContent((Element) e.clone());
	        }
	        
	        //set values for the workitem identifiers
	        eId.setText(_wir.getID()) ;
	        eSpecid.setText(_wir.getSpecificationID());
	        eTaskid.setText(Library.getTaskNameFromId(_wir.getTaskID()));
	        eCaseid.setText(_wir.getCaseID());	        
	        eWorklet.setText(_workletName) ;
	        eRunningCaseId.setText(_runningCaseId) ;

            // add the nodeids to the relevent elements
	    	eSatisfied.setText(_searchPair[0].getNodeIdAsString()) ;
	    	eTested.setText(_searchPair[1].getNodeIdAsString()) ;
	    	eLastNode.addContent(eSatisfied) ;
	    	eLastNode.addContent(eTested) ;
           	  
           	// add the elements to the document    	
	        doc.getRootElement().addContent(eId) ;
	        doc.getRootElement().addContent(eSpecid);
	        doc.getRootElement().addContent(eTaskid);
	        doc.getRootElement().addContent(eCaseid);	        
	        doc.getRootElement().addContent(eWorklet) ;
	        doc.getRootElement().addContent(eRunningCaseId) ;	        
	    	doc.getRootElement().addContent(eLastNode) ;
	    	doc.getRootElement().addContent(eCaseData) ;
     	
     	    // create the output file
     		saveDocument(fPath + fName, doc) ;	    
     	}	
	    catch (IllegalAddException iae) {
	    	_log.error("Exception when adding content", iae) ;
	    }
     }
 
 
     /** saves a JDOM Document to a file */    	
     private void saveDocument(String fileName, Document doc)   {
        try {
           FileOutputStream fos = new FileOutputStream(fileName);
           XMLOutputter xop = new XMLOutputter(Format.getPrettyFormat());
           xop.output(doc, fos);
           fos.flush();
           fos.close();
      }
      catch (IOException ioe){
      	_log.error("IO Exeception in saving Document to file", ioe) ;
      }
   }
   
//===========================================================================//
	
	/** returns String representation of current CheckedOutChildItem */
    public String toString() {
    	StringBuffer s = new StringBuffer("CheckedOutChildItem record:") ;
    	String n = Library.newline ; 	
    	
    	s.append(n); 
    	s.append("WORKITEM: ");
    	s.append(_wir.toXML());
    	s.append(n); 
    	 
    	s.append("DATALIST: ");
    	s.append((_datalist != null ? _datalist.toString() : "null"));
    	s.append(n);
    	
    	s.append("WORKLET: ");
    	s.append((_workletName != null ? _workletName : "null"));
    	s.append(n);
    	
    	s.append("RUNNING CASE ID: ");
   	    s.append((_runningCaseId != null ? _runningCaseId : "null")) ;  
    	s.append(n);
    	
    	return s.toString() ;
    }
   
//===========================================================================//
//===========================================================================//
  
}
	
