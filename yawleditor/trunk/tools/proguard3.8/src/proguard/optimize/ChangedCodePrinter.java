/* $Id: ChangedCodePrinter.java,v 1.7.2.2 2007/01/18 21:31:53 eric Exp $
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
package proguard.optimize;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.util.ClassUtil;
import proguard.classfile.visitor.*;

/**
 * This AttrInfoVisitor delegates its call to another AttrInfoVisitor, and
 * prints out the code if the other visitor has changed it.
 *
 * @author Eric Lafortune
 */
public class ChangedCodePrinter implements AttrInfoVisitor
{
    private AttrInfoVisitor attrInfoVisitor;


    public ChangedCodePrinter(AttrInfoVisitor attrInfoVisitor)
    {
        this.attrInfoVisitor = attrInfoVisitor;
    }


    // Implementations for AttrInfoVisitor.

    public void visitUnknownAttrInfo(ClassFile classFile, UnknownAttrInfo unknownAttrInfo)
    {
      attrInfoVisitor.visitUnknownAttrInfo(classFile, unknownAttrInfo);
    }

    public void visitInnerClassesAttrInfo(ClassFile classFile, InnerClassesAttrInfo innerClassesAttrInfo)
    {
      attrInfoVisitor.visitInnerClassesAttrInfo(classFile, innerClassesAttrInfo);
    }

    public void visitEnclosingMethodAttrInfo(ClassFile classFile, EnclosingMethodAttrInfo enclosingMethodAttrInfo)
    {
        attrInfoVisitor.visitEnclosingMethodAttrInfo(classFile, enclosingMethodAttrInfo);
    }

    public void visitConstantValueAttrInfo(ClassFile classFile, FieldInfo fieldInfo, ConstantValueAttrInfo constantValueAttrInfo)
    {
      attrInfoVisitor.visitConstantValueAttrInfo(classFile, fieldInfo, constantValueAttrInfo);
    }

    public void visitExceptionsAttrInfo(ClassFile classFile, MethodInfo methodInfo, ExceptionsAttrInfo exceptionsAttrInfo)
    {
      attrInfoVisitor.visitExceptionsAttrInfo(classFile, methodInfo, exceptionsAttrInfo);
    }

