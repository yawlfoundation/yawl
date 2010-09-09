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

package org.yawlfoundation.yawl.resourcing;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.yawlfoundation.yawl.authentication.YExternalClient;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.TaskInformation;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.engine.interfce.interfaceE.YLogGatewayClient;
import org.yawlfoundation.yawl.exceptions.YAWLException;
import org.yawlfoundation.yawl.exceptions.YAuthenticationException;
import org.yawlfoundation.yawl.logging.YLogDataItem;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.resourcing.allocators.AllocatorFactory;
import org.yawlfoundation.yawl.resourcing.calendar.ResourceCalendar;
import org.yawlfoundation.yawl.resourcing.codelets.AbstractCodelet;
import org.yawlfoundation.yawl.resourcing.codelets.CodeletFactory;
import org.yawlfoundation.yawl.resourcing.constraints.ConstraintFactory;
import org.yawlfoundation.yawl.resourcing.datastore.HibernateEngine;
import org.yawlfoundation.yawl.resourcing.datastore.PersistedAutoTask;
import org.yawlfoundation.yawl.resourcing.datastore.WorkItemCache;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.LogMiner;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.DataSource;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.DataSourceFactory;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.EmptyDataSource;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.ResourceDataSet;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;
import org.yawlfoundation.yawl.resourcing.filters.FilterFactory;
import org.yawlfoundation.yawl.resourcing.interactions.AbstractInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.StartInteraction;
import org.yawlfoundation.yawl.resourcing.jsf.ApplicationBean;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.FormParameter;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.UserPrivileges;
import org.yawlfoundation.yawl.resourcing.rsInterface.ConnectionCache;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayServer;
import org.yawlfoundation.yawl.resourcing.rsInterface.UserConnection;
import org.yawlfoundation.yawl.resourcing.rsInterface.UserConnectionCache;
import org.yawlfoundation.yawl.resourcing.util.*;
import org.yawlfoundation.yawl.schema.YDataValidator;
import org.yawlfoundation.yawl.util.*;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

/**
 * The ResourceManager singleton manages all aspects of the resource perspective,
 * including the loading & maintenance of the org model, and overseeing the distribution
 * of tasks to participants.
 *
 *  @author Michael Adams
 *  @date 03/08/2007
 */

public class ResourceManager extends InterfaceBWebsideController {

    // store of organisational resources and their attributes
    private ResourceDataSet _orgDataSet;

    // cache of 'live' workitems
    private WorkItemCache _workItemCache = new WorkItemCache();

    // map of userid -> participant id
    private Map<String,String> _userKeys = new Hashtable<String,String>();

    // a cache of connections directly to the service from client apps & services
    private ConnectionCache _connections = ConnectionCache.getInstance();

    // currently logged on participants
    private UserConnectionCache _liveSessions = new UserConnectionCache();

    // local cache of specifications: id -> SpecificationData
    private SpecDataCache _specCache = new SpecDataCache();

    // local cache of specification data schemas: id -> [name, schema defn. element]
    private DataSchemaCache _dataSchemaCache = new DataSchemaCache();

    // groups of items that are members of a deferred choice offering
    private Set<TaggedStringList> _deferredItemGroups = new HashSet<TaggedStringList>();

    // cases that have workitems chained to a participant: <caseid, Participant>
    private Map<String, Participant> _chainedCases = new Hashtable<String, Participant>();

    // cache of who completed tasks, for four-eyes and retain familiar use <caseid, cache>
    private Map<String, FourEyesCache> _taskCompleters =
            new Hashtable<String, FourEyesCache>();

    // map of workitem id -> CodeletRunner running codelet for it
    private Hashtable<String, CodeletRunner> _codeletRunners =
            new Hashtable<String, CodeletRunner>(); 

    // started workitems that have been restored for a no longer existing participant.
    // these are force-completed once start-up has completed
    private List<WorkItemRecord> _orphanedStartedItems ;

    private static ResourceManager _me ;                  // instance reference
    private ResourceAdministrator _resAdmin ;             // admin capabilities
    private DataSource _orgdb;                            // the org model db i'face
    private Persister _persister;                         // persist changes to db
    private Logger _log ;                                 // debug log4j file
    private ResourceCalendar _calendar;                   // resource availability
    private YBuildProperties _buildProps;                 // build version info
    private boolean _persisting ;                         // flag to enable persistence
    private boolean _isNonDefaultOrgDB ;                  // flag for non-yawl org model
    private final Object _mutex = new Object();           // for synchronizing ib events

    private Timer _orgDataRefreshTimer;               // if set, reloads db at intervals

    private boolean _serviceEnabled = true ;          // will disable if no participants
    private boolean _initCompleted = false;               // guard for restarted engine
    public static boolean serviceInitialised = false ;    // flag for init on restore

    private ApplicationBean _jsfApplicationReference ;   // ref to jsf app manager bean

    public boolean _logOffers ;
    private boolean _persistPiling ;
    private boolean _visualiserEnabled;

    // Mappings for specid -> version -> taskid <-> resourceMap
    private ResourceMapCache _resMapCache = new ResourceMapCache() ;

    // required data members for interfacing with the engine
    private String _engineUser = "DefaultWorklist" ;
    private String _enginePassword = "resource" ;
    private String _engineSessionHandle = null ;
    private String _serviceURI = null;
    private String _exceptionServiceURI = null ;
    private Namespace _yNameSpace =
            Namespace.getNamespace("http://www.yawlfoundation.org/yawlschema");

    // interface client references - IBClient is inherited from WebSideController
    private InterfaceA_EnvironmentBasedClient _interfaceAClient ;
    private YLogGatewayClient _interfaceEClient;
    private ResourceGatewayServer _gatewayServer;


    // Constructor - called exclusively by getInstance()
    private ResourceManager() {
        super();
        _resAdmin = ResourceAdministrator.getInstance() ;
        _log = Logger.getLogger(getClass());
        _me = this ;
    }

    /**
     * @return the instantiated ResourceManager reference  
     */
    public static ResourceManager getInstance() {
        if (_me == null) _me = new ResourceManager();
        return _me ;
    }


    /*********************************************************************************/

    // Initialisation methods //

    public void initOrgDataSource(String dataSourceClassName, int refreshRate) {
        _log.info("Loading org data...");

        // get correct ref to org data backend
        _orgdb = DataSourceFactory.getInstance(dataSourceClassName);

        if (_orgdb != null) {

            // set flag to true if the org model db backend is not the default
            _isNonDefaultOrgDB =
                 ! (_orgdb.getClass().getSimpleName().equalsIgnoreCase("HibernateImpl"));

            // load all org data into the resources dataset
            loadResources() ;

            // set refresh rate if required
            if (refreshRate > 0) startOrgDataRefreshTimer(refreshRate);
        }
        else
            _log.warn("Invalid Datasource: No dataset loaded. " +
                      "Check datasource settings in 'web.xml'") ;
    }

    
    public void initInterfaceClients(String engineURI, String exceptionURI) {
        _interfaceAClient = new InterfaceA_EnvironmentBasedClient(
                                                 engineURI.replaceFirst("/ib", "/ia"));
        _interfaceEClient = new YLogGatewayClient(
                                         engineURI.replaceFirst("/ib", "/logGateway"));
        if (exceptionURI != null) {
            _exceptionServiceURI = exceptionURI;
            _gatewayServer = new ResourceGatewayServer(exceptionURI + "/ix");
        }    
    }


    private void setServiceURI() {
        _serviceURI = "http://localhost:8080/resourceService/ib";         // a default
        Set<YAWLServiceReference> services =
                _interfaceAClient.getRegisteredYAWLServices(getEngineSessionHandle()) ;
        if (services != null) {
            for (YAWLServiceReference service : services) {
                if (service.getURI().contains("resourceService")) {
                    _serviceURI = service.getURI();
                }
            }
        }
    }


    private void reestablishInterfaceClients() {
        String uriA = _interfaceAClient.getBackEndURI();
        String uriB = _interfaceBClient.getBackEndURI();
        String uriE = _interfaceEClient.getBackEndURI();
        _interfaceAClient = new InterfaceA_EnvironmentBasedClient(uriA);
        setUpInterfaceBClient(uriB);
        _interfaceEClient = new YLogGatewayClient(uriE);
        HttpURLValidator.pingUntilAvailable(uriB, 5);
    }

    
    public String getServiceURI() {
        if (_serviceURI == null) setServiceURI();
        return _serviceURI;
    }


    public String getEngineXESLog(YSpecificationID specID, boolean withData) {
        try {
            return _interfaceEClient.getSpecificationXESLog(specID, withData,
                                                     getEngineSessionHandle());
        }
        catch (IOException ioe) {
            return null;
        }
    }

    public void initBuildProperties(InputStream stream) {
        _buildProps = new YBuildProperties();
        _buildProps.load(stream);
    }


    public YBuildProperties getBuildProperties() {
        return _buildProps;
    }


    public String getEngineBuildProperties() {
        try {
            return _interfaceAClient.getBuildProperties(getEngineSessionHandle());
        }
        catch (IOException ioe) {
            return "<failure>IO Exception retrieving engine build properties.</failure>";
        }
    }


    public synchronized void finaliseInitialisation() {
        EventLogger.setLogging(
            HibernateEngine.getInstance(false).isAvailable("ResourceEvent"));
        _workItemCache.setPersist(_persisting) ;
        if (_persisting) {
            restoreWorkQueues() ;
        }
        _calendar = ResourceCalendar.getInstance();
    }


    public void setAllowExternalOrgDataMods(boolean allow) {
        if (_orgdb != null) _orgDataSet.setAllowExternalOrgDataMods(allow);
    }

    public void setExternalUserAuthentication(boolean externalAuth) {
        if (_orgdb != null) _orgDataSet.setExternalUserAuthentication(externalAuth);
    }
    
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

    public WorkItemCache getWorkItemCache() { return _workItemCache ; }


    public void registerJSFApplicationReference(ApplicationBean app) {
        _jsfApplicationReference = app;
    }

    public boolean hasOrgDataSource() {
        return (_orgdb != null);
    }

    public boolean hasExceptionServiceEnabled() {
        return (_exceptionServiceURI != null);
    }

    public String getExceptionServiceURI() {
        return _exceptionServiceURI;
    }

    public Logger getLogger() { return _log ; }

    public ResourceCalendar getCalendar() { return _calendar ; }

    /*********************************************************************************/

    // Server methods (event announcements) //

    public void announceResourceUnavailable(WorkItemRecord wir) {
        try {
            if (_gatewayServer != null) _gatewayServer.announceResourceUnavailable(wir);
        }
        catch (IOException ioe) {
            _log.error("Failed to announce unavailable resource to environment", ioe);
        }
    }


    /*********************************************************************************/

    // Interface B implemented methods //

    public synchronized void handleEnabledWorkItemEvent(WorkItemRecord wir) {
        if (_serviceEnabled) {
            if (isAutoTask(wir)) {
                handleAutoTask(wir, false);
            }
            else {
                ResourceMap rMap = getResourceMap(wir) ;
                if (rMap != null)
                    wir = rMap.distribute(wir) ;
                else
                    wir = offerToAll(wir) ;   // only when no resourcing spec for item
            }
        }

        // service disabled, so route directly to admin's unoffered queue
        else _resAdmin.addToUnoffered(wir);

        if (wir.isDeferredChoiceGroupMember()) mapDeferredChoice(wir);

        // store all manually-resourced workitems in the local cache
        if (! isAutoTask(wir)) _workItemCache.add(wir);
    }


    public synchronized void handleCancelledWorkItemEvent(WorkItemRecord wir) {
        if (cleanupWorkItemReferences(wir)) {
            EventLogger.log(wir, null, EventLogger.event.cancel);
        }    
    }


