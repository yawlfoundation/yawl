/* $Id: LibraryMemberInfo.java,v 1.22.2.2 2007/01/18 21:31:51 eric Exp $
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
import proguard.classfile.attribute.*;

import java.io.*;

/**
 * Representation of a field or method from a library class file.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
abstract public class LibraryMemberInfo implements MemberInfo
{
    private static final int ACC_VISIBLE = ClassConstants.INTERNAL_ACC_PUBLIC |
                                           ClassConstants.INTERNAL_ACC_PROTECTED;


    public int    u2accessFlags;
    public String name;
    public String descriptor;

    /**
     * An extra field in which visitors can store information.
     */
    public Object visitorInfo;


    protected LibraryMemberInfo() {}


    /**
     * Accepts the given member info visitor.
     */
    public abstract void accept(LibraryClassFile  libraryClassFile,
                                MemberInfoVisitor memberInfoVisitor);


    /**
     * Imports the field or method data to internal representation.
     */
    protected void read(DataInput din, CpInfo[] constantPool) throws IOException
    {
        // Read the access flags.
        u2accessFlags = din.readUnsignedShort();

        // Read the name and descriptor indices.
        int u2nameIndex       = din.readUnsignedShort();
        int u2descriptorIndex = din.readUnsignedShort();

        // Store the actual name and descriptor.
        name       = ((Utf8CpInfo)constantPool[u2nameIndex]).getString();
        descriptor = ((Utf8CpInfo)constantPool[u2descriptorIndex]).getString();

        // Skip the attributes.
        int u2attributesCount = din.readUnsignedShort();
        for (int i = 0; i < u2attributesCount; i++)
        {
            LibraryAttrInfo.skip(din);
        }
    }


    /**
     * Returns whether this library member is visible to the outside world.
     */
    boolean isVisible()
    {
        return (u2accessFlags & ACC_VISIBLE) != 0;
    }


    // Implementations for MemberInfo.

    public int getAccessFlags()
    {
        return u2accessFlags;
    }

    public String getName(ClassFile classFile)
    {
        return name;
    }

    public String getDescriptor(ClassFile classFile)
    {
        return descriptor;
    }

    public void accept(ClassFile classFile, MemberInfoVisitor memberInfoVisitor)
    {
        accept((LibraryClassFile)classFile, memberInfoVisitor);
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
