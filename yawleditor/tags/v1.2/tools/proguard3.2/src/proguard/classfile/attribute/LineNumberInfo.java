/* $Id: LineNumberInfo.java,v 1.1 2004/10/10 21:10:04 eric Exp $
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

import java.io.*;

/**
 * Representation of an Line Number table entry.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class LineNumberInfo
{
    public static final int CONSTANT_FIELD_SIZE = 4;


    public int u2startpc;
    public int u2lineNumber;


    public static LineNumberInfo create(DataInput din) throws IOException
    {
        LineNumberInfo lni = new LineNumberInfo();
        lni.read(din);
        return lni;
    }


    private LineNumberInfo() {}
    private void read(DataInput din) throws IOException
    {
        u2startpc    = din.readUnsignedShort();
        u2lineNumber = din.readUnsignedShort();
    }

    /**
     * Exports the representation to a DataOutput stream.
     */
    public void write(DataOutput dout) throws IOException
    {
        dout.writeShort(u2startpc);
        dout.writeShort(u2lineNumber);
    }
}
