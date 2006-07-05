/* $Id: SingleImplementationInliner.java,v 1.7 2004/12/11 16:35:23 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2004 Eric Lafortune (eric@graphics.cornell.edu)
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
import proguard.classfile.instruction.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;

/**
 * This MemberInfoVisitor and AttrInfoVisitor replaces all references to
 * interfaces that have single implementations by references to those
 * implementations.
 *
 * @author Eric Lafortune
 */
public class SingleImplementationInliner
implements   MemberInfoVisitor,
             AttrInfoVisitor,
             InstructionVisitor,
             CpInfoVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor
{
    private MemberFinder memberFinder = new MemberFinder();

    private ConstantPoolEditor constantPoolEditor = new ConstantPoolEditor();
    private CodeAttrInfoEditor codeAttrInfoEditor = new CodeAttrInfoEditor(1024);


    // Return values of the single implementation inliner.
    private int       cpIndex;
    private ClassFile singleImplementationClassFile;


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        visitMemberInfo(programClassFile, programFieldInfo);
    }

    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        visitMemberInfo(programClassFile, programMethodInfo);
    }

    private void visitMemberInfo(ProgramClassFile programClassFile, ProgramMemberInfo programMemberInfo)
    {
        // Update the member info if any of its referenced classes
        // is an interface with a single implementation.
        ClassFile[] referencedClassFiles =
            updateReferencedClassFiles(programMemberInfo.referencedClassFiles);

        // Update the descriptor if necessary.
        if (referencedClassFiles != null)
        {
            programMemberInfo.u2descriptorIndex =
                constantPoolEditor.addUtf8CpInfo(programClassFile,
                                                 newDescriptor(programMemberInfo.getDescriptor(programClassFile),
                                                               referencedClassFiles));

            programMemberInfo.referencedClassFiles = referencedClassFiles;
        }
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}
    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo) {}


    // Implementations for AttrInfoVisitor.

    public void visitUnknownAttrInfo(ClassFile classFile, UnknownAttrInfo unknownAttrInfo) {}
    public void visitInnerClassesAttrInfo(ClassFile classFile, InnerClassesAttrInfo innerClassesAttrInfo) {}
    public void visitEnclosingMethodAttrInfo(ClassFile classFile, EnclosingMethodAttrInfo enclosingMethodAttrInfo) {}
    public void visitSourceFileAttrInfo(ClassFile classFile, SourceFileAttrInfo sourceFileAttrInfo) {}
    public void visitSourceDirAttrInfo(ClassFile classFile, SourceDirAttrInfo sourceDirAttrInfo) {}
    public void visitConstantValueAttrInfo(ClassFile classFile, FieldInfo fieldInfo, ConstantValueAttrInfo constantValueAttrInfo) {}
    public void visitExceptionsAttrInfo(ClassFile classFile, MethodInfo methodInfo, ExceptionsAttrInfo exceptionsAttrInfo) {}
    public void visitLineNumberTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LineNumberTableAttrInfo lineNumberTableAttrInfo) {}
    public void visitDeprecatedAttrInfo(ClassFile classFile, DeprecatedAttrInfo deprecatedAttrInfo) {}
    public void visitSyntheticAttrInfo(ClassFile classFile, SyntheticAttrInfo syntheticAttrInfo) {}


    public void visitCodeAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo)
    {
        // Reset the code changes.
        codeAttrInfoEditor.reset(codeAttrInfo.u4codeLength);

        // Update the instructions that refer to interfaces with a single
        // implementation.
        codeAttrInfo.instructionsAccept(classFile, methodInfo, this);

        // Apply the code changes.
        codeAttrInfoEditor.visitCodeAttrInfo(classFile, methodInfo, codeAttrInfo);

        //codeAttrInfo.attributesAccept(classFile, methodInfo, this);
    }


    public void visitLocalVariableTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTableAttrInfo localVariableTableAttrInfo)
    {
        // Rename the types of the local variables.
        localVariableTableAttrInfo.localVariablesAccept(classFile, methodInfo, codeAttrInfo, this);
    }


    public void visitLocalVariableTypeTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeTableAttrInfo localVariableTypeTableAttrInfo)
    {
        // Rename the signatures of the local variables.
        localVariableTypeTableAttrInfo.localVariablesAccept(classFile, methodInfo, codeAttrInfo, this);
    }


    public void visitSignatureAttrInfo(ClassFile classFile, SignatureAttrInfo signatureAttrInfo)
    {
        // TODO: Update signature attribute.
    }


    public void visitRuntimeVisibleAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleAnnotationsAttrInfo runtimeVisibleAnnotationsAttrInfo)
    {
        // TODO: Update runtime visible annotation attribute.

        //runtimeVisibleAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeInvisibleAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleAnnotationsAttrInfo runtimeInvisibleAnnotationsAttrInfo)
    {
        // TODO: Update runtime invisible annotation attribute.

        //runtimeInvisibleAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeVisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleParameterAnnotationsAttrInfo runtimeVisibleParameterAnnotationsAttrInfo)
    {
        // TODO: Update runtime visible parameter annotation attribute.

        //runtimeVisibleParameterAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeInvisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleParameterAnnotationsAttrInfo runtimeInvisibleParameterAnnotationsAttrInfo)
    {
        // TODO: Update runtime invisible parameter annotation attribute.

        //runtimeInvisibleParameterAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitAnnotationDefaultAttrInfo(ClassFile classFile, AnnotationDefaultAttrInfo annotationDefaultAttrInfo)
    {
        // TODO: Update annotation default attribute.

        //annotationDefaultAttrInfo.defaultValueAccept(classFile, this);
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction) {}
    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction) {}
    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction) {}
    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction) {}
    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction) {}


    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction)
    {
        cpIndex = 0;
        classFile.constantPoolEntryAccept(cpInstruction.cpIndex, this);

        if (cpIndex != 0)
        {
            byte opcode = cpInstruction.opcode;

            // Does this instruction now invoke a single implementation?
            if (opcode == InstructionConstants.OP_INVOKEINTERFACE &&
                singleImplementationClassFile != null)
            {
                // Replace the interface invocation by an ordinary invocation.
                opcode = InstructionConstants.OP_INVOKEVIRTUAL;
            }

            // Replace the instruction by an updated instruction.
            Instruction replacementInstruction = new CpInstruction(opcode,
                                                                   cpIndex,
                                                                   cpInstruction.constant).shrink();

            codeAttrInfoEditor.replaceInstruction(offset, replacementInstruction);
        }
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}


    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo)
    {
        // Create a new string entry if its referenced class is an interface with
        // a single implementation.
        singleImplementationClassFile =
            SingleImplementationMarker.singleImplementation(stringCpInfo.referencedClassFile);

        if (singleImplementationClassFile != null)
        {
            // Create a new string entry.
            cpIndex = constantPoolEditor.addStringCpInfo((ProgramClassFile)classFile,
                                                         singleImplementationClassFile.getName(),
                                                         singleImplementationClassFile);
        }
    }


    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo)
    {
        // Create a new field reference entry if its type has changed.
        cpIndex = 0;
        classFile.constantPoolEntryAccept(fieldrefCpInfo.getNameAndTypeIndex(), this);
        int nameAndTypeIndex = cpIndex;

        if (nameAndTypeIndex != 0)
        {
            // Create a new field reference entry.
            cpIndex = constantPoolEditor.addFieldrefCpInfo((ProgramClassFile)classFile,
                                                           fieldrefCpInfo.getClassIndex(),
                                                           nameAndTypeIndex,
                                                           fieldrefCpInfo.referencedClassFile,
                                                           fieldrefCpInfo.referencedMemberInfo);
        }
    }


    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo)
    {
        // Create a new method reference entry if its type has changed.
        cpIndex = 0;
        classFile.constantPoolEntryAccept(methodrefCpInfo.getNameAndTypeIndex(), this);
        int nameAndTypeIndex = cpIndex;

        if (nameAndTypeIndex != 0)
        {
            // Create a new method reference entry.
            cpIndex = constantPoolEditor.addMethodrefCpInfo((ProgramClassFile)classFile,
                                                            methodrefCpInfo.getClassIndex(),
                                                            nameAndTypeIndex,
                                                            methodrefCpInfo.referencedClassFile,
                                                            methodrefCpInfo.referencedMemberInfo);
        }
    }


    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo)
    {
        // Create a new ordinary method reference entry, if its referenced class
        // is an interface with a single implementation, or a a new interface
        // method reference entry, if its type has changed.
        cpIndex = 0;
        classFile.constantPoolEntryAccept(interfaceMethodrefCpInfo.getClassIndex(), this);
        int classIndex = cpIndex;

        cpIndex = 0;
        classFile.constantPoolEntryAccept(interfaceMethodrefCpInfo.getNameAndTypeIndex(), this);
        int nameAndTypeIndex = cpIndex;

        if (classIndex != 0)
        {
            if (nameAndTypeIndex == 0)
            {
                nameAndTypeIndex = interfaceMethodrefCpInfo.getNameAndTypeIndex();
            }

            // See if we can find the referenced method.
            ClassFile referencedClassFile = singleImplementationClassFile;

            String name = interfaceMethodrefCpInfo.getName(classFile);
            String type = interfaceMethodrefCpInfo.getType(classFile);

            // See if we can find the referenced class membver somewhere in the
            // hierarchy.
            MethodInfo referencedMethodInfo = memberFinder.findMethod(referencedClassFile,
                                                                      name,
                                                                      type);
            referencedClassFile             = memberFinder.correspondingClassFile();

            // Create an ordinary method reference entry.
            cpIndex = constantPoolEditor.addMethodrefCpInfo((ProgramClassFile)classFile,
                                                            classIndex,
                                                            nameAndTypeIndex,
                                                            referencedClassFile,
                                                            referencedMethodInfo);
        }
        else if (nameAndTypeIndex != 0)
        {
            classIndex = interfaceMethodrefCpInfo.getClassIndex();

            // Create an interface method reference entry.
            cpIndex = constantPoolEditor.addInterfaceMethodrefCpInfo((ProgramClassFile)classFile,
                                                                     classIndex,
                                                                     nameAndTypeIndex,
                                                                     interfaceMethodrefCpInfo.referencedClassFile,
                                                                     interfaceMethodrefCpInfo.referencedMemberInfo);
        }
    }


    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        // Create a new class entry if its referenced class is an interface with
        // a single implementation.
        singleImplementationClassFile =
            SingleImplementationMarker.singleImplementation(classCpInfo.referencedClassFile);

        if (singleImplementationClassFile != null)
        {
            // Create a new class entry.
            cpIndex = constantPoolEditor.addClassCpInfo((ProgramClassFile)classFile,
                                                        newClassName(classCpInfo.getName(classFile),
                                                                     singleImplementationClassFile),
                                                        singleImplementationClassFile);
        }
    }


    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo)
    {
        // Create a new name and type entry if any of the referenced classes of
        // its type is an interface with a single implementation.
        ClassFile[] referencedClassFiles =
            updateReferencedClassFiles(nameAndTypeCpInfo.referencedClassFiles);

        if (referencedClassFiles != null)
        {
            // Create a new name and type entry.
            cpIndex = constantPoolEditor.addNameAndTypeCpInfo((ProgramClassFile)classFile,
                                                              nameAndTypeCpInfo.getName(classFile),
                                                              newDescriptor(nameAndTypeCpInfo.getType(classFile),
                                                                            referencedClassFiles),
                                                              referencedClassFiles);
        }
    }


    // Implementations for LocalVariableInfoVisitor.

    public void visitLocalVariableInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableInfo localVariableInfo)
    {
        // Create a new type entry if its referenced class is an interface with
        // a single implementation.
        ClassFile singleImplementationClassFile =
            SingleImplementationMarker.singleImplementation(localVariableInfo.referencedClassFile);

        // Update the type if necessary.
        if (singleImplementationClassFile != null)
        {
            // Refer to a new Utf8 entry.
            localVariableInfo.u2descriptorIndex =
                constantPoolEditor.addUtf8CpInfo((ProgramClassFile)classFile,
                                                 newClassName(classFile.getCpString(localVariableInfo.u2descriptorIndex),
                                                              singleImplementationClassFile));
        }
    }


    // Implementations for LocalVariableTypeInfoVisitor.

    public void visitLocalVariableTypeInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeInfo localVariableTypeInfo)
    {
        // Create a new signature entry if any of the referenced classes of
        // its type is an interface with a single implementation.
        ClassFile[] referencedClassFiles =
            updateReferencedClassFiles(localVariableTypeInfo.referencedClassFiles);

        // Update the signature if necessary.
        if (referencedClassFiles != null)
        {
            localVariableTypeInfo.u2signatureIndex =
                constantPoolEditor.addUtf8CpInfo((ProgramClassFile)classFile,
                                                 newDescriptor(classFile.getCpString(localVariableTypeInfo.u2signatureIndex),
                                                               referencedClassFiles));
        }
    }


    // Small utility methods.

    /**
     * Updates the given array of referenced class files, if the refer to an
     * interface with a single implementation. Returns a new array if it
     * needed to be updated, or <code>null</code> otherwise.
     */
    private ClassFile[] updateReferencedClassFiles(ClassFile[] referencedClassFiles)
    {
        ClassFile[] newReferencedClassFiles = null;

        // Check all referenced classes.
        if (referencedClassFiles != null &&
            referencedClassFilesChanged(referencedClassFiles))
        {
            // Create a new array to copy the elements.
            newReferencedClassFiles = new ClassFile[referencedClassFiles.length];

            // Update all referenced classes.
            for (int index = 0; index < referencedClassFiles.length; index++)
            {
                ClassFile referencedClassFile = referencedClassFiles[index];

                // See if we have is an interface with a single implementation.
                ClassFile singleImplementationClassFile =
                    SingleImplementationMarker.singleImplementation(referencedClassFile);

                // Update or copy the referenced class file.
                newReferencedClassFiles[index] = singleImplementationClassFile != null ?
                    singleImplementationClassFile :
                    referencedClassFile;
            }
        }

        return newReferencedClassFiles;
    }


    private boolean referencedClassFilesChanged(ClassFile[] referencedClassFiles)
    {
        // Check all referenced classes.
        for (int index = 0; index < referencedClassFiles.length; index++)
        {
            // See if we have is an interface with a single implementation.
            if (SingleImplementationMarker.singleImplementation(referencedClassFiles[index]) != null)
            {
                // We've found an element that needs to be updated.
                return true;
            }
        }

        return false;
    }


    /**
     * Returns the new descriptor based on the given descriptor and the new
     * names of the given referenced class files.
     */
    private String newDescriptor(String      descriptor,
                                 ClassFile[] referencedClassFiles)
    {
        // Unravel and reconstruct the class elements of the descriptor.
        DescriptorClassEnumeration descriptorClassEnumeration =
            new DescriptorClassEnumeration(descriptor);

        String newDescriptor = descriptorClassEnumeration.nextFluff();

        int index = 0;
        while (descriptorClassEnumeration.hasMoreClassNames())
        {
            String className = descriptorClassEnumeration.nextClassName();
            String fluff     = descriptorClassEnumeration.nextFluff();

            String newClassName = newClassName(className,
                                               referencedClassFiles[index++]);

            // Fall back on the original class name if there is no new name.
            if (newClassName == null)
            {
                newClassName = className;
            }

            newDescriptor = newDescriptor + newClassName + fluff;
        }

        return newDescriptor;
    }


    /**
     * Returns the new class name based on the given class name and the new
     * name of the given referenced class file. Class names of array types
     * are handled properly.
     */
    private String newClassName(String    className,
                                ClassFile referencedClassFile)
    {
        // If there is no new class name, the descriptor doesn't change.
        if (referencedClassFile == null)
        {
            return className;
        }

        // Reconstruct the class name.
        String newClassName = referencedClassFile.getName();

        // Is it an array type?
        if (className.charAt(0) == ClassConstants.INTERNAL_TYPE_ARRAY)
        {
            // Add the array prefixes and suffix "[L...;".
            newClassName =
                 className.substring(0, className.indexOf(ClassConstants.INTERNAL_TYPE_CLASS_START)+1) +
                 newClassName +
                 ClassConstants.INTERNAL_TYPE_CLASS_END;
        }

        return newClassName;
    }
}
