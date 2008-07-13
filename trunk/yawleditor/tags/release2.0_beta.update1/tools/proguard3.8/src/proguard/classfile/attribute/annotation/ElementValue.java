/* $Id: ElementValue.java,v 1.6.2.3 2007/01/18 21:31:51 eric Exp $
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
import proguard.classfile.visitor.MemberInfoVisitor;

import java.io.*;

/**
 * Representation of an element value.
 *
 * @author Eric Lafortune
 */
public abstract class ElementValue implements VisitorAccepter
{
    protected static final int CONSTANT_FIELD_SIZE = 1;


    public int u1tag;

    /**
     * An extra field for the optional element name. It is used in element value
     * pairs of annotations. Otherwise, it is 0.
     */
    public int u2elementName;

    /**
     * An extra field pointing to the referenced ClassFile object.
     * This field is typically filled out by the <code>{@link
     * proguard.classfile.util.ClassReferenceInitializer}</code>.
     */
    public ClassFile referencedClassFile;

    /**
     * An extra field pointing to the referenced <code>MethodInfo</code>
     * object, if applicable. This field is typically filled out by the
     * <code>{@link proguard.classfile.util.ClassFileReferenceInitializer}</code>.
     */
    public MethodInfo referencedMethodInfo;

    /**
     * An extra field in which visitors can store information.
     */
    public Object visitorInfo;


    public static ElementValue create(DataInput din) throws IOException
    {
        int u1tag = din.readUnsignedByte();
        ElementValue elementValue = createElementValue(u1tag);

        elementValue.u1tag = u1tag;
        elementValue.readInfo(din);

        return elementValue;
    }


    private static ElementValue createElementValue(int u1tag)
    {
        switch (u1tag)
        {
            case ClassConstants.INTERNAL_TYPE_BOOLEAN:
            case ClassConstants.INTERNAL_TYPE_BYTE:
            case ClassConstants.INTERNAL_TYPE_CHAR:
            case ClassConstants.INTERNAL_TYPE_SHORT:
            case ClassConstants.INTERNAL_TYPE_INT:
            case ClassConstants.INTERNAL_TYPE_FLOAT:
            case ClassConstants.INTERNAL_TYPE_LONG:
            case ClassConstants.INTERNAL_TYPE_DOUBLE:
            case ClassConstants.ELEMENT_VALUE_STRING_CONSTANT: return new ConstantElementValue();

            case ClassConstants.ELEMENT_VALUE_ENUM_CONSTANT:   return new EnumConstantElementValue();
            case ClassConstants.ELEMENT_VALUE_CLASS:           return new ClassElementValue();
            case ClassConstants.ELEMENT_VALUE_ANNOTATION:      return new AnnotationElementValue();
            case ClassConstants.ELEMENT_VALUE_ARRAY:           return new ArrayElementValue();
        }

        throw new IllegalArgumentException("Unknown element value tag ["+u1tag+"]");
    }


    protected ElementValue()
    {
    }


    /**
     * Exports the representation to a DataOutput stream.
     */
    public final void write(DataOutput dout) throws IOException
    {
        dout.writeByte(u1tag);
        writeInfo(dout);
    }


    /**
     * Returns the element name.
     */
    public String getMethodName(ClassFile classFile)
    {
        return classFile.getCpString(u2elementName);
    }


    // Abstract methods to be implemented by extensions.

    /**
     * Returns the length of this element value, expressed in bytes.
     */
    protected abstract int getLength();


    /**
     * Reads the data following the header.
     */
    protected abstract void readInfo(DataInput din) throws IOException;


    /**
     * Exports data following the header to a DataOutput stream.
     */
    protected abstract void writeInfo(DataOutput dout) throws IOException;


    /**
     * Accepts the given visitor.
     */
    public abstract void accept(ClassFile classFile, Annotation annotation, ElementValueVisitor elementValueVisitor);


    /**
     * Applies the given visitor to the referenced method.
     */
    public void referencedMethodInfoAccept(MemberInfoVisitor memberInfoVisitor)
    {
        if (referencedMethodInfo != null)
        {
            referencedMethodInfo.accept(referencedClassFile, memberInfoVisitor);
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
