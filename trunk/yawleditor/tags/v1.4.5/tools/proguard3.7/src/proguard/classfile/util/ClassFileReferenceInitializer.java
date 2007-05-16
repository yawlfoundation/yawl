/* $Id: ClassFileReferenceInitializer.java,v 1.31.2.4 2006/11/25 16:56:11 eric Exp $
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
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.visitor.*;

/**
 * This ClassFileVisitor initializes the references of all class files that
 * it visits.
 * <p>
 * All class constant pool entries get direct references to the corresponding
 * classes. These references make it more convenient to travel up and across
 * the class hierarchy.
 * <p>
 * All field and method reference constant pool entries get direct references
 * to the corresponding classes, fields, and methods.
 * <p>
 * All name and type constant pool entries get a list of direct references to
 * the classes listed in the type.
 * <p>
 * This visitor optionally prints warnings if some items can't be found.
 * <p>
 * The class file hierarchy must be initialized before using this visitor.
 *
 * @author Eric Lafortune
 */
public class ClassFileReferenceInitializer
  implements ClassFileVisitor,
             MemberInfoVisitor,
             CpInfoVisitor,
             AttrInfoVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor,
             AnnotationVisitor,
             ElementValueVisitor
{
    private MemberFinder memberFinder = new MemberFinder();

    private ClassPool      programClassPool;
    private ClassPool      libraryClassPool;
    private WarningPrinter warningPrinter;


    /**
     * Creates a new ClassFileReferenceInitializer that initializes the
     * references of all visited class files, optionally printing warnings if
     * some classes can't be found.
     */
    public ClassFileReferenceInitializer(ClassPool      programClassPool,
                                         ClassPool      libraryClassPool,
                                         WarningPrinter warningPrinter)
    {
        this.programClassPool = programClassPool;
        this.libraryClassPool = libraryClassPool;
        this.warningPrinter   = warningPrinter;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Initialize the constant pool entries.
        programClassFile.constantPoolEntriesAccept(this);

        // Initialize all fields and methods.
        programClassFile.fieldsAccept(this);
        programClassFile.methodsAccept(this);

        // Initialize the attributes.
        programClassFile.attributesAccept(this);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        // Initialize all fields and methods.
        libraryClassFile.fieldsAccept(this);
        libraryClassFile.methodsAccept(this);
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        programFieldInfo.referencedClassFile =
            findReferencedClass(programFieldInfo.getDescriptor(programClassFile));

        // Initialize the attributes.
        programFieldInfo.attributesAccept(programClassFile, this);
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        programMethodInfo.referencedClassFiles =
            findReferencedClasses(programMethodInfo.getDescriptor(programClassFile));

        // Initialize the attributes.
        programMethodInfo.attributesAccept(programClassFile, this);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
        libraryFieldInfo.referencedClassFile =
            findReferencedClass(libraryFieldInfo.getDescriptor(libraryClassFile));
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        libraryMethodInfo.referencedClassFiles =
            findReferencedClasses(libraryMethodInfo.getDescriptor(libraryClassFile));
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}


    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo)
    {
        visitRefCpInfo(classFile, fieldrefCpInfo, true);
    }


    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo)
    {
        visitRefCpInfo(classFile, interfaceMethodrefCpInfo, false);
    }


    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo)
    {
        visitRefCpInfo(classFile, methodrefCpInfo, false);
    }


    private void visitRefCpInfo(ClassFile classFile, RefCpInfo refCpInfo, boolean isFieldRef)
    {
        String className = refCpInfo.getClassName(classFile);

        // See if we can find the referenced class file.
        // Unresolved references are assumed to refer to library class files
        // that will not change anyway.
        ClassFile referencedClassFile = findClass(className);

        if (referencedClassFile != null)
        {
            String name = refCpInfo.getName(classFile);
            String type = refCpInfo.getType(classFile);

            // See if we can find the referenced class member somewhere in the
            // hierarchy.
            refCpInfo.referencedMemberInfo = memberFinder.findMember(classFile,
                                                                     referencedClassFile,
                                                                     name,
                                                                     type,
                                                                     isFieldRef);
            refCpInfo.referencedClassFile  = memberFinder.correspondingClassFile();

            // Check if we haven't found the class member anywhere in the
            // hierarchy.
            if (refCpInfo.referencedMemberInfo == null &&
                warningPrinter != null)
            {
                warningPrinter.print("Warning: " +
                                     ClassUtil.externalClassName(classFile.getName()) +
                                     ": can't find referenced " +
                                     (isFieldRef ?
                                         "field '"  + ClassUtil.externalFullFieldDescription(0, name, type) :
                                         "method '" + ClassUtil.externalFullMethodDescription(className, 0, name, type)) +
                                     "' in class " +
                                     ClassUtil.externalClassName(className));
            }
        }
    }


    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        classCpInfo.referencedClassFile =
            findClass(classCpInfo.getName(classFile));
    }


    // Implementations for AttrInfoVisitor.

    public void visitUnknownAttrInfo(ClassFile classFile, UnknownAttrInfo unknownAttrInfo) {}
    public void visitInnerClassesAttrInfo(ClassFile classFile, InnerClassesAttrInfo innerClassesAttrInfo) {}
    public void visitConstantValueAttrInfo(ClassFile classFile, FieldInfo fieldInfo, ConstantValueAttrInfo constantValueAttrInfo) {}
    public void visitExceptionsAttrInfo(ClassFile classFile, MethodInfo methodInfo, ExceptionsAttrInfo exceptionsAttrInfo) {}
    public void visitLineNumberTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LineNumberTableAttrInfo lineNumberTableAttrInfo) {}
    public void visitSourceFileAttrInfo(ClassFile classFile, SourceFileAttrInfo sourceFileAttrInfo) {}
    public void visitSourceDirAttrInfo(ClassFile classFile, SourceDirAttrInfo sourceDirAttrInfo) {}
    public void visitDeprecatedAttrInfo(ClassFile classFile, DeprecatedAttrInfo deprecatedAttrInfo) {}
    public void visitSyntheticAttrInfo(ClassFile classFile, SyntheticAttrInfo syntheticAttrInfo) {}


    public void visitEnclosingMethodAttrInfo(ClassFile classFile, EnclosingMethodAttrInfo enclosingMethodAttrInfo)
    {
        String className = enclosingMethodAttrInfo.getClassName(classFile);

        // See if we can find the referenced class file.
        ClassFile referencedClassFile = findClass(className);

        if (referencedClassFile == null)
        {
            // We couldn't find the enclosing class.
            if (warningPrinter != null)
            {
                warningPrinter.print("Warning: " +
                                     ClassUtil.externalClassName(classFile.getName()) +
                                     ": can't find enclosing class " +
                                     ClassUtil.externalClassName(className));
            }

            return;
        }

        // Make sure there is actually an enclosed method.
        if (enclosingMethodAttrInfo.u2nameAndTypeIndex == 0)
        {
            return;
        }

        String name = enclosingMethodAttrInfo.getName(classFile);
        String type = enclosingMethodAttrInfo.getType(classFile);

        // See if we can find the method in the referenced class.
        MethodInfo referencedMethodInfo = referencedClassFile.findMethod(name, type);

        if (referencedMethodInfo == null)
        {
            // We couldn't find the enclosing method.
            if (warningPrinter != null)
            {
                warningPrinter.print("Warning: " +
                                     ClassUtil.externalClassName(classFile.getName()) +
                                     ": can't find enclosing method '" +
                                     ClassUtil.externalFullMethodDescription(className, 0, name, type) +
                                     "' in class " +
                                     ClassUtil.externalClassName(className));
            }

            return;
        }

        // Save the references.
        enclosingMethodAttrInfo.referencedClassFile  = referencedClassFile;
        enclosingMethodAttrInfo.referencedMethodInfo = referencedMethodInfo;
    }


    public void visitCodeAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo)
    {
        // Initialize the nested attributes.
        codeAttrInfo.attributesAccept(classFile, methodInfo, this);
    }


    public void visitLocalVariableTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTableAttrInfo localVariableTableAttrInfo)
    {
        // Initialize the local variables.
        localVariableTableAttrInfo.localVariablesAccept(classFile, methodInfo, codeAttrInfo, this);
    }


    public void visitLocalVariableTypeTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeTableAttrInfo localVariableTypeTableAttrInfo)
    {
        // Initialize the local variable types.
        localVariableTypeTableAttrInfo.localVariablesAccept(classFile, methodInfo, codeAttrInfo, this);
    }


    public void visitSignatureAttrInfo(ClassFile classFile, SignatureAttrInfo signatureAttrInfo)
    {
        signatureAttrInfo.referencedClassFiles =
            findReferencedClasses(classFile.getCpString(signatureAttrInfo.u2signatureIndex));
    }


    public void visitRuntimeVisibleAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleAnnotationsAttrInfo runtimeVisibleAnnotationsAttrInfo)
    {
        // Initialize the annotations.
        runtimeVisibleAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeInvisibleAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleAnnotationsAttrInfo runtimeInvisibleAnnotationsAttrInfo)
    {
        // Initialize the annotations.
        runtimeInvisibleAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeVisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleParameterAnnotationsAttrInfo runtimeVisibleParameterAnnotationsAttrInfo)
    {
        // Initialize the annotations.
        runtimeVisibleParameterAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitRuntimeInvisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleParameterAnnotationsAttrInfo runtimeInvisibleParameterAnnotationsAttrInfo)
    {
        // Initialize the annotations.
        runtimeInvisibleParameterAnnotationsAttrInfo.annotationsAccept(classFile, this);
    }


    public void visitAnnotationDefaultAttrInfo(ClassFile classFile, AnnotationDefaultAttrInfo annotationDefaultAttrInfo)
    {
        // Initialize the annotation.
        annotationDefaultAttrInfo.defaultValueAccept(classFile, this);
    }


    // Implementations for LocalVariableInfoVisitor.

    public void visitLocalVariableInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableInfo localVariableInfo)
    {
        localVariableInfo.referencedClassFile =
            findReferencedClass(classFile.getCpString(localVariableInfo.u2descriptorIndex));
    }


    // Implementations for LocalVariableTypeInfoVisitor.

    public void visitLocalVariableTypeInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeInfo localVariableTypeInfo)
    {
        localVariableTypeInfo.referencedClassFiles =
            findReferencedClasses(classFile.getCpString(localVariableTypeInfo.u2signatureIndex));
    }


    // Implementations for AnnotationVisitor.

    public void visitAnnotation(ClassFile classFile, Annotation annotation)
    {
        annotation.referencedClassFiles =
            findReferencedClasses(classFile.getCpString(annotation.u2typeIndex));

        // Initialize the element values.
        annotation.elementValuesAccept(classFile, this);
    }


    // Implementations for ElementValueVisitor.

    public void visitConstantElementValue(ClassFile classFile, Annotation annotation, ConstantElementValue constantElementValue)
    {
        initializeElementValue(classFile, annotation, constantElementValue);
    }


    public void visitEnumConstantElementValue(ClassFile classFile, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        initializeElementValue(classFile, annotation, enumConstantElementValue);

        enumConstantElementValue.referencedClassFiles =
            findReferencedClasses(classFile.getCpString(enumConstantElementValue.u2typeNameIndex));
    }


    public void visitClassElementValue(ClassFile classFile, Annotation annotation, ClassElementValue classElementValue)
    {
        initializeElementValue(classFile, annotation, classElementValue);

        classElementValue.referencedClassFiles =
            findReferencedClasses(classFile.getCpString(classElementValue.u2classInfoIndex));
    }


    public void visitAnnotationElementValue(ClassFile classFile, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        initializeElementValue(classFile, annotation, annotationElementValue);

        // Initialize the annotation.
        annotationElementValue.annotationAccept(classFile, this);
    }


    public void visitArrayElementValue(ClassFile classFile, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        initializeElementValue(classFile, annotation, arrayElementValue);

        // Initialize the element values.
        arrayElementValue.elementValuesAccept(classFile, annotation, this);
    }


    /**
     * Initializes the referenced method of an element value, if any.
     */
    private void initializeElementValue(ClassFile classFile, Annotation annotation, ElementValue elementValue)
    {
        // See if we have a referenced class file.
        if (annotation                      != null &&
            annotation.referencedClassFiles != null &&
            elementValue.u2elementName      != 0)
        {
            // See if we can find the method in the referenced class
            // (ignoring the descriptor).
            String name = classFile.getCpString(elementValue.u2elementName);

            ClassFile referencedClassFile = annotation.referencedClassFiles[0];
            elementValue.referencedClassFile  = referencedClassFile;
            elementValue.referencedMethodInfo = referencedClassFile.findMethod(name, null);
        }
    }


    // Small utility methods.

    /**
     * Returns the single class file referenced by the given descriptor, or
     * <code>null</code> if there isn't any useful reference.
     */
    private ClassFile findReferencedClass(String descriptor)
    {
        DescriptorClassEnumeration enumeration =
            new DescriptorClassEnumeration(descriptor);

        if (enumeration.hasMoreClassNames())
        {
            return findClass(enumeration.nextClassName());
        }

        return null;
    }


    /**
     * Returns an array of class files referenced by the given descriptor, or
     * <code>null</code> if there aren't any useful references.
     */
    private ClassFile[] findReferencedClasses(String descriptor)
    {
        DescriptorClassEnumeration enumeration =
            new DescriptorClassEnumeration(descriptor);

        int classCount = enumeration.classCount();
        if (classCount > 0)
        {
            ClassFile[] referencedClassFiles = new ClassFile[classCount];

            boolean foundReferencedClassFiles = false;

            for (int index = 0; index < classCount; index++)
            {
                String name = enumeration.nextClassName();

                ClassFile referencedClassFile = findClass(name);

                if (referencedClassFile != null)
                {
                    referencedClassFiles[index] = referencedClassFile;
                    foundReferencedClassFiles = true;
                }
            }

            if (foundReferencedClassFiles)
            {
                return referencedClassFiles;
            }
        }

        return null;
    }


    /**
     * Returns the class with the given name, either for the program class pool
     * or from the library class pool, or <code>null</code> if it can't be found.
     */
    private ClassFile findClass(String name)
    {
        // First look for the class in the program class pool.
        ClassFile classFile = programClassPool.getClass(name);

        // Otherwise look for the class in the library class pool.
        if (classFile == null)
        {
            classFile = libraryClassPool.getClass(name);
        }

        return classFile;
    }
}
