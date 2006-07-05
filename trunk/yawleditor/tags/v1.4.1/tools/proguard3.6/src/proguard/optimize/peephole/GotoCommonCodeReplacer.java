/* $Id: GotoCommonCodeReplacer.java,v 1.2.2.3 2006/02/13 00:20:43 eric Exp $
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
import proguard.classfile.attribute.CodeAttrInfo;
import proguard.classfile.editor.CodeAttrInfoEditor;
import proguard.classfile.instruction.*;

/**
 * This InstructionVisitor redirects unconditional branches so any common code
 * is shared, and the code preceding the branch can be removed.
 *
 * @author Eric Lafortune
 */
public class GotoCommonCodeReplacer implements InstructionVisitor
{
    private static final boolean DEBUG = false;


    private BranchTargetFinder branchTargetFinder;
    private CodeAttrInfoEditor codeAttrInfoEditor;
    private InstructionVisitor extraInstructionVisitor;


    /**
     * Creates a new GotoCommonCodeReplacer.
     * @param branchTargetFinder      a branch target finder that has been
     *                                initialized to indicate branch targets
     *                                in the visited code.
     * @param codeAttrInfoEditor      a code editor that can be used for
     *                                accumulating changes to the code.
     */
    public GotoCommonCodeReplacer(BranchTargetFinder branchTargetFinder,
                                  CodeAttrInfoEditor codeAttrInfoEditor)
    {
        this(branchTargetFinder, codeAttrInfoEditor, null);
    }


    /**
     * Creates a new GotoCommonCodeReplacer.
     * @param branchTargetFinder      a branch target finder that has been
     *                                initialized to indicate branch targets
     *                                in the visited code.
     * @param codeAttrInfoEditor      a code editor that can be used for
     *                                accumulating changes to the code.
     * @param extraInstructionVisitor an optional extra visitor for all replaced
     *                                goto instructions.
     */
    public GotoCommonCodeReplacer(BranchTargetFinder branchTargetFinder,
                                  CodeAttrInfoEditor codeAttrInfoEditor,
                                  InstructionVisitor extraInstructionVisitor)
    {
        this.branchTargetFinder      = branchTargetFinder;
        this.codeAttrInfoEditor      = codeAttrInfoEditor;
        this.extraInstructionVisitor = extraInstructionVisitor;
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction) {}
    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction) {}
    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction) {}
    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction) {}
    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction) {}


    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction)
    {
        // Check if the instruction is an unconditional goto instruction that
        // isn't the target of a branch itself.
        byte opcode = branchInstruction.opcode;
        if ((opcode == InstructionConstants.OP_GOTO ||
             opcode == InstructionConstants.OP_GOTO_W) &&
             !branchTargetFinder.isBranchTarget(offset))
        {
            int branchOffset = branchInstruction.branchOffset;
            int targetOffset = offset + branchOffset;

            // Get the number of common bytes.
            int commonCount = commonByteCodeCount(codeAttrInfo, offset, targetOffset);

            if (commonCount > 0 &&
                !exceptionBoundary(codeAttrInfo, offset, targetOffset))
            {
                if (DEBUG)
                {
                    System.out.println("GotoCommonCodeReplacer: "+classFile.getName()+"."+methodInfo.getName(classFile)+" ("+commonCount+" instructions)");
                }

                // Delete the common instructions.
                for (int delta = 0; delta <= commonCount; delta++)
                {
                    int deleteOffset = offset - delta;
                    if (branchTargetFinder.isInstruction(deleteOffset))
                    {
                        codeAttrInfoEditor.replaceInstruction(     deleteOffset, null);
                        codeAttrInfoEditor.insertBeforeInstruction(deleteOffset, null);
                        codeAttrInfoEditor.insertAfterInstruction( deleteOffset, null);

                        codeAttrInfoEditor.deleteInstruction(deleteOffset);
                    }
                }

                // Redirect the goto instruction, if it is still necessary.
                int newBranchOffset = branchOffset - commonCount;
                if (newBranchOffset != branchInstruction.length(offset))
                {
                    Instruction newGotoInstruction =
                         new BranchInstruction(opcode, newBranchOffset);
                    codeAttrInfoEditor.replaceInstruction(offset,
                                                          newGotoInstruction);
                }

                // Visit the instruction, if required.
                if (extraInstructionVisitor != null)
                {
                    extraInstructionVisitor.visitBranchInstruction(classFile, methodInfo, codeAttrInfo, offset, branchInstruction);
                }
            }
        }
    }


    // Small utility methods.

    /**
     * Returns the number of common bytes preceding the given offsets,
     * avoiding branches and exception blocks.
     */
    private int commonByteCodeCount(CodeAttrInfo codeAttrInfo, int offset1, int offset2)
    {
        // Find the block of common instructions preceding it.
        byte[] code = codeAttrInfo.code;

        int successfulDelta = 0;

        for (int delta = 1;
             delta <= offset1 &&
             delta <= offset2 &&
             offset2 - delta != offset1;
             delta++)
        {
            int newOffset1 = offset1 - delta;
            int newOffset2 = offset2 - delta;

            // Is the code identical at both offsets?
            if (code[newOffset1] != code[newOffset2])
            {
                break;
            }

            // Are there instructions at either offset but not both?
            if (branchTargetFinder.isInstruction(newOffset1) ^
                branchTargetFinder.isInstruction(newOffset2))
            {
                break;
            }

            // Are there instructions at both offsets?
            if (branchTargetFinder.isInstruction(newOffset1) &&
                branchTargetFinder.isInstruction(newOffset2))
            {
                // Are the offsets involved in some branches?
                // Note that the preverifier also doesn't like
                // initializer invocations to be moved around.
                // Also note that the preverifier doesn't like pop instructions
                // that work on different operands.
                if (branchTargetFinder.isBranchOrigin(newOffset1)   ||
                    branchTargetFinder.isBranchTarget(newOffset1)   ||
                    branchTargetFinder.isExceptionStart(newOffset1) ||
                    branchTargetFinder.isExceptionEnd(newOffset1)   ||
                    branchTargetFinder.isInitializer(newOffset1)    ||
                    branchTargetFinder.isExceptionStart(newOffset2) ||
                    branchTargetFinder.isExceptionEnd(newOffset2)   ||
                    isPop(code[newOffset1]))
                {
                    break;
                }

                successfulDelta = delta;
            }
        }

        return successfulDelta;
    }


    /**
     * Returns whether the given opcode represents a pop instruction
     * (pop, pop2).
     */
    private boolean isPop(byte opcode)
    {
        return opcode == InstructionConstants.OP_POP ||
               opcode == InstructionConstants.OP_POP2;
    }


    /**
     * Returns the whether there is a boundary of an exception block between
     * the given offsets (including both).
     */
    private boolean exceptionBoundary(CodeAttrInfo codeAttrInfo, int offset1, int offset2)
    {
        // Swap the offsets if the second one is smaller than the first one.
        if (offset2 < offset1)
        {
            int offset = offset1;
            offset1 = offset2;
            offset2 = offset;
        }

        // Check if there is a boundary of an exception block.
        for (int offset = offset1; offset <= offset2; offset++)
        {
            if (branchTargetFinder.isExceptionStart(offset) ||
                branchTargetFinder.isExceptionEnd(offset))
            {
                return true;
            }
        }

        return false;
    }
}
