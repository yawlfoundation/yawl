/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;

import au.edu.qut.yawl.authentication.UserList;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.engine.interfce.InterfaceB_EngineBasedClient;
import au.edu.qut.yawl.engine.interfce.InterfaceB_InternalEngineBasedClient;
import au.edu.qut.yawl.engine.interfce.ServiceHandler;
import au.edu.qut.yawl.engine.interfce.interfaceX.EngineExceptionLogger;
import au.edu.qut.yawl.engine.interfce.interfaceX.ExceptionGateway;
import au.edu.qut.yawl.engine.interfce.interfaceX.InterfaceX_EngineSideClient;
import au.edu.qut.yawl.events.YawlEventLogger;
import au.edu.qut.yawl.exceptions.YAWLException;
import au.edu.qut.yawl.exceptions.YAuthenticationException;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import au.edu.qut.yawl.util.YVerificationMessage;

/**
 *
 * This singleton is responsible for storing all of the active process specifications
 * in object format.  It is also a single control point for all operations over running
 * process instances, e.g. launching cases, starting work-items (check-out), completing
 * work-items (check-in), cancelling cases etc.   It delegates some of these process
 * instance controlling operations to some YNetRunner (see below) objects, however the
 * engine stores and aggregates each YNetRunner instance and correlates it with the
 * YIdentifier object running through it.
 *
 *
 * @author Lachlan Aldred
 *         Date: 17/06/2003
 *         Time: 13:46:54
 * 
 */
public class YEngine extends AbstractEngine {


	private static Logger logger = Logger.getLogger(YEngine.class);


    protected static YWorkItemRepository _workItemRepository;
    private static YEngine _myInstance;
    private static YawlEventLogger yawllog = null;
    private static UserList _userList;
    private final Object mutex = new Object();


    /*************************************************/
    /*INSERTED VARIABLES AND METHODS FOR PERSISTANCE */
    /**
     * *********************************************
     */
    private static boolean journalising = true;
    private static boolean restoring;
    private static int maxcase = 0;
// TODO   private static SessionFactory factory = null;

    /*
     * May seem strange. But this is used by the
     * engine factory to get the YEngine of a transactional
     * engine created through the spring framework
     * This engine is then accessed by most classes
     * except the engineGatewayImpl
     * */
    public YEngine getYEngine() {
    	return this;
    }

    /**
     * AJH: Switch indicating if we generate user interface attributes with a tasks output XML doclet.
     */
    private static boolean generateUIMetaData = false;

    /**
     * Constructor.
     */
    protected YEngine() {
        super();
        yawllog = YawlEventLogger.getInstance();

    }


    /**
     * @ deprecated This is being called from restore() only so will be commented for now (Persistence related code)
     * @param uri
     * @return
     * @throws YPersistenceException
     */
//    private List addSpecifications(String uri) throws YPersistenceException {
//        try {
//            return addSpecifications(new File(uri), true, new Vector());
//        } catch (Exception e) {
//            System.out.println("Exception caught in adding specification");
//            return null;
//        }
//    }

    /**
     * This method is only used in restore, so we're commenting it out for now (persistence related junk)
     * @param runner
     */
//    private void addRunner(YNetRunner runner) {
//        String specID = runner.getYNetID();
//        YSpecification specification = (YSpecification) _specifications.get(specID);
//        if (specification != null) {
//            /*
//              initialize the runner
//             */
//            runner.setEngine(this);
//            runner.restoreprepare();
//            _caseIDToNetRunnerMap.put(runner.getCaseID(), runner);
//            _runningCaseIDToSpecIDMap.put(runner.getCaseID(), specID);
//
//            if (_interfaceBClient != null) {
//                _interfaceBClient.addCase(specID, runner.getCaseID().toString());
//            }
//        }
//    }

