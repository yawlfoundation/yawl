package com.nexusbpm.editor.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Instances of this class can compare two objects with and without an Operator
 *
 * @author Mitchell J. Friedman
 */
public class ComparatorHelper implements Comparator, Serializable {
	/**
	 * Returns whether the two given objects are equivalent.
	 *
	 * @param o1 one of the objects to compare
	 * @param o2 the other object to compare
	 * @return true if and only if compare(o1, o2) returns 0
	 * @see #compare(Object, Object)
	 */
	public boolean equals( Object o1, Object o2 ) {
		return 0 == compare( o1, o2 );
	}

	/**
	 * Attempts to compare the two given Objects based on the given kind of
	 * comparison. Returns a negative integer, zero, or a positive integer
	 * as the first Object is considered to be less than, equal to, or greater
	 * than the second Object.
	 *
	 * @param o1 the first Object to compare.
	 * @param o2 the second Object to compare.
	 * @return the integer result of the comparison.
	 */
	public int compare( Object o1, Object o2 ) {
		int comparison;

		if( null == o1 ) {
			if( null == o2 )
				comparison = 0;
			else
				comparison = -1;
		}
		else if( null == o2 ) {
			comparison = 1;
		}
		else {
			if( !(o1 instanceof Comparable) ) {
				if( !(o2 instanceof Comparable) ) {
					if( o1 instanceof Boolean && o2 instanceof Boolean ) {
						comparison = compareBoolean( (Boolean) o1, (Boolean) o2 );
					}
					else {
						comparison = 0;
					}
				}
				else
					comparison = -1;
			}
			else if( !(o2 instanceof Comparable) )
				comparison = 1;
			else
				comparison = compareTo( (Comparable) o1, (Comparable) o2 );
		}

		return comparison;
	}

	/**
	 * Compares the two given Objects based on the given operator. For
	 * example, if the operator is less-than it returns true when the
	 * first Object is less than the second Object. A parameter that is
	 * <tt>null</tt> is considered to be less than any parameter that is
	 * not <tt>null</tt>.
	 *
	 * @param o1       the first Object to compare.
	 * @param operator the kind of comparison to make
	 * @param o2       the second Object to compare.
	 * @return the result of comparing the given Objects using the given operator.
	 * @see #compare(Object, Object)
	 */
	public boolean compare( Object o1, Operator operator, Object o2 ) {
		int comparison = compare( o1, o2 );
		return compareResults( operator, comparison );
	}

	/**
	 * Compares the two given Strings. Returns a negative integer, zero, or a
	 * positive integer as the first String is less than, equal to, or greater
	 * than the second String.
	 *
	 * @param s1
	 * @param s2
	 * @return the integer result of the comparison.
	 * @see java.lang.String#compareTo(java.lang.String)
	 */
	public int compareStrings( String s1, String s2 ) {
		int comparison;

		if( s1 == null || s2 == null ) {
			comparison = compareTo( (Comparable) s1, (Comparable) s2 );
		}
		else
			comparison = s1.compareTo( s2 );

		return comparison;
	}

	/**
	 * Compares the two given Strings based on the given operator. For
	 * example, if the operator is less-than it returns true when the
	 * first String is less than the second String.
	 *
	 * @param s1       the first String to compare.
	 * @param operator the kind of comparison to make
	 * @param s2       the second String to compare.
	 * @return the result of comparing the given Strings using the given operator.
	 */
	public boolean compareStrings( String s1, Operator operator, String s2 ) {
		int comparison = compareStrings( s1, s2 );
		return compareResults( operator, comparison );
	}

	/**
	 * Compares the two given longs based on the given kind of comparison.
	 * Returns true if the first long compares with the second long in the
	 * manner indicated by the given kind of comparison. For example, if
	 * the operator is less-than and the two numbers are 5 and 8 the result
	 * is true, but if the numbers are 8 and 5 the result is false.
	 *
	 * @param x1       the first long
	 * @param operator the type of comparison to make
	 * @param x2       the second long
	 * @return the boolean result of the comparison.
	 */
	public boolean compareTo( long x1, Operator operator, long x2 ) {
		int comparison = compareTo( x1, x2 );
		return compareResults( operator, comparison );
	}

