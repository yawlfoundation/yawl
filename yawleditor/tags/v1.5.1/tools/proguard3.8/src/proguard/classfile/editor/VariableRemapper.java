/* $Id: VariableRemapper.java,v 1.4.2.2 2007/01/18 21:31:51 eric Exp $
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
package proguard.classfile.editor;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.instruction.*;

/**
 * This AttrInfoVisitor remaps variable indexes in all attributes that it
 * visits, based on a given index map.
 *
 * @author Eric Lafortune
 */
public class VariableRemapper
  implements AttrInfoVisitor,
             InstructionVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor
{
    private CodeAttrInfoEditor codeAttrInfoEditor;

    private int[] variableMap;


    /**
     * Creates a new VariableRemapper.
     * @param codeLength an estimate of the maximum length of all the code that
     *                   will be edited.
     */
    public VariableRemapper(int codeLength)
    {
        codeAttrInfoEditor = new CodeAttrInfoEditor(codeLength);
    }


    /**
     * Sets the given mapping of old variable indexes to their new indexes.
     * Variables that should disappear can be mapped to -1.
     */
    public void setVariableMap(int[] variableMap)
    {
        this.variableMap = variableMap;
    }


    // Implementations for AttrInfoVisitor.

    public void visitUnknownAttrInfo(ClassFile classFile, UnknownAttrInfo unknownAttrInfo) {}
    public void visitInnerClassesAttrInfo(ClassFile classFile, InnerClassesAttrInfo innerClassesAttrInfo) {}
    public void visitEnclosingMethodAttrInfo(ClassFile classFile, EnclosingMethodAttrInfo enclosingMethodAttrInfo) {}
    public void visitConstantValueAttrInfo(ClassFile classFile, FieldInfo fieldInfo, ConstantValueAttrInfo constantValueAttrInfo) {}
    public void visitExceptionsAttrInfo(ClassFile classFile, MethodInfo methodInfo, ExceptionsAttrInfo exceptionsAttrInfo) {}
    public void visitLineNumberTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LineNumberTableAttrInfo lineNumberTableAttrInfo) {}
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
        // Initially, the code attribute editor doesn't contain any changes.
        codeAttrInfoEditor.reset(codeAttrInfo.u4codeLength);

        // Remap the variables of the instructions.
        codeAttrInfo.instructionsAccept(classFile, methodInfo, this);

        // Apply the code atribute editor.
        codeAttrInfoEditor.visitCodeAttrInfo(classFile, methodInfo, codeAttrInfo);

        // Remap the variables of the attributes.
        codeAttrInfo.attributesAccept(classFile, methodInfo, this);
    }


    public void visitLocalVariableTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTableAttrInfo localVariableTableAttrInfo)
    {
        // Remap the variable references of the local variables.
        localVariableTableAttrInfo.localVariablesAccept(classFile, methodInfo, codeAttrInfo, this);

        // Remove local variables that haven't been mapped.
        localVariableTableAttrInfo.u2localVariableTableLength =
            removeEmptyLocalVariables(localVariableTableAttrInfo.localVariableTable,
                                      localVariableTableAttrInfo.u2localVariableTableLength);
    }


    public void visitLocalVariableTypeTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeTableAttrInfo localVariableTypeTableAttrInfo)
    {
        // Remap the variable references of the local variables.
        localVariableTypeTableAttrInfo.localVariablesAccept(classFile, methodInfo, codeAttrInfo, this);

        // Remove local variables that haven't been mapped.
        localVariableTypeTableAttrInfo.u2localVariableTypeTableLength =
            removeEmptyLocalVariableTypes(localVariableTypeTableAttrInfo.localVariableTypeTable,
                                          localVariableTypeTableAttrInfo.u2localVariableTypeTableLength);
    }


    // Implementations for LocalVariableInfoVisitor.

    public void visitLocalVariableInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableInfo localVariableInfo)
    {
        localVariableInfo.u2index =
            remapVariable(localVariableInfo.u2index);
    }


    // Implementations for LocalVariableTypeInfoVisitor.

    public void visitLocalVariableTypeInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeInfo localVariableTypeInfo)
    {
        localVariableTypeInfo.u2index =
            remapVariable(localVariableTypeInfo.u2index);
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction) {}
    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction) {}
    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction) {}
    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction) {}
    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction) {}


    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction)
    {
        // Is the new variable index different from the original one?
        int oldVariableIndex = variableInstruction.variableIndex;
        int newVariableIndex = remapVariable(oldVariableIndex);
        if (newVariableIndex != oldVariableIndex)
        {
            // Replace the instruction.
            variableInstruction = new VariableInstruction().copy(variableInstruction);
            variableInstruction.variableIndex = newVariableIndex;
            variableInstruction.shrink();

            codeAttrInfoEditor.replaceInstruction(offset, variableInstruction);
        }
    }


    // Small utility methods.

    /**
     * Returns the new variable index of the given variable.
     */
    private int remapVariable(int variableIndex)
    {
        return variableMap[variableIndex];
    }


    /**
     * Returns the given list of local variables, without the ones that have
     * been removed.
     */
    private int removeEmptyLocalVariables(LocalVariableInfo[] localVariableInfos,
                                          int                 localVariableInfoCount)
    {
        // Overwrite all empty local variable entries.
        int newIndex = 0;
        for (int index = 0; index < localVariableInfoCount; index++)
        {
            LocalVariableInfo localVariableInfo = localVariableInfos[index];
            if (localVariableInfo.u2index >= 0)
            {
                localVariableInfos[newIndex++] = localVariableInfo;
            }
        }

        return newIndex;
    }


    /**
     * Returns the given list of local variable types, without the ones that
     * have been removed.
     */
    private int removeEmptyLocalVariableTypes(LocalVariableTypeInfo[] localVariableTypeInfos,
                                              int                     localVariableTypeInfoCount)
    {
        // Overwrite all empty local variable type entries.
        int newIndex = 0;
        for (int index = 0; index < localVariableTypeInfoCount; index++)
        {
            LocalVariableTypeInfo localVariableTypeInfo = localVariableTypeInfos[index];
            if (localVariableTypeInfo.u2index >= 0)
            {
                localVariableTypeInfos[newIndex++] = localVariableTypeInfo;
            }
        }

        return newIndex;
    }
}
