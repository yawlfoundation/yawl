/* $Id: LookUpSwitchInstruction.java,v 1.7.2.1 2006/01/16 22:57:55 eric Exp $
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
public class LookUpSwitchInstruction extends Instruction
{
    public int   defaultOffset;
    public int   jumpOffsetCount;
    public int[] cases;
    public int[] jumpOffsets;


    /**
     * Creates an uninitialized LookUpSwitchInstruction.
     */
    public LookUpSwitchInstruction() {}


    /**
     * Copies the given instruction into this instruction.
     * @param lookUpSwitchInstruction the instruction to be copied.
     * @return this instruction.
     */
    public LookUpSwitchInstruction copy(LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        this.opcode          = lookUpSwitchInstruction.opcode;
        this.defaultOffset   = lookUpSwitchInstruction.defaultOffset;
        this.jumpOffsetCount = lookUpSwitchInstruction.jumpOffsetCount;
        this.cases           = lookUpSwitchInstruction.cases;
        this.jumpOffsets     = lookUpSwitchInstruction.jumpOffsets;

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

        // Read the two 32-bit arguments.
        defaultOffset   = readInt(code, offset); offset += 4;
        jumpOffsetCount = readInt(code, offset); offset += 4;

        // Make sure there are sufficiently large match-offset tables.
        if (jumpOffsets == null ||
            jumpOffsets.length < jumpOffsetCount)
        {
            cases       = new int[jumpOffsetCount];
            jumpOffsets = new int[jumpOffsetCount];
        }

        // Read the matches-offset pairs.
        for (int index = 0; index < jumpOffsetCount; index++)
        {
            cases[index]       = readInt(code, offset); offset += 4;
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

        // Write the two 32-bit arguments.
        writeInt(code, offset, defaultOffset);   offset += 4;
        writeInt(code, offset, jumpOffsetCount); offset += 4;

        // Write the matches-offset pairs.
        for (int index = 0; index < jumpOffsetCount; index++)
        {
            writeInt(code, offset, cases[index]);       offset += 4;
            writeInt(code, offset, jumpOffsets[index]); offset += 4;
        }
    }


    public int length(int offset)
    {
        return 1 + (-(offset+1) & 3) + 8 + jumpOffsetCount * 8;
    }


    public void accept(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, InstructionVisitor instructionVisitor)
    {
        instructionVisitor.visitLookUpSwitchInstruction(classFile, methodInfo, codeAttrInfo, offset, this);
    }


    public String toString(int offset)
    {
        return "["+offset+"] "+getName()+" (switchCaseCount="+jumpOffsetCount+")";
    }


    // Implementations for Object.

    public String toString()
    {
        return getName()+" (switchCaseCount="+jumpOffsetCount+")";
    }
}