	/**
	 * Compares the two given longs. Returns a negative integer, zero, or a
	 * positive integer as the first long is less than, equal to, or greater
	 * than the second long.
	 *
	 * @param x1 the first long to compare
	 * @param x2 the second long to compare
	 * @return a negative integer, zero, or a positive integer as the first
	 *         long is less than, equal to, or greater than the second long.
	 */
	public int compareTo( long x1, long x2 ) {
		int comparison;
		if( x1 < x2 )
			comparison = -1;
		else if( x1 > x2 )
			comparison = 1;
		else
			comparison = 0;

		return comparison;
	}

	/**
	 * Compares the two given doubles based on the given kind of comparison.
	 * Returns true if the first double compares with the second double in the
	 * manner indicated by the given kind of comparison. For example, if
	 * the operator is less-than and the two numbers are 5.0 and 8.0 the result
	 * is true, but if the numbers are 8.0 and 5.0 the result is false.
	 *
	 * @param x1       the first double
	 * @param operator the type of comparison to make
	 * @param x2       the second double
	 * @return the boolean result of the comparison.
	 */
	public boolean compareTo( double x1, Operator operator, double x2 ) {
		int comparison = compareTo( x1, x2 );
		return compareResults( operator, comparison );
	}

	/**
	 * Compares the two given doubles. Returns a negative integer, zero, or a
	 * positive integer as the first double is less than, equal to, or greater
	 * than the second double.
	 *
	 * @param x1 the first double to compare
	 * @param x2 the second double to compare
	 * @return a negative integer, zero, or a positive integer as the first
	 *         double is less than, equal to, or greater than the second double.
	 */
	public int compareTo( double x1, double x2 ) {
		int comparison;
		if( x1 < x2 )
			comparison = -1;
		else if( x1 > x2 )
			comparison = 1;
		else
			comparison = 0;

		return comparison;
	}

	/**
	 * Compares the two given objects for ordering. Returns a negative
	 * integer, zero, or a positive integer as the first object is less
	 * than, equal to, or greater than the second object.
	 *
	 * @param c1 the first of the objects to compare
	 * @param c2 the second of the objects to compare
	 * @return a negative integer, zero, or a positive integer as the first
	 *         object is less than, equal to, or greater than the second object.
	 */
	public int compareTo( Comparable c1, Comparable c2 ) {
		int comparison;

		if( null == c1 ) {
			if( null == c2 )
				comparison = 0;
			else
				comparison = -1;
		}
		else if( null == c2 ) {
			comparison = 1;
		}
		else {
			if( c1.getClass() == c2.getClass() ) {
				// same class - just do the comparison
				comparison = c1.compareTo( c2 );
			}
			else if( c1 instanceof Number && c2 instanceof Number ) {
				// different classes but they are both numbers
				comparison = compareNumbers( (Number) c1, (Number) c2 );
			}
			else if( c1 instanceof String && c2 instanceof String ) {
				// one is a string and the other is a null
				comparison = compareStrings( (String) c1, (String) c2 );
			}
			else if( c1 instanceof String && c2 instanceof Number ) {
				comparison = compareNumbers( convertToDouble( (String) c1 ), (Number) c2 );
			}
			else if( c1 instanceof Number && c2 instanceof String ) {
				comparison = compareNumbers( (Number) c1, convertToDouble( (String) c2 ) );
			}
			else
			// two not comparables should karmically be the same in some universe
				comparison = 0;
		}

		return comparison;
	}

	/**
	 * Converts the given String to a Double. Returns null if there
	 * is an error converting to a double.
	 *
	 * @param s1 the String to convert.
	 * @return a Double or null.
	 */
	public Double convertToDouble( String s1 ) {
		Double ret = null;

		try {
			ret = new Double( s1 );
		}
		catch( NumberFormatException e ) {
		}

		return ret;
	}

	/**
	 * Compares the two given Numbers. Returns a negative integer, zero, or a
	 * positive integer as the first Number is less than, equal to, or greater
	 * than the second Number.
	 *
	 * @param x1 the first Number to compare
	 * @param x2 the second Number to compare
	 * @return the integer result of the comparison.
	 */
	public int compareNumbers( Number x1, Number x2 ) {

		int comparison;

		if( null == x1 || null == x2 )
			comparison = compareTo( (Comparable) x1, (Comparable) x2 );
		else if( x1.getClass() == x2.getClass() ) {
			Comparable comparable = (Comparable) x1;
			comparison = comparable.compareTo( x2 );
		}
		else {
			long long1a = x1.longValue();
			long long2a = x2.longValue();
			comparison = compareTo( long1a, long2a );

			if( 0 == comparison ) {
				double double1a = x1.doubleValue();
				double double2a = x2.doubleValue();
				comparison = compareTo( double1a, double2a );
			}
		}

		return comparison;
	}

