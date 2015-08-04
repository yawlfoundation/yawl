/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.worklet.support;

import org.yawlfoundation.yawl.engine.YSpecificationID;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  The sole purpose of this class is to generate an event log record via Persistence.
 *  An instance is created via the constructor (from the EventLogger class), then the
 *  object is persisted to create one event log record.
 *
 *  @author Michael Adams
 *  v0.8, 04-09/2006
 */

public class WorkletEvent {
    private YSpecificationID _specId;
    private String _event, _caseId, _taskId, _parentCaseId, _stamp;
    private int _xType;
    private long _id ;

    private	SimpleDateFormat _sdfe = new SimpleDateFormat("yyy.MM.dd hh:mm:ss:SS");


    private WorkletEvent() {}                  // empty cons. required for persistence

    /** the one and only constructor */
    public WorkletEvent(String event, String caseId, YSpecificationID specId,
                         String taskId, String parentCaseId, int xType) {
        _event = event;
        _caseId = caseId ;
        _specId = specId ;
        _taskId = taskId ;
        _parentCaseId = parentCaseId ;
        _xType = xType ;
        _stamp = _sdfe.format(new Date());
        _id = new Date().getTime();
    }

    /** getters & setters used by persistence */
    private String get_event() { return _event; }
    private String get_caseId() { return _caseId; }
    private YSpecificationID get_specId() { return _specId; }
    private String get_taskId() { return _taskId; }
    private String get_parentCaseId() { return _parentCaseId; }
    private int get_xType() { return _xType; }
    private String get_stamp() { return _stamp; }
    private long get_id() { return _id; }

    private void set_event(String s) { _event = s; }
    private void set_caseId(String s) { _caseId = s; }
    private void set_specId(YSpecificationID s) { _specId = s; }
    private void set_taskId(String s) { _taskId = s; }
    private void set_parentCaseId(String s) { _parentCaseId = s; }
    private void set_xType(int i) { _xType = i; }
    private void set_stamp(String s) { _stamp = s; }
    private void set_id(long lg) { _id = lg; }

}
