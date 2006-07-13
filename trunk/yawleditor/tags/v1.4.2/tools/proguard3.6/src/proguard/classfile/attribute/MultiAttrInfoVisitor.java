/* $Id: MultiAttrInfoVisitor.java,v 1.2.2.1 2006/01/16 22:57:55 eric Exp $
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
package proguard.classfile.attribute;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;

/**
 * This AttrInfoVisitor delegates all visits to each AttrInfoVisitor
 * in a given list.
 *
 * @author Eric Lafortune
 */
public class MultiAttrInfoVisitor implements AttrInfoVisitor
{
    private static final int ARRAY_SIZE_INCREMENT = 5;

    private AttrInfoVisitor[] attrInfoVisitors;
    private int               attrInfoVisitorCount;


    public MultiAttrInfoVisitor()
    {
    }


    public MultiAttrInfoVisitor(AttrInfoVisitor[] attrInfoVisitors)
    {
        this.attrInfoVisitors     = attrInfoVisitors;
        this.attrInfoVisitorCount = attrInfoVisitors.length;
    }


    public void addAttrInfoVisitor(AttrInfoVisitor attrInfoVisitor)
    {
        ensureArraySize();

        attrInfoVisitors[attrInfoVisitorCount++] = attrInfoVisitor;
    }


    private void ensureArraySize()
    {
        if (attrInfoVisitors == null)
        {
            attrInfoVisitors = new AttrInfoVisitor[ARRAY_SIZE_INCREMENT];
        }
        else if (attrInfoVisitors.length == attrInfoVisitorCount)
        {
            AttrInfoVisitor[] newAttrInfoVisitors =
                new AttrInfoVisitor[attrInfoVisitorCount +
                                     ARRAY_SIZE_INCREMENT];
            System.arraycopy(attrInfoVisitors, 0,
                             newAttrInfoVisitors, 0,
                             attrInfoVisitorCount);
            attrInfoVisitors = newAttrInfoVisitors;
        }
    }


    // Implementations for AttrInfoVisitor.

    public void visitUnknownAttrInfo(ClassFile classFile, UnknownAttrInfo unknownAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitUnknownAttrInfo(classFile, unknownAttrInfo);
        }
    }

    public void visitInnerClassesAttrInfo(ClassFile classFile, InnerClassesAttrInfo innerClassesAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitInnerClassesAttrInfo(classFile, innerClassesAttrInfo);
        }
    }

    public void visitEnclosingMethodAttrInfo(ClassFile classFile, EnclosingMethodAttrInfo enclosingMethodAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitEnclosingMethodAttrInfo(classFile, enclosingMethodAttrInfo);
        }
    }


    public void visitConstantValueAttrInfo(ClassFile classFile, FieldInfo fieldInfo, ConstantValueAttrInfo constantValueAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitConstantValueAttrInfo(classFile, fieldInfo, constantValueAttrInfo);
        }
    }

    public void visitExceptionsAttrInfo(ClassFile classFile, MethodInfo methodInfo, ExceptionsAttrInfo exceptionsAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitExceptionsAttrInfo(classFile, methodInfo, exceptionsAttrInfo);
        }
    }

    public void visitCodeAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitCodeAttrInfo(classFile, methodInfo, codeAttrInfo);
        }
    }

    public void visitLineNumberTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LineNumberTableAttrInfo lineNumberTableAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitLineNumberTableAttrInfo(classFile, methodInfo, codeAttrInfo, lineNumberTableAttrInfo);
        }
    }

    public void visitLocalVariableTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTableAttrInfo localVariableTableAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitLocalVariableTableAttrInfo(classFile, methodInfo, codeAttrInfo, localVariableTableAttrInfo);
        }
    }

    public void visitLocalVariableTypeTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeTableAttrInfo localVariableTypeTableAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitLocalVariableTypeTableAttrInfo(classFile, methodInfo, codeAttrInfo, localVariableTypeTableAttrInfo);
        }
    }

    public void visitSourceFileAttrInfo(ClassFile classFile, SourceFileAttrInfo sourceFileAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitSourceFileAttrInfo(classFile, sourceFileAttrInfo);
        }
    }

    public void visitSourceDirAttrInfo(ClassFile classFile, SourceDirAttrInfo sourceDirAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitSourceDirAttrInfo(classFile, sourceDirAttrInfo);
        }
    }

    public void visitDeprecatedAttrInfo(ClassFile classFile, DeprecatedAttrInfo deprecatedAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitDeprecatedAttrInfo(classFile, deprecatedAttrInfo);
        }
    }

    public void visitSyntheticAttrInfo(ClassFile classFile, SyntheticAttrInfo syntheticAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitSyntheticAttrInfo(classFile, syntheticAttrInfo);
        }
    }

    public void visitSignatureAttrInfo(ClassFile classFile, SignatureAttrInfo syntheticAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitSignatureAttrInfo(classFile, syntheticAttrInfo);
        }
    }

    public void visitRuntimeVisibleAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleAnnotationsAttrInfo runtimeVisibleAnnotationsAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitRuntimeVisibleAnnotationAttrInfo(classFile, runtimeVisibleAnnotationsAttrInfo);
        }
    }

    public void visitRuntimeInvisibleAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleAnnotationsAttrInfo runtimeInvisibleAnnotationsAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitRuntimeInvisibleAnnotationAttrInfo(classFile, runtimeInvisibleAnnotationsAttrInfo);
        }
    }

    public void visitRuntimeVisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleParameterAnnotationsAttrInfo runtimeVisibleParameterAnnotationsAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitRuntimeVisibleParameterAnnotationAttrInfo(classFile, runtimeVisibleParameterAnnotationsAttrInfo);
        }
    }

    public void visitRuntimeInvisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleParameterAnnotationsAttrInfo runtimeInvisibleParameterAnnotationsAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitRuntimeInvisibleParameterAnnotationAttrInfo(classFile, runtimeInvisibleParameterAnnotationsAttrInfo);
        }
    }

    public void visitAnnotationDefaultAttrInfo(ClassFile classFile, AnnotationDefaultAttrInfo annotationDefaultAttrInfo)
    {
        for (int index = 0; index < attrInfoVisitorCount; index++)
        {
            attrInfoVisitors[index].visitAnnotationDefaultAttrInfo(classFile, annotationDefaultAttrInfo);
        }
    }
}