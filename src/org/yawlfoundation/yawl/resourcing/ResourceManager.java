/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.yawlfoundation.yawl.authentication.YExternalClient;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.TaskInformation;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.exceptions.YAWLException;
import org.yawlfoundation.yawl.exceptions.YAuthenticationException;
import org.yawlfoundation.yawl.logging.YLogDataItem;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.resourcing.allocators.RandomChoice;
import org.yawlfoundation.yawl.resourcing.calendar.CalendarException;
import org.yawlfoundation.yawl.resourcing.calendar.ResourceCalendar;
import org.yawlfoundation.yawl.resourcing.datastore.PersistedAutoTask;
import org.yawlfoundation.yawl.resourcing.datastore.WorkItemCache;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.LogMiner;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.ResourceEvent;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.DataSource;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.EmptyDataSource;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.ResourceDataSet;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.util.OrgDataRefresher;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;
import org.yawlfoundation.yawl.resourcing.interactions.AbstractInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.AllocateInteraction;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.FormParameter;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.SecondaryResources;
import org.yawlfoundation.yawl.resourcing.resource.UserPrivileges;
import org.yawlfoundation.yawl.resourcing.util.*;
import org.yawlfoundation.yawl.schema.YDataValidator;
import org.yawlfoundation.yawl.util.*;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.datatype.Duration;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;

/**
 * The ResourceManager singleton manages all aspects of the resource perspective,
 * including the loading & maintenance of the org model, and overseeing the distribution
 * of tasks to participants.
 *
 * @author Michael Adams
 * @date 03/08/2007
 */

public final class ResourceManager extends InterfaceBWebsideController {

    // a cache of misc. runtime items
    private RuntimeCache _cache = new RuntimeCache();

    // handles all of the interfacing between other services
    private InterfaceClients _services;

    // store of organisational resources and their attributes
    private ResourceDataSet _orgDataSet;

    // cache of 'live' workitems
    private WorkItemCache _workItemCache = WorkItemCache.getInstance();

    // String literals
    public static final String ADMIN_STR = "admin";
    private static final String FAIL_STR = "failure";
    private static final String INVALID_LOGON_ERR = "Invalid logon credentials";
    private static final String WORKITEM_ERR = "Unknown workitem";

    private static ResourceManager _me;                  // instance reference
    private ResourceAdministrator _resAdmin;             // admin capabilities
    private DataSource _orgdb;                            // the org model db i'face
    private Persister _persister;                         // persist changes to db
    private Logger _log;                                 // debug log4j file
    private ResourceCalendar _calendar;                   // resource availability
    private YBuildProperties _buildProps;                 // build version info
    private boolean _persisting;                         // flag to enable persistence
    private boolean _isNonDefaultOrgDB;                  // flag for non-yawl org model

    private final Object _autoTaskMutex = new Object();   // for executing autotasks
    private final Object _ibEventMutex = new Object();    // for synchronizing ib events
    private final Object _removalMutex = new Object();    // for removing participants

    private OrgDataRefresher _orgDataRefresher;        // if set, reloads db at intervals

    private boolean _serviceEnabled = true;          // will disable if no participants
    private boolean _initCompleted = false;               // guard for restarted engine
    private boolean _orgDataRefreshing = false;           // flag during auto-refresh
    public static boolean serviceInitialised = false;    // flag for init on restore

//    private ApplicationBean _jsfApplicationReference;   // ref to jsf app manager bean

    private boolean _blockIfSecondaryResourcesUnavailable = false;
    private boolean _persistPiling;
    private boolean _visualiserEnabled;
    private Dimension _visualiserDimension;

    // Mappings for specid -> version -> taskid <-> resourceMap
    private ResourceMapCache _resMapCache = new ResourceMapCache();

    // Constructor - called exclusively by getInstance()
    private ResourceManager() {
        super();
        _resAdmin = ResourceAdministrator.getInstance();
        _log = LogManager.getLogger(getClass());
        _me = this;
    }

    /**
     * @return the instantiated ResourceManager reference
     */
    public static ResourceManager getInstance() {
        if (_me == null) _me = new ResourceManager();
        return _me;
    }


    /**
     * *****************************************************************************
     */

    // Initialisation methods //

    public void initServices() {
        _services = new InterfaceClients(engineLogonName, engineLogonPassword);
    }

    public void initOrgDataSource(String dataSourceClassName, int refreshRate) {
        _log.info("Loading org data...");

        // get correct ref to org data backend
        _orgdb = PluginFactory.newDataSourceInstance(dataSourceClassName);

        if (_orgdb != null) {

            // set flag to true if the org model db backend is not the default
            _isNonDefaultOrgDB =
                    !(_orgdb.getClass().getSimpleName().equalsIgnoreCase("HibernateImpl"));

            // load all org data into the resources dataset
            loadResources();

            // set refresh rate if required
            if (refreshRate > 0) startOrgDataRefreshTimer(refreshRate);
        }
        else {
            _log.warn("Invalid Datasource: No dataset loaded. " +
                    "Check datasource settings in 'web.xml'");
            _orgDataSet = new EmptyDataSource().getDataSource();
        }
    }


    public InterfaceClients getClients() { return _services; }


    private String getEngineSessionHandle() {
        return _services.getSessionHandle();
    }


    public void initBuildProperties(InputStream stream) {
        _buildProps = new YBuildProperties();
        _buildProps.load(stream);
    }


    public YBuildProperties getBuildProperties() {
        return _buildProps;
    }


    public String getEngineBuildProperties() {
        return _services.getEngineBuildProperties();
    }


    public synchronized void finaliseInitialisation() {
        _workItemCache.setPersist(_persisting);
        if (_persisting) restoreWorkQueues();
        _calendar = ResourceCalendar.getInstance();

        // if tomcat is up, it means this was a 'hot' reload, so the final initialisation
        // things that need a running engine need to be re-run now. If tomcat is not yet
        // fully started, we'll wait until the engine is available
        if (HttpURLValidator.isTomcatRunning(_services.getIABackendURI())) {
            doFinalServiceToEngineInitialisation(true);
            _log.info("The Tomcat 'Invalid Command' warning above is a side-effect " +
                    "of the Resource Service being 'hot reloaded' and can be safely ignored.");
        }
    }


    public void setAllowExternalOrgDataMods(boolean allow) {
        if (_orgdb != null) _orgDataSet.setAllowExternalOrgDataMods(allow);
    }

    public void setExternalUserAuthentication(boolean externalAuth) {
        if (_orgdb != null) _orgDataSet.setExternalUserAuthentication(externalAuth);
    }


    public void setVisualiserDimension(String s) {
        if (s != null) {
            String[] parts = s.split(",");
            if (parts.length == 2) {
                try {
                    int width = Integer.parseInt(parts[0].trim());
                    int height = Integer.parseInt(parts[1].trim());
                    if ((width > 0) && (height > 0)) {
                        setVisualiserDimension(new Dimension(width, height));
                        return;
                    }
                } catch (NumberFormatException nfe) {
                    // nothing to do - will fall through to the following statements
                }
            }
            _log.warn("Invalid visualiser dimension parameter value - using default");
        }
        setVisualiserDimension((Dimension) null);               // null is a valid value
    }

    public void setVisualiserDimension(Dimension d) {
        _visualiserDimension = d;
    }

    public Dimension getVisualiserDimension() { return _visualiserDimension; }

    public void initRandomOrgDataGeneration(int count) {
        if (count > 0) {
            RandomOrgDataGenerator rodg = new RandomOrgDataGenerator();
            rodg.generate(count);
        }
    }

    public void setVisualiserEnabled(boolean enable) {
        _visualiserEnabled = enable;
    }

    public boolean isVisualiserEnabled() { return _visualiserEnabled; }

    public WorkItemCache getWorkItemCache() { return _workItemCache; }


//    public void registerJSFApplicationReference(ApplicationBean app) {
//        _jsfApplicationReference = app;
//    }

    public boolean hasOrgDataSource() {
        return (_orgdb != null);
    }

    public void setOrgDataRefreshing(boolean refreshing) {
        _orgDataRefreshing = refreshing;
    }

    public boolean isOrgDataRefreshing() {
        return _orgDataRefreshing;
    }


    public Logger getLogger() { return _log; }

    public ResourceCalendar getCalendar() { return _calendar; }


    public String registerCalendarStatusChangeListener(String uri, String handle) {
        return _services.registerCalendarStatusChangeListener(uri,
                getUserIDForSessionHandle(handle));
    }

    public void removeCalendarStatusChangeListener(String uri, String handle) {
        _services.removeCalendarStatusChangeListener(uri, getUserIDForSessionHandle(handle));
    }

    public void removeCalendarStatusChangeListeners(String handle) {
        _services.removeCalendarStatusChangeListeners(getUserIDForSessionHandle(handle));
    }

    public String addEventListener(String uri) {
        return _services.addEventListener(uri);
    }

    public boolean removeEventListener(String uri) {
        return _services.removeEventListener(uri);
    }


    /**
     * *****************************************************************************
     */

    // Interface B implemented methods //
    public void handleEnabledWorkItemEvent(WorkItemRecord wir) {
        synchronized (_ibEventMutex) {
            if (_serviceEnabled) {
                if (_workItemCache.contains(wir)) {
                    _log.warn("Duplicate post received for new work item [{}]" +
                            " - no further action required.", wir.getID());
                    return;
                }
                if (wir.isAutoTask()) {
                    handleAutoTask(wir, false);
                } else {

                    // pre 2.0 specs don't have any resourcing info
                    ResourceMap rMap = getResourceMap(wir);
                    wir = rMap != null ? rMap.distribute(wir) : offerToAll(wir);
                }
            }

            // service disabled, so route directly to admin's unoffered queue
            else _resAdmin.addToUnoffered(wir);

            // store all manually-resourced workitems in the local cache
            if (!wir.isAutoTask()) _workItemCache.add(wir);
        }
    }


    public void handleCancelledWorkItemEvent(WorkItemRecord wir) {
        synchronized (_ibEventMutex) {
            if (cleanupWorkItemReferences(wir)) {
                EventLogger.log(wir, null, EventLogger.event.cancel);
            }
        }
    }


    public void handleTimerExpiryEvent(WorkItemRecord wir) {
        if (wir.isAutoTask())
            handleAutoTask(wir, true);
        else {
            if (cleanupWorkItemReferences(wir)) {                // remove from worklists
                EventLogger.log(wir, null, EventLogger.event.timer_expired);
            }
        }
    }


    public void handleCancelledCaseEvent(String caseID) {
        if (_serviceEnabled) {
  //          synchronized (_ibEventMutex) {
                removeCaseFromAllQueues(caseID);                          // workqueues
                _cache.removeCaseFromTaskCompleters(caseID);
                _cache.cancelCodeletRunnersForCase(caseID);
                _cache.removeDeferredGroupForCase(caseID);
                freeSecondaryResourcesForCase(caseID);
                removeChain(caseID);
                removeActiveCalendarEntriesForCase(caseID);
                _services.removeCaseFromDocStore(caseID);
  //          }
        }
    }


    public void handleDeadlockedCaseEvent(String caseID, String tasks) {
        _log.error("Case {} has deadlocked at tasks {}", caseID, tasks);
        handleCancelledCaseEvent(caseID);       // just for cleaning up purposes, if any
    }


    public void handleCompleteCaseEvent(String caseID, String casedata) {
        _log.info("Case completed: {}", caseID);
        handleCancelledCaseEvent(caseID);       // just for cleaning up purposes, if any
    }


    public void handleEngineInitialisationCompletedEvent() {
        doFinalServiceToEngineInitialisation(false);
    }


