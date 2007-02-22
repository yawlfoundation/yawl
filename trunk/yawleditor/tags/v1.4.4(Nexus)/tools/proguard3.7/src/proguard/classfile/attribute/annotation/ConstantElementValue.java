/* $Id: ConstantElementValue.java,v 1.3.2.1 2006/01/16 22:57:55 eric Exp $
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
 * Representation of a constant element value.
 *
 * @author Eric Lafortune
 */
public class ConstantElementValue extends ElementValue
{
    protected static final int CONSTANT_FIELD_SIZE = ElementValue.CONSTANT_FIELD_SIZE + 2;


    public int u2constantValueIndex;


    protected ConstantElementValue()
    {
    }


    // Implementations for ElementValue.

    protected int getLength()
    {
        return CONSTANT_FIELD_SIZE;
    }

    protected void readInfo(DataInput din) throws IOException
    {
        u2constantValueIndex = din.readUnsignedShort();
    }

    protected void writeInfo(DataOutput dout) throws IOException
    {
        dout.writeShort(u2constantValueIndex);
    }

    public void accept(ClassFile classFile, Annotation annotation, ElementValueVisitor elementValueVisitor)
    {
        elementValueVisitor.visitConstantElementValue(classFile, annotation, this);
    }
}
