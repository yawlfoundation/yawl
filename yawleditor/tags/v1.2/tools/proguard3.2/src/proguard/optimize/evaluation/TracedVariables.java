/* $Id: TracedVariables.java,v 1.7 2004/11/20 15:06:55 eric Exp $
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
 * This Variables class saves a given store Value along with each Value it
 * stores, and at the same time generalizes a given trace Value with the store
 * Value of each Value it loads. The store Value and the trace Value can be set;
 * the generalized trace Value can be retrieved. The object is to store
 * additional information along with the actual variable values, for instance
 * to keep track of their origins.
 * <p>
 * In addition, a boolean initialization flag can be reset and retrieved,
 * indicating whether store operations on a variable may have initialized the
 * variable.
 *
 * @author Eric Lafortune
 */
class TracedVariables extends Variables
{
    private Variables traceVariables;
    private Value     storeValue;
    private Value     traceValue;
    private boolean   initialization;


    public TracedVariables(int size)
    {
        super(size);

        traceVariables = new Variables(size);
    }


    public TracedVariables(TracedVariables tracedVariables)
    {
        super(tracedVariables);

        traceVariables = new Variables(tracedVariables.traceVariables);
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
     * Resets the initialization flag.
     */
    public void resetInitialization()
    {
        initialization = false;
    }

    public boolean wasInitialization()
    {
        return initialization;
    }


    // Implementations for Variables.

    public void reset(int size)
    {
        super.reset(size);

        traceVariables.reset(size);
    }

    public void initialize(TracedVariables other)
    {
        super.initialize(other);

        traceVariables.initialize(other.traceVariables);
    }

    public boolean generalize(TracedVariables other)
    {
        return
            super.generalize(other) |
            traceVariables.generalize(other.traceVariables);
    }

    public void store(int index, Value value)
    {
        // Is this store operation an initialization of the variable?
        Value previousValue = super.load(index);
        initialization =
            initialization        ||
            previousValue == null ||
            previousValue.computationalType() != value.computationalType();

        // Store the value itself in the variable.
        super.store(index, value);

        // Store the store value in its trace variable.
        traceVariables.store(index, storeValue);
    }

    public Value load(int index)
    {
        // Load and accumulate the store value of the variable.
        if (traceValue != null)
        {
            traceValue = traceValue.generalize(traceVariables.load(index));
        }

        // Return the value itself.
        return super.load(index);
    }


    // Implementations for Object.

    public boolean equals(Object object)
    {
        if (this.getClass() != object.getClass())
        {
            return false;
        }

        TracedVariables other = (TracedVariables)object;

        return super.equals(object) && this.traceVariables.equals(other.traceVariables);
    }


    public int hashCode()
    {
        return super.hashCode() ^ traceVariables.hashCode();
    }


    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        for (int index = 0; index < this.size(); index++)
        {
            Value value       = this.values[index];
            Value tracedValue = traceVariables.values[index];
            buffer = buffer.append('[')
                           .append(tracedValue == null ? "empty" : tracedValue.toString())
                           .append('>')
                           .append(value       == null ? "empty" : value.toString())
                           .append(']');
        }

        return buffer.toString();
    }
}
