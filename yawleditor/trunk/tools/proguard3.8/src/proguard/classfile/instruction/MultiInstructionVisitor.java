/* $Id: MultiInstructionVisitor.java,v 1.5.2.2 2007/01/18 21:31:51 eric Exp $
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
package proguard.classfile.instruction;

import proguard.classfile.*;
import proguard.classfile.attribute.*;


/**
 * This InstructionVisitor delegates all visits to each InstructionVisitor
 * in a given list.
 *
 * @author Eric Lafortune
 */
public class MultiInstructionVisitor implements InstructionVisitor
{
    private static final int ARRAY_SIZE_INCREMENT = 5;

    // Shared copies of Instruction objects, to avoid creating a lot of objects.
    private final SimpleInstruction       simpleInstruction       = new SimpleInstruction();
    private final CpInstruction           cpInstruction           = new CpInstruction();
    private final VariableInstruction     variableInstruction     = new VariableInstruction();
    private final BranchInstruction       branchInstruction       = new BranchInstruction();
    private final TableSwitchInstruction  tableSwitchInstruction  = new TableSwitchInstruction();
    private final LookUpSwitchInstruction lookUpSwitchInstruction = new LookUpSwitchInstruction();


    private InstructionVisitor[] instructionVisitors;
    private int                  instructionVisitorCount;


    public MultiInstructionVisitor()
    {
    }


    public MultiInstructionVisitor(InstructionVisitor[] instructionVisitors)
    {
        this.instructionVisitors     = instructionVisitors;
        this.instructionVisitorCount = instructionVisitors.length;
    }


    public void addInstructionVisitor(InstructionVisitor instructionVisitor)
    {
        ensureArraySize();

        instructionVisitors[instructionVisitorCount++] = instructionVisitor;
    }


    private void ensureArraySize()
    {
        if (instructionVisitors == null)
        {
            instructionVisitors = new InstructionVisitor[ARRAY_SIZE_INCREMENT];
        }
        else if (instructionVisitors.length == instructionVisitorCount)
        {
            InstructionVisitor[] newInstructionVisitors =
                new InstructionVisitor[instructionVisitorCount +
                                     ARRAY_SIZE_INCREMENT];
            System.arraycopy(instructionVisitors, 0,
                             newInstructionVisitors, 0,
                             instructionVisitorCount);
            instructionVisitors = newInstructionVisitors;
        }
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction)
    {
        simpleInstruction = this.simpleInstruction.copy(simpleInstruction);

        for (int index = 0; index < instructionVisitorCount; index++)
        {
            instructionVisitors[index].visitSimpleInstruction(classFile, methodInfo, codeAttrInfo, offset, simpleInstruction);
        }
    }

    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction)
    {
        variableInstruction = this.variableInstruction.copy(variableInstruction);

        for (int index = 0; index < instructionVisitorCount; index++)
        {
            instructionVisitors[index].visitVariableInstruction(classFile, methodInfo, codeAttrInfo, offset, variableInstruction);
        }
    }

    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction)
    {
        cpInstruction = this.cpInstruction.copy(cpInstruction);

        for (int index = 0; index < instructionVisitorCount; index++)
        {
            instructionVisitors[index].visitCpInstruction(classFile, methodInfo, codeAttrInfo, offset, cpInstruction);
        }
    }

    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction)
    {
        branchInstruction = this.branchInstruction.copy(branchInstruction);

        for (int index = 0; index < instructionVisitorCount; index++)
        {
            instructionVisitors[index].visitBranchInstruction(classFile, methodInfo, codeAttrInfo, offset, branchInstruction);
        }
    }

    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction)
    {
        tableSwitchInstruction = this.tableSwitchInstruction.copy(tableSwitchInstruction);

        for (int index = 0; index < instructionVisitorCount; index++)
        {
            instructionVisitors[index].visitTableSwitchInstruction(classFile, methodInfo, codeAttrInfo, offset, tableSwitchInstruction);
        }
    }

    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        lookUpSwitchInstruction = this.lookUpSwitchInstruction.copy(lookUpSwitchInstruction);

        for (int index = 0; index < instructionVisitorCount; index++)
        {
            instructionVisitors[index].visitLookUpSwitchInstruction(classFile, methodInfo, codeAttrInfo, offset, lookUpSwitchInstruction);
        }
    }
}
