/* $Id: PushPopRemover.java,v 1.11.2.2 2006/02/13 00:20:43 eric Exp $
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
import proguard.classfile.editor.*;
import proguard.classfile.instruction.*;

/**
 * This InstructionVisitor deletes all push/pop instruction pairs. In this
 * context, push instructions are instructions that push values onto the stack,
 * like dup and load instructions.
 *
 * @author Eric Lafortune
 */
public class PushPopRemover implements InstructionVisitor
{
    private BranchTargetFinder branchTargetFinder;
    private CodeAttrInfoEditor codeAttrInfoEditor;
    private InstructionVisitor extraInstructionVisitor;


    /**
     * Creates a new PushPopRemover.
     * @param branchTargetFinder      a branch target finder that has been
     *                                initialized to indicate branch targets
     *                                in the visited code.
     * @param codeAttrInfoEditor      a code editor that can be used for
     *                                accumulating changes to the code.
     */
    public PushPopRemover(BranchTargetFinder branchTargetFinder,
                          CodeAttrInfoEditor codeAttrInfoEditor)
    {
        this(branchTargetFinder, codeAttrInfoEditor, null);
    }


    /**
     * Creates a new PushPopRemover.
     * @param branchTargetFinder      a branch target finder that has been
     *                                initialized to indicate branch targets
     *                                in the visited code.
     * @param codeAttrInfoEditor      a code editor that can be used for
     *                                accumulating changes to the code.
     * @param extraInstructionVisitor an optional extra visitor for all deleted
     *                                push instructions.
     */
    public PushPopRemover(BranchTargetFinder branchTargetFinder,
                          CodeAttrInfoEditor codeAttrInfoEditor,
                          InstructionVisitor extraInstructionVisitor)
    {
        this.branchTargetFinder      = branchTargetFinder;
        this.codeAttrInfoEditor      = codeAttrInfoEditor;
        this.extraInstructionVisitor = extraInstructionVisitor;
    }


    // Implementations for InstructionVisitor.

    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction) {}
    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction) {}
    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction) {}
    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction) {}


    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction)
    {
        switch (simpleInstruction.opcode)
        {
            case InstructionConstants.OP_ICONST_M1:
            case InstructionConstants.OP_ICONST_0:
            case InstructionConstants.OP_ICONST_1:
            case InstructionConstants.OP_ICONST_2:
            case InstructionConstants.OP_ICONST_3:
            case InstructionConstants.OP_ICONST_4:
            case InstructionConstants.OP_ICONST_5:
            case InstructionConstants.OP_LCONST_0:
            case InstructionConstants.OP_LCONST_1:
            case InstructionConstants.OP_FCONST_0:
            case InstructionConstants.OP_FCONST_1:
            case InstructionConstants.OP_FCONST_2:
            case InstructionConstants.OP_DCONST_0:
            case InstructionConstants.OP_DCONST_1:

            case InstructionConstants.OP_DUP:
            case InstructionConstants.OP_DUP2:
            case InstructionConstants.OP_BIPUSH:
            case InstructionConstants.OP_SIPUSH:
            case InstructionConstants.OP_LDC:
            case InstructionConstants.OP_LDC_W:
            case InstructionConstants.OP_LDC2_W:
                // All these simple instructions are pushing instructions.
                deleteWithSubsequentPop(classFile, methodInfo, codeAttrInfo, offset, simpleInstruction);
                break;
        }
    }

    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction)
    {
        if (variableInstruction.isLoad() &&
            variableInstruction.opcode != InstructionConstants.OP_RET)
        {
            // All load instructions are pushing instructions.
            deleteWithSubsequentPop(classFile, methodInfo, codeAttrInfo, offset, variableInstruction);
        }
    }


    // Small utility methods.

    /**
     * Deletes the given instruction and its subsequent compatible pop instruction,
     * if any, and if the latter is not a branch target.
     */
    private void deleteWithSubsequentPop(ClassFile    classFile,
                                         MethodInfo   methodInfo,
                                         CodeAttrInfo codeAttrInfo,
                                         int          offset,
                                         Instruction  instruction)
    {
        boolean isCategory2 = instruction.isCategory2();

        int nextOffset = offset + instruction.length(offset);

        if (!codeAttrInfoEditor.isModified(offset)     &&
            !codeAttrInfoEditor.isModified(nextOffset) &&
            !branchTargetFinder.isTarget(nextOffset))
        {
            Instruction nextInstruction = InstructionFactory.create(codeAttrInfo.code,
                                                                    nextOffset);
            int nextOpcode = nextInstruction.opcode;
            if ((nextOpcode == InstructionConstants.OP_POP ||
                 nextOpcode == InstructionConstants.OP_POP2) &&
                nextInstruction.isCategory2() == isCategory2)
            {
                // Delete the pushing instruction and the pop instruction.
                codeAttrInfoEditor.deleteInstruction(offset);
                codeAttrInfoEditor.deleteInstruction(nextOffset);

                // Visit the instruction, if required.
                if (extraInstructionVisitor != null)
                {
                    instruction.accept(classFile, methodInfo, codeAttrInfo, offset, extraInstructionVisitor);
                }
            }
        }
    }
}
