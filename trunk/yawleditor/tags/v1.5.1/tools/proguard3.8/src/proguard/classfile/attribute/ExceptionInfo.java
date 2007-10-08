/* $Id: ExceptionInfo.java,v 1.3.2.2 2007/01/18 21:31:51 eric Exp $
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

import proguard.classfile.*;

import java.io.*;

/**
 * Representation of an Exception table entry.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class ExceptionInfo implements VisitorAccepter
{
    public static final int CONSTANT_FIELD_SIZE = 8;


    public int u2startpc;
    public int u2endpc;
    public int u2handlerpc;
    public int u2catchType;

    /**
     * An extra field in which visitors can store information.
     */
    public Object visitorInfo;


    public static ExceptionInfo create(DataInput din) throws IOException
    {
        ExceptionInfo ei = new ExceptionInfo();
        ei.read(din);
        return ei;
    }


    private ExceptionInfo() {}

    private void read(DataInput din) throws IOException
    {
        u2startpc   = din.readUnsignedShort();
        u2endpc     = din.readUnsignedShort();
        u2handlerpc = din.readUnsignedShort();
        u2catchType = din.readUnsignedShort();
    }

    /**
     * Exports the representation to a DataOutput stream.
     */
    public void write(DataOutput dout) throws IOException
    {
        dout.writeShort(u2startpc);
        dout.writeShort(u2endpc);
        dout.writeShort(u2handlerpc);
        dout.writeShort(u2catchType);
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