    /**
     * Restore the engine state from perstistent storage.<P>
     *
     * @throws YPersistenceException
     */
//    private void restore(YPersistenceManager pmgr) throws YPersistenceException {
//        Vector runners = new Vector();
//        HashMap runnermap = new HashMap();
//        Map idtoid = new HashMap();
//
//        logger.debug("--> restore");
//
//        try {
//            restoring = true;
//
//            logger.info("Restoring Users");
//            Query query = pmgr.createQuery("from au.edu.qut.yawl.admintool.model.Resource" +
//                    " where IsOfResourceType = 'Human'");
//
//            for (Iterator it = query.iterate(); it.hasNext();) {
//                HumanResource user = (HumanResource) it.next();
//                logger.debug("Restoring user '" + user.getRsrcID() + "'");
//                UserList.getInstance().addUser(
//                        user.getRsrcID(),
//                        user.getPassword(),
//                        user.getIsAdministrator());
//            }
//
//
//            logger.info("Restoring Services");
//            query = pmgr.createQuery("from au.edu.qut.yawl.elements.YAWLServiceReference");
//
//            for (Iterator it = query.iterate(); it.hasNext();) {
//                YAWLServiceReference service = (YAWLServiceReference) it.next();
//                addYawlService(service);
//            }
//
//            logger.info("Restoring Specifications - Starts");
//            query = pmgr.createQuery("from au.edu.qut.yawl.engine.YSpecFile");
//
//            for (Iterator it = query.iterate(); it.hasNext();) {
//                YSpecFile spec = (YSpecFile) it.next();
//                String xml = spec.getXML();
//
//                {
//                    logger.debug("Restoring specification " + spec.getId());
//
//                    File f = File.createTempFile("yawltemp", null);
//                    BufferedWriter buf = new BufferedWriter(new FileWriter(f));
//                    buf.write(xml, 0, xml.length());
//                    buf.close();
//                    addSpecifications(f.getAbsolutePath());
//
//                    f.delete();
//                }
//            }
//            logger.info("Restoring Specifications - Ends");
//
//            logger.info("Restoring process instances - Starts");
//            query = pmgr.createQuery("from au.edu.qut.yawl.engine.YNetRunner order by case_id");
//            for (Iterator it = query.iterate(); it.hasNext();) {
//                YNetRunner runner = (YNetRunner) it.next();
//                runners.add(runner);
//            }
//
//            HashMap map = new HashMap();
//            for (int i = 0; i < runners.size(); i++) {
//                YNetRunner runner = (YNetRunner) runners.get(i);
//                String id = runner.get_caseID();
//                query = pmgr.createQuery("select from au.edu.qut.yawl.engine.YLogIdentifier where case_id = '" + id + "'");
//                for (Iterator it = query.iterate(); it.hasNext();) {
//                    YLogIdentifier ylogid = (YLogIdentifier) it.next();
//                    map.put(ylogid.getIdentifier(), ylogid);
//                }
//            }
//            YawlLogServletInterface.getInstance().setListofcases(map);
//
//            int checkedrunners = 0;
//
//            Vector storedrunners = (Vector) runners.clone();
//
//            while (checkedrunners < runners.size()) {
//
//                for (int i = 0; i < runners.size(); i++) {
//                    YNetRunner runner = (YNetRunner) runners.get(i);
//
//                    if (runner.getContainingTaskID() == null) {
//
//                        //This is a root net runner
//                        YSpecification specification = getSpecification(runner.getYNetID());
//                        if (specification != null) {
//                            YNet net = (YNet) specification.getRootNet().clone();
//                            runner.setNet(net);
//
//                            runnermap.put(runner.get_standin_caseIDForNet().toString(), runner);
//                        } else {
//                            /* This occurs when a specification has been unloaded, but the case is still there
//                               This case is not persisted, since we must have the specification stored as well.
//                             */
//                            // todo AJH Sort this
//                            pmgr.deleteObject(runner);
//                            storedrunners.remove(runner);
//
//                        }
//                        checkedrunners++;
//
//                    } else {
//                        //This is not a root net, but a decomposition
//
//                        // Find the parent runner
//                        String myid = runner.get_standin_caseIDForNet().toString();
//                        String parentid = myid.substring(0, myid.lastIndexOf("."));
//
//
//                        YNetRunner parentrunner = (YNetRunner) runnermap.get(parentid);
//
//                        if (parentrunner != null) {
//                            YNet parentnet = parentrunner.getNet();
//
//                            YCompositeTask task = (YCompositeTask) parentnet.getNetElement(runner.getContainingTaskID());
//                            runner.setTask(task);
//
//                            YNet net = (YNet) task.getDecompositionPrototype().clone();
//                            runner.setNet(net);
//                            runnermap.put(runner.get_standin_caseIDForNet().toString(), runner);
//
//                            checkedrunners++;
//                        }
//
//                    }
//                }
//            }
//
//            runners = storedrunners;
//
//            for (int i = 0; i < runners.size(); i++) {
//
//                YNetRunner runner = (YNetRunner) runners.get(i);
//
//                YNet net = runner.getNet();
//
//
//                P_YIdentifier pid = runner.get_standin_caseIDForNet();
//
//                if (runner.getContainingTaskID() == null) {
//                    // This is a root net runner
//
//                    YIdentifier id = restoreYID(pmgr, runnermap, idtoid, pid, null, runner.getYNetID(), net);
//                    runner.set_caseIDForNet(id);
//                    addRunner(runner);
//                }
//
//                YIdentifier yid = new YIdentifier(runner.get_caseID());
//                YWorkItemRepository.getInstance().setNetRunnerToCaseIDBinding(runner, yid);
//
//                Set busytasks = runner.getBusyTaskNames();
//
//                for (Iterator busyit = busytasks.iterator(); busyit.hasNext();) {
//                    String name = (String) busyit.next();
//                    YExternalNetElement element = net.getNetElement(name);
//
//                    runner.addBusyTask(element);
//                }
//
//                Set enabledtasks = runner.getEnabledTaskNames();
//
//                for (Iterator enabit = enabledtasks.iterator(); enabit.hasNext();) {
//                    String name = (String) enabit.next();
//                    YExternalNetElement element = net.getNetElement(name);
//
//                    if (element instanceof YTask) {
//                        YTask externalTask = (YTask) element;
//                        runner.addEnabledTask(externalTask);
//                    }
//
//                }
//            }
//
//            // restore case & exception observers (where they exist)
//            for (int i = 0; i < runners.size(); i++) {
//                YNetRunner runner = (YNetRunner) runners.get(i);
//                runner.restoreObservers();
//            }
//
//            logger.info("Restoring process instances - Ends");
//
//            logger.info("Restoring work items - Starts");
//            query = pmgr.createQuery("from au.edu.qut.yawl.engine.YWorkItem");
//            for (Iterator it = query.iterate(); it.hasNext();) {
//                YWorkItem witem = (YWorkItem) it.next();
//
//                if (witem.getStatus().equals(YWorkItem.statusEnabled)) {
//                    witem.setStatus(YWorkItem.statusEnabled);
//                }
//                if (witem.getStatus().equals(YWorkItem.statusFired)) {
//                    witem.setStatus(YWorkItem.statusFired);
//                }
//                if (witem.getStatus().equals(YWorkItem.statusExecuting)) {
//                    witem.setStatus(YWorkItem.statusExecuting);
//                }
//                if (witem.getStatus().equals(YWorkItem.statusComplete)) {
//                    witem.setStatus(YWorkItem.statusComplete);
//                }
//                if (witem.getStatus().equals(YWorkItem.statusIsParent)) {
//                    witem.setStatus(YWorkItem.statusIsParent);
//                }
//                if (witem.getStatus().equals(YWorkItem.statusDeadlocked)) {
//                    witem.setStatus(YWorkItem.statusDeadlocked);
//                }
//                if (witem.getStatus().equals(YWorkItem.statusDeleted)) {
//                    witem.setStatus(YWorkItem.statusDeleted);
//                }
//
//                if (witem.getData_string() != null) {
//                    StringReader reader = new StringReader(witem.getData_string());
//                    SAXBuilder builder = new SAXBuilder();
//                    Document data = builder.build(reader);
//                    witem.setInitData(data.getRootElement());
//                }
//
//                java.util.StringTokenizer st = new java.util.StringTokenizer(witem.getThisId(), ":");
//                String caseandid = st.nextToken();
//                java.util.StringTokenizer st2 = new java.util.StringTokenizer(caseandid, ".");
//                //String caseid =
//                st2.nextToken();
//                String taskid = st.nextToken();
//                // AJH: Strip off unique ID to obtain our taskID
//                {
//                    java.util.StringTokenizer st3 = new java.util.StringTokenizer(taskid, "!");
//                    taskid = st3.nextToken();
//                }
//                YIdentifier workitemid = (YIdentifier) idtoid.get(caseandid);
//                if (workitemid != null) {
//                    witem.setWorkItemID(new YWorkItemID(workitemid, taskid));
//                    witem.addToRepository();
//                } else {
//                    pmgr.deleteObject(witem);
//                }
//
//
//            }
//            logger.info("Restoring work items - Ends");
//
//            /*
//              Start net runners. This is a restart of a NetRunner not a clean start, therefore, the net runner should not create any new work items, if they have already been created.
//             */
//            logger.info("Restarting restored process instances - Starts");
//
//            for (int i = 0; i < runners.size(); i++) {
//                YNetRunner runner = (YNetRunner) runners.get(i);
//                logger.debug("Restarting " + runner.get_caseID());
//                runner.start(pmgr);
//            }
//            logger.info("Restarting restored process instances - Ends");
//
//            restoring = false;
//
//            logger.info("Restore completed OK");
//
//            if (logger.isDebugEnabled()) {
//                dump();
//            }
//
//        } catch (Exception e) {
//            throw new YPersistenceException("Failure whilst restoring engine session", e);
//        }
//    }

/**
 * TODO This is only for persistence purposes so it is commented out for now.
 */
//    public YIdentifier restoreYID(HashMap runnermap, Map idtoid, P_YIdentifier pid, YIdentifier father, String specname, YNet net) throws YPersistenceException {
//
//        YIdentifier id = new YIdentifier(pid.toString());
//
//        YNet sendnet = net;
//
//        id.set_father(father);
//
//        List list = pid.get_children();
//
//        if (list.size() > 0) {
//            List idlist = new Vector();
//
//            for (int i = 0; i < list.size(); i++) {
//                P_YIdentifier child = (P_YIdentifier) list.get(i);
//
//                YNetRunner netRunner = (YNetRunner) runnermap.get(child.toString());
//                if (netRunner != null) {
//                    sendnet = netRunner.getNet();
//                }
//                YIdentifier caseid = restoreYID(runnermap, idtoid, child, id, specname, sendnet);
//
//                if (netRunner != null) {
//                    netRunner.set_caseIDForNet(caseid);
//                }
//
//                idlist.add(caseid);
//            }
//
//            id.set_children(idlist);
//        }
//
//
//        for (int i = 0; i < pid.getLocationNames().size(); i++) {
//
//            String name = (String) pid.getLocationNames().get(i);
//            YExternalNetElement element = net.getNetElement(name);
//
//            if (element == null) {
//                name = name.substring(0, name.length() - 1);
//                String[] splitname = name.split(":");
//
//
//                /*
//                  Get the task associated with this condition
//                */
//                YTask task;
//                if (name.indexOf("CompositeTask") != -1) {
//                    YNetRunner netRunner_temp = (YNetRunner) runnermap.get(father.toString());
//                    task = (YTask) netRunner_temp.getNet().getNetElement(splitname[1]);
//                } else {
//                    task = (YTask) net.getNetElement(splitname[1]);
//                }
//                if (task != null) {
//                    YInternalCondition condition;
//                    if (splitname[0].startsWith(YInternalCondition._mi_active)) {
//
//                        condition = task.getMIActive();
//                        condition.add(id);
//
//                    } else if (splitname[0].startsWith(YInternalCondition._mi_complete)) {
//
//                        condition = task.getMIComplete();
//                        condition.add(id);
//
//                    } else if (splitname[0].startsWith(YInternalCondition._mi_entered)) {
//
//                        condition = task.getMIEntered();
//                        condition.add(id);
//
//                    } else if (splitname[0].startsWith(YInternalCondition._executing)) {
//
//                        condition = task.getMIExecuting();
//                        condition.add(id);
//
//                    } else {
//                        logger.error("Unknown YInternalCondition state");
//                    }
//                } else {
//                    if (splitname[0].startsWith("InputCondition")) {
//                        net.getInputCondition().add(id);
//                    } else if (splitname[0].startsWith("OutputCondition")) {
//                        net.getOutputCondition().add(id);
//                    }
//                }
//            } else {
//                if (element instanceof YTask) {
//                    ((YTask) element).setI(id);
//                    ((YTask) element).prepareDataDocsForTaskOutput();
//
//                } else if (element instanceof YCondition) {
//
//                    YConditionInterface cond = (YConditionInterface) element;
//                    cond.add(id);
//                }
//            }
//        }
//
//        idtoid.put(id.toString(), id);
//        return id;
//    }


