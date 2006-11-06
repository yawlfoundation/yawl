/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.worklist.model;

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

