/* $Id: FloatCpInfo.java,v 1.17.2.1 2006/01/16 22:57:55 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 1999      Mark Welsh (markw@retrologic.com)
 * Copyright (c) 2002-2006 Eric Lafortune (eric@graphics.cornell.edu)
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
 * Representation of a 'float' entry in the ConstantPool.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class FloatCpInfo extends CpInfo
{
    public int u4bytes;


    /**
     * Creates a new FloatCpInfo with the given float value.
     */
    public FloatCpInfo(float value)
    {
        setValue(value);
    }


    protected FloatCpInfo()
    {
    }


    /**
     * Returns the float value of this FloatCpInfo.
     */
    public float getValue()
    {
        return Float.intBitsToFloat(u4bytes);
    }


    /**
     * Sets the float value of this FloatCpInfo.
     */
    public void setValue(float value)
    {
        u4bytes = Float.floatToIntBits(value);
    }


    // Implementations for CpInfo.

    public int getTag()
    {
        return ClassConstants.CONSTANT_Float;
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
        cpInfoVisitor.visitFloatCpInfo(classFile, this);
    }
}
