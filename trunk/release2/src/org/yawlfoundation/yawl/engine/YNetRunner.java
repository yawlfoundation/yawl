/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.announcement.Announcements;
import org.yawlfoundation.yawl.engine.announcement.CancelWorkItemAnnouncement;
import org.yawlfoundation.yawl.engine.announcement.NewWorkItemAnnouncement;
import org.yawlfoundation.yawl.engine.interfce.interfaceX.InterfaceX_EngineSideClient;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.net.URL;
import java.util.*;

/**
 *
 *
 * @author Lachlan Aldred
 *         Date: 16/04/2003
 *         Time: 16:08:01
 *
 */
public class YNetRunner // extends Thread
{
    private static final Logger logger = Logger.getLogger(YNetRunner.class);
	
	static final String INITIAL_VERSION = "0.1";     //MLR (31/10/07): added after merge

    protected YNet _net;
    private Set<YExternalNetElement> _enabledTasks = new HashSet<YExternalNetElement>();
    private Set<YExternalNetElement> _busyTasks = new HashSet<YExternalNetElement>();
    private Set _suspTasks = new HashSet();   // added

    private static YWorkItemRepository _workItemRepository = YWorkItemRepository.getInstance();
    private YIdentifier _caseIDForNet;
    private YCompositeTask _containingCompositeTask;
    private YEngine _engine;
    private boolean _cancelling;
                                                                 

    /**
     * **************************
     * INSERTED FOR PERSISTANCE
     * ***************************
     */
    protected String yNetID = null;       // name of the spec
    protected YSpecVersion _yNetVersion = new YSpecVersion(INITIAL_VERSION);  //MLF - version of the spec
    private Set<String> enabledTaskNames = new HashSet<String>();
    private Set<String> busyTaskNames = new HashSet<String>();
    private P_YIdentifier _standin_caseIDForNet;
    private String _caseID = null;
    private String containingTaskID = null;
    private YCaseData casedata = null;
    private YAWLServiceReference _caseObserver;
    private InterfaceX_EngineSideClient _exceptionObserver;

    // these members are used to persist observers
    private String _caseObserverStr = null ;
    private String _exceptionObserverStr = null ;


    public void restoreprepare() {
        _workItemRepository.setNetRunnerToCaseIDBinding(this, _caseIDForNet);
    }

    public void setTask(YCompositeTask task) {
        _containingCompositeTask = task;
    }

    public String getContainingTaskID() {
        return containingTaskID;
    }

    public void setContainingTaskID(String taskid) {
        containingTaskID = taskid;
    }

    public void setNet(YNet net) {
        _net = net;
        yNetID = net.getSpecification().getID();
        _net.restoreData(casedata);
    }

    public YNet getNet() {
        return _net;
    }

    public void setEngine(YEngine engine) {
        _engine = engine;
    }

    public void setYNetID(String id) {
        this.yNetID = id;
    }

    public String getYNetID() {
        return yNetID;
    }

    //BEGIN: MLF - Accessors for the version number of the spec this is running against
    public YSpecVersion getYNetVersion()
    {
        return _yNetVersion;
    }

    public void setYNetVersion(YSpecVersion yNetVersion)
    {
        this._yNetVersion = yNetVersion;
    }
    //END: MLF

    public YCaseData getCasedata() {
        return casedata;
    }

    public void setCasedata(YCaseData data) {
        casedata = data;
    }

    //todo Refactor this to use getCaseID()?  signed Lach
    public YIdentifier get_caseIDForNet() {
        return _caseIDForNet;
    }


    public void set_caseIDForNet(YIdentifier id) {
        this._caseIDForNet = id;
        _caseID = _caseIDForNet.toString();
    }

    public P_YIdentifier get_standin_caseIDForNet() {
        return _standin_caseIDForNet;
    }

    public void set_standin_caseIDForNet(P_YIdentifier id) {
        this._standin_caseIDForNet = id;
    }

    public void addBusyTask(YExternalNetElement ext) {
        _busyTasks.add(ext);
    }

    public void addEnabledTask(YExternalNetElement ext) {
        _enabledTasks.add(ext);
    }


    public String get_caseID() {
        return this._caseID;
    }

    public void set_caseID(String ID) {
        this._caseID = ID;
    }

    public Set<String> getEnabledTaskNames() {
        return enabledTaskNames;
    }

    public Set<String> getBusyTaskNames() {
        return busyTaskNames;
    }
    /************************************************/


