/* $Id: DescriptorClassEnumeration.java,v 1.12.2.1 2006/01/16 22:57:55 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2006 Eric Lafortune (eric@graphics.cornell.edu)
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
package proguard.classfile.util;

import proguard.classfile.*;

/**
 * A <code>DescriptorClassEnumeration</code> provides an enumeration of all
 * classes mentioned in a given descriptor string.
 * <p>
 * A <code>DescriptorClassEnumeration</code> object can be reused for processing
 * different subsequent descriptors, by means of the <code>setDescriptor</code>
 * method.
 *
 * @author Eric Lafortune
 */
public class DescriptorClassEnumeration
{
    private String descriptor;
    private int    index;


    public DescriptorClassEnumeration(String descriptor)
    {
        setDescriptor(descriptor);
    }


    DescriptorClassEnumeration()
    {
    }


    void setDescriptor(String descriptor)
    {
        this.descriptor = descriptor;

        reset();
    }


    public void reset()
    {
        index = 0;
    }


    /**
     * Returns the number of classes contained in the descriptor. This
     * is the number of class names that the enumeration will return.
     */
    public int classCount()
    {
        int count = 0;

        while (nextClassNameStartIndex() >= 0)
        {
            count++;

            nextClassNameEndIndex();
        }

        index = 0;

        return count;
    }


    public boolean hasMoreClassNames()
    {
        return index >= 0 && nextClassNameStartIndex() >= 0;
    }


    public String nextFluff()
    {
        int fluffStartIndex = index;
        int fluffEndIndex   = nextClassNameStartIndex() + 1;

        // There may be fluff at the end of the descriptor.
        if (fluffEndIndex == 0)
        {
            fluffEndIndex = descriptor.length();
        }

        return descriptor.substring(fluffStartIndex, fluffEndIndex);
    }


    public String nextClassName()
    {
        int classNameStartIndex = nextClassNameStartIndex() + 1;
        int classNameEndIndex   = nextClassNameEndIndex();

        return descriptor.substring(classNameStartIndex, classNameEndIndex);
    }


    private int nextClassNameStartIndex()
    {
        index = descriptor.indexOf(ClassConstants.INTERNAL_TYPE_CLASS_START, index);

        return index;
    }


    private int nextClassNameEndIndex()
    {
        while (++index < descriptor.length())
        {
            char c = descriptor.charAt(index);
            if (c == ClassConstants.INTERNAL_TYPE_CLASS_END ||
                c == ClassConstants.INTERNAL_TYPE_GENERIC_START)
            {
                return index;
            }
        }

        throw new IllegalArgumentException("Missing class name terminator in descriptor ["+descriptor+"]");
    }




    /**
     * A main method for testing the class name enumeration.
     */
    public static void main(String[] args)
    {
        try
        {
            System.out.println("Descriptor ["+args[0]+"]");
            DescriptorClassEnumeration enumeration = new DescriptorClassEnumeration(args[0]);
            System.out.println("Class count = "+enumeration.classCount());
            System.out.println("  Fluff: ["+enumeration.nextFluff()+"]");
            while (enumeration.hasMoreClassNames())
            {
                System.out.println("  Name:  ["+enumeration.nextClassName()+"]");
                System.out.println("  Fluff: ["+enumeration.nextFluff()+"]");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
