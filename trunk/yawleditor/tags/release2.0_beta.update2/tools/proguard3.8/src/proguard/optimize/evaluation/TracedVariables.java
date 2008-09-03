/* $Id: TracedVariables.java,v 1.10.2.2 2007/01/18 21:31:53 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2007 Eric Lafortune (eric@graphics.cornell.edu)
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
 * This Variables class saves additional information with variables, to keep track
 * of their origins.
 * <p>
 * The Variables class stores a given producer Value along with each Value it
 * stores. It then generalizes a given collected Value with the producer Value
 * of each Value it loads. The producer Value and the initial collected Value
 * can be set; the generalized collected Value can be retrieved.
 * <p>
 * In addition, an initialization index can be reset and retrieved, pointing
 * to the most recent variable that has been initialized by a store operation.
 *
 * @author Eric Lafortune
 */
class TracedVariables extends Variables
{
    private Value     producerValue;
    private Value     collectedProducerValue;
    private Variables producerVariables;
//  private Variables consumerVariables;
    private int       initializationIndex;


    public TracedVariables(int size)
    {
        super(size);

        producerVariables = new Variables(size);
    }


    public TracedVariables(TracedVariables tracedVariables)
    {
        super(tracedVariables);

        producerVariables = new Variables(tracedVariables.producerVariables);
    }


    /**
     * Sets the Value that will be stored along with all store instructions.
     */
    public void setProducerValue(Value producerValue)
    {
        this.producerValue = producerValue;
    }


    /**
     * Sets the initial Value with which all values stored along with load
     * instructions will be generalized.
     */
    public void setCollectedProducerValue(Value collectedProducerValue)
    {
        this.collectedProducerValue = collectedProducerValue;
    }

    public Value getCollectedProducerValue()
    {
        return collectedProducerValue;
    }


    /**
     * Resets the initialization index.
     */
    public void resetInitialization()
    {
        initializationIndex = -1;
    }

    public int getInitializationIndex()
    {
        return initializationIndex;
    }


    /**
     * Gets the specified trace Value from the variables, without disturbing them.
     * @param index the variable index.
     * @return the trace value at the specified position.
     */
    public Value getStoredTraceValue(int index)
    {
        return producerVariables.load(index);
    }


    /**
     * Gets the specified trace Value from the variables, without disturbing them.
     * @param index the variable index.
     * @param value the trace value to set.
     */
    public void setStoredTraceValue(int index, Value value)
    {
        producerVariables.store(index, value);
    }


    // Implementations for Variables.

    public void reset(int size)
    {
        super.reset(size);

        producerVariables.reset(size);
    }

    public void initialize(TracedVariables other)
    {
        super.initialize(other);

        producerVariables.initialize(other.producerVariables);
    }

    public boolean generalize(TracedVariables other)
    {
        return
            super.generalize(other) |
            producerVariables.generalize(other.producerVariables);
    }

    public void store(int index, Value value)
    {
        // Is this store operation an initialization of the variable?
        Value previousValue = super.load(index);
        if (previousValue == null ||
            previousValue.computationalType() != value.computationalType())
        {
            initializationIndex = index;
        }

        // Store the value itself in the variable.
        super.store(index, value);

        // Store the store value in its trace variable.
        producerVariables.store(index, producerValue);
    }

    public Value load(int index)
    {
        // Load and accumulate the store value of the variable.
        if (collectedProducerValue != null)
        {
            collectedProducerValue = collectedProducerValue.generalize(producerVariables.load(index));
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

        return super.equals(object) && this.producerVariables.equals(other.producerVariables);
    }


    public int hashCode()
    {
        return super.hashCode() ^ producerVariables.hashCode();
    }


    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        for (int index = 0; index < this.size(); index++)
        {
            Value value       = this.values[index];
            Value tracedValue = producerVariables.values[index];
            buffer = buffer.append('[')
                           .append(tracedValue == null ? "empty" : tracedValue.toString())
                           .append('>')
                           .append(value       == null ? "empty" : value.toString())
                           .append(']');
        }

        return buffer.toString();
    }
}
