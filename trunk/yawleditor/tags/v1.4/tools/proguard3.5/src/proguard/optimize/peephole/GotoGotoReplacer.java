/* $Id: GotoGotoReplacer.java,v 1.2.2.1 2006/01/16 22:57:56 eric Exp $
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
 * This InstructionVisitor simplifies unconditional branches to other
 * unconditional branches.
 *
 * @author Eric Lafortune
 */
public class GotoGotoReplacer implements InstructionVisitor
{
    private CodeAttrInfoEditor codeAttrInfoEditor;
    private InstructionVisitor extraInstructionVisitor;


    /**
     * Creates a new GotoGotoReplacer.
     * @param codeAttrInfoEditor      a code editor that can be used for
     *                                accumulating changes to the code.
     */
    public GotoGotoReplacer(CodeAttrInfoEditor codeAttrInfoEditor)
    {
        this(codeAttrInfoEditor, null);
    }


    /**
     * Creates a new GotoGotoReplacer.
     * @param codeAttrInfoEditor      a code editor that can be used for
     *                                accumulating changes to the code.
     * @param extraInstructionVisitor an optional extra visitor for all replaced
     *                                goto instructions.
     */
    public GotoGotoReplacer(CodeAttrInfoEditor codeAttrInfoEditor,
                            InstructionVisitor extraInstructionVisitor)
    {
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
        // Check if the instruction is an unconditional goto instruction.
        byte opcode = branchInstruction.opcode;
        if (opcode == InstructionConstants.OP_GOTO ||
            opcode == InstructionConstants.OP_GOTO_W)
        {
            // Check if the goto instruction points to another simple goto
            // instruction.
            int branchOffset = branchInstruction.branchOffset;
            int targetOffset = offset + branchOffset;

            if (branchOffset != branchInstruction.length(offset) &&
                !codeAttrInfoEditor.isModified(offset) &&
                !codeAttrInfoEditor.isModified(targetOffset))
            {
                Instruction targetInstruction = InstructionFactory.create(codeAttrInfo.code,
                                                                          targetOffset);
                if (targetInstruction.opcode == InstructionConstants.OP_GOTO)
                {
                    // Simplify the goto instruction.
                    int targetBranchOffset   = ((BranchInstruction)targetInstruction).branchOffset;
                    
                    Instruction newBranchInstruction =
                         new BranchInstruction(opcode,
                                               (branchOffset + targetBranchOffset));
                    codeAttrInfoEditor.replaceInstruction(offset,
                                                          newBranchInstruction);

                    // Visit the instruction, if required.
                    if (extraInstructionVisitor != null)
                    {
                        extraInstructionVisitor.visitBranchInstruction(classFile, methodInfo, codeAttrInfo, offset, branchInstruction);
                    }
                }
            }
        }
    }
}
