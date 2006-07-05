/* $Id: LocalVariableInfo.java,v 1.3.2.1 2006/01/16 22:57:55 eric Exp $
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
package proguard.classfile.attribute;

import proguard.classfile.ClassFile;

import java.io.*;

/**
 * Representation of an Local Variable table entry.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class LocalVariableInfo
{
    public static final int CONSTANT_FIELD_SIZE = 10;


    public int u2startpc;
    public int u2length;
    public int u2nameIndex;
    public int u2descriptorIndex;
    public int u2index;

    /**
     * An extra field pointing to the referenced ClassFile object.
     * This field is filled out by the <code>{@link
     * proguard.classfile.util.ClassFileReferenceInitializer ClassFileReferenceInitializer}</code>.
     */
    public ClassFile referencedClassFile;


    public static LocalVariableInfo create(DataInput din) throws IOException
    {
        LocalVariableInfo lvi = new LocalVariableInfo();
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
        return u2descriptorIndex;
    }

    /**
     * Sets the descriptor index.
     */
    protected void setDescriptorIndex(int index)
    {
        u2descriptorIndex = index;
    }

    private void read(DataInput din) throws IOException
    {
        u2startpc         = din.readUnsignedShort();
        u2length          = din.readUnsignedShort();
        u2nameIndex       = din.readUnsignedShort();
        u2descriptorIndex = din.readUnsignedShort();
        u2index           = din.readUnsignedShort();
    }

    /**
     * Exports the representation to a DataOutput stream.
     */
    public void write(DataOutput dout) throws IOException
    {
        dout.writeShort(u2startpc);
        dout.writeShort(u2length);
        dout.writeShort(u2nameIndex);
        dout.writeShort(u2descriptorIndex);
        dout.writeShort(u2index);
    }
}
