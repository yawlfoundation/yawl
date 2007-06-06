/* $Id: MemberReferenceFixer.java,v 1.4.2.4 2007/01/18 21:31:51 eric Exp $
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
import proguard.classfile.util.ClassUtil;
import proguard.classfile.visitor.*;

/**
 * This ClassFileVisitor fixes constant pool field and method references to
 * fields and methods whose names or descriptors have changed.
 *
 * @author Eric Lafortune
 */
public class MemberReferenceFixer
implements   ClassFileVisitor,
             CpInfoVisitor,
             MemberInfoVisitor,
             AttrInfoVisitor,
             AnnotationVisitor,
             ElementValueVisitor
{
    private static final boolean DEBUG = false;


    private ConstantPoolEditor constantPoolEditor = new ConstantPoolEditor();
    private StackSizeUpdater   stackSizeUpdater;

    // Parameter for the visitor methods.
    private int cpIndex;

    // Return values for the visitor methods.
    private boolean isInterfaceMethod;
    private boolean stackSizesMayHaveChanged;


    /**
     * Creates a new MemberReferenceFixer.
     * @param codeLength an estimate of the maximum length of all the code that
     *                   will be edited.
     */
    public MemberReferenceFixer(int codeLength)
    {
        stackSizeUpdater = new StackSizeUpdater(codeLength);
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        stackSizesMayHaveChanged = false;

        // Fix the constant pool entries.
        for (int index = 1; index < programClassFile.u2constantPoolCount; index++)
        {
            CpInfo cpInfo = programClassFile.constantPool[index];
            if (cpInfo != null)
            {
                // Fix the entry, replacing it entirely if needed.
                this.cpIndex = index;

                cpInfo.accept(programClassFile, this);
            }
        }

        // Fix class members.
        programClassFile.fieldsAccept(this);
        programClassFile.methodsAccept(this);

        // Fix the attributes.
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
    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}


    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo)
    {
        // Do we know the referenced field?
        MemberInfo referencedMemberInfo = fieldrefCpInfo.referencedMemberInfo;
        if (referencedMemberInfo != null)
        {
            ClassFile referencedClassFile = fieldrefCpInfo.referencedClassFile;

            // Does it have a new name or type?
            String newName = referencedMemberInfo.getName(referencedClassFile);
            String newType = referencedMemberInfo.getDescriptor(referencedClassFile);

            if (!fieldrefCpInfo.getName(classFile).equals(newName) ||
                !fieldrefCpInfo.getType(classFile).equals(newType))
            {
                if (DEBUG)
                {
                    debug(classFile, fieldrefCpInfo, referencedClassFile, referencedMemberInfo);
                }

                // Update the name and type index.
                fieldrefCpInfo.u2nameAndTypeIndex =
                    constantPoolEditor.addNameAndTypeCpInfo((ProgramClassFile)classFile,
                                                            newName,
                                                            newType);
            }
        }
    }


    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo)
    {
        // Do we know the referenced interface method?
        MemberInfo referencedMemberInfo = interfaceMethodrefCpInfo.referencedMemberInfo;
        if (referencedMemberInfo != null)
        {
            ClassFile referencedClassFile = interfaceMethodrefCpInfo.referencedClassFile;

            // Does it have a new name or type?
            String newName = referencedMemberInfo.getName(referencedClassFile);
            String newType = referencedMemberInfo.getDescriptor(referencedClassFile);

            if (!interfaceMethodrefCpInfo.getName(classFile).equals(newName) ||
                !interfaceMethodrefCpInfo.getType(classFile).equals(newType))
            {
                if (DEBUG)
                {
                    debug(classFile, interfaceMethodrefCpInfo, referencedClassFile, referencedMemberInfo);
                }

                // Update the name and type index.
                interfaceMethodrefCpInfo.u2nameAndTypeIndex =
                    constantPoolEditor.addNameAndTypeCpInfo((ProgramClassFile)classFile,
                                                            newName,
                                                            newType);

                // Remember that the stack sizes of the methods in this class
                // may have changed.
                stackSizesMayHaveChanged = true;
            }

            // Check if this is an interface method.
            isInterfaceMethod = true;
            classFile.constantPoolEntryAccept(interfaceMethodrefCpInfo.u2classIndex, this);

            // Has the method become a non-interface method?
            if (!isInterfaceMethod)
            {
                if (DEBUG)
                {
                    System.out.println("MemberReferenceFixer:");
                    System.out.println("  Class file     = "+classFile.getName());
                    System.out.println("  Ref class file = "+referencedClassFile.getName());
                    System.out.println("  Ref method     = "+interfaceMethodrefCpInfo.getName(classFile)+interfaceMethodrefCpInfo.getType(classFile));
                    System.out.println("    -> ordinary method");
                }

                // Replace the interface method reference by a method reference.
                ((ProgramClassFile)classFile).constantPool[this.cpIndex] =
                    new MethodrefCpInfo(interfaceMethodrefCpInfo.u2classIndex,
                                        interfaceMethodrefCpInfo.u2nameAndTypeIndex,
                                        referencedClassFile,
                                        referencedMemberInfo);
            }
        }
    }


    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo)
    {
        // Do we know the referenced method?
        MemberInfo referencedMemberInfo = methodrefCpInfo.referencedMemberInfo;
        if (referencedMemberInfo != null)
        {
            ClassFile referencedClassFile = methodrefCpInfo.referencedClassFile;

            // Does it have a new name or type?
            String newName = referencedMemberInfo.getName(referencedClassFile);
            String newType = referencedMemberInfo.getDescriptor(referencedClassFile);

            if (!methodrefCpInfo.getName(classFile).equals(newName) ||
                !methodrefCpInfo.getType(classFile).equals(newType))
            {
                if (DEBUG)
                {
                    debug(classFile, methodrefCpInfo, referencedClassFile, referencedMemberInfo);
                }

                // Update the name and type index.
                methodrefCpInfo.u2nameAndTypeIndex =
                    constantPoolEditor.addNameAndTypeCpInfo((ProgramClassFile)classFile,
                                                            newName,
                                                            newType);

                // Remember that the stack sizes of the methods in this class
                // may have changed.
                stackSizesMayHaveChanged = true;
            }

            // Check if this is an interface method.
            isInterfaceMethod = false;
            classFile.constantPoolEntryAccept(methodrefCpInfo.u2classIndex, this);

            // Has the method become an interface method?
            if (isInterfaceMethod)
            {
                if (DEBUG)
                {
                    System.out.println("MemberReferenceFixer:");
                    System.out.println("  Class file     = "+classFile.getName());
                    System.out.println("  Ref class file = "+referencedClassFile.getName());
                    System.out.println("  Ref method     = "+methodrefCpInfo.getName(classFile)+methodrefCpInfo.getType(classFile));
                    System.out.println("    -> interface method");
                }

                // Replace the method reference by an interface method reference.
                ((ProgramClassFile)classFile).constantPool[this.cpIndex] =
                    new InterfaceMethodrefCpInfo(methodrefCpInfo.u2classIndex,
                                                 methodrefCpInfo.u2nameAndTypeIndex,
                                                 referencedClassFile,
                                                 referencedMemberInfo);
            }
        }
    }


    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        // Check if this class entry is an array type.
        if (ClassUtil.isInternalArrayType(classCpInfo.getName(classFile)))
        {
            isInterfaceMethod = false;
        }
        else
        {
            // Check if this class entry refers to an interface class.
            ClassFile referencedClassFile = classCpInfo.referencedClassFile;
            if (referencedClassFile != null)
            {
                isInterfaceMethod = (referencedClassFile.getAccessFlags() & ClassConstants.INTERNAL_ACC_INTERFACE) != 0;
            }
        }
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        // Fix the attributes.
        programFieldInfo.attributesAccept(programClassFile, this);
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        // Fix the attributes.
        programMethodInfo.attributesAccept(programClassFile, this);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}
    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo) {}


    // Implementations for AttrInfoVisitor.

    public void visitUnknownAttrInfo(ClassFile classFile, UnknownAttrInfo unknownAttrInfo) {}
    public void visitInnerClassesAttrInfo(ClassFile classFile, InnerClassesAttrInfo innerClassesAttrInfo) {}
    public void visitConstantValueAttrInfo(ClassFile classFile, FieldInfo fieldInfo, ConstantValueAttrInfo constantValueAttrInfo) {}
    public void visitExceptionsAttrInfo(ClassFile classFile, MethodInfo methodInfo, ExceptionsAttrInfo exceptionsAttrInfo) {}
    public void visitLineNumberTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LineNumberTableAttrInfo lineNumberTableAttrInfo) {}
    public void visitLocalVariableTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTableAttrInfo localVariableTableAttrInfo) {}
    public void visitLocalVariableTypeTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeTableAttrInfo localVariableTypeTableAttrInfo) {}
    public void visitSourceFileAttrInfo(ClassFile classFile, SourceFileAttrInfo sourceFileAttrInfo) {}
    public void visitSourceDirAttrInfo(ClassFile classFile, SourceDirAttrInfo sourceDirAttrInfo) {}
    public void visitDeprecatedAttrInfo(ClassFile classFile, DeprecatedAttrInfo deprecatedAttrInfo) {}
    public void visitSyntheticAttrInfo(ClassFile classFile, SyntheticAttrInfo syntheticAttrInfo) {}
    public void visitSignatureAttrInfo(ClassFile classFile, SignatureAttrInfo signatureAttrInfo) {}


    public void visitEnclosingMethodAttrInfo(ClassFile classFile, EnclosingMethodAttrInfo enclosingMethodAttrInfo)
    {
        MemberInfo referencedMemberInfo = enclosingMethodAttrInfo.referencedMethodInfo;
        if (referencedMemberInfo != null)
        {
            ClassFile referencedClassFile = enclosingMethodAttrInfo.referencedClassFile;

            // Does it have a new class?
            if (!enclosingMethodAttrInfo.getClassName(classFile).equals(referencedClassFile.getName()))
            {
                // Update the class index.
                enclosingMethodAttrInfo.u2classIndex =
                    constantPoolEditor.addClassCpInfo((ProgramClassFile)classFile,
                                                      referencedClassFile);
            }

            // Does it have a new name or type?
            if (!enclosingMethodAttrInfo.getName(classFile).equals(referencedMemberInfo.getName(referencedClassFile)) ||
                !enclosingMethodAttrInfo.getType(classFile).equals(referencedMemberInfo.getDescriptor(referencedClassFile)))
            {
                // Update the name and type index.
                enclosingMethodAttrInfo.u2nameAndTypeIndex =
                    constantPoolEditor.addNameAndTypeCpInfo((ProgramClassFile)classFile,
                                                            referencedMemberInfo.getName(referencedClassFile),
                                                            referencedMemberInfo.getDescriptor(referencedClassFile));
            }
        }
    }


    public void visitCodeAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo)
    {
        // Recompute the maximum stack size if necessary.
        if (stackSizesMayHaveChanged)
        {
            stackSizeUpdater.visitCodeAttrInfo(classFile, methodInfo, codeAttrInfo);
        }

        // Fix the nested attributes.
        codeAttrInfo.attributesAccept(classFile, methodInfo, this);
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


    // Implementations for AnnotationVisitor.

    public void visitAnnotation(ClassFile classFile, Annotation annotation)
    {
        // Fix the element values.
        annotation.elementValuesAccept(classFile, this);
    }


    // Implementations for ElementValueVisitor.

    public void visitConstantElementValue(ClassFile classFile, Annotation annotation, ConstantElementValue constantElementValue)
    {
        fixElementValue(classFile, annotation, constantElementValue);
    }


    public void visitEnumConstantElementValue(ClassFile classFile, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        fixElementValue(classFile, annotation, enumConstantElementValue);
    }


    public void visitClassElementValue(ClassFile classFile, Annotation annotation, ClassElementValue classElementValue)
    {
        fixElementValue(classFile, annotation, classElementValue);
    }


    public void visitAnnotationElementValue(ClassFile classFile, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        fixElementValue(classFile, annotation, annotationElementValue);

        // Fix the annotation.
        annotationElementValue.annotationAccept(classFile, this);
    }


    public void visitArrayElementValue(ClassFile classFile, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        fixElementValue(classFile, annotation, arrayElementValue);

        // Fix the element values.
        arrayElementValue.elementValuesAccept(classFile, annotation, this);
    }


    // Small utility methods.

    /**
     * Fixs the method reference of the element value, if any.
     */
    private void fixElementValue(ClassFile    classFile,
                                 Annotation   annotation,
                                 ElementValue elementValue)
    {
        // Do we know the referenced method?
        MemberInfo referencedMemberInfo = elementValue.referencedMethodInfo;
        if (referencedMemberInfo != null)
        {
            // Does it have a new name or type?
            String methodName    = elementValue.getMethodName(classFile);
            String newMethodName = referencedMemberInfo.getName(elementValue.referencedClassFile);
            if (!methodName.equals(newMethodName))
            {
                // Update the element name index.
                elementValue.u2elementName =
                    constantPoolEditor.addUtf8CpInfo((ProgramClassFile)classFile,
                                                     newMethodName);
            }
        }
    }


    private void debug(ClassFile  classFile,
                       RefCpInfo  refCpInfo,
                       ClassFile  referencedClassFile,
                       MemberInfo referencedMemberInfo)
    {
        System.out.println("MemberReferenceFixer:");
        System.out.println("  Class file      = "+classFile.getName());
        System.out.println("  Ref class file  = "+referencedClassFile.getName());
        System.out.println("  Ref member name = "+refCpInfo.getName(classFile));
        System.out.println("                 -> "+referencedMemberInfo.getName(referencedClassFile));
        System.out.println("  Ref descriptor  = "+refCpInfo.getType(classFile));
        System.out.println("                 -> "+referencedMemberInfo.getDescriptor(referencedClassFile));
    }
}
