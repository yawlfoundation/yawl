/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.worklet.selection;

import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.interfce.InterfaceA_EnvironmentBasedClient;
import au.edu.qut.yawl.engine.interfce.InterfaceBWebsideController;
import au.edu.qut.yawl.worklist.model.*;
import au.edu.qut.yawl.elements.data.* ;

import au.edu.qut.yawl.worklet.support.*;
import au.edu.qut.yawl.worklet.rdr.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;                                
import java.util.*;
import java.text.*;
import java.net.*;


/** 
 *  This class and its support classes represent an implementation for YAWL 
 *  of the Worklet paradigm.
 *
 *  The WorkletSelector class is the main class for the selection process. It 
 *  receives an enabled workitem from the engine and attempts to 
 *  substitute it with a worklet.
 *
 *  Here's the class hierarchy for the service:
 *
 *
 *    Interfaces A & B                      RDREditor
 *          ^                                   ^
 *          |                                   |
 * ---------+-----------------------------------+---------------------------- *
 *          |                                   V                             *
 *          |                             +===========+                       *
 *          |    +----------------------> | wsGateway |                       *
 *          |    |                        +===========+                       *
 *          |    |                                                            *
 *          V    V                                                            *
 *  +=================+       +========+       +=========+       +=========+  *
 *  | WorkletSelector | 1---M | RdrSet | 1---M | RdrTree | 1---M | RdrNode |  *
 *  +=================+       +========+       +=========+       +=========+  *
 *         1                                                         1        *  
 *         |                                                         |        *
 *         |                      +========+                         |        *   
 *         M                      | Logger |                         1        *
 *  +================+            +========+          +====================+  *
 *  | CheckedOutItem |                                | ConditionEvaluator |  *
 *  +================+                                +====================+  *
 *         1                      +=========+                        ^        *            
 *         |                      | Library |                        |        *  
 *         |                      +=========+                        |        *
 *         M                                                         V        *   
 *  +=====================+                        +=======================+  *               
 *  | CheckedOutChildItem |                        | RDRConditionException |  *
 *  +=====================+                        +=======================+  * 
 *                                                                            *
 * -------------------------------------------------------------------------- *                                                                           *
 *
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.7, 10/12/2005
 */

public class WorkletSelector extends InterfaceBWebsideController {
	
	// required data for interfacing with the engine
    private String _userName = "admin" ;
    private String _password = "YAWL" ;
    private String _sessionHandle = null ;
    private String _engineURI =  "http://localhost:8080/yawl/ia" ;
    private String _workletURI = "http://localhost:8080/workletSelector/ib" ;
    private InterfaceA_EnvironmentBasedClient _interfaceAClient ;

    // running datasets to keep track of what's executing
    private Map _handledParentItems = new HashMap() ;
    private Map _handledWorkItems = new HashMap() ;
    private Map _casesStarted = new HashMap() ;
    private ArrayList _loadedSpecs = new ArrayList() ;
    
    private RdrSet _ruleSet ;             // set of currently loaded rdr trees                    
    private Logger _log ;                 // event and debug log file 
    private String _workletsDir ;         // where the worklet specs are
    
    private static WorkletSelector _me ;  // reference to self  
    
    
    /** the constructor */
    public WorkletSelector(){
        super();
        _interfaceAClient = new InterfaceA_EnvironmentBasedClient(_engineURI);
        _ruleSet = new RdrSet() ;
        _log = new Logger("workletSelector.log");
        _workletsDir = Library.wsWorkletsDir;
        _me = this ;
    }
    
    /** @return a reference to the current WorkletSelector instance */
    public static WorkletSelector getInstance() {
    	return _me ;
    }
    
//***************************************************************************//

/************************************ 
 * 1. OVERRIDDEN BASE CLASS METHODS *
 ***********************************/ 
       
    /** 
     *  Handles a message from the engine that a workitem has been enabled 
     *  (see InterfaceBWebsideController for more details)
     *  In this case, it either starts a worklet subatitution process, or, if
     *  the workitem denotes the end of a worklet case, it completes the
     *  substitution process by checking the original workitem back into
     *  the engine.
     *
     *  @param workItemRecord - a record describing the enabled workitem
     */
   
    public void handleEnabledWorkItemEvent(WorkItemRecord workItemRecord) {
	    	
    	_log.writeAsHeader("HANDLE ENABLED WORKITEM EVENT") ;  // note to log
    	
    	if (connected()) {
    	 	_log.write("Connection to engine is active") ;
   	 	  	handleWorkletSelection(workItemRecord) ;
		}	 	
    	else _log.write("Could not connect to YAWL engine") ; 	
    }
    
//***************************************************************************//

