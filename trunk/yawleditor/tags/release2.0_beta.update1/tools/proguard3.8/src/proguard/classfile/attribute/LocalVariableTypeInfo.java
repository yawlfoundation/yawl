/* $Id: LocalVariableTypeInfo.java,v 1.3.2.2 2007/01/18 21:31:51 eric Exp $
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
package proguard.classfile.attribute;

import proguard.classfile.ClassFile;

import java.io.*;

/**
 * Representation of an Local Variable table entry.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class LocalVariableTypeInfo
{
    public static final int CONSTANT_FIELD_SIZE = 10;


    public int u2startpc;
    public int u2length;
    public int u2nameIndex;
    public int u2signatureIndex;
    public int u2index;

    /**
     * An extra field pointing to the ClassFile objects referenced in the
     * type string. This field is filled out by the <code>{@link
     * proguard.classfile.util.ClassFileReferenceInitializer ClassFileReferenceInitializer}</code>.
     * References to primitive types are ignored.
     */
    public ClassFile[] referencedClassFiles;


    public static LocalVariableTypeInfo create(DataInput din) throws IOException
    {
        LocalVariableTypeInfo lvi = new LocalVariableTypeInfo();
        lvi.read(din);
        return lvi;
    }

    /**
     * Returns name index into Constant Pool.
     */
    protected int getNameIndex()
    {
        return u2nameIndex;
    }

    /**
     * Sets the name index.
     */
    protected void setNameIndex(int index)
    {
        u2nameIndex = index;
    }

    /**
     * Returns descriptor index into Constant Pool.
     */
    protected int getDescriptorIndex()
    {
        return u2signatureIndex;
    }

    /**
     * Sets the descriptor index.
     */
    protected void setDescriptorIndex(int index)
    {
        u2signatureIndex = index;
    }

    private void read(DataInput din) throws IOException
    {
        u2startpc = din.readUnsignedShort();
        u2length = din.readUnsignedShort();
        u2nameIndex = din.readUnsignedShort();
        u2signatureIndex = din.readUnsignedShort();
        u2index = din.readUnsignedShort();
    }

    /**
     * Exports the representation to a DataOutput stream.
     */
    public void write(DataOutput dout) throws IOException
    {
        dout.writeShort(u2startpc);
        dout.writeShort(u2length);
        dout.writeShort(u2nameIndex);
        dout.writeShort(u2signatureIndex);
        dout.writeShort(u2index);
    }
}
