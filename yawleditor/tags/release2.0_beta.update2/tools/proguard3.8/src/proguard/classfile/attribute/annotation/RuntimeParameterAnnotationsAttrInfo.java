/* $Id: RuntimeParameterAnnotationsAttrInfo.java,v 1.6.2.2 2007/01/18 21:31:51 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2007 Eric Lafortune (eric@graphics.cornell.edu)
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
    private static final int CONSTANT_FIELD_SIZE1 = 1;
    private static final int CONSTANT_FIELD_SIZE2 = 2;


    public int            u2numberOfParameters;
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
        for (int parameterIndex = 0; parameterIndex < u2numberOfParameters; parameterIndex++)
        {
            Annotation[] annotations = parameterAnnotations[parameterIndex];
            int u2numberOfAnnotations = annotations.length;

            for (int i = 0; i < u2numberOfAnnotations; i++)
            {
                // We don't need double dispatching here, since there is only one
                // type of Annotation.
                //annotationVisitor.visitAnnotation(classFile, methodInfo, i, annotations[j]);
                annotationVisitor.visitAnnotation(classFile, annotations[i]);
            }
        }
    }


    // Implementations for AttrInfo.

    protected int getLength()
    {
        int length = CONSTANT_FIELD_SIZE1;

        for (int parameterIndex = 0; parameterIndex < u2numberOfParameters; parameterIndex++)
        {
            Annotation[] annotations = parameterAnnotations[parameterIndex];
            int u2numberOfAnnotations = annotations.length;

            length += CONSTANT_FIELD_SIZE2;

            for (int i = 0; i < u2numberOfAnnotations; i++)
            {
                length += annotations[i].getLength();
            }
        }

        return length;
    }

    protected void readInfo(DataInput din, ClassFile classFile) throws IOException
    {
        u2numberOfParameters = din.readUnsignedByte();
        parameterAnnotations = new Annotation[u2numberOfParameters][];

        for (int parameterIndex = 0; parameterIndex < u2numberOfParameters; parameterIndex++)
        {
            int u2numberOfAnnotations = din.readUnsignedShort();

            Annotation[] annotations = new Annotation[u2numberOfAnnotations];

            for (int i = 0; i < u2numberOfAnnotations; i++)
            {
                annotations[i] = Annotation.create(din);
            }

            parameterAnnotations[parameterIndex] = annotations;
        }
    }

    protected void writeInfo(DataOutput dout) throws IOException
    {
        dout.writeByte(u2numberOfParameters);

        for (int parameterIndex = 0; parameterIndex < u2numberOfParameters; parameterIndex++)
        {
            Annotation[] annotations = parameterAnnotations[parameterIndex];
            int u2numberOfAnnotations = annotations.length;

            dout.writeShort(u2numberOfAnnotations);

            for (int i = 0; i < u2numberOfAnnotations; i++)
            {
                annotations[i].write(dout);
            }
        }
    }
}