    /** 
     *  Handles a message from the engine that a workitem has been cancelled 
     *  (see InterfaceBWebsideController for more details)
     *  In this case, it cancels any worklet(s) running in place of the 
     *  workitem.
     *  Only deals with child workitems currently checked out - not interested
     *  in workitems that haven't been handled by the service, or parent
     *  workitems, since handling all the children takes care of the parent
     *     
     *  @param workItemRecord - a record describing the cancelled workitem
     */
     
    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {
    	
    	CheckedOutChildItem coItem ;
    	CheckedOutItem coParent ;
    	String itemId = workItemRecord.getID() ;
    	String runningCaseId ;
    	
        // only interested in child workitems - ignore all others
    	if (_handledWorkItems.containsKey(itemId)) {
	    	_log.writeAsHeader("HANDLE CANCELLED WORKITEM EVENT") ;  
	    	
	    	if (connected()) {
	    	 	_log.write("Connection to engine is active") ;
  	    	    _log.write("ID of cancelled workitem: " + itemId ) ;
	    	
				coItem = (CheckedOutChildItem) _handledWorkItems.get(itemId) ;
				runningCaseId = coItem.getRunningCaseId() ;
				_log.write("Worklet case running for the cancelled workitem " +
				           "has id of: " + runningCaseId) ;
				
				if (cancelWorkletCase(runningCaseId, coItem)) {				
   		           _handledWorkItems.remove(itemId) ;
     		       _log.write("Removed from handled child workitems: " + itemId);
   		           
   		           // remove child and if last child also remove parent
   		           coParent = coItem.getParent() ;
   		           coParent.removeChild(coItem) ;

   		           if (! coParent.hasCheckedOutChildItems()) {
		    		   String parentId = coParent.getItem().getID() ;
		    		   _log.write("No more child cases running for workitem: " +
		    		                  parentId);
		    		   _handledParentItems.remove(parentId) ;
		    		   _log.write("Completed handling of workitem: " + parentId);
		    	   }
   		        }
   		        else _log.write("Could not cancel case: " + runningCaseId) ; 
	    	}
	    	else _log.write("Could not connect to engine") ;
	    }	
    }


//***************************************************************************//
    
    /** 
     *  Handles a message from the engine that a (worklet) case has 
     *  completed (see InterfaceBWebsideController for more details).
     *
     *  Only those services that register as an 'observer' for the case will
     *  receive these events. All worklets launched (through launchCase())
     *  register as an observer.
     *       
     *  @param caseID - the id of the completed case
     *  @param casedata - an (XML) string containing the output data for
     *         the case        
     */

    public void handleCompleteCaseEvent(String caseID, String casedata) {
    	_log.writeAsHeader("HANDLE COMPLETE CASE EVENT") ;     // note to log
    	_log.write("ID of completed case: " + caseID) ;
    	
    	// reconstruct casedata to JDOM Element
    	Element cdata = StringToElement(casedata); 
    	
    	if (connected()) {
    	 	_log.write("Connection to engine is active") ;
    	 	handleCompletingWorklet(caseID, cdata) ;
    	}
    	else _log.write("Could not connect to YAWL engine") ; 	
    }

    
//***************************************************************************//
    
   /** override of IB's checkout method to allow for retrieval of non-cached 
    *  work items if required
    *  Change: negates the 'else if' condition 
    *          checks which child is checkedout with parent rather than relying
    *          on it being the first child.
    *  TO DO: remove after next beta iteration of engine
    */
   public String checkOut(String workItemID, String sessionHandle) 
                                                     throws IOException {
      
      String msg = _interfaceBClient.checkOutWorkItem(workItemID, sessionHandle);
      
      if (successful(msg)) {
         try {
         	
         	 // get engine's workitem, not the remotely cached one
             WorkItemRecord item = getEngineStoredWorkItem(workItemID, 
                                                            sessionHandle);
 
             // if it's the parent, find which child got checked out with it
             if (item.getStatus().equals(YWorkItem.statusIsParent)) {
                 
                 // get the kids - which one's moved on from 'fired'?
                 List child = getChildren(workItemID, sessionHandle);
                 Iterator itr = child.iterator();
   		         while(itr.hasNext()) {
                    WorkItemRecord childRec = (WorkItemRecord) itr.next();
                    if (! childRec.getStatus().equals(YWorkItem.statusFired)) {
                       _model.addWorkItem(childRec);        // executing child
               	    }   
                 }               
             } 
             
             // else not a parent, add it if its 'executing'
             else if (! item.getStatus().equals(YWorkItem.statusFired)) {
                 _model.addWorkItem(item);
             }
             
             // there's a problem
             else {
                 _log.write("NOT adding item to remote cache!") ;
                 _log.write("    item's id is: " + item.getID()) ;
                 _log.write("    item's status is: " + item.getStatus());
            }
         } catch (JDOMException jde) {
          	 _log.write("JDOMException in checkout() method");
             _log.write(jde);
         } catch (IOException ioe) {
          	 _log.write("IOException in checkout() method");
             _log.write(ioe);
         }
     }
     return msg;
 }   
   
