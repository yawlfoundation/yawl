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

package org.yawlfoundation.yawl.exceptions;

import java.util.Date;
import java.io.Serializable;

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
        return _messageType;
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
        if (!(o instanceof Problem)) return false;

        final Problem warning = (Problem) o;
        return _problemTime.equals(warning._problemTime) && _source.equals(warning._source);
    }

    public int hashCode() {
        return 29 * _source.hashCode() + _problemTime.hashCode();
    }
}
