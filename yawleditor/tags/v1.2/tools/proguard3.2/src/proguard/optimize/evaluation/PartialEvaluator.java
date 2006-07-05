/* $Id: PartialEvaluator.java,v 1.26 2004/12/11 16:35:23 eric Exp $
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
public class PartialEvaluator
implements   MemberInfoVisitor,
             AttrInfoVisitor,
             ExceptionInfoVisitor,
             InstructionVisitor,
             CpInfoVisitor
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

    private static final int MAXIMUM_EVALUATION_COUNT = 100;


    private InstructionOffsetValue[] varTraceValues     = new InstructionOffsetValue[INITIAL_CODE_LENGTH];
    private InstructionOffsetValue[] stackTraceValues   = new InstructionOffsetValue[INITIAL_CODE_LENGTH];
    private InstructionOffsetValue[] branchOriginValues = new InstructionOffsetValue[INITIAL_CODE_LENGTH];
    private InstructionOffsetValue[] branchTargetValues = new InstructionOffsetValue[INITIAL_CODE_LENGTH];
    private TracedVariables[]        vars               = new TracedVariables[INITIAL_CODE_LENGTH];
    private TracedStack[]            stacks             = new TracedStack[INITIAL_CODE_LENGTH];
    private int[]                    evaluationCounts   = new int[INITIAL_CODE_LENGTH];
    private boolean[]                initialization     = new boolean[INITIAL_CODE_LENGTH];
    private boolean[]                isNecessary        = new boolean[INITIAL_CODE_LENGTH];
    private boolean                  evaluateExceptions;

    private TracedVariables  parameters = new TracedVariables(INITIAL_VALUE_COUNT);
    private TracedVariables  variables  = new TracedVariables(INITIAL_VALUE_COUNT);
    private TracedStack      stack      = new TracedStack(INITIAL_VALUE_COUNT);
    private TracedBranchUnit branchUnit = new TracedBranchUnit();

    private ClassFileCleaner             classFileCleaner             = new ClassFileCleaner();
    private SideEffectInstructionChecker sideEffectInstructionChecker = new SideEffectInstructionChecker(true);
    private CodeAttrInfoEditor           codeAttrInfoEditor           = new CodeAttrInfoEditor(INITIAL_CODE_LENGTH);

    private boolean isInitializer;


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo) {}


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
//        DEBUG = DEBUG_ANALYSIS = DEBUG_RESULTS =
//            programClassFile.getName().equals("abc/Def") &&
//            programMethodInfo.getName(programClassFile).equals("abc");

        // Initialize the parameters.
        boolean isStatic =
            (programMethodInfo.u2accessFlags & ClassConstants.INTERNAL_ACC_STATIC) != 0;

        // Count the number of parameters, taking into account their Categories.
        String parameterDescriptor = programMethodInfo.getDescriptor(programClassFile);
        int count = (isStatic ? 0 : 1) +
                    ClassUtil.internalMethodParameterSize(parameterDescriptor);

        // Reuse the existing parameters object, ensuring the right size.
        parameters.reset(count);

        // Go over the parameters again.
        InternalTypeEnumeration internalTypeEnumeration =
            new InternalTypeEnumeration(parameterDescriptor);

        int index = 0;

        // Clear the store value of each parameter.
        parameters.setStoreValue(InstructionOffsetValueFactory.create());

        // Put the caller's reference in parameter 0.
        if (!isStatic)
        {
            parameters.store(index++, ReferenceValueFactory.create(false));
        }

        while (internalTypeEnumeration.hasMoreTypes())
        {
            String type = internalTypeEnumeration.nextType();

            // Get a generic corresponding value.
            Value value = ValueFactory.create(type);

            // Store the value in the parameter.
            parameters.store(index, value);

            // Increment the index according to the Category of the value.
            index += value.isCategory2() ? 2 : 1;
        }

        // Reset the return value.
        branchUnit.setTraceReturnValue(null);

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

        if (DEBUG)
        {
            Value returnValue = branchUnit.getTraceReturnValue();
            if (returnValue != null)
            {
                System.out.println("Return value for method "+
                                   ClassUtil.externalFullMethodDescription(programClassFile.getName(),
                                                                           0,
                                                                           programMethodInfo.getName(programClassFile),
                                                                           programMethodInfo.getDescriptor(programClassFile))+
                                   " -> ["+returnValue.toString()+"]");
                System.out.println();
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

        int codeLength = codeAttrInfo.u4codeLength;

        // Reset the code changes.
        codeAttrInfoEditor.reset(codeLength);

        if (DEBUG)
        {
            System.out.println("  Max locals = "+codeAttrInfo.u2maxLocals);
            System.out.println("  Max stack  = "+codeAttrInfo.u2maxStack);
        }

        // Create new arrays for storing a stack and a set of variables at each
        // branch target.
        if (isNecessary.length < codeLength)
        {
            varTraceValues     = new InstructionOffsetValue[codeLength];
            stackTraceValues   = new InstructionOffsetValue[codeLength];
            branchOriginValues = new InstructionOffsetValue[codeLength];
            branchTargetValues = new InstructionOffsetValue[codeLength];
            vars               = new TracedVariables[codeLength];
            stacks             = new TracedStack[codeLength];
            evaluationCounts   = new int[codeLength];
            initialization     = new boolean[codeLength];
            isNecessary        = new boolean[codeLength];
        }
        else
        {
            for (int index = 0; index < codeLength; index++)
            {
                varTraceValues[index]     = null;
                stackTraceValues[index]   = null;
                branchOriginValues[index] = null;
                branchTargetValues[index] = null;
                evaluationCounts[index]   = 0;
                initialization[index]     = false;
                isNecessary[index]        = false;

                if (vars[index] != null)
                {
                    vars[index].reset(codeAttrInfo.u2maxLocals);
                }

                if (stacks[index] != null)
                {
                    stacks[index].reset(codeAttrInfo.u2maxStack);
                }
            }
        }

        // Reuse the existing variables and stack objects, ensuring the right size.
        variables.reset(codeAttrInfo.u2maxLocals);
        stack.reset(codeAttrInfo.u2maxStack);

        // Initialize the local variables with the parameters.
        variables.initialize(parameters);

        // Evaluate the instructions, starting at the entry point.
        if (DEBUG) System.out.println("Partial evaluation: ");

        evaluateInstructionBlock(classFile,
                                 methodInfo,
                                 codeAttrInfo,
                                 variables,
                                 stack,
                                 branchUnit,
                                 0);

        // Evaluate the exception catch blocks, until their entry variables
        // have stabilized.
        do
        {
            // Reset the flag to stop evaluating.
            evaluateExceptions = false;

            // Evaluate all relevant exception catch blocks once.
            codeAttrInfo.exceptionsAccept(classFile, methodInfo, this);
        }
        while (evaluateExceptions);

        // Clean up the visitor information in the exceptions right away.
        codeAttrInfo.exceptionsAccept(classFile, methodInfo, classFileCleaner);

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

        int aload0Index = 0;

        int index = 0;
        do
        {
            if (isTraced(index))
            {
                Instruction instruction = InstructionFactory.create(codeAttrInfo.code,
                                                                    index);

                // Remember the most recent aload0 instruction index.
                if (instruction.opcode == InstructionConstants.OP_ALOAD_0)
                {
                    aload0Index = index;
                }

                // Mark the instruction as necessary if it is the first
                // invocation of the "super" or "this" <init> method
                // inside a constructor.
                else if (markSuperOrThis &&
                         instruction.opcode == InstructionConstants.OP_INVOKESPECIAL &&
                         stackTraceValues[index].contains(aload0Index))
                {
                    markSuperOrThis = false;
                    
                    if (DEBUG_ANALYSIS) System.out.print(index+",");
                    isNecessary[index] = true;
                }
                
                // Mark the instruction as necessary if it has side effects.
                else if (sideEffectInstructionChecker.hasSideEffects(classFile,
                                                                     methodInfo,
                                                                     codeAttrInfo,
                                                                     index,
                                                                     instruction))
                {
                    if (DEBUG_ANALYSIS) System.out.print(index+",");
                    isNecessary[index] = true;
                }
            }

            index++;
        }
        while (index < codeLength);
        if (DEBUG_ANALYSIS) System.out.println();


        // Mark all other instructions on which the essential instructions
        // depend. Instead of doing this recursively, we loop across all
        // instructions, starting at the last one, and restarting at any
        // higher, previously unmarked instruction that is being marked.
        if (DEBUG_ANALYSIS) System.out.println("Usage marking:");

        int lowestNecessaryIndex = codeLength;
        index = codeLength - 1;
        do
        {
            int nextIndex = index - 1;

            // Update the lowest index of all marked instructions higher up.
            if (isNecessary[index])
            {
                lowestNecessaryIndex = index;
            }

            // Check if this instruction is a branch origin from a branch that
            // straddles some marked code.
            nextIndex = markStraddlingBranches(index,
                                               branchTargetValues[index],
                                               true,
                                               lowestNecessaryIndex,
                                               nextIndex);

            // Mark the instructions on which this instruction depends.
            nextIndex = markDependencies(index,
                                         nextIndex);

            // Update the lowest index of all marked instructions higher up.
            if (isNecessary[index])
            {
                lowestNecessaryIndex = index;
            }

            // Check if this instruction is a branch target from a branch that
            // straddles some marked code.
            nextIndex = markStraddlingBranches(index,
                                               branchOriginValues[index],
                                               false,
                                               lowestNecessaryIndex,
                                               nextIndex);

            if (DEBUG_ANALYSIS)
            {
                if (nextIndex >= index)
                {
                    System.out.println();
                }
            }

            // Update the lowest index of all marked instructions higher up.
            if (isNecessary[index])
            {
                lowestNecessaryIndex = index;
            }

            // Update the index of the instruction to be investigated next.
            index = nextIndex;
        }
        while (index >= 0);
        if (DEBUG_ANALYSIS) System.out.println();


        // Insert pop instructions where necessary, to keep the stack consistent.
        if (DEBUG_ANALYSIS) System.out.println("Stack consistency marking:");

        index = codeLength - 1;
        do
        {
            if (isTraced(index) &&
                !isNecessary[index])
            {
                // Make sure the stack is always consistent at this offset.
                fixStackConsistency(classFile,
                                    codeAttrInfo,
                                    index);
            }

            index--;
        }
        while (index >= 0);
        if (DEBUG_ANALYSIS) System.out.println();


        // Fix dup/swap instructions where necessary, to keep the stack consistent.
        if (DEBUG_ANALYSIS) System.out.println("Dup/swap fixing:");

        index = 0;
        do
        {
            if (isNecessary[index])
            {
                // Make sure any dup/swap instructions are always consistent at this offset.
                fixDupInstruction(codeAttrInfo,
                                  index);
            }

            index++;
        }
        while (index < codeLength);
        if (DEBUG_ANALYSIS) System.out.println();


        // Mark branches straddling just inserted push/pop instructions.
        if (DEBUG_ANALYSIS) System.out.println("Final straddling branch marking:");

        lowestNecessaryIndex = codeLength;
        index = codeLength - 1;
        do
        {
            int nextIndex = index - 1;

            // Update the lowest index of all marked instructions higher up.
            if (isNecessary[index])
            {
                lowestNecessaryIndex = index;
            }

            // Check if this instruction is a branch origin from a branch that
            // straddles some marked code.
            nextIndex = markAndSimplifyStraddlingBranches(index,
                                                          branchTargetValues[index],
                                                          lowestNecessaryIndex,
                                                          nextIndex);

            // Update the lowest index of all marked instructions higher up.
            if (isNecessary[index])
            {
                lowestNecessaryIndex = index;
            }

            // Check if this instruction is a branch target from a branch that
            // straddles some marked code.
            nextIndex = markAndSimplifyStraddlingBranches(branchOriginValues[index],
                                                          index,
                                                          lowestNecessaryIndex,
                                                          nextIndex);

            if (DEBUG_ANALYSIS)
            {
                if (nextIndex >= index)
                {
                    System.out.println();
                }
            }

            // Update the lowest index of all marked instructions higher up.
            if (isNecessary[index])
            {
                lowestNecessaryIndex = index;
            }

            // Update the index of the instruction to be investigated next.
            index = nextIndex;
        }
        while (index >= 0);
        if (DEBUG_ANALYSIS) System.out.println();


        // Mark variable initializations, even if they aren't strictly necessary.
        // The virtual machine is not smart enough to see this, and may complain
        // otherwise.
        if (DEBUG_ANALYSIS) System.out.println("Initialization marking: ");

        // TODO: Find a better way.
        index = 0;
        do
        {
            // Is it an initialization that hasn't been marked yet?
            if (initialization[index] &&
                !isNecessary[index])
            {
                if (DEBUG_ANALYSIS) System.out.println(index+",");

                // Figure out what kind of initialization value has to be stored.
                int pushInstructionOffset = stackTraceValues[index].instructionOffset(0);
                int pushComputationalType = stacks[pushInstructionOffset].getTop(0).computationalType();
                increaseStackSize(index, pushComputationalType, false);
            }

            index++;
        }
        while (index < codeLength);
        if (DEBUG_ANALYSIS) System.out.println();


        if (DEBUG_RESULTS)
        {
            System.out.println("Results:");
            int offset = 0;
            do
            {
                Instruction instruction = InstructionFactory.create(codeAttrInfo.code,
                                                                    offset);
                System.out.println((isNecessary[offset] ? " + " : " - ")+instruction.toString(offset));
                if (isTraced(offset))
                {
                    if (varTraceValues[offset] != null &&
                        varTraceValues[offset].instructionOffsetCount() > 0)
                    {
                        System.out.println("     has overall been using information from instructions setting vars: "+varTraceValues[offset]);
                    }
                    if (stackTraceValues[offset] != null &&
                        stackTraceValues[offset].instructionOffsetCount() > 0)
                    {
                        System.out.println("     has overall been using information from instructions setting stack: "+stackTraceValues[offset]);
                    }
                    if (branchTargetValues[offset] != null)
                    {
                        System.out.println("     has overall been branching to "+branchTargetValues[offset]);
                    }
                    if (codeAttrInfoEditor.preInsertions[offset] != null)
                    {
                        System.out.println("     is preceded by: "+codeAttrInfoEditor.preInsertions[offset]);
                    }
                    if (codeAttrInfoEditor.postInsertions[offset] != null)
                    {
                        System.out.println("     is followed by: "+codeAttrInfoEditor.preInsertions[offset]);
                    }
                    System.out.println("     Vars:  "+vars[offset]);
                    System.out.println("     Stack: "+stacks[offset]);
                }

                offset += instruction.length(offset);
            }
            while (offset < codeLength);
        }

        // Delete all instructions that are not used.
        int offset = 0;
        do
        {
            Instruction instruction = InstructionFactory.create(codeAttrInfo.code,
                                                                offset);
            if (!isNecessary[offset])
            {
                codeAttrInfoEditor.replaceInstruction(offset, null);
                codeAttrInfoEditor.replaceInstruction2(offset, null);
            }

            offset += instruction.length(offset);
        }
        while (offset < codeLength);

        // Apply all accumulated changes to the code.
        codeAttrInfoEditor.visitCodeAttrInfo(classFile, methodInfo, codeAttrInfo);
    }


    /**
     * Marks the instructions at the given offsets, if the current instruction
     * itself has been marked.
     * @param index     the offset of the current instruction.
     * @param nextIndex the index of the instruction to be investigated next.
     * @return the updated index of the instruction to be investigated next.
     *         It is always greater than or equal, because instructions are
     *         investigated starting at the highest index.
     */
    private int markDependencies(int index,
                                 int nextIndex)
    {
        if (isNecessary[index] &&
            !codeAttrInfoEditor.isModified(index))
        {
            if (DEBUG_ANALYSIS) System.out.print(index);

            // Mark all instructions whose variable values are used.
            nextIndex = markDependencies(varTraceValues[index], nextIndex);

            // Mark all instructions whose stack values are used.
            nextIndex = markDependencies(stackTraceValues[index], nextIndex);

            if (DEBUG_ANALYSIS) System.out.print(",");
        }

        return nextIndex;
    }


    /**
     * Marks the instructions at the given offsets.
     * @param traceOffsetValue the offsets of the instructions to be marked.
     * @param nextIndex        the index of the instruction to be investigated
     *                         next.
     * @return the updated index of the instruction to be investigated next.
     *         It is always greater than or equal, because instructions are
     *         investigated starting at the highest index.
     */
    private int markDependencies(InstructionOffsetValue traceOffsetValue,
                                 int                    nextIndex)
    {
        if (traceOffsetValue != null)
        {
            int traceOffsetCount = traceOffsetValue.instructionOffsetCount();
            for (int traceOffsetIndex = 0; traceOffsetIndex < traceOffsetCount; traceOffsetIndex++)
            {
                // Has the other instruction been marked yet?
                int traceOffset = traceOffsetValue.instructionOffset(traceOffsetIndex);
                if (!isNecessary[traceOffset])
                {
                    if (DEBUG_ANALYSIS) System.out.print("["+traceOffset+"]");

                    // Mark it.
                    isNecessary[traceOffset] = true;

                    // Restart at this instruction if it has a higher offset.
                    if (nextIndex < traceOffset)
                    {
                        if (DEBUG_ANALYSIS) System.out.print("!");

                        nextIndex = traceOffset;
                    }
                }
            }
        }

        return nextIndex;
    }


    /**
     * Marks the branch instructions of straddling branches, if they straddle
     * some code that has been marked.
     * @param index                the offset of the branch origin or branch target.
     * @param branchValue          the offsets of the straddling branch targets
     *                             or branch origins.
     * @param isPointingToTargets  <code>true</code> if the above offsets are
     *                             branch targets, <code>false</code> if they
     *                             are branch origins.
     * @param lowestNecessaryIndex the lowest offset of all instructions marked
     *                             so far.
     * @param nextIndex            the index of the instruction to be investigated
     *                             next.
     * @return the updated index of the instruction to be investigated next.
     *         It is always greater than or equal the original index, because
     *         instructions are investigated starting at the highest index.
     */
    private int markStraddlingBranches(int                    index,
                                       InstructionOffsetValue branchValue,
                                       boolean                isPointingToTargets,
                                       int                    lowestNecessaryIndex,
                                       int                    nextIndex)
    {
        if (branchValue != null)
        {
            // Loop over all branch origins.
            int branchCount = branchValue.instructionOffsetCount();
            for (int branchIndex = 0; branchIndex < branchCount; branchIndex++)
            {
                // Is the branch straddling any necessary instructions?
                int branch = branchValue.instructionOffset(branchIndex);

                // Is the offset pointing to a branch origin or to a branch target?
                nextIndex = isPointingToTargets ?
                    markStraddlingBranch(index, branch, lowestNecessaryIndex, nextIndex) :
                    markStraddlingBranch(branch, index, lowestNecessaryIndex, nextIndex);
            }
        }

        return nextIndex;
    }


    /**
     * Marks the given branch instruction, if it straddles some code that has
     * been marked.
     * @param branchOrigin         the branch origin.
     * @param branchTarget         the branch target.
     * @param lowestNecessaryIndex the lowest offset of all instructions marked
     *                             so far.
     * @param nextIndex            the index of the instruction to be investigated
     *                             next.
     * @return the updated index of the instruction to be investigated next.
     *         It is always greater than or equal the original index, because
     *         instructions are investigated starting at the highest index.
     */
    private int markStraddlingBranch(int branchOrigin,
                                     int branchTarget,
                                     int lowestNecessaryIndex,
                                     int nextIndex)
    {
        // Has the branch origin been marked yet, and is it straddling the
        // lowest necessary instruction?
        if (!isNecessary[branchOrigin] &&
            isStraddlingBranch(branchOrigin, branchTarget, lowestNecessaryIndex))
        {
            if (DEBUG_ANALYSIS) System.out.print("["+branchOrigin+"->"+branchTarget+"]");

            // Mark the branch origin.
            isNecessary[branchOrigin] = true;

            // Restart at the branch origin if it has a higher offset.
            if (nextIndex < branchOrigin)
            {
                if (DEBUG_ANALYSIS) System.out.print("!");

                nextIndex = branchOrigin;
            }
        }

        return nextIndex;
    }


    /**
     * Marks and simplifies the branch instructions of straddling branches,
     * if they straddle some code that has been marked.
     * @param branchOrigin         the branch origin.
     * @param branchTargets        the branch targets.
     * @param lowestNecessaryIndex the lowest offset of all instructions marked
     *                             so far.
     * @param nextIndex            the index of the instruction to be investigated
     *                             next.
     * @return the updated index of the instruction to be investigated next.
     *         It is always greater than or equal the original index, because
     *         instructions are investigated starting at the highest index.
     */
    private int markAndSimplifyStraddlingBranches(int                    branchOrigin,
                                                  InstructionOffsetValue branchTargets,
                                                  int                    lowestNecessaryIndex,
                                                  int                    nextIndex)
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
                                            lowestNecessaryIndex))
                    {
                        return nextIndex;
                    }
                }

                nextIndex = markAndSimplifyStraddlingBranch(branchOrigin,
                                                            branchTargets.instructionOffset(0),
                                                            lowestNecessaryIndex,
                                                            nextIndex);
            }
        }

        return nextIndex;
    }


    /**
     * Marks and simplifies the branch instructions of straddling branches,
     * if they straddle some code that has been marked.
     * @param branchOrigins        the branch origins.
     * @param branchTarget         the branch target.
     * @param lowestNecessaryIndex the lowest offset of all instructions marked
     *                             so far.
     * @param nextIndex            the index of the instruction to be investigated
     *                             next.
     * @return the updated index of the instruction to be investigated next.
     *         It is always greater than or equal the original index, because
     *         instructions are investigated starting at the highest index.
     */
    private int markAndSimplifyStraddlingBranches(InstructionOffsetValue branchOrigins,
                                                  int                    branchTarget,
                                                  int                    lowestNecessaryIndex,
                                                  int                    nextIndex)
    {
        if (branchOrigins != null)
        {
            // Loop over all branch origins.
            int branchCount = branchOrigins.instructionOffsetCount();
            for (int branchIndex = 0; branchIndex < branchCount; branchIndex++)
            {
                // Is the branch straddling any necessary instructions?
                int branchOrigin = branchOrigins.instructionOffset(branchIndex);

                nextIndex = markAndSimplifyStraddlingBranch(branchOrigin,
                                                            branchTarget,
                                                            lowestNecessaryIndex,
                                                            nextIndex);
            }
        }

        return nextIndex;
    }


    /**
     * Marks and simplifies the given branch instruction, if it straddles some
     * code that has been marked.
     * @param branchOrigin         the branch origin.
     * @param branchTarget         the branch target.
     * @param lowestNecessaryIndex the lowest offset of all instructions marked
     *                             so far.
     * @param nextIndex            the index of the instruction to be investigated
     *                             next.
     * @return the updated index of the instruction to be investigated next.
     *         It is always greater than or equal the original index, because
     *         instructions are investigated starting at the highest index.
     */
    private int markAndSimplifyStraddlingBranch(int branchOrigin,
                                                int branchTarget,
                                                int lowestNecessaryIndex,
                                                int nextIndex)
    {
        // Has the branch origin been marked yet, and is it straddling the
        // lowest necessary instruction?
        if (!isNecessary[branchOrigin] &&
            isStraddlingBranch(branchOrigin, branchTarget, lowestNecessaryIndex))
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
            if (nextIndex < branchOrigin)
            {
                if (DEBUG_ANALYSIS) System.out.print("!");

                nextIndex = branchOrigin;
            }
        }

        return nextIndex;
    }


    /**
     * Returns whether the given branch straddling some code that has been marked.
     * @param branchOrigin         the branch origin.
     * @param branchTarget         the branch target.
     * @param lowestNecessaryIndex the lowest offset of all instructions marked
     *                             so far.
     */
    private boolean isStraddlingBranch(int branchOrigin,
                                       int branchTarget,
                                       int lowestNecessaryIndex)
    {
        return branchOrigin <= lowestNecessaryIndex ^
               branchTarget <= lowestNecessaryIndex;
    }


    /**
     * Inserts pop instructions where necessary, in order to make sure the
     * stack is consistent at the given index.
     * @param classFile    the class file that is being checked.
     * @param codeAttrInfo the code that is being checked.
     * @param index        the offset of the dependent instruction.
     */
    private void fixStackConsistency(ClassFile    classFile,
                                     CodeAttrInfo codeAttrInfo,
                                     int          index)
    {
        // Is the unnecessary instruction popping values (but not a dup/swap
        // instruction)?
        Instruction popInstruction = InstructionFactory.create(codeAttrInfo.code,
                                                               index);
        byte popOpcode = popInstruction.opcode;

        int popCount = popInstruction.stackPopCount(classFile);
        if (popCount > 0) // &&
            //popOpcode != InstructionConstants.OP_DUP     &&
            //popOpcode != InstructionConstants.OP_DUP_X1  &&
            //popOpcode != InstructionConstants.OP_DUP_X2  &&
            //popOpcode != InstructionConstants.OP_DUP2    &&
            //popOpcode != InstructionConstants.OP_DUP2_X1 &&
            //popOpcode != InstructionConstants.OP_DUP2_X2 &&
            //popOpcode != InstructionConstants.OP_SWAP)
        {
            // Check the instructions on which it depends.
            InstructionOffsetValue traceOffsetValue = stackTraceValues[index];
            int traceOffsetCount = traceOffsetValue.instructionOffsetCount();

            if (popCount <= 4 &&
                isAllNecessary(traceOffsetValue))
            {
                if (popOpcode == InstructionConstants.OP_POP ||
                    popOpcode == InstructionConstants.OP_POP2)
                {
                    if (DEBUG_ANALYSIS) System.out.println("  Popping value again at "+popInstruction.toString(index)+" (pushed at all "+traceOffsetValue.instructionOffsetCount()+" offsets)");

                    // Simply mark pop and pop2 instructions.
                    isNecessary[index] = true;
                }
                else
                {
                    if (DEBUG_ANALYSIS) System.out.println("  Popping value instead of "+popInstruction.toString(index)+" (pushed at all "+traceOffsetValue.instructionOffsetCount()+" offsets)");

                    // Make sure the pushed value is popped again,
                    // right before this instruction.
                    decreaseStackSize(index, popCount, true, true);
                }
            }
            //else if (popCount == (popInstruction.isCategory2() ? 4 : 2) &&
            //         traceOffsetCount == 2                              &&
            //         isAnyNecessary(traceOffsetValue))
            //{
            //    if (DEBUG_ANALYSIS) System.out.println("  Popping single value instead of "+popInstruction.toString(index)+" (pushed at some of "+traceOffsetValue.instructionOffsetCount()+" offsets)");
            //
            //    // Make sure the single pushed value is popped again,
            //    // right before this instruction.
            //    decreaseStackSize(index, popCount / 2, true, true);
            //}
            else if (isAnyNecessary(traceOffsetValue))
            {
                if (DEBUG_ANALYSIS) System.out.println("  Popping value somewhere before "+index+" (pushed at some of "+traceOffsetValue.instructionOffsetCount()+" offsets):");

                // Go over all stack pushing instructions.
                for (int traceOffsetIndex = 0; traceOffsetIndex < traceOffsetCount; traceOffsetIndex++)
                {
                    // Has the push instruction been marked?
                    int pushInstructionOffset = traceOffsetValue.instructionOffset(traceOffsetIndex);
                    if (isNecessary[pushInstructionOffset])
                    {
                        Instruction pushInstruction = InstructionFactory.create(codeAttrInfo.code,
                                                                                pushInstructionOffset);

                        int lastOffset = lastPopInstructionOffset(pushInstructionOffset,
                                                                  index,
                                                                  pushInstructionOffset);

                        if (DEBUG_ANALYSIS) System.out.println("    Popping value right after "+lastOffset+", due to push at "+pushInstructionOffset);

                        // Make sure the pushed value is popped again,
                        // right after the instruction that pushes it
                        // (or after the dup instruction that still uses it).
                        decreaseStackSize(lastOffset,
                                          pushInstruction.stackPushCount(classFile),
                                          false, false);
                    }
                }
            }
        }
    }


    /**
     * Returns the last offset of the necessary instruction that depends on the
     * stack result of the instruction at the given index.
     * @param startOffset           the start offset in the search.
     * @param endOffset             the end offset in the search.
     * @param pushInstructionOffset the offset of the instruction that pushes
     *                              a result onto the stack.
     * @return the last offset of the necessary instruction that uses the
     *         above result.
     */
    private int lastPopInstructionOffset(int startOffset,
                                         int endOffset,
                                         int pushInstructionOffset)
    {
        int lastOffset = startOffset;

        for (int index = startOffset; index < endOffset; index++)
        {
            if (isNecessary[index] &&
                stackTraceValues[index].contains(pushInstructionOffset))
            {
                lastOffset = index;
            }
        }

        return lastOffset;
    }


    /**
     * Puts the required push instruction before the given index. The
     * instruction is marked as necessary.
     * @param index             the offset of the instruction.
     * @param computationalType the computational type on the stack, for
     *                          push instructions.
     * @param delete            specifies whether the instruction should be
     *                          deleted.
     */
    private void increaseStackSize(int     index,
                                   int     computationalType,
                                   boolean delete)
    {
        // Mark this instruction.
        isNecessary[index] = true;

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
        codeAttrInfoEditor.insertBeforeInstruction(index,
                                                   replacementInstruction);

        // Delete the original instruction if necessary.
        if (delete)
        {
            codeAttrInfoEditor.deleteInstruction(index);
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
        boolean after = !before;

        // Special case: we may replace the instruction by two pop instructions.
        if (delete && popCount > 2)
        {
            before = true;
            after  = true;
        }

        if (popCount < 1 ||
            popCount > 4)
        {
            throw new IllegalArgumentException("Unsupported stack size reduction ["+popCount+"]");
        }

        // Mark this instruction.
        isNecessary[offset] = true;

        if (before)
        {
            // Create a simple pop instrucion.
            byte replacementOpcode = popCount == 1 || popCount == 3 ?
                InstructionConstants.OP_POP :
                InstructionConstants.OP_POP2;

            Instruction replacementInstruction = new SimpleInstruction(replacementOpcode);

            // Insert the pop instruction.
            codeAttrInfoEditor.insertBeforeInstruction(offset,
                                                       replacementInstruction);
        }

        if (after)
        {
            // Create a simple pop instrucion.
            byte replacementOpcode = popCount == 1 ?
                InstructionConstants.OP_POP :
                InstructionConstants.OP_POP2;

            Instruction replacementInstruction = new SimpleInstruction(replacementOpcode);

            if (DEBUG_ANALYSIS) System.out.println("    Pop instruction after ["+offset+"]: "+replacementInstruction);
 
            // Insert the pop instruction.
            codeAttrInfoEditor.insertAfterInstruction(offset,
                                                      replacementInstruction);
        }

        // Delete the original instruction if necessary.
        if (delete)
        {
            codeAttrInfoEditor.deleteInstruction(offset);
        }
    }


    /**
     * Replaces the specified instruction by the proper dup/swap variant,
     * if necessary, depending on the state of the stack.
     * @param codeAttrInfo the code that is being checked.
     * @param offset       the offset of the instruction.
     */
    private void fixDupInstruction(CodeAttrInfo codeAttrInfo,
                                   int          offset)
    {
        byte replacementOpcode = 0;

        // Simplify the popping instruction if possible.
        switch (codeAttrInfo.code[offset])
        {
            case InstructionConstants.OP_DUP_X1:
                if (!isStackEntryPresent(offset, 1))
                {
                    replacementOpcode = InstructionConstants.OP_DUP;
                }
                break;

            case InstructionConstants.OP_DUP_X2:
                if (!isStackEntryPresent(offset, 1) ||
                    !isStackEntryPresent(offset, 2))
                {
                    if (isStackEntryPresent(offset, 1) ||
                        isStackEntryPresent(offset, 2))
                    {
                        replacementOpcode = InstructionConstants.OP_DUP_X1;
                    }
                    else
                    {
                        replacementOpcode = InstructionConstants.OP_DUP;
                    }
                }
                break;

            case InstructionConstants.OP_DUP2_X1:
                if (!isStackEntryPresent(offset, 2))
                {
                    replacementOpcode = InstructionConstants.OP_DUP2;
                }
                break;

            case InstructionConstants.OP_DUP2_X2:
                if (!isStackEntryPresent(offset, 2) ||
                    !isStackEntryPresent(offset, 3))
                {
                    if (isStackEntryPresent(offset, 2) ||
                        isStackEntryPresent(offset, 3))
                    {
                        replacementOpcode = InstructionConstants.OP_DUP2_X1;
                    }
                    else
                    {
                        replacementOpcode = InstructionConstants.OP_DUP2;
                    }
                }
                break;

            case InstructionConstants.OP_SWAP:
                if (!isStackEntryPresent(offset, 0))
                {
                    isNecessary[offset] = false;
                }
                break;
        }

        // Actually replace the instruction with the new opcde, if any.
        if (replacementOpcode != 0)
        {
            Instruction replacementInstruction = new SimpleInstruction(replacementOpcode);
            codeAttrInfoEditor.replaceInstruction(offset,
                                                  replacementInstruction);

            if (DEBUG_ANALYSIS) System.out.println("  Replacing instruction at ["+offset+"] by "+replacementInstruction.toString());
        }
    }


    /**
     * Returns whether the given stack entry is present after execution of the
     * instruction at the given offset.
     */
    private boolean isStackEntryPresent(int instructionOffset, int stackIndex)
    {
        return isAnyNecessary(stacks[instructionOffset].getTopTraceValue(stackIndex).instructionOffsetValue());
    }


    // Implementations for ExceptionInfoVisitor.

    public void visitExceptionInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, ExceptionInfo exceptionInfo)
    {
        if (isTraced(exceptionInfo.u2startpc, exceptionInfo.u2endpc))
        {
            if (DEBUG) System.out.println("Partial evaluation of exception ["+exceptionInfo.u2startpc+","+exceptionInfo.u2endpc+"] -> ["+exceptionInfo.u2handlerpc+"]:");

            // Generalize the variables of the try block.
            variables.reset(codeAttrInfo.u2maxLocals);
            generalizeVariables(exceptionInfo.u2startpc,
                                exceptionInfo.u2endpc,
                                variables);

            // Remember the entry variables of the exception.
            TracedVariables exceptionVariables = (TracedVariables)exceptionInfo.getVisitorInfo();
            if (exceptionVariables == null)
            {
                exceptionVariables = new TracedVariables(codeAttrInfo.u2maxLocals);

                exceptionInfo.setVisitorInfo(exceptionVariables);
            }
            else
            {
                // Bail out if the entry variables are the same as last time.
                if (exceptionVariables.equals(variables))
                {
                    if (DEBUG) System.out.println("  Repeated initial variables");

                    return;
                }
            }

            exceptionVariables.initialize(variables);

            // Reuse the existing variables and stack objects, ensuring the right size.
            variables.reset(codeAttrInfo.u2maxLocals);
            stack.reset(codeAttrInfo.u2maxStack);

            // The initial stack doesn't have associated instruction offsets.
            Value storeValue = InstructionOffsetValueFactory.create();
            variables.setStoreValue(storeValue);
            stack.setStoreValue(storeValue);

            // Initialize the local variables and the stack.
            variables.initialize(exceptionVariables);
            //stack.push(ReferenceValueFactory.create((ClassCpInfo)((ProgramClassFile)classFile).getCpEntry(exceptionInfo.u2catchType), false));
            stack.push(ReferenceValueFactory.create(false));

            // Evaluate the instructions, starting at the entry point.
            evaluateInstructionBlock(classFile,
                                     methodInfo,
                                     codeAttrInfo,
                                     variables,
                                     stack,
                                     branchUnit,
                                     exceptionInfo.u2handlerpc);

            // Remember to check this exception and other exceptions once more.
            evaluateExceptions = true;
        }
    }


    /**
     * Returns whether a block of instructions is ever used.
     */
    private boolean isTraced(int startOffset, int endOffset)
    {
        for (int index = startOffset; index < endOffset; index++)
        {
            if (isTraced(index))
            {
                return true;
            }
        }

        return false;
    }


    /**
     * Generalize the local variable frames of a block of instructions.
     */
    private void generalizeVariables(int startOffset, int endOffset, TracedVariables generalizedVariables)
    {
        for (int index = startOffset; index < endOffset; index++)
        {
            if (isTraced(index))
            {
                // We can't use the return value, because local generalization
                // can be different a couple of times, with the global
                // generalization being the same.
                generalizedVariables.generalize(vars[index]);
            }
        }
    }


    // Utility methods to evaluate instruction blocks.

    /**
     * Evaluates a block of instructions, starting at the given offset and ending
     * at a branch instruction, a return instruction, or a throw instruction.
     */
    private void evaluateInstructionBlock(ClassFile        classFile,
                                          MethodInfo       methodInfo,
                                          CodeAttrInfo     codeAttrInfo,
                                          TracedVariables  variables,
                                          TracedStack      stack,
                                          TracedBranchUnit branchUnit,
                                          int              instructionOffset)
    {
        byte[] code = codeAttrInfo.code;

        if (DEBUG)
        {
             System.out.println("Instruction block starting at ["+instructionOffset+"] in "+
                                ClassUtil.externalFullMethodDescription(classFile.getName(),
                                                                        0,
                                                                        methodInfo.getName(classFile),
                                                                        methodInfo.getDescriptor(classFile)));
             System.out.println("Init vars:  "+variables);
             System.out.println("Init stack: "+stack);
        }

        Processor processor = new Processor(variables, stack, branchUnit);

        // Evaluate the subsequent instructions.
        while (true)
        {
            // Maintain a generalized trace instruction offset.
            int evaluationCount = evaluationCounts[instructionOffset]++;
            if (evaluationCount == 0)
            {
                varTraceValues[instructionOffset]   = InstructionOffsetValueFactory.create();
                stackTraceValues[instructionOffset] = InstructionOffsetValueFactory.create();
            }

            // Remember this instruction's offset with any stored value.
            Value storeValue = InstructionOffsetValueFactory.create(instructionOffset);
            variables.setStoreValue(storeValue);
            stack.setStoreValue(storeValue);

            // Reset the trace value.
            InstructionOffsetValue traceValue = InstructionOffsetValueFactory.create();
            variables.setTraceValue(traceValue);
            stack.setTraceValue(traceValue);

            // Reset the initialization flag.
            variables.resetInitialization();

            // Note that the instruction is only volatile.
            Instruction instruction = InstructionFactory.create(code, instructionOffset);

            // By default, the next instruction will be the one after this
            // instruction.
            int nextInstructionOffset = instructionOffset +
                                        instruction.length(instructionOffset);
            InstructionOffsetValue nextInstructionOffsetValue = InstructionOffsetValueFactory.create(nextInstructionOffset);
            branchUnit.resetCalled();
            branchUnit.setTraceBranchTargets(nextInstructionOffsetValue);


            if (DEBUG)
            {
                System.out.println(instruction.toString(instructionOffset));
            }

            try
            {
                // Process the instruction. The processor may call
                // the Variables methods of 'variables',
                // the Stack methods of 'stack', and
                // the BranchUnit methods of this evaluator.
                instruction.accept(classFile, methodInfo, codeAttrInfo, instructionOffset, processor);
            }
            catch (RuntimeException ex)
            {
                System.err.println("Unexpected error while performing partial evaluation:");
                System.err.println("  ClassFile   = ["+classFile.getName()+"]");
                System.err.println("  Method      = ["+methodInfo.getName(classFile)+methodInfo.getDescriptor(classFile)+"]");
                System.err.println("  Instruction = "+instruction.toString(instructionOffset));

                throw ex;
            }

            // Collect the offsets of the instructions whose results were used.
            InstructionOffsetValue variablesTraceValue = variables.getTraceValue().instructionOffsetValue();
            InstructionOffsetValue stackTraceValue     = stack.getTraceValue().instructionOffsetValue();
            varTraceValues[instructionOffset] =
                varTraceValues[instructionOffset].generalize(variablesTraceValue).instructionOffsetValue();
            stackTraceValues[instructionOffset] =
                stackTraceValues[instructionOffset].generalize(stackTraceValue).instructionOffsetValue();
            initialization[instructionOffset] =
                initialization[instructionOffset] || variables.wasInitialization();

            // Collect the branch targets from the branch unit.
            InstructionOffsetValue branchTargets = branchUnit.getTraceBranchTargets();
            int branchTargetCount = branchTargets.instructionOffsetCount();

            // Stop tracing.
            variables.setTraceValue(traceValue);
            stack.setTraceValue(traceValue);
            branchUnit.setTraceBranchTargets(traceValue);

            if (DEBUG)
            {
                if (variablesTraceValue.instructionOffsetCount() > 0)
                {
                    System.out.println("     has used information from instructions setting vars: "+variablesTraceValue);
                }
                if (stackTraceValue.instructionOffsetCount() > 0)
                {
                    System.out.println("     has used information from instructions setting stack: "+stackTraceValue);
                }
                if (branchUnit.wasCalled())
                {
                    System.out.println("     is branching to "+branchTargets);
                }

                if (varTraceValues[instructionOffset].instructionOffsetCount() > 0)
                {
                    System.out.println("     has up til now been using information from instructions setting vars: "+varTraceValues[instructionOffset]);
                }
                if (stackTraceValues[instructionOffset].instructionOffsetCount() > 0)
                {
                    System.out.println("     has up till now been using information from instructions setting stack: "+stackTraceValues[instructionOffset]);
                }
                if (branchTargetValues[instructionOffset] != null)
                {
                    System.out.println("     has up till now been branching to "+branchTargetValues[instructionOffset]);
                }

                System.out.println("     Vars:  "+variables);
                System.out.println("     Stack: "+stack);
            }

            // Maintain a generalized local variable frame and stack at this
            // branch instruction offset.
            if (vars[instructionOffset] == null)
            {
                // First time we're passing by this instruction.
                // There's not even a context at this index yet.
                vars[instructionOffset]   = new TracedVariables(variables);
                stacks[instructionOffset] = new TracedStack(stack);
            }
            else if (evaluationCount == 0)
            {
                // First time we're passing by this instruction.
                // Reuse the context objects at this index.
                vars[instructionOffset].initialize(variables);
                stacks[instructionOffset].copy(stack);
            }
            else
            {
                // If there are multiple alternative branches, or if this
                // instruction has been evaluated an excessive number of
                // times, then generalize the current context.
                // TODO: See if we can avoid generalizing the current context.
                //if (branchTargetCount > 1 ||
                //    evaluationCount > MAXIMUM_EVALUATION_COUNT)
                //{
                //    variables.generalize(vars[instructionOffset]);
                //    stack.generalize(stacks[instructionOffset]);
                //}

                boolean vars_changed  = vars[instructionOffset].generalize(variables);
                boolean stack_changed = stacks[instructionOffset].generalize(stack);

                // Bail out if the current context is the same as last time.
                if (!vars_changed  &&
                    !stack_changed &&
                    branchTargets.equals(branchTargetValues[instructionOffset]))
                {
                    if (DEBUG) System.out.println("Repeated variables, stack, and branch targets");

                    break;
                }

                // Generalize the current context. Note that the most recent
                // variable values have to remain last in the generalizations,
                // for the sake of the ret instruction.
                variables.initialize(vars[instructionOffset]);
                stack.copy(stacks[instructionOffset]);
            }

            // Did the branch unit get called?
            if (branchUnit.wasCalled())
            {
                // Accumulate the branch targets at this offset.
                branchTargetValues[instructionOffset] = branchTargetValues[instructionOffset] == null ?
                    branchTargets :
                    branchTargetValues[instructionOffset].generalize(branchTargets).instructionOffsetValue();

                // Are there no branch targets at all?
                if (branchTargetCount == 0)
                {
                    // Exit from this code block.
                    break;
                }

                // Accumulate the branch origins at the branch target offsets.
                InstructionOffsetValue instructionOffsetValue = InstructionOffsetValueFactory.create(instructionOffset);
                for (int index = 0; index < branchTargetCount; index++)
                {
                    int branchTarget = branchTargets.instructionOffset(index);
                    branchOriginValues[branchTarget] = branchOriginValues[branchTarget] == null ?
                        instructionOffsetValue:
                        branchOriginValues[branchTarget].generalize(instructionOffsetValue).instructionOffsetValue();
                }

                // Are there multiple branch targets?
                if (branchTargetCount > 1)
                {
                    // Handle them recursively and exit from this code block.
                    for (int index = 0; index < branchTargetCount; index++)
                    {
                        if (DEBUG) System.out.println("Alternative branch #"+index+" out of "+branchTargetCount+", from ["+instructionOffset+"] to ["+branchTargets.instructionOffset(index)+"]");

                        evaluateInstructionBlock(classFile,
                                                 methodInfo,
                                                 codeAttrInfo,
                                                 new TracedVariables(variables),
                                                 new TracedStack(stack),
                                                 branchUnit,
                                                 branchTargets.instructionOffset(index));
                    }

                    break;
                }

                if (DEBUG) System.out.println("Definite branch from ["+instructionOffset+"] to ["+branchTargets.instructionOffset(0)+"]");
            }

            // Just continue with the next instruction.
            instructionOffset = branchTargets.instructionOffset(0);
        }

        if (DEBUG) System.out.println("Ending processing of instruction block");
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction)
    {
        if (isTraced(offset))
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
        if (isTraced(offset))
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
        // Make sure 'new' instructions (or subsequent 'dup' instructions)
        // depend on the subsequent initializer calls, in case these calls
        // are marked as not having any side effects.
        if (isTraced(offset) &&
            cpInstruction.opcode == InstructionConstants.OP_INVOKESPECIAL)
        {
            // Check if the invoked method is an initalizer.
            classFile.constantPoolEntryAccept(cpInstruction.cpIndex, this);

            if (isInitializer)
            {
                // Find the previous instruction (assuming there was no branch).
                int previousOffset = offset - 1;
                while (!isTraced(previousOffset))
                {
                    previousOffset--;
                }

                // Compute the stack index of the uninitialized object.
                int stackIndex = stacks[offset].size();

                // Get the (first and presumably only) offset of the instruction
                // that put it there. This is typically a dup instruction.
                int newOffset = stacks[previousOffset].getBottomTraceValue(stackIndex).instructionOffsetValue().instructionOffset(0);

                // Add a reverse dependency. The source instruction depends on
                // the initializer instruction, thus making sure that the latter
                // is preserved whenever the former is used.
                stackTraceValues[newOffset] = stackTraceValues[newOffset].generalize(InstructionOffsetValueFactory.create(offset)).instructionOffsetValue();
            }
        }
    }


    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction)
    {
        replaceBranchInstruction(offset, branchInstruction);
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
        Stack stack = stacks[offset];
        Value pushedValue = stack.getTop(0);
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
        Stack stack = stacks[offset];
        Value pushedValue = stack.getTop(0);
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
        Stack stack = stacks[offset];
        Value pushedValue = stack.getTop(0);
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
        Stack stack = stacks[offset];
        Value pushedValue = stack.getTop(0);
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
        Stack stack = stacks[offset];
        Value pushedValue = stack.getTop(0);
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
    }


    /**
     * Deletes the given branch instruction, or replaces it by a simpler branch
     * instruction, if possible.
     */
    private void replaceBranchInstruction(int offset, Instruction instruction)
    {
        if (isTraced(offset))
        {
            InstructionOffsetValue branchTargetValue = branchTargetValues[offset];

            // Is there exactly one branch target (not from a goto or jsr)?
            if (branchTargetValue != null &&
                branchTargetValue.instructionOffsetCount() == 1      &&
                instruction.opcode != InstructionConstants.OP_GOTO   &&
                instruction.opcode != InstructionConstants.OP_GOTO_W &&
                instruction.opcode != InstructionConstants.OP_JSR    &&
                instruction.opcode != InstructionConstants.OP_JSR_W)
            {
                // Is it branching to the next instruction?
                int branchOffset = branchTargetValue.instructionOffset(0) - offset;
                if (branchOffset == instruction.length(offset))
                {
                    if (DEBUG_ANALYSIS) System.out.println("  Deleting zero branch instruction at ["+offset+"]");

                    // Delete the branch instruction.
                    codeAttrInfoEditor.deleteInstruction(offset);
                }
                else
                {
                    // Replace the branch instruction by a simple branch instrucion.
                    Instruction replacementInstruction =
                        new BranchInstruction(InstructionConstants.OP_GOTO_W,
                                              branchOffset).shrink();

                    if (DEBUG_ANALYSIS) System.out.println("  Replacing branch instruction at ["+offset+"] by "+replacementInstruction.toString());

                    codeAttrInfoEditor.replaceInstruction2(offset,
                                                           replacementInstruction);
                }
            }
        }
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


    // Small utility methods.

    /**
     * Returns whether any of the instructions at the given offsets are marked as
     * necessary.
     */
    private boolean isAnyNecessary(InstructionOffsetValue traceValue)
    {
        int traceCount = traceValue.instructionOffsetCount();
        if (traceCount == 0)
        {
            return true;
        }

        for (int traceIndex = 0; traceIndex < traceCount; traceIndex++)
        {
            if (isNecessary[traceValue.instructionOffset(traceIndex)])
            {
                return true;
            }
        }

        return false;
    }


    /**
     * Returns whether all of the instructions at the given offsets are marked as
     * necessary.
     */
    private boolean isAllNecessary(InstructionOffsetValue traceValue)
    {
        int traceCount = traceValue.instructionOffsetCount();
        for (int traceIndex = 0; traceIndex < traceCount; traceIndex++)
        {
            if (!isNecessary[traceValue.instructionOffset(traceIndex)])
            {
                return false;
            }
        }

        return true;
    }


    /**
     * Returns whether the instruction at the given offset has ever been
     * executed during the partial evaluation.
     */
    private boolean isTraced(int instructionOffset)
    {
        return evaluationCounts[instructionOffset] > 0;
    }
}
