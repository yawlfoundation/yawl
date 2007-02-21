/* $Id: TracedStack.java,v 1.10.2.1 2006/01/16 22:57:56 eric Exp $
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
package proguard.optimize.evaluation;

import proguard.optimize.evaluation.value.*;

/**
 * This Stack saves additional information with stack elements, to keep track
 * of their origins and destionations.
 * <p>
 * The stack stores a given producer Value along with each Value it stores.
 * It then generalizes a given collected Value with the producer Value
 * of each Value it loads. The producer Value and the initial collected Value
 * can be set; the generalized collected Value can be retrieved.
 * <p>
 * The stack also stores an empty consumer Value along with each Value it
 * stores. It then generalizes and updates this consumer Value with the
 * given producer Value each time the Value is loaded from the stack. The
 * generalized consumer Value of each stack element can be retrieved.
 *
 * @author Eric Lafortune
 */
class TracedStack extends Stack
{
    private Value producerValue;
    private Value collectedProducerValue;
    private Stack producerStack;
    private Stack consumerStack;


    public TracedStack(int maxSize)
    {
        super(maxSize);

        producerStack = new Stack(maxSize);
        consumerStack = new Stack(maxSize);
    }


    public TracedStack(TracedStack tracedStack)
    {
        super(tracedStack);

        producerStack = new Stack(tracedStack.producerStack);
        consumerStack = new Stack(tracedStack.consumerStack);
    }


    /**
     * Sets the Value that will be stored along with all push and pop
     * instructions.
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
     * Gets the specified producer Value from the stack, without disturbing it.
     * @param index the index of the stack element, counting from the bottom
     *              of the stack.
     * @return the producer value at the specified position.
     */
    public Value getBottomProducerValue(int index)
    {
        return producerStack.getBottom(index);
    }


    /**
     * Sets the specified producer Value on the stack, without disturbing it.
     * @param index the index of the stack element, counting from the bottom
     *              of the stack.
     * @param value the producer value to set.
     */
    public void setBottomProducerValue(int index, Value value)
    {
        producerStack.setBottom(index, value);
    }


    /**
     * Gets the specified producer Value from the stack, without disturbing it.
     * @param index the index of the stack element, counting from the top
     *              of the stack.
     * @return the producer value at the specified position.
     */
    public Value getTopProducerValue(int index)
    {
        return producerStack.getTop(index);
    }


    /**
     * Sets the specified producer Value on the stack, without disturbing it.
     * @param index the index of the stack element, counting from the top
     *              of the stack.
     * @param value the producer value to set.
     */
    public void setTopProducerValue(int index, Value value)
    {
        producerStack.setTop(index, value);
    }


    /**
     * Gets the specified consumer Value from the stack, without disturbing it.
     * @param index the index of the stack element, counting from the bottom of
     *              the stack.
     * @return the consumer value at the specified position.
     */
    public Value getBottomConsumerValue(int index)
    {
        return ((MutableValue)consumerStack.getBottom(index)).getContainedValue();
    }

    /**
     * Gets the specified consumer Value from the stack, without disturbing it.
     * @param index the index of the stack element, counting from the top of the
     *              stack.
     * @return the consumer value at the specified position.
     */
    public Value getTopConsumerValue(int index)
    {
        return ((MutableValue)consumerStack.getTop(index)).getContainedValue();
    }


    /**
     * Sets the specified consumer Value on the stack, without disturbing it.
     * @param index the index of the stack element, counting from the top
     *              of the stack.
     * @param value the consumer value to set.
     */
    public void setTopConsumerValue(int index, Value value)
    {
        ((MutableValue)consumerStack.getTop(index)).setContainedValue(value);
        consumerStack.setTop(index, new MutableValue());
    }


    // Implementations for Stack.

    public void reset(int size)
    {
        super.reset(size);

        producerStack.reset(size);
        consumerStack.reset(size);
    }

    public void copy(TracedStack other)
    {
        super.copy(other);

        producerStack.copy(other.producerStack);
        consumerStack.copy(other.consumerStack);
    }

    public boolean generalize(TracedStack other)
    {
        return
            super.generalize(other) |
            producerStack.generalize(other.producerStack) |
            consumerStack.generalize(other.consumerStack);
    }

    public void clear()
    {
        super.clear();

        producerStack.clear();
        consumerStack.clear();
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

        producerGeneralize(0);
        producerStack.dup();

        consumerPop();
        consumerPush();
        consumerPush();
    }

    public void dup_x1()
    {
        super.dup_x1();

        producerGeneralize(0);
        producerStack.dup_x1();

        consumerPop();
        consumerPush();
        consumerStack.swap();
        consumerPush();
    }

    public void dup_x2()
    {
        super.dup_x2();

        producerGeneralize(0);
        producerStack.dup_x2();

        consumerPop();
        consumerPush();
        consumerStack.dup_x2();
        consumerStack.pop();
        consumerPush();
    }

    public void dup2()
    {
        super.dup2();

        producerGeneralize(0);
        producerGeneralize(1);
        producerStack.dup2();

        consumerPop();
        consumerPop();
        consumerPush();
        consumerPush();
        consumerPush();
        consumerPush();
    }

