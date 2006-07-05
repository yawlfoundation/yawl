/* $Id: BranchTargetFinder.java,v 1.8.2.1 2006/01/16 22:57:56 eric Exp $
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
package proguard.optimize.peephole;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.instruction.*;
import proguard.classfile.visitor.*;

/**
 * This AttrInfoVisitor finds all instruction offsets, branch targets, and
 * exception targets in the CodeAttrInfo objects that it visits.
 *
 * @author Eric Lafortune
 */
public class BranchTargetFinder
implements   AttrInfoVisitor,
             InstructionVisitor,
             ExceptionInfoVisitor,
             CpInfoVisitor
{
    private static final byte INSTRUCTION       =  1;
    private static final byte BRANCH_ORIGIN     =  2;
    private static final byte BRANCH_TARGET     =  4;
    private static final byte INITIALIZER       =  8;
    private static final byte EXCEPTION_START   = 16;
    private static final byte EXCEPTION_END     = 32;
    private static final byte EXCEPTION_HANDLER = 64;


    private byte[] instructionMarks;

    private boolean isInitializer;


    /**
     * Creates a new BranchTargetFinder.
     * @param codeLength an estimate of the maximum length of all the code that
     *                   will be edited.
     */
    public BranchTargetFinder(int codeLength)
    {
        instructionMarks = new byte[codeLength + 1];
    }


    /**
     * Returns whether there is an instruction at the given offset in the
     * CodeAttrInfo that was visited most recently.
     */
    public boolean isInstruction(int offset)
    {
        return (instructionMarks[offset] & INSTRUCTION) != 0;
    }


    /**
     * Returns whether the instruction at the given offset is the target of a
     * branch instruction or an exception in the CodeAttrInfo that was visited
     * most recently.
     */
    public boolean isTarget(int offset)
    {
        return (instructionMarks[offset] & (BRANCH_TARGET   |
                                            EXCEPTION_START |
                                            EXCEPTION_END   |
                                            EXCEPTION_HANDLER)) != 0;
    }


    /**
     * Returns whether the instruction at the given offset is the origin of a
     * branch instruction in the CodeAttrInfo that was visited most recently.
     */
    public boolean isBranchOrigin(int offset)
    {
        return (instructionMarks[offset] & BRANCH_ORIGIN) != 0;
    }


    /**
     * Returns whether the instruction at the given offset is the target of a
     * branch instruction in the CodeAttrInfo that was visited most recently.
     */
    public boolean isBranchTarget(int offset)
    {
        return (instructionMarks[offset] & BRANCH_TARGET) != 0;
    }


    /**
     * Returns whether the instruction at the given offset is the start of an
     * exception try block in the CodeAttrInfo that was visited most recently.
     */
    public boolean isExceptionStart(int offset)
    {
        return (instructionMarks[offset] & EXCEPTION_START) != 0;
    }


    /**
     * Returns whether the instruction at the given offset is the end of an
     * exception try block in the CodeAttrInfo that was visited most recently.
     */
    public boolean isExceptionEnd(int offset)
    {
        return (instructionMarks[offset] & EXCEPTION_END) != 0;
    }


    /**
     * Returns whether the instruction at the given offset is the start of an
     * exception catch block in the CodeAttrInfo that was visited most recently.
     */
    public boolean isExceptionHandler(int offset)
    {
        return (instructionMarks[offset] & EXCEPTION_HANDLER) != 0;
    }


    /**
     * Returns whether the instruction at the given offset is the special
     * invocation of an instance initializer in the CodeAttrInfo that was
     * visited most recently.
     */
    public boolean isInitializer(int offset)
    {
        return (instructionMarks[offset] & INITIALIZER) != 0;
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
        if (instructionMarks.length < length)
        {
            // Create a new boolean array.
            instructionMarks = new byte[length];
        }
        else
        {
            // Reset the boolean array.
            for (int index = 0; index < length; index++)
            {
                instructionMarks[index] = 0;
            }
        }

        // The first instruction and the end of the code are always branch targets.
        instructionMarks[0]                         = BRANCH_TARGET;
        instructionMarks[codeAttrInfo.u4codeLength] = BRANCH_TARGET;

        // Mark branch targets by going over all instructions.
        codeAttrInfo.instructionsAccept(classFile, methodInfo, this);

        // Mark branch targets in the exception table.
        codeAttrInfo.exceptionsAccept(classFile, methodInfo, this);
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction)
    {
        // Mark the instruction.
        instructionMarks[offset] |= INSTRUCTION;

        byte opcode = simpleInstruction.opcode;
        if (opcode == InstructionConstants.OP_RET     ||
            opcode == InstructionConstants.OP_IRETURN ||
            opcode == InstructionConstants.OP_LRETURN ||
            opcode == InstructionConstants.OP_FRETURN ||
            opcode == InstructionConstants.OP_DRETURN ||
            opcode == InstructionConstants.OP_ARETURN ||
            opcode == InstructionConstants.OP_ATHROW)
        {
            instructionMarks[offset] |= BRANCH_ORIGIN;
        }
    }


    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction)
    {
        // Mark the instruction.
        instructionMarks[offset] |= INSTRUCTION;

        // Check if the instruction is an initializer invocation.
        isInitializer = false;
        classFile.constantPoolEntryAccept(cpInstruction.cpIndex, this);
        if (isInitializer)
        {
            instructionMarks[offset] |= INITIALIZER;
        }
    }


    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction)
    {
        // Mark the instruction.
        instructionMarks[offset] |= INSTRUCTION;
    }


    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction)
    {
        // Mark the instruction.
        instructionMarks[offset] |= INSTRUCTION | BRANCH_ORIGIN;

        // Mark the branch target.
        instructionMarks[offset + branchInstruction.branchOffset] |= BRANCH_TARGET;
    }

    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction)
    {
        // Mark the instruction.
        instructionMarks[offset] |= INSTRUCTION | BRANCH_ORIGIN;

        // Mark the branch targets of the default jump offset.
        instructionMarks[offset + tableSwitchInstruction.defaultOffset] |= BRANCH_TARGET;

        // Mark the branch targets of the jump offsets.
        markBranchTargets(offset,
                          tableSwitchInstruction.jumpOffsets,
                          tableSwitchInstruction.highCase -
                          tableSwitchInstruction.lowCase + 1);
    }

    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        // Mark the instruction.
        instructionMarks[offset] |= INSTRUCTION | BRANCH_ORIGIN;

        // Mark the branch targets of the default jump offset.
        instructionMarks[offset + lookUpSwitchInstruction.defaultOffset] |= BRANCH_TARGET;

        // Mark the branch targets of the jump offsets.
        markBranchTargets(offset,
                          lookUpSwitchInstruction.jumpOffsets,
                          lookUpSwitchInstruction.jumpOffsetCount);
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}
    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo) {}
    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo) {}
    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}


    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo)
    {
        isInitializer = methodrefCpInfo.getName(classFile).equals(ClassConstants.INTERNAL_METHOD_NAME_INIT);
    }


    // Implementations for ExceptionInfoVisitor.

    public void visitExceptionInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, ExceptionInfo exceptionInfo)
    {
        // Remap the code offsets. Note that the branch target array also has
        // an entry for the first offset after the code, for u2endpc.
        instructionMarks[exceptionInfo.u2startpc]   |= EXCEPTION_START;
        instructionMarks[exceptionInfo.u2endpc]     |= EXCEPTION_END;
        instructionMarks[exceptionInfo.u2handlerpc] |= EXCEPTION_HANDLER;
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
            instructionMarks[offset + jumpOffsets[index]] |= BRANCH_TARGET;
        }
    }
}
