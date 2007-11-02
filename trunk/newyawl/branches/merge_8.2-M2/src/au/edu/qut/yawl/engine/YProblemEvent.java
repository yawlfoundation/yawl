/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of 
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import org.apache.log4j.Logger;
import au.edu.qut.yawl.exceptions.Problem;
import au.edu.qut.yawl.exceptions.YPersistenceException;

import java.util.Date;


/**
 * A problem event describes the nature of a runtime execution problem.
 *
 * 
 * @author Lachlan Aldred
 * Date: 15/10/2004
 * Time: 11:50:00
 *
 */
public class YProblemEvent {
    private static final String EXECUTION_ERROR_STR = "Engine Execution Problem";
    private Object _source;
    private String _message;
    private int _eventType;

    public static int RuntimeError = 1;
    public static int RuntimeWarning = 2;

    public YProblemEvent(Object source, String message, int eventType) {
        this._source = source;
        this._message = message;
        this._eventType = eventType;

        if (source == null ||
                (eventType != RuntimeError && eventType != RuntimeWarning)) {
            throw new IllegalArgumentException("Check your arguments: " +
                    "source cannot equal null and evenType must be a registered type.");
        }
    }

    /**
     * Gets the message of the event, if any.
     * @return message
     */
    public String getMessage() {
        return _message;
    }


    /**
     * Gets the source of the event.
     * @return event source object.
     */
    public Object getSource() {
        return _source;
    }

    /**
     * Gets the event type.
     * @return event ype.
     */
    public int getEventType() {
        return _eventType;
    }

    public void logProblem(YPersistenceManager pmgr) throws YPersistenceException {
        Logger.getLogger(this.getClass()).error("Problem source: " + _source + " " +
                " Message: " + _message);
        Problem error = new Problem();
        error.setMessage(_message);
        error.setMessageType(EXECUTION_ERROR_STR);
        error.setTimeStamp(new Date());
        error.setSource(_source.toString());

        /**
         * AJH: Bugfix - Prevent NPE being thrown if persistence switched off
         */
        if (pmgr != null) {
            pmgr.storeObject(error);
        }
    }
}