 //***************************************************************************//
   
     /**
      *  displays a web page describing the service
      */   
     public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException{
        response.setContentType("text/html");
        PrintWriter outputWriter = response.getWriter();
        StringBuffer output = new StringBuffer();
        String fileName = Library.wsHomeDir + "welcome.htm";
        String welcomePage = Library.FileToString(fileName) ;
        
        // load the full welcome page if possible
        if (welcomePage != null) output.append(welcomePage) ;
        else {
        	
        	// otherwise load a boring default
		    output.append(
		        "<html><head>" +
		        "<title>Worklet Dynamic Process Selection Service</title>" +
		        "</head><body>" +
		        "<H3>Welcome to the Worklet Dynamic Process Selection Service</H3>" +
		        "</body></html>");
		}        
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();
    }

 //***************************************************************************//
   

    /** Override of InterfaceB_EnvironmentBasedClient.launchCase() to provide
     *  the ability to send the worklet service as a case completed observer
     */
    public String launchCase(String specID, String caseParams, 
                             String sessionHandle) 
                                     throws IOException, URISyntaxException {
        Map paramsMap = new HashMap();
        paramsMap.put("sessionHandle", sessionHandle);
        paramsMap.put("action", "launchCase");
        paramsMap.put("caseParams", caseParams);
        paramsMap.put("completionObserverURI", _workletURI);

        return _interfaceBClient.executePost(
        	    "http://localhost:8080/yawl/ib/specID/" + specID, paramsMap);
    }


//***************************************************************************//
    

    
/****************************************
 * 2. TOP LEVEL WORKITEM EVENT HANDLERS *
 ***************************************/    
    
    /** Attempt to substitute the enabled workitem with a worklet
     *  @param wir - the enabled workitem record
     */
    private void handleWorkletSelection(WorkItemRecord wir) {
    	
     	String specId = wir.getSpecificationID() ;       // info about item
    	String taskId = wir.getTaskID() ;
    	String itemId = wir.getID() ;
    	RdrTree rdrTreeForWorkItem ;                     // rules for task      
    	CheckedOutItem coItem ;                          // record of item
    	CheckedOutChildItem coChild ;                    // child item rec. 

    	_log.write("Received workitem for worklet substitution: " + itemId) ;
    	_log.write("   specId = " + specId);
			    	 		 	 	
	 	// locate rdr ruleset for this task
	 	_ruleSet.refresh() ;
	 	rdrTreeForWorkItem = _ruleSet.getRdrTree(specId, taskId);
	 	
	 	if (rdrTreeForWorkItem != null) {

    	 	// OK - this workitem has an associated ruleset so check it out
    	 	// all the child items also get checked out here
    	 	_log.write("Ruleset found for workitem: " + itemId) ;
    	 	coItem = checkOutItem(wir) ; 
    	 	   	 	
    		// remember this handling of the parent item (if not a replace)
    	 	if (! _handledParentItems.containsKey(itemId))    
			    _handledParentItems.put(itemId, coItem) ;
    	 	
    	 	// launch a worklet case for each checked out child workitem
    	 	for (int i = 0; i < coItem.getChildCount(); i++) {
    	 		coChild = coItem.getCheckedOutChildItem(i) ;
    	 		ProcessWorkItemSubstitution(rdrTreeForWorkItem, coChild) ; 
	        } 	
		}
		else _log.write("No rule set found for specId: " + specId);
	}	

//***************************************************************************//

   
   /** Deals with the end of a worklet case. Checks in the workitem the 
    *  completing worklet was subbed for and, if the original parent workitem
    *  has no more children after this workitem is checked in, removes its
    *  record from the dynamic datsets of currently handling workitems
    *
    *  @param caseId - the id of the completing case
    *  @param wlCaseData - the completing case's datalist Element
    */
    private void handleCompletingWorklet(String caseId, Element wlCasedata) {
    	
    	// get the id of the workitem this worklet was selected for 
    	String origWorkItemId = (String) _casesStarted.get(caseId);
    	_log.write("Workitem this worklet case ran in place of is: " + 
    	            origWorkItemId);
    	
    	// get the checkedoutchilditem record for the original workitem
    	CheckedOutChildItem cociOrig = (CheckedOutChildItem) 
    	                         _handledWorkItems.get(origWorkItemId) ;
    	
        // get the actual workitem this worklet case substituted
       	WorkItemRecord childItem = cociOrig.getItem() ;
    	
    	if (childItem != null) {
    	    	
	    	// get the workitem's input data list
	    	Element in = cociOrig.getDatalist() ;    
	
	    	// update workitem's datalist with the worklet's output values
	    	Element out = updateWorkItemDataList(in, wlCasedata) ; 
		       	    
		    // log the worklet's case completion event
		    Logger.logEvent(Logger.eComplete, caseId, cociOrig.getWorkletName(),
		       	                     "", cociOrig.getItem().getCaseID()) ;
	
	    	// checkin original workitem & clean up dynamic lists
	    	if (checkinItem(childItem, in, out)) {  
	    	
		    	// get the parent of the original checked out workitem
		    	CheckedOutItem coiParent = cociOrig.getParent();
		    	
	    	    // remove child workitem reference from parent object
		    	coiParent.removeChild(cociOrig) ;   
		    	    	            
		    	// and remove it from the dynamic execution datasets    	            
		    	_casesStarted.remove(caseId) ;  
		       	_log.write("Removed from cases started: " + caseId) ;
		       	_handledWorkItems.remove(origWorkItemId) ;
		       	_log.write("Removed from handled child workitems: " +
		       	            origWorkItemId) ;
		    	 
		    	// if there is no more child cases, we're done with this parent
		    	if (! coiParent.hasCheckedOutChildItems()) {
		    		String parentId = coiParent.getItem().getID() ;
		    		_log.write("No more child cases running for workitem: " + 
		    		            parentId);
		    		_handledParentItems.remove(parentId) ;
		    		_log.write("Completed handling of workitem: " + parentId);
		    	}
		    }
		    else _log.write("Could not check in child workitem: " + 
		                                              childItem.getID());
	    }
	   	else _log.write("Could not retrieve the substituted workitem " +
	    	                    "for this worklet case");
    }
    
