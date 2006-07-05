/* $Id: TracedStack.java,v 1.7 2004/11/20 15:06:55 eric Exp $
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
package proguard.optimize.evaluation;

import proguard.optimize.evaluation.value.*;

/**
 * This Stack saves a given store Value along with each Value it stores, and
 * at the same time generalizes a given trace Value with the store Value of
 * each Value it loads. The store Value and the trace Value can be set; the
 * generalized trace Value can be retrieved. The object is to store additional
 * information along with the actual stack values, for instance to keep track
 * of their origins.
 *
 * @author Eric Lafortune
 */
class TracedStack extends Stack
{
    private Stack traceStack;
    private Value storeValue;
    private Value traceValue;


    public TracedStack(int maxSize)
    {
        super(maxSize);

        traceStack = new Stack(maxSize);
    }


    public TracedStack(TracedStack tracedStack)
    {
        super(tracedStack);

        traceStack = new Stack(tracedStack.traceStack);
    }


    /**
     * Sets the Value that will be stored along with all store instructions.
     */
    public void setStoreValue(Value storeValue)
    {
        this.storeValue = storeValue;
    }


    /**
     * Sets the initial Value with which all values stored along with load
     * instructions will be generalized.
     */
    public void setTraceValue(Value traceValue)
    {
        this.traceValue = traceValue;
    }

    public Value getTraceValue()
    {
        return traceValue;
    }


    /**
     * Gets the specified trace Value from the stack, without disturbing it.
     * @param index the index of the stack element, counting from the bottom
     *              of the stack.
     */
    public Value getBottomTraceValue(int index)
    {
        return traceStack.getBottom(index);
    }


    /**
     * Gets the specified trace Value from the stack, without disturbing it.
     * @param index the index of the stack element, counting from the top
     *              of the stack.
     */
    public Value getTopTraceValue(int index)
    {
        return traceStack.getTop(index);
    }


    // Implementations for Stack.

    public void reset(int size)
    {
        super.reset(size);

        traceStack.reset(size);
    }

    public void copy(TracedStack other)
    {
        super.copy(other);

        traceStack.copy(other.traceStack);
    }

    public boolean generalize(TracedStack other)
    {
        return
            super.generalize(other) |
            traceStack.generalize(other.traceStack);
    }

    public void clear()
    {
        super.clear();

        traceStack.clear();
    }

    public void push(Value value)
    {
        super.push(value);

        tracePush();

        // Account for the extra space required by Category 2 values.
        if (value.isCategory2())
        {
            tracePush();
        }
    }

    public Value pop()
    {
        Value value = super.pop();

        tracePop();

        // Account for the extra space required by Category 2 values.
        if (value.isCategory2())
        {
            tracePop();
        }

        return value;
    }

    public void pop1()
    {
        super.pop1();

        tracePop();
    }

    public void pop2()
    {
        super.pop2();

        tracePop();
        tracePop();
    }

    public void dup()
    {
        super.dup();

        // For now, we're letting all stack values that are somehow involved
        // depend on this instruction.
        Value tracePopValue = tracePop();

        tracePush();
        traceStack.push(tracePopValue);
    }

    public void dup_x1()
    {
        super.dup_x1();

        // Let the duplicated value depend on this instruction.
        Value tracePopValue  = tracePop();
        Value traceSkipValue = traceStack.pop();

        tracePush();
        traceStack.push(traceSkipValue);
        traceStack.push(tracePopValue);
    }

    public void dup_x2()
    {
        super.dup_x2();

        // Let the duplicated value depend on this instruction.
        Value tracePopValue   = tracePop();
        Value traceSkipValue1 = traceStack.pop();
        Value traceSkipValue2 = traceStack.pop();

        tracePush();
        traceStack.push(traceSkipValue2);
        traceStack.push(traceSkipValue1);
        traceStack.push(tracePopValue);
    }

    public void dup2()
    {
        super.dup2();

        // Let the duplicated value depend on this instruction.
        Value tracePopValue1  = tracePop();
        Value tracePopValue2  = tracePop();

        tracePush();
        tracePush();
        traceStack.push(tracePopValue2);
        traceStack.push(tracePopValue1);
    }

    public void dup2_x1()
    {
        super.dup2_x1();

        // Let the duplicated value depend on this instruction.
        Value tracePopValue1 = tracePop();
        Value tracePopValue2 = tracePop();
        Value traceSkipValue = traceStack.pop();

        tracePush();
        tracePush();
        traceStack.push(traceSkipValue);
        traceStack.push(tracePopValue2);
        traceStack.push(tracePopValue1);
    }

    public void dup2_x2()
    {
        super.dup2_x2();

        // Let the duplicated value depend on this instruction.
        Value tracePopValue1  = tracePop();
        Value tracePopValue2  = tracePop();
        Value traceSkipValue1 = traceStack.pop();
        Value traceSkipValue2 = traceStack.pop();

        tracePush();
        tracePush();
        traceStack.push(traceSkipValue2);
        traceStack.push(traceSkipValue1);
        traceStack.push(tracePopValue2);
        traceStack.push(tracePopValue1);
    }

    public void swap()
    {
        super.swap();

        // Let one of the swapped values depend on this instruction.
        tracePop();
        Value traceSwapValue = traceStack.pop();

        tracePush();
        traceStack.push(traceSwapValue);
    }


    // Implementations for Object.

    public boolean equals(Object object)
    {
        if (this.getClass() != object.getClass())
        {
            return false;
        }

        TracedStack other = (TracedStack)object;

        return super.equals(object) && this.traceStack.equals(other.traceStack);
    }


    public int hashCode()
    {
        return super.hashCode() ^ traceStack.hashCode();
    }


    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        for (int index = 0; index < this.size(); index++)
        {
            Value value       = this.values[index];
            Value tracedValue = traceStack.values[index];
            buffer = buffer.append('[')
                           .append(tracedValue == null ? "empty" : tracedValue.toString())
                           .append('>')
                           .append(value       == null ? "empty" : value.toString())
                           .append(']');
        }

        return buffer.toString();
    }


    // Small utility methods.

    private void tracePush()
    {
        traceStack.push(storeValue);
    }


    private Value tracePop()
    {
        Value popTraceValue = traceStack.pop();
        if (traceValue != null)
        {
            traceValue = traceValue.generalize(popTraceValue);
        }

        return popTraceValue;
    }
}
