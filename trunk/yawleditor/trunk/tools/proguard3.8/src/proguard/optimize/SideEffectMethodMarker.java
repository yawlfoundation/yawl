/* $Id: SideEffectMethodMarker.java,v 1.6.2.2 2007/01/18 21:31:53 eric Exp $
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
package proguard.optimize;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.instruction.*;
import proguard.classfile.visitor.*;

/**
 * This ClassPoolVisitor marks all methods that have side effects.
 *
 * @see WriteOnlyFieldMarker
 * @see NoSideEffectMethodMarker
 * @author Eric Lafortune
 */
public class SideEffectMethodMarker
  implements ClassPoolVisitor,
             ClassFileVisitor,
             MemberInfoVisitor,
             AttrInfoVisitor
{
    // A reusable object for checking whether instructions have side effects.
    private SideEffectInstructionChecker sideEffectInstructionChecker = new SideEffectInstructionChecker(false);

    // Parameters and values for visitor methods.
    private int     newSideEffectCount;
    private boolean hasSideEffects;


    // Implementations for ClassPoolVisitor.

    public void visitClassPool(ClassPool classPool)
    {
        // Go over all class files and their methods, marking if they have side
        // effects, until no new cases can be found.
        do
        {
            newSideEffectCount = 0;

            // Go over all class files and their methods once.
            classPool.classFilesAccept(this);
        }
        while (newSideEffectCount > 0);
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Go over all methods.
        programClassFile.methodsAccept(this);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile) {}


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo) {}


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        if (!hasSideEffects(programMethodInfo) &&
            !NoSideEffectMethodMarker.hasNoSideEffects(programMethodInfo))
        {
            // Set the return value, in case the method doesn't have a code
            // attribute (a native method or an abstract method).
            hasSideEffects = (programMethodInfo.getAccessFlags() & ClassConstants.INTERNAL_ACC_NATIVE) != 0;

            programMethodInfo.attributesAccept(programClassFile, this);

            // Mark the method depending on the return value.
            if (hasSideEffects)
            {
                markSideEffects(programMethodInfo);

                newSideEffectCount++;
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
    public void visitRuntimeVisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleParameterAnnotationsAttrInfo runtimeVisibleParameterAnnotationsAttrInfo) {}
    public void visitRuntimeInvisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleParameterAnnotationsAttrInfo runtimeInvisibleParameterAnnotationsAttrInfo) {}
    public void visitAnnotationDefaultAttrInfo(ClassFile classFile, AnnotationDefaultAttrInfo annotationDefaultAttrInfo) {}


    public void visitCodeAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo)
    {
        byte[] code   = codeAttrInfo.code;
        int    length = codeAttrInfo.u4codeLength;

        // Go over all instructions.
        int offset = 0;
        do
        {
            // Get the current instruction.
            Instruction instruction = InstructionFactory.create(code, offset);

            // Check if it is causing any side effects.
            hasSideEffects = sideEffectInstructionChecker.hasSideEffects(classFile, methodInfo, codeAttrInfo, offset, instruction);

            // Go to the next instruction.
            offset += instruction.length(offset);
        }
        while (offset < length && !hasSideEffects);
    }


    // Small utility methods.

    public static void markSideEffects(MethodInfo methodInfo)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(methodInfo);
        if (info != null)
        {
            info.setSideEffects();
        }
    }


    public static boolean hasSideEffects(MethodInfo methodInfo)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(methodInfo);
        return info == null ||
               info.hasSideEffects();
    }
}
