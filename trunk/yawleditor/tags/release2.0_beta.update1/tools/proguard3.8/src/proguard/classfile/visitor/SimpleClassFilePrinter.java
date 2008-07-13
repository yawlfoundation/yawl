/* $Id: SimpleClassFilePrinter.java,v 1.18.2.2 2007/01/18 21:31:52 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2007 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.classfile.visitor;

import proguard.classfile.*;
import proguard.classfile.util.*;
import proguard.optimize.*;
import proguard.optimize.SideEffectMethodMarker;

import java.io.*;


/**
 * This <code>ClassFileVisitor</code> and <code>MemberInfoVisitor</code>
 * prints out the class names of the class files it visits, and the full class
 * member descriptions of the class members it visits. The names are printed
 * in a readable, Java-like format. The access modifiers can be included or not.
 *
 * @author Eric Lafortune
 */
public class SimpleClassFilePrinter
  implements ClassFileVisitor,
             MemberInfoVisitor
{
    private boolean     printAccessModifiers;
    private PrintStream ps;


    /**
     * Creates a new SimpleClassFilePrinter that prints to
     * <code>System.out</code>, including the access modifiers.
     */
    public SimpleClassFilePrinter()
    {
        this(true);
    }

    /**
     * Creates a new SimpleClassFilePrinter that prints to
     * <code>System.out</code>, with or without the access modifiers.
     */
    public SimpleClassFilePrinter(boolean printAccessModifiers)
    {
        this(printAccessModifiers, System.out);
    }

    /**
     * Creates a new SimpleClassFilePrinter that prints to the given
     * <code>PrintStream</code>, with or without the access modifiers.
     */
    public SimpleClassFilePrinter(boolean     printAccessModifiers,
                                  PrintStream printStream)
    {
        this.printAccessModifiers = printAccessModifiers;
        this.ps                   = printStream;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        ps.println(ClassUtil.externalFullClassDescription(
                       printAccessModifiers ?
                           programClassFile.getAccessFlags() :
                           0,
                       programClassFile.getName()));
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        ps.println(ClassUtil.externalFullClassDescription(
                       printAccessModifiers ?
                           libraryClassFile.getAccessFlags() :
                           0,
                       libraryClassFile.getName()));
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        ps.println(ClassUtil.externalFullClassDescription(
                       printAccessModifiers ?
                           programClassFile.getAccessFlags() :
                           0,
                       programClassFile.getName()) +
                   ": " +
                   ClassUtil.externalFullFieldDescription(
                       printAccessModifiers ?
                           programFieldInfo.getAccessFlags() :
                           0,
                       programFieldInfo.getName(programClassFile),
                       programFieldInfo.getDescriptor(programClassFile)));
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        ps.println(ClassUtil.externalFullClassDescription(
                       printAccessModifiers ?
                           programClassFile.getAccessFlags() :
                           0,
                       programClassFile.getName()) +
                   ": " +
                   ClassUtil.externalFullMethodDescription(
                       programClassFile.getName(),
                       printAccessModifiers ?
                           programMethodInfo.getAccessFlags() :
                           0,
                       programMethodInfo.getName(programClassFile),
                       programMethodInfo.getDescriptor(programClassFile)));
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
        ps.println(ClassUtil.externalFullClassDescription(
                       printAccessModifiers ?
                           libraryClassFile.getAccessFlags() :
                           0,
                       libraryClassFile.getName()) +
                   ": " +
                   ClassUtil.externalFullFieldDescription(
                       printAccessModifiers ?
                           libraryFieldInfo.getAccessFlags() :
                           0,
                       libraryFieldInfo.getName(libraryClassFile),
                       libraryFieldInfo.getDescriptor(libraryClassFile)));
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        ps.println(ClassUtil.externalFullClassDescription(
                       printAccessModifiers ?
                           libraryClassFile.getAccessFlags() :
                           0,
                       libraryClassFile.getName()) +
                   ": " +
                   ClassUtil.externalFullMethodDescription(
                       libraryClassFile.getName(),
                       printAccessModifiers ?
                           libraryMethodInfo.getAccessFlags() :
                           0,
                       libraryMethodInfo.getName(libraryClassFile),
                       libraryMethodInfo.getDescriptor(libraryClassFile)));
    }
}
