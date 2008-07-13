/* $Id: AnnotationDefaultAttrInfo.java,v 1.4.2.2 2007/01/18 21:31:51 eric Exp $
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

import proguard.classfile.ClassFile;
import proguard.classfile.attribute.*;

import java.io.*;

/**
 * Representation of a runtime visible annotations attribute.
 *
 * @author Eric Lafortune
 */
public class AnnotationDefaultAttrInfo extends AttrInfo
{
    private static final int CONSTANT_FIELD_SIZE = 0;


    public ElementValue defaultValue;


    public AnnotationDefaultAttrInfo()
    {
    }


    /**
     * Applies the given visitor to the default element values.
     */
    public void defaultValueAccept(ClassFile classFile, ElementValueVisitor elementValueVisitor)
    {
        defaultValue.accept(classFile, null, elementValueVisitor);
    }


    // Implementations for AttrInfo.

    protected int getLength()
    {
        return CONSTANT_FIELD_SIZE + defaultValue.getLength();
    }

    protected void readInfo(DataInput din, ClassFile classFile) throws IOException
    {
        defaultValue = ElementValue.create(din);
    }

    protected void writeInfo(DataOutput dout) throws IOException
    {
        defaultValue.write(dout);
    }


    public void accept(ClassFile classFile, AttrInfoVisitor attrInfoVisitor)
    {
        attrInfoVisitor.visitAnnotationDefaultAttrInfo(classFile, this);
    }
}
