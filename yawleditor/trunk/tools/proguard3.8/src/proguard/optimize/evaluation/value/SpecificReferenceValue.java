/* $Id: SpecificReferenceValue.java,v 1.5.2.2 2007/01/18 21:31:53 eric Exp $
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
 * This ReferenceValue represents a reference value of a specific type.
 *
 * @author Eric Lafortune
 */
class SpecificReferenceValue extends ReferenceValue
{
    protected ClassFile value;


    /**
     * Creates a new specific reference value.
     */
    public SpecificReferenceValue(ClassFile value, boolean mayBeNull)
    {
        super(mayBeNull);

        this.value = value;
    }


    // Implementations for ReferenceValue.

    public ClassFile value()
    {
        return value;
    }


    // Implementations of binary methods of ReferenceValue.

    // Perhaps the other value arguments are more specific than apparent
    // in these methods, so delegate to them.

    public ReferenceValue generalize(ReferenceValue other)
    {
        return other.generalize(this);
    }

    public int equal(ReferenceValue other)
    {
        return other.equal(this);
    }


    // Implementations of unary methods of ReferenceValue.

    public int isNull()
    {
        return value == null ? ALWAYS :
               mayBeNull     ? MAYBE  :
                               NEVER;
    }


    public int instanceOf(ClassFile typeClassFile, int typeDimension)
    {
        // If this value is null, it is never an instance of any class.
        if (value == null)
        {
            return NEVER;
        }

        // If the type class is unknown or the type is an array type, we can't
        // tell for sure.
        if (typeClassFile == null ||
            typeDimension > 0)
        {
            return MAYBE;
        }

        // If the value extends the type, we're sure.
        return value.extends_(typeClassFile) ||
               value.implements_(typeClassFile) ?
                   ALWAYS :
                   MAYBE;
    }


    // Implementations of binary ReferenceValue methods with SpecificReferenceValue
    // arguments.

    public ReferenceValue generalize(SpecificReferenceValue other)
    {
        return this.value == other.value ?
            this.mayBeNull ? this : other :
            ReferenceValueFactory.create(this.mayBeNull || other.mayBeNull);
    }

    public int equal(SpecificReferenceValue other)
    {
        return this.value  == null &&
               other.value == null ? ALWAYS : MAYBE;
    }


    // Implementations for Value.

    public boolean isSpecific()
    {
        return true;
    }


    // Implementations for Object.

    public boolean equals(Object object)
    {
        if (object == null ||
            this.getClass() != object.getClass())
        {
            return false;
        }

        SpecificReferenceValue other = (SpecificReferenceValue)object;
        return this.mayBeNull == other.mayBeNull &&
               (this.value == null ? other.value == null :
                                     this.value.equals(other.value));
    }


    public int hashCode()
    {
        return this.getClass().hashCode() ^
               (value == null ? 0 : value.hashCode());
    }


    public String toString()
    {
        return "a:" + (value == null ? "null" : value.getName());
    }
}
