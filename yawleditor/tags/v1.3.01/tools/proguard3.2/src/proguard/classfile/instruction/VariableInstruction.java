/* $Id: VariableInstruction.java,v 1.17 2004/10/10 20:56:58 eric Exp $
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
package proguard.classfile.instruction;

import proguard.classfile.*;
import proguard.classfile.attribute.*;

/**
 * This Instruction represents an instruction that refers to a variable on the
 * local variable stack.
 *
 * @author Eric Lafortune
 */
public class VariableInstruction extends Instruction
{
    public boolean wide;
    public int     variableIndex;
    public int     constant;


    /**
     * Creates an uninitialized VariableInstruction.
     */
    public VariableInstruction() {}


    public VariableInstruction(byte opcode,
                               int  variableIndex)
    {
        this(opcode, variableIndex, 0);
    }


    public VariableInstruction(byte opcode,
                               int  variableIndex,
                               int  constant)
    {
        this.opcode        = opcode;
        this.variableIndex = variableIndex;
        this.constant      = constant;
        this.wide          = mustBeWide();
    }


    /**
     * Copies the given instruction into this instruction.
     * @param variableInstruction the instruction to be copied.
     * @return this instruction.
     */
    public VariableInstruction copy(VariableInstruction variableInstruction)
    {
        this.opcode        = variableInstruction.opcode;
        this.variableIndex = variableInstruction.variableIndex;
        this.constant      = variableInstruction.constant;
        this.wide          = variableInstruction.wide;

        return this;
    }


    /**
     * Returns whether this instruction loads the value of a variable onto the
     * stack, or if it stores one.
     */
    public boolean isLoad()
    {
        // A load instruction can be recognized as follows.
        // Note that this includes the ret instruction, which has a negative opcode.
        // The iinc instruction is considered a store instruction.
        return opcode <  InstructionConstants.OP_ISTORE &&
               opcode != InstructionConstants.OP_IINC;
    }


    // Implementations for Instruction.

    public Instruction shrink()
    {
        // Is this instruction pointing to a variable with index from 0 to 3?
        if (variableIndex <= 3)
        {
            switch (opcode)
            {
                case InstructionConstants.OP_ILOAD: opcode = (byte)(InstructionConstants.OP_ILOAD_0 + variableIndex); break;
                case InstructionConstants.OP_LLOAD: opcode = (byte)(InstructionConstants.OP_LLOAD_0 + variableIndex); break;
                case InstructionConstants.OP_FLOAD: opcode = (byte)(InstructionConstants.OP_FLOAD_0 + variableIndex); break;
                case InstructionConstants.OP_DLOAD: opcode = (byte)(InstructionConstants.OP_DLOAD_0 + variableIndex); break;
                case InstructionConstants.OP_ALOAD: opcode = (byte)(InstructionConstants.OP_ALOAD_0 + variableIndex); break;

                case InstructionConstants.OP_ISTORE: opcode = (byte)(InstructionConstants.OP_ISTORE_0 + variableIndex); break;
                case InstructionConstants.OP_LSTORE: opcode = (byte)(InstructionConstants.OP_LSTORE_0 + variableIndex); break;
                case InstructionConstants.OP_FSTORE: opcode = (byte)(InstructionConstants.OP_FSTORE_0 + variableIndex); break;
                case InstructionConstants.OP_DSTORE: opcode = (byte)(InstructionConstants.OP_DSTORE_0 + variableIndex); break;
                case InstructionConstants.OP_ASTORE: opcode = (byte)(InstructionConstants.OP_ASTORE_0 + variableIndex); break;
            }
        }

        // Only make the instruction wide if necessary.
        wide = mustBeWide();

        return this;
    }


    protected boolean isWide()
    {
        return wide;
    }


    protected void readInfo(byte[] code, int offset)
    {
        int variableIndexSize = variableIndexSize();
        int constantSize      = constantSize();

        // Also initialize embedded variable indexes.
        if (variableIndexSize == 0)
        {
            // An embedded variable index can be decoded as follows.
            variableIndex = opcode < InstructionConstants.OP_ISTORE_0 ?
                (opcode - InstructionConstants.OP_ILOAD_0 ) & 3 :
                (opcode - InstructionConstants.OP_ISTORE_0) & 3;
        }
        else
        {
            variableIndex = readValue(code, offset, variableIndexSize); offset += variableIndexSize;
        }

        constant = readSignedValue(code, offset, constantSize);
    }


    protected void writeInfo(byte[] code, int offset)
    {
        int variableIndexSize = variableIndexSize();
        int constantSize      = constantSize();

        writeValue(code, offset, variableIndex, variableIndexSize); offset += variableIndexSize;
        writeValue(code, offset, constant,      constantSize);
    }


    public int length(int offset)
    {
        return (wide ? 2 : 1) + variableIndexSize() + constantSize();
    }


    public void accept(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, InstructionVisitor instructionVisitor)
    {
        instructionVisitor.visitVariableInstruction(classFile, methodInfo, codeAttrInfo, offset, this);
    }


    public String toString(int offset)
    {
        return "["+offset+"] "+getName()+(wide?"_w":"")+" (variableIndex="+variableIndex+", constant="+constant+")";
    }


    // Implementations for Object.

    public String toString()
    {
        return getName()+" (variableIndex="+variableIndex+", constant="+constant+")";
    }


    // Small utility methods.

    /**
     * Returns whether this instruction must be wide due to its variable index
     * and constant.
     */
    private boolean mustBeWide()
    {
        return variableIndex > 0xff ||
               (opcode == InstructionConstants.OP_IINC ? (constant < -128 ||
                                                          constant > 127) :
                                                         constant > 255);
    }


    /**
     * Returns the appropriate variable index size for this instruction.
     */
    private int variableIndexSize()
    {
        return (opcode >= InstructionConstants.OP_ILOAD_0 &&
                opcode <= InstructionConstants.OP_ALOAD_3) ||
               (opcode >= InstructionConstants.OP_ISTORE_0 &&
                opcode <= InstructionConstants.OP_ASTORE_3) ? 0 :
               wide                                         ? 2 :
                                                              1;
    }


    /**
     * Returns the appropriate constant size for this instruction.
     */
    private int constantSize()
    {
        return opcode == InstructionConstants.OP_IINC ? (wide ? 2 : 1) :
                                                        0;
    }
}
