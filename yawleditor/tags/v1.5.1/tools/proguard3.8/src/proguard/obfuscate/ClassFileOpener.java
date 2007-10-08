/* $Id: ClassFileOpener.java,v 1.2.2.2 2007/01/18 21:31:52 eric Exp $
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
package proguard.obfuscate;

import proguard.classfile.*;
import proguard.classfile.editor.ConstantPoolEditor;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;

/**
 * This <code>ClassFileVisitor</code> makes package visible classes and class
 * members public.
 *
 * @author Eric Lafortune
 */
public class ClassFileOpener
  implements ClassFileVisitor,
             MemberInfoVisitor
{
    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Make the class public, if it is package visible.
        makePackageVisible(programClassFile);

        // Make package visible class members public.
        programClassFile.fieldsAccept(this);
        programClassFile.methodsAccept(this);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile) {}


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        makePackageVisible(programFieldInfo);
    }

    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        makePackageVisible(programMethodInfo);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}
    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo) {}


    // Small utility methods.

    /**
     * Makes the given class file public, if it is package visible.
     */
    private void makePackageVisible(ProgramClassFile programClassFile)
    {
        if (isPackageVisible(programClassFile.u2accessFlags))
        {
            programClassFile.u2accessFlags = makePublic(programClassFile.u2accessFlags);
        }
    }

    /**
     * Makes the given class member public, if it is package visible.
     */
    private void makePackageVisible(ProgramMemberInfo programMemberInfo)
    {
        if (isPackageVisible(programMemberInfo.u2accessFlags))
        {
            programMemberInfo.u2accessFlags = makePublic(programMemberInfo.u2accessFlags);
        }
    }


    /**
     * Returns whether the given access flags specify a package visible class
     * or class member (including public or protected access).
     */
    private boolean isPackageVisible(int accessFlags)
    {
        return AccessUtil.accessLevel(accessFlags) >= AccessUtil.PACKAGE_VISIBLE;
    }


    /**
     * Returns the given access flags, modified such that the class or class
     * member becomes public.
     */
    private int makePublic(int accessFlags)
    {
        return AccessUtil.replaceAccessFlags(accessFlags,
                                             ClassConstants.INTERNAL_ACC_PUBLIC);
    }
}
