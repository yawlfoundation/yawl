/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing;

import org.yawlfoundation.yawl.resourcing.interactions.*;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.allocators.ShortestQueue;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.HashSet;
import java.util.HashMap;

import org.jdom.Element;
import org.jdom.Namespace;


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

    private String _taskID ;
    private String _specID ;

    private Participant _piledResource ;
    private HashSet<Participant> _ignoreSet = new HashSet<Participant>();

    // workitem id - offered-to-participants mapping
    private HashMap<String, HashSet<Participant>> _offered = new
            HashMap<String, HashSet<Participant>>() ;


    public ResourceMap(String taskID) {
        _taskID = taskID ;
        _offer = new OfferInteraction(taskID) ;
        _allocate = new AllocateInteraction(taskID);
        _start = new StartInteraction(taskID) ;
        _privileges = new TaskPrivileges(taskID);

        _allocate.setAllocator(new ShortestQueue());               // default allocator
    }

    public ResourceMap(String specID, String taskID, Element eleSpec) {
        this(taskID);
        _specID = specID ;
        parse(eleSpec);
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

    public String getSpecID() { return _specID; }

    public void setSpecID(String specID) { _specID = specID ; }


    public Participant getPiledResource() { return _piledResource; }

    public void setPiledResource(Participant p) { _piledResource = p; }

    public void ignore(Participant p) { _ignoreSet.add(p) ; }
    
    public HashSet<Participant> getOfferedParticipants(String itemID) {
        return _offered.get(itemID) ;
    }
    /****************************************************************************/

    

    /****************************************************************************/

    public WorkItemRecord distribute(WorkItemRecord wir) {

        // if this task is piled, send directly to the piled participant
        if (_piledResource != null)
            _piledResource.getWorkQueues().addToQueue(wir, WorkQueue.STARTED);
        else {
            HashSet<Participant> distributionSet ;
            Participant chosen = null ;

            distributionSet = doOffer(wir) ;
            if (distributionSet != null) removeIgnoredParticipants(distributionSet);
            if (distributionSet != null) chosen = doAllocate(distributionSet, wir) ;
            if (chosen != null) doStart(chosen, wir) ;
        }
        return wir ;
    }


    /** removes the workitem from all offer queues */ 
    public void withdrawOffer(WorkItemRecord wir) {
        HashSet<Participant> pSet = _offered.remove(wir.getID());
        _offer.withdrawOffer(wir, pSet);
    }


    private HashSet<Participant> doOffer(WorkItemRecord wir) {
        ResourceAdministrator admin = ResourceAdministrator.getInstance();
        HashSet<Participant> offerSet = null;
        if (_offer.getInitiator() == AbstractInteraction.USER_INITIATED) {

            // put workitem in admin's unoffered & DONE
            admin.getWorkQueues().addToQueue(wir, WorkQueue.UNOFFERED);
        }
        else {
           // if pile or chain then pile / chain & END else ...
           offerSet = (HashSet<Participant>) _offer.performOffer(wir);
           if (offerSet.isEmpty()) {

               // put workitem in admin's unoffered queue & DONE
               admin.getWorkQueues().addToQueue(wir, WorkQueue.UNOFFERED);
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
               if (qs == null) qs = p.createWorkQueues(rm.getPersisting());
               qs.addToQueue(wir, WorkQueue.OFFERED);
               rm.announceModifiedQueue(p.getID()) ;
           }
           _offered.put(wir.getID(), pSet) ; 
        }
        else {      
            chosenOne = _allocate.performAllocation(pSet);
        }
        return chosenOne ;
    }

    private void doStart(Participant p, WorkItemRecord wir) {
        QueueSet qs = p.getWorkQueues() ;
        if (qs == null) qs = p.createWorkQueues(rm.getPersisting());

        if (_start.getInitiator() == AbstractInteraction.USER_INITIATED)
            qs.addToQueue(wir, WorkQueue.ALLOCATED);
        else
            qs.addToQueue(wir, WorkQueue.STARTED);
    }


    private void removeIgnoredParticipants(HashSet<Participant> distributionSet) {
        for (Participant p : _ignoreSet) distributionSet.remove(p) ;
    }

    /**
     * Parse the Element passed for task resourcing info and build the appropriate
     * objects.
     * @param eleSpec the [resourcing] section from a particular task definition
     * within a specification file.
     */
    public void parse(Element eleSpec) {
        System.out.println("resmap parse") ;
        System.out.println(JDOMUtil.elementToString(eleSpec)) ;


        if (eleSpec != null) {
            Namespace nsYawl = eleSpec.getNamespace() ;
            _offer.parse(eleSpec.getChild("offer", nsYawl), nsYawl) ;
            _allocate.parse(eleSpec.getChild("allocate", nsYawl), nsYawl) ;
            _start.parse(eleSpec.getChild("start", nsYawl), nsYawl) ;
            _privileges.parse(eleSpec.getChild("privileges", nsYawl), nsYawl) ;
        }
    }


    
    public String toXML() {
        StringBuilder xml = new StringBuilder("<resourcing>");
        xml.append(_offer.toXML()) ;
        xml.append(_allocate.toXML()) ;
        xml.append(_start.toXML()) ;
        if (_privileges != null) xml.append(_privileges.toXML()) ;
        xml.append("</resourcing>");
        return xml.toString() ;
    }

}
