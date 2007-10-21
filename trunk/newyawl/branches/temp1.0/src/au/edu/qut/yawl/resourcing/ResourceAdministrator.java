/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing;

import au.edu.qut.yawl.resourcing.resource.Participant;
import au.edu.qut.yawl.resourcing.datastore.WorkItemCache;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;

import java.util.HashSet;

/**
 * Provides resource administration capabilties to authorised participants
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 03/09/2007
 */


public class ResourceAdministrator {

    private QueueSet _qSet  ;

    private static ResourceAdministrator _me ;

    public ResourceAdministrator() {
//        _qSet = new QueueSet("", QueueSet.setType.adminSet, false);
        _me = this ;
    }

    public static ResourceAdministrator getInstance() {
        if (_me == null) _me = new ResourceAdministrator() ;
        return _me;
    }

    public QueueSet getWorkQueues() { return _qSet ; }

    public void removeFromAllQueues(WorkItemRecord wir) {
        _qSet.removeFromQueue(wir, WorkQueue.UNOFFERED);
        _qSet.removeFromQueue(wir, WorkQueue.WORKLISTED);
    }

    public void createWorkQueues(boolean persisting) {
        _qSet = new QueueSet("", QueueSet.setType.adminSet, persisting) ;
    }


    public void restoreWorkQueue(WorkQueue q, WorkItemCache cache, boolean persisting) {
        if (_qSet == null) createWorkQueues(persisting) ;
        _qSet.restoreWorkQueue(q, cache) ;
    }

//    public void persistWorkQueues() {
//        _qSet.persistAdminQueues() ;
//    }

    private void refreshWorklistedQueue() {
        _qSet.getQueuedWorkItems(WorkQueue.WORKLISTED).clear();

        HashSet<Participant> pSet = ResourceManager.getInstance().getParticipants();

        for (Participant p : pSet) {
            _qSet.addToQueue(WorkQueue.WORKLISTED, p.getWorkQueues().getCombinedQueues());
        }
    }



}
