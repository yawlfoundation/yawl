/* $Id: ClassCpInfo.java,v 1.21.2.2 2007/01/18 21:31:51 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 1999      Mark Welsh (markw@retrologic.com)
 * Copyright (c) 2002-2007 Eric Lafortune (eric@graphics.cornell.edu)
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
 * Representation of a 'class' entry in the ConstantPool.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class ClassCpInfo extends CpInfo
{
    public int u2nameIndex;

    /**
     * An extra field pointing to the referenced ClassFile object.
     * This field is filled out by the <code>{@link
     * proguard.classfile.util.ClassFileReferenceInitializer ClassFileReferenceInitializer}</code>.
     */
    public ClassFile referencedClassFile;


    protected ClassCpInfo()
    {
    }


    /**
     * Creates a new ClassCpInfo with the given name index.
     * @param u2nameIndex         the index of the name in the constant pool.
     * @param referencedClassFile the referenced class file.
     */
    public ClassCpInfo(int       u2nameIndex,
                       ClassFile referencedClassFile)
    {
        this.u2nameIndex         = u2nameIndex;
        this.referencedClassFile = referencedClassFile;
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
     * Returns the name.
     */
    public String getName(ClassFile classFile)
    {
        return classFile.getCpString(u2nameIndex);
    }


    // Implementations for CpInfo.

    public int getTag()
    {
        return ClassConstants.CONSTANT_Class;
    }

    protected void readInfo(DataInput din) throws IOException
    {
        u2nameIndex = din.readUnsignedShort();
    }

    protected void writeInfo(DataOutput dout) throws IOException
    {
        dout.writeShort(u2nameIndex);
    }

    public void accept(ClassFile classFile, CpInfoVisitor cpInfoVisitor)
    {
        cpInfoVisitor.visitClassCpInfo(classFile, this);
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
}
