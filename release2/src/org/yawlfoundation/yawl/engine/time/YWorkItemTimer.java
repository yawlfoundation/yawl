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

package org.yawlfoundation.yawl.engine.time;

import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.engine.YPersistenceManager;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.engine.YWorkItemStatus;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;

import javax.xml.datatype.Duration;
import java.util.Date;
import java.util.Set;

/**
 * A timer associated with an Atomic Task.
 *
 * Author: Michael Adams
 * Creation Date: 31/01/2008
 */

public class YWorkItemTimer implements YTimedObject {

    public enum Trigger { OnEnabled, OnExecuting }

    public enum State { dormant, active, closed, expired }

    private String _ownerID;
    private long _endTime ;
    private boolean _persisting ;

    public YWorkItemTimer() {}                                   // for hibernate

    public YWorkItemTimer(String workItemID, long msec, boolean persisting) {
        _ownerID = workItemID ;
        _persisting = persisting ;
        _endTime = YTimer.getInstance().schedule(this, msec) ;
//        if (persisting) persistThis(true) ;
    }


    public YWorkItemTimer(String workItemID, Date expiryTime, boolean persisting) {
        _ownerID = workItemID ;
        _persisting = persisting ;
        _endTime = YTimer.getInstance().schedule(this, expiryTime) ;
//        if (persisting) persistThis(true) ;
    }


    public YWorkItemTimer(String workItemID, Duration duration, boolean persisting) {
        _ownerID = workItemID ;
        _persisting = persisting ;
        _endTime = YTimer.getInstance().schedule(this, duration) ;
  //      if (persisting) persistThis(true) ;
    }


    public YWorkItemTimer(String workItemID, long units, YTimer.TimeUnit interval,
                          boolean persisting) {
        _ownerID = workItemID ;
        _persisting = persisting ;
        _endTime = YTimer.getInstance().schedule(this, units, interval);
//        if (persisting) persistThis(true) ;
    }


    public String getOwnerID() { return _ownerID; }

    public void setOwnerID(String id) { _ownerID = id; }

    public long getEndTime() { return _endTime; }

    public void setEndTime(long time) { _endTime = time; }

    public void setPersisting(boolean persist) { _persisting = persist; }
    

    public void persistThis(boolean insert) {
        if (_persisting) {
            YPersistenceManager pmgr = YEngine.getPersistenceManager();
            if (pmgr != null) {
                try {
                    if (insert) pmgr.storeObjectFromExternal(this);
                    else pmgr.deleteObjectFromExternal(this);
                }
                catch (YPersistenceException ype) {
                    // handle exc.
                }
            }    
        }
    }

            
    public void handleTimerExpiry() {
        YEngine engine = YEngine.getInstance();
        YWorkItem item = engine.getWorkItem(_ownerID) ;
        if (item != null) {

            // special case: if the workitem timer started on enabled, and the item
            // has since been started, the ownerID now refers to the parent, and so the
            // child is needed so it can be expired correctly.
            if (item.getStatus().equals(YWorkItemStatus.statusIsParent)) {
                Set<YWorkItem> children = item.getChildren();
                if ((children != null) && (! children.isEmpty())) {
                    item = children.iterator().next();          // there will only be 1
                }
            }
            
            engine.getNetRunner(item).updateTimerState(item.getTask(), State.expired);

            try {
                if (item.getStatus().equals(YWorkItemStatus.statusEnabled)) {
                    if (item.requiresManualResourcing())              // not an autotask
                        engine.skipWorkItem(item, null) ;
                    engine.getAnnouncer().announceTimerExpiryEvent(item);
                }
                else if (item.hasUnfinishedStatus()) {
                    if (item.requiresManualResourcing())              // not an autotask
                        engine.completeWorkItem(item, item.getDataString(), null, true) ;
                    engine.getAnnouncer().announceTimerExpiryEvent(item);
                }
            }
            catch (Exception e) {
                // handle exc.
            }
        }
        persistThis(false) ;                                 // unpersist this timer
    }


    // unpersist this timer when the workitem is cancelled
    public void cancel() {
        persistThis(false) ;
    }

}
