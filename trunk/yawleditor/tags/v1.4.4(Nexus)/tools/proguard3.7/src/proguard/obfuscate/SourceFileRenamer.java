/* $Id: SourceFileRenamer.java,v 1.2.2.1 2006/01/16 22:57:56 eric Exp $
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
package proguard.obfuscate;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.editor.ConstantPoolEditor;
import proguard.classfile.visitor.ClassFileVisitor;

/**
 * This ClassFileVisitor changes the name stored in the source file attributes
 * and source dir attributes of the class files that it visits, if the
 * atributes are present.
 *
 * @author Eric Lafortune
 */
public class SourceFileRenamer
implements   ClassFileVisitor,
             AttrInfoVisitor
{
    private ConstantPoolEditor constantPoolEditor = new ConstantPoolEditor();

    private String newSourceFileAttribute;


    /**
     * Creates a new SourceFileRenamer.
     * @param newSourceFileAttribute the new string to be put in the source file
     *                               attributes.
     */
    public SourceFileRenamer(String newSourceFileAttribute)
    {
        this.newSourceFileAttribute = newSourceFileAttribute;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Only visit the class file attributes.
        programClassFile.attributesAccept(this);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        // Library class files don't have attributes.
    }


    // Implementations for AttrInfoVisitor.

    public void visitUnknownAttrInfo(ClassFile classFile, UnknownAttrInfo unknownAttrInfo) {}
    public void visitInnerClassesAttrInfo(ClassFile classFile, InnerClassesAttrInfo innerClassesAttrInfo) {}
    public void visitEnclosingMethodAttrInfo(ClassFile classFile, EnclosingMethodAttrInfo enclosingMethodAttrInfo) {}
    public void visitConstantValueAttrInfo(ClassFile classFile, FieldInfo fieldInfo, ConstantValueAttrInfo constantValueAttrInfo) {}
    public void visitExceptionsAttrInfo(ClassFile classFile, MethodInfo methodInfo, ExceptionsAttrInfo exceptionsAttrInfo) {}
    public void visitCodeAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo) {}
    public void visitLineNumberTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LineNumberTableAttrInfo lineNumberTableAttrInfo) {}
    public void visitLocalVariableTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTableAttrInfo localVariableTableAttrInfo) {}
    public void visitLocalVariableTypeTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeTableAttrInfo localVariableTypeTableAttrInfo) {}
    public void visitDeprecatedAttrInfo(ClassFile classFile, DeprecatedAttrInfo deprecatedAttrInfo) {}
    public void visitSyntheticAttrInfo(ClassFile classFile, SyntheticAttrInfo syntheticAttrInfo) {}
    public void visitSignatureAttrInfo(ClassFile classFile, SignatureAttrInfo signatureAttrInfo) {}
    public void visitRuntimeVisibleAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleAnnotationsAttrInfo runtimeVisibleAnnotationsAttrInfo) {}
    public void visitRuntimeInvisibleAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleAnnotationsAttrInfo runtimeInvisibleAnnotationsAttrInfo) {}
    public void visitRuntimeVisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeVisibleParameterAnnotationsAttrInfo runtimeVisibleParameterAnnotationsAttrInfo) {}
    public void visitRuntimeInvisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleParameterAnnotationsAttrInfo runtimeInvisibleParameterAnnotationsAttrInfo) {}
    public void visitAnnotationDefaultAttrInfo(ClassFile classFile, AnnotationDefaultAttrInfo annotationDefaultAttrInfo) {}


    public void visitSourceFileAttrInfo(ClassFile classFile, SourceFileAttrInfo sourceFileAttrInfo)
    {
        // Fix the source file attribute.
        sourceFileAttrInfo.u2sourceFileIndex =
            constantPoolEditor.addUtf8CpInfo((ProgramClassFile)classFile,
                                             newSourceFileAttribute);
    }


    public void visitSourceDirAttrInfo(ClassFile classFile, SourceDirAttrInfo sourceDirAttrInfo)
    {
        // Fix the source file attribute.
        sourceDirAttrInfo.u2sourceDirIndex =
            constantPoolEditor.addUtf8CpInfo((ProgramClassFile)classFile,
                                             newSourceFileAttribute);
    }
}
