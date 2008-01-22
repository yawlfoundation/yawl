/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing;

import org.yawlfoundation.yawl.authentication.User;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.engine.interfce.interfaceE.YLogGatewayClient;
import org.yawlfoundation.yawl.exceptions.YAWLException;
import org.yawlfoundation.yawl.resourcing.allocators.AllocatorFactory;
import org.yawlfoundation.yawl.resourcing.constraints.ConstraintFactory;
import org.yawlfoundation.yawl.resourcing.datastore.HibernateEngine;
import org.yawlfoundation.yawl.resourcing.datastore.WorkItemCache;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.DataSource;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.DataSourceFactory;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.HibernateImpl;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;
import org.yawlfoundation.yawl.resourcing.filters.FilterFactory;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.rsInterface.ConnectionCache;
import org.yawlfoundation.yawl.resourcing.rsInterface.Docket;
import org.yawlfoundation.yawl.resourcing.jsf.FormParameter;
import org.yawlfoundation.yawl.resourcing.jsf.ApplicationBean;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.engine.interfce.TaskInformation;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.JDOMException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * The ResourceManager singleton manages all aspects of the resource perspective,
 * including the loading & maintenance of the org model, and overseeing the distribution
 * of tasks to participants.
 *
 *  @author Michael Adams
 *  v0.1, 03/08/2007
 */