    /**
     * *********************************************
     */

    public void initialise() throws YPersistenceException {
    	_myInstance = this;

    	//restore();

        /**
         * Initialise the standard Observer Gateways.
         */
        ObserverGateway stdHttpObserverGateway = new InterfaceB_EngineBasedClient();
        observerGatewayController.addGateway(stdHttpObserverGateway);

        ObserverGateway stdInternalObserverGateway = new InterfaceB_InternalEngineBasedClient();
        observerGatewayController.addGateway(stdInternalObserverGateway);

        _userList = UserList.getInstance();
        _workItemRepository = YWorkItemRepository.getInstance();

        /*
         * Add internal exception logger
         * */
        this.addExceptionObserver(new EngineExceptionLogger());


        /***************************/
        /**
         * Delete and re-create the standard engine InterfaceB services
         */
        YAWLServiceReference ys;
        Set s = getYAWLServices();


		if (s.size()==0) {
			/*
			 * If the set is empty, there are no services in the database
			 * initialise the set of standard services
			 * */
			ys = new YAWLServiceReference(
					"http://localhost:8080/yawlWSInvoker/");
			ys.setDocumentation("This YAWL Service enables suitably declared"
							+ " workflow tasks to invoke RPC style service on the Web.");
			this.addYawlService(ys);
			ys = new YAWLServiceReference(
					"http://localhost:8080/workletService/ib");
			ys.setDocumentation("Worklet Dynamic Process Selection and Exception Service");
			this.addYawlService(ys);
			ys = new YAWLServiceReference(
					"http://localhost:8080/yawlSMSInvoker/ib");
			ys.setDocumentation("SMS Message Module. Works if you have an account.");
			this.addYawlService(ys);
			ys = new YAWLServiceReference(
					"http://localhost:8080/timeService/ib");
			ys.setDocumentation("Time service, allows tasks to be a timeout task.");
			this.addYawlService(ys);
			// TODO standard services on startup should probably be dynamically loaded from a properties file
			ys = new YAWLServiceReference(
					"http://localhost:8080/NexusServiceInvoker/");
			ys.setDocumentation("This service enables YAWL specifications to access"
							+ " the Nexus Workflow services");
			this.addYawlService(ys);
		}
		/**
         * Ensure we have an 'admin' user
         */
        try {
            UserList.getInstance().addUser("admin", "YAWL", true);
        } catch (YAuthenticationException e) {
            // User already exists ??? Do nothing
        }

    }

