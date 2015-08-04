/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

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
