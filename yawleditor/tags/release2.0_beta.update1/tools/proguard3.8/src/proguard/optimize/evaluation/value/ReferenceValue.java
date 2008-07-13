/* $Id: ReferenceValue.java,v 1.4.2.2 2007/01/18 21:31:53 eric Exp $
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
package proguard.optimize.evaluation.value;

import proguard.classfile.*;
import proguard.classfile.ClassCpInfo;

/**
 * This class represents a partially evaluated reference value.
 *
 * @author Eric Lafortune
 */
public class ReferenceValue extends Category1Value
{
    protected boolean mayBeNull;


    /**
     * Creates a new reference value that may or may not be null.
     */
    public ReferenceValue(boolean mayBeNull)
    {
        this.mayBeNull = mayBeNull;
    }


    /**
     * Returns the specific reference value, if applicable.
     */
    public ClassFile value()
    {
        return null;
    }


    /**
     * Returns the array dimension, if applicable.
     */
    public int dimension()
    {
        return 0;
    }


    // Basic binary methods.

    /**
     * Returns the generalization of this ReferenceValue and the given other
     * ReferenceValue.
     */
    public ReferenceValue generalize(ReferenceValue other)
    {
        return ReferenceValueFactory.create(this.mayBeNull || other.mayBeNull);
    }


    /**
     * Returns whether this ReferenceValue and the given ReferenceValue are equal:
     * <code>NEVER</code>, <code>MAYBE</code>, or <code>ALWAYS</code>.
     */
    public int equal(ReferenceValue other)
    {
        return MAYBE;
    }


    // Basic unary methods.

    /**
     * Returns whether this ReferenceValue is <code>null</code>:
     * <code>NEVER</code>, <code>MAYBE</code>, or <code>ALWAYS</code>.
     */
    public int isNull()
    {
        return mayBeNull ? MAYBE : NEVER;
    }


    /**
     * Returns whether this ReferenceValue is an instance of the given class
     * with the given dimensionality:
     * <code>NEVER</code>, <code>MAYBE</code>, or <code>ALWAYS</code>.
     */
    public int instanceOf(ClassFile typeClassFile, int typeDimension)
    {
        return MAYBE;
    }


    // Derived binary methods.

    /**
     * Returns whether this ReferenceValue and the given ReferenceValue are different:
     * <code>NEVER</code>, <code>MAYBE</code>, or <code>ALWAYS</code>.
     */
    public final int notEqual(ReferenceValue other)
    {
        return -equal(other);
    }


    /**
     * Returns whether this ReferenceValue is not <code>null</code>:
     * <code>NEVER</code>, <code>MAYBE</code>, or <code>ALWAYS</code>.
     */
    public final int isNotNull()
    {
        return -isNull();
    }


    // Similar binary methods, but this time with more specific arguments.

    /**
     * Returns the generalization of this ReferenceValue and the given other
     * SpecificReferenceValue.
     */
    public ReferenceValue generalize(SpecificReferenceValue other)
    {
        return ReferenceValueFactory.create(this.mayBeNull || other.mayBeNull);
    }


    /**
     * Returns whether this ReferenceValue and the given SpecificReferenceValue are
     * equal: <code>NEVER</code>, <code>MAYBE</code>, or <code>ALWAYS</code>.
     */
    public int equal(SpecificReferenceValue other)
    {
        return MAYBE;
    }


    // Implementations for Value.

    public final ReferenceValue referenceValue()
    {
        return this;
    }

    public final Value generalize(Value other)
    {
        return this.generalize(other.referenceValue());
    }

    public final int computationalType()
    {
        return TYPE_REFERENCE;
    }


    // Implementations for Object.

    public boolean equals(Object object)
    {
        return object != null &&
               this.getClass() == object.getClass() &&
               this.mayBeNull  == ((ReferenceValue)object).mayBeNull;
    }


    public int hashCode()
    {
        return this.getClass().hashCode() ^ (mayBeNull ? 0 : 1);
    }


    public String toString()
    {
        return mayBeNull ? "a" : "a:!null";
    }
}
