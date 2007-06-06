/* $Id: ShortestUsagePrinter.java,v 1.3.2.2 2007/01/18 21:31:53 eric Exp $
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
package proguard.shrink;

import proguard.classfile.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;

import java.io.*;


/**
 * This ClassFileVisitor and MemberInfoVisitor prints out the reasons why
 * class files and class members have been marked as being used.
 *
 * @see UsageMarker
 *
 * @author Eric Lafortune
 */
public class ShortestUsagePrinter
  implements ClassFileVisitor,
             MemberInfoVisitor
{
    private ShortestUsageMarker shortestUsageMarker;
    private boolean             verbose;
    private PrintStream         ps;


    /**
     * Creates a new UsagePrinter that prints verbosely to <code>System.out</code>.
     * @param shortestUsageMarker the usage marker that was used to mark the
     *                            classes and class members.
     */
    public ShortestUsagePrinter(ShortestUsageMarker shortestUsageMarker)
    {
        this(shortestUsageMarker, true);
    }


    /**
     * Creates a new UsagePrinter that prints to the given stream.
     * @param shortestUsageMarker the usage marker that was used to mark the
     *                            classes and class members.
     * @param verbose             specifies whether the output should be verbose.
     */
    public ShortestUsagePrinter(ShortestUsageMarker shortestUsageMarker,
                                boolean             verbose)
    {
        this(shortestUsageMarker, verbose, System.out);
    }

    /**
     * Creates a new UsagePrinter that prints to the given stream.
     * @param shortestUsageMarker the usage marker that was used to mark the
     *                            classes and class members.
     * @param verbose             specifies whether the output should be verbose.
     * @param printStream         the stream to which to print.
     */
    public ShortestUsagePrinter(ShortestUsageMarker shortestUsageMarker,
                                boolean             verbose,
                                PrintStream         printStream)
    {
        this.shortestUsageMarker = shortestUsageMarker;
        this.verbose             = verbose;
        this.ps                  = printStream;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Print the name of this class file.
        ps.println(ClassUtil.externalClassName(programClassFile.getName()));

        // Print the reason for keeping this class file.
        printReason(programClassFile);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        // Print the name of this class file.
        ps.println(ClassUtil.externalClassName(libraryClassFile.getName()));

        // Print the reason for keeping this class file.
        ps.println("  is a library class.\n");
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        // Print the name of this field.
        String name = programFieldInfo.getName(programClassFile);
        String type = programFieldInfo.getDescriptor(programClassFile);

        ps.println(ClassUtil.externalClassName(programClassFile.getName()) +
                   (verbose ?
                        ": " + ClassUtil.externalFullFieldDescription(0, name, type):
                        "."  + name) +
                   lineNumberRange(programClassFile, programFieldInfo));

        // Print the reason for keeping this method.
        printReason(programFieldInfo);
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        // Print the name of this method.
        String name = programMethodInfo.getName(programClassFile);
        String type = programMethodInfo.getDescriptor(programClassFile);

        ps.println(ClassUtil.externalClassName(programClassFile.getName()) +
                   (verbose ?
                        ": " + ClassUtil.externalFullMethodDescription(programClassFile.getName(), 0, name, type):
                        "."  + name) +
                   lineNumberRange(programClassFile, programMethodInfo));

        // Print the reason for keeping this method.
        printReason(programMethodInfo);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
        // Print the name of this field.
        String name = libraryFieldInfo.getName(libraryClassFile);
        String type = libraryFieldInfo.getDescriptor(libraryClassFile);

        ps.println(ClassUtil.externalClassName(libraryClassFile.getName()) +
                   (verbose ?
                        ": " + ClassUtil.externalFullFieldDescription(0, name, type):
                        "."  + name));

        // Print the reason for keeping this field.
        ps.println("  is a library field.\n");
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        // Print the name of this method.
        String name = libraryMethodInfo.getName(libraryClassFile);
        String type = libraryMethodInfo.getDescriptor(libraryClassFile);

        ps.println(ClassUtil.externalClassName(libraryClassFile.getName()) +
                   (verbose ?
                        ": " + ClassUtil.externalFullMethodDescription(libraryClassFile.getName(), 0, name, type):
                        "."  + name));

        // Print the reason for keeping this method.
        ps.println("  is a library method.\n");
    }


    // Small utility methods.

    private void printReason(VisitorAccepter visitorAccepter)
    {
        if (shortestUsageMarker.isUsed(visitorAccepter))
        {
            ShortestUsageMark shortestUsageMark = shortestUsageMarker.getShortestUsageMark(visitorAccepter);

            // Print the reason for keeping this class file.
            ps.print("  " + shortestUsageMark.getReason());

            // Print the class or method that is responsible, with its reasons.
            shortestUsageMark.acceptClassFileVisitor(this);
            shortestUsageMark.acceptMethodInfoVisitor(this);
        }
        else
        {
            ps.println("  is not being kept.\n");
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
            (" (" + range + ")") :
            "";
    }
}
