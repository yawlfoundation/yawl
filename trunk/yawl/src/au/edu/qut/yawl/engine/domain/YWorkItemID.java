/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.domain;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import au.edu.qut.yawl.elements.state.YIdentifier;
/**
 * 
 * @author Lachlan Aldred
 * Date: 23/05/2003
 * Time: 14:32:32
 * 
 */
@Embeddable
public class YWorkItemID implements Serializable {
    private static final char[] _uniqifier = UniqueIDGenerator.newAlphas();
    private char[] _uniqueID;
    private YIdentifier _caseID;
    private String _taskID;

    public YWorkItemID() {
    	
    }

    public YWorkItemID(YIdentifier caseID, String taskID) {
        _uniqueID = (char[]) _uniqifier.clone();
        UniqueIDGenerator.nextInOrder(_uniqifier);
        _caseID = caseID;
        _taskID = taskID;
    }

    public String toString() {
        return _caseID.toString() + ":" + _taskID;
    }

    @OneToOne//(cascade={CascadeType.PERSIST})
    @OnDelete(action=OnDeleteAction.CASCADE)
    public YIdentifier getCaseID() {
        return _caseID;
    }

    public void setCaseID(YIdentifier id) {
    	this._caseID = id;
    }

    @Basic
    public String getTaskID() {
        return _taskID;
    }

    public void setTaskID(String taskid) {
    	this._taskID = taskid;
    }
    
    
    @Column(name="identifier_id")
    public String getUniqueID() {
    	if (getId()!=null) {
    		return getId().toString();
    	}
        return new String(_uniqueID);
    }
    public void setUniqueID(String uqid) {
    	this._uniqueID = uqid.toCharArray();
    }
    
    private Long _id = null;  
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    public Long getId() {
		return _id;
	}
    
	public void setId( Long id ) {
		_id = id;
	}
}

class UniqueIDGenerator {

    static char[] newAlphas() {
        char[] alphas = new char[25];
        Arrays.fill(alphas, '0');
        return alphas;
    }

    /**
     *
     * @param chars
     * @return
     */
    static char[] nextInOrder(char[] chars) {
        for (int i = chars.length - 1; i >= 0; i--) {
            char aChar = chars[i];
            if (inRange(aChar)) {
                aChar = inc(aChar);
                chars[i] = aChar;
                break;
            } else {
                chars[i] = '0';
            }
        }
        return chars;
    }

    private static char inc(char aChar) {
        /**
         * 0 = 48
         * 9 = 57
         * A = 65
         * Z = 90
         * a = 97
         * z = 122 */
        if (aChar == '9') {
            return 'A';
        }
        if (aChar == 'Z') {
            return 'a';
        }
        if (aChar == 'z') {
            throw new IllegalArgumentException("Shouldn't be called with 'z'.");
        }
        char incChar = (++aChar);
        return incChar;
    }

    private static boolean inRange(char aChar) {
        boolean inRange = ((int) aChar) < ((int) 'z');
        return inRange;
    }

}
