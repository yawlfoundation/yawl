/* $Id: WriteOnlyFieldMarker.java,v 1.7.2.1 2006/01/16 22:57:56 eric Exp $
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
 * This InstructionVisitor marks all fields that are write-only.
 *
 * @author Eric Lafortune
 */
public class WriteOnlyFieldMarker
  implements InstructionVisitor,
             CpInfoVisitor,
             MemberInfoVisitor
{
    // Visitor info flags to indicate whether a FieldInfo object is write-only.
    private static final Object READ       = new Object();
    private static final Object WRITE_ONLY = new Object();


    // Parameters and values for visitor methods.
    private boolean reading;


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction) {}
    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction) {}
    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction) {}
    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction) {}
    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction) {}


    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction)
    {
        byte opcode = cpInstruction.opcode;

        // Check for instructions that involve fields.
        if      (opcode == InstructionConstants.OP_GETSTATIC     ||
                 opcode == InstructionConstants.OP_GETFIELD)
        {
            // Mark the field as being read from.
            reading = true;
            classFile.constantPoolEntryAccept(cpInstruction.cpIndex, this);
        }
        else if (opcode == InstructionConstants.OP_PUTSTATIC     ||
                 opcode == InstructionConstants.OP_PUTFIELD)
        {
            // Mark the field as being written to.
            reading = false;
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
    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo) {}
    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo) {}
    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}


    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo)
    {
        MemberInfo referencedMemberInfo = fieldrefCpInfo.referencedMemberInfo;

        if (referencedMemberInfo != null)
        {
            referencedMemberInfo.accept(fieldrefCpInfo.referencedClassFile, this);
        }
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        // Hasn't the field been marked as being read yet?
        if (!isRead(programFieldInfo))
        {
            // Mark it now, depending on whether this is a reading operation
            // or a writing operation.
            if (reading)
            {
                markAsRead(programFieldInfo);
            }
            else
            {
                markAsWriteOnly(programFieldInfo);
            }
        }
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo) {}
    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}
    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo) {}


    // Small utility methods.

    private static void markAsRead(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(READ);
    }


    private static boolean isRead(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter == null                  ||
               visitorAccepter.getVisitorInfo() == READ ||
               KeepMarker.isKept(visitorAccepter);
    }


    public static void markAsWriteOnly(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(WRITE_ONLY);
    }


    public static boolean isWriteOnly(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter != null                        &&
               visitorAccepter.getVisitorInfo() == WRITE_ONLY &&
               !KeepMarker.isKept(visitorAccepter);
    }
}