 //***************************************************************************//
   
     
    /**
     *  Gets a worklet running for a checked-out workitem
     *  @param tree - the RdrTree of rules for the task that the checked-out
     *                workitem is an instance of
     *  @param coChild - the info record of the checked out workitem 
     */
    private void ProcessWorkItemSubstitution(RdrTree tree, 
                                             CheckedOutChildItem coChild) {
                                             	
	   String childId = coChild.getItem().getID() ; 
       
	   _log.write("Processing worklet substitution for workitem: " + childId);
                                   	
	 	// select appropriate worklet
	 	String wSelected = tree.search(coChild.getDatalist());
	 	
	 	if (wSelected != null) {
	 	    _log.write("Rule search returned worklet: " + wSelected); 
	 	    coChild.setWorkletName(wSelected) ;   	 		
	 	    
		 	// load spec & launch case as substitute for checked out workitem 
			if (uploadWorklet(wSelected)) {
	            launchWorklet(coChild) ;
	            
	            // remember this handling (if not a replace)
	            if (! _handledWorkItems.containsKey(childId))    
				    _handledWorkItems.put(childId, coChild) ;
	
	 	        // save the search results (if later rule append is needed)
	 	        coChild.setSearchPair(tree.getLastPair());
	 	        coChild.saveSearchResults() ;
		    }
		    else _log.write("could not launch worklet: " + wSelected) ;      	 	    
		}
	    else _log.write("Rule search did not find a worklet to select " +
	    	                "for workItem: " + coChild.getItem().toXML()) ;
	}

//***************************************************************************//

/*******************************
 * 3. CHECKOUT/CHECKIN METHODS *
 ******************************/
   
    /**
     *  Manages the checking out of a workitem and its children
     *  @param wir - the WorkItemRecord of the workitem to check out
     *  @return a CheckedOutItem record of the checked out parent workitem,
     *          which also contains references to its child items. 
     */    
    private CheckedOutItem checkOutItem(WorkItemRecord wir) {
    	String itemId = wir.getID() ;

    	// if this item is having it's worklet replaced due to an 'add rule',
    	// then it's already checked out so don't do it again
   		if (_handledParentItems.containsKey(itemId)) {
   			return (CheckedOutItem) _handledParentItems.get(itemId) ;
   		}
   		else {
    	 	CheckedOutItem coItem = checkOutParentItem(wir) ;
    	 	checkOutChildren(coItem) ;	          // always at least one child
    	 	return coItem ;
   		}	
    }

//***************************************************************************//

    /** checks out the parent enabled workitem */
    private CheckedOutItem checkOutParentItem(WorkItemRecord wir) {
     
     	_log.write("Checking parent workitem out of engine: " + wir.getID());

 	    if (checkOutWorkItem(wir)) 
 	        return new CheckedOutItem(wir) ;
        else return null;
    }
    
 //***************************************************************************//
 
