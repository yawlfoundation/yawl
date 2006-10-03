/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.worklet;

import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.interfce.InterfaceA_EnvironmentBasedClient;
import au.edu.qut.yawl.engine.interfce.InterfaceBWebsideController;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.worklist.model.*;
import au.edu.qut.yawl.elements.data.* ;
import au.edu.qut.yawl.authentication.User;
import au.edu.qut.yawl.exceptions.YAWLException;

import au.edu.qut.yawl.worklet.support.*;
import au.edu.qut.yawl.worklet.rdr.*;
import au.edu.qut.yawl.worklet.selection.*;
import au.edu.qut.yawl.worklet.exception.*;
import au.edu.qut.yawl.worklet.admin.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;                                
import java.util.*;


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
 *          |    +----------------------> | WorkletGateway |                       *
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
 *  | CheckedOutChildItem |                        | RdrConditionException |  *
 *  +=====================+                        +=======================+  * 
 *                                                                            *
 * -------------------------------------------------------------------------- *
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.7, 10/12/2005
 */

public class WorkletService extends InterfaceBWebsideController {

    // constants for Exception Types
    public static final int XTYPE_CASE_PRE_CONSTRAINTS = 0;
    public static final int XTYPE_CASE_POST_CONSTRAINTS = 1;
    public static final int XTYPE_ITEM_PRE_CONSTRAINTS = 2;
    public static final int XTYPE_ITEM_POST_CONSTRAINTS = 3;
    public static final int XTYPE_WORKITEM_ABORT = 4;
    public static final int XTYPE_TIMEOUT = 5;
    public static final int XTYPE_RESOURCE_UNAVAILABLE = 6;
    public static final int XTYPE_CONSTRAINT_VIOLATION = 7;
    public static final int XTYPE_CASE_EXTERNAL_TRIGGER = 8;
    public static final int XTYPE_ITEM_EXTERNAL_TRIGGER = 9;
    public static final int XTYPE_SELECTION = 10;             // not really an ex. type


    // required data for interfacing with the engine
    protected String _user = "workletService" ;
    protected String _password = "worklet" ;
    protected String _adminUser = "admin" ;
    protected String _adminPassword = "YAWL" ;
    protected String _sessionHandle = null ;
    protected String _engineURI =  "http://localhost:8080/yawl" ;
    private String _workletURI = "http://localhost:8080/workletService/ib" ;
    private InterfaceA_EnvironmentBasedClient _interfaceAClient ;


    /** running datasets to keep track of what's executing. Mappings:
     *    _handledParentItems:
     *       - KEY: [String] id of the parent WorkItemRecord (via wir.getID())
     *       - VALUE: [CheckedOutItem] obj referring to the parent work item
     *    _handledWorkItems:
     *       - KEY: [String] id of the child WorkItemRecord (via wir.getID())
     *       - VALUE: [CheckedOutChildItem] obj referring to the child work item
     *    _casesStarted:
     *       - KEY: [String] case id of a launched worklet case
     *       - VALUE: [String] id of the child WorkItemRecord the worklet was
     *                launched for (i.e. the key of a corresponding _handledWorkItem)
     *    _ruleSets:
     *       - KEY: [String] spec id this set of rules appies to
     *       - VALUE: [RdrSet] set of rules for the spec
     */
    private HashMap<String,CheckedOutItem> _handledParentItems =
                   new HashMap<String,CheckedOutItem>() ; // checked out parents
    private HashMap<String,CheckedOutChildItem> _handledWorkItems =
                   new HashMap<String,CheckedOutChildItem>() ;   // checked out children
    private HashMap<String,String> _casesStarted =
                   new HashMap<String,String>() ;         // running selection worklets
    private HashMap<String,RdrSet> _ruleSets =
                   new HashMap<String,RdrSet>() ;         // rdrSets for each spec
    protected ArrayList<SpecificationData> _loadedSpecs =
                   new ArrayList<SpecificationData>() ;   // all specs loaded in engine
    private AdminTasksManager _adminTasksMgr = new AdminTasksManager() ;   // admin tasks


