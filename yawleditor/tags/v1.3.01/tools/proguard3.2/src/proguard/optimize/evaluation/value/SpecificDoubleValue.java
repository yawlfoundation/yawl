/* $Id: SpecificDoubleValue.java,v 1.2 2004/08/15 12:39:30 eric Exp $
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
 * You should have received a copy of the GNU General Public License adouble
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.optimize.evaluation.value;

/**
 * This DoubleValue represents a specific double value.
 *
 * @author Eric Lafortune
 */
class SpecificDoubleValue extends DoubleValue
{
    private double value;


    /**
     * Creates a new specific double value.
     */
    public SpecificDoubleValue(double value)
    {
        this.value = value;
    }


    // Implementations for DoubleValue.

    public double value()
    {
        return value;
    }


    // Implementations of binary methods of DoubleValue.

    // Perhaps the other value arguments are more specific than apparent
    // in these methods, so delegate to them.

    public DoubleValue generalize(DoubleValue other)
    {
        return other.generalize(this);
    }

    public DoubleValue add(DoubleValue other)
    {
        return other.add(this);
    }

    public DoubleValue subtract(DoubleValue other)
    {
        return other.subtractFrom(this);
    }

    public DoubleValue subtractFrom(DoubleValue other)
    {
        return other.subtract(this);
    }

    public DoubleValue multiply(DoubleValue other)
    {
        return other.multiply(this);
    }

    public DoubleValue divide(DoubleValue other)
    {
        return other.divideOf(this);
    }

    public DoubleValue divideOf(DoubleValue other)
    {
        return other.divide(this);
    }

    public DoubleValue remainder(DoubleValue other)
    {
        return other.remainderOf(this);
    }

    public DoubleValue remainderOf(DoubleValue other)
    {
        return other.remainder(this);
    }

    public IntegerValue compare(DoubleValue other)
    {
        return other.compareReverse(this);
    }


    // Implementations of unary methods of DoubleValue.

    public DoubleValue negate()
    {
        return DoubleValueFactory.create(-value);
    }

    public IntegerValue convertToInteger()
    {
        return IntegerValueFactory.create((int)value);
    }

    public LongValue convertToLong()
    {
        return LongValueFactory.create((long)value);
    }

    public FloatValue convertToFloat()
    {
        return FloatValueFactory.create((float)value);
    }


    // Implementations of binary DoubleValue methods with SpecificDoubleValue
    // arguments.

    public DoubleValue generalize(SpecificDoubleValue other)
    {
        return this.value == other.value ? this : DoubleValueFactory.create();
    }

    public DoubleValue add(SpecificDoubleValue other)
    {
        return DoubleValueFactory.create(this.value + other.value);
    }

    public DoubleValue subtract(SpecificDoubleValue other)
    {
        return DoubleValueFactory.create(this.value - other.value);
    }

    public DoubleValue subtractFrom(SpecificDoubleValue other)
    {
        return DoubleValueFactory.create(other.value - this.value);
    }

    public DoubleValue multiply(SpecificDoubleValue other)
    {
        return DoubleValueFactory.create(this.value * other.value);
    }

    public DoubleValue divide(SpecificDoubleValue other)
    {
        return DoubleValueFactory.create(this.value / other.value);
    }

    public DoubleValue divideOf(SpecificDoubleValue other)
    {
        return DoubleValueFactory.create(other.value / this.value);
    }

    public DoubleValue remainder(SpecificDoubleValue other)
    {
        return DoubleValueFactory.create(this.value % other.value);
    }

    public DoubleValue remainderOf(SpecificDoubleValue other)
    {
        return DoubleValueFactory.create(other.value % this.value);
    }

    public IntegerValue compare(SpecificDoubleValue other)
    {
        return this.value <  other.value ? IntegerValueFactory.create(-1) :
               this.value == other.value ? IntegerValueFactory.create(0) :
                                           IntegerValueFactory.create(1);
    }


    // Implementations for Value.

    public boolean isSpecific()
    {
        return true;
    }


    // Implementations for Object.

    public boolean equals(Object object)
    {
        return object          != null              &&
               this.getClass() == object.getClass() &&
               this.value      == ((SpecificDoubleValue)object).value;
    }


    public int hashCode()
    {
        return this.getClass().hashCode() ^ (int)Double.doubleToLongBits(value);
    }


    public String toString()
    {
        return "d:"+value;
    }
}
