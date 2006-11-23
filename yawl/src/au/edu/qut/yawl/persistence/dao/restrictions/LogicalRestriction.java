/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao.restrictions;

/**
 * The logical restriction performs a logical operation on two sub-restrictions.
 * 
 * @author Nathan Rose
 */
public class LogicalRestriction implements Restriction {
	/** The types of logical operations. */
	public enum Operation { AND, OR, XOR, NOR, NAND };
	
	private Restriction restriction1;
	private Operation operation;
	private Restriction restriction2;
	
	public LogicalRestriction( Restriction restriction1, Operation operation, Restriction restriction2 ) {
		this.restriction1 = restriction1;
		this.operation = operation;
		this.restriction2 = restriction2;
	}
	
	public Operation getOperation() {
		return operation;
	}
	
	public void setOperation( Operation operation ) {
		this.operation = operation;
	}
	
	public Restriction getRestriction1() {
		return restriction1;
	}
	
	public void setRestriction1( Restriction restriction1 ) {
		this.restriction1 = restriction1;
	}
	
	public Restriction getRestriction2() {
		return restriction2;
	}
	
	public void setRestriction2( Restriction restriction2 ) {
		this.restriction2 = restriction2;
	}
	
	public static Operation getOperation( String operation ) {
		if( Operation.AND.toString().equals( operation ) ) {
			return Operation.AND;
		}
		else if( Operation.OR.toString().equals( operation ) ) {
			return Operation.OR;
		}
		else if( Operation.XOR.toString().equals( operation ) ) {
			return Operation.XOR;
		}
		else if( Operation.NOR.toString().equals( operation ) ) {
			return Operation.NOR;
		}
		else if( Operation.NAND.toString().equals( operation ) ) {
			return Operation.NAND;
		}
		else {
			throw new IllegalArgumentException( "Operation '" + operation + "' is not a legal operation!" );
		}
	}
}
