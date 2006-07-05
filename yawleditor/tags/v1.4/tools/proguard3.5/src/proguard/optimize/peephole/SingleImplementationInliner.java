/* $Id: SingleImplementationInliner.java,v 1.10.2.1 2006/01/16 22:57:56 eric Exp $
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
package proguard.optimize.peephole;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.editor.ClassFileReferenceFixer;
import proguard.classfile.visitor.*;

/**
 * This ClassFileVisitor replaces all references to interfaces that have single
 * implementations by references to those implementations. The names will then
 * have to be fixed, based on the new references.
 *
 * @see SingleImplementationFixer
 * @see ClassFileReferenceFixer
 * @author Eric Lafortune
 */
public class SingleImplementationInliner
implements   ClassFileVisitor,
             CpInfoVisitor,
             MemberInfoVisitor,
             AttrInfoVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor,
             AnnotationVisitor,
             ElementValueVisitor
{
    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Update the constant pool.
        programClassFile.constantPoolEntriesAccept(this);

        // Update the class members.
        programClassFile.fieldsAccept(this);
        programClassFile.methodsAccept(this);

        // Update the attributes.
        programClassFile.attributesAccept(this);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}
    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo) {}
    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}


    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo)
    {
        // Update the referenced class file if it is an interface with a single
        // implementation.
        ClassFile singleImplementationClassFile =
            SingleImplementationMarker.singleImplementation(stringCpInfo.referencedClassFile);

        if (singleImplementationClassFile != null)
        {
            stringCpInfo.referencedClassFile = singleImplementationClassFile;
        }
    }


    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo)
    {
        // Update the referenced interface if it has a single implementation.
        ClassFile singleImplementationClassFile =
            SingleImplementationMarker.singleImplementation(interfaceMethodrefCpInfo.referencedClassFile);

        if (singleImplementationClassFile != null)
        {
            // We know the single implementation contains the method.
            String name = interfaceMethodrefCpInfo.getName(classFile);
            String type = interfaceMethodrefCpInfo.getType(classFile);

            interfaceMethodrefCpInfo.referencedClassFile  = singleImplementationClassFile;
            interfaceMethodrefCpInfo.referencedMemberInfo = singleImplementationClassFile.findMethod(name, type);
        }
    }


    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        // Update the referenced class file if it is an interface with a single
        // implementation.
        ClassFile singleImplementationClassFile =
            SingleImplementationMarker.singleImplementation(classCpInfo.referencedClassFile);

        if (singleImplementationClassFile != null)
        {
            classCpInfo.referencedClassFile = singleImplementationClassFile;
        }
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        // Update the referenced class file if the type is an interface with a
        // single implementation.
        ClassFile singleImplementationClassFile =
            SingleImplementationMarker.singleImplementation(programFieldInfo.referencedClassFile);

        if (singleImplementationClassFile != null)
        {
            programFieldInfo.referencedClassFile = singleImplementationClassFile;
        }

        // Update the attributes.
        programFieldInfo.attributesAccept(programClassFile, this);
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        // Update the referenced class files if the descriptor contains
        // interfaces with single implementations.
        updateReferencedClassFiles(programMethodInfo.referencedClassFiles);

        // Update the attributes.
        programMethodInfo.attributesAccept(programClassFile, this);
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
        // Update the referenced class files of the local variables.
        codeAttrInfo.attributesAccept(classFile, methodInfo, this);
    }


    public void visitLocalVariableTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTableAttrInfo localVariableTableAttrInfo)
    {
        // Update the referenced class files of the local variables.
        localVariableTableAttrInfo.localVariablesAccept(classFile, methodInfo, codeAttrInfo, this);
    }


    public void visitLocalVariableTypeTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeTableAttrInfo localVariableTypeTableAttrInfo)
    {
        // Update the referenced class files of the local variable types.
        localVariableTypeTableAttrInfo.localVariablesAccept(classFile, methodInfo, codeAttrInfo, this);
    }


    public void visitSignatureAttrInfo(ClassFile classFile, SignatureAttrInfo signatureAttrInfo)
    {
        // Update the referenced class files.
        updateReferencedClassFiles(signatureAttrInfo.referencedClassFiles);
    }


    public void visitRuntimeVisibleAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleAnnotationsAttrInfo runtimeVisibleAnnotationsAttrInfo)
    {
        // Update the annotations.
        runtimeVisibleAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeInvisibleAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleAnnotationsAttrInfo runtimeInvisibleAnnotationsAttrInfo)
    {
        // Update the annotations.
        runtimeInvisibleAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeVisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleParameterAnnotationsAttrInfo runtimeVisibleParameterAnnotationsAttrInfo)
    {
        // Update the annotations.
        runtimeVisibleParameterAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeInvisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleParameterAnnotationsAttrInfo runtimeInvisibleParameterAnnotationsAttrInfo)
    {
        // Update the annotations.
        runtimeInvisibleParameterAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitAnnotationDefaultAttrInfo(ClassFile classFile, AnnotationDefaultAttrInfo annotationDefaultAttrInfo)
    {
        // Update the annotation.
        annotationDefaultAttrInfo.defaultValueAccept(classFile, this);
    }


    // Implementations for LocalVariableInfoVisitor.

    public void visitLocalVariableInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableInfo localVariableInfo)
    {
        // Update the referenced class file if it is an interface with a single
        // implementation.
        ClassFile singleImplementationClassFile =
            SingleImplementationMarker.singleImplementation(localVariableInfo.referencedClassFile);

        if (singleImplementationClassFile != null)
        {
            localVariableInfo.referencedClassFile = singleImplementationClassFile;
        }
    }


    // Implementations for LocalVariableTypeInfoVisitor.

    public void visitLocalVariableTypeInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeInfo localVariableTypeInfo)
    {
        // Update the referenced class files.
        updateReferencedClassFiles(localVariableTypeInfo.referencedClassFiles);
    }


    // Implementations for AnnotationVisitor.

    public void visitAnnotation(ClassFile classFile, Annotation annotation)
    {
        // Update the referenced class files.
        updateReferencedClassFiles(annotation.referencedClassFiles);

        // Update the element values.
        annotation.elementValuesAccept(classFile, this);
    }


    // Implementations for ElementValueVisitor.

    public void visitConstantElementValue(ClassFile classFile, Annotation annotation, ConstantElementValue constantElementValue)
    {
    }


    public void visitEnumConstantElementValue(ClassFile classFile, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        // Update the referenced class files.
        updateReferencedClassFiles(enumConstantElementValue.referencedClassFiles);
    }


    public void visitClassElementValue(ClassFile classFile, Annotation annotation, ClassElementValue classElementValue)
    {
        // Update the referenced class files.
        updateReferencedClassFiles(classElementValue.referencedClassFiles);
    }


    public void visitAnnotationElementValue(ClassFile classFile, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        // Update the annotation.
        annotationElementValue.annotationAccept(classFile, this);
    }


    public void visitArrayElementValue(ClassFile classFile, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        // Update the element values.
        arrayElementValue.elementValuesAccept(classFile, annotation, this);
    }


    // Small utility methods.

    /**
     * Updates the given array of referenced class files, replacing references
     * to a interfaces with single implementations by these implementations.
     */
    private void updateReferencedClassFiles(ClassFile[] referencedClassFiles)
    {
        // Update all referenced classes.
        if (referencedClassFiles != null)
        {
            for (int index = 0; index < referencedClassFiles.length; index++)
            {
                // See if we have is an interface with a single implementation.
                ClassFile singleImplementationClassFile =
                    SingleImplementationMarker.singleImplementation(referencedClassFiles[index]);

                // Update or copy the referenced class file.
                if (singleImplementationClassFile != null)
                {
                    referencedClassFiles[index] = singleImplementationClassFile;
                }
            }
        }
    }
}
