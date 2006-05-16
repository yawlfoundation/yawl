/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.worklet.selection;

import au.edu.qut.yawl.engine.interfce.InterfaceA_EnvironmentBasedClient;
import au.edu.qut.yawl.engine.interfce.InterfaceBWebsideController;
import au.edu.qut.yawl.worklist.model.*;
import au.edu.qut.yawl.elements.data.* ;
import au.edu.qut.yawl.authentication.User;
import au.edu.qut.yawl.exceptions.YAWLException;

import au.edu.qut.yawl.worklet.support.*;
import au.edu.qut.yawl.worklet.rdr.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;                                
import java.util.*;
import java.net.*;


/** 
 *  This class and its support classes represent an implementation for YAWL 
 *  of the Worklet paradigm.
 *
 *  The WorkletService class is the main class for the selection and exception
 *  handling processes. For selection, it receives an enabled workitem from the
 *  engine and attempts to substitute it with a worklet.
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
 *  | WorkletService  | 1---M | RdrSet | 1---M | RdrTree | 1---M | RdrNode |  *
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

public class WorkletService extends InterfaceBWebsideController {
	
	// required data for interfacing with the engine
    private String _user = "workletService" ;
    private String _password = "worklet" ;
    private String _adminUser = "admin" ;
    private String _adminPassword = "YAWL" ;
    private String _sessionHandle = null ;
    private String _engineURI =  "http://localhost:8080/yawl/ia" ;
    private String _workletURI = "http://localhost:8080/workletService/ib" ;
    private InterfaceA_EnvironmentBasedClient _interfaceAClient ;

    // running datasets to keep track of what's executing
    private Map _handledParentItems = new HashMap() ;
    private Map _handledWorkItems = new HashMap() ;
    private Map _casesStarted = new HashMap() ;
    private ArrayList _loadedSpecs = new ArrayList() ;
    
    private RdrSet _selRuleSet ;          // set of currently loaded rdr selection trees
    private RdrSet _exRuleSet ;           // set of currently loaded rdr exception trees

    private static Logger _log ;          // debug log4j file
    private String _workletsDir ;         // where the worklet specs are
    
    private static WorkletService _me ;  // reference to self
    
    
    /** the constructor */
    public WorkletService(){
        super();
        _interfaceAClient = new InterfaceA_EnvironmentBasedClient(_engineURI);
        _selRuleSet = new RdrSet(RdrSet.SELECTION) ;
        _exRuleSet = new RdrSet(RdrSet.EXCEPTION) ;
        _workletsDir = Library.wsWorkletsDir;
        _me = this ;
        _log = Logger.getLogger("au.edu.qut.yawl.worklet.selection.WorkletService");
    }
    
    /** @return a reference to the current WorkletService instance */
    public static WorkletService getInstance() {
    	return _me ;
    }
    
//***************************************************************************//

