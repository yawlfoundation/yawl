/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.engine.time;

import javax.xml.datatype.Duration;
import java.util.*;

/**
 * Author: Michael Adams
 * Creation Date: 31/01/2008
 */

public class YTimer extends Timer {

    public enum TimeUnit { YEAR, MONTH, WEEK, DAY, HOUR, MIN, SEC, MSEC }

    private static YTimer _me;

    private Hashtable<String, TimeKeeper> _runners;

    private YTimer() {
        super(true) ;
        _runners = new Hashtable<String, TimeKeeper>();
    }


    public static YTimer getInstance() {
        if (_me == null) _me = new YTimer() ;
        return _me ;
    }


    public boolean hasActiveTimer(String itemID) {
        return _runners.containsKey(itemID);
    }

    public YTimedObject cancelTimerTask(String itemID) {
        YTimedObject result = null;
        TimeKeeper timer = _runners.get(itemID);
        if (timer != null) {
            result = timer.getOwner();
            timer.cancel();                           // cancel the scheduled timertask
            result.cancel();                          // cancel the YWorkItemTimer
            _runners.remove(itemID);
        }
        return result;
    }

    public void cancelTimersForCase(String caseID) {
        Set<String> toRemove = new HashSet<String>();
        for (String itemID : _runners.keySet()) {
            if (itemID.startsWith(caseID + ":") || itemID.startsWith(caseID + ".")) {
                toRemove.add(itemID);
            }
        }
        for (String itemID : toRemove) {
            cancelTimerTask(itemID);
        }
    }


    // both methods return a long value of the date/time stamp representing
    // the expiry time

    public long schedule(YTimedObject timee, long durationAsMilliseconds) {
        schedule(new TimeKeeper(timee), durationAsMilliseconds) ;
        return System.currentTimeMillis() + durationAsMilliseconds;
    }

    
    public long schedule(YTimedObject timee, Date expiryTime) {
        schedule(new TimeKeeper(timee), expiryTime) ;
        return expiryTime.getTime();
    }
    

    public long schedule(YTimedObject timee, Duration duration) {
        long durationAsMilliseconds = duration.getTimeInMillis(new Date());
        return schedule(timee, durationAsMilliseconds);
    }


    public long schedule(YTimedObject timee, long count, TimeUnit unit) {
        long msecFactor = 1;
        int dateFactor = 1;

        switch (unit) {
            case YEAR  : dateFactor *= 12 ;
            case MONTH : { Calendar date = Calendar.getInstance(); 
                           date.add(Calendar.MONTH, dateFactor * (int) count) ;
                           return schedule(timee, date.getTime());
                         }
            case WEEK  : msecFactor *= 7 ;
            case DAY   : msecFactor *= 24 ;
            case HOUR  : msecFactor *= 60 ;
            case MIN   : msecFactor *= 60 ;
            case SEC   : msecFactor *= 1000 ;
            case MSEC  : return schedule(timee, msecFactor * count) ;
        }
        return -1 ;
    }

    
    /********************************************************************************/


    private class TimeKeeper extends TimerTask {

        private YTimedObject _owner ;

        protected TimeKeeper(YTimedObject owner) {
            _owner = owner ;

            if (owner instanceof YWorkItemTimer) {
                String id = ((YWorkItemTimer) owner).getOwnerID();
                _runners.put(id, this);
            }
        }


        public YTimedObject getOwner() { return _owner; }


        public synchronized void run() {
            String id = ((YWorkItemTimer) _owner).getOwnerID();
            _owner.handleTimerExpiry();
            _runners.remove(id);
        }
    }
}
