/* $Id: ProgramMemberInfo.java,v 1.29.2.2 2006/02/08 00:04:25 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 1999      Mark Welsh (markw@retrologic.com)
 * Copyright (c) 2002-2006 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
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
package proguard.classfile;


import proguard.classfile.visitor.*;
import proguard.classfile.attribute.*;

import java.io.*;

/**
 * Representation of a field or method from a program class file.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
abstract public class ProgramMemberInfo implements MemberInfo
{
    public int        u2accessFlags;
    public int        u2nameIndex;
    public int        u2descriptorIndex;
    public int        u2attributesCount;
    public AttrInfo[] attributes;

    /**
     * An extra field in which visitors can store information.
     */
    public Object visitorInfo;


    protected ProgramMemberInfo() {}


    /**
     * Returns the line number range of the given class member as "m:n",
     * if it can find it, or <code>null</code> otherwise.
     */
    public String getLineNumberRange(ClassFile classFile)
    {
        CodeAttrInfo codeAttribute =
            (CodeAttrInfo)getAttribute(classFile, ClassConstants.ATTR_Code);
        if (codeAttribute == null)
        {
            return null;
        }

        LineNumberTableAttrInfo lineNumberTableAttribute =
            (LineNumberTableAttrInfo)codeAttribute.getAttribute(classFile,
                                                                ClassConstants.ATTR_LineNumberTable);
        if (lineNumberTableAttribute == null)
        {
            return null;
        }

        return "" +
               lineNumberTableAttribute.getLineNumber(0) +
               ":" +
               lineNumberTableAttribute.getLineNumber(Integer.MAX_VALUE);
    }


    /**
     * Returns the (first) attribute with the given name.
     */
    private AttrInfo getAttribute(ClassFile classFile, String name)
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


    /**
     * Accepts the given member info visitor.
     */
    public abstract void accept(ProgramClassFile  programClassFile,
                                MemberInfoVisitor memberInfoVisitor);



    /**
     * Lets the given attribute info visitor visit all the attributes of
     * this member info.
     */
    public abstract void attributesAccept(ProgramClassFile programClassFile,
                                          AttrInfoVisitor  attrInfoVisitor);


    /**
     * Imports the field or method data to internal representation.
     */
    protected void read(DataInput din, ClassFile cf) throws IOException
    {
        u2accessFlags = din.readUnsignedShort();
        u2nameIndex = din.readUnsignedShort();
        u2descriptorIndex = din.readUnsignedShort();
        u2attributesCount = din.readUnsignedShort();
        if (u2attributesCount > 0)
        {
            attributes = new AttrInfo[u2attributesCount];
            for (int i = 0; i < u2attributesCount; i++)
            {
                attributes[i] = AttrInfo.create(din, cf);
            }
        }
    }

    /**
     * Exports the representation to a DataOutput stream.
     */
    public void write(DataOutput dout) throws IOException
    {
        dout.writeShort(u2accessFlags);
        dout.writeShort(u2nameIndex);
        dout.writeShort(u2descriptorIndex);
        dout.writeShort(u2attributesCount);
        for (int i = 0; i < u2attributesCount; i++)
        {
            attributes[i].write(dout);
        }
    }


    // Implementations for MemberInfo.

    public int getAccessFlags()
    {
        return u2accessFlags;
    }

    public String getName(ClassFile classFile)
    {
        return classFile.getCpString(u2nameIndex);
    }

    public String getDescriptor(ClassFile classFile)
    {
        return classFile.getCpString(u2descriptorIndex);
    }

    public void accept(ClassFile classFile, MemberInfoVisitor memberInfoVisitor)
    {
        accept((ProgramClassFile)classFile, memberInfoVisitor);
    }


    // Implementations for VisitorAccepter.

    public Object getVisitorInfo()
    {
        return visitorInfo;
    }

    public void setVisitorInfo(Object visitorInfo)
    {
        this.visitorInfo = visitorInfo;
    }
}