    protected boolean _persisting ;                     // is persistence enabled?
    protected DBManager _dbMgr ;                        // manages persistence
    private static Logger _log ;                        // debug log4j file
    private String _workletsDir ;                       // where the worklet specs are
    private static WorkletService _me ;                 // reference to self
    private static ExceptionService _exService ;        // reference to ExceptionService
    private boolean restored = false;

    /** the constructor */
    public WorkletService(){
        super();
        _interfaceAClient = new InterfaceA_EnvironmentBasedClient(_engineURI + "/ia");
        _me = this ;
        _log = Logger.getLogger("au.edu.qut.yawl.worklet.WorkletService");
    }

    /** @return a reference to the current WorkletService instance */
    public static WorkletService getInstance() {
        return _me ;
    }

    protected void registerExceptionService(ExceptionService es) {
        _exService = es ;
    }

    // called from servlet WorkletGateway after contexts are loaded
    public void completeInitialisation() {
        _workletsDir = Library.wsWorkletsDir;
        _persisting = Library.wsPersistOn;

        // init persistence class
        if (_dbMgr == null) _dbMgr = DBManager.getInstance(_persisting);
        _persisting = (_dbMgr != null);                 // turn it off if no connection

        // reload running cases data
        if ((_persisting) && (! restored)) restoreDataSets();
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

        _log.info("HANDLE ENABLED WORKITEM EVENT") ;        // note to log

        if (connected()) {
             _log.info("Connection to engine is active") ;
            handleWorkletSelection(workItemRecord) ;
        }
        else _log.error("Could not connect to YAWL engine") ;
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

                coItem = _handledWorkItems.get(itemId) ;
                runningCaseId = coItem.getRunningCaseId() ;
                _log.debug("Worklet case running for the cancelled workitem " +
                           "has id of: " + runningCaseId) ;

                if (cancelWorkletCase(runningCaseId, coItem)) {
                      _handledWorkItems.remove(itemId) ;
                    if (_persisting) _dbMgr.persist(coItem, DBManager.DB_DELETE);
                    _log.debug("Removed from handled child workitems: " + itemId);

                      // remove child and if last child also remove parent
                      coParent = coItem.getParent() ;
                      coParent.removeChild(coItem) ;

                      if (! coParent.hasCheckedOutChildItems()) {
                       String parentId = coParent.getItem().getID() ;
                       _log.debug("No more child cases running for workitem: " +
                                      parentId);
                       _handledParentItems.remove(parentId) ;
                       if (_persisting) _dbMgr.persist(coParent, DBManager.DB_DELETE);
                       _log.debug("Completed handling of workitem: " + parentId);

                   }
                   }
                   else _log.error("Could not cancel case: " + runningCaseId) ;
            }
            else _log.error("Could not connect to engine") ;
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
            if (_casesStarted.containsKey(caseID))
                handleCompletingSelectionWorklet(caseID, cdata) ;
            else
                _log.info("Completing case is not a worklet selection: " + caseID);
       }
        else _log.error("Could not connect to YAWL engine") ;
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
                              String sessionHandle, boolean observer)
                                                   throws IOException {
        Map<String,String> paramsMap = new HashMap<String, String>();
        paramsMap.put("sessionHandle", sessionHandle);
        paramsMap.put("action", "launchCase");
        paramsMap.put("caseParams", caseParams);
        if (observer) paramsMap.put("completionObserverURI", _workletURI);

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
        String taskId = getDecompID(wir) ;
        String itemId = wir.getID() ;
        RdrTree selectionTreeForTask ;                   // rules for task
        CheckedOutItem coItem ;                          // record of item
        CheckedOutChildItem coChild ;                    // child item rec.

        _log.debug("Received workitem for worklet substitution: " + itemId) ;
        _log.debug("   specId = " + specId);

         // locate rdr ruleset for this task
        selectionTreeForTask = getTree(specId, taskId, XTYPE_SELECTION) ;

         if (selectionTreeForTask != null) {

             // OK - this workitem has an associated ruleset so check it out
             // all the child items also get checked out here
             _log.debug("Ruleset found for workitem: " + itemId) ;
             coItem = checkOutItem(wir) ;

            // remember this handling of the parent item (if not a replace)
             if (_handledParentItems.containsKey(itemId)) {
               if (_persisting) _dbMgr.persist(coItem,DBManager.DB_UPDATE);
             }
            else {
                _handledParentItems.put(itemId, coItem) ;
                if (_persisting) _dbMgr.persist(coItem,DBManager.DB_INSERT);
             }

             // launch a worklet case for each checked out child workitem
             for (int i = 0; i < coItem.getChildCount(); i++) {
                 coChild = coItem.getCheckedOutChildItem(i) ;
                 ProcessWorkItemSubstitution(selectionTreeForTask, coChild) ;
            }
        }
        else _log.warn("No rule set found for specId: " + specId);
    }

