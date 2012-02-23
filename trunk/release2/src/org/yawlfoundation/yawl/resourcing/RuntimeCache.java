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

package org.yawlfoundation.yawl.resourcing;

import org.jdom.Element;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.datastore.WorkItemCache;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.ResourceDataSet;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.rsInterface.ConnectionCache;
import org.yawlfoundation.yawl.resourcing.rsInterface.UserConnectionCache;
import org.yawlfoundation.yawl.resourcing.util.*;

import java.util.*;

/**
 * @author Michael Adams
 * @date 22/02/12
 */
public class RuntimeCache {

    // store of organisational resources and their attributes
    private ResourceDataSet _orgDataSet;

    // cache of 'live' workitems
    private WorkItemCache _workItemCache = WorkItemCache.getInstance();

    // map of userid -> participant id
    private Map<String, String> _userKeys = new Hashtable<String,String>();

    // a cache of connections directly to the service from client apps & services
    private ConnectionCache _connections = ConnectionCache.getInstance();

    // currently logged on participants
    private UserConnectionCache _liveSessions = new UserConnectionCache();

    // local cache of specifications: id -> SpecificationData
    private SpecDataCache _specCache = new SpecDataCache();

    // local cache of specification data schemas: id -> [name, schema defn. element]
    private DataSchemaCache _dataSchemaCache = new DataSchemaCache();

    // groups of items that are members of a deferred choice offering
    private Map<String, TaggedStringList> _deferredItemGroups = 
            new Hashtable<String, TaggedStringList>();

    // cases that have workitems chained to a participant: <caseid, Participant>
    private Map<String, Participant> _chainedCases = new Hashtable<String, Participant>();

    // cache of who completed tasks, for four-eyes and retain familiar use <caseid, cache>
    private Map<String, FourEyesCache> _taskCompleters =
            new Hashtable<String, FourEyesCache>();

    // map of workitem id -> CodeletRunner running codelet for it
    private Map<String, CodeletRunner> _codeletRunners =
            new Hashtable<String, CodeletRunner>();

    // started workitems that have been restored for a no longer existing participant.
    // these are force-completed once start-up has completed
    private List<WorkItemRecord> _orphanedStartedItems ;

    // a cache of delayed launches, sorted on launch time
    private SortedSet<DelayedLaunchRecord> _delayedLaunches =
            Collections.synchronizedSortedSet(new TreeSet<DelayedLaunchRecord>());


    public RuntimeCache() { }


    protected ResourceDataSet getOrgDataSet() { return _orgDataSet; }

    protected void setOrgDataSet(ResourceDataSet dataSet) { _orgDataSet = dataSet; }


    /*****************************************************************************/

    protected void addUserKey(Participant p) { _userKeys.put(p.getUserID(), p.getID()); }

    protected void removeUserKey(Participant p) { removeUserKey(p.getUserID()); }

    protected void removeUserKey(String userKey) { _userKeys.remove(userKey); }

    protected boolean isKnownUserID(String userid) { return _userKeys.containsKey(userid); }

    protected String getParticipantIDFromUserID(String userID) { return _userKeys.get(userID); }


    /*****************************************************************************/

    protected TaggedStringList addDeferredItemGroup(TaggedStringList group) {
        return _deferredItemGroups.put(group.getTag(), group);
    }

    protected TaggedStringList removeDeferredItemGroup(TaggedStringList group) {
        return _deferredItemGroups.remove(group.getTag());
    }

    protected TaggedStringList getDeferredItemGroup(String groupID) {
        return _deferredItemGroups.get(groupID);
    }

    
    /*****************************************************************************/
    
    protected Participant addChainedCase(String caseID, Participant p) {
        return _chainedCases.put(caseID, p);
    }
    
    protected boolean isChainedCase(String caseID) {
        return _chainedCases.containsKey(caseID);
    }

    protected boolean isChainedParticipant(Participant p) {
        for (Participant chainer : _chainedCases.values()) {
             if (chainer.getID().equals(p.getID())) return true;
        }
        return false;
    }

    protected Participant getChainedParticipant(String caseID) {
        return _chainedCases.get(caseID);
    }

    public Set<String> getChainedCaseIDsForParticipant(Participant p) {
        Set<String> result = new HashSet<String>();
        for (String caseID : _chainedCases.keySet()) {
            Participant chainer = _chainedCases.get(caseID);
            if (chainer.getID().equals(p.getID()))
                result.add(caseID);
        }
        return result;
    }

    
    protected Set<String> getChainedCaseIDs() { return _chainedCases.keySet(); }
    
    protected Participant removeChainedCase(String caseID) {
        return _chainedCases.remove(caseID);
    }

