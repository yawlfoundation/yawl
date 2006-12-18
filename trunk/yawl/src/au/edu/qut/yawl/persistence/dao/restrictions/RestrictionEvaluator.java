/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao.restrictions;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;

/**
 * Evaluates a restriction for a single object.
 * 
 * @author Nathan Rose
 */
public class RestrictionEvaluator {
	private static final Log LOG = LogFactory.getLog( RestrictionEvaluator.class );
	
	public static boolean passesRestriction( Object object, Restriction r, BeanInfo typeInfo ) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if( r instanceof Unrestricted ) {
			return true;
		}
		else if( r instanceof LogicalRestriction ) {
			LogicalRestriction lr = (LogicalRestriction) r;
			switch( lr.getOperation() ) {
				case AND:
					return passesRestriction( object, lr.getRestriction1(), typeInfo )
						&&
						passesRestriction( object, lr.getRestriction2(), typeInfo );
				case OR:
					return passesRestriction( object, lr.getRestriction1(), typeInfo )
						||
						passesRestriction( object, lr.getRestriction2(), typeInfo );
				case XOR:
					boolean pass1 = passesRestriction( object, lr.getRestriction1(), typeInfo );
					boolean pass2 = passesRestriction( object, lr.getRestriction2(), typeInfo );
					return (pass1 && ! pass2) || (pass2 && ! pass1);
				case NOR:
					return ! ( passesRestriction( object, lr.getRestriction1(), typeInfo )
							||
							passesRestriction( object, lr.getRestriction2(), typeInfo ) );
				case NAND:
					return ! ( passesRestriction( object, lr.getRestriction1(), typeInfo )
							&&
							passesRestriction( object, lr.getRestriction2(), typeInfo ) );
				default:
					throw new UnsupportedOperationException(
							"Unsupported logical operation: " + lr.getOperation() );
			}
		}
		else if( r instanceof NegatedRestriction ) {
			NegatedRestriction nr = (NegatedRestriction) r;
			return ! passesRestriction( object, nr.getRestriction(), typeInfo );
		}
		else if( r instanceof PropertyRestriction ) {
			if( object == null ) {
				return false;
			}
			PropertyDescriptor[] properties = typeInfo.getPropertyDescriptors();
			
			PropertyRestriction restriction = (PropertyRestriction) r;
			PropertyDescriptor pd = null;
			for( int index = 0; index < properties.length; index++ ) {
				if( properties[ index ].getName().equalsIgnoreCase( restriction.getPropertyName() ) ) {
					pd = properties[ index ];
					break;
				}
			}
			if( pd == null ) {
				throw new IllegalArgumentException( "No property was found with the name " +
						restriction.getPropertyName() + " for the class " + object.getClass() );
			}
			
			return compare( object, pd, restriction );
		}
		else {
			throw new UnsupportedOperationException( "Restriction " + r.getClass().getName() +
					" is not supported!" );
		}
	}
	
	private static boolean compare(
			Object object, PropertyDescriptor property, PropertyRestriction restriction ) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if( property.getPropertyType().equals( int.class ) ||
				property.getPropertyType().equals( Integer.class ) ||
				property.getPropertyType().equals( long.class ) ||
				property.getPropertyType().equals( Long.class ) ) {
			Number val = (Number) property.getReadMethod().invoke( object, (Object[]) null );
			Number val2 = (Number) restriction.getValue();
			return compareLongs( val, restriction.getComparison(), val2 );
		}
		else if( property.getPropertyType().equals( float.class ) ||
				property.getPropertyType().equals( Float.class ) ||
				property.getPropertyType().equals( double.class ) ||
				property.getPropertyType().equals( Double.class ) ) {
			Number val = (Number) property.getReadMethod().invoke( object, (Object[]) null );
			Number val2 = (Number) restriction.getValue();
			return compareDoubles( val, restriction.getComparison(), val2 );
		}
		else if( property.getPropertyType().equals( String.class ) ) {
			String val = (String) property.getReadMethod().invoke( object, (Object[]) null );
			String val2 = (String) restriction.getValue();
			return compareStrings( val, restriction.getComparison(), val2 );
		} else if (property.getPropertyType().equals( boolean.class ) ) {
			boolean val = (Boolean) property.getReadMethod().invoke( object, (Object[]) null );
			boolean val2 = (Boolean) restriction.getValue();
			return compareBool( val, restriction.getComparison(), val2);
		}
		else if( restriction.getValue() == null ) {
            return property.getReadMethod().invoke( object, (Object[]) null ) == null;
        }
        else {
			throw new UnsupportedOperationException( "Cannot compare properties of type " +
					property.getPropertyType() );
		}
	}
	
	private static boolean compareLongs( Number actual, Comparison comparison, Number expected ) {
		switch( comparison ) {
			case EQUAL:
				return ( actual == null && expected == null ) ||
					( actual != null && expected != null &&
							actual.longValue() == expected.longValue() );
			case NOT_EQUAL:
				return actual != expected &&
					( actual == null || expected == null ||
					actual.longValue() != expected.longValue() );
			case LESS_THAN:
				return actual != null && expected != null &&
					actual.longValue() < expected.longValue();
			case LESS_THAN_OR_EQUAL:
				return actual != null && expected != null &&
					actual.longValue() <= expected.longValue();
			case GREATER_THAN:
				return actual != null && expected != null &&
					actual.longValue() > expected.longValue();
			case GREATER_THAN_OR_EQUAL:
				return actual != null && expected != null &&
					actual.longValue() >= expected.longValue();
			default:
				throw new UnsupportedOperationException(
						"Unsupported comparison for integers and longs: " + comparison );
		}
	}
	
	private static boolean compareDoubles( Number actual, Comparison comparison, Number expected ) {
		switch( comparison ) {
			case EQUAL:
				return ( actual == null && expected == null ) ||
					( actual != null && expected != null &&
							actual.doubleValue() == expected.doubleValue() );
			case NOT_EQUAL:
				return actual != expected &&
					( actual == null || expected == null ||
					actual.doubleValue() != expected.doubleValue() );
			case LESS_THAN:
				return actual != null && expected != null &&
					actual.doubleValue() < expected.doubleValue();
			case LESS_THAN_OR_EQUAL:
				return actual != null && expected != null &&
					actual.doubleValue() <= expected.doubleValue();
			case GREATER_THAN:
				return actual != null && expected != null &&
					actual.doubleValue() > expected.doubleValue();
			case GREATER_THAN_OR_EQUAL:
				return actual != null && expected != null &&
					actual.doubleValue() >= expected.doubleValue();
			default:
				throw new UnsupportedOperationException(
						"Unsupported comparison for integers and longs: " + comparison );
		}
	}
	
	private static boolean compareStrings( String actual, Comparison comparison, String expected ) {
		switch( comparison ) {
			case EQUAL:
				return actual == expected ||
					( actual != null && expected != null && actual.equals( expected ) );
			case LIKE:
				return actual != null && expected != null &&
					Pattern.matches( toRegex( expected ), actual );
			case NOT_EQUAL:
				return actual != expected &&
					( actual == null || expected == null ||
							! actual.equals( expected ) );
			case LESS_THAN:
				return actual != null && expected != null &&
					actual.compareTo( expected ) < 0;
			case LESS_THAN_OR_EQUAL:
				return actual != null && expected != null &&
					actual.compareTo( expected ) <= 0;
			case GREATER_THAN:
				return actual != null && expected != null &&
					actual.compareTo( expected ) > 0;
			case GREATER_THAN_OR_EQUAL:
				return actual != null && expected != null &&
					actual.compareTo( expected ) >= 0;
			default:
				throw new UnsupportedOperationException(
						"Unsupported comparison for strings: " + comparison );
		}
	}
	
	private static boolean compareBool( boolean actual, Comparison comparison, boolean expected ) {
		switch( comparison ) {
			case EQUAL:
				return actual == expected;
					
			case NOT_EQUAL:
				return actual != expected;
				
			default:
				throw new UnsupportedOperationException(
						"Unsupported comparison for strings: " + comparison );
		}
	}
	
	private static String toRegex( String like ) {
		StringBuilder b = new StringBuilder();
		char[] c = like.toCharArray();
		for( int index = 0; index < c.length; index++ ) {
			char ch = c[index];
			if( ch == '(' || ch == ')' ||
					ch == '\\' || ch == '*' ||
					ch == '$' || ch == '^' ||
					ch == '?' || ch == '+' ||
					ch == '{' || ch == '}' ||
					ch == '[' || ch == ']' ||
					ch == '|' ) {
				b.append( '\\' ).append( ch );
			}
			else if( ch == '\t' ) {
				b.append( '\\' ).append( 't' );
			}
			else if( ch == '\n' ) {
				b.append( '\\' ).append( 'n' );
			}
			else if( ch == '\r' ) {
				b.append( '\\' ).append( 'r' );
			}
			else if( ch == '\f' ) {
				b.append( '\\' ).append( 'f' );
			}
			else if( ch == '%' ) {
				b.append( '.' ).append( '*' );
			}
			else {
				b.append( ch );
			}
		}
		return b.toString();
	}
}
