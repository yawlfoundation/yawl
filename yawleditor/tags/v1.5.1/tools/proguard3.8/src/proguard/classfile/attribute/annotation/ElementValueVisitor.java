/* $Id: ElementValueVisitor.java,v 1.3.2.2 2007/01/18 21:31:51 eric Exp $
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
package proguard.classfile.attribute.annotation;

import proguard.classfile.*;
import proguard.classfile.attribute.*;


/**
 * This interface specifies the methods for a visitor of <code>ElementValue</code>
 * objects.
 *
 * @author Eric Lafortune
 */
public interface ElementValueVisitor
{
    public void visitConstantElementValue(    ClassFile classFile, Annotation annotation, ConstantElementValue     constantElementValue);
    public void visitEnumConstantElementValue(ClassFile classFile, Annotation annotation, EnumConstantElementValue enumConstantElementValue);
    public void visitClassElementValue(       ClassFile classFile, Annotation annotation, ClassElementValue        classElementValue);
    public void visitAnnotationElementValue(  ClassFile classFile, Annotation annotation, AnnotationElementValue   annotationElementValue);
    public void visitArrayElementValue(       ClassFile classFile, Annotation annotation, ArrayElementValue        arrayElementValue);
}