    protected void removeChainedCasesForParticpant(Participant p) {
        List<String> caseList = new ArrayList<String>();
        for (String caseID : _chainedCases.keySet()) {
            Participant chainer = _chainedCases.get(caseID);
            if (chainer.getID().equals(p.getID()))
                caseList.add(caseID) ;
        }
        for (String caseID : caseList) removeChainedCase(caseID);
    }


    /******************************************************************************/
    
    protected void addTaskCompleter(Participant p, WorkItemRecord wir) {
        String caseID = wir.getRootCaseID();
        FourEyesCache cache = _taskCompleters.get(caseID);
        if (cache == null) {
            cache = new FourEyesCache(caseID);
            _taskCompleters.put(caseID, cache);
        }
        cache.addCompleter(wir.getTaskID(), p);
    }


    protected Set<Participant> getTaskCompleters(String taskID, String caseID) {
        Set<Participant> result = new HashSet<Participant>();
        FourEyesCache cache = _taskCompleters.get(caseID);
        if (cache != null) result = cache.getCompleters(taskID);
        return result ;
    }


    protected void removeTaskCompleter(Participant p, WorkItemRecord wir) {
        FourEyesCache cache = _taskCompleters.get(wir.getRootCaseID());
        if (cache != null) cache.removeCompleter(wir.getTaskID(), p);
    }


    protected void removeCaseFromTaskCompleters(String caseid) {
        _taskCompleters.remove(caseid);
    }


    /*************************************************************************/

    protected CodeletRunner addCodeletRunner(String itemID, CodeletRunner runner) {
        return _codeletRunners.put(itemID, runner);
    }
    
    protected CodeletRunner removeCodeletRunner(String itemID) {
        return _codeletRunners.remove(itemID);
    }
    
    protected void cancelCodeletRunner(String itemID) {
        CodeletRunner runner = removeCodeletRunner(itemID);
        if (runner != null) runner.cancel();
    }

    protected void cancelCodeletRunnersForCase(String caseID) {
        Set<String> toRemove = new HashSet<String>();     // avoid concurrency exception
        for (String wirID : _codeletRunners.keySet()) {
             if (wirID.startsWith(caseID + ".")) {
                 toRemove.add(wirID);
             }
        }
        for (String wirID : toRemove) {
            cancelCodeletRunner(wirID);
        }
    }

    protected void shutdownCodeletRunners() {
        for (CodeletRunner runner : _codeletRunners.values()) {
            runner.shutdown();
        }
    }


    /***************************************************************************/

    protected void addOrphanedItem(WorkItemRecord wir) {
        if (_orphanedStartedItems == null) {
            _orphanedStartedItems = new ArrayList<WorkItemRecord>();
        }
        _orphanedStartedItems.add(wir);
    }

    protected List<WorkItemRecord> getOrphanedItems() {
        return _orphanedStartedItems != null ? _orphanedStartedItems :
                Collections.<WorkItemRecord>emptyList();
    }

    // reference no longer needed (only used on startup)
    protected void clearOrphanedItems() { _orphanedStartedItems = null; }


    /*****************************************************************************/

    protected void addDelayedCaseLaunch(DelayedLaunchRecord record) {
        _delayedLaunches.add(record);
    }
    
    protected boolean logDelayedCaseLaunch(YSpecificationID specID, String caseID) {
        DelayedLaunchRecord found = null;
        for (DelayedLaunchRecord record : _delayedLaunches) {
            if (record.getSpecID().equals(specID)) {
                record.logCaseLaunch(caseID);
                found = record;
                break;
            }
        }
        if (found != null) _delayedLaunches.remove(found);
        return (found != null);
    }


    /********************************************************************************/

    protected void addSpecificationData(SpecificationData specData) {
        _specCache.add(specData);
    }

    protected SpecificationData getSpecificationData(YSpecificationID specID) {
        return _specCache.get(specID);
    }

    protected void removeSpecificationData(YSpecificationID specID) {
        _specCache.remove(specID);
    }
    
    
    /*********************************************************************************/
    
    protected Map<String, Element> addDataSchema(YSpecificationID specID, String schema) {
        _dataSchemaCache.add(specID, schema);
        return getDataSchemaMap(specID);
    }

    
    protected Map<String, Element> getDataSchemaMap(YSpecificationID specID) {
        return _dataSchemaCache.getSchemaMap(specID);
    }

    protected void removeDataSchema(YSpecificationID specID) {
        _dataSchemaCache.remove(specID);
    }


    /********************************************************************************/

    protected void removeSpecification(YSpecificationID specID) {
        removeSpecificationData(specID);
        removeDataSchema(specID);
    }

}
