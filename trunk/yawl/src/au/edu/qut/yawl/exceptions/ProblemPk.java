package au.edu.qut.yawl.exceptions;

import java.util.Date;

import javax.persistence.Embeddable;

import au.edu.qut.yawl.persistence.PersistableObject;

@Embeddable
public class ProblemPk implements PersistableObject {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
	
    private String _source;
    private Date _timeStamp;
    
	public ProblemPk() {
		super();
	}

    public String getSource() {
        return _source;
    }

    public void setSource(String source) {
        this._source = source;
    }

    public Date getTimeStamp() {
        return _timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this._timeStamp = timeStamp;
    }
    
    public int hashCode() {
    	return (getSource().toString() + getTimeStamp().toString().hashCode()).hashCode();
    }
    
    public boolean equals(Object obj) {
    	boolean equal = false;
    	if (obj instanceof ProblemPk) {
    		if (((ProblemPk) obj).getSource().equals(this.getSource()) && ((ProblemPk) obj).getTimeStamp().equals(this.getTimeStamp())) {
    			equal = true;
    		}
    	}
    	return equal;
    }
}
