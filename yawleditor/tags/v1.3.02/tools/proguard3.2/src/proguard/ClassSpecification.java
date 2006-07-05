/* $Id: ClassSpecification.java,v 1.3 2004/08/28 17:03:30 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2004 Eric Lafortune (eric@graphics.cornell.edu)
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

import java.util.*;


/**
 * This class stores a specification of classes and possibly class members.
 * The specification is template-based: the class names and class member names
 * and descriptors can contain wildcards. Classes can be specified explicitly,
 * or as extensions or implementations in the class hierarchy.
 *
 * @author Eric Lafortune
 */
public class ClassSpecification implements Cloneable
{
    public int     requiredSetAccessFlags;
    public int     requiredUnsetAccessFlags;
    public String  className;
    public String  extendsClassName;
    public boolean markClassFiles;
    public boolean markConditionally;
    public String  comments;

    public List    fieldSpecifications;
    public List    methodSpecifications;


    /**
     * Creates a new option to keep all possible class(es).
     * The option doesn't have comments.
     */
    public ClassSpecification()
    {
        this(0,
             0,
             null,
             null,
             true,
             false);
    }


    /**
     * Creates a new option to keep the specified class(es).
     * The option doesn't have comments.
     *
     * @param requiredSetAccessFlags   the class access flags that must be set
     *                                 in order for the class to apply.
     * @param requiredUnsetAccessFlags the class access flags that must be unset
     *                                 in order for the class to apply.
     * @param className                the class name. The name may be null to
     *                                 specify any class, or it may contain
     *                                 "**", "*", or "?" wildcards.
     * @param extendsClassName         the name of the class that the class must
     *                                 extend or implement in order to apply.
     *                                 The name may be null to specify any class.
     * @param markClassFiles           specifies whether to mark the class files.
     *                                 If false, only class members are marked.
     *                                 If true, the class files are marked as
     *                                 well.
     * @param markConditionally        specifies whether to mark the class files
     *                                 and class members conditionally.
     *                                 If true, class files and class members
     *                                 are marked, on the condition that all
     *                                 specified class members are present.
     */
    public ClassSpecification(int     requiredSetAccessFlags,
                               int     requiredUnsetAccessFlags,
                               String  className,
                               String  extendsClassName,
                               boolean markClassFiles,
                               boolean markConditionally)
    {
        this(requiredSetAccessFlags,
             requiredUnsetAccessFlags,
             className,
             extendsClassName,
             markClassFiles,
             markConditionally,
             null);
    }


    /**
     * Creates a new option to keep the specified class(es).
     *
     * @param requiredSetAccessFlags   the class access flags that must be set
     *                                 in order for the class to apply.
     * @param requiredUnsetAccessFlags the class access flags that must be unset
     *                                 in order for the class to apply.
     * @param className                the class name. The name may be null to
     *                                 specify any class, or it may contain
     *                                 "**", "*", or "?" wildcards.
     * @param extendsClassName         the name of the class that the class must
     *                                 extend or implement in order to apply.
     *                                 The name may be null to specify any class.
     * @param markClassFiles           specifies whether to mark the class files.
     *                                 If false, only class members are marked.
     *                                 If true, the class files are marked as
     *                                 well.
     * @param markConditionally        specifies whether to mark the class files
     *                                 and class members conditionally.
     *                                 If true, class files and class members
     *                                 are marked, on the condition that all
     *                                 specified class members are present.
     * @param comments                 provides optional comments on this option.
     */
    public ClassSpecification(int     requiredSetAccessFlags,
                               int     requiredUnsetAccessFlags,
                               String  className,
                               String  extendsClassName,
                               boolean markClassFiles,
                               boolean markConditionally,
                               String  comments)
    {
        this.requiredSetAccessFlags   = requiredSetAccessFlags;
        this.requiredUnsetAccessFlags = requiredUnsetAccessFlags;
        this.className                = className;
        this.extendsClassName         = extendsClassName;
        this.markClassFiles           = markClassFiles;
        this.markConditionally        = markConditionally;
        this.comments                 = comments;
    }


    /**
     * Specifies to keep the specified field(s) of this option's class(es).
     *
     * @param fieldSpecification the field specification.
     */
    public void addField(ClassMemberSpecification fieldSpecification)
    {
        if (fieldSpecifications == null)
        {
            fieldSpecifications = new ArrayList();
        }

        fieldSpecifications.add(fieldSpecification);
    }


    /**
     * Specifies to keep the specified method(s) of this option's class(es).
     *
     * @param methodSpecification the method specification.
     */
    public void addMethod(ClassMemberSpecification methodSpecification)
    {
        if (methodSpecifications == null)
        {
            methodSpecifications = new ArrayList();
        }

        methodSpecifications.add(methodSpecification);
    }



    // Implementations for Object.

    public boolean equals(Object object)
    {
        if (this.getClass() != object.getClass())
        {
            return false;
        }

        ClassSpecification other = (ClassSpecification)object;
        return
            (this.requiredSetAccessFlags   == other.requiredSetAccessFlags  ) &&
            (this.requiredUnsetAccessFlags == other.requiredUnsetAccessFlags) &&
            (this.markClassFiles           == other.markClassFiles          ) &&
            (this.markConditionally        == other.markConditionally       ) &&
            (this.className            == null ? other.className            == null : this.className.equals(other.className))                     &&
            (this.extendsClassName     == null ? other.extendsClassName     == null : this.extendsClassName.equals(other.extendsClassName))       &&
            (this.fieldSpecifications  == null ? other.fieldSpecifications  == null : this.fieldSpecifications.equals(other.fieldSpecifications)) &&
            (this.methodSpecifications == null ? other.methodSpecifications == null : this.methodSpecifications.equals(other.methodSpecifications));
    }

    public int hashCode()
    {
        return
            requiredSetAccessFlags                                               ^
            requiredUnsetAccessFlags                                             ^
            (className            == null ? 0 : className.hashCode()           ) ^
            (extendsClassName     == null ? 0 : extendsClassName.hashCode()    ) ^
            (markClassFiles               ? 0 : 1                              ) ^
            (markConditionally            ? 0 : 2                              ) ^
            (fieldSpecifications  == null ? 0 : fieldSpecifications.hashCode() ) ^
            (methodSpecifications == null ? 0 : methodSpecifications.hashCode());
    }

    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            return null;
        }
    }
}
