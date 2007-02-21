/* $Id: CodeAttrInfoEditor.java,v 1.14.2.1 2006/01/16 22:57:55 eric Exp $
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

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.instruction.*;
import proguard.classfile.visitor.*;

/**
 * This AttrInfoVisitor accumulates specified changes to code, and then applies
 * these accumulated changes to the code attributes that it visits.
 *
 * @author Eric Lafortune
 */
public class CodeAttrInfoEditor
  implements AttrInfoVisitor,
             InstructionVisitor,
             ExceptionInfoVisitor,
             LineNumberInfoVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor
{
    private int              codeLength;
    private boolean          modified;
    private boolean          inserted;

    /*private*/public Instruction[]    preInsertions;
    /*private*/public Instruction[]    replacements;
    /*private*/public Instruction[]    postInsertions;
    private boolean[]        deleted;

    private int[]            instructionOffsetMap;

    private StackSizeUpdater stackSizeUpdater;


    /**
     * Creates a new CodeAttrInfoEditor.
     * @param codeLength an estimate of the maximum length of all the code that
     *                   will be edited.
     */
    public CodeAttrInfoEditor(int codeLength)
    {
        this.codeLength = codeLength;

        preInsertions    = new Instruction[codeLength];
        replacements     = new Instruction[codeLength];
        postInsertions   = new Instruction[codeLength];
        deleted          = new boolean[codeLength];

        stackSizeUpdater = new StackSizeUpdater(codeLength);
    }


    /**
     * Resets the accumulated code changes.
     * @param codeLength the length of the code that will be edited next.
     */
    public void reset(int codeLength)
    {
        this.codeLength = codeLength;

        // Try to reuse the previous arrays.
        if (preInsertions.length < codeLength)
        {
            preInsertions  = new Instruction[codeLength];
            replacements   = new Instruction[codeLength];
            postInsertions = new Instruction[codeLength];
            deleted        = new boolean[codeLength];
        }
        else
        {
            for (int index = 0; index < codeLength; index++)
            {
                preInsertions[index]  = null;
                replacements[index]   = null;
                postInsertions[index] = null;
                deleted[index]        = false;
            }
        }

        modified = false;
        inserted = false;
    }


    /**
     * Remembers to place the given instruction right before the instruction
     * at the given offset.
     * @param instructionOffset the offset of the instruction.
     * @param instruction       the new instruction.
     */
    public void insertBeforeInstruction(int instructionOffset, Instruction instruction)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }

        preInsertions[instructionOffset] = instruction;

        modified = true;
        inserted = true;
    }


    /**
     * Remembers to replace the instruction at the given offset by the given
     * instruction.
     * @param instructionOffset the offset of the instruction to be replaced.
     * @param instruction       the new instruction.
     */
    public void replaceInstruction(int instructionOffset, Instruction instruction)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }

        replacements[instructionOffset] = instruction;

        modified = true;
    }


    /**
     * Remembers to place the given instruction right after the instruction
     * at the given offset.
     * @param instructionOffset the offset of the instruction.
     * @param instruction       the new instruction.
     */
    public void insertAfterInstruction(int instructionOffset, Instruction instruction)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }

        postInsertions[instructionOffset] = instruction;

        modified = true;
        inserted = true;
    }


    /**
     * Remembers to delete the instruction at the given offset.
     * @param instructionOffset the offset of the instruction to be deleted.
     */
    public void deleteInstruction(int instructionOffset)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }

        deleted[instructionOffset] = true;

        modified = true;
        inserted = true;
    }


    /**
     * Returns whether the instruction at the given offset has been modified
     * in any way.
     */
    public boolean isModified(int instructionOffset)
    {
        return preInsertions[instructionOffset]  != null ||
               replacements[instructionOffset]   != null ||
               postInsertions[instructionOffset] != null ||
               deleted[instructionOffset];
    }


    /**
     * Returns whether any instruction has been modified in any way.
     */
    public boolean isModified()
    {
        return modified;
    }


    // Implementations for AttrInfoVisitor.

    public void visitUnknownAttrInfo(ClassFile classFile, UnknownAttrInfo unknownAttrInfo) {}
    public void visitInnerClassesAttrInfo(ClassFile classFile, InnerClassesAttrInfo innerClassesAttrInfo) {}
    public void visitEnclosingMethodAttrInfo(ClassFile classFile, EnclosingMethodAttrInfo enclosingMethodAttrInfo) {}
    public void visitConstantValueAttrInfo(ClassFile classFile, FieldInfo fieldInfo, ConstantValueAttrInfo constantValueAttrInfo) {}
    public void visitExceptionsAttrInfo(ClassFile classFile, MethodInfo methodInfo, ExceptionsAttrInfo exceptionsAttrInfo) {}
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
        if (!modified)
        {
            return;
        }

        // Check if we can perform a faster simple replacement of instructions.
        if (canPerformSimpleReplacements(codeAttrInfo))
        {
            // Simply overwrite the instructions.
            performSimpleReplacements(codeAttrInfo);
        }
        else
        {
            // Move and remap the instructions.
            codeAttrInfo.u4codeLength =
                moveInstructions(classFile, methodInfo, codeAttrInfo);

            // Remap the exception table.
            codeAttrInfo.exceptionsAccept(classFile, methodInfo, this);

            // Remap  the line number table and the local variable table.
            codeAttrInfo.attributesAccept(classFile, methodInfo, this);

            // Remove exceptions with empty code blocks.
            codeAttrInfo.u2exceptionTableLength =
                 removeEmptyExceptions(codeAttrInfo.exceptionTable,
                                       codeAttrInfo.u2exceptionTableLength);
        }

        // Update the maximum stack size.
        stackSizeUpdater.visitCodeAttrInfo(classFile, methodInfo, codeAttrInfo);
    }


    // Implementations for LineNumberInfoVisitor.

    public void visitLineNumberTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LineNumberTableAttrInfo lineNumberTableAttrInfo)
    {
        // Remap all line number table entries.
        lineNumberTableAttrInfo.lineNumbersAccept(classFile, methodInfo, codeAttrInfo, this);

        // Remove line numbers with empty code blocks.
        lineNumberTableAttrInfo.u2lineNumberTableLength =
           removeEmptyLineNumbers(lineNumberTableAttrInfo.lineNumberTable,
                                  lineNumberTableAttrInfo.u2lineNumberTableLength,
                                  codeAttrInfo.u4codeLength);
    }


    // Implementations for LocalVariableInfoVisitor.

    public void visitLocalVariableTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTableAttrInfo localVariableTableAttrInfo)
    {
        // Remap all local variable table entries.
        localVariableTableAttrInfo.localVariablesAccept(classFile, methodInfo, codeAttrInfo, this);

        // Remove local variables with empty code blocks.
        localVariableTableAttrInfo.u2localVariableTableLength =
            removeEmptyLocalVariables(localVariableTableAttrInfo.localVariableTable,
                                      localVariableTableAttrInfo.u2localVariableTableLength);
    }


    // Implementations for LocalVariableInfoVisitor.

    public void visitLocalVariableTypeTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeTableAttrInfo localVariableTypeTableAttrInfo)
    {
        // Remap all local variable table entries.
        localVariableTypeTableAttrInfo.localVariablesAccept(classFile, methodInfo, codeAttrInfo, this);

        // Remove local variables with empty code blocks.
        localVariableTypeTableAttrInfo.u2localVariableTypeTableLength =
            removeEmptyLocalVariableTypes(localVariableTypeTableAttrInfo.localVariableTypeTable,
                                          localVariableTypeTableAttrInfo.u2localVariableTypeTableLength);
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction) {}
    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction) {}
    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction) {}


    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction)
    {
        // Adjust the branch offset.
        branchInstruction.branchOffset = remapBranchOffset(offset,
                                                           branchInstruction.branchOffset);
    }


    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction)
    {
        // Adjust the default jump offset.
        tableSwitchInstruction.defaultOffset = remapBranchOffset(offset,
                                                                 tableSwitchInstruction.defaultOffset);

        // Adjust the jump offsets.
        remapJumpOffsets(offset,
                         tableSwitchInstruction.jumpOffsets,
                         tableSwitchInstruction.highCase -
                         tableSwitchInstruction.lowCase + 1);
    }


    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        // Adjust the default jump offset.
        lookUpSwitchInstruction.defaultOffset = remapBranchOffset(offset,
                                                                  lookUpSwitchInstruction.defaultOffset);

        // Adjust the jump offsets.
        remapJumpOffsets(offset,
                         lookUpSwitchInstruction.jumpOffsets,
                         lookUpSwitchInstruction.jumpOffsetCount);
    }


    // Implementations for ExceptionInfoVisitor.

    public void visitExceptionInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, ExceptionInfo exceptionInfo)
    {
        // Remap the code offsets. Note that the instruction offset map also has
        // an entry for the first offset after the code, for u2endpc.
        exceptionInfo.u2startpc   = remapInstructionOffset(exceptionInfo.u2startpc);
        exceptionInfo.u2endpc     = remapInstructionOffset(exceptionInfo.u2endpc);
        exceptionInfo.u2handlerpc = remapInstructionOffset(exceptionInfo.u2handlerpc);
    }


    // Implementations for LineNumberInfoVisitor.

    public void visitLineNumberInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LineNumberInfo lineNumberInfo)
    {
        // Remap the code offset.
        lineNumberInfo.u2startpc = remapInstructionOffset(lineNumberInfo.u2startpc);
    }


    // Implementations for LocalVariableInfoVisitor.

    public void visitLocalVariableInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableInfo localVariableInfo)
    {
        // Remap the code offset and length.
        localVariableInfo.u2length  = remapBranchOffset(localVariableInfo.u2startpc,
                                                        localVariableInfo.u2length);
        localVariableInfo.u2startpc = remapInstructionOffset(localVariableInfo.u2startpc);
    }


    // Implementations for LocalVariableTypeInfoVisitor.

    public void visitLocalVariableTypeInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeInfo localVariableTypeInfo)
    {
        // Remap the code offset and length.
        localVariableTypeInfo.u2length  = remapBranchOffset(localVariableTypeInfo.u2startpc,
                                                            localVariableTypeInfo.u2length);
        localVariableTypeInfo.u2startpc = remapInstructionOffset(localVariableTypeInfo.u2startpc);
    }


    // Small utility methods.

    /**
     * Checks if it is possible to modifies the given code without having to
     * update any offsets.
     *
     * @param codeAttrInfo the code to be changed.
     * @return the new code length.
     */
    private boolean canPerformSimpleReplacements(CodeAttrInfo codeAttrInfo)
    {
        if (inserted)
        {
            return false;
        }

        byte[] code       = codeAttrInfo.code;
        int    codeLength = codeAttrInfo.u4codeLength;

        // Go over all replacement instructions.
        for (int offset = 0; offset < codeLength; offset++)
        {
            // Check if the replacement instruction, if any, has a different
            // length than the original instruction.
            Instruction replacementInstruction = replacements[offset];
            if (replacementInstruction != null &&
                replacementInstruction.length(offset) !=
                    InstructionFactory.create(code, offset).length(offset))
            {
                return false;
            }
        }

        return true;
    }


    /**
     * Modifies the given code without updating any offsets.
     *
     * @param codeAttrInfo the code to be changed.
     */
    private void performSimpleReplacements(CodeAttrInfo codeAttrInfo)
    {
        byte[] code       = codeAttrInfo.code;
        int    codeLength = codeAttrInfo.u4codeLength;

        // Go over all replacement instructions.
        for (int offset = 0; offset < codeLength; offset++)
        {
            // Overwrite the original instruction with the replacement
            // instruction if any.
            Instruction replacementInstruction = replacements[offset];
            if (replacementInstruction != null)
            {
                replacementInstruction.write(codeAttrInfo, offset);
            }
        }
    }


    /**
     * Modifies the given code based on the previously specified changes.
     *
     * @param classFile    the class file of the code to be changed.
     * @param methodInfo   the method of the code to be changed.
     * @param codeAttrInfo the code to be changed.
     * @return the new code length.
     */
    private int moveInstructions(ClassFile    classFile,
                                 MethodInfo   methodInfo,
                                 CodeAttrInfo codeAttrInfo)
    {
        byte[] oldCode   = codeAttrInfo.code;
        int    oldLength = codeAttrInfo.u4codeLength;

        // Make sure there is a sufficiently large instruction offset map.
        if (instructionOffsetMap == null ||
            instructionOffsetMap.length < oldLength + 1)
        {
            instructionOffsetMap = new int[oldLength + 1];
        }

        // Fill out the offset map that specifies the new instruction offsets,
        // given their current instruction offsets, by going over the
        // instructions, deleting and inserting instructions as specified.
        int     oldOffset       = 0;
        int     newOffset       = 0;
        boolean lengthIncreased = false;
        do
        {
            // Get the next instruction.
            Instruction instruction = InstructionFactory.create(oldCode, oldOffset);

            // Compute the mapping of the instruction.
            newOffset = mapInstruction(instruction, oldOffset, newOffset);

            oldOffset += instruction.length(oldOffset);

            // Is the new instruction exceeding the available space?
            if (newOffset > oldOffset)
            {
                // Remember to create a new code array later on.
                lengthIncreased = true;
            }
        }
        while (oldOffset < oldLength);

        // Also add an entry for the first offset after the code.
        instructionOffsetMap[oldOffset] = newOffset;

        // Create a new code array if necessary.
        if (lengthIncreased)
        {
            codeAttrInfo.code = new byte[newOffset];
        }

        // Now actually move the instructions based on this map.
        oldOffset = 0;
        do
        {
            // Get the next instruction.
            Instruction instruction = InstructionFactory.create(oldCode, oldOffset);

            // Move the instruction to its new offset.
            moveInstruction(classFile, methodInfo, codeAttrInfo, oldOffset, instruction);

            oldOffset += instruction.length(oldOffset);
        }
        while (oldOffset < oldLength);

        return newOffset;
    }


    /**
     * Fills out the instruction offset map for the given instruction with its
     * new offset.
     * @param instruction the instruction to be moved.
     * @param oldOffset   the instruction's old offset.
     * @param newOffset   the instruction's new offset.
     * @return            the next new offset.
     */
    private int mapInstruction(Instruction instruction,
                               int         oldOffset,
                               int         newOffset)
    {
        instructionOffsetMap[oldOffset] = newOffset;

        // Account for the pre-inserted instruction, if any.
        Instruction preInstruction = preInsertions[oldOffset];
        if (preInstruction != null)
        {
            newOffset += preInstruction.length(newOffset);
        }

        // Account for the replacement instruction, or for the current
        // instruction, if it shouldn't be  deleted.
        Instruction replacementInstruction = replacements[oldOffset];
        if (replacementInstruction != null)
        {
            newOffset += replacementInstruction.length(newOffset);
        }
        else if (!deleted[oldOffset])
        {
            // Note that the instruction's length may change at its new offset,
            // e.g. if it is a switch instruction.
            newOffset += instruction.length(newOffset);
        }

        // Account for the post-inserted instruction, if any.
        Instruction postInstruction = postInsertions[oldOffset];
        if (postInstruction != null)
        {
            newOffset += postInstruction.length(newOffset);
        }

        return newOffset;
    }


    /**
     * Moves the given instruction to its new offset.
     */
    private void moveInstruction(ClassFile    classFile,
                                 MethodInfo   methodInfo,
                                 CodeAttrInfo codeAttrInfo,
                                 int          oldOffset,
                                 Instruction  instruction)
    {
        int newOffset = remapInstructionOffset(oldOffset);

        // Remap and insert the pre-inserted instruction, if any.
        Instruction preInstruction = preInsertions[oldOffset];
        if (preInstruction != null)
        {
            preInstruction.accept(classFile, methodInfo, codeAttrInfo, oldOffset, this);

            preInstruction.write(codeAttrInfo, newOffset);

            newOffset += preInstruction.length(newOffset);
        }

        // Remap and insert the replacment instruction, or the current
        // instruction, if it shouldn't be deleted.
        Instruction replacementInstruction = replacements[oldOffset];
        if (replacementInstruction != null)
        {
            replacementInstruction.accept(classFile, methodInfo, codeAttrInfo, oldOffset, this);

            replacementInstruction.write(codeAttrInfo, newOffset);

            newOffset += replacementInstruction.length(newOffset);
        }
        else if (!deleted[oldOffset])
        {
            instruction.accept(classFile, methodInfo, codeAttrInfo, oldOffset, this);

            instruction.write(codeAttrInfo, newOffset);

            newOffset += instruction.length(newOffset);
        }

        // Remap and insert the post-inserted instruction, if any.
        Instruction postInstruction = postInsertions[oldOffset];
        if (postInstruction != null)
        {
            postInstruction.accept(classFile, methodInfo, codeAttrInfo, oldOffset, this);

            postInstruction.write(codeAttrInfo, newOffset);
        }
    }


    /**
     * Adjusts the given jump offsets for the instruction at the given offset.
     */
    private void remapJumpOffsets(int offset, int[] jumpOffsets, int length)
    {
        for (int index = 0; index < length; index++)
        {
            jumpOffsets[index] = remapBranchOffset(offset, jumpOffsets[index]);
        }
    }


    /**
     * Computes the new branch offset for the instruction at the given offset
     * with the given branch offset.
     */
    private int remapBranchOffset(int offset, int branchOffset)
    {
        return remapInstructionOffset(offset + branchOffset) -
               remapInstructionOffset(offset);
    }


    /**
     * Computes the new instruction offset for the instruction at the given offset.
     */
    private int remapInstructionOffset(int offset)
    {
        if (offset < 0 ||
            offset > codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+offset+"] in code with length ["+codeLength+"]");
        }

        return instructionOffsetMap[offset];
    }


    /**
     * Returns the given list of exceptions, without the ones that have empty
     * code blocks.
     */
    private int removeEmptyExceptions(ExceptionInfo[] exceptionInfos,
                                      int             exceptionInfoCount)
    {
        // Overwrite all empty exceptions.
        int newIndex = 0;
        for (int index = 0; index < exceptionInfoCount; index++)
        {
            ExceptionInfo exceptionInfo = exceptionInfos[index];
            if (exceptionInfo.u2startpc < exceptionInfo.u2endpc)
            {
                exceptionInfos[newIndex++] = exceptionInfo;
            }
        }

        return newIndex;
    }


    /**
     * Returns the given list of line numbers, without the ones that have empty
     * code blocks.
     */
    private int removeEmptyLineNumbers(LineNumberInfo[] lineNumberInfos,
                                       int              lineNumberInfoCount,
                                       int              codeLength)
    {
        // Overwrite all empty localVariables.
        int newIndex = 0;
        for (int index = 0; index < lineNumberInfoCount; index++)
        {
            LineNumberInfo lineNumberInfo = lineNumberInfos[index];
            int startpc = lineNumberInfo.u2startpc;
            if (               startpc < codeLength &&
                (index == 0 || startpc > lineNumberInfos[index-1].u2startpc))
            {
                lineNumberInfos[newIndex++] = lineNumberInfo;
            }
        }

        return newIndex;
    }


    /**
     * Returns the given list of local variables, without the ones that have empty
     * code blocks.
     */
    private int removeEmptyLocalVariables(LocalVariableInfo[] localVariableInfos,
                                          int                 localVariableInfoCount)
    {
        // Overwrite all empty exceptions.
        int newIndex = 0;
        for (int index = 0; index < localVariableInfoCount; index++)
        {
            LocalVariableInfo localVariableInfo = localVariableInfos[index];
            if (localVariableInfo.u2length > 0)
            {
                localVariableInfos[newIndex++] = localVariableInfo;
            }
        }

        return newIndex;
    }


    /**
     * Returns the given list of local variable types, without the ones that
     * have empty code blocks.
     */
    private int removeEmptyLocalVariableTypes(LocalVariableTypeInfo[] localVariableTypeInfos,
                                              int                     localVariableTypeInfoCount)
    {
        // Overwrite all empty exceptions.
        int newIndex = 0;
        for (int index = 0; index < localVariableTypeInfoCount; index++)
        {
            LocalVariableTypeInfo localVariableTypeInfo = localVariableTypeInfos[index];
            if (localVariableTypeInfo.u2length > 0)
            {
                localVariableTypeInfos[newIndex++] = localVariableTypeInfo;
            }
        }

        return newIndex;
    }
}
