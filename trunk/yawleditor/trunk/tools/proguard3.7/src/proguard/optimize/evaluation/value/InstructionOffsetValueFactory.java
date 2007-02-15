/* $Id: InstructionOffsetValueFactory.java,v 1.3.2.1 2006/01/16 22:57:56 eric Exp $
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
 * This class provides methods to create and reuse InstructionOffsetValue objects.
 *
 * @author Eric Lafortune
 */
public class InstructionOffsetValueFactory
{
    // Shared copies of InstructionOffsetValue objects, to avoid creating a lot of objects.
    private static InstructionOffsetValue INSTRUCTION_OFFSET_VALUE = new InstructionOffsetValue();


    /**
     * Creates a new InstructionOffsetValue without a value.
     */
    public static InstructionOffsetValue create()
    {
        return INSTRUCTION_OFFSET_VALUE;
    }


    /**
     * Creates a new InstructionOffsetValue with a given specific value.
     */
    public static InstructionOffsetValue create(int value)
    {
        return new InstructionOffsetValue(value);
    }


    /**
     * Creates a new InstructionOffsetValue with a given list of possible values.
     */
    public static InstructionOffsetValue create(int[] values)
    {
        return new InstructionOffsetValue(values);
    }
}
