/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.engine;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.yawlfoundation.yawl.authentication.YClient;
import org.yawlfoundation.yawl.authentication.YExternalClient;
import org.yawlfoundation.yawl.authentication.YSessionCache;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.elements.state.YInternalCondition;
import org.yawlfoundation.yawl.engine.announcement.AnnouncementContext;
import org.yawlfoundation.yawl.engine.instance.InstanceCache;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceADesign;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceAManagement;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceAManagementObserver;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBClientObserver;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBInterop;
import org.yawlfoundation.yawl.engine.time.YTimer;
import org.yawlfoundation.yawl.engine.time.YWorkItemTimer;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.logging.YEventLogger;
import org.yawlfoundation.yawl.logging.YLogDataItem;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.logging.YLogPredicate;
import org.yawlfoundation.yawl.logging.table.YAuditEvent;
import org.yawlfoundation.yawl.schema.YDataValidator;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

/**
 * @author Lachlan Aldred
 *         Date: 17/06/2003
 *         Time: 13:46:54
 *
 * @author Michael Adams (refactoring for v2.0-2.1)
 */

public class YEngine implements InterfaceADesign,
                                InterfaceAManagement,
                                InterfaceBClient,
                                InterfaceBInterop {

    // STATIC MEMBERS //

    // Engine statuses
    public static final int ENGINE_STATUS_DORMANT = -1;
    public static final int ENGINE_STATUS_INITIALISING = 0;
    public static final int ENGINE_STATUS_RUNNING = 1;
    public static final int ENGINE_STATUS_TERMINATING = 2;

    // Default Persistence flag
    private static final boolean ENGINE_PERSISTS_BY_DEFAULT = false;

    private static YEngine _thisInstance;                         // reference to self
    private static final String _yawlVersion = "2.1" ;            // current version
    private static YEventLogger _yawllog = null;
    private static YSessionCache _sessionCache;
    private static boolean _persisting;
    private static boolean _restoring;
    private static YCaseNbrStore _caseNbrStore;
    private static SessionFactory _factory = null;               // for persistence
    private static boolean _generateUIMetaData = true;           // extended attributes
    private static Logger _logger;
    private static Set<YWorkItemTimer> _expiredTimers ;

    // NON-STATIC MEMBERS //

    protected Map<YIdentifier, YNetRunner> _caseIDToNetRunnerMap =
            new HashMap<YIdentifier, YNetRunner>();
    private Map<YIdentifier,YSpecification> _runningCaseIDToSpecMap =
            new HashMap<YIdentifier, YSpecification>();
    private Map<String, YAWLServiceReference> _yawlServices =
            new HashMap<String, YAWLServiceReference>();
    private Map<String, YExternalClient> _externalClients =
            new HashMap<String, YExternalClient>(); 

    private YSpecificationTable _specifications = new YSpecificationTable();
    private static YWorkItemRepository _workItemRepository;
    private InterfaceAManagementObserver _interfaceAClient;
    private InterfaceBClientObserver _interfaceBClient;
    private final Object mutex = new Object();
    private int engineStatus = ENGINE_STATUS_DORMANT;
    private YAnnouncer _announcer = null;              // handles all i'face notifys
    private YAWLServiceReference _defaultWorklist = null ;
    private InstanceCache instanceCache = new InstanceCache();
    private YBuildProperties _buildProps;
    private String engineClassesRootFilePath;
    private boolean _allowGenericAdminID;

    /********************************************************************************/

    /**
     * The Constructor - called from getInstance().
     */
    protected YEngine() {

        // get instances of global objects
        _sessionCache = YSessionCache.getInstance();
        _workItemRepository = YWorkItemRepository.getInstance();
        _caseNbrStore = YCaseNbrStore.getInstance();
        _announcer = new YAnnouncer(this);         // the 'pusher' of interface events
    }


    /**
     * Initialises the engine (if not already initialised) & returns the engine instance.
     * @param persisting true if engine state is to be persisted
     * @return a reference to the initialised engine
     * @throws YPersistenceException if there's a problem restoring from persistence
     */
    public static YEngine getInstance(boolean persisting) throws YPersistenceException {
        if (_thisInstance == null) {
            _logger = Logger.getLogger(YEngine.class);
            _logger.debug("--> YEngine: Creating initial instance");
            _thisInstance = new YEngine();
		      	_thisInstance.setEngineStatus(YEngine.ENGINE_STATUS_INITIALISING);

            // Initialise the persistence layer & restore state
            YEngine.setPersisting(persisting);
            _factory = YPersistenceManager.initialise(persisting);

            if (isPersisting()) {
                _caseNbrStore.setPersisting(true);
                _thisInstance.restore();
            }

            // init the process logger
            _yawllog = YEventLogger.getInstance(_thisInstance);

            // Init completed - set engine status to up and running
            _logger.info("Marking engine status = RUNNING");
            _thisInstance.setEngineStatus(YEngine.ENGINE_STATUS_RUNNING);
        }
        return _thisInstance;
    }


    /**
     * Initialises the engine (if not already initialised) & returns the engine instance,
     * using the default persistence flag.
     * @return a reference to the initialised engine
     */
    public static YEngine getInstance() {
        if (_thisInstance == null) {
            try {
                _thisInstance = getInstance(ENGINE_PERSISTS_BY_DEFAULT);
            } catch (Exception e) {
                throw new RuntimeException("Failure to instantiate the engine.");
            }
        }
        return _thisInstance;
    }


    /**
     * Checks if the engine is currently running
     * @return true if running, false otherwise
     */
    public static boolean isRunning() {
        return (_thisInstance != null) &&
               (_thisInstance.getEngineStatus() == YEngine.ENGINE_STATUS_RUNNING) ;
    }


    /**
     * Restores persisted data when the engine restarts.
     * @throws YPersistenceException when there's a problem with the restore process
     */
    private void restore() throws YPersistenceException {
        _logger.debug("--> restore");
        _restoring = true;

        YPersistenceManager pmgr = new YPersistenceManager(getPMSessionFactory());
        YEngineRestorer restorer = new YEngineRestorer(_thisInstance, pmgr);
        try {
            // start persistence session
            pmgr.setRestoring(true);
            pmgr.startTransactionalSession();

            // restore data objects from persistence
            restorer.restoreYAWLServices();
            restorer.restoreExternalClients();
            restorer.restoreSpecifications();
            _caseNbrStore = restorer.restoreNextAvailableCaseNumber();
            restorer.restoreProcessInstances();
            restorer.restoreWorkItems();
            _expiredTimers = restorer.restoreWorkItemTimers();
            restorer.restartRestoredProcessInstances();

            // complete session
            pmgr.commit();
            pmgr.setRestoring(false);
            pmgr.closeSession();

            // log result
            if (_logger.isDebugEnabled()) dump();
        }
        catch (YPersistenceException ype) {
            _logger.fatal("Failure to restart engine from persistence image", ype);
            throw new YPersistenceException("Failure to restart engine from persistence image");
        }
        catch (Exception e) {

            // a non-YPersistenceException means the restore failed, but the engine is
            // still operational
            _logger.error("Persisted state failed to fully restore - engine is " +
                          "operational but may be in an inconsistent state. Exception: ", e);
        }
        finally {
            _logger.debug("restore <---");
            _restoring = false;
            restorer.persistDefaultClients();
        }
    }

    /**
     * Indicate if user interface metadata is to be generated within a task's input XML doclet.
     * @param generate true to generate metadata, false to not generate it
     */
    public void setGenerateUIMetaData(boolean generate) {
        _generateUIMetaData = generate;
    }


    /**
     * Indicates if user interface metadata will be generated within a task's input XML doclet.
     * @return true=UIMetaData generated, false=UIMetaData not supported
     */
    public boolean generateUIMetaData() {
        return _generateUIMetaData;
    }

    /** returns the current version of this engine */
    public String getYawlVersion() { return _yawlVersion ; }


   public InstanceCache getInstanceCache() {
       return instanceCache;
   }


    public Map<String, YParameter> getParameters(YSpecificationID specID, String taskID,
                                                 boolean input) {
        Map<String, YParameter> result = null;
        YTask task = getTaskDefinition(specID, taskID);
        if (task != null) {
            YDecomposition decomp = task.getDecompositionPrototype();
            if (decomp != null) {
                result = input ? decomp.getInputParameters() : decomp.getOutputParameters();
            }
        }
        return result;
    }


    public String getEngineClassesRootFilePath() { return engineClassesRootFilePath; }


    public void setEngineClassesRootFilePath(String path) {
        String pkgPath = "WEB-INF/classes/org/yawlfoundation/yawl/";
        engineClassesRootFilePath = path + pkgPath;
    }


    // called when servlet init() has completed
    public void initialised() {

        // Now that the engine's running, process any expired timers
        if (_expiredTimers != null) {
            for (YWorkItemTimer timer : _expiredTimers)
                timer.handleTimerExpiry();
        }
    }

    public void shutdown() {
        _sessionCache.shutdown();
        _announcer.shutdownObserverGateways();
        YTimer.getInstance().cancelAll();
        _factory.close();
    }


     public void initBuildProperties(InputStream stream) {
         _buildProps = new YBuildProperties();
         _buildProps.load(stream);
     }


     public YBuildProperties getBuildProperties() {
         return _buildProps;
     }



     public YSessionCache getSessionCache() { return _sessionCache; }


   /*********************************************************************************/

   public synchronized void registerInterfaceAClient(InterfaceAManagementObserver observer) {
       _interfaceAClient = observer;
   }

   public synchronized void registerInterfaceBObserver(InterfaceBClientObserver observer) {
       _interfaceBClient = observer;
   }

   /**
    * Registers an InterfaceB Observer Gateway with the engine in order to receive callbacks.
    *
    * @param gateway
    */
    public synchronized void registerInterfaceBObserverGateway(ObserverGateway gateway) {
        _announcer.registerInterfaceBObserverGateway(gateway);
    }

    public YAnnouncer getAnnouncer() {
        return _announcer;
    }

    public synchronized void setEngineStatus(int engineStatus) {
        this.engineStatus = engineStatus;
    }

    public synchronized int getEngineStatus() {
        return this.engineStatus;
    }

    public synchronized AnnouncementContext getAnnouncementContext() {
        return _announcer.getAnnouncementContext();
    }

    public int reannounceEnabledWorkItems() throws YStateException {
        return getAnnouncer().reannounceEnabledWorkItems();

     }

    public int reannounceExecutingWorkItems() throws YStateException {
        return getAnnouncer().reannounceExecutingWorkItems();
    }

    public int reannounceFiredWorkItems() throws YStateException {
        return getAnnouncer().reannounceFiredWorkItems();
    }

    public void reannounceWorkItem(YWorkItem workItem) throws YStateException {
        getAnnouncer().reannounceWorkItem(workItem);
    }


   /*****************************************************************************/

    /**
     * Adds a net runner instance to the engine caches. The specification is derived from
     * the runner instance.
     * @param runner the runner to add
     */
    protected void addRunner(YNetRunner runner) {
        YSpecificationID specID = runner.getSpecificationID();
        YSpecification specification = _specifications.getSpecification(specID);
        addRunner(runner, specification);
    }


    /**
     * Adds a net runner instance to the engine caches.
     * @param runner the runner to add
     * @param specification its specification
     */
    public synchronized void addRunner(YNetRunner runner, YSpecification specification) {
        if (specification != null) {
            runner.setEngine(this);
            runner.restoreprepare();
            _caseIDToNetRunnerMap.put(runner.getCaseID(), runner);
            _runningCaseIDToSpecMap.put(runner.getCaseID(), specification);
            instanceCache.addCase(runner.getCaseID().toString(),
                                  specification.getSpecificationID(),
                                  runner.getCasedata().getData(), null,
                                  runner.getStartTime());

            // announce the add
            if (_interfaceBClient != null) {
                _interfaceBClient.addCase(specification.getSpecificationID(),
                        runner.getCaseID().toString());
            }
        }
    }

    /**
     * Returns a vector of all net runners for a top level caseID.
     * @param primaryCaseID the id of the case
     * @return Vector of net runners for the case
     */
    private Vector<YNetRunner> getRunnersForPrimaryCase(YIdentifier primaryCaseID) {
        Vector<YNetRunner> runners = new Vector<YNetRunner>();
        for (YNetRunner runner : _workItemRepository.getNetRunners().values()) {
            if (primaryCaseID.equals(runner.getCaseID()) ||
                primaryCaseID.isAncestorOf(runner.getCaseID())) {

                runners.add(runner);
            }
        }
        return runners;
    }


    public YNetRunner getNetRunner(YWorkItem workItem) {
        YNetRunner runner = null;
        YIdentifier caseID = null;
        if ((workItem.getStatus() == YWorkItemStatus.statusEnabled) ||
            (workItem.getStatus() == YWorkItemStatus.statusIsParent)) {
             caseID = workItem.getCaseID();
        }
        else if (workItem.hasLiveStatus()) {
             caseID = workItem.getCaseID().getParent();
        }
        if (caseID != null) {
            runner = _workItemRepository.getNetRunner(caseID);
        }
        return runner;
    }    


    public synchronized String getNetData(String caseID) throws YStateException {

        // if this is a root net case id, the net data is equivalent to the case data
        if (caseID.indexOf(".") == -1) return getCaseData(caseID);

        YNetRunner subNetRunner = _workItemRepository.getNetRunner(caseID);
        if (subNetRunner != null) {
            return subNetRunner.getCasedata().getData();
        }
        else {
            throw new YStateException("Received invalid case id '" + caseID + "'.");
        }
    }



    /**************************************************************************/

    /**
     * Adds the specification (expressed as an xml string) to the engine
     * @param specStr an XML formatted specification
     * @param ignoreErrors ignore verfication errors and load the spec anyway.
     * @param errorMessages an in/out param passing any error messages.
     * @return the specification ids of the successfully loaded specs
     */
    public synchronized List<YSpecificationID> addSpecifications(String specStr,
                        boolean ignoreErrors, List<YVerificationMessage> errorMessages)
            throws JDOMException, IOException, YPersistenceException {

        _logger.debug("--> addSpecification");

        List<YSpecificationID> result = new Vector<YSpecificationID>();
        List<YSpecification> newSpecifications;
        try {
            newSpecifications = YMarshal.unmarshalSpecifications(specStr);
        }
        catch (YSyntaxException e) {

            // catch the xml parser's exception, transform it into YAWL format
            // and abort the load
            String[] msgs = e.getMessage().split("\n");
            for (String msg : msgs) {
                errorMessages.add(new YVerificationMessage(null, msg,
                        YVerificationMessage.ERROR_STATUS));
            }
            _logger.debug("<-- addSpecifcations: syntax exceptions found");
            return result;
        }
        catch (YSchemaBuildingException e) {
            // if there is an XML Schema problem report it and abort
            e.printStackTrace(); //TODO: propagate
            return result;
        }

        if (newSpecifications != null) {
            for (YSpecification specification : newSpecifications) {
                List<YVerificationMessage> messages = specification.verify();
                if (messages.size() > 0 && ! ignoreErrors) {
                    errorMessages.addAll(messages);
                }

                //if the error messages are empty or contain only warnings
                if (YVerificationMessage.containsNoErrors(errorMessages)) {
                    if (loadSpecification(specification)) {
                        if (_persisting && ! _restoring) {
                            try {
                                storeObject(specification);
                            }
                            catch (YPersistenceException e) {
                                throw new YPersistenceException(
                                        "Failure whilst persisting new specification", e);
                            }
                        }

                        result.add(specification.getSpecificationID());
                    }
                    else {
                        String errDetail = specification.getSchemaVersion().startsWith("2.") ?
                            "UID: " + specification.getID() : "URI: " + specification.getURI();
                        errDetail += "- Version: " + specification.getSpecVersion();
                        errorMessages.add(new YVerificationMessage(this,
                                "There is a specification with an identical id to ["
                                        + errDetail + "] already loaded into the engine.",
                                YVerificationMessage.ERROR_STATUS));
                    }
                }
            }
        }
        _logger.debug("<-- addSpecifications: " + result.size() + " IDs loaded");
        return result;
    }


    /**
     * Loads a specification
     * @param spec the specification to load
     * @return true if spec is loaded, false if it was already loaded
     */
    public boolean loadSpecification(YSpecification spec) {
        synchronized (mutex) {
            if (! _specifications.contains(spec)) {
                _specifications.loadSpecification(spec);
                return true;
            }
            return false;
        }
     }


    /**
     * Removes a previously loaded specification from the engine
     * @param specID the identifier of the specification to unload
     * @throws YStateException if the spec is still in use (with a live case)
     * @throws YPersistenceException if there's some persistence problem
     */
    public synchronized void unloadSpecification(YSpecificationID specID)
            throws YStateException, YPersistenceException {

        _logger.debug("--> unloadSpecification: URI=" + specID.getUri() +
                " Version=" + specID.getVersion().toString());

        YPersistenceManager pmgr = getPersistenceSession();
        if (_specifications.contains(specID)) {
            YSpecification specToUnload = _specifications.getSpecification(specID);

            // Reject unload request if we have active cases using it
            if (_runningCaseIDToSpecMap.values().contains(specToUnload)) {
                if (pmgr != null) pmgr.rollbackTransaction();
                throw new YStateException("Cannot unload specification '" + specID +
                            "' as one or more cases are currently active against it.");
            }

            _logger.info("Removing process specification " + specID);
            if (pmgr != null) pmgr.deleteObject(specToUnload);

            _specifications.unloadSpecification(specToUnload);
            _yawllog.removeSpecificationDataSchemas(specID);
        }
        else {
            // the spec's not in the engine
            if (pmgr != null) pmgr.rollbackTransaction();
            throw new YStateException("Engine contains no such specification with id '"
                    + specID + "'.");
        }

        if (pmgr != null) pmgr.commit();
        _logger.debug("<-- unloadSpecification");
    }



    /**
     * Provides the set of specification ids for specs loaded into the engine.  It returns
     * those that were loaded as well as those with running instances that are unloaded.
     *
     * @return a set of spec id strings.
     */
    public Set<YSpecificationID> getSpecIDs() {
        synchronized (mutex) {
            return _specifications.getSpecIDs();
        }
    }


    /**
     * Returns a set of all loaded process specifications.
     *
     * @return  A set of specification ids
     */
    public Set<YSpecificationID> getLoadedSpecifications() {
        synchronized (mutex) {
            return _specifications.getSpecIDs();
        }
    }

    /**
     * Returns the latest loaded version of a specification identified by 'key'
     * @param key the spec's identifier (v2.0+) or uri (pre-v2.0)
     * @return the matching specification or null if no match found
     */
    public YSpecification getLatestSpecification(String key) {
        synchronized (mutex) {
            _logger.debug("--> getSpecification: ID=" + key);

            if (_specifications.contains(key)) {
                _logger.debug("<-- getSpecification: Loaded spec");
                return _specifications.getLatestSpecification(key);
            } else {
                _logger.debug("<-- getSpecification: Unknown spec");
                return null;
            }
        }
    }

    public synchronized YSpecification getSpecification(YSpecificationID specID) {
            if (specID == null) return null;
            _logger.debug("--> getSpecification: ID=" + specID.toString());

            if (_specifications.contains(specID)) {
                _logger.debug("<-- getSpecification: Loaded spec");
                return _specifications.getSpecification(specID);
            }
            else {
                _logger.debug("<-- getSpecification: Unknown spec");
                return null;
            }
    }

    public synchronized YSpecification getSpecificationForCase(YIdentifier caseID) {
        return _runningCaseIDToSpecMap.get(caseID);
    }


    public synchronized YSpecification getProcessDefinition(YSpecificationID specID) {
        return _specifications.getSpecification(specID);
    }


    public String getSpecificationDataSchema(YSpecificationID specID) {
        String result = null;

        synchronized (mutex) {
            YSpecification spec = _specifications.getSpecification(specID);
            if (spec != null) {
                YDataValidator validator = spec.getDataValidator() ;
                if (validator != null) {
                   result = validator.getSchema();
                }
            }
        }
        return result;
    }


    public synchronized String getLoadStatus(YSpecificationID specID) {
        return _specifications.contains(specID) ? YSpecification._loaded
                : YSpecification._unloaded;
    }


    /***********************************************************************/

    /**
     * Given a process specification id return the cases that are its running
     * instances.
     *
     * @param specID the process specification id string.
     * @return a set of YIdentifer caseIDs that are run time instances of the
     *         process specification with id = specID
     */
    public synchronized Set<YIdentifier> getCasesForSpecification(YSpecificationID specID) {
        Set<YIdentifier> resultSet = new HashSet<YIdentifier>();
        if (_specifications.contains(specID)) {
            for (YIdentifier caseID : _runningCaseIDToSpecMap.keySet()) {
                YSpecification specForCaseID = _runningCaseIDToSpecMap.get(caseID);
                if (specForCaseID.getSpecificationID().equals(specID)) {
                    resultSet.add(caseID);
                }
            }
        }
        return resultSet;
    }


    /**
     * Gets the complete map of all running case ids, grouped by specification id
     * @return a map of [YSpecificationID, List<Yidentifier>]
     */
    public synchronized Map<YSpecificationID, List<YIdentifier>> getRunningCaseMap() {
        Map<YSpecificationID, List<YIdentifier>> caseMap =
                new HashMap<YSpecificationID, List<YIdentifier>>();
        List<YIdentifier> list;
        for (YIdentifier caseID : _runningCaseIDToSpecMap.keySet()) {
            YSpecification specForCaseID = _runningCaseIDToSpecMap.get(caseID);
            YSpecificationID specID = specForCaseID.getSpecificationID();
            list = caseMap.get(specID);
            if (list == null) {
                list = new ArrayList<YIdentifier>();
                caseMap.put(specID, list);
            }
            list.add(caseID);
        }
        return caseMap;
    }


    protected YIdentifier startCase(YPersistenceManager pmgr,
                                    YSpecificationID specID, String caseParams,
                                    URI completionObserver, YLogDataItemList logData)
            throws YStateException, YSchemaBuildingException, YDataStateException,
                   YPersistenceException, YQueryException {
        return startCase(pmgr, specID, caseParams, completionObserver, null, logData, null);
    }


    protected YIdentifier startCase(YPersistenceManager pmgr,
                                    YSpecificationID specID, String caseParams,
                                    URI completionObserver, String caseID,
                                    YLogDataItemList logData)
            throws YStateException, YSchemaBuildingException, YDataStateException,
                    YPersistenceException, YQueryException{
        return startCase(pmgr, specID, caseParams, completionObserver, caseID, logData, null);
    }


    protected synchronized YIdentifier startCase(YPersistenceManager pmgr,
                                    YSpecificationID specID, String caseParams,
                                    URI completionObserver, String caseID,
                                    YLogDataItemList logData, String serviceRef)
            throws YStateException, YSchemaBuildingException, YDataStateException,
                    YPersistenceException, YQueryException{

        // get the latest loaded spec version
        YSpecification specification = _specifications.getSpecification(specID);
        if (specification != null) {

            // check & format case data params (if any)
            Element data = formatCaseParams(caseParams, specification);
            
            YNetRunner runner = new YNetRunner(pmgr, specification.getRootNet(), data, caseID);
            
            // register exception service with the net runner
            _announcer.announceCheckCaseConstraints(specID, runner.getCaseID().toString(),
                    caseParams, true);

            if (completionObserver != null) {
                YAWLServiceReference observer =
                        getRegisteredYawlService(completionObserver.toString());
                if (observer != null) {
                    runner.setObserver(observer);
                } else {
                    _logger.warn("Completion observer: " + completionObserver +
                                 " is not a registered YAWL service.");
                }
            }

            // persist it
            if ((! _restoring) && (pmgr != null)) {
                pmgr.storeObject(runner);
            }

            // log case start event
            YIdentifier runnerCaseID = runner.getCaseID();
            YLogPredicate logPredicate = runner.getNet().getLogPredicate();
            if (logPredicate != null) {
                String predicate = logPredicate.getParsedStartPredicate(runner.getNet());
                if (predicate != null) {
                    logData.add(new YLogDataItem("Predicate", "OnLaunch", predicate, "string"));
                }            
            }
            _yawllog.logCaseCreated(pmgr, specID, runnerCaseID, logData, serviceRef);

            // cache instance
            instanceCache.addCase(runnerCaseID.toString(), specID, caseParams,
                                  logData, runner.getStartTime());

            runner.continueIfPossible(pmgr);
            runner.start(pmgr);
            _caseIDToNetRunnerMap.put(runnerCaseID, runner);
            _runningCaseIDToSpecMap.put(runnerCaseID, specification);

            if (_interfaceBClient != null) {
                _logger.debug("Asking client to add case " + runnerCaseID.toString());
                _interfaceBClient.addCase(specID, runnerCaseID.toString());
            }
            announceEvents(runner.getCaseID());
            return runnerCaseID;
        }
        else {
            throw new YStateException(
                    "No specification found with ID [" + specID + "]");
        }
    }

    protected Element formatCaseParams(String paramStr, YSpecification spec) throws YStateException {
        Element data = null;
        if (paramStr != null && !"".equals(paramStr)) {
            data = JDOMUtil.stringToElement(paramStr);
            if (data == null) {
                throw new YStateException("Invalid or malformed caseParams.");
            }
            else if (! (spec.getRootNet().getID().equals(data.getName()) ||
                       (spec.getURI().equals(data.getName())))) {
                throw new YStateException(
                        "Invalid caseParams: outermost element name must match " +
                                "specification URI or root net name.");
            }
        }
        return data;
    }


    /**
     * Finalises a case completion.
     * @param caseID the id of the completing case
     * @throws YPersistenceException if theres a persistence problem
     */
    protected void finishCase(YIdentifier caseID) throws YPersistenceException {
        _logger.debug("--> finishCase: Case=" + caseID.get_idString());

        _caseIDToNetRunnerMap.remove(caseID);
        _runningCaseIDToSpecMap.remove(caseID);
        _workItemRepository.cancelNet(caseID);
        instanceCache.removeCase(caseID.toString());
        
        if (_interfaceBClient != null)
            _interfaceBClient.removeCase(caseID.toString());

        _logger.debug("<-- finishCase");
    }


    /**
     * Cancels a running case.
     * @param pmgr an initialised persistence manager object
     * @param caseID the identifier of the cancelling case
     * @throws YPersistenceException if there's some persistence problem
     */
    protected synchronized void cancelCase(YPersistenceManager pmgr, YIdentifier caseID,
                                           String serviceHandle)
            throws YPersistenceException {
        _logger.debug("--> cancelCase");

        if (caseID != null) {

            _logger.info("Deleting persisted process instance " + caseID);

            try {
                YNetRunner runner = _caseIDToNetRunnerMap.get(caseID);
                if (_persisting) clearWorkItemsFromPersistence(pmgr, caseID);
                Set<YWorkItem> removedItems = _workItemRepository.removeWorkItemsForCase(caseID);
                YTimer.getInstance().cancelTimersForCase(caseID.toString());
                finishCase(caseID);
                if (runner != null) runner.cancel(pmgr);
                clearCase(pmgr, caseID);
                _announcer.announceCaseCancellationToEnvironment(caseID);
                _yawllog.logCaseCancelled(pmgr, caseID, null, serviceHandle);
                for (YWorkItem item : removedItems) {
                    _yawllog.logWorkItemEvent(pmgr, item,
                            YWorkItemStatus.statusCancelledByCase, null);
                }

            } catch (YPersistenceException e) {
                throw new YPersistenceException(
                                       "Failure whilst persisting case cancellation", e);
            }
        }
        else {
            throw new IllegalArgumentException(
                                       "Attempt to cancel a case using a null caseID");
        }

    }


    /**
     * Cancels the case - External interface.
     *
     * For NetRunner calls to cancel a case, see the other overloaded method which
     * accepts an instance of the current persistence manager object.
     *
     * @param id the case ID.
     * @throws YEngineStateException if engine is not in 'running' state
     * @throws YPersistenceException if there's some persistence problem
     */
    public synchronized void cancelCase(YIdentifier id, String serviceHandle)
            throws YPersistenceException, YEngineStateException {
        if (getEngineStatus() == ENGINE_STATUS_RUNNING) {
            YPersistenceManager pmgr = getPersistenceSession();
            cancelCase(pmgr, id, serviceHandle);
            if (pmgr != null) pmgr.commit();
        }
        else throw new YEngineStateException("Unable to accept request as engine" +
                " not in correct state: Current state = " + getEngineStatus());

    }

    /**
     * @deprecated use cancelCase(YIdentifier, String)
     * @param id
     * @throws YPersistenceException
     * @throws YEngineStateException
     */
    public synchronized void cancelCase(YIdentifier id)
            throws YPersistenceException, YEngineStateException {
        cancelCase(id, null);
    }

    public String launchCase(YSpecificationID specID, String caseParams,
                             URI completionObserver, YLogDataItemList logData)
            throws YStateException, YDataStateException, YSchemaBuildingException,
            YPersistenceException, YEngineStateException, YQueryException {
        return launchCase(specID, caseParams, completionObserver, null, logData, null);
    }

    public String launchCase(YSpecificationID specID, String caseParams,
                             URI completionObserver, YLogDataItemList logData,
                             String serviceHandle)
            throws YStateException, YDataStateException, YSchemaBuildingException,
            YPersistenceException, YEngineStateException, YQueryException {
        return launchCase(specID, caseParams, completionObserver, null, logData, serviceHandle);
    }


    public synchronized String launchCase(YSpecificationID specID, String caseParams,
                             URI completionObserver, String caseID,
                             YLogDataItemList logData, String serviceHandle)
                                 throws YStateException, YDataStateException, YEngineStateException,
                                        YSchemaBuildingException, YQueryException,
                                 YPersistenceException {
        _logger.debug("--> launchCase");

        if (getEngineStatus() != ENGINE_STATUS_RUNNING) {
            throw new YEngineStateException(
               "Unable to accept request as engine not in correct state: Current state = "
               + getEngineStatus());
        }

        YPersistenceManager pmgr = getPersistenceSession();
		    YIdentifier yCaseID;
        String caseIDString = null;

        try {

            // ensure that the caseid passed (if any) is not already in use
            if ((caseID != null) && (getCaseID(caseID) != null)) {
                throw new YStateException("CaseID '" + caseID + "' is already active.");
            }

            yCaseID = startCase(pmgr, specID, caseParams, completionObserver, caseID,
                                logData, serviceHandle);

            if (yCaseID != null)
                caseIDString = yCaseID.toString();
            else {
                if (pmgr != null)  pmgr.rollbackTransaction();
                throw new YStateException("No specification found for [" + specID + "].");
            }

            if (pmgr != null) pmgr.commit();
        }
        catch (YAWLException ye) {
            _logger.error("Failure returned from startCase - Rolling back Hibernate TXN", ye);
            if (pmgr != null) pmgr.rollbackTransaction();
            ye.rethrow();
        }
        _logger.debug("<-- launchCase");
        return caseIDString;
    }




    public synchronized YIdentifier getCaseID(String caseIDStr) {
        YIdentifier caseID = null;
        _logger.debug("--> getCaseID");
        for (YIdentifier identifier : _caseIDToNetRunnerMap.keySet()) {
            if (identifier.toString().equals(caseIDStr)) {
                caseID = identifier;
                break;
            }
        }
        return caseID;
    }


    public synchronized String getStateTextForCase(YIdentifier caseID) {
        _logger.debug("--> getStateTextForCase: ID=" + caseID.get_idString());

        Set<YIdentifier> allChildren = caseID.getDescendants();
        Set<YNetElement> allLocations = new HashSet<YNetElement>();
        for (YIdentifier identifier : allChildren) {
            allLocations.addAll(identifier.getLocations());
        }
        StringBuilder stateText = new StringBuilder();
        stateText.append("#######################################" +
                "######################\r\n" + "CaseID: ")
                .append(caseID)
                .append("\r\n" + "Spec:   ")
                .append(_runningCaseIDToSpecMap.get(caseID))
                .append("\r\n" + "###############################" +
                        "##############################\r\n");
        for (YNetElement element : allLocations) {
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
            }
            else if (element instanceof YTask) {
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
                        for (YIdentifier identifier : internalCondition.getIdentifiers()) {
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


    public synchronized String getStateForCase(YIdentifier caseID) {
        Set<YNetElement> allLocations = new HashSet<YNetElement>();
        for (YIdentifier identifier : caseID.getDescendants()) {
            allLocations.addAll(identifier.getLocations());
        }
        YSpecification spec = _runningCaseIDToSpecMap.get(caseID);
        StringBuilder stateText = new StringBuilder();
        stateText.append(String.format("<caseState caseID=\"%s\" specID=\"%s\">",
                caseID, spec.getSpecificationID().toString()));
        for (YNetElement element : allLocations) {
            if (element instanceof YCondition) {
                YCondition condition = (YCondition) element;   
                stateText.append(
                        String.format("<condition id=\"%s\" name=\"%s\" documentation=\"%s\">",
                                condition.toString(), condition.getName(),
                                condition.getDocumentation()));
                for (YIdentifier identifier : condition.getIdentifiers()) {
                    stateText.append(StringUtil.wrap(identifier.toString(), "identifier"));
                }
                stateText.append("<flowsInto>");
                for (YFlow flow : condition.getPostsetFlows()) {
                    String doc = (flow.getDocumentation() != null) ?
                            flow.getDocumentation() : "";
                    stateText.append(
                            String.format("<nextElementRef id=\"%s\" documentation=\"%s\">",
                                    flow.getNextElement().getID(), doc));
                }
                stateText.append("</flowsInto></condition>");
            }
            else if (element instanceof YTask) {
                YTask task = (YTask) element;
                stateText.append(String.format("<task id=\"%s\" name=\"%s\">",
                        task.toString(), task.getDecompositionPrototype().getID()));
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
                        stateText.append("<internalCondition id=\"")
                                .append(internalCondition.toString())
                                .append("\">");
                        for (YIdentifier identifier : internalCondition.getIdentifiers()) {
                            stateText.append(StringUtil.wrap(identifier.toString(),
                                    "identifier"));
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


    public synchronized String getCaseData(String caseID) throws YStateException {

        // if this is for a sub-net, act accordingly
        if (caseID.indexOf(".") > -1) return getNetData(caseID) ;

        YIdentifier id = getCaseID(caseID);
        if (id == null) {
            throw new YStateException("Received invalid case id '" + caseID + "'.");
        }

        YNetRunner runner = _caseIDToNetRunnerMap.get(id);
        return runner.getCasedata().getData();
    }

    /**
     * Returns a list of the YIdentifiers objects for running cases.
     * @return List of running cases
	   */
    public List<YIdentifier> getRunningCaseIDs() {
        return new ArrayList<YIdentifier>(_runningCaseIDToSpecMap.keySet());
    }


    /**
     * @return the next available case number.
     */
    public String getNextCaseNbr(YPersistenceManager pmgr) {
        return _caseNbrStore.getNextCaseNbr(pmgr);
    }


    /**
     * AJH: Public method which returns the next available caseID
     * Note: This is only available with a non-persisting engine and is used
     * to ascertain the case ID prior to launching a case (eg. for an XForms
     * execution framework).
     * @return  A unique case ID
     * @throws YPersistenceException if there's a problem persisting the change
     */
    public synchronized String allocateCaseID() throws YPersistenceException {
        if (isPersisting()) {
            throw new YPersistenceException(
                    "Pre-allocated CaseIDs are not available in a persisting engine instance");
        }
        return YCaseNbrStore.getInstance().getNextCaseNbr(null);
    }

    /**
     * Suspends the execution of a case.
     * @param caseID the id of the case to suspend
     * @throws YPersistenceException if there's a problem persisting the change
     * @throws YStateException if case cannot be suspended given the current engine
     */
    public synchronized void suspendCase(YIdentifier caseID)
            throws YPersistenceException, YStateException {
        YPersistenceManager pmgr = getPersistenceSession();
        try {
            suspendCase(pmgr, caseID);
            if (pmgr != null) pmgr.commit();
        }
        catch (Exception e) {
            _logger.error("Failure to suspend case " + caseID, e);
            if (pmgr != null) pmgr.rollbackTransaction();
            throw new YStateException("Could not suspend case (See log for details)");
        }
    }


    /**
     * Suspends the execution of a case.
     * @param pmgr the persistence manager object
     * @param id the case id to clear
     * @throws YPersistenceException if there's a problem clearing the case
     * @throws YStateException if case cannot be suspended given the current engine
     * operating state
     */
    private void suspendCase(YPersistenceManager pmgr, YIdentifier id)
            throws YPersistenceException, YStateException {

        debug("--> suspendCase: CaseID = ", id.toString());

        // Reject call if this case not currently in a normal state
        YNetRunner topLevelNet = _caseIDToNetRunnerMap.get(id);
        if (topLevelNet.getCasedata().getExecutionState() != YCaseData.SUSPEND_STATUS_NORMAL) {
            throw new YStateException("Case " + topLevelNet.getCaseID() +
                    " cannot be suspended as currently not executing normally (SuspendStatus="
                    + topLevelNet.getCasedata().getExecutionState() + ")");
        }
        else {
            // Go thru all runners and set status to suspending
            for (YNetRunner runner : getRunnersForPrimaryCase(id)) {
                debug("Current status of runner ", runner.get_caseID(),
                        " = " + runner.getCasedata().getExecutionState());
                runner.getCasedata().setExecutionState(YCaseData.SUSPEND_STATUS_SUSPENDING);
                _announcer.notifyCaseSuspending(id);
                if (pmgr != null) {
                    pmgr.updateObject(runner.getCasedata());
                }
            }
            _logger.info("Case " + topLevelNet.getCaseID() + " is attempting to suspend");

            // See if we can progress this case into a fully suspended state.
            progressCaseSuspension(pmgr, id);
        }
        debug("<-- suspendCase");
    }


    /**
     * Resumes execution of a case.
     * @param id the id of the case to resume
     * @throws YPersistenceException if there's a problem persisting the resumed case
     * @throws YStateException if case cannot be resumed
     */
    public synchronized void resumeCase(YIdentifier id) throws YPersistenceException, YStateException {
        YPersistenceManager pmgr = getPersistenceSession();
        try {
            resumeCase(pmgr, id);
            if (pmgr != null) pmgr.commit();
        }
        catch (Exception e) {
            _logger.error("Failure to resume case " + id, e);
            if (pmgr != null) pmgr.rollbackTransaction();
            throw new YStateException("Could not resume case (See log for details)");
        }
    }


    /**
     * Resumes execution of a case.
     * @param pmgr the persistence manager object
     * @param id the id of the case to resume
     * @throws YPersistenceException if there's a problem persisting the resumed case
     * @throws YStateException if case cannot be resumed
     * @throws YSchemaBuildingException
     * @throws YDataStateException
     * @throws YQueryException
     */
    private void resumeCase(YPersistenceManager pmgr, YIdentifier id)
            throws YPersistenceException, YStateException, YSchemaBuildingException,
                   YDataStateException, YQueryException {
        debug("--> resumeCase: CaseID = ", id.toString());

        // reject call if this case not currently suspended or suspending
        YCaseData caseData = _caseIDToNetRunnerMap.get(id).getCasedata();
        if (caseData.isInSuspense()) {
           for (YNetRunner runner : getRunnersForPrimaryCase(id)) {

               debug("Current status of runner ", runner.get_caseID(),
                       " = " + runner.getCasedata().getExecutionState());

               runner.getCasedata().setExecutionState(YCaseData.SUSPEND_STATUS_NORMAL);
               if (pmgr != null) {
                   pmgr.updateObject(runner.getCasedata());
               }

               runner.kick(pmgr);

               // Update persistence only if this runner has not completed. If it has
               // completed (as in the case where we resume a case and the last
               // workitem has previously been completed), the above call to 'kick'
               // will have progressed the net to its end point, so the persistence
               // object will have been deleted.
               if ((pmgr != null) && (! runner.isCompleted())) {
                       pmgr.updateObject(runner);
               }
           }

            announceEvents(id);
           _logger.info("Case " + id + " has resumed execution");
           _announcer.notifyCaseResumption(id);
       }
       else {
           throw new YStateException("Case " + id +
                   " cannot be suspended as currently not executing normally (SuspendStatus="
                   + caseData.getExecutionState() + ")");
       }
       debug("<-- resumeCase");
   }

    /**
     * Attempts to progress the suspension status of a case.
     *
     * Where a Case is "suspending", we scan the workitems associated with all the nets
     * associated with the case, and where no workitems are enabled, executing or fired,
     * we progress the suspension state from "suspending" to "suspended".
     * @param pmgr the persistence manager object
     * @param caseID the id of the case to progress
     * @throws YPersistenceException if there's a problem getting the case
     * @throws YStateException if case cannot be progressed
     */
    private void progressCaseSuspension(YPersistenceManager pmgr, YIdentifier caseID)
            throws YPersistenceException, YStateException {

        debug("--> progressCaseSuspension: CaseID=" + caseID);

        YIdentifier topNetID = caseID.getRootAncestor();
        if (getCaseExecutionStatus(caseID) != YCaseData.SUSPEND_STATUS_SUSPENDING) {
            throw new YStateException("Case " + caseID +
                      " cannot be suspended as case not currently attempting to suspend");
        }
        else {
            boolean executingTasks = false;
            Vector<YNetRunner> runners = getRunnersForPrimaryCase(topNetID);
            for (YNetRunner runner : runners) {

                // Go thru busy and executing tasks and see if we have any atomic tasks
                for (YTask task : runner.getActiveTasks()) {
                    if (task instanceof YAtomicTask) {
                        debug("One or more executing atomic tasks found for case - " +
                                " Cannot fully suspend at this time");
                        executingTasks = true;
                        break;
                    }
                }
            }

            // If no executing tasks found go thru nets and set state to suspended
            if (! executingTasks) {
                for (YNetRunner runner : runners) {
                    runner.getCasedata().setExecutionState(YCaseData.SUSPEND_STATUS_SUSPENDED);
                    if (pmgr != null) {
                        pmgr.updateObject(runner);
                    }
                }
                _logger.info("Case " + caseID +
                        " has suspended successfully. Announcing suspended.");
                _announcer.notifyCaseSuspended(topNetID);
            }
        }
        debug("<-- progressCaseSuspension");
    }


    /**
     * @param id the id of the case
     * @return the execution status of a case
     * @throws YPersistenceException if there's a problem getting the case
     */
    public int getCaseExecutionStatus(YIdentifier id) throws YPersistenceException {
        return _caseIDToNetRunnerMap.get(id).getCasedata().getExecutionState();
    }


    /**
     * @param id the id of the case
     * @return the case level data for the case
     */
    public YCaseData getCaseData(YIdentifier id) {
        YNetRunner runner = _caseIDToNetRunnerMap.get(id);
        return (runner != null) ? runner.getCasedata() : null;
    }

    /** updates the case data with the data passed after completion of an exception handler */
     public synchronized boolean updateCaseData(String idStr, String data) {
         YNetRunner runner = _caseIDToNetRunnerMap.get(getCaseID(idStr));
         if (runner != null) {
             try {
                 YPersistenceManager pmgr = getPersistenceSession() ;
                 YNet net = runner.getNet();
                 Element updatedVars = JDOMUtil.stringToElement(data);
                 for (Object o : updatedVars.getChildren()) {
                     Element eVar = (Element) o;
                     net.assignData(pmgr, (Element) eVar.clone());
                 }
                 if (pmgr != null) pmgr.commit();
                 return true;
             }
             catch (Exception e) {
                 _logger.error("Problem updating Case Data for case " + idStr, e);
             }
         }
         return false ;
     }


     /** @return the current case data for the case id passed */
     public Document getCaseDataDocument(String id) {
         YNetRunner runner = _caseIDToNetRunnerMap.get(getCaseID(id));
         if (runner != null)
             return runner.getNet().getInternalDataDocument() ;
         else
             return null;
     }


    // anoounces deferred events from all this case's net runners //
    private void announceEvents(YIdentifier caseID) {
        YIdentifier rootCaseID = caseID.getRootAncestor();
        for (YNetRunner runner : getRunnersForPrimaryCase(rootCaseID)) {
            runner.makeAnnouncements();
        }
    }


    /***************************************************************************/

    /**
     * Returns the task definition, not the task instance.
     *
     * @param specID the specification id
     * @param taskID the task id
     * @return the task definition object.
     */
    public synchronized YTask getTaskDefinition(YSpecificationID specID, String taskID) {
        YTask task = null;
        YSpecification spec = _specifications.getSpecification(specID);
        if (spec != null) {
            Set<YDecomposition> decompositions = spec.getDecompositions();
            for (YDecomposition decomposition : decompositions) {
                if (decomposition instanceof YNet) {
                    YNet net = (YNet) decomposition;
                    YExternalNetElement element = net.getNetElement(taskID);
                    if ((element != null) && (element instanceof YTask)) {
                        task = (YTask) element;
                        break;                                               // found it
                    }
                }
            }
        }
        return task;
    }

    public static YWorkItemRepository getWorkItemRepository() {
        return _workItemRepository;
    }

   
    public synchronized Set<YWorkItem> getAvailableWorkItems() {
            debug("--> getAvailableWorkItems: Enabled=" +
                        _workItemRepository.getEnabledWorkItems().size(),
                        " Fired=" + _workItemRepository.getFiredWorkItems().size());

            Set<YWorkItem> allItems = new HashSet<YWorkItem>();
            allItems.addAll(_workItemRepository.getEnabledWorkItems());
            allItems.addAll(_workItemRepository.getFiredWorkItems());

            debug("<-- getAvailableWorkItems");
            return allItems;
    }


    public synchronized YWorkItem getWorkItem(String workItemID) {
        return _workItemRepository.getWorkItem(workItemID);
    }


    public synchronized Set<YWorkItem> getAllWorkItems() {
        return _workItemRepository.getWorkItems();
    }


    public synchronized Set<YWorkItem> getWorkItemsWithIdentifier(String idType, String itemID) {
        return _workItemRepository.getWorkItemsWithIdentifier(idType, itemID);
    }


    /**
     * Starts a work item.  If the workitem param is enabled this method fires the task
     * and returns the first of its child instances in the exectuting state.
     * Else if the workitem is fired then it moves the state from fired to executing.
     * Either way the method returns the resultant work item.
     *
     * @param workItem the enabled, or fired workitem.
     * @param client the YAWL external client or service starting the workitem
     * @return the resultant work item in the executing state.
     * @throws YStateException     if the workitem is not in either of these
     *                             states.
     * @throws YDataStateException
     */
    public synchronized YWorkItem startWorkItem(YWorkItem workItem, YClient client)
            throws YStateException, YDataStateException, YQueryException,
                   YSchemaBuildingException, YPersistenceException, YEngineStateException {

        if (getEngineStatus() != ENGINE_STATUS_RUNNING)
            throw new YEngineStateException(
                    "Unable to accept request as engine not in correct state: Current state = "
                            + getEngineStatus());

        debug("--> startWorkItem");

        YPersistenceManager pmgr = getPersistenceSession();
        YWorkItem resultantItem = null;
        try {
            YNetRunner netRunner = null;

            if (workItem != null) {
                if (workItem.getStatus().equals(YWorkItemStatus.statusEnabled)) {
                    netRunner = _workItemRepository.getNetRunner(workItem.getCaseID());
                    List childCaseIDs = netRunner.attemptToFireAtomicTask(pmgr, workItem.getTaskID());
                    Element dataList;
                    YTask task = (YTask) netRunner.getNetElement(workItem.getTaskID());

                    if (childCaseIDs != null) {
                        for (int i = 0; i < childCaseIDs.size(); i++) {
                            YIdentifier childID = (YIdentifier) childCaseIDs.get(i);
                            YWorkItem nextWorkItem = workItem.createChild(pmgr, childID);
                            if (i == 0) {
                                netRunner.startWorkItemInTask(pmgr, nextWorkItem.getCaseID(),
                                        workItem.getTaskID());
                                nextWorkItem.setStatusToStarted(pmgr, client);
                                dataList = task.getData(childID);
                                nextWorkItem.setData(pmgr, dataList);
                                resultantItem = nextWorkItem;
                            }
                            else {
                                dataList = task.getData(childID);
                                nextWorkItem.setData(pmgr, dataList);
                            }
                            instanceCache.addParameters(nextWorkItem, task, dataList);
                        }
                    }
                }
                else if (workItem.getStatus().equals(YWorkItemStatus.statusFired)) {
                    netRunner = _workItemRepository.getNetRunner(workItem.getCaseID().getParent());
                    netRunner.startWorkItemInTask(pmgr, workItem.getCaseID(), workItem.getTaskID());
                    workItem.setStatusToStarted(pmgr, client);

                    // AJH: As the workitem's data is restored courtesy of Hibernate, why
                    // do we need to explicity restore it, get it wrong and subsequently
                    // set it to NULL? After further digging I suspect this id all down to
                    // implementing multi-atomics and getting it wrong.

                    Element dataList = ((YTask) netRunner.getNetElement(
                            workItem.getTaskID())).getData(workItem.getCaseID());
                    workItem.setData(pmgr, dataList);

                    resultantItem = workItem;
                }
                else if (workItem.getStatus().equals(YWorkItemStatus.statusDeadlocked)) {
                    resultantItem = workItem;
                }
                else {
                    // this work item is likely already executing.
                    if (pmgr != null) pmgr.rollbackTransaction();
                    throw new YStateException("Item (" + workItem.getIDString() + ") status (" +
                            workItem.getStatus() + ") does not permit starting.");
                }
            }
            else {
                if (pmgr != null) pmgr.rollbackTransaction();
                throw new YStateException("No such work item currently available.");
            }

            // COMMIT POINT
            if (pmgr != null) pmgr.commit();
            if (netRunner != null) announceEvents(netRunner.getCaseID());

            _logger.debug("<-- startWorkItem");
        }
        catch (YAWLException ye) {
            if (pmgr != null) pmgr.rollbackTransaction();
            ye.rethrow();
        }
        catch (Exception e) {
            if (pmgr != null) pmgr.rollbackTransaction();
            _logger.error("Failure starting workitem " +
                           workItem.getWorkItemID().toString(), e);
            throw new YStateException(e.getMessage());
        }
        return resultantItem;
    }



    /**
     * Completes the work item.
     *
     * @param workItem
     * @param data
     * @param logPredicate - a pre-parse of the completion log predicate for this item
     * @param force - true if this represents a 'forceComplete', false for normal
     *                completion
     * @throws YStateException
     */
    public synchronized void completeWorkItem(YWorkItem workItem, String data,
                                              String logPredicate, boolean force)
            throws YStateException, YDataStateException, YQueryException,
            YSchemaBuildingException, YPersistenceException, YEngineStateException{

        if (getEngineStatus() != ENGINE_STATUS_RUNNING) {
            throw new YEngineStateException(
                    "Unable to accept request as engine not in correct state: Current state = "
                            + getEngineStatus());
        }
        debug("--> completeWorkItem", "\nWorkItem = ",
                workItem.getWorkItemID().getUniqueID(), "\nXML = ", data);

        YPersistenceManager pmgr = getPersistenceSession();
        YNetRunner netRunner = null;
        try {
            if (force) data = mapOutputDataForSkippedWorkItem(workItem);

            if (workItem != null) {
                if (workItem.getStatus().equals(YWorkItemStatus.statusExecuting)) {
                    netRunner = _workItemRepository.getNetRunner(
                            workItem.getCaseID().getParent());

                    if (_announcer.hasInterfaceXListeners()) {
                        if (netRunner.isTimeServiceTask(workItem)) {
                            List timeOutSet = netRunner.getTimeOutTaskSet(workItem);
                            _announcer.announceTimeServiceExpiry(workItem, timeOutSet);
                        }
                    }
                    Document doc = JDOMUtil.stringToDocument(data);
                    workItem.setLogPredicateCompletion(logPredicate);
                    workItem.setStatusToComplete(pmgr, force);
                    workItem.completeData(pmgr, doc);

                    boolean taskExited = netRunner.completeWorkItemInTask(pmgr,
                            workItem, workItem.getCaseID(), workItem.getTaskID(), doc);
                    if (taskExited) {

                        /* Calling this to fix a problem.
                  * When a Task is enabled twice by virtue of having two enabling sets of
                  * tokens in the current marking the work items are not created twice.
                  * Instead an Enabled work item is created for one of the enabling sets.
                  * Once that task has well and truly finished it is then an appropriate
                  * time to notify the worklists that it is enabled again.
                  * This is done by calling continueIfPossible().*/
                        _logger.debug("Recalling continue (looping bugfix???)");
                        netRunner.continueIfPossible(pmgr);
                    }
                    instanceCache.closeWorkItem(workItem, doc);

                    // remove any active timer for this item
                    YTimer.getInstance().cancelTimerTask(workItem.getParent().getIDString());

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
                if (pmgr != null) pmgr.commit();
                if (netRunner != null) announceEvents(netRunner.getCaseID());
            }
            else {
                if (pmgr != null) pmgr.rollbackTransaction();
                throw new YStateException("WorkItem argument is equal to null.");
            }
        }
        catch (YAWLException ye) {
            if (pmgr != null) pmgr.rollbackTransaction();
            ye.rethrow();
        }
        catch (Exception e) {
            if (pmgr != null) pmgr.rollbackTransaction();

            if (e instanceof IllegalArgumentException) {
                e.printStackTrace();
                throw new YSchemaBuildingException(StringUtil.convertThrowableToString(e));
            } else {
                e.printStackTrace();
                throw new YSchemaBuildingException(StringUtil.convertThrowableToString(e));
            }
        }
        debug("<-- completeWorkItem");
    }


    public synchronized YWorkItem skipWorkItem(YWorkItem workItem, YClient client)
            throws YStateException, YDataStateException,
                   YQueryException, YSchemaBuildingException,
                   YPersistenceException, YEngineStateException {

        // start item, get output data, get children, complete each child
        YWorkItem startedItem = startWorkItem(workItem, client) ;
        if (startedItem != null) {
            String data = mapOutputDataForSkippedWorkItem(startedItem) ;
            Set<YWorkItem> children = workItem.getChildren() ;
            for (YWorkItem child : children)
                completeWorkItem(child, data, null, false) ;
        }
        else {
            throw new YStateException("Could not skip workitem: " + workItem.getIDString()) ;
        }
        return startedItem ;
    }


    private synchronized String mapOutputDataForSkippedWorkItem(YWorkItem workItem) {

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

        // for each output param:
        //   1. if matching input param, use its value
        //   2. else if default value specified, use its value
        //   3. else use default value for the param's data type
        for (String name : outputs.keySet()) {

            // if the output param has no corresponding input param, add an element
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
    public synchronized void checkElegibilityToAddInstances(String workItemID)
            throws YStateException {

        YWorkItem item = _workItemRepository.getWorkItem(workItemID);
        if (item != null) {
            if (item.getStatus().equals(YWorkItemStatus.statusExecuting)) {
                if (item.allowsDynamicCreation()) {
                    YIdentifier identifier = item.getCaseID().getParent();
                    YNetRunner netRunner = _workItemRepository.getNetRunner(identifier);
                    if (! netRunner.isAddEnabled(item.getTaskID(), item.getCaseID())) {
                        throw new YStateException("Adding instances is not possible in " +
                                "current state.");
                    }
                }
                else {
                    throw new YStateException("WorkItem[" + workItemID +
                            "] does not allow new instance creation.");
                }
            }
            else {
                throw new YStateException("WorkItem[" + workItemID +
                        "] is not in appropriate (executing) " +
                        "state for instance adding.");
            }
        }
        else {
            throw new YStateException("No work Item Found with id : " +
                    workItemID);
        }
    }


    /**
     * Checks whether new dynamic workitem instances can be started for a task
     * @pre the workitem is in executing state
     * @param workItemID the id of the workitem to check against
     * @return true if a new workitem can be dynamically spawned
     */
    public boolean canAddNewInstances(String workItemID) {
        YWorkItem item = _workItemRepository.getWorkItem(workItemID);
        if (item != null) {
            YIdentifier identifier = item.getCaseID().getParent();
            YNetRunner netRunner = _workItemRepository.getNetRunner(identifier);
            return netRunner.isAddEnabled(item.getTaskID(), item.getCaseID());
        }
        return false;
    }


    /**
     * Creates a new work item instance when possible.
     *
     * @param workItem the id of a work item inside the task to have a new instance.
     * @param paramValueForMICreation format "<data>[InputParam]*</data>
     *                                InputParam == <varName>varValue</varName>
     * @return the work item of the new instance.
     * @throws YStateException if the task is not able to create a new instance, due to
     *                         its state or its design.
     * @throws YPersistenceException if there's a problem with the persistence session
     */
    public synchronized YWorkItem createNewInstance(YWorkItem workItem,
                                                    String paramValueForMICreation)
            throws YStateException, YPersistenceException {

        if (workItem == null) throw new YStateException("No work item found.");

        // will throw a YStateException if not eligible
        checkElegibilityToAddInstances(workItem.getIDString());

        String taskID = workItem.getTaskID();
        YIdentifier siblingID = workItem.getCaseID();
        YNetRunner netRunner = _workItemRepository.getNetRunner(siblingID.getParent());
        YPersistenceManager pmgr = getPersistenceSession();

        try {
            Element paramValue = JDOMUtil.stringToElement(paramValueForMICreation);
            YIdentifier id = netRunner.addNewInstance(pmgr, taskID,
                    workItem.getCaseID(), paramValue);
            YWorkItem firedItem = workItem.getParent().createChild(pmgr, id);
            if (pmgr != null) pmgr.commit();
            return firedItem;                          //success!!!!
        }
        catch (Exception e) {
            if (pmgr != null) pmgr.rollbackTransaction();
            throw new YStateException(e.getMessage());
        }
    }


    public synchronized YWorkItem suspendWorkItem(String workItemID)
            throws YStateException, YPersistenceException {
        YPersistenceManager pmgr = getPersistenceSession();
        YWorkItem workItem = _workItemRepository.getWorkItem(workItemID);
        if ((workItem != null) && (workItem.hasLiveStatus())) {
            workItem.setStatusToSuspended(pmgr);
        }
        if (pmgr != null) {
            pmgr.commit();
            pmgr.closeSession();
        }
        return workItem ;
    }


    public synchronized YWorkItem unsuspendWorkItem(String workItemID)
            throws YStateException, YPersistenceException {
        YPersistenceManager pmgr = getPersistenceSession();
        YWorkItem workItem = _workItemRepository.getWorkItem(workItemID);
        if ((workItem != null) &&
            (workItem.getStatus().equals(YWorkItemStatus.statusSuspended)))
             workItem.setStatusToUnsuspended(pmgr);
        if (pmgr != null) {
            pmgr.commit();
            pmgr.closeSession();
        }
        return workItem ;
    }

    
    // rolls back a workitem from executing to fired
    public synchronized void rollbackWorkItem(String workItemID)
            throws YStateException, YPersistenceException {
        YPersistenceManager pmgr = getPersistenceSession();
        YWorkItem workItem = _workItemRepository.getWorkItem(workItemID);
        if (workItem != null) {
            if (workItem.getStatus().equals(YWorkItemStatus.statusExecuting)) {
                workItem.rollBackStatus(pmgr);
                YNetRunner netRunner =
                        _workItemRepository.getNetRunner(workItem.getCaseID().getParent());
                if (! netRunner.rollbackWorkItem(pmgr, workItem.getCaseID(), workItem.getTaskID())) {
                    if (pmgr != null) pmgr.rollbackTransaction();
                    throw new YStateException("Unable to rollback: work Item[" + workItemID +
                            "] is not in executing state.");
                }
                if (pmgr != null) pmgr.commit();
            }
        }
        else {
            if (pmgr != null) pmgr.rollbackTransaction();
            throw new YStateException("Work Item[" + workItemID + "] not found.");
        }
    }

    public synchronized Set getChildrenOfWorkItem(YWorkItem workItem) {
        return (workItem == null) ? null :
                _workItemRepository.getChildrenOf(workItem.getIDString());
    }

    /** updates the workitem with the data passed after completion of an exception handler */
    public synchronized boolean updateWorkItemData(String workItemID, String data) {
        YWorkItem workItem = getWorkItem(workItemID);
        if (workItem != null) {
            try {
                YPersistenceManager pmgr = getPersistenceSession() ;
                Element eleData = JDOMUtil.stringToElement(data);
                workItem.setData(pmgr, eleData);
                if (pmgr != null) pmgr.commit();
                instanceCache.updateWorkItemData(workItem, eleData);
                return true ;
            }
            catch (YPersistenceException e) {
                return false ;
            }
        }
        return false ;
    }


       /** cancels the workitem - marks final status as 'failed' if statusFail is true,
     *  or 'cancelled' if it is false */
       public synchronized void cancelWorkItem(YWorkItem workItem, boolean statusFail)  {
           try {
               if (workItem != null) {
                   if (workItem.getStatus().equals(YWorkItemStatus.statusExecuting)) {
                       YIdentifier caseID = workItem.getCaseID().getParent() ;
                       YNetRunner runner = _workItemRepository.getNetRunner(caseID);
                       String taskID = workItem.getTaskID();
                       YPersistenceManager pmgr = getPersistenceSession();
                       runner.cancelTask(null, taskID);
                       workItem.setStatusToDeleted(pmgr, statusFail);
                       if (workItem.hasTimerStarted()) {
                           YTimer.getInstance().cancelTimerTask(workItem.getIDString());
                       }
                       instanceCache.closeWorkItem(workItem, null);
                       runner.continueIfPossible(pmgr);
                       if (pmgr != null) pmgr.commit();
                       announceEvents(runner.getCaseID());
                   }
               }
           }
           catch (Exception e) {
               _logger.error("Failure whilst persisting workitem cancellation", e);
           }
       }

    /*********************************************************************/

    public synchronized YAWLServiceReference getRegisteredYawlService(String yawlServiceID) {
        return _yawlServices.get(yawlServiceID);
    }


    /**
     * Returns a set of YAWL services registered in the engine.
     *
     * @return Set of services
     */
    public synchronized Set<YAWLServiceReference> getYAWLServices() {
        return new HashSet<YAWLServiceReference>(_yawlServices.values());
    }


    /**
     * Adds a YAWL service to the engine.
     * @param yawlService
     */
    public synchronized void addYawlService(YAWLServiceReference yawlService)
            throws YPersistenceException {
        debug("--> addYawlService: Service=" + yawlService.getURI());

        _yawlServices.put(yawlService.getURI(), yawlService);

        if (!_restoring && isPersisting()) {
            _logger.info("Persisting YAWL Service " + yawlService.getURI() +
                    " with ID " + yawlService.getServiceID());
            storeObject(yawlService);
        }
        debug("<-- addYawlService");
    }

    /**
     * @param serviceURI
     * @return the removed service reference
     * @throws YPersistenceException
     */
    public synchronized YAWLServiceReference removeYawlService(String serviceURI)
            throws YPersistenceException {
        YAWLServiceReference service = _yawlServices.remove(serviceURI);
        if ((service != null) && isPersisting()) {
            _logger.info("Deleting persisted entry for YAWL service " +
                    service.getURI() + " with ID " + service.getServiceID());
            try {
                deleteObject(service);
            }
            catch (YPersistenceException e) {
                _logger.fatal("Failure whilst removing YAWL service", e);
                throw e;
            }
        }
        return service;
    }


    /**
     * Adds an external client credentials object to the engine. An external client is
     * an application that connects to the engine (as opposed to a service)
     * @param client the external client to add
     */
    public synchronized boolean addExternalClient(YExternalClient client)
            throws YPersistenceException {
        String userID = client.getUserName();
        if ((userID != null) && (client.getPassword() != null) &&
            (! _externalClients.containsKey(userID))) {

            _externalClients.put(userID, client);

            if (! _restoring && isPersisting()) {
                YPersistenceManager pmgr = getPersistenceSession();
                pmgr.storeObject(client);
                pmgr.commit();
            }
            return true;
        }
        else return false;
    }


    public synchronized boolean updateExternalClient(String id, String password, String doco)
            throws YPersistenceException {
        YExternalClient client = _externalClients.get(id);
        if (client != null) {
            client.setPassword(password);
            client.setDocumentation(doco);

            YPersistenceManager pmgr = getPersistenceSession();
            pmgr.updateObject(client);
            pmgr.commit();
        }
        return (client != null);
    }


    public synchronized YExternalClient getExternalClient(String name) {
        return _externalClients.get(name);
    }

    public synchronized Set<YExternalClient> getExternalClients() {
        return new HashSet<YExternalClient>(_externalClients.values());
    }


    public synchronized Set getUsers() {
        debug("--> getUsers");
        debug("<-- getUsers: Returned " + _externalClients.size() + " entries");
        return getExternalClients();
    }


    public synchronized YExternalClient removeExternalClient(String clientName)
            throws YPersistenceException {
        if (! clientName.equals("admin")) {
            YExternalClient client = _externalClients.remove(clientName);
            if (client != null) {
                _sessionCache.disconnect(client);         // if the client is connected
                if (isPersisting()) {
                    try {
                        deleteObject(client);
                    }
                    catch (YPersistenceException e) {
                        _logger.fatal("Failure whilst removing YAWL external client", e);
                        throw e;
                    }
                }
            }
            return client;
        }
        else {
            _logger.error("Removing the generic admin user is not allowed.");
            return null;
        }
    }


    /**
     * Sets the custom service that will serve as the default worklist. Called on
     * startup with values loaded from web.xml
     * @param paramStr the URL and password of the service (separated by a hash)
     * @throws RuntimeException if the parameters read from web.xml are incorrectly
     * formatted
     */
    public void setDefaultWorklist(String paramStr) {
        String[] parts = paramStr.split("#");
        if (parts.length != 2) {
            throw new RuntimeException("FATAL: Could not set default worklist from " +
                  "configuration file. No default worklist set. Cannot proceed.");
        }

        _defaultWorklist = new YAWLServiceReference(parts[0], null, "DefaultWorklist",
                                         PasswordEncryptor.encrypt(parts[1], null), "");
        _defaultWorklist.setAssignable(false);
        _yawlServices.put(_defaultWorklist.getURI(), _defaultWorklist);
    }


    public YAWLServiceReference getDefaultWorklist() {
        return _defaultWorklist;
    }


    public void setAllowAdminID(boolean allow) {
        _allowGenericAdminID = allow;
        try {
            if (allow) {

                // if its not yet there, add it
                if (! _externalClients.containsKey("admin")) {
                    addExternalClient(new YExternalClient("admin",
                        PasswordEncryptor.encrypt("YAWL", null), "generic admin user"));
                }
            }
            else {

                // if its already there, remove it
                YExternalClient admin = _externalClients.remove("admin");
                if (admin != null) {
                    deleteObject(admin);
                }
            }
        }
        catch (YPersistenceException e) {
            _logger.error("Failure whilst persisting 'admin' user", e);
        }
    }


    public boolean isGenericAdminAllowed() {
        return _allowGenericAdminID;
    }


    /**********************************************************************/

    /**
     * Indicates if persistence is enabled.
     * @return True=Persistent, False=Not Persistent
     */
    public static boolean isPersisting() {
        return _persisting;
    }

    /**
     * Indicates if persistence should be enabled.
     * @param persist true to persist, false to not persist
     */
    private static void setPersisting(boolean persist) {
        _persisting = persist;
    }


    public static SessionFactory getPMSessionFactory() {
        return _factory;
    }

    /**
     * Public interface to allow engine clients to ask the engine to store an object reference in its
     * persistent storage. It does this in its own transaction block.<P>
     *
     * @param obj
     * @throws YPersistenceException
     */
    public void storeObject(Object obj) throws YPersistenceException {
        synchronized (mutex) {
            doPersistAction(obj, YPersistenceManager.DB_INSERT);
        }
    }

    public void updateObject(Object obj) throws YPersistenceException {
        synchronized (mutex) {
            doPersistAction(obj, YPersistenceManager.DB_UPDATE);
        }
    }

    public void deleteObject(Object obj) throws YPersistenceException {
        synchronized (mutex) {
            doPersistAction(obj, YPersistenceManager.DB_DELETE);
        }
    }


    private void doPersistAction(Object obj, int action) throws YPersistenceException {
        synchronized (mutex) {
            if (isPersisting()) {
                YPersistenceManager pmgr = getPersistenceSession();
                if (pmgr != null) {
                    switch (action) {
                        case YPersistenceManager.DB_UPDATE : pmgr.updateObject(obj); break;
                        case YPersistenceManager.DB_DELETE : pmgr.deleteObject(obj); break;
                        case YPersistenceManager.DB_INSERT : pmgr.storeObject(obj); break;
                    }
                    pmgr.commit();
                }
            }
        }
    }

    /**
     * Clears a case from persistence
     * @param pmgr the persistence manager object
     * @param id the case id to clear
     * @throws YPersistenceException if there's a problem clearing the case
     */
    protected void clearCase(YPersistenceManager pmgr, YIdentifier id) throws YPersistenceException {
        debug("--> clearCase: CaseID = ", id.get_idString());

        if (_persisting) {
            clearCaseDelegate(pmgr, id);
        }
        debug("<-- clearCase");
    }

    /**
     * Removes the case from persistence
     * @param pmgr the persistence manager object
     * @param id the case id to clear
     * @throws YPersistenceException if there's a problem clearing the case
     */
    private synchronized void clearCaseDelegate(YPersistenceManager pmgr, YIdentifier id)
                                                        throws YPersistenceException {

        debug("--> clearCaseDelegate: CaseID = ", id.get_idString());

        if (_persisting) {
            try {
                List<YIdentifier> list = id.get_children();
                for (YIdentifier child : list) {
                    if (child != null) clearCaseDelegate(pmgr, child);
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

                // it's not a runner, so remove the YIdentifier only
                if (! runnerfound) {
                    Query quer = pmgr.getSession().createQuery(
                        "from org.yawlfoundation.yawl.elements.state.YIdentifier where _idString = '"
                                 + id.toString() + "'");
                    Iterator itx = quer.iterate();

                    // check the yid hasn't already been removed (can be one at most)
                    if (itx.hasNext())  {
                        pmgr.deleteObject(itx.next());
                    }
               }
            } catch (Exception e) {
                throw new YPersistenceException("Failure whilst clearing case", e);
            }
        }
        debug("<-- clearCaseDelegate");
    }


    protected YPersistenceManager getPersistenceSession() throws YPersistenceException {
        YPersistenceManager pmgr = null ;
        if (isPersisting()) {
            pmgr = new YPersistenceManager(getPMSessionFactory());
            pmgr.startTransactionalSession();
        }
        return pmgr ;
    }


        /**
     * Removes the workitems of the runner from persistence (after a case cancellation).
     *
     * @param pmgr - a (non-null) YPersistence object
     * @param caseID - the caseID for this case
     * @throws YPersistenceException if there's some persistence problem
     */
    private synchronized void clearWorkItemsFromPersistence(YPersistenceManager pmgr,
                                               YIdentifier caseID)
                                               throws YPersistenceException{

        List<YWorkItem> items = _workItemRepository.getWorkItemsForCase(caseID);

        // clear child items first (to avoid foreign key constraint exceptions)
        for (YWorkItem item : items) {
            if (! item.getStatus().equals(YWorkItemStatus.statusIsParent))
                pmgr.deleteObject(item);
        }

        // now clear any parents
        for (YWorkItem item : items) {
            if (item.getStatus().equals(YWorkItemStatus.statusIsParent))
                pmgr.deleteObject(item);
        }
    }


    public void writeAudit(YAuditEvent event) {
        try {
            storeObject(event);
        }
        catch (YPersistenceException ype) {
            Logger.getLogger(YEngine.class).warn("Unable to write audit event to log.");
        }
    }



    /** sets the URI passed as an listener for exception events */
    public boolean addInterfaceXListener(String observerURI) {
        _announcer.addInterfaceXListener(observerURI);
        return true;
    }

    
    /** removes an exception event listener */
    public boolean removeInterfaceXListener(String uri) {
        return _announcer.removeInterfaceXListener(uri);
    }


    /****************************************************************************/

    public synchronized void dump() {
        debug("*** DUMP OF ENGINE STARTS ***");

        Set<YSpecificationID> specids = _specifications.getSpecIDs();
        debug("\n*** DUMPING " + specids.size(), " SPECIFICATIONS ***");
        int i = 0;
        for(YSpecificationID specid : specids) {
            YSpecification spec = _specifications.getSpecification(specid);
            debug("Entry " + i++ + ":");
            debug("    ID             " + spec.getURI());
            debug("    Name           " + spec.getName());
            debug("    Version   " + spec.getMetaData().getVersion());
        }
        debug("*** DUMP OF SPECIFICATIONS ENDS ***");

        debug("*** DUMPING " + _caseIDToNetRunnerMap.size(),
                " ENTRIES IN CASE_ID_TO_NETRUNNER MAP ***");
        int sub = 0;
        for (YIdentifier key : _caseIDToNetRunnerMap.keySet()) {
            if (key != null) {
                YNetRunner runner = _caseIDToNetRunnerMap.get(key);
                debug("Entry " + sub++ + " Key=" + key.get_idString());
                debug(("    CaseID        " + runner.get_caseID()));
                debug("     YNetID        " + runner.getSpecificationID().getUri());
                runner.dump();
            }
            else debug("Key = NULL !!!");

        }
        debug("*** DUMP OF CASE_ID_TO_NETRUNNER_MAP ENDS");

        debug("*** DUMP OF RUNNING CASES TO SPEC MAP STARTS ***");
        sub = 0;
        for (YIdentifier key : _runningCaseIDToSpecMap.keySet()) {
            if (key != null) {
                YSpecification spec = _runningCaseIDToSpecMap.get(key);
                debug("Entry " + sub++ + " Key=" + key);
                debug("    ID             " + spec.getURI());
                debug("    Version        " + spec.getMetaData().getVersion());
            }
            else debug("key is NULL !!!");
        }
        debug("*** DUMP OF RUNNING CASES TO SPEC MAP ENDS ***");

        if (getWorkItemRepository() != null) {
            getWorkItemRepository().dump(_logger);
        }

        debug("*** DUMP OF ENGINE ENDS ***");
    }


    protected void debug(final String... phrases) {
        if (_logger.isDebugEnabled()) {
            if (phrases.length == 1) {
                _logger.debug(phrases[0]);
            }
            else {
                StringBuilder msg = new StringBuilder();
                for (String phrase : phrases) {
                    msg.append(phrase);
                }
                _logger.debug(msg.toString());
            }    
        }
    }

}
