/* $Id: BranchTargetFinder.java,v 1.5 2004/10/10 20:56:58 eric Exp $
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
package proguard.optimize.peephole;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.instruction.*;
import proguard.classfile.visitor.*;

/**
 * This AttrInfoVisitor finds all branch targets in the CodeAttrInfo objects
 * that it visits.
 *
 * @author Eric Lafortune
 */
public class BranchTargetFinder
implements   AttrInfoVisitor,
             InstructionVisitor,
             ExceptionInfoVisitor
{
    private boolean[] isBranchTarget;


    /**
     * Creates a new BranchTargetFinder.
     * @param codeLength an estimate of the maximum length of all the code that
     *                   will be edited.
     */
    public BranchTargetFinder(int codeLength)
    {
        isBranchTarget = new boolean[codeLength + 1];
    }


    /**
     * Returns whether the instruction at the given offset is the target of a
     * branch instruction in the CodeAttrInfo that was visited most recently.
     */
    public boolean isBranchTarget(int offset)
    {
        return isBranchTarget[offset];
    }


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
        // Make sure there is a sufficiently large boolean array.
        int length = codeAttrInfo.u4codeLength + 1;
        if (isBranchTarget.length < length)
        {
            // Create a new boolean array.
            isBranchTarget = new boolean[length];
        }
        else
        {
            // Reset the boolean array.
            for (int index = 0; index < length; index++)
            {
                isBranchTarget[index] = false;
            }
        }

        // The first instruction and the end of the code are always branch targets.
        isBranchTarget[0]                         = true;
        isBranchTarget[codeAttrInfo.u4codeLength] = true;

        // Mark branch targets by going over all instructions.
        codeAttrInfo.instructionsAccept(classFile, methodInfo, this);

        // Mark branch targets in the exception table.
        codeAttrInfo.exceptionsAccept(classFile, methodInfo, this);
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction) {}
    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction) {}
    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction) {}


    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction)
    {
        // Mark the branch target.
        isBranchTarget[offset + branchInstruction.branchOffset] = true;
    }

    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction)
    {
        // Mark the branch targets of the default jump offset.
        isBranchTarget[offset + tableSwitchInstruction.defaultOffset] = true;

        // Mark the branch targets of the jump offsets.
        markBranchTargets(offset,
                          tableSwitchInstruction.jumpOffsets,
                          tableSwitchInstruction.highCase -
                          tableSwitchInstruction.lowCase + 1);
    }

    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        // Mark the branch targets of the default jump offset.
        isBranchTarget[offset + lookUpSwitchInstruction.defaultOffset] = true;

        // Mark the branch targets of the jump offsets.
        markBranchTargets(offset,
                          lookUpSwitchInstruction.jumpOffsets,
                          lookUpSwitchInstruction.jumpOffsetCount);
    }


    // Implementations for ExceptionInfoVisitor.

    public void visitExceptionInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, ExceptionInfo exceptionInfo)
    {
        // Remap the code offsets. Note that the branch target array also has
        // an entry for the first offset after the code, for u2endpc.
        isBranchTarget[exceptionInfo.u2startpc]   = true;
        isBranchTarget[exceptionInfo.u2endpc]     = true;
        isBranchTarget[exceptionInfo.u2handlerpc] = true;
    }


    // Small utility methods.

    /**
     * Marks the branch targets of the given jump offsets for the instruction
     * at the given offset.
     */
    private void markBranchTargets(int offset, int[] jumpOffsets, int length)
    {
        for (int index = 0; index < length; index++)
        {
            isBranchTarget[offset + jumpOffsets[index]] = true;
        }
    }
}
