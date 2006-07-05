/* $Id: Processor.java,v 1.12 2004/12/11 16:35:23 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2004 Eric Lafortune (eric@graphics.cornell.edu)
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
import proguard.classfile.visitor.*;
import proguard.optimize.evaluation.value.*;

/**
 * This InstructionVisitor executes the instructions that it visits on a given
 * local variable frame and stack.
 *
 * @author Eric Lafortune
 */
public class Processor
implements   InstructionVisitor,
             CpInfoVisitor
{
    private Variables  variables;
    private Stack      stack;
    private BranchUnit branchUnit;

    // Fields acting as return parameters for the CpInfoVisitor methods.
    private int       parameterCount;
    private Value     cpValue;
    private ClassFile referencedClassFile;
    private int       referencedTypeDimensionCount;


    /**
     * Creates a new processor that operates on the given environment.
     * @param variables  the local variable frame.
     * @param stack      the local stack.
     * @param branchUnit the class that can affect the program counter.
     */
    public Processor(Variables  variables,
                     Stack      stack,
                     BranchUnit branchUnit)
    {
        this.variables  = variables;
        this.stack      = stack;
        this.branchUnit = branchUnit;
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction)
    {
        switch (simpleInstruction.opcode)
        {
            case InstructionConstants.OP_NOP:
                break;

            case InstructionConstants.OP_ACONST_NULL:
                stack.push(ReferenceValueFactory.createNull());
                break;

            case InstructionConstants.OP_ICONST_M1:
            case InstructionConstants.OP_ICONST_0:
            case InstructionConstants.OP_ICONST_1:
            case InstructionConstants.OP_ICONST_2:
            case InstructionConstants.OP_ICONST_3:
            case InstructionConstants.OP_ICONST_4:
            case InstructionConstants.OP_ICONST_5:
            case InstructionConstants.OP_BIPUSH:
            case InstructionConstants.OP_SIPUSH:
                stack.push(IntegerValueFactory.create(simpleInstruction.constant));
                break;

            case InstructionConstants.OP_LCONST_0:
            case InstructionConstants.OP_LCONST_1:
                stack.push(LongValueFactory.create(simpleInstruction.constant));
                break;

            case InstructionConstants.OP_FCONST_0:
            case InstructionConstants.OP_FCONST_1:
            case InstructionConstants.OP_FCONST_2:
                stack.push(FloatValueFactory.create((float)simpleInstruction.constant));
                break;

            case InstructionConstants.OP_DCONST_0:
            case InstructionConstants.OP_DCONST_1:
                stack.push(DoubleValueFactory.create((double)simpleInstruction.constant));
                break;

            case InstructionConstants.OP_IALOAD:
            case InstructionConstants.OP_BALOAD:
            case InstructionConstants.OP_CALOAD:
            case InstructionConstants.OP_SALOAD:
                stack.ipop();
                stack.apop();
                stack.push(IntegerValueFactory.create());
                break;

            case InstructionConstants.OP_LALOAD:
                stack.ipop();
                stack.apop();
                stack.push(LongValueFactory.create());
                break;

            case InstructionConstants.OP_FALOAD:
                stack.ipop();
                stack.apop();
                stack.push(FloatValueFactory.create());
                break;

            case InstructionConstants.OP_DALOAD:
                stack.ipop();
                stack.apop();
                stack.push(DoubleValueFactory.create());
                break;

            case InstructionConstants.OP_AALOAD:
                stack.ipop();
                stack.apop();
                stack.push(ReferenceValueFactory.create(true));
                break;

            case InstructionConstants.OP_IASTORE:
            case InstructionConstants.OP_BASTORE:
            case InstructionConstants.OP_CASTORE:
            case InstructionConstants.OP_SASTORE:
                stack.ipop();
                stack.ipop();
                stack.apop();
                break;

            case InstructionConstants.OP_LASTORE:
                stack.lpop();
                stack.ipop();
                stack.apop();
                break;

            case InstructionConstants.OP_FASTORE:
                stack.fpop();
                stack.ipop();
                stack.apop();
                break;

            case InstructionConstants.OP_DASTORE:
                stack.dpop();
                stack.ipop();
                stack.apop();
                break;

            case InstructionConstants.OP_AASTORE:
                stack.apop();
                stack.ipop();
                stack.apop();
                break;

            case InstructionConstants.OP_POP:
                stack.pop1();
                break;

            case InstructionConstants.OP_POP2:
                stack.pop2();
                break;

            case InstructionConstants.OP_DUP:
                stack.dup();
                break;

            case InstructionConstants.OP_DUP_X1:
                stack.dup_x1();
                break;

            case InstructionConstants.OP_DUP_X2:
                stack.dup_x2();
                break;

            case InstructionConstants.OP_DUP2:
                stack.dup2();
                break;

            case InstructionConstants.OP_DUP2_X1:
                stack.dup2_x1();
                break;

            case InstructionConstants.OP_DUP2_X2:
                stack.dup2_x2();
                break;

            case InstructionConstants.OP_SWAP:
                stack.swap();
                break;

            case InstructionConstants.OP_IADD:
                stack.push(stack.ipop().add(stack.ipop()));
                break;

            case InstructionConstants.OP_LADD:
                stack.push(stack.lpop().add(stack.lpop()));
                break;

            case InstructionConstants.OP_FADD:
                stack.push(stack.fpop().add(stack.fpop()));
                break;

            case InstructionConstants.OP_DADD:
                stack.push(stack.dpop().add(stack.dpop()));
                break;

            case InstructionConstants.OP_ISUB:
                stack.push(stack.ipop().subtractFrom(stack.ipop()));
                break;

            case InstructionConstants.OP_LSUB:
                stack.push(stack.lpop().subtractFrom(stack.lpop()));
                break;

            case InstructionConstants.OP_FSUB:
                stack.push(stack.fpop().subtractFrom(stack.fpop()));
                break;

            case InstructionConstants.OP_DSUB:
                stack.push(stack.dpop().subtractFrom(stack.dpop()));
                break;

            case InstructionConstants.OP_IMUL:
                stack.push(stack.ipop().multiply(stack.ipop()));
                break;

            case InstructionConstants.OP_LMUL:
                stack.push(stack.lpop().multiply(stack.lpop()));
                break;

            case InstructionConstants.OP_FMUL:
                stack.push(stack.fpop().multiply(stack.fpop()));
                break;

            case InstructionConstants.OP_DMUL:
                stack.push(stack.dpop().multiply(stack.dpop()));
                break;

            case InstructionConstants.OP_IDIV:
                try
                {
                    stack.push(stack.ipop().divideOf(stack.ipop()));
                }
                catch (ArithmeticException ex)
                {
                    stack.push(IntegerValueFactory.create());
                    // TODO: Forward ArithmeticExceptions.
                    //stack.clear();
                    //stack.push(ReferenceValueFactory.create(false));
                    //branchUnit.throwException();
                }
                break;

            case InstructionConstants.OP_LDIV:
                stack.push(stack.lpop().divideOf(stack.lpop()));
                break;

            case InstructionConstants.OP_FDIV:
                stack.push(stack.fpop().divideOf(stack.fpop()));
                break;

            case InstructionConstants.OP_DDIV:
                stack.push(stack.dpop().divideOf(stack.dpop()));
                break;

            case InstructionConstants.OP_IREM:
                try
                {
                    stack.push(stack.ipop().remainderOf(stack.ipop()));
                }
                catch (ArithmeticException ex)
                {
                    stack.push(IntegerValueFactory.create());
                    // TODO: Forward ArithmeticExceptions.
                    //stack.clear();
                    //stack.push(ReferenceValueFactory.create(false));
                    //branchUnit.throwException();
                }
                break;

            case InstructionConstants.OP_LREM:
                stack.push(stack.lpop().remainderOf(stack.lpop()));
                break;

            case InstructionConstants.OP_FREM:
                stack.push(stack.fpop().remainderOf(stack.fpop()));
                break;

            case InstructionConstants.OP_DREM:
                stack.push(stack.dpop().remainderOf(stack.dpop()));
                break;

            case InstructionConstants.OP_INEG:
                stack.push(stack.ipop().negate());
                break;

            case InstructionConstants.OP_LNEG:
                stack.push(stack.lpop().negate());
                break;

            case InstructionConstants.OP_FNEG:
                stack.push(stack.fpop().negate());
                break;

            case InstructionConstants.OP_DNEG:
                stack.push(stack.dpop().negate());
                break;

            case InstructionConstants.OP_ISHL:
                stack.push(stack.ipop().shiftLeftOf(stack.ipop()));
                break;

            case InstructionConstants.OP_LSHL:
                stack.push(stack.ipop().shiftLeftOf(stack.lpop()));
                break;

            case InstructionConstants.OP_ISHR:
                stack.push(stack.ipop().shiftRightOf(stack.ipop()));
                break;

            case InstructionConstants.OP_LSHR:
                stack.push(stack.ipop().shiftRightOf(stack.lpop()));
                break;

            case InstructionConstants.OP_IUSHR:
                stack.push(stack.ipop().unsignedShiftRightOf(stack.ipop()));
                break;

            case InstructionConstants.OP_LUSHR:
                stack.push(stack.ipop().unsignedShiftRightOf(stack.lpop()));
                break;

            case InstructionConstants.OP_IAND:
                stack.push(stack.ipop().and(stack.ipop()));
                break;

            case InstructionConstants.OP_LAND:
                stack.push(stack.lpop().and(stack.lpop()));
                break;

            case InstructionConstants.OP_IOR:
                stack.push(stack.ipop().or(stack.ipop()));
                break;

            case InstructionConstants.OP_LOR:
                stack.push(stack.lpop().or(stack.lpop()));
                break;

            case InstructionConstants.OP_IXOR:
                stack.push(stack.ipop().xor(stack.ipop()));
                break;

            case InstructionConstants.OP_LXOR:
                stack.push(stack.lpop().xor(stack.lpop()));
                break;

            case InstructionConstants.OP_I2L:
                stack.push(stack.ipop().convertToLong());
                break;

            case InstructionConstants.OP_I2F:
                stack.push(stack.ipop().convertToFloat());
                break;

            case InstructionConstants.OP_I2D:
                stack.push(stack.ipop().convertToDouble());
                break;

            case InstructionConstants.OP_L2I:
                stack.push(stack.lpop().convertToInteger());
                break;

            case InstructionConstants.OP_L2F:
                stack.push(stack.lpop().convertToFloat());
                break;

            case InstructionConstants.OP_L2D:
                stack.push(stack.lpop().convertToDouble());
                break;

            case InstructionConstants.OP_F2I:
                stack.push(stack.fpop().convertToInteger());
                break;

            case InstructionConstants.OP_F2L:
                stack.push(stack.fpop().convertToLong());
                break;

            case InstructionConstants.OP_F2D:
                stack.push(stack.fpop().convertToDouble());
                break;

            case InstructionConstants.OP_D2I:
                stack.push(stack.dpop().convertToInteger());
                break;

            case InstructionConstants.OP_D2L:
                stack.push(stack.dpop().convertToLong());
                break;

            case InstructionConstants.OP_D2F:
                stack.push(stack.dpop().convertToFloat());
                break;

            case InstructionConstants.OP_I2B:
                stack.push(stack.ipop().convertToByte());
                break;

            case InstructionConstants.OP_I2C:
                stack.push(stack.ipop().convertToCharacter());
                break;

            case InstructionConstants.OP_I2S:
                stack.push(stack.ipop().convertToShort());
                break;

            case InstructionConstants.OP_LCMP:
                stack.push(stack.lpop().compareReverse(stack.lpop()));
                break;

            case InstructionConstants.OP_FCMPL:
                FloatValue floatValue1 = stack.fpop();
                FloatValue floatValue2 = stack.fpop();
                stack.push(floatValue2.compare(floatValue1));
                break;

            case InstructionConstants.OP_FCMPG:
                stack.push(stack.fpop().compareReverse(stack.fpop()));
                break;

            case InstructionConstants.OP_DCMPL:
                DoubleValue doubleValue1 = stack.dpop();
                DoubleValue doubleValue2 = stack.dpop();
                stack.push(doubleValue2.compare(doubleValue1));
                break;

            case InstructionConstants.OP_DCMPG:
                stack.push(stack.dpop().compareReverse(stack.dpop()));
                break;

            case InstructionConstants.OP_IRETURN:
                branchUnit.returnFromMethod(stack.ipop());
                break;

            case InstructionConstants.OP_LRETURN:
                branchUnit.returnFromMethod(stack.lpop());
                break;

            case InstructionConstants.OP_FRETURN:
                branchUnit.returnFromMethod(stack.fpop());
                break;

            case InstructionConstants.OP_DRETURN:
                branchUnit.returnFromMethod(stack.dpop());
                break;

            case InstructionConstants.OP_ARETURN:
                branchUnit.returnFromMethod(stack.apop());
                break;

            case InstructionConstants.OP_RETURN:
                branchUnit.returnFromMethod(null);
                break;

            case InstructionConstants.OP_NEWARRAY:
                stack.ipop();
                stack.push(ReferenceValueFactory.create(false));
                break;

            case InstructionConstants.OP_ARRAYLENGTH:
                stack.apop();
                stack.push(IntegerValueFactory.create());
                break;

            case InstructionConstants.OP_ATHROW:
                ReferenceValue exceptionReferenceValue = stack.apop();
                stack.clear();
                stack.push(exceptionReferenceValue);
                branchUnit.throwException();
                break;

            case InstructionConstants.OP_MONITORENTER:
            case InstructionConstants.OP_MONITOREXIT:
                stack.apop();
                break;

            default:
                throw new IllegalArgumentException("Unknown simple instruction ["+simpleInstruction.opcode+"]");
        }
    }


    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction)
    {
        int cpIndex = cpInstruction.cpIndex;

        switch (cpInstruction.opcode)
        {
            case InstructionConstants.OP_LDC:
            case InstructionConstants.OP_LDC_W:
            case InstructionConstants.OP_LDC2_W:
            case InstructionConstants.OP_GETSTATIC:
                stack.push(cpValue(classFile, cpIndex));
                break;

            case InstructionConstants.OP_PUTSTATIC:
                stack.pop();
                break;

            case InstructionConstants.OP_GETFIELD:
                stack.apop();
                stack.push(cpValue(classFile, cpIndex));
                break;

            case InstructionConstants.OP_PUTFIELD:
                stack.pop();
                stack.apop();
                break;

            case InstructionConstants.OP_INVOKEVIRTUAL:
            case InstructionConstants.OP_INVOKESPECIAL:
            case InstructionConstants.OP_INVOKESTATIC:
            case InstructionConstants.OP_INVOKEINTERFACE:
                Value cpValue        = cpValue(classFile, cpIndex);
                int   parameterCount = parameterCount(classFile, cpIndex);

                for (int counter = 0; counter < parameterCount; counter++)
                {
                    stack.pop();
                }
                if (cpInstruction.opcode != InstructionConstants.OP_INVOKESTATIC)
                {
                    stack.apop();
                }
                if (cpValue != null)
                {
                    stack.push(cpValue);
                }
                break;

            case InstructionConstants.OP_NEW:
                stack.push(cpValue(classFile, cpIndex));
                break;

            case InstructionConstants.OP_ANEWARRAY:
                stack.ipop();
                stack.push(ReferenceValueFactory.create(referencedClassFile(classFile, cpIndex),
                                                        1,
                                                        false));
                break;

            case InstructionConstants.OP_CHECKCAST:
                // TODO: Check cast.
                stack.push(stack.apop());
                break;

            case InstructionConstants.OP_INSTANCEOF:
                int instanceOf = stack.apop().instanceOf(referencedClassFile(classFile, cpIndex),
                                                         referencedTypeDimensionCount(classFile, cpIndex));

                stack.push(instanceOf == Value.NEVER  ? IntegerValueFactory.create(0) :
                           instanceOf == Value.ALWAYS ? IntegerValueFactory.create(1) :
                                                        IntegerValueFactory.create());
                break;

            case InstructionConstants.OP_MULTIANEWARRAY:
                int dimensionCount = cpInstruction.constant;
                for (int dimension = 0; dimension < dimensionCount; dimension++)
                {
                    stack.ipop();
                }
                stack.push(cpValue(classFile, cpIndex));
                break;

            default:
                throw new IllegalArgumentException("Unknown constant pool instruction ["+cpInstruction.opcode+"]");
        }
    }


    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction)
    {
        int variableIndex = variableInstruction.variableIndex;

        switch (variableInstruction.opcode)
        {
            case InstructionConstants.OP_ILOAD:
            case InstructionConstants.OP_ILOAD_0:
            case InstructionConstants.OP_ILOAD_1:
            case InstructionConstants.OP_ILOAD_2:
            case InstructionConstants.OP_ILOAD_3:
                stack.push(variables.iload(variableIndex));
                break;

            case InstructionConstants.OP_LLOAD:
            case InstructionConstants.OP_LLOAD_0:
            case InstructionConstants.OP_LLOAD_1:
            case InstructionConstants.OP_LLOAD_2:
            case InstructionConstants.OP_LLOAD_3:
                stack.push(variables.lload(variableIndex));
                break;

            case InstructionConstants.OP_FLOAD:
            case InstructionConstants.OP_FLOAD_0:
            case InstructionConstants.OP_FLOAD_1:
            case InstructionConstants.OP_FLOAD_2:
            case InstructionConstants.OP_FLOAD_3:
                stack.push(variables.fload(variableIndex));
                break;

            case InstructionConstants.OP_DLOAD:
            case InstructionConstants.OP_DLOAD_0:
            case InstructionConstants.OP_DLOAD_1:
            case InstructionConstants.OP_DLOAD_2:
            case InstructionConstants.OP_DLOAD_3:
                stack.push(variables.dload(variableIndex));
                break;

            case InstructionConstants.OP_ALOAD:
            case InstructionConstants.OP_ALOAD_0:
            case InstructionConstants.OP_ALOAD_1:
            case InstructionConstants.OP_ALOAD_2:
            case InstructionConstants.OP_ALOAD_3:
                stack.push(variables.aload(variableIndex));
                break;

            case InstructionConstants.OP_ISTORE:
            case InstructionConstants.OP_ISTORE_0:
            case InstructionConstants.OP_ISTORE_1:
            case InstructionConstants.OP_ISTORE_2:
            case InstructionConstants.OP_ISTORE_3:
                variables.store(variableIndex, stack.ipop());
                break;

            case InstructionConstants.OP_LSTORE:
            case InstructionConstants.OP_LSTORE_0:
            case InstructionConstants.OP_LSTORE_1:
            case InstructionConstants.OP_LSTORE_2:
            case InstructionConstants.OP_LSTORE_3:
                variables.store(variableIndex, stack.lpop());
                break;

            case InstructionConstants.OP_FSTORE:
            case InstructionConstants.OP_FSTORE_0:
            case InstructionConstants.OP_FSTORE_1:
            case InstructionConstants.OP_FSTORE_2:
            case InstructionConstants.OP_FSTORE_3:
                variables.store(variableIndex, stack.fpop());
                break;

            case InstructionConstants.OP_DSTORE:
            case InstructionConstants.OP_DSTORE_0:
            case InstructionConstants.OP_DSTORE_1:
            case InstructionConstants.OP_DSTORE_2:
            case InstructionConstants.OP_DSTORE_3:
                variables.store(variableIndex, stack.dpop());
                break;

            case InstructionConstants.OP_ASTORE:
            case InstructionConstants.OP_ASTORE_0:
            case InstructionConstants.OP_ASTORE_1:
            case InstructionConstants.OP_ASTORE_2:
            case InstructionConstants.OP_ASTORE_3:
                // The operand on the stack can be a reference or a return
                // address, so we'll relax the pop operation.
                //variables.store(variableIndex, stack.apop());
                variables.store(variableIndex, stack.pop());
                break;

            case InstructionConstants.OP_IINC:
                variables.store(variableIndex,
                                variables.iload(variableIndex).add(
                                IntegerValueFactory.create(variableInstruction.constant)));
                break;

            case InstructionConstants.OP_RET:
                // The return address should be in the last offset of the
                // given instruction offset variable (even though there may
                // be other offsets).
                InstructionOffsetValue instructionOffsetValue = variables.oload(variableIndex);
                branchUnit.branch(classFile,
                                  codeAttrInfo,
                                  offset,
                                  instructionOffsetValue.instructionOffset(instructionOffsetValue.instructionOffsetCount()-1));
                break;

            default:
                throw new IllegalArgumentException("Unknown variable instruction ["+variableInstruction.opcode+"]");
        }
    }


    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction)
    {
        int branchTarget = offset + branchInstruction.branchOffset;

        switch (branchInstruction.opcode)
        {
            case InstructionConstants.OP_IFEQ:
                branchUnit.branchConditionally(classFile, codeAttrInfo, offset, branchTarget,
                    stack.ipop().equal(IntegerValueFactory.create(0)));
                break;

            case InstructionConstants.OP_IFNE:
                branchUnit.branchConditionally(classFile, codeAttrInfo, offset, branchTarget,
                    stack.ipop().notEqual(IntegerValueFactory.create(0)));
                break;

            case InstructionConstants.OP_IFLT:
                branchUnit.branchConditionally(classFile, codeAttrInfo, offset, branchTarget,
                    stack.ipop().lessThan(IntegerValueFactory.create(0)));
                break;

            case InstructionConstants.OP_IFGE:
                branchUnit.branchConditionally(classFile, codeAttrInfo, offset, branchTarget,
                    stack.ipop().greaterThanOrEqual(IntegerValueFactory.create(0)));
                break;

            case InstructionConstants.OP_IFGT:
                branchUnit.branchConditionally(classFile, codeAttrInfo, offset, branchTarget,
                    stack.ipop().greaterThan(IntegerValueFactory.create(0)));
                break;

            case InstructionConstants.OP_IFLE:
                branchUnit.branchConditionally(classFile, codeAttrInfo, offset, branchTarget,
                    stack.ipop().lessThanOrEqual(IntegerValueFactory.create(0)));
                break;


            case InstructionConstants.OP_IFICMPEQ:
                branchUnit.branchConditionally(classFile, codeAttrInfo, offset, branchTarget,
                    stack.ipop().equal(stack.ipop()));
                break;

            case InstructionConstants.OP_IFICMPNE:
                branchUnit.branchConditionally(classFile, codeAttrInfo, offset, branchTarget,
                    stack.ipop().notEqual(stack.ipop()));
                break;


            case InstructionConstants.OP_IFICMPLT:
                branchUnit.branchConditionally(classFile, codeAttrInfo, offset, branchTarget,
                    -stack.ipop().lessThan(stack.ipop()));
                break;

            case InstructionConstants.OP_IFICMPGE:
                branchUnit.branchConditionally(classFile, codeAttrInfo, offset, branchTarget,
                    -stack.ipop().greaterThanOrEqual(stack.ipop()));
                break;

            case InstructionConstants.OP_IFICMPGT:
                branchUnit.branchConditionally(classFile, codeAttrInfo, offset, branchTarget,
                    -stack.ipop().greaterThan(stack.ipop()));
                break;

            case InstructionConstants.OP_IFICMPLE:
                branchUnit.branchConditionally(classFile, codeAttrInfo, offset, branchTarget,
                    -stack.ipop().lessThanOrEqual(stack.ipop()));
                break;

            case InstructionConstants.OP_IFACMPEQ:
                branchUnit.branchConditionally(classFile, codeAttrInfo, offset, branchTarget,
                    stack.apop().equal(stack.apop()));
                break;

            case InstructionConstants.OP_IFACMPNE:
                branchUnit.branchConditionally(classFile, codeAttrInfo, offset, branchTarget,
                    stack.apop().notEqual(stack.apop()));
                break;

            case InstructionConstants.OP_GOTO:
            case InstructionConstants.OP_GOTO_W:
                branchUnit.branch(classFile, codeAttrInfo, offset, branchTarget);
                break;


            case InstructionConstants.OP_JSR:
            case InstructionConstants.OP_JSR_W:
                stack.push(InstructionOffsetValueFactory.create(offset +
                                                                branchInstruction.length(offset)));
                branchUnit.branch(classFile, codeAttrInfo, offset, branchTarget);
                break;

            case InstructionConstants.OP_IFNULL:
                branchUnit.branchConditionally(classFile, codeAttrInfo, offset, branchTarget,
                    stack.apop().isNull());
                break;

            case InstructionConstants.OP_IFNONNULL:
                branchUnit.branchConditionally(classFile, codeAttrInfo, offset, branchTarget,
                    stack.apop().isNotNull());
                break;

            default:
                throw new IllegalArgumentException("Unknown branch instruction ["+branchInstruction.opcode+"]");
        }
    }


    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction)
    {
        IntegerValue indexValue = stack.ipop();

        // If there is no definite branch in any of the cases below,
        // branch to the default offset.
        branchUnit.branch(classFile, codeAttrInfo,
                          offset,
                          offset + tableSwitchInstruction.defaultOffset);

        for (int index = 0; index < tableSwitchInstruction.jumpOffsetCount; index++)
        {
            int conditional = indexValue.equal(IntegerValueFactory.create(
                tableSwitchInstruction.lowCase + index));
            branchUnit.branchConditionally(classFile, codeAttrInfo,
                                           offset,
                                           offset + tableSwitchInstruction.jumpOffsets[index],
                                           conditional);

            // If this branch is always taken, we can skip the rest.
            if (conditional == Value.ALWAYS)
            {
                break;
            }
        }
    }


    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        IntegerValue indexValue = stack.ipop();

        // If there is no definite branch in any of the cases below,
        // branch to the default offset.
        branchUnit.branch(classFile, codeAttrInfo,
                          offset,
                          offset + lookUpSwitchInstruction.defaultOffset);

        for (int index = 0; index < lookUpSwitchInstruction.jumpOffsetCount; index++)
        {
            int conditional = indexValue.equal(IntegerValueFactory.create(
                lookUpSwitchInstruction.cases[index]));
            branchUnit.branchConditionally(classFile, codeAttrInfo,
                                           offset,
                                           offset + lookUpSwitchInstruction.jumpOffsets[index],
                                           conditional);

            // If this branch is always taken, we can skip the rest.
            if (conditional == Value.ALWAYS)
            {
                break;
            }
        }
    }


    // Implementations for CpInfoVisitor.

    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}


    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo)
    {
        cpValue = IntegerValueFactory.create(integerCpInfo.getValue());
    }

    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo)
    {
        cpValue = LongValueFactory.create(longCpInfo.getValue());
    }

    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo)
    {
        cpValue = FloatValueFactory.create(floatCpInfo.getValue());
    }

    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo)
    {
        cpValue = DoubleValueFactory.create(doubleCpInfo.getValue());
    }

    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo)
    {
        cpValue = ReferenceValueFactory.create(false);
    }

    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo)
    {
        cpValue = ValueFactory.create(fieldrefCpInfo.getType(classFile));
    }

    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo)
    {
        visitRefCpInfo(classFile, interfaceMethodrefCpInfo);
    }

    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo)
    {
        visitRefCpInfo(classFile, methodrefCpInfo);
    }

    private void visitRefCpInfo(ClassFile classFile, RefCpInfo methodrefCpInfo)
    {
        String type = methodrefCpInfo.getType(classFile);

        parameterCount = ClassUtil.internalMethodParameterCount(type);
        cpValue        = ValueFactory.create(ClassUtil.internalMethodReturnType(type));
    }

    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        String className = classCpInfo.getName(classFile);

        referencedClassFile          = classCpInfo.referencedClassFile;
        referencedTypeDimensionCount = ClassUtil.internalArrayTypeDimensionCount(className);

        cpValue = ReferenceValueFactory.create(referencedClassFile,
                                               referencedTypeDimensionCount,
                                               false);
    }


    // Small utility methods.

    /**
     * Returns the Value of the constant pool element at the given index.
     * The element can be a constant, a field, a method,...
     */
    private Value cpValue(ClassFile classFile, int cpIndex)
    {
        // Visit the constant pool entry to get its return value.
        classFile.constantPoolEntryAccept(cpIndex, this);

        return cpValue;
    }


    /**
     * Returns the class file referenced by the class constant pool entry at the
     * given index.
     */
    private ClassFile referencedClassFile(ClassFile classFile, int cpIndex)
    {
        // Visit the constant pool entry to get its referenced class file.
        classFile.constantPoolEntryAccept(cpIndex, this);

        return referencedClassFile;
    }


    /**
     * Returns the dimensionality of the class constant pool entry at the given
     * index.
     */
    private int referencedTypeDimensionCount(ClassFile classFile, int cpIndex)
    {
        // Visit the constant pool entry to get its referenced class file.
        //classFile.constantPoolEntryAccept(this, cpIndex);

        // We'll return the value that was just computed.
        return referencedTypeDimensionCount;
    }


    /**
     * Returns the number of parameters of the method reference at the given
     * constant pool index. This method must be invoked right after the
     * cpValue(ClassFile,int) method.
     */
    private int parameterCount(ClassFile classFile, int methodRefCpIndex)
    {
        // Visit the method ref constant pool entry to get its parameter count.
        //classFile.constantPoolEntryAccept(this, methodRefCpIndex);

        // We'll return the value that was just computed.
        return parameterCount;
    }
}
