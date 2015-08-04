/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.allocators.ShortestQueue;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;
import org.yawlfoundation.yawl.resourcing.interactions.*;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.util.TaggedStringList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Manages all of the resourcing requirements and distribution strategies for one
 * task.
 *
 *  @author Michael Adams
 *  v0.1, 03/08/2007
 */

public class ResourceMap {

    private ResourceManager rm = ResourceManager.getInstance();

    // interaction points
    private OfferInteraction _offer ;
    private AllocateInteraction _allocate ;
    private StartInteraction _start ;

    // user-task privileges
    private TaskPrivileges _privileges ;

    private long _id ;                                             // hibernate pkey
    private String _taskID ;
    private YSpecificationID _specID ;

    private Participant _piledResource = null ;
    private String _piledResourceID ;                              // for persistence
    private Persister _persister ;

    private Set<TaggedStringList> _ignoreSet = new HashSet<TaggedStringList>();

    // workitem id - offered-to-participants mapping
    private HashMap<String, HashSet<Participant>> _offered = new
            HashMap<String, HashSet<Participant>>() ;

    private static final Logger _log = Logger.getLogger(ResourceMap.class) ;


    public ResourceMap() { }                                       // for persistence

    public ResourceMap(String taskID) {
        _taskID = taskID ;
        _offer = new OfferInteraction(taskID) ;
        _allocate = new AllocateInteraction(taskID);
        _start = new StartInteraction(taskID) ;

        _allocate.setAllocator(new ShortestQueue());               // default allocator
    }

    public ResourceMap(YSpecificationID specID, String taskID, Element eleSpec) {
        this(taskID);
        _specID = specID ;
        _privileges = new TaskPrivileges(specID, taskID);
        parse(eleSpec);
        restorePiledResource() ;
    }

    public ResourceMap(YSpecificationID specID, String taskID, Element eleSpec, boolean persisting) {
        this(specID, taskID, eleSpec);
        if (persisting) setPersisting(true);
        restorePiledResource() ;
    }


    public void setOfferInteraction(OfferInteraction oi) {
       _offer = oi ;
    }

    public void setAllocateInteraction(AllocateInteraction ai) {
       _allocate = ai ;
    }

    public void setStartInteraction(StartInteraction si) {
       _start = si ;
    }

    public void setTaskPrivileges(TaskPrivileges tp) {
        _privileges = tp ;
    }

    public OfferInteraction getOfferInteraction() {
        return _offer ;
    }

    public AllocateInteraction getAllocateInteraction() {
       return _allocate ;
    }

    public StartInteraction getStartInteraction() {
       return _start ;
    }

    public TaskPrivileges getTaskPrivileges() {
        return _privileges ;
    }

    public String getTaskID() { return _taskID; }

    public void setTaskID(String taskID) { _taskID = taskID ; }

    public String getSpecName() { return _specID.getSpecName(); }

    public YSpecificationID getSpecID() { return _specID; }

    public void setSpecID(YSpecificationID specID) { _specID = specID ; }

    public String getPiledResourceID() { return _piledResourceID; }

    public void setPiledResourceID(String id) { _piledResourceID = id; }


    public Participant getPiledResource() { return _piledResource; }

    public String setPiledResource(Participant p, WorkItemRecord wir) {
        String result;
        if (! hasPiledResource()) {
            _piledResource = p;
            _piledResourceID = p.getID();
            if (getPersisting()) _persister.insert(this);
            if (rm.routePiledWorkItem(_piledResource, wir)) {
                result = "Task successfully piled." ;
            }
            else {
                result = "Cannot pile task: problem starting workitem." ;
            }
        }
        else result = "Cannot pile task: already piled by another resource.";

        return result ;
    }

    public void removePiledResource() {
        _piledResource = null ;
        _piledResourceID = null ;
        if (getPersisting()) {

            // have to get persisted map first, so we can delete it (since 'this' is not
            // the same object as the one persisted)
            ResourceMap map = getPersistedMap();
            if (map != null) _persister.delete(map);
        }
    }

    
    private ResourceMap getPersistedMap() {
        ResourceMap result = null;
        if (getPersisting()) {
            String where = String.format("_specID='%s' and _taskID='%s'",
                                          _specID.toString(), _taskID);
            List map = _persister.selectWhere("ResourceMap", where) ;
            if ((map != null) && (! map.isEmpty())) {
                result = (ResourceMap) map.iterator().next();
            }
        }
        return result;
    }

