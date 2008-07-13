/* $Id: EnclosingMethodAttrInfo.java,v 1.3.2.2 2007/01/18 21:31:51 eric Exp $
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

import proguard.classfile.*;
import proguard.classfile.visitor.*;

import java.io.*;

/**
 * Representation of an enclosing method attribute.
 *
 * @author Eric Lafortune
 */
public class EnclosingMethodAttrInfo extends AttrInfo
{
    private static final int CONSTANT_FIELD_SIZE = 4;


    public int u2classIndex;
    public int u2nameAndTypeIndex;

    /**
     * An extra field pointing to the referenced ClassFile object.
     * This field is typically filled out by the <code>{@link
     * ClassFileReferenceInitializer}</code>.
     */
    public ClassFile referencedClassFile;

    /**
     * An extra field optionally pointing to the referenced MethodInfo object.
     * This field is typically filled out by the <code>{@link
     * ClassFileReferenceInitializer}</code>.
     */
    public MethodInfo referencedMethodInfo;


    protected EnclosingMethodAttrInfo()
    {
    }


    /**
     * Returns the class name.
     */
    public String getClassName(ClassFile classFile)
    {
        return classFile.getCpClassNameString(u2classIndex);
    }

    /**
     * Returns the method/field name.
     */
    public String getName(ClassFile classFile)
    {
        return classFile.getCpNameString(u2nameAndTypeIndex);
    }

    /**
     * Returns the type.
     */
    public String getType(ClassFile classFile)
    {
        return classFile.getCpTypeString(u2nameAndTypeIndex);
    }


    /**
     * Lets the referenced class file accept the given visitor.
     */
    public void referencedClassAccept(ClassFileVisitor classFileVisitor)
    {
        if (referencedClassFile != null)
        {
            referencedClassFile.accept(classFileVisitor);
        }
    }


    /**
     * Lets the referenced class member accept the given visitor.
     */
    public void referencedMethodInfoAccept(MemberInfoVisitor memberInfoVisitor)
    {
        if (referencedMethodInfo != null)
        {
            referencedMethodInfo.accept(referencedClassFile,
                                        memberInfoVisitor);
        }
    }


    // Implementations for AttrInfo.

    protected int getLength()
    {
        return CONSTANT_FIELD_SIZE;
    }

    protected void readInfo(DataInput din, ClassFile classFile) throws IOException
    {
        u2classIndex       = din.readUnsignedShort();
        u2nameAndTypeIndex = din.readUnsignedShort();
    }

    protected void writeInfo(DataOutput dout) throws IOException
    {
        dout.writeShort(u2classIndex);
        dout.writeShort(u2nameAndTypeIndex);
    }

    public void accept(ClassFile classFile, AttrInfoVisitor attrInfoVisitor)
    {
        attrInfoVisitor.visitEnclosingMethodAttrInfo(classFile, this);
    }
}
