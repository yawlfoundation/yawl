/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jdom.Document;
import org.jdom.Element;

import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YCompositeTask;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YMultiInstanceAttributes;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YNetElement;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YCaseData;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemID;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.exceptions.YAWLException;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.util.JDOMConversionTools;

/**
 *
 *
 * @author Lachlan Aldred
 *         Date: 16/04/2003
 *         Time: 16:08:01
 * @hibernate.class table="RUNNER_STATES"
 */
@Entity
public class YNetRunner implements Serializable // extends Thread
{
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed.
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;

    private static Logger logger;

    protected YNet _net;
    private Set<YExternalNetElement> _enabledTasks = new HashSet<YExternalNetElement>();
    private Set<YExternalNetElement> _busyTasks = new HashSet<YExternalNetElement>();

    private static YWorkItemRepository _workItemRepository = YWorkItemRepository.getInstance();
    private YIdentifier _caseIDForNet;
    private YCompositeTask _containingCompositeTask;
    public AbstractEngine _engine;
    private boolean _cancelling;

    /**
     * **************************
     * INSERTED FOR PERSISTANCE
     * ***************************
     */
    protected String yNetID = null;
    private String containingTaskID = null;
    private YCaseData casedata = null;
    private URI _caseObserver;
    //private InterfaceX_EngineSideClient _exceptionObserver;

    // inserted to persist observers
    private String _caseObserverURI = null ;
    //protected String _exceptionObserverStr = null ;

    /*****************************/

    /*************************************/
    /*INSERTED METHODS*/
    /**
     * *********************************
     */

    public void restoreprepare() {
//        _workItemRepository.setNetRunnerToCaseIDBinding(this, _caseIDForNet);
        //_net.initialise();
    }

    public void setTask(YCompositeTask task) {
        this._containingCompositeTask = task;
    }

    /**
     * @hibernate.property column="containingTaskID"
     * @return id of any containing any composite task
     */
    @Basic
    public String getContainingTaskID() {
        return containingTaskID;
    }

    public void setContainingTaskID(String taskid) {
        containingTaskID = taskid;
    }

    public void setNet(YNet net) {
        _net = net;
        yNetID = net.getParent().getID();
        //_net.restoreData(casedata);
    }

    @OneToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @OnDelete(action=OnDeleteAction.CASCADE) 
    public YNet getNet() {
        return _net;
    }

    public void setYNetID(String id) {
        this.yNetID = id;
    }

    /**
     * @hibernate.property column="net_id"
     */
    @Basic
    public String getYNetID() {
        return yNetID;
    }