    // here we are only interested in items the service knows about, being updated
    // by services other than this one
    public void handleWorkItemStatusChangeEvent(WorkItemRecord wir,
                                                String oldStatus, String newStatus) {
        WorkItemRecord cachedWir = _workItemCache.get(wir.getID());

        // if its a status change this service didn't cause
        if (!(cachedWir == null || newStatus.equals(cachedWir.getStatus()))) {

            synchronized (_ibEventMutex) {

                // if it has been 'finished', remove it from all queues
                if ((newStatus.equals(WorkItemRecord.statusComplete)) ||
                        (newStatus.equals(WorkItemRecord.statusDeadlocked)) ||
                        (newStatus.equals(WorkItemRecord.statusFailed)) ||
                        (newStatus.equals(WorkItemRecord.statusDiscarded)) ||
                        (newStatus.equals(WorkItemRecord.statusForcedComplete))) {

                    cleanupWorkItemReferences(cachedWir);
                }

                // if it has been 'suspended', find it on a 'started' queue & move it
                else if (newStatus.equals(WorkItemRecord.statusSuspended)) {
                    Participant p = getParticipantAssignedWorkItem(cachedWir, WorkQueue.STARTED);
                    if (p != null) {
                        p.getWorkQueues().movetoSuspend(cachedWir);
                        _workItemCache.updateResourceStatus(
                                cachedWir, WorkItemRecord.statusResourceSuspended);
                    }
                    _workItemCache.updateStatus(cachedWir, newStatus);
                }

                // if it has moved to started status
                else if (newStatus.equals(WorkItemRecord.statusExecuting)) {

                    // ...and was previously suspended
                    if (cachedWir.hasStatus(WorkItemRecord.statusSuspended)) {
                        Participant p = getParticipantAssignedWorkItem(cachedWir, WorkQueue.SUSPENDED);
                        if (p != null) {
                            p.getWorkQueues().movetoUnsuspend(cachedWir);
                            _workItemCache.updateResourceStatus(
                                    cachedWir, WorkItemRecord.statusResourceStarted);
                        }
                        _workItemCache.updateStatus(cachedWir, newStatus);
                    }
                }

                // if it was 'suspended', it's been unsuspended or rolled back
                else if (oldStatus.equals(WorkItemRecord.statusSuspended)) {
                    _workItemCache.updateStatus(cachedWir, newStatus);
                    for (Participant p : getParticipantsAssignedWorkItem(cachedWir)) {
                        p.getWorkQueues().refresh(cachedWir);
                    }
                }

                // if it is 'Is Parent', its just been newly started and has spawned
                // child items. Since we don't know who started it, all we can do is
                // pass responsibility to the starting service & remove knowledge of it
                else if (newStatus.equals(WorkItemRecord.statusIsParent)) {
                    cleanupWorkItemReferences(cachedWir);
                }
            }
        }
    }


    public void handleStartCaseEvent(YSpecificationID specID, String caseID,
                                     String launchingService, boolean delayed) {

        // if we initiated this delayed launch, log it
        if (delayed && launchingService != null && launchingService.equals(_services.getServiceURI())) {
            if (!_cache.logDelayedCaseLaunch(specID, caseID)) {
                EventLogger.log(specID, caseID, null, true);  // log without launcher info
            }
        }
    }

    /**
     * displays a web page describing the service
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/html");
        ServletOutputStream outStream = response.getOutputStream();
        String fileName = System.getenv("CATALINA_HOME") +
                "/webapps/resourceService/welcome.htm";

        // convert htm file to a byte array
        if (new File(fileName).exists()) {
            FileInputStream fStream = new FileInputStream(fileName);
            byte[] b = new byte[fStream.available()];
            fStream.read(b);
            fStream.close();

            // load the full welcome page if possible
            outStream.write(b);
        }
        else {
            // otherwise load a boring default
            StringBuilder output = new StringBuilder();
            output.append("<html><head><title>YAWL Resource Service</title>")
                    .append("</head><body><H3>Welcome to the YAWL Resource Service")
                    .append("</H3></body></html>");
            outStream.print(output.toString());
        }
        outStream.flush();
        outStream.close();
    }


    private void doFinalServiceToEngineInitialisation(boolean reloaded) {
        synchronized (_ibEventMutex) {

            // if the engine or the service has been restarted during this session
            if (_initCompleted || reloaded) {
                setUpInterfaceBClient(_services.getEngineURI());
                _services.reestablishClients(_interfaceBClient);
            } else _services.setInterfaceBClient(_interfaceBClient);


            if (!_services.engineIsAvailable()) return;

            setAuthorisedServiceConnections();
            _services.setServiceURI();
            sanitiseCaches();

            // if this is the first time the engine has started since service start...
            // (these things are only to be done once per service start)
            if (!_initCompleted) {
                restoreAutoTasks();
                for (WorkItemRecord wir : _cache.getOrphanedItems()) {
                    checkinItem(null, wir);
                }
                _cache.clearOrphanedItems();
                _initCompleted = true;
            }
        }
    }

    private boolean cleanupWorkItemReferences(WorkItemRecord wir) {
        if (_serviceEnabled) {
            if (!removeFromAll(wir)) return false;                    // workqueues
            ResourceMap rMap = getResourceMap(wir);
            if (rMap != null) {
                rMap.removeIgnoreList(wir);
                if (wir.getStatus().equals(WorkItemRecord.statusExecuting)) {
                    freeSecondaryResources(wir);
                }
            }
            _cache.cancelCodeletRunner(wir.getID());                    // if any
        }
        return true;
    }


    /**
     * **************************************************************************
     */

    // ORG DATA METHODS //
    public boolean isDefaultOrgDB() {
        return !_isNonDefaultOrgDB;
    }

    public ResourceDataSet getOrgDataSet() {
        return _orgDataSet;
    }

    /**
     * Loads all the org data from db into the ResourceDataSet mappings
     */
    public void loadResources() {
        if (_orgdb != null) {
            _orgDataSet = _orgdb.loadResources();

            // complete mappings for non-default org data backends
            if (_isNonDefaultOrgDB) finaliseNonDefaultLoad();

            // refresh the cached ResourceMaps (to allow for org data changes)
            _resMapCache.clear();

            // rebuild a work queue set and userid keymap for each participant
            for (Participant p : _orgDataSet.getParticipants()) {
                p.createQueueSet(_persisting);
                _cache.addUserKey(p);
            }

            // ... and the administrator (only on initial load)
            if (! _orgDataRefreshing) _resAdmin.createWorkQueues(_persisting);
        }
        else {
            _orgDataSet = new EmptyDataSource().getDataSource();
        }
    }


    public void refreshOrgData() {
        new OrgDataRefresher(this).refresh();
        _resMapCache.clear();   // reset static resource maps
        sanitiseCaches();
    }


    /**
     * This does final initialisation tasks involved in ensuring the caches match
     * the engine's known work. It is called via the EngineInitialisationCompleted
     * event, because it needs the engine to be completely initialised first. It
     * may also be executed via the 'synch' button on the admin queues.
     */
    public void sanitiseCaches() {
        List<String> engineIDs = syncCacheWithEngineItems();
        removeOrphanedItemsFromCache(engineIDs);
        cleanseQueues();                       // removed any uncached items from queues
    }


    private List<String> syncCacheWithEngineItems() {
        List<String> engineIDs = new ArrayList<String>();
        try {
            List<WorkItemRecord> engineItems = _services.getWorkItemsForService();

            // check that a copy of each engine child item is stored locally
            for (WorkItemRecord wir : engineItems) {
                if (wir.isAutoTask()) continue;                // ignore automated tasks

                if (!_workItemCache.containsKey(wir.getID())) {

                    // Parent items are treated differently. If they have never been started
                    // they should be in the cache, otherwise at least one child will be cached
                    if (wir.getStatus().equals(WorkItemRecord.statusIsParent)) {
                        List children = getChildren(wir.getID());
                        if (!((children == null) || children.isEmpty())) { // has kids
                            continue;                                       // so ignore
                        }
                    }

                    _log.info("Engine workItem '{}' was missing from local cache and " +
                            "so will be added and distributed according to its " +
                            "resourcing parameters.", wir.getID());
                    handleEnabledWorkItemEvent(wir);
                }
                engineIDs.add(wir.getID());
            }
        } catch (IOException ioe) {
            _log.warn("Could not get workitem list from engine to synchronise caches.");
        }
        return engineIDs;
    }


    private void removeOrphanedItemsFromCache(List<String> engineIDs) {

        // check that each item stored locally is also in engine
        Set<String> missingIDs = new HashSet<String>();
        for (String cachedID : _workItemCache.keySet()) {
            if (!engineIDs.contains(cachedID))
                missingIDs.add(cachedID);
        }
        for (String missingID : missingIDs) {
            WorkItemRecord deadWir = _workItemCache.get(missingID);

            // remove from queues first to avoid a db foreign key violation
            // then let the cache 'cleanser' runner remove it
            removeFromAll(deadWir);
            _log.info("Cached workitem '{}' did not exist in the Engine and so was removed.",
                    missingID);
        }
    }


    private void cleanseQueues() {
        for (Participant p : _orgDataSet.getParticipants()) {
            QueueSet qSet = p.getWorkQueues();
            if (qSet != null) {
                qSet.cleanseAllQueues(_workItemCache);

                // rebuild all 'offered' datasets
                WorkQueue wq = qSet.getQueue(WorkQueue.OFFERED);
                if (wq != null) {
                    for (WorkItemRecord wir : wq.getAll()) addToOfferedSet(wir, p);
                }
            }
        }
        WorkQueue q = _resAdmin.getWorkQueues().getQueue(WorkQueue.UNOFFERED);
        if (q != null) q.cleanse(_workItemCache);
    }


    private void finaliseNonDefaultLoad() {

        // for each entity set not supplied by the backend, load the service defaults.
        // At a minimum, the datasource must supply a set of participants
        if (_orgDataSet.getParticipants().isEmpty()) {
            _log.error("Participant set not loaded - service will disable.");
            _serviceEnabled = false;
            return;
        }

        // check other entities
        _orgDataSet.augmentDataSourceAsRequired();

        // restore user privileges for each participant
        Map<String, Object> upMap = _persister.selectMap("UserPrivileges");
        for (Participant p : _orgDataSet.getParticipants()) {
            UserPrivileges up = (UserPrivileges) upMap.get(p.getID());
            if (up != null) p.setUserPrivileges(up);
        }
    }


    private void restoreWorkQueues() {
        _log.info("Restoring persisted work queue data...");
        _workItemCache.restore();
        List<WorkQueue> orphanedQueues = new ArrayList<WorkQueue>();

        // restore the queues and attach to their owners
        List<WorkQueue> qList = _persister.select("WorkQueue");

        if (qList != null) {
            for (WorkQueue wq : qList) {
                wq.setPersisting(true);
                Hibernate.initialize(wq.getQueueAsMap());   // avoid lazy loading errors
                if (wq.getOwnerID().equals(ADMIN_STR)) {
                    _resAdmin.attachWorkQueue(wq, _persisting);
                } else {
                    if (_orgDataSet != null) {
                        Participant p = _orgDataSet.getParticipant(wq.getOwnerID());
                        if (p != null) {
                            p.attachWorkQueue(wq, _persisting);
                        } else {
                            orphanedQueues.add(wq);
                        }
                    }
                }
            }

            if (!orphanedQueues.isEmpty()) {
                removeOrphanedQueues(orphanedQueues);
            }
        }
        _persister.commit();
    }


    private void removeOrphanedQueues(List<WorkQueue> orphanedQueues) {
        for (WorkQueue orphan : orphanedQueues) {
            handleWorkQueueOnRemoval(orphan);
            orphan.clear();                        // del persisted items
            _persister.delete(orphan);             // del persisted queue
        }
    }


    public boolean isKnownUserID(String userid) { return _cache.isKnownUserID(userid); }

    private String fail(String msg) {
        return StringUtil.wrap(msg, FAIL_STR);
    }

    // ADD (NEW) ORG DATA OBJECTS //

