/* $Id: LongValue.java,v 1.2 2004/08/15 12:39:30 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2004 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.optimize.evaluation.value;

/**
 * This class represents a partially evaluated long value.
 *
 * @author Eric Lafortune
 */
public class LongValue extends Category2Value
{
    /**
     * Returns the specific long value, if applicable.
     */
    public long value()
    {
        return 0L;
    }


    // Basic binary methods.

    /**
     * Returns the generalization of this LongValue and the given other
     * LongValue.
     */
    public LongValue generalize(LongValue other)
    {
        return this;
    }


    /**
     * Returns the sum of this LongValue and the given LongValue.
     */
    public LongValue add(LongValue other)
    {
        return this;
    }

    /**
     * Returns the difference of this LongValue and the given LongValue.
     */
    public LongValue subtract(LongValue other)
    {
        return this;
    }

    /**
     * Returns the difference of the given LongValue and this LongValue.
     */
    public LongValue subtractFrom(LongValue other)
    {
        return this;
    }

    /**
     * Returns the product of this LongValue and the given LongValue.
     */
    public LongValue multiply(LongValue other)
    {
        return this;
    }

    /**
     * Returns the quotient of this LongValue and the given LongValue.
     */
    public LongValue divide(LongValue other)
    {
        return this;
    }

    /**
     * Returns the quotient of the given LongValue and this LongValue.
     */
    public LongValue divideOf(LongValue other)
    {
        return this;
    }

    /**
     * Returns the remainder of this LongValue divided by the given LongValue.
     */
    public LongValue remainder(LongValue other)
    {
        return this;
    }

    /**
     * Returns the remainder of the given LongValue divided by this LongValue.
     */
    public LongValue remainderOf(LongValue other)
    {
        return this;
    }

    /**
     * Returns this LongValue, shifted left by the given IntegerValue.
     */
    public LongValue shiftLeft(IntegerValue other)
    {
        return this;
    }

    /**
     * Returns this LongValue, shifted right by the given IntegerValue.
     */
    public LongValue shiftRight(IntegerValue other)
    {
        return this;
    }

    /**
     * Returns this unsigned LongValue, shifted left by the given
     * IntegerValue.
     */
    public LongValue unsignedShiftRight(IntegerValue other)
    {
        return this;
    }

    /**
     * Returns the logical <i>and</i> of this LongValue and the given
     * LongValue.
     */
    public LongValue and(LongValue other)
    {
        return this;
    }

    /**
     * Returns the logical <i>or</i> of this LongValue and the given
     * LongValue.
     */
    public LongValue or(LongValue other)
    {
        return this;
    }

    /**
     * Returns the logical <i>xor</i> of this LongValue and the given
     * LongValue.
     */
    public LongValue xor(LongValue other)
    {
        return this;
    }

    /**
     * Returns an IntegerValue with value -1, 0, or 1, if this LongValue is
     * less than, equal to, or greater than the given LongValue, respectively.
     */
    public IntegerValue compare(LongValue other)
    {
        return IntegerValueFactory.create();
    }


    // Derived binary methods.

    /**
     * Returns an IntegerValue with value 1, 0, or -1, if this LongValue is
     * less than, equal to, or greater than the given LongValue, respectively.
     */
    public final IntegerValue compareReverse(LongValue other)
    {
        return compare(other).negate();
    }


    // Basic unary methods.

    /**
     * Returns the negated value of this LongValue.
     */
    public LongValue negate()
    {
        return this;
    }

    /**
     * Converts this LongValue to an IntegerValue.
     */
    public IntegerValue convertToInteger()
    {
        return IntegerValueFactory.create();
    }

    /**
     * Converts this LongValue to a FloatValue.
     */
    public FloatValue convertToFloat()
    {
        return FloatValueFactory.create();
    }

    /**
     * Converts this LongValue to a DoubleValue.
     */
    public DoubleValue convertToDouble()
    {
        return DoubleValueFactory.create();
    }


    // Similar binary methods, but this time with more specific arguments.

    /**
     * Returns the generalization of this LongValue and the given other
     * SpecificLongValue.
     */
    public LongValue generalize(SpecificLongValue other)
    {
        return this;
    }


    /**
     * Returns the sum of this LongValue and the given SpecificLongValue.
     */
    public LongValue add(SpecificLongValue other)
    {
        return this;
    }

    /**
     * Returns the difference of this LongValue and the given SpecificLongValue.
     */
    public LongValue subtract(SpecificLongValue other)
    {
        return this;
    }

    /**
     * Returns the difference of the given SpecificLongValue and this LongValue.
     */
    public LongValue subtractFrom(SpecificLongValue other)
    {
        return this;
    }

    /**
     * Returns the product of this LongValue and the given SpecificLongValue.
     */
    public LongValue multiply(SpecificLongValue other)
    {
        return this;
    }

    /**
     * Returns the quotient of this LongValue and the given SpecificLongValue.
     */
    public LongValue divide(SpecificLongValue other)
    {
        return this;
    }

    /**
     * Returns the quotient of the given SpecificLongValue and this LongValue.
     */
    public LongValue divideOf(SpecificLongValue other)
    {
        return this;
    }

    /**
     * Returns the remainder of this LongValue divided by the given
     * SpecificLongValue.
     */
    public LongValue remainder(SpecificLongValue other)
    {
        return this;
    }

    /**
     * Returns the remainder of the given SpecificLongValue and this
     * LongValue.
     */
    public LongValue remainderOf(SpecificLongValue other)
    {
        return this;
    }

    /**
     * Returns the logical <i>and</i> of this LongValue and the given
     * SpecificLongValue.
     */
    public LongValue and(SpecificLongValue other)
    {
        return this;
    }

    /**
     * Returns the logical <i>or</i> of this LongValue and the given
     * SpecificLongValue.
     */
    public LongValue or(SpecificLongValue other)
    {
        return this;
    }

    /**
     * Returns the logical <i>xor</i> of this LongValue and the given
     * SpecificLongValue.
     */
    public LongValue xor(SpecificLongValue other)
    {
        return this;
    }

    /**
     * Returns an IntegerValue with value -1, 0, or 1, if this LongValue is
     * less than, equal to, or greater than the given SpecificLongValue,
     * respectively.
     */
    public IntegerValue compare(SpecificLongValue other)
    {
        return IntegerValueFactory.create();
    }


    // Derived binary methods.

    /**
     * Returns an IntegerValue with value 1, 0, or -1, if this LongValue is
     * less than, equal to, or greater than the given SpecificLongValue,
     * respectively.
     */
    public final IntegerValue compareReverse(SpecificLongValue other)
    {
        return compare(other).negate();
    }


    // Implementations for Value.

    public final LongValue longValue()
    {
        return this;
    }

    public final Value generalize(Value other)
    {
        return this.generalize(other.longValue());
    }

    public final int computationalType()
    {
        return TYPE_LONG;
    }


    // Implementations for Object.

    public boolean equals(Object object)
    {
        return object != null &&
               this.getClass() == object.getClass();
    }


    public int hashCode()
    {
        return this.getClass().hashCode();
    }


    public String toString()
    {
        return "l";
    }
}
