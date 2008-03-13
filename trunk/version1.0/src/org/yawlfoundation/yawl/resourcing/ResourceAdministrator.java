/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.datastore.WorkItemCache;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.HashSet;

/**
 * Provides resource administration capabilties to authorised participants
 *
 *  @author Michael Adams
 *  v0.1, 03/09/2007
 */


public class ResourceAdministrator {

    private QueueSet _qSet  ;

    private static ResourceAdministrator _me ;

    private ResourceAdministrator() {
//        _qSet = new QueueSet("", QueueSet.setType.adminSet, false);
    }

    public static ResourceAdministrator getInstance() {
        if (_me == null) _me = new ResourceAdministrator() ;
        return _me;
    }

    public QueueSet getWorkQueues() {
        refreshWorklistedQueue();
        return _qSet ;
    }

    public void removeFromAllQueues(WorkItemRecord wir) {
        _qSet.removeFromQueue(wir, WorkQueue.UNOFFERED);
        _qSet.removeFromQueue(wir, WorkQueue.WORKLISTED);
    }

    public void createWorkQueues(boolean persisting) {
        _qSet = new QueueSet(null, QueueSet.setType.adminSet, persisting) ;
    }


    public void assignUnofferedItem(WorkItemRecord wir, Participant p, String action) {
        WorkQueue unoffered = _qSet.getQueue(WorkQueue.UNOFFERED) ;
        if (unoffered != null) {
            if (action.equals("Offer")) {
                wir.setResourceStatus(WorkItemRecord.statusResourceOffered);
                p.getWorkQueues().addToQueue(wir, WorkQueue.OFFERED);
            }
            else if (action.equals("Allocate")) {
                wir.setResourceStatus(WorkItemRecord.statusResourceAllocated);
                p.getWorkQueues().addToQueue(wir, WorkQueue.ALLOCATED);
            }
            else if (action.equals("Start")) {
                wir.setResourceStatus(WorkItemRecord.statusResourceStarted);
                p.getWorkQueues().addToQueue(wir, WorkQueue.STARTED);
            }
            unoffered.remove(wir);
        }
    }


    public void restoreWorkQueue(WorkQueue q, WorkItemCache cache, boolean persisting) {
        if (_qSet == null) createWorkQueues(persisting) ;
        _qSet.restoreWorkQueue(q, cache) ;
    }


    private void refreshWorklistedQueue() {
        _qSet.purgeQueue(WorkQueue.WORKLISTED);

        HashSet<Participant> pSet = ResourceManager.getInstance().getParticipants();
        for (Participant p : pSet)
            _qSet.addToQueue(WorkQueue.WORKLISTED, p.getWorkQueues().getWorklistedQueues());
    }

}