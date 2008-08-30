package org.yawlfoundation.yawl.engine.time;

/**
 * Author: Michael Adams
 * Creation Date: 31/01/2008
 */
public interface YTimedObject {

    // do something when the timer for the implementing object expires
    public void handleTimerExpiry() ;

    // do whatever necessary when a timer is cancelled before expiry
    public void cancel();
}
