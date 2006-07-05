/* $Id: ClassFileRenamer.java,v 1.37 2004/11/20 15:41:24 eric Exp $
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
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;

/**
 * This <code>ClassFileVisitor</code> renames the class names and class member
 * names of the classes it visits, using names previously determined by the
 * obfuscator. It can also make package visible classes and class members public,
 * and it can replace the source file attribute by a given constant string.
 *
 * @see ClassFileObfuscator
 *
 * @author Eric Lafortune
 */
public class ClassFileRenamer
  implements ClassFileVisitor,
             MemberInfoVisitor,
             CpInfoVisitor,
             AttrInfoVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor,
             AnnotationVisitor,
             ElementValueVisitor
{
    private MyNameAndTypeRenamer nameAndTypeRenamer = new MyNameAndTypeRenamer();

    private boolean openUpPackages;
    private String  newSourceFileAttribute;


    /**
     * Creates a new ClassFileRenamer.
     * @param openUpPackages         specifies whether to make package visible
     *                               classes and class members public.
     * @param newSourceFileAttribute the new string to be put in the source file
     *                               attribute (if present) of the visited classes,
     *                               or <code>null</code> to leave it unchanged.
     */
    public ClassFileRenamer(boolean openUpPackages,
                            String  newSourceFileAttribute)
    {
        this.openUpPackages         = openUpPackages;
        this.newSourceFileAttribute = newSourceFileAttribute;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Rename class members.
        programClassFile.fieldsAccept(this);
        programClassFile.methodsAccept(this);

        // Rename NameAndTypeCpInfo type references in the constant pool.
        programClassFile.constantPoolEntriesAccept(nameAndTypeRenamer);

        // Rename class references and class member references in the constant pool.
        programClassFile.constantPoolEntriesAccept(this);

        // Make package visible classes public, if specified.
        if (openUpPackages && isPackageVisible(programClassFile.u2accessFlags))
        {
            programClassFile.u2accessFlags = makePublic(programClassFile.u2accessFlags);
        }

        programClassFile.attributesAccept(this);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile) {}


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
        String name       = programMemberInfo.getName(programClassFile);
        String descriptor = programMemberInfo.getDescriptor(programClassFile);

        // The new name is stored with the class member.
        String newName = MemberInfoObfuscator.newMemberName(programMemberInfo);
        if (newName != null &&
            !newName.equals(name))
        {
            programMemberInfo.u2nameIndex =
                createUtf8CpInfo(programClassFile, newName);
        }

        // Compute the new descriptor.
        String newDescriptor = newDescriptor(programMemberInfo.getDescriptor(programClassFile),
                                             programMemberInfo.referencedClassFiles);
        if (newDescriptor != null)
        {
            programMemberInfo.u2descriptorIndex =
                createUtf8CpInfo(programClassFile, newDescriptor);
        }

        // Make package visible class members public, if specified.
        if (openUpPackages && isPackageVisible(programMemberInfo.u2accessFlags))
        {
            programMemberInfo.u2accessFlags = makePublic(programMemberInfo.u2accessFlags);
        }
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}
    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo) {}


    /**
     * This CpInfoVisitor renames all type elements in all NameAndTypeCpInfo
     * constant pool entries it visits.
     */
    private class MyNameAndTypeRenamer
       implements CpInfoVisitor
    {
        // Implementations for CpInfoVisitor.

        public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
        public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
        public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
        public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
        public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo) {}
        public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}
        public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo) {}
        public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo) {}
        public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo) {}
        public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo) {}


        public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo)
        {
            // Compute the new descriptor.
            String newDescriptor = newDescriptor(nameAndTypeCpInfo.getType(classFile),
                                                 nameAndTypeCpInfo.referencedClassFiles);
            if (newDescriptor != null)
            {
                nameAndTypeCpInfo.u2descriptorIndex =
                    createUtf8CpInfo((ProgramClassFile)classFile, newDescriptor);
            }
        }
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}


    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo)
    {
        // If the string is being used in a Class.forName construct, the new
        // class name can be retrieved from the referenced ClassFile.
        String newClassName = newClassName(stringCpInfo.getString(classFile),
                                           stringCpInfo.referencedClassFile);
        if (newClassName != null)
        {
            String newExternalClassName = ClassUtil.externalClassName(newClassName);

            // Refer to a new Utf8 entry.
            stringCpInfo.u2stringIndex =
                createUtf8CpInfo((ProgramClassFile)classFile, newExternalClassName);
        }
    }


    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        // Compute the new class name (or type).
        String newClassName = newClassName(classCpInfo.getName(classFile),
                                           classCpInfo.referencedClassFile);
        if (newClassName != null)
        {
            // Refer to a new Utf8 entry.
            classCpInfo.u2nameIndex =
                createUtf8CpInfo((ProgramClassFile)classFile, newClassName);
        }
    }


    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo)
    {
        visitRefCpInfo(classFile, fieldrefCpInfo);
    }


    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo)
    {
        visitRefCpInfo(classFile, interfaceMethodrefCpInfo);
    }


    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo)
    {
        visitRefCpInfo(classFile, methodrefCpInfo);
    }


    private void visitRefCpInfo(ClassFile classFile, RefCpInfo refCpInfo)
    {
        // Compute the new class member name.
        String newMemberName = newMemberName(refCpInfo.referencedMemberInfo);

        if (newMemberName != null)
        {
            // Refer to a new NameAndType entry.
            refCpInfo.u2nameAndTypeIndex =
                createNameAndTypeCpInfo((ProgramClassFile)classFile,
                                        newMemberName,
                                        refCpInfo.getType(classFile));
        }
    }


    // Implementations for AttrInfoVisitor.

    public void visitUnknownAttrInfo(ClassFile classFile, UnknownAttrInfo unknownAttrInfo) {}
    public void visitInnerClassesAttrInfo(ClassFile classFile, InnerClassesAttrInfo innerClassesAttrInfo) {}
    public void visitConstantValueAttrInfo(ClassFile classFile, FieldInfo fieldInfo, ConstantValueAttrInfo constantValueAttrInfo) {}
    public void visitExceptionsAttrInfo(ClassFile classFile, MethodInfo methodInfo, ExceptionsAttrInfo exceptionsAttrInfo) {}
    public void visitCodeAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo) {}
    public void visitLineNumberTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LineNumberTableAttrInfo lineNumberTableAttrInfo) {}
    public void visitDeprecatedAttrInfo(ClassFile classFile, DeprecatedAttrInfo deprecatedAttrInfo) {}
    public void visitSyntheticAttrInfo(ClassFile classFile, SyntheticAttrInfo syntheticAttrInfo) {}


    public void visitEnclosingMethodAttrInfo(ClassFile classFile, EnclosingMethodAttrInfo enclosingMethodAttrInfo)
    {
        // Compute the new class member name.
        String newMethodName = newMemberName(enclosingMethodAttrInfo.referencedMethodInfo);

        if (newMethodName != null)
        {
            // Refer to a new NameAndType entry.
            enclosingMethodAttrInfo.u2nameAndTypeIndex =
                createNameAndTypeCpInfo((ProgramClassFile)classFile,
                                        newMethodName,
                                        enclosingMethodAttrInfo.getType(classFile));
        }
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


    public void visitSourceFileAttrInfo(ClassFile classFile, SourceFileAttrInfo sourceFileAttrInfo)
    {
        // Rename the source file attribute, if specified.
        if (newSourceFileAttribute != null)
        {
            sourceFileAttrInfo.u2sourceFileIndex =
                createUtf8CpInfo((ProgramClassFile)classFile, newSourceFileAttribute);
        }
    }


    public void visitSourceDirAttrInfo(ClassFile classFile, SourceDirAttrInfo sourceDirAttrInfo)
    {
        // Rename the source file attribute, if specified.
        if (newSourceFileAttribute != null)
        {
            sourceDirAttrInfo.u2sourceDirIndex =
                createUtf8CpInfo((ProgramClassFile)classFile, newSourceFileAttribute);
        }
    }


    public void visitSignatureAttrInfo(ClassFile classFile, SignatureAttrInfo signatureAttrInfo)
    {
        // Compute the new signature.
        String newSignature = newDescriptor(classFile.getCpString(signatureAttrInfo.u2signatureIndex),
                                            signatureAttrInfo.referencedClassFiles);
        if (newSignature != null)
        {
            signatureAttrInfo.u2signatureIndex =
                createUtf8CpInfo((ProgramClassFile)classFile, newSignature);
        }
    }


    public void visitRuntimeVisibleAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleAnnotationsAttrInfo runtimeVisibleAnnotationsAttrInfo)
    {
        // Rename the annotations.
        runtimeVisibleAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeInvisibleAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleAnnotationsAttrInfo runtimeInvisibleAnnotationsAttrInfo)
    {
        // Rename the annotations.
        runtimeInvisibleAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeVisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleParameterAnnotationsAttrInfo runtimeVisibleParameterAnnotationsAttrInfo)
    {
        // Rename the annotations.
        runtimeVisibleParameterAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeInvisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleParameterAnnotationsAttrInfo runtimeInvisibleParameterAnnotationsAttrInfo)
    {
        // Rename the annotations.
        runtimeInvisibleParameterAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitAnnotationDefaultAttrInfo(ClassFile classFile, AnnotationDefaultAttrInfo annotationDefaultAttrInfo)
    {
        // Rename the annotation.
        annotationDefaultAttrInfo.defaultValueAccept(classFile, this);
    }


    // Implementations for LocalVariableInfoVisitor.

    public void visitLocalVariableInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableInfo localVariableInfo)
    {
        // Compute the new descriptor.
        String newDescriptor = newClassName(classFile.getCpString(localVariableInfo.u2descriptorIndex),
                                            localVariableInfo.referencedClassFile);
        if (newDescriptor != null)
        {
            // Refer to a new Utf8 entry.
            localVariableInfo.u2descriptorIndex =
                createUtf8CpInfo((ProgramClassFile)classFile, newDescriptor);
        }
    }


    // Implementations for LocalVariableTypeInfoVisitor.

    public void visitLocalVariableTypeInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeInfo localVariableTypeInfo)
    {
        // Compute the new signature.
        String newSignature = newDescriptor(classFile.getCpString(localVariableTypeInfo.u2signatureIndex),
                                            localVariableTypeInfo.referencedClassFiles);
        if (newSignature != null)
        {
            localVariableTypeInfo.u2signatureIndex =
                createUtf8CpInfo((ProgramClassFile)classFile, newSignature);
        }
    }


    // Implementations for AnnotationVisitor.

    public void visitAnnotation(ClassFile classFile, Annotation annotation)
    {
        // Compute the new type name.
        String newTypeName = newDescriptor(classFile.getCpString(annotation.u2typeIndex),
                                           annotation.referencedClassFiles);
        if (newTypeName != null)
        {
            // Refer to a new Utf8 entry.
            annotation.u2typeIndex =
                createUtf8CpInfo((ProgramClassFile)classFile, newTypeName);
        }

        // Rename the element values.
        annotation.elementValuesAccept(classFile, this);
    }


    // Implementations for ElementValueVisitor.

    public void visitConstantElementValue(ClassFile classFile, Annotation annotation, ConstantElementValue constantElementValue)
    {
        renameElementValue(classFile, constantElementValue);
    }


    public void visitEnumConstantElementValue(ClassFile classFile, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        renameElementValue(classFile, enumConstantElementValue);

        // Compute the new type name.
        String newTypeName = newDescriptor(classFile.getCpString(enumConstantElementValue.u2typeNameIndex),
                                           enumConstantElementValue.referencedClassFiles);
        if (newTypeName != null)
        {
            // Refer to a new Utf8 entry.
            enumConstantElementValue.u2typeNameIndex =
                createUtf8CpInfo((ProgramClassFile)classFile, newTypeName);
        }
    }


    public void visitClassElementValue(ClassFile classFile, Annotation annotation, ClassElementValue classElementValue)
    {
        renameElementValue(classFile, classElementValue);

        // Compute the new class name.
        String newClassName = newDescriptor(classFile.getCpString(classElementValue.u2classInfoIndex),
                                            classElementValue.referencedClassFiles);

        if (newClassName != null)
        {
            // Refer to a new Utf8 entry.
            classElementValue.u2classInfoIndex =
                createUtf8CpInfo((ProgramClassFile)classFile, newClassName);
        }
    }


    public void visitAnnotationElementValue(ClassFile classFile, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        renameElementValue(classFile, annotationElementValue);

        // Rename the annotation.
        annotationElementValue.annotationAccept(classFile, this);
    }


    public void visitArrayElementValue(ClassFile classFile, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        renameElementValue(classFile, arrayElementValue);

        // Rename the element values.
        arrayElementValue.elementValuesAccept(classFile, annotation, this);
    }


    /**
     * Renames the method reference of the element value, if any.
     */
    public void renameElementValue(ClassFile classFile, ElementValue elementValue)
    {
        // Compute the new class member name.
        String newMethodName = newMemberName(elementValue.referencedMethodInfo);

        if (newMethodName != null)
        {
            // Refer to a new NameAndType entry.
            elementValue.u2elementName =
                createUtf8CpInfo((ProgramClassFile)classFile, newMethodName);
        }
    }


    // Small utility methods.

    /**
     * Finds or creates a NameAndTypeCpInfo constant pool entry with the given
     * name and type, in the given class file.
     * @return the constant pool index of the NameAndTypeCpInfo.
     */
    private int createNameAndTypeCpInfo(ProgramClassFile programClassFile,
                                        String           name,
                                        String           type)
    {
        CpInfo[] constantPool        = programClassFile.constantPool;
        int      u2constantPoolCount = programClassFile.u2constantPoolCount;

        // Pick up the right list of referenced class files, in case we need to
        // create a new NameAndTypeCpInfo.
        ClassFile[] referencedClassFiles = null;

        // Check if there is a NameAndTypeCpInfo with the given name and type already.
        for (int index = 1; index < u2constantPoolCount; index++)
        {
            CpInfo cpInfo = constantPool[index];

            if (cpInfo != null &&
                cpInfo.getTag() == ClassConstants.CONSTANT_NameAndType)
            {
                NameAndTypeCpInfo nameAndTypeCpInfo = (NameAndTypeCpInfo)cpInfo;
                if (nameAndTypeCpInfo.getType(programClassFile).equals(type))
                {
                    if (nameAndTypeCpInfo.getName(programClassFile).equals(name))
                    {
                        return index;
                    }

                    referencedClassFiles = nameAndTypeCpInfo.referencedClassFiles;
                }
            }
        }

        int u2nameIndex       = createUtf8CpInfo(programClassFile, name);
        int u2descriptorIndex = createUtf8CpInfo(programClassFile, type);

        return addCpInfo(programClassFile, new NameAndTypeCpInfo(u2nameIndex,
                                                                 u2descriptorIndex,
                                                                 referencedClassFiles));
    }


    /**
     * Finds or creates an Utf8CpInfo constant pool entry for the given string,
     * in the given class file.
     * @return the constant pool index of the Utf8CpInfo.
     */
    private int createUtf8CpInfo(ProgramClassFile programClassFile, String string)
    {
        CpInfo[] constantPool        = programClassFile.constantPool;
        int      u2constantPoolCount = programClassFile.u2constantPoolCount;

        // Check if there is a Utf8CpInfo with the given string already.
        for (int index = 1; index < u2constantPoolCount; index++)
        {
            CpInfo cpInfo = constantPool[index];

            if (cpInfo != null &&
                cpInfo.getTag() == ClassConstants.CONSTANT_Utf8)
            {
                Utf8CpInfo utf8CpInfo = (Utf8CpInfo)cpInfo;
                if (utf8CpInfo.getString().equals(string))
                {
                    return index;
                }
            }
        }

        return addCpInfo(programClassFile, new Utf8CpInfo(string));
    }


    /**
     * Adds a given constant pool entry to the end of the constant pool.
     * @return the constant pool index for the added entry.
     */
    private int addCpInfo(ProgramClassFile programClassFile, CpInfo cpInfo)
    {
        CpInfo[] constantPool        = programClassFile.constantPool;
        int      u2constantPoolCount = programClassFile.u2constantPoolCount;

        // Make sure there is enough space for another constant pool entry.
        if (u2constantPoolCount == constantPool.length)
        {
            programClassFile.constantPool = new CpInfo[u2constantPoolCount+1];
            System.arraycopy(constantPool, 0,
                             programClassFile.constantPool, 0,
                             u2constantPoolCount);
            constantPool = programClassFile.constantPool;

        }

        // Create a new Utf8CpInfo for the given string.
        constantPool[programClassFile.u2constantPoolCount++] = cpInfo;

        return u2constantPoolCount;
    }


    /**
     * Returns the new descriptor based on the given descriptor and the new
     * names of the given referenced class files.
     */
    private String newDescriptor(String      descriptor,
                                 ClassFile[] referencedClassFiles)
    {
        // If there are no referenced classes, the descriptor doesn't change.
        if (referencedClassFiles == null)
        {
            return null;
        }

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

        // If the descriptor hasn't changed after all, just return null.
        if (descriptor.equals(newDescriptor))
        {
            return null;
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
        // If there is no referenced class, the descriptor doesn't change.
        if (referencedClassFile == null)
        {
            return null;
        }

        String newClassName =
            ClassFileObfuscator.newClassName(referencedClassFile);

        // If there is no new class name, the descriptor doesn't change.
        if (newClassName == null)
        {
            return null;
        }

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


    /**
     * Returns the new class member name based on the given referenced class
     * member.
     */
    private String newMemberName(MemberInfo referencedMemberInfo)
    {
        if (referencedMemberInfo == null)
        {
            return null;
        }

        return MemberInfoObfuscator.newMemberName(referencedMemberInfo);
    }


    /**
     * Returns whether the given access flags specify a package visible class
     * or class member (including public or protected access).
     */
    private boolean isPackageVisible(int accessFlags)
    {
        return AccessUtil.accessLevel(accessFlags) >= AccessUtil.PACKAGE_VISIBLE;
    }


    /**
     * Returns the given access flags, modified such that the class or class
     * member becomes public.
     */
    private int makePublic(int accessFlags)
    {
        return AccessUtil.replaceAccessFlags(accessFlags,
                                             ClassConstants.INTERNAL_ACC_PUBLIC);
    }
}
