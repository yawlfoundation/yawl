package com.nexusbpm.editor.util;

import java.io.Serializable;

/**
 * Operator allows class level support for operators.  It will hopefully replace both the code in ExtendedTableModel
 * as well as the code in CompatorComponent.  Unlike those two, it has a full 6 operators, rather than 5 and 3 respectively.
 * And its intention is to use the Operator instances rather than the integers - so it should be a bit more obvious.
 * Assuming LoopComponent and SwitchComponent are built, they would use this class as well along with ComparatorHelper.
 *
 * @author Mitchell J. Friedman
 * @version $Revision: 1.10 $
 * @created April 2005
 * @see ComparatorHelper
 * @see ExtendedTableModel
 */
public class Operator implements Serializable {
	/**
	 * Integer constant for less-than
	 */
	public final static int LESS_THAN_INT = 0;
	/**
	 * Integer constant for less-than-or-equal-to
	 */
	public final static int LESS_THAN_OR_EQUAL_TO_INT = 1;
	/**
	 * Integer constant for equal-to
	 */
	public final static int EQUAL_TO_INT = 2;
	/**
	 * Integer constant for greater-than-or-equal-to
	 */
	public final static int GREATER_THAN_OR_EQUAL_TO_INT = 3;
	/**
	 * Integer constant for great-than
	 */
	public final static int GREATER_THAN_INT = 4;
	/**
	 * Integer constant for not-equal-to
	 */
	public final static int NOT_EQUAL_TO_INT = 5;

	/**
	 * The less-than operator.
	 */
	public final static Operator LESS_THAN = new Operator( LESS_THAN_INT );
	/**
	 * The less-than-or-equal-to operator.
	 */
	public final static Operator LESS_THAN_OR_EQUAL_TO = new Operator( LESS_THAN_OR_EQUAL_TO_INT );
	/**
	 * The equal-to operator.
	 */
	public final static Operator EQUAL_TO = new Operator( EQUAL_TO_INT );
	/**
	 * The greater-than-or-equal-to operator.
	 */
	public final static Operator GREATER_THAN_OR_EQUAL_TO = new Operator( GREATER_THAN_OR_EQUAL_TO_INT );
	/**
	 * The greater-than operator.
	 */
	public final static Operator GREATER_THAN = new Operator( GREATER_THAN_INT );
	/**
	 * The not-equal-to operator.
	 */
	public final static Operator NOT_EQUAL_TO = new Operator( NOT_EQUAL_TO_INT );

	/**
	 * The String representations of the operators.
	 */
	public final static String[] operators = {"<", "<=", "=", ">=", ">", "<>"};
	/**
	 * The set operator versions of the String representations of operators.
	 *
	 * @see #toSetString()
	 */
	protected final static String[] setOperators = {")", "]", "", "[", "(", ""};
	/**
	 * An array of operator objects that correspond to the operator integers.
	 */
	protected final static Operator[] operatorObjects = {LESS_THAN,
														 LESS_THAN_OR_EQUAL_TO, EQUAL_TO, GREATER_THAN_OR_EQUAL_TO,
														 GREATER_THAN, NOT_EQUAL_TO};

	/**
	 * The integer constant of the operation represented by the instance.
	 */
	protected int op;

	/**
	 * Create a new <tt>Operator</tt> with the operator constant <tt>aop</tt>.
	 *
	 * @param aop one of the integer constants defined in this class (<tt>Operator</tt>)
	 */
	protected Operator( int aop ) {
		op = aop;
	}

	/**
	 * Returns the integer constant of the operation represented by this <tt>Operator</tt>
	 *
	 * @return one of the integer constants defined in this class (<tt>Operator</tt>)
	 */
	public int getOp() {
		return op;
	}

	/**
	 * Returns <tt>true</tt> if and only if the given <tt>Operator</tt> has the same
	 * operator constant as this <tt>Operator</tt>.
	 *
	 * @param o the other <tt>Operator</tt> to compare to.
	 * @return whether the <tt>Operator</tt>s are the same.
	 */
	public boolean equals( Operator o ) {
		return o != null && o.getOp() == getOp();
	}

	/**
	 * Returns the visual representation of the given operator as a <tt>String</tt>.
	 *
	 * @param operator the operator in question
	 * @return the operator as a <tt>String</tt>.
	 */
	public String toString( Operator operator ) {
		String ret;

		int op = operator.getOp();

		if( op >= 0 && op < operators.length )
			ret = operators[ op ];
		else
			ret = null;

		return ret;
	}

	/**
	 * Returns the set boundary operator for the given operator.<br>
	 *
	 * @param operator the operator in question
	 * @return the set boundary operator.
	 * @see #toSetString()
	 */
	public String toSetString( Operator operator ) {
		String ret;

		int op = operator.getOp();

		if( op >= 0 && op < setOperators.length )
			ret = setOperators[ op ];
		else
			ret = null;

		return ret;
	}

	/**
	 * Returns a <tt>String</tt> displaying a set boundary operator equivalent
	 * to this operator. The greater-than operator will return:<br>
	 * <tt>(</tt><br>
	 * the greater-than-or-equal-to operator will return:<br>
	 * <tt>[</tt><br>
	 * the less-than operator will return:<br>
	 * <tt>)</tt><br>
	 * and the less-than-or-equal-to operator will return:<br>
	 * <tt>]</tt>
	 *
	 * @return the appropriate set boundary operator.
	 */
	public String toSetString() {
		return toSetString( this );
	}

	/**
	 * Returns the visual representation of this operator as a <tt>String</tt>.
	 *
	 * @return this operator as a <tt>String</tt>.
	 */
	public String toString() {
		return toString( this );
	}

	/**
	 * Gets the Operator object corresponding with the specified string representation
	 * of the operator.
	 *
	 * @param operator the string representation of the operator.
	 * @return the operator object.
	 */
	public static Operator getOperator( String operator ) {
		for( int index = 0; index < operators.length; index++ ) {
			if( operators[ index ].equals( operator ) ) {
				return operatorObjects[ index ];
			}
		}
		throw new RuntimeException( "Invalid operator: " + operator );
	}
}
