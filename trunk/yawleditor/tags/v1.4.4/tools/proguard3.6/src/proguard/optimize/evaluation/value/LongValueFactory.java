/* $Id: LongValueFactory.java,v 1.3.2.1 2006/01/16 22:57:56 eric Exp $
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
package proguard.optimize.evaluation.value;

/**
 * This class provides methods to create and reuse LongValue objects.
 *
 * @author Eric Lafortune
 */
public class LongValueFactory
{
    // Shared copies of LongValue objects, to avoid creating a lot of objects.
    private static LongValue         LONG_VALUE   = new LongValue();
    private static SpecificLongValue LONG_VALUE_0 = new SpecificLongValue(0);
    private static SpecificLongValue LONG_VALUE_1 = new SpecificLongValue(1);


    /**
     * Creates a new LongValue with an undefined value.
     */
    public static LongValue create()
    {
        return LONG_VALUE;
    }

    /**
     * Creates a new LongValue with a given specific value.
     */
    public static SpecificLongValue create(long value)
    {
        return value == 0 ? LONG_VALUE_0 :
               value == 1 ? LONG_VALUE_1 :
                            new SpecificLongValue(value);
    }
}
