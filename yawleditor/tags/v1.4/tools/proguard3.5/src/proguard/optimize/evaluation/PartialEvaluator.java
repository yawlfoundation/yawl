/* $Id: PartialEvaluator.java,v 1.37.2.1 2006/01/16 22:57:56 eric Exp $
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
import proguard.classfile.instruction.*;
import proguard.classfile.util.ClassUtil;
import proguard.optimize.evaluation.value.*;
import proguard.optimize.peephole.BranchTargetFinder;

/**
 * This class performs partial evaluation.
 *
 * @author Eric Lafortune
 */
public class PartialEvaluator
implements   ExceptionInfoVisitor,
             InstructionVisitor
{
    //*
    private static final boolean DEBUG         = false;
    private static final boolean DEBUG_RESULTS = false;
    /*/
    private static boolean DEBUG         = true;
    private static boolean DEBUG_RESULTS = true;
    //*/

    private static final int INITIAL_CODE_LENGTH = 1024;
    private static final int INITIAL_VALUE_COUNT = 32;

    //private static final int MAXIMUM_EVALUATION_COUNT = 100;

    public static final int AT_METHOD_ENTRY = -1;
    public static final int AT_CATCH_ENTRY  = -1;
    public static final int NONE            = -1;

    private BranchTargetFinder branchTargetFinder = new BranchTargetFinder(1024);

    private InstructionOffsetValue[] varProducerValues    = new InstructionOffsetValue[INITIAL_CODE_LENGTH];
    private InstructionOffsetValue[] stackProducerValues  = new InstructionOffsetValue[INITIAL_CODE_LENGTH];
    private InstructionOffsetValue[] unusedProducerValues = new InstructionOffsetValue[INITIAL_CODE_LENGTH];
    private InstructionOffsetValue[] branchOriginValues   = new InstructionOffsetValue[INITIAL_CODE_LENGTH];
    private InstructionOffsetValue[] branchTargetValues   = new InstructionOffsetValue[INITIAL_CODE_LENGTH];
    private TracedVariables[]        vars                 = new TracedVariables[INITIAL_CODE_LENGTH];
    private TracedStack[]            stacks               = new TracedStack[INITIAL_CODE_LENGTH];
    private int[]                    evaluationCounts     = new int[INITIAL_CODE_LENGTH];
    private int[]                    initializedVariables = new int[INITIAL_CODE_LENGTH];
    private boolean                  evaluateExceptions;

    private TracedVariables  variables  = new TracedVariables(INITIAL_VALUE_COUNT);
    private TracedStack      stack      = new TracedStack(INITIAL_VALUE_COUNT);
    private TracedBranchUnit branchUnit = new TracedBranchUnit();


    /**
     * Performs partial evaluation of the given method with the given parameters.
     * @param classFile    the method's class file.
     * @param methodInfo   the method's header.
     * @param codeAttrInfo the method's code.
     * @param parameters   the method parameters.
     * @return             the partial result.
     */
    public Value evaluate(ClassFile    classFile,
                          MethodInfo   methodInfo,
                          CodeAttrInfo codeAttrInfo,
                          Variables    parameters)
    {
        // Initialize the reusable arrays and variables.
        initializeVariables(codeAttrInfo, parameters);

        // Find all instruction offsets,...
        codeAttrInfo.accept(classFile, methodInfo, branchTargetFinder);

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

        if (DEBUG_RESULTS)
        {
            System.out.println("Evaluation results:");

            int codeLength = codeAttrInfo.u4codeLength;

            int offset = 0;
            do
            {
                Instruction instruction = InstructionFactory.create(codeAttrInfo.code,
                                                                    offset);
                System.out.println(instruction.toString(offset));
                if (isTraced(offset))
                {
                    InstructionOffsetValue varProducerOffsets = varProducerOffsets(offset);
                    if (varProducerOffsets.instructionOffsetCount() > 0)
                    {
                        System.out.println("     has overall been using information from instructions setting vars: "+varProducerOffsets);
                    }

                    InstructionOffsetValue stackProducerOffsets = stackProducerOffsets(offset);
                    if (stackProducerOffsets.instructionOffsetCount() > 0)
                    {
                        System.out.println("     has overall been using information from instructions setting stack: "+stackProducerOffsets);
                    }

                    InstructionOffsetValue unusedProducerOffsets = unusedProducerOffsets(offset);
                    if (unusedProducerOffsets.instructionOffsetCount() > 0)
                    {
                        System.out.println("     no longer needs information from instructions setting stack: "+unusedProducerOffsets);
                    }

                    InstructionOffsetValue branchTargets = branchTargets(offset);
                    if (branchTargets != null)
                    {
                        System.out.println("     has overall been branching to "+branchTargets);
                    }

                    System.out.println("     Vars:  "+vars[offset]);
                    System.out.println("     Stack: "+stacks[offset]);
                }

                offset += instruction.length(offset);
            }
            while (offset < codeLength);
        }

        if (DEBUG)
        {
            Value returnValue = branchUnit.getTraceReturnValue();
            if (returnValue != null)
            {
                System.out.println("Return value for method "+
                                   ClassUtil.externalFullMethodDescription(classFile.getName(),
                                                                           0,
                                                                           methodInfo.getName(classFile),
                                                                           methodInfo.getDescriptor(classFile))+
                                   " -> ["+returnValue.toString()+"]");
                System.out.println();
            }
        }

        // Mark special dependencies of constructors.
        codeAttrInfo.instructionsAccept(classFile, methodInfo, this);

        return branchUnit.getTraceReturnValue();
    }


    /**
     * Returns whether a block of instructions is ever used.
     */
    public boolean isTraced(int startOffset, int endOffset)
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
     * Returns whether the instruction at the given offset has ever been
     * executed during the partial evaluation.
     */
    public boolean isTraced(int instructionOffset)
    {
        return evaluationCounts[instructionOffset] > 0;
    }


    /**
     * Returns the variable value at the given instruction offset and variable
     * index.
     */
    public Value variableValue(int instructionOffset,
                               int variableIndex)
    {
        return vars[instructionOffset].load(variableIndex);
    }


    /**
     * Returns the instruction offsets that set the variable value at at the given
     * instruction offset and variable index.
     */
    public InstructionOffsetValue variableProducerOffsets(int instructionOffset,
                                                          int variableIndex)
    {
        return vars[instructionOffset].getStoredTraceValue(variableIndex).instructionOffsetValue();
    }


    /**
     * Returns the instruction offsets that set the variable that is being
     * used at the given instruction offset.
     */
    public InstructionOffsetValue varProducerOffsets(int instructionOffset)
    {
        return varProducerValues[instructionOffset];
    }


    /**
     * Returns the stack value at the given instruction offset and stack index.
     */
    public Value stackTopValue(int instructionOffset,
                               int stackIndex)
    {
        return stacks[instructionOffset].getTop(stackIndex);
    }


    /**
     * Returns the instruction offsets that set the stack value at at the given
     * instruction offset and stack index.
     */
    public InstructionOffsetValue stackTopProducerOffsets(int instructionOffset,
                                                          int stackIndex)
    {
        return stacks[instructionOffset].getTopProducerValue(stackIndex).instructionOffsetValue();
    }


    /**
     * Returns the instruction offsets that set the stack entries that are being
     * used at the given instruction offset.
     */
    public InstructionOffsetValue stackProducerOffsets(int instructionOffset)
    {
        return stackProducerValues[instructionOffset];
    }


    /**
     * Returns the instruction offsets that use the stack value at at the given
     * instruction offset and stack index.
     */
    public InstructionOffsetValue stackTopConsumerOffsets(int instructionOffset,
                                                          int stackIndex)
    {
        return stacks[instructionOffset].getTopConsumerValue(stackIndex).instructionOffsetValue();
    }


    /**
     * Returns the instruction offsets that set stack entries that are not being
     * used at the given instruction offset (e.g. because the parameters are not
     * being used).
     */
    public InstructionOffsetValue unusedProducerOffsets(int instructionOffset)
    {
        return unusedProducerValues[instructionOffset];
    }


    /**
     * Returns the instruction offsets that branch to the given instruction
     * offset.
     */
    public InstructionOffsetValue branchOrigins(int instructionOffset)
    {
        return branchOriginValues[instructionOffset];
    }


    /**
     * Returns the instruction offsets to which the given instruction offset
     * branches.
     */
    public InstructionOffsetValue branchTargets(int instructionOffset)
    {
        return branchTargetValues[instructionOffset];
    }


    /**
     * Returns the variable that is initialized at the given instruction offset,
     * or NONE if no variable was initialized.
     */
    public int initializedVariable(int instructionOffset)
    {
        return initializedVariables[instructionOffset];
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

            // The initial stack has a generic instruction offset.
            Value storeValue = InstructionOffsetValueFactory.create(AT_CATCH_ENTRY);
            variables.setProducerValue(storeValue);
            stack.setProducerValue(storeValue);

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


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction) {}
    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction) {}
    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction) {}
    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction) {}
    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction) {}


    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction)
    {
        // Make sure 'new' instructions (or subsequent 'dup' instructions)
        // depend on the subsequent initializer calls, in case these calls
        // are marked as not having any side effects.

        // Check if the invoked method is an initalizer.
        if (isTraced(offset) &&
            branchTargetFinder.isInitializer(offset))
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
            int newOffset = stacks[previousOffset].getBottomProducerValue(stackIndex).instructionOffsetValue().instructionOffset(0);

            // Add a reverse dependency. The source instruction depends on
            // the initializer instruction, thus making sure that the latter
            // is preserved whenever the former is used.
            stackProducerValues[newOffset] = stackProducerValues[newOffset].generalize(InstructionOffsetValueFactory.create(offset)).instructionOffsetValue();
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

        UnusedParameterCleaner unusedParameterCleaner = new UnusedParameterCleaner(stack);

        // Evaluate the subsequent instructions.
        while (true)
        {
            // Maintain a generalized trace instruction offset.
            int evaluationCount = evaluationCounts[instructionOffset]++;
            if (evaluationCount == 0)
            {
                varProducerValues[instructionOffset]    = InstructionOffsetValueFactory.create();
                stackProducerValues[instructionOffset]  = InstructionOffsetValueFactory.create();
                unusedProducerValues[instructionOffset] = InstructionOffsetValueFactory.create();
            }

            // Remember this instruction's offset with any stored value.
            Value storeValue = InstructionOffsetValueFactory.create(instructionOffset);
            variables.setProducerValue(storeValue);
            stack.setProducerValue(storeValue);

            // Reset the trace value.
            InstructionOffsetValue traceValue = InstructionOffsetValueFactory.create();
            variables.setCollectedProducerValue(traceValue);
            stack.setCollectedProducerValue(traceValue);
            unusedParameterCleaner.setTraceValue(traceValue);

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

            // First clean all traces to unused parameters if this is a method
            // invocation.
            instruction.accept(classFile,
                               methodInfo,
                               codeAttrInfo,
                               instructionOffset,
                               unusedParameterCleaner);

            if (DEBUG)
            {
                System.out.println(instruction.toString(instructionOffset));
            }

            try
            {
                // Process the instruction. The processor may call
                // the Variables methods of 'variables',
                // the Stack methods of 'stack', and
                // the BranchUnit methods of 'branchUnit'.
                instruction.accept(classFile,
                                   methodInfo,
                                   codeAttrInfo,
                                   instructionOffset,
                                   processor);
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
            InstructionOffsetValue variablesTraceValue = variables.getCollectedProducerValue().instructionOffsetValue();
            InstructionOffsetValue stackTraceValue     = stack.getCollectedProducerValue().instructionOffsetValue();
            InstructionOffsetValue unusedTraceValue    = unusedParameterCleaner.getTraceValue().instructionOffsetValue();
            varProducerValues[instructionOffset] =
                varProducerValues[instructionOffset].generalize(variablesTraceValue).instructionOffsetValue();
            stackProducerValues[instructionOffset] =
                stackProducerValues[instructionOffset].generalize(stackTraceValue).instructionOffsetValue();
            unusedProducerValues[instructionOffset] =
                unusedProducerValues[instructionOffset].generalize(unusedTraceValue).instructionOffsetValue();
            initializedVariables[instructionOffset] = variables.getInitializationIndex();

            // Collect the branch targets from the branch unit.
            InstructionOffsetValue branchTargets = branchUnit.getTraceBranchTargets();
            int branchTargetCount = branchTargets.instructionOffsetCount();

            // Stop tracing.
            variables.setCollectedProducerValue(traceValue);
            stack.setCollectedProducerValue(traceValue);
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

                if (varProducerValues[instructionOffset].instructionOffsetCount() > 0)
                {
                    System.out.println("     has up til now been using information from instructions setting vars: "+varProducerValues[instructionOffset]);
                }
                if (stackProducerValues[instructionOffset].instructionOffsetCount() > 0)
                {
                    System.out.println("     has up till now been using information from instructions setting stack: "+stackProducerValues[instructionOffset]);
                }
                if (unusedProducerValues[instructionOffset].instructionOffsetCount() > 0)
                {
                    System.out.println("     no longer needs information from instructions setting stack: "+unusedProducerValues[instructionOffset]);
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


    // Small utility methods.

    /**
     * Initializes the data structures for the variables, stack, etc.
     */
    private void initializeVariables(CodeAttrInfo codeAttrInfo,
                                     Variables    parameters)
    {
        int codeLength = codeAttrInfo.u4codeLength;

        if (DEBUG)
        {
            System.out.println("  Max locals = "+codeAttrInfo.u2maxLocals);
            System.out.println("  Max stack  = "+codeAttrInfo.u2maxStack);
        }

        // Create new arrays for storing information at each instruction offset.
        if (vars.length < codeLength)
        {
            varProducerValues    = new InstructionOffsetValue[codeLength];
            stackProducerValues  = new InstructionOffsetValue[codeLength];
            unusedProducerValues = new InstructionOffsetValue[codeLength];
            branchOriginValues   = new InstructionOffsetValue[codeLength];
            branchTargetValues   = new InstructionOffsetValue[codeLength];
            vars                 = new TracedVariables[codeLength];
            stacks               = new TracedStack[codeLength];
            evaluationCounts     = new int[codeLength];
            initializedVariables = new int[codeLength];

            for (int index = 0; index < codeLength; index++)
            {
                initializedVariables[index] = NONE;
            }
        }
        else
        {
            for (int index = 0; index < codeLength; index++)
            {
                varProducerValues[index]    = null;
                stackProducerValues[index]  = null;
                unusedProducerValues[index] = null;
                branchOriginValues[index]   = null;
                branchTargetValues[index]   = null;
                evaluationCounts[index]     = 0;
                initializedVariables[index] = NONE;

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

        // Initialize the variables with the parameters.
        variables.initialize(parameters);

        // Set the store value of each parameter variable.
        InstructionOffsetValue atMethodEntry = InstructionOffsetValueFactory.create(PartialEvaluator.AT_METHOD_ENTRY);

        for (int index = 0; index < parameters.size(); index++)
        {
            variables.setStoredTraceValue(index, atMethodEntry);
        }

        // Reset the return value.
        branchUnit.setTraceReturnValue(null);
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
}
