/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.util;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.xml.datatype.Duration;
import java.util.Date;

/**
 * @author Michael Adams
 * @date 10/02/12
 */
public class DelayedLaunchRecord implements Comparable<DelayedLaunchRecord> {
    
    private YSpecificationID specID;
    private String launcher;
    private long delay;
    
    public DelayedLaunchRecord() { }

    private DelayedLaunchRecord(YSpecificationID specID, String handle) {
        this.specID = specID;
        this.launcher = handle;
    }

    public DelayedLaunchRecord(YSpecificationID specID, String handle, long delay) {
        this(specID, handle);
        this.delay = delay;
    }

    public DelayedLaunchRecord(YSpecificationID specID, String handle, Date delay) {
        this(specID, handle);
        this.delay = delay.getTime();
    }

    public DelayedLaunchRecord(YSpecificationID specID, String handle, Duration delay) {
        this(specID, handle);
        this.delay = StringUtil.durationToMSecs(delay);
    }


    public YSpecificationID getSpecID() { return specID; }

    public void setSpecID(YSpecificationID specID) {
        this.specID = specID;
    }


    public void logCaseLaunch(String caseID) {
        EventLogger.log(specID, caseID, launcher, true);
    }


    public int compareTo(DelayedLaunchRecord other) {
        if (other == null) return 1;
        if (this.delay == other.delay) return 0;
        return this.delay > other.delay ? 1 : -1;
    }

    public boolean equals(Object o) {
        if (o instanceof DelayedLaunchRecord) {
            DelayedLaunchRecord other = (DelayedLaunchRecord) o;
            return other.getSpecID().equals(this.specID) &&
                   other.delay == this.delay &&
                   ((other.launcher == null && this.launcher == null) ||
                    (other.launcher != null && other.launcher.equals(this.launcher)));
        }
        return false;
    }
    
    public int hashCode() {
        int launcherHash = launcher != null ? launcher.hashCode() : 33;
        return 17 * launcherHash * specID.hashCode() * ((int) (delay ^ (delay >>> 32)));
    }
}
