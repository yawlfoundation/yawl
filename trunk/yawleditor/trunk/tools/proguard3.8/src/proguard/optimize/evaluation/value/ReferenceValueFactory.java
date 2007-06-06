/* $Id: ReferenceValueFactory.java,v 1.5.2.2 2007/01/18 21:31:53 eric Exp $
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
 * This class provides methods to create and reuse ReferenceValue objects.
 *
 * @author Eric Lafortune
 */
public class ReferenceValueFactory
{
    // Shared copies of ReferenceValue objects, to avoid creating a lot of objects.
    private static ReferenceValue REFERENCE_VALUE_MAYBE_NULL = new ReferenceValue(true);
    private static ReferenceValue REFERENCE_VALUE_NOT_NULL   = new ReferenceValue(false);
    private static ReferenceValue REFERENCE_VALUE_NULL       = new SpecificReferenceValue(null, true);


    /**
     * Creates a new ReferenceValue with an undefined value.
     */
    public static ReferenceValue create(boolean mayBeNull)
    {
        return mayBeNull ? REFERENCE_VALUE_MAYBE_NULL :
                           REFERENCE_VALUE_NOT_NULL;
    }


    /**
     * Creates a new ReferenceValue that represents <code>null</code>.
     */
    public static ReferenceValue createNull()
    {
        return REFERENCE_VALUE_NULL;
    }


    /**
     * Creates a new ReferenceValue of a specific type. If the value is
     * <code>null</code>, a ReferenceValue of an undefined type is returned.
     */
    public static ReferenceValue create(ClassFile value, boolean mayBeNull)
    {
        return value == null ? ReferenceValueFactory.create(mayBeNull) :
                               new SpecificReferenceValue(value, mayBeNull);
    }


    /**
     * Creates a new array ReferenceValue of a specific type and dimensionality.
     * If the value is <code>null</code>, a ReferenceValue of an undefined
     * type is returned. If the dimension is 0, a ReferenceValue of the given
     * type is returned.
     */
    public static ReferenceValue create(ClassFile value, int dimension, boolean mayBeNull)
    {
        return value == null  ? ReferenceValueFactory.create(mayBeNull)        :
               dimension == 0 ? ReferenceValueFactory.create(value, mayBeNull) :
                                new SpecificArrayReferenceValue(value, dimension, mayBeNull);
    }
}
