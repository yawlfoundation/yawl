/* $Id: ClassFileFinalizer.java,v 1.6 2004/12/19 21:03:13 eric Exp $
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
 * makes the class files it visits, and their class members, final, if possible.
 *
 * @author Eric Lafortune
 */
public class ClassFileFinalizer
  implements ClassFileVisitor,
             MemberInfoVisitor
{
    private MemberFinder memberFinder = new MemberFinder();


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

        // If the method is not final/abstract/private.
        // and it is not an initialization method,
        // and its class is final,
        //     or it is not being kept and it is not overridden,
        // then make it final.
        if ((programMethodInfo.u2accessFlags & (ClassConstants.INTERNAL_ACC_FINAL    |
                                                ClassConstants.INTERNAL_ACC_ABSTRACT |
                                                ClassConstants.INTERNAL_ACC_PRIVATE)) == 0 &&
            !name.equals(ClassConstants.INTERNAL_METHOD_NAME_CLINIT)                        &&
            !name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT)                          &&
            ((programClassFile.u2accessFlags & ClassConstants.INTERNAL_ACC_FINAL) != 0 ||
             (!KeepMarker.isKept(programMethodInfo) &&
              (programClassFile.subClasses == null ||
               !memberFinder.isOverriden(programClassFile, programMethodInfo)))))
        {
            programMethodInfo.u2accessFlags |= ClassConstants.INTERNAL_ACC_FINAL;
        }
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}
    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo) {}
}