	/**
	 * Compares the two given Booleans. Returns a negative integer, zero, or a
	 * positive integer as the first Boolean is considered less than, equal to,
	 * or greater than the second Boolean. For this operation <tt>null</tt> is
	 * considered to be less than any Boolean and <tt>true</tt> is considered
	 * to be less than <tt>false</tt>.
	 *
	 * @param b1 the first Boolean value.
	 * @param b2 the second Boolean value.
	 * @return the integer result of the comparison.
	 */
	public int compareBoolean( Boolean b1, Boolean b2 ) {

		int comparison;

		if( null == b1 ) {
			if( null == b2 ) {
				comparison = 0;
			}
			else {
				comparison = -1;
			}
		}
		else if( null == b2 ) {
			comparison = 1;
		}
		else if( b1.equals( b2 ) ) {
			comparison = 0;
		}
		else if( b1.booleanValue() == false ) {
			comparison = 1;
		}
		else {
			comparison = -1;
		}

		return comparison;
	}

	/**
	 * Compares the two given Booleans based on the given operator. For
	 * example, if the operator is less-than it returns true when the
	 * first Boolean is less than the second Boolean.
	 *
	 * @param b1       the first Boolean to compare.
	 * @param operator the kind of comparison to make
	 * @param b2       the second Boolean to compare.
	 * @return the result of comparing the given Booleans using the given operator.
	 * @see #compareBoolean(Boolean, Boolean)
	 */
	public boolean compareBoolean( Boolean b1, Operator operator, Boolean b2 ) {
		int comparison = compareBoolean( b1, b2 );
		return compareResults( operator, comparison );
	}

	/**
	 * Compares the two given Comparables based on the given operator. For
	 * example, if the operator is less-than it returns true when the first
	 * Comparable is less than the second Comparable.
	 *
	 * @param c1       the first Comparable to compare.
	 * @param operator the kind of comparison to make
	 * @param c2       the second Comparable to compare.
	 * @return the result of comparing the given Comparables using the given operator.
	 */
	public boolean compareTo( Comparable c1, Operator operator, Comparable c2 ) {
		int comparison = compareTo( c1, c2 );
		return compareResults( operator, comparison );
	}

	/**
	 * Compares the two given Numbers based on the given operator. For
	 * example, if the operator is less-than it returns true when the
	 * first Number is less than the second Number.
	 *
	 * @param x1       the first Number to compare.
	 * @param operator the kind of comparison to make
	 * @param x2       the second Number to compare.
	 * @return the result of comparing the given Numbers using the given operator.
	 */
	public boolean compareNumbers( Number x1, Operator operator, Number x2 ) {
		int comparison = compareNumbers( x1, x2 );
		return compareResults( operator, comparison );
	}

	/**
	 * Checks whether the given result of a comparison matches the given
	 * operator. For example, returns true if the operator is less-than
	 * when the comparison is a negative number, but returns true if the
	 * operator is equal-to only when the comparison is zero. The compareTo
	 * functions should be used to obtain the integer to pass as the
	 * comparison. (@see #compareTo(Comparable, Comparable))
	 *
	 * @param operator   the type of comparison
	 * @param comparison the result of comparing two things
	 * @return whether the comparison fits the given operator
	 */
	protected boolean compareResults( Operator operator, int comparison ) {
		boolean ret = false;

		if( null == operator ) {
			// if no operator than true enough
			return true;
		}

		int op = operator.getOp();

		switch( op ) {
			case Operator.LESS_THAN_INT:
				if( comparison < 0 )
					ret = true;
				break;
			case Operator.LESS_THAN_OR_EQUAL_TO_INT:
				if( comparison <= 0 )
					ret = true;
				break;
			case Operator.EQUAL_TO_INT:
				if( comparison == 0 )
					ret = true;
				break;
			case Operator.GREATER_THAN_OR_EQUAL_TO_INT:
				if( comparison >= 0 )
					ret = true;
				break;
			case Operator.GREATER_THAN_INT:
				if( comparison > 0 )
					ret = true;
				break;
			case Operator.NOT_EQUAL_TO_INT:
				if( comparison != 0 )
					ret = true;
			default:
				break;
		}

		return ret;

	}
}
