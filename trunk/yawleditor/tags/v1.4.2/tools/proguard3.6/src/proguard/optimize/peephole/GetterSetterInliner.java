/* $Id: GetterSetterInliner.java,v 1.18.2.2 2006/02/13 00:20:43 eric Exp $
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
package proguard.optimize.peephole;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.util.*;
import proguard.classfile.editor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.visitor.*;

/**
 * This InstructionVisitor inlines simple getter and setter methods.
 *
 * @author Eric Lafortune
 */
public class GetterSetterInliner
implements   InstructionVisitor,
             CpInfoVisitor
{
    private static final String SETTER_RETURN_TYPE = "V";

    private ConstantPoolEditor constantPoolEditor  = new ConstantPoolEditor();
    private MemberInfoVisitor  getterSetterChecker = new AllAttrInfoVisitor(
                                                     new MyGetterSetterChecker());
    private MemberFinder       memberFinder        = new MemberFinder();

    private boolean            allowAccessModification;
    private CodeAttrInfoEditor codeAttrInfoEditor;
    private InstructionVisitor extraInstructionVisitor;


    // Return values of the getter/setter checker.
    private byte       getFieldPutFieldOpcode;
    private int        referencedFieldIndex;
    private ClassFile  referencedClassFile;
    private MemberInfo referencedFieldInfo;


    /**
     * Creates a new GetterSetterInliner.
     * @param allowAccessModification indicates whether the access modifiers of
     *                                a field can be changed in order to inline
     *                                its getter or setter.
     * @param codeAttrInfoEditor      a code editor that can be used for
     *                                accumulating changes to the code.
     */
    public GetterSetterInliner(boolean            allowAccessModification,
                               CodeAttrInfoEditor codeAttrInfoEditor)
    {
        this(allowAccessModification, codeAttrInfoEditor, null);
    }


    /**
     * Creates a new GetterSetterInliner.
     * @param allowAccessModification indicates whether the access modifiers of
     *                                a field can be changed in order to inline
     *                                its getter or setter.
     * @param codeAttrInfoEditor      a code editor that can be used for
     *                                accumulating changes to the code.
     * @param extraInstructionVisitor an optional extra visitor for all replaced
     *                                store instructions.
     */
    public GetterSetterInliner(boolean            allowAccessModification,
                               CodeAttrInfoEditor codeAttrInfoEditor,
                               InstructionVisitor extraInstructionVisitor)
    {
        this.allowAccessModification = allowAccessModification;
        this.codeAttrInfoEditor      = codeAttrInfoEditor;
        this.extraInstructionVisitor = extraInstructionVisitor;
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction) {}
    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction) {}
    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction) {}
    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction) {}
    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction) {}


    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction)
    {
        byte opcode = cpInstruction.opcode;

        // Is this instruction a non-static invoke instruction?
        if (opcode == InstructionConstants.OP_INVOKEVIRTUAL ||
            opcode == InstructionConstants.OP_INVOKESPECIAL)
        {
            // Check if it's a getter or setter that can be inlined.
            getFieldPutFieldOpcode = 0;
            classFile.constantPoolEntryAccept(cpInstruction.cpIndex, this);

            // Do we have a getfield or putfield instruction to inline?
            if (getFieldPutFieldOpcode != 0)
            {
                // Reuse or create the field reference in this class.
                int fieldrefCpInfoIndex = classFile.equals(referencedClassFile) ?
                    referencedFieldIndex :
                    constantPoolEditor.addFieldrefCpInfo((ProgramClassFile)classFile,
                                                         referencedClassFile.getName(),
                                                         referencedFieldInfo.getName(referencedClassFile),
                                                         referencedFieldInfo.getDescriptor(referencedClassFile),
                                                         referencedClassFile,
                                                         referencedFieldInfo);

                // Inline the getfield or putfield instruction.
                Instruction replacementInstruction = new CpInstruction(getFieldPutFieldOpcode,
                                                                       fieldrefCpInfoIndex).shrink();

                codeAttrInfoEditor.replaceInstruction(offset, replacementInstruction);

                // Visit the instruction, if required.
                if (extraInstructionVisitor != null)
                {
                    extraInstructionVisitor.visitCpInstruction(classFile, methodInfo, codeAttrInfo, offset, cpInstruction);
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
        String descriptor = methodrefCpInfo.getType(classFile);

        // A getter or a setter can't have more than one parameter.
        int parameterCount = ClassUtil.internalMethodParameterCount(descriptor);
        if (parameterCount > 1)
        {
            return;
        }

        // A getter must return a value, a setter must return void.
        String returnType = ClassUtil.internalMethodReturnType(descriptor);
        if ((parameterCount > 0) ^ returnType.equals(SETTER_RETURN_TYPE))
        {
            return;
        }

        // The referenced method must be present and private or final.
        MemberInfo referencedMethodInfo = methodrefCpInfo.referencedMemberInfo;
        if (referencedMethodInfo == null ||
            (referencedMethodInfo.getAccessFlags() &
             (ClassConstants.INTERNAL_ACC_PRIVATE |
              ClassConstants.INTERNAL_ACC_FINAL)) == 0)
        {
            return;
        }

        // Check if the method can be inlined.
        referencedMethodInfo.accept(methodrefCpInfo.referencedClassFile,
                                    getterSetterChecker);

        // Do we have a getfield or putfield instruction to inline?
        if (getFieldPutFieldOpcode == 0)
        {
            return;
        }

        // Doesn't the field allow at least the same access as the getter or
        // setter?
        if (AccessUtil.accessLevel(referencedFieldInfo.getAccessFlags()) <
            AccessUtil.accessLevel(referencedMethodInfo.getAccessFlags()))
        {
            // Are we allowed to fix the access?
            if (allowAccessModification)
            {
                // Is the field access private, and is the field shadowed by
                // a non-private field in a subclass?
                if (AccessUtil.accessLevel(referencedFieldInfo.getAccessFlags()) == AccessUtil.PRIVATE &&
                    memberFinder.isShadowed(referencedClassFile, (FieldInfo)referencedFieldInfo))
                {
                    // Cancel the inlining.
                    getFieldPutFieldOpcode = 0;
                }

                if (getFieldPutFieldOpcode != 0)
                {
                    // Fix the access.
                    ((ProgramFieldInfo)referencedFieldInfo).u2accessFlags =
                        AccessUtil.replaceAccessFlags(referencedFieldInfo.getAccessFlags(),
                                                      referencedMethodInfo.getAccessFlags());
                }
            }
            else
            {
                // We can't give the field the access of the getter or setter.
                // Forget about inlining it after all.
                getFieldPutFieldOpcode = 0;
            }
        }
    }


    /**
     * This MemberInfoVisitor checks whether the visited member is a simple
     * getter or setter.
     */
    private class MyGetterSetterChecker
    implements    AttrInfoVisitor,
                  InstructionVisitor,
                  CpInfoVisitor
    {
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
            byte[] code = codeAttrInfo.code;

            int         offset = 0;
            Instruction instruction;

            // TODO: Handle static getters and setters.

            // Check the first instruction.
            instruction = InstructionFactory.create(code, offset);
            if (instruction.opcode != InstructionConstants.OP_ALOAD_0)
            {
                return;
            }

            offset += instruction.length(offset);

            // Start remembering if this is a getter or a setter.
            boolean getter = true;

            // TODO: Handle array getters and setters.

            // Check the second instruction.
            instruction = InstructionFactory.create(code, offset);
            byte opcode = instruction.opcode;
            if (opcode == InstructionConstants.OP_ILOAD_1 ||
                opcode == InstructionConstants.OP_LLOAD_1 ||
                opcode == InstructionConstants.OP_FLOAD_1 ||
                opcode == InstructionConstants.OP_DLOAD_1 ||
                opcode == InstructionConstants.OP_ALOAD_1)
            {
                // This instruction is loading the argument. Skip it.
                offset += instruction.length(offset);

                instruction = InstructionFactory.create(code, offset);
                opcode      = instruction.opcode;

                // So this is a setter.
                getter = false;
            }

            // Check the next instruction.
            if (getter ? opcode != InstructionConstants.OP_GETFIELD :
                         opcode != InstructionConstants.OP_PUTFIELD)
            {
                return;
            }

            // Retrieve the field class, name and type.
            instruction.accept(classFile, methodInfo, codeAttrInfo, offset, this);

            // Check the class.
            // TODO: Handle fields that are in super classes.
            if (!classFile.equals(referencedClassFile))
            {
                return;
            }

            offset += instruction.length(offset);

            // Remember the instruction opcode.
            getFieldPutFieldOpcode = opcode;

            // Check the last instruction.
            instruction = InstructionFactory.create(code, offset);
            opcode      = instruction.opcode;
            if (getter ? opcode != InstructionConstants.OP_IRETURN &&
                         opcode != InstructionConstants.OP_LRETURN &&
                         opcode != InstructionConstants.OP_FRETURN &&
                         opcode != InstructionConstants.OP_DRETURN &&
                         opcode != InstructionConstants.OP_ARETURN :
                         opcode != InstructionConstants.OP_RETURN)
            {
                // This isn't a simple getter or setter.
                // Forget about inlining it.
                getFieldPutFieldOpcode = 0;
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
            // Remember the index of the field reference.
            referencedFieldIndex = cpInstruction.cpIndex;

            // Retrieve the referenced field and its class file.
            classFile.constantPoolEntryAccept(cpInstruction.cpIndex, this);
        }


        // Implementations for CpInfoVisitor.

        public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
        public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
        public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
        public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
        public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo) {}
        public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}
        public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo) {}
        public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo) {}
        public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo) {}
        public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}


        public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo)
        {
            // Remember the class file and the field.
            referencedClassFile = fieldrefCpInfo.referencedClassFile;
            referencedFieldInfo = fieldrefCpInfo.referencedMemberInfo;
        }
    }
}