    /**
     * Adds a new participant to the Resource DataSet, and persists it also
     *
     * @param p the new Participant
     */
    public String addParticipant(Participant p) {

        // check for userid uniqueness
        if (!isKnownUserID(p.getUserID())) {
            String newID = _orgDataSet.addParticipant(p);
            if (!newID.startsWith("<fail")) {
                p.createQueueSet(_persisting);

                // cleanup for non-default db
                if (_isNonDefaultOrgDB) {
                    p.setID(newID);
                    if (_persisting) {
                        _persister.insert(p.getUserPrivileges());
                        _persister.insert(p.getWorkQueues());
                    }
                } else _orgDataSet.updateParticipant(p);

                _cache.addUserKey(p);                         // and the userid--pid map
            }
            return newID;
        } else return fail("User id '" + p.getUserID() + "' is already in use");
    }


    public void importParticipant(Participant p) {
        if (_orgDataSet.importParticipant(p)) {
            p.createQueueSet(_persisting);

            // cleanup for non-default db
            if (_isNonDefaultOrgDB) {
                if (_persisting) {
                    _persister.insert(p.getUserPrivileges());
                    _persister.insert(p.getWorkQueues());
                }
            } else _orgdb.update(p);

            _cache.addUserKey(p);                           // and the userid--pid map
        }
    }


    public void updateParticipant(Participant p) {
        if (_orgDataSet.updateParticipant(p)) {
            _cache.addUserKey(p);                            // and the userid--pid map
            if (_isNonDefaultOrgDB) {
                _persister.update(p.getUserPrivileges());  // persist other classes
                _persister.update(p.getWorkQueues());
            }
        }
    }


    public void removeParticipant(Participant p) {
        synchronized (_removalMutex) {
            if (_orgDataSet.removeParticipant(p)) {
                handleWorkQueuesOnRemoval(p);
                QueueSet qSet = p.getWorkQueues();
                qSet.purgeAllQueues();
                for (WorkQueue wq : qSet.getActiveQueues()) {
                    if (wq != null) _persister.delete(wq);
                }
                _persister.delete(p.getUserPrivileges());
                _cache.removeUserKey(p);
            }
        }
    }


    public boolean removeParticipant(String pid) {
        if (pid != null) {
            Participant p = _orgDataSet.getParticipant(pid);
            if (p != null) {
                removeParticipant(p);
                return true;
            }
        }
        return false;
    }


    // RETRIEVAL METHODS //

    public String getActiveParticipantsAsXML() {
        return _cache.getActiveParticipantsAsXML();
    }


    public Participant getParticipantFromUserID(String userID) {
        String pid = _cache.getParticipantIDFromUserID(userID);
        return _orgDataSet.getParticipant(pid);
    }


    public QueueSet getUserQueueSet(String userID) {
        Participant p = getParticipantFromUserID(userID);
        if (p != null)
            return p.getWorkQueues();
        else return null;
    }


    public Set<Participant> getParticipantsAssignedWorkItem(String workItemID,
                                                            int queueType) {
        Set<Participant> result = new HashSet<Participant>();
        for (Participant p : _orgDataSet.getParticipants()) {
            QueueSet qSet = p.getWorkQueues();
            if ((qSet != null) && (qSet.hasWorkItemInQueue(workItemID, queueType)))
                result.add(p);
        }
        if (result.isEmpty()) result = null;
        return result;
    }


    public Set<Participant> getParticipantsAssignedWorkItem(WorkItemRecord wir) {
        Set<Participant> result = new HashSet<Participant>();
        for (Participant p : _orgDataSet.getParticipants()) {
            QueueSet qSet = p.getWorkQueues();
            if ((qSet != null) && (qSet.hasWorkItemInAnyQueue(wir)))
                result.add(p);
        }
        if (result.isEmpty()) result = null;
        return result;
    }


    public Participant getParticipantAssignedWorkItem(WorkItemRecord wir, int qType) {
        Participant result = null;
        if ((qType > WorkQueue.OFFERED) && (qType <= WorkQueue.SUSPENDED)) {
            for (Participant p : _orgDataSet.getParticipants()) {
                QueueSet qSet = p.getWorkQueues();
                if ((qSet != null) && (qSet.hasWorkItemInQueue(wir.getID(), qType))) {
                    result = p;
                    break;
                }
            }
        }
        return result;
    }


    public String getFullNameForUserID(String userID) {
        if (userID.equals(ADMIN_STR)) return "Administrator";

        Participant p = getParticipantFromUserID(userID);
        return (p != null) ? p.getFullName() : null;
    }


    /**
     * ***********************************************************************
     */

    // WORKITEM ALLOCATION AND WORKQUEUE METHODS //
    public WorkItemRecord offerToAll(WorkItemRecord wir) {
        if (_orgDataSet.getParticipantCount() > 0) {
            _workItemCache.updateResourceStatus(wir, WorkItemRecord.statusResourceOffered);
            for (Participant p : _orgDataSet.getParticipants()) {
                QueueSet qSet = p.getWorkQueues();
                if (qSet == null) qSet = p.createQueueSet(_persisting);
                qSet.addToQueue(wir, WorkQueue.OFFERED);
                announceModifiedQueue(p.getID());
            }
        } else _resAdmin.addToUnoffered(wir);

        return wir;
    }


    private void withdrawOffer(ResourceMap rMap, WorkItemRecord wir) {
        if (rMap != null && rMap.hasOffers(wir.getID())) {
            rMap.withdrawOffer(wir);     // rMap has record of who was offered wir
        }
        else {
            withdrawOfferFromAll(wir);   // non-default org db or beta spec
        }
    }


    public void withdrawOfferFromAll(WorkItemRecord wir) {
        for (Participant p : _orgDataSet.getParticipants()) {
            QueueSet qSet = p.getWorkQueues();
            if (qSet != null && qSet.removeFromQueue(wir, WorkQueue.OFFERED)) {
                announceModifiedQueue(p.getID());
            }
        }
    }


    public boolean removeFromAll(WorkItemRecord wir) {
        for (Participant p : _orgDataSet.getParticipants()) {
            QueueSet qSet = p.getWorkQueues();
            if (qSet != null) {
                qSet.removeFromAllQueues(wir);
                announceModifiedQueue(p.getID());
            }
        }
        return _resAdmin.removeFromAllQueues(wir);
    }


    public void removeCaseFromAllQueues(String caseID) {
        for (Participant p : _orgDataSet.getParticipants()) {
            QueueSet qSet = p.getWorkQueues();
            if (qSet != null) {
                qSet.removeCaseFromAllQueues(caseID);
                announceModifiedQueue(p.getID());
            }
        }
        _resAdmin.removeCaseFromAllQueues(caseID);
    }

    public QueueSet getAdminQueues() {
        return _resAdmin.getWorkQueues();
    }


    public WorkItemRecord acceptOffer(Participant p, WorkItemRecord wir) {

        // if a deferred choice, disallow any other acceptances
        if (wir.isDeferredChoiceGroupMember()) {
            if (isDeferredChoiceHandled(wir)) return wir;
            setDeferredChoiceHandled(wir);
        }

        ResourceMap rMap = getResourceMap(wir);
        withdrawOffer(rMap, wir);

        // take the appropriate start action
        if (rMap != null && rMap.getStartInteraction().getInitiator() ==
                AbstractInteraction.SYSTEM_INITIATED) {
            startImmediate(p, wir);
            WorkItemRecord startedItem = getExecutingChild(getChildren(wir.getID()));
            if (startedItem != null) {
                WorkItemRecord cachedItem = _workItemCache.get(startedItem.getID());
                if (cachedItem != null) wir = cachedItem;
            }
        } else {

            // either start is user-initiated or there's no resource map (beta spec) 
            wir.setResourceStatus(WorkItemRecord.statusResourceAllocated);
            QueueSet qSet = p.getWorkQueues();
            if (qSet == null) qSet = p.createQueueSet(_persisting);
            qSet.addToQueue(wir, WorkQueue.ALLOCATED);
        }

        _workItemCache.update(wir);
        return wir;
    }


    // DEFERRED CHOICE HANDLERS //

    protected void setDeferredChoiceHandled(WorkItemRecord wir) {
        if (wir.isDeferredChoiceGroupMember()) {
            _cache.setDeferredGroupHandled(wir.getRootCaseID(),
                    wir.getDeferredChoiceGroupID());
        }
    }


    protected boolean unsetDeferredChoiceHandled(WorkItemRecord wir) {
        return wir.isDeferredChoiceGroupMember() &&
                _cache.unsetDeferredGroupHandled(wir.getRootCaseID(),
                        wir.getDeferredChoiceGroupID());
        } 


    protected boolean isDeferredChoiceHandled(WorkItemRecord wir) {
        return wir.isDeferredChoiceGroupMember() &&
                _cache.isDeferredGroupHandled(wir.getRootCaseID(),
                        wir.getDeferredChoiceGroupID());
    }


    // Deals with live workitems in a participant's queues when the participant is
    // removed. An admin is advised to manually reallocate items before removing this p.,
    // but this is the default behaviour if there are still items in the queue.
    // The strategy is:
    //  - Offered: if this is the only p. that has received this offer, give it back to
    //             the admin for re-offering. If others have been offered the same item,
    //             there's nothing more to do.
    //  - Allocated: give it back to admin for reallocating (via unoffered queue)
    //  - Started: give it back to admin for restarting (via unoffered queue)
    //  - Suspended: same as Started.

    public void handleWorkQueuesOnRemoval(Participant p) {
        handleWorkQueuesOnRemoval(p, p.getWorkQueues());
    }

    public void handleWorkQueuesOnRemoval(Participant p, QueueSet qs) {
        if (qs == null) return;    // no queues = nothing to do
        synchronized (_removalMutex) {
            handleOfferedQueueOnRemoval(p, qs.getQueue(WorkQueue.OFFERED));
            handleNonOfferedQueueOnRemoval(qs.getQueue(WorkQueue.ALLOCATED));
            handleNonOfferedQueueOnRemoval(qs.getQueue(WorkQueue.STARTED));
            handleNonOfferedQueueOnRemoval(qs.getQueue(WorkQueue.SUSPENDED));
        }
    }


    public void handleWorkQueueOnRemoval(WorkQueue wq) {
        synchronized (_removalMutex) {
            if (wq != null) {
                wq.setPersisting(false);                 // turn off circular persistence
                if (wq.getQueueType() == WorkQueue.OFFERED)
                    handleOfferedQueueOnRemoval(null, wq);
                else
                    handleNonOfferedQueueOnRemoval(wq);
            }
        }
    }


    public void handleOfferedQueueOnRemoval(Participant p, WorkQueue qOffer) {
        synchronized (_removalMutex) {
            if ((qOffer != null) && (!qOffer.isEmpty())) {
                Set<WorkItemRecord> wirSet = qOffer.getAll();

                // get all items on all offered queues, except this part's queue
                Set<WorkItemRecord> offerSet = new HashSet<WorkItemRecord>();
                for (Participant temp : _orgDataSet.getParticipants()) {
                    if ((p == null) || (!temp.getID().equals(p.getID()))) {
                        WorkQueue q = temp.getWorkQueues().getQueue(WorkQueue.OFFERED);
                        if (q != null) offerSet.addAll(q.getAll());
                    }
                }

                // compare each item in this part's queue to the complete set
                for (WorkItemRecord wir : wirSet) {
                    if (!offerSet.contains(wir)) {
                        _resAdmin.getWorkQueues().removeFromQueue(wir, WorkQueue.WORKLISTED);
                        _resAdmin.addToUnoffered(wir);
                    }
                }
            }
        }
    }


    public void handleNonOfferedQueueOnRemoval(WorkQueue queue) {
        synchronized (_removalMutex) {

            // allocated queue - all allocated go back to admin's unoffered
            if (!(queue == null || queue.isEmpty())) {
                _resAdmin.getWorkQueues().removeFromQueue(queue, WorkQueue.WORKLISTED);
                _resAdmin.getWorkQueues().addToQueue(WorkQueue.UNOFFERED, queue);
                for (WorkItemRecord wir : queue.getAll()) {
                    _services.announceResourceUnavailable(wir);
                }
            }
        }
    }


