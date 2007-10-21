/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import au.edu.qut.yawl.elements.*;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.exceptions.*;
import au.edu.qut.yawl.engine.interfce.interfaceX.InterfaceX_EngineSideClient;
import au.edu.qut.yawl.util.JDOMConversionTools;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

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
    private static Logger logger;

    protected YNet _net;
    private Set _enabledTasks = new HashSet();
    private Set _busyTasks = new HashSet();
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
    protected String yNetID = null;
    private Set enabledTaskNames = new HashSet();
    private Set busyTaskNames = new HashSet();
    private P_YIdentifier _standin_caseIDForNet;
    private String _caseID = null;
    private String containingTaskID = null;
    private YCaseData casedata = null;
    private YAWLServiceReference _caseObserver;
    private InterfaceX_EngineSideClient _exceptionObserver;

    // inserted to persist observers
    private String _caseObserverStr = null ;
    private String _exceptionObserverStr = null ;

    /*****************************/

    /*************************************/
    /*INSERTED METHODS*/
    /**
     * *********************************
     */

    public void restoreprepare() {
        _workItemRepository.setNetRunnerToCaseIDBinding(this, _caseIDForNet);
        //_net.initialise();
    }

    public void setTask(YCompositeTask task) {
        this._containingCompositeTask = task;
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

    public Set getEnabledTaskNames() {
        return enabledTaskNames;
    }

    public Set getBusyTaskNames() {
        return busyTaskNames;
    }
    /************************************************/


    /**
     * Needed for hibernate to work.
     */
    private YNetRunner() {
        logger = Logger.getLogger(this.getClass());
        logger.debug("YNetRunner: <init>");
    }


    public YNetRunner(YPersistenceManager pmgr, YNet netPrototype, Element paramsData) throws YDataStateException, YSchemaBuildingException, YPersistenceException {
//        super("NetRunner:" + netPrototype.getID());
        _caseIDForNet = new YIdentifier();
        /*
        INSERTED FOR PERSISTANCE
        */
//        YPersistance.getInstance().storeData(_caseIDForNet);
        if (pmgr != null) {
            pmgr.storeObject(_caseIDForNet);
        }
        _caseID = _caseIDForNet.toString();
        /*****************************/

        _net = (YNet) netPrototype.clone();

        /*
  INSERTED FOR PERSISTANCE
 */
        casedata = new YCaseData();
        casedata.setId(_caseID);
        _net.initializeDataStore(pmgr, casedata);
        /*************************************/

        _engine = YEngine.getInstance();
        /*
          INSERTED FOR PERSISTANCE
         */
        yNetID = netPrototype.getSpecification().getID();
        /*****************************/

        prepare(pmgr);
        if(paramsData != null) {
            _net.setIncomingData(pmgr, paramsData);
        }
    }


    public YNetRunner(YPersistenceManager pmgr, YNet netPrototype, YCompositeTask container, YIdentifier caseIDForNet, Element incomingData) throws YDataStateException, YSchemaBuildingException, YPersistenceException {
//        super("NetRunner:" + netPrototype.getID());
        _caseIDForNet = caseIDForNet;
        _caseID = _caseIDForNet.toString();
        /*****************************/
        _net = (YNet) netPrototype.clone();
        /*
  INSERTED FOR PERSISTANCE
*/
        casedata = new YCaseData();
        casedata.setId(_caseID);
        _net.initializeDataStore(pmgr, casedata);
        /*****************************/
        _containingCompositeTask = container;
        _engine = YEngine.getInstance();

        /*
          INSERTED FOR PERSISTANCE
         */
        yNetID = netPrototype.getSpecification().getID();
        setContainingTaskID(container.getID());
        /******************************/
        prepare(pmgr);
        _net.setIncomingData(pmgr, incomingData);

        if (pmgr != null) {
            pmgr.storeObject(this);
        }
    }


    private void prepare(YPersistenceManager pmgr) throws YPersistenceException {
        _workItemRepository.setNetRunnerToCaseIDBinding(this, _caseIDForNet);
        YInputCondition inputCondition = _net.getInputCondition();
        inputCondition.add(pmgr, _caseIDForNet);
        _net.initialise(pmgr);

        /*
        Uncommented for persistance
        */
        //continueIfPossible();
    }


    /**
     * COPIED INTO KICK METHOD !!!
     */
    /*
    public void run()
    {
        logger = Logger.getLogger(this.getClass());
        logger.debug("--> YNetRunner.run");

        while (continueIfPossible())
        {
            try
            {
                synchronized (this)
                {
                    Logger.getLogger(this.getClass()).debug("THREAD WAITING ...");
                    wait();
                    Logger.getLogger(this.getClass()).debug("THREAD KICKED");
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        if (_engine != null)
        {
            Logger.getLogger(this.getClass()).debug("Asking engine to finish case");
            _engine.finishCase(_caseIDForNet);

            if (_caseIDForNet.toString().indexOf(".") == -1)
            {
                try
                {
                    YPersistance.getInstance().clearCase(_caseIDForNet);
                }
                catch (Exception e)
                {
                    //todo So, what shall we do now ????
                }
            }

        }

        _workItemRepository.cancelNet(_caseIDForNet);
        if (!_cancelling && deadLocked())
        {
            notifyDeadLock();
        }
        cancel();

        logger.debug("<-- YNetRunner.run");

    }
    */

    public void start(YPersistenceManager pmgr) throws YPersistenceException {
        kick(pmgr);
    }


    public boolean isAlive() {
        if (_cancelling) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Assumption: this will only get called AFTER a workitem has been progressed?
     * Because if it is called any other time then it will cause the case to stop.
     * @param pmgr
     * @throws YPersistenceException
     */
    public void kick(YPersistenceManager pmgr) throws YPersistenceException {
        logger = Logger.getLogger(this.getClass());
        logger.debug("--> YNetRunner.kick");

        boolean progressed = continueIfPossible(pmgr);

        if (!progressed) {
            logger.debug("YNetRunner not able to continue");
            if (_engine != null) {
                //case completion
                if (_caseObserver != null) {
                    _engine.announceCaseCompletionToEnvironment(_caseObserver,
                                      _caseIDForNet, _net.getOutputData());
                }

                // notify exception checkpoint to service if available (post's for case end)
                if (_exceptionObserver != null) {
                    Document data = _net.getInternalDataDocument();
                    _engine.announceCheckCaseConstraints(_exceptionObserver, null, _caseID,
                                       JDOMConversionTools.documentToString(data), false);
                }

                Logger.getLogger(this.getClass()).debug("Asking engine to finish case");
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
        String specificationID = _net.getSpecification().getID();
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


    private synchronized void processCompletedSubnet(YPersistenceManager pmgr, YIdentifier caseIDForSubnet, YCompositeTask busyCompositeTask,
                                                     Document rawSubnetData)
            throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        Logger.getLogger(this.getClass()).debug("--> processCompletedSubnet");

        if (caseIDForSubnet == null) {
            throw new RuntimeException();
        }
        boolean compositeTaskExited = busyCompositeTask.t_complete(pmgr, caseIDForSubnet, rawSubnetData);
        if (compositeTaskExited) {
            _busyTasks.remove(busyCompositeTask);

            /******************
             INSERTED FOR PERSISTANCE
             */
//            YPersistance.getInstance().updateData(this);
            if (pmgr != null) {
                pmgr.updateObject(this);
            }
            /***************************/

            Logger.getLogger(this.getClass()).debug("NOTIFYING RUNNER");
//            notify();
            kick(pmgr);

//            String caseIDStr = caseIDForSubnet.toString();
//            String taskIDStr = busyCompositeTask.getID();
//            YWorkItem item = _workItemRepository.getEngineStoredWorkItem(
//                    caseIDStr,
//                    taskIDStr);
//            _workItemRepository.removeWorkItemFamily(item);
//            YWorkItem item = YLocalWorklist.getEngineStoredWorkItem(caseIDForSubnet.toString(), busyCompositeTask.getURI());
//            YLocalWorklist.removeWorkItemFamily(item);
        }

        Logger.getLogger(this.getClass()).debug("<-- processCompletedSubnet");

    }


    public List attemptToFireAtomicTask(YPersistenceManager pmgr, String taskID)
            throws YDataStateException, YStateException, YQueryException, YPersistenceException {
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
                Logger.getLogger(this.getClass()).debug("NOTIFYING RUNNER");
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


    public synchronized void startWorkItemInTask(YPersistenceManager pmgr, YIdentifier caseID, String taskID) throws YDataStateException, YSchemaBuildingException, YPersistenceException {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        task.t_start(pmgr, caseID);
    }


    public synchronized boolean completeWorkItemInTask(YPersistenceManager pmgr, YWorkItem workItem, YIdentifier caseID, String taskID, Document outputData)
            throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        logger.debug("--> completeWorkItemInTask");
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        try {
            boolean success = completeTask(pmgr, workItem, task, caseID, outputData);

            // notify exception checkpoint to service if available
            if (_exceptionObserver != null)
                _engine.announceCheckWorkItemConstraints(_exceptionObserver, workItem, outputData, false);

            logger.debug("<-- completeWorkItemInTask");
            return success;
        } catch (YDataStateException e) {
            YProblemEvent ev = new YProblemEvent(task,
                    e.getMessage(),
                    YProblemEvent.RuntimeError);
            ev.logProblem(pmgr);
            throw e;
        }
    }


    public synchronized boolean continueIfPossible(YPersistenceManager pmgr) throws YPersistenceException {
        Logger.getLogger(this.getClass()).debug("--> continueIfPossible");
        List tasks = new ArrayList(_net.getNetElements().values());

        Iterator tasksIter = tasks.iterator();
        while (tasksIter.hasNext()) {
            YExternalNetElement netElement = (YExternalNetElement) tasksIter.next();
            if (netElement instanceof YTask) {
                YTask task = (YTask) netElement;

                if (task.t_enabled(_caseIDForNet)) {
                    boolean taskRecordedAsEnabled = _enabledTasks.contains(task);

                    boolean taskRecordedAsBusy = _busyTasks.contains(task);

                    if (!taskRecordedAsEnabled && !taskRecordedAsBusy) {

                        if (task instanceof YAtomicTask) {
                            YAtomicTask atomicTask = (YAtomicTask) task;
                            YAWLServiceGateway wsgw = (YAWLServiceGateway) atomicTask.getDecompositionPrototype();
                            //if its not an empty task
                            if (wsgw != null) {
                                createEnabledWorkItem(pmgr, _caseIDForNet, atomicTask);
                                YAWLServiceReference ys = wsgw.getYawlService();
                                YWorkItem item = _workItemRepository.getWorkItem(_caseIDForNet.toString(), atomicTask.getID());
                                if (ys != null)
                                    _engine.announceEnabledTask(ys, item);
                                else
                                    _engine.announceEnabledTaskToResourceService(item);

                                if (_exceptionObserver != null)
                                    _engine.announceCheckWorkItemConstraints(_exceptionObserver, item, _net.getInternalDataDocument(), true);

                                _enabledTasks.add(task);

                                /*************************/
                                /* INSERTED FOR PERSISTANCE*/
                                enabledTaskNames.add(task.getID());
//                                YPersistance.getInstance().updateData(this);
                                if (pmgr != null) {
                                    pmgr.updateObject(this);
                                }

                                /***********************/
                            } else {
                                //fire the empty atomic task
                                YIdentifier id = null;
                                try {
                                    id = (YIdentifier) atomicTask.t_fire(pmgr).iterator().next();
                                    atomicTask.t_start(pmgr, id);
                                    completeTask(pmgr, null, atomicTask, id, null);//atomicTask.t_complete(id);
                                } catch (YAWLException e) {
                                    YProblemEvent pe =
                                            new YProblemEvent(atomicTask,
                                                    e.getMessage(),
                                                    YProblemEvent.RuntimeError);
                                    pe.logProblem(pmgr);
                                }
                            }
                        } else {
                            //fire the composite task
                            _busyTasks.add(task);

                            /*************************/
                            /* INSERTED FOR PERSISTANCE*/
                            busyTaskNames.add(task.getID());
//                            YPersistance.getInstance().updateData(this);
                            if (pmgr != null) {
                                pmgr.updateObject(this);
                            }
                            /****************************/

                            Iterator caseIDs = null;
                            try {
                                caseIDs = task.t_fire(pmgr).iterator();
                            } catch (YAWLException e) {
                                e.printStackTrace();
                                YProblemEvent pe =
                                        new YProblemEvent(task,
                                                e.getMessage(),
                                                YProblemEvent.RuntimeError);
                                pe.logProblem(pmgr);
                            }
                            while (caseIDs.hasNext()) {
                                YIdentifier id = (YIdentifier) caseIDs.next();
                                try {
                                    task.t_start(pmgr, id);
                                } catch (YSchemaBuildingException e) {
                                    YProblemEvent f = new YProblemEvent(task,
                                            e.getMessage(),
                                            YProblemEvent.RuntimeError);
                                    f.logProblem(pmgr);
                                    e.printStackTrace();
                                } catch (YDataStateException e) {
                                    YProblemEvent f = new YProblemEvent(task,
                                            e.getMessage(),
                                            YProblemEvent.RuntimeError);
                                    f.logProblem(pmgr);
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } else /*if (!task.t_enabled(_caseIDForNet))*/ {

                    if (_enabledTasks.contains(task)) {
//                        YLocalWorklist.announceToWorklistsNoLongerEnabled(_caseIDForNet, task.getID());
                        _enabledTasks.remove(task);
                        /*************************/
                        /* INSERTED FOR PERSISTANCE*/
                        enabledTaskNames.remove(task.getID());

                        /**
                         * AJH: Bugfix: We need to remove from persistence the cancelled task
                         */
                        YWorkItem wItem = _workItemRepository.getWorkItem(_caseID, task.getID());
                        if (pmgr != null)
                        {
                            pmgr.deleteObject(wItem);
                        }                        
//                        YPersistance.getInstance().updateData(this);
                        if (pmgr != null) {
                            pmgr.updateObject(this);
                        }

                        /******************/
                    }

                }
                if (task.t_isBusy() && !_busyTasks.contains(task)) {
                    logger.error("Throwing RTE for lists out of sync");
                    throw new RuntimeException("busy task list out of synch with a busy task: " + task.getID() + " busy tasks: " + _busyTasks);
                }
            }
        }
        _busyTasks = _net.getBusyTasks();

        Logger.getLogger(this.getClass()).debug("<-- continueIfPossible");
        return _enabledTasks.size() > 0 || _busyTasks.size() > 0;
    }


    /**
     * Creates an enabled work item.
     *
     * @param caseIDForNet the caseid for the net
     * @param atomicTask   the atomic task that contains it.
     */
    private void createEnabledWorkItem(YPersistenceManager pmgr, YIdentifier caseIDForNet, YAtomicTask atomicTask) throws YPersistenceException {
        Logger.getLogger(this.getClass()).debug("--> createEnabledWorkItem: Case=" + caseIDForNet.get_idString() + " Task=" + atomicTask.getID());

        boolean allowDynamicCreation =
                atomicTask.getMultiInstanceAttributes() == null ? false :
                YMultiInstanceAttributes._creationModeDynamic.equals(atomicTask.getMultiInstanceAttributes().getCreationMode());

        //creating a new work item puts it into the work item
        //repository automatically.
        YWorkItem workItem = new YWorkItem(pmgr, atomicTask.getNet().getSpecification().getID(),
                new YWorkItemID(caseIDForNet, atomicTask.getID()),
                allowDynamicCreation, false);
        if (atomicTask.getDataMappingsForEnablement().size() > 0) {
            Element data = null;
            try {
                data = atomicTask.prepareEnablementData();
            } catch (YQueryException e) {
                e.printStackTrace();
            } catch (YSchemaBuildingException e) {
                e.printStackTrace();
            } catch (YDataStateException e) {
                //todo log this exception for admin, because the data did not pass schema validation.
                e.printStackTrace();
            } catch (YStateException e) {
                e.printStackTrace();
            }
            workItem.setData(pmgr, data);
        }
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

                                if (_net.usesSimpleRootData()) {
                                    parentRunner.processCompletedSubnet(pmgr,
                                            _caseIDForNet,
                                            _containingCompositeTask,
                                            _net.getInternalDataDocument());
                                } else {//version is Beta 4 or greater
                                    parentRunner.processCompletedSubnet(pmgr,
                                            _caseIDForNet,
                                            _containingCompositeTask,
                                            _net.getOutputData());
                                }
                                if (_caseIDForNet == null) {
                                    System.out.println("YNetRunner::completeTask() finished local task: " +
                                            atomicTask + " composite task: " +
                                            _containingCompositeTask +
                                            " caseid for decomposed net: " +
                                            _caseIDForNet);
                                }
                            }
                        }
                    }
                } else if (_engine != null && _containingCompositeTask != null) {
                    _engine.finishCase(pmgr, _caseIDForNet);
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
            Logger.getLogger(this.getClass()).debug("NOTIFYING RUNNER");
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
            task.cancel(pmgr);
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
            Element eTask = JDOMConversionTools.stringToElement(task.toXML());
            return eTask.getChild("flowsInto").getChild("nextElementRef").getAttributeValue("id");
        }
        return null ;
    }
}


