/* $Id: TableSwitchInstruction.java,v 1.7.2.1 2006/01/16 22:57:55 eric Exp $
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
package proguard.classfile.instruction;

import proguard.classfile.*;
import proguard.classfile.attribute.*;

/**
 * This Instruction represents a simple instruction without variable arguments
 * or constant pool references.
 *
 * @author Eric Lafortune
 */
public class TableSwitchInstruction extends Instruction
{
    public int   defaultOffset;
    public int   lowCase;
    public int   highCase;
    public int   jumpOffsetCount;
    public int[] jumpOffsets;


    /**
     * Creates an uninitialized TableSwitchInstruction.
     */
    public TableSwitchInstruction() {}


    /**
     * Copies the given instruction into this instruction.
     * @param tableSwitchInstruction the instruction to be copied.
     * @return this instruction.
     */
    public TableSwitchInstruction copy(TableSwitchInstruction tableSwitchInstruction)
    {
        this.opcode          = tableSwitchInstruction.opcode;
        this.defaultOffset   = tableSwitchInstruction.defaultOffset;
        this.lowCase         = tableSwitchInstruction.lowCase;
        this.highCase        = tableSwitchInstruction.highCase;
        this.jumpOffsetCount = tableSwitchInstruction.jumpOffsetCount;
        this.jumpOffsets     = tableSwitchInstruction.jumpOffsets;

        return this;
    }


    // Implementations for Instruction.

    public Instruction shrink()
    {
        // There aren't any ways to shrink this instruction.
        return this;
    }

    protected void readInfo(byte[] code, int offset)
    {
        // Skip up to three padding bytes.
        offset += -offset & 3;

        // Read the three 32-bit arguments.
        defaultOffset = readInt(code, offset); offset += 4;
        lowCase       = readInt(code, offset); offset += 4;
        highCase      = readInt(code, offset); offset += 4;

        // Make sure there is a sufficiently large jump offset table.
        jumpOffsetCount = highCase - lowCase + 1;
        if (jumpOffsets == null ||
            jumpOffsets.length < jumpOffsetCount)
        {
            jumpOffsets = new int[jumpOffsetCount];
        }

        // Read the jump offsets.
        for (int index = 0; index < jumpOffsetCount; index++)
        {
            jumpOffsets[index] = readInt(code, offset); offset += 4;
        }
    }


    protected void writeInfo(byte[] code, int offset)
    {
        // Write up to three padding bytes.
        while ((offset & 3) != 0)
        {
            writeByte(code, offset++, 0);
        }

        // Write the three 32-bit arguments.
        writeInt(code, offset, defaultOffset); offset += 4;
        writeInt(code, offset, lowCase);       offset += 4;
        writeInt(code, offset, highCase);      offset += 4;

        // Write the jump offsets.
        int length = highCase - lowCase + 1;
        for (int index = 0; index < length; index++)
        {
            writeInt(code, offset, jumpOffsets[index]); offset += 4;
        }
    }


    public int length(int offset)
    {
        return 1 + (-(offset+1) & 3) + 12 + (highCase - lowCase + 1) * 4;
    }


    public void accept(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, InstructionVisitor instructionVisitor)
    {
        instructionVisitor.visitTableSwitchInstruction(classFile, methodInfo, codeAttrInfo, offset, this);
    }


    public String toString(int offset)
    {
        return "["+offset+"] "+getName()+" (low="+lowCase+", high="+highCase+")";
    }


    // Implementations for Object.

    public String toString()
    {
        return getName()+" (low="+lowCase+", high="+highCase+")";
    }
}
