/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao.restrictions;

import java.util.Date;

import junit.framework.TestCase;
import au.edu.qut.yawl.persistence.dao.restrictions.LogicalRestriction.Operation;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;

public class TestRestrictionStringConverter extends TestCase {
	private Restriction a = new PropertyRestriction( "a", Comparison.EQUAL, "a" );
	private Restriction notA = new NegatedRestriction( a );
	private Restriction b = new PropertyRestriction( "b", Comparison.EQUAL, "b" );
	private Restriction notB = new NegatedRestriction( b );
	
	Restriction[] r = {
			// make sure Unrestricted works
			new Unrestricted(),
			
			// make sure basic property restrictions work
			new PropertyRestriction( "foo", Comparison.LIKE, "asdf%" ),
			
			// make sure negated restrictions work
			new NegatedRestriction( new PropertyRestriction( "bar", Comparison.EQUAL, "asdf" ) ),
			
			// make sure logical restrictions work, and
			// test different kinds of values and operations for property restrictions
			new LogicalRestriction(
					new PropertyRestriction( "q", Comparison.EQUAL, null),
					Operation.AND,
					new NegatedRestriction(
							new PropertyRestriction( "wtargawrg", Comparison.GREATER_THAN_OR_EQUAL, Long.valueOf( 5 ) ) ) ),
			new LogicalRestriction(
					new PropertyRestriction( "", Comparison.LESS_THAN, Double.valueOf( 4.2 ) ),
					Operation.OR,
					new PropertyRestriction( "p", Comparison.LESS_THAN_OR_EQUAL, Float.valueOf( 3.3f ) ) ),
			new LogicalRestriction(
					new PropertyRestriction( "asd", Comparison.NOT_EQUAL, new Date() ),
					Operation.XOR,
					new PropertyRestriction( "q", Comparison.GREATER_THAN, Integer.valueOf( 123456 ) ) ),
			new LogicalRestriction(
					new Unrestricted(),
					Operation.NAND,
					new Unrestricted() ),
			
			// make sure nested logical restrictions work
			new LogicalRestriction(
					new LogicalRestriction( a, Operation.AND, notB ),
					Operation.OR,
					new LogicalRestriction( b, Operation.AND, notA ) ),
			
			// make sure all kinds of weird strings for property names and values work
			new PropertyRestriction( "qwerty", Comparison.EQUAL, "a ( asdf" ),
			new PropertyRestriction( "qwerty", Comparison.EQUAL, "a ) asdf" ),
			new PropertyRestriction( "qwerty", Comparison.EQUAL, "a ' asdf" ),
			new PropertyRestriction( "qwe(ty", Comparison.EQUAL, "asdf" ),
			new PropertyRestriction( "qwe'ty", Comparison.EQUAL, "asdf" ),
			new PropertyRestriction( "qwe)ty", Comparison.EQUAL, "asdf" )
	};
	
	public void testRestrictions() {
		for( int index = 0; index < r.length; index++ ) {
			System.out.println( "comparing restriction " + index );
			compareRestrictions(
					r[ index ],
					RestrictionStringConverter.stringToRestriction(
							RestrictionStringConverter.restrictionToString( r[ index ] ) ) );
		}
	}
	
	
	private void compareRestrictions( Restriction r1, Restriction r2 ) {
		if( ! r1.getClass().equals( r2.getClass() ) ) {
			throw new RuntimeException(
					r1.getClass().toString() + " not equal to " + r2.getClass().toString() );
		}
		
		if( r1 instanceof Unrestricted ) {
			// nothing to check for unrestricted
		}
		else if( r1 instanceof LogicalRestriction ) {
			LogicalRestriction lr1 = (LogicalRestriction) r1;
			LogicalRestriction lr2 = (LogicalRestriction) r2;
			
			if( ! lr1.getOperation().equals( lr2.getOperation() ) ) {
				throw new RuntimeException(
						"Comparison operation " + lr1.getOperation() + " != " + lr2.getOperation() );
			}
			
			compareRestrictions( lr1.getRestriction1(), lr2.getRestriction1() );
			compareRestrictions( lr1.getRestriction2(), lr2.getRestriction2() );
		}
		else if( r1 instanceof NegatedRestriction ) {
			NegatedRestriction nr1 = (NegatedRestriction) r1;
			NegatedRestriction nr2 = (NegatedRestriction) r2;
			
			compareRestrictions( nr1.getRestriction(), nr2.getRestriction() );
		}
		else if( r1 instanceof PropertyRestriction ) {
			PropertyRestriction pr1 = (PropertyRestriction) r1;
			PropertyRestriction pr2 = (PropertyRestriction) r2;
			
			if( ! pr1.getPropertyName().equals( pr2.getPropertyName() ) ) {
				throw new RuntimeException(
						"Property name '" + pr1.getPropertyName() + "' != '" +
						pr2.getPropertyName() );
			}
			if( ! pr1.getComparison().equals( pr2.getComparison() ) ) {
				throw new RuntimeException(
						"Comparison '" + pr1.getComparison() + "' != '" + pr2.getComparison() );
			}
			
			if( pr1.getValue() == null && pr2.getValue() != null ) {
				throw new RuntimeException( "null property value became '" + pr2.getValue() + "' " +
						pr2.getValue().getClass() );
			}
			else if( pr1.getValue() != null && pr2.getValue() == null ) {
				throw new RuntimeException(
						pr1.getValue().getClass() + " " + pr1.getValue() + " became null!" );
			}
			else if( pr1.getValue() != null && ! pr1.getValue().equals( pr2.getValue() ) ) {
				throw new RuntimeException(
						pr1.getValue().getClass() + " " + pr1.getValue() + " != " +
						pr2.getValue().getClass() + " " + pr2.getValue() );
			}
		}
		else {
			throw new UnsupportedOperationException( "Restriction " + r1.getClass().getName() +
					" is not supported!" );
		}
	}
}
