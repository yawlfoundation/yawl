/* $Id: ClassFileReferenceFixer.java,v 1.4.2.3 2007/01/18 21:31:51 eric Exp $
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
package proguard.classfile.editor;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;

/**
 * This ClassFileVisitor fixes constant pool references to classes whose
 * names have changed. Descriptors of member references are not updated yet.
 *
 * @see MemberReferenceFixer
 * @author Eric Lafortune
 */
public class ClassFileReferenceFixer
  implements ClassFileVisitor,
             CpInfoVisitor,
             MemberInfoVisitor,
             AttrInfoVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor,
             AnnotationVisitor,
             ElementValueVisitor
{
    private boolean ensureUniqueMemberNames;


    private ConstantPoolEditor constantPoolEditor = new ConstantPoolEditor();


    /**
     * Creates a new ClassFileReferenceFixer.
     * @param ensureUniqueMemberNames specifies whether class members whose
     *                                descriptor changes should get new, unique
     *                                names, in order to avoid naming conflicts
     *                                with similar methods.
     */
    public ClassFileReferenceFixer(boolean ensureUniqueMemberNames)
    {
        this.ensureUniqueMemberNames = ensureUniqueMemberNames;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Fix the constant pool.
        programClassFile.constantPoolEntriesAccept(this);

        // Fix class members.
        programClassFile.fieldsAccept(this);
        programClassFile.methodsAccept(this);

        // Fix the attributes.
        programClassFile.attributesAccept(this);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        // Fix class members.
        libraryClassFile.fieldsAccept(this);
        libraryClassFile.methodsAccept(this);
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        // Has the descriptor changed?
        String descriptor    = programFieldInfo.getDescriptor(programClassFile);
        String newDescriptor = newDescriptor(descriptor,
                                             programFieldInfo.referencedClassFile);

        if (!descriptor.equals(newDescriptor))
        {
            // Update the descriptor.
            programFieldInfo.u2descriptorIndex =
                constantPoolEditor.addUtf8CpInfo(programClassFile, newDescriptor);

            // Update the name, if requested.
            if (ensureUniqueMemberNames)
            {
                String name    = programFieldInfo.getName(programClassFile);
                String newName = newUniqueMemberName(name, descriptor);
                programFieldInfo.u2nameIndex =
                    constantPoolEditor.addUtf8CpInfo(programClassFile, newName);
            }
        }

        // Fix the attributes.
        programFieldInfo.attributesAccept(programClassFile, this);
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        // Has the descriptor changed?
        String descriptor    = programMethodInfo.getDescriptor(programClassFile);
        String newDescriptor = newDescriptor(descriptor,
                                             programMethodInfo.referencedClassFiles);

        if (!descriptor.equals(newDescriptor))
        {
            // Update the descriptor.
            programMethodInfo.u2descriptorIndex =
                constantPoolEditor.addUtf8CpInfo(programClassFile, newDescriptor);

            // Update the name, if requested.
            if (ensureUniqueMemberNames)
            {
                String name    = programMethodInfo.getName(programClassFile);
                String newName = newUniqueMemberName(name, descriptor);
                programMethodInfo.u2nameIndex =
                    constantPoolEditor.addUtf8CpInfo(programClassFile, newName);
            }
        }

        // Fix the attributes.
        programMethodInfo.attributesAccept(programClassFile, this);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
        // Has the descriptor changed?
        String descriptor    = libraryFieldInfo.getDescriptor(libraryClassFile);
        String newDescriptor = newDescriptor(descriptor,
                                             libraryFieldInfo.referencedClassFile);

        // Update the descriptor.
        libraryFieldInfo.descriptor = newDescriptor;
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        // Has the descriptor changed?
        String descriptor    = libraryMethodInfo.getDescriptor(libraryClassFile);
        String newDescriptor = newDescriptor(descriptor,
                                             libraryMethodInfo.referencedClassFiles);

        // Update the descriptor.
        libraryMethodInfo.descriptor = newDescriptor;
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo) {}
    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo) {}
    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}


    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo)
    {
        // Does the string refer to a class file, due to a Class.forName construct?
        ClassFile referencedClassFile = stringCpInfo.referencedClassFile;
        if (referencedClassFile != null)
        {
            // Reconstruct the new class name.
            String externalClassName    = stringCpInfo.getString(classFile);
            String internalClassName    = ClassUtil.internalClassName(externalClassName);
            String newInternalClassName = newClassName(internalClassName,
                                                       referencedClassFile);

            // Update the String entry if required.
            if (!newInternalClassName.equals(internalClassName))
            {
                String newExternalClassName = ClassUtil.externalClassName(newInternalClassName);

                // Refer to a new Utf8 entry.
                stringCpInfo.u2stringIndex =
                    constantPoolEditor.addUtf8CpInfo((ProgramClassFile)classFile,
                                                     newExternalClassName);
            }
        }
    }


    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        // Do we know the referenced class file?
        ClassFile referencedClassFile = classCpInfo.referencedClassFile;
        if (referencedClassFile != null)
        {
            // Has the class name changed?
            String className    = classCpInfo.getName(classFile);
            String newClassName = newClassName(className, referencedClassFile);
            if (!className.equals(newClassName))
            {
                // Refer to a new Utf8 entry.
                classCpInfo.u2nameIndex =
                    constantPoolEditor.addUtf8CpInfo((ProgramClassFile)classFile,
                                                     newClassName);
            }
        }
    }


    // Implementations for AttrInfoVisitor.

    public void visitUnknownAttrInfo(ClassFile classFile, UnknownAttrInfo unknownAttrInfo) {}
    public void visitInnerClassesAttrInfo(ClassFile classFile, InnerClassesAttrInfo innerClassesAttrInfo) {}
    public void visitEnclosingMethodAttrInfo(ClassFile classFile, EnclosingMethodAttrInfo enclosingMethodAttrInfo) {}
    public void visitConstantValueAttrInfo(ClassFile classFile, FieldInfo fieldInfo, ConstantValueAttrInfo constantValueAttrInfo) {}
    public void visitExceptionsAttrInfo(ClassFile classFile, MethodInfo methodInfo, ExceptionsAttrInfo exceptionsAttrInfo) {}
    public void visitLineNumberTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LineNumberTableAttrInfo lineNumberTableAttrInfo) {}
    public void visitSourceFileAttrInfo(ClassFile classFile, SourceFileAttrInfo sourceFileAttrInfo) {}
    public void visitSourceDirAttrInfo(ClassFile classFile, SourceDirAttrInfo sourceDirAttrInfo) {}
    public void visitDeprecatedAttrInfo(ClassFile classFile, DeprecatedAttrInfo deprecatedAttrInfo) {}
    public void visitSyntheticAttrInfo(ClassFile classFile, SyntheticAttrInfo syntheticAttrInfo) {}


    public void visitCodeAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo)
    {
        // Fix the attributes.
        codeAttrInfo.attributesAccept(classFile, methodInfo, this);
    }


    public void visitLocalVariableTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTableAttrInfo localVariableTableAttrInfo)
    {
        // Fix the types of the local variables.
        localVariableTableAttrInfo.localVariablesAccept(classFile, methodInfo, codeAttrInfo, this);
    }


    public void visitLocalVariableTypeTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeTableAttrInfo localVariableTypeTableAttrInfo)
    {
        // Fix the signatures of the local variables.
        localVariableTypeTableAttrInfo.localVariablesAccept(classFile, methodInfo, codeAttrInfo, this);
    }


    public void visitSignatureAttrInfo(ClassFile classFile, SignatureAttrInfo signatureAttrInfo)
    {
        // Compute the new signature.
        String signature    = classFile.getCpString(signatureAttrInfo.u2signatureIndex);
        String newSignature = newDescriptor(signature,
                                            signatureAttrInfo.referencedClassFiles);

        if (!signature.equals(newSignature))
        {
            signatureAttrInfo.u2signatureIndex =
                constantPoolEditor.addUtf8CpInfo((ProgramClassFile)classFile,
                                                 newSignature);
        }
    }


    public void visitRuntimeVisibleAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleAnnotationsAttrInfo runtimeVisibleAnnotationsAttrInfo)
    {
        // Fix the annotations.
        runtimeVisibleAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeInvisibleAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleAnnotationsAttrInfo runtimeInvisibleAnnotationsAttrInfo)
    {
        // Fix the annotations.
        runtimeInvisibleAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeVisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleParameterAnnotationsAttrInfo runtimeVisibleParameterAnnotationsAttrInfo)
    {
        // Fix the annotations.
        runtimeVisibleParameterAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeInvisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleParameterAnnotationsAttrInfo runtimeInvisibleParameterAnnotationsAttrInfo)
    {
        // Fix the annotations.
        runtimeInvisibleParameterAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitAnnotationDefaultAttrInfo(ClassFile classFile, AnnotationDefaultAttrInfo annotationDefaultAttrInfo)
    {
        // Fix the annotation.
        annotationDefaultAttrInfo.defaultValueAccept(classFile, this);
    }


    // Implementations for LocalVariableInfoVisitor.

    public void visitLocalVariableInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableInfo localVariableInfo)
    {
        // Has the descriptor changed?
        String descriptor    = classFile.getCpString(localVariableInfo.u2descriptorIndex);
        String newDescriptor = newDescriptor(descriptor,
                                             localVariableInfo.referencedClassFile);

        if (!descriptor.equals(newDescriptor))
        {
            // Refer to a new Utf8 entry.
            localVariableInfo.u2descriptorIndex =
                constantPoolEditor.addUtf8CpInfo((ProgramClassFile)classFile,
                                                 newDescriptor);
        }
    }


    // Implementations for LocalVariableTypeInfoVisitor.

    public void visitLocalVariableTypeInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeInfo localVariableTypeInfo)
    {
        // Has the signature changed?
        String signature    = classFile.getCpString(localVariableTypeInfo.u2signatureIndex);
        String newSignature = newDescriptor(signature,
                                            localVariableTypeInfo.referencedClassFiles);

        if (!signature.equals(newSignature))
        {
            localVariableTypeInfo.u2signatureIndex =
                constantPoolEditor.addUtf8CpInfo((ProgramClassFile)classFile,
                                                 newSignature);
        }
    }


    // Implementations for AnnotationVisitor.

    public void visitAnnotation(ClassFile classFile, Annotation annotation)
    {
        // Compute the new type name.
        String typeName    = classFile.getCpString(annotation.u2typeIndex);
        String newTypeName = newDescriptor(typeName,
                                           annotation.referencedClassFiles);

        if (!typeName.equals(newTypeName))
        {
            // Refer to a new Utf8 entry.
            annotation.u2typeIndex =
                constantPoolEditor.addUtf8CpInfo((ProgramClassFile)classFile,
                                                 newTypeName);
        }

        // Fix the element values.
        annotation.elementValuesAccept(classFile, this);
    }


    // Implementations for ElementValueVisitor.

    public void visitConstantElementValue(ClassFile classFile, Annotation annotation, ConstantElementValue constantElementValue)
    {
    }


    public void visitEnumConstantElementValue(ClassFile classFile, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        // Compute the new type name.
        String typeName    = classFile.getCpString(enumConstantElementValue.u2typeNameIndex);
        String newTypeName = newDescriptor(typeName,
                                           enumConstantElementValue.referencedClassFiles);

        if (!typeName.equals(newTypeName))
        {
            // Refer to a new Utf8 entry.
            enumConstantElementValue.u2typeNameIndex =
                constantPoolEditor.addUtf8CpInfo((ProgramClassFile)classFile,
                                                 newTypeName);
        }
    }


    public void visitClassElementValue(ClassFile classFile, Annotation annotation, ClassElementValue classElementValue)
    {
        // Compute the new class name.
        String className    = classFile.getCpString(classElementValue.u2classInfoIndex);
        String newClassName = newDescriptor(className,
                                            classElementValue.referencedClassFiles);

        if (!className.equals(newClassName))
        {
            // Refer to a new Utf8 entry.
            classElementValue.u2classInfoIndex =
                constantPoolEditor.addUtf8CpInfo((ProgramClassFile)classFile,
                                                 newClassName);
        }
    }


    public void visitAnnotationElementValue(ClassFile classFile, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        // Fix the annotation.
        annotationElementValue.annotationAccept(classFile, this);
    }


    public void visitArrayElementValue(ClassFile classFile, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        // Fix the element values.
        arrayElementValue.elementValuesAccept(classFile, annotation, this);
    }


    // Small utility methods.

    private static String newDescriptor(String    descriptor,
                                        ClassFile referencedClassFile)
    {
        // If there is no referenced class, the descriptor won't change.
        if (referencedClassFile == null)
        {
            return descriptor;
        }

        // Unravel and reconstruct the class element of the descriptor.
        DescriptorClassEnumeration descriptorClassEnumeration =
            new DescriptorClassEnumeration(descriptor);

        StringBuffer newDescriptorBuffer = new StringBuffer(descriptor.length());
        newDescriptorBuffer.append(descriptorClassEnumeration.nextFluff());

        // Only if the descriptor contains a class name (e.g. with an array of
        // primitive types), the descriptor can change.
        if (descriptorClassEnumeration.hasMoreClassNames())
        {
            String className = descriptorClassEnumeration.nextClassName();
            String fluff     = descriptorClassEnumeration.nextFluff();

            String newClassName = newClassName(className,
                                               referencedClassFile);

            newDescriptorBuffer.append(newClassName);
            newDescriptorBuffer.append(fluff);
        }

        return newDescriptorBuffer.toString();
    }


    private static String newDescriptor(String      descriptor,
                                        ClassFile[] referencedClassFiles)
    {
        // If there are no referenced classes, the descriptor won't change.
        if (referencedClassFiles == null ||
            referencedClassFiles.length == 0)
        {
            return descriptor;
        }

        // Unravel and reconstruct the class elements of the descriptor.
        DescriptorClassEnumeration descriptorClassEnumeration =
            new DescriptorClassEnumeration(descriptor);

        StringBuffer newDescriptorBuffer = new StringBuffer(descriptor.length());
        newDescriptorBuffer.append(descriptorClassEnumeration.nextFluff());

        int index = 0;
        while (descriptorClassEnumeration.hasMoreClassNames())
        {
            String className = descriptorClassEnumeration.nextClassName();
            String fluff     = descriptorClassEnumeration.nextFluff();

            String newClassName = newClassName(className,
                                               referencedClassFiles[index++]);

            newDescriptorBuffer.append(newClassName);
            newDescriptorBuffer.append(fluff);
        }

        return newDescriptorBuffer.toString();
    }


    /**
     * Returns a new unique class member name, based on the given name and
     * descriptor.
     */
    private String newUniqueMemberName(String name, String descriptor)
    {
        // TODO: Avoid duplicate constructors.
        return name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT) ?
            ClassConstants.INTERNAL_METHOD_NAME_INIT :
            name + '$' + Long.toHexString(Math.abs((descriptor).hashCode()));
    }


    /**
     * Returns the new class name based on the given class name and the new
     * name of the given referenced class file. Class names of array types
     * are handled properly.
     */
    private static String newClassName(String    className,
                                       ClassFile referencedClassFile)
    {
        // If there is no referenced class, the class name won't change.
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
