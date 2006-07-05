/* $Id: InnerClassesInfo.java,v 1.1 2004/10/10 21:10:04 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 1999      Mark Welsh (markw@retrologic.com)
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
package proguard.classfile.attribute;

import proguard.classfile.*;

import java.io.*;

/**
 * Representation of an Inner Classes table entry.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class InnerClassesInfo implements VisitorAccepter
{
    public static final int CONSTANT_FIELD_SIZE = 8;


    public int u2innerClassInfoIndex;
    public int u2outerClassInfoIndex;
    public int u2innerNameIndex;
    public int u2innerClassAccessFlags;

    /**
     * An extra field in which visitors can store information.
     */
    public Object visitorInfo;


    public static InnerClassesInfo create(DataInput din) throws IOException
    {
        InnerClassesInfo ici = new InnerClassesInfo();
        ici.read(din);
        return ici;
    }

    /**
     * Returns the inner class index.
     */
    protected int getInnerClassIndex()
    {
        return u2innerClassInfoIndex;
    }

    /**
     * Returns the name index.
     */
    protected int getInnerNameIndex()
    {
        return u2innerNameIndex;
    }

    /**
     * Sets the name index.
     */
    protected void setInnerNameIndex(int index)
    {
        u2innerNameIndex = index;
    }

    private void read(DataInput din) throws IOException
    {
        u2innerClassInfoIndex   = din.readUnsignedShort();
        u2outerClassInfoIndex   = din.readUnsignedShort();
        u2innerNameIndex        = din.readUnsignedShort();
        u2innerClassAccessFlags = din.readUnsignedShort();
    }

    /**
     * Exports the representation to a DataOutput stream.
     */
    public void write(DataOutput dout) throws IOException
    {
        dout.writeShort(u2innerClassInfoIndex);
        dout.writeShort(u2outerClassInfoIndex);
        dout.writeShort(u2innerNameIndex);
        dout.writeShort(u2innerClassAccessFlags);
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
