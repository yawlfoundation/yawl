/* $Id: InternalTypeEnumeration.java,v 1.9.2.1 2006/01/16 22:57:55 eric Exp $
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
 * An <code>InternalTypeEnumeration</code> provides an enumeration of all
 * types listed in a given internal descriptor string. The return type can
 * retrieved separately.
 * <p>
 * A <code>InternalTypeEnumeration</code> object can be reused for processing
 * different subsequent descriptors, by means of the <code>setDescriptor</code>
 * method.
 *
 * @author Eric Lafortune
 */
public class InternalTypeEnumeration
{
    private String descriptor;
    private int    index;


    public InternalTypeEnumeration(String descriptor)
    {
        setDescriptor(descriptor);
    }


    public InternalTypeEnumeration()
    {
    }


    void setDescriptor(String descriptor)
    {
        this.descriptor = descriptor;

        reset();
    }


    public void reset()
    {
        index = descriptor.indexOf(ClassConstants.INTERNAL_METHOD_ARGUMENTS_OPEN) + 1;

        if (index < 1)
        {
            throw new IllegalArgumentException("Missing opening parenthesis in descriptor ["+descriptor+"]");
        }
    }


    public boolean hasMoreTypes()
    {
        return descriptor.charAt(index) != ClassConstants.INTERNAL_METHOD_ARGUMENTS_CLOSE;
    }


    public String nextType()
    {
        int startIndex = index;

        // Include all leading array characters.
        while (descriptor.charAt(index) == ClassConstants.INTERNAL_TYPE_ARRAY)
        {
            index++;
        }

        // Class types consist of an entire string.
        if (descriptor.charAt(index) == ClassConstants.INTERNAL_TYPE_CLASS_START)
        {
            index = descriptor.indexOf(ClassConstants.INTERNAL_TYPE_CLASS_END,
                                       index + 1);
            if (index < 0)
            {
                throw new IllegalArgumentException("Missing closing class type in descriptor ["+descriptor+"]");
            }
        }

        return descriptor.substring(startIndex, ++index);
    }


    public String returnType()
    {
        return descriptor.substring(descriptor.indexOf(ClassConstants.INTERNAL_METHOD_ARGUMENTS_CLOSE) + 1);
    }
}
