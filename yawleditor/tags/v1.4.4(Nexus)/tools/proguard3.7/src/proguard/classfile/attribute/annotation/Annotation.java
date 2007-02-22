/* $Id: Annotation.java,v 1.4.2.2 2006/10/14 12:33:22 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
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
import proguard.classfile.visitor.ClassFileVisitor;

import java.io.*;

/**
 * Representation of a runtime annotation.
 *
 * @author Eric Lafortune
 */
public class Annotation implements VisitorAccepter
{
    private static final int CONSTANT_FIELD_SIZE  = 4;
    private static final int CONSTANT_FIELD_SIZE2 = 2;


    public int            u2typeIndex;
    public int            u2numberOfElementValuePairs;
    public ElementValue[] elementValues;

    /**
     * An extra field pointing to the ClassFile objects referenced in the
     * type string. This field is filled out by the <code>{@link
     * proguard.classfile.util.ClassFileReferenceInitializer ClassFileReferenceInitializer}</code>.
     * References to primitive types are ignored.
     */
    public ClassFile[] referencedClassFiles;

    /**
     * An extra field in which visitors can store information.
     */
    public Object visitorInfo;


    public static Annotation create(DataInput din) throws IOException
    {
        Annotation annotation = new Annotation();
        annotation.read(din);
        return annotation;
    }

    private void read(DataInput din) throws IOException
    {
        u2typeIndex                 = din.readUnsignedShort();
        u2numberOfElementValuePairs = din.readUnsignedShort();

        elementValues = new ElementValue[u2numberOfElementValuePairs];

        for (int i = 0; i < u2numberOfElementValuePairs; i++)
        {
            int u2elementName = din.readUnsignedShort();

            elementValues[i] = ElementValue.create(din);

            elementValues[i].u2elementName = u2elementName;
        }
    }

    /**
     * Exports the representation to a DataOutput stream.
     */
    public void write(DataOutput dout) throws IOException
    {
        dout.writeShort(u2typeIndex);
        dout.writeShort(u2numberOfElementValuePairs);
        for (int i = 0; i < u2numberOfElementValuePairs; i++)
        {
            dout.writeShort(elementValues[i].u2elementName);

            elementValues[i].write(dout);
        }
    }


    /**
     * Returns the length of this annotation, expressed in bytes.
     */
    protected int getLength()
    {
        int length = CONSTANT_FIELD_SIZE;
        for (int i = 0; i < u2numberOfElementValuePairs; i++)
        {
            length += CONSTANT_FIELD_SIZE2 + elementValues[i].getLength();
        }

        return length;
    }


    /**
     * Applies the given visitor to the first referenced class. This is the
     * main annotation class.
     */
    public void referencedClassFileAccept(ClassFileVisitor classFileVisitor)
    {
        if (referencedClassFiles != null)
        {
            ClassFile referencedClassFile = referencedClassFiles[0];
            if (referencedClassFile != null)
            {
                referencedClassFile.accept(classFileVisitor);
            }
        }
    }


    /**
     * Applies the given visitor to all referenced classes.
     */
    public void referencedClassFilesAccept(ClassFileVisitor classFileVisitor)
    {
        if (referencedClassFiles != null)
        {
            for (int index = 0; index < referencedClassFiles.length; index++)
            {
                ClassFile referencedClassFile = referencedClassFiles[index];
                if (referencedClassFile != null)
                {
                    referencedClassFile.accept(classFileVisitor);
                }
            }
        }
    }


    /**
     * Applies the given visitor to all element value pairs.
     */
    public void elementValuesAccept(ClassFile classFile, ElementValueVisitor elementValueVisitor)
    {
        for (int i = 0; i < u2numberOfElementValuePairs; i++)
        {
            elementValues[i].accept(classFile, this, elementValueVisitor);
        }
    }


    // Implementations for VisitorAccepter.

    public Object getVisitorInfo()
    {
        return visitorInfo;
    }

    public void setVisitorInfo(Object visitorInfo)
    {
        this.visitorInfo = visitorInfo;
    }
}