public class ResourceManager extends InterfaceBWebsideController
                             implements Serializable {

    // store of organisational resources and their attributes
    private DataSource.ResourceDataSet _ds ;

    private WorkItemCache _workItemCache = new WorkItemCache();
    private HashMap<String,String> _userKeys = new HashMap<String,String>();

    // a cache of connections directly to the service - not to the engine
    private ConnectionCache _connections = ConnectionCache.getInstance();

    // currently logged on participants: <sessionHandle, Participant>
    private HashMap<String,Participant> _liveSessions =
            new HashMap<String,Participant>();

    private static ResourceManager _me ;                  // instance reference
    private ResourceAdministrator _resAdmin ;             // admin capabilities
    private DataSource _orgdb;                            // the org model db i'face
    private Persister _persister;                         // persist changes to db
    private Logger _log ;                                 // debug log4j file
    private boolean _persisting ;                         // flag to enable persistence
    private boolean _isNonDefaultOrgDB ;                  // flag for non-yawl org model

    private Timer _orgDataRefreshTimer;               // if set, reloads db at intervals

    private boolean _serviceEnabled = true ;          // will disable if no participants
    public static boolean serviceInitialised = false ;    // flag for init on restore

    private ApplicationBean _jsfApplicationReference ;   // ref to jsf app manager bean

    public boolean _logOffers ;

    // authority for write access to org data entities
    private boolean[] _dsEditable = {true, true, true, true, true} ;

    // Mappings for spec -> task <-> resourceMap
    private HashMap<String,HashMap<String,ResourceMap>> _specTaskResMap = 
        new HashMap<String,HashMap<String,ResourceMap>>() ;

    // required data members for interfacing with the engine
    private String _user = "resourceService" ;
    private String _password = "resource" ;
    private String _adminUser = "admin" ;
    private String _adminPassword = "YAWL" ;
    private String _engineSessionHandle = null ;
    private String _engineURI ;
    private String _xformsURI ;
    private Namespace _yNameSpace =
            Namespace.getNamespace("http://www.yawlfoundation.org/yawlschema");

    private InterfaceA_EnvironmentBasedClient _interfaceAClient ;
    private YLogGatewayClient _interfaceEClient;


    // Constructor - initialises references to engine and database(s), and loads org data.
    // Called exclusively by getInstance()
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


    public void initOrgDataSource(String dataSourceClassName, int refreshRate) {
        // get correct ref to org data backend
        _orgdb = DataSourceFactory.getInstance(dataSourceClassName);

        // set flag to true if the org model db backend is not the default
        _isNonDefaultOrgDB =
                ! (_orgdb.getClass().getSimpleName().equalsIgnoreCase("HibernateImpl"));

        // load all org data into the resources dataset
        loadResources() ;

        // set refresh rate if required
        if (refreshRate > 0) startOrgDataRefreshTimer(refreshRate);

    }

    public void initEngineURI(String uri) {
        _engineURI = uri ;
        _interfaceAClient = new InterfaceA_EnvironmentBasedClient(
                                                 _engineURI.replaceFirst("/ib", "/ia"));
        _interfaceEClient = new YLogGatewayClient(
                                         _engineURI.replaceFirst("/ib", "/logGateway"));
    }

    public void setXFormsURI(String uri) { _xformsURI = uri ; }

    public String getXFormsURI() { return _xformsURI ; }

    public void finaliseInitialisation() {
        EventLogger.setLogging(
            HibernateEngine.getInstance(false).isAvailable(HibernateEngine.tblEventLog));
        _workItemCache.setPersist(_persisting) ;
        if (_persisting) restoreWorkQueues() ;
    }


    public WorkItemCache getWorkItemCache() { return _workItemCache ; }

    public void registerJSFApplicationReference(ApplicationBean app) {
        _jsfApplicationReference = app;
    }

    /*********************************************************************************/

    // Interface B implemented methods //

    public void handleEnabledWorkItemEvent(WorkItemRecord wir) {
        System.out.println(wir.toXML());
        if (_serviceEnabled) {
            ResourceMap rMap = getResourceMap(wir) ;
            if (rMap != null)
                wir = rMap.distribute(wir) ;            
            else
                wir = offerToAll(wir) ;        // only when no resourcing spec for item
        }

        // service disabled, so route directly to admin's unoffered
        else _resAdmin.getWorkQueues().addToQueue(wir, WorkQueue.UNOFFERED);

        _workItemCache.add(wir);        
    }


    public void handleCancelledWorkItemEvent(WorkItemRecord wir) {
        if (_serviceEnabled) {
            List<WorkItemRecord> itemsToRemove = new ArrayList<WorkItemRecord>() ;
            itemsToRemove.add(wir) ;

            // add list of child items (if any) to parent (if necessary)
            if (wir.getStatus().equals(WorkItemRecord.statusIsParent))
                itemsToRemove.addAll(getChildren(wir.getID())) ;

            // remove items from all queues and cache
            for (WorkItemRecord item : itemsToRemove) {
                removeFromAll(item) ;
                _workItemCache.remove(item);
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


    /*********************************************************************************/


    public void handleCompletedWorkItemEvent(WorkItemRecord wir) {
        _workItemCache.remove(wir);
    }

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

   /******************************************************************************/

    // LOGGING METHODS //

    public void setOfferLogging(boolean log) { _logOffers = log ; }

   /******************************************************************************/

    // ORG DATA METHODS //

    public boolean mayEditDataset(String dsName) {
       if (dsName.equalsIgnoreCase("participant")) return _dsEditable[0] ;
       else if (dsName.equalsIgnoreCase("role")) return _dsEditable[1] ;
       else if (dsName.equalsIgnoreCase("capability")) return _dsEditable[2] ;
       else if (dsName.equalsIgnoreCase("position")) return _dsEditable[3] ;
       else if (dsName.equalsIgnoreCase("orggroup")) return _dsEditable[4] ;
       return false ;                                      // should not be reachable
   }


    /** Loads all the org data from db into the ResourceDataSet mappings */
    public void loadResources() {
        _ds = _orgdb.loadResources() ;

        // complete mappings for non-default org data backends
        if (_isNonDefaultOrgDB) finaliseNonDefaultLoad() ;

        // rebuild a work queue set and userid keymap for each participant
        for (Participant p : _ds.participantMap.values()) {
            p.createQueueSet(_persisting) ;
            _userKeys.put(p.getUserID(), p.getID()) ;
        }

        _resAdmin.createWorkQueues(_persisting);   // ... and the administrator
    }


    private void finaliseNonDefaultLoad() {
        HibernateImpl yawlDB = new HibernateImpl() ;
        
        // for each entity set not supplied by the backend, load the service defaults.
        // At a minimum, the datasource must supply a set of participants
        if (_ds.participantMap.isEmpty()){
            _log.error("Participant set not loaded - service will disable.") ;
            _serviceEnabled = false ;
            return ;
        }
        else _dsEditable[0] = false ;          // external data - do not allow modify

        // check roles
        if (_ds.roleMap.isEmpty())
            _ds.roleMap = yawlDB.loadRoles();
        else _dsEditable[1] = false ;

        // check capbilities
        if (_ds.capabilityMap.isEmpty())
            _ds.capabilityMap = yawlDB.loadCapabilities();
        else _dsEditable[2] = false ;

        // check orgGroups
        if (_ds.orgGroupMap.isEmpty())
            _ds.orgGroupMap = yawlDB.loadOrgGroups();
        else _dsEditable[3] = false ;

        // check positions
        if (_ds.positionMap.isEmpty())
            _ds.positionMap = yawlDB.loadPositions();
        else _dsEditable[4] = false ;

        // restore user privileges for each participant
        HashMap<String,UserPrivileges> upMap =
                _persister.selectMap(HibernateEngine.tblUserPrivileges) ;
        for (Participant p : _ds.participantMap.values()) {
            UserPrivileges up = upMap.get(p.getID());
            if (up != null) p.setUserPrivileges(up);
            else p.setUserPrivileges(new UserPrivileges(p.getID()));
        }
    }


    private void restoreWorkQueues() {
        _workItemCache.restore() ;

        // restore the queues to their owners
        List<WorkQueue> qList = _persister.select("WorkQueue") ;

        if (qList != null) {
            for (WorkQueue wq : qList) {
                if (wq.getOwnerID().equals("admin"))
                    _resAdmin.restoreWorkQueue(wq, _workItemCache, _persisting);
                else {
                    Participant p = _ds.participantMap.get(wq.getOwnerID()) ;
                    p.restoreWorkQueue(wq, _workItemCache, _persisting);
                }
            }
        }
    }


    // ADD (NEW) ORG DATA OBJECTS //

    /**
     * Adds a new participant to the Resource DataSet, and persists it also
     * @param p the new Participant
     */
    public void addParticipant(Participant p) {

        // persist it to the data store
        String newID = _orgdb.insert(p) ;
        p.setPersisting(_persisting);
        p.createQueueSet(_persisting) ;

        // cleanup for non-default db
        if (_isNonDefaultOrgDB) {
            p.setID(newID);
            if (_persisting) {
                _persister.insert(p.getUserPrivileges());
                _persister.insert(p.getWorkQueues());
            }    
        }
        else _orgdb.update(p);

        // ...and add it to the data set
        _ds.participantMap.put(newID, p) ;
    }


    public void addRole(Role r) {
        r.setPersisting(_persisting);
        String newID = _orgdb.insert(r) ;             // persist it
        if (_isNonDefaultOrgDB) r.setID(newID);       // cleanup for non-default db
        _ds.roleMap.put(newID, r) ;                   // ...and add it to the data set
    }


    public void addCapability(Capability c) {
        c.setPersisting(_persisting);
        String newID = _orgdb.insert(c) ;             // persist it
        if (_isNonDefaultOrgDB) c.setID(newID);       // cleanup for non-default db
        _ds.capabilityMap.put(newID, c) ;             // ...and add it to the data set
    }


    public void addPosition(Position p) {
        p.setPersisting(_persisting);
        String newID = _orgdb.insert(p) ;             // persist it
        if (_isNonDefaultOrgDB) p.setID(newID);       // cleanup for non-default db
        _ds.positionMap.put(newID, p) ;               // ...and add it to the data set
    }


    public void addOrgGroup(OrgGroup o) {
        o.setPersisting(_persisting);
        String newID = _orgdb.insert(o) ;             // persist it
        if (_isNonDefaultOrgDB) o.setID(newID);       // cleanup for non-default db
        _ds.orgGroupMap.put(newID, o) ;               // ...and add it to the data set
    }


    // UPDATE ORG DATA OBJECTS //

    public void updateParticipant(Participant p) {
        _orgdb.update(p);                              // persist it
        _ds.participantMap.put(p.getID(), p) ;         // ... and update the data set
        if (_isNonDefaultOrgDB) {
            _persister.update(p.getUserPrivileges());  // persist other classes
            _persister.update(p.getWorkQueues());
        }
    }

    public void updateResourceAttribute(Object obj) {
        if (obj instanceof Role) updateRole((Role) obj);
        else if (obj instanceof Capability) updateCapability((Capability) obj);
        else if (obj instanceof Position) updatePosition((Position) obj);
        else if (obj instanceof OrgGroup) updateOrgGroup((OrgGroup) obj);
    }

    public void updateRole(Role r) {
        _orgdb.update(r) ;                             // persist it
        _ds.roleMap.put(r.getID(), r) ;                // ... and update the data set
    }


    public void updateCapability(Capability c) {
        _orgdb.update(c) ;                             // persist it
        _ds.capabilityMap.put(c.getID(), c) ;          // ... and update the data set
    }


    public void updatePosition(Position p) {
        _orgdb.update(p) ;                             // persist it
        _ds.positionMap.put(p.getID(), p) ;            // ... and update the data set
    }


    public void updateOrgGroup(OrgGroup o) {
        _orgdb.update(o) ;                             // persist it
        _ds.orgGroupMap.put(o.getID(), o) ;            // ... and update the data set
    }


    // REMOVE ORG DATA OBJECTS //

    public void removeParticipant(Participant p) {
        _ds.participantMap.remove(p.getID()) ;
        _orgdb.delete(p);
        if (_isNonDefaultOrgDB) {
            _persister.delete(p.getUserPrivileges());
            _persister.delete(p.getWorkQueues());
        }
    }

    public void removeRole(Role r) {
        _ds.roleMap.remove(r.getID());
        _orgdb.delete(r);
    }


    public void removeCapability(Capability c) {
        _ds.capabilityMap.remove(c.getID());
        _orgdb.delete(c);
    }

    public void removePosition(Position p) {
        _ds.positionMap.remove(p.getID());
        _orgdb.delete(p);
    }

    public void removeOrgGroup(OrgGroup o) {
        _ds.orgGroupMap.remove(o.getID());
        _orgdb.delete(o);
    }


    // RETRIEVAL METHODS //

    public String getParticipantsAsXML() {
        StringBuilder xml = new StringBuilder("<participants>") ;
        for (Participant p : _ds.participantMap.values()) xml.append(p.getSummaryXML()) ;
        xml.append("</participants>");
        return xml.toString() ;
    }


    public String getActiveParticipantsAsXML() {
        StringBuilder xml = new StringBuilder("<participants>") ;
        for (Participant p : _liveSessions.values()) xml.append(p.getSummaryXML()) ;
        xml.append("</participants>");
        return xml.toString() ;
    }

    
    public String getRolesAsXML() {
        StringBuilder xml = new StringBuilder("<roles>") ;
        for (Role r : _ds.roleMap.values()) xml.append(r.getSummaryXML()) ;
        xml.append("</roles>");
        return xml.toString() ;
    }


    public String getParticipantRolesAsXML(String pid) {
        Set<Role> roles = getParticipantRoles(pid);
        if (roles != null) {
            String header = String.format("<roles participantid=\"%s\">", pid);
            StringBuilder xml = new StringBuilder(header) ;
            for (Role r : roles) xml.append(r.getSummaryXML()) ;
            xml.append("</roles>");
            return xml.toString() ;
        }
        else return("<roles/>") ;   
    }


    public String getCapabilitiesAsXML() {
        StringBuilder xml = new StringBuilder("<capabilities>") ;
        for (Capability c : _ds.capabilityMap.values()) xml.append(c.getSummaryXML()) ;
        xml.append("</capabilities>");
        return xml.toString() ;
    }

    public String getPositionsAsXML() {
        StringBuilder xml = new StringBuilder("<positions>") ;
        for (Position p : _ds.positionMap.values()) xml.append(p.getSummaryXML()) ;
        xml.append("</positions>");
        return xml.toString() ;
    }

    public String getOrgGroupsAsXML() {
        StringBuilder xml = new StringBuilder("<orggroups>") ;
        for (OrgGroup o : _ds.orgGroupMap.values()) xml.append(o.getSummaryXML()) ;
        xml.append("</orggroups>");
        return xml.toString() ;
    }



    /**
     * @return a csv listing of the full name of each participant
     */
    public String getParticipantNames() {
        StringBuilder csvList = new StringBuilder() ;
        for  (Participant p : _ds.participantMap.values()) {
            if (csvList.length() > 0) csvList.append(",");
            csvList.append(p.getFullName()) ;
        }
        return csvList.toString();
    }


    /**
     * @return a csv listing of the full name of each participant
     */
    public String getRoleNames() {
        StringBuilder csvList = new StringBuilder() ;
        for (Role r : _ds.roleMap.values()) {
            if (csvList.length() > 0) csvList.append(",");
            csvList.append(r.getName()) ;
        }
        return csvList.toString();
    }


    public String getParticipantIDFromUserID(String userID) {
        System.out.println("getIDs: " + userID) ;
        for (String s : _userKeys.keySet())
            System.out.println("ID: " + s + " PID: " + _userKeys.get(s)) ;

        return _userKeys.get(userID) ;
    }


    public Participant getParticipantFromUserID(String userID) {
        String pid = getParticipantIDFromUserID(userID) ;
        if (pid != null)
           return _ds.participantMap.get(pid);
        else return null ;
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


    public HashSet<Participant> getParticipants() {
        if (_ds.participantMap == null) return null ;
        return new HashSet<Participant>(_ds.participantMap.values()) ;
    }

    public Participant getParticipant(String pid) {
       return _ds.participantMap.get(pid) ;
    }

    public boolean isKnownParticipant(Participant p) {
        return isKnownParticipant(p.getID());
    }

    public boolean isKnownParticipant(String pid) {
        return _ds.participantMap.containsKey(pid);
    }
    
    public boolean isKnownRole(Role r) {
        return isKnownRole(r.getID());
    }

    public boolean isKnownRole(String rid) {
        return _ds.roleMap.containsKey(rid);
    }

    public boolean isKnownCapability(String cid) {
        return _ds.capabilityMap.containsKey(cid);
    }

    public boolean isKnownPosition(String pid) {
        return _ds.positionMap.containsKey(pid);
    }

    public boolean isKnownOrgGroup(String oid) {
        return _ds.orgGroupMap.containsKey(oid);
    }


    public String getFullNameForUserID(String userID) {
        if (userID.equals("admin")) return "Administrator" ;

        Participant p = getParticipantFromUserID(userID) ;
        if (p != null)
           return p.getFullName();
        else
           return null ;
    }

    public HashSet<Role> getRoles() {
        if (_ds.roleMap == null) return null ;
        return new HashSet<Role>(_ds.roleMap.values()) ;
    }

    public HashSet<Position> getPositions() {
        if (_ds.positionMap == null) return null ;
        return new HashSet<Position>(_ds.positionMap.values()) ;
    }

    public HashSet<Capability> getCapabilities() {
        if (_ds.capabilityMap == null) return null ;
        return new HashSet<Capability>(_ds.capabilityMap.values()) ;
    }

    public HashSet<OrgGroup> getOrgGroups() {
        if (_ds.orgGroupMap == null) return null ;
        return new HashSet<OrgGroup>(_ds.orgGroupMap.values()) ;
    }

    public Set<Participant> getRoleParticipants(String rid) {
        Role r = _ds.roleMap.get(rid);
        if (r != null)
            return r.getResources() ;
        else
            return null ;
    }

    public Set<Participant> getCapabiltyParticipants(String cid) {
        Capability c = _ds.capabilityMap.get(cid);
        if (c != null)
            return c.getResources() ;
        else
            return null ;
    }

    public Set<Participant> getPositionParticipants(String pid) {
        Position p = _ds.positionMap.get(pid);
        if (p != null)
            return p.getResources() ;
        else
            return null ;
    }

    public Set<Capability> getParticipantCapabilities(String pid) {
        Participant p = _ds.participantMap.get(pid);
        if (p != null)
            return p.getCapabilities() ;
        else
            return null ;
    }

    public Set<Role> getParticipantRoles(String pid) {
        Participant p = _ds.participantMap.get(pid);
        if (p != null)
            return p.getRoles() ;
        else
            return null ;
    }

    public Set<Position> getParticipantPositions(String pid) {
        Participant p = _ds.participantMap.get(pid);
        if (p != null)
            return p.getPositions() ;
        else
            return null ;
    }

    /**
     * Gets the complete set of Participants that ultimately report to the
     * position(s) held by a Participant
     * @param pid the id of the 'manager' Participant
     * @return the set of Particpants 'managed' by this Participant
     */
    public Set<Participant> getParticipantsReportingTo(String pid) {
        Set<Participant> result = new HashSet<Participant>() ;
        Set<Position> posSet = getParticipantPositions(pid) ;
        for (Position pos : posSet) {
            result.addAll(getParticipantsReportingToPosition(pos)) ;
        }
        if (result.isEmpty()) result = null ;
        return result ;
    }

    /**
     * Gets the set of Participants the ultimately report to the Position passed
     * @param manager the 'manager' Position
     * @return the set of Particpants 'managed' by this Position
     */
    public Set<Participant> getParticipantsReportingToPosition(Position manager) {
        Set<Participant> result = new HashSet<Participant>() ;
        Set<Position> posSet = getPositions();
        for (Position pos : posSet) {
            if (pos.ultimatelyReportsTo(manager)) {
                Set temp = pos.getResources() ;
                for (Object o : temp)
                    result.add((Participant) o) ;
            }
        }
        return result ;
    }

    public Participant getParticpant(String pid) {
        return _ds.participantMap.get(pid);
    }

    public Role getRole(String rid) {
        return _ds.roleMap.get(rid);
    }

    public Capability getCapability(String cid) {
        return _ds.capabilityMap.get(cid);
    }

    public Position getPosition(String pid) {
        return _ds.positionMap.get(pid);
    }

    public OrgGroup getOrgGroup(String oid) {
        return _ds.orgGroupMap.get(oid);
    }


    public Namespace getNameSpace() {
        return _yNameSpace;
    }


    /***************************************************************************/

    // WORKITEM ALLOCATION AND WORKQUEUE METHODS //


    public WorkItemRecord offerToAll(WorkItemRecord wir) {
        for (Participant p : _ds.participantMap.values()) {
            p.getWorkQueues().addToQueue(wir, WorkQueue.OFFERED);
            announceModifiedQueue(p.getID()) ;
        }
        wir.setResourceStatus(WorkItemRecord.statusResourceOffered);
        return wir ;
    }

    public void withdrawOfferFromAll(WorkItemRecord wir) {
        for (Participant p : _ds.participantMap.values()) {
            p.getWorkQueues().removeFromQueue(wir, WorkQueue.OFFERED);
            announceModifiedQueue(p.getID()) ;
        }
    }


    public void removeFromAll(WorkItemRecord wir) {
        for (Participant p : _ds.participantMap.values()) {
            p.getWorkQueues().removeFromAllQueues(wir);
            announceModifiedQueue(p.getID()) ;
        }
        _resAdmin.removeFromAllQueues(wir);
    }


    public void acceptOffer(Participant p, WorkItemRecord wir) {
        ResourceMap rMap = getResourceMap(wir);
        if (rMap != null)
            rMap.withdrawOffer(wir);
        else
           withdrawOfferFromAll(wir);        // beta version spec

        wir.setResourceStatus(WorkItemRecord.statusResourceAllocated);
        p.getWorkQueues().addToQueue(wir, WorkQueue.ALLOCATED);
    }


    /**
     * moves the workitem to executing for the participant.
     *
     * Note that when an item is checked out of the engine, at least one child item
     * is spawned, and that is the item that executes (i.e. not the parent).
     *
     * @param p the participant starting the workitem
     * @param wir the item to start
     * @param handle the user's current sessionhandle
     */
    public void start(Participant p, WorkItemRecord wir, String handle) {
        WorkItemRecord oneToStart ;

        if (checkOutWorkItem(wir, handle)) {

            // get all the child instances of this workitem
            List children = getChildren(wir.getID(), _engineSessionHandle);

            if (children.size() > 1) {                   // i.e. if multi atomic task

                // which one got started with the checkout?
                oneToStart = getExecutingChild(wir, children) ;

                // get the rest of the kids and distribute them
                distributeChildren(oneToStart, children) ;
            }
            else oneToStart = (WorkItemRecord) children.get(0) ;

            oneToStart.setResourceStatus(WorkItemRecord.statusResourceStarted);

            // replace the parent in the cache with the executing child
            _workItemCache.remove(wir) ;
            _workItemCache.add(oneToStart);

            p.getWorkQueues().movetoStarted(wir, oneToStart);
        }
        else _log.error("Could not start workitem: " + wir.getID()) ;
    }


    private void distributeChildren(WorkItemRecord started, List children) {

        // list should always have at least one member
        for (int i = 0; i < children.size(); i++) {
            WorkItemRecord child = (WorkItemRecord) children.get(i);

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
                    _interfaceBClient.suspendWorkItem(wir.getID(), _engineSessionHandle))) {
                    wir.setResourceStatus(WorkItemRecord.statusResourceSuspended);
                    p.getWorkQueues().movetoSuspend(wir);
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

        // if user can suspend they also have unsuspend privileges
        boolean success = false;
        try {

            // reset status before polling engine
            if (successful(
                _interfaceBClient.unsuspendWorkItem(wir.getID(), _engineSessionHandle))) {
                wir.setResourceStatus(WorkItemRecord.statusResourceStarted);
                p.getWorkQueues().movetoUnsuspend(wir);
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
            success = true ;
        }
        return success ;
    }

    
    public boolean reallocateStatefulWorkItem(Participant pFrom, Participant pTo,
                                               WorkItemRecord wir) {
        boolean success = false ;
        if (hasUserTaskPrivilege(pFrom, wir, TaskPrivileges.CAN_REALLOCATE_STATEFUL)) {
            reallocateWorkItem(pFrom, pTo, wir);
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
            rMap.ignore(p);                           // add Participant to ignore list
            rMap.distribute(wir);                     // redistribute workitem
            success = true ;
        }
        return success ;
    }


    public boolean delegateWorkItem(Participant pFrom, Participant pTo,
                                                       WorkItemRecord wir) {
        boolean success = false ;
        if (hasUserTaskPrivilege(pFrom, wir, TaskPrivileges.CAN_DELEGATE)) {
            pFrom.getWorkQueues().removeFromQueue(wir, WorkQueue.ALLOCATED);
            pTo.getWorkQueues().addToQueue(wir, WorkQueue.ALLOCATED);
            success = true ;
        }
        return success ;
    }


    public boolean skipWorkItem(Participant p, WorkItemRecord wir, String handle) {
        String result ;
        if (hasUserTaskPrivilege(p, wir, TaskPrivileges.CAN_SKIP)) {
            try {
                result = _interfaceBClient.skipWorkItem(wir.getID(), handle) ;
                if (successful(result)) {
                    p.getWorkQueues().removeFromQueue(wir, WorkQueue.ALLOCATED);
                    return true ;
                }
            }
            catch (IOException ioe) {
                return false ;
            }
        }
        return false ;
    }

    // ASSUMPTION: Piling applies to this task in this case instance only (not all cases)
    public boolean pileWorkItem(Participant p, WorkItemRecord wir) {
        boolean success = false ;
        if (hasUserTaskPrivilege(p, wir, TaskPrivileges.CAN_PILE)) {
            getResourceMap(wir).setPiledResource(p);
            success = true ;
        }
        return success;
    }


    public boolean hasUserTaskPrivilege(Participant p, WorkItemRecord wir,
                                        int privilege) {
        ResourceMap rMap = getResourceMap(wir);
        if (rMap != null)
            return rMap.getTaskPrivileges().hasPrivilege(p, privilege) ;
        else
           return false ;
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
    

    public void addResourceMap(String specID, String taskID, ResourceMap rMap) {
        HashMap<String,ResourceMap> taskMap = _specTaskResMap.get(specID) ;

        // if this is the first task added for this spec, create a new map
        if (taskMap == null)
            taskMap = new HashMap<String,ResourceMap>() ;

        taskMap.put(taskID, rMap) ;
        _specTaskResMap.put(specID, taskMap) ;
    }

    // removes a resource map for a particular task of a specification
    public void removeResourceMap(ResourceMap rMap) {
        String specID = rMap.getSpecID() ;
        HashMap<String,ResourceMap> taskMap = _specTaskResMap.get(specID) ;
        taskMap.remove(rMap.getTaskID()) ;
        _specTaskResMap.put(specID, taskMap) ;
    }

    // removes all resource maps for a specification
    public void removeResourceMapsForSpec(String specID) {
        _specTaskResMap.remove(specID) ;
    }

    public ResourceMap getResourceMap(WorkItemRecord wir) {
        return getResourceMap(wir.getSpecificationID(), wir.getTaskID()) ;
    }

    public ResourceMap getResourceMap(String specID, String taskID) {
        ResourceMap result = null;
        HashMap<String,ResourceMap> taskMap = _specTaskResMap.get(specID) ;
        if (taskMap != null) result = taskMap.get(taskID) ;

        // if we don't have a resource map for the task stored yet, let's make one
        if (result == null) {
            if (connected()) {
                try {
                    Element resElem = getResourcingSpecs(specID, taskID, _engineSessionHandle) ;

                    if ((resElem != null) &&
                         successful(JDOMUtil.elementToString(resElem))) {
                        // if (schemaValidate(resElem))

                        // strip 'response' tags - NOW DONE IN INT B
                      //  resElem = resElem.getChild("resourcing", _yNameSpace);
                      //  System.out.println("getResourceMap, got xml" );

                        result = new ResourceMap(specID, taskID, resElem) ;
                        addResourceMap(specID, taskID, result) ;
                    }
                }
                catch (IOException ioe) {
                    _log.error("Exception getting resource specs from Engine", ioe) ;
                }
            }
        }
        return result ;            // result = null if no resourcing spec for this task
    }



    public static void setServiceInitialised() { serviceInitialised = true ; }


    public Set<Participant> getWhoCompletedTask(String taskID, WorkItemRecord wir) {
        Set<Participant> result ; 

        // BUILD ADAPTER CLASS FOR LOG IE
        try {
            _interfaceEClient.getParentWorkItemEventsForCaseID(wir.getCaseID(),
                      _engineSessionHandle) ;
        }
        catch (IOException ioe) {
            _log.error("Connection to engine failed.", ioe);
            result = null ;
        }
        // return set of participants who completed workitems for this task
        return null ;
    }


     /**
     * get the workitem's (task) decomposition id
     * @param wir - the workitem to get the decomp id for
     */
     public String getDecompID(WorkItemRecord wir) {
         return getDecompID(wir.getSpecificationID(), wir.getTaskID());
     }

  //***************************************************************************//

    /**
     *  gets a task's decomposition id
     *  @param specID - the specification's id
     *  @param taskID - the task's id
     */
    public String getDecompID(String specID, String taskID) {

       try {
           TaskInformation taskinfo = getTaskInformation(specID, taskID, _engineSessionHandle);
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
     *  @param handle - the user's current sessionhandle
     *  @return true if checkout was successful
     */
    protected boolean checkOutWorkItem(WorkItemRecord wir, String handle) {
        if (connected()) {
            try {
                if (null != checkOut(wir.getID(), handle)) {
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
        _log.error("Could not connect to Engine to checkout: " + wir.getID());
        return false ;

    }

  //***************************************************************************//

    // re-adds checkedout item to local cache after a restore (if required)
    private void checkCacheForWorkItem(WorkItemRecord wir) {
        WorkItemRecord wiTemp = getCachedWorkItem(wir.getID());
        if (wiTemp == null) {

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
     *  @param handle - the sessionHndle of the current user
     *  @return true id checkin is successful
     */
    public boolean checkinItem(Participant p, WorkItemRecord wir, String handle) {

        try {
            wir = _workItemCache.get(wir.getID()) ;   // refresh wir

            if (wir != null) {
                Element outData = wir.getUpdatedData();
                if (outData == null) outData = wir.getDataList();
                checkCacheForWorkItem(wir);
                String result = checkInWorkItem(wir.getID(), wir.getDataList(),
                                                outData, handle) ;
                if (successful(result)) {
                    p.getWorkQueues().getQueue(WorkQueue.STARTED).remove(wir);
                    _workItemCache.remove(wir) ;
                    return true ;
                }
            }
        }
        catch (IOException ioe) {
            _log.error("checkinItem method caused java IO Exception", ioe) ;
        }
        catch (JDOMException jde) {
            _log.error("checkinItem method caused JDOM Exception", jde) ;
        }
        return false ;                                 // check-in unsucessful
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
              checkOutWorkItem(itemRec, null);
        }

        // update child item list after checkout (to capture status changes) & return
        return getChildren(wir.getID(), _engineSessionHandle);
    }


    private WorkItemRecord getExecutingChild(WorkItemRecord wir, List children) {
        for (int i = 0; i < children.size(); i++) {
           WorkItemRecord itemRec = (WorkItemRecord) children.get(i);

           // find the one that's executing
           if (WorkItemRecord.statusExecuting.equals(itemRec.getStatus()))
              return itemRec;
        }
        return null ;
    }



    /*************************
     * 9. CONNECTION METHODS *
     ************************/

    public String login(String userid, String password) {
        String result ;
        if (connected()) {
            if (userid.equals("admin")) return loginAdmin(password) ;
            
            Participant p = getParticipantFromUserID(userid) ;
            if (p != null) {
                if (p.getPassword().equals(password)) {
                    result = connectParticipant(userid, password) ;
                    if (successful(result)) _liveSessions.put(result, p) ;
                }
                else
                    result = "<failure>Incorrect Password</failure>" ;
            }
            else
                result = "<failure>Unknown user name</failure>" ;
        }
        else
            result = "<failure>Could not connect to YAWL Engine</failure>" ;

        return result ;
    }

    private String loginAdmin(String password) {
        String handle ;
        try {
            handle = connect("admin", password);
        }
        catch (IOException ioe) {
            _log.error("IOException trying to connect admin user to engine");
            handle = "<failure>Failed to connect to YAWL engine</failure>";
        }
        return handle ;
    }

    // pseudo-logout by removing session handle from map of live users
    public void logout(String handle) {
        _liveSessions.remove(handle);
    }

    public boolean isValidSession(String handle) {
        return _liveSessions.containsKey(handle) ;
    }


    /** Checks if there is a connection to the engine, and
     *  if there isn't, attempts to connect
     *  @return true if connected to the engine
     */
    protected boolean connected() {
        try {
            // if not connected
             if ((_engineSessionHandle == null) || (!checkConnection(_engineSessionHandle)))
                _engineSessionHandle = connectAsService();
        }
        catch (IOException ioe) {
             _log.error("Exception attempting to connect to engine", ioe);
        }
        return (successful(_engineSessionHandle)) ;
    }

//***************************************************************************//


    private String connectParticipant(String userid, String password) {
        String handle = null ;
        try {
            // create new user for service if necessary
            if (! isRegisteredUser(userid))
               _interfaceAClient.createUser(userid, password, false, _engineSessionHandle);

            handle = connect(userid, password);
        }
        catch (IOException ioe) {
            _log.error("IOException trying to connect user to engine: " + userid);
            handle = "<failure>Failed to connect to YAWL engine</failure>";
        }
        return handle ;

    }


    /**
     * Attempts to logon to the engine using a service id
     * @return  a sessionHandle for the connection
     * @throws IOException
     */
    private String connectAsService() throws IOException{

        // first connect as default admin user
        _engineSessionHandle = connect(_adminUser, _adminPassword);

        // create new user for service if necessary
        if (! isRegisteredUser(_user))
           _interfaceAClient.createUser(_user, _password, true, _engineSessionHandle);

        // logon with service user and return the result
        return connect(_user, _password);
    }

//***************************************************************************//

    /**
     * Checks if a user is currently registered for this session
     * @param user
     * @return true if the user is registered in the current session
     */
    private boolean isRegisteredUser(String user) {

        // check if service is a registered user
        ArrayList users = (ArrayList) _interfaceAClient.getUsers(_engineSessionHandle);

        Iterator itr = users.iterator();
           while (itr.hasNext()) {
           User u = (User) itr.next() ;
           if ( u.getUserID().equals(user) ) return true ;      // user in list
        }
        return false;                                       // user not in list
    }



    /**
     * Builds a WorkItemRecord from its representation as an XML String
     * @param xmlStr
     * @return the reconstructed WorkItemRecord
     */
    public WorkItemRecord xmlStringtoWIR(String xmlStr) {
        Element eWIR = JDOMUtil.stringToElement(xmlStr) ; // reform as Element

        String status = eWIR.getChildText("status");
        String specID = eWIR.getChildText("specid");
        String id = eWIR.getChildText("id");
        String[] idSplit = id.split(":");                      // id = taskid:caseid
        String taskName = getDecompID(specID, idSplit[0]);

        // call the wir constructor
        WorkItemRecord wir = new WorkItemRecord( idSplit[1], idSplit[0], specID,
                              null, status);

        // add data list if non-parent item
        Element data = eWIR.getChild("data").getChild(taskName) ;
        if (data != null) {
            data = (Element) data.detach() ;
            wir.setDataList(data);
        }
        return wir;
    }

    private class OrgDataRefresh extends TimerTask {
        public void run() { loadResources() ; }
    }

    /*******************************************************************************/

    public String serviceConnect(String userid, String password) {
        return _connections.connect(userid, password) ;
    }

    public void serviceDisconnect(String handle) {
        _connections.disconnect(handle) ;
    }

    public boolean checkServiceConnection(String handle) {
        return _connections.checkConnection(handle);
    }

    public Set<SpecificationData> getLoadedSpecs(String handle) {
        Set<SpecificationData> result = getSpecList(handle) ;
        if (result != null) {
            for (SpecificationData specData : result) {
                if (! specData.getStatus().equals(YSpecification._loaded))
                   result.remove(specData) ;
            }
        }
        return result ;
    }
    

    public Set<SpecificationData> getSpecList(String handle) {
        Set<SpecificationData> result = new HashSet<SpecificationData>() ;
        try {
            Iterator itr = getSpecificationPrototypesList(handle).iterator() ;
            while (itr.hasNext()) result.add((SpecificationData) itr.next()) ;
        }
        catch (IOException ioe) {
            _log.error("IO Exception retrieving specification list", ioe) ;
            result = null ;
        }
        return result ;
    }

    public SpecificationData getSpecData(String specID, String handle) {
        try {
            return getSpecificationData(specID, handle) ;
        }
        catch (IOException ioe) {
            _log.error("IO Exception retrieving specification data", ioe) ;
            return null ;
        }        
    }


    public List<String> getRunningCases(String specID, String handle) {
        try {
            String casesAsXML = _interfaceBClient.getCases(specID, handle);
            if (_interfaceBClient.successful(casesAsXML))
                return Marshaller.unmarshalCaseIDs(casesAsXML);
        }
        catch (IOException ioe) {
            _log.error("IO Exception retrieving running cases list", ioe) ;
        }
        return null;
    }

    public String uploadSpecification(String fileContents, String fileName, String handle) {
        return _interfaceAClient.uploadSpecification(fileContents, fileName, handle);         
    }

    
    public String cancelCase(String caseID, String handle) throws IOException {
        List<WorkItemRecord> liveItems = getLiveWorkItemsForCase(caseID, handle) ;

        // cancel the case in the engine
        String result =  _interfaceBClient.cancelCase(caseID, handle);

        // remove live items for case from workqueues and cache
        if (successful(result)) {
            for (WorkItemRecord wir : liveItems) {
                removeFromAll(wir) ;
                _workItemCache.remove(wir);
            }
        }
        else _log.error("Unable to remove workitems for Cancelled Case") ;

        return result ;
    }


    private List<WorkItemRecord> getLiveWorkItemsForCase(String caseID, String handle) {
        List<WorkItemRecord> result = null ;
        try {
            result = _interfaceBClient.getLiveWorkItemsForIdentifier("case", caseID,
                                                                         handle) ;

            // the above method only gets parents, so get any child items too
            for (WorkItemRecord wir : result) {
                List<WorkItemRecord> children = getChildren(wir.getID()) ;
                result.addAll(children) ;
            }
        }
        catch (Exception e) {
            _log.error("Exception attempting to retrieve work item list from engine");
        }
        return result;
    }

    private List<WorkItemRecord> getChildren(String parentID) {
        List<WorkItemRecord> result = new ArrayList<WorkItemRecord>();
        List children = getChildren(parentID, _engineSessionHandle) ;
        for (Object obj : children) result.add((WorkItemRecord) obj) ;
        return result ;
    }

    public String unloadSpecification(String specID, String handle) throws IOException {
        String result = _interfaceAClient.unloadSpecification(specID, handle);
        if (successful(result)) removeResourceMapsForSpec(specID) ;
        return result ;
    }
    
    public String launchCase(String specID, String caseData, String handle) throws IOException {
        return _interfaceBClient.launchCase(specID, caseData, handle) ;
    }

    public Map<String, FormParameter> getWorkItemParamsForPost(WorkItemRecord wir, String handle)
           throws IOException, JDOMException {
        Map<String, FormParameter> inputs, outputs;
        TaskInformation taskInfo = getTaskInformation(
                                   wir.getSpecificationID(), wir.getTaskID(), handle);

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
            wir = _workItemCache.get(wir);              // refresh data list if required
            itemData = wir.getUpdatedData() ;
        }
        else
            itemData = JDOMUtil.stringToElement(wir.getDataListString());

        for (String name : outputs.keySet()) {
            outputs.get(name).setValue(itemData.getChildText(name));
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
       // if (result.isEmpty()) result = null ;
        return result ;
    }

    private WorkItemRecord refreshWIRFromEngine(WorkItemRecord wir, String handle)
                                                    throws IOException, JDOMException {

            wir = getEngineStoredWorkItem(wir.getID(), handle);
            _workItemCache.update(wir) ;
            return wir ;

    }

    public void announceModifiedQueue(String pid) {
        if (_jsfApplicationReference != null) {
            _jsfApplicationReference.refreshUserWorkQueues(pid);
        }
    }

}                                                                                  
