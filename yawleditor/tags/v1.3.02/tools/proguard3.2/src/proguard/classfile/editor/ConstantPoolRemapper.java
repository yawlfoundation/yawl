/* $Id: ConstantPoolRemapper.java,v 1.9 2004/11/20 15:41:24 eric Exp $
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
package proguard.classfile.editor;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.instruction.*;
import proguard.classfile.visitor.*;

/**
 * This ClassFileVisitor remaps all possible references to constant pool entries
 * of the classes that it visits, based on a given index map. It is assumed that
 * the constant pool entries themselves have already been remapped.
 *
 * @author Eric Lafortune
 */
public class ConstantPoolRemapper
  implements ClassFileVisitor,
             CpInfoVisitor,
             MemberInfoVisitor,
             AttrInfoVisitor,
             InstructionVisitor,
             InnerClassesInfoVisitor,
             ExceptionInfoVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor,
             AnnotationVisitor,
             ElementValueVisitor
{
    private CodeAttrInfoEditor codeAttrInfoEditor;

    private int[] cpIndexMap;


    /**
     * Creates a new ConstantPoolRemapper.
     * @param codeLength an estimate of the maximum length of all the code that
     *                   will be edited.
     */
    public ConstantPoolRemapper(int codeLength)
    {
        codeAttrInfoEditor = new CodeAttrInfoEditor(codeLength);
    }


    /**
     * Sets the given mapping of old constant pool entry indexes to their new
     * indexes.
     */
    public void setCpIndexMap(int[] cpIndexMap)
    {
        this.cpIndexMap = cpIndexMap;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Remap the local constant pool references.
        programClassFile.u2thisClass  = remapCpIndex(programClassFile.u2thisClass);
        programClassFile.u2superClass = remapCpIndex(programClassFile.u2superClass);

        remapCpIndexArray(programClassFile.u2interfaces,
                          programClassFile.u2interfacesCount);

        // Remap the references of the contant pool entries themselves.
        programClassFile.constantPoolEntriesAccept(this);

        // Remap the references in all fields, methods, and attributes.
        programClassFile.fieldsAccept(this);
        programClassFile.methodsAccept(this);
        programClassFile.attributesAccept(this);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
    }


    // Implementations for CpInfoVisitor.

    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        classCpInfo.u2nameIndex =
            remapCpIndex(classCpInfo.u2nameIndex);
    }


    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo)
    {
        // Nothing to do.
    }


    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo)
    {
        fieldrefCpInfo.u2classIndex =
            remapCpIndex(fieldrefCpInfo.u2classIndex);
        fieldrefCpInfo.u2nameAndTypeIndex =
            remapCpIndex(fieldrefCpInfo.u2nameAndTypeIndex);
    }


    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo)
    {
        // Nothing to do.
    }


    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo)
    {
        // Nothing to do.
    }


    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo)
    {
        interfaceMethodrefCpInfo.u2classIndex =
            remapCpIndex(interfaceMethodrefCpInfo.u2classIndex);
        interfaceMethodrefCpInfo.u2nameAndTypeIndex =
            remapCpIndex(interfaceMethodrefCpInfo.u2nameAndTypeIndex);
    }


    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo)
    {
        // Nothing to do.
    }


    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo)
    {
        methodrefCpInfo.u2classIndex =
            remapCpIndex(methodrefCpInfo.u2classIndex);
        methodrefCpInfo.u2nameAndTypeIndex =
            remapCpIndex(methodrefCpInfo.u2nameAndTypeIndex);
    }


    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo)
    {
        nameAndTypeCpInfo.u2nameIndex =
            remapCpIndex(nameAndTypeCpInfo.u2nameIndex);
        nameAndTypeCpInfo.u2descriptorIndex =
            remapCpIndex(nameAndTypeCpInfo.u2descriptorIndex);
    }


    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo)
    {
        stringCpInfo.u2stringIndex =
            remapCpIndex(stringCpInfo.u2stringIndex);
    }


    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo)
    {
        // Nothing to do.
    }


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
        // Remap the local constant pool references.
        programMemberInfo.u2nameIndex =
            remapCpIndex(programMemberInfo.u2nameIndex);
        programMemberInfo.u2descriptorIndex =
            remapCpIndex(programMemberInfo.u2descriptorIndex);

        // Remap the constant pool references of the remaining attributes.
        programMemberInfo.attributesAccept(programClassFile, this);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
        // Library class files are left unchanged.
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        // Library class files are left unchanged.
    }


    // Implementations for AttrInfoVisitor.

    public void visitUnknownAttrInfo(ClassFile classFile, UnknownAttrInfo unknownAttrInfo)
    {
        unknownAttrInfo.u2attrNameIndex =
            remapCpIndex(unknownAttrInfo.u2attrNameIndex);

        // There's not much else we can do with unknown attributes.
    }


    public void visitInnerClassesAttrInfo(ClassFile classFile, InnerClassesAttrInfo innerClassesAttrInfo)
    {
        innerClassesAttrInfo.u2attrNameIndex =
            remapCpIndex(innerClassesAttrInfo.u2attrNameIndex);

        // Remap the constant pool references of the inner classes.
        innerClassesAttrInfo.innerClassEntriesAccept(classFile, this);
    }


    public void visitEnclosingMethodAttrInfo(ClassFile classFile, EnclosingMethodAttrInfo enclosingMethodAttrInfo)
    {
        enclosingMethodAttrInfo.u2attrNameIndex =
            remapCpIndex(enclosingMethodAttrInfo.u2attrNameIndex);
        enclosingMethodAttrInfo.u2classIndex =
            remapCpIndex(enclosingMethodAttrInfo.u2classIndex);
        enclosingMethodAttrInfo.u2nameAndTypeIndex =
            remapCpIndex(enclosingMethodAttrInfo.u2nameAndTypeIndex);
    }


    public void visitConstantValueAttrInfo(ClassFile classFile, FieldInfo fieldInfo, ConstantValueAttrInfo constantValueAttrInfo)
    {
        constantValueAttrInfo.u2attrNameIndex =
            remapCpIndex(constantValueAttrInfo.u2attrNameIndex);
        constantValueAttrInfo.u2constantValueIndex =
            remapCpIndex(constantValueAttrInfo.u2constantValueIndex);
    }


    public void visitExceptionsAttrInfo(ClassFile classFile, MethodInfo methodInfo, ExceptionsAttrInfo exceptionsAttrInfo)
    {
        exceptionsAttrInfo.u2attrNameIndex =
            remapCpIndex(exceptionsAttrInfo.u2attrNameIndex);

        // Remap the constant pool references of the exceptions.
        remapCpIndexArray(exceptionsAttrInfo.u2exceptionIndexTable,
                          exceptionsAttrInfo.u2numberOfExceptions);
    }


    public void visitCodeAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo)
    {
        codeAttrInfo.u2attrNameIndex =
            remapCpIndex(codeAttrInfo.u2attrNameIndex);

        // Initially, the code attribute editor doesn't contain any changes.
        codeAttrInfoEditor.reset(codeAttrInfo.u4codeLength);

        // Remap the constant pool references of the instructions.
        codeAttrInfo.instructionsAccept(classFile, methodInfo, this);

        // Apply the code atribute editor. It will only contain any changes if
        // the code length is changing at any point.
        codeAttrInfoEditor.visitCodeAttrInfo(classFile, methodInfo, codeAttrInfo);

        // Remap the constant pool references of the exceptions and attributes.
        codeAttrInfo.exceptionsAccept(classFile, methodInfo, this);
        codeAttrInfo.attributesAccept(classFile, methodInfo, this);
    }


    public void visitLineNumberTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LineNumberTableAttrInfo lineNumberTableAttrInfo)
    {
        lineNumberTableAttrInfo.u2attrNameIndex =
            remapCpIndex(lineNumberTableAttrInfo.u2attrNameIndex);
    }


    public void visitLocalVariableTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTableAttrInfo localVariableTableAttrInfo)
    {
        localVariableTableAttrInfo.u2attrNameIndex =
            remapCpIndex(localVariableTableAttrInfo.u2attrNameIndex);

        // Remap the constant pool references of the local variables.
        localVariableTableAttrInfo.localVariablesAccept(classFile, methodInfo, codeAttrInfo, this);
    }


    public void visitLocalVariableTypeTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeTableAttrInfo localVariableTypeTableAttrInfo)
    {
        localVariableTypeTableAttrInfo.u2attrNameIndex =
            remapCpIndex(localVariableTypeTableAttrInfo.u2attrNameIndex);

        // Remap the constant pool references of the local variables.
        localVariableTypeTableAttrInfo.localVariablesAccept(classFile, methodInfo, codeAttrInfo, this);
    }


    public void visitSourceFileAttrInfo(ClassFile classFile, SourceFileAttrInfo sourceFileAttrInfo)
    {
        sourceFileAttrInfo.u2attrNameIndex =
            remapCpIndex(sourceFileAttrInfo.u2attrNameIndex);
        sourceFileAttrInfo.u2sourceFileIndex =
            remapCpIndex(sourceFileAttrInfo.u2sourceFileIndex);
    }


    public void visitSourceDirAttrInfo(ClassFile classFile, SourceDirAttrInfo sourceDirAttrInfo)
    {
        sourceDirAttrInfo.u2attrNameIndex =
            remapCpIndex(sourceDirAttrInfo.u2attrNameIndex);
        sourceDirAttrInfo.u2sourceDirIndex =
            remapCpIndex(sourceDirAttrInfo.u2sourceDirIndex);
    }


    public void visitDeprecatedAttrInfo(ClassFile classFile, DeprecatedAttrInfo deprecatedAttrInfo)
    {
        deprecatedAttrInfo.u2attrNameIndex =
            remapCpIndex(deprecatedAttrInfo.u2attrNameIndex);
    }


    public void visitSyntheticAttrInfo(ClassFile classFile, SyntheticAttrInfo syntheticAttrInfo)
    {
        syntheticAttrInfo.u2attrNameIndex =
            remapCpIndex(syntheticAttrInfo.u2attrNameIndex);
    }


    public void visitSignatureAttrInfo(ClassFile classFile, SignatureAttrInfo signatureAttrInfo)
    {
        signatureAttrInfo.u2attrNameIndex =
            remapCpIndex(signatureAttrInfo.u2attrNameIndex);
        signatureAttrInfo.u2signatureIndex =
            remapCpIndex(signatureAttrInfo.u2signatureIndex);
    }


    public void visitRuntimeVisibleAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleAnnotationsAttrInfo runtimeVisibleAnnotationsAttrInfo)
    {
        runtimeVisibleAnnotationsAttrInfo.u2attrNameIndex =
            remapCpIndex(runtimeVisibleAnnotationsAttrInfo.u2attrNameIndex);

        // Remap the constant pool references of the annotations.
        runtimeVisibleAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeInvisibleAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleAnnotationsAttrInfo runtimeInvisibleAnnotationsAttrInfo)
    {
        runtimeInvisibleAnnotationsAttrInfo.u2attrNameIndex =
            remapCpIndex(runtimeInvisibleAnnotationsAttrInfo.u2attrNameIndex);

        // Remap the constant pool references of the annotations.
        runtimeInvisibleAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeVisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleParameterAnnotationsAttrInfo runtimeVisibleParameterAnnotationsAttrInfo)
    {
        runtimeVisibleParameterAnnotationsAttrInfo.u2attrNameIndex =
            remapCpIndex(runtimeVisibleParameterAnnotationsAttrInfo.u2attrNameIndex);

        // Remap the constant pool references of the annotations.
        runtimeVisibleParameterAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeInvisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleParameterAnnotationsAttrInfo runtimeInvisibleParameterAnnotationsAttrInfo)
    {
        runtimeInvisibleParameterAnnotationsAttrInfo.u2attrNameIndex =
            remapCpIndex(runtimeInvisibleParameterAnnotationsAttrInfo.u2attrNameIndex);

        // Remap the constant pool references of the annotations.
        runtimeInvisibleParameterAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitAnnotationDefaultAttrInfo(ClassFile classFile, AnnotationDefaultAttrInfo annotationDefaultAttrInfo)
    {
        annotationDefaultAttrInfo.u2attrNameIndex =
            remapCpIndex(annotationDefaultAttrInfo.u2attrNameIndex);

        // Remap the constant pool references of the annotations.
        annotationDefaultAttrInfo.defaultValueAccept(classFile, this);
    }


    // Implementations for InnerClassesInfoVisitor.

    public void visitInnerClassesInfo(ClassFile classFile, InnerClassesInfo innerClassesInfo)
    {
        if (innerClassesInfo.u2innerClassInfoIndex != 0)
        {
            innerClassesInfo.u2innerClassInfoIndex =
                remapCpIndex(innerClassesInfo.u2innerClassInfoIndex);
        }

        if (innerClassesInfo.u2outerClassInfoIndex != 0)
        {
            innerClassesInfo.u2outerClassInfoIndex =
                remapCpIndex(innerClassesInfo.u2outerClassInfoIndex);
        }

        if (innerClassesInfo.u2innerNameIndex != 0)
        {
            innerClassesInfo.u2innerNameIndex =
                remapCpIndex(innerClassesInfo.u2innerNameIndex);
        }
    }


    // Implementations for ExceptionInfoVisitor.

    public void visitExceptionInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, ExceptionInfo exceptionInfo)
    {
        if (exceptionInfo.u2catchType != 0)
        {
            exceptionInfo.u2catchType =
                remapCpIndex(exceptionInfo.u2catchType);
        }
    }


    // Implementations for LocalVariableInfoVisitor.

    public void visitLocalVariableInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableInfo localVariableInfo)
    {
        localVariableInfo.u2nameIndex =
            remapCpIndex(localVariableInfo.u2nameIndex);
        localVariableInfo.u2descriptorIndex =
            remapCpIndex(localVariableInfo.u2descriptorIndex);
    }


    // Implementations for LocalVariableTypeInfoVisitor.

    public void visitLocalVariableTypeInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeInfo localVariableTypeInfo)
    {
        localVariableTypeInfo.u2nameIndex =
            remapCpIndex(localVariableTypeInfo.u2nameIndex);
        localVariableTypeInfo.u2signatureIndex =
            remapCpIndex(localVariableTypeInfo.u2signatureIndex);
    }


    // Implementations for AnnotationVisitor.

    public void visitAnnotation(ClassFile classFile, Annotation annotation)
    {
        annotation.u2typeIndex =
            remapCpIndex(annotation.u2typeIndex);

        // Remap the constant pool references of the element values.
        annotation.elementValuesAccept(classFile, this);
    }


    // Implementations for ElementValueVisitor.

    public void visitConstantElementValue(ClassFile classFile, Annotation annotation, ConstantElementValue constantElementValue)
    {
        constantElementValue.u2elementName =
            remapCpIndex(constantElementValue.u2elementName);
        constantElementValue.u2constantValueIndex =
            remapCpIndex(constantElementValue.u2constantValueIndex);
    }


    public void visitEnumConstantElementValue(ClassFile classFile, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        enumConstantElementValue.u2elementName =
            remapCpIndex(enumConstantElementValue.u2elementName);
        enumConstantElementValue.u2typeNameIndex =
            remapCpIndex(enumConstantElementValue.u2typeNameIndex);
        enumConstantElementValue.u2constantNameIndex =
            remapCpIndex(enumConstantElementValue.u2constantNameIndex);
    }


    public void visitClassElementValue(ClassFile classFile, Annotation annotation, ClassElementValue classElementValue)
    {
        classElementValue.u2elementName =
            remapCpIndex(classElementValue.u2elementName);
        classElementValue.u2classInfoIndex =
            remapCpIndex(classElementValue.u2classInfoIndex);
    }


    public void visitAnnotationElementValue(ClassFile classFile, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        annotationElementValue.u2elementName =
            remapCpIndex(annotationElementValue.u2elementName);

        // Remap the constant pool references of the annotation.
        annotationElementValue.annotationAccept(classFile, this);
    }


    public void visitArrayElementValue(ClassFile classFile, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        arrayElementValue.u2elementName =
            remapCpIndex(arrayElementValue.u2elementName);

        // Remap the constant pool references of the element values.
        arrayElementValue.elementValuesAccept(classFile, annotation, this);
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction) {}
    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction) {}
    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction) {}
    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction) {}
    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction) {}


    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction)
    {
        // Remap the instruction, and get the old and the new instruction length.
        int oldLength = cpInstruction.length(offset);

        cpInstruction.cpIndex = remapCpIndex(cpInstruction.cpIndex);
        cpInstruction.shrink();

        int newLength = cpInstruction.length(offset);

        // Is the code length changing?
        if (newLength != oldLength ||
            codeAttrInfoEditor.isModified())
        {
            // We have to go through the code attribute editor.
            cpInstruction = new CpInstruction().copy(cpInstruction);

            codeAttrInfoEditor.replaceInstruction(offset, cpInstruction);
        }
        else
        {
            // We can write the instruction directly.
            cpInstruction.write(codeAttrInfo, offset);
        }
    }


    // Small utility methods.

    /**
     * Remaps all constant pool indices in the given array.
     */
    private void remapCpIndexArray(int[] array, int length)
    {
        for (int index = 0; index < length; index++)
        {
            array[index] = remapCpIndex(array[index]);
        }
    }


    /**
     * Returns the new constant pool index of the entry at the
     * given index.
     */
    private int remapCpIndex(int cpIndex)
    {
        return cpIndexMap[cpIndex];
    }
}
