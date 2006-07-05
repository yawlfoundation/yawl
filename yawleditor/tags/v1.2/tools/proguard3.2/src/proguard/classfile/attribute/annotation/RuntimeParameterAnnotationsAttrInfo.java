/* $Id: RuntimeParameterAnnotationsAttrInfo.java,v 1.2 2004/11/20 15:41:24 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2004 Eric Lafortune (eric@graphics.cornell.edu)
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
import proguard.classfile.attribute.AttrInfo;

import java.io.*;

/**
 * Representation of a runtime parameter annotations attribute.
 *
 * @author Eric Lafortune
 */
public abstract class RuntimeParameterAnnotationsAttrInfo extends AttrInfo
{
    private static final int CONSTANT_FIELD_SIZE = 2;


    //public int            u2numberOfParameters;
    //public int[]          u2numberOfAnnotations;
    public Annotation[][] parameterAnnotations;


    protected RuntimeParameterAnnotationsAttrInfo()
    {
    }


    /**
     * Applies the given visitor to all annotations.
     */
    public void annotationsAccept(ClassFile classFile, AnnotationVisitor annotationVisitor)
    {
        for (int i = 0; i < parameterAnnotations.length; i++)
        {
            Annotation[] annotations = parameterAnnotations[i];
            for (int j = 0; j < annotations.length; i++)
            {
                // We don't need double dispatching here, since there is only one
                // type of Annotation.
                //annotationVisitor.visitAnnotation(classFile, methodInfo, i, annotations[j]);
                annotationVisitor.visitAnnotation(classFile, annotations[j]);
            }
        }
    }


    // Implementations for AttrInfo.

    protected int getLength()
    {
        int length = CONSTANT_FIELD_SIZE;

        for (int i = 0; i < parameterAnnotations.length; i++)
        {
            length += CONSTANT_FIELD_SIZE;

            Annotation[] annotations = parameterAnnotations[i];
            for (int j = 0; j < annotations.length; i++)
            {
                length += annotations[j].getLength();
            }
        }

        return length;
    }

    protected void readInfo(DataInput din, ClassFile classFile) throws IOException
    {
        int u2numberOfParameters = din.readUnsignedShort();
        parameterAnnotations = new Annotation[u2numberOfParameters][];

        for (int i = 0; i < u2numberOfParameters; i++)
        {
            int u2numberOfAnnotations = din.readUnsignedShort();
            Annotation[] annotations = new Annotation[u2numberOfAnnotations];

            for (int j = 0; j < u2numberOfAnnotations; j++)
            {
                annotations[j] = Annotation.create(din);
            }

            parameterAnnotations[i] = annotations;
        }
    }

    protected void writeInfo(DataOutput dout) throws IOException
    {
        dout.writeShort(parameterAnnotations.length);
        for (int i = 0; i < parameterAnnotations.length; i++)
        {
            Annotation[] annotations = parameterAnnotations[i];

            dout.writeShort(annotations.length);
            for (int j = 0; j < annotations.length; i++)
            {
                annotations[j].write(dout);
            }
        }
    }
}
