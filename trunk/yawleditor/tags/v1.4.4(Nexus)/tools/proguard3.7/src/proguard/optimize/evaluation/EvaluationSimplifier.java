/* $Id: EvaluationSimplifier.java,v 1.4.2.15 2006/10/07 12:08:25 eric Exp $
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
package proguard.optimize.evaluation;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.editor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import proguard.optimize.*;
import proguard.optimize.evaluation.value.*;

/**
 * This MemberInfoVisitor performs partial evaluation on the program methods
 * that it visits.
 *
 * @author Eric Lafortune
 */
public class EvaluationSimplifier
implements   MemberInfoVisitor,
             AttrInfoVisitor,
             InstructionVisitor
{
    //*
    private static final boolean DEBUG_RESULTS  = false;
    private static final boolean DEBUG_ANALYSIS = false;
    private static final boolean DEBUG          = false;
    /*/
    private static boolean DEBUG_RESULTS  = true;
    private static boolean DEBUG_ANALYSIS = true;
    private static boolean DEBUG          = true;
    //*/

    private static final int INITIAL_CODE_LENGTH = 1024;
    private static final int INITIAL_VALUE_COUNT = 32;

    private InstructionVisitor extraPushInstructionVisitor;
    private InstructionVisitor extraBranchInstructionVisitor;
    private InstructionVisitor extraDeletedInstructionVisitor;

    private PartialEvaluator             partialEvaluator             = new PartialEvaluator();
    private SideEffectInstructionChecker sideEffectInstructionChecker = new SideEffectInstructionChecker(true);
    private ClassFileCleaner             classFileCleaner             = new ClassFileCleaner();
    private CodeAttrInfoEditor           codeAttrInfoEditor           = new CodeAttrInfoEditor(INITIAL_CODE_LENGTH);


    private Variables parameters   = new TracedVariables(INITIAL_VALUE_COUNT);
    private boolean[] isNecessary  = new boolean[INITIAL_CODE_LENGTH];
    private boolean[] isSimplified = new boolean[INITIAL_CODE_LENGTH];


    /**
     * Creates a new EvaluationSimplifier.
     */
    public EvaluationSimplifier()
    {
        this(null, null, null);
    }


    /**
     * Creates a new EvaluationSimplifier.
     * @param extraPushInstructionVisitor    an optional extra visitor for all
     *                                       simplified push instructions.
     * @param extraBranchInstructionVisitor  an optional extra visitor for all
     *                                       simplified branch instructions.
     * @param extraDeletedInstructionVisitor an optional extra visitor for all
     *                                       deleted instructions.
     */
    public EvaluationSimplifier(InstructionVisitor extraPushInstructionVisitor,
                                InstructionVisitor extraBranchInstructionVisitor,
                                InstructionVisitor extraDeletedInstructionVisitor)
    {
        this.extraPushInstructionVisitor    = extraPushInstructionVisitor;
        this.extraBranchInstructionVisitor  = extraBranchInstructionVisitor;
        this.extraDeletedInstructionVisitor = extraDeletedInstructionVisitor;
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo) {}


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
//        DEBUG = DEBUG_ANALYSIS = DEBUG_RESULTS =
//            programClassFile.getName().equals("abc/Def") &&
//            programMethodInfo.getName(programClassFile).equals("abc");

        try
        {
            // Process the code.
            programMethodInfo.attributesAccept(programClassFile, this);
        }
        catch (RuntimeException ex)
        {
            // TODO: Remove this when the partial evaluator has stabilized.
            System.err.println("Unexpected error while optimizing after partial evaluation:");
            System.err.println("  ClassFile   = ["+programClassFile.getName()+"]");
            System.err.println("  Method      = ["+programMethodInfo.getName(programClassFile)+programMethodInfo.getDescriptor(programClassFile)+"]");
            System.err.println("Not optimizing this method");

            if (DEBUG)
            {
                throw ex;
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
        if (DEBUG_RESULTS)
        {
            System.out.println();
            System.out.println("Class "+ClassUtil.externalClassName(classFile.getName()));
            System.out.println("Method "+ClassUtil.externalFullMethodDescription(classFile.getName(),
                                                                                 0,
                                                                                 methodInfo.getName(classFile),
                                                                                 methodInfo.getDescriptor(classFile)));
            System.out.println("  Params:"+parameters);
        }

        // Initialize the method parameters.
        initializeParameters(classFile, methodInfo, codeAttrInfo);

        // Initialize the necessary array.
        initializeNecessary(codeAttrInfo);

        // Evaluate the method.
        Value returnValue = partialEvaluator.evaluate(classFile,
                                                      methodInfo,
                                                      codeAttrInfo,
                                                      parameters);

        // Clean up the visitor information in the exceptions right away.
        codeAttrInfo.exceptionsAccept(classFile, methodInfo, classFileCleaner);

        int codeLength = codeAttrInfo.u4codeLength;

        // Reset the code changes.
        codeAttrInfoEditor.reset(codeLength);

        // Replace any instructions that can be simplified.
        if (DEBUG_ANALYSIS) System.out.println("Instruction simplification:");

        codeAttrInfo.instructionsAccept(classFile, methodInfo, this);

        // Mark all essential instructions that have been encountered as used.
        if (DEBUG_ANALYSIS) System.out.println("Usage initialization: ");


        // The invocation of the "super" or "this" <init> method inside a
        // constructor is always necessary, even if it is assumed to have no
        // side effects.
        boolean markSuperOrThis =
            methodInfo.getName(classFile).equals(ClassConstants.INTERNAL_METHOD_NAME_INIT);

        int aload0Offset = 0;

        int offset = 0;
        do
        {
            if (partialEvaluator.isTraced(offset))
            {
                Instruction instruction = InstructionFactory.create(codeAttrInfo.code,
                                                                    offset);

                // Remember the most recent aload0 instruction index.
                if (instruction.opcode == InstructionConstants.OP_ALOAD_0)
                {
                    aload0Offset = offset;
                }

                // Mark that the instruction is necessary if it is the first
                // invocation of the "super" or "this" <init> method
                // inside a constructor.
                else if (markSuperOrThis &&
                         instruction.opcode == InstructionConstants.OP_INVOKESPECIAL &&
                         partialEvaluator.stackProducerOffsets(offset).contains(aload0Offset))
                {
                    markSuperOrThis = false;

                    if (DEBUG_ANALYSIS) System.out.print(offset+"(<init>),");
                    isNecessary[offset] = true;
                }

                // Mark that the instruction is necessary if it is an infinite loop.
                else if (instruction.opcode == InstructionConstants.OP_GOTO &&
                         partialEvaluator.branchTargets(offset).instructionOffsetValue().instructionOffset(0) == offset)
                {
                    if (DEBUG_ANALYSIS) System.out.print(offset+"(infinite loop),");
                    isNecessary[offset] = true;
                }

                // Mark that the instruction is necessary if it has side effects.
                else if (sideEffectInstructionChecker.hasSideEffects(classFile,
                                                                     methodInfo,
                                                                     codeAttrInfo,
                                                                     offset,
                                                                     instruction))
                {
                    if (DEBUG_ANALYSIS) System.out.print(offset +",");
                    isNecessary[offset] = true;
                }
            }

            offset++;
        }
        while (offset < codeLength);
        if (DEBUG_ANALYSIS) System.out.println();


        // Mark all other instructions on which the essential instructions
        // depend. Instead of doing this recursively, we loop across all
        // instructions, starting at the last one, and restarting at any
        // higher, previously unmarked instruction that is being marked.
        if (DEBUG_ANALYSIS) System.out.println("Usage marking:");

        int lowestNecessaryOffset = codeLength;
        offset = codeLength - 1;
        do
        {
            int nextOffset = offset - 1;

            // Update the lowest index of all marked instructions higher up.
            if (isNecessary[offset])
            {
                lowestNecessaryOffset = offset;
            }

            // Check if this instruction is a branch origin from a branch that
            // straddles some marked code.
            nextOffset = markStraddlingBranches(offset,
                                                partialEvaluator.branchTargets(offset),
                                                true,
                                                lowestNecessaryOffset,
                                                nextOffset);

            // Mark the producers on which this instruction depends.
            if (isNecessary[offset] &&
                !isSimplified[offset])
            {
                nextOffset = markProducers(offset,
                                           nextOffset);
            }

            // Update the lowest index of all marked instructions higher up.
            if (isNecessary[offset])
            {
                lowestNecessaryOffset = offset;
            }

            // Check if this instruction is a branch target from a branch that
            // straddles some marked code.
            nextOffset = markStraddlingBranches(offset,
                                                partialEvaluator.branchOrigins(offset),
                                                false,
                                                lowestNecessaryOffset,
                                                nextOffset);

            if (DEBUG_ANALYSIS)
            {
                if (nextOffset >= offset)
                {
                    System.out.println();
                }
            }

            // Update the lowest index of all marked instructions higher up.
            if (isNecessary[offset])
            {
                lowestNecessaryOffset = offset;
            }

            // Update the index of the instruction to be investigated next.
            offset = nextOffset;
        }
        while (offset >= 0);
        if (DEBUG_ANALYSIS) System.out.println();


        // Insert pop instructions before unmarked popping instructions,
        // if required to keep the stack consistent.
        if (DEBUG_ANALYSIS) System.out.println("Unmarked pop fixing:");

        // Also figure out the offset of the last dup/swap instruction.
        int highestDupOffset = -1;

        offset = codeLength - 1;
        do
        {
            if (partialEvaluator.isTraced(offset) &&
                !isNecessary[offset] &&
                !isSimplified[offset])
            {
                Instruction instruction = InstructionFactory.create(codeAttrInfo.code,
                                                                    offset);

                // Make sure any non-dup/swap instructions are always consistent
                // at this offset.
                if (!isDupOrSwap(instruction))
                {
                    // Make sure any popping instructions are always
                    // consistent after this offset.
                    fixPopInstruction(classFile,
                                      codeAttrInfo,
                                      offset,
                                      instruction);
                }
                else if (highestDupOffset < 0)
                {
                    // Remember the offset of the last dup/swap instruction.
                    highestDupOffset = offset;
                }
            }

            offset--;
        }
        while (offset >= 0);
        if (DEBUG_ANALYSIS) System.out.println();


        // Insert dup instructions where necessary, to keep the stack consistent.
        boolean updated;
        do
        {
            if (DEBUG_ANALYSIS) System.out.println("Dup marking:");

            // Repeat going over all instructions, as long as dup/swap
            // instructions are updated.
            updated = false;

            offset = highestDupOffset;
            while (offset >= 0)
            {
                if (partialEvaluator.isTraced(offset))
                {
                    Instruction instruction = InstructionFactory.create(codeAttrInfo.code,
                                                                        offset);

                    // Make sure any dup/swap instructions are always consistent
                    // at this offset.
                    if (isDupOrSwap(instruction))
                    {
                        updated |= fixDupInstruction(classFile,
                                                     codeAttrInfo,
                                                     offset,
                                                     instruction);
                    }
                }

                offset--;
            }
        }
        while (updated);
        if (DEBUG_ANALYSIS) System.out.println();


        // Insert pop instructions after marked pushing instructions,
        // if required to keep the stack consistent.
        if (DEBUG_ANALYSIS) System.out.println("Marked push fixing:");

        offset = codeLength - 1;
        do
        {
            if (//partialEvaluator.isTraced(offset) &&
                isNecessary[offset] &&
                !isSimplified[offset])
            {
                Instruction instruction = InstructionFactory.create(codeAttrInfo.code,
                                                                    offset);

                // Make sure any non-dup/swap instructions are always consistent
                // at this offset.
                if (!isDupOrSwap(instruction))
                {
                    // Make sure any pushing instructions are always
                    // consistent after this offset.
                    fixPushInstruction(classFile,
                                       codeAttrInfo,
                                       offset,
                                       instruction);
                }
            }

            offset--;
        }
        while (offset >= 0);
        if (DEBUG_ANALYSIS) System.out.println();


        // Mark branches straddling just inserted push/pop instructions.
        if (DEBUG_ANALYSIS) System.out.println("Final straddling branch marking:");

        lowestNecessaryOffset = codeLength;
        offset = codeLength - 1;
        do
        {
            int nextOffset = offset - 1;

            // Update the lowest index of all marked instructions higher up.
            if (isNecessary[offset])
            {
                lowestNecessaryOffset = offset;
            }

            // Check if this instruction is a branch origin from a branch that
            // straddles some marked code.
            nextOffset = markAndSimplifyStraddlingBranches(offset,
                                                           partialEvaluator.branchTargets(offset),
                                                           lowestNecessaryOffset,
                                                           nextOffset);

            // Update the lowest index of all marked instructions higher up.
            if (isNecessary[offset])
            {
                lowestNecessaryOffset = offset;
            }

            // Check if this instruction is a branch target from a branch that
            // straddles some marked code.
            nextOffset = markAndSimplifyStraddlingBranches(partialEvaluator.branchOrigins(offset),
                                                           offset,
                                                           lowestNecessaryOffset,
                                                           nextOffset);

            if (DEBUG_ANALYSIS)
            {
                if (nextOffset >= offset)
                {
                    System.out.println();
                }
            }

            // Update the lowest index of all marked instructions higher up.
            if (isNecessary[offset])
            {
                lowestNecessaryOffset = offset;
            }

            // Update the index of the instruction to be investigated next.
            offset = nextOffset;
        }
        while (offset >= 0);
        if (DEBUG_ANALYSIS) System.out.println();


        // Mark variable initializations, even if they aren't strictly necessary.
        // The virtual machine's verification step is not smart enough to see
        // this, and may complain otherwise.
        if (DEBUG_ANALYSIS) System.out.println("Initialization marking: ");

        offset = 0;
        do
        {
            // Is it an initialization that hasn't been marked yet, and whose
            // corresponding variable is used for storage?
            int variableIndex = partialEvaluator.initializedVariable(offset);
            if (variableIndex != PartialEvaluator.NONE &&
                !isNecessary[offset] &&
                isVariableReferenced(codeAttrInfo, variableIndex))
            {
                if (DEBUG_ANALYSIS) System.out.println(offset +",");

                // Figure out what kind of initialization value has to be stored.
                int pushComputationalType = partialEvaluator.variableValueAfter(offset, variableIndex).computationalType();
                increaseStackSize(offset, pushComputationalType, false);
            }

            offset++;
        }
        while (offset < codeLength);
        if (DEBUG_ANALYSIS) System.out.println();


        if (DEBUG_RESULTS)
        {
            System.out.println("Simplification results:");
            offset = 0;
            do
            {
                Instruction instruction = InstructionFactory.create(codeAttrInfo.code,
                                                                    offset);
                System.out.println((isNecessary[offset] ? " + " : " - ")+instruction.toString(offset));

                if (partialEvaluator.isTraced(offset))
                {
                    InstructionOffsetValue varProducerOffsets = partialEvaluator.varProducerOffsets(offset);
                    if (varProducerOffsets.instructionOffsetCount() > 0)
                    {
                        System.out.println("     has overall been using information from instructions setting vars: "+varProducerOffsets);
                    }

                    InstructionOffsetValue stackProducerOffsets = partialEvaluator.stackProducerOffsets(offset);
                    if (stackProducerOffsets.instructionOffsetCount() > 0)
                    {
                        System.out.println("     has overall been using information from instructions setting stack: "+stackProducerOffsets);
                    }

                    InstructionOffsetValue unusedProducerOffsets = partialEvaluator.unusedProducerOffsets(offset);
                    if (unusedProducerOffsets.instructionOffsetCount() > 0)
                    {
                        System.out.println("     no longer needs information from instructions setting stack: "+unusedProducerOffsets);
                    }

                    InstructionOffsetValue branchTargets = partialEvaluator.branchTargets(offset);
                    if (branchTargets != null)
                    {
                        System.out.println("     has overall been branching to "+branchTargets);
                    }

                    Instruction preInsertion = codeAttrInfoEditor.preInsertions[offset];
                    if (preInsertion != null)
                    {
                        System.out.println("     is preceded by: "+preInsertion);
                    }

                    Instruction replacement = codeAttrInfoEditor.replacements[offset];
                    if (replacement != null)
                    {
                        System.out.println("     is replaced by: "+replacement);
                    }

                    Instruction postInsertion = codeAttrInfoEditor.postInsertions[offset];
                    if (postInsertion != null)
                    {
                        System.out.println("     is followed by: "+postInsertion);
                    }

                    //System.out.println("     Vars:  "+vars[offset]);
                    //System.out.println("     Stack: "+stacks[offset]);
                }

                offset += instruction.length(offset);
            }
            while (offset < codeLength);
        }

        // Delete all instructions that are not used.
        offset = 0;
        do
        {
            Instruction instruction = InstructionFactory.create(codeAttrInfo.code,
                                                                offset);
            if (!isNecessary[offset])
            {
                codeAttrInfoEditor.deleteInstruction(offset);

                codeAttrInfoEditor.insertBeforeInstruction(offset, null);
                codeAttrInfoEditor.replaceInstruction(offset, null);
                codeAttrInfoEditor.insertAfterInstruction(offset, null);

                // Visit the instruction, if required.
                if (extraDeletedInstructionVisitor != null)
                {
                    instruction.accept(classFile, methodInfo, codeAttrInfo, offset, extraDeletedInstructionVisitor);
                }
            }

            offset += instruction.length(offset);
        }
        while (offset < codeLength);

        // Apply all accumulated changes to the code.
        codeAttrInfoEditor.visitCodeAttrInfo(classFile, methodInfo, codeAttrInfo);
    }


    /**
     * Marks the producers at the given offsets.
     * @param consumerOffset the offset of the consumer.
     * @param nextOffset     the offset of the instruction to be investigated next.
     * @return the updated offset of the instruction to be investigated next.
     *         It is always greater than or equal the original offset, because
     *         instructions are investigated starting at the highest index.
     */
    private int markProducers(int consumerOffset,
                              int nextOffset)
    {
        if (DEBUG_ANALYSIS) System.out.print(consumerOffset);

        // Mark all instructions whose variable values are used.
        nextOffset = markProducers(partialEvaluator.varProducerOffsets(consumerOffset), nextOffset);

        // Mark all instructions whose stack values are used.
        nextOffset = markProducers(partialEvaluator.stackProducerOffsets(consumerOffset), nextOffset);

        if (DEBUG_ANALYSIS) System.out.print(",");

        return nextOffset;
    }


    /**
     * Marks the instructions at the given offsets.
     * @param producerOffsets the offsets of the producers to be marked.
     * @param nextOffset      the offset of the instruction to be investigated
     *                        next.
     * @return the updated offset of the instruction to be investigated next.
     *         It is always greater than or equal the original offset, because
     *         instructions are investigated starting at the highest index.
     */
    private int markProducers(InstructionOffsetValue producerOffsets,
                              int                    nextOffset)
    {
        if (producerOffsets != null)
        {
            int offsetCount = producerOffsets.instructionOffsetCount();
            for (int offsetIndex = 0; offsetIndex < offsetCount; offsetIndex++)
            {
                // Has the other instruction been marked yet?
                int offset = producerOffsets.instructionOffset(offsetIndex);
                if (offset > PartialEvaluator.AT_METHOD_ENTRY &&
                    !isNecessary[offset])
                {
                    if (DEBUG_ANALYSIS) System.out.print("["+offset +"]");

                    // Mark it.
                    isNecessary[offset] = true;

                    // Restart at this instruction if it has a higher offset.
                    if (nextOffset < offset)
                    {
                        if (DEBUG_ANALYSIS) System.out.print("!");

                        nextOffset = offset;
                    }
                }
            }
        }

        return nextOffset;
    }


    /**
     * Marks the branch instructions of straddling branches, if they straddle
     * some code that has been marked.
     * @param index                 the offset of the branch origin or branch target.
     * @param branchOffsets         the offsets of the straddling branch targets
     *                              or branch origins.
     * @param isPointingToTargets   <code>true</code> if the above offsets are
     *                              branch targets, <code>false</code> if they
     *                              are branch origins.
     * @param lowestNecessaryOffset the lowest offset of all instructions marked
     *                              so far.
     * @param nextOffset            the offset of the instruction to be investigated
     *                              next.
     * @return the updated offset of the instruction to be investigated next.
     *         It is always greater than or equal the original offset, because
     *         instructions are investigated starting at the highest index.
     */
    private int markStraddlingBranches(int                    index,
                                       InstructionOffsetValue branchOffsets,
                                       boolean                isPointingToTargets,
                                       int                    lowestNecessaryOffset,
                                       int                    nextOffset)
    {
        if (branchOffsets != null)
        {
            // Loop over all branch origins.
            int branchCount = branchOffsets.instructionOffsetCount();
            for (int branchIndex = 0; branchIndex < branchCount; branchIndex++)
            {
                // Is the branch straddling any necessary instructions?
                int branch = branchOffsets.instructionOffset(branchIndex);

                // Is the offset pointing to a branch origin or to a branch target?
                nextOffset = isPointingToTargets ?
                    markStraddlingBranch(index, branch, lowestNecessaryOffset, nextOffset) :
                    markStraddlingBranch(branch, index, lowestNecessaryOffset, nextOffset);
            }
        }

        return nextOffset;
    }


    /**
     * Marks the given branch instruction, if it straddles some code that has
     * been marked.
     * @param branchOrigin          the branch origin.
     * @param branchTarget          the branch target.
     * @param lowestNecessaryOffset the lowest offset of all instructions marked
     *                              so far.
     * @param nextOffset            the offset of the instruction to be investigated
     *                              next.
     * @return the updated offset of the instruction to be investigated next.
     *         It is always greater than or equal the original offset, because
     *         instructions are investigated starting at the highest index.
     */
    private int markStraddlingBranch(int branchOrigin,
                                     int branchTarget,
                                     int lowestNecessaryOffset,
                                     int nextOffset)
    {
        // Has the branch origin been marked yet, and is it straddling the
        // lowest necessary instruction?
        if (!isNecessary[branchOrigin] &&
            isStraddlingBranch(branchOrigin, branchTarget, lowestNecessaryOffset))
        {
            if (DEBUG_ANALYSIS) System.out.print("["+branchOrigin+"->"+branchTarget+"]");

            // Mark the branch origin.
            isNecessary[branchOrigin] = true;

            // Restart at the branch origin if it has a higher offset.
            if (nextOffset < branchOrigin)
            {
                if (DEBUG_ANALYSIS) System.out.print("!");

                nextOffset = branchOrigin;
            }
        }

        return nextOffset;
    }


    /**
     * Marks and simplifies the branch instructions of straddling branches,
     * if they straddle some code that has been marked.
     * @param branchOrigin          the branch origin.
     * @param branchTargets         the branch targets.
     * @param lowestNecessaryOffset the lowest offset of all instructions marked
     *                              so far.
     * @param nextOffset            the offset of the instruction to be investigated
     *                              next.
     * @return the updated offset of the instruction to be investigated next.
     *         It is always greater than or equal the original offset, because
     *         instructions are investigated starting at the highest index.
     */
    private int markAndSimplifyStraddlingBranches(int                    branchOrigin,
                                                  InstructionOffsetValue branchTargets,
                                                  int                    lowestNecessaryOffset,
                                                  int                    nextOffset)
    {
        if (branchTargets != null &&
            !isNecessary[branchOrigin])
        {
            // Loop over all branch targets.
            int branchCount = branchTargets.instructionOffsetCount();
            if (branchCount > 0)
            {
                for (int branchIndex = 0; branchIndex < branchCount; branchIndex++)
                {
                    // Is the branch straddling any necessary instructions?
                    int branchTarget = branchTargets.instructionOffset(branchIndex);

                    if (!isStraddlingBranch(branchOrigin,
                                            branchTarget,
                                            lowestNecessaryOffset))
                    {
                        return nextOffset;
                    }
                }

                nextOffset = markAndSimplifyStraddlingBranch(branchOrigin,
                                                             branchTargets.instructionOffset(0),
                                                             lowestNecessaryOffset,
                                                             nextOffset);
            }
        }

        return nextOffset;
    }


    /**
     * Marks and simplifies the branch instructions of straddling branches,
     * if they straddle some code that has been marked.
     * @param branchOrigins         the branch origins.
     * @param branchTarget          the branch target.
     * @param lowestNecessaryOffset the lowest offset of all instructions marked
     *                              so far.
     * @param nextOffset            the offset of the instruction to be investigated
     *                              next.
     * @return the updated offset of the instruction to be investigated next.
     *         It is always greater than or equal the original offset, because
     *         instructions are investigated starting at the highest index.
     */
    private int markAndSimplifyStraddlingBranches(InstructionOffsetValue branchOrigins,
                                                  int                    branchTarget,
                                                  int                    lowestNecessaryOffset,
                                                  int                    nextOffset)
    {
        if (branchOrigins != null)
        {
            // Loop over all branch origins.
            int branchCount = branchOrigins.instructionOffsetCount();
            for (int branchIndex = 0; branchIndex < branchCount; branchIndex++)
            {
                // Is the branch straddling any necessary instructions?
                int branchOrigin = branchOrigins.instructionOffset(branchIndex);

                nextOffset = markAndSimplifyStraddlingBranch(branchOrigin,
                                                             branchTarget,
                                                             lowestNecessaryOffset,
                                                             nextOffset);
            }
        }

        return nextOffset;
    }


    /**
     * Marks and simplifies the given branch instruction, if it straddles some
     * code that has been marked.
     * @param branchOrigin          the branch origin.
     * @param branchTarget          the branch target.
     * @param lowestNecessaryOffset the lowest offset of all instructions marked
     *                              so far.
     * @param nextOffset            the offset of the instruction to be investigated
     *                              next.
     * @return the updated offset of the instruction to be investigated next.
     *         It is always greater than or equal the original offset, because
     *         instructions are investigated starting at the highest index.
     */
    private int markAndSimplifyStraddlingBranch(int branchOrigin,
                                                int branchTarget,
                                                int lowestNecessaryOffset,
                                                int nextOffset)
    {
        // Has the branch origin been marked yet, and is it straddling the
        // lowest necessary instruction?
        if (!isNecessary[branchOrigin] &&
            isStraddlingBranch(branchOrigin, branchTarget, lowestNecessaryOffset))
        {
            if (DEBUG_ANALYSIS) System.out.print("["+branchOrigin+"->"+branchTarget+"]");

            // Mark the branch origin.
            isNecessary[branchOrigin] = true;

            // Replace the branch instruction by a simple branch instrucion.
            Instruction replacementInstruction =
                new BranchInstruction(InstructionConstants.OP_GOTO_W,
                                      branchTarget - branchOrigin).shrink();

            codeAttrInfoEditor.replaceInstruction(branchOrigin,
                                                  replacementInstruction);

            // Restart at the branch origin if it has a higher offset.
            if (nextOffset < branchOrigin)
            {
                if (DEBUG_ANALYSIS) System.out.print("!");

                nextOffset = branchOrigin;
            }
        }

        return nextOffset;
    }


    /**
     * Returns whether the given branch straddling some code that has been marked.
     * @param branchOrigin          the branch origin.
     * @param branchTarget          the branch target.
     * @param lowestNecessaryOffset the lowest offset of all instructions marked
     *                              so far.
     */
    private boolean isStraddlingBranch(int branchOrigin,
                                       int branchTarget,
                                       int lowestNecessaryOffset)
    {
        return branchOrigin <= lowestNecessaryOffset ^
               branchTarget <= lowestNecessaryOffset;
    }


    /**
     * Marks the specified instruction if it is a required dup/swap instruction,
     * replacing it by an appropriate variant if necessary.
     * @param classFile    the class that is being checked.
     * @param codeAttrInfo the code that is being checked.
     * @param dupOffset    the offset of the dup/swap instruction.
     * @param instruction  the dup/swap instruction.
     * @return whether the instruction is updated.
     */
    private boolean fixDupInstruction(ClassFile    classFile,
                                      CodeAttrInfo codeAttrInfo,
                                      int          dupOffset,
                                      Instruction  instruction)
    {
        byte    oldOpcode = instruction.opcode;
        byte    newOpcode = 0;
        boolean present   = false;

        // Simplify the popping instruction if possible.
        switch (oldOpcode)
        {
            case InstructionConstants.OP_DUP:
            {
                boolean stackEntryPresent0 = isStackEntryNecessaryAfter(dupOffset, 0, false);
                boolean stackEntryPresent1 = isStackEntryNecessaryAfter(dupOffset, 1, false);

                // Should either the original element or the copy be present?
                if (stackEntryPresent0 ||
                    stackEntryPresent1)
                {
                    present = true;

                    // Should both the original element and the copy be present?
                    if (stackEntryPresent0 &&
                        stackEntryPresent1)
                    {
                        newOpcode = InstructionConstants.OP_DUP;
                    }
                }
                break;
            }
            case InstructionConstants.OP_DUP_X1:
            {
                boolean stackEntryPresent0 = isStackEntryNecessaryAfter(dupOffset, 0, false);
                boolean stackEntryPresent1 = isStackEntryNecessaryAfter(dupOffset, 1, false);
                boolean stackEntryPresent2 = isStackEntryNecessaryAfter(dupOffset, 2, false);

                // Should either the original element or the copy be present?
                if (stackEntryPresent0 ||
                    stackEntryPresent2)
                {
                    present = true;

                    // Should the copy be present?
                    if (stackEntryPresent2)
                    {
                        // Compute the number of elements to be skipped.
                        int skipCount = stackEntryPresent1 ? 1 : 0;

                        // Should the original element be present?
                        if (stackEntryPresent0)
                        {
                            // Copy the original element.
                            newOpcode = (byte)(InstructionConstants.OP_DUP + skipCount);
                        }
                        else if (skipCount == 1)
                        {
                            // Move the original element.
                            newOpcode = InstructionConstants.OP_SWAP;
                        }
                    }
                }
                break;
            }
            case InstructionConstants.OP_DUP_X2:
            {
                boolean stackEntryPresent0 = isStackEntryNecessaryAfter(dupOffset, 0, false);
                boolean stackEntryPresent1 = isStackEntryNecessaryAfter(dupOffset, 1, false);
                boolean stackEntryPresent2 = isStackEntryNecessaryAfter(dupOffset, 2, false);
                boolean stackEntryPresent3 = isStackEntryNecessaryAfter(dupOffset, 3, false);

                // Should either the original element or the copy be present?
                if (stackEntryPresent0 ||
                    stackEntryPresent3)
                {
                    present = true;

                    // Should the copy be present?
                    if (stackEntryPresent3)
                    {
                        int skipCount = (stackEntryPresent1 ? 1 : 0) +
                                        (stackEntryPresent2 ? 1 : 0);

                        // Should the original element be present?
                        if (stackEntryPresent0)
                        {
                            // Copy the original element.
                            newOpcode = (byte)(InstructionConstants.OP_DUP + skipCount);
                        }
                        else if (skipCount == 1)
                        {
                            // Move the original element.
                            newOpcode = InstructionConstants.OP_SWAP;
                        }
                        else if (skipCount == 2)
                        {
                            // We can't easily move the original element.
                            throw new IllegalArgumentException("Can't handle dup_x2 instruction moving original element across two elements at ["+dupOffset +"]");
                        }
                    }
                }
                break;
            }
            case InstructionConstants.OP_DUP2:
            {
                boolean stackEntriesPresent01 = isStackEntriesNecessaryAfter(dupOffset, 0, 1, false);
                boolean stackEntriesPresent23 = isStackEntriesNecessaryAfter(dupOffset, 2, 3, false);

                // Should either the original element or the copy be present?
                if (stackEntriesPresent01 ||
                    stackEntriesPresent23)
                {
                    present = true;

                    // Should both the original element and the copy be present?
                    if (stackEntriesPresent01 &&
                        stackEntriesPresent23)
                    {
                        newOpcode = InstructionConstants.OP_DUP2;
                    }
                }
                break;
            }
            case InstructionConstants.OP_DUP2_X1:
            {
                boolean stackEntriesPresent01 = isStackEntriesNecessaryAfter(dupOffset, 0, 1, false);
                boolean stackEntryPresent2    = isStackEntryNecessaryAfter(dupOffset, 2, false);
                boolean stackEntriesPresent34 = isStackEntriesNecessaryAfter(dupOffset, 3, 4, false);

                // Should either the original element or the copy be present?
                if (stackEntriesPresent01 ||
                    stackEntriesPresent34)
                {
                    present = true;

                    // Should the copy be present?
                    if (stackEntriesPresent34)
                    {
                        int skipCount = stackEntryPresent2 ? 1 : 0;

                        // Should the original element be present?
                        if (stackEntriesPresent01)
                        {
                            // Copy the original element.
                            newOpcode = (byte)(InstructionConstants.OP_DUP2 + skipCount);
                        }
                        else if (skipCount > 0)
                        {
                            // We can't easily move the original element.
                            throw new IllegalArgumentException("Can't handle dup2_x1 instruction moving original element across "+skipCount+" elements at ["+dupOffset +"]");
                        }
                    }
                }
                break;
            }
            case InstructionConstants.OP_DUP2_X2:
            {
                boolean stackEntriesPresent01 = isStackEntriesNecessaryAfter(dupOffset, 0, 1, false);
                boolean stackEntryPresent2    = isStackEntryNecessaryAfter(dupOffset, 2, false);
                boolean stackEntryPresent3    = isStackEntryNecessaryAfter(dupOffset, 3, false);
                boolean stackEntriesPresent45 = isStackEntriesNecessaryAfter(dupOffset, 4, 5, false);

                // Should either the original element or the copy be present?
                if (stackEntriesPresent01 ||
                    stackEntriesPresent45)
                {
                    present = true;

                    // Should the copy be present?
                    if (stackEntriesPresent45)
                    {
                        int skipCount = (stackEntryPresent2 ? 1 : 0) +
                                        (stackEntryPresent3 ? 1 : 0);

                        // Should the original element be present?
                        if (stackEntriesPresent01)
                        {
                            // Copy the original element.
                            newOpcode = (byte)(InstructionConstants.OP_DUP2 + skipCount);
                        }
                        else if (skipCount > 0)
                        {
                            // We can't easily move the original element.
                            throw new IllegalArgumentException("Can't handle dup2_x2 instruction moving original element across "+skipCount+" elements at ["+dupOffset +"]");
                        }
                    }
                }
                break;
            }
            case InstructionConstants.OP_SWAP:
            {
                boolean stackEntryPresent0 = isStackEntryNecessaryAfter(dupOffset, 0, false);
                boolean stackEntryPresent1 = isStackEntryNecessaryAfter(dupOffset, 1, false);

                // Will either element be present?
                if (stackEntryPresent0 ||
                    stackEntryPresent1)
                {
                    present = true;

                    // Will both elements be present?
                    if (stackEntryPresent0 &&
                        stackEntryPresent1)
                    {
                        newOpcode = InstructionConstants.OP_SWAP;
                    }
                }
                break;
            }
        }

        boolean updated = false;

        // Actually replace the instruction with the new opcode, if any.
        if (present)
        {
            // If this is the first pass, note that the instruction is updated.
            if (!isNecessary[dupOffset])
            {
                updated = true;

                // Mark that the instruction is necessary.
                isNecessary[dupOffset]  = true;
            }

            if      (newOpcode == 0)
            {
                // Delete the instruction.
                codeAttrInfoEditor.deleteInstruction(dupOffset);

                if (DEBUG_ANALYSIS) System.out.println("  Marking but deleting instruction "+instruction.toString(dupOffset));
            }
            else if (newOpcode == oldOpcode)
            {
                // Leave the instruction unchanged.
                codeAttrInfoEditor.undeleteInstruction(dupOffset);

                if (DEBUG_ANALYSIS) System.out.println("  Marking unchanged instruction "+instruction.toString(dupOffset));
            }
            else
            {
                // Replace the instruction.
                Instruction replacementInstruction = new SimpleInstruction(newOpcode);
                codeAttrInfoEditor.replaceInstruction(dupOffset,
                                                      replacementInstruction);

                if (DEBUG_ANALYSIS) System.out.println("  Replacing instruction "+instruction.toString(dupOffset)+" by "+replacementInstruction.toString());
            }
        }

        return updated;
    }


    /**
     * Pops the stack after the given necessary instruction, if it pushes an
     * entry that is not used at all.
     * @param classFile      the class that is being checked.
     * @param codeAttrInfo   the code that is being checked.
     * @param producerOffset the offset of the instruction.
     * @param instruction    the instruction.
     */
    private void fixPushInstruction(ClassFile    classFile,
                                    CodeAttrInfo codeAttrInfo,
                                    int          producerOffset,
                                    Instruction  instruction)
    {
        int pushCount = instruction.stackPushCount(classFile);
        if (pushCount > 0)
        {
            boolean stackEntryPresent0 = isStackEntryNecessaryAfter(producerOffset, 0, false);

            if (!stackEntryPresent0)
            {
                if (instruction.opcode != InstructionConstants.OP_JSR &&
                    instruction.opcode != InstructionConstants.OP_JSR_W)
                {
                    // Make sure the pushed value is popped again,
                    // right after this instruction.
                    decreaseStackSize(producerOffset, pushCount, false, false);

                    if (DEBUG_ANALYSIS) System.out.println("  Popping unused value right after "+instruction.toString(producerOffset));
                }
            }
        }
    }


    /**
     * Pops the stack before the given unnecessary instruction, if the stack
     * contains an entry that it would have popped.
     * @param classFile      the class that is being checked.
     * @param codeAttrInfo   the code that is being checked.
     * @param consumerOffset the offset of the consumer instruction.
     * @param instruction    the instruction.
     */
    private void fixPopInstruction(ClassFile    classFile,
                                   CodeAttrInfo codeAttrInfo,
                                   int          consumerOffset,
                                   Instruction  instruction)
    {
        int popCount = instruction.stackPopCount(classFile);
        if (popCount > 0)
        {
            if (partialEvaluator.stackProducerOffsets(consumerOffset).contains(PartialEvaluator.AT_CATCH_ENTRY) ||
                isStackEntryNecessaryBefore(consumerOffset, 0, false) &&
                !isStackEntryNecessaryBefore(consumerOffset, 0, true))
            {
                // Is the consumer a simple pop or pop2 instruction?
                byte popOpcode = instruction.opcode;
                if (popOpcode == InstructionConstants.OP_POP ||
                    popOpcode == InstructionConstants.OP_POP2)
                {
                    if (DEBUG_ANALYSIS) System.out.println("  Popping value again at "+instruction.toString(consumerOffset));

                    // Simply mark the pop or pop2 instruction.
                    isNecessary[consumerOffset] = true;
                }
                else
                {
                    if (DEBUG_ANALYSIS) System.out.println("  Popping value instead of "+instruction.toString(consumerOffset));

                    // Make sure the pushed value is popped again,
                    // right before this instruction.
                    decreaseStackSize(consumerOffset, popCount, true, true);
                }
            }
        }
    }


    /**
     * Puts the required push instruction before the given index. The
     * instruction is marked as necessary.
     * @param offset            the offset of the instruction.
     * @param computationalType the computational type on the stack, for
     *                          push instructions.
     * @param delete            specifies whether the instruction should be
     *                          deleted.
     */
    private void increaseStackSize(int     offset,
                                   int     computationalType,
                                   boolean delete)
    {
        // Mark this instruction.
        isNecessary[offset] = true;

        // Create a simple push instrucion.
        byte replacementOpcode =
            computationalType == Value.TYPE_INTEGER   ? InstructionConstants.OP_ICONST_0    :
            computationalType == Value.TYPE_LONG      ? InstructionConstants.OP_LCONST_0    :
            computationalType == Value.TYPE_FLOAT     ? InstructionConstants.OP_FCONST_0    :
            computationalType == Value.TYPE_DOUBLE    ? InstructionConstants.OP_DCONST_0    :
            computationalType == Value.TYPE_REFERENCE ? InstructionConstants.OP_ACONST_NULL :
                                                        InstructionConstants.OP_NOP;

        Instruction replacementInstruction = new SimpleInstruction(replacementOpcode);

        // Insert the pop or push instruction.
        codeAttrInfoEditor.insertBeforeInstruction(offset,
                                                   replacementInstruction);

        // Delete the original instruction if necessary.
        if (delete)
        {
            codeAttrInfoEditor.deleteInstruction(offset);
        }
    }


    /**
     * Puts the required pop instruction at the given index. The
     * instruction is marked as necessary.
     * @param offset   the offset of the instruction.
     * @param popCount the required reduction of the stack size.
     * @param before   specifies whether the pop instruction should be inserted
     *                 before or after the present instruction.
     * @param delete   specifies whether the instruction should be deleted.
     */
    private void decreaseStackSize(int     offset,
                                   int     popCount,
                                   boolean before,
                                   boolean delete)
    {
        // Mark this instruction.
        isNecessary[offset] = true;

        boolean after = !before;

        int remainingPopCount = popCount;

        if (delete)
        {
            // Replace the original instruction.
            int count = remainingPopCount == 1 ? 1 : 2;

            // Create a simple pop instrucion.
            byte replacementOpcode = count == 1 ?
                InstructionConstants.OP_POP :
                InstructionConstants.OP_POP2;

            Instruction replacementInstruction = new SimpleInstruction(replacementOpcode);

            // Insert the pop instruction.
            codeAttrInfoEditor.replaceInstruction(offset,
                                                  replacementInstruction);

            remainingPopCount -= count;

            // We may insert other pop instructions before and after this one.
            before = true;
            after  = true;
        }

        if (before && remainingPopCount > 0)
        {
            // Insert before the original instruction.
            int count = remainingPopCount == 1 ? 1 : 2;

            // Create a simple pop instrucion.
            byte replacementOpcode = count == 1 ?
                InstructionConstants.OP_POP :
                InstructionConstants.OP_POP2;

            Instruction replacementInstruction = new SimpleInstruction(replacementOpcode);

            // Insert the pop instruction.
            codeAttrInfoEditor.insertBeforeInstruction(offset,
                                                       replacementInstruction);

            remainingPopCount -= count;
        }

        if (after && remainingPopCount > 0)
        {
            // Insert after the original instruction.
            int count = remainingPopCount == 1 ? 1 : 2;

            // Create a simple pop instrucion.
            byte replacementOpcode = count == 1 ?
                InstructionConstants.OP_POP :
                InstructionConstants.OP_POP2;

            Instruction replacementInstruction = new SimpleInstruction(replacementOpcode);

            // Insert the pop instruction.
            codeAttrInfoEditor.insertAfterInstruction(offset,
                                                      replacementInstruction);

            remainingPopCount -= count;
        }

        if (remainingPopCount > 0)
        {
            throw new IllegalArgumentException("Unsupported stack size reduction ["+popCount+"] at ["+offset+"]");
        }
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction)
    {
        if (partialEvaluator.isTraced(offset))
        {
            switch (simpleInstruction.opcode)
            {
                case InstructionConstants.OP_IALOAD:
                case InstructionConstants.OP_BALOAD:
                case InstructionConstants.OP_CALOAD:
                case InstructionConstants.OP_SALOAD:
                case InstructionConstants.OP_IADD:
                case InstructionConstants.OP_ISUB:
                case InstructionConstants.OP_IMUL:
                case InstructionConstants.OP_IDIV:
                case InstructionConstants.OP_IREM:
                case InstructionConstants.OP_INEG:
                case InstructionConstants.OP_ISHL:
                case InstructionConstants.OP_ISHR:
                case InstructionConstants.OP_IUSHR:
                case InstructionConstants.OP_IAND:
                case InstructionConstants.OP_IOR:
                case InstructionConstants.OP_IXOR:
                case InstructionConstants.OP_L2I:
                case InstructionConstants.OP_F2I:
                case InstructionConstants.OP_D2I:
                case InstructionConstants.OP_I2B:
                case InstructionConstants.OP_I2C:
                case InstructionConstants.OP_I2S:
                    replaceIntegerPushInstruction(offset, simpleInstruction);
                    break;

                case InstructionConstants.OP_LALOAD:
                case InstructionConstants.OP_LADD:
                case InstructionConstants.OP_LSUB:
                case InstructionConstants.OP_LMUL:
                case InstructionConstants.OP_LDIV:
                case InstructionConstants.OP_LREM:
                case InstructionConstants.OP_LNEG:
                case InstructionConstants.OP_LSHL:
                case InstructionConstants.OP_LSHR:
                case InstructionConstants.OP_LUSHR:
                case InstructionConstants.OP_LAND:
                case InstructionConstants.OP_LOR:
                case InstructionConstants.OP_LXOR:
                case InstructionConstants.OP_I2L:
                case InstructionConstants.OP_F2L:
                case InstructionConstants.OP_D2L:
                    replaceLongPushInstruction(offset, simpleInstruction);
                    break;

                case InstructionConstants.OP_FALOAD:
                case InstructionConstants.OP_FADD:
                case InstructionConstants.OP_FSUB:
                case InstructionConstants.OP_FMUL:
                case InstructionConstants.OP_FDIV:
                case InstructionConstants.OP_FREM:
                case InstructionConstants.OP_FNEG:
                case InstructionConstants.OP_I2F:
                case InstructionConstants.OP_L2F:
                case InstructionConstants.OP_D2F:
                    replaceFloatPushInstruction(offset, simpleInstruction);
                    break;

                case InstructionConstants.OP_DALOAD:
                case InstructionConstants.OP_DADD:
                case InstructionConstants.OP_DSUB:
                case InstructionConstants.OP_DMUL:
                case InstructionConstants.OP_DDIV:
                case InstructionConstants.OP_DREM:
                case InstructionConstants.OP_DNEG:
                case InstructionConstants.OP_I2D:
                case InstructionConstants.OP_L2D:
                case InstructionConstants.OP_F2D:
                    replaceDoublePushInstruction(offset, simpleInstruction);
                    break;

                case InstructionConstants.OP_AALOAD:
                    replaceReferencePushInstruction(offset, simpleInstruction);
                    break;
            }
        }
    }


    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction)
    {
        if (partialEvaluator.isTraced(offset))
        {
            switch (variableInstruction.opcode)
            {
                case InstructionConstants.OP_ILOAD:
                case InstructionConstants.OP_ILOAD_0:
                case InstructionConstants.OP_ILOAD_1:
                case InstructionConstants.OP_ILOAD_2:
                case InstructionConstants.OP_ILOAD_3:
                    replaceIntegerPushInstruction(offset, variableInstruction);
                    break;

                case InstructionConstants.OP_LLOAD:
                case InstructionConstants.OP_LLOAD_0:
                case InstructionConstants.OP_LLOAD_1:
                case InstructionConstants.OP_LLOAD_2:
                case InstructionConstants.OP_LLOAD_3:
                    replaceLongPushInstruction(offset, variableInstruction);
                    break;

                case InstructionConstants.OP_FLOAD:
                case InstructionConstants.OP_FLOAD_0:
                case InstructionConstants.OP_FLOAD_1:
                case InstructionConstants.OP_FLOAD_2:
                case InstructionConstants.OP_FLOAD_3:
                    replaceFloatPushInstruction(offset, variableInstruction);
                    break;

                case InstructionConstants.OP_DLOAD:
                case InstructionConstants.OP_DLOAD_0:
                case InstructionConstants.OP_DLOAD_1:
                case InstructionConstants.OP_DLOAD_2:
                case InstructionConstants.OP_DLOAD_3:
                    replaceDoublePushInstruction(offset, variableInstruction);
                    break;

                case InstructionConstants.OP_ALOAD:
                case InstructionConstants.OP_ALOAD_0:
                case InstructionConstants.OP_ALOAD_1:
                case InstructionConstants.OP_ALOAD_2:
                case InstructionConstants.OP_ALOAD_3:
                    replaceReferencePushInstruction(offset, variableInstruction);
                    break;

            }
        }
    }


    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction)
    {
        // Constant pool instructions are not simplified at this point.
    }


    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction)
    {
        switch (branchInstruction.opcode)
        {
            case InstructionConstants.OP_GOTO:
            case InstructionConstants.OP_GOTO_W:
                // Don't replace unconditional branches.
                break;

            case InstructionConstants.OP_JSR:
            case InstructionConstants.OP_JSR_W:
                replaceJsrInstruction(offset, branchInstruction);
                break;

            default:
                replaceBranchInstruction(offset, branchInstruction);
                break;
        }
    }


    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction)
    {
        replaceBranchInstruction(offset, tableSwitchInstruction);
    }


    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        replaceBranchInstruction(offset, lookUpSwitchInstruction);
    }


    // Small utility methods.

    /**
     * Replaces the given integer instruction by a simpler push instruction,
     * if possible.
     */
    private void replaceIntegerPushInstruction(int offset, Instruction instruction)
    {
        Value pushedValue = partialEvaluator.stackTopValueAfter(offset, 0);
        if (pushedValue.isSpecific())
        {
            int value = pushedValue.integerValue().value();
            if (value << 16 >> 16 == value)
            {
                replacePushInstruction(offset,
                                       InstructionConstants.OP_SIPUSH,
                                       value);
            }
        }
    }


    /**
     * Replaces the given long instruction by a simpler push instruction,
     * if possible.
     */
    private void replaceLongPushInstruction(int offset, Instruction instruction)
    {
        Value pushedValue = partialEvaluator.stackTopValueAfter(offset, 0);
        if (pushedValue.isSpecific())
        {
            long value = pushedValue.longValue().value();
            if (value == 0L ||
                value == 1L)
            {
                replacePushInstruction(offset,
                                       (byte)(InstructionConstants.OP_LCONST_0 + value),
                                       0);
            }
        }
    }


    /**
     * Replaces the given float instruction by a simpler push instruction,
     * if possible.
     */
    private void replaceFloatPushInstruction(int offset, Instruction instruction)
    {
        Value pushedValue = partialEvaluator.stackTopValueAfter(offset, 0);
        if (pushedValue.isSpecific())
        {
            float value = pushedValue.floatValue().value();
            if (value == 0f ||
                value == 1f ||
                value == 2f)
            {
                replacePushInstruction(offset,
                                       (byte)(InstructionConstants.OP_FCONST_0 + value),
                                       0);
            }
        }
    }


    /**
     * Replaces the given double instruction by a simpler push instruction,
     * if possible.
     */
    private void replaceDoublePushInstruction(int offset, Instruction instruction)
    {
        Value pushedValue = partialEvaluator.stackTopValueAfter(offset, 0);
        if (pushedValue.isSpecific())
        {
            double value = pushedValue.doubleValue().value();
            if (value == 0.0 ||
                value == 1.0)
            {
                replacePushInstruction(offset,
                                       (byte)(InstructionConstants.OP_DCONST_0 + value),
                                       0);
            }
        }
    }


    /**
     * Replaces the given reference instruction by a simpler push instruction,
     * if possible.
     */
    private void replaceReferencePushInstruction(int offset, Instruction instruction)
    {
        Value pushedValue = partialEvaluator.stackTopValueAfter(offset, 0);
        if (pushedValue.isSpecific())
        {
            ReferenceValue value = pushedValue.referenceValue();
            if (value.isNull() == Value.ALWAYS)
            {
                replacePushInstruction(offset,
                                       InstructionConstants.OP_ACONST_NULL,
                                       0);
            }
        }
    }


    /**
     * Replaces the instruction at a given offset by a given push instruction.
     */
    private void replacePushInstruction(int offset, byte opcode, int value)
    {
        // Remember the replacement instruction.
        Instruction replacementInstruction =
             new SimpleInstruction(opcode, value).shrink();

        if (DEBUG_ANALYSIS) System.out.println("  Replacing instruction at ["+offset+"] by "+replacementInstruction.toString());

        codeAttrInfoEditor.replaceInstruction(offset, replacementInstruction);

        // Mark that the instruction has been simplified.
        isSimplified[offset] = true;

        // Visit the instruction, if required.
        if (extraPushInstructionVisitor != null)
        {
            // Note: we're not passing the right arguments for now, knowing that
            // they aren't used anyway.
            extraPushInstructionVisitor.visitSimpleInstruction(null, null, null, offset, null);
        }
    }


    /**
     * Replaces the given 'jsr' instruction by a simpler branch instruction,
     * if possible.
     */
    private void replaceJsrInstruction(int offset, BranchInstruction branchInstruction)
    {
        if (partialEvaluator.isTraced(offset))
        {
            // Is the subroutine ever returning?
            if (!isReturningFromSubroutine(offset + branchInstruction.branchOffset))
            {
                // All 'jsr' instructions to this subroutine can be replaced
                // by unconditional branch instructions.
                replaceBranchInstruction(offset, branchInstruction);
            }
            else if (!partialEvaluator.isTraced(offset + branchInstruction.length(offset)))
            {
                // We have to make sure the instruction after this 'jsr'
                // instruction is valid, even if it is never reached.
                insertInfiniteLoop(offset + branchInstruction.length(offset));
            }
        }
    }


    /**
     * Deletes the given branch instruction, or replaces it by a simpler branch
     * instruction, if possible.
     */
    private void replaceBranchInstruction(int offset, Instruction instruction)
    {
        if (partialEvaluator.isTraced(offset))
        {
            InstructionOffsetValue branchTargets = partialEvaluator.branchTargets(offset);

            // Is there exactly one branch target (not from a goto or jsr)?
            if (branchTargets != null &&
                branchTargets.instructionOffsetCount() == 1)
            {
                // Is it branching to the next instruction?
                int branchOffset = branchTargets.instructionOffset(0) - offset;
                if (branchOffset == instruction.length(offset))
                {
                    if (DEBUG_ANALYSIS) System.out.println("  Deleting zero branch instruction at ["+offset+"]");

                    // Delete the branch instruction.
                    codeAttrInfoEditor.deleteInstruction(offset);
                }
                else
                {
                    // Replace the branch instruction by a simple branch instruction.
                    Instruction replacementInstruction =
                        new BranchInstruction(InstructionConstants.OP_GOTO_W,
                                              branchOffset).shrink();

                    if (DEBUG_ANALYSIS) System.out.println("  Replacing branch instruction at ["+offset+"] by "+replacementInstruction.toString());

                    codeAttrInfoEditor.replaceInstruction(offset,
                                                          replacementInstruction);

                    // Mark that the instruction has been simplified.
                    isSimplified[offset] = true;

                    // Visit the instruction, if required.
                    if (extraBranchInstructionVisitor != null)
                    {
                        // Note: we're not passing the right arguments for now,
                        // knowing that they aren't used anyway.
                        extraBranchInstructionVisitor.visitBranchInstruction(null, null, null, offset, null);
                    }
                }
            }
        }
    }


    /**
     * Puts an infinite loop at the given offset.
     */
    private void insertInfiniteLoop(int offset)
    {
        // Replace the branch instruction by a simple branch instruction.
        Instruction replacementInstruction =
            new BranchInstruction(InstructionConstants.OP_GOTO, 0);

        if (DEBUG_ANALYSIS) System.out.println("  Inserting infinite loop at unreachable instruction at ["+offset+"]");

        codeAttrInfoEditor.replaceInstruction(offset,
                                              replacementInstruction);

        // Mark that the instruction has been simplified.
        isNecessary[offset]  = true;
        isSimplified[offset] = true;
    }


    // Small utility methods.

    /**
     * Initializes the parameter data structure.
     */
    private void initializeParameters(ClassFile    classFile,
                                      MethodInfo   methodInfo,
                                      CodeAttrInfo codeAttrInfo)
    {
        // Initialize the parameters.
        boolean isStatic =
            (methodInfo.getAccessFlags() & ClassConstants.INTERNAL_ACC_STATIC) != 0;

        // Count the number of parameters, taking into account their Categories.
        String parameterDescriptor = methodInfo.getDescriptor(classFile);
        int parameterSize = (isStatic ? 0 : 1) +
            ClassUtil.internalMethodParameterSize(parameterDescriptor);

        // Reuse the existing parameters object, ensuring the right size.
        parameters.reset(parameterSize);

        // Go over the parameters again.
        InternalTypeEnumeration internalTypeEnumeration =
            new InternalTypeEnumeration(parameterDescriptor);

        int parameterIndex = 0;

        // Put the caller's reference in parameter 0.
        if (!isStatic)
        {
            parameters.store(parameterIndex++, ReferenceValueFactory.create(false));
        }

        while (internalTypeEnumeration.hasMoreTypes())
        {
            String type = internalTypeEnumeration.nextType();

            // Get a generic corresponding value.
            Value value = ValueFactory.create(type);

            // Store the value in the parameter.
            parameters.store(parameterIndex, value);

            // Increment the index according to the Category of the value.
            parameterIndex += value.isCategory2() ? 2 : 1;
        }
    }


    /**
     * Initializes the necessary data structure.
     */
    private void initializeNecessary(CodeAttrInfo codeAttrInfo)
    {
        int codeLength = codeAttrInfo.u4codeLength;

        // Create new arrays for storing information at each instruction offset.
        if (isNecessary.length < codeLength)
        {
            isNecessary  = new boolean[codeLength];
            isSimplified = new boolean[codeLength];
        }
        else
        {
            for (int index = 0; index < codeLength; index++)
            {
                isNecessary[index]  = false;
                isSimplified[index] = false;
            }
        }
    }


    /**
     * Returns whether the given instruction is a dup or swap instruction
     * (dup, dup_x1, dup_x2, dup2, dup2_x1, dup2_x1, swap).
     */
    private boolean isDupOrSwap(Instruction instruction)
    {
        return instruction.opcode >= InstructionConstants.OP_DUP &&
               instruction.opcode <= InstructionConstants.OP_SWAP;
    }


    /**
     * Returns whether the subroutine starting at the given offset is ever
     * returning.
     */
    private boolean isReturningFromSubroutine(int subroutineOffset)
    {
        int subroutineEnd = partialEvaluator.subroutineEnd(subroutineOffset);
        return partialEvaluator.isSubroutineEnd(subroutineEnd) &&
               partialEvaluator.isTraced(subroutineEnd);
    }


    /**
     * Returns whether the given variable is ever referenced (stored) by an
     * instruction that is marked as necessary.
     */
    private boolean isVariableReferenced(CodeAttrInfo codeAttrInfo,
                                         int          variableIndex)
    {
        int codeLength = codeAttrInfo.u4codeLength;

        for (int offset = 0; offset < codeLength; offset++)
        {
            if (isNecessary[offset]   &&
                !isSimplified[offset] &&
                partialEvaluator.variableValueBefore(offset, variableIndex) != null &&
                isVariableNecessaryBefore(offset, variableIndex, false))
            {
                return true;
            }
        }

        return false;
    }


    /**
     * Returns whether the given variable is present before execution of the
     * instruction at the given offset.
     */
    private boolean isVariableNecessaryBefore(int     instructionOffset,
                                              int     variableIndex,
                                              boolean all)
    {
        return isNecessary(partialEvaluator.variableProducerOffsetsBefore(instructionOffset, variableIndex),
                           true,
                           all);
    }


    /**
     * Returns whether the given stack entry is present after execution of the
     * instruction at the given offset.
     */
    private boolean isStackEntriesNecessaryAfter(int instructionOffset, int stackIndex1, int stackIndex2, boolean all)
    {
        boolean present1 = isStackEntryNecessaryAfter(instructionOffset, stackIndex1, all);
        boolean present2 = isStackEntryNecessaryAfter(instructionOffset, stackIndex2, all);

//        if (present1 ^ present2)
//        {
//            throw new IllegalArgumentException("Can't handle partial use of dup2 instructions at ["+instructionOffset+"]");
//        }

        return present1 || present2;
    }


    /**
     * Returns whether the given stack entry is present before execution of the
     * instruction at the given offset.
     */
    private boolean isStackEntryNecessaryBefore(int     instructionOffset,
                                                int     stackIndex,
                                                boolean all)
    {
        return isNecessary(partialEvaluator.stackTopConsumerOffsetsBefore(instructionOffset, stackIndex),
                           false,
                           all);
    }


    /**
     * Returns whether the given stack entry is present after execution of the
     * instruction at the given offset.
     */
    private boolean isStackEntryNecessaryAfter(int     instructionOffset,
                                               int     stackIndex,
                                               boolean all)
    {
        return isNecessary(partialEvaluator.stackTopConsumerOffsetsAfter(instructionOffset, stackIndex),
                           false,
                           all);
    }


    /**
     * Returns whether any or all of the instructions at the given offsets are
     * marked as necessary.
     */
    private boolean isNecessary(InstructionOffsetValue offsets,
                                boolean                producer,
                                boolean                all)
    {
        int offsetCount = offsets.instructionOffsetCount();

        for (int offsetIndex = 0; offsetIndex < offsetCount; offsetIndex++)
        {
            int offset = offsets.instructionOffset(offsetIndex);

            if (all ^ (isNecessary[offset] && (producer || !isSimplified[offset])))
            {
                return !all;
            }
        }

        return all;
    }
}
