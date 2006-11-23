/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao.restrictions;

import java.util.Date;

public final class RestrictionStringConverter {
	private RestrictionStringConverter() {}
	public static Restriction stringToRestriction( String restriction ) {
		if( restriction.startsWith( "(unrestricted)" ) ) {
			return new Unrestricted();
		}
		else if( restriction.startsWith( "(logical " ) ) {
			String operation = nextToken( restriction.substring( "(logical ".length() ), " " );
			String left = matchParens( restriction.substring( "(locical  ".length() + operation.length() ) );
			String right = matchParens( restriction.substring(
					"(logical   ".length() + left.length() + operation.length() ) );
			
			return new LogicalRestriction(
					stringToRestriction( left ),
					LogicalRestriction.getOperation( operation ),
					stringToRestriction( right ) );
		}
		else if( restriction.startsWith( "(negated " ) ) {
			String sub = matchParens( restriction.substring( "(negated ".length() ) );
			
			return new NegatedRestriction( stringToRestriction( sub ) );
		}
		else if( restriction.startsWith( "(property " ) ) {
			String property = nextToken( restriction.substring( "(property ".length() ), " " );
			String comparison = nextToken(
					restriction.substring( "(property  ".length() + property.length() ), " " );
			Object value = stringToObject( nextToken( restriction.substring(
					"(property   ".length() + property.length() + comparison.length() ), ")" ) );
			
			return new PropertyRestriction(
					property,
					PropertyRestriction.getComparison( comparison ),
					value );
		}
		else {
			throw new IllegalArgumentException( "Unable to parse restriction string:" + restriction );
		}
	}
	
	public static String restrictionToString( Restriction restriction ) {
		if( restriction instanceof Unrestricted ) {
			return "(unrestricted)";
		}
		else if( restriction instanceof LogicalRestriction ) {
			LogicalRestriction lr = (LogicalRestriction) restriction;
			
			return "(logical " +
				lr.getOperation().toString() + " " +
				restrictionToString( lr.getRestriction1() ) + " " +
				restrictionToString( lr.getRestriction2() ) + ")";
		}
		else if( restriction instanceof NegatedRestriction ) {
			NegatedRestriction nr = (NegatedRestriction) restriction;
			return "(negated " + restrictionToString( nr.getRestriction() ) + ")";
		}
		else if( restriction instanceof PropertyRestriction ) {
			PropertyRestriction pr = (PropertyRestriction) restriction;
			
			return "(property " +
				pr.getPropertyName() + " " +
				pr.getComparison() + " " +
				objectToString( pr.getValue() ) + ")";
		}
		else {
			throw new UnsupportedOperationException( "Restriction " + restriction.getClass().getName() +
					" is not supported!" );
		}
	}
	
	private static String nextToken( String string, String tokens ) {
		StringBuilder b = new StringBuilder();
		int index = 0;
		
		while( index < string.length() && !memberOf( string.charAt( index ), tokens ) ) {
			b.append( string.charAt( index ) );
			index += 1;
		}
		
		return b.toString();
	}
	
	private static boolean memberOf( char c, String str ) {
		for( int index = 0; index < str.length(); index++ ) {
			if( str.charAt( index ) == c ) {
				return true;
			}
		}
		return false;
	}
	
	private static String matchParens( String string ) {
		int depth = 1;
		int index = 1;
		
		StringBuilder b = new StringBuilder();
		b.append( string.charAt( 0 ) );
		
		while( depth > 0 ) {
			b.append( string.charAt( index ) );
			if( string.charAt( index ) == '(' ) {
				depth += 1;
			}
			else if( string.charAt( index ) == ')' ) {
				depth -= 1;
			}
			index++;
		}
		
		return b.toString();
	}
	
	private static String objectToString( Object object ) {
		if( object == null ) {
			return "null";
		}
		else if( object instanceof String ) {
			String str = (String) object;
			StringBuilder b = new StringBuilder();
			b.append( "'" );
			for( int index = 0; index < str.length(); index++ ) {
				if( str.charAt( index ) == '\'' ) {
					b.append( "\\" );
				}
				else if( str.charAt( index ) == '\\' ) {
					b.append( "\\\\" );
				}
				else {
					b.append( str.charAt( index ) );
				}
			}
			b.append( "'" );
			return b.toString();
		}
		else if( object instanceof Date ) {
			return "date:" + ((Date) object ).getTime();
		}
		else if( object instanceof Double ) {
			return "double:" + object;
		}
		else if( object instanceof Float ) {
			return "float:" + object;
		}
		else if( object instanceof Integer ) {
			return "integer:" + object;
		}
		else if( object instanceof Long ) {
			return "long:" + object;
		}
		else {
			throw new IllegalArgumentException(
					"Illegal type for property value! (" + object.getClass().toString() + ")" );
		}
	}
	
	private static Object stringToObject( String string ) {
		if( string.equals( "null" ) ) {
			return null;
		}
		else if( string.startsWith( "'" ) ) {
			StringBuilder b = new StringBuilder();
			for( int index = 1; index < string.length() - 1; index++ ) {
				if( string.charAt( index ) == '\'' ) {
					b.append( string.charAt( index + 1 ) );
					index += 1;
				}
				else {
					b.append( string.charAt( index ) );
				}
			}
			return b.toString();
		}
		else if( string.startsWith( "date:" ) ) {
			return new Date( Long.parseLong( string.substring( "date:".length() ) ) );
		}
		else if( string.startsWith( "double:" ) ) {
			return new Double( string.substring( "double:".length() ) );
		}
		else if( string.startsWith( "float:" ) ) {
			return new Float( string.substring( "float:".length() ) );
		}
		else if( string.startsWith( "integer:" ) ) {
			return new Integer( string.substring( "integer:".length() ) );
		}
		else if( string.startsWith( "long:" ) ) {
			return new Long( string.substring( "long:".length() ) );
		}
		else {
			throw new IllegalArgumentException( "String cannot be converted to object! (" + string + ")" );
		}
	}
}