    /**
     *  Checks out all the child workitems of the parent item specified
     *  @param coi - the parent's data object
     */ 
	private void checkOutChildren(CheckedOutItem coi) {
		
		_log.write("Checking out child workitems...") ;
		
		// get all the child instances of this workitem
		List children = getChildren(coi.getItem().getID(), _sessionHandle);
		
		// checkout each child instance
	    for (int i = 0; i < children.size(); i++) {
	       WorkItemRecord itemRec = (WorkItemRecord) children.get(i); 
	       
	       // if its 'fired' check it out	       
	       if (WorkItemRecord.statusFired.equals(itemRec.getStatus())) {
	       	   if (checkOutWorkItem(itemRec))
        	      Logger.logEvent(Logger.eCheckOut, itemRec) ;  // log checkout 
           }          
	       	   
	       // if its 'executing', it means it got checked out with the parent	   
	       else if (WorkItemRecord.statusExecuting.equals(itemRec.getStatus())) {
	          _log.write("   child already checked out with parent: " + 
	                         itemRec.getID()) ;
        	  Logger.logEvent(Logger.eCheckOut, itemRec) ;      // log checkout 
           }    
		}

		// update child item list after checkout (to capture status changes)
		children = getChildren(coi.getItem().getID(), _sessionHandle);
		
		// if checkout ok and status is 'executing' add each child to parent
		for (int j=0; j < children.size(); j++) {
			WorkItemRecord w = (WorkItemRecord) children.get(j) ;
		
			if (WorkItemRecord.statusExecuting.equals(w.getStatus())) {
			   coi.addChild(w) ;
			}
			else 
			   _log.write("child " + j + " has NOT been added to CheckedOutItems") ;         
		}
	}
	
//***************************************************************************//
	
	/**
	 *  Check the workitem out of the engine
	 *  @param wir - the workitem to check out
	 *  @return true if checkout was successful
	 */
	private boolean checkOutWorkItem(WorkItemRecord wir) {

	    try {
	 	    if (successful(checkOut(wir.getID(), _sessionHandle))) {
 	 	        _log.write("   checkout successful: " + wir.getID());
 	 	        return true ;
 	        }
 	        else {
 	        	_log.write("   checkout unsuccessful: " + wir.getID());
 	        	return false;
 	        } 		    
	 	}
	 	catch (IOException ioe) {
	 		_log.write("IO Exception with checkout: " + wir.getID());
            _log.write(ioe);
	 		return false ;
	 	}  	
	}
  
//***************************************************************************//
   
    /**
     *  Checks a (checked out) workitem back into the engine
     *
     *  @param wir - workitem to check into the engine
     *  @param in - a JDOM Element containing the input params of the workitem
     *  @param out - a JDOM Element containing the output params of the workitem
     *  @return true id checkin is successful
     */ 
    private boolean checkinItem(WorkItemRecord wir, Element in, Element out) {

    	try {
    		String result = checkInWorkItem(wir.getID(), in, out, 
    		                                              _sessionHandle) ;
    		if (successful(result)) {


	 	    	// log the successful checkin event
    		   Logger.logEvent(Logger.eCheckIn, wir) ;
    		   _log.write("Successful checkin of work item: " + wir.getID()) ;
    		   return true ;
    		}   
    		else {
    		   _log.write("Checkin unsuccessful for: " + wir.getID()) ; 
    		   _log.write("Diagnostic string: " + result) ;
    		}     
    	} 
    	catch (IOException ioe) {
    		_log.write("checkinItem method caused java IO Exception") ;
    		_log.write(ioe);
    	}
    	catch (JDOMException jde) {
    		_log.write("checkinItem method caused JDOM Exception") ;
    		_log.write(jde);
     	}
     	return false ;                                 // check-in unsucessful
    }   
     
  //***************************************************************************//
    
/************************************************
 * 4. UPLOADING, LAUNCHING & CANCELLING METHODS *
 ***********************************************/    
    
    /**
     *  Uploads a worklet specification into the engine
     *  @param workletName - the name of the worklet specification to upoad
     *  @returns true if upload is successful or spec is already loaded
     */ 
    private boolean uploadWorklet(String workletName) {
    	
    	String fileName = workletName + ".xml" ;
    	String fullFileName = _workletsDir + fileName ;

    	if (isUploaded(workletName)) {
            _log.write("Worklet specification '" + workletName 
                        + "' is already loaded in Engine") ;
    		return true ; 
    	}                        
    	    	
    	String wSpec = Library.FileToString(fullFileName);  // needs spec as String

		if (wSpec != null) {
    	   if (successful(_interfaceAClient.uploadSpecification(wSpec,
    	 	               fileName, _sessionHandle))) {
    	 	  _log.write("Successfully uploaded worklet specification: " 
    	 	               + workletName) ;
    	 	  return true ; 
    	   }	    	
    	   else {
    	      _log.write("Unsuccessful worklet specification upload : " 
    	                   + workletName)	;
    	      return false ;   
    	   }
    	}
  	    else {
  	    	_log.write("Rule search found: " + workletName + 
		               ", but there is no worklet of that name in " +
		               "the repository, or there was a problem " +
		               "opening/reading the worklet specification") ; 	
		    return false ;
		}               
    } 
    
//***************************************************************************//
    
