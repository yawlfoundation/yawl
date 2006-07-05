/* $Id: StackSizeUpdater.java,v 1.12 2004/11/20 15:41:24 eric Exp $
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
package proguard.classfile.editor;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.instruction.*;
import proguard.classfile.visitor.*;

/**
 * This AttrInfoVisitor computes and updates the maximum stack size of the
 * code attributes that it visits.
 *
 * @author Eric Lafortune
 */
class      StackSizeUpdater
implements AttrInfoVisitor,
           InstructionVisitor,
           ExceptionInfoVisitor
{
    //*
    private static final boolean DEBUG = false;
    /*/
    private static boolean DEBUG       = true;
    //*/


    private boolean[] evaluated;

    private boolean exitInstructionBlock;

    private int stackSize;
    private int maxStackSize;


    /**
     * Creates a new StackSizeUpdater.
     * @param codeLength an estimate of the maximum length of all the code that
     *                   will be visited.
     */
    public StackSizeUpdater(int codeLength)
    {
        evaluated = new boolean[codeLength];
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
//        DEBUG =
//            classFile.getName().equals("abc/Def") &&
//            methodInfo.getName(classFile).equals("abc");

        // Try to reuse the previous array.
        int codeLength = codeAttrInfo.u4codeLength;
        if (evaluated.length < codeLength)
        {
            evaluated = new boolean[codeLength];
        }
        else
        {
            for (int index = 0; index < codeLength; index++)
            {
                evaluated[index] = false;
            }
        }

        // The initial stack is always empty.
        stackSize    = 0;
        maxStackSize = 0;

        // Evaluate the instruction block starting at the entry point of the method.
        evaluateInstructionBlock(classFile, methodInfo, codeAttrInfo, 0);

        // Evaluate the exception handlers.
        codeAttrInfo.exceptionsAccept(classFile, methodInfo, this);

        // Update the maximum stack size.
        codeAttrInfo.u2maxStack = maxStackSize;
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction)
    {
        byte opcode = simpleInstruction.opcode;

        // Some simple instructions exit from the current instruction block.
        exitInstructionBlock =
            opcode == InstructionConstants.OP_IRETURN ||
            opcode == InstructionConstants.OP_LRETURN ||
            opcode == InstructionConstants.OP_FRETURN ||
            opcode == InstructionConstants.OP_DRETURN ||
            opcode == InstructionConstants.OP_ARETURN ||
            opcode == InstructionConstants.OP_RETURN  ||
            opcode == InstructionConstants.OP_ATHROW;
    }

    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction)
    {
        // Constant pool instructions never end the current instruction block.
        exitInstructionBlock = false;
    }

    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction)
    {
        byte opcode = variableInstruction.opcode;

        // The ret instruction end the current instruction block.
        exitInstructionBlock =
            opcode == InstructionConstants.OP_RET;
    }

    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction)
    {
        byte opcode = branchInstruction.opcode;

        // Evaluate the target instruction blocks.
        evaluateInstructionBlock(classFile,
                                 methodInfo,
                                 codeAttrInfo,
                                 offset +
                                 branchInstruction.branchOffset);

        // Evaluate the instructions after a subroutine branch.
        if (opcode == InstructionConstants.OP_JSR ||
            opcode == InstructionConstants.OP_JSR_W)
        {
            // We assume subroutine calls (jsr and jsr_w instructions) don't
            // change the stack, other than popping the return value.
            stackSize -= 1;

            evaluateInstructionBlock(classFile,
                                     methodInfo,
                                     codeAttrInfo,
                                     offset + branchInstruction.length(offset));
        }

        // Some branch instructions always end the current instruction block.
        exitInstructionBlock =
            opcode == InstructionConstants.OP_GOTO   ||
            opcode == InstructionConstants.OP_GOTO_W ||
            opcode == InstructionConstants.OP_JSR    ||
            opcode == InstructionConstants.OP_JSR_W;
    }


    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction)
    {
        // Evaluate the target instruction blocks.
        evaluateInstructionBlocks(classFile,
                                  methodInfo,
                                  codeAttrInfo,
                                  offset,
                                  tableSwitchInstruction.jumpOffsets,
                                  tableSwitchInstruction.jumpOffsetCount,
                                  tableSwitchInstruction.defaultOffset);

        // The switch instruction always ends the current instruction block.
        exitInstructionBlock = true;
    }


    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        // Evaluate the target instruction blocks.
        evaluateInstructionBlocks(classFile,
                                  methodInfo,
                                  codeAttrInfo,
                                  offset,
                                  lookUpSwitchInstruction.jumpOffsets,
                                  lookUpSwitchInstruction.jumpOffsetCount,
                                  lookUpSwitchInstruction.defaultOffset);

        // The switch instruction always ends the current instruction block.
        exitInstructionBlock = true;
    }


    // Implementations for ExceptionInfoVisitor.

    public void visitExceptionInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, ExceptionInfo exceptionInfo)
    {
        // The stack size when entering the exception handler is always 1.
        stackSize = 1;

        // Evaluate the instruction block starting at the entry point of the
        // exception handler.
        evaluateInstructionBlock(classFile,
                                 methodInfo,
                                 codeAttrInfo,
                                 exceptionInfo.u2handlerpc);
    }


    // Small utility methods.

    /**
     * Evaluates the blocks of instructions starting at the given jump offsets.
     */
    private void evaluateInstructionBlocks(ClassFile    classFile,
                                           MethodInfo   methodInfo,
                                           CodeAttrInfo codeAttrInfo,
                                           int          instructionOffset,
                                           int[]        jumpOffsets,
                                           int          jumpOffsetCount,
                                           int          defaultOffset)
    {
        // The jump offset arrays may be reused, so we have to make a local copy.
        int[] jumpOffsetsCopy = new int[jumpOffsetCount];
        System.arraycopy(jumpOffsets, 0, jumpOffsetsCopy, 0,  jumpOffsetCount);
        jumpOffsets = jumpOffsetsCopy;

        // Loop over all jump offsets.
        for (int index = 0; index < jumpOffsetCount; index++)
        {
            // Evaluate the jump instruction block.
            evaluateInstructionBlock(classFile,
                                     methodInfo,
                                     codeAttrInfo,
                                     instructionOffset + jumpOffsets[index]);
        }

        // Also evaluate the default instruction block.
        evaluateInstructionBlock(classFile,
                                 methodInfo,
                                 codeAttrInfo,
                                 instructionOffset + defaultOffset);
    }


    /**
     * Evaluates a block of instructions that hasn't been handled before,
     * starting at the given offset and ending a branch instruction, a return
     * instruction, or a throw instruction. Branch instructions are handled
     * recursively.
     */
    private void evaluateInstructionBlock(ClassFile    classFile,
                                          MethodInfo   methodInfo,
                                          CodeAttrInfo codeAttrInfo,
                                          int          instructionOffset)
    {
        if (DEBUG)
        {
            System.out.println("--");
        }

        // Remember the initial stack size.
        int initialStackSize = stackSize;

        // Remember the maximum stack size.
        if (maxStackSize < stackSize)
        {
            maxStackSize = stackSize;
        }

        // Evaluate any instructions that haven't been evaluated before.
        while (!evaluated[instructionOffset])
        {
            // Mark the instruction as evaluated.
            evaluated[instructionOffset] = true;

            Instruction instruction = InstructionFactory.create(codeAttrInfo.code,
                                                                instructionOffset);

            if (DEBUG)
            {
                int stackPushCount = instruction.stackPushCount(classFile);
                int stackPopCount  = instruction.stackPopCount(classFile);
                System.out.println("["+instructionOffset+"]: "+
                                   stackSize+" - "+
                                   stackPopCount+" + "+
                                   stackPushCount+" = "+
                                   (stackSize+stackPushCount-stackPopCount)+": "+
                                   instruction.toString());
            }

            // Compute the instruction's effect on the stack size.
            stackSize += instruction.stackPushCount(classFile) -
                         instruction.stackPopCount(classFile);

            // Remember the maximum stack size.
            if (maxStackSize < stackSize)
            {
                maxStackSize = stackSize;
            }

            // Remember the next instruction offset.
            int nextInstructionOffset = instructionOffset +
                                        instruction.length(instructionOffset);

            // Visit the instruction, in order to handle branches.
            instruction.accept(classFile, methodInfo, codeAttrInfo, instructionOffset, this);

            // Stop evaluating after a branch.
            if (exitInstructionBlock)
            {
                break;
            }

            // Continue with the next instruction.
            instructionOffset = nextInstructionOffset;

            if (DEBUG)
            {
                if (evaluated[instructionOffset])
                {
                    System.out.println("-- ("+instructionOffset+" already evaluated)");
                }
            }
        }

        // Restore the stack size for possible subsequent instruction blocks.
        this.stackSize = initialStackSize;
    }
}
