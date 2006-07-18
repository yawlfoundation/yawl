/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.exceptions;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 * 
 * @author Lachlan Aldred
 * Date: 10/10/2005
 * Time: 19:47:43
 * 
 */
@Entity
@IdClass(ProblemPk.class)
public class Problem implements Serializable {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
	
    private String _source;
    private Date _timeStamp;
    private String _messageType;
    private String _message;
    public static final String EMPTY_RESOURCE_SET_MESSAGETYPE = "EmptyResourceSetType";

    /**
     * Default explicit constructor required by hibernate
     */
    public Problem() {
    }
    
    @Id
    public String getSource() {
        return _source;
    }

    public void setSource(String source) {
        this._source = source;
    }

    @Id
    public Date getTimeStamp() {
        return _timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this._timeStamp = timeStamp;
    }

    @Basic
    public String getMessageType() {
        return _messageType;
    }

    public void setMessageType(String messageType) {
        this._messageType = messageType;
    }

    @Basic
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

        if (!_timeStamp.equals(warning._timeStamp)) return false;
        if (!_source.equals(warning._source)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = _source.hashCode();
        result = 29 * result + _timeStamp.hashCode();  // TODO DM: Why 29 * result?  What is 29 for?
        return result;
    }
}