    /**
     * @hibernate.many-to-one
     */
    @OneToOne(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
    @OnDelete(action=OnDeleteAction.CASCADE)
    public YCaseData getCasedata() {
        return casedata;
    }

    @OneToOne(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
    @OnDelete(action=OnDeleteAction.CASCADE)
    public void setCasedata(YCaseData data) {
        casedata = data;
    }

    public void addBusyTask(YTask ext) {
        _busyTasks.add(ext);
    }

    public void addEnabledTask(YTask ext) {
        _enabledTasks.add(ext);
    }
    /************************************************/


    /**
     * Needed for hibernate to work.
     */
    public YNetRunner() {
        logger = Logger.getLogger(this.getClass());
        logger.debug("YNetRunner: <init>");
    }


    public YNetRunner(YSpecification spec, Element paramsData) throws YDataStateException, YSchemaBuildingException, YPersistenceException {
        YNet netPrototype = spec.getRootNet();
    	if( logger == null ) {
    		logger = Logger.getLogger(this.getClass());
            logger.debug("YNetRunner: <init>");
    	}
        _caseIDForNet = new YIdentifier(spec.getID(), spec.getVersion());
        _engine = EngineFactory.createYEngine();
        _engine.getDao().save(_caseIDForNet);
        setBasicCaseId(_caseIDForNet.toString());
        
        /*****************************/
        _net = (YNet) netPrototype.clone();
        casedata = new YCaseData();
        casedata.setId(_caseIDForNet.toString());
        _net.initializeDataStore(casedata);
        /*************************************/
        /*
          INSERTED FOR PERSISTANCE
         */
        yNetID = netPrototype.getParent().getID();
        /*****************************/

        prepare();
        if(paramsData != null) {
            _net.setIncomingData(paramsData);
        }
    }


    public YNetRunner(YNet netPrototype, YCompositeTask container, YIdentifier caseIDForNet, Element incomingData) throws YDataStateException, YSchemaBuildingException, YPersistenceException {
    	if( logger == null ) {
    		logger = Logger.getLogger(this.getClass());
            logger.debug("YNetRunner: <init>");
    	}
        
        _caseIDForNet = caseIDForNet;
        setBasicCaseId(_caseIDForNet.toString());
        
        /*****************************/
        _net = (YNet) netPrototype.clone();
        
        casedata = new YCaseData();
        casedata.setId(_caseIDForNet.toString());
        _net.initializeDataStore(casedata);
        /*****************************/
        _containingCompositeTask = container;
        _engine = EngineFactory.createYEngine();
        
        yNetID = netPrototype.getParent().getID();
        setContainingTaskID(container.getID());
        /******************************/
        prepare();
        _net.setIncomingData(incomingData);
    }

    private void prepare() throws YPersistenceException {
//        _workItemRepository.setNetRunnerToCaseIDBinding(this, _caseIDForNet);
        YInputCondition inputCondition = _net.getInputCondition();
        inputCondition.add(_caseIDForNet);
        _net.initialise();
        /*
        Uncommented for persistance
        */
        //continueIfPossible();
    }


    /* *
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

    public void start() throws YPersistenceException {
        kick();
    }

    @Transient
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
     * @throws YPersistenceException
     */
    public void kick() throws YPersistenceException {
        logger = Logger.getLogger(this.getClass());
        logger.debug("--> YNetRunner.kick");

        boolean progressed = continueIfPossible();

        if (!progressed) {
            logger.debug("YNetRunner not able to continue");

            /*
             * Make sure that the engine reference
             * is restored in case of failure
             * */
            _engine = EngineFactory.createYEngine();


            if (_engine != null) {
                //case completion
                if (_caseObserver != null) {
                    _engine.announceCaseCompletionToEnvironment(_caseObserver,
                                      _caseIDForNet, _net.getOutputData());
                }

                // notify exception checkpoint to service if available (post's for case end)
//                if (_exceptionObserver != null) {
                    Document data = _net.getInternalDataDocument();
                    _engine.announceCheckCaseConstraints(null,
                                       _caseIDForNet.toString(),
                                       JDOMConversionTools.documentToString(data), false);
//                }

                if(!_cancelling) {
	                Logger.getLogger(this.getClass()).debug("Asking engine to finish case");
	                YIdentifier id = _caseIDForNet;
//	                if(_caseIDForNet.getParent() != null) {
//	                	_caseIDForNet = null;
//	                }
//	                _engine.getDao().save(this);
	                
	                _engine.finishCase(id);
	                
//	                _caseIDForNet = id;
	
	                if (isRootNet()) {
	                    try {
	                        _engine.clearCase(id);
	                    } catch (Exception e) {
	                        //todo So, what shall we do now ????
	                    }
	                }
                }
            }
            if (!_cancelling && deadLocked()) {
                notifyDeadLock();
            }
            cancel();
        } else {
        	_engine.getDao().save(this);
        }
        logger.debug("<-- YNetRunner.kick");
    }

    @Transient
    private boolean isRootNet() {
        return _caseIDForNet.getParent() == null;
    }


    private void notifyDeadLock() throws YPersistenceException {
        List locations = _caseIDForNet.getLocationsForNet(getNet());
        for (int i = 0; i < locations.size(); i++) {
            YNetElement element = (YNetElement) locations.get(i);
            if (_net.getNetElements().contains(element)) {
                if (element instanceof YTask) {
                    createDeadlockItem((YTask) element);
                }
                List<YExternalNetElement> postset = ((YExternalNetElement) element).getPostsetElements();
                if (postset.size() > 0) {
                    for (Iterator iterator = postset.iterator(); iterator.hasNext();) {
                        YExternalNetElement postsetElement = (YExternalNetElement) iterator.next();
                        createDeadlockItem(postsetElement);
                    }
                }
            }
        }
    }


    private void createDeadlockItem(YExternalNetElement netElement) throws YPersistenceException {
        String specificationID = _net.getParent().getID();
        boolean allowsNewInstances = false;
        boolean isDeadlocked = true;
//        YWorkItemID deadlockWorkItemID = new YWorkItemID(_caseIDForNet,
//                netElement.getID());
        YWorkItem item = new YWorkItem(specificationID,
                _caseIDForNet,
                netElement.getID(),
                allowsNewInstances,
                isDeadlocked);
        _engine.getDao().save(item);
        //Log to Problems table of database.
        YProblemEvent event  = new YProblemEvent(_net, "Deadlocked", YProblemEvent.RuntimeError);
        event.logProblem(_engine.getDao());
    }


    /**
     * Assumption: there are no enabled tasks
     * @return if deadlocked
     */
    private boolean deadLocked() throws YPersistenceException {
        List locations = _caseIDForNet.getLocationsForNet(getNet());
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


    private synchronized void processCompletedSubnet(YIdentifier caseIDForSubnet, YCompositeTask busyCompositeTask,
                                                     Document rawSubnetData)
            throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        Logger.getLogger(this.getClass()).debug("--> processCompletedSubnet");

        if (caseIDForSubnet == null) {
            throw new RuntimeException();
        }
        boolean compositeTaskExited = busyCompositeTask.t_complete(caseIDForSubnet, rawSubnetData);
        if (compositeTaskExited) {
            _busyTasks.remove(busyCompositeTask);

            //check to see if completing this task resulted in completing the net.
            if (this.isCompleted() && _net.getOutputCondition().getIdentifiers().size() == 1) {

                if (_containingCompositeTask != null) {
                    YNetRunner parentRunner = _workItemRepository.getNetRunner(_caseIDForNet.getParent());
                    if (parentRunner != null) {
                        synchronized (parentRunner) {
                            if (_containingCompositeTask.t_isBusy()) {

                                if (_net.usesSimpleRootData()) {
                                    parentRunner.processCompletedSubnet(
                                            _caseIDForNet,
                                            _containingCompositeTask,
                                            _net.getInternalDataDocument());
                                } else {//version is Beta 4 or greater
                                    parentRunner.processCompletedSubnet(
                                            _caseIDForNet,
                                            _containingCompositeTask,
                                            _net.getOutputData());
                                }
                            }
                        }
                    }
                } else if (_engine != null && _containingCompositeTask != null) {
                    _engine.finishCase(_caseIDForNet);
                }
            }


            Logger.getLogger(this.getClass()).debug("NOTIFYING RUNNER");

            kick();
        }

        // TODO this shouldn't be needed. The comment is wrong, it doesn't mean it wasn't persisted it
        // means it was deleted in the call to kick() above. We shouldn't need to repersist.
        // note that the check for getId() does no good, because the id won't be null.
//        if (getId() != null) {
//            /* If the proxy is == null that means that the object was never persisted
//		     * which it should have been. See unit tests testImproperCompletionOfSubnet
//             */
//        	_engine.getDao().save(this);
//        }

        Logger.getLogger(this.getClass()).debug("<-- processCompletedSubnet");
    }


    public List attemptToFireAtomicTask(String taskID)
            throws YDataStateException, YStateException, YQueryException, YPersistenceException {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        if (task.t_enabled(_caseIDForNet)) {
            List<YIdentifier> newChildIdentifiers = task.t_fire();
            _enabledTasks.remove(task);

            /**********************/
            _busyTasks.add(task);
            
            for( YIdentifier id : newChildIdentifiers ) {
            	_engine.getDao().save( id );
            }

            /******************************/
            synchronized (this) {
                Logger.getLogger(this.getClass()).debug("NOTIFYING RUNNER");
//                notify();
                kick();
            }
            return newChildIdentifiers;
        }
        return null;
    }


    public synchronized YIdentifier addNewInstance(String taskID, YIdentifier aSiblingInstance, Element newInstanceData)
            throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        if (task.t_isBusy()) {
            return task.t_add(aSiblingInstance, newInstanceData);
        }
        return null;
    }


    public synchronized void startWorkItemInTask(YIdentifier caseID, String taskID) throws YDataStateException, YSchemaBuildingException, YPersistenceException {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        task.t_start(caseID);
    }


    public synchronized boolean completeWorkItemInTask(YWorkItem workItem, YIdentifier caseID, String taskID, Document outputData)
            throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {

        /*
         * Make sure that the engine reference
         * is restored in case of failure
         * */
        _engine = EngineFactory.createYEngine();


        logger.debug("--> completeWorkItemInTask");
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        try {
            boolean success = completeTask(workItem, task, caseID, outputData);

            // notify exception checkpoint to service if available

            _engine.announceCheckWorkItemConstraints(workItem, outputData, false);

            logger.debug("<-- completeWorkItemInTask");
            return success;
        } catch (YDataStateException e) {
            YProblemEvent ev = new YProblemEvent(task,
                    e.getMessage(),
                    YProblemEvent.RuntimeError);
            ev.logProblem(_engine.getDao());
            throw e;
        }
    }


    public synchronized boolean continueIfPossible() throws YPersistenceException {
        Logger.getLogger(this.getClass()).debug("--> continueIfPossible");

        /*
         * Make sure that the engine reference
         * is restored in case of failure
         * */
        _engine = EngineFactory.createYEngine();

        List tasks = new ArrayList(_net.getNetElements());

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
                                createEnabledWorkItem(_caseIDForNet, atomicTask);
                                URI ys = wsgw.getYawlService();
                                YWorkItem item = _workItemRepository.getWorkItem(_caseIDForNet.toString(), atomicTask.getID());
                                if (ys != null) {
                                    _engine.announceEnabledTask(ys, item);
                                }

                                _engine.announceCheckWorkItemConstraints(item, _net.getInternalDataDocument(), true);

                                _enabledTasks.add(task);

                                /*************************/
                                /* INSERTED FOR PERSISTANCE*/
//                                enabledTaskNames.add(task.getSpecURI());
//                                YPersistance.getInstance().updateData(this);
//  TODO                              if (pmgr != null) {
//                                    pmgr.updateObject(this);
//                                }

                                /***********************/
                            } else {
                                //fire the empty atomic task
                                YIdentifier id = null;
                                try {
                                    id = (YIdentifier) atomicTask.t_fire().iterator().next();
                                    atomicTask.t_start(id);
                                    completeTask(null, atomicTask, id, null);//atomicTask.t_complete(id);
                                } catch (YAWLException e) {
                                    YProblemEvent pe =
                                            new YProblemEvent(atomicTask,
                                                    e.getMessage(),
                                                    YProblemEvent.RuntimeError);
                                    pe.logProblem(_engine.getDao());
                                }
                            }
                        } else {
                            //fire the composite task
                            _busyTasks.add(task);

                            /*************************/
                            /* INSERTED FOR PERSISTANCE*/
//                            busyTaskNames.add(task.getSpecURI());
//                            YPersistance.getInstance().updateData(this);
//  TODO                          if (pmgr != null) {
//                                pmgr.updateObject(this);
//                            }
                            /****************************/

                            Iterator caseIDs = null;
                            try {
                                caseIDs = task.t_fire().iterator();
                            } catch (YAWLException e) {
                                e.printStackTrace();
                                YProblemEvent pe =
                                        new YProblemEvent(task,
                                                e.getMessage(),
                                                YProblemEvent.RuntimeError);
                                pe.logProblem(_engine.getDao());
                            }
                            while (caseIDs.hasNext()) {
                                YIdentifier id = (YIdentifier) caseIDs.next();
                                _engine.getDao().save(id);
                                try {
                                    task.t_start(id);
                                } catch (YSchemaBuildingException e) {
                                    YProblemEvent f = new YProblemEvent(task,
                                            e.getMessage(),
                                            YProblemEvent.RuntimeError);
                                    f.logProblem(_engine.getDao());
                                    e.printStackTrace();
                                } catch (YDataStateException e) {
                                    YProblemEvent f = new YProblemEvent(task,
                                            e.getMessage(),
                                            YProblemEvent.RuntimeError);
                                    f.logProblem(_engine.getDao());
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } else /*if (!task.t_enabled(_caseIDForNet))*/ {

                    if (_enabledTasks.contains(task)) {
//                        YLocalWorklist.announceToWorklistsNoLongerEnabled(_caseIDForNet, task.getSpecURI());
                    	logger.error("removing task " + task);
                        _enabledTasks.remove(task);
                        /*************************/
                        /* INSERTED FOR PERSISTANCE*/
//                        enabledTaskNames.remove(task.getSpecURI());

                        /**
                         * AJH: Bugfix: We need to remove from persistence the cancelled task
                         */
                        YWorkItem wItem = _workItemRepository.getWorkItem(_caseIDForNet.toString(), task.getID());
                        _workItemRepository.removeWorkItemFamily(wItem);
//                        YWorkItem persistentItem = (YWorkItem) _engine.getDao().retrieve( YWorkItem.class, wItem.getId() );
//                        _engine.getDao().delete(persistentItem);
// TODO                       if (pmgr != null)
//                        {
//                            pmgr.deleteObject(wItem);
//                        }
//                        YPersistance.getInstance().updateData(this);
//  TODO                      if (pmgr != null) {
//                            pmgr.updateObject(this);
//                        }

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

        if (!_cancelling) {
        	_engine.getDao().save(this);
        }

        Logger.getLogger(this.getClass()).debug("<-- continueIfPossible");
        return _enabledTasks.size() > 0 || _busyTasks.size() > 0;
    }


    /**
     * Creates an enabled work item.
     *
     * @param caseIDForNet the caseid for the net
     * @param atomicTask   the atomic task that contains it.
     */
    private void createEnabledWorkItem(YIdentifier caseIDForNet, YAtomicTask atomicTask) throws YPersistenceException {
        Logger.getLogger(this.getClass()).debug("--> createEnabledWorkItem: Case=" + caseIDForNet.getId() + " Task=" + atomicTask.getID());

        boolean allowDynamicCreation =
                atomicTask.getMultiInstanceAttributes() == null ? false :
                YMultiInstanceAttributes._creationModeDynamic.equals(atomicTask.getMultiInstanceAttributes().getCreationMode());

        //creating a new work item puts it into the work item
        //repository automatically.
        YWorkItem workItem = new YWorkItem(atomicTask.getParent().getParent().getID(),
                caseIDForNet, atomicTask.getID(),
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
            workItem.setData(data);
            _engine.getDao().save(workItem);
        }
        _engine.getDao().save(workItem);
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
    private boolean completeTask(YWorkItem workItem, YAtomicTask atomicTask, YIdentifier identifier, Document outputData)
            throws YDataStateException, YStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        logger.debug("--> completeTask");
        /*
         * Make sure that the engine reference
         * is restored in case of failure
         * */
        _engine = EngineFactory.createYEngine();

        boolean taskExited = atomicTask.t_complete(identifier, outputData);

        if (taskExited) {
            //todo AJH New code here - Relocated from YEngine.completeWorkItem
            if (workItem != null) {
                AbstractEngine.getWorkItemRepository().removeWorkItemFamily(workItem);
            }


            //check to see if completing this task resulted in completing the net.
            if (this.isCompleted() && _net.getOutputCondition().getIdentifiers().size() == 1) {
                //so now we know the net is complete we check if this net is a subnet.
                if (_containingCompositeTask != null) {
                    YNetRunner parentRunner = _workItemRepository.getNetRunner(_caseIDForNet.getParent());
                    if (parentRunner != null) {
                        synchronized (parentRunner) {
                            if (_containingCompositeTask.t_isBusy()) {

                                if (_net.usesSimpleRootData()) {
                                    parentRunner.processCompletedSubnet(
                                            _caseIDForNet,
                                            _containingCompositeTask,
                                            _net.getInternalDataDocument());
                                } else {//version is Beta 4 or greater
                                    parentRunner.processCompletedSubnet(
                                            _caseIDForNet,
                                            _containingCompositeTask,
                                            _net.getOutputData());
                                }
                            }
                        }
                    }
                } else if (_engine != null && _containingCompositeTask != null) {
                    _engine.finishCase(_caseIDForNet);
                }
            }

            continueIfPossible();
            _busyTasks.remove(atomicTask);
            /*
              INSERTED FOR PERSISTANCE
             */
            if (!_cancelling) {
            	_engine.getDao().save(this);
            }
//            busyTaskNames.remove(atomicTask.getSpecURI());
//            YPersistance.getInstance().updateData(this);
// TODO           if (pmgr != null) {
//                pmgr.updateObject(this);
//            }

            /**************************/
            logger.debug("NOTIFYING RUNNER");
            //todo Removing this causes sequence problems when going cyclic
            kick();
        }
        logger.debug("<-- completeTask: Exited=" + taskExited);
        return taskExited;
    }


    public synchronized void cancel() throws YPersistenceException {
        logger.debug("--> NetRunner cancel " + this.getCaseID().getId());

        /*
         * Make sure that the engine reference
         * is restored in case of failure
         * */
        _engine = EngineFactory.createYEngine();


        Collection netElements = _net.getNetElements();
        Iterator iterator = netElements.iterator();
        while (iterator.hasNext()) {
            YExternalNetElement netElement = (YExternalNetElement) iterator.next();
            if (netElement instanceof YTask) {
                YTask task = ((YTask) netElement);
                if (task.t_isBusy()) {
                    task.cancel();
                }
            } else if (((YCondition) netElement).containsIdentifier()) {
                ((YCondition) netElement).removeAll();
            }
        }
        _workItemRepository.cancelNet(_caseIDForNet);

        /*
         * when a composite task completes. This method may actually
         * be called twice. This would cause two deletions and the
         * last one would throw an exception. Therefore the cancelling
         * variable is used to see if this method has been called before
         *
         * */
        if (!_cancelling) {
            setArchived(true);
            /*
             * If we just deleted the root net, then also delete all identifiers
             * since the case is completed
             * */
            //if (this.isRootNet()) {
            //	context.delete( context.getDataProxy( this.getCaseID() ));
            //}
        }

        _cancelling = true;

        _enabledTasks.clear(); // = new HashSet<YExternalNetElement>();
        _busyTasks.clear(); // = new HashSet<YExternalNetElement>();

        /*
         * Clear the engine lookup maps
         * */
        _engine.finishCase(this._caseIDForNet);
    }


    public synchronized boolean rollbackWorkItem(YIdentifier caseID, String taskID) throws YPersistenceException {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        return task.t_rollBackToFired(caseID);
    }


    public YExternalNetElement getNetElement(String id) {
        return _net.getNetElement(id);
    }


    private Long _id;

    /*
     * Tore: I made these public to enable the persistence
     * utilities to use these to access the database key
     * Before, the case_id was used as key, but the retreive
     * method expected a long
     * */
    @Id
    @Column(name="netrunner_id")
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    public Long getId() {
		return _id;
	}

	public void setId( Long id ) {
		_id = id;
	}

    @ManyToOne
    public YCompositeTask getContainingCompositeTask() {
        return this._containingCompositeTask;
    }

    public void setContainingCompositeTask(YCompositeTask composite) {
        this._containingCompositeTask = composite;
    }



    @ManyToOne(cascade= {CascadeType.ALL})
    @JoinColumn(name="caseID")
    public YIdentifier getCaseID() {
        return _caseIDForNet;
    }
    public void setCaseID(YIdentifier caseIDForNet) {
    	_caseIDForNet = caseIDForNet;
    	/*
    	 * TODO: Tore unit test to verify that this is
    	 * working
    	 * */
//    	_workItemRepository.setNetRunnerToCaseIDBinding(this, _caseIDForNet);
    }

    @Basic
    private String basicCaseId;
    public String getBasicCaseId() {
    	return basicCaseId;
    }
    private void setBasicCaseId(String caseid) {
    	this.basicCaseId = caseid;
    }

    @Transient
    public boolean isCompleted() {
        if (_net.getOutputCondition().containsIdentifier()) {
            return true;
        } else {
            return isEmpty();
        }
    }

    @Transient
    public boolean isEmpty() {
        Iterator elements = _net.getNetElements().iterator();
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

    private boolean archived = false;
    @Basic
    public boolean getArchived() {
       return archived;
    }

    public void setArchived(boolean archived) {
       this.archived = archived;
    }


    @OneToMany(cascade={CascadeType.ALL}, fetch= FetchType.EAGER)
    @JoinTable(
            name="busy_task_set",
            joinColumns = { @JoinColumn( name="netrunner_id") },
            inverseJoinColumns = @JoinColumn( name="extern_id")
    )
    public Set<YExternalNetElement> getBusyTasks() {
        return _busyTasks;
    }

    private void setBusyTasks(Set<YExternalNetElement> tasks) {
    	_busyTasks = tasks;
    }

    @OneToMany(cascade={CascadeType.ALL}, fetch= FetchType.EAGER)
    @JoinTable(
            name="enabled_task_set",
            joinColumns = { @JoinColumn( name="netrunner_id") },
            inverseJoinColumns = @JoinColumn( name="extern_id")
    )
    public Set<YExternalNetElement> getEnabledTasks() {
        return _enabledTasks;
    }

    private void setEnabledTasks(Set<YExternalNetElement> tasks) {
    	_enabledTasks = tasks;
    }


    public boolean isAddEnabled(String taskID, YIdentifier childID) throws YQueryException {
        YAtomicTask task = (YAtomicTask) _net.getNetElement(taskID);
        return task.t_addEnabled(childID);
    }

    public void setObserver(URI observer) {
        _caseObserver = observer;
        _caseObserverURI = observer.toString();                       // for persistence
    }


    /***************************************************************************/
    /** The following methods have been added to support the exception service */

    public void restoreObservers() throws YPersistenceException {

        /*
         * Make sure that the engine reference
         * is restored in case of failure
         * */
        _engine = EngineFactory.createYEngine();

        if(_caseObserverURI != null) {
            YAWLServiceReference caseObserver =
                                    _engine.getRegisteredYawlService(_caseObserverURI);
            URI observer = null;
			try {
				observer = new URI(caseObserver.getURI());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
            if (caseObserver != null) setObserver(observer);
        }
//        if (_exceptionObserverStr != null) {
//            InterfaceX_EngineSideClient exObserver =
//                                new InterfaceX_EngineSideClient(_exceptionObserverStr);
//            setExceptionObserver(exObserver);
//            _engine.setExceptionObserver(_exceptionObserverStr);
//        }
    }


    private String get_caseObserverURI() {
        return _caseObserverURI ;
    }

    private void set_caseObserverURI(String observerURI) {
        _caseObserverURI = observerURI;
    }

    /** cancels the specified task */
    public void cancelTask(String taskID) {
        YAtomicTask task = (YAtomicTask) getNetElement(taskID);

        try {
            task.cancel();
            _busyTasks.remove(task);
//            busyTaskNames.remove(task.getSpecURI());
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
                URI ys = wsgw.getYawlService();
                if (ys != null)
                   return ys.toString().indexOf("timeService") > -1 ;
            }
        }
        return false ;
    }


    /** returns a list of all workitems executing in parallel to the time-out
        workitem passed (the list includes the time-out task) */
    public List getTimeOutTaskSet(YWorkItem item) {
        YTask timeOutTask = (YTask) getNetElement(item.getTaskID());
        String nextTaskID = getFlowsIntoTaskID(timeOutTask);
        ArrayList<String> result = new ArrayList<String>() ;

        if (nextTaskID != null) {
            for ( YNetElement yne : getNet().getNetElements()) {
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

    public static void main(String[] args) {
		System.out.println(URLDecoder.decode("au.edu.qut.yawl.exceptions.YQueryException%0D%0A%09at+au.edu.qut.yawl.engine.AbstractEngine.startWorkItem%28AbstractEngine.java%3A984%29%0D%0A%09at+au.edu.qut.yawl.engine.YEngine.startWorkItem%28YEngine.java%3A872%29%0D%0A%09at+sun.reflect.NativeMethodAccessorImpl.invoke0%28Native+Method%29%0D%0A%09at+sun.reflect.NativeMethodAccessorImpl.invoke%28NativeMethodAccessorImpl.java%3A39%29%0D%0A%09at+sun.reflect.DelegatingMethodAccessorImpl.invoke%28DelegatingMethodAccessorImpl.java%3A25%29%0D%0A%09at+java.lang.reflect.Method.invoke%28Method.java%3A585%29%0D%0A%09at+org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection%28AopUtils.java%3A334%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint%28ReflectiveMethodInvocation.java%3A181%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.proceed%28ReflectiveMethodInvocation.java%3A148%29%0D%0A%09at+org.springframework.transaction.interceptor.TransactionInterceptor.invoke%28TransactionInterceptor.java%3A100%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.proceed%28ReflectiveMethodInvocation.java%3A170%29%0D%0A%09at+org.springframework.aop.framework.JdkDynamicAopProxy.invoke%28JdkDynamicAopProxy.java%3A209%29%0D%0A%09at+%24Proxy31.startWorkItem%28Unknown+Source%29%0D%0A%09at+au.edu.qut.yawl.engine.interfce.EngineGatewayImpl.startWorkItem%28EngineGatewayImpl.java%3A331%29%0D%0A%09at+au.edu.qut.yawl.engine.interfce.InterfaceB_EngineBasedServer.processPostQuery%28InterfaceB_EngineBasedServer.java%3A254%29%0D%0A%09at+au.edu.qut.yawl.engine.interfce.InterfaceB_EngineBasedServer.doPost%28InterfaceB_EngineBasedServer.java%3A112%29%0D%0A%09at+javax.servlet.http.HttpServlet.service%28HttpServlet.java%3A709%29%0D%0A%09at+javax.servlet.http.HttpServlet.service%28HttpServlet.java%3A802%29%0D%0A%09at+org.apache.catalina.core.ApplicationFilterChain.internalDoFilter%28ApplicationFilterChain.java%3A252%29%0D%0A%09at+org.apache.catalina.core.ApplicationFilterChain.doFilter%28ApplicationFilterChain.java%3A173%29%0D%0A%09at+org.apache.catalina.core.StandardWrapperValve.invoke%28StandardWrapperValve.java%3A213%29%0D%0A%09at+org.apache.catalina.core.StandardContextValve.invoke%28StandardContextValve.java%3A178%29%0D%0A%09at+org.apache.catalina.core.StandardHostValve.invoke%28StandardHostValve.java%3A126%29%0D%0A%09at+org.apache.catalina.valves.ErrorReportValve.invoke%28ErrorReportValve.java%3A105%29%0D%0A%09at+org.apache.catalina.core.StandardEngineValve.invoke%28StandardEngineValve.java%3A107%29%0D%0A%09at+org.apache.catalina.connector.CoyoteAdapter.service%28CoyoteAdapter.java%3A148%29%0D%0A%09at+org.apache.coyote.http11.Http11Processor.process%28Http11Processor.java%3A869%29%0D%0A%09at+org.apache.coyote.http11.Http11BaseProtocol%24Http11ConnectionHandler.processConnection%28Http11BaseProtocol.java%3A664%29%0D%0A%09at+org.apache.tomcat.util.net.PoolTcpEndpoint.processSocket%28PoolTcpEndpoint.java%3A527%29%0D%0A%09at+org.apache.tomcat.util.net.LeaderFollowerWorkerThread.runIt%28LeaderFollowerWorkerThread.java%3A80%29%0D%0A%09at+org.apache.tomcat.util.threads.ThreadPool%24ControlRunnable.run%28ThreadPool.java%3A684%29%0D%0A%09at+java.lang.Thread.run%28Thread.java%3A595%29%0D%0ACaused+by%3A+java.lang.NullPointerException%0D%0A%09at+au.edu.qut.yawl.persistence.dao.AbstractDelegatedDAO.save%28AbstractDelegatedDAO.java%3A35%29%0D%0A%09at+au.edu.qut.yawl.engine.AbstractEngine.startWorkItem%28AbstractEngine.java%3A923%29%0D%0A%09...+31+more%0D%0A"));
	}
    
}

