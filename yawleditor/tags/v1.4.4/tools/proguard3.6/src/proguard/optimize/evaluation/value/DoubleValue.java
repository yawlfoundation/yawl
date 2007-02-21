/* $Id: DoubleValue.java,v 1.3.2.1 2006/01/16 22:57:56 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2006 Eric Lafortune (eric@graphics.cornell.edu)
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
 * This class represents a partially evaluated double value.
 *
 * @author Eric Lafortune
 */
public class DoubleValue extends Category2Value
{
    /**
     * Returns the specific double value, if applicable.
     */
    public double value()
    {
        return 0.0;
    }


    // Basic binary methods.

    /**
     * Returns the generalization of this DoubleValue and the given other
     * DoubleValue.
     */
    public DoubleValue generalize(DoubleValue other)
    {
        return this;
    }


    /**
     * Returns the sum of this DoubleValue and the given DoubleValue.
     */
    public DoubleValue add(DoubleValue other)
    {
        return this;
    }

    /**
     * Returns the difference of this DoubleValue and the given DoubleValue.
     */
    public DoubleValue subtract(DoubleValue other)
    {
        return this;
    }

    /**
     * Returns the difference of the given DoubleValue and this DoubleValue.
     */
    public DoubleValue subtractFrom(DoubleValue other)
    {
        return this;
    }

    /**
     * Returns the product of this DoubleValue and the given DoubleValue.
     */
    public DoubleValue multiply(DoubleValue other)
    {
        return this;
    }

    /**
     * Returns the quotient of this DoubleValue and the given DoubleValue.
     */
    public DoubleValue divide(DoubleValue other)
    {
        return this;
    }

    /**
     * Returns the quotient of the given DoubleValue and this DoubleValue.
     */
    public DoubleValue divideOf(DoubleValue other)
    {
        return this;
    }

    /**
     * Returns the remainder of this DoubleValue divided by the given DoubleValue.
     */
    public DoubleValue remainder(DoubleValue other)
    {
        return this;
    }

    /**
     * Returns the remainder of the given DoubleValue divided by this DoubleValue.
     */
    public DoubleValue remainderOf(DoubleValue other)
    {
        return this;
    }

    /**
     * Returns an IntegerValue with value -1, 0, or 1, if this DoubleValue is
     * less than, equal to, or greater than the given DoubleValue, respectively.
     */
    public IntegerValue compare(DoubleValue other)
    {
        return IntegerValueFactory.create();
    }


    // Derived binary methods.

    /**
     * Returns an IntegerValue with value 1, 0, or -1, if this DoubleValue is
     * less than, equal to, or greater than the given DoubleValue, respectively.
     */
    public final IntegerValue compareReverse(DoubleValue other)
    {
        return compare(other).negate();
    }


    // Basic unary methods.

    /**
     * Returns the negated value of this DoubleValue.
     */
    public DoubleValue negate()
    {
        return this;
    }

    /**
     * Converts this DoubleValue to an IntegerValue.
     */
    public IntegerValue convertToInteger()
    {
        return IntegerValueFactory.create();
    }

    /**
     * Converts this DoubleValue to a LongValue.
     */
    public LongValue convertToLong()
    {
        return LongValueFactory.create();
    }

    /**
     * Converts this DoubleValue to a FloatValue.
     */
    public FloatValue convertToFloat()
    {
        return FloatValueFactory.create();
    }


    // Similar binary methods, but this time with more specific arguments.

    /**
     * Returns the generalization of this DoubleValue and the given other
     * SpecificDoubleValue.
     */
    public DoubleValue generalize(SpecificDoubleValue other)
    {
        return this;
    }


    /**
     * Returns the sum of this DoubleValue and the given SpecificDoubleValue.
     */
    public DoubleValue add(SpecificDoubleValue other)
    {
        return this;
    }

    /**
     * Returns the difference of this DoubleValue and the given SpecificDoubleValue.
     */
    public DoubleValue subtract(SpecificDoubleValue other)
    {
        return this;
    }

    /**
     * Returns the difference of the given SpecificDoubleValue and this DoubleValue.
     */
    public DoubleValue subtractFrom(SpecificDoubleValue other)
    {
        return this;
    }

    /**
     * Returns the product of this DoubleValue and the given SpecificDoubleValue.
     */
    public DoubleValue multiply(SpecificDoubleValue other)
    {
        return this;
    }

    /**
     * Returns the quotient of this DoubleValue and the given SpecificDoubleValue.
     */
    public DoubleValue divide(SpecificDoubleValue other)
    {
        return this;
    }

    /**
     * Returns the quotient of the given SpecificDoubleValue and this
     * DoubleValue.
     */
    public DoubleValue divideOf(SpecificDoubleValue other)
    {
        return this;
    }

    /**
     * Returns the remainder of this DoubleValue divided by the given
     * SpecificDoubleValue.
     */
    public DoubleValue remainder(SpecificDoubleValue other)
    {
        return this;
    }

    /**
     * Returns the remainder of the given SpecificDoubleValue and this
     * DoubleValue.
     */
    public DoubleValue remainderOf(SpecificDoubleValue other)
    {
        return this;
    }

    /**
     * Returns an IntegerValue with value -1, 0, or  1, if this DoubleValue is
     * less than, equal to, or greater than the given SpecificDoubleValue,
     * respectively.
     */
    public IntegerValue compare(SpecificDoubleValue other)
    {
        return IntegerValueFactory.create();
    }


    // Derived binary methods.

    /**
     * Returns an IntegerValue with value 1, 0, or -1, if this DoubleValue is
     * less than, equal to, or greater than the given SpecificDoubleValue,
     * respectively.
     */
    public final IntegerValue compareReverse(SpecificDoubleValue other)
    {
        return compare(other).negate();
    }


    // Implementations for Value.

    public final DoubleValue doubleValue()
    {
        return this;
    }

    public final Value generalize(Value other)
    {
        return this.generalize(other.doubleValue());
    }

    public final int computationalType()
    {
        return TYPE_DOUBLE;
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
        return "d";
    }
}
