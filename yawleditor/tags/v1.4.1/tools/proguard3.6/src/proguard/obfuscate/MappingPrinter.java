/* $Id: MappingPrinter.java,v 1.19.2.1 2006/01/16 22:57:56 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2006 Eric Lafortune (eric@graphics.cornell.edu)
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
package proguard.obfuscate;

import proguard.classfile.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;

import java.io.*;


/**
 * This ClassFileVisitor prints out the renamed class files and class members with
 * their old names and new names.
 *
 * @see ClassFileRenamer
 *
 * @author Eric Lafortune
 */
public class MappingPrinter
  implements ClassFileVisitor,
             MemberInfoVisitor
{
    private PrintStream ps;


    /**
     * Creates a new MappingPrinter that prints to <code>System.out</code>.
     */
    public MappingPrinter()
    {
        this(System.out);
    }


    /**
     * Creates a new MappingPrinter that prints to the given stream.
     * @param printStream the stream to which to print
     */
    public MappingPrinter(PrintStream printStream)
    {
        this.ps = printStream;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        String name    = programClassFile.getName();
        String newName = ClassFileObfuscator.newClassName(programClassFile);

        ps.println(ClassUtil.externalClassName(name) +
                   " -> " +
                   ClassUtil.externalClassName(newName) +
                   ":");

        // Print out the class members.
        programClassFile.fieldsAccept(this);
        programClassFile.methodsAccept(this);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        String newName = MemberInfoObfuscator.newMemberName(programFieldInfo);
        if (newName != null)
        {
            ps.println("    " +
                       //lineNumberRange(programClassFile, programFieldInfo) +
                       ClassUtil.externalFullFieldDescription(
                           0,
                           programFieldInfo.getName(programClassFile),
                           programFieldInfo.getDescriptor(programClassFile)) +
                       " -> " +
                       newName);
        }
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        // Special cases: <clinit> and <init> are always kept unchanged.
        // We can ignore them here.
        String name = programMethodInfo.getName(programClassFile);
        if (name.equals(ClassConstants.INTERNAL_METHOD_NAME_CLINIT) ||
            name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
        {
            return;
        }

        String newName = MemberInfoObfuscator.newMemberName(programMethodInfo);
        if (newName != null)
        {
            ps.println("    " +
                       lineNumberRange(programClassFile, programMethodInfo) +
                       ClassUtil.externalFullMethodDescription(
                           programClassFile.getName(),
                           0,
                           programMethodInfo.getName(programClassFile),
                           programMethodInfo.getDescriptor(programClassFile)) +
                       " -> " +
                       newName);
        }
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}
    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo) {}


    // Small utility methods.

    /**
     * Returns the line number range of the given class member, followed by a
     * colon, or just an empty String if no range is available.
     */
    private static String lineNumberRange(ProgramClassFile programClassFile, ProgramMemberInfo programMemberInfo)
    {
        String range = programMemberInfo.getLineNumberRange(programClassFile);
        return range != null ?
            (range + ":") :
            "";
    }
}