    // started & suspended queues
    public void handleStartedQueuesOnRemoval(Participant p, WorkQueue qStart) {
        synchronized (_removalMutex) {
            if (qStart != null) {
                Set<WorkItemRecord> startSet = qStart.getAll();

                // if called during restore, there's no engine available yet, so we have to
                // save the wir until there is one; otherwise, we can do the checkin now
                for (WorkItemRecord wir : startSet) {
                    if (serviceInitialised) {
                        checkinItem(p, wir);
                    } else {
                        _cache.addOrphanedItem(wir);
                    }
                    _resAdmin.getWorkQueues().removeFromQueue(wir, WorkQueue.WORKLISTED);
                }
            }
        }
    }


    /**
     * moves the workitem to executing for the participant.
     * <p/>
     * Note that when an item is checked out of the engine, at least one child item
     * is spawned, and that is the item that executes (i.e. not the parent).
     *
     * @param p   the participant starting the workitem
     * @param wir the item to start
     * @return true for a successful workitem start
     */
    public boolean start(Participant p, WorkItemRecord wir) {

        // if 'executing', it's already been started so move queues & we're done
        if (wir.getStatus().equals(WorkItemRecord.statusExecuting)) {
            wir.setResourceStatus(WorkItemRecord.statusResourceStarted);
            p.getWorkQueues().movetoStarted(wir);
            return true;
        }

        if (_blockIfSecondaryResourcesUnavailable &&
                (!secondaryResourcesAvailable(wir, p))) {
            return false;
        }

        if (checkOutWorkItem(wir, getStartingLogPredicate(p, wir))) {
            WorkItemRecord oneToStart = getStartedChild(wir);
            if (oneToStart == null)
                return false;      // problem: no executing children

            // add the executing child to the cache
            oneToStart.setResourceStatus(WorkItemRecord.statusResourceStarted);
            _workItemCache.add(oneToStart);

            p.getWorkQueues().movetoStarted(wir, oneToStart);

            if (wir.getResourceStatus().equals(WorkItemRecord.statusResourceUnoffered)) {
                _resAdmin.getWorkQueues().removeFromQueue(wir, WorkQueue.UNOFFERED);
            }

            // cleanup deallocation list for started item (if any)
            ResourceMap rMap = getResourceMap(wir);
            if (rMap != null) {
                rMap.removeIgnoreList(wir);  // cleanup deallocation list for item (if any)
                rMap.getSecondaryResources().engage(oneToStart);     // mark SR's as busy
            }

            return true;
        } else {
            _log.error("Could not start workitem: {}", wir.getID());
            return false;
        }
    }


    private WorkItemRecord getStartedChild(WorkItemRecord wir) {
        WorkItemRecord startedChild;

        // get all the child instances of this workitem
        List<WorkItemRecord> children = getChildren(wir.getID());

        if (children == null) {
            _log.error("Checkout of workitem '{}' unsuccessful.", wir.getID());
            return null;
        }

        if (children.size() > 1) {                   // i.e. if multi atomic task

            // which one got started with the checkout?
            startedChild = getExecutingChild(children);

            // get the rest of the kids and distribute them
            distributeChildren(startedChild, children);
        } else if (children.size() == 0) {                  // a 'fired' workitem
            startedChild = refreshWIRFromEngine(wir);
        } else {                                            // exactly one child
            startedChild = children.get(0);
        }
        return startedChild;
    }


    private void distributeChildren(WorkItemRecord started, List<WorkItemRecord> children) {

        // list should always have at least one member
        for (WorkItemRecord child : children) {

            // don't distribute the already started child, but only the others
            if (!started.getID().equals(child.getID()))
                handleEnabledWorkItemEvent(child);
        }
    }


    private boolean secondaryResourcesAvailable(WorkItemRecord wir, Participant p) {
        ResourceMap rMap = getResourceMap(wir);
        if (!(rMap == null || rMap.getSecondaryResources().available(wir))) {
            _log.warn("Workitem '{}' could not be started due " +
                    "to one or more unavailable secondary resources. The workitem " +
                    "has been placed on the participant's allocated queue.", wir.getID());
            if (wir.getResourceStatus().equals(WorkItemRecord.statusResourceOffered)) {
                withdrawOffer(rMap, wir);
            }
            wir.setResourceStatus(WorkItemRecord.statusResourceAllocated);
            p.getWorkQueues().addToQueue(wir, WorkQueue.ALLOCATED);
            return false;
        }
        return true;
    }


    // USER - TASK PRIVILEGE ACTIONS //

    public boolean suspendWorkItem(Participant p, WorkItemRecord wir) {
        boolean success = false;
        if (hasUserTaskPrivilege(p, wir, TaskPrivileges.CAN_SUSPEND)) {
            try {
                if (successful(_services.suspendWorkItem(wir.getID()))) {
                    wir.setResourceStatus(WorkItemRecord.statusResourceSuspended);
                    p.getWorkQueues().movetoSuspend(wir);
                    _workItemCache.update(wir);
                    success = true;
                }
            } catch (IOException ioe) {
                _log.error("Exception trying to suspend work item: " + wir.getID(), ioe);
            }
        }
        return success;
    }

    public boolean unsuspendWorkItem(Participant p, WorkItemRecord wir) {

        // if user successfully suspended they also have unsuspend privileges
        boolean success = false;
        try {

            if (successful(_services.unsuspendWorkItem(wir.getID()))) {
                wir.setResourceStatus(WorkItemRecord.statusResourceStarted);
                p.getWorkQueues().movetoUnsuspend(wir);
                _workItemCache.update(wir);
                success = true;
            }
        } catch (IOException ioe) {
            _log.error("Exception trying to unsuspend work item: " + wir.getID(), ioe);
        }
        return success;
    }


    public boolean reallocateStatelessWorkItem(Participant pFrom, Participant pTo,
                                               WorkItemRecord wir) {
        boolean success = false;
        if (hasUserTaskPrivilege(pFrom, wir, TaskPrivileges.CAN_REALLOCATE_STATELESS)) {

            // reset the item's data params to original values
            wir.setUpdatedData(wir.getDataList());
            reallocateWorkItem(pFrom, pTo, wir, EventLogger.event.reallocate_stateless);
            success = true;
        }
        return success;
    }


    public boolean reallocateStatefulWorkItem(Participant pFrom, Participant pTo,
                                              WorkItemRecord wir) {
        boolean success = false;
        if (hasUserTaskPrivilege(pFrom, wir, TaskPrivileges.CAN_REALLOCATE_STATEFUL)) {
            reallocateWorkItem(pFrom, pTo, wir, EventLogger.event.reallocate_stateful);
            success = true;
        }
        return success;
    }


    private void reallocateWorkItem(Participant pFrom, Participant pTo,
                                    WorkItemRecord wir, EventLogger.event event) {
        EventLogger.log(wir, pFrom.getID(), event);
        pFrom.getWorkQueues().removeFromQueue(wir, WorkQueue.STARTED);
        pTo.getWorkQueues().addToQueue(wir, WorkQueue.STARTED);
    }


    public boolean deallocateWorkItem(Participant p, WorkItemRecord wir) {
        boolean success = false;
        if (hasUserTaskPrivilege(p, wir, TaskPrivileges.CAN_DEALLOCATE)) {
            p.getWorkQueues().removeFromQueue(wir, WorkQueue.ALLOCATED);

            if (wir.isDeferredChoiceGroupMember()) unsetDeferredChoiceHandled(wir);

            ResourceMap rMap = getResourceMap(wir);
            if (rMap != null) {
                rMap.ignore(wir, p);                  // add Participant to ignore list
                rMap.distribute(wir);                 // redistribute workitem
            } else {                                    // pre version 2.0
                if (_orgDataSet.getParticipantCount() > 1) {
                    offerToAll(wir);
                    p.getWorkQueues().removeFromQueue(wir, WorkQueue.OFFERED);
                } else _resAdmin.addToUnoffered(wir);
            }
            EventLogger.log(wir, p.getID(), EventLogger.event.deallocate);
            success = true;
        }
        return success;
    }


    public boolean delegateWorkItem(Participant pFrom, Participant pTo,
                                    WorkItemRecord wir) {
        boolean success = false;
        if (hasUserTaskPrivilege(pFrom, wir, TaskPrivileges.CAN_DELEGATE)) {
            pFrom.getWorkQueues().removeFromQueue(wir, WorkQueue.ALLOCATED);
            pTo.getWorkQueues().addToQueue(wir, WorkQueue.ALLOCATED);
            EventLogger.log(wir, pFrom.getID(), EventLogger.event.delegate);
            success = true;
        }
        return success;
    }


    public boolean skipWorkItem(Participant p, WorkItemRecord wir) {
        if (hasUserTaskPrivilege(p, wir, TaskPrivileges.CAN_SKIP)) {
            try {
                if (successful(_services.skipWorkItem(wir.getID()))) {
                    p.getWorkQueues().removeFromQueue(wir, WorkQueue.ALLOCATED);
                    EventLogger.log(wir, p.getID(), EventLogger.event.skip);
                    return true;
                }
            } catch (IOException ioe) {
                return false;
            }
        }
        return false;
    }


    public String pileWorkItem(Participant p, WorkItemRecord wir) {
        String result;
        if (hasUserTaskPrivilege(p, wir, TaskPrivileges.CAN_PILE)) {
            ResourceMap map = getResourceMap(wir);
            if (map != null) {
                result = map.setPiledResource(p, wir);
                if (!result.startsWith("Cannot"))
                    EventLogger.log(wir, p.getID(), EventLogger.event.pile);
            } else
                result = "Cannot pile task: no resourcing parameters defined for specification.";
        } else result = "Cannot pile task: insufficient privileges.";

        return result;
    }


    public String unpileTask(ResourceMap resMap, Participant p) {
        if (resMap.getPiledResourceID().equals(p.getID())) {
            resMap.removePiledResource();
            return "Task successfully unpiled";
        }
        return "Cannot unpile task - resource settings unavailable";
    }


    // ISSUE: If p is currently logged on, we'll use p's handle (the engine will use
    //        it to log p as the starter). If p is not logged on, the service's handle
    //        has to be used, and thus the service will be logged as the starter. There is
    //        no way around this currently, but will be handled when the engine is
    //        made completely agnostic to resources. 
    public boolean routePiledWorkItem(Participant p, WorkItemRecord wir) {
        return routeWorkItem(p, wir, getEngineSessionHandle());
    }

    // ISSUE: same as that in the method above. Called by this.acceptOffer and
    // ResourceMap.doStart
    public boolean startImmediate(Participant p, WorkItemRecord wir) {
        return routeWorkItem(p, wir, getEngineSessionHandle());
    }


    private boolean routeWorkItem(Participant p, WorkItemRecord wir, String handle) {
        if (handle != null) start(p, wir);
        return (handle != null);
    }

    public boolean hasUserTaskPrivilege(Participant p, WorkItemRecord wir,
                                        int privilege) {

        // admin access overrides set privileges
        if (p.isAdministrator()) return true;

        TaskPrivileges taskPrivileges = getTaskPrivileges(wir);
        return (taskPrivileges != null) && taskPrivileges.hasPrivilege(p, privilege);
    }


    public TaskPrivileges getTaskPrivileges(String itemID) {
        return getTaskPrivileges(_workItemCache.get(itemID));
    }


    public TaskPrivileges getTaskPrivileges(WorkItemRecord wir) {
        ResourceMap rMap = getResourceMap(wir);
        return (rMap != null) ? rMap.getTaskPrivileges() : null;
    }


