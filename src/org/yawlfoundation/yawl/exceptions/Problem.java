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

package org.yawlfoundation.yawl.exceptions;

import java.util.Date;
import java.io.Serializable;
import java.util.Objects;

/**
 * 
 * @author Lachlan Aldred
 * Date: 10/10/2005
 * Time: 19:47:43
 * 
 */
public class Problem implements Serializable {
    private String _source;
    private Date _problemTime;
    private String _messageType;
    private String _message;
    public static final String EMPTY_RESOURCE_SET_MESSAGETYPE = "EmptyResourceSetType";

    public String getSource() {
        return _source;
    }

    public void setSource(String source) {
        this._source = source;
    }

    public Date getProblemTime() {
        return _problemTime;
    }

    public void setProblemTime(Date timeStamp) {
        this._problemTime = timeStamp;
    }

    public String getMessageType() {
        return _messageType != null ? _messageType : EMPTY_RESOURCE_SET_MESSAGETYPE;
    }

    public void setMessageType(String messageType) {
        this._messageType = messageType;
    }

    public String getMessage() {
        return _message;
    }

    public void setMessage(String message) {
        this._message = message;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Problem other)) return false;

        // null safety checks
        if (!Objects.equals(_source, other._source)) return false;
        if (_problemTime == null || other._problemTime == null) {
            return _problemTime == other._problemTime;
        }
        
        return _problemTime.getTime() == other._problemTime.getTime();
    }


    public int hashCode() {
        int sourceHash = _source != null ? _source.hashCode() : 0;
        long time = _problemTime != null ? _problemTime.getTime() : 0L;
        return 29 * sourceHash + Long.hashCode(time);
    }
}
