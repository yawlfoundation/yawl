/* $Id: SimpleInstruction.java,v 1.9.2.1 2006/01/16 22:57:55 eric Exp $
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
public class SimpleInstruction extends Instruction
{
    public int constant;


    /**
     * Creates an uninitialized SimpleInstruction.
     */
    public SimpleInstruction() {}


    /**
     * Creates a new SimpleInstruction with the given opcode.
     */
    public SimpleInstruction(byte opcode)
    {
        this(opcode, 0);
    }


    /**
     * Creates a new SimpleInstruction with the given opcode and constant.
     */
    public SimpleInstruction(byte opcode, int constant)
    {
        this.opcode   = opcode;
        this.constant = constant;
    }


    /**
     * Copies the given instruction into this instruction.
     * @param simpleInstruction the instruction to be copied.
     * @return this instruction.
     */
    public SimpleInstruction copy(SimpleInstruction simpleInstruction)
    {
        this.opcode   = simpleInstruction.opcode;
        this.constant = simpleInstruction.constant;

        return this;
    }


    // Implementations for Instruction.

    public Instruction shrink()
    {
        // Is this a sipush instruction that can be a bipush instruction?
        if (opcode == InstructionConstants.OP_SIPUSH &&
            constant << 24 >> 24 == constant)
        {
            opcode = InstructionConstants.OP_BIPUSH;
        }

        // Is this a bipush instruction that can be an iconst instruction?
        if (opcode == InstructionConstants.OP_BIPUSH &&
            constant >= -1 &&
            constant <= 5)
        {
            opcode = (byte)(InstructionConstants.OP_ICONST_0 + constant);
        }

        return this;
    }

    protected void readInfo(byte[] code, int offset)
    {
        // Also initialize embedded constants that are different from 0.
        switch (opcode)
        {
            case InstructionConstants.OP_ICONST_M1:
                constant = -1;
                break;

            case InstructionConstants.OP_ICONST_1:
            case InstructionConstants.OP_LCONST_1:
            case InstructionConstants.OP_FCONST_1:
            case InstructionConstants.OP_DCONST_1:
                constant = 1;
                break;

            case InstructionConstants.OP_ICONST_2:
            case InstructionConstants.OP_FCONST_2:
                constant = 2;
                break;

            case InstructionConstants.OP_ICONST_3:
                constant = 3;
                break;

            case InstructionConstants.OP_ICONST_4:
                constant = 4;
                break;

            case InstructionConstants.OP_ICONST_5:
                constant = 5;
                break;

            default:
                constant = readSignedValue(code, offset, constantSize());
                break;
        }
    }


    protected void writeInfo(byte[] code, int offset)
    {
        writeValue(code, offset, constant, constantSize());
    }


    public int length(int offset)
    {
        return 1 + constantSize();
    }


    public void accept(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, InstructionVisitor instructionVisitor)
    {
        instructionVisitor.visitSimpleInstruction(classFile, methodInfo, codeAttrInfo, offset, this);
    }


    public String toString(int offset)
    {
        return "["+offset+"] "+getName()+" (constant="+constant+")";
    }


    // Implementations for Object.

    public String toString()
    {
        return getName()+" (constant="+constant+")";
    }


    // Small utility methods.

    /**
     * Computes the appropriate constant size for this instruction.
     */
    private int constantSize()
    {
        return opcode == InstructionConstants.OP_BIPUSH ||
               opcode == InstructionConstants.OP_NEWARRAY ? 1 :
               opcode == InstructionConstants.OP_SIPUSH   ? 2 :
                                                            0;
    }
}
