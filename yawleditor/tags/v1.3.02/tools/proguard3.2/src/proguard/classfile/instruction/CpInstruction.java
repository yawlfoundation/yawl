/* $Id: CpInstruction.java,v 1.19 2004/12/11 16:35:23 eric Exp $
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
import proguard.classfile.util.ClassUtil;
import proguard.classfile.visitor.CpInfoVisitor;

/**
 * This Instruction represents an instruction that refers to an entry in the
 * constant pool.
 *
 * @author Eric Lafortune
 */
public class CpInstruction extends Instruction
implements   CpInfoVisitor
{
    public int cpIndex;
    public int constant;


    // Fields acting as return parameters for the CpInfoVisitor methods.
    private int parameterStackDelta;
    private int typeStackDelta;


    /**
     * Creates an uninitialized CpInstruction.
     */
    public CpInstruction() {}


    /**
     * Creates a new CpInstruction with the given opcode and constant pool index.
     */
    public CpInstruction(byte opcode, int cpIndex)
    {
        this(opcode, cpIndex, 0);
    }


    /**
     * Creates a new CpInstruction with the given opcode, constant pool index,
     * and constant.
     */
    public CpInstruction(byte opcode, int cpIndex, int constant)
    {
        this.opcode   = opcode;
        this.cpIndex  = cpIndex;
        this.constant = constant;
    }


    /**
     * Copies the given instruction into this instruction.
     * @param cpInstruction the instruction to be copied.
     * @return this instruction.
     */
    public CpInstruction copy(CpInstruction cpInstruction)
    {
        this.opcode   = cpInstruction.opcode;
        this.cpIndex  = cpInstruction.cpIndex;
        this.constant = cpInstruction.constant;

        return this;
    }


    // Implementations for Instruction.

    public Instruction shrink()
    {
        if      (opcode == InstructionConstants.OP_LDC &&
                 cpIndex > 0xff)
        {
            opcode = InstructionConstants.OP_LDC_W;
        }
        else if (opcode == InstructionConstants.OP_LDC_W &&
                 cpIndex <= 0xff)
        {
            opcode = InstructionConstants.OP_LDC;
        }

        return this;
    }

    protected void readInfo(byte[] code, int offset)
    {
        int cpIndexSize  = cpIndexSize();
        int constantSize = constantSize();

        cpIndex  = readValue(code, offset, cpIndexSize);  offset += cpIndexSize;
        constant = readValue(code, offset, constantSize);
    }


    protected void writeInfo(byte[] code, int offset)
    {
        int cpIndexSize  = cpIndexSize();
        int constantSize = constantSize();

        writeValue(code, offset, cpIndex,  cpIndexSize);  offset += cpIndexSize;
        writeValue(code, offset, constant, constantSize);
    }


    public int length(int offset)
    {
        return 1 + cpIndexSize() + constantSize();
    }


    public void accept(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, InstructionVisitor instructionVisitor)
    {
        instructionVisitor.visitCpInstruction(classFile, methodInfo, codeAttrInfo, offset, this);
    }


    public String toString(int offset)
    {
        return "["+offset+"] "+getName()+" (cpIndex="+cpIndex+")";
    }


    public int stackPopCount(ClassFile classFile)
    {
        int stackPopCount = super.stackPopCount(classFile);

        // Some special cases.
        switch (opcode)
        {
            case InstructionConstants.OP_MULTIANEWARRAY:
                // For each dimension, an integer size is popped from the stack.
                stackPopCount += constant;
                break;

            case InstructionConstants.OP_PUTSTATIC:
            case InstructionConstants.OP_PUTFIELD:
                // The field value is be popped from the stack.
                classFile.constantPoolEntryAccept(cpIndex, this);
                stackPopCount += typeStackDelta;
                break;

            case InstructionConstants.OP_INVOKEVIRTUAL:
            case InstructionConstants.OP_INVOKESPECIAL:
            case InstructionConstants.OP_INVOKESTATIC:
            case InstructionConstants.OP_INVOKEINTERFACE:
                // The some parameters may be popped from the stack.
                classFile.constantPoolEntryAccept(cpIndex, this);
                stackPopCount += parameterStackDelta;
                break;
        }

        return stackPopCount;
    }


    public int stackPushCount(ClassFile classFile)
    {
        int stackPushCount = super.stackPushCount(classFile);

        // Some special cases.
        switch (opcode)
        {
            case InstructionConstants.OP_GETSTATIC:
            case InstructionConstants.OP_GETFIELD:
            case InstructionConstants.OP_INVOKEVIRTUAL:
            case InstructionConstants.OP_INVOKESPECIAL:
            case InstructionConstants.OP_INVOKESTATIC:
            case InstructionConstants.OP_INVOKEINTERFACE:
                // The field value or a return value may be pushed onto the stack.
                classFile.constantPoolEntryAccept(cpIndex, this);
                stackPushCount += typeStackDelta;
                break;
        }

        return stackPushCount;
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}
    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}


    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo)
    {
        String type = fieldrefCpInfo.getType(classFile);

        typeStackDelta = ClassUtil.internalTypeSize(ClassUtil.internalMethodReturnType(type));
    }

    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo)
    {
        visitRefCpInfo(classFile, interfaceMethodrefCpInfo);
    }

    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo)
    {
        visitRefCpInfo(classFile, methodrefCpInfo);
    }

    private void visitRefCpInfo(ClassFile classFile, RefCpInfo methodrefCpInfo)
    {
        String type = methodrefCpInfo.getType(classFile);

        parameterStackDelta = ClassUtil.internalMethodParameterSize(type);
        typeStackDelta      = ClassUtil.internalTypeSize(ClassUtil.internalMethodReturnType(type));
    }


    // Implementations for Object.

    public String toString()
    {
        return getName()+" (cpIndex="+cpIndex+")";
    }


    // Small utility methods.

    /**
     * Computes the appropriate constant pool index size for this instruction.
     */
    private int cpIndexSize()
    {
        return opcode == InstructionConstants.OP_LDC ? 1 :
                                                       2;
    }


    /**
     * Computes the appropriate constant size for this instruction.
     */
    private int constantSize()
    {
        return opcode == InstructionConstants.OP_MULTIANEWARRAY  ? 1 :
               opcode == InstructionConstants.OP_INVOKEINTERFACE ? 2 :
                                                                   0;
    }
}
