/* $Id: ClassFileFinalizer.java,v 1.8.2.1 2006/02/13 00:20:43 eric Exp $
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
package proguard.optimize.peephole;

import proguard.classfile.*;
import proguard.classfile.util.MemberFinder;
import proguard.classfile.visitor.*;
import proguard.optimize.*;

/**
 * This <code>ClassFileVisitor</code> and <code>MemberInfoVisitor</code>
 * makes the class files it visits, and their methods, final, if possible.
 *
 * @author Eric Lafortune
 */
public class ClassFileFinalizer
  implements ClassFileVisitor,
             MemberInfoVisitor
{
    private ClassFileVisitor  extraClassFileVisitor;
    private MemberInfoVisitor extraMemberInfoVisitor;

    private MemberFinder memberFinder = new MemberFinder();


    /**
     * Creates a new ClassFileFinalizer.
     */
    public ClassFileFinalizer()
    {
        this(null, null);
    }


    /**
     * Creates a new ClassFileFinalizer.
     * @param extraClassFileVisitor  an optional extra visitor for all finalized
     *                               class files.
     * @param extraMemberInfoVisitor an optional extra visitor for all finalized
     *                               methods.
     */
    public ClassFileFinalizer(ClassFileVisitor  extraClassFileVisitor,
                              MemberInfoVisitor extraMemberInfoVisitor)
    {
        this.extraClassFileVisitor  = extraClassFileVisitor;
        this.extraMemberInfoVisitor = extraMemberInfoVisitor;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // If the class is not final/interface/abstract,
        // and it is not being kept,
        // then make it final.
        if ((programClassFile.u2accessFlags & (ClassConstants.INTERNAL_ACC_FINAL     |
                                               ClassConstants.INTERNAL_ACC_INTERFACE |
                                               ClassConstants.INTERNAL_ACC_ABSTRACT)) == 0 &&
            !KeepMarker.isKept(programClassFile)                                           &&
            programClassFile.subClasses == null)
        {
            programClassFile.u2accessFlags |= ClassConstants.INTERNAL_ACC_FINAL;

            // Visit the class file, if required.
            if (extraClassFileVisitor != null)
            {
                extraClassFileVisitor.visitProgramClassFile(programClassFile);
            }
        }

        // Check all methods.
        programClassFile.methodsAccept(this);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile) {}


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo) {}


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        String name = programMethodInfo.getName(programClassFile);

        // If the method is not already private/static/final/abstract,
        // and it is not a constructor,
        // and its class is final,
        //     or it is not being kept and it is not overridden,
        // then make it final.
        if ((programMethodInfo.u2accessFlags & (ClassConstants.INTERNAL_ACC_PRIVATE |
                                                ClassConstants.INTERNAL_ACC_STATIC  |
                                                ClassConstants.INTERNAL_ACC_FINAL   |
                                                ClassConstants.INTERNAL_ACC_ABSTRACT)) == 0 &&
            !name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT)                         &&
            ((programClassFile.u2accessFlags & ClassConstants.INTERNAL_ACC_FINAL) != 0 ||
             (!KeepMarker.isKept(programMethodInfo) &&
              (programClassFile.subClasses == null ||
               !memberFinder.isOverriden(programClassFile, programMethodInfo)))))
        {
            programMethodInfo.u2accessFlags |= ClassConstants.INTERNAL_ACC_FINAL;

            // Visit the method, if required.
            if (extraMemberInfoVisitor != null)
            {
                extraMemberInfoVisitor.visitProgramMethodInfo(programClassFile, programMethodInfo);
            }
        }
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}
    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo) {}
}
