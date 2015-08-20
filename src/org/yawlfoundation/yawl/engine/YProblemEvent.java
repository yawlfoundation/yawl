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

package org.yawlfoundation.yawl.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.exceptions.Problem;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;

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
    private static final Logger logger = LogManager.getLogger(YProblemEvent.class);
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
        logger.error("Problem source: {}, Message: {}", _source, _message);
        Problem error = new Problem();
        error.setMessage(_message);
        error.setMessageType(EXECUTION_ERROR_STR);
        error.setProblemTime(new Date());
        error.setSource(_source.toString());

        /**
         * AJH: Bugfix - Prevent NPE being thrown if persistence switched off
         */
        if (pmgr != null) {
            pmgr.storeObject(error);
        }
    }
}
