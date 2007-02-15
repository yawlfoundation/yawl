/* $Id: AnnotationElementValue.java,v 1.4.2.1 2006/01/16 22:57:55 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 1999      Mark Welsh (markw@retrologic.com)
 * Copyright (c) 2002-2006 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.classfile.attribute.annotation;

import proguard.classfile.*;

import java.io.*;

/**
 * Representation of an annotation element value.
 *
 * @author Eric Lafortune
 */
public class AnnotationElementValue extends ElementValue
{
    protected static final int CONSTANT_FIELD_SIZE = ElementValue.CONSTANT_FIELD_SIZE;


    public Annotation annotationValue;


    protected AnnotationElementValue()
    {
    }


    /**
     * Applies the given visitor to the annotation.
     */
    public void annotationAccept(ClassFile classFile, AnnotationVisitor annotationVisitor)
    {
        annotationVisitor.visitAnnotation(classFile, annotationValue);
    }


    // Implementations for ElementValue.

    protected int getLength()
    {
        return CONSTANT_FIELD_SIZE + annotationValue.getLength();
    }

    protected void readInfo(DataInput din) throws IOException
    {
        annotationValue = Annotation.create(din);
    }

    protected void writeInfo(DataOutput dout) throws IOException
    {
        annotationValue.write(dout);
    }

    public void accept(ClassFile classFile, Annotation annotation, ElementValueVisitor elementValueVisitor)
    {
        elementValueVisitor.visitAnnotationElementValue(classFile, annotation, this);
    }
}
