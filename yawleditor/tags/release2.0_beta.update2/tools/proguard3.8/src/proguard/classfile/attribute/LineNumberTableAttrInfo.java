/* $Id: LineNumberTableAttrInfo.java,v 1.2.2.2 2007/01/18 21:31:51 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 1999      Mark Welsh (markw@retrologic.com)
 * Copyright (c) 2002-2007 Eric Lafortune (eric@graphics.cornell.edu)
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

import proguard.classfile.visitor.*;
import proguard.classfile.attribute.*;
import proguard.classfile.*;

import java.io.*;

/**
 * Representation of a line number table attribute.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class LineNumberTableAttrInfo extends AttrInfo
{
    private static final int CONSTANT_FIELD_SIZE = 2;


    public int              u2lineNumberTableLength;
    public LineNumberInfo[] lineNumberTable;


    protected LineNumberTableAttrInfo()
    {
    }


    /**
     * Returns the line number corresponding to the given byte code program
     * counter.
     */
    public int getLineNumber(int pc)
    {
        for (int i = u2lineNumberTableLength-1 ; i >= 0 ; i--)
        {
            LineNumberInfo info = lineNumberTable[i];
            if (pc >= info.u2startpc)
            {
                return info.u2lineNumber;
            }
        }

        return u2lineNumberTableLength > 0 ?
            lineNumberTable[0].u2lineNumber :
            0;
    }


    // Implementations for AttrInfo.

    protected int getLength()
    {
        return CONSTANT_FIELD_SIZE +
               u2lineNumberTableLength * LineNumberInfo.CONSTANT_FIELD_SIZE;
    }

    protected void readInfo(DataInput din, ClassFile classFile) throws IOException
    {
        u2lineNumberTableLength = din.readUnsignedShort();
        lineNumberTable = new LineNumberInfo[u2lineNumberTableLength];
        for (int i = 0; i < u2lineNumberTableLength; i++)
        {
            lineNumberTable[i] = LineNumberInfo.create(din);
        }
    }

    protected void writeInfo(DataOutput dout) throws IOException
    {
        dout.writeShort(u2lineNumberTableLength);
        for (int i = 0; i < u2lineNumberTableLength; i++)
        {
            lineNumberTable[i].write(dout);
        }
    }

    public void accept(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, AttrInfoVisitor attrInfoVisitor)
    {
        attrInfoVisitor.visitLineNumberTableAttrInfo(classFile, methodInfo, codeAttrInfo, this);
    }


    /**
     * Applies the given visitor to all line numbers.
     */
    public void lineNumbersAccept(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LineNumberInfoVisitor lineNumberInfoVisitor)
    {
        for (int i = 0; i < u2lineNumberTableLength; i++)
        {
            // We don't need double dispatching here, since there is only one
            // type of LineNumberInfo.
            lineNumberInfoVisitor.visitLineNumberInfo(classFile, methodInfo, codeAttrInfo, lineNumberTable[i]);
        }
    }
}
