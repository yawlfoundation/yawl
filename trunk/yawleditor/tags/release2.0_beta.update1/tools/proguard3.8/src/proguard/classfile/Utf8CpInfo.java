/* $Id: Utf8CpInfo.java,v 1.21.2.2 2007/01/18 21:31:51 eric Exp $
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
 * Representation of a 'UTF-8' entry in the ConstantPool.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class Utf8CpInfo extends CpInfo
{
    private static final String ENCODING = "UTF-8";

    private static final char TWO_BYTE_LIMIT     = 0x80;
    private static final byte TWO_BYTE_CONSTANT1 = (byte)0xc0;
    private static final byte TWO_BYTE_CONSTANT2 = (byte)0x80;
    private static final int  TWO_BYTE_SHIFT1    = 6;
    private static final byte TWO_BYTE_MASK1     = (byte)0x1f;
    private static final byte TWO_BYTE_MASK2     = (byte)0x3f;

    private static final char THREE_BYTE_LIMIT     = 0x800;
    private static final byte THREE_BYTE_CONSTANT1 = (byte)0xe0;
    private static final byte THREE_BYTE_CONSTANT2 = (byte)0x80;
    private static final byte THREE_BYTE_CONSTANT3 = (byte)0x80;
    private static final int  THREE_BYTE_SHIFT1    = 12;
    private static final int  THREE_BYTE_SHIFT2    = 6;
    private static final byte THREE_BYTE_MASK1     = (byte)0x0f;
    private static final byte THREE_BYTE_MASK2     = (byte)0x3f;
    private static final byte THREE_BYTE_MASK3     = (byte)0x3f;


    // There are a lot of Utf8CpInfo objects, so we're optimising their storage.
    // Initially, we're storing the UTF-8 bytes in a byte array.
    // When the corresponding String is requested, we ditch the array and just
    // store the String.

    //private int u2length;
    private byte[] bytes;

    private String utf8string;


    protected Utf8CpInfo()
    {
    }

    /**
     * Constructor used when appending fresh UTF-8 entries to the constant pool.
     */
    public Utf8CpInfo(String utf8string)
    {
        this.bytes      = null;
        this.utf8string = utf8string;
    }

    /**
     * Returns UTF-8 data as a String.
     */
    public String getString()
    {
        try
        {
            switchToStringRepresentation();
        }
        catch (UnsupportedEncodingException ex)
        {
        }

        return utf8string;
    }

    /**
     * Sets UTF-8 data as String.
     */
    public void setString(String utf8String) throws Exception
    {
        this.bytes      = null;
        this.utf8string = utf8String;
    }


    // Implementations for CpInfo.

    public int getTag()
    {
        return ClassConstants.CONSTANT_Utf8;
    }

    protected void readInfo(DataInput din) throws IOException
    {
        int u2length = din.readUnsignedShort();
        bytes = new byte[u2length];
        din.readFully(bytes);
    }

    protected void writeInfo(DataOutput dout) throws IOException
    {
        byte[] bytes    = getByteArrayRepresentation();
        int    u2length = bytes.length;

        dout.writeShort(u2length);
        dout.write(bytes);
    }

    public void accept(ClassFile classFile, CpInfoVisitor cpInfoVisitor)
    {
        cpInfoVisitor.visitUtf8CpInfo(classFile, this);
    }


    /**
     * Switches to a String representation of the UTF-8 data.
     */
    private void switchToStringRepresentation() throws UnsupportedEncodingException
    {
        if (utf8string == null)
        {
            utf8string = new String(bytes, ENCODING);
            bytes = null;
        }
    }


    /**
     * Transforms UTF-8 bytes to the slightly modified UTF-8 representation that
     * is used by class files.
     */
    private byte[] getByteArrayRepresentation() throws UnsupportedEncodingException
    {
        // Do we still have the byte array representation?
        if (bytes != null)
        {
            // Then return that one.
            return bytes;
        }

        // We're computing the byte array ourselves, because the implementation
        // of String.getBytes("UTF-8") has a bug, at least up to JRE 1.4.2.
        // Also note the special treatment of the 0 character.

        // Compute the byte array length.
        int byteLength   = 0;
        int stringLength = utf8string.length();
        for (int stringIndex = 0; stringIndex < stringLength; stringIndex++)
        {
            char c = utf8string.charAt(stringIndex);

            // The character is represented by one, two, or three bytes.
            byteLength += c == 0                ? 2 :
                          c <  TWO_BYTE_LIMIT   ? 1 :
                          c <  THREE_BYTE_LIMIT ? 2 :
                                                  3;
        }

        // Allocate the byte array with the computed length.
        byte[] bytes  = new byte[byteLength];

        // Fill out the array.
        int byteIndex = 0;
        for (int stringIndex = 0; stringIndex < stringLength; stringIndex++)
        {
            char c = utf8string.charAt(stringIndex);
            if (c == 0)
            {
                // The 0 character gets a two-byte representation in class files.
                bytes[byteIndex++] = TWO_BYTE_CONSTANT1;
                bytes[byteIndex++] = TWO_BYTE_CONSTANT2;
            }
            else if (c < TWO_BYTE_LIMIT)
            {
                // The character is represented by a single byte.
                bytes[byteIndex++] = (byte)c;
            }
            else if (c < THREE_BYTE_LIMIT)
            {
                // The character is represented by two bytes.
                bytes[byteIndex++] = (byte)(TWO_BYTE_CONSTANT1 | ((c >>> TWO_BYTE_SHIFT1) & TWO_BYTE_MASK1));
                bytes[byteIndex++] = (byte)(TWO_BYTE_CONSTANT2 | ( c                      & TWO_BYTE_MASK2));
            }
            else
            {
                // The character is represented by three bytes.
                bytes[byteIndex++] = (byte)(THREE_BYTE_CONSTANT1 | ((c >>> THREE_BYTE_SHIFT1) & THREE_BYTE_MASK1));
                bytes[byteIndex++] = (byte)(THREE_BYTE_CONSTANT2 | ((c >>> THREE_BYTE_SHIFT2) & THREE_BYTE_MASK2));
                bytes[byteIndex++] = (byte)(THREE_BYTE_CONSTANT3 | ( c                        & THREE_BYTE_MASK3));
            }
        }

        return bytes;
    }
}
