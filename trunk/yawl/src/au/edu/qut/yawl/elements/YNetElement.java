/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.io.Serializable;



/**
 * 
 * Abstract class. A super class of YExternalNetElement.   Has little relation to the
 * YAWL paper.
 * @author Lachlan Aldred
 * 
 */
public abstract class YNetElement implements Cloneable, Comparable, Serializable {

	private String _id;

    /**
     * Null constructor for hibernate
     *
     */
    public YNetElement() {
    	setID("null");
    }
    
    /**
     * Constructor
     * @param id
     */
    protected YNetElement(String id) {
        setID(id);
    }

    /**
     * @return the id of this YNetElement
     */
    public String getID() {
        return _id;
    }

    @Override
	public int hashCode() {
		return getID().hashCode();
	}
    
    /**
     * Set method only used by hibernate
     * 
     * @param id
     */
    protected void setID(String id) {
    	if (id != null) this._id = id.replace(" ", "_");
    }

    public String toString() {
        String fullClassName = getClass().getName();
        String shortClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 2);
        return shortClassName + ":" + getID();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int compareTo(Object o) {
        YNetElement ne = (YNetElement) o;
        return getID().compareTo(ne.getID());
    }
}
