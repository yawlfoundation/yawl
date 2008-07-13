/* $Id: LoadStoreRemover.java,v 1.10.2.3 2007/01/18 21:31:53 eric Exp $
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
package proguard.optimize.peephole;

import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttrInfo;
import proguard.classfile.editor.CodeAttrInfoEditor;
import proguard.classfile.instruction.*;

/**
 * This InstructionVisitor deletes load/store instruction pairs.
 *
 * @author Eric Lafortune
 */
public class LoadStoreRemover implements InstructionVisitor
{
    private BranchTargetFinder branchTargetFinder;
    private CodeAttrInfoEditor codeAttrInfoEditor;
    private InstructionVisitor extraInstructionVisitor;


    /**
     * Creates a new LoadStoreRemover.
     * @param branchTargetFinder      a branch target finder that has been
     *                                initialized to indicate branch targets
     *                                in the visited code.
     * @param codeAttrInfoEditor      a code editor that can be used for
     *                                accumulating changes to the code.
     */
    public LoadStoreRemover(BranchTargetFinder branchTargetFinder,
                            CodeAttrInfoEditor codeAttrInfoEditor)
    {
        this(branchTargetFinder, codeAttrInfoEditor, null);
    }


    /**
     * Creates a new LoadStoreRemover.
     * @param branchTargetFinder      a branch target finder that has been
     *                                initialized to indicate branch targets
     *                                in the visited code.
     * @param codeAttrInfoEditor      a code editor that can be used for
     *                                accumulating changes to the code.
     * @param extraInstructionVisitor an optional extra visitor for all deleted
     *                                load instructions.
     */
    public LoadStoreRemover(BranchTargetFinder branchTargetFinder,
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
    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction) {}
    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction) {}
    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction) {}


    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction)
    {
        // Is this instruction a load instruction?
        if (variableInstruction.isLoad() &&
            variableInstruction.opcode != InstructionConstants.OP_RET)
        {
            int variableIndex = variableInstruction.variableIndex;

            int nextOffset = offset + variableInstruction.length(offset);

            if (!codeAttrInfoEditor.isModified(offset)     &&
                !codeAttrInfoEditor.isModified(nextOffset) &&
                !branchTargetFinder.isTarget(nextOffset))
            {
                // Is the next instruction a corresponding store instruction?
                Instruction nextInstruction = InstructionFactory.create(codeAttrInfo.code,
                                                                        nextOffset);

                if (nextInstruction instanceof VariableInstruction)
                {
                    variableInstruction = (VariableInstruction)nextInstruction;
                    if (!variableInstruction.isLoad()                              &&
                        variableInstruction.opcode != InstructionConstants.OP_IINC &&
                        variableInstruction.variableIndex == variableIndex)
                    {
                        // Delete both instructions.
                        codeAttrInfoEditor.deleteInstruction(offset);
                        codeAttrInfoEditor.deleteInstruction(nextOffset);

                        // Visit the instruction, if required.
                        if (extraInstructionVisitor != null)
                        {
                            extraInstructionVisitor.visitVariableInstruction(classFile, methodInfo, codeAttrInfo, offset, variableInstruction);
                        }
                    }
                }
            }
        }
    }
}