    public void dup2_x1()
    {
        super.dup2_x1();

        producerGeneralize(0);
        producerGeneralize(1);
        producerStack.dup2_x1();

        consumerPop();
        consumerPop();
        consumerPush();
        consumerPush();
        consumerStack.dup2_x1();
        consumerStack.pop2();
        consumerPush();
        consumerPush();
    }

    public void dup2_x2()
    {
        super.dup2_x2();

        producerGeneralize(0);
        producerGeneralize(1);
        producerStack.dup2_x2();

        consumerPop();
        consumerPop();
        consumerPush();
        consumerPush();
        consumerStack.dup2_x2();
        consumerStack.pop2();
        consumerPush();
        consumerPush();
    }

    public void swap()
    {
        super.swap();

        producerGeneralize(0);
        producerGeneralize(1);
        producerStack.swap();

        consumerPop();
        consumerPop();
        consumerPush();
        consumerPush();
    }


    // Implementations for Object.

    public boolean equals(Object object)
    {
        if (this.getClass() != object.getClass())
        {
            return false;
        }

        TracedStack other = (TracedStack)object;

        return super.equals(object) &&
               this.producerStack.equals(other.producerStack) &&
               this.consumerStack.equals(other.consumerStack);
    }


    public int hashCode()
    {
        return super.hashCode() ^
               producerStack.hashCode() ^
               consumerStack.hashCode();
    }


    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        for (int index = 0; index < this.size(); index++)
        {
            Value value         = this.values[index];
            Value producerValue = producerStack.values[index];
            Value consumerValue = consumerStack.values[index];
            buffer = buffer.append('[')
                           .append(producerValue == null ? "empty" : producerValue.toString())
                           .append('>')
                           .append(value         == null ? "empty" : value.toString())
                           .append('>')
                           .append(consumerValue == null ? "empty" : consumerValue.toString())
                           .append(']');
        }

        return buffer.toString();
    }


    // Small utility methods.

    private void tracePush()
    {
        producerPush();
        consumerPush();
    }


    private void producerPush()
    {
        producerStack.push(producerValue);
    }


    private void consumerPush()
    {
        consumerStack.push(new MutableValue());
    }


    private void tracePop()
    {
        producerPop();
        consumerPop();
    }


    private void producerPop()
    {
        Value popProducerValue = producerStack.pop();
        producerGeneralize(popProducerValue);
    }


    private void consumerPop()
    {
        MutableValue popConsumerValue = (MutableValue)consumerStack.pop();
        popConsumerValue.generalizeContainedValue(producerValue);
    }


    private void producerGeneralize(int index)
    {
        // We're not remembering the producers for dup/swap calls.
        producerGeneralize(producerStack.getTop(index));
    }


    private void producerGeneralize(Value producerValue)
    {
        if (collectedProducerValue != null)
        {
            collectedProducerValue = collectedProducerValue.generalize(producerValue);
        }
    }


    /**
     * This Value is a mutable wrapper for other Value instances.
     * Its generalization method affects the Value itself as a side-effect.
     */
    private static class MutableValue extends Category1Value
    {
        Value containedValue;


        public void generalizeContainedValue(Value containedValue)
        {
            MutableValue lastMutableValue  = lastMutableValue();
            Value        lastContainedValue= lastMutableValue.containedValue;

            lastMutableValue.containedValue =
                lastContainedValue == null ? containedValue :
                                             containedValue.generalize(lastContainedValue);
        }


        public void setContainedValue(Value value)
        {
            lastMutableValue().containedValue = value;
        }


        public Value getContainedValue()
        {
            return lastMutableValue().containedValue;
        }


        // Implementations for Value.

        public Value generalize(Value other)
        {
            MutableValue otherMutableValue = (MutableValue)other;

            MutableValue thisLastMutableValue  = this.lastMutableValue();
            MutableValue otherLastMutableValue = otherMutableValue.lastMutableValue();

            Value thisLastContainedValue  = thisLastMutableValue.containedValue;
            Value otherLastContainedValue = otherLastMutableValue.containedValue;

            if (thisLastMutableValue != otherLastMutableValue)
            {
                otherLastMutableValue.containedValue = thisLastMutableValue;
            }

            thisLastMutableValue.containedValue =
                thisLastContainedValue  == null ? otherLastContainedValue :
                otherLastContainedValue == null ? thisLastContainedValue  :
                                                  thisLastContainedValue.generalize(otherLastContainedValue);
            return thisLastMutableValue;
        }


        public int computationalType()
        {
            return 0;
        }


        // Implementations for Object.

//        public boolean equals(Object other)
//        {
//            return this.getClass() == other.getClass() &&
//                   this.lastMutableValue() == ((MutableValue)other).lastMutableValue();
//        }
//
//
//        public int hashCode()
//        {
//            return lastMutableValue().containedValue.hashCode();
//        }


        public String toString()
        {
            return containedValue == null ? "none" : containedValue.toString();
        }


        // Small utility methods.

        public MutableValue lastMutableValue()
        {
            MutableValue mutableValue = this;

            while (mutableValue.containedValue instanceof MutableValue)
            {
                mutableValue = (MutableValue)mutableValue.containedValue;
            }

            return mutableValue;
        }
    }
}