    /**
     * Needed for hibernate to work.
     */
    protected YNetRunner() {
        logger.debug("YNetRunner: <init>");

        // MJF: also needed for persistence
        try {
       	    _engine = YEngine.getInstance(YEngine.isPersisting());
        } catch (YPersistenceException e) {
        	  logger.error(getClass().getCanonicalName(), e);
        }
    }

    public YNetRunner(YPersistenceManager pmgr, YNet netPrototype, Element paramsData,
                      String caseID) throws YDataStateException, YSchemaBuildingException,
                                            YPersistenceException {

        _caseIDForNet = caseID == null ? new YIdentifier() : new YIdentifier(caseID);
        _caseID = _caseIDForNet.toString();

        if (pmgr != null) pmgr.storeObject(_caseIDForNet);         // persist case id

        _net = (YNet) netPrototype.clone();
        casedata = new YCaseData(_caseID);
        _net.initializeDataStore(pmgr, casedata);
        _engine = YEngine.getInstance();
        yNetID = netPrototype.getSpecification().getID();
        _yNetVersion = netPrototype.getSpecification().getMetaData().getVersion();

        prepare(pmgr);
        if (paramsData != null) _net.setIncomingData(pmgr, paramsData);

    }


    public YNetRunner(YPersistenceManager pmgr, YNet netPrototype,
                      YCompositeTask container, YIdentifier caseIDForNet,
                      Element incomingData)
            throws YDataStateException, YSchemaBuildingException, YPersistenceException {

        _caseIDForNet = caseIDForNet;
        _caseID = _caseIDForNet.toString();
        _net = (YNet) netPrototype.clone();
        casedata = new YCaseData(_caseID);
        _net.initializeDataStore(pmgr, casedata);
        _containingCompositeTask = container;
        _engine = YEngine.getInstance();
        yNetID = netPrototype.getSpecification().getID();
        setContainingTaskID(container.getID());
        prepare(pmgr);
        _net.setIncomingData(pmgr, incomingData);

        if (pmgr != null) pmgr.storeObject(this);
    }


    private void prepare(YPersistenceManager pmgr) throws YPersistenceException {
        _workItemRepository.setNetRunnerToCaseIDBinding(this, _caseIDForNet);
        YInputCondition inputCondition = _net.getInputCondition();
        inputCondition.add(pmgr, _caseIDForNet);
        _net.initialise(pmgr);
    }


    public void start(YPersistenceManager pmgr) throws YPersistenceException, YDataStateException, YSchemaBuildingException, YQueryException, YStateException {
        kick(pmgr);
    }


    public boolean isAlive() {
        return ! _cancelling;
    }

    /**
     * Assumption: this will only get called AFTER a workitem has been progressed?
     * Because if it is called any other time then it will cause the case to stop.
     * @param pmgr
     * @throws YPersistenceException
     */
    public void kick(YPersistenceManager pmgr) throws YPersistenceException, YDataStateException, YSchemaBuildingException, YQueryException, YStateException {
        logger.debug("--> YNetRunner.kick");

        boolean progressed = continueIfPossible(pmgr);

        if (!progressed) {
            logger.debug("YNetRunner not able to continue");
            if (_engine != null) {
                //case completion
                if (_caseObserver != null)
                    _engine.announceCaseCompletionToEnvironment(_caseObserver,
                                      _caseIDForNet, _net.getOutputData());
                else
                    _engine.announceCaseCompletionToEnvironment(_caseIDForNet,
                                                                _net.getOutputData());

                // notify exception checkpoint to service if available (post's for case end)
                if (_exceptionObserver != null) {
                    Document data = _net.getInternalDataDocument();
                    _engine.announceCheckCaseConstraints(_exceptionObserver, null, _caseID,
                                       JDOMUtil.documentToString(data), false);
                }

                logger.debug("Asking engine to finish case");
                _engine.finishCase(pmgr, _caseIDForNet);

                if (isRootNet()) {
                    try {
                        _engine.clearCase(pmgr, _caseIDForNet);
                    } catch (Exception e) {
                        //todo So, what shall we do now ????
                    }
                }
            }
            if (!_cancelling && deadLocked()) {
                notifyDeadLock(pmgr);
            }
            cancel(pmgr);
        }
        logger.debug("<-- YNetRunner.kick");
    }

    private boolean isRootNet() {
        return _caseIDForNet.getParent() == null;
    }


