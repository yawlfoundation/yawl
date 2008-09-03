/* $Id: NameMarker.java,v 1.17.2.3 2007/01/18 21:31:52 eric Exp $
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
import proguard.classfile.visitor.*;


/**
 * This <code>ClassFileVisitor</code> and <code>MemberInfoVisitor</code>
 * marks names of the class files and class members it visits. The marked names
 * will remain unchanged in the obfuscation step.
 *
 * @see ClassFileObfuscator
 * @see MemberInfoObfuscator
 *
 * @author Eric Lafortune
 */
public class NameMarker
  implements ClassFileVisitor,
             MemberInfoVisitor
{
    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        keepClassName(programClassFile);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        keepClassName(libraryClassFile);
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        keepFieldName(programClassFile, programFieldInfo);
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        keepMethodName(programClassFile, programMethodInfo);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
        keepFieldName(libraryClassFile, libraryFieldInfo);
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        keepMethodName(libraryClassFile, libraryMethodInfo);
    }


    // Small utility method.

    /**
     * Ensures the name of the given class name will be kept.
     */
    public void keepClassName(ClassFile classFile)
    {
        ClassFileObfuscator.setNewClassName(classFile,
                                            classFile.getName());
    }


    /**
     * Ensures the name of the given field name will be kept.
     */
    private void keepFieldName(ClassFile classFile, FieldInfo fieldInfo)
    {
        MemberInfoObfuscator.setFixedNewMemberName(fieldInfo,
                                                   fieldInfo.getName(classFile));
    }


    /**
     * Ensures the name of the given method name will be kept.
     */
    private void keepMethodName(ClassFile classFile, MethodInfo methodInfo)
    {
        String name = methodInfo.getName(classFile);

        if (!name.equals(ClassConstants.INTERNAL_METHOD_NAME_CLINIT) &&
            !name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
        {
            MemberInfoObfuscator.setFixedNewMemberName(methodInfo,
                                                       methodInfo.getName(classFile));
        }
    }
}