    /**
     *  Starts a worklet case executing in the engine
     *  @param coci - the item record of the child workitem to start the 
     *                worklet for
     *  @return - the case id of the started worklet case
     */
    private String launchWorklet(CheckedOutChildItem coci) {

    	String caseId ;
    	String workletName = coci.getWorkletName() ;
    	
    	// fill the case params with matching data values from the workitem
    	String caseData = mapItemParamsToWorkletCaseParams(coci);
    	    	
    	try {
      	    caseId = launchCase(workletName, caseData, _sessionHandle); 
			if (successful(caseId)) {
				
				// record case id 
			    coci.setRunningCaseId(caseId) ;
			    _casesStarted.put(caseId, coci.getItem().getID()) ;
			    
			    // and log launch event
			    Logger.logEvent(Logger.eLaunch, caseId, workletName, "",
			                                    coci.getItem().getCaseID());
			    _log.write("Launched case for worklet " + workletName +
			               " with ID: " + caseId) ;
			    return caseId ;
			}
			else {
				_log.write("Unable to launch worklet: " + workletName) ;
				_log.write("Diagnostic message: " + caseId) ;
				return null ;
			}    
		}
		catch (URISyntaxException use) {
			_log.write("URI Exception when assigning worklet URI") ;
			_log.write(use) ;
			return null ;
		}
		catch (IOException ioe) {
			_log.write("IO Exception when attempting to launch case") ;
			_log.write(ioe) ;
			return null ;
		}
    }
    
//***************************************************************************//
   
    /**
     *  Replaces a running worklet case with another worklet case after an
     *  amendment to the ruleset for this task.
     *  Called by wsGateway after a call from the RdrEditor that the ruleset
     *  has been updated.
     *
     *  @param itemid - the id of the orginal checked out workitem
     *  @return a string of messages decribing the success or otherwise of
     *          the process
     */        
   	public String replaceWorklet(String itemid) { 

     	String caseid ;
   		String result = "Locating workitem '" + itemid + 
   		                "' in the set of currently handled workitems..." ;
   		
   		_log.writeAsHeader("REPLACE WORKLET REQUEST");         
   		
   		// if workitem is currently checked out
   		if (_handledWorkItems.containsKey(itemid)) {
   			result += "found." + Library.newline ;
   			_log.write("Itemid received found in handleditems: " + itemid);
   		   
    	   // get the checkedout child item record for this workitem
    	   CheckedOutChildItem coci = 
    	                 (CheckedOutChildItem) _handledWorkItems.get(itemid) ; 
    	   
    	   // get the case id of the running worklet subbed for this workitem
    	   caseid = coci.getRunningCaseId() ;       
    	      	      
    	   // cancel the worklet running for the workitem
    	   result += "Cancelling running worklet case with case id " +
    	             caseid + " for workitem..." ;
    	   _log.write("Running worklet case id for this workitem is: " + caseid);

    	   if (cancelWorkletCase(caseid, coci)) {
	    	   _log.write("Removing case from cases started: " + caseid);
		       _casesStarted.remove(caseid) ;    	   	  
	
	           result += "done." + Library.newline ;
           
	           // go through the selection process again
	           result += "Launching new replacement worklet case based on revised ruleset...";
	           _log.write("Launching new replacement worklet case based on revised ruleset");
	           
	           // locate rdr ruleset for this task
	     	   String specId = coci.getItem().getSpecificationID() ;   
	    	   String taskId = coci.getItem().getTaskID() ;
	           
	 	 	   _ruleSet.refresh() ;
		 	   RdrTree tree = _ruleSet.getRdrTree(specId, taskId);
		 	
		       if (tree != null) {
	    	 	   _log.write("Ruleset found for workitem: " + coci.getItemId()) ;
	    	 	   ProcessWorkItemSubstitution(tree, coci) ;
	               result += "done. " + Library.newline +
	                   "The worklet '" + coci.getWorkletName() +
	                   "' has been launched for workitem '" + itemid + 
	                   "' and has case id: " + coci.getRunningCaseId() + 
	                   Library.newline ; 
	           }
	           else {
	           	 _log.write("Failed to locate ruleset for workitem." ) ;
	           	 result += "failed." + Library.newline + 
   		                   "Replacement process cannot continue." ;
	           }          
    	   }
    	   else {
    	   	   _log.write("Failed to cancel running case: " + caseid) ;
    	   	   result += "failed." + Library.newline + 
   		                 "Replacement process cannot continue."  ; 
    	   }	                                            
   		}   
   		else {
   		   _log.write("Itemid not found in handleditems: " + itemid) ;  
   		   result += "not found." + Library.newline + 
   		             "There are no checked out workitems with that id." ;
 
   		}    

   	    return result ;    
   	}	

//***************************************************************************//

