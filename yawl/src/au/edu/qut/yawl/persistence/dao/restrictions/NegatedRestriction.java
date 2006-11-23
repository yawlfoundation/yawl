/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao.restrictions;

/**
 * Objects fit the negated restriction if and only if they do not fit the
 * restriction contained inside the negated restriction.
 * 
 * @author Nathan Rose
 */
public class NegatedRestriction implements Restriction {
	private Restriction restriction;
	
	public NegatedRestriction( Restriction restriction ) {
		this.restriction = restriction;
	}
	
	public Restriction getRestriction() {
		return restriction;
	}
	
	public void setRestriction( Restriction restriction ) {
		this.restriction = restriction;
	}
}
