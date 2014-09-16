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

package org.yawlfoundation.yawl.logging.table;

/**
 * Author: Michael Adams
 * Creation Date: 22/10/2009
 */
public class YAuditEvent {

    public static enum Action { logon, logoff, invalid, unknown, shutdown, expired }

    private long _id ;                                           // hibernate PK
    private String _username;
    private String _event ;
    private long _timeStamp ;

    public YAuditEvent() { }

    public YAuditEvent(String username, Action event) {
        _username = username;
        _event = event.name();
        _timeStamp = System.currentTimeMillis();
    }

    public long get_id() { return _id; }

    public void set_id(long id) { _id = id; }

    public String get_username() { return _username; }

    public void set_username(String userid) { _username = userid; }

    public String get_event() { return _event; }

    public void set_event(String event) {_event = event; }

    public long get_timeStamp() { return _timeStamp; }

    public void set_timeStamp(long timeStamp) {_timeStamp = timeStamp; }

    public boolean equals(Object other) {
        return (other instanceof YAuditEvent) &&
                (this.get_id() == ((YAuditEvent) other).get_id());
    }

    public int hashCode() {
        return (int) (31 * get_id()) % Integer.MAX_VALUE;
    }    

}