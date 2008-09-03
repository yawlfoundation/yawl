/* $Id: ClassFileCleaner.java,v 1.18.2.2 2007/01/18 21:31:51 eric Exp $
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
package proguard.classfile.visitor;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;

/**
 * This <code>ClassFileVisitor</code> removes all visitor information of the
 * class files it visits.
 *
 * @author Eric Lafortune
 */
public class ClassFileCleaner
  implements ClassFileVisitor,
             CpInfoVisitor,
             MemberInfoVisitor,
             AttrInfoVisitor,
             ExceptionInfoVisitor,
             InnerClassesInfoVisitor,
             AnnotationVisitor,
             ElementValueVisitor
{
    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        clean(programClassFile);

        programClassFile.constantPoolEntriesAccept(this);

        programClassFile.fieldsAccept(this);
        programClassFile.methodsAccept(this);

        programClassFile.attributesAccept(this);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        clean(libraryClassFile);

        libraryClassFile.fieldsAccept(this);
        libraryClassFile.methodsAccept(this);
    }


    // Implementations for CpInfoVisitor.

    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        clean(classCpInfo);
    }


    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo)
    {
        clean(doubleCpInfo);
    }


    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo)
    {
        clean(fieldrefCpInfo);
    }


    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo)
    {
        clean(floatCpInfo);
    }


    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo)
    {
        clean(integerCpInfo);
    }


    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo)
    {
        clean(interfaceMethodrefCpInfo);
    }


    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo)
    {
        clean(longCpInfo);
    }


    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo)
    {
        clean(methodrefCpInfo);
    }


    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo)
    {
        clean(nameAndTypeCpInfo);
    }


    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo)
    {
        clean(stringCpInfo);
    }


    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo)
    {
        clean(utf8CpInfo);
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
        clean(programMemberInfo);

        programMemberInfo.attributesAccept(programClassFile, this);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
        clean(libraryFieldInfo);
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        clean(libraryMethodInfo);
    }


    // Implementations for AttrInfoVisitor.

    public void visitUnknownAttrInfo(ClassFile classFile, UnknownAttrInfo unknownAttrInfo)
    {
        clean(unknownAttrInfo);
    }


    public void visitInnerClassesAttrInfo(ClassFile classFile, InnerClassesAttrInfo innerClassesAttrInfo)
    {
        clean(innerClassesAttrInfo);

        innerClassesAttrInfo.innerClassEntriesAccept(classFile, this);
    }


    public void visitEnclosingMethodAttrInfo(ClassFile classFile, EnclosingMethodAttrInfo enclosingMethodAttrInfo)
    {
        clean(enclosingMethodAttrInfo);
    }


    public void visitConstantValueAttrInfo(ClassFile classFile, FieldInfo fieldInfo, ConstantValueAttrInfo constantValueAttrInfo)
    {
        clean(constantValueAttrInfo);
    }


    public void visitExceptionsAttrInfo(ClassFile classFile, MethodInfo methodInfo, ExceptionsAttrInfo exceptionsAttrInfo)
    {
        clean(exceptionsAttrInfo);

        exceptionsAttrInfo.exceptionEntriesAccept((ProgramClassFile)classFile, this);
    }


    public void visitCodeAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo)
    {
        clean(codeAttrInfo);

        codeAttrInfo.exceptionsAccept(classFile, methodInfo, this);
        codeAttrInfo.attributesAccept(classFile, methodInfo, this);
    }


    public void visitLineNumberTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LineNumberTableAttrInfo lineNumberTableAttrInfo)
    {
        clean(lineNumberTableAttrInfo);
    }


    public void visitLocalVariableTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTableAttrInfo localVariableTableAttrInfo)
    {
        clean(localVariableTableAttrInfo);
    }


    public void visitLocalVariableTypeTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeTableAttrInfo localVariableTypeTableAttrInfo)
    {
        clean(localVariableTypeTableAttrInfo);
    }


    public void visitSourceFileAttrInfo(ClassFile classFile, SourceFileAttrInfo sourceFileAttrInfo)
    {
        clean(sourceFileAttrInfo);
    }


    public void visitSourceDirAttrInfo(ClassFile classFile, SourceDirAttrInfo sourceDirAttrInfo)
    {
        clean(sourceDirAttrInfo);
    }


    public void visitDeprecatedAttrInfo(ClassFile classFile, DeprecatedAttrInfo deprecatedAttrInfo)
    {
        clean(deprecatedAttrInfo);
    }


    public void visitSyntheticAttrInfo(ClassFile classFile, SyntheticAttrInfo syntheticAttrInfo)
    {
        clean(syntheticAttrInfo);
    }


    public void visitSignatureAttrInfo(ClassFile classFile, SignatureAttrInfo signatureAttrInfo)
    {
        clean(signatureAttrInfo);
    }


    public void visitRuntimeVisibleAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleAnnotationsAttrInfo runtimeVisibleAnnotationsAttrInfo)
    {
        clean(runtimeVisibleAnnotationsAttrInfo);

        runtimeVisibleAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeInvisibleAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleAnnotationsAttrInfo runtimeInvisibleAnnotationsAttrInfo)
    {
        clean(runtimeInvisibleAnnotationsAttrInfo);

        runtimeInvisibleAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeVisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleParameterAnnotationsAttrInfo runtimeVisibleParameterAnnotationsAttrInfo)
    {
        clean(runtimeVisibleParameterAnnotationsAttrInfo);

        runtimeVisibleParameterAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeInvisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleParameterAnnotationsAttrInfo runtimeInvisibleParameterAnnotationsAttrInfo)
    {
        clean(runtimeInvisibleParameterAnnotationsAttrInfo);

        runtimeInvisibleParameterAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitAnnotationDefaultAttrInfo(ClassFile classFile, AnnotationDefaultAttrInfo annotationDefaultAttrInfo)
    {
        clean(annotationDefaultAttrInfo);

        annotationDefaultAttrInfo.defaultValueAccept(classFile, this);
    }


    // Implementations for InnerClassesInfoVisitor.

    public void visitInnerClassesInfo(ClassFile classFile, InnerClassesInfo innerClassesInfo)
    {
        clean(innerClassesInfo);
    }


    // Implementations for ExceptionInfoVisitor.

    public void visitExceptionInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, ExceptionInfo exceptionInfo)
    {
        clean(exceptionInfo);
    }


    // Implementations for AnnotationVisitor.

    public void visitAnnotation(ClassFile classFile, Annotation annotation)
    {
        clean(annotation);

        annotation.elementValuesAccept(classFile, this);
    }


    // Implementations for ElementValueVisitor.

    public void visitConstantElementValue(ClassFile classFile, Annotation annotation, ConstantElementValue constantElementValue)
    {
        clean(constantElementValue);
    }


    public void visitEnumConstantElementValue(ClassFile classFile, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        clean(enumConstantElementValue);
    }


    public void visitClassElementValue(ClassFile classFile, Annotation annotation, ClassElementValue classElementValue)
    {
        clean(classElementValue);
    }


    public void visitAnnotationElementValue(ClassFile classFile, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        clean(annotationElementValue);

        annotationElementValue.annotationAccept(classFile, this);
    }


    public void visitArrayElementValue(ClassFile classFile, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        clean(arrayElementValue);
    }


    // Small utility methods.

    private void clean(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(null);
    }
}
