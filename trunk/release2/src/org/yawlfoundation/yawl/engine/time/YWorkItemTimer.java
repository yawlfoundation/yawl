/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.engine.time;

import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.engine.YWorkItemStatus;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;

import javax.xml.datatype.Duration;
import java.util.Date;

/**
 * Author: Michael Adams
 * Creation Date: 31/01/2008
 */

public class YWorkItemTimer implements YTimedObject {

    public enum Trigger { OnEnabled, OnExecuting }

    public enum Status { Dormant, Active, Closed, Expired }

    private String _ownerID;
    private long _endTime ;
    private boolean _persisting ;

    public YWorkItemTimer() {}                                   // for hibernate

    public YWorkItemTimer(String workItemID, long msec, boolean persisting) {
        _ownerID = workItemID ;
        _persisting = persisting ;
        _endTime = YTimer.getInstance().schedule(this, msec) ;
        if (persisting) persistThis(true) ;
    }


    public YWorkItemTimer(String workItemID, Date expiryTime, boolean persisting) {
        _ownerID = workItemID ;
        _persisting = persisting ;
        _endTime = YTimer.getInstance().schedule(this, expiryTime) ;
        if (persisting) persistThis(true) ;
    }


    public YWorkItemTimer(String workItemID, Duration duration, boolean persisting) {
        _ownerID = workItemID ;
        _persisting = persisting ;
        _endTime = YTimer.getInstance().schedule(this, duration) ;
        if (persisting) persistThis(true) ;
    }


    public YWorkItemTimer(String workItemID, long units,
                                           YTimer.TimeUnit interval, boolean persisting) {
        _ownerID = workItemID ;
        _persisting = persisting ;
        _endTime = YTimer.getInstance().schedule(this, units, interval);
        if (persisting) persistThis(true) ;
    }


    public String getOwnerID() { return _ownerID; }

    public void setOwnerID(String id) { _ownerID = id; }

    public long getEndTime() { return _endTime; }

    public void setEndTime(long time) { _endTime = time; }

    public void setPersisting(boolean persist) { _persisting = persist; }
    

    public void persistThis(boolean insert) {
        if (_persisting) {
            try {
                if (insert)
                    YEngine.getInstance().storeObject(this);
                else
                    YEngine.getInstance().deleteObject(this);
            }
            catch (YPersistenceException ype) {
                // handle exc.
            }
        }
    }

            
    public void handleTimerExpiry() {

        // when workitem completes, check if there's a timer for it and if so, cancel it.
        YEngine engine = YEngine.getInstance();
        YWorkItem item = engine.getWorkItem(_ownerID) ;
        if (item != null) {
            try {
                if (item.getStatus().equals(YWorkItemStatus.statusEnabled)) {
                    if (item.requiresManualResourcing())              // not an autotask
                        engine.skipWorkItem(item, "_timerExpiry") ;
                    engine.announceTimerExpiryEvent(item);
                }
                else if (item.hasUnfinishedStatus()) {
                    if (item.requiresManualResourcing())              // not an autotask
                        engine.completeWorkItem(item, "_timerExpiry", true) ;
                    engine.announceTimerExpiryEvent(item);
                }
            }
            catch (Exception e) {
                // handle exc.
            }
        }
        persistThis(false) ;                                 // unpersist this timer

    }


    // do whatever necessary when a timer is cancelled before expiry
    public void cancel() {
        persistThis(false) ;                                
    }

}