/************************************ 
 * 1. OVERRIDDEN BASE CLASS METHODS *
 ***********************************/ 
       
    /** 
     *  Handles a message from the engine that a workitem has been enabled 
     *  (see InterfaceBWebsideController for more details)
     *  In this case, it either starts a worklet substitution process, or, if
     *  the workitem denotes the end of a worklet case, it completes the
     *  substitution process by checking the original workitem back into
     *  the engine.
     *
     *  @param workItemRecord - a record describing the enabled workitem
     */
   
    public void handleEnabledWorkItemEvent(WorkItemRecord workItemRecord) {
	    	
    	_log.info("HANDLE ENABLED WORKITEM EVENT") ;  // note to log
    	
    	if (connected()) {
    	 	_log.info("Connection to engine is active") ;
   	 	  	handleWorkletSelection(workItemRecord) ;
		}	 	
    	else _log.info("Could not connect to YAWL engine") ;
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
	    	_log.info("HANDLE CANCELLED WORKITEM EVENT") ;
	    	
	    	if (connected()) {
	    	 	_log.debug("Connection to engine is active") ;
  	    	    _log.debug("ID of cancelled workitem: " + itemId ) ;
	    	
				coItem = (CheckedOutChildItem) _handledWorkItems.get(itemId) ;
				runningCaseId = coItem.getRunningCaseId() ;
				_log.debug("Worklet case running for the cancelled workitem " +
				           "has id of: " + runningCaseId) ;
				
				if (cancelWorkletCase(runningCaseId, coItem)) {				
   		           _handledWorkItems.remove(itemId) ;
     		       _log.debug("Removed from handled child workitems: " + itemId);
   		           
   		           // remove child and if last child also remove parent
   		           coParent = coItem.getParent() ;
   		           coParent.removeChild(coItem) ;

   		           if (! coParent.hasCheckedOutChildItems()) {
		    		   String parentId = coParent.getItem().getID() ;
		    		   _log.debug("No more child cases running for workitem: " +
		    		                  parentId);
		    		   _handledParentItems.remove(parentId) ;
		    		   _log.debug("Completed handling of workitem: " + parentId);
		    	   }
   		        }
   		        else _log.debug("Could not cancel case: " + runningCaseId) ;
	    	}
	    	else _log.debug("Could not connect to engine") ;
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
    	_log.info("HANDLE COMPLETE CASE EVENT") ;     // note to log
    	_log.debug("ID of completed case: " + caseID) ;
    	
    	// reconstruct casedata to JDOM Element
    	Element cdata = StringToElement(casedata); 
    	
    	if (connected()) {
    	 	_log.debug("Connection to engine is active") ;
    	 	handleCompletingWorklet(caseID, cdata) ;
    	}
    	else _log.debug("Could not connect to YAWL engine") ;
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
    private String launchCase(String specID, String caseParams,
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

    	_log.debug("Received workitem for worklet substitution: " + itemId) ;
    	_log.debug("   specId = " + specId);
			    	 		 	 	
	 	// locate rdr ruleset for this task
	 	_selRuleSet.refresh() ;
	 	rdrTreeForWorkItem = _selRuleSet.getRdrTree(specId, taskId);
	 	
	 	if (rdrTreeForWorkItem != null) {

    	 	// OK - this workitem has an associated ruleset so check it out
    	 	// all the child items also get checked out here
    	 	_log.debug("Ruleset found for workitem: " + itemId) ;
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
		else _log.debug("No rule set found for specId: " + specId);
	}	

//***************************************************************************//

   
   /** Deals with the end of a worklet case. Checks in the workitem the 
    *  completing worklet was subbed for and, if the original parent workitem
    *  has no more children after this workitem is checked in, removes its
    *  record from the dynamic datsets of currently handling workitems
    *
    *  @param caseId - the id of the completing case
    *  @param wlCasedata - the completing case's datalist Element
    */
    private void handleCompletingWorklet(String caseId, Element wlCasedata) {
    	
    	// get the id of the workitem this worklet was selected for 
    	String origWorkItemId = (String) _casesStarted.get(caseId);
    	_log.debug("Workitem this worklet case ran in place of is: " +
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
		    EventLogger.log(EventLogger.eComplete, caseId, cociOrig.getWorkletName(),
		       	                     "", cociOrig.getItem().getCaseID()) ;
	
	    	// checkin original workitem & clean up dynamic lists
	    	if (checkinItem(childItem, in, out)) {  
	    	
		    	// get the parent of the original checked out workitem
		    	CheckedOutItem coiParent = cociOrig.getParent();
		    	
	    	    // remove child workitem reference from parent object
		    	coiParent.removeChild(cociOrig) ;   
		    	    	            
		    	// and remove it from the dynamic execution datasets    	            
		    	_casesStarted.remove(caseId) ;  
		       	_log.debug("Removed from cases started: " + caseId) ;
		       	_handledWorkItems.remove(origWorkItemId) ;
		       	_log.debug("Removed from handled child workitems: " +
		       	            origWorkItemId) ;
		    	 
		    	// if there is no more child cases, we're done with this parent
		    	if (! coiParent.hasCheckedOutChildItems()) {
		    		String parentId = coiParent.getItem().getID() ;
		    		_log.debug("No more child cases running for workitem: " +
		    		            parentId);
		    		_handledParentItems.remove(parentId) ;
		    		_log.debug("Completed handling of workitem: " + parentId);
		    	}
		    }
		    else _log.debug("Could not check in child workitem: " +
		                                              childItem.getID());
	    }
	   	else _log.debug("Could not retrieve the substituted workitem " +
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
       
	   _log.debug("Processing worklet substitution for workitem: " + childId);
                                   	
	 	// select appropriate worklet
	 	String wSelected = tree.search(coChild.getDatalist());
	 	
	 	if (wSelected != null) {
	 	    _log.debug("Rule search returned worklet: " + wSelected);
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
		    else _log.debug("could not launch worklet: " + wSelected) ;
		}
	    else _log.debug("Rule search did not find a worklet to select " +
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
     
     	_log.debug("Checking parent workitem out of engine: " + wir.getID());

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
		
		_log.debug("Checking out child workitems...") ;
		
		// get all the child instances of this workitem
		List children = getChildren(coi.getItem().getID(), _sessionHandle);
		
		// checkout each child instance
	    for (int i = 0; i < children.size(); i++) {
	       WorkItemRecord itemRec = (WorkItemRecord) children.get(i); 
	       
	       // if its 'fired' check it out	       
	       if (WorkItemRecord.statusFired.equals(itemRec.getStatus())) {
	       	   if (checkOutWorkItem(itemRec))
        	      EventLogger.log(EventLogger.eCheckOut, itemRec) ;  // log checkout
           }          
	       	   
	       // if its 'executing', it means it got checked out with the parent	   
	       else if (WorkItemRecord.statusExecuting.equals(itemRec.getStatus())) {
	          _log.debug("   child already checked out with parent: " +
	                         itemRec.getID()) ;
        	  EventLogger.log(EventLogger.eCheckOut, itemRec) ;      // log checkout
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
			   _log.debug("child " + j + " has NOT been added to CheckedOutItems") ;
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
	 	    if (null != checkOut(wir.getID(), _sessionHandle)) {
 	 	        _log.debug("   checkout successful: " + wir.getID());
 	 	        return true ;
 	        }
 	        else {
 	        	_log.debug("   checkout unsuccessful: " + wir.getID());
 	        	return false;
 	        } 		    
	 	}
         catch (YAWLException ye) {
             _log.error("YAWL Exception with checkout: " + wir.getID(), ye);
             return false ;
         }
         catch (IOException ioe) {
	 		_log.error("IO Exception with checkout: " + wir.getID(), ioe);
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
    		   EventLogger.log(EventLogger.eCheckIn, wir) ;
    		   _log.debug("Successful checkin of work item: " + wir.getID()) ;
    		   return true ;
    		}   
    		else {
    		   _log.debug("Checkin unsuccessful for: " + wir.getID()) ;
    		   _log.debug("Diagnostic string: " + result) ;
    		}     
    	} 
    	catch (IOException ioe) {
    		_log.error("checkinItem method caused java IO Exception", ioe) ;
    	}
    	catch (JDOMException jde) {
    		_log.error("checkinItem method caused JDOM Exception", jde) ;
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
     *  @return true if upload is successful or spec is already loaded
     */ 
    private boolean uploadWorklet(String workletName) {
    	
    	String fileName = workletName + ".xml" ;
    	String fullFileName = _workletsDir + fileName ;

    	if (isUploaded(workletName)) {
            _log.debug("Worklet specification '" + workletName
                        + "' is already loaded in Engine") ;
    		return true ; 
    	}                        
    	    	
    	String wSpec = Library.FileToString(fullFileName);  // needs spec as String

		if (wSpec != null) {
    	   if (successful(_interfaceAClient.uploadSpecification(wSpec,
    	 	               fileName, _sessionHandle))) {
    	 	  _log.debug("Successfully uploaded worklet specification: "
    	 	               + workletName) ;
    	 	  return true ; 
    	   }	    	
    	   else {
    	      _log.debug("Unsuccessful worklet specification upload : "
    	                   + workletName)	;
    	      return false ;   
    	   }
    	}
  	    else {
  	    	_log.debug("Rule search found: " + workletName +
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
			    EventLogger.log(EventLogger.eLaunch, caseId, workletName, "",
			                                    coci.getItem().getCaseID());
			    _log.debug("Launched case for worklet " + workletName +
			               " with ID: " + caseId) ;
			    return caseId ;
			}
			else {
				_log.debug("Unable to launch worklet: " + workletName) ;
				_log.debug("Diagnostic message: " + caseId) ;
				return null ;
			}    
		}
		catch (URISyntaxException use) {
			_log.error("URI Exception when assigning worklet URI", use) ;
			return null ;
		}
		catch (IOException ioe) {
			_log.error("IO Exception when attempting to launch case", ioe) ;
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
   		
   		_log.info("REPLACE WORKLET REQUEST");
   		
   		// if workitem is currently checked out
   		if (_handledWorkItems.containsKey(itemid)) {
   			result += "found." + Library.newline ;
   			_log.debug("Itemid received found in handleditems: " + itemid);
   		   
    	   // get the checkedout child item record for this workitem
    	   CheckedOutChildItem coci = 
    	                 (CheckedOutChildItem) _handledWorkItems.get(itemid) ; 
    	   
    	   // get the case id of the running worklet subbed for this workitem
    	   caseid = coci.getRunningCaseId() ;       
    	      	      
    	   // cancel the worklet running for the workitem
    	   result += "Cancelling running worklet case with case id " +
    	             caseid + " for workitem..." ;
    	   _log.debug("Running worklet case id for this workitem is: " + caseid);

    	   if (cancelWorkletCase(caseid, coci)) {
	    	   _log.debug("Removing case from cases started: " + caseid);
		       _casesStarted.remove(caseid) ;    	   	  
	
	           result += "done." + Library.newline ;
           
	           // go through the selection process again
	           result += "Launching new replacement worklet case based on revised ruleset...";
	           _log.debug("Launching new replacement worklet case based on revised ruleset");
	           
	           // locate rdr ruleset for this task
	     	   String specId = coci.getItem().getSpecificationID() ;   
	    	   String taskId = coci.getItem().getTaskID() ;
	           
	 	 	   _selRuleSet.refresh() ;
		 	   RdrTree tree = _selRuleSet.getRdrTree(specId, taskId);
		 	
		       if (tree != null) {
	    	 	   _log.debug("Ruleset found for workitem: " + coci.getItemId()) ;
	    	 	   ProcessWorkItemSubstitution(tree, coci) ;
	               result += "done. " + Library.newline +
	                   "The worklet '" + coci.getWorkletName() +
	                   "' has been launched for workitem '" + itemid + 
	                   "' and has case id: " + coci.getRunningCaseId() + 
	                   Library.newline ; 
	           }
	           else {
	           	 _log.debug("Failed to locate ruleset for workitem." ) ;
	           	 result += "failed." + Library.newline + 
   		                   "Replacement process cannot continue." ;
	           }          
    	   }
    	   else {
    	   	   _log.debug("Failed to cancel running case: " + caseid) ;
    	   	   result += "failed." + Library.newline + 
   		                 "Replacement process cannot continue."  ; 
    	   }	                                            
   		}   
   		else {
   		   _log.debug("Itemid not found in handleditems: " + itemid) ;
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
	   _log.debug("Cancelling worklet case: " + caseid) ;
	   try {
	   	   _interfaceBClient.cancelCase(caseid, _sessionHandle) ;
	   	   
	   	   // log successful cancellation event
	   	   EventLogger.log(EventLogger.eCancel, caseid, coci.getWorkletName(),
	   	                       "", coci.getItem().getCaseID()) ;
	   	   _log.debug("Worklet case successfully cancelled: " + caseid) ;
	   	   return true ;                    
	   }
	   catch (IOException ioe) {
           _log.error("IO Exception when attempting to cancel case", ioe) ;
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
       	     	_log.error("Exception adding content to worklet data list", iae) ;
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
		     _log.error("StringToElement: JDOM Exception parsing String: "+
			             xmlString, jdx);
	    }
	    catch (IOException iox) {
			_log.error("StringToElememt: Java IO Exception with String: " +
			             xmlString, iox);
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
        	_log.error("IO Exception in getDecompId", ioe) ;
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
	    	_log.error("IO Exception in getLoadedSpecs", ioe);
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
    	Iterator sitr = specs.iterator();
   		while (sitr.hasNext()) {
    		SpecificationData spec = (SpecificationData) sitr.next() ;
    		ArrayList params = (ArrayList) spec.getInputParams() ;
    		_log.info("Specification " + spec.getID() +
    		           " has these input params:");
    		
    		// and for each param
     		Iterator pitr = params.iterator();
   			while (pitr.hasNext()) {   		
    		   YParameter y = (YParameter) pitr.next() ;
    		   _log.info(y.toXML());
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
			_log.info("workitem child " + j + " has a status of " +
			           x.getStatus() );
			_log.info("workitem child " + j + " has a datalist of ... ") ;
			_log.info(x.getWorkItemData());
		}
    }

//***************************************************************************//


/**********************
 * 7. BOOLEAN METHODS *
 *********************/
    /** Checks if a worklet spec has already been loaded into engine
     *  @param workletName
     *  @return true if the specification is alreaded loaded in the engine
     */
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

    /** Checks if there is a connection to the engine, and
     *  if there isn't, attempts to connect
     *  @return true if connected to the engine
     */
    private boolean connected() {
    	try {
	 	    if (!checkConnection(_sessionHandle))          // if not connected
                _sessionHandle = connectAsService();
        }
	    catch (IOException ioe) {
	    	 _log.error("Exception attempting to connect to engine", ioe);
        }        
	    return (successful(_sessionHandle)) ;   
    }
    
//***************************************************************************//

    /**
     * Attempts to logon to the engine using a service id
     * @return  a sessionHandle for the connection
     * @throws IOException
     */
    private String connectAsService() throws IOException{

        // first connect as default admin user
        _sessionHandle = connect(_adminUser, _adminPassword);

        // create new user for service if necessary
        if (! isRegisteredUser(_user))
           _interfaceAClient.createUser(_user, _password, true, _sessionHandle);

        // logon with service user and return the result
        return connect(_user, _password);
    }

//***************************************************************************//

    /**
     * Checks if a user is currently registered for this session
     * @param user
     * @return true if the user is registered in the current session
     */
    private boolean isRegisteredUser(String user) {

        // check if service is a registered user
        ArrayList users = (ArrayList) _interfaceAClient.getUsers(_sessionHandle);

        Iterator itr = users.iterator();
   		while (itr.hasNext()) {
    	   User u = (User) itr.next() ;
           if ( u.getUserID().equals(user) ) return true ;      // user in list
	    }
        return false;                                       // user not in list
    }
}

//***************************************************************************//
//***************************************************************************//
