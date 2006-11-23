/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao.restrictions;

/**
 * The PropertyRestriction restricts a particular property based on the type
 * of comparison in the PropertyRestriction.
 * 
 * @author Nathan Rose
 */
public class PropertyRestriction implements Restriction {
	/** The types of comparisons. */
	public enum Comparison {
		EQUAL,
		LIKE,
		NOT_EQUAL,
		LESS_THAN,
		LESS_THAN_OR_EQUAL,
		GREATER_THAN,
		GREATER_THAN_OR_EQUAL
	};
	
	public static Comparison getComparison( String comparison ) {
		if( Comparison.EQUAL.toString().equals( comparison ) ) {
			return Comparison.EQUAL;
		}
		else if( Comparison.LIKE.toString().equals( comparison ) ) {
			return Comparison.LIKE;
		}
		else if( Comparison.NOT_EQUAL.toString().equals( comparison ) ) {
			return Comparison.NOT_EQUAL;
		}
		else if( Comparison.LESS_THAN.toString().equals( comparison ) ) {
			return Comparison.LESS_THAN;
		}
		else if( Comparison.LESS_THAN_OR_EQUAL.toString().equals( comparison ) ) {
			return Comparison.LESS_THAN_OR_EQUAL;
		}
		else if( Comparison.GREATER_THAN.toString().equals( comparison ) ) {
			return Comparison.GREATER_THAN;
		}
		else if( Comparison.GREATER_THAN_OR_EQUAL.toString().equals( comparison ) ) {
			return Comparison.GREATER_THAN_OR_EQUAL;
		}
		else {
			throw new IllegalArgumentException(
					"The string '" + comparison + "' is not a valid Comparison!" );
		}
	}
	
	private String propertyName;
	private Comparison comparison;
	private Object value;
	
	public PropertyRestriction( String propertyName, Comparison comparison, Object value ) {
		this.propertyName = propertyName;
		this.comparison = comparison;
		this.value = value;
	}
	
	public Comparison getComparison() {
		return comparison;
	}
	
	public void setComparison( Comparison comparison ) {
		this.comparison = comparison;
	}
	
	public String getPropertyName() {
		return propertyName;
	}
	
	public void setPropertyName( String propertyName ) {
		this.propertyName = propertyName;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue( Object value ) {
		this.value = value;
	}
}
