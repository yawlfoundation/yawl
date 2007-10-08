/* $Id: ValueFactory.java,v 1.3.2.2 2007/01/18 21:31:53 eric Exp $
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

import proguard.classfile.ClassConstants;

/**
 * This class provides methods to create and reuse Value objects.
 *
 * @author Eric Lafortune
 */
public class ValueFactory
{
    /**
     * Creates a new Value with an undefined value, corresponding to the given
     * internal type. The void type returns <code>null</code>.
     */
    public static Value create(String internalType)
    {
        switch (internalType.charAt(0))
        {
            case ClassConstants.INTERNAL_TYPE_VOID:
                return null;

            case ClassConstants.INTERNAL_TYPE_BOOLEAN:
            case ClassConstants.INTERNAL_TYPE_BYTE:
            case ClassConstants.INTERNAL_TYPE_CHAR:
            case ClassConstants.INTERNAL_TYPE_SHORT:
            case ClassConstants.INTERNAL_TYPE_INT:
                return IntegerValueFactory.create();

            case ClassConstants.INTERNAL_TYPE_FLOAT:
                return FloatValueFactory.create();

            case ClassConstants.INTERNAL_TYPE_LONG:
                return LongValueFactory.create();

            case ClassConstants.INTERNAL_TYPE_DOUBLE:
                return DoubleValueFactory.create();

            default:
                return ReferenceValueFactory.create(true);
        }
    }
}
