/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine;

import org.yawlfoundation.yawl.admintool.model.HumanResource;
import org.yawlfoundation.yawl.authentication.UserList;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.elements.state.YInternalCondition;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EngineBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBClientObserver;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBInterop;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceADesign;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceAManagement;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceAManagementObserver;
import org.yawlfoundation.yawl.engine.interfce.interfaceX.InterfaceX_EngineSideClient;
import org.yawlfoundation.yawl.engine.ObserverGateway;
import org.yawlfoundation.yawl.engine.ObserverGatewayController;
import org.yawlfoundation.yawl.engine.time.YWorkItemTimer;
import org.yawlfoundation.yawl.engine.time.YTimer;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.logging.YEventLogger;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.*;
import java.net.URI;
import java.util.*;

import org.yawlfoundation.yawl.util.*;

/**
 *
 *
 * @author Lachlan Aldred
 *         Date: 17/06/2003
 *         Time: 13:46:54
 *
 */
public class YEngine implements InterfaceADesign,
        InterfaceAManagement,
        InterfaceBClient,
        InterfaceBInterop {
    public static final int ENGINE_STATUS_INITIALISING = 0;
    public static final int ENGINE_STATUS_RUNNING = 1;
    public static final int ENGINE_STATUS_TERMINATING = 2;

    /**
     * Reannoucements are being posted due an extra-engine request.
     */
    public static final int REANNOUNCEMENT_CONTEXT_NORMAL  = 0;

    /**
     * Reannouncements are being posted due to restart processing within the engine.
     * Note: In this context, the underlying engine status may be running rather than initialising!
     */
    public static final int REANNOUNCEMENT_CONTEXT_RECOVERING = 1;
    
	private static final boolean ENGINE_PERSISTS_BY_DEFAULT = false;
    private static Logger logger;

    private YSpecificationMap _specifications = new YSpecificationMap();
    protected Map<YIdentifier, YNetRunner> _caseIDToNetRunnerMap = new HashMap<YIdentifier, YNetRunner>();
    private Map<YIdentifier,YSpecification> _runningCaseIDToSpecMap = new HashMap<YIdentifier, YSpecification>(); //todo MLF: changed to direct refence to spec, should it be YSpecID?
    private static YWorkItemRepository _workItemRepository;
    private static YEngine _myInstance;
    private InterfaceAManagementObserver _interfaceAClient;
    private InterfaceBClientObserver _interfaceBClient;
    private ObserverGatewayController observerGatewayController = null;
    private Map _yawlServices = new HashMap();
    private static YEventLogger yawllog = null;
    private static UserList _userList;
    private Map _unloadedSpecifications = new HashMap();
    private final Object mutex = new Object();
    private int engineStatus;
    private int reannouncementContext;
    private boolean workItemsAnnounced = false;

    private static InterfaceX_EngineSideClient _exceptionObserver = null ;
    private YAWLServiceReference _resourceObserver = null ;

    private static boolean persisting;
    private static boolean restoring;
    private static int _nextCaseNbr = 1;
    private static SessionFactory factory = null;
    private static final String _yawlVersion = "2.0" ;

    /**
     * AJH: Switch indicating if we generate user interface attributes with a tasks output XML doclet.
     */
    private static boolean generateUIMetaData = false;

    /**
     * Constructor.
     */
    protected YEngine() {
        yawllog = YEventLogger.getInstance();
        observerGatewayController = new ObserverGatewayController();

        /**
         * Initialise the standard Observer Gateways.
         *
         * Currently the only standard gateway is the HTTP driven Servlet client.
         */
        ObserverGateway stdHttpObserverGateway = new InterfaceB_EngineBasedClient();
        observerGatewayController.addGateway(stdHttpObserverGateway);
    }


    protected List addSpecifications(String specID, String uri) throws YPersistenceException {
        try {
            return addSpecifications(new File(uri), true, new Vector());
        } catch (Exception e) {
            throw new YPersistenceException("Failure whilst restoring specification [" + specID + "]", e);
        }
    }

    protected void addRunner(YNetRunner runner) {
        String specID = runner.getYNetID();
        double version = runner.getYNetVersion();
        YSpecification specification = (YSpecification) _specifications.getSpecification(specID, version);
        if (specification != null) {
            /*
              initialise the runner
             */
            runner.setEngine(this);
            runner.restoreprepare();
            _caseIDToNetRunnerMap.put(runner.getCaseID(), runner);
            _runningCaseIDToSpecMap.put(runner.getCaseID(), specification);

            if (_interfaceBClient != null) {
                _interfaceBClient.addCase(specID, runner.getCaseID().toString());
            }
        }

    }

    /**
     * Restore the engine state from perstistent storage.<P>
     *
     * @throws YPersistenceException
     */
    private void restore(YPersistenceManager pmgr) throws YPersistenceException {
        Vector runners = new Vector();
        HashMap runnermap = new HashMap();
        Map idtoid = new HashMap();

        logger.debug("--> restore");

        try {
            restoring = true;

            logger.info("Restoring Users");
            Query query = pmgr.createQuery("from org.yawlfoundation.yawl.admintool.model.Resource" +
                    " where IsOfResourceType = 'Human'");

            for (Iterator it = query.iterate(); it.hasNext();) {
                HumanResource user = (HumanResource) it.next();
                logger.debug("Restoring user '" + user.getRsrcID() + "'");
                UserList.getInstance().addUser(
                        user.getRsrcID(),
                        user.getPassword(),
                        user.getIsAdministrator());
            }

            //Lachlan: later we delete these loaded services and reload them anew
            //this seems kind of redundant, don't you think?
            logger.info("Restoring Services");
            query = pmgr.createQuery("from org.yawlfoundation.yawl.elements.YAWLServiceReference");

            for (Iterator it = query.iterate(); it.hasNext();) {
                YAWLServiceReference service = (YAWLServiceReference) it.next();
                addYawlService(service);
            }

            logger.info("Restoring Specifications - Starts");
            query = pmgr.createQuery("from org.yawlfoundation.yawl.engine.YSpecFile");

            for (Iterator it = query.iterate(); it.hasNext();) {
                YSpecFile spec = (YSpecFile) it.next();
                String xml = spec.getXML();

                {
                    logger.debug("Restoring specification " + spec.getSpecid().getId());

                    File f = File.createTempFile("yawltemp", null);
                    BufferedWriter buf = new BufferedWriter(new FileWriter(f));
                    buf.write(xml, 0, xml.length());
                    buf.close();
                    addSpecifications(spec.getSpecid().getId(), f.getAbsolutePath());

                    f.delete();
                }
            }
            logger.info("Restoring Specifications - Ends");

            logger.info("Restoring process instances - Starts");
            query = pmgr.createQuery("from org.yawlfoundation.yawl.engine.YNetRunner order by case_id");
            for (Iterator it = query.iterate(); it.hasNext();) {
                YNetRunner runner = (YNetRunner) it.next();
                /**
                 * FRA-67 - Set engine reference for parent and composite nets
                 */
                runner.setEngine(this);
                // FRA-67: End
                runners.add(runner);
            }

            _nextCaseNbr = pmgr.getNextCaseNbr();

//            HashMap map = new HashMap();
//            for (int i = 0; i < runners.size(); i++) {
//                YNetRunner runner = (YNetRunner) runners.get(i);
//                String id = runner.get_caseID();
//                query = pmgr.createQuery("select from org.yawlfoundation.yawl.logging.YCaseEvent where case_id = '" + id + "'");
//                for (Iterator it = query.iterate(); it.hasNext();) {
//                    YCaseEvent ylogid = (YCaseEvent) it.next();
//                    map.put(ylogid.get_caseID(), ylogid);
//                }
//            }
//            YEventLogger.getInstance().setListofcases(map);

            int checkedrunners = 0;

            Vector storedrunners = (Vector) runners.clone();

            while (checkedrunners < runners.size()) {

                for (int i = 0; i < runners.size(); i++) {
                    YNetRunner runner = (YNetRunner) runners.get(i);

                    if (runner.getContainingTaskID() == null) {

                        //This is a root net runner
                        YSpecification specification = getSpecification(runner.getYNetID(), runner.getYNetVersion());
                        if (specification != null) {
                            YNet net = (YNet) specification.getRootNet().clone();
                            runner.setNet(net);

                            runnermap.put(runner.get_standin_caseIDForNet().toString(), runner);
                        } else {
                            /* This occurs when a specification has been unloaded, but the case is still there
                               This case is not persisted, since we must have the specification stored as well.
                             */
                            // todo AJH Sort this
                            pmgr.deleteObject(runner);
                            storedrunners.remove(runner);
                            //MLF: remove all the workitems for this case
                            String wiquery = "DELETE FROM YWorkItem WHERE (" +
                                             "id like '" + runner.get_caseID() + ":%" + "') OR (" +
                                             "id like '" + runner.get_caseID() + ".%:%" + "')";
                            logger.debug("Deleting work_items on query " + wiquery);
                            pmgr.getSession().createQuery(wiquery).executeUpdate();

                        }
                        checkedrunners++;

                    } else {
                        //This is not a root net, but a decomposition

                        // Find the parent runner
                        String myid = runner.get_standin_caseIDForNet().toString();
                        String parentid = myid.substring(0, myid.lastIndexOf("."));


                        YNetRunner parentrunner = (YNetRunner) runnermap.get(parentid);

                        if (parentrunner != null) {
                            logger.debug("Restoring composite YNetRunner: " + parentrunner.get_caseID());
                            YNet parentnet = parentrunner.getNet();

                            YCompositeTask task = (YCompositeTask) parentnet.getNetElement(runner.getContainingTaskID());
                            runner.setTask(task);

                            YNet net = (YNet) task.getDecompositionPrototype().clone();
                            runner.setNet(net);
                            runnermap.put(runner.get_standin_caseIDForNet().toString(), runner);

                            checkedrunners++;
                        }
                        else
                        {
                            logger.error("No parent netrunner found for parentid '" + parentid + "'.");
                        }

                    }
                }
            }

            runners = storedrunners;

            for (int i = 0; i < runners.size(); i++) {

                YNetRunner runner = (YNetRunner) runners.get(i);

                YNet net = runner.getNet();


                P_YIdentifier pid = runner.get_standin_caseIDForNet();

                if (runner.getContainingTaskID() == null) {
                    // This is a root net runner
                    YIdentifier id = restoreYID(pmgr, runnermap, idtoid, pid, null, runner.getYNetID(), net);
                    runner.set_caseIDForNet(id);
                    addRunner(runner);
                }

                YIdentifier yid = new YIdentifier(runner.get_caseID());
                YWorkItemRepository.getInstance().setNetRunnerToCaseIDBinding(runner, yid);

                Set busytasks = runner.getBusyTaskNames();

                for (Iterator busyit = busytasks.iterator(); busyit.hasNext();) {
                    String name = (String) busyit.next();
                    YExternalNetElement element = net.getNetElement(name);

                    runner.addBusyTask(element);
                }

                Set enabledtasks = runner.getEnabledTaskNames();

                for (Iterator enabit = enabledtasks.iterator(); enabit.hasNext();) {
                    String name = (String) enabit.next();
                    YExternalNetElement element = net.getNetElement(name);

                    if (element instanceof YTask) {
                        YTask externalTask = (YTask) element;
                        runner.addEnabledTask(externalTask);
                    }

                }
            }

            // restore case & exception observers (where they exist)
            for (int i = 0; i < runners.size(); i++) {
                YNetRunner runner = (YNetRunner) runners.get(i);
                runner.restoreObservers();
            }

            logger.info("Restoring process instances - Ends");

            logger.info("Restoring work items - Starts");
            query = pmgr.createQuery("from org.yawlfoundation.yawl.engine.YWorkItem");
            for (Iterator it = query.iterate(); it.hasNext();) {
                YWorkItem witem = (YWorkItem) it.next();

                String data = witem.getDataString();
                if (data != null)
                    witem.setInitData(JDOMUtil.stringToElement(data));
                

                java.util.StringTokenizer st = new java.util.StringTokenizer(witem.get_thisID(), ":");
                String caseandid = st.nextToken();
//				MLR (02/11/07): next two lines commented out as used by M2 to assist in debugging only
                //java.util.StringTokenizer st2 = new java.util.StringTokenizer(caseandid, ".");
                //String caseid = st2.nextToken();
                String taskid = st.nextToken();
                // AJH: Strip off unique ID to obtain our taskID
                {
                    java.util.StringTokenizer st3 = new java.util.StringTokenizer(taskid, "!");
                    taskid = st3.nextToken();
                }
                YIdentifier workitemid = (YIdentifier) idtoid.get(caseandid);
                if (workitemid != null) {
                    witem.setWorkItemID(new YWorkItemID(workitemid, taskid));
                    witem.addToRepository();
                } else {
                    pmgr.deleteObject(witem);
                }
            }
            logger.info("Restoring work items - Ends");

            logger.info("Restoring work item timers - Starts");
            query = pmgr.createQuery("from org.yawlfoundation.yawl.engine.time.YWorkItemTimer");
            for (Iterator it = query.iterate(); it.hasNext();) {
                YWorkItemTimer witemTimer = (YWorkItemTimer) it.next();

                // check to see if workitem still exists
                YWorkItem witem = getWorkItem(witemTimer.getOwnerID()) ;
                if (witem == null)
                    deleteObject(witemTimer) ;          // remove from persistence
                else {
                     long endTime = witemTimer.getEndTime();

                    // if the deadline has passed, time the workitem out
                    if (endTime < System.currentTimeMillis())
                        witemTimer.handleTimerExpiry();
                    else
                        // reschedule the workitem's timer
                        YTimer.getInstance().schedule(witemTimer, new Date(endTime));
                }
            }    
            logger.info("Restoring work item timers - Ends");

            /*
              Start net runners. This is a restart of a NetRunner not a clean start, therefore, the net runner should not create any new work items, if they have already been created.
             */
            logger.info("Restarting restored process instances - Starts");

            for (int i = 0; i < runners.size(); i++) {
                YNetRunner runner = (YNetRunner) runners.get(i);
                logger.debug("Restarting " + runner.get_caseID());
                runner.start(pmgr);
            }
            logger.info("Restarting restored process instances - Ends");

            restoring = false;

            logger.info("Restore completed OK");

            if (logger.isDebugEnabled()) {
                dump();
            }

        } catch (Exception e) {
            throw new YPersistenceException("Failure whilst restoring engine session", e);
        }
    }


    public YIdentifier restoreYID(YPersistenceManager pmgr, HashMap runnermap, Map idtoid, P_YIdentifier pid, YIdentifier father, String specname, YNet net) throws YPersistenceException {

        YIdentifier id = new YIdentifier(pid.toString());

        YNet sendnet = net;

        id.set_father(father);

        List list = pid.get_children();

        if (list.size() > 0) {
            List idlist = new Vector();

            for (int i = 0; i < list.size(); i++) {
                P_YIdentifier child = (P_YIdentifier) list.get(i);

                YNetRunner netRunner = (YNetRunner) runnermap.get(child.toString());
                if (netRunner != null) {
                    sendnet = netRunner.getNet();
                }
                YIdentifier caseid = restoreYID(pmgr, runnermap, idtoid, child, id, specname, sendnet);

                if (netRunner != null) {
                    netRunner.set_caseIDForNet(caseid);
                }

                idlist.add(caseid);
            }

            id.set_children(idlist);
        }


        for (int i = 0; i < pid.getLocationNames().size(); i++) {

            String name = (String) pid.getLocationNames().get(i);
            YExternalNetElement element = net.getNetElement(name);

            if (element == null) {
                name = name.substring(0, name.length() - 1);
                String[] splitname = name.split(":");


                /*
                  Get the task associated with this condition
                */
                YTask task = null;
                if (name.indexOf("CompositeTask") != -1) {
                    YNetRunner netRunner_temp = (YNetRunner) runnermap.get(father.toString());
                    task = (YTask) netRunner_temp.getNet().getNetElement(splitname[1]);
                } else {
                    task = (YTask) net.getNetElement(splitname[1]);
                }

                /**
                 * FRA-66: Check if we need to find the father task and post conditions against it
                 */
                if ((task == null) && (father != null))

                {
//                    logger.debug("++++++++++++++++++++++++++++++ Frig on " + splitname[1] + " ++++++++++++++++");
                    YNetRunner netRunner_temp = (YNetRunner) runnermap.get(father.toString());

                    // FRA-70 Start
                    Object obj = netRunner_temp.getNet().getNetElement(splitname[1]);
                    if (obj instanceof YTask)
                    {
                        task = (YTask)obj;
                    }
                    // FRA-70 End
                }
                // FRA-66: End
                if (task != null) {
                    logger.debug("Posting conditions on task " + task);
                    YInternalCondition condition;
                    if (splitname[0].startsWith(YInternalCondition._mi_active)) {

                        condition = task.getMIActive();
                        condition.add(pmgr, id);

                    } else if (splitname[0].startsWith(YInternalCondition._mi_complete)) {

                        condition = task.getMIComplete();
                        condition.add(pmgr, id);

                    } else if (splitname[0].startsWith(YInternalCondition._mi_entered)) {

                        condition = task.getMIEntered();
                        condition.add(pmgr, id);

                    } else if (splitname[0].startsWith(YInternalCondition._executing)) {

                        condition = task.getMIExecuting();
                        condition.add(pmgr, id);

                    } else {
                        logger.error("Unknown YInternalCondition state");
                    }
                } else {
                    if (splitname[0].startsWith("InputCondition")) {
                        net.getInputCondition().add(pmgr, id);
                    } else if (splitname[0].startsWith("OutputCondition")) {
                        net.getOutputCondition().add(pmgr, id);
                    }
                }
            } else {
                if (element instanceof YTask) {
                    ((YTask) element).setI(id);
                    ((YTask) element).prepareDataDocsForTaskOutput();

                } else if (element instanceof YCondition) {

                   ((YConditionInterface) element).add(pmgr, id);
                }
            }
        }

        idtoid.put(id.toString(), id);
        return id;
    }


    /**
     * *********************************************
     */


    public static YEngine getInstance(boolean persisting) throws YPersistenceException {
        if (_myInstance == null) {
            logger = Logger.getLogger("org.yawlfoundation.yawl.engine.YEngine");
            logger.debug("--> YEngine: Creating initial instance");
            _myInstance = new YEngine();
			_myInstance.setEngineStatus(YEngine.ENGINE_STATUS_INITIALISING);            
            YEngine.setPersisting(persisting);
            //todo TESTING
//            _myInstance.setPersisting(false);

            // Initialise the persistence layer
            factory = YPersistenceManager.initialise(persisting);
            /***************************
             START POSSIBLE RESTORATION PROCESS
             */

            _userList = UserList.getInstance();
            _workItemRepository = YWorkItemRepository.getInstance();

            if (isPersisting()) {
                YPersistenceManager pmgr = new YPersistenceManager(getPMSessionFactory());
                try {
                    pmgr.setRestoring(true);
                    pmgr.startTransactionalSession();
                    _myInstance.restore(pmgr);
                    pmgr.commit();
                    pmgr.setRestoring(false);
                    pmgr.closeSession();
                } catch (YPersistenceException e) {
                    logger.fatal("Failure to restart engine from persistence image", e);
                    throw new YPersistenceException("Failure to restart engine from persistence image");
                }
            }

            /***************************/
            /**
             * Delete and re-create the standard engine InterfaceB services
             */
            YAWLServiceReference ys;

            ys = new YAWLServiceReference("http://localhost:8080/yawlWSInvoker/", null);
            ys.setDocumentation("This YAWL Service enables suitably declared" +
                    " workflow tasks to invoke RPC style service on the Web.");
            ys.set_serviceName("yawlWSInvoker");
            _myInstance.removeYawlService(ys.getURI());
            _myInstance.addYawlService(ys);

            ys = new YAWLServiceReference("http://localhost:8080/workletService/ib", null);
            ys.setDocumentation("Worklet Dynamic Process Selection and Exception Service");
            ys.set_serviceName("workletService");
            _myInstance.removeYawlService(ys.getURI());
            _myInstance.addYawlService(ys);

            ys = new YAWLServiceReference("http://localhost:8080/yawlSMSInvoker/ib", null);
            ys.setDocumentation("SMS Message Module. Works if you have an account.");
            ys.set_serviceName("yawlSMSInvoker");
            _myInstance.removeYawlService(ys.getURI());
            _myInstance.addYawlService(ys);

            ys = new YAWLServiceReference("http://localhost:8080/timeService/ib", null);
            ys.setDocumentation("Time service, allows tasks to be a timeout task.");
            ys.set_serviceName("timeService");
            _myInstance.removeYawlService(ys.getURI());
            _myInstance.addYawlService(ys);

            ys = new YAWLServiceReference("http://localhost:8080/resourceService/ib", null);
            ys.setDocumentation("Resource Service, assigns workitems to resources.");
            ys.set_serviceName("resourceService");
            ys.set_assignable(false);                            // don't show in editor
            _myInstance.removeYawlService(ys.getURI());
            _myInstance.addYawlService(ys);
            _myInstance.setResourceService(ys);

            /**
             * Ensure we have an 'admin' user
             */
            try {
                UserList.getInstance().addUser("admin", "YAWL", true);
            } catch (YAuthenticationException e) {
                // User already exists ??? Do nothing
            }
            /**
             * Finally set engine status to up and running
             */
            logger.info("Marking engine status = RUNNING");
            _myInstance.setEngineStatus(YEngine.ENGINE_STATUS_RUNNING);
        }
        return _myInstance;
    }

    public static YEngine getInstance() {
        if (_myInstance == null) {
            try {
                _myInstance = getInstance(ENGINE_PERSISTS_BY_DEFAULT);
            } catch (Exception e) {
                throw new RuntimeException("Failure to instanciate an engine");
            }

            return _myInstance;
        } else {
            return _myInstance;
        }
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
    public List<YSpecificationID> addSpecifications(File specificationFile, boolean ignoreErors, List errorMessages) throws JDOMException, IOException, YPersistenceException {

        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            logger.debug("--> addSpecification: File=" + specificationFile.getAbsolutePath());

            List<YSpecificationID> returnIDs = new Vector<YSpecificationID>();
            List newSpecifications = null;
            String parsingMsg = null;
            try {
                //if there is an XML Schema problem _report it and abort
                newSpecifications = YMarshal.unmarshalSpecifications(specificationFile.getAbsolutePath());
            } catch (YSyntaxException e) {

                parsingMsg = e.getMessage();
                //catch the xml parsers exception,
                //transform it into YAWL format and abort the load
                for (StringTokenizer tokens = new StringTokenizer(parsingMsg, "\n"); tokens.hasMoreTokens();) {
                    String msg = tokens.nextToken();
                    errorMessages.add(new YVerificationMessage(null, msg, YVerificationMessage.ERROR_STATUS));
                }
                logger.debug("<-- addSpecifcations: syntax exceptions found");
                return returnIDs;
            } catch (YSchemaBuildingException e) {
                //logger.error("Could not build schema.", e);
                e.printStackTrace();//TODO: propagate
                //return null;
            }
            for (Iterator iterator = newSpecifications.iterator(); iterator.hasNext();) {
                YSpecification specification = (YSpecification) iterator.next();
                List messages = specification.verify();
                if (messages.size() > 0 && !ignoreErors) {
                    YMessagePrinter.printMessages(messages);
                    errorMessages.addAll(messages);
                }
                //if the error messages are empty or contain only warnings
                if (YVerificationMessage.containsNoErrors(errorMessages)) {
                    boolean success = loadSpecification(specification);
                    if (success) {
                        /*
                          INSERTED FOR PERSISTANCE
                         */
                        if (!restoring) {
                            logger.info("Persisting specification loaded from file " + specificationFile.getAbsolutePath());
                            YSpecFile yspec = new YSpecFile(specificationFile.getAbsolutePath());
                            yspec.getSpecid().setVersion(specification.getMetaData().getVersion());

                            if (persisting) {
                                YPersistenceManager pmgr = new YPersistenceManager(getPMSessionFactory());
                                try {
                                    pmgr.startTransactionalSession();
                                    pmgr.storeObject(yspec);
                                    pmgr.commit();
                                } catch (YPersistenceException e) {
                                    throw new YPersistenceException("Failrue whilst persisting new specification", e);
                                }
                            }
                        }
                        /******************/

                        returnIDs.add(specification.getSpecificationID());
                    } else {//the user has loaded the specification with identical id
                        errorMessages.add(new YVerificationMessage(this,
                                "There is a specification with an identical id to ["
                                + specification.getID() + "] already loaded into the engine.",
                                YVerificationMessage.ERROR_STATUS));
                    }
                }
            }

            logger.debug("<-- addSpecifications: " + returnIDs.size() + " IDs loaded");
            return returnIDs;
        }
    }


    public boolean loadSpecification(YSpecification spec) {

        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            if (!_specifications.contains(spec)) {
                _specifications.loadSpecification(spec);
                return true;
            }
            return false;
        }
     }

    protected YIdentifier startCase(String username, YPersistenceManager pmgr, String specID, String caseParams, URI completionObserver) throws YStateException, YSchemaBuildingException, YDataStateException, YPersistenceException, YQueryException
    {
        return startCase(username, pmgr, specID, caseParams, completionObserver, null);
    }

    protected YIdentifier startCase(String username, YPersistenceManager pmgr, String specID, String caseParams, URI completionObserver, String caseID) throws YStateException, YSchemaBuildingException, YDataStateException, YPersistenceException, YQueryException{
        SAXBuilder builder = new SAXBuilder();
        Element data = null;
        if(caseParams != null && !"".equals(caseParams)) {
            try {
                Document dirtyDoc;
                dirtyDoc = builder.build(new StringReader(caseParams));
                data = YDocumentCleaner.cleanDocument(dirtyDoc).getRootElement();
                
            } catch (Exception e) {
                YStateException f = new YStateException(e.getMessage());
                f.setStackTrace(e.getStackTrace());
                throw f;
            }
        }

        YSpecification specification = (YSpecification) _specifications.getSpecification(specID);
        if (specification != null) {
            YNetRunner runner = new YNetRunner(pmgr, specification.getRootNet(), data, caseID);
            
            // register exception service with the net runner (MJA 4/4/06)
            if (_exceptionObserver != null) {
                announceCheckCaseConstraints(_exceptionObserver, specID,
                                             runner.getCaseID().toString(), caseParams, true);
                runner.setExceptionObserver(_exceptionObserver);
            }

            if(completionObserver != null) {
                YAWLServiceReference observer = getRegisteredYawlService(completionObserver.toString());
                if (observer != null) {
                    runner.setObserver(observer);
                } else {
                    logger.warn("Completion observer: " + completionObserver + " is not a registered YAWL service.");
                }
            }

            /*
             * INSERTED FOR PERSISTANCE
             */
            if (!restoring) {
//AJH                yper.storeData(runner);
                if (pmgr != null) {
                    pmgr.storeObject(runner);
                }
            }

            // log case start event
            YIdentifier runnerCaseID = runner.getCaseID();
            yawllog.logCaseCreated(pmgr, runnerCaseID.toString(), username, specID);

            runner.continueIfPossible(pmgr);

            runner.start(pmgr);
            _caseIDToNetRunnerMap.put(runnerCaseID, runner);
            _runningCaseIDToSpecMap.put(runnerCaseID, specification);

            if (_interfaceBClient != null) {
                logger.debug("Asking client to add case " + runnerCaseID.toString());
                _interfaceBClient.addCase(specID, runnerCaseID.toString());
            }

            return runnerCaseID;
        } else {
            throw new YStateException(
                    "No specification found with ID [" + specID + "]");
        }
    }


    protected void finishCase(YPersistenceManager pmgr, YIdentifier caseIDForNet) throws YPersistenceException {
        logger.debug("--> finishCase: Case=" + caseIDForNet.get_idString());

        _caseIDToNetRunnerMap.remove(caseIDForNet);
        _runningCaseIDToSpecMap.remove(caseIDForNet);
        _workItemRepository.cancelNet(caseIDForNet);

        yawllog.logCaseCompleted(pmgr, caseIDForNet.toString());
        
        if (_interfaceBClient != null) {
            _interfaceBClient.removeCase(caseIDForNet.toString());
        }

        logger.debug("<-- finishCase");
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
    public void unloadSpecification(YSpecificationID specID) throws YStateException, YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            YPersistenceManager pmgr = null;
            YSpecification spec = _specifications.getSpecification(specID);

            if (isPersisting()) {
                pmgr = new YPersistenceManager(getPMSessionFactory());
                pmgr.startTransactionalSession();
            }
            logger.debug("--> unloadSpecification: ID=" + specID.getSpecName() + " Version=" + specID.getVersion());

            /**
             * AJH: Reject unload request if we have active cases using it
             */
            Iterator caseIDs = _runningCaseIDToSpecMap.keySet().iterator();
            while(caseIDs.hasNext())
            {
                YIdentifier caseID = (YIdentifier)caseIDs.next();
                YSpecification specForCase = _runningCaseIDToSpecMap.get(caseID);

                if (specForCase.equals(spec))
                {
                    if (isPersisting())
                    {
                        pmgr.rollbackTransaction();
                    }
                    throw new YStateException("Cannot unload specification with id [" + specID + "] as one or more cases are currently active against it.");
                }
            }
            

            if (_specifications.contains(specID)) {
                /* REMOVE FROM PERSISTANT STORAGE*/
                logger.info("Removing process specification " + specID);
                YSpecFile yspec = new YSpecFile();
                yspec.getSpecid().setId(specID.getSpecName());
                yspec.getSpecid().setVersion(specID.getVersion());

//AJH           yper.removeData(yspec);
                if (pmgr != null) {
                    pmgr.deleteObject(yspec);
                }

                _specifications.unloadSpecification(spec);
            } else {

                if (isPersisting()) {
                    pmgr.rollbackTransaction();
                }
                throw new YStateException(
                        "Engine contains no such specification with id [" +
                        specID + "].");
            }

            if (isPersisting()) {
                pmgr.commit();
            }
            logger.debug("<-- unloadSpecification");
        }
    }


    /**
     * Cancels a running case - Internal interface that requires reference to current transaction's persistence
     * manager object.<P>
     *
     * @param pmgr
     * @param id
     * @throws YPersistenceException
     */
    protected void cancelCase(YPersistenceManager pmgr, YIdentifier id) throws YPersistenceException {
        logger.debug("--> cancelCase");

        if (id == null) {
            throw new IllegalArgumentException("should not cancel case with a null id");
        }

        //todo AJH - Suspect NetRunner is also clearing from database ????
        logger.info("Deleting persisted process instance " + id);

        try {
            clearCase(pmgr, id);
            YNetRunner runner = (YNetRunner) _caseIDToNetRunnerMap.get(id);
            yawllog.logCaseCancelled(pmgr, id.toString());
            if (persisting) clearWorkItemsFromPersistence(pmgr, id);
            runner.cancel(pmgr);
            _workItemRepository.removeWorkItemsForCase(id);
            finishCase(pmgr, id);

            // announce cancellation to exception service (if required)
            if (_exceptionObserver != null)
                announceCancellationToExceptionService(_exceptionObserver, id) ;

        } catch (YPersistenceException e) {
            throw new YPersistenceException("Failure whilst persisting case cancellation", e);
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
    public void cancelCase(YIdentifier id) throws YPersistenceException, YEngineStateException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            if (getEngineStatus() != ENGINE_STATUS_RUNNING)
            {
                throw new YEngineStateException("Unable to accept request as engine not in correct state: Current state = " + getEngineStatus());
            }

            YPersistenceManager pmgr = null;

            if (isPersisting()) {
                pmgr = new YPersistenceManager(getPMSessionFactory());
                pmgr.startTransactionalSession();
            }

            cancelCase(pmgr, id);

            try {
                if (isPersisting()) {
                    pmgr.commit();
                    pmgr = null;
                }
            } catch (YPersistenceException e) {
                logger.warn("Persistence exception ignored (to be fixed)", e);
            }
        }
    }

    /**
     * Removes the workitems of the runner from persistence (after a case cancellation).
     *
     * @param pmgr - a (non-null) YPersistence object
     * @param id - the caseID for this case
     * @throws YPersistenceException
     */
    private void clearWorkItemsFromPersistence(YPersistenceManager pmgr,
                                               YIdentifier id)
                                               throws YPersistenceException{
        YWorkItem item ;
        List items = _workItemRepository.getWorkItemsForCase(id);

        // clear child items first (to avoid foreign key constraint exceptions)
        Iterator itrChild = items.iterator();
        while (itrChild.hasNext()) {
            item = (YWorkItem) itrChild.next() ;
            if (! item.getStatus().equals(YWorkItemStatus.statusIsParent)) {
                pmgr.deleteObject(item);
            }
        }

        // now clear any parents
        Iterator itrParent = items.iterator();
        while (itrParent.hasNext()) {
            item = (YWorkItem) itrParent.next() ;
            if (item.getStatus().equals(YWorkItemStatus.statusIsParent)) {
                pmgr.deleteObject(item);
            }
        }
    }


    //####################################################################################
    //                          ACCESSORS
    //####################################################################################
    /**
     * Provides the set of specification ids for specs loaded into the engine.  It returns
     * those that were loaded as well as those with running instances that are unloaded.
     *
     * @return a set of spec id strings.
     */
    public Set<YSpecificationID> getSpecIDs() {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            return _specifications.getSpecIDs();        }
    }


    /**
     * Returns a set of all loaded process specifications.
     *
     * @return  A set of specification ids
     */

    public Set getLoadedSpecifications() {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            return _specifications.getSpecIDs();
        }
    }


    public YSpecification getSpecification(String specID) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            logger.debug("--> getSpecification: ID=" + specID);

            if (_specifications.contains(specID)) {
                logger.debug("<-- getSpecification: Loaded spec");
                return _specifications.getSpecification(specID);
            } else {
                logger.debug("<-- getSpecification: Unknown spec");
                return null;
            }
        }
    }

 public YSpecification getSpecification(YSpecificationID specID) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            logger.debug("--> getSpecification: ID=" + specID.toString());

            if (_specifications.contains(specID)) {
                logger.debug("<-- getSpecification: Loaded spec");
                return _specifications.getSpecification(specID);
            } else {
                logger.debug("<-- getSpecification: Unknown spec");
                return null;
            }
        }
    }

    public YSpecification getSpecificationForCase(YIdentifier caseID)
    {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            if(_runningCaseIDToSpecMap.containsKey(caseID))
            {
                return _runningCaseIDToSpecMap.get(caseID);
            }

            return null;
        }
    }

    public YSpecification getSpecification(String specID, double version) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            logger.debug("--> getSpecification: ID=" + specID);

            if (_specifications.contains(specID, version)) {
                logger.debug("<-- getSpecification: Loaded spec");
                return _specifications.getSpecification(specID, version);
            } else {
                logger.debug("<-- getSpecification: Unknown spec");
                return null;
            }
        }
    }

    public YIdentifier getCaseID(String caseIDStr) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            logger.debug("--> getCaseID");

            Set idSet = _caseIDToNetRunnerMap.keySet();
            for (Iterator idSetIter = idSet.iterator(); idSetIter.hasNext();) {
                YIdentifier identifier = (YIdentifier) idSetIter.next();
                if (identifier.toString().equals(caseIDStr)) {
                    return identifier;
                }
            }
            return null;
        }
    }


    public String getStateTextForCase(YIdentifier caseID) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            logger.debug("--> getStateTextForCase: ID=" + caseID.get_idString());

            Set allChildren = caseID.getDescendants();
            Set allLocations = new HashSet();
            for (Iterator childIter = allChildren.iterator(); childIter.hasNext();) {
                YIdentifier identifier = (YIdentifier) childIter.next();
                allLocations.addAll(identifier.getLocations());
            }
            StringBuffer stateText = new StringBuffer();
            stateText.append("#######################################" +
                    "######################\r\n" + "CaseID: ")
                    .append(caseID)
                    .append("\r\n" + "Spec:   ")
                    .append(_runningCaseIDToSpecMap.get(caseID))
                    .append("\r\n" + "###############################" +
                    "##############################\r\n");
            for (Iterator locationsIter = allLocations.iterator(); locationsIter.hasNext();) {
                YNetElement element = (YNetElement) locationsIter.next();
                if (element instanceof YCondition) {
                    stateText.append("CaseIDs in: ")
                            .append(element.toString())
                            .append("\r\n");
                    List identifiers = ((YConditionInterface) element).getIdentifiers();
                    stateText.append("\thashcode ")
                            .append(element.hashCode())
                            .append("\r\n");
                    for (Iterator idIter = identifiers.iterator(); idIter.hasNext();) {
                        YIdentifier identifier = (YIdentifier) idIter.next();
                        stateText.append("\t")
                                .append(identifier.toString())
                                .append("\r\n");
                    }
                } else if (element instanceof YTask) {
                    stateText.append("CaseIDs in: ")
                            .append(element.toString())
                            .append("\r\n");
                    YTask task = (YTask) element;
                    for (int i = 0; i < 4; i++) {
                        YInternalCondition internalCondition = null;
                        if (i == 0) {
                            internalCondition = task.getMIActive();
                        }
                        else if (i == 1) {
                            internalCondition = task.getMIEntered();
                        }
                        else if (i == 2) {
                            internalCondition = task.getMIExecuting();
                        }
                        else {//(i == 3)
                            internalCondition = task.getMIComplete();
                        }
                        if (internalCondition.containsIdentifier()) {
                            stateText.append("\t")
                                    .append(internalCondition.toString())
                                    .append("\r\n");
                            List identifiers = internalCondition.getIdentifiers();
                            for (Iterator iterator = identifiers.iterator(); iterator.hasNext();) {
                                YIdentifier identifier = (YIdentifier) iterator.next();
                                stateText.append("\t\t")
                                        .append(identifier.toString())
                                        .append("\r\n");
                            }
                        }
                    }
                }
            }
            return stateText.toString();
        }
    }


    public String getStateForCase(YIdentifier caseID) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            Set allChildren = caseID.getDescendants();
            Set allLocations = new HashSet();
            for (Iterator childIter = allChildren.iterator(); childIter.hasNext();) {
                YIdentifier identifier = (YIdentifier) childIter.next();
                allLocations.addAll(identifier.getLocations());
            }
            StringBuffer stateText = new StringBuffer();
            stateText.append("<caseState " + "caseID=\"")
                    .append(caseID)
                    .append("\" " + "specID=\"")
                    .append(_runningCaseIDToSpecMap.get(caseID))
                    .append("\">");
            for (Iterator locationsIter = allLocations.iterator(); locationsIter.hasNext();) {
                YNetElement element = (YNetElement) locationsIter.next();
                if (element instanceof YCondition) {
                    stateText.append("<condition " + "id=\"")
                            .append(element.toString())
                            .append("\" " + "name=\"")
                            .append(((YCondition) element).getName())
                            .append("\" " + "documentation=\"")
                            .append(((YCondition) element).getDocumentation())
                            .append("\">");
                    List identifiers = ((YConditionInterface) element).getIdentifiers();
                    for (Iterator idIter = identifiers.iterator(); idIter.hasNext();) {
                        YIdentifier identifier = (YIdentifier) idIter.next();
                        stateText.append("<identifier>")
                                .append(identifier.toString())
                                .append("</identifier>");
                    }

                    /**
                     * AJH: Add in flow/link data
                     */
                    stateText.append("<flowsInto>");

                    Iterator postsetFlows = ((YCondition)element).getPostsetFlows().iterator();
                    if (postsetFlows != null)
                    {
                        while(postsetFlows.hasNext())
                        {
                            Object obj = postsetFlows.next();
                            if (obj instanceof YFlow)
                            {
                                YFlow flow = (YFlow)obj;
                                String doc;
                                if (flow.getDocumentation() == null)
                                {
                                    doc = "";
                                }
                                else
                                {
                                    doc = flow.getDocumentation();
                                }

                                stateText.append("<nextElementRef id=\"")
                                        .append(flow.getNextElement().getID())
                                        .append("\" " + "documentation=\"")
                                        .append(doc)
                                        .append("\"/>");
                            }
                        }
                    }

                    stateText.append("</flowsInto>");

                    stateText.append("</condition>");
                } else if (element instanceof YTask) {
                    stateText.append("<task " + "id=\"")
                            .append(element.toString())
                            .append("\" " + "name=\"")
                            .append(((YTask) element)
                                    .getDecompositionPrototype().getID())
                            .append("\">");
                    YTask task = (YTask) element;
                    for (int i = 0; i < 4; i++) {
                        YInternalCondition internalCondition;
                        if (i == 0) {
                            internalCondition = task.getMIActive();
                        }
                        else if (i == 1) {
                            internalCondition = task.getMIEntered();
                        }
                        else if (i == 2) {
                            internalCondition = task.getMIExecuting();
                        }
                        else {//if (i == 3)
                            internalCondition = task.getMIComplete();
                        }
                        if (internalCondition.containsIdentifier()) {
                            stateText.append("<internalCondition " + "id=\"")
                                    .append(internalCondition.toString())
                                    .append("\">");
                            List identifiers = internalCondition.getIdentifiers();
                            for (Iterator iterator = identifiers.iterator(); iterator.hasNext();) {
                                YIdentifier identifier = (YIdentifier) iterator.next();
                                stateText.append("<identifier>")
                                        .append(identifier.toString())
                                        .append("</identifier>");
                            }
                            stateText.append("</internalCondition>");
                        }
                    }
                    stateText.append("</task>");
                }
            }
            stateText.append("</caseState>");
            return stateText.toString();
        }
    }


    public void registerInterfaceAClient(InterfaceAManagementObserver observer) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            _interfaceAClient = observer;
        }
    }

    public void registerInterfaceBObserver(InterfaceBClientObserver observer) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            _interfaceBClient = observer;
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
            observerGatewayController.addGateway(gateway);
            //MLF: moved from restore logic. There is no point in reannouncing before the first gateway
            //     is registered as the announcements will simply fall on deaf errors. Obviously we
            //     also don't want to do it everytime either!
            if(!workItemsAnnounced)
            {
                int sum = 0;
                workItemsAnnounced = true;
                logger.info("Detected first gateway registration. Reannouncing all work items.");

                setReannouncementContext(REANNOUNCEMENT_CONTEXT_RECOVERING);

                try
                {
                    /**
                     * FRA-68: Reannounce all enabled workitems
                     */
                    logger.info("Reannouncing all enabled workitems");
                    int itemsReannounced = reannounceEnabledWorkItems();
                    logger.info("" + itemsReannounced + " enabled workitems reannounced");
                    sum += itemsReannounced;

                    /**
                     * FRA-89: Worklist performance issues
                     */
                    logger.info("Reannouncing all executing workitems");
                    itemsReannounced = reannounceExecutingWorkItems();
                    logger.info("" + itemsReannounced + " executing workitems reannounced");
                    sum += itemsReannounced;

                    logger.info("Reannouncing all fired workitems");
                    itemsReannounced = reannounceFiredWorkItems();
                    logger.info("" + itemsReannounced + " fired workitems reannounced");
                    sum += itemsReannounced;
                }
                catch (YStateException e)
                {
                    logger.error("Failure whilst reannouncing workitems. Some workitems might not have been reannounced.", e);
                }

                setReannouncementContext(REANNOUNCEMENT_CONTEXT_NORMAL);

                logger.info("Reannounced " + sum + " workitems in total.");
            }
        }
    }


    //################################################################################
    //   BEGIN REST-FUL SERVICE METHODS
    //################################################################################
    public Set getAvailableWorkItems() {

        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            if (logger.isDebugEnabled()) {
                logger.debug("--> getAvailableWorkItems: Enabled=" + _workItemRepository.getEnabledWorkItems().size() +
                        " Fired=" + _workItemRepository.getFiredWorkItems().size());
            }

            Set allItems = new HashSet();
            allItems.addAll(_workItemRepository.getEnabledWorkItems());
            allItems.addAll(_workItemRepository.getFiredWorkItems());

            logger.debug("<-- getAvailableWorkItems");
            return allItems;
        }
    }


    public YSpecification getProcessDefinition(YSpecificationID specID) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            return _specifications.getSpecification(specID);
        }
    }


    public YWorkItem getWorkItem(String workItemID) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            YWorkItem workItem = _workItemRepository.getWorkItem(workItemID);
            if (workItem != null) {
                return workItem;
            } else {
                return null;
            }
        }
    }

    public String getCaseData(String caseID) throws YStateException
    {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex)
        {
            YIdentifier id = getCaseID(caseID);

            if(id == null)
            {
                throw new YStateException("Received invalid case id '" + caseID + "'.");
            }
            YNetRunner runner = (YNetRunner)_caseIDToNetRunnerMap.get(id);
            return runner.getCasedata().getData();
        }
    }


    public Set getAllWorkItems() {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return _workItemRepository.getWorkItems();
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
    public YWorkItem startWorkItem(YWorkItem workItem, String userID)
                                    throws YStateException, YDataStateException, 
                                           YQueryException, YSchemaBuildingException,
                                           YPersistenceException, YEngineStateException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            if (getEngineStatus() != ENGINE_STATUS_RUNNING)
            {
                throw new YEngineStateException("Unable to accept request as engine not in correct state: Current state = " + getEngineStatus());
            }

            YPersistenceManager pmgr = null;

            logger.debug("--> startWorkItem");

            try {
                if (isPersisting()) {
                    pmgr = new YPersistenceManager(getPMSessionFactory());
                    pmgr.startTransactionalSession();
                }

                YNetRunner netRunner;
                YWorkItem resultantItem = null;
                if (workItem != null) {
                    if (workItem.getStatus().equals(YWorkItemStatus.statusEnabled)) {
                        netRunner = _workItemRepository.getNetRunner(workItem.getCaseID());
                        List childCaseIDs;
                        childCaseIDs = netRunner.attemptToFireAtomicTask(pmgr, workItem.getTaskID());

                        if (childCaseIDs != null) {
                            for (int i = 0; i < childCaseIDs.size(); i++) {
                                YIdentifier childID = (YIdentifier) childCaseIDs.get(i);
                                YWorkItem nextWorkItem = workItem.createChild(pmgr, childID);
                                if (i == 0) {
                                    netRunner.startWorkItemInTask(pmgr, nextWorkItem.getCaseID(), workItem.getTaskID());
                                    nextWorkItem.setStatusToStarted(pmgr, userID);
                                    Element dataList = ((YTask)
                                            netRunner.getNetElement(workItem.getTaskID())).getData(childID);
                                    nextWorkItem.setData(pmgr, dataList);
                                    resultantItem = nextWorkItem;
                                }
                                /**
                                 * AJH: Surely we need to map the data into the workitem to get it to persist?????
                                 */
                                else
                                {
                                    Element dataList = ((YTask)netRunner.getNetElement(workItem.getTaskID())).getData(childID);
                                    nextWorkItem.setData(pmgr, dataList);
                                }
                            }
                        }
                    } else if (workItem.getStatus().equals(YWorkItemStatus.statusFired)) {
                        workItem.setStatusToStarted(pmgr, userID);
                        netRunner = _workItemRepository.getNetRunner(workItem.getCaseID().getParent());
/**
 * AJH:  As the workitem's data is restored coutesy of Hibernate, why do we need to explicity restore it, get it wrong and
 * subsequently set it to NULL?
 * After further digging I suspect this id all down to implementing multi-atomics and getting it wrong.
 */
//                        Element dataList = ((YTask) netRunner.getNetElement(workItem.getTaskID())).getData(workItem.getCaseID());
//                      workItem.setData(pmgr, dataList);
   
                        netRunner.startWorkItemInTask(pmgr, workItem.getCaseID(), workItem.getTaskID());
                        resultantItem = workItem;
                    } else if (workItem.getStatus().equals(YWorkItemStatus.statusDeadlocked)) {
                        resultantItem = workItem;
                    } else {
                        if (isPersisting()) {
                            pmgr.rollbackTransaction();
                        }

                        throw new YStateException("Item (" + workItem.getIDString() + ") status (" +
                                workItem.getStatus() + ") does not permit starting.");
                        //this work item is likely already executing.
                    }
                } else {
                    if (isPersisting()) {
                        pmgr.rollbackTransaction();
                    }
                    throw new YStateException("No such work item currently available.");
                }

                // COMMIT POINT
                if (persisting) {
                    pmgr.commit();
                }

                logger.debug("<-- startWorkItem");
                return resultantItem;
            } catch (Exception e) {
                if (persisting) {
                    pmgr.rollbackTransaction();
                }

                // re-throw exception (tacky code ....)
                if (e instanceof YStateException) {
                    throw (YStateException) e;
                } else if (e instanceof YDataStateException) {
                    throw (YDataStateException) e;
                } else if (e instanceof YQueryException) {
                    throw (YQueryException) e;
                } else if (e instanceof YSchemaBuildingException) {
                    throw (YSchemaBuildingException) e;
                } else if (e instanceof YPersistenceException) {
                    throw (YPersistenceException) e;
                }
                /**
                 * AJH - Catch other exceptions here to avoid silient failures ....
                 */
                else {
                    logger.error("Failure starting workitem " + workItem.getWorkItemID().toString(), e);
                    throw new YQueryException(e.getMessage());
                }
            }
        }
    }


    /**
     * Returns the task definition, not the task instance.
     *
     * @param specificationID the specification id
     * @param taskID          the task id
     * @return the task definition object.
     */
    public YTask getTaskDefinition(YSpecificationID specificationID, String taskID) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            YSpecification specification = _specifications.getSpecification(specificationID);
            if (specification != null) {
                Set decompositions = specification.getDecompositions();
                for (Iterator iterator2 = decompositions.iterator(); iterator2.hasNext();) {
                    YDecomposition decomposition = (YDecomposition) iterator2.next();
                    if (decomposition instanceof YNet) {
                        if (((YNet) decomposition).getNetElements().containsKey(taskID)) {
                            YExternalNetElement el = ((YNet) decomposition).getNetElement(taskID);
                            if (el instanceof YTask) {
                                return (YTask) ((YNet) decomposition).getNetElement(taskID);
                            }
                        }
                    }
                }
            }
            return null;
        }
    }

    public void setEngineStatus(int engineStatus)
    {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex)
        {
            this.engineStatus = engineStatus;
        }
    }

    public int getEngineStatus()
    {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex)
        {
            return this.engineStatus;
        }
    }

    private void setReannouncementContext(int context)
    {
        this.reannouncementContext = context;
    }


    public int getReannouncementContext()
    {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex)
        {
            return this.reannouncementContext;
        }
    }

    // MLR (31/10/07): stub added to keep backwards compatibility with custom services
    // that do not support the status ForcedComplete
    public void completeWorkItem(YWorkItem workItem, String data)
                                     throws YStateException, YDataStateException,
                                            YQueryException, YSchemaBuildingException,
                                            YPersistenceException, YEngineStateException {
	    completeWorkItem(workItem, data, false);
    }

    /**
     * Completes the work item.
     *
     * @param workItem
     * @param data
     * @param force - true if this represents a 'forceComplete', false for normal
     *                completion
     * @throws YStateException
     */
    public void completeWorkItem(YWorkItem workItem, String data, boolean force)
            throws YStateException, YDataStateException, YQueryException,
                   YSchemaBuildingException, YPersistenceException, YEngineStateException{
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            if (getEngineStatus() != ENGINE_STATUS_RUNNING)
            {
                throw new YEngineStateException("Unable to accept request as engine not in correct state: Current state = " + getEngineStatus());
            }
            YPersistenceManager pmgr = null;
            if (logger.isDebugEnabled())
            {
                logger.debug("--> completeWorkItem");
                logger.debug("WorkItem = " + workItem.getWorkItemID().getUniqueID());
                logger.debug("XML = " + data);
            }

            try {
                // Create a PM
                if (isPersisting()) {
                    pmgr = new YPersistenceManager(getPMSessionFactory());
                    pmgr.startTransactionalSession();
                }

                if (force) data = mapOutputDataForSkippedWorkItem(workItem);

                Document doc;
                if (workItem != null) {
                    if (workItem.getStatus().equals(YWorkItemStatus.statusExecuting)) {
                        YNetRunner netRunner = _workItemRepository.getNetRunner(workItem.getCaseID().getParent());
                        synchronized (netRunner) {

                            if (_exceptionObserver != null) {
                                if (netRunner.isTimeServiceTask(workItem)) {
                                    List timeOutSet = netRunner.getTimeOutTaskSet(workItem);
                                    announceTimeOutToExceptionService(_exceptionObserver, workItem, timeOutSet);
                                }
                            }
                            SAXBuilder builder = new SAXBuilder();
                            //doing this because saxon can't do an effective query when the whitespace is there
                            try {
                                Document d = builder.build(new StringReader(data));
                                Document e = YDocumentCleaner.cleanDocument(d);
                                doc = e;
                                boolean taskExited = netRunner.completeWorkItemInTask(pmgr, workItem, workItem.getCaseID(), workItem.getTaskID(), e);
                                if (taskExited) {
                                    //todo AJH:
                                    /* BUG - If we post the same task again (i.e. tight loop), we end up clearing the task we've just posted.
                                     */
                                    //                            _workItemRepository.removeWorkItemFamily(workItem);

                                    /* Calling this to fix a problem.
                                     * When a Task is enabled twice by virtue of having two enabling sets of
                                     * tokens in the current marking the work items are not created twice.
                                     * Instead an Enabled work item is created for one of the enabling sets.
                                     * Once that task has well and truly finished it is then an appropriate
                                     * time to notify the worklists that it is enabled again.
                                     * This is done by calling continueIfPossible().*/
                                    logger.debug("Recalling continue (looping bugfix???)");
                                    netRunner.continueIfPossible(pmgr);
                                }
                            } catch (JDOMException e) {
                                YStateException f = new YStateException(e.getMessage());
                                f.setStackTrace(e.getStackTrace());
                                if (isPersisting()) {
                                    pmgr.rollbackTransaction();
                                }
                                throw f;
                            } catch (IOException e) {
                                YStateException f = new YStateException(e.getMessage());
                                f.setStackTrace(e.getStackTrace());
                                if (isPersisting()) {
                                    pmgr.rollbackTransaction();
                                }
                                throw f;
                            }
                        }
                        workItem.setStatusToComplete(pmgr, force);
                        workItem.completeData(pmgr, doc);

                        /**
                         * If case is suspending, see if we can progress into a fully suspended state
                         */
                        if (netRunner.getCasedata().getExecutionState() == YCaseData.SUSPEND_STATUS_SUSPENDING)
                        {
                            progressCaseSuspension(pmgr, workItem.getParent().getCaseID());
                        }

                    } else if (workItem.getStatus().equals(YWorkItemStatus.statusDeadlocked)) {
                        _workItemRepository.removeWorkItemFamily(workItem);
                    } else {
                        throw new YStateException("WorkItem with ID [" + workItem.getIDString() +
                                "] not in executing state.");
                    }

                    // COMMIT POINT
                    if (isPersisting()) {
                        pmgr.commit();
                    }

                    /**
                     * AJH: Test hook for revised persistence
                     */
                    persistCase(workItem.getCaseID());
                } else {
                    if (isPersisting()) {
                        pmgr.rollbackTransaction();
                    }
                    throw new YStateException("WorkItem argument is equal to null.");
                }
            } catch (Exception e) {
                if (isPersisting()) {
                    pmgr.rollbackTransaction();
                }

                // Re-Throw exception
                if (e instanceof YStateException) {
                    throw (YStateException) e;
                } else if (e instanceof YDataStateException) {
                    throw (YDataStateException) e;
                } else if (e instanceof YQueryException) {
                    throw (YQueryException) e;
                } else if (e instanceof YSchemaBuildingException) {
                    throw (YSchemaBuildingException) e;
                } else if (e instanceof YPersistenceException) {
                    throw (YPersistenceException) e;
                }
                /**
                 * AJH: More error handling
                 */
                else if (e instanceof IllegalArgumentException) {
                    e.printStackTrace();
                    throw new YSchemaBuildingException(StringUtil.convertThrowableToString(e));
                } else {
                    e.printStackTrace();
                    throw new YSchemaBuildingException(StringUtil.convertThrowableToString(e));
                }
            }
            logger.debug("<-- completeWorkItem");
        }
    }


    public YWorkItem skipWorkItem(YWorkItem workItem, String userID)
                                   throws YStateException, YDataStateException,
                                          YQueryException, YSchemaBuildingException,
                                          YPersistenceException, YEngineStateException {

        // start item, get output data, get children, complete each child
        YWorkItem startedItem = startWorkItem(workItem, userID) ;
        if (startedItem != null) {
            String data = mapOutputDataForSkippedWorkItem(startedItem) ;
            Set<YWorkItem> children = workItem.getChildren() ;
            for (YWorkItem child : children)
                completeWorkItem(child, data, false) ;
        }
        else {
            throw new YStateException("Could not skip workitem: " + workItem.getIDString()) ;
        }
        return startedItem ;
    }


    private String mapOutputDataForSkippedWorkItem(YWorkItem workItem) {

        // get input and output params for task
        YSpecificationID specID = workItem.getSpecificationID();
        String taskID = workItem.getTaskID();
        YTask task = getTaskDefinition(specID, taskID) ;

        Map<String, YParameter> inputs =
                                 task.getDecompositionPrototype().getInputParameters();
        Map<String, YParameter> outputs =
                                 task.getDecompositionPrototype().getOutputParameters();

        // map data values to params
        Element itemData = JDOMUtil.stringToElement(workItem.getDataString());
        Element outputData = (Element) itemData.clone();

        // remove the input-only params from output data
        for (String name : inputs.keySet())
            if (outputs.get(name) == null) outputData.removeChild(name);

        // for each output param
        for (String name : outputs.keySet()) {

            // if the output param has no corresponding input param, add an emelent
            if (inputs.get(name) == null) {
                Element outData = new Element(name) ;
                YParameter outParam = outputs.get(name);
                String defaultValue = outParam.getDefaultValue();
                if (defaultValue != null)
                    outData.setText(defaultValue) ;
                else
                    outData.setText(JDOMUtil.getDefaultValueForType(outParam.getDataTypeName()));

                outputData.addContent(outData);
            }
        }
        return JDOMUtil.elementToStringDump(outputData);
    }

    /**
     * Determines whether or not a task will allow a dynamically
     * created new instance to be created.  MultiInstance Task with
     * dynamic instance creation.
     *
     * @param workItemID the workItemID of a sibling work item.
     * @throws YStateException if task is not MultiInstance, or
     *                         if task does not allow dynamic instance creation,
     *                         or if current number of instances is not less than the maxInstances
     *                         for the task.
     */
    public void checkElegibilityToAddInstances(String workItemID) throws YStateException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            YWorkItem item = _workItemRepository.getWorkItem(workItemID);
            if (item != null) {
                if (item.getStatus().equals(YWorkItemStatus.statusExecuting)) {
                    if (item.allowsDynamicCreation()) {
                        YIdentifier identifier = item.getCaseID().getParent();
                        YNetRunner netRunner =
                                _workItemRepository.getNetRunner(identifier);
                        boolean addEnabled =
                                netRunner.isAddEnabled(item.getTaskID(), item.getCaseID());
                        if (addEnabled) {
                            //do nothing
                        } else {
                            throw new YStateException("Adding instances is not possible in " +
                                    "current state.");
                        }
                    } else {
                        throw new YStateException("WorkItem[" + workItemID +
                                "] does not allow new instance creation.");
                    }
                } else {
                    throw new YStateException("WorkItem[" + workItemID +
                            "] is not in appropriate (executing) " +
                            "state for instance adding.");
                }
            } else {
                throw new YStateException("No work Item Found with id : " +
                        workItemID);
            }
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
    public YWorkItem createNewInstance(YWorkItem workItem, String paramValueForMICreation) throws YStateException, YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            YPersistenceManager pmgr = null;

            if (isPersisting()) {
                pmgr = new YPersistenceManager(getPMSessionFactory());
                pmgr.startTransactionalSession();
            }

            try {
                if (workItem == null) {
                    throw new YStateException("No work item found.");
                }
                String taskID = workItem.getTaskID();
                YIdentifier siblingID = workItem.getCaseID();
                YNetRunner netRunner = _workItemRepository.getNetRunner(siblingID.getParent());
                synchronized (netRunner) {
                    checkElegibilityToAddInstances(workItem.getIDString());
                    //calling it again to double check while we hold the semaphore to the netRunner
                    SAXBuilder builder = new SAXBuilder();
                    //doing this because Saxon can't do an effective query when the whitespace is there
                    try {
                        Document d;
                        d = builder.build(new StringReader(paramValueForMICreation));
                        Document e = YDocumentCleaner.cleanDocument(d);
                        Element el = e.detachRootElement();
                        YIdentifier id = netRunner.addNewInstance(pmgr, taskID, workItem.getCaseID(), el);
                        if (id != null) {
                            YWorkItem firedItem = workItem.getParent().createChild(pmgr, id);

                            if (pmgr != null) {
                                pmgr.commit();
                            }

                            return firedItem;
                            //success!!!!
                        } else {
                            if (isPersisting()) {
                                pmgr.rollbackTransaction();
                            }

                            throw new YStateException("New work item not created.");
                        }
                    } catch (Exception e) {
                        if (isPersisting()) {
                            pmgr.rollbackTransaction();
                        }
                        throw new YStateException(e.getMessage());
                    }
                }
            } catch (YStateException e1) {
                if (pmgr != null) {
                    pmgr.rollbackTransaction();
                }
                throw e1;
            }
        }
    }

   //added method
    public YWorkItem suspendWorkItem(String workItemID) throws YStateException, YPersistenceException {

        synchronized (mutex) {

            YPersistenceManager pmgr = null;

            YWorkItem workItem = _workItemRepository.getWorkItem(workItemID);
            if (workItem != null) {
                if (isPersisting()) {
                    pmgr = new YPersistenceManager(getPMSessionFactory());
                    pmgr.startTransactionalSession();
                }

                if (workItem.hasLiveStatus()) {
                    workItem.setStatusToSuspended(pmgr);  //added
            }
        }
        if (pmgr != null) pmgr.closeSession();
        return workItem ;
    }
   }

    public YWorkItem unsuspendWorkItem(String workItemID) throws YStateException, YPersistenceException {

        synchronized (mutex) {
            YPersistenceManager pmgr = null;
            YWorkItem workItem = _workItemRepository.getWorkItem(workItemID);
            if (workItem != null) {
                if (isPersisting()) {
                    pmgr = new YPersistenceManager(getPMSessionFactory());
                    pmgr.startTransactionalSession();
                }

                if (workItem.getStatus().equals(YWorkItemStatus.statusSuspended))
                    workItem.setStatusToUnsuspended(pmgr);  //added
            }
            if (pmgr != null) pmgr.closeSession();

            return workItem ;
        }
    }

    // rolls back a workitem from executing to fired
    public void rollbackWorkItem(String workItemID, String userName) throws YStateException, YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            YPersistenceManager pmgr = null;

            YWorkItem workItem = _workItemRepository.getWorkItem(workItemID);
            if (workItem != null) {
                if (isPersisting()) {
                    pmgr = new YPersistenceManager(getPMSessionFactory());
                    pmgr.startTransactionalSession();
                }

                if (workItem.getStatus().equals(YWorkItemStatus.statusExecuting)) {
                    workItem.rollBackStatus(pmgr);
                    YNetRunner netRunner = _workItemRepository.getNetRunner(workItem.getCaseID().getParent());
                    if (netRunner.rollbackWorkItem(pmgr, workItem.getCaseID(), workItem.getTaskID())) {
                    } else {
                        if (isPersisting()) {
                            pmgr.rollbackTransaction();
                        }

                        throw new YStateException("Work Item[" + workItemID +
                                "] is not in executing state.");
                    }
                }
                if (pmgr != null) {
                    pmgr.commit();
                }
            } else {
                if (isPersisting()) {
                    pmgr.rollbackTransaction();
                }
                throw new YStateException("Work Item[" + workItemID + "] not found.");
            }
        }
    }

    public String launchCase(String username, String specID, String caseParams, URI completionObserver, String caseID) throws YStateException, YDataStateException, YSchemaBuildingException, YPersistenceException, YEngineStateException
    {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            if (getEngineStatus() != ENGINE_STATUS_RUNNING)
            {
                throw new YEngineStateException("Unable to accept request as engine not in correct state: Current state = " + getEngineStatus());
            }

            return _launchCase(username, specID, caseParams, completionObserver, caseID);
        }
    }

    public String launchCase(String username, String specID, String caseParams, URI completionObserver) throws YStateException, YDataStateException, YSchemaBuildingException, YPersistenceException, YEngineStateException
    {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            if (getEngineStatus() != ENGINE_STATUS_RUNNING)
            {
                throw new YEngineStateException("Unable to accept request as engine not in correct state: Current state = " + getEngineStatus());
            }

            return _launchCase(username, specID, caseParams, completionObserver, null);
        }
    }    

    private String _launchCase(String username, String specID, String caseParams, URI completionObserver, String caseID) throws YStateException, YDataStateException, YSchemaBuildingException, YPersistenceException {
            YPersistenceManager pmgr = null;
			YIdentifier yCaseID = null;
            String caseIDString = null;

            logger.debug("--> launchCase");

            // Create a PM
            if (isPersisting()) {
                pmgr = new YPersistenceManager(getPMSessionFactory());
                pmgr.startTransactionalSession();
            }

       try
        {
            if (caseID != null)
            {
                //ensure that this is not already in use
                if(getCaseID(caseID) == null)
                {
                    yCaseID = startCase(username, pmgr, specID, caseParams, completionObserver, caseID);
                }
                else
                {
                    throw new YStateException("CaseID '" + caseID + "' is already active.");
                }
            }
            else
            {
                yCaseID = startCase(username, pmgr, specID, caseParams, completionObserver, null);
            }

            if (yCaseID != null) {
                caseIDString = yCaseID.toString();
            } else {
                if (isPersisting()) {
                    pmgr.rollbackTransaction();
                }

                throw new YStateException("No specification found for [" + specID + "].");
            }

            if (isPersisting()) {
                pmgr.commit();
            }
        }
        catch (Exception e)
        {
            /**
             * Better to trap generic Exception here as we *MUST* rollback the TXN. Unforunately entails
             * tacky code to propagate correct exception class back up call stack.
             */
            logger.error("Failure returned from startCase - Rolling back Hibernate TXN", e);
            if (isPersisting()) {
                pmgr.rollbackTransaction();
            }

            /**
             * AJH: Re-throw appropriate exception back up call stack
             */
            if (e instanceof  YStateException)
            {
                throw (YStateException)e;
            }
            else if (e instanceof YDataStateException)
            {
                throw (YDataStateException)e;
            }
            else if (e instanceof YSchemaBuildingException)
            {
                throw (YSchemaBuildingException)e;
            }
            else if (e instanceof YPersistenceException)
            {
                throw (YPersistenceException)e;
            }
            else
            {
                throw new YStateException("Unexpected failure from launchCase (see log for details");
            }
        }

        logger.debug("<-- launchCase");
        return caseIDString;
    }




    /**
     * Given a process specification id return the cases that are its running
     * instances.
     *
     * @param specID the process specification id string.
     * @return a set of YIdentifer caseIDs that are run time instances of the
     *         process specification with id = specID
     */
    public Set<YIdentifier> getCasesForSpecification(YSpecificationID specID) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            Set<YIdentifier> resultSet = new HashSet<YIdentifier>();
            if (_specifications.contains(specID)) {
                Set<YIdentifier> caseIDs = _runningCaseIDToSpecMap.keySet();
                for (YIdentifier caseID : caseIDs)
                {
                    YSpecification specForCaseID = _runningCaseIDToSpecMap.get(caseID);
                    if (specForCaseID.getSpecificationID().equals(specID)) {
                        resultSet.add(caseID);
                    }
                }
            }
            return resultSet;
        }
    }


    public YAWLServiceReference getRegisteredYawlService(String yawlServiceID) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            return (YAWLServiceReference) _yawlServices.get(yawlServiceID);
        }
    }


    /**
     * Returns a set of YAWL services registered in the engine.
     *
     * @return Set of services
     */
    public Set getYAWLServices() {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            return new HashSet(_yawlServices.values());
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

            logger.debug("--> addYawlService: Service=" + yawlService.getURI());

            _yawlServices.put(yawlService.getURI(), yawlService);

            /*
              INSERTED FOR PERSISTANCE
             */
            if (!restoring && isPersisting()) {

                logger.info("Persisting YAWL Service " + yawlService.getURI() + " with ID " + yawlService.get_yawlServiceID());
                YPersistenceManager pmgr = new YPersistenceManager(getPMSessionFactory());
                pmgr.startTransactionalSession();
                pmgr.storeObject(yawlService);
                pmgr.commit();
            }

            logger.debug("<-- addYawlService");
        }
    }


    public Set getChildrenOfWorkItem(YWorkItem workItem) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            if (workItem != null)
                return _workItemRepository.getChildrenOf(workItem.getIDString());
            else
                return null ;
        }
    }


    /**
     * Announces a task to a YAWL service.  This is a classic push style
     * interaction where the Engine pushes the work item out into the
     * YAWL service.
     * PRE: the YAWL service exists and is on line.
     *
     * @param yawlService the YAWL service
     * @param item        the work item must be enabled.
     */
    protected void announceTask(YAWLServiceReference yawlService, YWorkItem item) {
        logger.debug("Announcing " + item.getStatus() + " task " + item.getIDString() + " on service " + yawlService.get_yawlServiceID());
        observerGatewayController.notifyAddWorkItem(yawlService, item);
    }


    public void announceCancellationToEnvironment(YAWLServiceReference yawlService, YWorkItem item) {
        logger.debug("Announcing task cancellation " + item.getIDString() + " on service " + yawlService.get_yawlServiceID());
        observerGatewayController.notifyRemoveWorkItem(yawlService, item);
    }

    protected void announceCaseCompletionToEnvironment(YAWLServiceReference yawlService, YIdentifier caseID, Document casedata) {
        observerGatewayController.notifyCaseCompletion(yawlService, caseID, casedata);
    }