//***************************************************************************//

   /** Deals with the end of a selection worklet case.
    *
    *  It checks in the workitem the completing worklet was subbed for and,
    *  if the original parent workitem has no more children after this workitem
    *  is checked in, removes its record from the dynamic datsets of currently
    *  handled workitems
    *
    *  @param caseId - the id of the completing case
    *  @param wlCasedata - the completing case's datalist Element
    */
    private void handleCompletingSelectionWorklet(String caseId, Element wlCasedata) {

        // get the id of the workitem this worklet was selected for
        String origWorkItemId = _casesStarted.get(caseId);
        _log.debug("Workitem this worklet case ran in place of is: " +
                    origWorkItemId);

        // get the checkedoutchilditem record for the original workitem
        CheckedOutChildItem cociOrig = _handledWorkItems.get(origWorkItemId) ;

        // get the actual workitem this worklet case substituted
           WorkItemRecord childItem = cociOrig.getItem() ;

        if (childItem != null) {

            // get the workitem's input data list
            Element in = cociOrig.getDatalist() ;

            // update workitem's datalist with the worklet's output values
            Element out = updateDataList(in, wlCasedata) ;

            // log the worklet's case completion event
            EventLogger.log(_dbMgr, EventLogger.eComplete, caseId,
                            cociOrig.getWorkletName(), "", cociOrig.getItem().getCaseID(), -1) ;

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
                if (_persisting) _dbMgr.persist(cociOrig, DBManager.DB_DELETE);
                   _log.debug("Removed from handled child workitems: " +
                               origWorkItemId) ;

                // if there is no more child cases, we're done with this parent
                if (! coiParent.hasCheckedOutChildItems()) {
                    String parentId = coiParent.getItem().getID() ;
                    _log.debug("No more child cases running for workitem: " +
                                parentId);
                    _handledParentItems.remove(parentId) ;
                    if (_persisting) _dbMgr.persist(coiParent, DBManager.DB_DELETE);
                    _log.info("Completed handling of workitem: " + parentId);
                }
            }
            else _log.warn("Could not check in child workitem: " +
                                                      childItem.getID());
        }
           else _log.warn("Could not retrieve the substituted workitem " +
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
        RdrConclusion result = new RdrConclusion(tree.search(coChild.getDatalist()));
        String wSelected =  result.getTarget(1);

         if (wSelected != null) {

             _log.debug("Rule search returned worklet: " + wSelected);
             coChild.setWorkletName(wSelected) ;
            coChild.setExType(XTYPE_SELECTION);

             // load spec & launch case as substitute for checked out workitem
            if (uploadWorklet(wSelected)) {
                String caseID = launchWorklet(coChild, true) ;
               if (caseID != null) _casesStarted.put(caseID, childId) ;

                // save the search results (if later rule append is needed)
                coChild.setSearchPair(tree.getLastPair());
                coChild.saveSearchResults() ;

                // remember this handling (if not a replace)
                if (_handledWorkItems.containsKey(childId)) {
                    if (_persisting) _dbMgr.persist(coChild, DBManager.DB_UPDATE);
                }
                else {
                    _handledWorkItems.put(childId, coChild) ;
                    if (_persisting) {
                        _dbMgr.persist(coChild, DBManager.DB_INSERT);
                        coChild.ObjectPersisted();
                    }
                }
            }
            else _log.warn("could not launch worklet: " + wSelected) ;
        }
        else _log.warn("Rule search did not find a worklet to select " +
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
               return _handledParentItems.get(itemId) ;
           }
           else {
             CheckedOutItem coItem = checkOutParentItem(wir) ;
             checkOutChildren(coItem) ;	          // always at least one child
             return coItem ;
           }
    }

