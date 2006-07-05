/* $Id: FloatValueFactory.java,v 1.2 2004/08/15 12:39:30 eric Exp $
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
package proguard.optimize.evaluation.value;

/**
 * This class provides methods to create and reuse FloatValue objects.
 *
 * @author Eric Lafortune
 */
public class FloatValueFactory
{
    // Shared copies of FloatValue objects, to avoid creating a lot of objects.
    private static FloatValue         FLOAT_VALUE   = new FloatValue();
    private static SpecificFloatValue FLOAT_VALUE_0 = new SpecificFloatValue(0.0f);
    private static SpecificFloatValue FLOAT_VALUE_1 = new SpecificFloatValue(1.0f);
    private static SpecificFloatValue FLOAT_VALUE_2 = new SpecificFloatValue(2.0f);


    /**
     * Creates a new FloatValue with an undefined value.
     */
    public static FloatValue create()
    {
        return FLOAT_VALUE;
    }

    /**
     * Creates a new FloatValue with a given specific value.
     */
    public static SpecificFloatValue create(float value)
    {
        return value == 0.0f ? FLOAT_VALUE_0 :
               value == 1.0f ? FLOAT_VALUE_1 :
               value == 2.0f ? FLOAT_VALUE_2 :
                               new SpecificFloatValue(value);
    }
}