    public String getWorkItem(String itemID) {
        String result = fail(WORKITEM_ERR + " ID");
        WorkItemRecord wir = _workItemCache.get(itemID);
        if (wir != null) {
            result = wir.toXML();
        }
        return result;
    }

    public WorkItemRecord getWorkItemRecord(String itemID) {
        return (itemID != null) ? _workItemCache.get(itemID) : null;
    }


    public String updateWorkItemData(String itemID, String data) {
        String result;
        if ((data != null) && (data.length() > 0)) {
            WorkItemRecord wir = _workItemCache.get(itemID);
            if (wir != null) {
                if (wir.getStatus().equals(WorkItemRecord.statusExecuting)) {
                    Element dataElem = JDOMUtil.stringToElement(data);
                    if (dataElem != null) {
                        String validate = checkWorkItemDataAgainstSchema(wir, dataElem);
                        if (validate.startsWith("<success")) {
                            wir.setUpdatedData(dataElem);                  // all's good
                            _workItemCache.update(wir);
                            result = "<success/>";
                        }
                        else result = fail("Data failed validation: " + validate);
                    }
                    else result = fail("Data XML is malformed");
                }
                else result = fail(
                        "Workitem '" + itemID + "' has a status of '" + wir.getStatus() +
                                "' - data may only be updated for a workitem with 'Executing' status.");
            }
            else result = fail(WORKITEM_ERR + ": " + itemID);
        }
        else result = fail("Data is null or empty.");

        return result;
    }


    public String checkWorkItemDataAgainstSchema(WorkItemRecord wir, Element data) {
        String result = "<success/>";
        YSpecificationID specID = new YSpecificationID(wir);
        try {
            TaskInformation taskInfo = getTaskInformation(specID, wir.getTaskID());
            if (! isValidDataName(wir, data, taskInfo.getDecompositionID())) {
                return fail("Invalid data structure: root element name " +
                        "must match decomposition ID, task ID or task name");
            }
            SpecificationData specData = getSpecData(specID);
            String schema = specData.getSchemaLibrary();
            YDataValidator validator = new YDataValidator(schema);
            if (validator.validateSchema()) {

                // a YDataValidationException is thrown here if validation fails
                validator.validate(taskInfo.getParamSchema().getCombinedParams(), data, "");
            }
            else {
                result = fail("Invalid data schema");
            }
        }
        catch (Exception e) {
                result = fail(e.getMessage());
        }
        return result;
    }


    // checks for valid root data element name - includes backwards compatibility
    private boolean isValidDataName(WorkItemRecord wir, Element data, String decompID) {
        String name = data.getName();
        String taskID = wir.getTaskID();
        String taskName = wir.getTaskName().replace(' ', '_');
        return name.equals(decompID) || name.equals(taskID) || name.equals(taskName);
    }


    /**
     * *************************************************************************
     */

    public String chainCase(Participant p, WorkItemRecord wir) {
        String result;
        ResourceMap rMap = getResourceMap(wir);
        if (rMap != null) {
            result = addChain(p, wir);
            if (result.contains("success"))
                withdrawOffer(rMap, wir);
        } else
            result = "Cannot chain tasks: no resourcing parameters defined for specification.";

        return result;
    }

    public boolean routeChainedWorkItem(Participant p, WorkItemRecord wir) {

        // only route if user is still logged on
        return _cache.getSessionHandle(p) != null &&
                routeWorkItem(p, wir, getEngineSessionHandle());
    }

    public String addChain(Participant p, WorkItemRecord wir) {
        String result;
        String caseID = wir.getRootCaseID();
        if (!_cache.isChainedCase(caseID)) {
            _cache.addChainedCase(caseID, p);
            routeChainedWorkItem(p, wir);
            EventLogger.log(wir, p.getID(), EventLogger.event.chain);
            result = "Chaining successful.";
        } else result = "Cannot chain: case already chained by another user.";
        return result;
    }

    public void removeChain(String caseID) { _cache.removeChainedCase(caseID); }


    public Set<String> getChainedCases(Participant p) {
        cleanCaches();
        Set<String> result = new HashSet<String>();
        for (String caseID : _cache.getChainedCaseIDsForParticipant(p)) {
            result.add(caseID + "::" + getSpecIdentifierForCase(caseID));
        }
        return result;
    }


    public boolean routeIfChained(WorkItemRecord wir, Set<Participant> distributionSet) {
        boolean result = false;
        String caseID = wir.getRootCaseID();
        if (_cache.isChainedCase(caseID)) {
            Participant p = _cache.getChainedParticipant(caseID);
            if (distributionSet.contains(p))
                result = routeChainedWorkItem(p, wir);
        }
        return result;
    }

    /**
     * *************************************************************************
     */

    private void removeActiveCalendarEntriesForCase(String caseID) {
        try {
            for (String resourceID : _calendar.freeResourcesForCase(caseID)) {
                _orgDataSet.freeResource(caseID, resourceID);
            }
            _calendar.commitTransaction();
        } catch (CalendarException ce) {
            _log.error("Could not clear Calendar bookings for case: " + caseID, ce);
            _calendar.rollBackTransaction();
        }
    }


    private String getSpecIdentifierForCase(String caseID) {
        for (WorkItemRecord wir : _workItemCache.values()) {
            if (wir.getRootCaseID().equals(caseID))
                return wir.getSpecIdentifier();
        }
        return "";
    }

    private void cleanCaches() {
        Set<String> liveCases = _services.getAllRunningCaseIDs();
        if ((liveCases != null) && (!liveCases.isEmpty())) {
            for (String id : _cache.getChainedCaseIDs()) {
                if (!liveCases.contains(id)) _cache.removeChainedCase(id);
            }
        }
    }


    /**
     * @return the union of persisted and unpersisted maps
     */
    public Set<ResourceMap> getPiledTaskMaps(Participant p) {
        Set<ResourceMap> result = getUnpersistedPiledTasks(p);
        if (_persisting) {
            for (ResourceMap map : getPersistedPiledTasks(p)) {
                if (!mapSetContains(result, map)) {
                    result.add(map);
                }
            }
        }
        return result;
    }

    public Set<ResourceMap> getUnpersistedPiledTasks(Participant p) {
        Set<ResourceMap> result = new HashSet<ResourceMap>();
        Set<ResourceMap> mapSet = getAllResourceMaps();
        for (ResourceMap map : mapSet) {
            Participant piler = map.getPiledResource();
            if ((piler != null) && (piler.getID().equals(p.getID())))
                result.add(map);
        }
        return result;
    }

    public Set<ResourceMap> getPersistedPiledTasks(Participant p) {
        Set<ResourceMap> result = new HashSet<ResourceMap>();
        List maps = _persister.select("ResourceMap");
        if (maps != null) {
            for (Object o : maps) {
                ResourceMap map = (ResourceMap) o;
                String pid = map.getPiledResourceID();
                if ((pid != null) && (pid.equals(p.getID())))
                    result.add(map);
            }
        }
        _persister.commit();
        return result;
    }


    public ResourceMap getPersistedPiledTask(YSpecificationID specID, String taskID) {
        ResourceMap map = null;
        String where = String.format(
                "_specID.identifier='%s' and _specID.version.version='%s' and _taskID='%s'",
                specID.getIdentifier(), specID.getVersionAsString(), taskID);
        List mapList = _persister.selectWhere("ResourceMap", where);
        if ((mapList != null) && (!mapList.isEmpty())) {
            map = (ResourceMap) mapList.iterator().next();
            map.setPersisting(true);
        }
        _persister.commit();
        return map;
    }


    public void deletePersistedPiledTasks(YSpecificationID specID) {
        String where = String.format(
                "_specID.identifier='%s' and _specID.version.version='%s'",
                specID.getIdentifier(), specID.getVersionAsString());
        List mapList = _persister.selectWhere("ResourceMap", where);
        if (mapList != null) {
            for (Object map : mapList) {
                _persister.delete(map);
            }
        }
        _persister.commit();
    }


    public boolean mapSetContains(Set<ResourceMap> mapSet, ResourceMap other) {
        for (ResourceMap map : mapSet) {
            if (map.equals(other) &&
                    map.getPiledResourceID().equals(other.getPiledResourceID())) {
                return true;
            }
        }
        return false;
    }


    public Set<ResourceMap> getAllResourceMaps() {
        return _resMapCache.getAll();
    }

    /**
     * ***********************************************************************
     */


    public DataSource getOrgDataSource() { return _orgdb; }

    public void setPersisting(boolean flag) {
        _persisting = flag;
        if (_persisting) _persister = Persister.getInstance();
        else _persister = null;
    }

    public boolean isPersisting() { return _persisting; }

    /**
     * Starts a timer task to refresh the org data dataset at regular intervals, or turns
     * an existing timer off if interval < 0
     *
     * @param interval the number of minutes between each refresh
     */
    public void startOrgDataRefreshTimer(long interval) {
        if ((interval < 1) && (_orgDataRefresher != null)) {
            _orgDataRefresher.cancel();            // disable timer
        } else {
            _orgDataRefresher = new OrgDataRefresher(this, interval);
        }
    }

    public void setPersistPiling(boolean persist) {
        _persistPiling = persist;
    }

    public boolean isPersistPiling() { return _persistPiling; }


    public void setBlockOnUnavailableSecondaryResources(boolean block) {
        _blockIfSecondaryResourcesUnavailable = block;
    }


    public Set<Participant> getDistributionSet(String itemID) {
        ResourceMap map = getResourceMap(itemID);
        return (map != null) ? map.getDistributionSet() : null;
    }

    public ResourceMap getResourceMap(String itemID) {
        if (itemID != null) {
            return getCachedResourceMap(getWorkItemCache().get(itemID));
        } else return null;
    }

    public ResourceMap getCachedResourceMap(WorkItemRecord wir) {
        if (wir == null) return null;
        return getCachedResourceMap(new YSpecificationID(wir), wir.getTaskID());
    }


    public ResourceMap getCachedResourceMap(YSpecificationID specID, String taskID) {
        return _resMapCache.get(specID, taskID);
    }


    public ResourceMap getResourceMap(WorkItemRecord wir) {
        if (wir == null) return null;
        ResourceMap map = getCachedResourceMap(wir);

        // if we don't have a resource map for the task stored yet, let's make one
        if (map == null) {
            try {
                YSpecificationID specID = new YSpecificationID(wir);
                String taskID = wir.getTaskID();
                Element resElem = getResourcingSpecs(specID, taskID,
                        getEngineSessionHandle());

                if ((resElem != null) &&
                        successful(JDOMUtil.elementToString(resElem))) {

                    map = new ResourceMap(specID, taskID, resElem, _persisting);
                    _resMapCache.add(specID, taskID, map);
                }
            } catch (IOException ioe) {
                _log.error("Exception getting resource specs from Engine", ioe);
            }
        }

        return map;              // map = null if no resourcing spec for this task
    }


    public static void setServiceInitialised() { serviceInitialised = true; }


    public Set<Participant> getWhoCompletedTask(String taskID, WorkItemRecord wir) {
        String caseID = wir.getRootCaseID();
        Set<Participant> completers = _cache.getTaskCompleters(taskID, caseID);
        if (completers.isEmpty()) {          // empty if tomcat restarted mid-case
            LogMiner miner = LogMiner.getInstance();
            String xml = miner.getCaseEvents(caseID);
            if (! (xml == null || xml.startsWith("<fail"))) {
                XNode root = new XNodeParser().parse(xml);
                for (XNode eventNode : root.getChildren()) {
                    String eTaskID = eventNode.getChildText("taskid");
                    String eType = eventNode.getChildText("eventtype");
                    if (! (eTaskID == null || eType == null) &&
                            eTaskID.equals(taskID) && eType.equals("complete")) {
                        String pid = eventNode.getChildText("resourceid");
                        Participant p = _orgDataSet.getParticipant(pid);
                        if (p != null) {
                            completers.add(p);
                        }
                    }
                }
            }
        }
        return completers;
    }