    private void notifyDeadLock(YPersistenceManager pmgr) throws YPersistenceException {
        List locations = _caseIDForNet.getLocations();
        for (int i = 0; i < locations.size(); i++) {
            YNetElement element = (YNetElement) locations.get(i);
            if (_net.getNetElements().values().contains(element)) {
                if (element instanceof YTask) {
                    createDeadlockItem(pmgr, (YTask) element);
                }
                Set postset = ((YExternalNetElement) element).getPostsetElements();
                if (postset.size() > 0) {
                    for (Iterator iterator = postset.iterator(); iterator.hasNext();) {
                        YExternalNetElement postsetElement = (YExternalNetElement) iterator.next();
                        createDeadlockItem(pmgr, postsetElement);
                    }
                }
            }
        }
    }


    private void createDeadlockItem(YPersistenceManager pmgr, YExternalNetElement netElement) throws YPersistenceException {
        YSpecificationID specificationID = _net.getSpecification().getSpecificationID();
        boolean allowsNewInstances = false;
        boolean isDeadlocked = true;
        YWorkItemID deadlockWorkItemID = new YWorkItemID(_caseIDForNet,
                netElement.getID());
        new YWorkItem(pmgr, specificationID,
                deadlockWorkItemID,
                allowsNewInstances,
                isDeadlocked);
        //Log to Problems table of database.
        YProblemEvent event  = new YProblemEvent(_net, "Deadlocked", YProblemEvent.RuntimeError);
        event.logProblem(pmgr);
    }


    /**
     * Assumption: there are no enabled tasks
     * @return if deadlocked
     */
    private boolean deadLocked() {
        List locations = _caseIDForNet.getLocations();
        for (int i = 0; i < locations.size(); i++) {
            Object o = locations.get(i);
            if(o instanceof YExternalNetElement) {
                YExternalNetElement element = (YExternalNetElement) o;
                if (element.getPostsetElements().size() > 0) {
                    return true;
                }
            }
        }
        return false;
    }


    private synchronized void processCompletedSubnet(YPersistenceManager pmgr,
                           YIdentifier caseIDForSubnet, YCompositeTask busyCompositeTask,
                           Document rawSubnetData)
            throws YDataStateException, YStateException, YQueryException,
                   YSchemaBuildingException, YPersistenceException {

        logger.debug("--> processCompletedSubnet");

        if (caseIDForSubnet == null) throw new RuntimeException();
        
        if (busyCompositeTask.t_complete(pmgr, caseIDForSubnet, rawSubnetData)) {
            _busyTasks.remove(busyCompositeTask);

            //check to see if completing this task resulted in completing the net.
            if (this.isCompleted() && _net.getOutputCondition().getIdentifiers().size() == 1) {

                if (_containingCompositeTask != null) {
                    YNetRunner parentRunner = _workItemRepository.getNetRunner(_caseIDForNet.getParent());
                    if (parentRunner != null) {
                        synchronized (parentRunner) {

                           // MJF: Added below to avoid NPE
                            parentRunner.setEngine(_engine);

                            if (_containingCompositeTask.t_isBusy()) {

                                Document dataDoc = _net.usesSimpleRootData() ?
                                                   _net.getInternalDataDocument() :
                                                   _net.getOutputData() ;

                                parentRunner.processCompletedSubnet(pmgr,
                                            _caseIDForNet,
                                            _containingCompositeTask,
                                            dataDoc);
 
                            }
                        }
                    }
                // todo: this is always false - remove after testing
                }
//                else if (_engine != null && _containingCompositeTask != null) {
//                    _engine.finishCase(pmgr, _caseIDForNet);
//                }
            }


            if (pmgr != null) pmgr.updateObject(this);

            kick(pmgr);
        }

        logger.debug("<-- processCompletedSubnet");
    }


    public List attemptToFireAtomicTask(YPersistenceManager pmgr, String taskID)
            throws YDataStateException, YStateException, YQueryException, YPersistenceException, YSchemaBuildingException {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        if (task.t_enabled(_caseIDForNet)) {
            List newChildIdentifiers = task.t_fire(pmgr);
            _enabledTasks.remove(task);

            //todo AJH - Why persist twice here ??????
            /*
             INSERTED FOR PERSISTANCE
            */
            enabledTaskNames.remove(task.getID());
//            YPersistance.getInstance().updateData(this);
            if (pmgr != null) {
                pmgr.updateObject(this);
            }

            /**********************/
            _busyTasks.add(task);
            /*
  INSERTED FOR PERSISTANCE
 */
            busyTaskNames.add(task.getID());
//            YPersistance.getInstance().updateData(this);
            if (pmgr != null) {
                pmgr.updateObject(this);
            }

            /******************************/
            synchronized (this) {
                logger.debug("NOTIFYING RUNNER");
//                notify();
                kick(pmgr);
            }
            return newChildIdentifiers;
        }
        return null;
    }