    public void visitLineNumberTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LineNumberTableAttrInfo lineNumberTableAttrInfo)
    {
      attrInfoVisitor.visitLineNumberTableAttrInfo(classFile, methodInfo, codeAttrInfo, lineNumberTableAttrInfo);
    }

    public void visitLocalVariableTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTableAttrInfo localVariableTableAttrInfo)
    {
      attrInfoVisitor.visitLocalVariableTableAttrInfo(classFile, methodInfo, codeAttrInfo, localVariableTableAttrInfo);
    }

    public void visitLocalVariableTypeTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeTableAttrInfo localVariableTypeTableAttrInfo)
    {
        attrInfoVisitor.visitLocalVariableTypeTableAttrInfo(classFile, methodInfo, codeAttrInfo, localVariableTypeTableAttrInfo);
    }

    public void visitSourceFileAttrInfo(ClassFile classFile, SourceFileAttrInfo sourceFileAttrInfo)
    {
      attrInfoVisitor.visitSourceFileAttrInfo(classFile, sourceFileAttrInfo);
    }

    public void visitSourceDirAttrInfo(ClassFile classFile, SourceDirAttrInfo sourceDirAttrInfo)
    {
      attrInfoVisitor.visitSourceDirAttrInfo(classFile, sourceDirAttrInfo);
    }

    public void visitDeprecatedAttrInfo(ClassFile classFile, DeprecatedAttrInfo deprecatedAttrInfo)
    {
      attrInfoVisitor.visitDeprecatedAttrInfo(classFile, deprecatedAttrInfo);
    }

    public void visitSyntheticAttrInfo(ClassFile classFile, SyntheticAttrInfo syntheticAttrInfo)
    {
      attrInfoVisitor.visitSyntheticAttrInfo(classFile, syntheticAttrInfo);
    }

    public void visitSignatureAttrInfo(ClassFile classFile, SignatureAttrInfo signatureAttrInfo)
    {
      attrInfoVisitor.visitSignatureAttrInfo(classFile, signatureAttrInfo);
    }

    public void visitRuntimeVisibleAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleAnnotationsAttrInfo runtimeVisibleAnnotationsAttrInfo)
    {
        attrInfoVisitor.visitRuntimeVisibleAnnotationAttrInfo(classFile, runtimeVisibleAnnotationsAttrInfo);
    }

    public void visitRuntimeInvisibleAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleAnnotationsAttrInfo runtimeInvisibleAnnotationsAttrInfo)
    {
        attrInfoVisitor.visitRuntimeInvisibleAnnotationAttrInfo(classFile, runtimeInvisibleAnnotationsAttrInfo);
    }

    public void visitRuntimeVisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleParameterAnnotationsAttrInfo runtimeVisibleParameterAnnotationsAttrInfo)
    {
        attrInfoVisitor.visitRuntimeVisibleParameterAnnotationAttrInfo(classFile, runtimeVisibleParameterAnnotationsAttrInfo);
    }

    public void visitRuntimeInvisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleParameterAnnotationsAttrInfo runtimeInvisibleParameterAnnotationsAttrInfo)
    {
        attrInfoVisitor.visitRuntimeInvisibleParameterAnnotationAttrInfo(classFile, runtimeInvisibleParameterAnnotationsAttrInfo);
    }

    public void visitAnnotationDefaultAttrInfo(ClassFile classFile, AnnotationDefaultAttrInfo annotationDefaultAttrInfo)
    {
        attrInfoVisitor.visitAnnotationDefaultAttrInfo(classFile, annotationDefaultAttrInfo);
    }



    public void visitCodeAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo)
    {
        byte[] code    = codeAttrInfo.code;
        byte[] oldCode = new byte[code.length];

        // Copy the current code.
        for (int index = 0; index < codeAttrInfo.u4codeLength; index++)
        {
            oldCode[index] = code[index];
        }

        // Delegate to the real visitor.
        attrInfoVisitor.visitCodeAttrInfo(classFile, methodInfo, codeAttrInfo);

        // Check if the code has changed.
        if (codeHasChanged(codeAttrInfo, oldCode))
        {
            printChangedCode(classFile, methodInfo, codeAttrInfo, oldCode);
        }
    }


    // Small utility methods.

    private boolean codeHasChanged(CodeAttrInfo codeAttrInfo, byte[] oldCode)
    {
        if (oldCode.length != codeAttrInfo.u4codeLength)
        {
            return true;
        }

        for (int index = 0; index < codeAttrInfo.u4codeLength; index++)
        {
            if (oldCode[index] != codeAttrInfo.code[index])
            {
                return true;
            }
        }

        return false;
    }


    private void printChangedCode(ClassFile    classFile,
                                  MethodInfo   methodInfo,
                                  CodeAttrInfo codeAttrInfo,
                                  byte[]       oldCode)
    {
        System.out.println("Class "+ClassUtil.externalClassName(classFile.getName()));
        System.out.println("Method "+ClassUtil.externalFullMethodDescription(classFile.getName(),
                                                                             0,
                                                                             methodInfo.getName(classFile),
                                                                             methodInfo.getDescriptor(classFile)));

        for (int index = 0; index < codeAttrInfo.u4codeLength; index++)
        {
            System.out.println(
                (oldCode[index] == codeAttrInfo.code[index]? "  -- ":"  => ")+
                index+": "+
                Integer.toHexString(0x100|oldCode[index]          &0xff).substring(1)+" "+
                Integer.toHexString(0x100|codeAttrInfo.code[index]&0xff).substring(1));
        }
    }
}
