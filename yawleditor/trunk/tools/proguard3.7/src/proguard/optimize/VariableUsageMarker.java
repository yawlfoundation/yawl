/* $Id: VariableUsageMarker.java,v 1.3.2.1 2006/01/16 22:57:56 eric Exp $
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
package proguard.optimize;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.instruction.*;

/**
 * This InstructionVisitor marks all local variables that are used.
 *
 * @author Eric Lafortune
 */
public class VariableUsageMarker
implements   InstructionVisitor
{
    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction) {}
    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction) {}
    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction) {}
    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction) {}
    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction) {}


    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction)
    {
        markVariableUsed(methodInfo, variableInstruction.variableIndex);

        // Account for Category 2 instructions, which take up two entries.
        if (variableInstruction.isCategory2())
        {
            markVariableUsed(methodInfo, variableInstruction.variableIndex + 1);
        }
    }


    // Small utility methods.

    public static void markVariableUsed(MethodInfo methodInfo, int variableIndex)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(methodInfo);
        if (info != null)
        {
            info.setVariableUsed(variableIndex);
        }
    }


    public static boolean isVariableUsed(MethodInfo methodInfo, int variableIndex)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(methodInfo);
        return info == null ||
               info.isVariableUsed(variableIndex);
    }
}
