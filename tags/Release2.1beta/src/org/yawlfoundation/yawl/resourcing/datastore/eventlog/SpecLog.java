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
