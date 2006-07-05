/* $Id: LibraryMethodInfo.java,v 1.16.2.1 2006/01/16 22:57:55 eric Exp $
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
 * Representation of a method from a class-file.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class LibraryMethodInfo extends LibraryMemberInfo implements MethodInfo
{
    /**
     * An extra field pointing to the ClassFile objects referenced in the
     * descriptor string. This field is filled out by the <code>{@link
     * proguard.classfile.util.ClassFileReferenceInitializer ClassFileReferenceInitializer}</code>.
     * References to primitive types are ignored.
     */
    public ClassFile[] referencedClassFiles;


    /**
     * Creates a new LibraryMethodInfo from the file format data in the DataInput stream.
     *
     * @throws IOException if class file is corrupt or incomplete
     */
    public static LibraryMethodInfo create(DataInput din, CpInfo[] constantPool) throws IOException
    {
        LibraryMethodInfo mi = new LibraryMethodInfo();
        mi.read(din, constantPool);
        return mi;
    }


    protected LibraryMethodInfo()
    {
    }


    /**
     * Accepts the given visitor.
     */
    public void accept(LibraryClassFile libraryClassFile, MemberInfoVisitor memberInfoVisitor)
    {
        memberInfoVisitor.visitLibraryMethodInfo(libraryClassFile, this);
    }
}