    public void handleTimerExpiryEvent(WorkItemRecord wir) {
        if (isAutoTask(wir))
            handleAutoTask(wir, true);
        else {
            if (cleanupWorkItemReferences(wir)) {                // remove from worklists
                EventLogger.log(wir, null, EventLogger.event.timer_expired); 
            }
        }
    }


    public synchronized void handleCancelledCaseEvent(String caseID) {
        if (_serviceEnabled) {
            removeCaseFromAllQueues(caseID) ;                              // workqueues
            removeCaseFromTaskCompleters(caseID);
            cancelCodeletRunnersForCase(caseID);
            _workItemCache.removeCase(caseID);
            removeChain(caseID);
        }
    }


    public void handleCompletedWorkItemEvent(WorkItemRecord wir) {
        cleanupWorkItemReferences(wir);
    }


    public void handleCompleteCaseEvent(String caseID, String casedata) {
        handleCancelledCaseEvent(caseID);       // just for cleaning up purposes, if any
    }


    public synchronized void handleEngineInitialisationCompletedEvent() {

        // if the engine has been restarted during this service session
        if (_initCompleted) {
            _engineSessionHandle = null;
            reestablishInterfaceClients();
        }

        setServiceURI();
        sanitiseCaches();
        setAuthorisedServiceConnections();

        // if this is the first time the engine has started since service start...
        // (these things are only to be done once per service start)
        if (! _initCompleted) {
            restoreAutoTasks();
            if (_orphanedStartedItems != null) {
                for (WorkItemRecord wir : _orphanedStartedItems) {
                    this.checkinItem(null, wir);
                }
            }
            _initCompleted = true;
        }    
    }