//***************************************************************************//

    /** checks out the parent enabled workitem */
    protected CheckedOutItem checkOutParentItem(WorkItemRecord wir) {

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
    protected void checkOutChildren(CheckedOutItem coi) {

        _log.debug("Checking out child workitems...") ;

        // get all the child instances of this workitem
        List<WorkItemRecord> children = getChildren(coi.getItem().getID(), _sessionHandle);

        // checkout each child instance
        for (WorkItemRecord itemRec : children) {

            // if its 'fired' check it out
            if (YWorkItem.Status.Fired.equals(itemRec.getStatus())) {
                if (checkOutWorkItem(itemRec))
                    EventLogger.log(_dbMgr, EventLogger.eCheckOut, itemRec, -1);  // log checkout
            }

            // if its 'executing', it means it got checked out with the parent
            else if (YWorkItem.Status.Executing.equals(itemRec.getStatus())) {
                _log.debug("   child already checked out with parent: " +
                        itemRec.getID());
                EventLogger.log(_dbMgr, EventLogger.eCheckOut, itemRec, -1);      // log checkout
            }
        }

        // update child item list after checkout (to capture status changes)
        children = getChildren(coi.getItem().getID(), _sessionHandle);

        // if checkout ok and status is 'executing' add each child to parent coi
        for (int j=0; j < children.size(); j++) {
            WorkItemRecord w = children.get(j) ;

            if (YWorkItem.Status.Executing.equals(w.getStatus())) {
               coi.addChild(w) ;
            }
            else
               _log.error("child " + j + " has NOT been added to CheckedOutItems") ;
        }
    }

//***************************************************************************//

    /**
     *  Check the workitem out of the engine
     *  @param wir - the workitem to check out
     *  @return true if checkout was successful
     */
    protected boolean checkOutWorkItem(WorkItemRecord wir) {

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

        // make sure the wir is locally cached (esp. important after a restore)
        checkCacheForWorkItem(wir) ;

        try {
            String result = checkInWorkItem(wir.getID(), in, out,
                                                          _sessionHandle) ;
            if (successful(result)) {

                 // log the successful checkin event
               EventLogger.log(_dbMgr, EventLogger.eCheckIn, wir, -1) ;
               _log.info("Successful checkin of work item: " + wir.getID()) ;
               return true ;
            }
            else {
               _log.error("Checkin unsuccessful for: " + wir.getID()) ;
               _log.error("Diagnostic string: " + result) ;
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
    protected boolean uploadWorklet(String workletName) {

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
     *  @param wr - the record of the CheckedOutChildItem or the HandlerRunner
     *              to start the worklet for
     *  @return - the case id of the started worklet case
     */
    protected String launchWorklet(WorkletRecord wr, boolean setObserver) {

        String caseId ;
        String workletName = wr.getWorkletName() ;

        // fill the case params with matching data values from the workitem
        String caseData = mapItemParamsToWorkletCaseParams(wr);

        try {
              // launch case (and set completion observer)
              caseId = launchCase(workletName, caseData, _sessionHandle, setObserver);
            if (successful(caseId)) {

                // record case id
                wr.setRunningCaseId(caseId) ;

                // and log launch event
                EventLogger.log(_dbMgr, EventLogger.eLaunch, caseId, workletName, "",
                                                wr.getCaseID(), wr.getReasonType());
                _log.debug("Launched case for worklet " + workletName +
                           " with ID: " + caseId) ;
                return caseId ;
            }
            else {
                _log.warn("Unable to launch worklet: " + workletName) ;
                _log.warn("Diagnostic message: " + caseId) ;
                return null ;
            }
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
     *  Called by WorkletGateway after a call from the RdrEditor that the ruleset
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
           CheckedOutChildItem coci = _handledWorkItems.get(itemid) ;

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
               String taskId = getDecompID(coci.getItem()) ;

               // refresh ruleset to pickup newly added rule
               RefreshRuleSet(specId);

               RdrTree tree = getTree(specId, taskId, XTYPE_SELECTION) ;

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
                    _log.warn("Failed to locate ruleset for workitem." ) ;
                    result += "failed." + Library.newline +
                              "Replacement process cannot continue." ;
               }
           }
           else {
                  _log.warn("Failed to cancel running case: " + caseid) ;
                  result += "failed." + Library.newline +
                            "Replacement process cannot continue."  ;
           }
           }
           else {
              _log.warn("Itemid not found in handleditems: " + itemid) ;
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
              EventLogger.log(_dbMgr, EventLogger.eCancel, caseid, coci.getWorkletName(),
                                  "", coci.getItem().getCaseID(), -1) ;
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
   protected Element updateDataList(Element in, Element out) {

        // get a copy of the 'in' list   	
           Element result = (Element) in.clone() ;

           // for each child in 'out' list, get its value and copy to 'in' list
        for (Object o : (out.getChildren())) {
            Element e = (Element) o;

            // if there's a matching 'in' data item, update its value
            Element resData = result.getChild(e.getName());
            if (resData != null)
                resData.setText(e.getText());
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
    *  @param wr - the CheckedOutChildItem's or HandlerRunner's info object
    *  @return the loaded input params of the new worklet case 
    *          (launchCase() requires the input params as a String)
    */
   private String mapItemParamsToWorkletCaseParams(WorkletRecord wr) {

          String wlName = wr.getWorkletName() ;    // worklet to replace item
          Element itemData = wr.getDatalist() ;    // get datalist of work item
          Element wlData = new Element(wlName);      // new datalist for worklet

           ArrayList<YParameter> inParams = getInputParams(wlName) ;  // worklet input params

        // if worklet has no net-level inputs, or workitem has no datalist, we're done
        if ((inParams == null) || (itemData == null)) return null;

        // extract the name of each worklet input param
        for (YParameter param : inParams) {
           String paramName = param.getName();

           // get the data element of the workitem with the same name as
           // the one for the worklet (assigns null if no match)
           Element wlElem = itemData.getChild(paramName);

           try {
               // if matching element, copy it and add to worklet datalist
               if (wlElem != null) {
                   Element copy = (Element) wlElem.clone();
                   wlData.addContent(copy);
               }

               // no matching data for input param, so add empty element
               else
                   wlData.addContent(new Element(paramName));
           }
           catch (IllegalAddException iae) {
               _log.error("Exception adding content to worklet data list", iae);
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

       if (xmlString == null) return null ;
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

     protected RdrTree getTree(String specID, String taskID, int treeType) {

        RdrSet ruleSet ;
        RdrTree result ;

        // if we already have this rdrset loaded
        if (_ruleSets.containsKey(specID))
           ruleSet = _ruleSets.get(specID) ;
        else {
           ruleSet = new RdrSet(specID);                  // make a new set
           if (ruleSet.hasRules())
              _ruleSets.put(specID, ruleSet) ;               // & store it
           else
              return null ;                           // no rules for spec
        }

        if ((treeType == XTYPE_CASE_PRE_CONSTRAINTS) ||
            (treeType == XTYPE_CASE_POST_CONSTRAINTS) ||
            (treeType == XTYPE_CASE_EXTERNAL_TRIGGER))
            result = ruleSet.getTree(treeType) ;          // trees at case level
        else
            result = ruleSet.getTree(treeType, taskID) ;  // trees at task level

        return result ;
     }

     protected void RefreshRuleSet(String specID) {
         RdrSet ruleSet = (RdrSet) _ruleSets.get(specID) ;
         if (ruleSet != null) ruleSet.refresh() ;
     }

    //***************************************************************************//

     /**  get the workitem's (task) decomposition id */
     public String getDecompID(WorkItemRecord wir) {

         try {
            TaskInformation taskinfo = getTaskInformation(
                                              wir.getSpecificationID(),
                                              wir.getTaskID(), _sessionHandle);
            return taskinfo.getDecompositionID() ;
        }
        catch (IOException ioe) {
            _log.error("IO Exception in getDecompId ", ioe) ;
            return null ;
        }
     }

  //***************************************************************************//

    /**  get the workitem's (task) decomposition id */
    public String getDecompID(String specID, String taskID) {

       try {
           TaskInformation taskinfo = getTaskInformation(specID, taskID, _sessionHandle);
           return taskinfo.getDecompositionID() ;
       }
       catch (IOException ioe) {
           _log.error("IO Exception in getDecompId ", ioe) ;
           return null ;
       }
    }

 //***************************************************************************//

    /** fill an array with details of each spec loaded into engine */
    private void getLoadedSpecs() {

        try {
            _loadedSpecs = (ArrayList<SpecificationData>)
                             _interfaceBClient.getSpecificationList(_sessionHandle);
        }
        catch (IOException ioe) {
            _log.error("IO Exception in getLoadedSpecs", ioe);
        }
    }

 //***************************************************************************//

   /** get the list of input params for a specified specification */
   private ArrayList<YParameter> getInputParams(String specId) {

          // refresh list of specifications loaded into the engine
        getLoadedSpecs();

        // locate input params for the specified spec id
       for (SpecificationData thisSpec : _loadedSpecs) {
           if (specId.equals(thisSpec.getID()))
               return (ArrayList<YParameter>) thisSpec.getInputParams();
       }
         return null ;
   }

 //***************************************************************************//

   /**
    * DEBUG: writes the list of input params to the log for each 
    * specification loaded 
    */
   private void iterateAllSpecsInputParams(){

        ArrayList<SpecificationData> specs = _loadedSpecs ;

           // for each spec
        Iterator<SpecificationData> sitr = specs.iterator();
           while (sitr.hasNext()) {
            SpecificationData spec = sitr.next() ;
            ArrayList<YParameter> params = (ArrayList<YParameter>) spec.getInputParams() ;
            _log.info("Specification " + spec.getID() +
                       " has these input params:");

            // and for each param
             Iterator<YParameter> pitr = params.iterator();
               while (pitr.hasNext()) {
               YParameter y = pitr.next() ;
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
    protected boolean connected() {
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
        ArrayList<User> users = (ArrayList<User>) _interfaceAClient.getUsers(_sessionHandle);

        Iterator<User> itr = users.iterator();
           while (itr.hasNext()) {
           User u = itr.next() ;
           if ( u.getUserID().equals(user) ) return true ;      // user in list
        }
        return false;                                       // user not in list
    }

//***************************************************************************//
//***************************************************************************//

    /** add case-level admin task */
    public void addAdministrationTask(String caseID, String title, String scenario,
                                      String process, int taskType) {

        AdministrationTask adminTask =
               _adminTasksMgr.addTask(caseID, title, scenario, process, taskType);
        if (_persisting) _dbMgr.persist(adminTask, DBManager.DB_INSERT) ;

        // suspend case pending admin action
        _exService.suspendCase(caseID);
    }

    
    /** add item-level admin task */
    public void addAdministrationTask(String caseID, String itemID, String title,
                                      String scenario, String process, int taskType) {

        AdministrationTask adminTask =
               _adminTasksMgr.addTask(caseID, itemID, title, scenario, process, taskType);
        if (_persisting) _dbMgr.persist(adminTask, DBManager.DB_INSERT) ;

        // suspend item pending admin action
        _exService.suspendWorkItem(itemID);
    }


    public List<String> getAdminTaskTitles() {
        return _adminTasksMgr.getAllTaskTitles() ;
    }

    public void completeAdminTask(String adminTaskID) {
        AdministrationTask adminTask =_adminTasksMgr.removeTask(adminTaskID);
        if (_persisting) _dbMgr.persist(adminTask, DBManager.DB_DELETE) ;
    }

    public List<AdministrationTask> getAllAdminTasksAsList() {
        return _adminTasksMgr.getAllTasksAsList();
    }

    public AdministrationTask getAdminTask(String id) {
        return _adminTasksMgr.getTask(id);
    }

    public boolean isAdminSession(String sessionHandle) {
        String msg = _interfaceAClient.checkConnection(sessionHandle);
        return successful(msg);
    }

    public boolean isWorkletCase(String caseID) {
        return _casesStarted.containsKey(caseID) ;
    }


    private void restoreDataSets() {
        if (! restored) {
            _handledParentItems = restoreHandledParentItems() ;
            _handledWorkItems = restoreHandledChildItems() ;
            _adminTasksMgr = restoreAdminTasksManager() ;  // admin tasks
            // dump();                                    // temp call to dump datasets
            restored = true;                               // only restore once
        }
    }

    private HashMap<String,CheckedOutItem> restoreHandledParentItems() {
        HashMap<String,CheckedOutItem> result = new HashMap<String, CheckedOutItem>();
        CheckedOutItem coi ;
        List items = _dbMgr.getObjectsForClass(CheckedOutItem.class.getName());

        if (items != null) {
           Iterator itr = items.iterator();
           while (itr.hasNext()) {
               coi = (CheckedOutItem) itr.next();
               coi.setItem(RDRConversionTools.xmlStringtoWIR(coi.get_wirStr()));     // restore wir
               coi.initNonPersistedItems();
               loadTree(coi.getSpecId());             // needed when child items restore
               result.put(coi.getParentID(), coi);
           }
       }
       return result ;
   }


    private HashMap<String,CheckedOutChildItem> restoreHandledChildItems() {
        HashMap<String,CheckedOutChildItem> result = new HashMap<String, CheckedOutChildItem>();
        List items = _dbMgr.getObjectsForClass(CheckedOutChildItem.class.getName());

        if (items != null) {
           Iterator itr = items.iterator();
           while (itr.hasNext()) {
               CheckedOutChildItem coci = (CheckedOutChildItem) itr.next();

               // reset data list & wir
               coci.initNonPersistedItems();

               // restore link child <--> parent
               CheckedOutItem parent = (CheckedOutItem) _handledParentItems.get(coci.get_parentID());
               parent.addChild(coci);

               // rebuild search pair nodes
               coci.rebuildSearchPair(coci.getSpecID(),
                                   Library.getTaskNameFromId(coci.getItem().getTaskID()));

               // rebuild cases started data
               String itemID = coci.getItem().getID();
               _casesStarted.put(coci.getRunningCaseId(), itemID);

               result.put(itemID, coci);
           }
       }
       return result ;
    }


    private AdminTasksManager restoreAdminTasksManager() {
        AdminTasksManager result = new AdminTasksManager();
        List<AdministrationTask> items =
                       _dbMgr.getObjectsForClass(AdministrationTask.class.getName());

        if (items != null) {
            for (AdministrationTask task : items) result.addTask(task);
        }
        return result ;
    }



    public void loadTree(String specID) {
        if (! _ruleSets.containsKey(specID)) {
            RdrSet ruleSet = new RdrSet(specID);                  // make a new set
            _ruleSets.put(specID, ruleSet) ;                      // & store it
        }
    }


    private void dump() {
        Iterator itr ;

        _log.info("##### BEGINNING DUMP OF WORKLET SERVICE DATA SETS #####");
        _log.info(" ");

        _log.info("1. Handled Parent Items");
        _log.info("-----------------------");
        iterateMap(_handledParentItems);
        _log.info(" ");

        _log.info("2. Handled Child Items");
        _log.info("----------------------");
        iterateMap(_handledWorkItems);
        _log.info(" ");

        _log.info("3. Cases Started");
        _log.info("----------------");
        iterateMap(_casesStarted);
        _log.info(" ");

        _log.info("4. Rule Sets");
        _log.info("------------");
        iterateMap(_ruleSets);
        _log.info(" ");

        _log.info("5. Loaded Specs");
        _log.info("---------------");
        if (_loadedSpecs.isEmpty()) _log.info("No items in list.");
        itr = _loadedSpecs.iterator();
        while (itr.hasNext()) _log.info(itr.next());
        _log.info(" ");

        _log.info("6. Admin Tasks");
        _log.info("---------------");
        if (_adminTasksMgr.getAllTasksAsList().isEmpty()) _log.info("No items in list.");
        itr = _adminTasksMgr.getAllTasksAsList().iterator();   // admin tasks
        while (itr.hasNext()) _log.info(itr.next());
        _log.info(" ");

        _log.info("##### COMPLETED DUMP OF WORKLET SERVICE DATA SETS #####");
    }


    private void iterateMap(HashMap map) {

        if (map != null) {
            ArrayList keys = new ArrayList(map.keySet());
            ArrayList values = new ArrayList(map.values());
            if (keys.isEmpty()) _log.info("No items in list.");

            for (int i=0; i < keys.size(); i++) {
                _log.info("KEY: " + keys.get(i).toString());
                _log.info("VALUE: " + values.get(i).toString());
            }
        }
    }

    private void checkCacheForWorkItem(WorkItemRecord wir) {
        WorkItemRecord wiTemp = getCachedWorkItem(wir.getID());
        if (wiTemp == null) {

            // if the item is not locally cached, it means a restore has occurred
            // after a checkout & the item is still checked out, so lets put it back
            // so that it can be checked back in
            getModel().addWorkItem(wir);
        }
    }

    public static String getXTypeString(int xType) {
       switch( xType ) {
           case XTYPE_CASE_PRE_CONSTRAINTS : return "Pre-case constraint violation" ;
           case XTYPE_CASE_POST_CONSTRAINTS : return "Post-case constraint violation";
           case XTYPE_ITEM_PRE_CONSTRAINTS : return "Workitem pre-constraint violation";
           case XTYPE_ITEM_POST_CONSTRAINTS : return "Workitem post-constraint violation";
           case XTYPE_WORKITEM_ABORT : return "Workitem abort";
           case XTYPE_TIMEOUT : return "Workitem timeout";
           case XTYPE_RESOURCE_UNAVAILABLE : return "Resource Unavailable";
           case XTYPE_CONSTRAINT_VIOLATION : return "Workitem constraint violation";
           case XTYPE_CASE_EXTERNAL_TRIGGER : return "Case-level external trigger";
           case XTYPE_ITEM_EXTERNAL_TRIGGER : return "Workitem-level external trigger";
           case XTYPE_SELECTION : return "Selection";
       }
       return null;
    }


    public static String getShortXTypeString(int xType) {
       switch( xType ) {
           case XTYPE_CASE_PRE_CONSTRAINTS : return "PreCaseConstraint" ;
           case XTYPE_CASE_POST_CONSTRAINTS : return "PostCaseConstraint";
           case XTYPE_ITEM_PRE_CONSTRAINTS : return "ItemPreConstraint";
           case XTYPE_ITEM_POST_CONSTRAINTS : return "ItemPostConstraint";
           case XTYPE_WORKITEM_ABORT : return "ItemAbort";
           case XTYPE_TIMEOUT : return "ItemTimeout";
           case XTYPE_RESOURCE_UNAVAILABLE : return "ResourceUnavailable";
           case XTYPE_CONSTRAINT_VIOLATION : return "ConstraintViolation";
           case XTYPE_CASE_EXTERNAL_TRIGGER : return "CaseExternal";
           case XTYPE_ITEM_EXTERNAL_TRIGGER : return "ItemExternal";
           case XTYPE_SELECTION : return "Selection";
       }
       return null;
    }


} // end of WorkletService class


