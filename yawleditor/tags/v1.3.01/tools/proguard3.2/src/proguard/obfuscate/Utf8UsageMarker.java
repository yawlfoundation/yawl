/* $Id: Utf8UsageMarker.java,v 1.22 2004/11/20 15:41:24 eric Exp $
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
package proguard.obfuscate;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.visitor.*;

/**
 * This ClassFileVisitor marks all UTF-8 constant pool entries that are
 * being used in the classes it visits.
 *
 * @see Utf8Shrinker
 *
 * @author Eric Lafortune
 */
public class Utf8UsageMarker
  implements ClassFileVisitor,
             MemberInfoVisitor,
             CpInfoVisitor,
             AttrInfoVisitor,
             InnerClassesInfoVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor,
             AnnotationVisitor,
             ElementValueVisitor
{
    // A visitor info flag to indicate the UTF-8 constant pool entry is being used.
    private static final Object USED = new Object();


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Mark the UTF-8 entries referenced by the other constant pool entries.
        programClassFile.constantPoolEntriesAccept(this);

        // Mark the UTF-8 entries referenced by the fields and methods.
        programClassFile.fieldsAccept(this);
        programClassFile.methodsAccept(this);

        // Mark the UTF-8 entries referenced by the attributes.
        programClassFile.attributesAccept(this);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
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
        // Mark the name and descriptor UTF-8 entries.
        markCpUtf8Entry(programClassFile, programMemberInfo.u2nameIndex);
        markCpUtf8Entry(programClassFile, programMemberInfo.u2descriptorIndex);

        // Mark the UTF-8 entries referenced by the attributes.
        programMemberInfo.attributesAccept(programClassFile, this);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}
    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo) {}


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}
    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo) {}
    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo) {}
    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo) {}


    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo)
    {
        markCpUtf8Entry(classFile, stringCpInfo.u2stringIndex);
    }


    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        markCpUtf8Entry(classFile, classCpInfo.u2nameIndex);
    }


    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo)
    {
        markCpUtf8Entry(classFile, nameAndTypeCpInfo.u2nameIndex);
        markCpUtf8Entry(classFile, nameAndTypeCpInfo.u2descriptorIndex);
    }


    // Implementations for AttrInfoVisitor.

    public void visitUnknownAttrInfo(ClassFile classFile, UnknownAttrInfo unknownAttrInfo)
    {
        // This is the best we can do for unknown attributes.
        markCpUtf8Entry(classFile, unknownAttrInfo.u2attrNameIndex);
    }


    public void visitInnerClassesAttrInfo(ClassFile classFile, InnerClassesAttrInfo innerClassesAttrInfo)
    {
        markCpUtf8Entry(classFile, innerClassesAttrInfo.u2attrNameIndex);

        // Mark the UTF-8 entries referenced by the inner classes.
        innerClassesAttrInfo.innerClassEntriesAccept(classFile, this);
    }


    public void visitEnclosingMethodAttrInfo(ClassFile classFile, EnclosingMethodAttrInfo enclosingMethodAttrInfo)
    {
        markCpUtf8Entry(classFile, enclosingMethodAttrInfo.u2attrNameIndex);

        // These entries have already been marked in the constant pool.
        //classFile.constantPoolEntryAccept(this, enclosingMethodAttrInfo.u2classIndex);
        //classFile.constantPoolEntryAccept(this, enclosingMethodAttrInfo.u2nameAndTypeIndex);
    }


    public void visitConstantValueAttrInfo(ClassFile classFile, FieldInfo fieldInfo, ConstantValueAttrInfo constantValueAttrInfo)
    {
        markCpUtf8Entry(classFile, constantValueAttrInfo.u2attrNameIndex);
    }


    public void visitExceptionsAttrInfo(ClassFile classFile, MethodInfo methodInfo, ExceptionsAttrInfo exceptionsAttrInfo)
    {
        markCpUtf8Entry(classFile, exceptionsAttrInfo.u2attrNameIndex);
    }


    public void visitCodeAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo)
    {
        markCpUtf8Entry(classFile, codeAttrInfo.u2attrNameIndex);

        // Mark the UTF-8 entries referenced by the attributes.
        codeAttrInfo.attributesAccept(classFile, methodInfo, this);
    }


    public void visitLineNumberTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LineNumberTableAttrInfo lineNumberTableAttrInfo)
    {
        markCpUtf8Entry(classFile, lineNumberTableAttrInfo.u2attrNameIndex);
    }


    public void visitLocalVariableTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTableAttrInfo localVariableTableAttrInfo)
    {
        markCpUtf8Entry(classFile, localVariableTableAttrInfo.u2attrNameIndex);

        // Mark the UTF-8 entries referenced by the local variables.
        localVariableTableAttrInfo.localVariablesAccept(classFile, methodInfo, codeAttrInfo, this);
    }


    public void visitLocalVariableTypeTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeTableAttrInfo localVariableTypeTableAttrInfo)
    {
        markCpUtf8Entry(classFile, localVariableTypeTableAttrInfo.u2attrNameIndex);

        // Mark the UTF-8 entries referenced by the local variable types.
        localVariableTypeTableAttrInfo.localVariablesAccept(classFile, methodInfo, codeAttrInfo, this);
    }


    public void visitSourceFileAttrInfo(ClassFile classFile, SourceFileAttrInfo sourceFileAttrInfo)
    {
        markCpUtf8Entry(classFile, sourceFileAttrInfo.u2attrNameIndex);

        markCpUtf8Entry(classFile, sourceFileAttrInfo.u2sourceFileIndex);
    }


    public void visitSourceDirAttrInfo(ClassFile classFile, SourceDirAttrInfo sourceDirAttrInfo)
    {
        markCpUtf8Entry(classFile, sourceDirAttrInfo.u2attrNameIndex);

        markCpUtf8Entry(classFile, sourceDirAttrInfo.u2sourceDirIndex);
    }


    public void visitDeprecatedAttrInfo(ClassFile classFile, DeprecatedAttrInfo deprecatedAttrInfo)
    {
        markCpUtf8Entry(classFile, deprecatedAttrInfo.u2attrNameIndex);
    }


    public void visitSyntheticAttrInfo(ClassFile classFile, SyntheticAttrInfo syntheticAttrInfo)
    {
        markCpUtf8Entry(classFile, syntheticAttrInfo.u2attrNameIndex);
    }


    public void visitSignatureAttrInfo(ClassFile classFile, SignatureAttrInfo signatureAttrInfo)
    {
        markCpUtf8Entry(classFile, signatureAttrInfo.u2attrNameIndex);

        markCpUtf8Entry(classFile, signatureAttrInfo.u2signatureIndex);
    }


    public void visitRuntimeVisibleAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleAnnotationsAttrInfo runtimeVisibleAnnotationsAttrInfo)
    {
        markCpUtf8Entry(classFile, runtimeVisibleAnnotationsAttrInfo.u2attrNameIndex);

        // Mark the UTF-8 entries referenced by the annotations.
        runtimeVisibleAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeInvisibleAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleAnnotationsAttrInfo runtimeInvisibleAnnotationsAttrInfo)
    {
        markCpUtf8Entry(classFile, runtimeInvisibleAnnotationsAttrInfo.u2attrNameIndex);

        // Mark the UTF-8 entries referenced by the annotations.
        runtimeInvisibleAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeVisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleParameterAnnotationsAttrInfo runtimeVisibleParameterAnnotationsAttrInfo)
    {
        markCpUtf8Entry(classFile, runtimeVisibleParameterAnnotationsAttrInfo.u2attrNameIndex);

        // Mark the UTF-8 entries referenced by the annotations.
        runtimeVisibleParameterAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeInvisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleParameterAnnotationsAttrInfo runtimeInvisibleParameterAnnotationsAttrInfo)
    {
        markCpUtf8Entry(classFile, runtimeInvisibleParameterAnnotationsAttrInfo.u2attrNameIndex);

        // Mark the UTF-8 entries referenced by the annotations.
        runtimeInvisibleParameterAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitAnnotationDefaultAttrInfo(ClassFile classFile, AnnotationDefaultAttrInfo annotationDefaultAttrInfo)
    {
        markCpUtf8Entry(classFile, annotationDefaultAttrInfo.u2attrNameIndex);

        // Mark the UTF-8 entries referenced by the element value.
        annotationDefaultAttrInfo.defaultValueAccept(classFile, this);
    }


    // Implementations for InnerClassesInfoVisitor.

    public void visitInnerClassesInfo(ClassFile classFile, InnerClassesInfo innerClassesInfo)
    {
        if (innerClassesInfo.u2innerNameIndex != 0)
        {
            markCpUtf8Entry(classFile, innerClassesInfo.u2innerNameIndex);
        }
    }


    // Implementations for LocalVariableInfoVisitor.

    public void visitLocalVariableInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableInfo localVariableInfo)
    {
        markCpUtf8Entry(classFile, localVariableInfo.u2nameIndex);
        markCpUtf8Entry(classFile, localVariableInfo.u2descriptorIndex);
    }


    // Implementations for LocalVariableTypeInfoVisitor.

    public void visitLocalVariableTypeInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeInfo localVariableTypeInfo)
    {
        markCpUtf8Entry(classFile, localVariableTypeInfo.u2nameIndex);
        markCpUtf8Entry(classFile, localVariableTypeInfo.u2signatureIndex);
    }


    // Implementations for AnnotationVisitor.

    public void visitAnnotation(ClassFile classFile, Annotation annotation)
    {
        markCpUtf8Entry(classFile, annotation.u2typeIndex);

        // Mark the UTF-8 entries referenced by the element values.
        annotation.elementValuesAccept(classFile, this);
    }


    // Implementations for ElementValueVisitor.

    public void visitConstantElementValue(ClassFile classFile, Annotation annotation, ConstantElementValue constantElementValue)
    {
        if (constantElementValue.u2elementName != 0)
        {
            markCpUtf8Entry(classFile, constantElementValue.u2elementName);
        }

        // Only the string constant element value refers to a UTF-8 entry.
        if (constantElementValue.u1tag == ClassConstants.ELEMENT_VALUE_STRING_CONSTANT)
        {
            markCpUtf8Entry(classFile, constantElementValue.u2constantValueIndex);
        }
    }


    public void visitEnumConstantElementValue(ClassFile classFile, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        if (enumConstantElementValue.u2elementName != 0)
        {
            markCpUtf8Entry(classFile, enumConstantElementValue.u2elementName);
        }

        markCpUtf8Entry(classFile, enumConstantElementValue.u2typeNameIndex);
        markCpUtf8Entry(classFile, enumConstantElementValue.u2constantNameIndex);
    }


    public void visitClassElementValue(ClassFile classFile, Annotation annotation, ClassElementValue classElementValue)
    {
        if (classElementValue.u2elementName != 0)
        {
            markCpUtf8Entry(classFile, classElementValue.u2elementName);
        }

        markCpUtf8Entry(classFile, classElementValue.u2classInfoIndex);
    }


    public void visitAnnotationElementValue(ClassFile classFile, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        if (annotationElementValue.u2elementName != 0)
        {
            markCpUtf8Entry(classFile, annotationElementValue.u2elementName);
        }

        // Mark the UTF-8 entries referenced by the annotation.
        annotationElementValue.annotationAccept(classFile, this);
    }


    public void visitArrayElementValue(ClassFile classFile, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        if (arrayElementValue.u2elementName != 0)
        {
            markCpUtf8Entry(classFile, arrayElementValue.u2elementName);
        }

        // Mark the UTF-8 entries referenced by the element values.
        arrayElementValue.elementValuesAccept(classFile, annotation, this);
    }


    // Small utility methods.

    /**
     * Marks the given UTF-8 constant pool entry of the given class.
     */
    private void markCpUtf8Entry(ClassFile classFile, int index)
    {
         markAsUsed((Utf8CpInfo)((ProgramClassFile)classFile).getCpEntry(index));
    }


    /**
     * Marks the given VisitorAccepter as being used.
     * In this context, the VisitorAccepter will be a Utf8CpInfo object.
     */
    private static void markAsUsed(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(USED);
    }


    /**
     * Returns whether the given VisitorAccepter has been marked as being used.
     * In this context, the VisitorAccepter will be a Utf8CpInfo object.
     */
    static boolean isUsed(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter.getVisitorInfo() == USED;
    }
}
