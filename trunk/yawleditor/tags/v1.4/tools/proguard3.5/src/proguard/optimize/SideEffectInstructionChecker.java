/* $Id: SideEffectInstructionChecker.java,v 1.12.2.1 2006/01/16 22:57:56 eric Exp $
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
import proguard.classfile.visitor.*;

/**
 * This class can tell whether an instruction has any side effects. Return
 * instructions can be included or not.
 *
 * @see WriteOnlyFieldMarker
 * @see NoSideEffectMethodMarker
 * @see SideEffectMethodMarker
 * @author Eric Lafortune
 */
public class SideEffectInstructionChecker
  implements InstructionVisitor,
             CpInfoVisitor,
             MemberInfoVisitor
{
    private boolean includeReturnInstructions;

    // A return value for the visitor methods.
    private boolean hasSideEffects;


    public SideEffectInstructionChecker(boolean includeReturnInstructions)
    {
        this.includeReturnInstructions = includeReturnInstructions;
    }


    public boolean hasSideEffects(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, Instruction instruction)
    {
        hasSideEffects = false;

        instruction.accept(classFile, methodInfo,  codeAttrInfo, offset, this);

        return hasSideEffects;
    }


    // Implementations for InstructionVisitor.

    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction) {}
    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction) {}
    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction) {}


    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction)
    {
        byte opcode = simpleInstruction.opcode;

        // Check for instructions that might cause side effects.
        if (opcode == InstructionConstants.OP_IASTORE      ||
            opcode == InstructionConstants.OP_LASTORE      ||
            opcode == InstructionConstants.OP_FASTORE      ||
            opcode == InstructionConstants.OP_DASTORE      ||
            opcode == InstructionConstants.OP_AASTORE      ||
            opcode == InstructionConstants.OP_BASTORE      ||
            opcode == InstructionConstants.OP_CASTORE      ||
            opcode == InstructionConstants.OP_SASTORE      ||
            opcode == InstructionConstants.OP_ATHROW       ||
            opcode == InstructionConstants.OP_MONITORENTER ||
            opcode == InstructionConstants.OP_MONITOREXIT  ||
            (includeReturnInstructions &&
             (opcode == InstructionConstants.OP_IRETURN ||
              opcode == InstructionConstants.OP_LRETURN ||
              opcode == InstructionConstants.OP_FRETURN ||
              opcode == InstructionConstants.OP_DRETURN ||
              opcode == InstructionConstants.OP_ARETURN ||
              opcode == InstructionConstants.OP_RETURN)))
        {
            // These instructions always cause a side effect.
            hasSideEffects = true;
        }

    }


    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction)
    {
        byte opcode = variableInstruction.opcode;

        // Check for instructions that might cause side effects.
        if (includeReturnInstructions &&
            opcode == InstructionConstants.OP_RET)
        {
            hasSideEffects = true;
        }
    }


    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction)
    {
        byte opcode = cpInstruction.opcode;

        // Check for instructions that might cause side effects.
        if      (opcode == InstructionConstants.OP_PUTSTATIC     ||
                 opcode == InstructionConstants.OP_PUTFIELD      ||
                 opcode == InstructionConstants.OP_INVOKEVIRTUAL ||
                 opcode == InstructionConstants.OP_INVOKESPECIAL ||
                 opcode == InstructionConstants.OP_INVOKESTATIC  ||
                 opcode == InstructionConstants.OP_INVOKEINTERFACE)
        {
            // Check if the field is write-only, or if the invoked method is
            // causing any side effects.
            classFile.constantPoolEntryAccept(cpInstruction.cpIndex, this);
        }
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}
    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}


    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo)
    {
        hasSideEffects = !WriteOnlyFieldMarker.isWriteOnly(fieldrefCpInfo.referencedMemberInfo);
    }


    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo)
    {
        MemberInfo referencedMemberInfo = interfaceMethodrefCpInfo.referencedMemberInfo;

        // Do we have a reference to the interface method?
        if (referencedMemberInfo == null)
        {
            // We'll have to assume the unknown interface method has side effects.
            hasSideEffects = true;
        }
        else
        {
            // First check the referenced interface method itself.
            interfaceMethodrefCpInfo.referencedMemberInfoAccept(this);

            // If the result isn't conclusive, check down the hierarchy.
            if (!hasSideEffects)
            {
                ClassFile  referencedClassFile  = interfaceMethodrefCpInfo.referencedClassFile;
                MethodInfo referencedMethodInfo = (MethodInfo)referencedMemberInfo;

                // Check all implementations of the method.
                referencedClassFile.methodImplementationsAccept(referencedMethodInfo,
                                                                false,
                                                                this);
            }
        }
    }


    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo)
    {
        MemberInfo referencedMemberInfo = methodrefCpInfo.referencedMemberInfo;

        // Do we have a reference to the method?
        if (referencedMemberInfo == null)
        {
            // We'll have to assume the unknown method has side effects.
            hasSideEffects = true;
        }
        else
        {
            // First check the referenced method itself.
            methodrefCpInfo.referencedMemberInfoAccept(this);

            // If the result isn't conclusive, check down the hierarchy.
            if (!hasSideEffects)
            {
                ClassFile  referencedClassFile  = methodrefCpInfo.referencedClassFile;
                MethodInfo referencedMethodInfo = (MethodInfo)referencedMemberInfo;

                // Check all other implementations of the method in the class
                // hierarchy.
                referencedClassFile.methodImplementationsAccept(referencedMethodInfo,
                                                                false,
                                                                this);
            }
        }
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo) {}

    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        hasSideEffects = hasSideEffects ||
                         SideEffectMethodMarker.hasSideEffects(programMethodInfo);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}

    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        hasSideEffects = hasSideEffects ||
                         !NoSideEffectMethodMarker.hasNoSideEffects(libraryMethodInfo);
    }
}
