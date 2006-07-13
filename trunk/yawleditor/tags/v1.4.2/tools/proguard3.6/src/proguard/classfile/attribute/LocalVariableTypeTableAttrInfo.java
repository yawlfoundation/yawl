/* $Id: LocalVariableTypeTableAttrInfo.java,v 1.2.2.1 2006/01/16 22:57:55 eric Exp $
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

import java.io.*;

/**
 * Representation of a local variable table type attribute.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class LocalVariableTypeTableAttrInfo extends AttrInfo
{
    private static final int CONSTANT_FIELD_SIZE = 2;


    public int                     u2localVariableTypeTableLength;
    public LocalVariableTypeInfo[] localVariableTypeTable;


    protected LocalVariableTypeTableAttrInfo()
    {
    }


    /**
     * Returns the array of local variable type table entries.
     */
    protected LocalVariableTypeInfo[] getLocalVariableTypeTable() throws Exception
    {
        return localVariableTypeTable;
    }


    // Implementations for AttrInfo.

    protected int getLength()
    {
        return CONSTANT_FIELD_SIZE +
               u2localVariableTypeTableLength * LocalVariableTypeInfo.CONSTANT_FIELD_SIZE;
    }

    protected void readInfo(DataInput din, ClassFile classFile) throws IOException
    {
        u2localVariableTypeTableLength = din.readUnsignedShort();
        localVariableTypeTable = new LocalVariableTypeInfo[u2localVariableTypeTableLength];
        for (int i = 0; i < u2localVariableTypeTableLength; i++)
        {
            localVariableTypeTable[i] = LocalVariableTypeInfo.create(din);
        }
    }

    protected void writeInfo(DataOutput dout) throws IOException
    {
        dout.writeShort(u2localVariableTypeTableLength);
        for (int i = 0; i < u2localVariableTypeTableLength; i++)
        {
            localVariableTypeTable[i].write(dout);
        }
    }

    public void accept(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, AttrInfoVisitor attrInfoVisitor)
    {
        attrInfoVisitor.visitLocalVariableTypeTableAttrInfo(classFile, methodInfo, codeAttrInfo, this);
    }


    /**
     * Applies the given visitor to all local variable types.
     */
    public void localVariablesAccept(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeInfoVisitor localVariableTypeInfoVisitor)
    {
        for (int i = 0; i < u2localVariableTypeTableLength; i++)
        {
            // We don't need double dispatching here, since there is only one
            // type of LocalVariableTypeInfo.
            localVariableTypeInfoVisitor.visitLocalVariableTypeInfo(classFile, methodInfo, codeAttrInfo, localVariableTypeTable[i]);
        }
    }
}
