/* $Id: SpecificFloatValue.java,v 1.3.2.1 2006/01/16 22:57:56 eric Exp $
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
 * You should have received a copy of the GNU General Public License afloat
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.optimize.evaluation.value;

/**
 * This FloatValue represents a specific float value.
 *
 * @author Eric Lafortune
 */
class SpecificFloatValue extends FloatValue
{
    private float value;


    /**
     * Creates a new specific float value.
     */
    public SpecificFloatValue(float value)
    {
        this.value = value;
    }


    // Implementations for FloatValue.

    public float value()
    {
        return value;
    }


    // Implementations of binary methods of FloatValue.

    // Perhaps the other value arguments are more specific than apparent
    // in these methods, so delegate to them.

    public FloatValue generalize(FloatValue other)
    {
        return other.generalize(this);
    }

    public FloatValue add(FloatValue other)
    {
        return other.add(this);
    }

    public FloatValue subtract(FloatValue other)
    {
        return other.subtractFrom(this);
    }

    public FloatValue subtractFrom(FloatValue other)
    {
        return other.subtract(this);
    }

    public FloatValue multiply(FloatValue other)
    {
        return other.multiply(this);
    }

    public FloatValue divide(FloatValue other)
    {
        return other.divideOf(this);
    }

    public FloatValue divideOf(FloatValue other)
    {
        return other.divide(this);
    }

    public FloatValue remainder(FloatValue other)
    {
        return other.remainderOf(this);
    }

    public FloatValue remainderOf(FloatValue other)
    {
        return other.remainder(this);
    }

    public IntegerValue compare(FloatValue other)
    {
        return other.compareReverse(this);
    }


    // Implementations of unary methods of FloatValue.

    public FloatValue negate()
    {
        return FloatValueFactory.create(-value);
    }

    public IntegerValue convertToInteger()
    {
        return IntegerValueFactory.create((int)value);
    }

    public LongValue convertToLong()
    {
        return LongValueFactory.create((long)value);
    }

    public DoubleValue convertToDouble()
    {
        return DoubleValueFactory.create((double)value);
    }


    // Implementations of binary FloatValue methods with SpecificFloatValue
    // arguments.

    public FloatValue generalize(SpecificFloatValue other)
    {
        return this.value == other.value ? this : FloatValueFactory.create();
    }

    public FloatValue add(SpecificFloatValue other)
    {
        return FloatValueFactory.create(this.value + other.value);
    }

    public FloatValue subtract(SpecificFloatValue other)
    {
        return FloatValueFactory.create(this.value - other.value);
    }

    public FloatValue subtractFrom(SpecificFloatValue other)
    {
        return FloatValueFactory.create(other.value - this.value);
    }

    public FloatValue multiply(SpecificFloatValue other)
    {
        return FloatValueFactory.create(this.value * other.value);
    }

    public FloatValue divide(SpecificFloatValue other)
    {
        return FloatValueFactory.create(this.value / other.value);
    }

    public FloatValue divideOf(SpecificFloatValue other)
    {
        return FloatValueFactory.create(other.value / this.value);
    }

    public FloatValue remainder(SpecificFloatValue other)
    {
        return FloatValueFactory.create(this.value % other.value);
    }

    public FloatValue remainderOf(SpecificFloatValue other)
    {
        return FloatValueFactory.create(other.value % this.value);
    }

    public IntegerValue compare(SpecificFloatValue other)
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
               this.value      == ((SpecificFloatValue)object).value;
    }


    public int hashCode()
    {
        return this.getClass().hashCode() ^ Float.floatToIntBits(value);
    }


    public String toString()
    {
        return "f:"+value;
    }
}
