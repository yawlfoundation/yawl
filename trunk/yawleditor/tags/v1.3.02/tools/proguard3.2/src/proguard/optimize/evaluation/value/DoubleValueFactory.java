/* $Id: DoubleValueFactory.java,v 1.2 2004/08/15 12:39:30 eric Exp $
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
 * This class provides methods to create and reuse DoubleValue objects.
 *
 * @author Eric Lafortune
 */
public class DoubleValueFactory
{
    // Shared copies of DoubleValue objects, to avoid creating a lot of objects.
    private static DoubleValue         DOUBLE_VALUE   = new DoubleValue();
    private static SpecificDoubleValue DOUBLE_VALUE_0 = new SpecificDoubleValue(0.0);
    private static SpecificDoubleValue DOUBLE_VALUE_1 = new SpecificDoubleValue(1.0);


    /**
     * Creates a new DoubleValue with an undefined value.
     */
    public static DoubleValue create()
    {
        return DOUBLE_VALUE;
    }

    /**
     * Creates a new DoubleValue with a given specific value.
     */
    public static SpecificDoubleValue create(double value)
    {
        return value == 0.0 ? DOUBLE_VALUE_0 :
               value == 1.0 ? DOUBLE_VALUE_1 :
                              new SpecificDoubleValue(value);
    }
}