    protected static YEngine createInstance(boolean journalising) throws YPersistenceException {
        if (_myInstance == null) {
            logger = Logger.getLogger("au.edu.qut.yawl.engine.YEngine");
            logger.debug("--> YEngine: Creating initial instance");
            _myInstance = new YEngine();
            /*
             * TODO: One of these are reduntant and
             * should be removed
             * */
            _myInstance.setJournalising(journalising);

        }
        return _myInstance;
    }



    //###################################################################################
    //                                MUTATORS
    //###################################################################################
    /**
     * Adds the specification contained in the parameter file to the engine
     *
     * AJH - MODIFIED FOR TRANSACTIONAL PERSISTENCE
     *
     *
     * @param specificationFile
     * @param ignoreErors       ignore verfication errors and load the spec anyway.
     * @param errorMessages     - an in/out param passing any error messages.
     * @return the specification ids of the sucessfully loaded specs
     */
    public List<String> addSpecifications(File specificationFile, boolean ignoreErors, List<YVerificationMessage> errorMessages) throws JDOMException, IOException, YPersistenceException {

        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.addSpecifications(specificationFile, ignoreErors, errorMessages);
        }
    }


    public boolean loadSpecification(YSpecification spec) throws YPersistenceException {

        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.loadSpecification(spec);
        }
    }

    /**
     *
     *
     * AJH - MODIFIED FOR TRANSACTIONAL PERSISTENCE
     *
     * @param specID
     * @throws YStateException
     * @throws YPersistenceException
     */
    public void unloadSpecification(String specID) throws YStateException, YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            super.unloadSpecification(specID);
        }
    }

    /**
     * Cancels the case - External interface.<P>
     *
     * For NetRunner calls to cancel a case, see the other overloaded method which accepts an instance of the
     * current persistence manager object.
     *
     * AJH - MODIFIED FOR TRANSACTIONAL PERSISTENCE
     *
     *
     * @param id the case ID.
     */
