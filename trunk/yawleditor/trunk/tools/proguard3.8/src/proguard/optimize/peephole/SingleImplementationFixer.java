/* $Id: SingleImplementationFixer.java,v 1.3.2.2 2007/01/18 21:31:53 eric Exp $
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
package proguard.optimize.peephole;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.editor.*;
import proguard.classfile.editor.ConstantPoolEditor;
import proguard.classfile.visitor.*;

/**
 * This ClassFileVisitor cleans up after the SingleImplementationInliner.
 * It fixes the names of interfaces that have single implementations, lets
 * the implementations and fields references point to them again. This is
 * necessary after the SingleImplementationInliner has overzealously renamed
 * the interfaces to the single implementations, let the single implementations
 * point to themselves as interfaces, and let the field references point to the
 * single implementations.
 *
 * @see SingleImplementationInliner
 * @see ClassFileReferenceFixer
 * @author Eric Lafortune
 */
public class SingleImplementationFixer
implements   ClassFileVisitor,
             CpInfoVisitor
{
    private ConstantPoolEditor constantPoolEditor = new ConstantPoolEditor();


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Is this an interface with a single implementation?
        ClassFile singleImplementationClassFile =
            SingleImplementationMarker.singleImplementation(programClassFile);

        if (singleImplementationClassFile != null)
        {
            // Fix the reference to its own name.
            fixThisClassReference(programClassFile);

            // Fix the reference from its single interface or implementation.
            fixInterfaceReference((ProgramClassFile)programClassFile.subClasses[0],
                                  programClassFile);
        }

        // Fix the field references in the constant pool.
        programClassFile.constantPoolEntriesAccept(this);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}
    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo) {}
    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo) {}
    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}


    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo)
    {
        // Update the referenced class file if it is an interface with a single
        // implementation.
        ClassFile singleImplementationClassFile =
            SingleImplementationMarker.singleImplementation(fieldrefCpInfo.referencedClassFile);

        if (singleImplementationClassFile != null)
        {
            // Fix the reference to the interface.
            fixFieldrefClassReference((ProgramClassFile)classFile,
                                      fieldrefCpInfo);
        }
    }


    // Small utility methods.

    /**
     * Fixes the given class file, so its name points to itself again.
     */
    private void fixThisClassReference(ProgramClassFile programClassFile)
    {
        // We have to add a new class entry to avoid an existing entry with the
        // same name being reused. The names have to be fixed later, based on
        // their referenced class files.
        int nameIndex =
            constantPoolEditor.addUtf8CpInfo(programClassFile,
                                             programClassFile.getName());
        programClassFile.u2thisClass =
            constantPoolEditor.addCpInfo(programClassFile,
                                         new ClassCpInfo(nameIndex,
                                                         programClassFile));
    }


    /**
     * Fixes the given class file, so it points to the given interface again.
     */
    private void fixInterfaceReference(ProgramClassFile programClassFile,
                                       ProgramClassFile interfaceClassFile)
    {
        // Make sure the class refers to the given interface again.
        String interfaceName = interfaceClassFile.getName();

        int interfacesCount = programClassFile.u2interfacesCount;
        for (int index = 0; index < interfacesCount; index++)
        {
            if (interfaceName.equals(programClassFile.getInterfaceName(index)))
            {
                // Update the class index.
                // We have to add a new class entry to avoid an existing entry
                // with the same name being reused. The names have to be fixed
                // later, based on their referenced class files.
                int nameIndex =
                    constantPoolEditor.addUtf8CpInfo(programClassFile,
                                                     interfaceName);
                programClassFile.u2interfaces[index] =
                    constantPoolEditor.addCpInfo(programClassFile,
                                                 new ClassCpInfo(nameIndex,
                                                                 interfaceClassFile));
                break;

            }
        }
    }


    /**
     * Fixes the given field reference, so its class index points to its
     * class again. Note that this could be a different class than the one
     * in the original class file.
     */
    private void fixFieldrefClassReference(ProgramClassFile programClassFile,
                                           FieldrefCpInfo   fieldrefCpInfo)
    {
        ClassFile referencedClassFile = fieldrefCpInfo.referencedClassFile;

        // We have to add a new class entry to avoid an existing entry with the
        // same name being reused. The names have to be fixed later, based on
        // their referenced class files.
        int nameIndex =
            constantPoolEditor.addUtf8CpInfo(programClassFile,
                                             fieldrefCpInfo.getClassName(programClassFile));
        fieldrefCpInfo.u2classIndex =
            constantPoolEditor.addCpInfo(programClassFile,
                                         new ClassCpInfo(nameIndex,
                                                         referencedClassFile));
    }
}
