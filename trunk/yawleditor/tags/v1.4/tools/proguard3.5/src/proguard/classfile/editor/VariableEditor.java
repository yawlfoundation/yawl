/* $Id: VariableEditor.java,v 1.5.2.2 2006/01/16 22:57:55 eric Exp $
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
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.instruction.*;
import proguard.classfile.visitor.*;

/**
 * This AttrInfoVisitor accumulates specified changes to local variables, and
 * then applies these accumulated changes to the code attributes that it visits.
 *
 * @author Eric Lafortune
 */
public class VariableEditor
  implements AttrInfoVisitor
{
    private VariableRemapper variableRemapper;

    private boolean          modified;

    private boolean[]        deleted;

    private int[]            variableMap;


    /**
     * Creates a new VariableEditor.
     * @param codeLength an estimate of the maximum length of all the code
     *                   that will be edited.
     * @param maxLocals  an estimate of the maximum length of all the local
     *                   variable frames that will be edited.
     */
    public VariableEditor(int codeLength, int maxLocals)
    {
        variableRemapper = new VariableRemapper(codeLength);
        deleted          = new boolean[maxLocals];
        variableMap      = new int[maxLocals];
    }


    /**
     * Resets the accumulated code changes.
     * @param maxLocals the length of the local variable frame that will be
     *                  edited next.
     */
    public void reset(int maxLocals)
    {
        // Try to reuse the previous array.
        if (deleted.length < maxLocals)
        {
            deleted = new boolean[maxLocals];
        }
        else
        {
            for (int index = 0; index < maxLocals; index++)
            {
                deleted[index] = false;
            }
        }

        modified = false;
    }




    /**
     * Remembers to delete the given variable.
     * @param variableIndex the index of the variable to be deleted.
     */
    public void deleteVariable(int variableIndex)
    {
        deleted[variableIndex] = true;

        modified = true;
    }


    /**
     * Returns whether the given variable at the given offset has deleted.
     */
    public boolean isDeleted(int instructionOffset)
    {
        return deleted[instructionOffset];
    }


    /**
     * Returns whether any oarameter has been modified.
     */
    public boolean isModified()
    {
        return modified;
    }


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
        // Avoid doing any work if nothing is changing anyway.
        if (!modified)
        {
            return;
        }

        int oldMaxLocals = codeAttrInfo.u2maxLocals;

        // Make sure there is a sufficiently large variable map.
        if (variableMap.length < oldMaxLocals)
        {
            variableMap = new int[oldMaxLocals];
        }

        // Fill out the variable map.
        int newVariableIndex = 0;
        for (int oldVariableIndex = 0; oldVariableIndex < oldMaxLocals; oldVariableIndex++)
        {
            if (oldVariableIndex >= deleted.length ||
                !deleted[oldVariableIndex])
            {
                variableMap[oldVariableIndex] = newVariableIndex++;
            }
            else
            {
                variableMap[oldVariableIndex] = -1;
            }
        }

        // Set the map.
        variableRemapper.setVariableMap(variableMap);

        // Remap the variables.
        variableRemapper.visitCodeAttrInfo(classFile, methodInfo, codeAttrInfo);

        // Update the length of local variable frame.
        codeAttrInfo.u2maxLocals = newVariableIndex;
    }
}