    public synchronized YIdentifier addNewInstance(YPersistenceManager pmgr, String taskID, YIdentifier aSiblingInstance, Element newInstanceData)
            throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        if (task.t_isBusy()) {
            return task.t_add(pmgr, aSiblingInstance, newInstanceData);
        }
        return null;
    }


    public synchronized void startWorkItemInTask(YPersistenceManager pmgr, YIdentifier caseID, String taskID) throws YDataStateException, YSchemaBuildingException, YPersistenceException, YQueryException, YStateException {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        task.t_start(pmgr, caseID);
    }


    public synchronized boolean completeWorkItemInTask(YPersistenceManager pmgr, YWorkItem workItem, YIdentifier caseID, String taskID, Document outputData)
            throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        logger.debug("--> completeWorkItemInTask");
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
            boolean success = completeTask(pmgr, workItem, task, caseID, outputData);

            // notify exception checkpoint to service if available
            if (_exceptionObserver != null)
                _engine.announceCheckWorkItemConstraints(_exceptionObserver, workItem, outputData, false);

            logger.debug("<-- completeWorkItemInTask");
            return success;
    }


    public synchronized boolean continueIfPossible(YPersistenceManager pmgr)
                           throws YDataStateException, YStateException, YQueryException,
                                  YSchemaBuildingException, YPersistenceException {
        logger.debug("--> continueIfPossible");

        // AJH: Check if we are suspending (or suspended?) and if so exit out as we
        // shouldn't post new workitems
        if ((getCasedata().getExecutionState() == YCaseData.SUSPEND_STATUS_SUSPENDING) ||
            (getCasedata().getExecutionState() == YCaseData.SUSPEND_STATUS_SUSPENDED)) {
            logger.debug("Aborting runner continuation as case is currently suspending/suspended");
            return true;
        }

        // storage for the running set of enabled tasks
        YEnabledTransitionSet enabledTransitions = new YEnabledTransitionSet();

        // storage for service cancellation announcements
        Announcements<CancelWorkItemAnnouncement> announceCancel =
                                          new Announcements<CancelWorkItemAnnouncement>();
        CancelWorkItemAnnouncement announcement;
        
        // iterate through the full set of elements for the net
        List tasks = new ArrayList(_net.getNetElements().values());
        Iterator tasksIter = tasks.iterator();
        while (tasksIter.hasNext()) {
            YExternalNetElement netElement = (YExternalNetElement) tasksIter.next();
            if (netElement instanceof YTask) {
                YTask task = (YTask) netElement;

                // if this task is an enabled 'transition'
                if (task.t_enabled(_caseIDForNet)) {
                    if (! (_enabledTasks.contains(task) || _busyTasks.contains(task)))
                        enabledTransitions.add(task) ;
                }
                else {

                    // if the task is not an enabled transition, and its been enabled
                    // by the engine, then it must be cancelled
                    if (_enabledTasks.contains(task)) {
                        announcement = cancelEnabledTask(task, pmgr) ;
                        if (announcement != null)
                            announceCancel.addAnnouncement(announcement);
                    }
                }

                if (task.t_isBusy() && !_busyTasks.contains(task)) {
                    logger.error("Throwing RTE for lists out of sync");
                    throw new RuntimeException("busy task list out of synch with a busy task: "
                                           + task.getID() + " busy tasks: " + _busyTasks);
                }
            }
        }

        // fire the set of enabled 'transitions' (if any)
        if (! enabledTransitions.isEmpty()) fireTasks(enabledTransitions, pmgr);

        // announce the cancellations (if any)
        if (announceCancel.size() > 0)
            _engine.announceCancellationToEnvironment(announceCancel);

        _busyTasks = _net.getBusyTasks();

        logger.debug("<-- continueIfPossible");
        return _enabledTasks.size() > 0 || _busyTasks.size() > 0;
    }


    private void fireTasks(YEnabledTransitionSet enabledSet, YPersistenceManager pmgr)
                              throws YDataStateException, YStateException, YQueryException,
                                     YSchemaBuildingException, YPersistenceException {

        Announcements<NewWorkItemAnnouncement> announceNew =
                                          new Announcements<NewWorkItemAnnouncement>();
        NewWorkItemAnnouncement announcement ;

        Set<YTask> enabledTasks = new HashSet<YTask>() ;
        List<YEnabledTransitionSet.TaskGroup> taskGroups = enabledSet.getEnabledTaskGroups();
        for (YEnabledTransitionSet.TaskGroup group : taskGroups) {
            if (group.hasEnabledCompositeTasks()) {
                YCompositeTask composite = group.getRandomCompositeTaskFromTaskGroup();
                if (! enabledTasks.contains(composite)) {
                    fireCompositeTask(composite, pmgr) ;
                    enabledTasks.add(composite) ;
                }
            }
            else {
                List<YAtomicTask> taskList = group.getEnabledAtomicTasks();
                String groupID = taskList.size() > 1 ? group.getID() : null;
                for (YAtomicTask atomic : taskList) {
                    if (! enabledTasks.contains(atomic)) {
                        announcement = fireAtomicTask(atomic, groupID, pmgr) ;
                        if (announcement != null)
                            announceNew.addAnnouncement(announcement);
                        enabledTasks.add(atomic) ;
                    }
                }
            }
        }
        if (announceNew.size() > 0) _engine.announceTasks(announceNew);
    }


    private NewWorkItemAnnouncement fireAtomicTask(YAtomicTask task, String groupID,
                                                   YPersistenceManager pmgr)
                           throws YDataStateException, YStateException, YQueryException,
                                  YSchemaBuildingException, YPersistenceException {

        NewWorkItemAnnouncement announcement = null ;
        YAWLServiceGateway wsgw = (YAWLServiceGateway) task.getDecompositionPrototype();

        // if its not an empty task
        if (wsgw != null) {
            YWorkItem item = createEnabledWorkItem(pmgr, _caseIDForNet, task);
            if (groupID != null) item.setDeferredChoiceGroupID(groupID);
            YAWLServiceReference ys = wsgw.getYawlService();
            if (ys != null)
                announcement = new NewWorkItemAnnouncement(ys, item,
                                                      _engine.getAnnouncementContext());
            else
                _engine.announceEnabledTaskToResourceService(item);

            if (_exceptionObserver != null)
                _engine.announceCheckWorkItemConstraints(_exceptionObserver, item,
                                                 _net.getInternalDataDocument(), true);

            _enabledTasks.add(task);
            enabledTaskNames.add(task.getID());
            if (pmgr != null)  pmgr.updateObject(this);
        }
        else {                                             //fire the empty atomic task
            YIdentifier id = (YIdentifier) task.t_fire(pmgr).iterator().next();
            task.t_start(pmgr, id);
            completeTask(pmgr, null, task, id, null);
        }
        return announcement;
    }


    private void fireCompositeTask(YCompositeTask task, YPersistenceManager pmgr)
                      throws YDataStateException, YStateException, YQueryException,
                             YSchemaBuildingException, YPersistenceException {
        _busyTasks.add(task);
        busyTaskNames.add(task.getID());
        if (pmgr != null) pmgr.updateObject(this);

        Iterator caseIDs = task.t_fire(pmgr).iterator();
        while (caseIDs.hasNext()) {
            YIdentifier id = (YIdentifier) caseIDs.next();
            task.t_start(pmgr, id);
        }
    }

    private CancelWorkItemAnnouncement cancelEnabledTask(YTask task, YPersistenceManager pmgr)
                      throws YDataStateException, YStateException, YQueryException,
                             YSchemaBuildingException, YPersistenceException {

        CancelWorkItemAnnouncement result = null;
        _enabledTasks.remove(task);
        enabledTaskNames.remove(task.getID());

        //  remove the cancelled task from persistence
        YWorkItem wItem = _workItemRepository.getWorkItem(_caseID, task.getID());
        if (wItem != null) {               //may already have been removed by task.cancel

            //MLF: announce all cancelled work items (maybe subset to enabled/fired...?)
            if (task.getDecompositionPrototype() != null) {
                YAWLServiceGateway wsgw = (YAWLServiceGateway) task.getDecompositionPrototype();
                if (wsgw != null) {
                    YAWLServiceReference ys = wsgw.getYawlService();
                    if (ys != null)
                        result = new CancelWorkItemAnnouncement(ys, wItem);
                    else
                        _engine.announceCancelledTaskToResourceService(wItem);

                }
            }
            if (pmgr != null) {
               pmgr.deleteObject(wItem);
               pmgr.updateObject(this);
            }
        }
        return result;
    }


    /**
     * Creates an enabled work item.
     *
     * @param caseIDForNet the caseid for the net
     * @param atomicTask   the atomic task that contains it.
     */
    private YWorkItem createEnabledWorkItem(YPersistenceManager pmgr, YIdentifier caseIDForNet, YAtomicTask atomicTask) throws YPersistenceException, YDataStateException, YSchemaBuildingException, YQueryException, YStateException {
        logger.debug("--> createEnabledWorkItem: Case=" + caseIDForNet.get_idString() + " Task=" + atomicTask.getID());

        boolean allowDynamicCreation =
                atomicTask.getMultiInstanceAttributes() == null ? false :
                YMultiInstanceAttributes._creationModeDynamic.equals(atomicTask.getMultiInstanceAttributes().getCreationMode());

        //creating a new work item puts it into the work item
        //repository automatically.
        YWorkItem workItem = new YWorkItem(pmgr,
                atomicTask.getNet().getSpecification().getSpecificationID(),
                new YWorkItemID(caseIDForNet, atomicTask.getID()),
                allowDynamicCreation, false);

        if (atomicTask.getDataMappingsForEnablement().size() > 0) {
            Element data = atomicTask.prepareEnablementData();            
			workItem.setData(pmgr, data);
        }

        // copy in relevant data from the task's decomposition
        YDecomposition decomp = atomicTask.getDecompositionPrototype();
        if (decomp != null) {
            workItem.setRequiresManualResourcing(decomp.requiresResourcingDecisions());
            workItem.setCodelet(decomp.getCodelet());
            workItem.setAttributes(decomp.getAttributes());
        }

        // set timer params and start timer if required
        Map timerParams = atomicTask.getTimeParameters();
        if (timerParams != null) {
            workItem.setTimerParameters(timerParams);
            workItem.checkStartTimer(pmgr, casedata);
        }

        // set custom form for workitem if specified
        URL customFormURL = atomicTask.getCustomFormURL();
        if (customFormURL != null)
            workItem.setCustomFormURL(customFormURL) ;
        
        return workItem;
    }


    /**
     * Completes a work item inside an atomic task.
     *
     * @param workItem The work item. If null is supplied, this work item cannot be removed from the work items repository (hack)
     * @param atomicTask the atomic task
     * @param identifier the identifier of the work item
     * @param outputData the document containing output data
     * @return whether or not the task exited
     * @throws YDataStateException
     */
    private boolean completeTask(YPersistenceManager pmgr, YWorkItem workItem, YAtomicTask atomicTask, YIdentifier identifier, Document outputData)
            throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
       // logger.debug("--> completeTask");

        boolean taskExited;

        taskExited = atomicTask.t_complete(pmgr, identifier, outputData);
        if (taskExited) {
            //todo AJH New code here - Relocated from YEngine.completeWorkItem
            if (workItem != null) {
                _engine.getWorkItemRepository().removeWorkItemFamily(workItem);
            }

            //here are check to see if completing this task resulted in completing the net.
            if (this.isCompleted() && _net.getOutputCondition().getIdentifiers().size() == 1) {
                //so now we know the net is complete we check if this net is a subnet.
                if (_containingCompositeTask != null) {
                    YNetRunner parentRunner = _workItemRepository.getNetRunner(_caseIDForNet.getParent());
                    if (parentRunner != null) {
                        synchronized (parentRunner) {
                            if (_containingCompositeTask.t_isBusy()) {

                                Document dataDoc = _net.usesSimpleRootData() ?
                                                   _net.getInternalDataDocument() :
                                                   _net.getOutputData() ;

                                parentRunner.processCompletedSubnet(pmgr,
                                            _caseIDForNet,
                                            _containingCompositeTask,
                                            dataDoc);

                                if (_caseIDForNet == null) {
                                    logger.debug("YNetRunner::completeTask() finished local task: " +
                                            atomicTask + " composite task: " +
                                            _containingCompositeTask +
                                            " caseid for decomposed net: " +
                                            _caseIDForNet);
                                }
                            }
                        }
                    }
                }
            }

            continueIfPossible(pmgr);
            _busyTasks.remove(atomicTask);
            /*
              INSERTED FOR PERSISTANCE
             */
            busyTaskNames.remove(atomicTask.getID());
//            YPersistance.getInstance().updateData(this);

            if (pmgr != null) {
                ArrayList liveCases = (ArrayList) _engine.getRunningCaseIDs();
                if (liveCases.indexOf(_caseIDForNet) > -1) {
                    pmgr.updateObject(this);
                }
            }

            /**************************/
            logger.debug("NOTIFYING RUNNER");
            //todo Removing this causes sequence problems when going cyclic
            kick(pmgr);
        }
        logger.debug("<-- completeTask: Exited=" + taskExited);
        return taskExited;
    }


    public synchronized void cancel(YPersistenceManager pmgr) throws YPersistenceException {
        logger.debug("--> NetRunner cancel " + this.getCaseID().get_idString());

        _cancelling = true;
        Collection netElements = _net.getNetElements().values();
        Iterator iterator = netElements.iterator();
        while (iterator.hasNext()) {
            YExternalNetElement netElement = (YExternalNetElement) iterator.next();
            if (netElement instanceof YTask) {
                YTask task = ((YTask) netElement);
                if (task.t_isBusy()) {
                    task.cancel(pmgr);
                }
            } else if (((YCondition) netElement).containsIdentifier()) {
                ((YCondition) netElement).removeAll(pmgr);
            }
        }
        _workItemRepository.cancelNet(_caseIDForNet);
        _enabledTasks = new HashSet();
        _busyTasks = new HashSet();

//        _cancelling = false;
    }


    public synchronized boolean rollbackWorkItem(YPersistenceManager pmgr, YIdentifier caseID, String taskID) throws YPersistenceException {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        return task.t_rollBackToFired(pmgr, caseID);
    }


    //###############################################################################
    //                              accessors
    //###############################################################################
    public YExternalNetElement getNetElement(String id) {
        return _net.getNetElement(id);
    }


    public YIdentifier getCaseID() {
        return _caseIDForNet;
    }


    public boolean isCompleted() {
        if (_net.getOutputCondition().containsIdentifier()) {
            return true;
        } else {
            return isEmpty();
        }
    }


    public boolean isEmpty() {
        Iterator elements = _net.getNetElements().values().iterator();
        while (elements.hasNext()) {
            YExternalNetElement element = (YExternalNetElement) elements.next();
            if (element instanceof YCondition) {
                if (((YCondition) element).containsIdentifier()) {
                    return false;
                }
            } else {
                if (((YTask) element).t_isBusy()) {
                    return false;
                }
            }
        }
        return true;
    }


    protected Set getBusyTasks() {
        return _busyTasks;
    }


    protected Set getEnabledTasks() {
        return _enabledTasks;
    }


    public boolean isAddEnabled(String taskID, YIdentifier childID) {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        return task.t_addEnabled(childID);
    }

    public void setObserver(YAWLServiceReference observer) {
        _caseObserver = observer;
        _caseObserverStr = observer.getURI();                       // for persistence
    }

    public void announceToEnvironment(YWorkItem workitem)
    {
        logger.debug("--> announceToEnvironment");

        YTask task = _engine.getTaskDefinition(workitem.getSpecificationID(), workitem.getTaskID());
        YAtomicTask atomicTask = (YAtomicTask)task;
        YAWLServiceGateway wsgw = (YAWLServiceGateway)atomicTask.getDecompositionPrototype();
        if (wsgw != null)
        {
            YAWLServiceReference ys = wsgw.getYawlService();
            if (ys != null) {
                YWorkItem item = _workItemRepository.getWorkItem(_caseIDForNet.toString(), atomicTask.getID());
                if(item == null) throw new RuntimeException("Unable to find YWorKItem for atomic task '" + atomicTask.getID() + "' of case '" + _caseIDForNet + "'."); //todo should this be named
                if(item.getStatus() == YWorkItemStatus.statusIsParent) item.add_child(workitem);  //MLF: restore the linkage
                try
                {
                    Announcements<NewWorkItemAnnouncement> items = new Announcements<NewWorkItemAnnouncement>();
                    items.addAnnouncement(new NewWorkItemAnnouncement(ys, item, _engine.getAnnouncementContext()));
                    _engine.announceTasks(items);
                }
                catch (YStateException e)
                {
                    logger.error("Failed to announce task '" + atomicTask.getID() + "' of case '" + _caseIDForNet + "': ", e);
                }
            }
            else logger.warn("No YawlService defined, unable to announce task '" + atomicTask.getID() + "' of case '" + _caseIDForNet + "'."); //MLF added task info
        }
        else logger.warn("No YAWLServiceGateway defined, unable to announce tasl '" + atomicTask.getID() + "' of case '" + _caseIDForNet + "'."); //MLF added task info

        logger.debug("<-- announceToEnvironment");
    }

    public void dump()
    {
        dump(logger);
    }

    public void dump(Logger logger)
    {
        logger.debug("*** DUMP OF NETRUNNER ENABLED TASKS ***");

        Iterator iter = _enabledTasks.iterator();
        while(iter.hasNext())
        {
            Object obj = iter.next();
            logger.debug("Type = " + obj.getClass().getName());
        }

        logger.debug("*** END OF DUMP OF NETRUNNER ENABLED TASKS ***");

        logger.debug("*** DUMP OF NETRUNNER BUSY TASKS ***");

        iter = _busyTasks.iterator();
        while(iter.hasNext())
        {
            Object obj = iter.next();
            logger.debug("Type = " + obj.getClass().getName());
        }

        logger.debug("*** END OF DUMP OF NETRUNNER BUSY TASKS ***");
    }

    /***************************************************************************/
    /** The following methods have been added to support the exception service */

    /** sets the IX observer to the specified IX client */
    public void setExceptionObserver(InterfaceX_EngineSideClient observer){
        _exceptionObserver = observer;
        _exceptionObserverStr = observer.getURI();                  // for persistence
     }


    /** restores the IB and IX observers on session startup (via persistence) */
    public void restoreObservers() {
        if(_caseObserverStr != null) {
            YAWLServiceReference caseObserver =
                                    _engine.getRegisteredYawlService(_caseObserverStr);
            if (caseObserver != null) setObserver(caseObserver);
        }
        if (_exceptionObserverStr != null) {
            InterfaceX_EngineSideClient exObserver =
                                new InterfaceX_EngineSideClient(_exceptionObserverStr);
            setExceptionObserver(exObserver);
            _engine.setExceptionObserver(_exceptionObserverStr);
        }
    }


   /** these four methods are here to support persistence of the IB and IX Observers */
    private String get_caseObserverStr() { return _caseObserverStr ; }

    private String get_exceptionObserverStr() { return _exceptionObserverStr ; }

    private void set_caseObserverStr(String obStr) { _caseObserverStr = obStr ; }

    private void set_exceptionObserverStr(String obStr) { _exceptionObserverStr = obStr ; }


    /** cancels the specified task */
    public void cancelTask(YPersistenceManager pmgr, String taskID) {
        YAtomicTask task = (YAtomicTask) getNetElement(taskID);

        try {
            task.cancel(pmgr, this.getCaseID());
            _busyTasks.remove(task);
            busyTaskNames.remove(task.getID());
        }
        catch (YPersistenceException ype) {
            logger.fatal("Failure whilst cancelling task: " + taskID, ype);

        }
    }


    /** returns true if the specified workitem is registered with the Time Service */
    public boolean isTimeServiceTask(YWorkItem item) {
        YTask task = (YTask) getNetElement(item.getTaskID());
        if ((task != null) && (task instanceof YAtomicTask)) {
            YAWLServiceGateway wsgw = (YAWLServiceGateway) task.getDecompositionPrototype();
            if (wsgw != null) {
                YAWLServiceReference ys = wsgw.getYawlService();
                if (ys != null)
                   return ys.get_yawlServiceID().indexOf("timeService") > -1 ;
            }
        }
        return false ;
    }


    /** returns a list of all workitems executing in parallel to the time-out
        workitem passed (the list includes the time-out task) */
    public List getTimeOutTaskSet(YWorkItem item) {
        YTask timeOutTask = (YTask) getNetElement(item.getTaskID());
        String nextTaskID = getFlowsIntoTaskID(timeOutTask);
        ArrayList result = new ArrayList() ;

        if (nextTaskID != null) {
            List netTasks = new ArrayList(getNet().getNetElements().values());
            Iterator itr = netTasks.iterator();
            while (itr.hasNext()){
               YNetElement yne = (YNetElement) itr.next();
               if (yne instanceof YTask) {
                   YTask task = (YTask) yne;
                   String nextTask = getFlowsIntoTaskID(task);
                   if (nextTask != null) {
                       if (nextTask.equals(nextTaskID))
                          result.add(task.getID());
                   }
               }
            }
        }
        if (result.isEmpty()) result = null ;
        return result;
    }


    /** returns the task id of the task that the specifiedtasks flows into
        In other words, gets the id of the next task in the process flow */
    private String getFlowsIntoTaskID(YTask task) {
        if ((task != null) && (task instanceof YAtomicTask)) {
            Element eTask = JDOMUtil.stringToElement(task.toXML());
            return eTask.getChild("flowsInto").getChild("nextElementRef").getAttributeValue("id");
        }
        return null ;
    }


    /****************************************************************************/

    // For Hibernate //

    public String get_yNetVersion() {
        return _yNetVersion.toString();
    }

    public void set_yNetVersion(String version) {
        this._yNetVersion = new YSpecVersion(version);
    }
}


