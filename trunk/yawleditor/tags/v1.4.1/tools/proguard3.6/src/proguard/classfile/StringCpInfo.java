/* $Id: StringCpInfo.java,v 1.18.2.1 2006/01/16 22:57:55 eric Exp $
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

import java.io.*;

/**
 * Representation of a 'string' entry in the ConstantPool.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class StringCpInfo extends CpInfo
{
    public int u2stringIndex;

    /**
     * An extra field pointing to the referenced ClassFile object, if this
     * string is being used in Class.forName() or .class constructs.
     * This field is filled out by the <code>{@link
     * proguard.classfile.util.ClassFileReferenceInitializer ClassFileReferenceInitializer}</code>.
     * References to library class files are not filled out.
     */
    public ClassFile referencedClassFile;


    protected StringCpInfo()
    {
    }


    /**
     * Creates a new StringCpInfo with the given string index.
     * @param u2nameIndex         the index of the string in the constant pool.
     * @param referencedClassFile the referenced class file, if any.
     */
    public StringCpInfo(int       u2stringIndex,
                        ClassFile referencedClassFile)
    {
        this.u2stringIndex       = u2stringIndex;
        this.referencedClassFile = referencedClassFile;
    }


    /**
     * Returns the string value.
     */
    public String getString(ClassFile classFile)
    {
        return classFile.getCpString(u2stringIndex);
    }


    // Implementations for CpInfo.

    public int getTag()
    {
        return ClassConstants.CONSTANT_String;
    }

    protected void readInfo(DataInput din) throws IOException
    {
        u2stringIndex = din.readUnsignedShort();
    }

    protected void writeInfo(DataOutput dout) throws IOException
    {
        dout.writeShort(u2stringIndex);
    }

    public void accept(ClassFile classFile, CpInfoVisitor cpInfoVisitor)
    {
        cpInfoVisitor.visitStringCpInfo(classFile, this);
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
