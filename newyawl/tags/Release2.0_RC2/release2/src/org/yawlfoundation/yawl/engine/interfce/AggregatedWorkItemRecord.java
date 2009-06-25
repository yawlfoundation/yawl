package org.yawlfoundation.yawl.engine.interfce;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

public class AggregatedWorkItemRecord extends WorkItemRecord {

    /*
      Store the time taken for activities
      used by the admintool
     */
    private long fired2Complete = 0;
    private long started2Complete = 0;
    private long fired2Started = 0;

    public void setFired2Complete(long time) {
	fired2Complete = time;
    }
    public void setStarted2Complete(long time) {
	started2Complete = time;
    }
    public void setFired2Started(long time) {
	fired2Started = time;
    }

    public long getFired2Complete() {
	return fired2Complete;
    }
    public long getStarted2Complete() {
	return started2Complete;
    }
    public long getFired2Started() {
	return fired2Started;
    }

    public long getCount() {
	return count;
    }
    public void setCount(long count) {
	this.count = count;	
    }
    
    private long count = 0;

    public AggregatedWorkItemRecord() {
	super();
    }

}

