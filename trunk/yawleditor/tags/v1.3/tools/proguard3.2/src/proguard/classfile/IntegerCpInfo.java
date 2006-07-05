/* $Id: IntegerCpInfo.java,v 1.16 2004/10/23 16:53:00 eric Exp $
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
 * Representation of a 'integer' entry in the ConstantPool.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class IntegerCpInfo extends CpInfo
{
    public int u4bytes;


    /**
     * Creates a new IntegerCpInfo with the given integer value.
     */
    public IntegerCpInfo(int value)
    {
        setValue(value);
    }


    protected IntegerCpInfo()
    {
    }


    /**
     * Returns the integer value of this IntegerCpInfo.
     */
    public int getValue()
    {
        return u4bytes;
    }


    /**
     * Sets the integer value of this IntegerCpInfo.
     */
    public void setValue(int value)
    {
        u4bytes = value;
    }


    // Implementations for CpInfo.

    public int getTag()
    {
        return ClassConstants.CONSTANT_Integer;
    }

    protected void readInfo(DataInput din) throws IOException
    {
        u4bytes = din.readInt();
    }

    protected void writeInfo(DataOutput dout) throws IOException
    {
        dout.writeInt(u4bytes);
    }

    public void accept(ClassFile classFile, CpInfoVisitor cpInfoVisitor)
    {
        cpInfoVisitor.visitIntegerCpInfo(classFile, this);
    }
}
