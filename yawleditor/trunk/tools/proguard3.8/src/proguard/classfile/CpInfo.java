/* $Id: CpInfo.java,v 1.23.2.2 2007/01/18 21:31:51 eric Exp $
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
 * Representation of an entry in the ConstantPool. Specific types of entry
 * have their representations sub-classed from this.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public abstract class CpInfo implements VisitorAccepter
{
    // Shared copies of constant pool objects, to avoid creating a lot of objects.
    private static final IntegerCpInfo            integerCpInfo            = new IntegerCpInfo();
    private static final FloatCpInfo              floatCpInfo              = new FloatCpInfo();
    private static final LongCpInfo               longCpInfo               = new LongCpInfo();
    private static final DoubleCpInfo             doubleCpInfo             = new DoubleCpInfo();
    private static final StringCpInfo             stringCpInfo             = new StringCpInfo();
    private static final FieldrefCpInfo           fieldrefCpInfo           = new FieldrefCpInfo();
    private static final MethodrefCpInfo          methodrefCpInfo          = new MethodrefCpInfo();
    private static final InterfaceMethodrefCpInfo interfaceMethodrefCpInfo = new InterfaceMethodrefCpInfo();
    private static final NameAndTypeCpInfo        nameAndTypeCpInfo        = new NameAndTypeCpInfo();


    //public int  u1tag;
    //public byte info[];

    /**
     * An extra field in which visitors can store information.
     */
    public Object visitorInfo;


    /**
     * Creates a new CpInfo from the data passed.
     *
     * @throws IOException if the class file is corrupt or incomplete.
     */
    public static CpInfo create(DataInput din) throws IOException
    {
        // Instantiate based on tag byte
        CpInfo cpInfo = null;
        int u1tag = din.readUnsignedByte();
        switch (u1tag)
        {
        case ClassConstants.CONSTANT_Utf8:               cpInfo = new Utf8CpInfo();              break;
        case ClassConstants.CONSTANT_Integer:            cpInfo = new IntegerCpInfo();           break;
        case ClassConstants.CONSTANT_Float:              cpInfo = new FloatCpInfo();             break;
        case ClassConstants.CONSTANT_Long:               cpInfo = new LongCpInfo();              break;
        case ClassConstants.CONSTANT_Double:             cpInfo = new DoubleCpInfo();            break;
        case ClassConstants.CONSTANT_Class:              cpInfo = new ClassCpInfo();             break;
        case ClassConstants.CONSTANT_String:             cpInfo = new StringCpInfo();            break;
        case ClassConstants.CONSTANT_Fieldref:           cpInfo = new FieldrefCpInfo();          break;
        case ClassConstants.CONSTANT_Methodref:          cpInfo = new MethodrefCpInfo();         break;
        case ClassConstants.CONSTANT_InterfaceMethodref: cpInfo = new InterfaceMethodrefCpInfo();break;
        case ClassConstants.CONSTANT_NameAndType:        cpInfo = new NameAndTypeCpInfo();       break;
        default: throw new IOException("Unknown constant type ["+u1tag+"] in constant pool");
        }
        cpInfo.readInfo(din);
        return cpInfo;
    }


    /**
     * Creates a new CpInfo from the data passed, for UTF-8 and Class constant
     * pool entries, or returns a shared object, for all other entries.
     *
     * @throws IOException if the class file is corrupt or incomplete.
     */
    public static CpInfo createOrShare(DataInput din) throws IOException
    {
        // Instantiate based on tag byte
        CpInfo cpInfo = null;
        int u1tag = din.readUnsignedByte();
        switch (u1tag)
        {
        case ClassConstants.CONSTANT_Utf8:               cpInfo = new Utf8CpInfo();         break;
        case ClassConstants.CONSTANT_Integer:            cpInfo = integerCpInfo;            break;
        case ClassConstants.CONSTANT_Float:              cpInfo = floatCpInfo;              break;
        case ClassConstants.CONSTANT_Long:               cpInfo = longCpInfo;               break;
        case ClassConstants.CONSTANT_Double:             cpInfo = doubleCpInfo;             break;
        case ClassConstants.CONSTANT_Class:              cpInfo = new ClassCpInfo();        break;
        case ClassConstants.CONSTANT_String:             cpInfo = stringCpInfo;             break;
        case ClassConstants.CONSTANT_Fieldref:           cpInfo = fieldrefCpInfo;           break;
        case ClassConstants.CONSTANT_Methodref:          cpInfo = methodrefCpInfo;          break;
        case ClassConstants.CONSTANT_InterfaceMethodref: cpInfo = interfaceMethodrefCpInfo; break;
        case ClassConstants.CONSTANT_NameAndType:        cpInfo = nameAndTypeCpInfo;        break;
        default: throw new IOException("Unknown constant type ["+u1tag+"] in constant pool");
        }
        cpInfo.readInfo(din);
        return cpInfo;
    }


    /**
     * Exports the representation to a DataOutput stream.
     */
    public void write(DataOutput dout) throws IOException
    {
        dout.writeByte(getTag());
        writeInfo(dout);
    }


    // Abstract methods to be implemented by extensions.

    /**
     * Returns the class pool info tag that specifies the entry type.
     */
    public abstract int getTag();


    /**
     * Reads the 'info' data following the u1tag byte.
     */
    protected abstract void readInfo(DataInput din) throws IOException;


    /**
     * Writes the 'info' data following the u1tag byte.
     */
    protected abstract void writeInfo(DataOutput dout) throws IOException;


    /**
     * Accepts the given visitor.
     */
    public abstract void accept(ClassFile classFile, CpInfoVisitor cpInfoVisitor);


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