    /**
     * get the workitem's (task) decomposition id
     *
     * @param wir - the workitem to get the decomp id for
     */
    public String getDecompID(WorkItemRecord wir) {
        return getDecompID(new YSpecificationID(wir), wir.getTaskID());
    }

    //***************************************************************************//

    /**
     * gets a task's decomposition id
     *
     * @param specID the specification id
     * @param taskID the task's id
     */
    public String getDecompID(YSpecificationID specID, String taskID) {
        try {
            TaskInformation taskinfo = getTaskInformation(specID, taskID);
            return taskinfo.getDecompositionID();
        } catch (IOException ioe) {
            _log.error("IO Exception in getDecompId ", ioe);
            return null;
        }
    }


    // CHECKOUT METHODS //

    /**
     * Check the workitem out of the engine
     *
     * @param wir - the workitem to check out
     * @return true if checkout was successful
     */
    protected boolean checkOutWorkItem(WorkItemRecord wir, String logPredicate) {
        try {
            if (null != checkOut(wir.getID(), logPredicate, getEngineSessionHandle())) {
                _log.info("   checkout successful: {}", wir.getID());
                return true;
            } else {
                _log.info("   checkout unsuccessful: {}", wir.getID());
                return false;
            }
        } catch (YAWLException ye) {
            _log.error("YAWL Exception with checkout: " + wir.getID(), ye);
            return false;
        } catch (IOException ioe) {
            _log.error("IO Exception with checkout: " + wir.getID(), ioe);
            return false;
        }
    }

    //***************************************************************************//

    // re-adds checkedout item to local cache after a restore (if required)
    private void checkCacheForWorkItem(WorkItemRecord wir) {
        if (getCachedWorkItem(wir.getID()) == null) {

            // if the item is not locally cached, it means a restore has occurred
            // after a checkout & the item is still checked out, so lets put it back
            // so that it can be checked back in
            getIBCache().addWorkItem(wir);
        }
    }


    /**
     * Checks a (checked out) workitem back into the engine
     *
     * @param p   - the participant checking in the item
     * @param wir - workitem to check into the engine
     * @return true if checkin is successful
     */
    public String checkinItem(Participant p, WorkItemRecord wir) {
        String result = "<failure/>";                              // assume the worst
        try {
            wir = _workItemCache.get(wir.getID());                // refresh wir

            if (wir != null) {
                Element outData = wir.getUpdatedData();
                if (outData == null) outData = wir.getDataList();
                checkCacheForWorkItem(wir);
                if (p != null) _cache.addTaskCompleter(p, wir);
                result = checkInWorkItem(wir.getID(), wir.getDataList(), outData,
                        getCompletionLogPredicate(p, wir), getEngineSessionHandle());
                if (successful(result)) {
                    EventLogger.log(wir, (p != null) ? p.getID() : "",
                            EventLogger.event.complete);       // log it immediately

                    if (p != null) {
                        QueueSet qSet = p.getWorkQueues();
                        if (qSet != null) {
                            WorkQueue queue = qSet.getQueue(WorkQueue.STARTED);
                            if (queue != null) queue.remove(wir);
                        }
                    }
                    freeSecondaryResources(wir);
                } else {
                    _cache.removeTaskCompleter(p, wir);

                    // trim the error message
                    result = trimCheckinErrorMessage(result);
                }
            }
        } catch (IOException ioe) {
            result = fail("checkinItem method caused IO Exception");
            _log.error(result, ioe);
        } catch (JDOMException jde) {
            result = fail("checkinItem method caused JDOM Exception");
            _log.error(result, jde);
        }
        return result;
    }


    private void freeSecondaryResources(WorkItemRecord wir) {
        ResourceMap rMap = getResourceMap(wir);
        if (rMap != null) {
            rMap.getSecondaryResources().disengage(wir);         // mark SR's as released
        }
    }


    private void freeSecondaryResourcesForCase(String caseID) {
        for (Object o : LogMiner.getInstance().getBusyResourcesForCase(caseID)) {
            ResourceEvent row = (ResourceEvent) o;
            WorkItemRecord wir = _workItemCache.get(row.get_itemID());
            if (wir != null) {
                freeSecondaryResources(wir);
            }
        }
    }


    public SecondaryResources getSecondaryResources(WorkItemRecord wir) {
        ResourceMap rMap = getResourceMap(wir);
        return (rMap != null) ? rMap.getSecondaryResources() : null;
    }


    private String trimCheckinErrorMessage(String msg) {
        int start = msg.indexOf("XQuery [");
        int end = msg.lastIndexOf("Validation error message");
        if ((start > -1) && (end > -1)) {
            msg = msg.substring(0, start) + msg.substring(end);
        }
        return msg;
    }


    private String getCompletionLogPredicate(Participant p, WorkItemRecord wir) {
        String decompPredicate = parseCompletionLogPredicate(p, wir);
        if (wir.isDocumentationChanged()) {
            YLogDataItem docoItem = new YLogDataItem("Predicate", "Documentation",
                    wir.getDocumentation(), "string");
            YLogDataItemList itemList = new YLogDataItemList(docoItem);
            if (decompPredicate != null) {
                YLogDataItem decompItem = new YLogDataItem("Predicate", "Complete",
                        decompPredicate, "string");
                itemList.add(decompItem);
            }
            return itemList.toXML();
        }
        return decompPredicate;
    }


    private String getStartingLogPredicate(Participant p, WorkItemRecord wir) {
         return parseLogPredicate(p, wir, wir.getLogPredicateStarted());
    }


    private String parseCompletionLogPredicate(Participant p, WorkItemRecord wir) {
        return parseLogPredicate(p, wir, wir.getLogPredicateCompletion());
    }


    private String parseLogPredicate(Participant p, WorkItemRecord wir, String predicate) {
        return (predicate != null) ? new LogPredicateParser(p, wir).parse(predicate) : null;
    }

    //***************************************************************************//

    /**
     * Checks out all the child workitems of the parent item specified
     *
     * @param wir - the parent wir object
     */
    protected List checkOutChildren(WorkItemRecord wir, List children) {

        for (int i = 0; i < children.size(); i++) {
            WorkItemRecord itemRec = (WorkItemRecord) children.get(i);

            // if its 'fired' check it out
            if (WorkItemRecord.statusFired.equals(itemRec.getStatus()))
                checkOutWorkItem(itemRec, null);
        }

        // update child item list after checkout (to capture status changes) & return
        return getChildren(wir.getID());
    }


    public WorkItemRecord getExecutingChild(WorkItemRecord parent) {
        return getExecutingChild(getChildren(parent.getID()));
    }


    public WorkItemRecord getEngineStoredWorkItem(String wirID) throws IOException {
        return getEngineStoredWorkItem(wirID, getEngineSessionHandle());
    }


    private WorkItemRecord getExecutingChild(List<WorkItemRecord> children) {
        for (WorkItemRecord itemRec : children) {

            // find the one that's executing
            if (WorkItemRecord.statusExecuting.equals(itemRec.getStatus()))
                return itemRec;
        }
        return null;
    }


    /**
     * **********************
     * 9. CONNECTION METHODS *
     * **********************
     */

    public String login(String userid, String password, String jSessionID) {
        if (userid.equals(ADMIN_STR)) return loginAdmin(password, jSessionID);

        String result;
        Participant p = getParticipantFromUserID(userid);
        if (p != null) {
            boolean validPassword;
            if (_orgDataSet.isUserAuthenticationExternal()) {
                try {
                    validPassword = _orgdb.authenticate(userid, password);
                } catch (YAuthenticationException yae) {
                    return fail(yae.getMessage());
                }
            } else {
                validPassword = p.isValidPassword(password);
            }
            if (validPassword) {
                result = newSessionHandle();
                _cache.addSession(result, p, jSessionID);
                EventLogger.audit(userid, EventLogger.audit.logon);
            } else {
                result = fail(INVALID_LOGON_ERR);
                EventLogger.audit(userid, EventLogger.audit.invalid);
            }
        } else {
            result = fail(INVALID_LOGON_ERR);
            EventLogger.audit(userid, EventLogger.audit.unknown);
        }
        return result;
    }


    private String newSessionHandle() {
        return UUID.randomUUID().toString();
    }


    private String loginAdmin(String password, String jSessionID) {
        String handle;
        if (!_cache.hasClientCredentials()) setAuthorisedServiceConnections();
        String adminPassword = _cache.getClientPassword(ADMIN_STR);
        if (adminPassword == null) {
            adminPassword = _services.getAdminUserPassword();    // from engine
        }
        if (successful(adminPassword)) {
            if (password.equals(adminPassword)) {
                handle = newSessionHandle();
                _cache.addSession(handle, jSessionID);
                EventLogger.audit(ADMIN_STR, EventLogger.audit.logon);
            } else {
                handle = fail(INVALID_LOGON_ERR);
                EventLogger.audit(ADMIN_STR, EventLogger.audit.invalid);
            }
        } else handle = adminPassword;     // an error message

        return handle;
    }

    // removes session handle from map of live users
    public void logout(String handle) {
        _cache.logout(handle);
    }

    public boolean isValidUserSession(String handle) {
        return _cache.isValidUserSession(handle);
    }

    public boolean isAdminSession(String handle) {
        Participant p = _cache.getParticipantWithSessionHandle(handle);

        // p == null if 'admin' logon for handle
        return _cache.isValidUserSession(handle) && (p == null || p.isAdministrator());
    }

    public UserPrivileges getSessionPrivileges(String handle) {
        if (! _cache.isValidUserSession(handle)) return null;

        Participant p = _cache.getParticipantWithSessionHandle(handle);
        return p != null ? p.getUserPrivileges() : null;
    }


    public String validateUserCredentials(String userid, String password, boolean admin) {
        String result = "<success/>";
        if (userid.equals(ADMIN_STR)) {
            String adminPassword = _services.getAdminUserPassword();
            if (successful(adminPassword)) {
                if (!password.equals(adminPassword)) {
                    result = fail(INVALID_LOGON_ERR);
                }
            } else result = adminPassword;

            return result;
        }

        Participant p = getParticipantFromUserID(userid);
        if (p != null) {
            if (p.getPassword().equals(password)) {
                if (admin && !p.isAdministrator()) {
                    result = fail("Administrative privileges required.");
                }
            } else result = fail(INVALID_LOGON_ERR);
        } else result = fail("Unknown user name");

        return result;
    }


    public Participant expireSession(String jSessionID) {
        return _cache.expireSession(jSessionID);
    }


    public boolean isActiveSession(String jSessionID) {
        return _cache.isActiveSession(jSessionID);
    }

    public int getUserSessionCount() { return _cache.getUserSessionCount(); }

    public int getAdminSessionCount() { return _cache.getAdminSessionCount(); }

    public int getUserAdminSessionCount() { return _cache.getUserAdminSessionCount(); }

    /**
     * ***************************************************************************
     */

    public void shutdown() {
        try {
            _cache.shutdown();
            _persister.closeDB();
            if (_orgDataRefresher != null) _orgDataRefresher.cancel();
            _workItemCache.stopCleanserThread();
        } catch (Exception e) {
            _log.error("Unsuccessful audit log update on shutdown.");
        }
    }

    public String serviceConnect(String userid, String password, long timeOutSeconds) {
        if (!_cache.hasClientCredentials()) setAuthorisedServiceConnections();
        return _cache.connectClient(userid, password, timeOutSeconds);
    }

    public void serviceDisconnect(String handle) {
        _cache.serviceDisconnect(handle);
        removeCalendarStatusChangeListeners(handle);
    }

    public boolean checkServiceConnection(String handle) {
        return _cache.checkServiceConnection(handle);
    }

    public String getUserIDForSessionHandle(String handle) {
        return _cache.getUserIDForSessionHandle(handle);
    }


