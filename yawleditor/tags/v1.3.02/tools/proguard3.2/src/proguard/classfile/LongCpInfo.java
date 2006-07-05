/* $Id: LongCpInfo.java,v 1.16 2004/10/23 16:53:00 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 1999      Mark Welsh (markw@retrologic.com)
 * Copyright (c) 2002-2004 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
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
package proguard.classfile;

import proguard.classfile.visitor.*;

import java.io.*;

/**
 * Representation of a 'long' entry in the ConstantPool (takes up two indices).
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class LongCpInfo extends CpInfo
{
    public int u4highBytes;
    public int u4lowBytes;


    /**
     * Creates a new LongCpInfo with the given long value.
     */
    public LongCpInfo(long value)
    {
        setValue(value);
    }


    protected LongCpInfo()
    {
    }


    /**
     * Returns the long value of this LongCpInfo.
     */
    public long getValue()
    {
        return (long)u4highBytes << 32 | (long)u4lowBytes;
    }


    /**
     * Sets the long value of this LongCpInfo.
     */
    public void setValue(long value)
    {
        u4highBytes = (int)(value >> 32);
        u4lowBytes  = (int) value;
    }


    // Implementations for CpInfo.

    public int getTag()
    {
        return ClassConstants.CONSTANT_Long;
    }

    protected void readInfo(DataInput din) throws IOException
    {
        u4highBytes = din.readInt();
        u4lowBytes = din.readInt();
    }

    protected void writeInfo(DataOutput dout) throws IOException
    {
        dout.writeInt(u4highBytes);
        dout.writeInt(u4lowBytes);
    }

    public void accept(ClassFile classFile, CpInfoVisitor cpInfoVisitor)
    {
        cpInfoVisitor.visitLongCpInfo(classFile, this);
    }
}
