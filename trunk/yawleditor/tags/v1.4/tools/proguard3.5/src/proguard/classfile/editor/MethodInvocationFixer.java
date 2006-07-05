/* $Id: MethodInvocationFixer.java,v 1.4.2.1 2006/01/16 22:57:55 eric Exp $
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
package proguard.classfile.editor;

import proguard.classfile.*;
import proguard.classfile.util.ClassUtil;
import proguard.classfile.attribute.CodeAttrInfo;
import proguard.classfile.instruction.*;
import proguard.classfile.visitor.*;

/**
 * This InstructionVisitor fixes all inappropriate special/virtual/static/interface
 * invocations.
 *
 * @author Eric Lafortune
 */
public class MethodInvocationFixer
implements   InstructionVisitor,
             CpInfoVisitor,
             MemberInfoVisitor
{
    private static final boolean DEBUG = false;

    private CodeAttrInfoEditor codeAttrInfoEditor;

    // Return values for the visitor methods.
    private boolean isMethodInvocation;
    private int     accessFlags;
    private boolean isInitializer;
    private boolean isInterfaceMethod;
    private int     parameterSize;


    /**
     * Creates a new MethodInvocationFixer.
     * @param codeAttrInfoEditor a code editor that can be used for
     *                           accumulating changes to the code.
     */
    public MethodInvocationFixer(CodeAttrInfoEditor codeAttrInfoEditor)
    {
        this.codeAttrInfoEditor = codeAttrInfoEditor;
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction) {}
    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction) {}
    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction) {}
    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction) {}
    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction) {}


    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction)
    {
        int cpIndex  = cpInstruction.cpIndex;
        int constant = cpInstruction.constant;

        // Get the constant pool entry's information.
        isMethodInvocation = false;
        isInterfaceMethod  = false;
        accessFlags        = 0;
        parameterSize      = constant;

        classFile.constantPoolEntryAccept(cpIndex, this);

        // Is it a method invocation?
        if (isMethodInvocation)
        {
            // Do we need to update the opcode?
            byte opcode = cpInstruction.opcode;

            // Is the method static?
            if ((accessFlags & ClassConstants.INTERNAL_ACC_STATIC) != 0)
            {
                // But is it not a static invocation?
                if (opcode != InstructionConstants.OP_INVOKESTATIC)
                {
                    // Replace the invocation by an invokestatic instruction.
                    Instruction replacementInstruction =
                        new CpInstruction(InstructionConstants.OP_INVOKESTATIC,
                                          cpIndex).shrink();

                    codeAttrInfoEditor.replaceInstruction(offset, replacementInstruction);

                    if (DEBUG)
                    {
                        debug(classFile, methodInfo, offset, cpInstruction, replacementInstruction);
                    }
                }
            }

            // Is the method private, or an instance initializer?
            else if ((accessFlags & ClassConstants.INTERNAL_ACC_PRIVATE) != 0 ||
                     isInitializer)
            {
                // But is it not a special invocation?
                if (opcode != InstructionConstants.OP_INVOKESPECIAL)
                {
                    // Replace the invocation by an invokespecial instruction.
                    Instruction replacementInstruction =
                        new CpInstruction(InstructionConstants.OP_INVOKESPECIAL,
                                          cpIndex).shrink();

                    codeAttrInfoEditor.replaceInstruction(offset, replacementInstruction);

                    if (DEBUG)
                    {
                        debug(classFile, methodInfo, offset, cpInstruction, replacementInstruction);
                    }
                }
            }

            // Is the method an interface method?
            else if (isInterfaceMethod)
            {
                // But is it not an interface invocation, or is the parameter
                // size incorrect?
                if (opcode != InstructionConstants.OP_INVOKEINTERFACE ||
                    parameterSize != constant)
                {
                    // Fix the parameter size of the interface invocation.
                    Instruction replacementInstruction =
                        new CpInstruction(InstructionConstants.OP_INVOKEINTERFACE,
                                          cpIndex,
                                          parameterSize).shrink();

                    codeAttrInfoEditor.replaceInstruction(offset, replacementInstruction);

                    if (DEBUG)
                    {
                        debug(classFile, methodInfo, offset, cpInstruction, replacementInstruction);
                    }
                }
            }

            // The method is not static, private, an instance initializer, or
            // an interface method.
            else
            {
                // But is it not a virtual invocation (or a special invocation,
                // which is allowed for super calls)?
                if (opcode != InstructionConstants.OP_INVOKEVIRTUAL &&
                    opcode != InstructionConstants.OP_INVOKESPECIAL)
                {
                    // Replace the invocation by an invokevirtual instruction.
                    Instruction replacementInstruction =
                        new CpInstruction(InstructionConstants.OP_INVOKEVIRTUAL,
                                          cpIndex).shrink();

                    codeAttrInfoEditor.replaceInstruction(offset, replacementInstruction);

                    if (DEBUG)
                    {
                        debug(classFile, methodInfo, offset, cpInstruction, replacementInstruction);
                    }
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
    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}


    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo)
    {
        // Check if this is an interface method.
        classFile.constantPoolEntryAccept(interfaceMethodrefCpInfo.u2classIndex, this);

        // Get the referenced method's access flags.
        interfaceMethodrefCpInfo.referencedMemberInfoAccept(this);
    }


    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo)
    {
        // Check if this is an interface method.
        classFile.constantPoolEntryAccept(methodrefCpInfo.u2classIndex, this);

        // Get the referenced method's access flags.
        methodrefCpInfo.referencedMemberInfoAccept(this);
    }


    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        // Check if this class entry refers to an interface class.
        ClassFile referencedClassFile = classCpInfo.referencedClassFile;
        if (referencedClassFile != null)
        {
            isInterfaceMethod = (referencedClassFile.getAccessFlags() & ClassConstants.INTERNAL_ACC_INTERFACE) != 0;
        }
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo) {}

    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        visitMethodInfo(programClassFile, programMethodInfo);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}

    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        visitMethodInfo(libraryClassFile, libraryMethodInfo);
    }


    private void visitMethodInfo(ClassFile classFile, MethodInfo methodInfo)
    {
        // We've found a method definition.
        isMethodInvocation = true;

        // Get the method's access flags.
        accessFlags = methodInfo.getAccessFlags();

        // Check if this is an instance initializer.
        isInitializer = methodInfo.getName(classFile).equals(ClassConstants.INTERNAL_METHOD_NAME_INIT);

        // Remember the parameter size of interface methods.
        if (isInterfaceMethod)
        {
            parameterSize = ClassUtil.internalMethodParameterSize(methodInfo.getDescriptor(classFile)) + 1 << 8;
        }
    }


    // Small utility methods.

    private void debug(ClassFile     classFile,
                       MethodInfo    methodInfo,
                       int           offset,
                       CpInstruction cpInstruction,
                       Instruction   replacementInstruction)
    {
        System.out.println("MethodInvocationFixer:");
        System.out.println("  Class file       = "+classFile.getName());
        System.out.println("  Method           = "+methodInfo.getName(classFile)+methodInfo.getDescriptor(classFile));
        System.out.println("  Instruction      = "+cpInstruction.toString(offset));
        System.out.println("  Interface method = "+isInterfaceMethod);
        if (isInterfaceMethod)
        {
            System.out.println("  Parameter size   = "+parameterSize);
        }
        System.out.println("  Replacement instruction = "+replacementInstruction.toString(offset));
    }
}
