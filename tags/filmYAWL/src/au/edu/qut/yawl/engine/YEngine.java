/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import au.edu.qut.yawl.authentication.UserList;
import au.edu.qut.yawl.elements.*;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.elements.state.YInternalCondition;
import au.edu.qut.yawl.engine.interfce.InterfaceB_EngineBasedClient;
import au.edu.qut.yawl.engine.interfce.interfaceX.InterfaceX_EngineSideClient;
import au.edu.qut.yawl.exceptions.*;
import au.edu.qut.yawl.logging.YawlLogServletInterface;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.util.YDocumentCleaner;
import au.edu.qut.yawl.util.YMessagePrinter;
import au.edu.qut.yawl.util.YVerificationMessage;
import au.edu.qut.yawl.util.JDOMConversionTools;
import au.edu.qut.yawl.admintool.model.HumanResource;
import net.sf.hibernate.Query;
import net.sf.hibernate.SessionFactory;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.*;
import java.util.*;
import java.net.URI;

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
    private static final boolean ENGINE_PERSISTS_BY_DEFAULT = false;
    private static Logger logger;

    private Map _specifications = new HashMap();
    protected Map _caseIDToNetRunnerMap = new HashMap();
    private Map _runningCaseIDToSpecIDMap = new HashMap();
    private static YWorkItemRepository _workItemRepository;
    private static YEngine _myInstance;
    private InterfaceAManagementObserver _interfaceAClient;
    private InterfaceBClientObserver _interfaceBClient;
    private ObserverGatewayController observerGatewayController = null;
    private Map _yawlServices = new HashMap();
    private static YawlLogServletInterface yawllog = null;
    private static UserList _userList;
    private Map _unloadedSpecifications = new HashMap();
    private final Object mutex = new Object();

    private static InterfaceX_EngineSideClient _exceptionObserver = null ;

    /*************************************************/
    /*INSERTED VARIABLES AND METHODS FOR PERSISTANCE */
    /**
     * *********************************************
     */
    private static boolean journalising;
    private static boolean restoring;
    private static int maxcase = 0;
    private static SessionFactory factory = null;

    /**
     * AJH: Switch indicating if we generate user interface attributes with a tasks output XML doclet.
     */
    private static boolean generateUIMetaData = false;

    /**
     * Constructor.
     */
    private YEngine() {
        yawllog = YawlLogServletInterface.getInstance();
        observerGatewayController = new ObserverGatewayController();

        /**
         * Initialise the standard Observer Gateways.
         *
         * Currently the only standard gateway is the HTTP driven Servlet client.
         */
        ObserverGateway stdHttpObserverGateway = new InterfaceB_EngineBasedClient();
        observerGatewayController.addGateway(stdHttpObserverGateway);
    }


    protected List addSpecifications(String uri) throws YPersistenceException {
        try {
            return addSpecifications(new File(uri), true, new Vector());
        } catch (Exception e) {
            System.out.println("Exception caught in adding specification");
            return null;
        }
    }

    protected void addRunner(YNetRunner runner) {
        String specID = runner.getYNetID();
        YSpecification specification = (YSpecification) _specifications.get(specID);
        if (specification != null) {
            /*
              initialize the runner
             */
            runner.setEngine(this);
            runner.restoreprepare();
            _caseIDToNetRunnerMap.put(runner.getCaseID(), runner);
            _runningCaseIDToSpecIDMap.put(runner.getCaseID(), specID);

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
            Query query = pmgr.createQuery("from au.edu.qut.yawl.admintool.model.Resource" +
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
            query = pmgr.createQuery("from au.edu.qut.yawl.elements.YAWLServiceReference");

            for (Iterator it = query.iterate(); it.hasNext();) {
                YAWLServiceReference service = (YAWLServiceReference) it.next();
                addYawlService(service);
            }

            logger.info("Restoring Specifications - Starts");
            query = pmgr.createQuery("from au.edu.qut.yawl.engine.YSpecFile");

            for (Iterator it = query.iterate(); it.hasNext();) {
                YSpecFile spec = (YSpecFile) it.next();
                String xml = spec.getXML();

                {
                    logger.debug("Restoring specification " + spec.getId());

                    File f = File.createTempFile("yawltemp", null);
                    BufferedWriter buf = new BufferedWriter(new FileWriter(f));
                    buf.write(xml, 0, xml.length());
                    buf.close();
                    addSpecifications(f.getAbsolutePath());

                    f.delete();
                }
            }
            logger.info("Restoring Specifications - Ends");

            logger.info("Restoring process instances - Starts");
            query = pmgr.createQuery("from au.edu.qut.yawl.engine.YNetRunner order by case_id");
            for (Iterator it = query.iterate(); it.hasNext();) {
                YNetRunner runner = (YNetRunner) it.next();
                runners.add(runner);
            }

            HashMap map = new HashMap();
            for (int i = 0; i < runners.size(); i++) {
                YNetRunner runner = (YNetRunner) runners.get(i);
                String id = runner.get_caseID();
                query = pmgr.createQuery("select from au.edu.qut.yawl.engine.YLogIdentifier where case_id = '" + id + "'");
                for (Iterator it = query.iterate(); it.hasNext();) {
                    YLogIdentifier ylogid = (YLogIdentifier) it.next();
                    map.put(ylogid.getIdentifier(), ylogid);
                }
            }
            YawlLogServletInterface.getInstance().setListofcases(map);

            int checkedrunners = 0;

            Vector storedrunners = (Vector) runners.clone();

            while (checkedrunners < runners.size()) {

                for (int i = 0; i < runners.size(); i++) {
                    YNetRunner runner = (YNetRunner) runners.get(i);

                    if (runner.getContainingTaskID() == null) {

                        //This is a root net runner
                        YSpecification specification = getSpecification(runner.getYNetID());
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

                        }
                        checkedrunners++;

                    } else {
                        //This is not a root net, but a decomposition

                        // Find the parent runner
                        String myid = runner.get_standin_caseIDForNet().toString();
                        String parentid = myid.substring(0, myid.lastIndexOf("."));


                        YNetRunner parentrunner = (YNetRunner) runnermap.get(parentid);

                        if (parentrunner != null) {
                            YNet parentnet = parentrunner.getNet();

                            YCompositeTask task = (YCompositeTask) parentnet.getNetElement(runner.getContainingTaskID());
                            runner.setTask(task);

                            YNet net = (YNet) task.getDecompositionPrototype().clone();
                            runner.setNet(net);
                            runnermap.put(runner.get_standin_caseIDForNet().toString(), runner);

                            checkedrunners++;
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
            query = pmgr.createQuery("from au.edu.qut.yawl.engine.YWorkItem");
            for (Iterator it = query.iterate(); it.hasNext();) {
                YWorkItem witem = (YWorkItem) it.next();

                if (witem.getStatus().equals(YWorkItem.statusEnabled)) {
                    witem.setStatus(YWorkItem.statusEnabled);
                }
                if (witem.getStatus().equals(YWorkItem.statusFired)) {
                    witem.setStatus(YWorkItem.statusFired);
                }
                if (witem.getStatus().equals(YWorkItem.statusExecuting)) {
                    witem.setStatus(YWorkItem.statusExecuting);
                }
                if (witem.getStatus().equals(YWorkItem.statusComplete)) {
                    witem.setStatus(YWorkItem.statusComplete);
                }
                if (witem.getStatus().equals(YWorkItem.statusIsParent)) {
                    witem.setStatus(YWorkItem.statusIsParent);
                }
                if (witem.getStatus().equals(YWorkItem.statusDeadlocked)) {
                    witem.setStatus(YWorkItem.statusDeadlocked);
                }
                if (witem.getStatus().equals(YWorkItem.statusDeleted)) {
                    witem.setStatus(YWorkItem.statusDeleted);
                }
                if (witem.getStatus().equals(YWorkItem.statusForcedComplete)) {
                    witem.setStatus(YWorkItem.statusForcedComplete);
                }
                if (witem.getStatus().equals(YWorkItem.statusFailed)) {
                    witem.setStatus(YWorkItem.statusFailed);
                }
                if (witem.getStatus().equals(YWorkItem.statusSuspended)) {
                    witem.setStatus(YWorkItem.statusSuspended);
                }

                if (witem.getData_string() != null) {
                    StringReader reader = new StringReader(witem.getData_string());
                    SAXBuilder builder = new SAXBuilder();
                    Document data = builder.build(reader);
                    witem.setInitData(data.getRootElement());
                }

                java.util.StringTokenizer st = new java.util.StringTokenizer(witem.getThisId(), ":");
                String caseandid = st.nextToken();
                java.util.StringTokenizer st2 = new java.util.StringTokenizer(caseandid, ".");
                //String caseid =
                st2.nextToken();
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
                YTask task;
                if (name.indexOf("CompositeTask") != -1) {
                    YNetRunner netRunner_temp = (YNetRunner) runnermap.get(father.toString());
                    task = (YTask) netRunner_temp.getNet().getNetElement(splitname[1]);
                } else {
                    task = (YTask) net.getNetElement(splitname[1]);
                }
                if (task != null) {
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

                    YConditionInterface cond = (YConditionInterface) element;
                    cond.add(pmgr, id);
                }
            }
        }

        idtoid.put(id.toString(), id);
        return id;
    }


    /**
     * *********************************************
     */


    public static YEngine getInstance(boolean journalising) throws YPersistenceException {
        if (_myInstance == null) {
            logger = Logger.getLogger("au.edu.qut.yawl.engine.YEngine");
            logger.debug("--> YEngine: Creating initial instance");
            _myInstance = new YEngine();
            YEngine.setJournalising(journalising);
            //todo TESTING
//            _myInstance.setJournalising(false);

            // Initialise the persistence layer
            factory = YPersistenceManager.initialise(journalising);
            /***************************
             START POSSIBLE RESTORATION PROCESS
             */

            _userList = UserList.getInstance();
            _workItemRepository = YWorkItemRepository.getInstance();

            if (isJournalising()) {
                YPersistenceManager pmgr = new YPersistenceManager(getPMSessionFactory());
                try {
                    pmgr.setRestoring(true);
                    pmgr.startTransactionalSession();
                    _myInstance.restore(pmgr);
                    pmgr.commit();
                    pmgr.setRestoring(false);
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

            ys = new YAWLServiceReference("http://131.181.70.9:8080/yawlWSInvoker/", null);
            ys.setDocumentation("This YAWL Service enables suitably declared" +
                    " workflow tasks to invoke RPC style service on the Web.");
            _myInstance.removeYawlService(ys.getURI());
            _myInstance.addYawlService(ys);

            ys = new YAWLServiceReference("http://131.181.70.9:8080/workletService/ib", null);
            ys.setDocumentation("Worklet Dynamic Process Selection and Exception Service");
            _myInstance.removeYawlService(ys.getURI());
            _myInstance.addYawlService(ys);

            ys = new YAWLServiceReference("http://131.181.70.9:8080/yawlSMSInvoker/ib", null);
            ys.setDocumentation("SMS Message Module. Works if you have an account.");
            _myInstance.removeYawlService(ys.getURI());
            _myInstance.addYawlService(ys);

            ys = new YAWLServiceReference("http://131.181.70.9:8080/timeService/ib", null);
            ys.setDocumentation("Time service, allows tasks to be a timeout task.");
            _myInstance.removeYawlService(ys.getURI());
            _myInstance.addYawlService(ys);

            /**
             * Ensure we have an 'admin' user
             */
            try {
                UserList.getInstance().addUser("admin", "YAWL", true);
            } catch (YAuthenticationException e) {
                // User already exists ??? Do nothing
            }
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
    public List addSpecifications(File specificationFile, boolean ignoreErors, List errorMessages) throws JDOMException, IOException, YPersistenceException {

        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            logger.debug("--> addSpecification: File=" + specificationFile.getAbsolutePath());

            List returnIDs = new Vector();
            List newSpecifications;
            String parsingMsg;
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
                logger.error("Could not build schema.", e);
                e.printStackTrace();
                return null;
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

                            if (journalising) {
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

                        returnIDs.add(specification.getID());
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

            if (!_specifications.containsKey(spec.getID())) {
                _specifications.put(spec.getID(), spec);
                return true;
            }
            return false;
        }
    }

    protected YIdentifier startCase(String username, YPersistenceManager pmgr, String specID, String caseParams, URI completionObserver) throws YStateException, YSchemaBuildingException, YDataStateException, YPersistenceException {
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

        YSpecification specification = (YSpecification) _specifications.get(specID);
        if (specification != null) {
            YNetRunner runner = new YNetRunner(pmgr, specification.getRootNet(), data);
            
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
                logger.info("Persisting process instance " + runner.getCaseID().get_idString());
//AJH                yper.storeData(runner);
                if (pmgr != null) {
                    pmgr.storeObject(runner);
                }
            }

            runner.continueIfPossible(pmgr);

            // LOG CASE EVENT
            yawllog.logCaseCreated(pmgr, runner.getCaseID().toString(), username, specID);

            runner.start(pmgr);
            _caseIDToNetRunnerMap.put(runner.getCaseID(), runner);
            _runningCaseIDToSpecIDMap.put(runner.getCaseID(), specID);

            if (_interfaceBClient != null) {
                logger.debug("Asking client to add case " + runner.getCaseID().toString());
                _interfaceBClient.addCase(specID, runner.getCaseID().toString());
            }

            return runner.getCaseID();
        } else {
            throw new YStateException(
                    "No specification found with ID [" + specID + "]");
        }
    }


    protected void finishCase(YPersistenceManager pmgr, YIdentifier caseIDForNet) throws YPersistenceException {
        logger.debug("--> finishCase: Case=" + caseIDForNet.get_idString());

        _caseIDToNetRunnerMap.remove(caseIDForNet);
        _runningCaseIDToSpecIDMap.remove(caseIDForNet);
        _workItemRepository.cancelNet(caseIDForNet);

        //  LOG CASE EVENT
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
    public void unloadSpecification(String specID) throws YStateException, YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            YPersistenceManager pmgr = null;

            if (isJournalising()) {
                pmgr = new YPersistenceManager(getPMSessionFactory());
                pmgr.startTransactionalSession();
            }

            logger.debug("--> unloadSpecification: ID=" + specID);

            if (_specifications.containsKey(specID)) {
                /* REMOVE FROM PERSISTANT STORAGE*/
                logger.info("Removing process specification " + specID);
                YSpecFile yspec = new YSpecFile();
                yspec.setId(specID);

//AJH           yper.removeData(yspec);
                if (pmgr != null) {
                    pmgr.deleteObject(yspec);
                }

                YSpecification toUnload = (YSpecification) _specifications.remove(specID);
                _unloadedSpecifications.put(specID, toUnload);
            } else {

                if (isJournalising()) {
                    pmgr.rollbackTransaction();
                }
                throw new YStateException(
                        "Engine contains no such specification with id [" +
                        specID + "].");
            }

            if (isJournalising()) {
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
            if (journalising) clearWorkItemsFromPersistence(pmgr, id);
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
    public void cancelCase(YIdentifier id) throws YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            YPersistenceManager pmgr = null;

            if (isJournalising()) {
                pmgr = new YPersistenceManager(getPMSessionFactory());
                pmgr.startTransactionalSession();
            }

            cancelCase(pmgr, id);

            try {
                if (isJournalising()) {
                    pmgr.commit();
                }
            } catch (YPersistenceException e) {
                logger.error("Persistence exception ignored (to be fixed)", e);
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
            if (! item.getStatus().equals(YWorkItem.statusIsParent)) {
                pmgr.deleteObject(item);
            }
        }

        // now clear any parents
        Iterator itrParent = items.iterator();
        while (itrParent.hasNext()) {
            item = (YWorkItem) itrParent.next() ;
            if (item.getStatus().equals(YWorkItem.statusIsParent)) {
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
    public Set getSpecIDs() {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            Set specids = new HashSet(_specifications.keySet());
            specids.addAll(_runningCaseIDToSpecIDMap.values());
            return specids;
        }
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

            return new HashSet(_specifications.keySet());
        }
    }


    public YSpecification getSpecification(String specID) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            logger.debug("--> getSpecification: ID=" + specID);

            if (_specifications.containsKey(specID)) {
                logger.debug("<-- getSpecification: Loaded spec");
                return (YSpecification) _specifications.get(specID);
            } else {
                logger.debug("<-- getSpecification: Unloaded spec");
                return (YSpecification) _unloadedSpecifications.get(specID);
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
                    .append(_runningCaseIDToSpecIDMap.get(caseID))
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
                    .append(_runningCaseIDToSpecIDMap.get(caseID))
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
        }
    }


    //################################################################################
    //   BEGIN REST-FUL SERVICE METHODS
    //################################################################################
    public Set getAvailableWorkItems() {
        // TESTING
        if (logger.isDebugEnabled()) {
            dump();
        }

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


    public YSpecification getProcessDefinition(String specID) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            return (YSpecification) _specifications.get(specID);
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
    public YWorkItem startWorkItem(YWorkItem workItem, String userID) throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            YPersistenceManager pmgr = null;

            logger.debug("--> startWorkItem");

            try {
                if (isJournalising()) {
                    pmgr = new YPersistenceManager(getPMSessionFactory());
                    pmgr.startTransactionalSession();
                }

                YNetRunner netRunner;
                YWorkItem resultantItem = null;
                if (workItem != null) {
                    if (workItem.getStatus().equals(YWorkItem.statusEnabled)) {
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
                            }
                        }
                    } else if (workItem.getStatus().equals(YWorkItem.statusFired)) {
                        workItem.setStatusToStarted(pmgr, userID);
                        netRunner = _workItemRepository.getNetRunner(workItem.getCaseID().getParent());
                        Element dataList = ((YTask) netRunner.getNetElement(workItem.getTaskID())
                                ).getData(workItem.getCaseID());
                        workItem.setData(pmgr, dataList);
                        netRunner.startWorkItemInTask(pmgr, workItem.getCaseID(), workItem.getTaskID());
                        resultantItem = workItem;
                    } else if (workItem.getStatus().equals(YWorkItem.statusDeadlocked)) {
                        resultantItem = workItem;
                    } else {
                        if (isJournalising()) {
                            pmgr.rollbackTransaction();
                        }

                        throw new YStateException("Item (" + workItem.getIDString() + ") status (" +
                                workItem.getStatus() + ") does not permit starting.");
                        //this work item is likely already executing.
                    }
                } else {
                    if (isJournalising()) {
                        pmgr.rollbackTransaction();
                    }
                    throw new YStateException("No such work item currently available.");
                }

                // COMMIT POINT
                if (journalising) {
                    pmgr.commit();
                }

                logger.debug("<-- startWorkItem");
                return resultantItem;
            } catch (Exception e) {
                if (journalising) {
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
    public YTask getTaskDefinition(String specificationID, String taskID) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            YSpecification specification = (YSpecification) _specifications.get(specificationID);
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
            throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            YPersistenceManager pmgr = null;
            logger.debug("--> completeWorkItem");

            try {
                // Create a PM
                if (isJournalising()) {
                    pmgr = new YPersistenceManager(getPMSessionFactory());
                    pmgr.startTransactionalSession();
                }

                Document doc;
                if (workItem != null) {
                    if (workItem.getStatus().equals(YWorkItem.statusExecuting)) {
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
                                if (isJournalising()) {
                                    pmgr.rollbackTransaction();
                                }
                                throw f;
                            } catch (IOException e) {
                                YStateException f = new YStateException(e.getMessage());
                                f.setStackTrace(e.getStackTrace());
                                if (isJournalising()) {
                                    pmgr.rollbackTransaction();
                                }
                                throw f;
                            }
                        }
                        workItem.setStatusToComplete(pmgr, force);
                        workItem.completeData(pmgr, doc);
                    } else if (workItem.getStatus().equals(YWorkItem.statusDeadlocked)) {
                        _workItemRepository.removeWorkItemFamily(workItem);
                    } else {
                        throw new YStateException("WorkItem with ID [" + workItem.getIDString() +
                                "] not in executing state.");
                    }

                    // COMMIT POINT
                    if (isJournalising()) {
                        pmgr.commit();
                    }

                    /**
                     * AJH: Test hook for revised persistence
                     */
                    persistCase(workItem.getCaseID());
                } else {
                    if (isJournalising()) {
                        pmgr.rollbackTransaction();
                    }
                    throw new YStateException("WorkItem argument is equal to null.");
                }
            } catch (Exception e) {
                if (isJournalising()) {
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
                    throw new YSchemaBuildingException(e.getMessage());
                } else {
                    e.printStackTrace();
                    throw new YSchemaBuildingException(e.getMessage());
                }
            }
            logger.debug("<-- completeWorkItem");
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
    public void checkElegibilityToAddInstances(String workItemID) throws YStateException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {

            YWorkItem item = _workItemRepository.getWorkItem(workItemID);
            if (item != null) {
                if (item.getStatus().equals(YWorkItem.statusExecuting)) {
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

            if (isJournalising()) {
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
                            if (isJournalising()) {
                                pmgr.rollbackTransaction();
                            }

                            throw new YStateException("New work item not created.");
                        }
                    } catch (Exception e) {
                        if (isJournalising()) {
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
                if (isJournalising()) {
                    pmgr = new YPersistenceManager(getPMSessionFactory());
                    pmgr.startTransactionalSession();
                }

                if (workItem.hasLiveStatus()) {
                    workItem.setStatusToSuspended(pmgr);  //added
            }
        }
        return workItem ;
    }
   }

    public YWorkItem unsuspendWorkItem(String workItemID) throws YStateException, YPersistenceException {

        synchronized (mutex) {
            YPersistenceManager pmgr = null;
            YWorkItem workItem = _workItemRepository.getWorkItem(workItemID);
            if (workItem != null) {
                if (isJournalising()) {
                    pmgr = new YPersistenceManager(getPMSessionFactory());
                    pmgr.startTransactionalSession();
                }

                if (workItem.getStatus().equals(YWorkItem.statusSuspended))
                    workItem.setStatusToUnsuspended(pmgr);  //added
            }

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
                if (isJournalising()) {
                    pmgr = new YPersistenceManager(getPMSessionFactory());
                    pmgr.startTransactionalSession();
                }

                if (workItem.getStatus().equals(YWorkItem.statusExecuting)) {
                    workItem.rollBackStatus(pmgr);
                    YNetRunner netRunner = _workItemRepository.getNetRunner(workItem.getCaseID().getParent());
                    if (netRunner.rollbackWorkItem(pmgr, workItem.getCaseID(), workItem.getTaskID())) {
                    } else {
                        if (isJournalising()) {
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
                if (isJournalising()) {
                    pmgr.rollbackTransaction();
                }
                throw new YStateException("Work Item[" + workItemID + "] not found.");
            }
        }
    }


    public String launchCase(String username, String specID, String caseParams, URI completionObserver) throws YStateException, YDataStateException, YSchemaBuildingException, YPersistenceException {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            YPersistenceManager pmgr = null;
            String caseIDString;

            logger.debug("--> launchCase");

            // Create a PM
            if (isJournalising()) {
                pmgr = new YPersistenceManager(getPMSessionFactory());
                pmgr.startTransactionalSession();
            }

            YIdentifier caseID = startCase(username, pmgr, specID, caseParams, completionObserver);

            if (caseID != null) {
                caseIDString = caseID.toString();
            } else {
                if (isJournalising()) {
                    pmgr.rollbackTransaction();
                }

                throw new YStateException("No specification found for [" + specID + "].");
            }

            if (isJournalising()) {
                pmgr.commit();
            }
            logger.debug("<-- launchCase");
            return caseIDString;
        }
    }




    /**
     * Given a process specification id return the cases that are its running
     * instances.
     *
     * @param specID the process specification id string.
     * @return a set of YIdentifer caseIDs that are run time instances of the
     *         process specification with id = specID
     */
    public Set getCasesForSpecification(String specID) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            Set resultSet = new HashSet();
            if (_specifications.containsKey(specID) || _unloadedSpecifications.containsKey(specID)) {
                Set caseIDs = _runningCaseIDToSpecIDMap.keySet();
                for (Iterator iterator = caseIDs.iterator(); iterator.hasNext();) {
                    YIdentifier caseID = (YIdentifier) iterator.next();
                    String specIDForCaseID = (String) _runningCaseIDToSpecIDMap.get(caseID);
                    if (specIDForCaseID.equals(specID)) {
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
     * Returns a set of YAWL service references registered in the engine.
     *
     * @return the set of current YAWL services
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
            if (!restoring && isJournalising()) {

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
            return _workItemRepository.getChildrenOf(workItem.getIDString());
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
    protected void announceEnabledTask(YAWLServiceReference yawlService, YWorkItem item) {
        logger.debug("Announcing enabled task " + item.getIDString() + " on service " + yawlService.get_yawlServiceID());
        observerGatewayController.notifyAddWorkItem(yawlService, item);
    }


    public void announceCancellationToEnvironment(YAWLServiceReference yawlService, YWorkItem item) {
        logger.debug("Announcing task cancellation " + item.getIDString() + " on service " + yawlService.get_yawlServiceID());
        observerGatewayController.notifyRemoveWorkItem(yawlService, item);
    }

    protected void announceCaseCompletionToEnvironment(YAWLServiceReference yawlService, YIdentifier caseID, Document casedata) {
        observerGatewayController.notifyCaseCompletion(yawlService, caseID, casedata);
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
     * @return the case ids of the current unfinished processes.
     */
    public List getRunningCaseIDs() {
        return new ArrayList(_runningCaseIDToSpecIDMap.keySet());
    }

    public String getLoadStatus(String specID) {
        /**
         * SYNC'D External interface
         */
        synchronized (mutex) {
            if (_specifications.containsKey(specID)) {
                return YSpecification._loaded;
            } else if (_unloadedSpecifications.containsKey(specID)) {
                return YSpecification._unloaded;
            } else {
                throw new RuntimeException("SpecID [" + specID + "] is neither loaded nor unloaded.");
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
            YAWLServiceReference service = (YAWLServiceReference) _yawlServices.get(serviceURI);

            if (service != null) {
                if (isJournalising()) {
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
     * @return if the engine if configured to store data in persistence.
     */
    public static boolean isJournalising() {
        return journalising;
    }

    /**
     * Indicates if persistence is to the database.
     *
     * @param arg
     */
    private static void setJournalising(boolean arg) {
        journalising = arg;
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

            logger.debug("\n*** DUMPING " + _specifications.size() + " SPECIFICATIONS ***");
            {
                Iterator keys = _specifications.keySet().iterator();
                int sub = 0;
                while (keys.hasNext()) {
                    sub++;
                    String key = (String) keys.next();
                    YSpecification spec = (YSpecification) _specifications.get(key);

                    logger.debug("Entry " + sub + " Key=" + key);
                    logger.debug("    ID             " + spec.getID());
                    logger.debug("    Name           " + spec.getName());
                    logger.debug("    Beta Version   " + spec.getBetaVersion());
                }
            }
            logger.debug("*** DUMP OF SPECIFICATIONS ENDS ***");

            logger.debug("\n*** DUMPING " + _caseIDToNetRunnerMap.size() + " ENTRIES IN CASE_ID_2_NETRUNNER MAP ***");
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
                    }
                }
            }

            logger.debug("*** DUMP OF CASE_ID_2_NETRUNNER_MAP ENDS");

            logger.debug("*** DUMP OF RUNNING CASES TO SPEC MAP STARTS ***");
            {
                Iterator keys = _runningCaseIDToSpecIDMap.keySet().iterator();
                int sub = 0;
                while (keys.hasNext()) {
                    sub++;
                    Object objKey = keys.next();

                    if (objKey == null) {
                        logger.debug("key is NULL !!!");
                    } else {
                        YIdentifier key = (YIdentifier) objKey;
                        String spec = (String) _runningCaseIDToSpecIDMap.get(key);
                        logger.debug("Entry " + sub + " Key=" + key);
                        logger.debug("    ID             " + spec);
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

    private static SessionFactory getPMSessionFactory() {
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

            if (isJournalising()) {
                YPersistenceManager pmgr = new YPersistenceManager(getPMSessionFactory());
                pmgr.startTransactionalSession();
                pmgr.storeObject(obj);
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

        if (journalising) {
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

        if (journalising) {
            try {
                List list = id.get_children();
                for (int i = 0; i < list.size(); i++) {
                    YIdentifier child = (YIdentifier) list.get(i);
                    clearCaseDelegate(pmgr, child);
                }

                boolean runnerfound = false;

                // if its a runner, remove it
                Query query = pmgr.getSession().createQuery(
                               "from au.edu.qut.yawl.engine.YNetRunner where case_id = '"
                               + id.toString() + "'");
                for (Iterator it = query.iterate(); it.hasNext();) {
                    pmgr.deleteObject(it.next());
                    runnerfound = true;
                }

                // it's not a runner, so remove the P_YIdentifier only
                if (!runnerfound) {
                    Query quer = pmgr.getSession().createQuery(
                                 "from au.edu.qut.yawl.engine.P_YIdentifier where id = '"
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
    public String getMaxCase() {
        if (!isJournalising()) {
            maxcase++;
            return Integer.toString(maxcase);
        } else {
            YPersistenceManager pmgr = new YPersistenceManager(getPMSessionFactory());
            return pmgr.getMaxCase();
        }
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

    /** updates the workitem with the data passed after completion of an exception handler */
    public boolean updateWorkItemData(String workItemID, String data) {
        synchronized (mutex) {
            YWorkItem workItem = getWorkItem(workItemID);
            YPersistenceManager pmgr = null ;

            if (workItem != null) {
                if (isJournalising()) {
                    try {
                        pmgr = new YPersistenceManager(getPMSessionFactory());
                        pmgr.startTransactionalSession();
                    }
                    catch (YPersistenceException e) {
                        pmgr = null ;
                    }
                }
                try {
                    workItem.setData(pmgr, JDOMConversionTools.stringToElement(data));
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
                if (isJournalising()) {
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
                    Element updatedVars = JDOMConversionTools.stringToElement(data);
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

    /** cancels the workitem - marks final status as 'failed' if statusFail is true,
     *  or 'cancelled' if it is false */
    public void cancelWorkItem(YWorkItem workItem, boolean statusFail)  {

         synchronized (mutex) {
             YPersistenceManager pmgr = null;

             try {
                if (workItem != null) {
                   if (workItem.getStatus().equals(YWorkItem.statusExecuting)) {
                       YIdentifier caseID = workItem.getCaseID().getParent() ;
                       YNetRunner runner = _workItemRepository.getNetRunner(caseID);
                       String taskID = workItem.getTaskID();

                       if (isJournalising()) {
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
             catch (YPersistenceException e) {
                logger.error("Failure whilst persisting workitem cancellation", e);
             }
        }
   }

}
