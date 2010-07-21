/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.*;

/**
 * Author: Michael Adams
 * Creation Date: 28/04/2010
 */
public class UserConnectionCache {

    private Map<String, UserConnection> _jSessionIDLookup;
    private Map<String, UserConnection> _ySessionHandleLookup;
    private Map<String, UserConnection> _participantLookup;

    public UserConnectionCache() {
        _jSessionIDLookup = new Hashtable<String, UserConnection>();
        _ySessionHandleLookup = new Hashtable<String, UserConnection>();
        _participantLookup = new Hashtable<String, UserConnection>();
    }


    public UserConnection add(String ySessionHandle, Participant p, String jSessionID) {
        UserConnection connection = new UserConnection(jSessionID, ySessionHandle, p);
        _jSessionIDLookup.put(jSessionID, connection);
        _ySessionHandleLookup.put(ySessionHandle, connection);
        if (p != null) {
            _participantLookup.put(p.getID(), connection);
        }
        return connection;
    }

    public UserConnection removeSessionID(String id) {
        UserConnection connection = _jSessionIDLookup.get(id);
        if (connection != null) {
            remove(connection);
        }
        return connection;
    }


    public UserConnection removeSessionHandle(String id) {
        UserConnection connection = _ySessionHandleLookup.get(id);
        if (connection != null) {
            remove(connection);
        }
        return connection;
    }


    public UserConnection removeParticipantSession(Participant p) {
        if (p != null) {
            UserConnection connection = _participantLookup.get(p.getID());
            if (connection != null) {
                remove(connection);
            }
            return connection;
        }
        return null;
    }


    public Set<Participant> getActiveParticipants() {
        Set<Participant> participants = new HashSet<Participant>();
        for (UserConnection connection : _participantLookup.values()) {
            Participant p = connection.getParticipant();
            if (p != null) {
                participants.add(connection.getParticipant());
            }
        }
        return participants;
    }

    
    public Collection<UserConnection> getAllSessions() {
        return _ySessionHandleLookup.values();
    }


    public String getSessionHandle(Participant p) {
        UserConnection connection = _participantLookup.get(p.getID());
        return (connection != null) ? connection.getSessionHandle() : null;
    }


    public Participant getParticipantWithSessionHandle(String handle) {
        UserConnection connection = _ySessionHandleLookup.get(handle);
        return (connection != null) ? connection.getParticipant() : null;        
    }


    public boolean containsSessionHandle(String handle) {
        return (handle != null) && (_ySessionHandleLookup.get(handle) != null);
    }

    public boolean containsSessionID(String id) {
        return (id != null) && (_jSessionIDLookup.get(id) != null);
    }

    public boolean containsParticipant(String pid) {
        return (pid != null) && (_participantLookup.get(pid) != null);
    }


    /***********************************************************************/

    private void remove(UserConnection connection) {
        _jSessionIDLookup.remove(connection.getSessionID());
        _ySessionHandleLookup.remove(connection.getSessionHandle());
        if (connection.getParticipant() != null) {
            _participantLookup.remove(connection.getParticipant().getID());
        }    
    }

}
