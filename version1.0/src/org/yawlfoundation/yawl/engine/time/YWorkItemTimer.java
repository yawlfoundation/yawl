package org.yawlfoundation.yawl.engine.time;

import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.engine.YWorkItemStatus;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;

import java.util.Date;

/**
 * Author: Michael Adams
 * Creation Date: 31/01/2008
 */

public class YWorkItemTimer implements YTimedObject {

    public enum Trigger { OnEnabled, OnExecuting }

    private String _ownerID;
    private long _endTime ;
    private boolean _persisting ;


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

        // getstatus
        YEngine engine = YEngine.getInstance();
        YWorkItem item = engine.getWorkItem(_ownerID) ;
        if (item != null) {
            try {
                if (item.getStatus().equals(YWorkItemStatus.statusEnabled)) {
                    engine.skipWorkItem(item, "_timerExpiry") ;
                    engine.announceTimerExpiryToExceptionService(item);
                }
                else if (item.hasUnfinishedStatus()) {
                    engine.completeWorkItem(item, "_timerExpiry", true) ;
                    engine.announceTimerExpiryToExceptionService(item);
                }
            }
            catch (Exception e) {
                // handle exc.
            }
        }
        persistThis(false) ;                                 // unpersist this timer

    }

    
}
