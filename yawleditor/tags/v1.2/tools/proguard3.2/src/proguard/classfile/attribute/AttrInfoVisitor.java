/* $Id: AttrInfoVisitor.java,v 1.1 2004/10/10 21:10:04 eric Exp $
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
package proguard.classfile.attribute;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;

/**
 * This interface specifies the methods for a visitor of <code>AttrInfo</code>
 * objects.
 *
 * @author Eric Lafortune
 */
public interface AttrInfoVisitor
{
    public void visitUnknownAttrInfo(               ClassFile classFile, UnknownAttrInfo         unknownAttrInfo);
    public void visitInnerClassesAttrInfo(          ClassFile classFile, InnerClassesAttrInfo    innerClassesAttrInfo);
    public void visitEnclosingMethodAttrInfo(       ClassFile classFile, EnclosingMethodAttrInfo enclosingMethodAttrInfo);

    public void visitConstantValueAttrInfo(         ClassFile classFile, FieldInfo fieldInfo, ConstantValueAttrInfo constantValueAttrInfo);

    public void visitExceptionsAttrInfo(            ClassFile classFile, MethodInfo methodInfo, ExceptionsAttrInfo exceptionsAttrInfo);
    public void visitCodeAttrInfo(                  ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo       codeAttrInfo);

    public void visitLineNumberTableAttrInfo(       ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LineNumberTableAttrInfo        lineNumberTableAttrInfo);
    public void visitLocalVariableTableAttrInfo(    ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTableAttrInfo     localVariableTableAttrInfo);
    public void visitLocalVariableTypeTableAttrInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LocalVariableTypeTableAttrInfo localVariableTypeTableAttrInfo);

    public void visitSourceFileAttrInfo(            ClassFile classFile, SourceFileAttrInfo sourceFileAttrInfo);
    public void visitSourceDirAttrInfo(             ClassFile classFile, SourceDirAttrInfo  sourceDirAttrInfo);
    public void visitDeprecatedAttrInfo(            ClassFile classFile, DeprecatedAttrInfo deprecatedAttrInfo);
    public void visitSyntheticAttrInfo(             ClassFile classFile, SyntheticAttrInfo  syntheticAttrInfo);
    public void visitSignatureAttrInfo(             ClassFile classFile, SignatureAttrInfo  syntheticAttrInfo);

    public void visitRuntimeVisibleAnnotationAttrInfo(           ClassFile classFile, RuntimeVisibleAnnotationsAttrInfo            runtimeVisibleAnnotationsAttrInfo);
    public void visitRuntimeInvisibleAnnotationAttrInfo(         ClassFile classFile, RuntimeInvisibleAnnotationsAttrInfo          runtimeInvisibleAnnotationsAttrInfo);
    public void visitRuntimeVisibleParameterAnnotationAttrInfo(  ClassFile classFile, RuntimeVisibleParameterAnnotationsAttrInfo   runtimeVisibleParameterAnnotationsAttrInfo);
    public void visitRuntimeInvisibleParameterAnnotationAttrInfo(ClassFile classFile, RuntimeInvisibleParameterAnnotationsAttrInfo runtimeInvisibleParameterAnnotationsAttrInfo);
    public void visitAnnotationDefaultAttrInfo(                  ClassFile classFile, AnnotationDefaultAttrInfo                    annotationDefaultAttrInfo);
}
