/* $Id: NameAndTypeCpInfo.java,v 1.19 2004/10/23 16:53:01 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 1999      Mark Welsh (markw@retrologic.com)
 * Copyright (c) 2002-2004 Eric Lafortune (eric@graphics.cornell.edu)
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

import java.io.*;

/**
 * Representation of a 'name and type' entry in the ConstantPool.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class NameAndTypeCpInfo extends CpInfo implements Cloneable
{
    public int u2nameIndex;
    public int u2descriptorIndex;

    /**
     * An extra field pointing to the ClassFile objects referenced in the
     * descriptor string. This field is filled out by the <code>{@link
     * proguard.classfile.util.ClassFileReferenceInitializer ClassFileReferenceInitializer}</code>.
     * References to primitive types are ignored.
     */
    public ClassFile[] referencedClassFiles;


    protected NameAndTypeCpInfo()
    {
    }


    /**
     * Creates a new NameAndTypeCpInfo with the given name and type indices.
     * @param u2nameIndex          the index of the name in the constant pool.
     * @param u2descriptorIndex    the index of the descriptor in the constant
     *                             pool.
     * @param referencedClassFiles the list of class files referenced in the
     *                             descriptor string.
     */
    public NameAndTypeCpInfo(int         u2nameIndex,
                             int         u2descriptorIndex,
                             ClassFile[] referencedClassFiles)
    {
        this.u2nameIndex          = u2nameIndex;
        this.u2descriptorIndex    = u2descriptorIndex;
        this.referencedClassFiles = referencedClassFiles;
    }


    /**
     * Returns the name index.
     */
    protected int getNameIndex()
    {
        return u2nameIndex;
    }

    /**
     * Sets the name index.
     */
    protected void setNameIndex(int index)
    {
        u2nameIndex = index;
    }

    /**
     * Returns the descriptor index.
     */
    protected int getDescriptorIndex()
    {
        return u2descriptorIndex;
    }

    /**
     * Sets the descriptor index.
     */
    protected void setDescriptorIndex(int index)
    {
        u2descriptorIndex = index;
    }

    /**
     * Returns the name.
     */
    public String getName(ClassFile classFile)
    {
        return classFile.getCpString(u2nameIndex);
    }

    /**
     * Returns the type.
     */
    public String getType(ClassFile classFile)
    {
        return classFile.getCpString(u2descriptorIndex);
    }


    // Implementations for CpInfo.

    public int getTag()
    {
        return ClassConstants.CONSTANT_NameAndType;
    }

    protected void readInfo(DataInput din) throws IOException
    {
        u2nameIndex = din.readUnsignedShort();
        u2descriptorIndex = din.readUnsignedShort();
    }

    protected void writeInfo(DataOutput dout) throws IOException
    {
        dout.writeShort(u2nameIndex);
        dout.writeShort(u2descriptorIndex);
    }

    public void accept(ClassFile classFile, CpInfoVisitor cpInfoVisitor)
    {
        cpInfoVisitor.visitNameAndTypeCpInfo(classFile, this);
    }


    /**
     * Lets the ClassFile objects referenced in the descriptor string
     * accept the given visitor.
     */
    public void referencedClassesAccept(ClassFileVisitor classFileVisitor)
    {
        if (referencedClassFiles != null)
        {
            for (int i = 0; i < referencedClassFiles.length; i++)
            {
                if (referencedClassFiles[i] != null)
                {
                    referencedClassFiles[i].accept(classFileVisitor);
                }
            }
        }
    }
}
