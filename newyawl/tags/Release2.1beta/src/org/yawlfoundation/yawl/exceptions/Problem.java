/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
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

        if (!_problemTime.equals(warning._problemTime)) return false;
        if (!_source.equals(warning._source)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = _source.hashCode();
        result = 29 * result + _problemTime.hashCode();
        return result;
    }
}