	/**
	 *  Cancels an executing worklet process
	 *  @param caseid - the id of the case to cancel
	 *  @param coci - child workitem this case is running for
	 *  @return true if case is successfully cancelled
	 */
	private boolean cancelWorkletCase(String caseid,
	                                  CheckedOutChildItem coci) {
	   _log.write("Cancelling worklet case: " + caseid) ;
	   try {
	   	   _interfaceBClient.cancelCase(caseid, _sessionHandle) ;
	   	   
	   	   // log successful cancellation event
	   	   Logger.logEvent(Logger.eCancel, caseid, coci.getWorkletName(),
	   	                       "", coci.getItem().getCaseID()) ;
	   	   _log.write("Worklet case successfully cancelled: " + caseid) ;                    
	   	   return true ;                    
	   }
	   catch (IOException ioe) {
           _log.write("IO Exception when attempting to cancel case") ;
           _log.write(ioe) ;	   	  	 
	   }   
	   return false ;
	}	    

//***************************************************************************//

/************************************
 * 5. DATALIST MANIPULATION METHODS *
 ***********************************/
   
   /** updates the input datalist with the changed data in the output datalist
    *  @param in - the JDOM Element containing the input params 
    *  @param out - the JDOM Element containing the output params 
    *  @return a JDOM Element with the data updated
    */
   private Element updateWorkItemDataList(Element in, Element out) {

        // get a copy of the 'in' list   	
	   	Element result = (Element) in.clone() ;
    
	   	// for each child in 'out' list, get its value and copy to 'in' list
	   	Iterator itr = (out.getChildren()).iterator();
   		while(itr.hasNext()) {
           Element e = (Element) itr.next();
           
           // if there's a matching 'in' data item, update its value
           Element resData = result.getChild(e.getName()) ;  
           if (resData != null)	                  
	          resData.setText(e.getText()) ;
	    }
	    return result ;
  }   
    
 //***************************************************************************//

   /**
    *  Maps the values of the data attributes in the datalist of a 
    *  checked out workitem to the input params of the worklet case that will
    *  run as a substitute for the checked out workitem. 
    *  The input params for the worklet case are required by the interface's
    *  launchcase() method.
    *  @param coci - the checked out workitem's info object
    *  @return the loaded input params of the new worklet case 
    *          (launchCase() requires the input params as a String)
    */
   private String mapItemParamsToWorkletCaseParams(CheckedOutChildItem coci) {
      	
      	String wlName = coci.getWorkletName() ;    // worklet to replace item
      	Element itemData = coci.getDatalist() ;    // get datalist of work item
      	Element wlData = new Element(wlName);      // new datalist for worklet
       	
       	ArrayList inParams = getInputParams(wlName) ;  // worklet input params
       	
       	// extract the name of each worklet input param
       	for (int i=0;i<inParams.size();i++) {
	       YParameter param = (YParameter) inParams.get(i) ;
	       String paramName = param.getName() ;
	       
	       // get the data element of the workitem with the same name as
	       // the one for the worklet (assigns null if no match)   
	       Element wlElem = itemData.getChild(paramName) ; 

	       try {
	       	   // if matching element, copy it and add to worklet datalist
		       if (wlElem != null) {
		       	   Element copy = (Element) wlElem.clone() ;	
		           wlData.addContent(copy) ; 
		       }    
		       
		       // no matching data for input param, so add empty element
		       else                                
	       	       wlData.addContent(new Element(paramName)) ;  
       	   }
       	   catch (IllegalAddException iae) {
       	     	_log.write("Exception adding content to worklet data list") ;
       	   	    _log.write(iae) ;
       	   }    
       	}

       	// return the datalist as as string (as required by launchcase) 	    	
    	return new XMLOutputter().outputString(wlData) ;
   }  
   
 //***************************************************************************//

   /**
    *  Attempts to convert an xml string to a JDOM Element
    *  @param xmlString - the string to convert
    *  @return a JDOM Element, or null if there is a conversion problem
    */
   private Element StringToElement(String xmlString) {
   
   	    try {
 	        SAXBuilder builder = new SAXBuilder();
 	        Document doc = builder.build(new StringReader(xmlString));
 	        return doc.getRootElement() ;	
	    } 
	    catch (JDOMException jdx) {
		     _log.write("StringToElement: JDOM Exception parsing String: "+
			             xmlString); 
           _log.write(jdx);
	    } 
	    catch (IOException iox) {
			_log.write("StringToElememt: Java IO Exception with String: " +
			             xmlString); 
           _log.write(iox);
	    } 
	    return null ;
   }

