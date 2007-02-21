/* $Id: NonPrivateMethodMarker.java,v 1.7.2.1 2006/01/16 22:57:56 eric Exp $
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
package proguard.optimize;

import proguard.classfile.*;
import proguard.classfile.visitor.*;

/**
 * This ClassFileVisitor marks all methods that can not be made private in the
 * classes that it visits, and in the classes to which they refer.
 *
 * @author Eric Lafortune
 */
public class NonPrivateMethodMarker
  implements ClassFileVisitor,
             CpInfoVisitor,
             MemberInfoVisitor
{
    private MethodImplementationFilter filteredMethodMarker = new MethodImplementationFilter(this);


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Explicitly mark the <clinit> method.
        programClassFile.methodAccept(ClassConstants.INTERNAL_METHOD_NAME_CLINIT,
                                      ClassConstants.INTERNAL_METHOD_TYPE_CLINIT,
                                      this);

        // Explicitly mark the parameterless <init> method.
        programClassFile.methodAccept(ClassConstants.INTERNAL_METHOD_NAME_INIT,
                                      ClassConstants.INTERNAL_METHOD_TYPE_INIT,
                                      this);

        // Go over all referenced methods.
        programClassFile.constantPoolEntriesAccept(this);

        // Go over all methods that may have implementations.
        programClassFile.methodsAccept(filteredMethodMarker);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        // Go over all methods.
        libraryClassFile.methodsAccept(this);
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}
    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo) {}
    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}


    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo)
    {
        // Implementations of the referenced method can never be made private.
        interfaceMethodrefCpInfo.referencedMemberInfoAccept(this);
    }


    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo)
    {
        ClassFile referencedClassFile = methodrefCpInfo.referencedClassFile;

        // Is it refering to a method in another class file?
        if (referencedClassFile != null &&
            !referencedClassFile.equals(classFile))
        {
            // The referenced method can never be made private.
            methodrefCpInfo.referencedMemberInfoAccept(this);
        }
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo) {}


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        markCanNotBeMadePrivate(programMethodInfo);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        markCanNotBeMadePrivate(libraryMethodInfo);
    }


    // Small utility methods.

    public static void markCanNotBeMadePrivate(MethodInfo methodInfo)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(methodInfo);
        if (info != null)
        {
            info.setCanNotBeMadePrivate();
        }
    }


    public static boolean canBeMadePrivate(MethodInfo methodInfo)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(methodInfo);
        return info != null &&
               info.canBeMadePrivate();
    }
}
