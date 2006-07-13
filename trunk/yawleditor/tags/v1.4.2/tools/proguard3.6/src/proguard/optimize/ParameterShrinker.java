/* $Id: ParameterShrinker.java,v 1.6.2.1 2006/01/16 22:57:56 eric Exp $
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
package proguard.optimize;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.editor.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.MemberInfoVisitor;

/**
 * This MemberInfoVisitor removes unused parameters from the descriptors and
 * the code of the methods that it visits. It also makes methods static if the
 * 'this' parameter is unused.
 *
 * @see ParameterUsageMarker
 * @author Eric Lafortune
 */
public class ParameterShrinker
  implements MemberInfoVisitor,
             AttrInfoVisitor
{
    private static final boolean DEBUG = false;


    private MemberInfoVisitor extraParameterMemberInfoVisitor;
    private MemberInfoVisitor extraStaticMemberInfoVisitor;

    private VariableEditor     variableEditor;
    private ConstantPoolEditor constantPoolEditor = new ConstantPoolEditor();

    // A parameter for the parameter annotation visiting methods.
    private MethodInfo methodInfo;


    /**
     * Creates a new ParameterShrinker.
     * @param codeLength an estimate of the maximum length of all the code
     *                   that will be edited.
     * @param maxLocals  an estimate of the maximum length of all the local
     *                   variable frames that will be edited.
     */
    public ParameterShrinker(int codeLength, int maxLocals)
    {
        this(codeLength, maxLocals, null, null);
    }


    /**
     * Creates a new ParameterShrinker.
     * @param codeLength an estimate of the maximum length of all the code
     *                   that will be edited.
     * @param maxLocals  an estimate of the maximum length of all the local
     *                   variable frames that will be edited.
     * @param extraParameterMemberInfoVisitor an optional extra visitor for all
     *                                        methods whose parameters have been
     *                                        simplified.
     * @param extraStaticMemberInfoVisitor    an optional extra visitor for all
     *                                        methods that have been made static.
     */
    public ParameterShrinker(int codeLength, int maxLocals,
                             MemberInfoVisitor extraParameterMemberInfoVisitor,
                             MemberInfoVisitor extraStaticMemberInfoVisitor)
    {
        this.variableEditor = new VariableEditor(codeLength, maxLocals);

        this.extraParameterMemberInfoVisitor = extraParameterMemberInfoVisitor;
        this.extraStaticMemberInfoVisitor    = extraStaticMemberInfoVisitor;
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo) {}


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        // Update the descriptor if it has any unused parameters.
        String descriptor    = programMethodInfo.getDescriptor(programClassFile);
        String newDescriptor = shrinkDescriptor(programClassFile, programMethodInfo);
        if (!descriptor.equals(newDescriptor))
        {
            // Update the parameter annotations.
            this.methodInfo = programMethodInfo;
            programMethodInfo.attributesAccept(programClassFile, this);

            // TODO: Avoid duplicate constructors.
            String name    = programMethodInfo.getName(programClassFile);
            String newName = name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT) ?
                                 ClassConstants.INTERNAL_METHOD_NAME_INIT :
                                 name + '$' + Long.toHexString(Math.abs((descriptor).hashCode()));

            if (DEBUG)
            {
                System.out.println("ParameterShrinker:");
                System.out.println("  Class file        = "+programClassFile.getName());
                System.out.println("  Method name       = "+name);
                System.out.println("                   -> "+newName);
                System.out.println("  Method descriptor = "+descriptor);
                System.out.println("                   -> "+newDescriptor);
            }

            // Update the name, if necessary.
            if (!newName.equals(name))
            {
                programMethodInfo.u2nameIndex =
                constantPoolEditor.addUtf8CpInfo(programClassFile, newName);
            }

            // Clear the unused referenced class files.
            shrinkReferencedClassFiles(programClassFile, programMethodInfo);

            // Update the descriptor.
            programMethodInfo.u2descriptorIndex =
                constantPoolEditor.addUtf8CpInfo(programClassFile, newDescriptor);

            // Visit the method, if required.
            if (extraParameterMemberInfoVisitor != null)
            {
                extraParameterMemberInfoVisitor.visitProgramMethodInfo(programClassFile, programMethodInfo);
            }
        }

        // Delete unused variables from the local variable frame.
        variableEditor.reset(64);

        for (int variableIndex = 0; variableIndex < 64; variableIndex++)
        {
            if (!VariableUsageMarker.isVariableUsed(programMethodInfo, variableIndex))
            {
                // Delete the unused variable.
                variableEditor.deleteVariable(variableIndex);
            }
        }

        // Shift all remaining variables in the byte code.
        programMethodInfo.attributesAccept(programClassFile, variableEditor);

        // Is the method not static, and can it be made static?
        int accessFlags = programMethodInfo.getAccessFlags();
        if ((accessFlags & ClassConstants.INTERNAL_ACC_STATIC) == 0 &&
            !VariableUsageMarker.isVariableUsed(programMethodInfo, 0))
        {
            if (DEBUG)
            {
                System.out.println("ParameterShrinker:");
                System.out.println("  Class file = "+programClassFile.getName());
                System.out.println("  Method     = "+programMethodInfo.getName(programClassFile)+programMethodInfo.getDescriptor(programClassFile));
                System.out.println("    -> static");
            }

            // Make the method static.
            programMethodInfo.u2accessFlags =
                (accessFlags & ~ClassConstants.INTERNAL_ACC_FINAL) |
                ClassConstants.INTERNAL_ACC_STATIC;

            // Visit the method, if required.
            if (extraStaticMemberInfoVisitor != null)
            {
                extraStaticMemberInfoVisitor.visitProgramMethodInfo(programClassFile, programMethodInfo);
            }
        }
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}
    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo) {}


    // Implementations for AttrInfoVisitor.

    public void visitUnknownAttrInfo(ClassFile classFile, UnknownAttrInfo unknownAttrInfo) {}
    public void visitInnerClassesAttrInfo(ClassFile classFile, InnerClassesAttrInfo innerClassesAttrInfo) {}
    public void visitEnclosingMethodAttrInfo(ClassFile classFile, EnclosingMethodAttrInfo enclosingMethodAttrInfo) {}
    public void visitConstantValueAttrInfo(ClassFile classFile, FieldInfo fieldInfo, ConstantValueAttrInfo constantValueAttrInfo) {}
    public void visitExceptionsAttrInfo(ClassFile classFile, MethodInfo methodInfo, ExceptionsAttrInfo exceptionsAttrInfo) {}
    public void visitCodeAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo) {}
    public void visitLineNumberTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LineNumberTableAttrInfo lineNumberTableAttrInfo) {}
    public void visitLocalVariableTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTableAttrInfo localVariableTableAttrInfo) {}
    public void visitLocalVariableTypeTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeTableAttrInfo localVariableTypeTableAttrInfo) {}
    public void visitSourceFileAttrInfo(ClassFile classFile, SourceFileAttrInfo sourceFileAttrInfo) {}
    public void visitSourceDirAttrInfo(ClassFile classFile, SourceDirAttrInfo sourceDirAttrInfo) {}
    public void visitDeprecatedAttrInfo(ClassFile classFile, DeprecatedAttrInfo deprecatedAttrInfo) {}
    public void visitSyntheticAttrInfo(ClassFile classFile, SyntheticAttrInfo syntheticAttrInfo) {}
    public void visitSignatureAttrInfo(ClassFile classFile, SignatureAttrInfo signatureAttrInfo) {}
    public void visitRuntimeVisibleAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleAnnotationsAttrInfo runtimeVisibleAnnotationsAttrInfo) {}
    public void visitRuntimeInvisibleAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleAnnotationsAttrInfo runtimeInvisibleAnnotationsAttrInfo) {}
    public void visitAnnotationDefaultAttrInfo(ClassFile classFile, AnnotationDefaultAttrInfo annotationDefaultAttrInfo) {}


    public void visitRuntimeVisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleParameterAnnotationsAttrInfo runtimeVisibleParameterAnnotationsAttrInfo)
    {
        // Update the parameter annotations.
        shrinkParameterAnnotations(classFile, runtimeVisibleParameterAnnotationsAttrInfo);
    }


    public void visitRuntimeInvisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleParameterAnnotationsAttrInfo runtimeInvisibleParameterAnnotationsAttrInfo)
    {
        // Update the parameter annotations.
        shrinkParameterAnnotations(classFile, runtimeInvisibleParameterAnnotationsAttrInfo);
    }


    // Small utility methods.

    /**
     * Shrinks the given parameter annotations.
     */
    private void shrinkParameterAnnotations(ClassFile                           classFile,
                                            RuntimeParameterAnnotationsAttrInfo runtimeParameterAnnotationsAttrInfo)
    {
        Annotation[][] annotations = runtimeParameterAnnotationsAttrInfo.parameterAnnotations;

        // All parameters of non-static methods are shifted by one in the local
        // variable frame.
        int parameterIndex =
            (methodInfo.getAccessFlags() & ClassConstants.INTERNAL_ACC_STATIC) != 0 ?
                0 : 1;

        int annotationIndex    = 0;
        int newAnnotationIndex = 0;

        // Go over the parameters.
        String descriptor = methodInfo.getDescriptor(classFile);
        InternalTypeEnumeration internalTypeEnumeration =
            new InternalTypeEnumeration(descriptor);

        while (internalTypeEnumeration.hasMoreTypes())
        {
            String type = internalTypeEnumeration.nextType();
            if (VariableUsageMarker.isVariableUsed(methodInfo, parameterIndex))
            {
                annotations[newAnnotationIndex++] = annotations[annotationIndex];
            }

            annotationIndex++;

            parameterIndex += ClassUtil.isInternalCategory2Type(type) ? 2 : 1;
        }

        runtimeParameterAnnotationsAttrInfo.u2numberOfParameters = newAnnotationIndex;

        // Clear the unused entries.
        while (newAnnotationIndex < annotationIndex)
        {
            annotations[newAnnotationIndex++] = null;
        }
    }


    /**
     * Returns a shrunk descriptor of the given method.
     */
    public static String shrinkDescriptor(ProgramClassFile  classFile,
                                          ProgramMethodInfo methodInfo)
    {
        // All parameters of non-static methods are shifted by one in the local
        // variable frame.
        int parameterIndex =
            (methodInfo.getAccessFlags() & ClassConstants.INTERNAL_ACC_STATIC) != 0 ?
                0 : 1;

        // Go over the parameters.
        InternalTypeEnumeration internalTypeEnumeration =
            new InternalTypeEnumeration(methodInfo.getDescriptor(classFile));

        StringBuffer newDescriptorBuffer = new StringBuffer();
        newDescriptorBuffer.append(ClassConstants.INTERNAL_METHOD_ARGUMENTS_OPEN);

        while (internalTypeEnumeration.hasMoreTypes())
        {
            String type = internalTypeEnumeration.nextType();
            if (VariableUsageMarker.isVariableUsed(methodInfo, parameterIndex))
            {
                newDescriptorBuffer.append(type);
            }

            parameterIndex += ClassUtil.isInternalCategory2Type(type) ? 2 : 1;
        }

        newDescriptorBuffer.append(ClassConstants.INTERNAL_METHOD_ARGUMENTS_CLOSE);
        newDescriptorBuffer.append(internalTypeEnumeration.returnType());

        return newDescriptorBuffer.toString();
    }


    /**
     * Shrinks the array of referenced class files of the given method.
     */
    private static void shrinkReferencedClassFiles(ProgramClassFile  classFile,
                                                   ProgramMethodInfo methodInfo)
    {
        ClassFile[] referencedClassFiles = methodInfo.referencedClassFiles;

        if (referencedClassFiles != null)
        {
            // All parameters of non-static methods are shifted by one in the local
            // variable frame.
            int parameterIndex =
                (methodInfo.getAccessFlags() & ClassConstants.INTERNAL_ACC_STATIC) != 0 ?
                    0 : 1;

            int referencedClassFileIndex    = 0;
            int newReferencedClassFileIndex = 0;

            // Go over the parameters.
            String descriptor = methodInfo.getDescriptor(classFile);
            InternalTypeEnumeration internalTypeEnumeration =
                new InternalTypeEnumeration(descriptor);

            while (internalTypeEnumeration.hasMoreTypes())
            {
                String type = internalTypeEnumeration.nextType();
                if (ClassUtil.isInternalArrayType(type))
                {
                    type = ClassUtil.internalTypeFromArrayType(type);
                }

                if (ClassUtil.isInternalClassType(type))
                {
                    if (VariableUsageMarker.isVariableUsed(methodInfo, parameterIndex))
                    {
                        referencedClassFiles[newReferencedClassFileIndex++] =
                            referencedClassFiles[referencedClassFileIndex];
                    }

                    referencedClassFileIndex++;
                }

                parameterIndex += ClassUtil.isInternalCategory2Type(type) ? 2 : 1;
            }

            // Also look at the return value.
            String type = internalTypeEnumeration.returnType();
            if (ClassUtil.isInternalArrayType(type))
            {
                type = ClassUtil.internalTypeFromArrayType(type);
            }

            if (ClassUtil.isInternalClassType(type))
            {
                referencedClassFiles[newReferencedClassFileIndex++] =
                    referencedClassFiles[referencedClassFileIndex];

                referencedClassFileIndex++;
            }

            // Clear the unused entries.
            while (newReferencedClassFileIndex < referencedClassFileIndex)
            {
                referencedClassFiles[newReferencedClassFileIndex++] = null;
            }
        }
    }
}