    public boolean hasPiledResource() { return _piledResource != null; }

    private void restorePiledResource() {
        ResourceMap map = getPersistedMap();
        if (map != null) {
            if (rm.isPersistPiling()) {
                _piledResourceID = map.getPiledResourceID();
                _piledResource = rm.getParticipant(_piledResourceID) ;
            }
            else _persister.delete(map);
        }
    }


    public void setPersisting(boolean persist) {
        if ((persist) && (_persister == null))
            _persister = Persister.getInstance();
        else
            _persister = null;
    }

    public boolean getPersisting() { return (_persister != null); }


    public void ignore(WorkItemRecord wir, Participant p) {
        TaggedStringList ignoredForWorkItem = getIgnoredList(wir.getID());
        if (ignoredForWorkItem != null)
            ignoredForWorkItem.add(p.getID());
        else
            _ignoreSet.add(new TaggedStringList(wir.getID(), p.getID())) ;
    }


    private TaggedStringList getIgnoredList(String key) {
        TaggedStringList result = null;
        for (TaggedStringList list : _ignoreSet) {
            if (list.getTag().equals(key)) result = list;
        }
        return result;
    }


    public HashSet<Participant> getOfferedParticipants(String itemID) {
        return _offered.get(itemID) ;
    }

    /****************************************************************************/

    

    /****************************************************************************/

    public WorkItemRecord distribute(WorkItemRecord wir) {
        boolean routed = false;

        // if this task is piled, send directly to the piled participant's started queue
        if (_piledResource != null)
            routed = rm.routePiledWorkItem(_piledResource, wir) ;

        if (! routed) {

            // construct distribution set from resource spec
            HashSet<Participant> distributionSet = doOffer(wir) ;
            if (distributionSet != null) {

                // if case is chained and the chained participant is in the
                // distribution set, route it directly to their started queue
                routed = rm.routeIfChained(wir, distributionSet) ;

                if (! routed) {

                    // not piled or chained, distribute in normal manner
                    removeIgnoredParticipants(wir, distributionSet);
                    if (! distributionSet.isEmpty()) {
                        Participant chosen = doAllocate(distributionSet, wir) ;
                        if (chosen != null) doStart(chosen, wir) ;
                    }
                    else {

                        // ignored p's --> empty distribution set: put in unoffered
                        addToAdminUnofferedQueue(wir);
                    }
                }
            }
        }
        return wir ;
    }


    /** removes the workitem from all offer queues */ 
    public void withdrawOffer(WorkItemRecord wir) {
        HashSet<Participant> pSet = _offered.remove(wir.getID());
        _offer.withdrawOffer(wir, pSet);
    }


    private void addToAdminUnofferedQueue(WorkItemRecord wir) {
        wir.setResourceStatus(WorkItemRecord.statusResourceUnoffered);
        ResourceAdministrator.getInstance().addToUnoffered(wir);
    }



    private HashSet<Participant> doOffer(WorkItemRecord wir) {
        HashSet<Participant> offerSet = null;
        if (_offer.getInitiator() == AbstractInteraction.USER_INITIATED) {

            // put workitem in admin's unoffered queue & DONE
            addToAdminUnofferedQueue(wir);
        }
        else {
           offerSet = (HashSet<Participant>) _offer.performOffer(wir);
           if (offerSet.isEmpty()) {
               _log.warn("Parse of resource specifications for workitem " + wir.getID() +
                         " resulted in an empty distribution list. Workitem will be" +
                         " passed to an administrator for manual distribution.");

               // put workitem in admin's unoffered queue & DONE
               addToAdminUnofferedQueue(wir);
               offerSet = null ;
           }
        }
        return offerSet ;
    }    

