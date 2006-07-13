/* $Id: CodeAttrInfo.java,v 1.3.2.1 2006/01/16 22:57:55 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 1999      Mark Welsh (markw@retrologic.com)
 * Copyright (c) 2002-2006 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.classfile.attribute;


import proguard.classfile.*;
import proguard.classfile.instruction.*;

import java.io.*;

/**
 * Representation of a code attribute.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class CodeAttrInfo extends AttrInfo
{
    private static final int CONSTANT_FIELD_SIZE = 12;


    public int             u2maxStack;
    public int             u2maxLocals;
    public int             u4codeLength;
    public byte[]          code;
    public int             u2exceptionTableLength;
    public ExceptionInfo[] exceptionTable;
    public int             u2attributesCount;
    public AttrInfo[]      attributes;


    protected CodeAttrInfo()
    {
    }


    /**
     * Returns the (first) attribute with the given name.
     */
    public AttrInfo getAttribute(ClassFile classFile, String name)
    {
        for (int i = 0; i < u2attributesCount; i++)
        {
            AttrInfo attribute = attributes[i];
            if (attribute.getAttributeName(classFile).equals(name))
            {
                return attribute;
            }
        }

        return null;
    }


    // Implementations for AttrInfo.

    protected int getLength()
    {
        int length = CONSTANT_FIELD_SIZE + u4codeLength +
                        u2exceptionTableLength * ExceptionInfo.CONSTANT_FIELD_SIZE;
        for (int i = 0; i < u2attributesCount; i++)
        {
            length += AttrInfo.CONSTANT_FIELD_SIZE + attributes[i].getLength();
        }

        return length;
    }

    protected void readInfo(DataInput din, ClassFile classFile) throws IOException
    {
        u2maxStack = din.readUnsignedShort();
        u2maxLocals = din.readUnsignedShort();
        u4codeLength = din.readInt();
        code = new byte[u4codeLength];
        din.readFully(code);
        u2exceptionTableLength = din.readUnsignedShort();
        exceptionTable = new ExceptionInfo[u2exceptionTableLength];
        for (int i = 0; i < u2exceptionTableLength; i++)
        {
            exceptionTable[i] = ExceptionInfo.create(din);
        }
        u2attributesCount = din.readUnsignedShort();
        attributes = new AttrInfo[u2attributesCount];
        for (int i = 0; i < u2attributesCount; i++)
        {
            attributes[i] = AttrInfo.create(din, classFile);
        }
    }

    protected void writeInfo(DataOutput dout) throws IOException
    {
        dout.writeShort(u2maxStack);
        dout.writeShort(u2maxLocals);
        dout.writeInt(u4codeLength);
        dout.write(code, 0, u4codeLength);
        dout.writeShort(u2exceptionTableLength);
        for (int i = 0; i < u2exceptionTableLength; i++)
        {
            exceptionTable[i].write(dout);
        }
        dout.writeShort(u2attributesCount);
        for (int i = 0; i < u2attributesCount; i++)
        {
            attributes[i].write(dout);
        }
    }

    public void accept(ClassFile classFile, AttrInfoVisitor attrInfoVisitor)
    {
        attrInfoVisitor.visitCodeAttrInfo(classFile, null, this);
    }

    public void accept(ClassFile classFile, MethodInfo methodInfo, AttrInfoVisitor attrInfoVisitor)
    {
        attrInfoVisitor.visitCodeAttrInfo(classFile, methodInfo, this);
    }


    /**
     * Applies the given instruction visitor to all instructions.
     */
    public void instructionsAccept(ClassFile classFile, MethodInfo methodInfo, InstructionVisitor instructionVisitor)
    {
        int offset = 0;

        do
        {
            // Note that the instruction is only volatile.
            Instruction instruction = InstructionFactory.create(code, offset);
            int length = instruction.length(offset);
            instruction.accept(classFile, methodInfo, this, offset, instructionVisitor);
            offset += length;
        }
        while (offset < u4codeLength);
    }

    /**
     * Applies the given instruction visitor to the given instruction.
     */
    public void instructionAccept(ClassFile classFile, MethodInfo methodInfo, InstructionVisitor instructionVisitor, int offset)
    {
        Instruction instruction = InstructionFactory.create(code, offset);
        instruction.accept(classFile, methodInfo, this, offset, instructionVisitor);
    }

    /**
     * Applies the given exception visitor to all exceptions.
     */
    public void exceptionsAccept(ClassFile classFile, MethodInfo methodInfo, ExceptionInfoVisitor exceptionInfoVisitor)
    {
        for (int i = 0; i < u2exceptionTableLength; i++)
        {
            // We don't need double dispatching here, since there is only one
            // type of ExceptionInfo.
            exceptionInfoVisitor.visitExceptionInfo(classFile, methodInfo, this, exceptionTable[i]);
        }
    }

    /**
     * Applies the given attribute visitor to all attributes.
     */
    public void attributesAccept(ClassFile classFile, MethodInfo methodInfo, AttrInfoVisitor attrInfoVisitor)
    {
        for (int i = 0; i < u2attributesCount; i++)
        {
            attributes[i].accept(classFile, methodInfo, this, attrInfoVisitor);
        }
    }
}
