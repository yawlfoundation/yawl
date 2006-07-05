/* $Id: IntegerValueFactory.java,v 1.2 2004/08/15 12:39:30 eric Exp $
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
 * This class provides methods to create and reuse IntegerValue objects.
 *
 * @author Eric Lafortune
 */
public class IntegerValueFactory
{
    // Shared copies of IntegerValue objects, to avoid creating a lot of objects.
    private static IntegerValue         INTEGER_VALUE    = new IntegerValue();
    private static SpecificIntegerValue INTEGER_VALUE_M1 = new SpecificIntegerValue(-1);
    private static SpecificIntegerValue INTEGER_VALUE_0  = new SpecificIntegerValue(0);
    private static SpecificIntegerValue INTEGER_VALUE_1  = new SpecificIntegerValue(1);
    private static SpecificIntegerValue INTEGER_VALUE_2  = new SpecificIntegerValue(2);
    private static SpecificIntegerValue INTEGER_VALUE_3  = new SpecificIntegerValue(3);
    private static SpecificIntegerValue INTEGER_VALUE_4  = new SpecificIntegerValue(4);
    private static SpecificIntegerValue INTEGER_VALUE_5  = new SpecificIntegerValue(5);

    /**
     * Creates a new IntegerValue with an undefined value.
     */
    public static IntegerValue create()
    {
        return INTEGER_VALUE;
    }

    /**
     * Creates a new IntegerValue with a given specific value.
     */
    public static SpecificIntegerValue create(int value)
    {
        switch (value)
        {
            case -1: return INTEGER_VALUE_M1;
            case  0: return INTEGER_VALUE_0;
            case  1: return INTEGER_VALUE_1;
            case  2: return INTEGER_VALUE_2;
            case  3: return INTEGER_VALUE_3;
            case  4: return INTEGER_VALUE_4;
            case  5: return INTEGER_VALUE_5;
            default: return new SpecificIntegerValue(value);
        }
    }

}
