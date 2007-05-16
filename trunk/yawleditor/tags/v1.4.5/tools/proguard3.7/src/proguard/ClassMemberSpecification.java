/* $Id: ClassMemberSpecification.java,v 1.3.2.1 2006/01/16 22:57:55 eric Exp $
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
package proguard;


/**
 * This class stores a specification of class members. The specification is
 * template-based: the class member names and descriptors can contain wildcards.
 *
 * @author Eric Lafortune
 */
public class ClassMemberSpecification
{
    public int    requiredSetAccessFlags;
    public int    requiredUnsetAccessFlags;
    public String name;
    public String descriptor;


    /**
     * Creates a new option to keep the specified class member(s).
     */
    public ClassMemberSpecification()
    {
        this(0,
             0,
             null,
             null);
    }


    /**
     * Creates a new option to keep the specified class member(s).
     *
     * @param requiredSetAccessFlags   the class access flags that must be set
     *                                 in order for the class to apply.
     * @param requiredUnsetAccessFlags the class access flags that must be unset
     *                                 in order for the class to apply.
     * @param name                     the class member name. The name may be
     *                                 null to specify any class member or it
     *                                 may contain "*" or "?" wildcards.
     * @param descriptor               the class member descriptor. The
     *                                 descriptor may be null to specify any
     *                                 class member or it may contain
     *                                 "**", "*", or "?" wildcards.
     */
    public ClassMemberSpecification(int    requiredSetAccessFlags,
                                 int    requiredUnsetAccessFlags,
                                 String name,
                                 String descriptor)
    {
        this.requiredSetAccessFlags   = requiredSetAccessFlags;
        this.requiredUnsetAccessFlags = requiredUnsetAccessFlags;
        this.name                     = name;
        this.descriptor               = descriptor;
    }



    // Implementations for Object.

    public boolean equals(Object object)
    {
        if (this.getClass() != object.getClass())
        {
            return false;
        }

        ClassMemberSpecification other = (ClassMemberSpecification)object;
        return
            (this.requiredSetAccessFlags   == other.requiredSetAccessFlags  ) &&
            (this.requiredUnsetAccessFlags == other.requiredUnsetAccessFlags) &&
            (this.name       == null ? other.name       == null : this.name.equals(other.name)            ) &&
            (this.descriptor == null ? other.descriptor == null : this.descriptor.equals(other.descriptor));
    }

    public int hashCode()
    {
        return
            requiredSetAccessFlags                           ^
            requiredUnsetAccessFlags                         ^
            (name       == null ? 0 : name.hashCode()      ) ^
            (descriptor == null ? 0 : descriptor.hashCode());
    }
}