//    public void cancelCase(YIdentifier id) throws YPersistenceException {
//        /**
//         * SYNC'D External interface
//         */
//        synchronized (mutex) {
////
////    TODO        YPersistenceManager pmgr = null;
////
////            if (isJournalising()) {
////                pmgr = new YPersistenceManager(getPMSessionFactory());
////                pmgr.startTransactionalSession();
////            }
//
//            cancelCase(id);
//
//            try {
////   TODO             if (isJournalising()) {
////                    pmgr.commit();
////                }
//            } catch (YPersistenceException e) {
//                logger.warn("Persistence exception ignored (to be fixed)", e);
//            }
//        }
//    }


    //####################################################################################
    //                          ACCESSORS
    //####################################################################################
    /**
     * Provides the set of specification ids for specs loaded into the engine.  It returns
     * those that were loaded as well as those with running instances that are unloaded.
     *
     * @return a set of spec id strings.
     */
    public Set getSpecIDs() throws YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.getSpecIDs();
        }
    }


    /**
     * Returns a set of all loaded process specifications.
     *
     * @return  A set of specification ids
     */

    public Set getLoadedSpecifications() throws YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.getLoadedSpecifications();
        }
    }


    public YSpecification getSpecification(String specID) throws YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.getSpecification(specID);
        }
    }


    public YIdentifier getCaseID(String caseIDStr) throws YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.getCaseID(caseIDStr);
        }
    }


    public String getStateTextForCase(YIdentifier caseID) throws YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.getStateTextForCase(caseID);
        }
    }


    public String getStateForCase(YIdentifier caseID) throws YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.getStateForCase(caseID);
        }
    }


    public void registerInterfaceAClient(InterfaceAManagementObserver observer) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            super.registerInterfaceAClient(observer);
        }
    }

    public void registerInterfaceBObserver(InterfaceBClientObserver observer) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            super.registerInterfaceBObserver(observer);
        }
    }

    /**
     * Registers an InterfaceB Observer Gateway with the engine in order to receive callbacks.<P>
     *
     * @param gateway
     */
    public void registerInterfaceBObserverGateway(ObserverGateway gateway) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            super.registerInterfaceBObserverGateway(gateway);
        }
    }


    //################################################################################
    //   BEGIN REST-FUL SERVICE METHODS
    //################################################################################
    public Set<YWorkItem> getAvailableWorkItems() {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.getAvailableWorkItems();
        }
    }


    public String getProcessDefinition(String specID) throws YPersistenceException, YAWLException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.getProcessDefinition(specID);
        }
    }


    public YWorkItem getWorkItem(String workItemID) throws YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.getWorkItem(workItemID);
        }
    }


    public Set getAllWorkItems() throws YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.getAllWorkItems();
        }
    }


    /**
     * Starts a work item.  If the workitem param is enabled this method fires the task
     * and returns the first of its child instances in the exectuting state.
     * Else if the workitem is fired then it moves the state from fired to exectuing.
     * Either way the method returns the resultant work item.
     *
     * @param workItem the enabled, or fired workitem.
     * @param userID   the user id.
     * @return the resultant work item in the executing state.
     * @throws YStateException     if the workitem is not in either of these
     *                             states.
     * @throws YDataStateException
     */
    public YWorkItem startWorkItem(String workItemId, String userID) throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.startWorkItem(workItemId, userID);
        }
    }


    /**
     * Returns the task definition, not the task instance.
     *
     * @param specificationID the specification id
     * @param taskID          the task id
     * @return the task definition object.
     */
    public YTask getTaskDefinition(String specificationID, String taskID) throws YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.getTaskDefinition(specificationID, taskID);
        }
    }


    /**
     * Completes the work item.
     *
     * @param workItem
     * @param data
     * @throws YStateException
     */
    public void completeWorkItem(String workItemId, String data, boolean force)
            throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            super.completeWorkItem(workItemId, data, force);
        }
    }


    /**
     * Determines whether or not a task will aloow a dynamically
     * created new instance to be created.  MultiInstance Task with
     * dyanmic instance creation.
     *
     * @param workItemID the workItemID of a sibling work item.
     * @throws YStateException if task is not MultiInstance, or
     *                         if task does not allow dynamic instance creation,
     *                         or if current number of instances is not less than the maxInstances
     *                         for the task.
     */
    public void checkElegibilityToAddInstances(String workItemID) throws YStateException, YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            super.checkElegibilityToAddInstances(workItemID);
        }
    }


    /**
     * Creates a new work item instance when possible.
     *
     * @param workItem                the id of a work item inside the task to have a new instance.
     * @param paramValueForMICreation format "<data>[InputParam]*</data>
     *                                InputParam == <varName>varValue</varName>
     * @return the work item of the new instance.
     * @throws YStateException if the task is not able to create a new instance, due to
     *                         its state or its design.
     */
    public YWorkItem createNewInstance(String workItemID, String paramValueForMICreation) throws YStateException, YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.createNewInstance(workItemID, paramValueForMICreation);
        }
    }

    public YWorkItem suspendWorkItem(String workItemID) throws YStateException, YPersistenceException {

        synchronized (mutex) {
           return super.suspendWorkItem(workItemID);
        }
    }
    public YWorkItem unsuspendWorkItem(String workItemID) throws YStateException, YPersistenceException {

        synchronized (mutex) {
           return super.unsuspendWorkItem(workItemID);
        }
    }


    // rolls back a workitem from executing to fired
    public void rollbackWorkItem(String workItemID, String userName) throws YStateException, YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            super.rollbackWorkItem(workItemID, userName);
        }
    }

    public String launchCase(String username, String specID, String caseParams, URI completionObserver) throws YStateException, YDataStateException, YSchemaBuildingException, YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.launchCase(username, specID, caseParams, completionObserver);
        }
    }


    public void cancelCase(YIdentifier id) throws YPersistenceException {
    /**
     * SYNC'D External interface
     */
    	synchronized (mutex) {
    		cancelCase(id);
    	}
	}

	public void addExceptionObserver(InterfaceX_EngineSideClient ix) {
		super.addExceptionObserver((ExceptionGateway)ix);
	}

	public void addExceptionObserver(String observerURI){
		super.addExceptionObserver((ExceptionGateway)new InterfaceX_EngineSideClient(observerURI));
	}


	public void removeExceptionObservers() {
		super.removeExceptionObservers();

	}


    /**
     * Given a process specification id return the cases that are its running
     * instances.
     *
     * @param specID the process specification id string.
     * @return a set of YIdentifer caseIDs that are run time instances of the
     *         process specification with id = specID
     */
    public Set getCasesForSpecification(String specID) throws YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.getCasesForSpecification(specID);
        }
    }


    public YAWLServiceReference getRegisteredYawlService(String yawlServiceID) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.getRegisteredYawlService(yawlServiceID);
        }
    }


    /**
     * Returns a set of YAWL service references registered in the engine.
     *
     * @return the set of current YAWL services
     */
    public Set getYAWLServices() throws YPersistenceException{
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
        	List<YAWLServiceReference> refs = getDao().retrieveByRestriction( YAWLServiceReference.class, new Unrestricted() );
        	Set<YAWLServiceReference> references = new HashSet<YAWLServiceReference>();
        	for( YAWLServiceReference ref : refs ) {
        		if (ref.getEnabled()) {
        			references.add( ref );
        		}
        	}
        	return references;

        }
    }


    /**
     * Adds a YAWL service to the engine.<P>
     *
     * AJH - MODIFIED FOR TRANSACTIONAL PERSISTENCE
     *
     * @param yawlService
     */
    public void addYawlService(YAWLServiceReference yawlService) throws YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            super.addYawlService(yawlService);
        }
    }


    public Set getChildrenOfWorkItem(YWorkItem workItem) throws YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.getChildrenOfWorkItem(workItem);
        }
    }


    /**
     * Announces an enabled task to a YAWL service.  This is a classic push style
     * interaction where the Engine pushes the work item out into the
     * YAWL service.
     * PRE: the YAWL service exists and is on line.
     *
     * @param yawlService the YAWL service
     * @param item        the work item must be enabled.
     */
    protected void announceEnabledTask(URI yawlService, YWorkItem item) {
        logger.debug("Announcing enabled task " + item.getIDString() + " on service " + yawlService.toString());
        observerGatewayController.notifyAddWorkItem(yawlService, item);
    }


    public void announceCancellationToEnvironment(URI yawlService, YWorkItem item) {
        logger.debug("Announcing task cancellation " + item.getIDString() + " on service " + yawlService.toString());
        observerGatewayController.notifyRemoveWorkItem(yawlService, item);
    }

    protected void announceCaseCompletionToEnvironment(URI yawlService, YIdentifier caseID, Document casedata) {
        observerGatewayController.notifyCaseCompletion(yawlService, caseID, casedata);
    }


    public Set getUsers() {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.getUsers();
        }
    }


    /**
     * Returns a list of the YIdentifiers objects for running cases.
     *
     * @return the case ids of the current unfinished processes.
     * @ deprecated this method is not being used
     */
