/* $Id: InstructionCounter.java,v 1.1.2.4 2007/01/18 21:31:51 eric Exp $
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
import proguard.classfile.attribute.CodeAttrInfo;
import proguard.classfile.instruction.*;

/**
 * This InstructionVisitor counts the number of instructions that has been visited.
 *
 * @author Eric Lafortune
 */
public class InstructionCounter implements InstructionVisitor
{
    private int count;


    /**
     * Returns the number of instructions that has been visited so far.
     */
    public int getCount()
    {
        return count;
    }


    // Implementations for InstructionVisitor.

    public void visitBranchInstruction(ClassFile         classFile,
                                       MethodInfo        methodInfo,
                                       CodeAttrInfo      codeAttrInfo,
                                       int               offset,
                                       BranchInstruction branchInstruction)
    {
        count++;
    }


    public void visitCpInstruction(ClassFile     classFile,
                                   MethodInfo    methodInfo,
                                   CodeAttrInfo  codeAttrInfo,
                                   int           offset,
                                   CpInstruction cpInstruction)
    {
        count++;
    }


    public void visitLookUpSwitchInstruction(ClassFile               classFile,
                                             MethodInfo              methodInfo,
                                             CodeAttrInfo            codeAttrInfo,
                                             int                     offset,
                                             LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        count++;
    }


    public void visitSimpleInstruction(ClassFile         classFile,
                                       MethodInfo        methodInfo,
                                       CodeAttrInfo      codeAttrInfo,
                                       int               offset,
                                       SimpleInstruction simpleInstruction)
    {
        count++;
    }


    public void visitTableSwitchInstruction(ClassFile              classFile,
                                            MethodInfo             methodInfo,
                                            CodeAttrInfo           codeAttrInfo,
                                            int                    offset,
                                            TableSwitchInstruction tableSwitchInstruction)
    {
        count++;
    }


    public void visitVariableInstruction(ClassFile           classFile,
                                         MethodInfo          methodInfo,
                                         CodeAttrInfo        codeAttrInfo,
                                         int                 offset,
                                         VariableInstruction variableInstruction)
    {
        count++;
    }
}
