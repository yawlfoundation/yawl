/* $Id: MappingPrinter.java,v 1.15 2004/08/15 12:39:30 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2004 Eric Lafortune (eric@graphics.cornell.edu)
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

    // A field to remember the class name, if a header is needed for class members.
    private String className;


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
        className = programClassFile.getName();

        String newClassName = ClassFileObfuscator.newClassName(programClassFile);

        if (newClassName != null)
        {
            ps.println(ClassUtil.externalClassName(className) +
                       " -> " +
                       ClassUtil.externalClassName(newClassName) +
                       ":");

            className = null;
        }

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
        String newMemberName = MemberInfoObfuscator.newMemberName(programFieldInfo);

        if (newMemberName != null)
        {
            printClassNameHeader();

            ps.println("    " +
                       lineNumberRange(programClassFile, programFieldInfo) +
                       ClassUtil.externalFullFieldDescription(
                           0,
                           programFieldInfo.getName(programClassFile),
                           programFieldInfo.getDescriptor(programClassFile)) +
                       " -> " +
                       newMemberName);
        }
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
      String newMemberName = MemberInfoObfuscator.newMemberName(programMethodInfo);

      if (newMemberName != null)
        {
            printClassNameHeader();

            ps.println("    " +
                       lineNumberRange(programClassFile, programMethodInfo) +
                       ClassUtil.externalFullMethodDescription(
                       programClassFile.getName(),
                       0,
                       programMethodInfo.getName(programClassFile),
                       programMethodInfo.getDescriptor(programClassFile)) +
                       " -> " +
                       newMemberName);
        }
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}
    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo) {}


    // Small utility methods.

    /**
     * Prints the class name field. The field is then cleared, so it is not
     * printed again.
     */
    private void printClassNameHeader()
    {
        if (className != null)
        {
            ps.println(ClassUtil.externalClassName(className) + ":");
            className = null;
        }
    }


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
