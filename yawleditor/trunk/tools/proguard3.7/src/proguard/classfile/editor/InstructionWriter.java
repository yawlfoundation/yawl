/* $Id: InstructionWriter.java,v 1.1.2.2 2006/11/26 15:25:17 eric Exp $
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
package proguard.classfile.editor;

import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.instruction.*;
import proguard.classfile.*;

/**
 * This InstructionVisitor writes out the instructions that it visits,
 * collecting instructions that have to be widened. As an AttrInfoVisitor,
 * it then applies the collected changes. The process will be repeated
 * recursively, if necessary.
 *
 * @author Eric Lafortune
 */
public class InstructionWriter
  implements InstructionVisitor,
             AttrInfoVisitor
{
    private int codeLength;

    private CodeAttrInfoEditor codeAttrInfoEditor;


    /**
     * Resets the accumulated code changes.
     * @param codeLength the length of the code that will be edited next.
     */
    public void reset(int codeLength)
    {
        this.codeLength = codeLength;

        // The code attribute editor has to be created lazily.
        if (codeAttrInfoEditor != null)
        {
            codeAttrInfoEditor.reset(codeLength);
        }
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction)
    {
        // Try to write out the instruction.
        // Simple instructions should always fit.
        simpleInstruction.write(codeAttrInfo, offset);
    }


    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction)
    {
        try
        {
            // Try to write out the instruction.
            cpInstruction.write(codeAttrInfo, offset);
        }
        catch (IllegalArgumentException exception)
        {
            // Create a new constant instruction that will fit.
            Instruction replacementInstruction =
                new CpInstruction().copy(cpInstruction).shrink();

            replaceInstruction(offset, replacementInstruction);

            // Write out a dummy constant instruction for now.
            cpInstruction.cpIndex  = 0;
            cpInstruction.constant = 0;
            cpInstruction.write(codeAttrInfo, offset);
        }
    }


    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction)
    {
        try
        {
            // Try to write out the instruction.
            variableInstruction.write(codeAttrInfo, offset);
        }
        catch (IllegalArgumentException exception)
        {
            // Create a new variable instruction that will fit.
            Instruction replacementInstruction =
                new VariableInstruction(variableInstruction.opcode,
                                        variableInstruction.variableIndex,
                                        variableInstruction.constant);

            replaceInstruction(offset, replacementInstruction);

            // Write out a dummy variable instruction for now.
            variableInstruction.variableIndex = 0;
            variableInstruction.constant      = 0;
            variableInstruction.write(codeAttrInfo, offset);
        }
    }


    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction)
    {
        try
        {
            // Try to write out the instruction.
            branchInstruction.write(codeAttrInfo, offset);
        }
        catch (IllegalArgumentException exception)
        {
            // Create a new unconditional branch that will fit.
            Instruction replacementInstruction =
                new BranchInstruction(InstructionConstants.OP_GOTO_W,
                                      branchInstruction.branchOffset);

            // Create a new instruction that will fit.
            switch (branchInstruction.opcode)
            {
                default:
                {
                    // Create a new branch instruction that will fit.
                    replacementInstruction =
                        new BranchInstruction().copy(branchInstruction).shrink();

                    break;
                }

                // Some special cases, for which a wide branch doesn't exist.
                case InstructionConstants.OP_IFEQ:
                case InstructionConstants.OP_IFNE:
                case InstructionConstants.OP_IFLT:
                case InstructionConstants.OP_IFGE:
                case InstructionConstants.OP_IFGT:
                case InstructionConstants.OP_IFLE:
                case InstructionConstants.OP_IFICMPEQ:
                case InstructionConstants.OP_IFICMPNE:
                case InstructionConstants.OP_IFICMPLT:
                case InstructionConstants.OP_IFICMPGE:
                case InstructionConstants.OP_IFICMPGT:
                case InstructionConstants.OP_IFICMPLE:
                case InstructionConstants.OP_IFACMPEQ:
                case InstructionConstants.OP_IFACMPNE:
                {
                    // Insert the complementary conditional branch.
                    Instruction complementaryConditionalBranch =
                        new BranchInstruction((byte)(((branchInstruction.opcode+1) ^ 1) - 1),
                                              (1+2) + (1+4));

                    insertBeforeInstruction(offset, complementaryConditionalBranch);

                    // Create a new unconditional branch that will fit.
                    break;
                }

                case InstructionConstants.OP_IFNULL:
                case InstructionConstants.OP_IFNONNULL:
                {
                    // Insert the complementary conditional branch.
                    Instruction complementaryConditionalBranch =
                        new BranchInstruction((byte)(branchInstruction.opcode ^ 1),
                                              (1+2) + (1+4));

                    insertBeforeInstruction(offset, complementaryConditionalBranch);

                    // Create a new unconditional branch that will fit.
                    break;
                }
            }

            replaceInstruction(offset, replacementInstruction);

            // Write out a dummy branch instruction for now.
            branchInstruction.branchOffset = 0;
            branchInstruction.write(codeAttrInfo, offset);
        }
    }


    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction)
    {
        // Try to write out the instruction.
        // Table switch instructions should always fit.
        tableSwitchInstruction.write(codeAttrInfo, offset);
    }


    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        // Try to write out the instruction.
        // Table switch instructions should always fit.
        lookUpSwitchInstruction.write(codeAttrInfo, offset);
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
        // Avoid doing any work if nothing is changing anyway.
        if (codeAttrInfoEditor != null)
        {
            // Apply the collected expansions.
            codeAttrInfoEditor.visitCodeAttrInfo(classFile, methodInfo, codeAttrInfo);

            // Clear the modifications for the next run.
            codeAttrInfoEditor = null;
        }
    }


    // Small utility methods.

    /**
     * Remembers to place the given instruction right before the instruction
     * at the given offset.
     */
    private void insertBeforeInstruction(int instructionOffset, Instruction instruction)
    {
        ensureCodeAttrInfoEditor();

        // Replace the instruction.
        codeAttrInfoEditor.insertBeforeInstruction(instructionOffset, instruction);
    }


    /**
     * Remembers to replace the instruction at the given offset by the given
     * instruction.
     */
    private void replaceInstruction(int instructionOffset, Instruction instruction)
    {
        ensureCodeAttrInfoEditor();

        // Replace the instruction.
        codeAttrInfoEditor.replaceInstruction(instructionOffset, instruction);
    }


    /**
     * Remembers to place the given instruction right after the instruction
     * at the given offset.
     */
    private void insertAfterInstruction(int instructionOffset, Instruction instruction)
    {
        ensureCodeAttrInfoEditor();

        // Replace the instruction.
        codeAttrInfoEditor.insertAfterInstruction(instructionOffset, instruction);
    }


    /**
     * Makes sure there is a code attribute editor for the given code attribute.
     */
    private void ensureCodeAttrInfoEditor()
    {
        if (codeAttrInfoEditor == null)
        {
            codeAttrInfoEditor = new CodeAttrInfoEditor(codeLength);
        }
    }
}
