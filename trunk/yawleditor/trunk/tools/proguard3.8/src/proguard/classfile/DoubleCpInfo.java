/* $Id: DoubleCpInfo.java,v 1.18.2.3 2007/01/18 21:31:51 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 1999      Mark Welsh (markw@retrologic.com)
 * Copyright (c) 2002-2007 Eric Lafortune (eric@graphics.cornell.edu)
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
 * Representation of a 'double' entry in the ConstantPool (takes up two indices).
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class DoubleCpInfo extends CpInfo
{
    public int u4highBytes;
    public int u4lowBytes;


    /**
     * Creates a new DoubleCpInfo with the given double value.
     */
    public DoubleCpInfo(double value)
    {
        setValue(value);
    }


    protected DoubleCpInfo()
    {
    }


    /**
     * Returns the double value of this DoubleCpInfo.
     */
    public double getValue()
    {
        return Double.longBitsToDouble((long)u4highBytes << 32 | (u4lowBytes & 0xffffffffL));
    }


    /**
     * Sets the double value of this DoubleCpInfo.
     */
    public void setValue(double value)
    {
        long longValue = Double.doubleToLongBits(value);
        u4highBytes = (int)(longValue >> 32);
        u4lowBytes  = (int) longValue;
    }


    // Implementations for CpInfo.

    public int getTag()
    {
        return ClassConstants.CONSTANT_Double;
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
        cpInfoVisitor.visitDoubleCpInfo(classFile, this);
    }
}
