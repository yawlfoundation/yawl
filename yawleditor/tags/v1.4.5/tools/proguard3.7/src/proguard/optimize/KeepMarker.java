/* $Id: KeepMarker.java,v 1.7.2.3 2006/06/07 22:36:52 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2003 Eric Lafortune (eric@graphics.cornell.edu)
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
package proguard.optimize;

import proguard.classfile.*;
import proguard.classfile.util.MethodInfoLinker;
import proguard.classfile.visitor.*;


/**
 * This <code>ClassFileVisitor</code> and <code>MemberInfoVisitor</code>
 * marks class files and class members it visits. The marked elements
 * will remain unchanged as necessary in the optimization step.
 *
 * @author Eric Lafortune
 */
public class KeepMarker
  implements ClassFileVisitor,
             MemberInfoVisitor
{
    // A visitor info flag to indicate the visitor accepter is being kept.
    private static final Object KEPT = new Object();


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        markAsKept(programClassFile);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        markAsKept(libraryClassFile);
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        markAsKept(programFieldInfo);
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        markAsKept(MethodInfoLinker.lastMemberInfo(programMethodInfo));
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
        markAsKept(libraryFieldInfo);
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        markAsKept(MethodInfoLinker.lastMemberInfo(libraryMethodInfo));
    }


    // Small utility methods.

    public static void markAsKept(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(KEPT);
    }


    public static boolean isKept(VisitorAccepter visitorAccepter)
    {
        return MethodInfoLinker.lastVisitorAccepter(visitorAccepter).getVisitorInfo() == KEPT;
    }
}
