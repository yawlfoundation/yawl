/* $Id: SpecificLongValue.java,v 1.3.2.1 2006/01/16 22:57:56 eric Exp $
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
 * This LongValue represents a specific long value.
 *
 * @author Eric Lafortune
 */
class SpecificLongValue extends LongValue
{
    private long value;


    /**
     * Creates a new specific long value.
     */
    public SpecificLongValue(long value)
    {
        this.value = value;
    }


    // Implementations for LongValue.

    public long value()
    {
        return value;
    }


    // Implementations of binary methods of LongValue.

    // Perhaps the other value arguments are more specific than apparent
    // in these methods, so delegate to them.

    public LongValue generalize(LongValue other)
    {
        return other.generalize(this);
    }

    public LongValue add(LongValue other)
    {
        return other.add(this);
    }

    public LongValue subtract(LongValue other)
    {
        return other.subtractFrom(this);
    }

    public LongValue subtractFrom(LongValue other)
    {
        return other.subtract(this);
    }

    public LongValue multiply(LongValue other)
    {
        return other.multiply(this);
    }

    public LongValue divide(LongValue other)
    {
        return other.divideOf(this);
    }

    public LongValue divideOf(LongValue other)
    {
        return other.divide(this);
    }

    public LongValue remainder(LongValue other)
    {
        return other.remainderOf(this);
    }

    public LongValue remainderOf(LongValue other)
    {
        return other.remainder(this);
    }

    public LongValue shiftLeft(IntegerValue other)
    {
        return other.shiftLeftOf(this);
    }

    public LongValue shiftRight(IntegerValue other)
    {
        return other.shiftRightOf(this);
    }

    public LongValue unsignedShiftRight(IntegerValue other)
    {
        return other.unsignedShiftRightOf(this);
    }

    public LongValue and(LongValue other)
    {
        return other.and(this);
    }

    public LongValue or(LongValue other)
    {
        return other.or(this);
    }

    public LongValue xor(LongValue other)
    {
        return other.xor(this);
    }

    public IntegerValue compare(LongValue other)
    {
        return other.compareReverse(this);
    }


    // Implementations of unary methods of LongValue.

    public LongValue negate()
    {
        return LongValueFactory.create(-value);
    }

    public IntegerValue convertToInteger()
    {
        return IntegerValueFactory.create((int)value);
    }

    public FloatValue convertToFloat()
    {
        return FloatValueFactory.create((float)value);
    }

    public DoubleValue convertToDouble()
    {
        return DoubleValueFactory.create((double)value);
    }


    // Implementations of binary LongValue methods with SpecificLongValue
    // arguments.

    public LongValue generalize(SpecificLongValue other)
    {
        return this.value == other.value ? this : LongValueFactory.create();
    }

    public LongValue add(SpecificLongValue other)
    {
        return LongValueFactory.create(this.value + other.value);
    }

    public LongValue subtract(SpecificLongValue other)
    {
        return LongValueFactory.create(this.value - other.value);
    }

    public LongValue subtractFrom(SpecificLongValue other)
    {
        return LongValueFactory.create(other.value - this.value);
    }

    public LongValue multiply(SpecificLongValue other)
    {
        return LongValueFactory.create(this.value * other.value);
    }

    public LongValue divide(SpecificLongValue other)
    {
        return LongValueFactory.create(this.value / other.value);
    }

    public LongValue divideOf(SpecificLongValue other)
    {
        return LongValueFactory.create(other.value / this.value);
    }

    public LongValue remainder(SpecificLongValue other)
    {
        return LongValueFactory.create(this.value % other.value);
    }

    public LongValue remainderOf(SpecificLongValue other)
    {
        return LongValueFactory.create(other.value % this.value);
    }

    public LongValue and(SpecificLongValue other)
    {
        return LongValueFactory.create(this.value & other.value);
    }

    public LongValue or(SpecificLongValue other)
    {
        return LongValueFactory.create(this.value | other.value);
    }

    public LongValue xor(SpecificLongValue other)
    {
        return LongValueFactory.create(this.value ^ other.value);
    }

    public IntegerValue compare(SpecificLongValue other)
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
               this.value      == ((SpecificLongValue)object).value;
    }


    public int hashCode()
    {
        return this.getClass().hashCode() ^ (int)value;
    }


    public String toString()
    {
        return "l:"+value;
    }
}