 //***************************************************************************//

     
/****************************
 * 6. INFORMATIONAL METHODS *
 ***************************/    
     
     /**  get the workitem's (task) decomposition id */
     private String getDecompId(WorkItemRecord wir) {
     	
     	try{
            TaskInformation taskinfo = getTaskInformation(
            	                              wir.getSpecificationID(),
                                              wir.getTaskID(), _sessionHandle);
            return taskinfo.getDecompositionID() ;
        }
        catch (IOException ioe) {
        	_log.write("IO Exception in getDecompId") ;
    		_log.write(ioe);  
    		return null ;
        }
     }                                         
     
  //***************************************************************************//
    
    /** fill an array with details of each spec loaded into engine */
    private void getLoadedSpecs() {

    	try {
	    	_loadedSpecs = (ArrayList) _interfaceBClient.getSpecificationList(
   		                               _sessionHandle);
	    }	
	    catch (IOException ioe) {
	    	_log.write("IO Exception in getLoadedSpecs");
    		_log.write(ioe);
	    }	    
    }	
    
 //***************************************************************************//
    
   /** get the list of input params for a specified specification */
   private ArrayList getInputParams(String specId) {
   	
      	// refresh list of specifications loaded into the engine
    	getLoadedSpecs();
    	
    	// locate input params for the specified spec id
   	    for (int i=0;i<_loadedSpecs.size();i++) {
	    	SpecificationData thisSpec = (SpecificationData) _loadedSpecs.get(i) ;
	    	if (specId.equals(thisSpec.getID())) 
    		    return (ArrayList) thisSpec.getInputParams() ;    		
	    }
 		return null ; 	
   }  
  
 //***************************************************************************//
   
   /** 
    * DEBUG: writes the list of input params to the log for each 
    * specification loaded 
    */
   private void iterateAllSpecsInputParams(){

    	ArrayList specs = _loadedSpecs ;
   		
   		// for each spec
    	Iterator sitr = _loadedSpecs.iterator();
   		while (sitr.hasNext()) {
    		SpecificationData spec = (SpecificationData) sitr.next() ;
    		ArrayList params = (ArrayList) spec.getInputParams() ;
    		_log.write("Specification " + spec.getID() +
    		           " has these input params:");
    		
    		// and for each param
     		Iterator pitr = _loadedSpecs.iterator();
   			while (pitr.hasNext()) {   		
    		   YParameter y = (YParameter) pitr.next() ;
    		   _log.write(y.toXML());
	    	} 
	    }	 	
   }
   
 //***************************************************************************//

   /** 
    * DEBUG: writes the status & datalist of each child of a 
    *        checked out workitem to the log 
    */
   private void attemptToGetChildDataList(WorkItemRecord w) {
 		List children = getChildren(w.getID(), _sessionHandle);

		for (int j=0; j < children.size(); j++) {
			WorkItemRecord x = (WorkItemRecord) children.get(j);
			_log.write("workitem child " + j + " has a status of " +
			           x.getStatus() );
			_log.write("workitem child " + j + " has a datalist of ... ") ;
			_log.write(x.getWorkItemData());          
		}
    }

//***************************************************************************//

    

/**********************
 * 7. BOOLEAN METHODS *
 *********************/   
    
    /**
     *   Workaround to capture end-of-case for a running worklet	
     *   - the last task of a yawl worklet is always called 'terminator'
     *   @return true if this workitem is called 'terminator' 
     */

    private boolean terminatingWorkletItem(WorkItemRecord wir) {
    	String tName = Library.getTaskNameFromId(wir.getTaskID()) ;
    	return (tName.equalsIgnoreCase("terminator"));	
    }
    
//***************************************************************************//
  
    /** Checks if there is a connection to the engine, and
     *  if there isn't, attempts to connect
     *  @return true if connected to the engine
     */
    private boolean connected() {
    	try {
	 	    if (!checkConnection(_sessionHandle))          // if not connected
	            _sessionHandle = connect(_userName, _password);     // connect
	    } 
	    catch (IOException ioe) {
	    	 _log.write(ioe);         
        }        
	    return (successful(_sessionHandle)) ;   
    }
    
//***************************************************************************//
 
   /** return true if the specification is alreaded loaded in the engine */ 
   private boolean isUploaded(String workletName) {
    	
    	// refresh list of specifications loaded into the engine
    	getLoadedSpecs();
    	
    	// check if any loaded specids match the worklet spec selected    	
  	    for (int i=0;i<_loadedSpecs.size();i++) {
	    	SpecificationData spec = (SpecificationData) _loadedSpecs.get(i) ;
	    	if (workletName.equals(spec.getID())) return true ;
        }
        return false ;                                           // no matches
	}
    
//***************************************************************************//

}
