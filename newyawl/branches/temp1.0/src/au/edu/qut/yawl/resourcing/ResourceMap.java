/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing;

import au.edu.qut.yawl.resourcing.interactions.*;
import au.edu.qut.yawl.resourcing.resource.Participant;
import au.edu.qut.yawl.engine.YWorkItem;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;

import java.util.HashSet;

import org.jdom.Element;


/**
 * Manages all of the resourcing requirements and distribution strategies for one
 * task.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 03/08/2007
 */

public class ResourceMap {

    // interaction points
    private OfferInteraction _offer ;
    private AllocateInteraction _allocate ;
    private StartInteraction _start ;

    // user-task privileges
    private TaskPrivileges _privileges ;

    private String _taskID ;
    private String _specID ;


    public ResourceMap(String taskID) {
        _taskID = taskID ;
        _offer = new OfferInteraction() ;
        _allocate = new AllocateInteraction();
        _start = new StartInteraction() ;
        _privileges = new TaskPrivileges();
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



    /****************************************************************************/

    public void distribute(WorkItemRecord wir) {
        HashSet<Participant> distributionSet ;
        Participant chosen = null ;

        distributionSet = doOffer(wir) ;
        if (distributionSet != null) chosen = doAllocate(distributionSet, wir) ;
        if (chosen != null) doStart(chosen, wir) ;
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
           for (Participant p : pSet) p.getWorkQueues().addToQueue(wir, WorkQueue.OFFERED);
        }
        else {      
            chosenOne = _allocate.performAllocation(pSet);
        }
        return chosenOne ;
    }

    private void doStart(Participant p, WorkItemRecord wir) {
        if (_start.getInitiator() == AbstractInteraction.USER_INITIATED)
            p.getWorkQueues().addToQueue(wir, WorkQueue.ALLOCATED);
        else
            p.getWorkQueues().addToQueue(wir, WorkQueue.STARTED);
    }


    /**
     * Parse the Element passed for task resourcing info and build the appropriate
     * objects.
     * @param eleSpec the [resourcing] section from a particular task definition
     * within a specification file.
     */
    public void parse(Element eleSpec) {
        if (eleSpec != null) {
            _offer.parse(eleSpec.getChild("offer")) ;
            _allocate.parse(eleSpec.getChild("allocate")) ;
            _start.parse(eleSpec.getChild("start")) ;
            _privileges.parse(eleSpec.getChild("privileges")) ;
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
