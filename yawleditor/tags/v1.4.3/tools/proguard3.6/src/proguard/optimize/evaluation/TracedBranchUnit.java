/* $Id: TracedBranchUnit.java,v 1.5.2.1 2006/01/16 22:57:56 eric Exp $
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
import proguard.optimize.evaluation.value.*;

/**
 * This BranchUnit remembers the branch unit commands that are invoked on it.
 *
 * @author Eric Lafortune
 */
class TracedBranchUnit implements BranchUnit
{
    private boolean                wasCalled;
    private InstructionOffsetValue traceBranchTargets;
    private Value                  traceReturnValue;


    /**
     * Resets the flag that tells whether any of the branch unit commands was
     * called.
     */
    public void resetCalled()
    {
        wasCalled = false;
    }

    /**
     * Returns whether any of the branch unit commands was called.
     */
    public boolean wasCalled()
    {
        return wasCalled;
    }


    /**
     * Sets the initial branch targets, which will be updated as the branch
     * methods of the branch unit are called.
     */
    public void setTraceBranchTargets(InstructionOffsetValue branchTargets)
    {
        this.traceBranchTargets = branchTargets;
    }

    public InstructionOffsetValue getTraceBranchTargets()
    {
        return traceBranchTargets;
    }


    /**
     * Sets the initial return Value, which will be generalized as the
     * return method of the branch unit is called. The initial value may be
     * null.
     */
    public void setTraceReturnValue(Value traceReturnValue)
    {
        this.traceReturnValue = traceReturnValue;
    }

    public Value getTraceReturnValue()
    {
        return traceReturnValue;
    }


    // Implementations for BranchUnit.

    public void branch(ClassFile    classFile,
                       CodeAttrInfo codeAttrInfo,
                       int          offset,
                       int          branchTarget)
    {
        branchConditionally(classFile,
                            codeAttrInfo,
                            offset,
                            branchTarget,
                            Value.ALWAYS);
    }


    public void branchConditionally(ClassFile    classFile,
                                    CodeAttrInfo codeAttrInfo,
                                    int          offset,
                                    int          branchTarget,
                                    int          conditional)
    {
        // Mark possible branches at the offset and at the branch target.
        if (conditional != Value.NEVER)
        {
            InstructionOffsetValue branchTargetValue = InstructionOffsetValueFactory.create(branchTarget);

            // Accumulate the branch targets for the evaluator.
            traceBranchTargets = conditional == Value.ALWAYS ?
                branchTargetValue :
                traceBranchTargets.generalize(branchTargetValue).instructionOffsetValue();
        }

        wasCalled = true;
    }


    public void returnFromMethod(Value returnValue)
    {
        traceReturnValue = traceReturnValue == null ?
            returnValue :
            traceReturnValue.generalize(returnValue);

        // Stop processing this block.
        traceBranchTargets = InstructionOffsetValueFactory.create();

        wasCalled = true;
    }


    public void throwException()
    {
        // Stop processing this block.
        traceBranchTargets = InstructionOffsetValueFactory.create();

        wasCalled = true;
    }
}