public void announceWorkItemStatusChange(YWorkItem workItem, YWorkItemStatus oldStatus, YWorkItemStatus newStatus)
    {
        logger.debug("Announcing workitem status change from '" + oldStatus + "' to new status '" + newStatus +
                     "' for workitem '" + workItem.getWorkItemID().toString() + "'.");
        observerGatewayController.notifyWorkItemStatusChange(workItem,oldStatus,newStatus);
    }

    /**
     * Causes the engine to re-announce all workitems which are in an "enabled" state.<P>
     *
     * @return The number of enabled workitems that were reannounced
     */
    public int reannounceEnabledWorkItems() throws YStateException
    {
        logger.debug("--> reannounceEnabledWorkItems");

        int retCount = 0;
        Iterator enabledWorkItems = _workItemRepository.getEnabledWorkItems().iterator();

        while(enabledWorkItems.hasNext())
        {
            Object obj = enabledWorkItems.next();
            if (obj instanceof YWorkItem)
            {
                YWorkItem workitem = (YWorkItem)obj;
                reannounceWorkItem(workitem);
                retCount++;
            }
        }

        logger.debug("<-- reannounceEnabledWorkItems");
        return retCount;
    }

    /**
     * Causes the engine to re-announce all workitems which are in an "executing" state.<P>
     *
     * @return The number of executing workitems that were reannounced
     */
    public int reannounceExecutingWorkItems() throws YStateException
    {
        logger.debug("--> reannounceExecutingWorkItems");

        int retCount = 0;
        Iterator executingWorkItems = _workItemRepository.getExecutingWorkItems().iterator();

        while(executingWorkItems.hasNext())
        {
            Object obj = executingWorkItems.next();
            if (obj instanceof YWorkItem)
            {
                YWorkItem workitem = (YWorkItem)obj;
                reannounceWorkItem(workitem);
                retCount++;
            }
        }

        logger.debug("<-- reannounceExecutingWorkItems");
        return retCount;
    }

    /**
     * Causes the engine to re-announce all workitems which are in an "fired" state.<P>
     *
     * @return The number of fired workitems that were reannounced
     */
    public int reannounceFiredWorkItems() throws YStateException
    {
        logger.debug("--> reannounceFiredWorkItems");

        int retCount = 0;
        Iterator firedWorkItems = _workItemRepository.getFiredWorkItems().iterator();

        while(firedWorkItems.hasNext())
        {
            Object obj = firedWorkItems.next();
            if (obj instanceof YWorkItem)
            {
                YWorkItem workitem = (YWorkItem)obj;
                reannounceWorkItem(workitem);
                retCount++;
            }
        }

        logger.debug("<-- reannounceFiredWorkItems");
        return retCount;
    }

    /**
     * Causes the engine to re-announce a specific workitem regardless of state.<P>
     */
    public void reannounceWorkItem(YWorkItem workItem) throws YStateException
    {
        logger.debug("--> reannounceWorkItem: WorkitemID=" + workItem.getWorkItemID().getTaskID());

        YNetRunner netRunner = null;

        if (workItem.getStatus() == YWorkItemStatus.statusExecuting)
        {
             netRunner = _workItemRepository.getNetRunner(workItem.getCaseID().getParent());
        }
        else if (workItem.getStatus() == YWorkItemStatus.statusEnabled)
        {
            netRunner = _workItemRepository.getNetRunner(workItem.getCaseID());
        }
        else if (workItem.getStatus() == YWorkItemStatus.statusFired)
        {
            netRunner = _workItemRepository.getNetRunner(workItem.getCaseID().getParent());
        }
        else
        {
            throw new YStateException("Failed to reannounce workitem " + workItem + " as state " + workItem.getStatus() + " is unsupported");
        }

        if (netRunner != null)
        {
            netRunner.announceToEnvironment(workItem);
        }

        logger.debug("<-- reannounceEnabledWorkItem");
    }


    /**
     * Connects the user to the engine, and returns a sessionhandle back to the user.
     * This session lasts for one hour at time of writing.
     *
     * @param userID   the userid
     * @param password the password
     * @return the session handle
     * @throws YAuthenticationException if the password is not valid.
     * @deprecated
     */
    private String connect(String userID, String password) throws YAuthenticationException {
        return _userList.connect(userID, password);
    }


    protected void announceEnabledTaskToResourceService(YWorkItem item) {
        if (_resourceObserver != null) {
            logger.debug("Announcing enabled task " + item.getIDString() + " on service " +
                          _resourceObserver.get_yawlServiceID());
            observerGatewayController.notifyAddWorkItem(_resourceObserver, item);
        }
    }

    // this method should be called by an IB service when it decides it is not going
    // to handle (i.e. checkout) a workitem announced to it. It passes the workitem to
    // the resourceService for normal assignment.
    public void rejectAnnouncedEnabledTask(YWorkItem item) {
        announceEnabledTaskToResourceService(item);
    }


    public Set getUsers() {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            logger.debug("--> getUsers");
            logger.debug("<-- getUsers: Returned " + _userList.getUsers().size() + " entries");

            return _userList.getUsers();
        }
    }


    /**
     * Returns a list of the YIdentifiers objects for running cases.
     *
     * @return List of running cases     
	*/
    public List getRunningCaseIDs() {
        return new ArrayList(_runningCaseIDToSpecMap.keySet());
    }

    public String getLoadStatus(YSpecificationID specID) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            if (_specifications.contains(specID)) {
                return YSpecification._loaded;
            } else if (_unloadedSpecifications.containsKey(specID)) {
                return YSpecification._unloaded;
            } else {
                throw new RuntimeException("SpecID [" + specID + "] is not loaded.");
            }
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

            /*
              INSERTED FOR PERSISTANCE
             */
            YAWLServiceReference service = (YAWLServiceReference) _yawlServices.remove(serviceURI);

            if (service != null) {
                if (isPersisting()) {
                    logger.info("Deleting persisted entry for YAWL service " + service.getURI() + " with ID " + service.get_yawlServiceID());

                    YPersistenceManager pmgr = new YPersistenceManager(getPMSessionFactory());

                    try {
                        pmgr.startTransactionalSession();
                        pmgr.deleteObject(service);
                        pmgr.commit();
                    } catch (YPersistenceException e) {
                        logger.fatal("Failure whilst removing YAWL service", e);
                        throw e;
                    }
                }
            }

            return service;
        }
    }

    /**
     * Indicates if persistence is to the database.
     *
     * @return True=Persistent, False=Not Persistent
     */
    public static boolean isPersisting() {
        return persisting;
    }

    /**
     * Indicates if persistence is to the database.
     *
     * @param arg
     */
    private static void setPersisting(boolean arg) {
        persisting = arg;
    }

    /**
     * Performs a diagnostic dump of the engine internal tables and state to trace.<P>
     */
    public void dump() {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            logger.debug("*** DUMP OF ENGINE STARTS ***");

            Set<YSpecificationID> specids = _specifications.getSpecIDs();

            logger.debug("\n*** DUMPING " + specids.size() + " SPECIFICATIONS ***");
            {
                int i = 0;
                for(YSpecificationID specid : specids)
                {
                    YSpecification spec = _specifications.getSpecification(specid);
                    logger.debug("Entry " + i + ":");
                    logger.debug("    ID             " + spec.getID());
                    logger.debug("    Name           " + spec.getName());
                    logger.debug("    Version   " + spec.getMetaData().getVersion());
                    i++;
                }
            }
            logger.debug("*** DUMP OF SPECIFICATIONS ENDS ***");

            logger.debug("*** DUMPING " + _caseIDToNetRunnerMap.size() + " ENTRIES IN CASE_ID_2_NETRUNNER MAP ***");
            {
                Iterator keys = _caseIDToNetRunnerMap.keySet().iterator();
                int sub = 0;
                while (keys.hasNext()) {
                    sub++;
                    Object objKey = keys.next();
                    if (objKey == null) {
                        logger.debug("Key = NULL !!!");
                    } else {
                        YIdentifier key = (YIdentifier) objKey;
                        YNetRunner runner = (YNetRunner) _caseIDToNetRunnerMap.get(key);
                        logger.debug("Entry " + sub + " Key=" + key.get_idString());
                        logger.debug(("    CaseID        " + runner.get_caseID()));
                        logger.debug("     YNetID        " + runner.getYNetID());
                        runner.dump();
                    }
                }
            }

            logger.debug("*** DUMP OF CASE_ID_2_NETRUNNER_MAP ENDS");

            logger.debug("*** DUMP OF RUNNING CASES TO SPEC MAP STARTS ***");
            {
                Iterator keys = _runningCaseIDToSpecMap.keySet().iterator();
                int sub = 0;
                while (keys.hasNext()) {
                    sub++;
                    Object objKey = keys.next();

                    if (objKey == null) {
                        logger.debug("key is NULL !!!");
                    } else {
                        YIdentifier key = (YIdentifier) objKey;
                        YSpecification spec = _runningCaseIDToSpecMap.get(key);
                        logger.debug("Entry " + sub + " Key=" + key);
                        logger.debug("    ID             " + spec.getID());
                        logger.debug("    Version        " + spec.getMetaData().getVersion());
                    }
                }
            }
            logger.debug("*** DUMP OF RUNNING CASES TO SPEC MAP ENDS ***");

            if (getWorkItemRepository() != null) {
                getWorkItemRepository().dump(logger);
            }

            logger.debug("*** DUMP OF ENGINE ENDS ***");
        }
    }

    public static YWorkItemRepository getWorkItemRepository() {
        return _workItemRepository;
    }

    public static SessionFactory getPMSessionFactory() {
        return factory;
    }

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

            if (isPersisting()) {
                YPersistenceManager pmgr = new YPersistenceManager(getPMSessionFactory());
                pmgr.startTransactionalSession();
                pmgr.storeObject(obj);
                pmgr.commit();
            }
        }
    }

    public void deleteObject(Object obj) throws YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            if (isPersisting()) {
                YPersistenceManager pmgr = new YPersistenceManager(getPMSessionFactory());
                pmgr.startTransactionalSession();
                pmgr.deleteObject(obj);
                pmgr.commit();
            }
        }
    }

    /**
     *
     *
     * AJH: Relocated from YPersistance
     *
     * @param pmgr
     * @param id
     * @throws YPersistenceException
     */
    protected void clearCase(YPersistenceManager pmgr, YIdentifier id) throws YPersistenceException {
        logger.debug("--> clearCase: CaseID = " + id.get_idString());

        if (persisting) {
            clearCaseDelegate(pmgr, id);
        }

        logger.debug("<-- clearCase");
    }

    /**
     * Removes the case from persistence
     *
     * AJH: Originally relocated from YPersistance
     *
     * @param pmgr
     * @param id
     * @throws YPersistenceException
     */
    private void clearCaseDelegate(YPersistenceManager pmgr, YIdentifier id)
                                                        throws YPersistenceException {

        logger.debug("--> clearCaseDelegate: CaseID = " + id.get_idString());

        if (persisting) {
            try {
                List list = id.get_children();
                for (int i = 0; i < list.size(); i++) {
                    YIdentifier child = (YIdentifier) list.get(i);
                    clearCaseDelegate(pmgr, child);
                }

                boolean runnerfound = false;

                // if its a runner, remove it
                Query query = pmgr.getSession().createQuery(
                               "from org.yawlfoundation.yawl.engine.YNetRunner where case_id = '"
                               + id.toString() + "'");
                for (Iterator it = query.iterate(); it.hasNext();) {
                    pmgr.deleteObject(it.next());
                    runnerfound = true;
                }

                // it's not a runner, so remove the P_YIdentifier only
                if (!runnerfound) {
                    Query quer = pmgr.getSession().createQuery(
                                 "from org.yawlfoundation.yawl.engine.P_YIdentifier where id = '"
                                 + id.toString() + "'");
                    Iterator itx = quer.iterate();

                    // check the p_yid hasn't already been removed (can be one at most)
                    if (itx.hasNext())  {
                        pmgr.deleteObject(itx.next());
                    }
               }
            } catch (Exception e) {
                throw new YPersistenceException("Failure whilst clearing case", e);
            }
        }
        logger.debug("<-- clearCaseDelegate");
    }


    /**
     * Returns the next available case number.<P>
     *
     * Note: This method replaces that previously included within YPersistance.
     *
     */
    public String getNextCaseNbr() {
        String result = String.valueOf(_nextCaseNbr);
        _nextCaseNbr++;
        return result;
    }

    /**
     * Indicate if user interface metadata is to be generated within a tasks input XML doclet.
     *
     * @param arg
     */
    public void setGenerateUIMetaData(boolean arg)
    {
        generateUIMetaData = arg;
    }

    /**
     * Indicates if user interface metadata is to be generated within a tasks input XML doclet.
     *
     * @return True=UIMetaData generated, False=UIMetaData not supported
     */
    public boolean generateUIMetaData()
    {
        return generateUIMetaData;
    }

    /**
     * AJH: Stub method for testing revised persistence mechanism
     * @param caseID
     */
    private void persistCase(YIdentifier caseID) {
        logger.debug("--> persistCase: CaseID = " + caseID.getId());
        logger.debug("<-- persistCase");
    }

    /**
     * AJH: Public method which returns the next available caseID
     *
     * Note: This is currently only available with a non-persisting engine as M2 require to ascertain the case
     * ID prior to launching a case for their XForms execution framework.
     *
     * @return  A unique case ID
     */
    public String allocateCaseID() throws YPersistenceException
    {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex)
        {
            String caseID = null;

            if (isPersisting())
            {
                throw new YPersistenceException("Pre-allocated CaseIDs are not available in a persisting engine instance");
            }
            else
            {
                caseID = getNextCaseNbr();
            }

            return caseID;
        }
    }

    /**
     * Suspends execution of a case.
     *
     * @param id
     * @throws YPersistenceException
     */
    public void suspendCase(YIdentifier id) throws YPersistenceException, YStateException
    {
       /**
         * SYNC'D External interface
         */
        synchronized (mutex)
        {
            YPersistenceManager pmgr = null;

            try
            {

                if (isPersisting())
                {
                    pmgr = new YPersistenceManager(getPMSessionFactory());
                    pmgr.startTransactionalSession();
                }

                suspendCase(pmgr, id);

                if (isPersisting())
                {
                    pmgr.commit();
                    pmgr = null;
                }
            }
            catch (Exception e)
            {
                logger.error("Failure to suspend case " + id, e);
                if (isPersisting())
                {
                    pmgr.rollbackTransaction();
                }
                throw new YStateException("Could not suspend case (See log for details)");
            }
        }
    }

    private void suspendCase(YPersistenceManager pmgr, YIdentifier id) throws YPersistenceException, YStateException
    {
        logger.debug("--> suspendCase: CaseID = " + id.toString());

        // Check current case status and reject call if this case not currently in a normal state
        YNetRunner topLevelNet = (YNetRunner)_caseIDToNetRunnerMap.get(id);
        if (topLevelNet.getCasedata().getExecutionState() != YCaseData.SUSPEND_STATUS_NORMAL)
        {
            throw new YStateException("Case " + topLevelNet.getCaseID() + " cannot be suspended as currently not executing normally (SuspendStatus=" + topLevelNet.getCasedata().getExecutionState() + ")");
        }
        else
        {
            Vector runners = getRunnersForPrimaryCase(id);

            // Go thru all runners and set status to suspending
            Enumeration runnersEnum = runners.elements();
            while(runnersEnum.hasMoreElements())
            {
                YNetRunner runner = (YNetRunner)runnersEnum.nextElement();
                logger.debug("Current status of runner " + runner.get_caseID() + " = " + runner.getCasedata().getExecutionState());
                runner.getCasedata().setExecutionState(YCaseData.SUSPEND_STATUS_SUSPENDING);
                observerGatewayController.notifyCaseSuspending(id);

                if (pmgr != null)
                {
                    pmgr.updateObject(runner.getCasedata());
                }
            }

            logger.info("Case " + topLevelNet.getCaseID() + " is attempting to suspend");

            // See if we can progress this case into a fully suspended state.
            progressCaseSuspension(pmgr, id);
        }

        logger.debug("<-- suspendCase");
    }

    /**
     * Resumes execution of a case.
     *
     * @param id
     * @throws YPersistenceException
     */
    public void resumeCase(YIdentifier id) throws YPersistenceException, YStateException
    {
       /**
         * SYNC'D External interface
         */
        synchronized (mutex)
        {
            YPersistenceManager pmgr = null;

            try
            {
                if (isPersisting())
                {
                    pmgr = new YPersistenceManager(getPMSessionFactory());
                    pmgr.startTransactionalSession();
                }

                resumeCase(pmgr, id);

                if (isPersisting())
                {
                    pmgr.commit();
                    pmgr = null;
                }
            }
            catch (Exception e)
            {
                logger.error("Failure to resume case " + id, e);
                if (isPersisting())
                {
                    pmgr.rollbackTransaction();
                }
                throw new YStateException("Could not resume case (See log for details)");
            }
        }
    }

    private void resumeCase(YPersistenceManager pmgr, YIdentifier id) throws YPersistenceException, YStateException, YSchemaBuildingException, YDataStateException, YQueryException
    {
       logger.debug("--> resumeCase: CaseID = " + id.toString());

       // Check current case status and reject call if this case not currently suspended or suspending
       YNetRunner topLevelNet = (YNetRunner)_caseIDToNetRunnerMap.get(id);
       if ((topLevelNet.getCasedata().getExecutionState() == YCaseData.SUSPEND_STATUS_SUSPENDING) ||
           (topLevelNet.getCasedata().getExecutionState() == YCaseData.SUSPEND_STATUS_SUSPENDED))
       {
           Vector runners = getRunnersForPrimaryCase(id);

           // Go thru all runners and set status to normal
           Enumeration runnersEnum = runners.elements();
           while(runnersEnum.hasMoreElements())
           {
               YNetRunner runner = (YNetRunner)runnersEnum.nextElement();
               logger.debug("Current status of runner " + runner.get_caseID() + " = " + runner.getCasedata().getExecutionState());
               runner.getCasedata().setExecutionState(YCaseData.SUSPEND_STATUS_NORMAL);

               if (pmgr != null)
               {
                   pmgr.updateObject(runner.getCasedata());
               }

               runner.kick(pmgr);

               if (pmgr != null)
               {
                   /**
                    * Update persistence only if this runner has not completed. If it has completed (as in the case
                    * where we resume a case and the last workitem has previously been completed), the above call
                    * to 'kick' will have progressed the net to its end point, so the persistence object will have
                    * been deleted.
                    */
                   if (!runner.isCompleted())
                   {
                       pmgr.updateObject(runner);
                   }
               }
           }

           logger.info("Case " + topLevelNet.getCaseID() + " has resumed execution");
           observerGatewayController.notifyCaseResumption(id);
       }
       else
       {
           throw new YStateException("Case " + topLevelNet.getCaseID() + " cannot be suspended as currently not executing normally (SuspendStatus=" + topLevelNet.getCasedata().getExecutionState() + ")");
       }
       logger.debug("<-- resumeCase");
   }

    /**
     * Returns the execution status of a case.
     *
     * @param id
     * @return
     * @throws YPersistenceException
     */
    public int getCaseExecutionStatus(YIdentifier id) throws YPersistenceException
    {
        YNetRunner runner = (YNetRunner)_caseIDToNetRunnerMap.get(id);
        return runner.getCasedata().getExecutionState();
    }

    public YCaseData getCaseData(YIdentifier id)
    {
        YNetRunner runner = (YNetRunner)_caseIDToNetRunnerMap.get(id);
        if (runner != null)
            return runner.getCasedata();
        else return null ;
    }


    /**
     * Helper routine which returns a vector of all net runners for a top level caseID.
     *
     * @param caseID
     * @return  Vector of net runners
     */
    private Vector getRunnersForPrimaryCase(YIdentifier caseID)
    {
        String match = caseID.toString() + ".";

        Vector runners = new Vector();

        Iterator iter = _workItemRepository.getNetRunners().values().iterator();

        while(iter.hasNext())
        {
            YNetRunner runner = (YNetRunner)iter.next();
            String thisID;
            if (runner.getCaseID().toString().indexOf(".") == -1)
            {
                thisID = runner.getCaseID().toString() + ".";
            }
            else
            {
                thisID = runner.getCaseID().toString();
            }

            if (thisID.startsWith(match))
            {
                runners.add(runner);
            }
        }

        return runners;
    }

    /**
     * Helper routine which attempts to progress the suspension status of a case.
     *
     * Where a Case is "suspending", we scan the workitems associated with all the nets associated
     * with the case, and where no workitems are enabled, executing or fired, we progress the suspension state from
     * "suspending" to "suspended".
     *
     * @param caseID
     */
    private void progressCaseSuspension(YPersistenceManager pmgr, YIdentifier caseID) throws YPersistenceException, YStateException
    {
        logger.debug("--> progressCaseSuspension: CaseID=" + caseID);

        // Navigate up the net call stack to get the top level net
        YIdentifier netID = caseID;
        while(true)
        {
            if (netID.getParent() != null)
            {
                netID = netID.getParent();
            }
            else
            {
                break;
            }
        }

        YNetRunner runner = (YNetRunner)_caseIDToNetRunnerMap.get(netID);
        if (runner.getCasedata().getExecutionState() != YCaseData.SUSPEND_STATUS_SUSPENDING)
        {
            throw new YStateException("Case " + caseID + " cannot be suspended as not currently attempting to suspend");
        }
        else
        {
            Vector runners = getRunnersForPrimaryCase(netID);

            Enumeration runnersEnum = runners.elements();
            boolean executingTasks = false;

            while(runnersEnum.hasMoreElements())
            {
                runner = (YNetRunner)runnersEnum.nextElement();

                // Go thru busy and executing tasks and see if we have any atomic tasks
                Iterator iter = runner.getBusyTasks().iterator();
                while(iter.hasNext())
                {
                    if (iter.next() instanceof YAtomicTask)
                    {
                        logger.debug("One or more executing atomic tasks found for case - Cannot fully suspend at this time");
                        executingTasks = true;
                        break;
                    }
                }

                if (!executingTasks)
                {
                    iter = runner.getEnabledTasks().iterator();
                    while(iter.hasNext())
                    {
                        if (iter.next() instanceof YAtomicTask)
                        {
                            logger.debug("One or more enabled atomic tasks found for case - Cannot fully suspend at this time");
                            executingTasks = true;
                            break;
                        }
                    }
                }
            }

            if (!executingTasks)
            {
                // No executing tasks found so go thru nets and set state to suspended
                Enumeration runnersEnum2 = runners.elements();
                while(runnersEnum2.hasMoreElements())
                {
                    YNetRunner runner2 = (YNetRunner)runnersEnum2.nextElement();
                    runner2.getCasedata().setExecutionState(YCaseData.SUSPEND_STATUS_SUSPENDED);

                    if (pmgr != null)
                    {
                        pmgr.updateObject(runner2);
                    }
                }
                logger.info("Case " + caseID + " has suspended successfully. Announcing suspended.");
                observerGatewayController.notifyCaseSuspended(netID);
            }
        }
        logger.debug("<-- progressCaseSuspension");
    }


    /***************************************************************************/
    /** Required methods to enable exception handling service (MJA 04-09/2006) */
    /***************************************************************************/

    /** sets the URI passed as an observer of exception events */
    public boolean setExceptionObserver(String observerURI){
        _exceptionObserver = new InterfaceX_EngineSideClient(observerURI);
        return (_exceptionObserver != null) ;
    }

    /** removes the current exception event observer (max of one at any time) */
    public boolean removeExceptionObserver() {
        _exceptionObserver = null ;
        return true ;
    }

    public String getYawlVersion() { return _yawlVersion ; }

    public void setResourceService(YAWLServiceReference ys) {
        _resourceObserver = ys ;
    }


    /** These next four methods announce an exception event to the observer */
    protected void announceCheckWorkItemConstraints(InterfaceX_EngineSideClient ixClient,
                                                    YWorkItem item, Document data,
                                                    boolean preCheck) {
        logger.debug("Announcing Check Constraints for task " + item.getIDString() +
                     " on client " + ixClient.toString());
        ixClient.announceCheckWorkItemConstraints(item, data, preCheck);
    }

    protected void announceCheckCaseConstraints(InterfaceX_EngineSideClient ixClient,
                           String specID, String caseID, String data, boolean preCheck) {
        logger.debug("Announcing Check Constraints for case " + caseID +
                     " on client " + ixClient.toString());
        ixClient.announceCheckCaseConstraints(specID, caseID, data, preCheck);
    }

    public void announceCancellationToExceptionService(InterfaceX_EngineSideClient ixClient,
                                                       YIdentifier caseID) {
        logger.debug("Announcing Cancel Case for case " + caseID.get_idString() +
                     " on client " + ixClient.toString());
            ixClient.announceCaseCancellation(caseID.get_idString());
    }

    public void announceTimeOutToExceptionService(InterfaceX_EngineSideClient ixClient,
                                                   YWorkItem item, List timeOutTaskIds) {
        logger.debug("Announcing Time Out for item " + item.getWorkItemID() +
                     " on client " + ixClient.toString());
            ixClient.announceTimeOut(item, timeOutTaskIds);
    }

    public void announceTimerExpiryToExceptionService(YWorkItem item) {
        if (_exceptionObserver != null) _exceptionObserver.announceTimeOut(item, null);
    }

    /** updates the workitem with the data passed after completion of an exception handler */
    public boolean updateWorkItemData(String workItemID, String data) {
        synchronized (mutex) {
            YWorkItem workItem = getWorkItem(workItemID);
            YPersistenceManager pmgr = null ;

            if (workItem != null) {
                if (isPersisting()) {
                    try {
                        pmgr = new YPersistenceManager(getPMSessionFactory());
                        pmgr.startTransactionalSession();
                    }
                    catch (YPersistenceException e) {
                        pmgr = null ;
                    }
                }
                try {
                    workItem.setData(pmgr, JDOMUtil.stringToElement(data));
                    if (pmgr != null) pmgr.commit();
                    return true ;
                }
                catch (YPersistenceException e) {
                    return false ;
                }
            }
            return false ;
        }
    }

    /** updates the case data with the data passed after completion of an exception handler */
    public boolean updateCaseData(String idStr, String data) {
        synchronized (mutex) {
            YPersistenceManager pmgr = null ;
            YNetRunner runner = (YNetRunner) _caseIDToNetRunnerMap.get(getCaseID(idStr));

            if (runner != null) {
                if (isPersisting()) {
                    try {
                        pmgr = new YPersistenceManager(getPMSessionFactory());
                        pmgr.startTransactionalSession();
                    }
                    catch (YPersistenceException e) {
                        pmgr = null ;
                    }
                }

                try {
                    YNet net = runner.getNet();
                    Element updatedVars = JDOMUtil.stringToElement(data);
                    List vars = updatedVars.getChildren();
                    Iterator itr = vars.iterator();
                    while (itr.hasNext()){
                        Element eVar = (Element) itr.next();
                        net.assignData(pmgr, (Element) eVar.clone());
                    }
                    if (pmgr != null) pmgr.commit();
                    return true;
                }
                catch (Exception e) {
                    logger.error("Problem updating Case Data for case " + idStr, e);
                }
            }
        }
        return false ;
    }

    /** @return the current case data for the case id passed */ 
    public Document getCaseDataDocument(String id) {
        YNetRunner runner = (YNetRunner) _caseIDToNetRunnerMap.get(getCaseID(id));
        if (runner != null)
            return runner.getNet().getInternalDataDocument() ;
        else
            return null;
    }

    /** cancels the workitem - marks final status as 'failed' if statusFail is true,
     *  or 'cancelled' if it is false */
    public void cancelWorkItem(YWorkItem workItem, boolean statusFail)  {

         synchronized (mutex) {
             YPersistenceManager pmgr = null;

             try {
                if (workItem != null) {
                   if (workItem.getStatus().equals(YWorkItemStatus.statusExecuting)) {
                       YIdentifier caseID = workItem.getCaseID().getParent() ;
                       YNetRunner runner = _workItemRepository.getNetRunner(caseID);
                       String taskID = workItem.getTaskID();

                       if (isPersisting()) {
                          pmgr = new YPersistenceManager(getPMSessionFactory());
                          pmgr.startTransactionalSession();
                       }
                       runner.cancelTask(pmgr, taskID);
                       workItem.setStatusToDeleted(pmgr, statusFail);
                       runner.continueIfPossible(pmgr);

                       if (pmgr != null) pmgr.commit();
                   }
                }
             }
             catch (Exception e) {
                logger.error("Failure whilst persisting workitem cancellation", e);
             }
        }
   }

}