    private Participant doAllocate(HashSet<Participant> pSet, WorkItemRecord wir) {
        Participant chosenOne = null;
        if (_allocate.getInitiator() == AbstractInteraction.USER_INITIATED) {

            // for each participant in set, place workitem on their offered queue
            for (Participant p : pSet) {
                QueueSet qs = p.getWorkQueues() ;
                if (qs == null) qs = p.createQueueSet(rm.getPersisting());
                qs.addToQueue(wir, WorkQueue.OFFERED);
                rm.announceModifiedQueue(p.getID()) ;
            }
            _offered.put(wir.getID(), pSet) ;
            wir.setResourceStatus(WorkItemRecord.statusResourceOffered);
        }
        else {
            chosenOne = _allocate.performAllocation(pSet, wir);
            if (chosenOne == null) {
                _log.warn("The system allocator '" + _allocate.getAllocator().getName() +
                          "' has been unable to allocate workitem '" + wir.getID() +
                          "' to a participant. The workitem has been passed to the " +
                          "administrator's unoffered queue for manual allocation.");
                addToAdminUnofferedQueue(wir);                
            }
        }
        return chosenOne ;
    }

    private void doStart(Participant p, WorkItemRecord wir) {
        boolean started = false ;
        QueueSet qs = p.getWorkQueues() ;
        if (qs == null) qs = p.createQueueSet(rm.getPersisting());

        if (_start.getInitiator() == AbstractInteraction.SYSTEM_INITIATED) {
            started = rm.startImmediate(p, wir) ;
            if (! started) {
                _log.warn("The workitem '" + wir.getID() + "' could not be " +
                          "automatically started. The workitem has been placed on " +
                          "the participant's allocated queue.");
            }
        }

        // either initiator is 'user' or start was unsuccessful
        if (! started) {
            qs.addToQueue(wir, WorkQueue.ALLOCATED);
            wir.setResourceStatus(WorkItemRecord.statusResourceAllocated);
        }
    }


    private void removeIgnoredParticipants(WorkItemRecord wir,
                                           HashSet<Participant> distributionSet) {
        TaggedStringList ignoredForWorkItem = getIgnoredList(wir.getID());
        if (ignoredForWorkItem != null) {
            Set<Participant> ignored = new HashSet<Participant>();
            for (Participant p : distributionSet) {
                if (ignoredForWorkItem.contains(p.getID()))
                    ignored.add(p) ;
            }
            distributionSet.removeAll(ignored);
        }
    }


    public void removeIgnoreList(WorkItemRecord wir) {
        TaggedStringList ignoredForWorkItem = getIgnoredList(wir.getID());
        if (ignoredForWorkItem != null) {
            _ignoreSet.remove(ignoredForWorkItem) ;
        }
    }

    
    public void addToOfferedSet(WorkItemRecord wir, Participant p) {
        HashSet<Participant> pSet = _offered.get(wir.getID());
        if (pSet == null)
            pSet = new HashSet<Participant>();

        pSet.add(p);
        _offered.put(wir.getID(), pSet);
    }

    /**
     * Parse the Element passed for task resourcing info and build the appropriate
     * objects.
     * @param eleSpec the [resourcing] section from a particular task definition
     * within a specification file.
     */
    public void parse(Element eleSpec) {
        if (eleSpec != null) {
            Namespace nsYawl = eleSpec.getNamespace() ;
            try {
                _offer.parse(eleSpec.getChild("offer", nsYawl), nsYawl) ;
                _allocate.parse(eleSpec.getChild("allocate", nsYawl), nsYawl) ;
                _start.parse(eleSpec.getChild("start", nsYawl), nsYawl) ;
                _privileges.parse(eleSpec.getChild("privileges", nsYawl), nsYawl) ;
                _log.info("Resourcing specification parse completed for task: " + _taskID);
            }
            catch (ResourceParseException rpe) {
                _log.error(
                     "Error parsing resourcing specification for task: " + _taskID, rpe);
            }
        }
    }

    
    public String toXML() {
        StringBuilder xml = new StringBuilder("<resourcing>");
        xml.append(_offer.toXML()) ;
        xml.append(_allocate.toXML()) ;
        xml.append(_start.toXML()) ;
        xml.append(_privileges.toXML()) ;
        xml.append("</resourcing>");
        return xml.toString() ;
    }


    // hibernate pkey getter & setter

    public long get_id() { return _id; }

    public void set_id(long id) { _id = id; }

    private String get_specID() { return _specID.toString(); }

    private void set_specID(String specID) {
        if (specID != null) {
            String[] parts = specID.split(" - version ");
            if (parts.length > 1)
                _specID = new YSpecificationID(parts[0], parts[1]);
            else
                _specID = new YSpecificationID(parts[0]);
        }
    }

}
