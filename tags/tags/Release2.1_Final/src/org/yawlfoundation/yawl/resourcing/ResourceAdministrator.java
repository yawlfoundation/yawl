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

    public void addToUnoffered(WorkItemRecord wir) {
        ResourceManager rm = ResourceManager.getInstance();
        rm.getWorkItemCache().updateResourceStatus(wir, WorkItemRecord.statusResourceUnoffered);
        _qSet.addToQueue(wir, WorkQueue.UNOFFERED);
        rm.announceResourceUnavailable(wir);
    }

    public void removeFromAllQueues(WorkItemRecord wir) {
        _qSet.removeFromQueue(wir, WorkQueue.UNOFFERED);
        _qSet.removeFromQueue(wir, WorkQueue.WORKLISTED);
    }

    public void removeCaseFromAllQueues(String caseID) {
        _qSet.removeCaseFromAllQueues(caseID);
    }

    public void createWorkQueues(boolean persisting) {
        _qSet = new QueueSet(null, QueueSet.setType.adminSet, persisting) ;
    }


    public void assignUnofferedItem(WorkItemRecord wir, String[] pidList, String action) {
        WorkQueue unoffered = _qSet.getQueue(WorkQueue.UNOFFERED) ;
        if (unoffered != null) {
            ResourceManager rm = ResourceManager.getInstance();
            rm.getWorkItemCache().updateResourceStatus(wir, WorkItemRecord.statusResourceOffered);
            if (action.equals("Offer")) {

                // an offer can be made to several participants
                for (String pid : pidList) {
                    Participant p = rm.getOrgDataSet().getParticipant(pid);
                    p.getWorkQueues().addToQueue(wir, WorkQueue.OFFERED);
                    rm.addToOfferedSet(wir, p);
                }
            }
            else if (action.equals("Allocate")) {
                Participant p = rm.getOrgDataSet().getParticipant(pidList[0]);
                rm.acceptOffer(p, wir);
            }

            // 'Start' actions are handled by the resMgr.start() method

            unoffered.remove(wir);
        }
    }


    public void restoreWorkQueue(WorkQueue q, WorkItemCache cache, boolean persisting) {
        if (_qSet == null) createWorkQueues(persisting) ;
        _qSet.restoreWorkQueue(q, cache) ;
    }


    private void refreshWorklistedQueue() {
        if (_qSet == null) createWorkQueues(false) ;
        _qSet.purgeQueue(WorkQueue.WORKLISTED);

        HashSet<Participant> pSet = ResourceManager.getInstance().getOrgDataSet().getParticipants();
        for (Participant p : pSet)
            _qSet.addToQueue(WorkQueue.WORKLISTED, p.getWorkQueues().getWorklistedQueues());
    }

}
