/* $Id: ClassFileShrinker.java,v 1.27.2.1 2006/01/16 22:57:56 eric Exp $
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
package proguard.shrink;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.editor.ConstantPoolRemapper;
import proguard.classfile.instruction.*;
import proguard.classfile.visitor.*;


/**
 * This ClassFileVisitor removes constant pool entries and class members that
 * are not marked as being used.
 *
 * @see UsageMarker
 *
 * @author Eric Lafortune
 */
public class ClassFileShrinker
  implements ClassFileVisitor,
             MemberInfoVisitor,
             AttrInfoVisitor
{
    private UsageMarker          usageMarker;
    private ConstantPoolRemapper constantPoolRemapper;
    private int[]                cpIndexMap;


    /**
     * Creates a new ClassFileShrinker.
    /**
     * Creates a new InnerUsageMarker.
     * @param usageMarker the usage marker that is used to mark the classes
     *                    and class members.
     * @param codeLength an estimate of the maximum length of all the code that
     *                   will be edited.
     */
    public ClassFileShrinker(UsageMarker usageMarker,
                             int         codeLength)
    {
        this.usageMarker          = usageMarker;
        this.constantPoolRemapper = new ConstantPoolRemapper(codeLength);
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Shrink the arrays for constant pool, interfaces, fields, methods,
        // and class attributes.
        programClassFile.u2interfacesCount =
            shrinkCpIndexArray(programClassFile.constantPool,
                               programClassFile.u2interfaces,
                               programClassFile.u2interfacesCount);

        // Shrinking the constant pool also sets up an index map.
        programClassFile.u2constantPoolCount =
            shrinkConstantPool(programClassFile.constantPool,
                               programClassFile.u2constantPoolCount);

        programClassFile.u2fieldsCount =
            shrinkArray(programClassFile.fields,
                        programClassFile.u2fieldsCount);

        programClassFile.u2methodsCount =
            shrinkArray(programClassFile.methods,
                        programClassFile.u2methodsCount);

        programClassFile.u2attributesCount =
            shrinkArray(programClassFile.attributes,
                        programClassFile.u2attributesCount);

        // Compact the remaining fields, methods, and attributes,
        // and remap their references to the constant pool.
        programClassFile.fieldsAccept(this);
        programClassFile.methodsAccept(this);
        programClassFile.attributesAccept(this);

        // Remap all constant pool references.
        constantPoolRemapper.setCpIndexMap(cpIndexMap);
        constantPoolRemapper.visitProgramClassFile(programClassFile);

        // Compact the extra field pointing to the subclasses of this class.
        programClassFile.subClasses =
            shrinkToNewArray(programClassFile.subClasses);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        // Library class files are left unchanged.

        // Compact the extra field pointing to the subclasses of this class.
        libraryClassFile.subClasses =
            shrinkToNewArray(libraryClassFile.subClasses);
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        visitMemberInfo(programClassFile, programFieldInfo);
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        visitMemberInfo(programClassFile, programMethodInfo);
    }


    private void visitMemberInfo(ProgramClassFile programClassFile, ProgramMemberInfo programMemberInfo)
    {
        // Compact the attributes array.
        programMemberInfo.u2attributesCount =
            shrinkArray(programMemberInfo.attributes,
                        programMemberInfo.u2attributesCount);

        // Compact any attributes.
        programMemberInfo.attributesAccept(programClassFile, this);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
        // Library class files are left unchanged.
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        // Library class files are left unchanged.
    }


    // Implementations for AttrInfoVisitor.

    public void visitUnknownAttrInfo(ClassFile classFile, UnknownAttrInfo unknownAttrInfo) {}
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


    public void visitInnerClassesAttrInfo(ClassFile classFile, InnerClassesAttrInfo innerClassesAttrInfo)
    {
        // Compact the array of InnerClassesInfo objects.
        innerClassesAttrInfo.u2numberOfClasses =
            shrinkArray(innerClassesAttrInfo.classes,
                        innerClassesAttrInfo.u2numberOfClasses);
    }


    public void visitEnclosingMethodAttrInfo(ClassFile classFile, EnclosingMethodAttrInfo enclosingMethodAttrInfo)
    {
        // Sometimes, a class is still referenced (apparently as a dummy class),
        // but its enclosing method is not. Then remove the reference to
        // the enclosing method.
        // E.g. the anonymous inner class javax.swing.JList$1 is defined inside
        // a constructor of javax.swing.JList, but it is also referenced as a
        // dummy argument in a constructor of javax.swing.JList$ListSelectionHandler.
        if (enclosingMethodAttrInfo.referencedMethodInfo != null &&
            !usageMarker.isUsed(enclosingMethodAttrInfo.referencedMethodInfo))
        {
            enclosingMethodAttrInfo.u2nameAndTypeIndex = 0;

            enclosingMethodAttrInfo.referencedMethodInfo = null;
        }
    }


    public void visitCodeAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo)
    {
        // Compact the attributes array.
        codeAttrInfo.u2attributesCount =
            shrinkArray(codeAttrInfo.attributes,
                        codeAttrInfo.u2attributesCount);
    }


    // Small utility methods.

    /**
     * Removes all entries that are not marked as being used from the given
     * constant pool.
     * @return the new number of entries.
     */
    private int shrinkConstantPool(CpInfo[] constantPool, int length)
    {
        if (cpIndexMap == null ||
            cpIndexMap.length < length)
        {
            cpIndexMap = new int[length];
        }

        int     counter = 1;
        boolean isUsed  = false;

        // Shift the used constant pool entries together.
        for (int index = 1; index < length; index++)
        {
            cpIndexMap[index] = counter;

            CpInfo cpInfo = constantPool[index];

            // Don't update the flag if this is the second half of a long entry.
            if (cpInfo != null)
            {
                isUsed = usageMarker.isUsed(cpInfo);
            }

            if (isUsed)
            {
                constantPool[counter++] = cpInfo;
            }
        }

        // Clear the remaining constant pool elements.
        for (int index = counter; index < length; index++)
        {
            constantPool[index] = null;
        }

        return counter;
    }


    /**
     * Removes all indices that point to unused constant pool entries
     * from the given array.
     * @return the new number of indices.
     */
    private int shrinkCpIndexArray(CpInfo[] constantPool, int[] array, int length)
    {
        int counter = 0;

        // Shift the used objects together.
        for (int index = 0; index < length; index++)
        {
            if (usageMarker.isUsed(constantPool[array[index]]))
            {
                array[counter++] = array[index];
            }
        }

        // Clear the remaining array elements.
        for (int index = counter; index < length; index++)
        {
            array[index] = 0;
        }

        return counter;
    }


    /**
     * Removes all ClassFile objects that are not marked as being used
     * from the given array and returns the remaining objects in a an array
     * of the right size.
     * @return the new array.
     */
    private ClassFile[] shrinkToNewArray(ClassFile[] array)
    {
        if (array == null)
        {
            return null;
        }

        // Shrink the given array in-place.
        int length = shrinkArray(array, array.length);
        if (length == 0)
        {
            return null;
        }

        // Return immediately if the array is of right size already.
        if (length == array.length)
        {
            return array;
        }

        // Copy the remaining elements into a new array of the right size.
        ClassFile[] newArray = new ClassFile[length];
        System.arraycopy(array, 0, newArray, 0, length);
        return newArray;
    }


    /**
     * Removes all VisitorAccepter objects that are not marked as being used
     * from the given array.
     * @return the new number of VisitorAccepter objects.
     */
    private int shrinkArray(VisitorAccepter[] array, int length)
    {
        int counter = 0;

        // Shift the used objects together.
        for (int index = 0; index < length; index++)
        {
            if (usageMarker.isUsed(array[index]))
            {
                array[counter++] = array[index];
            }
        }

        // Clear the remaining array elements.
        for (int index = counter; index < length; index++)
        {
            array[index] = null;
        }

        return counter;
    }
}