    public Set<SpecificationData> getLoadedSpecs() {
        Set<SpecificationData> result = getSpecList();
        if (result != null) {
            for (SpecificationData specData : result) {
                if (!specData.getStatus().equals(YSpecification._loaded))
                    result.remove(specData);
            }
        }
        return result;
    }


    public Set<SpecificationData> getSpecList() {
        try {
            return new HashSet<SpecificationData>(
                    getSpecificationPrototypesList(getEngineSessionHandle()));
        } catch (IOException ioe) {
            _log.error("IO Exception retrieving specification list", ioe);
            return null;
        }
    }

    public SpecificationData getSpecData(YSpecificationID spec) {
        SpecificationData result = _cache.getSpecificationData(spec);
        if (result == null) {
            try {
                result = getSpecificationData(spec, getEngineSessionHandle());
                if (result != null) _cache.addSpecificationData(result);
            } catch (IOException ioe) {
                _log.error("IO Exception retrieving specification data", ioe);
                result = null;
            }
        }
        return result;
    }


    public boolean isSpecBetaVersion(WorkItemRecord wir) {
        SpecificationData specData = getSpecData(new YSpecificationID(wir));
        return (specData != null) && specData.getSchemaVersion().isBetaVersion();
    }


    /**
     * Cancels the case & removes its workitems (if any) from the service's queues
     * & caches. Note: this method is synchronised to prevent any clash with
     * 'handleCancelledCaseEvent', which is triggered by the call to cancelCase in
     * this method.
     *
     * @param caseID the case to cancel
     * @return a message from the engine indicating success or otherwise
     * @throws IOException if there's trouble talking to the engine
     */
    public String cancelCase(String caseID, String userHandle) throws IOException {
        synchronized (_ibEventMutex) {
            List<WorkItemRecord> liveItems = _services.getLiveWorkItemsForCase(caseID);
            YSpecificationID specID = null;                           // for logging only

            // cancel the case in the engine
            String result = _services.cancelCase(caseID);

            // remove live items for case from workqueues and cache
            if (successful(result)) {
                if (liveItems != null) {
                    for (WorkItemRecord wir : liveItems) {
                        if (specID == null) specID = new YSpecificationID(wir);
                        if (removeFromAll(wir)) {
                            freeSecondaryResources(wir);
                        }
                        EventLogger.log(wir, null, EventLogger.event.cancelled_by_case);
                    }
                    _cache.removeChainedCase(caseID);
                }

                // log the cancellation
                Participant p = _cache.getParticipantWithSessionHandle(userHandle);
                String pid = (p != null) ? p.getID() : ADMIN_STR;
                EventLogger.log(specID, caseID, pid, false);
            } else {
                _log.error("Error attempting to cancel case {}: {}",
                        caseID, StringUtil.unwrap(result));
            }

            return result;
        }
    }


    public List<WorkItemRecord> getChildren(String parentID) {
        try {
            return getChildren(parentID, getEngineSessionHandle());
        } catch (IOException ioe) {
            return null;
        }
    }

    public Set<WorkItemRecord> getChildrenFromCache(String parentID) {
        Set<WorkItemRecord> cachedChildren = new HashSet<WorkItemRecord>();
        List<WorkItemRecord> children = getChildren(parentID);
        if (children != null) {
            for (WorkItemRecord child : children) {
                WorkItemRecord cachedChild = _workItemCache.get(child.getID());
                if (cachedChild != null) {
                    cachedChildren.add(cachedChild);
                } else {
                    cachedChildren.add(child);
                }
            }
        }
        return cachedChildren;
    }

    public String unloadSpecification(YSpecificationID specID) throws IOException {
        String result = _services.unloadSpecification(specID);
        if (successful(result)) {
            _resMapCache.remove(specID);
            _cache.removeSpecification(specID);
            getIBCache().unloadSpecificationData(specID);
            deletePersistedPiledTasks(specID);  // for persisted tasks with unloaded maps
        }
        return result;
    }


    public String launchCase(YSpecificationID specID, String caseData, String handle)
            throws IOException {
        String caseID = _services.launchCase(specID, caseData, getLaunchLogData());
        if (successful(caseID)) {
            EventLogger.log(specID, caseID, _cache.getWhoLaunchedCase(handle), true);
        }
        return caseID;
    }


    public String launchCase(YSpecificationID specID, String caseData, String handle,
                             long delay) throws IOException {
        String result = _services.launchCase(specID, caseData, getLaunchLogData(), delay);
        if (successful(result)) {
            _cache.addDelayedCaseLaunch(new DelayedLaunchRecord(specID,
                    _cache.getWhoLaunchedCase(handle), delay));
        }
        return result;
    }

    public String launchCase(YSpecificationID specID, String caseData, String handle,
                             Date delay) throws IOException {
        String result = _services.launchCase(specID, caseData, getLaunchLogData(), delay);
        if (successful(result)) {
            _cache.addDelayedCaseLaunch(new DelayedLaunchRecord(specID,
                    _cache.getWhoLaunchedCase(handle), delay));
        }
        return result;
    }

    public String launchCase(YSpecificationID specID, String caseData, String handle,
                             Duration delay) throws IOException {
        String result = _services.launchCase(specID, caseData, getLaunchLogData(), delay);
        if (successful(result)) {
            _cache.addDelayedCaseLaunch(new DelayedLaunchRecord(specID,
                    _cache.getWhoLaunchedCase(handle), delay));
        }
        return result;
    }


    private YLogDataItemList getLaunchLogData() {
        return new YLogDataItemList(
                new YLogDataItem("launched", "name", "resourceService", "string"));
    }


    public String getTaskParamsAsXML(String itemID) throws IOException {
        WorkItemRecord wir = _workItemCache.get(itemID);
        if (wir != null) {
            return _services.getTaskParamsAsXML(new YSpecificationID(wir), wir.getTaskID());
        } else return fail(WORKITEM_ERR + ": " + itemID);
    }


    public String getOutputOnlyTaskParamsAsXML(String itemID) {
        String result;
        WorkItemRecord wir = _workItemCache.get(itemID);
        if (wir != null) {
            try {
                TaskInformation taskInfo = getTaskInformation(wir);
                List<YParameter> list = taskInfo.getParamSchema().getOutputOnlyParams();
                result = "<outputOnlyParameters>\n";
                for (YParameter param : list) {
                    result += param.toSummaryXML();
                }
                result += "\n</outputOnlyParameters>";
            } catch (IOException ioe) {
                result = fail("Exception connecting to Engine.");
            }
        } else result = fail(WORKITEM_ERR + " '" + itemID + "'.");

        return result;
    }


    public Map<String, FormParameter> getWorkItemParamsInfo(WorkItemRecord wir)
            throws IOException, JDOMException {
        Map<String, FormParameter> inputs, outputs;
        TaskInformation taskInfo = getTaskInformation(wir);
        // map the params
        inputs = mapParamList(taskInfo.getParamSchema().getInputParams());
        outputs = mapParamList(taskInfo.getParamSchema().getOutputParams());

        // if param is only in input list, mark it as input-only
        for (String name : inputs.keySet()) {
            if (!outputs.containsKey(name)) {
                inputs.get(name).setInputOnly(true);
            }
        }

        // combine the two maps
        if (outputs != null)
            outputs.putAll(inputs);
        else
            outputs = inputs;

        // now map data values to params
        Element itemData;
        if (wir.isEdited()) {
            wir = _workItemCache.get(wir.getID());       // refresh data list if required
            itemData = wir.getUpdatedData();
        } else
            itemData = JDOMUtil.stringToElement(wir.getDataListString());

        for (String name : outputs.keySet()) {
            Element data = itemData.getChild(name);
            if (data != null) {
                if (data.getContentSize() > 0)         // complex type
                    outputs.get(name).setValue(JDOMUtil.elementToStringDump(data));
                else                                   // simple type
                    outputs.get(name).setValue(itemData.getText());
            }
        }

        return outputs;
    }

    private Map<String, FormParameter> mapParamList(List<YParameter> params) {
        Map<String, FormParameter> result = new HashMap<String, FormParameter>();
        for (YParameter param : params) {
            FormParameter fp = new FormParameter(param);
            result.put(param.getName(), fp);
        }
        return result;
    }

    private WorkItemRecord refreshWIRFromEngine(WorkItemRecord wir) {
        try {
            wir = getEngineStoredWorkItem(wir.getID(), getEngineSessionHandle());
            if (wir != null) _workItemCache.update(wir);
            return wir;
        } catch (Exception e) {
            return wir;
        }
    }


    public void announceModifiedQueue(String pid) {
//        if (_jsfApplicationReference != null) {
//            _jsfApplicationReference.refreshUserWorkQueues(pid);
//        }
    }


    public String addRegisteredService(YAWLServiceReference service) throws IOException {
        String result = _services.addRegisteredService(service);
        if (successful(result)) {
            _cache.addClientCredentials(service.getServiceName(), service.getServicePassword());
        }
        return result;
    }


    public String removeRegisteredService(String id) throws IOException {
        String result = _services.removeRegisteredService(id);
        if (successful(result)) {
            _cache.deleteClientCredentials(id);
        }
        return result;
    }


    public String addExternalClient(YExternalClient client) throws IOException {
        String result = _services.addExternalClient(client);
        if (successful(result)) {
            _cache.addClientCredentials(client.getUserName(),
                    PasswordEncryptor.encrypt(client.getPassword(), ""));
        }
        return result;
    }


    public String removeExternalClient(String id) throws IOException {
        String result = _services.removeExternalClient(id);
        if (successful(result)) {
            _cache.deleteClientCredentials(id);
        }
        return result;
    }


    public String updateExternalClient(String id, String password, String doco)
            throws IOException {
        String result = _services.updateExternalClient(id, password, doco);
        if (successful(result)) {
            _cache.updateClientCredentials(id, PasswordEncryptor.encrypt(password, ""));
        }
        return result;
    }


    // any services or client apps authorised to connect to the engine may connect
    // to this service too.
    private void setAuthorisedServiceConnections() {
        Map<String, String> users = new Hashtable<String, String>();
        try {
            Set<YExternalClient> clients = _services.getExternalClients();
            if (clients != null) {
                for (YExternalClient client : clients) {
                    users.put(client.getUserName(), client.getPassword());
                }
            }
            Set<YAWLServiceReference> services = _services.getRegisteredServices();
            if (services != null) {
                for (YAWLServiceReference service : services) {
                    users.put(service.getServiceName(), service.getServicePassword());
                }
            }
            _cache.refreshClientCredentials(users);
        } catch (IOException ioe) {
            _log.error("IO Exception getting valid service-level users from engine.");
        }
    }


    public String getNetParamValue(String caseID, String paramName) throws IOException {
        String caseData = _services.getCaseData(caseID);
        Element eData = JDOMUtil.stringToElement(caseData);
        return (eData != null) ? eData.getChildText(paramName) : null;
    }


    public String getDataSchema(YSpecificationID specID) {
        String result = null;
        try {
            Map<String, Element> schemaMap = getSpecificationDataSchema(specID);
            SpecificationData specData = getSpecData(specID);
            result = new DataSchemaBuilder(schemaMap).build(specData);
        } catch (Exception e) {
            _log.error("Could not retrieve schema for case parameters", e);
        }
        return result;
    }


    public String getDataSchema(String itemID) {
        WorkItemRecord wir = _workItemCache.get(itemID);
        if (wir != null) {
            return getDataSchema(wir, new YSpecificationID(wir));
        } else return fail(WORKITEM_ERR + " '" + itemID + "'.");
    }


    public String getDataSchema(WorkItemRecord wir, YSpecificationID specID) {
        String result = null;
        try {
            Map<String, Element> schemaMap = getSpecificationDataSchema(specID);
            TaskInformation taskInfo = getTaskInformation(specID, wir.getTaskID());
            result = new DataSchemaBuilder(schemaMap).build(taskInfo);
        } catch (Exception e) {
            _log.error("Could not retrieve schema for workitem parameters", e);
        }
        return result;
    }


