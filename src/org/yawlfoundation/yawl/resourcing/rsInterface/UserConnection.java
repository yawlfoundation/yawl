/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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
