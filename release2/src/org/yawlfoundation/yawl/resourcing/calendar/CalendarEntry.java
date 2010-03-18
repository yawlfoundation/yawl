package org.yawlfoundation.yawl.resourcing.calendar;

/**
 * Author: Michael Adams
 * Creation Date: 12/03/2010
 */
public class CalendarEntry {

    private long entryID;                              // hibernate PK
    private String resourceID;
    private long startTime;
    private long endTime;
    private String comment;

    public CalendarEntry() {}

    public CalendarEntry(String resID, long start, long end, String comment) {
        resourceID = resID;
        startTime = start;
        endTime = end;
        this.comment = comment;
    }

    public long getEntryID() {
        return entryID;
    }

    public void setEntryID(long id) {
        entryID = id;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resID) {
        resourceID = resID;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long time) {
        startTime = time;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long time) {
        endTime = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