    public Map<String, Element> getSpecificationDataSchema(YSpecificationID specID)
            throws IOException {
        Map<String, Element> schemaMap = _cache.getDataSchemaMap(specID);
        if (schemaMap == null) {
            String schema = _services.getSpecificationDataSchema(specID);
            if (schema != null) {
                schemaMap = _cache.addDataSchema(specID, schema);
            }
        }
        return schemaMap;
    }


    public boolean assignUnofferedItem(WorkItemRecord wir, String[] pidList,
                                       String action) {
        boolean result = true;
        if (wir != null) {
            Set<Participant> pSet = pidListToSet(pidList);
            Participant allocated = allocateUnofferedItem(pSet, wir);
            if (allocated == null) {
                return false;                  // pSet is empty
            }
            _workItemCache.updateResourceStatus(wir, WorkItemRecord.statusResourceOffered);

            // admin start overrides all
            if (action.equals("Start")) {
                String status = wir.getStatus();
                if (status.equals(WorkItemRecord.statusEnabled) ||
                        status.equals(WorkItemRecord.statusFired)) {
                    result = start(allocated, wir);
                } else {
                    _log.error("Unable to start workitem due to invalid status: " + status);
                    result = false;
                }

                // if could not start, fallback to allocate action
                if (!result) acceptOffer(allocated, wir);
            }

            // if an admin offer and a user allocation, offer to the set
            else if (action.equals("Offer") && !isSystemAllocated(wir)) {

                // an offer can be made to several participants
                for (Participant p : pSet) {
                    p.getWorkQueues().addToQueue(wir, WorkQueue.OFFERED);
                    addToOfferedSet(wir, p);
                }
            }

            // else if an admin offer and a system allocate, or an admin allocate
            else {
                acceptOffer(allocated, wir);
            }

            _resAdmin.removeFromUnoffered(wir);
        }
        return result;
    }


    private boolean isSystemAllocated(WorkItemRecord wir) {
        ResourceMap rMap = getResourceMap(wir);
        return (rMap != null && rMap.getAllocateInteraction().getInitiator() ==
                 AbstractInteraction.SYSTEM_INITIATED);
    }


    private Set<Participant> pidListToSet(String[] pidList) {
        if (pidList == null || pidList.length == 0) {
            return Collections.emptySet();
        }
        Set<Participant> pSet = new HashSet<Participant>();
        for (String pid : pidList) {
            Participant p = _orgDataSet.getParticipant(pid);
            if (p != null) {
                pSet.add(p);
            }
        }
        return pSet;
    }


    private Participant allocateUnofferedItem(Set<Participant> pSet, WorkItemRecord wir) {
        ResourceMap rMap = getResourceMap(wir);
        if (rMap != null) {
            AllocateInteraction interaction = rMap.getAllocateInteraction();
            if (interaction.getAllocator() != null) {
                return interaction.performAllocation(pSet, wir);
            }
        }
        return new RandomChoice().performAllocation(pSet, wir);
    }


    public void addToOfferedSet(WorkItemRecord wir, Participant p) {
        ResourceMap rMap = getResourceMap(wir);
        if (rMap != null) rMap.addToOfferedSet(wir, p);
    }


    public void reassignWorklistedItem(WorkItemRecord wir, String[] pidList,
                                       String action) {
        if (pidList == null || pidList.length == 0) {
            return;                           // no-one to reassign item to
        }
        removeFromAll(wir);

        // a reoffer can be made to several participants
        if (action.equals("Reoffer")) {
            ResourceMap rMap = getResourceMap(wir);
            if (rMap != null) {
                if (wir.getResourceStatus().equals(WorkItemRecord.statusResourceOffered)) {
                    withdrawOffer(rMap, wir);
                }
                for (String pid : pidList) {
                    Participant p = _orgDataSet.getParticipant(pid);
                    if (p != null) {
                        rMap.addToOfferedSet(wir, p);
                        p.getWorkQueues().addToQueue(wir, WorkQueue.OFFERED);
                    }
                }
            }
            wir.resetDataState();
            wir.setResourceStatus(WorkItemRecord.statusResourceOffered);
        } else {
            // a reallocate or restart is made to exactly one participant
            Participant p = _orgDataSet.getParticipant(pidList[0]);
            if (action.equals("Reallocate")) {
                wir.resetDataState();
                wir.setResourceStatus(WorkItemRecord.statusResourceAllocated);
                p.getWorkQueues().addToQueue(wir, WorkQueue.ALLOCATED);
            } else if (action.equals("Restart")) {
                if (wir.getStatus().equals(WorkItemRecord.statusEnabled))
                    start(p, wir);
                else {
                    p.getWorkQueues().addToQueue(wir, WorkQueue.STARTED);
                    wir.setResourceStatus(WorkItemRecord.statusResourceStarted);
                }
            }
        }
        _workItemCache.update(wir);
    }


    private void handleAutoTask(WorkItemRecord wir, boolean timedOut) {

        // if this autotask has started a timer, don't process now - wait for timeout
        if ((!timedOut) && (wir.getTimerTrigger() != null)) return;

        synchronized (_autoTaskMutex) {

            // check out the auto workitem
            if (checkOutWorkItem(wir, null)) {
                List children = getChildren(wir.getID());

                if ((children != null) && (!children.isEmpty())) {
                    wir = (WorkItemRecord) children.get(0);  // get executing child
                    EventLogger.logAutoTask(wir, true);             // log item start
                    processAutoTask(wir, true);
                }
            } else _log.error("Could not check out automated workitem: " + wir.getID());
        }
    }


    private void processAutoTask(WorkItemRecord wir, boolean init) {
        String codelet = wir.getCodelet();
        if (StringUtil.isNullOrEmpty(codelet)) {
            checkInAutoTask(wir, wir.getDataList());     // check in immediately
            return;
        }

        // wir has a codelet, so execute it in its own thread
        try {
            TaskInformation taskInfo = getTaskInformation(wir);
            if (taskInfo == null) {
                _log.error("Could not run codelet '{}' for workitem '{}' - error " +
                                "getting task information from engine. Codelet ignored.",
                        codelet, wir.getID());
                checkInAutoTask(wir, wir.getDataList());     // check in immediately
                return;
            }

            CodeletRunner runner = new CodeletRunner(wir, taskInfo, init, _persisting);
            new Thread(runner, wir.getID() + ":codelet").start();  // will callback when done
            _cache.addCodeletRunner(wir.getID(), runner);
        }
        catch (Exception e) {
            _log.error("Exception attempting to execute automatic task: " +
                    wir.getID(), e);
        }
    }


    public void unpersistAutoTask(WorkItemRecord wir) {
        PersistedAutoTask task = (PersistedAutoTask) _persister.selectScalar(
                "PersistedAutoTask", wir.getID());
        if (task != null) task.unpersist();
        _persister.commit();
    }


    private void restoreAutoTasks() {
        if (_persisting) {
            List tasks = _persister.select("PersistedAutoTask");
            for (Object o : tasks) {
                PersistedAutoTask task = (PersistedAutoTask) o;
                WorkItemRecord wir = task.getWIR();
                if (wir != null) {
                    unpersistAutoTask(wir);             // remove persisted
                    processAutoTask(wir, false);    // resume processing task
                }
            }
            _persister.commit();
        }
    }


    // callback method from CodeletRunner when codelet execution completes
    public void handleCodeletCompletion(WorkItemRecord wir, Element codeletResult) {
        if (_cache.removeCodeletRunner(wir.getID()) != null) {
            if (codeletResult != null) {
                codeletResult = updateOutputDataList(wir.getDataList(), codeletResult);
            }
            Element outData = (codeletResult != null) ? codeletResult : wir.getDataList();
            checkInAutoTask(wir, outData);
        } else {
            _log.warn("A codelet has completed for a non-existent workitem '{}' - it " +
                    "was most likely cancelled during the codelet's execution.", wir.getID());
            if (_persisting) unpersistAutoTask(wir);
        }
    }


    public void checkInAutoTask(WorkItemRecord wir, Element outData) {
        checkCacheForWorkItem(wir);     // won't be cached if this is a restored item
        try {
            if (_persisting) unpersistAutoTask(wir);
            String msg = checkInWorkItem(wir.getID(), wir.getDataList(),
                    outData, null, getEngineSessionHandle());
            if (successful(msg)) {
                EventLogger.logAutoTask(wir, false);             // log item completion
                _log.info("Automated task '{}' successfully processed and checked back " +
                        "into the engine.", wir.getID());
            } else
                _log.error("Automated task '{}' could not be successfully completed. " +
                        "Result message: {}", wir.getID(), msg);
        } catch (Exception e) {
            _log.error("Exception attempting to check-in automatic task: " +
                    wir.getID(), e);
        }
    }


    /**
     * updates the input datalist with the changed data in the output datalist
     *
     * @param in  - the JDOM Element containing the input params
     * @param out - the JDOM Element containing the output params
     * @return a JDOM Element with the data updated
     */
    private Element updateOutputDataList(Element in, Element out) {

        // get a copy of the 'in' list
        Element result = in.clone();

        // for each child in 'out' list, get its value and copy to 'in' list
        for (Element e : out.getChildren()) {

            // if there's a matching 'in' data item, update its value
            Element resData = result.getChild(e.getName());
            if (resData != null) {
                if (resData.getContentSize() > 0) resData.setContent(e.cloneContent());
                else resData.setText(e.getText());
            } else {
                result.addContent(e.clone());
            }
        }

        return result;
    }


    public String getMIFormalInputParamName(WorkItemRecord wir) {
        String result = null;
        if (canAddNewInstance(wir)) {
            try {
                TaskInformation taskInfo = getTaskInformation(wir);
                YParameter formalInputParam = taskInfo.getParamSchema().getFormalInputParam();
                if (formalInputParam != null) {
                    result = formalInputParam.getName();
                }
            } catch (IOException ioe) {
                // nothing to do
            }
        }
        return result;
    }


    private TaskInformation getTaskInformation(WorkItemRecord wir) throws IOException {
        return getTaskInformation(new YSpecificationID(wir), wir.getTaskID());
    }

    private TaskInformation getTaskInformation(YSpecificationID specID, String taskID)
            throws IOException {
        return getTaskInformation(specID, taskID, getEngineSessionHandle());
    }


    public boolean canAddNewInstance(WorkItemRecord wir) {
        return _services.canAddNewInstance(wir);
    }


    public WorkItemRecord createNewWorkItemInstance(String id, String value) {
        WorkItemRecord newWIR = _services.createNewWorkItemInstance(id, value);
        if (newWIR != null) {
            _workItemCache.add(newWIR);
        }
        return newWIR;
    }


    public String getWorkItemDurationsForParticipant(YSpecificationID specID,
                                                     String taskName, String pid) {
        LogMiner miner = LogMiner.getInstance();
        return miner.getWorkItemDurationsForParticipant(specID, taskName, pid);
    }

    /**
     * Dispatches a work item to a YAWL Custom Service for handling.
     *
     * @param itemID      the id of the work to be redirected.
     * @param serviceName the name of the service to redirect it to
     * @return a success or diagnostic error message
     * @pre The item id refers to a work item that is currently in the list of items known
     * to the Resource Service, and the work item has enabled or fired status
     * @pre The service name refers to a service registered in the engine
     * @pre The service is up and running
     */
    public String redirectWorkItemToYawlService(String itemID, String serviceName) {
        WorkItemRecord wir = getWorkItemRecord(itemID);
        if (wir == null) return fail("Unknown work item: " + itemID);
        String result = _services.redirectWorkItemToYawlService(wir, serviceName);
        if (successful(result)) {
            removeFromAll(wir);
        } else if (result.startsWith(WORKITEM_ERR)) {
            result += ": " + itemID;
        }
        return result;
    }

    //***************************************************************************//

}                                                                                  
