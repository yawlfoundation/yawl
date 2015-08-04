package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.yawlfoundation.yawl.resourcing.resource.Participant;

/**
 * Author: Michael Adams
 * Creation Date: 28/04/2010
 */
public class UserConnection {

    private String _jSessionID;
    private String _ySessionHandle;
    private Participant _participant;

    public UserConnection(String jSessionID, String ySessionHandle, Participant participant) {
        _jSessionID = jSessionID;
        _ySessionHandle = ySessionHandle;
        _participant = participant;
    }

    public String getSessionID() {
        return _jSessionID;
    }

    public String getSessionHandle() {
        return _ySessionHandle;
    }

    public Participant getParticipant() {
        return _participant;
    }
}
