/* $Id: SpecificIntegerValue.java,v 1.3 2004/08/15 12:39:30 eric Exp $
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
 * This IntegerValue represents a specific integer value.
 *
 * @author Eric Lafortune
 */
class SpecificIntegerValue extends IntegerValue
{
    private int value;


    public SpecificIntegerValue(int value)
    {
        this.value = value;
    }


    // Implementations for IntegerValue.

    public int value()
    {
        return value;
    }


    // Implementations of binary methods of IntegerValue.

    // Perhaps the other value arguments are more specific than apparent
    // in these methods, so delegate to them.

    public IntegerValue generalize(IntegerValue other)
    {
        return other.generalize(this);
    }

    public IntegerValue add(IntegerValue other)
    {
        return other.add(this);
    }

    public IntegerValue subtract(IntegerValue other)
    {
        return other.subtractFrom(this);
    }

    public IntegerValue subtractFrom(IntegerValue other)
    {
        return other.subtract(this);
    }

    public IntegerValue multiply(IntegerValue other)
    {
        return other.multiply(this);
    }

    public IntegerValue divide(IntegerValue other)
    throws ArithmeticException
    {
        return other.divideOf(this);
    }

    public IntegerValue divideOf(IntegerValue other)
    throws ArithmeticException
    {
        return other.divide(this);
    }

    public IntegerValue remainder(IntegerValue other)
    throws ArithmeticException
    {
        return other.remainderOf(this);
    }

    public IntegerValue remainderOf(IntegerValue other)
    throws ArithmeticException
    {
        return other.remainder(this);
    }

    public IntegerValue shiftLeft(IntegerValue other)
    {
        return other.shiftLeftOf(this);
    }

    public IntegerValue shiftLeftOf(IntegerValue other)
    {
        return other.shiftLeft(this);
    }

    public IntegerValue shiftRight(IntegerValue other)
    {
        return other.shiftRightOf(this);
    }

    public IntegerValue shiftRightOf(IntegerValue other)
    {
        return other.shiftRight(this);
    }

    public IntegerValue unsignedShiftRight(IntegerValue other)
    {
        return other.unsignedShiftRightOf(this);
    }

    public IntegerValue unsignedShiftRightOf(IntegerValue other)
    {
        return other.unsignedShiftRight(this);
    }

    public LongValue shiftLeftOf(LongValue other)
    {
        return other.shiftLeft(this);
    }

    public LongValue shiftRightOf(LongValue other)
    {
        return other.shiftRight(this);
    }

    public LongValue unsignedShiftRightOf(LongValue other)
    {
        return other.unsignedShiftRight(this);
    }

    public IntegerValue and(IntegerValue other)
    {
        return other.and(this);
    }

    public IntegerValue or(IntegerValue other)
    {
        return other.or(this);
    }

    public IntegerValue xor(IntegerValue other)
    {
        return other.xor(this);
    }

    public int equal(IntegerValue other)
    {
        return other.equal(this);
    }

    public int lessThan(IntegerValue other)
    {
        return other.greaterThanOrEqual(this);
    }

    public int lessThanOrEqual(IntegerValue other)
    {
        return other.greaterThan(this);
    }


    // Implementations of unary methods of IntegerValue.

    public IntegerValue negate()
    {
        return IntegerValueFactory.create(-value);
    }

    public IntegerValue convertToByte()
    {
        int byteValue = (byte)value;

        return byteValue == value ?
            this :
            IntegerValueFactory.create(byteValue);
    }

    public IntegerValue convertToCharacter()
    {
        int charValue = (char)value;

        return charValue == value ?
            this :
            IntegerValueFactory.create(charValue);
    }

    public IntegerValue convertToShort()
    {
        int shortValue = (short)value;

        return shortValue == value ?
            this :
            IntegerValueFactory.create(shortValue);
    }

    public IntegerValue convertToInteger()
    {
        return this;
    }

    public LongValue convertToLong()
    {
        return LongValueFactory.create((long)value);
    }

