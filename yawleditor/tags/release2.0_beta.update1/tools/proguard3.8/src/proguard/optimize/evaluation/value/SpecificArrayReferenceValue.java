/* $Id: SpecificArrayReferenceValue.java,v 1.5.2.2 2007/01/18 21:31:53 eric Exp $
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
import proguard.classfile.util.ClassUtil;

/**
 * This ReferenceValue represents an array reference value of a specific type
 * and dimensionality.
 *
 * @author Eric Lafortune
 */
class SpecificArrayReferenceValue extends SpecificReferenceValue
{
    protected int dimension;


    /**
     * Creates a new specific array reference value. The dimensionality of the
     * value argument alone is ignored.
     */
    public SpecificArrayReferenceValue(ClassFile value, int dimension, boolean mayBeNull)
    {
        super(value, mayBeNull);

        this.dimension = dimension;
    }


    // Implementations for ReferenceValue.

    public int dimension()
    {
        return dimension;
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

    public int instanceOf(ClassFile typeClassFile, int typeDimension)
    {
        // If this value is null, it is never an instance of any class.
        if (value == null)
        {
            return NEVER;
        }

        // If the type class is unknown or the type has a higher dimension, we
        // can't tell for sure.
        if (typeClassFile == null ||
            typeDimension > dimension)
        {
            return MAYBE;
        }

        // If the type dimension is less than the value's dimension, the type
        // must be Object, Cloneable, or Serializable.
        if (typeDimension < dimension)
        {
            String typeClassName = typeClassFile.getName();
            return typeClassName.equals(ClassConstants.INTERNAL_NAME_JAVA_LANG_OBJECT)    ||
                   typeClassName.equals(ClassConstants.INTERNAL_NAME_JAVA_LANG_CLONEABLE) ||
                   typeClassName.equals(ClassConstants.INTERNAL_NAME_JAVA_IO_SERIALIZABLE) ?
                       ALWAYS :
                       NEVER;
        }

        // If the value extends the type, we're sure.
        return value.extends_(typeClassFile) ||
               value.implements_(typeClassFile) ?
                   ALWAYS :
                   MAYBE;
    }


    // Implementations of binary ReferenceValue methods with
    // SpecificArrayReferenceValue arguments.

    public ReferenceValue generalize(SpecificArrayReferenceValue other)
    {
        return this.value     == other.value &&
               this.dimension == other.dimension ?
            this.mayBeNull ? this : other :
            ReferenceValueFactory.create(this.mayBeNull || other.mayBeNull);
    }

    public int equal(SpecificArrayReferenceValue other)
    {
        return this.value  == null &&
               other.value == null ? ALWAYS : MAYBE;
    }


    // Implementations for Object.

    public boolean equals(Object object)
    {
        if (object == null ||
            this.getClass() != object.getClass())
        {
            return false;
        }

        SpecificArrayReferenceValue other = (SpecificArrayReferenceValue)object;
        return this.mayBeNull == other.mayBeNull                     &&
               (this.value == null ? other.value == null :
                                     this.value.equals(other.value)) &&
               this.dimension == other.dimension;
    }


    public int hashCode()
    {
        return this.getClass().hashCode()             ^
               (value == null ? 0 : value.hashCode()) ^
               dimension;
    }


    public String toString()
    {
        return "a:" + (value == null ? "null" : value.getName()+"["+dimension+"]");
    }
}