//    public List getRunningCaseIDs() {
//        return new ArrayList(_runningCaseIDToSpecIDMap.keySet());
//    }

    /**
     *
     */
    public String getLoadStatus(String specID) throws YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.getLoadStatus(specID);
        }

    }

    /**
     *
     *
     * AJH - MODIFIED FOR TRANSACTIONAL PERSISTENCE
     *
     * @param serviceURI
     * @return
     * @throws YPersistenceException
     */
    public YAWLServiceReference removeYawlService(String serviceURI) throws YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return super.removeYawlService(serviceURI);
        }
    }

    /**
     * Indicates if persistence is to the database.
     *
     * @return if the engine if configured to store data in persistence.
     */
    public boolean isJournalising() {
        return journalising;
    }

    /**
     * Indicates if persistence is to the database.
     *
     * @param arg
     */
    public void setJournalising(boolean arg) {
        AbstractEngine.setPersistence(arg);
    }

    /**
     * Performs a diagnostic dump of the engine internal tables and state to trace.<P>
     */
    public void dump() {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            super.dump();
        }
    }

// TODO   private static SessionFactory getPMSessionFactory() {
//        return factory;
//    }

    /**
     * Public interface to allow engine clients to ask the engine to store an object reference in its
     * persistent storage. It does this in its own transaction block.<P>
     *
     * @param obj
     * @throws YPersistenceException
     */
    public void storeObject(Object obj) throws YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            super.storeObject(obj);
        }
    }


    /**
     * Returns the next available case number.<P>
     *
     * Note: This method replaces that previously included within YPersistance.
     *
     */
    public String getMaxCase() {
// TODO       if (!isJournalising()) {
            maxcase++;
            return Integer.toString(maxcase);
//        } else {
// TODO           YPersistenceManager pmgr = new YPersistenceManager(getPMSessionFactory());
//            return pmgr.getMaxCase();
//        }
    }

    /**
     * AJH: Stub method for testing revised persistence mechanism
     * @param caseID
     */
    private void persistCase(YIdentifier caseID) {
        logger.debug("--> persistCase: CaseID = " + caseID.getId());
        logger.debug("<-- persistCase");
    }

    /************************************************************************/

    /**
     * The following methods have been added to support the exception service
     */

    public boolean updateWorkItemData(String workItemID, String data) {
        synchronized (mutex) {
            return super.updateWorkItemData(workItemID, data);
        }
    }


    public boolean updateCaseData(String idStr, String data) throws YPersistenceException {
        synchronized (mutex) {
            return super.updateCaseData(idStr, data);
        }
    }


    public void cancelWorkItem(String workItemID, boolean statusFail) throws YPersistenceException {
    	
    	YWorkItem workItem = getWorkItem(workItemID);
    	
        synchronized (mutex) {
            super.cancelWorkItem(workItem, statusFail);
        }
    }


    protected void announceCheckWorkItemConstraints(YWorkItem item, Document data, boolean preCheck) {

    	/*
    	 * Parse through available exception handler services
    	 * */
    	for (int i = 0; i < _exceptionObservers.size();i++) {

    		ExceptionGateway ixClient = _exceptionObservers.get(i);
    		logger.debug("Announcing Check Constraints for task " + ((item == null) ? "{null}" : item.getIDString()) +
    				" on client " + ixClient.toString());
    		ixClient.announceCheckWorkItemConstraints(item, data, preCheck);
    	}
    }


    protected void announceCheckCaseConstraints(String specID, String caseID, String data, boolean preCheck) {
    	for (int i = 0; i < _exceptionObservers.size();i++) {

    		ExceptionGateway ixClient = _exceptionObservers.get(i);logger.debug("Announcing Check Constraints for case " + caseID +
                     " on client " + ixClient.toString());
    		ixClient.announceCheckCaseConstraints(specID, caseID, data, preCheck);
    	}
    }


    public void announceCancellationToExceptionService(YIdentifier caseID) {
    	for (int i = 0; i < _exceptionObservers.size();i++) {

    		ExceptionGateway ixClient = _exceptionObservers.get(i);logger.debug("Announcing Cancel Case for case " + caseID.toString() +
                     " on client " + ixClient.toString());
            ixClient.announceCaseCancellation(caseID.toString());
    	}
    }


    public void announceTimeOutToExceptionService(YWorkItem item, List timeOutTaskIds) {
    	for (int i = 0; i < _exceptionObservers.size();i++) {

    		ExceptionGateway ixClient = _exceptionObservers.get(i);
    		logger.debug("Announcing Time Out for item " + item.getIDString() +
                     " on client " + ixClient.toString());
            ixClient.announceTimeOut(item, timeOutTaskIds);
    	}
    }

    public void announceServiceUnavailable(YWorkItem item, URI ref) {
    	for (int i = 0; i < _exceptionObservers.size();i++) {

    		ExceptionGateway ixClient = _exceptionObservers.get(i);
    		logger.debug("Announcing Service unavailability for item " + item.getIDString() +
                     " on client " + ixClient.toString());
            ixClient.announceServiceUnavailable(item, ref);
    	}
    }

    public void announceServiceError(YWorkItem item, URI ref) {
    	for (int i = 0; i < _exceptionObservers.size();i++) {

    		ExceptionGateway ixClient = _exceptionObservers.get(i);
    		logger.debug("Announcing Service unavailability for item " + item.getIDString() +
                     " on client " + ixClient.toString());
            ixClient.announceServiceUnavailable(item, ref);
    	}
    }
    
    private List<ServiceHandler> servicenotifications = new ArrayList<ServiceHandler>();
    
    public void addServiceNotification(ServiceHandler s) {
    	servicenotifications.add(s);
    	
    }
    
    public void executeServiceNotifications() {
    	for (int i = 0; i < servicenotifications.size();i++) {
    		servicenotifications.get(i).start();
    	}
    	servicenotifications.clear();
    }
    
    
}