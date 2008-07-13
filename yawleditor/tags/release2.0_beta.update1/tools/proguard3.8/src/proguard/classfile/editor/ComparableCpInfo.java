/* $Id: ComparableCpInfo.java,v 1.5.2.2 2007/01/18 21:31:51 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2007 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.classfile.editor;

import proguard.classfile.*;
import proguard.classfile.instruction.*;
import proguard.classfile.visitor.*;


/**
 * This class is a <code>Comparable</code> wrapper of <code>CpInfo</code>
 * objects. It can store an index, in order to identify the constant pool
 * entry after it has been sorted. The comparison is primarily based on the
 * types of the constant pool entries, and secondarily on the contents of
 * the constant pool entries.
 *
 * @author Eric Lafortune
 */
class ComparableCpInfo implements Comparable, CpInfoVisitor
{
    private static int[] PRIORITIES = new int[13];
    static
    {
        PRIORITIES[ClassConstants.CONSTANT_Class]              = 0;
        PRIORITIES[ClassConstants.CONSTANT_Fieldref]           = 1;
        PRIORITIES[ClassConstants.CONSTANT_Methodref]          = 2;
        PRIORITIES[ClassConstants.CONSTANT_InterfaceMethodref] = 3;
        PRIORITIES[ClassConstants.CONSTANT_String]             = 4;
        PRIORITIES[ClassConstants.CONSTANT_Integer]            = 5;
        PRIORITIES[ClassConstants.CONSTANT_Float]              = 6;
        PRIORITIES[ClassConstants.CONSTANT_Long]               = 7;
        PRIORITIES[ClassConstants.CONSTANT_Double]             = 8;
        PRIORITIES[ClassConstants.CONSTANT_NameAndType]        = 9;
        PRIORITIES[ClassConstants.CONSTANT_Utf8]               = 10;
    }

    private ClassFile classFile;
    private int       thisIndex;
    private CpInfo    thisCpInfo;
    private CpInfo    otherCpInfo;
    private int       result;


    public ComparableCpInfo(ClassFile classFile, int index, CpInfo cpInfo)
    {
        this.classFile  = classFile;
        this.thisIndex  = index;
        this.thisCpInfo = cpInfo;
    }


    public int getIndex()
    {
        return thisIndex;
    }


    public CpInfo getCpInfo()
    {
        return thisCpInfo;
    }


    // Implementations for Comparable.

    public int compareTo(Object other)
    {
        ComparableCpInfo otherComparableCpInfo = (ComparableCpInfo)other;

        otherCpInfo = otherComparableCpInfo.thisCpInfo;

        // Compare based on the original indices, if the actual constant pool
        // entries are the same.
        if (thisCpInfo == otherCpInfo)
        {
            int otherIndex = otherComparableCpInfo.thisIndex;

            return thisIndex <  otherIndex ? -1 :
                   thisIndex == otherIndex ?  0 :
                                              1;
        }

        // Compare based on the tags, if they are different.
        int thisTag  = thisCpInfo.getTag();
        int otherTag = otherCpInfo.getTag();

        if (thisTag != otherTag)
        {
            return PRIORITIES[thisTag] < PRIORITIES[otherTag] ? -1 : 1;
        }

        // Otherwise compare based on the contents of the CpInfo objects.
        thisCpInfo.accept(classFile, this);

        return result;
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo)
    {
        // In JDK 1.4, we can use Integer.compare(a,b).
        result = new Integer(integerCpInfo.getValue()).compareTo(new Integer(((IntegerCpInfo)otherCpInfo).getValue()));
    }

    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo)
    {
        // In JDK 1.4, we can use Long.compare(a,b).
        result = new Long(longCpInfo.getValue()).compareTo(new Long(((LongCpInfo)otherCpInfo).getValue()));
    }

    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo)
    {
        // In JDK 1.4, we can use Float.compare(a,b).
        result = new Float(floatCpInfo.getValue()).compareTo(new Float(((FloatCpInfo)otherCpInfo).getValue()));
    }

    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo)
    {
        // In JDK 1.4, we can use Double.compare(a,b).
        result = new Double(doubleCpInfo.getValue()).compareTo(new Double(((DoubleCpInfo)otherCpInfo).getValue()));
    }

    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo)
    {
        result = stringCpInfo.getString(classFile).compareTo(((StringCpInfo)otherCpInfo).getString(classFile));
    }

    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo)
    {
        result = utf8CpInfo.getString().compareTo(((Utf8CpInfo)otherCpInfo).getString());
    }

    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo)
    {
        result = fieldrefCpInfo.getName(classFile).compareTo(((FieldrefCpInfo)otherCpInfo).getName(classFile));
    }

    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo)
    {
        result = interfaceMethodrefCpInfo.getName(classFile).compareTo(((InterfaceMethodrefCpInfo)otherCpInfo).getName(classFile));
    }

    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo)
    {
        result = methodrefCpInfo.getName(classFile).compareTo(((MethodrefCpInfo)otherCpInfo).getName(classFile));
    }

    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        result = classCpInfo.getName(classFile).compareTo(((ClassCpInfo)otherCpInfo).getName(classFile));
    }

    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo)
    {
        result = nameAndTypeCpInfo.getName(classFile).compareTo(((NameAndTypeCpInfo)otherCpInfo).getName(classFile));
    }
}
