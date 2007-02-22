/* $Id: ClassFileRenamer.java,v 1.42.2.1 2006/01/16 22:57:56 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2006 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.obfuscate;

import proguard.classfile.*;
import proguard.classfile.editor.ConstantPoolEditor;
import proguard.classfile.visitor.*;

/**
 * This <code>ClassFileVisitor</code> renames the class names and class member
 * names of the classes it visits, using names previously determined by the
 * obfuscator.
 *
 * @see ClassFileObfuscator
 *
 * @author Eric Lafortune
 */
public class ClassFileRenamer
  implements ClassFileVisitor,
             MemberInfoVisitor,
             CpInfoVisitor
{
    private ConstantPoolEditor constantPoolEditor = new ConstantPoolEditor();


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Rename this class.
        programClassFile.constantPoolEntryAccept(programClassFile.u2thisClass, this);

        // Rename the class members.
        programClassFile.fieldsAccept(this);
        programClassFile.methodsAccept(this);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        libraryClassFile.thisClassName = ClassFileObfuscator.newClassName(libraryClassFile);

        // Rename the class members.
        libraryClassFile.fieldsAccept(this);
        libraryClassFile.methodsAccept(this);
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        renameProgramMemberInfo(programClassFile, programFieldInfo);
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        renameProgramMemberInfo(programClassFile, programMethodInfo);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
        renameLibraryMemberInfo(libraryClassFile, libraryFieldInfo);
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        renameLibraryMemberInfo(libraryClassFile, libraryMethodInfo);
    }


    /**
     * Renames the given program member info, if necessary.
     */
    private void renameProgramMemberInfo(ProgramClassFile  programClassFile,
                                         ProgramMemberInfo programMemberInfo)
    {
        // Has the class member name changed?
        String name    = programMemberInfo.getName(programClassFile);
        String newName = MemberInfoObfuscator.newMemberName(programMemberInfo);
        if (newName != null &&
            !newName.equals(name))
        {
            programMemberInfo.u2nameIndex =
                constantPoolEditor.addUtf8CpInfo(programClassFile, newName);
        }
    }


    /**
     * Renames the given library member info.
     */
    private void renameLibraryMemberInfo(LibraryClassFile  libraryClassFile,
                                         LibraryMemberInfo libraryMemberInfo)
    {
        String newName = MemberInfoObfuscator.newMemberName(libraryMemberInfo);
        if (newName != null)
        {
            libraryMemberInfo.name = newName;
        }
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo) {}
    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo) {}
    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo) {}
    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}


    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        // Update the Class entry if required.
        String newName = ClassFileObfuscator.newClassName(classFile);
        if (newName != null)
        {
            // Refer to a new Utf8 entry.
            classCpInfo.u2nameIndex =
                constantPoolEditor.addUtf8CpInfo((ProgramClassFile)classFile,
                                                 newName);
        }
    }
}