    public FloatValue convertToFloat()
    {
        return FloatValueFactory.create((float)value);
    }

    public DoubleValue convertToDouble()
    {
        return DoubleValueFactory.create((double)value);
    }


    // Implementations of binary IntegerValue methods with SpecificIntegerValue
    // arguments.

    public IntegerValue generalize(SpecificIntegerValue other)
    {
        return this.value == other.value ? this : IntegerValueFactory.create();
    }

    public IntegerValue add(SpecificIntegerValue other)
    {
        return IntegerValueFactory.create(this.value + other.value);
    }

    public IntegerValue subtract(SpecificIntegerValue other)
    {
        return IntegerValueFactory.create(this.value - other.value);
    }

    public IntegerValue subtractFrom(SpecificIntegerValue other)
    {
        return IntegerValueFactory.create(other.value - this.value);
    }

    public IntegerValue multiply(SpecificIntegerValue other)
    {
        return IntegerValueFactory.create(this.value * other.value);
    }

    public IntegerValue divide(SpecificIntegerValue other)
    throws ArithmeticException
    {
        return IntegerValueFactory.create(this.value / other.value);
    }

    public IntegerValue divideOf(SpecificIntegerValue other)
    throws ArithmeticException
    {
        return IntegerValueFactory.create(other.value / this.value);
    }

    public IntegerValue remainder(SpecificIntegerValue other)
    throws ArithmeticException
    {
        return IntegerValueFactory.create(this.value % other.value);
    }

    public IntegerValue remainderOf(SpecificIntegerValue other)
    throws ArithmeticException
    {
        return IntegerValueFactory.create(other.value % this.value);
    }

    public IntegerValue shiftLeft(SpecificIntegerValue other)
    {
        return IntegerValueFactory.create(this.value << other.value);
    }

    public IntegerValue shiftLeftOf(SpecificIntegerValue other)
    {
        return IntegerValueFactory.create(other.value << this.value);
    }

    public IntegerValue shiftRight(SpecificIntegerValue other)
    {
        return IntegerValueFactory.create(this.value >> other.value);
    }

    public IntegerValue shiftRightOf(SpecificIntegerValue other)
    {
        return IntegerValueFactory.create(other.value >> this.value);
    }

    public IntegerValue unsignedShiftRight(SpecificIntegerValue other)
    {
        return IntegerValueFactory.create(this.value >>> other.value);
    }

    public IntegerValue unsignedShiftRightOf(SpecificIntegerValue other)
    {
        return IntegerValueFactory.create(other.value >>> this.value);
    }

    public LongValue shiftLeftOf(SpecificLongValue other)
    {
        return LongValueFactory.create(other.value() << this.value);
    }

    public LongValue shiftRightOf(SpecificLongValue other)
    {
        return LongValueFactory.create(other.value() >> this.value);
    }

    public LongValue unsignedShiftRightOf(SpecificLongValue other)
    {
        return LongValueFactory.create(other.value() >>> this.value);
    }

    public IntegerValue and(SpecificIntegerValue other)
    {
        return IntegerValueFactory.create(this.value & other.value);
    }

    public IntegerValue or(SpecificIntegerValue other)
    {
        return IntegerValueFactory.create(this.value | other.value);
    }

    public IntegerValue xor(SpecificIntegerValue other)
    {
        return IntegerValueFactory.create(this.value ^ other.value);
    }

    public int equal(SpecificIntegerValue other)
    {
        return this.value == other.value ? ALWAYS : NEVER;
    }

    public int lessThan(SpecificIntegerValue other)
    {
        return this.value <  other.value ? ALWAYS : NEVER;
    }

    public int lessThanOrEqual(SpecificIntegerValue other)
    {
        return this.value <= other.value ? ALWAYS : NEVER;
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
               this.value      == ((SpecificIntegerValue)object).value;
    }


    public int hashCode()
    {
        return this.getClass().hashCode() ^ value;
    }


    public String toString()
    {
        return "i:"+value;
    }
}