    // here we are only interested in items the service knows about, being updated
    // by services other than this one
    public void handleWorkItemStatusChangeEvent(WorkItemRecord wir,
                                                String oldStatus, String newStatus) {
        WorkItemRecord cachedWir = _workItemCache.get(wir.getID());
        if (cachedWir != null) {

            // if its a status change this service didn't cause
            if (! newStatus.equals(cachedWir.getStatus())) {

                // if it has been 'finished', remove it from all queues
                if ((newStatus.equals(WorkItemRecord.statusComplete)) ||
                    (newStatus.equals(WorkItemRecord.statusDeadlocked)) ||
                    (newStatus.equals(WorkItemRecord.statusFailed)) ||
                    (newStatus.equals(WorkItemRecord.statusForcedComplete))) {

                    cleanupWorkItemReferences(cachedWir);

                }

                // if it has been 'suspended', find it on a 'started' queue & move it
                else if (newStatus.equals(WorkItemRecord.statusSuspended)) {
                    Participant p = getParticipantAssignedWorkItem(cachedWir, WorkQueue.STARTED);
                    if (p != null) {
                        p.getWorkQueues().movetoSuspend(wir);
                        cachedWir.setResourceStatus(WorkItemRecord.statusResourceSuspended);
                        cachedWir.setStatus(newStatus);
                        _workItemCache.update(wir);
                    }
                }

                // if it has moved to started status
                else if (newStatus.equals(WorkItemRecord.statusExecuting)) {

                    // ...and was previously suspended
                    if (cachedWir.hasStatus(WorkItemRecord.statusSuspended)) {
                        Participant p = getParticipantAssignedWorkItem(cachedWir, WorkQueue.SUSPENDED);
                        if (p != null) {
                            p.getWorkQueues().movetoUnsuspend(wir);
                            cachedWir.setResourceStatus(WorkItemRecord.statusResourceStarted);
                            cachedWir.setStatus(newStatus);
                            _workItemCache.update(wir);
                        }
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


    /**
     *  displays a web page describing the service
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
           throws IOException, ServletException {
        response.setContentType("text/html");
        ServletOutputStream outStream = response.getOutputStream();
        String root = Docket.getServiceRootDir() ;
        String fileName = root + "welcome.htm";

        // convert htm file to a byte array
        FileInputStream fStream = new FileInputStream (fileName);
        byte[] b = new byte[fStream.available()];
        fStream.read(b);
        fStream.close();

        // load the full welcome page if possible
        if (b.length > 0) outStream.write(b);
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


    private boolean cleanupWorkItemReferences(WorkItemRecord wir) {
        WorkItemRecord removed = null;
        if (_serviceEnabled) {
            removeFromAll(wir) ;                                      // workqueues
            removed = _workItemCache.remove(wir);
            ResourceMap rMap = getResourceMap(wir);
            if (rMap != null) rMap.removeIgnoreList(wir);
            cancelCodeletRunner(wir.getID());                         // if any
        }
        return (removed != null);
    }


    /*********************************************************************************/
    /*********************************************************************************/
    
    // GET SELECTOR METHODS - USED PRIMARILY BY THE RESOURCE GATEWAY //

    public Set getConstraints() {
        return ConstraintFactory.getConstraints() ;
    }

    public Set getFilters() {
        return FilterFactory.getFilters() ;
    }

    public Set getAllocators() {
        return AllocatorFactory.getAllocators() ;
    }

    public String getConstraintsAsXML() {
        Set constraints = getConstraints() ;
        StringBuilder result = new StringBuilder("<constraints>") ;
        result.append(getSelectors(constraints, "constraint")) ;
        result.append("</constraints>");
        return result.toString();
    }

    public String getFiltersAsXML() {
        Set filters = getFilters() ;
        StringBuilder result = new StringBuilder("<filters>") ;
        result.append(getSelectors(filters, "filter")) ;
        result.append("</filters>");
        return result.toString();
    }

    public String getAllocatorsAsXML() {
        Set allocators = getAllocators() ;
        StringBuilder result = new StringBuilder("<allocators>") ;
        result.append(getSelectors(allocators, "allocator")) ;
        result.append("</allocators>");
        return result.toString();
    }

    public String getAllSelectors() {
        StringBuilder xml = new StringBuilder("<selectors>") ;
        xml.append(getConstraintsAsXML());
        xml.append(getFiltersAsXML());
        xml.append(getAllocatorsAsXML());
        xml.append("</selectors>");
        return xml.toString();
    }

    public String getSelectors(Set<AbstractSelector> items, String tag) {
        StringBuilder result = new StringBuilder() ;
        for (AbstractSelector item : items) result.append(item.getInformation(tag));
        return result.toString();
    }

    public String getCodeletsAsXML() {
        Set<AbstractCodelet> codelets = getCodelets();
        StringBuilder result = new StringBuilder("<codelets>") ;
        for (AbstractCodelet codelet : codelets) result.append(codelet.toXML());
        result.append("</codelets>");
        return result.toString();

    }

    public Set<AbstractCodelet> getCodelets() {
        return CodeletFactory.getCodelets() ;
    }


    public List<YParameter> getCodeletParameters(String packageName, String codeletName) {
        if (packageName == null) return getCodeletParameters(codeletName);
        AbstractCodelet codelet = CodeletFactory.getInstance(packageName, codeletName);
        return (codelet != null) ? codelet.getRequiredParams() : null;
    }


    public List<YParameter> getCodeletParameters(String codeletName) {
        for (AbstractCodelet codelet : getCodelets()) {
            if (codelet.getClassName().equals(codeletName)) {
                return codelet.getRequiredParams();
            }
        }
        return null;
    }

    public String getCodeletParametersAsXML(String packageName, String codeletName) {
        if (packageName == null) return getCodeletParametersAsXML(codeletName);
        AbstractCodelet codelet = CodeletFactory.getInstance(packageName, codeletName);
        return (codelet != null) ? codelet.getRequiredParamsToXML() :
                "<failure>Could not locate codelet: '" + packageName + "." +
                        codeletName + "'.</failure>";
    }

    public String getCodeletParametersAsXML(String codeletName) {
        for (AbstractCodelet codelet : getCodelets()) {
            if (codelet.getClassName().equals(codeletName)) {
                return codelet.getRequiredParamsToXML();
            }
        }
        return "<failure>Could not locate codelet: '" + codeletName + "'.</failure>";
    }

   /******************************************************************************/

    // LOGGING METHODS //

    public void setOfferLogging(boolean log) { _logOffers = log ; }

   /******************************************************************************/

    // ORG DATA METHODS //

    public boolean isDefaultOrgDB() {
        return ! _isNonDefaultOrgDB;
    }

    public ResourceDataSet getOrgDataSet() {
        return _orgDataSet;
    }

    /** Loads all the org data from db into the ResourceDataSet mappings */
    public void loadResources() {
        if (_orgdb != null) {
            _orgDataSet = _orgdb.loadResources() ;

            // complete mappings for non-default org data backends
            if (_isNonDefaultOrgDB) finaliseNonDefaultLoad() ;

            // rebuild a work queue set and userid keymap for each participant
            for (Participant p : _orgDataSet.getParticipants()) {
                p.createQueueSet(_persisting) ;
                addUserKey(p) ;
            }

            _resAdmin.createWorkQueues(_persisting);   // ... and the administrator
        }
        else {
            _orgDataSet = new EmptyDataSource().getDataSource();
        }
    }

    /**
     * This does final initialisation tasks involved in ensuring the caches match
     * the engine's known work. It is called via the EngineInitialisationCompleted
     * event, because it needs the engine to be completely initialised first. It
     * may also be executed via the 'synch' button on the admin queues.
     */
    public void sanitiseCaches() {

        // check local cache = engine records
        try {
            List<WorkItemRecord> engineItems =
                  _interfaceBClient.getCompleteListOfLiveWorkItems(getEngineSessionHandle());
            List<String> engineIDs = new ArrayList<String>();
            List<WorkItemRecord> missingParents = new ArrayList<WorkItemRecord>();

            // check that a copy of each engine child item is stored locally
            for (WorkItemRecord wir : engineItems) {
                if (! _workItemCache.containsKey(wir.getID())) {
                    if (! wir.getStatus().equals(WorkItemRecord.statusIsParent)) {
                        _workItemCache.add(wir);
                        _log.warn("Engine workItem '" + wir.getID() +
                                  "' was missing from local cache and has been added.");
                    }
                    else {
                        missingParents.add(wir);
                    }
                }
                engineIDs.add(wir.getID());
            }

            // Parent items are treated differently. If they have never been started
            // they should be in the cache, otherwise at least one child will be cached
            for (WorkItemRecord wir : missingParents) {
                List children = getChildren(wir.getID());
                if ((children == null) || (children.isEmpty())) {            // no kids
                    _workItemCache.add(wir);
                    _log.warn("Engine workItem '" + wir.getID() +
                              "' was missing from local cache and has been added.");
                }
            }

            // now check that each item stored locally is also in engine
            Set<String> missingIDs = new HashSet<String>();
            for (String cachedID : _workItemCache.keySet()) {
                if (! engineIDs.contains(cachedID))
                    missingIDs.add(cachedID);
            }
            for (String missingID : missingIDs) {
                WorkItemRecord deadWir = _workItemCache.remove(missingID);
                removeFromAll(deadWir);                          // workqueues, that is
                _log.warn("Cached workitem '" + missingID +
                          "' did not exist in the Engine and was removed.");
            }
        }
        catch (IOException ioe) {
            _log.warn("Sanitise caches method could not get workitem list from engine.") ;
        }

        cleanseQueues();                       // removed any uncached items from queues

        // finally, rebuild all 'offered' datasets
       for (Participant p : _orgDataSet.getParticipants()) {
           QueueSet qSet = p.getWorkQueues();
           if (qSet != null) {
               WorkQueue wq = qSet.getQueue(WorkQueue.OFFERED);
               if (wq != null) {
                   Set<WorkItemRecord> wirSet = wq.getAll();
                   for (WorkItemRecord wir : wirSet) addToOfferedSet(wir, p);
               }
           }
       }
    }


    private void cleanseQueues() {
        for (Participant p : _orgDataSet.getParticipants()) {
            QueueSet qSet = p.getWorkQueues();
            if (qSet != null) {
                qSet.cleanseAllQueues(_workItemCache);
            }
        }
        WorkQueue q = _resAdmin.getWorkQueues().getQueue(WorkQueue.UNOFFERED);
        if (q != null) q.cleanse(_workItemCache);
    }


    private void finaliseNonDefaultLoad() {

        // for each entity set not supplied by the backend, load the service defaults.
        // At a minimum, the datasource must supply a set of participants
        if (_orgDataSet.getParticipants().isEmpty()) {
            _log.error("Participant set not loaded - service will disable.") ;
            _serviceEnabled = false ;
            return ;
        }

        // check other entities
        _orgDataSet.augmentDataSourceAsRequired();

        // restore user privileges for each participant
        Map<String,Object> upMap = _persister.selectMap("UserPrivileges");
        for (Participant p : _orgDataSet.getParticipants()) {
            UserPrivileges up = (UserPrivileges) upMap.get(p.getID());
            if (up != null) p.setUserPrivileges(up);
            else p.setUserPrivileges(new UserPrivileges(p.getID()));
        }
    }


    private void restoreWorkQueues() {
        _log.info("Restoring persisted work queue data...");
        _workItemCache.restore() ;
        List<WorkQueue> orphanedQueues = new ArrayList<WorkQueue>();

        // restore the queues to their owners
        List<WorkQueue> qList = _persister.select("WorkQueue") ;

        if (qList != null) {
            for (WorkQueue wq : qList) {
                wq.setPersisting(true);
                if (wq.getOwnerID().equals("admin"))
                    _resAdmin.restoreWorkQueue(wq, _workItemCache, _persisting);
                else {
                    if (_orgDataSet != null) {
                        Participant p = _orgDataSet.getParticipant(wq.getOwnerID()) ;
                        if (p != null) {
                            p.restoreWorkQueue(wq, _workItemCache, _persisting);
                        }
                        else {
                            orphanedQueues.add(wq);
                        }
                    }
                }
            }

            if (! orphanedQueues.isEmpty()) {
                removeOrphanedQueues(orphanedQueues);
            }
        }
    }


    private void removeOrphanedQueues(List<WorkQueue> orphanedQueues) {

        // have to restore the whole set first to avoid hibernate lazy refreshes
        for (WorkQueue orphan : orphanedQueues) {
            orphan.restore(_workItemCache);
        }

        for (WorkQueue orphan : orphanedQueues) {
            handleWorkQueueOnRemoval(orphan);
            orphan.clear();                        // del persisted items
            _persister.delete(orphan);             // del persisted queue
        }
    }



    private void addUserKey(Participant p) {
        _userKeys.put(p.getUserID(), p.getID()) ;
    }

    private void removeUserKey(Participant p) {
        removeUserKey(p.getUserID()) ;
    }

    private void removeUserKey(String userKey) {
        _userKeys.remove(userKey) ;
    }

    public boolean isKnownUserID(String userid) {
        return _userKeys.containsKey(userid);
    }

    private String fail(String msg) {
        return "<failure>" + msg + "</failure>";
    }

    // ADD (NEW) ORG DATA OBJECTS //

    /**
     * Adds a new participant to the Resource DataSet, and persists it also
     * @param p the new Participant
     */
    public String addParticipant(Participant p) {

        // check for userid uniqueness
        if (! isKnownUserID(p.getUserID())) {
            String newID = _orgDataSet.addParticipant(p);
            if (! newID.startsWith("<fail")) {
                p.createQueueSet(_persisting) ;

                // cleanup for non-default db
                if (_isNonDefaultOrgDB) {
                    p.setID(newID);
                    if (_persisting) {
                        _persister.insert(p.getUserPrivileges());
                        _persister.insert(p.getWorkQueues());
                    }
                }
                else _orgDataSet.updateParticipant(p);
        
                addUserKey(p);                                 // and the userid--pid map
            }
            return newID;
        }
        else return fail("User id '" + p.getUserID() + "' is already in use");
    }


    public void importParticipant(Participant p) {
        if (_orgDataSet.importParticipant(p)) {
            p.createQueueSet(_persisting) ;

            // cleanup for non-default db
            if (_isNonDefaultOrgDB) {
                if (_persisting) {
                    _persister.insert(p.getUserPrivileges());
                    _persister.insert(p.getWorkQueues());
                }
            }
            else _orgdb.update(p);

            addUserKey(p);                              // and the userid--pid map
        }
    }


    public void updateParticipant(Participant p) {
        if (_orgDataSet.updateParticipant(p)) {
            addUserKey(p);                                 // and the userid--pid map
            if (_isNonDefaultOrgDB) {
                _persister.update(p.getUserPrivileges());  // persist other classes
                _persister.update(p.getWorkQueues());
            }
        }
    }


    public synchronized void removeParticipant(Participant p) {
        if (_orgDataSet.removeParticipant(p)) {
            handleWorkQueuesOnRemoval(p);
            QueueSet qSet = p.getWorkQueues();
            qSet.purgeAllQueues();
            for (WorkQueue wq : qSet.getActiveQueues()) {
                if (wq != null) _persister.delete(wq);
            }    
            _persister.delete(p.getUserPrivileges());
            removeUserKey(p);
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
        StringBuilder xml = new StringBuilder("<participants>") ;
        for (Participant p : _liveSessions.getActiveParticipants()) {
            xml.append(p.toXML()) ;
        }
        xml.append("</participants>");
        return xml.toString() ;
    }


    public String getParticipantIDFromUserID(String userID) {
        return _userKeys.get(userID) ;
    }


    public Participant getParticipantFromUserID(String userID) {
        String pid = getParticipantIDFromUserID(userID) ;
        return _orgDataSet.getParticipant(pid);
    }


    public QueueSet getUserQueueSet(String userID) {
        Participant p = getParticipantFromUserID(userID) ;
        if (p != null)
           return p.getWorkQueues() ;
        else return null ;
    }


    public Set getUserQueuedItems(String userID, int queue) {
        QueueSet qs = getUserQueueSet(userID);
        if (qs != null)
           return qs.getQueuedWorkItems(queue) ;
        else return null ;
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
        if (userID.equals("admin")) return "Administrator" ;

        Participant p = getParticipantFromUserID(userID) ;
        return (p != null) ? p.getFullName() : null ;
    }


    public Namespace getNameSpace() {
        return _yNameSpace;
    }


    public String getSessionHandle(Participant p) {
        return _liveSessions.getSessionHandle(p);
    }

    
    public String getSessionHandle(String userid) {
        return getSessionHandle(getParticipantFromUserID(userid)) ;
    }


    /***************************************************************************/

    // WORKITEM ALLOCATION AND WORKQUEUE METHODS //

    public WorkItemRecord offerToAll(WorkItemRecord wir) {
        if (_orgDataSet.getParticipantCount() > 0) {
            wir.setResourceStatus(WorkItemRecord.statusResourceOffered);
            for (Participant p : _orgDataSet.getParticipants()) {
                p.getWorkQueues().addToQueue(wir, WorkQueue.OFFERED);
                announceModifiedQueue(p.getID()) ;
            }
        }
        else {
            wir.setResourceStatus(WorkItemRecord.statusResourceUnoffered);
            _resAdmin.addToUnoffered(wir);
        }
        _workItemCache.update(wir);
        return wir ;
    }

    
    public void withdrawOfferFromAll(WorkItemRecord wir) {
        for (Participant p : _orgDataSet.getParticipants()) {
            p.getWorkQueues().removeFromQueue(wir, WorkQueue.OFFERED);
            announceModifiedQueue(p.getID()) ;
        }
    }


    public void removeFromAll(WorkItemRecord wir) {
        for (Participant p : _orgDataSet.getParticipants()) {
            p.getWorkQueues().removeFromAllQueues(wir);
            announceModifiedQueue(p.getID()) ;
        }
        _resAdmin.removeFromAllQueues(wir);
    }


    public void removeCaseFromAllQueues(String caseID) {
        for (Participant p : _orgDataSet.getParticipants()) {
            p.getWorkQueues().removeCaseFromAllQueues(caseID);
            announceModifiedQueue(p.getID()) ;
        }
        _resAdmin.removeCaseFromAllQueues(caseID);
    }

    public QueueSet getAdminQueues() {
        return _resAdmin.getWorkQueues();
    }


    public WorkItemRecord acceptOffer(Participant p, WorkItemRecord wir) {
        StartInteraction starter = null;
        ResourceMap rMap = getResourceMap(wir);
        if (rMap != null) {
            rMap.withdrawOffer(wir);
            starter = rMap.getStartInteraction();
        }
        else
            withdrawOfferFromAll(wir);        // beta version spec

        // take the appropriate start action
        if ((starter != null) &&
            (starter.getInitiator() == AbstractInteraction.SYSTEM_INITIATED)) {
            startImmediate(p, wir);
            WorkItemRecord startedItem = getExecutingChild(getChildren(wir.getID()));
            if (startedItem != null) {
                WorkItemRecord cachedItem = _workItemCache.get(startedItem.getID());
                if (cachedItem != null) wir = cachedItem;
            }
        }
        else {

            // either start is user-initiated or there's no resource map (beta spec) 
            wir.setResourceStatus(WorkItemRecord.statusResourceAllocated);
            p.getWorkQueues().addToQueue(wir, WorkQueue.ALLOCATED);
         }

        // remove other wirs if this was a member of a deferred choice group
        if (wir.isDeferredChoiceGroupMember()) {
            withdrawDeferredChoiceGroup(wir, rMap) ;
        }

        _workItemCache.update(wir);
        return wir;
    }


    // DEFERRED CHOICE HANDLERS //

    private void mapDeferredChoice(WorkItemRecord wir) {
        String defID = wir.getDeferredChoiceGroupID() ;
        TaggedStringList itemGroup = getDeferredChoiceGroup(defID);
        if (itemGroup != null) {
            itemGroup.add(wir.getID());
        }
        else
            _deferredItemGroups.add(new TaggedStringList(defID, wir.getID())) ;
    }


    private void withdrawDeferredChoiceGroup(WorkItemRecord wir, ResourceMap rMap) {
        String chosenWIR = wir.getID();
        String groupID = wir.getDeferredChoiceGroupID();
        TaggedStringList itemGroup = getDeferredChoiceGroup(groupID);
        if (itemGroup != null) {
            for (String wirID : itemGroup) {
                if (! wirID.equals(chosenWIR)) {
                    if (rMap != null)
                        rMap.withdrawOffer(wir);
                    else
                        withdrawOfferFromAll(wir);        // beta version spec

                    _workItemCache.remove(wirID);
                }
            }
            _deferredItemGroups.remove(itemGroup) ;
        }
    }


    private TaggedStringList getDeferredChoiceGroup(String groupID) {
        TaggedStringList result = null ;
        for (TaggedStringList itemGroup : _deferredItemGroups) {
            if (groupID.equals(itemGroup.getTag())) {
                result = itemGroup;
                break;
            }
        }    
        return result ;
    }

    // Deals with live workitems in a participant's queues when the participant is
    // removed. An admin is advised to manually reallocate items before removing this p.,
    // but this is the default behaviour if there are still items in the queue.
    // The strategy is:
    //  - Offered: if this is the only p. that has received this offer, give it back to
    //             the admin for re-offering. If others have been offered the same item,
    //             there's nothing more to do.
    //  - Allocated: give it back to admin for reallocating
    //  - Started: forceComplete items (since we need another p. to reallocate to)
    //  - Suspended: same as Started.
    //

    public void handleWorkQueuesOnRemoval(Participant p) {
        handleWorkQueuesOnRemoval(p, p.getWorkQueues());
    }

    public synchronized void handleWorkQueuesOnRemoval(Participant p, QueueSet qs) {
        if (qs == null) return ;    // no queues = nothing to do
        handleOfferedQueueOnRemoval(p, qs.getQueue(WorkQueue.OFFERED));
        handleAllocatedQueueOnRemoval(qs.getQueue(WorkQueue.ALLOCATED));
        handleStartedQueuesOnRemoval(p, qs.getQueue(WorkQueue.STARTED));
        handleStartedQueuesOnRemoval(p, qs.getQueue(WorkQueue.SUSPENDED));
    }


    public synchronized void handleWorkQueueOnRemoval(WorkQueue wq) {
        if (wq != null) {
            wq.setPersisting(false);                    // turn off circular persistence
            if (wq.getQueueType() == WorkQueue.OFFERED)
                handleOfferedQueueOnRemoval(null, wq);
            else if (wq.getQueueType() == WorkQueue.ALLOCATED)
                handleAllocatedQueueOnRemoval(wq);
            else handleStartedQueuesOnRemoval(null, wq);
        }
    }

       
    public synchronized void handleOfferedQueueOnRemoval(Participant p, WorkQueue qOffer) {
        if ((qOffer != null) && (! qOffer.isEmpty())) {
            Set<WorkItemRecord> wirSet = qOffer.getAll();

            // get all items on all offered queues, except this part's queue
            Set<WorkItemRecord> offerSet = new HashSet<WorkItemRecord>();
            Set<Participant> allParticipants = _orgDataSet.getParticipants() ;
            for (Participant temp : allParticipants) {
                if ((p == null) || (! temp.getID().equals(p.getID()))) {
                    WorkQueue q = temp.getWorkQueues().getQueue(WorkQueue.OFFERED) ;
                    if (q != null) offerSet.addAll(q.getAll());
                }
            }

            // compare each item in this part's queue to the complete set
            for (WorkItemRecord wir : wirSet) {
                 if (! offerSet.contains(wir)) {
                     _resAdmin.getWorkQueues().removeFromQueue(wir, WorkQueue.WORKLISTED);
                     _resAdmin.addToUnoffered(wir);
                 }
            }
        }
    }


    public synchronized void handleAllocatedQueueOnRemoval(WorkQueue qAlloc) {

        // allocated queue - all allocated go back to admin's unoffered
        if ((qAlloc != null) && (! qAlloc.isEmpty())) {
            _resAdmin.getWorkQueues().removeFromQueue(qAlloc, WorkQueue.WORKLISTED);
            _resAdmin.getWorkQueues().addToQueue(WorkQueue.UNOFFERED, qAlloc);
            if (_gatewayServer != null) {
                Set<WorkItemRecord> wirSet = qAlloc.getAll() ;
                for (WorkItemRecord wir : wirSet) {
                    this.announceResourceUnavailable(wir);
                }
            }
        }
    }


    // started & suspended queues
    public synchronized void handleStartedQueuesOnRemoval(Participant p, WorkQueue qStart) {
        if (qStart != null) {
            Set<WorkItemRecord> startSet = qStart.getAll();

            // if called during restore, there's no engine available yet, so we have to
            // save the wir until there is one; otherwise, we can do the checkin now
            for (WorkItemRecord wir : startSet) {
                if (serviceInitialised) {
                    checkinItem(p, wir);
                }
                else {
                    if (_orphanedStartedItems == null) {
                        _orphanedStartedItems = new ArrayList<WorkItemRecord>();
                    }    
                    _orphanedStartedItems.add(wir);
                }    
                _resAdmin.getWorkQueues().removeFromQueue(wir, WorkQueue.WORKLISTED);
            }    
        }
    }


    /**
     * moves the workitem to executing for the participant.
     *
     * Note that when an item is checked out of the engine, at least one child item
     * is spawned, and that is the item that executes (i.e. not the parent).
     *
     * @param p the participant starting the workitem
     * @param wir the item to start
     * @return true for a successful workitem start
     */
    public boolean start(Participant p, WorkItemRecord wir) {
        WorkItemRecord oneToStart ;

        // if 'executing', it's already been started so move queues & we're done
        if (wir.getStatus().equals(WorkItemRecord.statusExecuting)) {
            p.getWorkQueues().movetoStarted(wir);            
            return true ;
        }

        if (checkOutWorkItem(wir)) {

            // get all the child instances of this workitem
            List<WorkItemRecord> children = getChildren(wir.getID());

            if (children == null) {
                _log.error("Checkout of workitem '" + wir.getID() + "' unsuccessful.");
                return false;
            }

            if (children.size() > 1) {                   // i.e. if multi atomic task

                // which one got started with the checkout?
                oneToStart = getExecutingChild(children) ;

                // get the rest of the kids and distribute them
                distributeChildren(oneToStart, children) ;
            }
            else if (children.size() == 0) {                  // a 'fired' workitem
                oneToStart = refreshWIRFromEngine(wir) ;
            }
            else {                                            // exactly one child
                oneToStart = children.get(0) ;
            }

            // replace the parent in the cache with the executing child
            _workItemCache.remove(wir) ;
            oneToStart.setResourceStatus(WorkItemRecord.statusResourceStarted);
            _workItemCache.add(oneToStart);

            p.getWorkQueues().movetoStarted(wir, oneToStart);

            if (wir.getResourceStatus().equals(WorkItemRecord.statusResourceUnoffered)) {
                _resAdmin.getWorkQueues().removeFromQueue(wir, WorkQueue.UNOFFERED);
            }

            // cleanup deallocation list for started item (if any)
            ResourceMap rMap = getResourceMap(wir);
            if (rMap != null) rMap.removeIgnoreList(wir);       
            
            return true ;
        }
        else {
            _log.error("Could not start workitem: " + wir.getID()) ;
            return false ;
        }
    }


    private void distributeChildren(WorkItemRecord started, List<WorkItemRecord> children) {

        // list should always have at least one member
        for (WorkItemRecord child : children) {

            // don't distribute the already started child, but only the others
            if (! started.getID().equals(child.getID()))
                handleEnabledWorkItemEvent(child) ;
        }
    }


    // USER - TASK PRIVILEGE ACTIONS //

    public boolean suspendWorkItem(Participant p, WorkItemRecord wir) {
        boolean success = false;
        if (hasUserTaskPrivilege(p, wir, TaskPrivileges.CAN_SUSPEND)) {
            try {
                if (successful(
                    _interfaceBClient.suspendWorkItem(wir.getID(), getEngineSessionHandle()))) {
                    wir.setResourceStatus(WorkItemRecord.statusResourceSuspended);
                    p.getWorkQueues().movetoSuspend(wir);
                    _workItemCache.update(wir);
                    success = true ;
                }
            }
            catch (IOException ioe) {
                _log.error("Exception trying to suspend work item: " + wir.getID(), ioe);
            }
        }
        return success ;
    }

    public boolean unsuspendWorkItem(Participant p, WorkItemRecord wir) {

        // if user successfully suspended they also have unsuspend privileges
        boolean success = false;
        try {

            if (successful(
                _interfaceBClient.unsuspendWorkItem(wir.getID(), getEngineSessionHandle()))) {
                wir.setResourceStatus(WorkItemRecord.statusResourceStarted);
                p.getWorkQueues().movetoUnsuspend(wir);
                _workItemCache.update(wir);
                success = true ;
            }
        }
        catch (IOException ioe) {
            _log.error("Exception trying to unsuspend work item: " + wir.getID(), ioe);
        }
        return success ;
    }



    public boolean reallocateStatelessWorkItem(Participant pFrom, Participant pTo,
                                               WorkItemRecord wir) {
        boolean success = false ;
        if (hasUserTaskPrivilege(pFrom, wir, TaskPrivileges.CAN_REALLOCATE_STATELESS)) {

            // reset the item's data params to original values
            wir.setUpdatedData(wir.getDataList());
            reallocateWorkItem(pFrom, pTo, wir);
            EventLogger.log(wir, pFrom.getID(), EventLogger.event.reallocate_stateless);
            success = true ;
        }
        return success ;
    }

    
    public boolean reallocateStatefulWorkItem(Participant pFrom, Participant pTo,
                                               WorkItemRecord wir) {
        boolean success = false ;
        if (hasUserTaskPrivilege(pFrom, wir, TaskPrivileges.CAN_REALLOCATE_STATEFUL)) {
            reallocateWorkItem(pFrom, pTo, wir);
            EventLogger.log(wir, pFrom.getID(), EventLogger.event.reallocate_stateful);
            success = true ;
        }
        return success ;
    }


    private void reallocateWorkItem(Participant pFrom, Participant pTo,
                                               WorkItemRecord wir) {
        pFrom.getWorkQueues().removeFromQueue(wir, WorkQueue.STARTED);
        pTo.getWorkQueues().addToQueue(wir, WorkQueue.STARTED);
    }


    public boolean deallocateWorkItem(Participant p, WorkItemRecord wir) {
        boolean success = false ;
        if (hasUserTaskPrivilege(p, wir, TaskPrivileges.CAN_DEALLOCATE)) {
            p.getWorkQueues().removeFromQueue(wir, WorkQueue.ALLOCATED);

            ResourceMap rMap = getResourceMap(wir) ;
            if (rMap != null) {
                rMap.ignore(wir, p);                  // add Participant to ignore list
                rMap.distribute(wir);                 // redistribute workitem
            }
            else {                                    // pre version 2.0
                if (_orgDataSet.getParticipantCount() > 1) {
                    offerToAll(wir);
                    p.getWorkQueues().removeFromQueue(wir, WorkQueue.OFFERED);
                }
                else _resAdmin.addToUnoffered(wir);
            }
            EventLogger.log(wir, p.getID(), EventLogger.event.deallocate);
            success = true;
        }
        return success ;
    }


    public boolean delegateWorkItem(Participant pFrom, Participant pTo,
                                                       WorkItemRecord wir) {
        boolean success = false ;
        if (hasUserTaskPrivilege(pFrom, wir, TaskPrivileges.CAN_DELEGATE)) {
            pFrom.getWorkQueues().removeFromQueue(wir, WorkQueue.ALLOCATED);
            pTo.getWorkQueues().addToQueue(wir, WorkQueue.ALLOCATED);
            EventLogger.log(wir, pFrom.getID(), EventLogger.event.delegate);
            success = true ;
        }
        return success ;
    }


    public boolean skipWorkItem(Participant p, WorkItemRecord wir) {
        String result ;
        if (hasUserTaskPrivilege(p, wir, TaskPrivileges.CAN_SKIP)) {
            try {
                result = _interfaceBClient.skipWorkItem(wir.getID(), getEngineSessionHandle()) ;
                if (successful(result)) {
                    p.getWorkQueues().removeFromQueue(wir, WorkQueue.ALLOCATED);
                    EventLogger.log(wir, p.getID(), EventLogger.event.skip);
                    return true ;
                }
            }
            catch (IOException ioe) {
                return false ;
            }
        }
        return false ;
    }


    public String pileWorkItem(Participant p, WorkItemRecord wir) {
        String result ;
        if (hasUserTaskPrivilege(p, wir, TaskPrivileges.CAN_PILE)) {
            ResourceMap map = getResourceMap(wir);
            if (map != null) {
                result = map.setPiledResource(p, wir);
                if (! result.startsWith("Cannot"))
                    EventLogger.log(wir, p.getID(), EventLogger.event.pile);
            }
            else
                result = "Cannot pile task: no resourcing parameters defined for specification." ;
        }
        else result = "Cannot pile task: insufficient privileges." ;

        return result;
    }


    public String unpileTask(ResourceMap resMap, Participant p) {
        if (resMap.getPiledResourceID().equals(p.getID())) {
            resMap.removePiledResource();
            return "Task successfully unpiled" ;
        }
        return "Cannot unpile task - resource settings unavailable";
    }


    // ISSUE: If p is currently logged on, we'll use p's handle (the engine will use
    //        it to log p as the starter). If p is not logged on, the service's handle
    //        has to be used, and thus the service will be logged as the starter. There is
    //        no way around this currently, but will be handled when the engine is
    //        made completely agnostic to resources. 
    public boolean routePiledWorkItem(Participant p, WorkItemRecord wir) {
        return routeWorkItem(p, wir, getEngineSessionHandle()) ;
    }

    // ISSUE: same as that in the method above. Called by this.acceptOffer and
    // ResourceMap.doStart
    public boolean startImmediate(Participant p, WorkItemRecord wir) {
        return routeWorkItem(p, wir, getEngineSessionHandle()) ;
    }


    private boolean routeWorkItem(Participant p, WorkItemRecord wir, String handle) {
        if (handle != null) start(p, wir) ;
        return (handle != null);        
    }

    public boolean hasUserTaskPrivilege(Participant p, WorkItemRecord wir,
                                        int privilege) {

        // admin access overrides set privileges
        if (p.isAdministrator()) return true ;

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
        String result = StringUtil.wrap("Unknown workitem ID", "failure");
        WorkItemRecord wir = _workItemCache.get(itemID);
        if (wir != null) {
            result = wir.toXML();
        }
        return result;
    }

    public WorkItemRecord getWorkItemRecord(String itemID) {
        return _workItemCache.get(itemID);
    }


    public String updateWorkItemData(String itemID, String data) {
        String result ;
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
                        else {
                            result = StringUtil.wrap(
                                    "Data failed validation: " + validate, "failure");
                        }
                    }
                    else {
                        result = StringUtil.wrap("Data XML is malformed", "failure");
                    }
                }
                else {
                    result = StringUtil.wrap(
                        "Workitem '" + itemID + "' has a status of '" + wir.getStatus() +
                        "' - data may only be updated for a workitem with 'Executing' status.",
                        "failure"
                    );
                }
            }
            else {
                result = StringUtil.wrap("Unknown workitem: " + itemID, "failure");
            }
        }
        else {
            result = StringUtil.wrap("Data is null or empty.", "failure");
        }
        return result;
    }


    private String checkWorkItemDataAgainstSchema(WorkItemRecord wir, Element data) {
        String result = "<success/>";
        if (! data.getName().equals(wir.getTaskName().replace(' ', '_'))) {
            result = StringUtil.wrap(
                    "Invalid data structure: root element name doesn't match task name",
                    "failure"
            );
        }
        else {
            YSpecificationID specID = new YSpecificationID(wir);
            SpecificationData specData = getSpecData(specID);
            try {
                String schema = specData.getSchemaLibrary();
                YDataValidator validator = new YDataValidator(schema);
                if (validator.validateSchema()) {
                    TaskInformation taskInfo = getTaskInformation(
                            specID, wir.getTaskID(), getEngineSessionHandle());

                    // a YDataValidationException is thrown here if validation fails
                    validator.validate(taskInfo.getParamSchema().getCombinedParams(), data, "");
                }
                else result = StringUtil.wrap("Invalid data schema", "failure");
            }
            catch (Exception e) {
                result = StringUtil.wrap(e.getMessage(), "failure");
            }
        }    
        return result;
    }


    /*****************************************************************************/

    public String chainCase(Participant p, WorkItemRecord wir) {
        String result ;
        ResourceMap rMap = getResourceMap(wir);
        if (rMap != null) {
            result = addChain(p, wir);
            if (result.indexOf("success") > -1)
                rMap.withdrawOffer(wir);        
        }
        else
            result = "Cannot chain tasks: no resourcing parameters defined for specification." ;

        return result;
    }

    public boolean routeChainedWorkItem(Participant p, WorkItemRecord wir) {

        // only route if user is still logged on
        if (getSessionHandle(p) != null) {
            return routeWorkItem(p, wir, getEngineSessionHandle()) ;
        }
        return false;
    }

    public String addChain(Participant p, WorkItemRecord wir) {
        String result ;
        String caseID = wir.getRootCaseID() ;
        if (! isChainedCase(caseID)) {
            _chainedCases.put(caseID, p);
            routeChainedWorkItem(p, wir) ;
            EventLogger.log(wir, p.getID(), EventLogger.event.chain);
            result = "Chaining successful." ;
        }
        else result = "Cannot chain: case already chained by another user." ;
        return result;
    }

    public void removeChain(String caseID) {
        _chainedCases.remove(caseID);
    }

    public Participant getChainedParticipant(String caseID) {
        return _chainedCases.get(caseID);
    }


    public Set<String> getChainedCases(Participant p) {
        cleanCaches();
        Set<String> result = new HashSet<String>();
        for (String caseID : _chainedCases.keySet()) {
            Participant chainer = _chainedCases.get(caseID);
            if (chainer.getID().equals(p.getID()))
                result.add(caseID + "::" + getSpecIDForCase(caseID));
        }
        return result;
    }

    public boolean isChainedParticipant(Participant p) {
        for (Participant chainer : _chainedCases.values())
             if (chainer.getID().equals(p.getID()))
                 return true;
        return false;
    }

    public void removeChainedCasesForParticpant(Participant p) {
        List<String> caseList = new ArrayList<String>();
        for (String caseID : _chainedCases.keySet()) {
            Participant chainer = _chainedCases.get(caseID);
            if (chainer.getID().equals(p.getID()))
                caseList.add(caseID) ;
        }
        for (String caseID : caseList) removeChain(caseID);
    }

    public boolean isChainedCase(String caseID) {
        return _chainedCases.containsKey(caseID);
    }

    public boolean routeIfChained(WorkItemRecord wir, Set<Participant> distributionSet) {
        boolean result = false;
        String caseID = wir.getRootCaseID() ;
        if (isChainedCase(caseID)) {
            Participant p = getChainedParticipant(caseID);
            if (distributionSet.contains(p))
                 result = routeChainedWorkItem(p, wir);
        }
        return result;
    }
    
    /*****************************************************************************/

    private String getSpecIDForCase(String caseID) {
        for (WorkItemRecord wir : _workItemCache.values()) {
            if (wir.getRootCaseID().equals(caseID))
                return wir.getSpecIdentifier();
        }
        return "" ;
    }

    private void cleanCaches() {
        Set<String> liveCases = getAllRunningCaseIDs();
        if ((liveCases != null) && (! liveCases.isEmpty())) {
            List<String> caseIDs = new ArrayList(_chainedCases.keySet());
            for (String id : caseIDs) {
                if (! liveCases.contains(id)) _chainedCases.remove(id);
            }
            List<WorkItemRecord> wirList = new ArrayList(_workItemCache.values());
            for (WorkItemRecord wir : wirList) {
                if (! liveCases.contains(wir.getRootCaseID())) _workItemCache.remove(wir);
            }
        }
    }
    

    /** @return the union of persisted and unpersisted maps */
    public Set<ResourceMap> getPiledTaskMaps(Participant p) {
        Set<ResourceMap> result = getUnpersistedPiledTasks(p);
        if (_persisting) result.addAll(getPersistedPiledTasks(p));
        return result ;
    }

    public Set<ResourceMap> getUnpersistedPiledTasks(Participant p) {
        Set<ResourceMap> result = new HashSet<ResourceMap>();
        Set<ResourceMap> mapSet = getAllResourceMaps() ;
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
            Iterator itr = maps.iterator();
            while (itr.hasNext()) {
                ResourceMap map = (ResourceMap) itr.next();
                String pid = map.getPiledResourceID();
                if ((pid != null) && (pid.equals(p.getID())))
                    result.add(map);
            }
        }
        return result ;
    }


    public Set<ResourceMap> getAllResourceMaps() {
        return _resMapCache.getAll();
    }

    /***************************************************************************/


    public DataSource getOrgDataSource() { return _orgdb; }

    public Persister getPersister() { return _persister ; }

    public void setPersisting(boolean flag) {
        _persisting = flag ;
        if (_persisting) _persister = Persister.getInstance();
        else _persister = null ;
    }

    public boolean getPersisting() { return _persisting; }

     /**
     * Starts a timer task to refresh the org data dataset at regular intervals
     * @param interval the number of minutes between each refresh
     */
    public void startOrgDataRefreshTimer(long interval) {
        if ((interval < 1) && (_orgDataRefreshTimer != null))
            _orgDataRefreshTimer.cancel();            // disable timer
        else {
            interval = interval * 60000 ;            // convert minutes to milliseconds
            _orgDataRefreshTimer = new Timer(true) ;
            TimerTask tTask = new OrgDataRefresh();
            _orgDataRefreshTimer.scheduleAtFixedRate(tTask, interval, interval);
        }
    }

    public void setPersistPiling(boolean persist) {
        _persistPiling = persist ;
    }

    public boolean isPersistPiling() { return _persistPiling; }


    public Set<Participant> getDistributionSet(String itemID) {
        ResourceMap map = getResourceMap(itemID);
        return (map != null) ? map.getDistributionSet() : null;
    }

    public ResourceMap getResourceMap(String itemID) {
        if (itemID != null) {
            return getCachedResourceMap(getWorkItemCache().get(itemID));
        }
        else return null;
    }

    public ResourceMap getCachedResourceMap(WorkItemRecord wir) {
        if (wir == null) return null;
        YSpecificationID specID = new YSpecificationID(wir);
        String taskID = wir.getTaskID();
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
                        getEngineSessionHandle()) ;

                if ((resElem != null) &&
                        successful(JDOMUtil.elementToString(resElem))) {

                    map = new ResourceMap(specID, taskID, resElem, _persisting) ;
                    _resMapCache.add(specID, taskID, map) ;
                }
            }
            catch (IOException ioe) {
                _log.error("Exception getting resource specs from Engine", ioe) ;
            }
        }

        return map;              // map = null if no resourcing spec for this task
    }


    public static void setServiceInitialised() { serviceInitialised = true ; }


    public Set<Participant> getWhoCompletedTask(String taskID, WorkItemRecord wir) {
        Set<Participant> result = new HashSet<Participant>();
        FourEyesCache cache = _taskCompleters.get(wir.getRootCaseID());
        if (cache != null) result = cache.getCompleters(taskID);
        return result ;
    }


     /**
     * get the workitem's (task) decomposition id
     * @param wir - the workitem to get the decomp id for
     */
     public String getDecompID(WorkItemRecord wir) {
         return getDecompID(new YSpecificationID(wir), wir.getTaskID());
     }

  //***************************************************************************//

    /**
     *  gets a task's decomposition id
     *  @param specID the specification id
     *  @param taskID the task's id
     */
    public String getDecompID(YSpecificationID specID, String taskID) {
       try {
           TaskInformation taskinfo = getTaskInformation(specID, taskID, getEngineSessionHandle());
           return taskinfo.getDecompositionID() ;
       }
       catch (IOException ioe) {
           _log.error("IO Exception in getDecompId ", ioe) ;
           return null ;
       }
    }


    // CHECKOUT METHODS //

    /**
     *  Check the workitem out of the engine
     *  @param wir - the workitem to check out
     *  @return true if checkout was successful
     */
    protected boolean checkOutWorkItem(WorkItemRecord wir) {
        try {
            if (null != checkOut(wir.getID(), getEngineSessionHandle())) {
                _log.info("   checkout successful: " + wir.getID());
                return true ;
            }
            else {
                _log.info("   checkout unsuccessful: " + wir.getID());
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

    // re-adds checkedout item to local cache after a restore (if required)
    private void checkCacheForWorkItem(WorkItemRecord wir) {
        if (getCachedWorkItem(wir.getID()) == null) {

            // if the item is not locally cached, it means a restore has occurred
            // after a checkout & the item is still checked out, so lets put it back
            // so that it can be checked back in
            getModel().addWorkItem(wir);
        }
    }


    /**
     *  Checks a (checked out) workitem back into the engine
     *
     *  @param p - the participant checking in the item
     *  @param wir - workitem to check into the engine
     *  @return true if checkin is successful
     */
    public String checkinItem(Participant p, WorkItemRecord wir) {
        String result = "<failure/>";                              // assume the worst
        try {
            wir = _workItemCache.get(wir.getID()) ;                // refresh wir

            if (wir != null) {
                Element outData = wir.getUpdatedData();
                if (outData == null) outData = wir.getDataList();
                checkCacheForWorkItem(wir);
                if (p != null) addTaskCompleter(p, wir);
                parseCompletionLogPredicate(p, wir);
                result = checkInWorkItem(wir.getID(), wir.getDataList(), outData,
                        wir.getLogPredicateCompletion(), getEngineSessionHandle()) ;
                if (successful(result)) {
                    if (p != null) {
                        QueueSet qSet = p.getWorkQueues();
                        if (qSet != null) {
                            WorkQueue queue = qSet.getQueue(WorkQueue.STARTED);
                            if (queue != null) queue.remove(wir);
                        }
                    }    
                    _workItemCache.remove(wir) ;
                    String pid = (p != null) ? p.getID() : "";
                    EventLogger.log(wir, pid, EventLogger.event.complete);
                }
                else {
                    removeTaskCompleter(p, wir);

                    // trim the error message
                    result = trimCheckinErrorMessage(result);
                }
            }
        }
        catch (IOException ioe) {
            result = "<failure>checkinItem method caused java IO Exception</failure>";
            _log.error(result, ioe) ;
        }
        catch (JDOMException jde) {
            result = "<failure>checkinItem method caused JDOM Exception</failure>" ;
            _log.error(result, jde) ;
        }
        return result ;
    }


    private String trimCheckinErrorMessage(String msg) {
        int start = msg.indexOf("XQuery [");
        int end = msg.lastIndexOf("Validation error message");
        if ((start > -1) && (end > -1)) {
            msg = msg.substring(0, start) + msg.substring(end);
        }
        return msg;
    }


    private void addTaskCompleter(Participant p, WorkItemRecord wir) {
        String caseid = getRootCaseID(wir.getCaseID());
        FourEyesCache cache = _taskCompleters.get(caseid);
        if (cache == null) {
            cache = new FourEyesCache(caseid);
            _taskCompleters.put(caseid, cache);
        }
        cache.addCompleter(wir.getTaskID(), p);
    }


    private void removeTaskCompleter(Participant p, WorkItemRecord wir) {
        FourEyesCache cache = _taskCompleters.get(getRootCaseID(wir.getCaseID()));
        if (cache != null) cache.removeCompleter(wir.getTaskID(), p);
    }


    private void removeCaseFromTaskCompleters(String caseid) {
        _taskCompleters.remove(getRootCaseID(caseid));
    }


    private void parseCompletionLogPredicate(Participant p, WorkItemRecord wir) {
        String predicate = wir.getLogPredicateCompletion();
        if (predicate != null) {
            wir.setLogPredicateCompletion(new LogPredicateParser(p, wir).parse(predicate));
        }
    }

    private String getRootCaseID(String id) {
        int firstDot = id.indexOf(".");
        if (firstDot > -1)
            return id.substring(0, firstDot);
        else
            return id;
    }

    private void cancelCodeletRunnersForCase(String caseID) {
        String rootID = getRootCaseID(caseID);
        Set<String> toRemove = new HashSet<String>();     // avoid concurrency exception
        for (String wirID : _codeletRunners.keySet()) {
             if (wirID.startsWith(rootID + ".")) {
                 toRemove.add(wirID);
             }
        }
        for (String wirID : toRemove) {
            cancelCodeletRunner(wirID);
        }
    }

    private void cancelCodeletRunner(String wirID) {
        CodeletRunner runner = _codeletRunners.remove(wirID);
        if (runner != null) runner.cancel();
    }

//***************************************************************************//

    /**
     *  Checks out all the child workitems of the parent item specified
     *  @param wir - the parent wir object
     */
    protected List checkOutChildren(WorkItemRecord wir, List children) {

        for (int i = 0; i < children.size(); i++) {
           WorkItemRecord itemRec = (WorkItemRecord) children.get(i);

           // if its 'fired' check it out
           if (WorkItemRecord.statusFired.equals(itemRec.getStatus()))
              checkOutWorkItem(itemRec);
        }

        // update child item list after checkout (to capture status changes) & return
        return getChildren(wir.getID());
    }


    public WorkItemRecord getExecutingChild(WorkItemRecord parent) {
        return getExecutingChild(getChildren(parent.getID()));
    }

    
    private WorkItemRecord getExecutingChild(List<WorkItemRecord> children) {
        for (WorkItemRecord itemRec : children) {

           // find the one that's executing
           if (WorkItemRecord.statusExecuting.equals(itemRec.getStatus()))
              return itemRec;
        }
        return null ;
    }



    /*************************
     * 9. CONNECTION METHODS *
     ************************/

    public String login(String userid, String password, String jSessionID) {
        if (userid.equals("admin")) return loginAdmin(password, jSessionID) ;

        String result ;
        Participant p = getParticipantFromUserID(userid) ;
        if (p != null) {
            boolean validPassword;
            if (_orgDataSet.isUserAuthenticationExternal()) {
                try {
                    validPassword = _orgdb.authenticate(userid, password);
                }
                catch (YAuthenticationException yae) {
                    return StringUtil.wrap(yae.getMessage(), "failure") ;
                }
            }
            else {
                validPassword = p.isValidPassword(password);
            }
            if (validPassword) {
                result = newSessionHandle();
                _liveSessions.add(result, p, jSessionID) ;
                EventLogger.audit(userid, EventLogger.audit.logon);
            }
            else {
                result = "<failure>Incorrect Password</failure>" ;
                EventLogger.audit(userid, EventLogger.audit.invalid);
            }
        }
        else {
            result = "<failure>Unknown user name</failure>" ;
            EventLogger.audit(userid, EventLogger.audit.unknown);
        }
        return result ;
    }


    private String newSessionHandle() {
        return UUID.randomUUID().toString();
    }


    private String getAdminUserPassword() {
        try {
            return _interfaceAClient.getPassword("admin", getEngineSessionHandle());
        }
        catch (IOException ioe) {
            return "<failure>Could not connect to YAWL Engine</failure>" ;
        }
    }

    
    private String loginAdmin(String password, String jSessionID) {
        String handle ;
        String adminPassword = _connections.getPassword("admin");
        if (adminPassword == null) {
            adminPassword = getAdminUserPassword();    // from engine
        }
        if (successful(adminPassword)) {
            if (password.equals(adminPassword)) {
                handle = newSessionHandle();
                _liveSessions.add(handle, null, jSessionID);
                EventLogger.audit("admin", EventLogger.audit.logon);
            }
            else {
                handle = "<failure>Incorrect Password</failure>";
                EventLogger.audit("admin", EventLogger.audit.invalid);
            }
        }
        else handle = adminPassword;     // an error message

        return handle ;
    }

    // removes session handle from map of live users
    public void logout(String handle) {
        UserConnection connection = _liveSessions.removeSessionHandle(handle);
        if (connection != null) {
            Participant p = connection.getParticipant();
            if (p != null) {
                removeChainedCasesForParticpant(p);
            }
            String id = (p != null) ? p.getUserID() : "admin";
            EventLogger.audit(id, EventLogger.audit.logoff);
        }
    }

    public boolean isValidUserSession(String handle) {
        return _liveSessions.containsSessionHandle(handle);
    }

    public String validateUserCredentials(String userid, String password, boolean admin) {
        String result = "<success/>";
        if (userid.equals("admin")) {
            String adminPassword = getAdminUserPassword();
            if (successful(adminPassword)) {
                if (! password.equals(adminPassword)) {
                    result = "<failure>Incorrect Password</failure>" ;
                }
            } else result = adminPassword;

            return result;
        }
        
        Participant p = getParticipantFromUserID(userid) ;
        if (p != null) {
            if (p.getPassword().equals(password)) {
                if (admin && ! p.isAdministrator()) {
                    result = "<failure>Administrative privileges required.</failure>" ;
                }
            } else result = "<failure>Incorrect Password</failure>" ;
        } else result = "<failure>Unknown user name</failure>" ;

        return result ;
    }


    public Participant expireSession(String jSessionID) {
        Participant p = null;
        UserConnection connection = _liveSessions.removeSessionID(jSessionID);
        if (connection != null) {
            p = connection.getParticipant();
            String id = (p != null) ? p.getUserID() : "admin";
            EventLogger.audit(id, EventLogger.audit.expired);
        }
        return p;
    }


    /** Checks if there is a connection to the engine, and
     *  if there isn't, attempts to connect
     *  @return true if connected to the engine
     */
    protected synchronized boolean connected() {
        try {
             // if not connected
             if ((_engineSessionHandle == null) ||
                 (_engineSessionHandle.length() == 0) ||
                 (! checkConnection(_engineSessionHandle))) {

                 _engineSessionHandle = connect(_engineUser, _enginePassword);
             }
        }
        catch (IOException ioe) {
             _log.error("Exception attempting to connect to engine", ioe);
        }
        return (successful(_engineSessionHandle)) ;
    }


    protected String getEngineSessionHandle() {
        connected();                           // (re)establish connection if required
        return _engineSessionHandle;
    }

    //***************************************************************************//

    private class OrgDataRefresh extends TimerTask {
        public void run() { loadResources() ; }
    }

    /*******************************************************************************/

    public void shutdown() {
        try {
            for (UserConnection connection : _liveSessions.getAllSessions()) {
                if (connection != null) {
                    Participant p = connection.getParticipant();
                    String id = (p != null) ? p.getUserID() : "admin";
                    EventLogger.audit(id, EventLogger.audit.shutdown);
                }
            }
            _connections.shutdown();

            for (CodeletRunner runner : _codeletRunners.values()) {
                runner.shutdown();
            }
        }
        catch (Exception e) {
            _log.error("Unsuccessful audit log update on shutdown.");
        }
    }

    public String serviceConnect(String userid, String password, long timeOutSeconds) {
        return _connections.connect(userid, password, timeOutSeconds) ;
    }

    public void serviceDisconnect(String handle) {
        _connections.disconnect(handle) ;
    }

    public boolean checkServiceConnection(String handle) {
        return _connections.checkConnection(handle);
    }


    private Participant getParticipantWithSessionHandle(String handle) {
        return _liveSessions.getParticipantWithSessionHandle(handle);
    }

    public Set<SpecificationData> getLoadedSpecs() {
        Set<SpecificationData> result = getSpecList() ;
        if (result != null) {
            for (SpecificationData specData : result) {
                if (! specData.getStatus().equals(YSpecification._loaded))
                   result.remove(specData) ;
            }
        }
        return result ;
    }
    

    public Set<SpecificationData> getSpecList() {
        try {
            return new HashSet<SpecificationData>(
                    getSpecificationPrototypesList(getEngineSessionHandle()));
        }
        catch (IOException ioe) {
            _log.error("IO Exception retrieving specification list", ioe) ;
            return null ;
        }
    }

    public SpecificationData getSpecData(YSpecificationID spec) {
        SpecificationData result = _specCache.get(spec);   
        if (result == null) {
            try {
                result = getSpecificationData(spec, getEngineSessionHandle()) ;
                if (result != null) _specCache.add(result) ;
            }
            catch (IOException ioe) {
                _log.error("IO Exception retrieving specification data", ioe) ;
                result = null ;
            }
        }
        return result;
    }


    public Set<String> getAllRunningCaseIDs() {
        Set<String> result = new HashSet<String>(); 
        Set<SpecificationData> specDataSet = getSpecList() ;
        if (specDataSet != null) {
            for (SpecificationData specData : specDataSet) {
                List<String> caseIDs = getRunningCasesAsList(specData.getID());
                if (caseIDs != null)
                    result.addAll(caseIDs) ;
            }
        }
        return result ;
    }


    public List<String> getRunningCasesAsList(YSpecificationID specID) {
        try {
            String casesAsXML = _interfaceBClient.getCases(specID, getEngineSessionHandle());
            if (_interfaceBClient.successful(casesAsXML))
                return Marshaller.unmarshalCaseIDs(casesAsXML);
        }
        catch (IOException ioe) {
            _log.error("IO Exception retrieving running cases list", ioe) ;
        }
        return null;
    }


    public String getRunningCases(YSpecificationID specID) throws IOException {
        return _interfaceBClient.getCases(specID, getEngineSessionHandle());
    }


    public String uploadSpecification(String fileContents, String fileName) {
        try {
            return _interfaceAClient.uploadSpecification(fileContents, getEngineSessionHandle());
        }
        catch (IOException ioe) {
            _log.error("IOException uploading specification " + fileName, ioe);
            return "<failure><reason><error>IOException uploading specification " +
                    fileName + "</error></reason></failure>";
        }
    }

    /**
     * Cancels the case & removes its workitems (if any) from the service's queues
     * & caches. Note: this method is synchronised to prevent any clash with
     * 'handleCancelledCaseEvent', which is triggered by the call to cancelCase in
     * this method.
     * @param caseID the case to cancel
     * @return a message from the engine indicating success or otherwise
     * @throws IOException if there's trouble talking to the engine
     */
    public synchronized String cancelCase(String caseID, String userHandle) throws IOException {
        List<WorkItemRecord> liveItems = getLiveWorkItemsForCase(caseID) ;
        YSpecificationID specID = null;                           // for logging only

        // cancel the case in the engine
        String result = _interfaceBClient.cancelCase(caseID, getEngineSessionHandle());

        // remove live items for case from workqueues and cache
        if (successful(result)) {
            if (liveItems != null) {
                for (WorkItemRecord wir : liveItems) {
                    if (specID == null) specID = new YSpecificationID(wir);
                    removeFromAll(wir) ;
                    _workItemCache.remove(wir);
                    EventLogger.log(wir, null, EventLogger.event.cancelled_by_case);
                }
                _chainedCases.remove(caseID);
            }

            // log the cancellation
            Participant p = getParticipantWithSessionHandle(userHandle);
            String pid = (p != null) ? p.getID() : "admin" ;
            EventLogger.log(specID, caseID, pid, false);
        }
        else _log.error("Error attempting to Cancel Case.") ;

        return result ;
    }


    private List<WorkItemRecord> getLiveWorkItemsForCase(String caseID) {
        List<WorkItemRecord> result = null ;
        List<WorkItemRecord> childList = new ArrayList<WorkItemRecord>();
        try {
            result = _interfaceBClient.getLiveWorkItemsForIdentifier("case", caseID,
                                                         getEngineSessionHandle()) ;
            if (result != null) {

                // the above method only gets parents, so get any child items too
                for (WorkItemRecord wir : result) {
                    List<WorkItemRecord> children = getChildren(wir.getID()) ;
                    childList.addAll(children) ;
                }
                result.addAll(childList) ;
            }    
        }
        catch (Exception e) {
            _log.error("Exception attempting to retrieve work item list from engine");
        }
        return result;
    }

    public List<WorkItemRecord> getChildren(String parentID) {
        try {
            return getChildren(parentID, getEngineSessionHandle()) ;
        }
        catch (IOException ioe) {
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
                }
                else {
                    cachedChildren.add(child);
                }
            }
        }
        return cachedChildren;
    }

    public String unloadSpecification(YSpecificationID specID) throws IOException {
        String result = _interfaceAClient.unloadSpecification(specID, getEngineSessionHandle());
        if (successful(result)) {
            _resMapCache.remove(specID);
            _specCache.remove(specID);
            _dataSchemaCache.remove(specID);
            getIBCache().unloadSpecificationData(specID);
        }
        return result ;
    }


    public String launchCase(YSpecificationID specID, String caseData, String handle)
            throws IOException {
        if (_serviceURI == null) setServiceURI();
        String caseID = _interfaceBClient.launchCase(specID, caseData,
                          getEngineSessionHandle(), getLogData(), _serviceURI) ;
        if (successful(caseID)) {
            Participant p = getParticipantWithSessionHandle(handle);
            String pid = (p != null) ? p.getID() : "admin" ;
            EventLogger.log(specID, caseID, pid, true);
        }
        return caseID;
    }


    private YLogDataItemList getLogData() {
        return new YLogDataItemList(
               new YLogDataItem("launched", "name", "resourceService", "string"));
    }


    public String getTaskParamsAsXML(String itemID) throws IOException {
        WorkItemRecord wir = _workItemCache.get(itemID);
        if (wir != null) {
            return getTaskParamsAsXML(new YSpecificationID(wir), wir.getTaskID());
        }
        else return "<failure>Unknown workitem: " + itemID + "</failure>";
    }


    public String getTaskParamsAsXML(YSpecificationID specID, String taskID) throws IOException {
        String xml = _interfaceBClient.getTaskInformationStr(specID, taskID, getEngineSessionHandle());
        if (xml != null) {
            Element response = JDOMUtil.stringToElement(xml);
            if (response != null) {
                Element taskInfo = response.getChild("taskInfo");
                if (taskInfo != null) {
                    Element params = taskInfo.getChild("params");
                    return JDOMUtil.elementToString(params);
                }
            }
        }
        return "";
    }

    
    public String getOutputOnlyTaskParamsAsXML(String itemID) {
        String result ;
        WorkItemRecord wir = _workItemCache.get(itemID);
        if (wir != null) {
            try {
                TaskInformation taskInfo = getTaskInformation(
                    new YSpecificationID(wir), wir.getTaskID(), getEngineSessionHandle()
                );
                List<YParameter> list = taskInfo.getParamSchema().getOutputOnlyParams();
                result = "<outputOnlyParameters>\n";
                for (YParameter param : list) {
                    result += param.toSummaryXML();
                }
                result += "\n</outputOnlyParameters>";
            }
            catch (IOException ioe) {
                result = "<failure>Exception connecting to Engine.</failure>";
            }
        }
        else result = "<failure>Unknown workitem '" + itemID + "'.</failure>";

        return result;
    }

    
    public Map<String, FormParameter> getWorkItemParamsInfo(WorkItemRecord wir)
           throws IOException, JDOMException {
        Map<String, FormParameter> inputs, outputs;
        TaskInformation taskInfo = getTaskInformation(new YSpecificationID(wir),
                                                      wir.getTaskID(), getEngineSessionHandle());
        // map the params
        inputs  = mapParamList(taskInfo.getParamSchema().getInputParams()) ;
        outputs = mapParamList(taskInfo.getParamSchema().getOutputParams()) ;

        // if param is only in input list, mark it as input-only
        for (String name : inputs.keySet()) {
            if (! outputs.containsKey(name)) {
                inputs.get(name).setInputOnly(true);
            }
        }

        // combine the two maps
        if (outputs != null)
            outputs.putAll(inputs);
        else
            outputs = inputs ;

        // now map data values to params
        Element itemData ;
        if (wir.isEdited()) {
            wir = _workItemCache.get(wir.getID());       // refresh data list if required
            itemData = wir.getUpdatedData() ;
        }
        else
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

    private Map<String, FormParameter> mapParamList(List params) {
        Map<String, FormParameter> result = new HashMap<String, FormParameter>();
        for (Object obj : params) {
            YParameter param = (YParameter) obj ;
            FormParameter fp = new FormParameter(param);
            result.put(param.getName(), fp) ;
        }
        return result ;
    }

    private WorkItemRecord refreshWIRFromEngine(WorkItemRecord wir) {
        try {
            wir = getEngineStoredWorkItem(wir.getID(), getEngineSessionHandle());
            _workItemCache.update(wir) ;
            return wir ;
        }
        catch (Exception e) {
            return wir;
        }
    }


    public void announceModifiedQueue(String pid) {
        if (_jsfApplicationReference != null) {
            _jsfApplicationReference.refreshUserWorkQueues(pid);
        }
    }


    public Set<YAWLServiceReference> getRegisteredServices() {
        return _interfaceAClient.getRegisteredYAWLServices(getEngineSessionHandle());
    }

    public String getRegisteredServicesAsXML() throws IOException {
        return _interfaceAClient.getRegisteredYAWLServicesAsXML(getEngineSessionHandle());
    }


    public String addRegisteredService(YAWLServiceReference service) throws IOException {
        String result = _interfaceAClient.addYAWLService(service, getEngineSessionHandle());
        if (successful(result)) {
            _connections.addUser(service.getServiceName(), service.getServicePassword());
        }
        return result;
    }


    public String removeRegisteredService(String id) throws IOException {
        String result = _interfaceAClient.removeYAWLService(id, getEngineSessionHandle());
        if (successful(result)) {
            _connections.deleteUser(id);
        }
        return result;
    }


    public Set<YExternalClient> getExternalClients() throws IOException {
        return _interfaceAClient.getClientAccounts(getEngineSessionHandle());
    }


    public String addExternalClient(YExternalClient client) throws IOException {
        String result = _interfaceAClient.addClientAccount(client, getEngineSessionHandle());
        if (successful(result)) {
            _connections.addUser(client.getUserName(), client.getPassword());
        }
        return result;
    }


    public String removeExternalClient(String id) throws IOException {
        String result = _interfaceAClient.removeClientAccount(id, getEngineSessionHandle());
        if (successful(result)) {
            _connections.deleteUser(id);
        }
        return result;
    }


    public String updateExternalClient(String id, String password, String doco)
            throws IOException {
        String result = _interfaceAClient.updateClientAccount(id, password, doco,
                getEngineSessionHandle());
        if (successful(result)) {
            _connections.updateUser(id, PasswordEncryptor.encrypt(password, null));
        }
        return result;
    }


    // any services or client apps authorised to connect to the engine may connect
    // to this service too.
    private void setAuthorisedServiceConnections() {
        Map<String, String> users = new Hashtable<String,String>();
        try {
            Set<YExternalClient> clients = getExternalClients();
            if (clients != null) {
                for (YExternalClient client : clients) {
                    users.put(client.getUserName(), client.getPassword());
                }
            }
            Set<YAWLServiceReference> services = getRegisteredServices();
            if (services != null) {
                for (YAWLServiceReference service : services) {
                    users.put(service.getServiceName(), service.getServicePassword());
                }
            }
            _connections.clear();
            _connections.addUsers(users);
        }
        catch (IOException ioe) {
            _log.error("IO Exception getting valid service-level users from engine.");
        }
    }


    public String getCaseData(String caseID) throws IOException {
        return _interfaceBClient.getCaseData(caseID, getEngineSessionHandle()) ;
    }


    public String getNetParamValue(String caseID, String paramName) throws IOException {
        String result = null;
        String caseData = getCaseData(caseID) ;
        Element eData = JDOMUtil.stringToElement(caseData);
        if (eData != null)
            result = eData.getChildText(paramName) ;
        return result;
    }


    public String getDataSchema(YSpecificationID specID) {
        String result = null ;
        try {
      //      Map<String, Element> schemaMap = getSpecificationDataSchema(specID);
            SpecificationData specData = getSpecData(specID);
     //       result = new DataSchemaBuilder(schemaMap).build(specData);
                  result = new DataSchemaProcessor().createSchema(specData);
        }
        catch (Exception e) {
            _log.error("Could not retrieve schema for case parameters", e)  ;
        }
        return result ;
    }

    
    public String getDataSchema(String itemID) {
        WorkItemRecord wir = _workItemCache.get(itemID);
        if (wir != null) {
            return getDataSchema(wir, new YSpecificationID(wir));
        }
        else return "<failure>Unknown workitem ID '" + itemID + "'.</failure>";
    }

    
    public String getDataSchema(WorkItemRecord wir, YSpecificationID specID) {
        String result = null ;
        try {
            Map<String, Element> schemaMap = getSpecificationDataSchema(specID);
            TaskInformation taskInfo = getTaskInformation(specID, wir.getTaskID(),
                                                          getEngineSessionHandle());
      //      SpecificationData specData = this.getSpecData(specID);
     //       result = new DataSchemaProcessor().createSchema(specData, taskInfo);
            result = new DataSchemaBuilder(schemaMap).build(taskInfo);
        }
        catch (Exception e) {
            _log.error("Could not retrieve schema for workitem parameters", e)  ;
        }
        return result ;
    }


    public Map<String, FormParameter> getCaseInputParams(YSpecificationID spec) {
        Map<String, FormParameter> result = new HashMap<String, FormParameter>();
        SpecificationData specData = getSpecData(spec);
        if (specData != null) {
            List<YParameter> inputs = specData.getInputParams();
            for (YParameter input : inputs) {
                FormParameter param = new FormParameter(input);
                result.put(param.getName(), param);
            }
        }
        return result;
    }


    public Map<String, Element> getSpecificationDataSchema(YSpecificationID specID)
            throws IOException {
        if (! _dataSchemaCache.contains(specID)) {
            String schema = _interfaceBClient.getSpecificationDataSchema(specID,
                        getEngineSessionHandle());
            if (schema != null) {
                _dataSchemaCache.add(specID, schema);
            }
        }
        return _dataSchemaCache.getSchemaMap(specID);
    }


    public String getInstanceData(String schema, YSpecificationID specID) {
        String result = null;
        SpecificationData specData = getSpecData(specID);
        if (specData != null) {
            result = new DataSchemaProcessor()
                                .getInstanceData(schema, specData.getRootNetID(), null);
        }
        return result ;
    }

    
    public String getInstanceData(String schema, WorkItemRecord wir) {
        String result = null;
        try {
            TaskInformation taskInfo = getTaskInformation(new YSpecificationID(wir),
                                                          wir.getTaskID(),
                                                          getEngineSessionHandle());
            if (taskInfo != null)
               result = new DataSchemaProcessor()
                                .getInstanceData(schema, taskInfo.getDecompositionID(),
                                                 wir.getDataListString());
        }
        catch (IOException ioe) {
            result = null ;
        }
        return result ;
    }


    public boolean assignUnofferedItem(WorkItemRecord wir, String[] pidList,
                                        String action) {
        boolean result = true ;
        if (wir != null) {
            if (action.equals("Start")) {

                // exactly one participant can have a workitem started 
                String status = wir.getStatus();
                if (status.equals(WorkItemRecord.statusEnabled) ||
                    status.equals(WorkItemRecord.statusFired)) {

                    Participant p = _orgDataSet.getParticipant(pidList[0]);
                    result = start(p, wir);
                }
                else {
                    _log.error("Unable to start workitem due to invalid status: " + status);
                    result = false;
                }
                
                // if could not start, fallback to allocate action
                if (! result) action = "Allocate";
            }

            _resAdmin.assignUnofferedItem(wir, pidList, action) ;
        }
        return result;
    }

    
    public void addToOfferedSet(WorkItemRecord wir, Participant p) {
        ResourceMap rMap = getResourceMap(wir);
        if (rMap != null)
            rMap.addToOfferedSet(wir, p);
    }


    public void reassignWorklistedItem(WorkItemRecord wir, String[] pidList,
                                                           String action) {
        removeFromAll(wir) ;

        // a reoffer can be made to several participants
        if (action.equals("Reoffer")) {            
            ResourceMap rMap = getResourceMap(wir);
            if (rMap != null) {
                if (wir.getResourceStatus().equals(WorkItemRecord.statusResourceOffered)) {
                    rMap.withdrawOffer(wir);
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
        }
        else {
            // a reallocate or restart is made to exactly one participant
            Participant p = _orgDataSet.getParticipant(pidList[0]);
            if (action.equals("Reallocate")) {
                wir.resetDataState();
                wir.setResourceStatus(WorkItemRecord.statusResourceAllocated);
                p.getWorkQueues().addToQueue(wir, WorkQueue.ALLOCATED);
            }
            else if (action.equals("Restart")) {
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


    public WorkItemRecord unMarshallWIR(String xml) {
        return Marshaller.unmarshalWorkItem(xml);
    }


    private boolean isAutoTask(WorkItemRecord wir) {
        return ((wir != null) &&
                (wir.getRequiresManualResourcing() != null) &&
                (wir.getRequiresManualResourcing().equalsIgnoreCase("false")));
    }


    private void handleAutoTask(WorkItemRecord wir, boolean timedOut) {

        // if this autotask has started a timer, don't process now - wait for timeout
        if ((! timedOut) && (wir.getTimerTrigger() != null)) return;

        synchronized(_mutex) {

            // check out the auto workitem
            if (checkOutWorkItem(wir)) {
                List children = getChildren(wir.getID());

                if ((children != null) && (! children.isEmpty())) {
                    wir = (WorkItemRecord) children.get(0) ;  // get executing child
                    processAutoTask(wir, true);
                }
            }
            else _log.error("Could not check out automated workitem: " + wir.getID());
        }
    }


    private void processAutoTask(WorkItemRecord wir, boolean init) {
        try {
            String codelet = wir.getCodelet();

            // if wir has a codelet, execute it in its own thread
            if ((codelet != null) && (codelet.length() > 0)) {
                TaskInformation taskInfo = getTaskInformation(new YSpecificationID(wir),
                                                              wir.getTaskID(),
                                                              getEngineSessionHandle());
                if (taskInfo != null) {
                    CodeletRunner runner = new CodeletRunner(wir, taskInfo, init);
                    Thread runnerThread = new Thread(runner, wir.getID() + ":codelet");
                    runnerThread.start();                     // will callback when done
                    if (_persisting && runner.persist()) persistAutoTask(wir, true);
                    _codeletRunners.put(wir.getID(), runner);
                }
                else {
                    _log.error(MessageFormat.format(
                            "Could not run codelet ''{0}'' for workitem ''{1}'' - error " +
                            "getting task information from engine. Codelet ignored.",
                            codelet, wir.getID()));
                    checkInAutoTask(wir, wir.getDataList());     // check in immediately
                }
            }
            else {
                checkInAutoTask(wir, wir.getDataList());     // check in immediately
            }
        }
        catch (Exception e) {
            _log.error("Exception attempting to execute automatic task: " +
                    wir.getID(), e);
        }
    }


    private void persistAutoTask(WorkItemRecord wir, boolean isSaving) {
        if (isSaving) {
            new PersistedAutoTask(wir);
        }
        else {
            PersistedAutoTask task = (PersistedAutoTask) _persister.selectScalar(
                                                 "PersistedAutoTask", wir.getID());
            if (task != null) task.unpersist();
        }
    }


    private void restoreAutoTasks() {
        if (_persisting) {
            List tasks = _persister.select("PersistedAutoTask");
            for (Object o : tasks) {
                PersistedAutoTask task = (PersistedAutoTask) o;
                WorkItemRecord wir = task.getWIR();
                if (wir != null) {
                    persistAutoTask(wir, false);             // remove persisted
                    processAutoTask(wir, false);             // resume processing task
                }
            }
        }
    }


    // callback method from CodeletRunner when codelet execution completes
    public void handleCodeletCompletion(WorkItemRecord wir, Element codeletResult) {
        if (_codeletRunners.remove(wir.getID()) != null) {
            if (codeletResult != null) {
                codeletResult = updateOutputDataList(wir.getDataList(), codeletResult);
            }
            Element outData = (codeletResult != null) ? codeletResult : wir.getDataList();
            checkInAutoTask(wir, outData);
        }
        else {
            _log.warn("A codelet has completed for a non-existent workitem '" + wir.getID() +
                      "' - it was most likely cancelled during the codelet's execution.");
            if (_persisting) persistAutoTask(wir, false);
        }    
    }


    public void checkInAutoTask(WorkItemRecord wir, Element outData) {
        checkCacheForWorkItem(wir);     // won't be cached if this is a restored item
        try {
            if (_persisting) persistAutoTask(wir, false);
            String msg = checkInWorkItem(wir.getID(), wir.getDataList(),
                    outData, null, getEngineSessionHandle()) ;
            if (successful(msg)) {
                _log.info("Automated task '" + wir.getID() +
                        "' successfully processed and checked back into the engine.");
            }
            else
                _log.error("Automated task '" + wir.getID() +
                        "' could not be successfully completed. Result message: " + msg) ;
        }
        catch (Exception e) {
            _log.error("Exception attempting to check-in automatic task: " +
                    wir.getID(), e);
        }
    }


    /** updates the input datalist with the changed data in the output datalist
     *  @param in - the JDOM Element containing the input params
     *  @param out - the JDOM Element containing the output params
     *  @return a JDOM Element with the data updated
     */
    private Element updateOutputDataList(Element in, Element out) {

         // get a copy of the 'in' list
         Element result = (Element) in.clone() ;

         // for each child in 'out' list, get its value and copy to 'in' list
         for (Object o : (out.getChildren())) {
             Element e = (Element) o;

             // if there's a matching 'in' data item, update its value
             Element resData = result.getChild(e.getName());
             if (resData != null) {
                 if (resData.getContentSize() > 0) resData.setContent(e.cloneContent()) ;
                 else resData.setText(e.getText());
             }
             else {
                 result.addContent((Element) e.clone()) ;
             }
         }

         return result ;
   }


   public String getMIFormalInputParamName(WorkItemRecord wir) {
       String result = null;
       if (canAddNewInstance(wir)) {
           try {
               TaskInformation taskInfo = getTaskInformation(new YSpecificationID(wir),
                                        wir.getTaskID(), getEngineSessionHandle());
               YParameter formalInputParam = taskInfo.getParamSchema().getFormalInputParam();
               if (formalInputParam != null) {
                   result = formalInputParam.getName();
               }
           }
           catch (IOException ioe) {
               // nothing to do
           }
       }
       return result;
   }


   public boolean canAddNewInstance(WorkItemRecord wir) {
       try {
           return successful(_interfaceBClient.checkPermissionToAddInstances(
                                     wir.getID(), getEngineSessionHandle()));
       }
       catch (IOException ioe) {
           return false;
       }
   }


   public WorkItemRecord createNewWorkItemInstance(String id, String value) {
       WorkItemRecord result = null;
       try {
           String xml = _interfaceBClient.createNewInstance(id, value, getEngineSessionHandle());
           if (successful(xml)) {
               result = Marshaller.unmarshalWorkItem(StringUtil.unwrap(xml));
               _workItemCache.add(result);
           }
           else _log.error(xml);
       }
       catch (IOException ioe) {
           // nothing to do
       }
       return result;
   }


    public String getWorkItemDurationsForParticipant(YSpecificationID specID,
                                                     String taskName, String pid) {
        LogMiner miner = LogMiner.getInstance();
        return miner.getWorkItemDurationsForParticipant(specID, taskName, pid);
    }    
}                                                                                  
