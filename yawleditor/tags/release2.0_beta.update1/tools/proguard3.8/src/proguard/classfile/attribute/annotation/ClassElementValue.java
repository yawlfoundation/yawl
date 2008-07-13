/* $Id: ClassElementValue.java,v 1.3.2.3 2007/01/18 21:31:51 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 1999      Mark Welsh (markw@retrologic.com)
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
import proguard.classfile.visitor.ClassFileVisitor;

import java.io.*;

/**
 * Representation of a class element value.
 *
 * @author Eric Lafortune
 */
public class ClassElementValue extends ElementValue
{
    protected static final int CONSTANT_FIELD_SIZE = ElementValue.CONSTANT_FIELD_SIZE + 2;


    public int u2classInfoIndex;

    /**
     * An extra field pointing to the ClassFile objects referenced in the
     * type name string. This field is filled out by the <code>{@link
     * proguard.classfile.util.ClassFileReferenceInitializer ClassFileReferenceInitializer}</code>.
     * References to primitive types are ignored.
     */
    public ClassFile[] referencedClassFiles;


    protected ClassElementValue()
    {
    }


    /**
     * Applies the given visitor to all referenced classes.
     */
    public void referencedClassesAccept(ClassFileVisitor classFileVisitor)
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


    // Implementations for ElementValue.

    protected int getLength()
    {
        return CONSTANT_FIELD_SIZE;
    }

    protected void readInfo(DataInput din) throws IOException
    {
        u2classInfoIndex = din.readUnsignedShort();
    }

    protected void writeInfo(DataOutput dout) throws IOException
    {
        dout.writeShort(u2classInfoIndex);
    }

    public void accept(ClassFile classFile, Annotation annotation, ElementValueVisitor elementValueVisitor)
    {
        elementValueVisitor.visitClassElementValue(classFile, annotation, this);
    }
}
