/* $Id: UnusedParameterCleaner.java,v 1.5.2.1 2006/01/16 22:57:56 eric Exp $
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
import proguard.classfile.attribute.CodeAttrInfo;
import proguard.classfile.instruction.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import proguard.optimize.*;
import proguard.optimize.VariableUsageMarker;
import proguard.optimize.evaluation.value.*;

/**
 * This InstructionVisitor clears the trace values to unused parameters on
 * the stack, right before the method is being invoked.
 *
 * @see VariableUsageMarker
 * @author Eric Lafortune
 */
public class UnusedParameterCleaner
implements   InstructionVisitor,
             CpInfoVisitor,
             MemberInfoVisitor
{
    private static final boolean DEBUG = false;


    private TracedStack tracedStack;
    private Value       traceValue;


    /**
     * Creates a new UnusedParameterCleaner.
     * @param tracedStack the stack on which trace values can be cleared.
     */
    public UnusedParameterCleaner(TracedStack tracedStack)
    {
        this.tracedStack = tracedStack;
    }


    /**
     * Sets the initial Value with which all unused values will be generalized.
     */
    public void setTraceValue(Value traceValue)
    {
        this.traceValue = traceValue;
    }

    public Value getTraceValue()
    {
        return traceValue;
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction) {}
    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction) {}
    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction) {}
    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction) {}
    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction) {}


    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction)
    {
        // Fix the stack if this is a method invocation of which some
        // parameters are marked as unused.
        classFile.constantPoolEntryAccept(cpInstruction.cpIndex, this);
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}
    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo) {}
    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}


    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo)
    {
        interfaceMethodrefCpInfo.referencedMemberInfoAccept(this);
    }


    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo)
    {
        methodrefCpInfo.referencedMemberInfoAccept(this);
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo) {}


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        // Get the used parameters,
        long usedParameters = ParameterUsageMarker.getUsedVariables(programMethodInfo);

        // Compute the number of parameters.
        int parameterSize = ClassUtil.internalMethodParameterSize(programMethodInfo.getDescriptor(programClassFile));

        if ((programMethodInfo.getAccessFlags() & ClassConstants.INTERNAL_ACC_STATIC) == 0)
        {
            parameterSize++;
        }

        // Only consider the 64 first parameters at most.
        if (parameterSize > 64)
        {
            parameterSize = 64;
        }

        // Loop over all parameters.
        for (int index = 0; index < parameterSize; index++)
        {
            if ((usedParameters & (1 << index)) == 0)
            {
                int stackIndex = parameterSize - index - 1;

                if (traceValue != null)
                {
                    traceValue = traceValue.generalize(tracedStack.getTopProducerValue(stackIndex));
                }

                tracedStack.setTopProducerValue(stackIndex,
                                                InstructionOffsetValueFactory.create());
                tracedStack.setTopConsumerValue(stackIndex,
                                                InstructionOffsetValueFactory.create());

                if (DEBUG)
                {
                    System.out.println("     clearing reference from unused parameter "+index);
                    System.out.println("     Stack: "+tracedStack);
                }
            }
        }
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}
    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo) {}
}
