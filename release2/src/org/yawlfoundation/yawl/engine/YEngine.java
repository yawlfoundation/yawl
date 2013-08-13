/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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
import org.jdom2.Document;
import org.jdom2.Element;
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
import org.yawlfoundation.yawl.engine.time.YTimedObject;
import org.yawlfoundation.yawl.engine.time.YTimer;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.logging.YEventLogger;
import org.yawlfoundation.yawl.logging.YLogDataItem;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.logging.YLogPredicate;
import org.yawlfoundation.yawl.logging.table.YAuditEvent;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.schema.YDataValidator;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.*;

import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    public static enum Status { Dormant, Initialising, Running, Terminating }

    // Workitem completion types
    public static enum WorkItemCompletion { Normal, Force, Fail }

    // Constants
    private static final YPersistenceManager _pmgr = new YPersistenceManager();
    private static final boolean ENGINE_PERSISTS_BY_DEFAULT = false;
    private static final String CURRENT_YAWL_VERSION = "3.0" ;

    private static YEngine _thisInstance;                         // reference to self
    private static YEventLogger _yawllog;
    private static YCaseNbrStore _caseNbrStore;
    private static Logger _logger;
    private static Set<YTimedObject> _expiredTimers;
    private static boolean _generateUIMetaData = true;           // extended attributes
    private static boolean _persisting;
    private static boolean _restoring;


    // NON-STATIC MEMBERS //

    private YWorkItemRepository _workItemRepository;
    protected YNetRunnerRepository _netRunnerRepository;
    private Map<YIdentifier, YSpecification> _runningCaseIDToSpecMap;
    private Map<String, YAWLServiceReference> _yawlServices;
    private Map<String, YExternalClient> _externalClients;
    private YSpecificationTable _specifications;
    private InterfaceAManagementObserver _interfaceAClient;
    private InterfaceBClientObserver _interfaceBClient;
    private Status _engineStatus;
    private YSessionCache _sessionCache;
    private YAnnouncer _announcer;                        // handles all i'face notifys
    private YAWLServiceReference _defaultWorklist;
    private InstanceCache _instanceCache;
    private YBuildProperties _buildProps;
    private String _engineClassesRootFilePath;
    private boolean _allowGenericAdminID;

    /********************************************************************************/

    /**
     * The Constructor - called from getInstance().
     */
    private YEngine() {
        _engineStatus = Status.Initialising;

        // initialise global objects
        _sessionCache = new YSessionCache();
        _workItemRepository = new YWorkItemRepository();
        _caseNbrStore = YCaseNbrStore.getInstance();
        _announcer = new YAnnouncer(this);         // the 'pusher' of interface events
        _specifications = new YSpecificationTable();
        _instanceCache = new InstanceCache();
        _logger = Logger.getLogger(YEngine.class);
        _netRunnerRepository = new YNetRunnerRepository();
        _runningCaseIDToSpecMap = new ConcurrentHashMap<YIdentifier, YSpecification>();
        _yawlServices = new ConcurrentHashMap<String, YAWLServiceReference>();
        _externalClients = new ConcurrentHashMap<String, YExternalClient>();
    }


    /**
     * Initialises the engine (if not already initialised) & returns the engine instance.
     * @param persisting true if engine state is to be persisted
     * @return a reference to the initialised engine
     * @throws YPersistenceException if there's a problem restoring from persistence
     */
    public static YEngine getInstance(boolean persisting, boolean gatherHbnStats)
            throws YPersistenceException {
        if (_thisInstance == null) {
            _thisInstance = new YEngine();
            _logger.debug("--> YEngine: Creating initial instance");

            // Initialise the persistence layer & restore state
            _persisting = persisting;
            if (_persisting) {
                _pmgr.initialise(true);
                _pmgr.setStatisticsEnabled(gatherHbnStats);
                _caseNbrStore.setPersisting(true);
                _thisInstance.restore();
            }
            else {
                _pmgr.setEnabled(false);

                // Default clients and services should always be available
                _thisInstance.loadDefaultClients();            
            }

            // init the process logger
            _yawllog = YEventLogger.getInstance(_thisInstance);

            // Init completed - set engine status to up and running
            _logger.info("Marking engine status = RUNNING");
            _thisInstance.setEngineStatus(Status.Running);
        }
        return _thisInstance;
    }


    public static YEngine getInstance(boolean persisting) throws YPersistenceException {
        return getInstance(persisting, false);
    }


    /**
     * Initialises the engine (if not already initialised) & returns the engine instance,
     * using the default persistence flag.
     * @return a reference to the initialised engine
     */
    public static YEngine getInstance() {
        try {
            return getInstance(ENGINE_PERSISTS_BY_DEFAULT);
        }
        catch (Exception e) {
            throw new RuntimeException("Failure to instantiate the engine.");
        }
    }


    /**
     * Checks if the engine is currently running
     * @return true if running, false otherwise
     */
    public static boolean isRunning() {
        return (_thisInstance != null) &&
               (_thisInstance.getEngineStatus() == Status.Running) ;
    }


    /**
     * Restores persisted data when the engine restarts.
     * @throws YPersistenceException when there's a problem with the restore process
     */
    private void restore() throws YPersistenceException {
        _logger.debug("--> restore");
        _restoring = true;

        YEngineRestorer restorer = new YEngineRestorer(_thisInstance, _pmgr);
        try {
            _pmgr.setRestoring(true);
            startTransaction();

            // restore data objects from persistence
            restorer.restoreYAWLServices();
            restorer.restoreExternalClients();
            restorer.restoreSpecifications();
            _caseNbrStore = restorer.restoreNextAvailableCaseNumber();
            restorer.restoreProcessInstances();
            restorer.restoreWorkItems();
            _expiredTimers = restorer.restoreTimedObjects();
            restorer.restartRestoredProcessInstances();

            // complete transaction
            commitTransaction();
            _pmgr.setRestoring(false);

            _workItemRepository.cleanseRepository();          // synch with net runners          

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
            restorer.persistDefaultClients();       // delayed til restoring is complete
        }
    }


    /**
     * Loads the logon accounts for the standard client apps and services from a
     * properties file on startup when they have not previously been persisted (ie. on
     * first startup) or when persistence is disabled.
     * @return the set of default clients loaded.
     * @throws YPersistenceException A passthrough - since it is only called when
     * restoring or when persistence is disabled, this exception is never thrown.
     */
    protected Set<YClient> loadDefaultClients() throws YPersistenceException {
        YDefClientsLoader loader = new YDefClientsLoader();
        for (YExternalClient client : loader.getLoadedClients()) {
            addExternalClient(client);
        }
        for (YAWLServiceReference service : loader.getLoadedServices()) {
            addYawlService(service);
        }
        return loader.getAllLoaded();
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
    public String getYawlVersion() { return CURRENT_YAWL_VERSION; }


    public InstanceCache getInstanceCache() {
        return _instanceCache;
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


    public String getEngineClassesRootFilePath() { return _engineClassesRootFilePath; }


    public void setEngineClassesRootFilePath(String path) {
        String pkgPath = "WEB-INF/classes/org/yawlfoundation/yawl/";
        _engineClassesRootFilePath = path + pkgPath;
    }


    // called when servlet init() has completed
    public void initialised(int maxWaitSeconds) {
        _announcer.announceEngineInitialisationCompletion(getYAWLServices(), maxWaitSeconds);

        // Now that the engine's running, process any expired timers
        if (_expiredTimers != null) {
            for (YTimedObject timer : _expiredTimers)
                timer.handleTimerExpiry();
        }
    }


    public void shutdown() {
        _announcer.shutdownObserverGateways();
        _announcer.shutdownInterfaceXListeners();
        _sessionCache.shutdown();
        YTimer.getInstance().shutdown();              // stop timer threads
        YTimer.getInstance().cancel();                // stop the timer
        if (_pmgr != null) _pmgr.closeFactory();
    }


     public void initBuildProperties(InputStream stream) {
         _buildProps = new YBuildProperties();
         _buildProps.load(stream);
     }


     public YBuildProperties getBuildProperties() {
         return _buildProps;
     }


     public YSessionCache getSessionCache() { return _sessionCache; }


    public void checkEngineRunning() throws YEngineStateException {
        if (getEngineStatus() != Status.Running) {
            throw new YEngineStateException("Unable to accept request as engine" +
                    " not in running state: Current state = " + getEngineStatus().name());
        }
    }


   /*********************************************************************************/

   /* These two 'register' methods are called by the standalone gui */

   public void registerInterfaceAClient(InterfaceAManagementObserver observer) {
       _interfaceAClient = observer;
   }

   public void registerInterfaceBObserver(InterfaceBClientObserver observer) {
       _interfaceBClient = observer;
   }

    
    /*********************************************************************************/

   /**
    * Registers an InterfaceB Observer Gateway with the engine in order to receive callbacks.
    * @param gateway the gateway to register
    * @throws YAWLException if the observerGateway has a null scheme value.
    */
    public void registerInterfaceBObserverGateway(ObserverGateway gateway) throws YAWLException {
        _announcer.registerInterfaceBObserverGateway(gateway);
    }

    public YAnnouncer getAnnouncer() {
        return _announcer;
    }

    public void setEngineStatus(Status status) {
        _engineStatus = status;
    }

    public Status getEngineStatus() {
        return _engineStatus;
    }

    public AnnouncementContext getAnnouncementContext() {
        return _announcer.getAnnouncementContext();
    }

    public int reannounceEnabledWorkItems() throws YStateException {
        return _announcer.reannounceEnabledWorkItems();
    }

    public int reannounceExecutingWorkItems() throws YStateException {
        return _announcer.reannounceExecutingWorkItems();
    }

    public int reannounceFiredWorkItems() throws YStateException {
        return _announcer.reannounceFiredWorkItems();
    }

    public void reannounceWorkItem(YWorkItem workItem) throws YStateException {
        _announcer.reannounceWorkItem(workItem);
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
    public void addRunner(YNetRunner runner, YSpecification specification) {
        if (specification != null) {
            runner.setEngine(this);
            _netRunnerRepository.add(runner);
            _runningCaseIDToSpecMap.put(runner.getCaseID(), specification);
            _instanceCache.addCase(runner.getCaseID().toString(),
                                  specification.getSpecificationID(),
                                  runner.getNetData().getData(), null,
                                  runner.getStartTime());

            // announce the add to the standalone gui (if any)
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
    private List<YNetRunner> getRunnersForPrimaryCase(YIdentifier primaryCaseID) {
        return _netRunnerRepository.getAllRunnersForCase(primaryCaseID);
    }


    public YNetRunner getNetRunner(YWorkItem workItem) {
        return _netRunnerRepository.get(workItem);
    }


    public YNetRunner getNetRunner(YIdentifier identifier) {
        return _netRunnerRepository.get(identifier);
    }


    public YNetRunnerRepository getNetRunnerRepository() {
        return _netRunnerRepository;
    }


    public String getNetData(String caseID) throws YStateException {

        // if this is a root net case id, the net data is equivalent to the case data
        if (! caseID.contains(".")) return getCaseData(caseID);

        YNetRunner subNetRunner = _netRunnerRepository.get(caseID);
        if (subNetRunner != null) {
            return subNetRunner.getNetData().getData();
        }
        else throw new YStateException("Received invalid case id '" + caseID + "'.");
    }



    /**************************************************************************/

    /**
     * Adds the specification(s) (expressed as an xml string) to the engine
     * @param specStr an XML formatted specification
     * @param ignoreErrors ignore verification errors and load the spec anyway.
     * @param verificationHandler an in/out param passing any error messages.
     * @return the specification ids of the successfully loaded specs
     */
    public List<YSpecificationID> addSpecifications(String specStr,
                        boolean ignoreErrors, YVerificationHandler verificationHandler)
            throws YPersistenceException {

        _logger.debug("--> addSpecifications");

        List<YSpecificationID> result = new Vector<YSpecificationID>();
        List<YSpecification> newSpecifications;
        try {
            newSpecifications = YMarshal.unmarshalSpecifications(specStr);
        }
        catch (YSyntaxException e) {

            // catch the xml parser's exception, transform it into YAWL format
            // and abort the load
            for (String msg : e.getMessage().split("\n")) {
                verificationHandler.error(null, msg);
            }
            _logger.debug("<-- addSpecifications: syntax exceptions found");
            return result;
        }

        if (newSpecifications != null) {
            for (YSpecification specification : newSpecifications) {
                specification.verify(verificationHandler);

                //if the error messages are empty or contain only warnings
                if (ignoreErrors || ! verificationHandler.hasErrors()) {
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
                        String errDetail = specification.getSchemaVersion().isBetaVersion() ?
                            "URI: " + specification.getURI() : "UID: " + specification.getID();
                        errDetail += "- Version: " + specification.getSpecVersion();
                        verificationHandler.error(this,
                                "There is a specification with an identical id to ["
                                 + errDetail + "] already loaded into the engine.");
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
        return _specifications.loadSpecification(spec);
    }


    /**
     * Removes a previously loaded specification from the engine
     * @param specID the identifier of the specification to unload
     * @throws YStateException if the spec is still in use (with a live case)
     * @throws YPersistenceException if there's some persistence problem
     */
    public void unloadSpecification(YSpecificationID specID)
            throws YStateException, YPersistenceException {

        _logger.debug("--> unloadSpecification: URI=" + specID.toString());

        if (_specifications.contains(specID)) {
            YSpecification specToUnload = _specifications.getSpecification(specID);

            // Reject unload request if we have active cases using it
            if (_runningCaseIDToSpecMap.values().contains(specToUnload)) {
                throw new YStateException("Cannot unload specification '" + specID +
                            "' as one or more cases are currently active against it.");
            }

            _logger.info("Removing process specification " + specID);
            _specifications.unloadSpecification(specToUnload);
            _yawllog.removeSpecificationFromCache(specID);
            deleteObject(specToUnload);
        }
        else {
            // the spec's not in the engine
            throw new YStateException("Engine contains no such specification with id '"
                    + specID + "'.");
        }
        _logger.debug("<-- unloadSpecification");
    }



    /**
     * Provides the set of specification ids for specs loaded into the engine.  It returns
     * those that were loaded as well as those with running instances that are unloaded.
     *
     * @return  A set of specification ids
     */
    public Set<YSpecificationID> getLoadedSpecificationIDs() {
        return _specifications.getSpecIDs();
    }

    /**
     * Returns the latest loaded version of a specification identified by 'key'
     * @param key the spec's identifier (v2.0+) or uri (pre-v2.0)
     * @return the matching specification or null if no match found
     */
    public YSpecification getLatestSpecification(String key) {
        return _specifications.getLatestSpecification(key);
    }

    public YSpecification getSpecification(YSpecificationID specID) {
        return _specifications.getSpecification(specID);
    }

    public YSpecification getSpecificationForCase(YIdentifier caseID) {
        return _runningCaseIDToSpecMap.get(caseID);
    }


    public YSpecification getProcessDefinition(YSpecificationID specID) {
        return _specifications.getSpecification(specID);
    }


    public String getSpecificationDataSchema(YSpecificationID specID) {
        YSpecification spec = _specifications.getSpecification(specID);
        if (spec != null) {
            YDataValidator validator = spec.getDataValidator() ;
            if (validator != null) {
               return validator.getSchema();
            }
        }
        return null;
    }


    public String getLoadStatus(YSpecificationID specID) {
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
    public Set<YIdentifier> getCasesForSpecification(YSpecificationID specID) {
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
    public Map<YSpecificationID, List<YIdentifier>> getRunningCaseMap() {
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


    protected YIdentifier startCase(YSpecificationID specID, String caseParams,
                                    URI completionObserver, String caseID,
                                    YLogDataItemList logData, String serviceRef, boolean delayed)
            throws YStateException, YDataStateException, YQueryException, YPersistenceException {

        // get the latest loaded spec version
        YSpecification specification = _specifications.getSpecification(specID);
        if (specification != null) {

            // check & format case data params (if any)
            Element data = formatCaseParams(caseParams, specification);

            YNetRunner runner = new YNetRunner(_pmgr, specification.getRootNet(), data, caseID);
            _netRunnerRepository.add(runner);
            logCaseStarted(specID, runner, completionObserver, caseParams, logData,
                    serviceRef, delayed);

            // persist it
            if ((! _restoring) && (_pmgr != null)) {
                _pmgr.storeObject(runner);
            }

            runner.continueIfPossible(_pmgr);
            runner.start(_pmgr);

            YIdentifier runnerCaseID = runner.getCaseID();
            _runningCaseIDToSpecMap.put(runnerCaseID, specification);

            // announce the new case to the standalone gui (if any)
            if (_interfaceBClient != null) {
                _logger.debug("Asking client to add case " + runnerCaseID.toString());
                _interfaceBClient.addCase(specID, runnerCaseID.toString());
            }
            return runnerCaseID;
        }
        else  throw new YStateException("No specification found with ID [" + specID + "]");
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


    private void logCaseStarted(YSpecificationID specID, YNetRunner runner,
                                URI completionObserver, String caseParams,
                                YLogDataItemList logData, String serviceRef, boolean delayed) {
        YIdentifier caseID = runner.getCaseID();
        _announcer.announceCheckCaseConstraints(specID, caseID.toString(), caseParams, true); // ix
        _announcer.announceCaseStart(specID, caseID, serviceRef, delayed);          // ib
        if (completionObserver != null) {
            YAWLServiceReference observer =
                    getRegisteredYawlService(completionObserver.toString());
            if (observer != null) {
                runner.setObserver(observer);
            }
            else {
                _logger.warn("Completion observer [" + completionObserver +
                        "] is not a registered YAWL service.");
            }
        }

        // log case start event
        YLogPredicate logPredicate = runner.getNet().getLogPredicate();
        if (logPredicate != null) {
            String predicate = logPredicate.getParsedStartPredicate(runner.getNet());
            if (predicate != null) {
                logData.add(new YLogDataItem("Predicate", "OnLaunch", predicate, "string"));
            }
        }
        _yawllog.logCaseCreated(_pmgr, specID, caseID, logData, serviceRef);

        // cache instance
        _instanceCache.addCase(caseID.toString(), specID, caseParams,
                logData, runner.getStartTime());
    }

    /**
     * Finalises a case completion.
     * @param caseID the id of the completing case
     * @throws YPersistenceException if theres a persistence problem
     */
    protected void removeCaseFromCaches(YIdentifier caseID) {
        _logger.debug("--> removeCaseFromCaches: Case=" + caseID.get_idString());

        _netRunnerRepository.remove(caseID);
        _runningCaseIDToSpecMap.remove(caseID);
        _instanceCache.removeCase(caseID.toString());

        // announce the completion to the standalone gui (if any)        
        if (_interfaceBClient != null) _interfaceBClient.removeCase(caseID.toString());
        _logger.debug("<-- removeCaseFromCaches");
    }


    /**
     * Cancels a running case.
     * @param caseID the identifier of the cancelling case
     * @throws YPersistenceException if there's some persistence problem
     */
    public void cancelCase(YIdentifier caseID, String serviceHandle)
            throws YPersistenceException, YEngineStateException {
        _logger.debug("--> cancelCase");
        checkEngineRunning();
        if (caseID == null) {
            throw new IllegalArgumentException(
                    "Attempt to cancel a case using a null caseID");
        }

        _logger.info("Deleting persisted process instance: " + caseID);

        Set<YWorkItem> removedItems = _workItemRepository.removeWorkItemsForCase(caseID);
        YNetRunner runner = _netRunnerRepository.get(caseID);
        synchronized(_pmgr) {
            startTransaction();
            if (_persisting) clearWorkItemsFromPersistence(removedItems);
            YTimer.getInstance().cancelTimersForCase(caseID.toString());
            removeCaseFromCaches(caseID);
            if (runner != null) runner.cancel(_pmgr);
            clearCaseFromPersistence(caseID);
            _yawllog.logCaseCancelled(_pmgr, caseID, null, serviceHandle);
            for (YWorkItem item : removedItems) {
                _yawllog.logWorkItemEvent(_pmgr, item,
                        YWorkItemStatus.statusCancelledByCase, null);
            }
            commitTransaction();
            _announcer.announceCaseCancellation(caseID, getYAWLServices());
        }
    }
    

    /**
     * @deprecated use cancelCase(YIdentifier, String)
     * @param id
     * @throws YPersistenceException
     * @throws YEngineStateException
     */
    public void cancelCase(YIdentifier id) throws YPersistenceException, YEngineStateException {
        cancelCase(id, null);
    }


    public String launchCase(YSpecificationID specID, String caseParams,
                             URI completionObserver, YLogDataItemList logData)
            throws YStateException, YDataStateException,
            YPersistenceException, YEngineStateException, YQueryException {
        return launchCase(specID, caseParams, completionObserver, null, logData, null, false);
    }

    public String launchCase(YSpecificationID specID, String caseParams,
                             URI completionObserver, YLogDataItemList logData,
                             String serviceHandle)
            throws YStateException, YDataStateException,
            YPersistenceException, YEngineStateException, YQueryException {
        return launchCase(specID, caseParams, completionObserver, null, logData, serviceHandle, false);
    }


    public String launchCase(YSpecificationID specID, String caseParams,
                             URI completionObserver, String caseID,
                             YLogDataItemList logData, String serviceHandle, boolean delayed)
            throws YStateException, YDataStateException, YEngineStateException,
            YQueryException, YPersistenceException {
        _logger.debug("--> launchCase");

        // ensure that the caseid passed (if any) is not already in use
        if ((caseID != null) && (getCaseID(caseID) != null)) {
            throw new YStateException("CaseID '" + caseID + "' is already active.");
        }
        checkEngineRunning();

        synchronized(_pmgr) {
            startTransaction();
            try {
                YIdentifier yCaseID = startCase(specID, caseParams, completionObserver,
                        caseID, logData, serviceHandle, delayed);
                if (yCaseID != null) {
                    commitTransaction();
                    announceEvents(yCaseID);
                    return yCaseID.toString();
                }
                else throw new YStateException("Unable to start case.");
            }
            catch (YAWLException ye) {
                _logger.error("Failure returned from startCase - Rolling back Hibernate TXN", ye);
                rollbackTransaction();
                ye.rethrow();
            }
        }
        _logger.debug("<-- launchCase");
        return null;
    }


    public YIdentifier getCaseID(String caseIDStr) {
        _logger.debug("--> getCaseID");
        return _netRunnerRepository.getCaseIdentifier(caseIDStr);
    }


    private Set<YNetElement> getCaseLocations(YIdentifier caseID) {
        Set<YNetElement> allLocations = new HashSet<YNetElement>();
        for (YIdentifier identifier : caseID.getDescendants()) {
            allLocations.addAll(identifier.getLocations());
        }
        return allLocations;
    }


    public String getStateTextForCase(YIdentifier caseID) {
        _logger.debug("--> getStateTextForCase: ID=" + caseID.get_idString()); 
        String cr = System.getProperty("line.separator");
        String hashLine = StringUtil.repeat('#', 60);
        StringBuilder stateText = new StringBuilder();
        stateText.append(hashLine).append(cr)
                 .append("CaseID: ").append(caseID).append(cr)
                 .append("Spec:   ").append(_runningCaseIDToSpecMap.get(caseID))
                 .append(cr).append(hashLine).append(cr);
        for (YNetElement element : getCaseLocations(caseID)) {
            stateText.append("CaseIDs in: ").append(element.toString()).append(cr);
            if (element instanceof YCondition) {
                stateText.append("\thashcode: ").append(element.hashCode()).append(cr);
                for (YIdentifier identifier : ((YConditionInterface) element).getIdentifiers()) {
                    stateText.append('\t').append(identifier.toString()).append(cr);
                }
            }
            else if (element instanceof YTask) {
                YTask task = (YTask) element;
                for (YInternalCondition internalCondition : task.getAllInternalConditions()) {
                    if (internalCondition.containsIdentifier()) {
                        stateText.append('\t').append(internalCondition.toString())
                                 .append(cr);
                        for (YIdentifier identifier : internalCondition.getIdentifiers()) {
                            stateText.append("\t\t").append(identifier.toString())
                                    .append(cr);
                        }
                    }
                }
            }
        }
        return stateText.toString();
    }
    
    
    public String getStateForCase(YIdentifier caseID) {
        YSpecification spec = _runningCaseIDToSpecMap.get(caseID);
        if (spec == null) {
            return "<caseState/>";
        }
        XNode stateNode = new XNode("caseState");
        stateNode.addAttribute("caseID", caseID);
        stateNode.addAttribute("specID", spec.getSpecificationID().toString());
        for (YNetElement element : getCaseLocations(caseID)) {
            if (element instanceof YCondition) {
                YCondition condition = (YCondition) element; 
                XNode conditionNode = stateNode.addChild("condition");
                conditionNode.addAttribute("id", condition.toString());
                conditionNode.addAttribute("name", condition.getName());
                conditionNode.addAttribute("documentation", condition.getDocumentation());
                for (YIdentifier identifier : condition.getIdentifiers()) {
                    conditionNode.addChild("identifier", identifier.toString());
                }
                XNode flowsIntoNode = conditionNode.addChild("flowsInto");
                for (YFlow flow : condition.getPostsetFlows()) {
                    String doc = (flow.getDocumentation() != null) ?
                            flow.getDocumentation() : "";
                    XNode nextRefNode = flowsIntoNode.addChild("nextElementRef");
                    nextRefNode.addAttribute("id", flow.getNextElement().getID());
                    nextRefNode.addAttribute("documentation", doc);
                }
            }
            else if (element instanceof YTask) {
                YTask task = (YTask) element;
                XNode taskNode = stateNode.addChild("task");
                taskNode.addAttribute("id", task.toString());
                taskNode.addAttribute("name", task.getDecompositionPrototype().getID());
                for (YInternalCondition internalCondition : task.getAllInternalConditions()) {
                    if (internalCondition.containsIdentifier()) {
                        taskNode.addChild(internalCondition.toXNode());
                    }
                }
            }
        }
        return stateNode.toString();
    }



    public String getCaseData(String caseID) throws YStateException {

        // if this is for a sub-net, act accordingly
        if (caseID.contains(".")) return getNetData(caseID) ;

        YIdentifier id = getCaseID(caseID);
        if (id != null) {
           YNetRunner runner = _netRunnerRepository.get(id);
           return runner.getNetData().getData();
        }
        throw new YStateException("Invalid case id '" + caseID + "'.");
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
    public String getNextCaseNbr() {
        return _caseNbrStore.getNextCaseNbr(_pmgr);
    }


    /**
     * AJH: Public method which returns the next available caseID
     * Note: This is only available with a non-persisting engine and is used
     * to ascertain the case ID prior to launching a case (eg. for an XForms
     * execution framework).
     * @return  A unique case ID
     * @throws YPersistenceException if there's a problem persisting the change
     */
    public String allocateCaseID() throws YPersistenceException {
        if (isPersisting()) {
            throw new YPersistenceException(
                    "Pre-allocated CaseIDs are not available in a persisting engine instance");
        }
        return YCaseNbrStore.getInstance().getNextCaseNbr(null);
    }

    /**
     * Suspends the execution of a case - currently only called from YAdminGUI.
     * @param caseID the id of the case to suspend
     * @throws YPersistenceException if there's a problem persisting the change
     * @throws YStateException if case cannot be suspended given the current engine
     */
    public void suspendCase(YIdentifier caseID)
            throws YPersistenceException, YStateException {
        synchronized(_pmgr) {
            startTransaction();
            try {
                suspendCase(_pmgr, caseID);
                commitTransaction();
            }
            catch (Exception e) {
                _logger.error("Failure to suspend case " + caseID, e);
                rollbackTransaction();
                throw new YStateException("Could not suspend case (See log for details)");
            }
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
        YNetRunner topLevelNet = _netRunnerRepository.get(id);
        if (! topLevelNet.hasNormalState()) {
            throw new YStateException("Case " + topLevelNet.getCaseID() +
                    " cannot be suspended as currently not executing normally (SuspendStatus="
                    + topLevelNet.getExecutionStatus() + ")");
        }
        else {
            // Go thru all runners and set status to suspending
            for (YNetRunner runner : getRunnersForPrimaryCase(id)) {
                debug("Current status of runner ", runner.get_caseID(),
                        " = ", runner.getExecutionStatus());
                runner.setStateSuspending();
                _announcer.announceCaseSuspending(id, getYAWLServices());
                if (pmgr != null) pmgr.updateObject(runner);
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
    public void resumeCase(YIdentifier id) throws YPersistenceException, YStateException {
        synchronized(_pmgr) {
            startTransaction();
            try {
                resumeCase(_pmgr, id);
                commitTransaction();
                announceEvents(id);
               _announcer.announceCaseResumption(id, getYAWLServices());
            }
            catch (Exception e) {
                _logger.error("Failure to resume case " + id, e);
                rollbackTransaction();
                throw new YStateException("Could not resume case (See log for details)");
            }
        }
    }


    /**
     * Resumes execution of a case.
     * @param pmgr the persistence manager object
     * @param id the id of the case to resume
     * @throws YPersistenceException if there's a problem persisting the resumed case
     * @throws YStateException if case cannot be resumed
     * @throws YDataStateException
     * @throws YQueryException
     */
    private void resumeCase(YPersistenceManager pmgr, YIdentifier id)
            throws YPersistenceException, YStateException,
                   YDataStateException, YQueryException {
        debug("--> resumeCase: CaseID = ", id.toString());

        // reject call if this case not currently suspended or suspending
        if (_netRunnerRepository.get(id).isInSuspense()) {
           for (YNetRunner runner : getRunnersForPrimaryCase(id)) {

               debug("Current status of runner ", runner.get_caseID(),
                       " = " + runner.getExecutionStatus());

               runner.setStateNormal();
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

           _logger.info("Case " + id + " has resumed execution");
       }
       else {
           throw new YStateException("Case " + id +
                   " cannot be suspended as currently not executing normally (SuspendStatus="
                   + _netRunnerRepository.get(id).getExecutionStatus() + ")");
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
        if (! _netRunnerRepository.get(caseID).isSuspending()) {
            throw new YStateException("Case " + caseID +
                    " cannot be suspended as case not currently attempting to suspend.");
        }

        YIdentifier topNetID = caseID.getRootAncestor();
        boolean executingTasks = false;
        List<YNetRunner> runners = getRunnersForPrimaryCase(topNetID);
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
                runner.setStateSuspended();
                if (pmgr != null) pmgr.updateObject(runner);
            }
            _logger.info("Case " + caseID +" has suspended successfully.");
            _announcer.announceCaseSuspended(topNetID, getYAWLServices());
        }
        debug("<-- progressCaseSuspension");
    }


    /**
     * @param id the id of the case
     * @return the case level data for the case
     */
    public YNetData getCaseData(YIdentifier id) {
        YNetRunner runner = _netRunnerRepository.get(id);
        return (runner != null) ? runner.getNetData() : null;
    }


    /** updates the case data with the data passed after completion of an exception handler */
    public boolean updateCaseData(String idStr, String data)
            throws YPersistenceException {
        YNetRunner runner = _netRunnerRepository.get(idStr);
        if (runner != null && data != null) {
            synchronized(_pmgr) {
                startTransaction();
                try {
                    YNet net = runner.getNet();
                    Element updatedVars = JDOMUtil.stringToElement(data);
                    for (Element eVar : updatedVars.getChildren()) {
                        net.assignData(_pmgr, eVar.clone());
                    }
                    commitTransaction();
                    return true;
                }
                catch (Exception e) {
                    rollbackTransaction();
                    _logger.error("Problem updating Case Data for case " + idStr, e);
                }
            }
        }
        return false ;
    }


     /** @return the current case data for the case id passed */
     public Document getCaseDataDocument(String id) {
         YNetRunner runner = _netRunnerRepository.get(id);
         return (runner != null) ? runner.getNet().getInternalDataDocument() : null;
     }


    // announces deferred events from all this case's net runners //
    private void announceEvents(YIdentifier caseID) {
        YIdentifier rootCaseID = caseID.getRootAncestor();
        for (YNetRunner runner : getRunnersForPrimaryCase(rootCaseID)) {
            _announcer.announceToGateways(runner.refreshAnnouncements());
        }
    }

    private void announceIfTimeServiceTimeout(YNetRunner netRunner, YWorkItem workItem) {
        if (_announcer.hasInterfaceXListeners()) {
            if (netRunner.isTimeServiceTask(workItem)) {
                List timeOutSet = netRunner.getTimeOutTaskSet(workItem);
                _announcer.announceTimeServiceExpiry(workItem, timeOutSet);
            }
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
    public YTask getTaskDefinition(YSpecificationID specID, String taskID) {
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

    public YWorkItemRepository getWorkItemRepository() {
        return _workItemRepository;
    }

   
    public Set<YWorkItem> getAvailableWorkItems() {
        Set<YWorkItem> allItems = new HashSet<YWorkItem>();
        Set<YWorkItem> enabledItems = _workItemRepository.getEnabledWorkItems();
        Set<YWorkItem> firedItems = _workItemRepository.getFiredWorkItems();

        if (_logger.isDebugEnabled()) {
            debug("--> getAvailableWorkItems: Enabled=" + enabledItems.size(),
                        " Fired=" + firedItems.size());
        }

        allItems.addAll(enabledItems);
        allItems.addAll(firedItems);

        debug("<-- getAvailableWorkItems");
        return allItems;
    }


    public YWorkItem getWorkItem(String workItemID) {
        return _workItemRepository.get(workItemID);
    }


    public Set<YWorkItem> getAllWorkItems() {
        return _workItemRepository.getWorkItems();
    }


    public YWorkItem startWorkItem(String itemID, YClient client)
            throws YStateException, YDataStateException, YQueryException,
                   YPersistenceException, YEngineStateException {
        YWorkItem item = getWorkItem(itemID);
        if (item != null) {
            return startWorkItem(item, client);
        }
        throw new YStateException("No work item found with id = " + itemID);
    }


    /**
     * Starts a work item.  If the workitem param is enabled this method fires the task
     * and returns the first of its child instances in the executing state.
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
    public YWorkItem startWorkItem(YWorkItem workItem, YClient client)
            throws YStateException, YDataStateException, YQueryException,
            YPersistenceException, YEngineStateException {

        debug("--> startWorkItem");
        checkEngineRunning();
        YWorkItem startedItem = null;

        synchronized(_pmgr) {
            startTransaction();
            try {
                YNetRunner netRunner = null;
                if (workItem != null) {
                    switch (workItem.getStatus()) {
                        case statusEnabled:
                            netRunner = getNetRunner(workItem.getCaseID());
                            startedItem = startEnabledWorkItem(netRunner, workItem, client);
                            break;

                        case statusFired:
                            netRunner = getNetRunner(workItem.getCaseID().getParent());
                            netRunner.startWorkItemInTask(_pmgr, workItem);
                            workItem.setStatusToStarted(_pmgr, client);
                            startedItem = workItem;
                            break;

                        case statusDeadlocked:
                            startedItem = workItem;
                            break;

                        default: // this work item is likely already executing.
                            rollbackTransaction();
                            throw new YStateException(String.format(
                                    "Item [%s]: status [%s] does not permit starting.",
                                     workItem.getIDString(), workItem.getStatus()));
                    }
                }
                else {
                    rollbackTransaction();
                    throw new YStateException("Cannot start null work item.");
                }

                // COMMIT POINT
                commitTransaction();
                if (netRunner != null) announceEvents(netRunner.getCaseID());

                _logger.debug("<-- startWorkItem");
            }
            catch (YAWLException ye) {
                rollbackTransaction();
                ye.rethrow();
            }
            catch (Exception e) {
                rollbackTransaction();
                _logger.error("Failure starting workitem " + workItem.getIDString(), e);
                throw new YStateException(e.getMessage());
            }
        }
        return startedItem;
    }


    private YWorkItem startEnabledWorkItem(YNetRunner netRunner, YWorkItem workItem, YClient client)
            throws YStateException, YDataStateException, YQueryException,
                   YPersistenceException, YEngineStateException {
        YWorkItem startedItem = null;
        YTask task = (YTask) netRunner.getNetElement(workItem.getTaskID());
        List<YIdentifier> childCaseIDs =
                netRunner.attemptToFireAtomicTask(_pmgr, workItem.getTaskID());

        if (childCaseIDs != null) {
            boolean oneStarted = false;
            for (YIdentifier childID : childCaseIDs) {
                YWorkItem childItem = workItem.createChild(_pmgr, childID);
                if (! oneStarted) {
                    netRunner.startWorkItemInTask(_pmgr, childItem);
                    childItem.setStatusToStarted(_pmgr, client);
                    startedItem = childItem;
                    oneStarted = true;
                }
                Element dataList = task.getData(childID);
                childItem.setData(_pmgr, dataList);
                _instanceCache.addParameters(childItem, task, dataList);
            }
        }
        return startedItem;
    }


    /**
     * Completes the work item.
     *
     * @param workItem
     * @param data
     * @param logPredicate - a pre-parse of the completion log predicate for this item
     * @param completionType - one of the completion types 'normal' (ordinary completion)
     * 'force' (forced completion) or 'fail' (forced fail) completion
     * @throws YStateException
     */
    public void completeWorkItem(YWorkItem workItem, String data, String logPredicate,
                                 WorkItemCompletion completionType)
            throws YStateException, YDataStateException, YQueryException,
            YPersistenceException, YEngineStateException{

        debug("--> completeWorkItem", "\nWorkItem = ",
                workItem != null ? workItem.get_thisID() : "null", "\nXML = ", data);
        checkEngineRunning();

        synchronized(_pmgr) {
            startTransaction();
            try {
                if (workItem != null) {
                    YNetRunner netRunner = getNetRunner(workItem.getCaseID().getParent());
                    if (workItem.getStatus().equals(YWorkItemStatus.statusExecuting)) {
                        completeExecutingWorkitem(workItem, netRunner, data,
                                logPredicate, completionType);
                    }
                    else if (workItem.getStatus().equals(YWorkItemStatus.statusDeadlocked)) {
                        _workItemRepository.removeWorkItemFamily(workItem);
                    }
                    else {
                        throw new YStateException("WorkItem with ID [" + workItem.getIDString() +
                                "] not in executing state.");
                    }

                    // COMMIT POINT
                    commitTransaction();
                    if (netRunner != null) announceEvents(netRunner.getCaseID());
                }
                else throw new YStateException("WorkItem argument is equal to null.");
            }
            catch (YAWLException ye) {
                rollbackTransaction();
                ye.rethrow();
            }
            catch (Exception e) {
                rollbackTransaction();
                _logger.error("Exception completing workitem", e);
            }
        }
        debug("<-- completeWorkItem");
    }


    private void completeExecutingWorkitem(YWorkItem workItem, YNetRunner netRunner,
                                           String data, String logPredicate,
                                           WorkItemCompletion completionType)
            throws YStateException, YDataStateException, YQueryException,
                   YPersistenceException, YEngineStateException {
        workItem.setExternalLogPredicate(logPredicate);
        if (completionType != WorkItemCompletion.Fail) {
            announceIfTimeServiceTimeout(netRunner, workItem);
            workItem.setStatusToComplete(_pmgr, completionType);
            Document doc = getDataDocForWorkItemCompletion(workItem, data, completionType);
            workItem.completeData(_pmgr, doc);
            if (netRunner.completeWorkItemInTask(_pmgr, workItem, doc)) {

                /* When a Task is enabled twice by virtue of having two enabling sets of
                 * tokens in the current marking the work items are not created twice.
                 * Instead an Enabled work item is created for one of the enabling sets.
                 * Once that task has well and truly finished it is then an appropriate
                 * time to notify the worklists that it is enabled again.*/
                netRunner.continueIfPossible(_pmgr);
            }
            cleanupCompletedWorkItem(workItem, netRunner, doc);
        }
        else cancelWorkItem(workItem);
    }


    public YWorkItem skipWorkItem(YWorkItem workItem, YClient client)
            throws YStateException, YDataStateException,
            YQueryException, YPersistenceException, YEngineStateException {

        // start item, get output data, get children, complete each child
        YWorkItem startedItem = startWorkItem(workItem, client) ;
        if (startedItem != null) {
            String data = mapOutputDataForSkippedWorkItem(startedItem, startedItem.getDataString()) ;
            Set<YWorkItem> children = workItem.getChildren() ;
            for (YWorkItem child : children)
                completeWorkItem(child, data, null, WorkItemCompletion.Normal) ;
        }
        else {
            throw new YStateException("Could not skip workitem: " + workItem.getIDString()) ;
        }
        return startedItem ;
    }


    private Document getDataDocForWorkItemCompletion(YWorkItem workItem, String data,
                                                     WorkItemCompletion completionType)
            throws YStateException {
        if (completionType != WorkItemCompletion.Normal) {
            data = mapOutputDataForSkippedWorkItem(workItem, data);
        }
        return JDOMUtil.stringToDocument(data);
    }


    private String mapOutputDataForSkippedWorkItem(YWorkItem workItem, String data)
            throws YStateException {

        // get input and output params for task
        YSpecificationID specID = workItem.getSpecificationID();
        String taskID = workItem.getTaskID();
        YTask task = getTaskDefinition(specID, taskID) ;

        Map<String, YParameter> inputs =
                                 task.getDecompositionPrototype().getInputParameters();
        Map<String, YParameter> outputs =
                                 task.getDecompositionPrototype().getOutputParameters();

        if (outputs.isEmpty()) return data;                   // no output data to map

        // map data values to params
        Element itemData = JDOMUtil.stringToElement(data);
        Element outputData = itemData.clone();

        // remove the input-only params from output data
        for (String name : inputs.keySet())
            if (outputs.get(name) == null) outputData.removeChild(name);

        // for each output param:
        //   1. if matching output Element, do nothing
        //   2. else if matching input param, use its value
        //   3. else if default value specified, use its value
        //   4. else use default value for the param's data type
        for (String name : outputs.keySet()) {

            if (outputData.getChild(name) != null) continue;   // matching element

            // if the output param has no corresponding input param, add an element
            if (inputs.get(name) == null) {
                Element outData = new Element(name) ;
                YParameter outParam = outputs.get(name);
                String defaultValue = outParam.getDefaultValue();
                if (defaultValue != null) {
                    String value = StringUtil.wrap(defaultValue, name);
                    outData = JDOMUtil.stringToElement(value);
                }
                else {
                    String typeName = outParam.getDataTypeName();
                    if (!XSDType.isBuiltInType(typeName)) {
                        throw new YStateException(String.format(
                                "Could not skip work item [%s]: Output-Only parameter [%s]" +
                                " requires a default value.", workItem.getIDString(), name));
                    }
                    outData.setText(JDOMUtil.getDefaultValueForType(outParam.getDataTypeName()));
                }
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
    public void checkElegibilityToAddInstances(String workItemID)
            throws YStateException {

        YWorkItem item = _workItemRepository.get(workItemID);
        if (item != null) {
            if (item.getStatus().equals(YWorkItemStatus.statusExecuting)) {
                if (item.allowsDynamicCreation()) {
                    YIdentifier identifier = item.getCaseID().getParent();
                    YNetRunner netRunner = getNetRunner(identifier);
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
        YWorkItem item = _workItemRepository.get(workItemID);
        if (item != null) {
            YIdentifier identifier = item.getCaseID().getParent();
            YNetRunner netRunner = getNetRunner(identifier);
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
    public YWorkItem createNewInstance(YWorkItem workItem,
                                       String paramValueForMICreation)
            throws YStateException, YPersistenceException {

        if (workItem == null) throw new YStateException("No work item found.");

        // will throw a YStateException if not eligible
        checkElegibilityToAddInstances(workItem.getIDString());

        String taskID = workItem.getTaskID();
        YIdentifier siblingID = workItem.getCaseID();
        YNetRunner netRunner = getNetRunner(siblingID.getParent());

        synchronized(_pmgr) {
            startTransaction();
            try {
                Element paramValue = JDOMUtil.stringToElement(paramValueForMICreation);
                YIdentifier id = netRunner.addNewInstance(_pmgr, taskID,
                        workItem.getCaseID(), paramValue);
                YWorkItem firedItem = workItem.getParent().createChild(_pmgr, id);
                commitTransaction();
                return firedItem;                          //success!!!!
            }
            catch (Exception e) {
                rollbackTransaction();
                throw new YStateException(e.getMessage());
            }
        }
    }


    public YWorkItem suspendWorkItem(String workItemID)
            throws YStateException, YPersistenceException {
        YWorkItem workItem = _workItemRepository.get(workItemID);
        if ((workItem != null) && (workItem.hasLiveStatus())) {
            synchronized(_pmgr) {
                startTransaction();
                workItem.setStatusToSuspended(_pmgr);
                commitTransaction();
            }
        }
        return workItem ;
    }


    public YWorkItem unsuspendWorkItem(String workItemID)
            throws YStateException, YPersistenceException {
        YWorkItem workItem = _workItemRepository.get(workItemID);
        if ((workItem != null) &&
                (workItem.getStatus().equals(YWorkItemStatus.statusSuspended))) {
            synchronized(_pmgr) {
                startTransaction();
                workItem.setStatusToUnsuspended(_pmgr);
                commitTransaction();
            }
        }
        return workItem ;
    }

    
    // rolls back a workitem from executing to fired
    public void rollbackWorkItem(String workItemID)
            throws YStateException, YPersistenceException {
        YWorkItem workItem = _workItemRepository.get(workItemID);
        if ((workItem != null) && workItem.getStatus().equals(YWorkItemStatus.statusExecuting)) {
            synchronized(_pmgr) {
                startTransaction();
                workItem.rollBackStatus(_pmgr);
                YNetRunner netRunner = getNetRunner(workItem.getCaseID().getParent());
                if (netRunner.rollbackWorkItem(_pmgr, workItem.getCaseID(), workItem.getTaskID())) {
                    commitTransaction();
                }
                else {
                    rollbackTransaction();
                    throw new YStateException("Unable to rollback: work Item[" + workItemID +
                            "] is not in executing state.");
                }
            }
        }
        else throw new YStateException("Work Item[" + workItemID + "] not found.");
    }


    public Set getChildrenOfWorkItem(YWorkItem workItem) {
        return (workItem == null) ? null :
                _workItemRepository.getChildrenOf(workItem.getIDString());
    }

    /** updates the workitem with the data passed after completion of an exception handler */
    public boolean updateWorkItemData(String workItemID, String data) {
        YWorkItem workItem = getWorkItem(workItemID);
        if (workItem != null) {
            synchronized(_pmgr) {
                try {
                    boolean localTransaction = startTransaction();
                    Element eleData = JDOMUtil.stringToElement(data);
                    workItem.setData(_pmgr, eleData);
                    if (localTransaction) commitTransaction();
                    _instanceCache.updateWorkItemData(workItem, eleData);
                    return true ;
                }
                catch (YPersistenceException e) {
                    return false ;
                }
            }
        }
        return false ;
    }


    public void cancelWorkItem(YWorkItem workItem)  {
        try {
            if ((workItem != null) && workItem.getStatus().equals(YWorkItemStatus.statusExecuting)) {
                YNetRunner runner = getNetRunner(workItem.getCaseID().getParent());
                synchronized(_pmgr) {
                    startTransaction();
                    workItem.setStatusToDeleted(_pmgr);
                    YWorkItem parent = workItem.getParent();
                    if ((parent != null) && (parent.getChildren().size() == 1)) {
                        runner.cancelTask(_pmgr, workItem.getTaskID());
                    }
                    else ((YAtomicTask) workItem.getTask()).cancel(_pmgr, workItem.getCaseID());

                    runner.kick(_pmgr);
                    cleanupCompletedWorkItem(workItem, runner, null);
                    commitTransaction();
                    announceEvents(runner.getCaseID());
                }
            }
        }
        catch (Exception e) {
            _logger.error("Failure whilst cancelling workitem", e);
        }
    }


    private void cleanupCompletedWorkItem(YWorkItem workItem, YNetRunner netRunner,
                                          Document data)
            throws YPersistenceException, YStateException {

        YWorkItem parent = workItem.getParent();

        // remove any active timer for this item
        if (workItem.hasTimerStarted()) {
            YTimer.getInstance().cancelTimerTask(workItem.getIDString());
        }

        _instanceCache.closeWorkItem(workItem, data);
        if (parent != null) {
            if (! parent.hasChildren() && parent.hasTimerStarted()) {
               YTimer.getInstance().cancelTimerTask(parent.getIDString());
            }

            // If case is suspending, see if we can progress into a fully suspended state
            if (netRunner.isSuspending()) {
                progressCaseSuspension(_pmgr, parent.getCaseID());
            }
        }
    }

    /*********************************************************************/

    public YAWLServiceReference getRegisteredYawlService(String yawlServiceID) {
        return _yawlServices.get(yawlServiceID);
    }


    /**
     * Returns a set of YAWL services registered in the engine.
     *
     * @return Set of services
     */
    public Set<YAWLServiceReference> getYAWLServices() {
        return new HashSet<YAWLServiceReference>(_yawlServices.values());
    }


    /**
     * Adds a YAWL service to the engine.
     * @param yawlService
     */
    public void addYawlService(YAWLServiceReference yawlService)
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
    public YAWLServiceReference removeYawlService(String serviceURI)
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
    public boolean addExternalClient(YExternalClient client)
            throws YPersistenceException {
        String userID = client.getUserName();
        if ((userID != null) && (client.getPassword() != null) &&
            (! _externalClients.containsKey(userID))) {

            _externalClients.put(userID, client);

            if (! _restoring && isPersisting()) {
                doPersistAction(client, YPersistenceManager.DB_INSERT);
            }
            return true;
        }
        else return false;
    }


    public boolean updateExternalClient(String id, String password, String doco)
            throws YPersistenceException {
        YExternalClient client = _externalClients.get(id);
        if (client != null) {
            client.setPassword(password);
            client.setDocumentation(doco);
            doPersistAction(client, YPersistenceManager.DB_UPDATE);
        }
        return (client != null);
    }


    public YExternalClient getExternalClient(String name) {
        return _externalClients.get(name);
    }

    public Set<YExternalClient> getExternalClients() {
        return new HashSet<YExternalClient>(_externalClients.values());
    }


    public Set getUsers() {
        debug("--> getUsers");
        debug("<-- getUsers: Returned " + _externalClients.size() + " entries");
        return getExternalClients();
    }


    public YExternalClient removeExternalClient(String clientName)
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


    /**
     * Public interface to allow engine clients to ask the engine to store an object reference in its
     * persistent storage. It does this in its own transaction block.<P>
     *
     * @param obj
     * @throws YPersistenceException
     */
    public void storeObject(Object obj) throws YPersistenceException {
        doPersistAction(obj, YPersistenceManager.DB_INSERT);
    }

    public void updateObject(Object obj) throws YPersistenceException {
        doPersistAction(obj, YPersistenceManager.DB_UPDATE);
    }

    public void deleteObject(Object obj) throws YPersistenceException {
        doPersistAction(obj, YPersistenceManager.DB_DELETE);
    }


    private void doPersistAction(Object obj, int action) throws YPersistenceException {
        if (isPersisting() && (_pmgr != null)) {
            synchronized(_pmgr) {
                boolean isLocalTransaction = startTransaction();
                switch (action) {
                    case YPersistenceManager.DB_UPDATE : _pmgr.updateObject(obj); break;
                    case YPersistenceManager.DB_DELETE : _pmgr.deleteObject(obj); break;
                    case YPersistenceManager.DB_INSERT : _pmgr.storeObject(obj); break;
                }
                if (isLocalTransaction) commitTransaction();
            }
        }
    }


    private boolean startTransaction() throws YPersistenceException {
        return (_pmgr != null) && _pmgr.startTransaction();
    }


    private void commitTransaction() throws YPersistenceException {
        if (_pmgr != null) _pmgr.commit();
    }


    private void rollbackTransaction() throws YPersistenceException {
        if (_pmgr != null) _pmgr.rollbackTransaction();
    }


    /**
     * Clears a case from persistence
     * @param id the case id to clear
     * @throws YPersistenceException if there's a problem clearing the case
     */
    protected void clearCaseFromPersistence(YIdentifier id) throws YPersistenceException {
        debug("--> clearCaseFromPersistence: CaseID = ", id.get_idString());
        if (_persisting) {
            try {
                List<YIdentifier> list = id.get_children();
                for (YIdentifier child : list) {
                    if (child != null) clearCaseFromPersistence(child);
                }

                synchronized(_pmgr) {
                    Object obj = _pmgr.getSession().get(YNetRunner.class, id.toString());
                    if (obj == null) {
                        obj = _pmgr.getSession().get(YIdentifier.class, id.toString());
                    }
                    if (obj != null) _pmgr.deleteObject(obj);
                }
            }
            catch (Exception e) {
                throw new YPersistenceException("Failure whilst clearing case", e);
            }
        }
        debug("<-- clearCaseFromPersistence");
    }


    public static YPersistenceManager getPersistenceManager() {
        return _pmgr;
    }


    /**
     * Removes the workitems of the runner from persistence (after a case cancellation).
     *
     * @param items the set of work items to delete from persistnece
     * @throws YPersistenceException if there's some persistence problem
     */
    private void clearWorkItemsFromPersistence(Set<YWorkItem> items)
            throws YPersistenceException {

        // clear child items first (to avoid foreign key constraint exceptions)
        for (YWorkItem item : items) {
            if (! item.getStatus().equals(YWorkItemStatus.statusIsParent))
                _pmgr.deleteObject(item);
        }

        // now clear any parents
        for (YWorkItem item : items) {
            if (item.getStatus().equals(YWorkItemStatus.statusIsParent))
                _pmgr.deleteObject(item);
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

    public void dump() {
        debug("*** DUMP OF ENGINE STARTS ***");

        Set<YSpecificationID> specids = _specifications.getSpecIDs();
        debug("\n*** DUMPING " + specids.size(), " SPECIFICATIONS ***");
        int i = 0;
        for(YSpecificationID specid : specids) {
            YSpecification spec = _specifications.getSpecification(specid);
            if (spec != null) {
                debug("Entry " + i++ + ":");
                debug("    ID             " + spec.getURI());
                debug("    Name           " + spec.getName());
                debug("    Version   " + spec.getMetaData().getVersion());
            }
        }
        debug("*** DUMP OF SPECIFICATIONS ENDS ***");
        _netRunnerRepository.dump(_logger);

        debug("*** DUMP OF RUNNING CASES TO SPEC MAP STARTS ***");
        int sub = 0;
        for (YIdentifier key : _runningCaseIDToSpecMap.keySet()) {
            if (key != null) {
                YSpecification spec = _runningCaseIDToSpecMap.get(key);
                if (spec != null) {
                    debug("Entry " + sub++ + " Key=" + key);
                    debug("    ID             " + spec.getURI());
                    debug("    Version        " + spec.getMetaData().getVersion());
                }
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


    public void setHibernateStatisticsEnabled(boolean enabled) {
        _pmgr.setStatisticsEnabled(enabled);
    }

    public boolean isHibernateStatisticsEnabled() {
        return _pmgr.isStatisticsEnabled();
    }

    public String getHibernateStatistics() {
        return _pmgr.getStatistics();
    }

    public void disableProcessLogging() {
        _yawllog.disable();
    }

}
