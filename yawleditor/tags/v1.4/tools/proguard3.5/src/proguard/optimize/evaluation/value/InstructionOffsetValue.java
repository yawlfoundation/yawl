/* $Id: InstructionOffsetValue.java,v 1.7.2.1 2006/01/16 22:57:56 eric Exp $
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
 * This class represents a partially evaluated instruction offset. It can
 * contain 0 or more specific instruction offsets.
 *
 * @author Eric Lafortune
 */
public class InstructionOffsetValue extends Category1Value
{
    private int[] values;


    public InstructionOffsetValue()
    {
    }


    public InstructionOffsetValue(int value)
    {
        this.values = new int[] { value };
    }


    public InstructionOffsetValue(int[] values)
    {
        this.values = values;
    }


    public int instructionOffsetCount()
    {
        return values == null ? 0 : values.length;
    }


    public int instructionOffset(int index)
    {
        return values[index];
    }


    /**
     * Returns whether the given value is present in this list of instruction
     * offsets.
     */
    public boolean contains(int value)
    {
        if (values != null)
        {
            for (int index = 0; index < values.length; index++)
            {
                if (values[index] == value)
                {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * Returns the generalization of this InstructionOffsetValue and the given
     * other InstructionOffsetValue. The values of the other InstructionOffsetValue
     * are guaranteed to remain at the end of the list, in the same order.
     */
    public final Value generalize(InstructionOffsetValue other)
    {
        // If the values array of either is null, return the other one.
        if (this.values == null)
        {
            return other;
        }

        if (other.values == null)
        {
            return this;
        }

        // Compute the length of the union of the arrays.
        int newLength = this.values.length;
        for (int index = 0; index < other.values.length; index++)
        {
            if (!this.contains(other.values[index]))
            {
                newLength++;
            }
        }

        // If the length of the union array is equal to the length of the values
        // array of either, return it.
        if (newLength == other.values.length)
        {
            return other;
        }

        // The ordering of the this array may not be right, so we can't just
        // use it.
        //if (newLength == this.values.length)
        //{
        //    return this;
        //}

        // Create the union array.
        int[] newValues = new int[newLength];

        int newIndex = 0;

        // Copy the values that are different from the other array.
        for (int index = 0; index < this.values.length; index++)
        {
            if (!other.contains(this.values[index]))
            {
                newValues[newIndex++] = this.values[index];
            }
        }

        // Copy the values from the other array.
        for (int index = 0; index < other.values.length; index++)
        {
            newValues[newIndex++] = other.values[index];
        }

        return InstructionOffsetValueFactory.create(newValues);
    }


    // Implementations for Value.

    public final InstructionOffsetValue instructionOffsetValue()
    {
        return this;
    }

    public final Value generalize(Value other)
    {
        return this.generalize(other.instructionOffsetValue());
    }

    public final int computationalType()
    {
        return TYPE_INSTRUCTION_OFFSET;
    }


    // Implementations for Object.

    public boolean equals(Object object)
    {
        if (object == null ||
            this.getClass() != object.getClass())
        {
            return false;
        }

        InstructionOffsetValue other = (InstructionOffsetValue)object;
        if (this.values == other.values)
        {
            return true;
        }

        if (this.values  == null ||
            other.values == null ||
            this.values.length != other.values.length)
        {
            return false;
        }

        for (int index = 0; index < other.values.length; index++)
        {
            if (!this.contains(other.values[index]))
            {
                return false;
            }
        }

        return true;
    }


    public int hashCode()
    {
        int hashCode = this.getClass().hashCode();

        if (values != null)
        {
            for (int index = 0; index < values.length; index++)
            {
                hashCode ^= values[index];
            }
        }

        return hashCode;
    }


    public String toString()
    {
        StringBuffer buffer = new StringBuffer("o:");

        if (values != null)
        {
            for (int index = 0; index < values.length; index++)
            {
                if (index > 0)
                {
                    buffer.append(',');
                }
                buffer.append(values[index]);
            }
        }

        return buffer.toString();
    }
}
