/* $Id: MemberFinder.java,v 1.7.2.4 2006/03/26 14:21:36 eric Exp $
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
package proguard.classfile.util;

import proguard.classfile.*;
import proguard.classfile.visitor.*;

/**
 * This class provides methods to find class members in a given class or in its
 * hierarchy.
 *
 * @author Eric Lafortune
 */
public class MemberFinder
  implements MemberInfoVisitor
{
    private static class MemberFoundException extends IllegalArgumentException {};
    private static final MemberFoundException MEMBER_FOUND = new MemberFoundException();

    private ClassFile  classFile;
    private MemberInfo memberInfo;


    /**
     * Finds the field with the given name and descriptor in the given
     * class file or its hierarchy.
     */
    public FieldInfo findField(ClassFile referencingClassFile,
                               ClassFile classFile,
                               String    name,
                               String    descriptor)
    {
        return (FieldInfo)findMember(referencingClassFile, classFile, name, descriptor, true);
    }


    /**
     * Finds the method with the given name and descriptor in the given
     * class file or its hierarchy.
     */
    public MethodInfo findMethod(ClassFile referencingClassFile,
                                 ClassFile classFile,
                                 String    name,
                                 String    descriptor)
    {
        return (MethodInfo)findMember(referencingClassFile, classFile, name, descriptor, false);
    }


    /**
     * Finds the class member with the given name and descriptor in the given
     * class file or its hierarchy.
     */
    public MemberInfo findMember(ClassFile referencingClassFile,
                                 ClassFile classFile,
                                 String    name,
                                 String    descriptor,
                                 boolean   isField)
    {
        // Organize a search in the hierarchy of superclasses and interfaces.
        // The class member may be in a different class, if the code was
        // compiled with "-target 1.2" or higher (the default in JDK 1.4).
        try
        {
            this.classFile  = null;
            this.memberInfo = null;
            classFile.hierarchyAccept(true, true, true, false, isField ?
                (ClassFileVisitor)new NamedFieldVisitor(name, descriptor,
                                  new MemberInfoClassFileAccessFilter(referencingClassFile, this)) :
                (ClassFileVisitor)new NamedMethodVisitor(name, descriptor,
                                  new MemberInfoClassFileAccessFilter(referencingClassFile, this)));
        }
        catch (MemberFoundException ex)
        {
        }

        return memberInfo;
    }


    /**
     * Returns the corresponding class file of the most recently found class
     * member.
     */
    public ClassFile correspondingClassFile()
    {
        return classFile;
    }


    /**
     * Returns whether the given method is overridden anywhere down the class
     * hierarchy.
     */
    public boolean isOverriden(ClassFile  classFile,
                               MethodInfo methodInfo)
    {
        String name       = methodInfo.getName(classFile);
        String descriptor = methodInfo.getDescriptor(classFile);

        // Go looking for the method down the class hierarchy.
        try
        {
            this.classFile  = null;
            this.memberInfo = null;
            classFile.hierarchyAccept(false, false, false, true,
                                      new NamedMethodVisitor(name, descriptor,
                                      new MemberInfoAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE, this)));
        }
        catch (MemberFoundException ex)
        {
            // We've found an overriding method.
            return true;
        }

        return false;
    }


    /**
     * Returns whether the given field is shadowed anywhere down the class
     * hierarchy.
     */
    public boolean isShadowed(ClassFile classFile,
                              FieldInfo fieldInfo)
    {
        String name       = fieldInfo.getName(classFile);
        String descriptor = fieldInfo.getDescriptor(classFile);

        // Go looking for the method down the class hierarchy.
        try
        {
            this.classFile  = null;
            this.memberInfo = null;
            classFile.hierarchyAccept(false, false, false, true,
                                      new NamedFieldVisitor(name, descriptor,
                                      new MemberInfoAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE, this)));
        }
        catch (MemberFoundException ex)
        {
            // We've found an overriding method.
            return true;
        }

        return false;
    }


//    // Implementations for ClassFileVisitor.
//
//    public void visitProgramClassFile(ProgramClassFile programClassFile)
//    {
//        visitClassFile(programClassFile);
//    }
//
//
//    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
//    {
//        visitClassFile(libraryClassFile);
//    }
//
//
//    private void visitClassFile(ClassFile classFile)
//    {
//        if (memberInfo == null)
//        {
//            memberInfo = isField ?
//                (MemberInfo)classFile.findField(name, descriptor) :
//                (MemberInfo)classFile.findMethod(name, descriptor);
//
//            if (memberInfo != null)
//            {
//                this.classFile = classFile;
//            }
//        }
//    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        visitMemberInfo(programClassFile, programFieldInfo);
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        visitMemberInfo(programClassFile, programMethodInfo);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
        visitMemberInfo(libraryClassFile, libraryFieldInfo);
    }

    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        visitMemberInfo(libraryClassFile, libraryMethodInfo);
    }


    private void visitMemberInfo(ClassFile classFile, MemberInfo memberInfo)
    {
        this.classFile  = classFile;
        this.memberInfo = memberInfo;

        throw MEMBER_FOUND;
    }
}
