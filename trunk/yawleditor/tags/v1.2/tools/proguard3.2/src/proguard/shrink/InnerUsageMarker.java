/* $Id: InnerUsageMarker.java,v 1.15 2004/12/11 16:35:23 eric Exp $
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
package proguard.shrink;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.visitor.*;


/**
 * This ClassFileVisitor recursively marks all inner classes
 * that are being used in the classes it visits.
 *
 * @see UsageMarker
 *
 * @author Eric Lafortune
 */
public class InnerUsageMarker
  implements ClassFileVisitor,
             CpInfoVisitor,
             AttrInfoVisitor,
             InnerClassesInfoVisitor
{
    private boolean markingAttributes = true;
    private boolean used;


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        boolean classUsed = UsageMarker.isUsed(programClassFile);

        if (markingAttributes && classUsed)
        {
            markingAttributes = false;

            // Check the inner class attribute.
            programClassFile.attributesAccept(this);

            markingAttributes = true;
        }

        // The return value.
        used = classUsed;
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        // The return value.
        used = true;
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo) {}
    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo) {}
    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo) {}
    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}


    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        boolean classUsed = UsageMarker.isUsed(classCpInfo);

        if (!classUsed)
        {
            // The ClassCpInfo isn't marked as being used yet. But maybe it should
            // be included as an interface, so check the actual class.
            classCpInfo.referencedClassAccept(this);
            classUsed = used;

            if (classUsed)
            {
                // The class is being used. Mark the ClassCpInfo as being used
                // as well.
                UsageMarker.markAsUsed(classCpInfo);

                markCpEntry(classFile, classCpInfo.u2nameIndex);
            }
        }

        // The return value.
        used = classUsed;
    }


    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo)
    {
        if (!UsageMarker.isUsed(utf8CpInfo))
        {
            UsageMarker.markAsUsed(utf8CpInfo);
        }
    }


    // Implementations for AttrInfoVisitor.

    public void visitUnknownAttrInfo(ClassFile classFile, UnknownAttrInfo unknownAttrInfo) {}
    public void visitEnclosingMethodAttrInfo(ClassFile classFile, EnclosingMethodAttrInfo enclosingMethodAttrInfo) {}
    public void visitConstantValueAttrInfo(ClassFile classFile, FieldInfo fieldInfo, ConstantValueAttrInfo constantValueAttrInfo) {}
    public void visitExceptionsAttrInfo(ClassFile classFile, MethodInfo methodInfo, ExceptionsAttrInfo exceptionsAttrInfo) {}
    public void visitCodeAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo) {}
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


    public void visitInnerClassesAttrInfo(ClassFile classFile, InnerClassesAttrInfo innerClassesAttrInfo)
    {
        boolean attributeUsed = false;

        // Mark the interfaces that are being used.
        for (int i = 0; i < innerClassesAttrInfo.u2numberOfClasses; i++)
        {
            // Check if the inner class entry is used.
            visitInnerClassesInfo(classFile, innerClassesAttrInfo.classes[i]);
            attributeUsed |= used;
        }

        if (attributeUsed)
        {
            // We got a positive used flag, so some inner class is being used.
            // Mark this attribute as being used as well.
            UsageMarker.markAsUsed(innerClassesAttrInfo);

            markCpEntry(classFile, innerClassesAttrInfo.u2attrNameIndex);
        }
    }


    // Implementations for InnerClassesInfoVisitor.

    public void visitInnerClassesInfo(ClassFile classFile, InnerClassesInfo innerClassesInfo)
    {
        boolean innerClassesInfoUsed = UsageMarker.isUsed(innerClassesInfo);

        if (!innerClassesInfoUsed)
        {
            int u2innerClassInfoIndex = innerClassesInfo.u2innerClassInfoIndex;
            int u2outerClassInfoIndex = innerClassesInfo.u2outerClassInfoIndex;
            int u2innerNameIndex      = innerClassesInfo.u2innerNameIndex;

            innerClassesInfoUsed = true;

            if (u2innerClassInfoIndex != 0)
            {
                // Check if the inner class is marked as being used.
                markCpEntry(classFile, u2innerClassInfoIndex);
                innerClassesInfoUsed &= used;
            }

            if (u2outerClassInfoIndex != 0)
            {
                // Check if the outer class is marked as being used.
                markCpEntry(classFile, u2outerClassInfoIndex);
                innerClassesInfoUsed &= used;
            }

            // If both the inner class and the outer class are marked as being
            // used, then mark this InnerClassesInfo as well.
            if (innerClassesInfoUsed)
            {
                UsageMarker.markAsUsed(innerClassesInfo);

                if (u2innerNameIndex != 0)
                {
                    markCpEntry(classFile, u2innerNameIndex);
                }
            }
        }

        // The return value.
        used = innerClassesInfoUsed;
    }


    // Small utility methods.

    /**
     * Marks the given constant pool entry of the given class. This includes
     * visiting any other referenced constant pool entries.
     */
    private void markCpEntry(ClassFile classFile, int index)
    {
         classFile.constantPoolEntryAccept(index, this);
    }
}
