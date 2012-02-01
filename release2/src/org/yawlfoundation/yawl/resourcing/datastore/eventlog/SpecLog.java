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

package org.yawlfoundation.yawl.resourcing.datastore.eventlog;

import org.yawlfoundation.yawl.engine.YSpecificationID;

/**
 * Author: Michael Adams
 * Creation Date: 29/04/2010
 */
public class SpecLog {

    private YSpecificationID specID;
    private long logID;                               // PK

    public SpecLog() { }

    public SpecLog(YSpecificationID specID) {
        this.specID = specID;      
    }

    public YSpecificationID getSpecID() {
        return specID;
    }

    public void setSpecID(YSpecificationID specID) {
        this.specID = specID;
    }

    public long getLogID() {
        return logID;
    }

    public void setLogID(long logid) {
        this.logID = logid;
    }

    public String getIdentifier() { return specID.getIdentifier(); }

    public String getVersion() {return specID.getVersionAsString(); }

    public String getURI() { return specID.getUri(); }
}
