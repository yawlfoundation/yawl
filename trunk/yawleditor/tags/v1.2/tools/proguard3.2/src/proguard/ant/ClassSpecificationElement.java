/* $Id: ClassSpecificationElement.java,v 1.3 2004/12/18 20:21:43 eric Exp $
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
package proguard.ant;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import proguard.*;
import proguard.classfile.*;
import proguard.classfile.util.*;

import java.util.*;

/**
 * This DataType represents a class specification in Ant.
 *
 * @author Eric Lafortune
 */
public class ClassSpecificationElement extends DataType
{
    private static final String ANY_CLASS_KEYWORD  = "*";

    private String access;
    private String type;
    private String name;
    private String extends_;
    private List   fieldSpecifications  = new ArrayList();
    private List   methodSpecifications = new ArrayList();


    /**
     * Adds the contents of this class specification element to the given list.
     * @param classSpecifications the class specifications to be extended.
     * @param markClassFiles      specifies whether to mark the class files.
     * @param markConditionally   specifies whether to mark the class files
     *                            and class members conditionally.
     */
    public void appendTo(List    classSpecifications,
                         boolean markClassFiles,
                         boolean markConditionally)
    {
        // Get the referenced file set, or else this one.
        ClassSpecificationElement classSpecificationElement = isReference() ?
            (ClassSpecificationElement)getCheckedRef(this.getClass(),
                                                     this.getClass().getName()) :
            this;

        // Create a new class specification.
        String access   = classSpecificationElement.access;
        String type     = classSpecificationElement.type;
        String name     = classSpecificationElement.name;
        String extends_ = classSpecificationElement.extends_;

        // For backward compatibility, allow a single "*" wildcard to match
        // any class.
        if (name != null &&
            name.equals(ANY_CLASS_KEYWORD))
        {
            name = null;
        }

        ClassSpecification classSpecification =
            new ClassSpecification(requiredAccessFlags(true,  access, type),
                                   requiredAccessFlags(false, access, type),
                                   name     != null ? ClassUtil.internalClassName(name)     : null,
                                   extends_ != null ? ClassUtil.internalClassName(extends_) : null,
                                   markClassFiles,
                                   markConditionally);

        for (int index = 0; index < fieldSpecifications.size(); index++)
        {
            classSpecification.addField((ClassMemberSpecification)fieldSpecifications.get(index));
        }

        for (int index = 0; index < methodSpecifications.size(); index++)
        {
            classSpecification.addMethod((ClassMemberSpecification)methodSpecifications.get(index));
        }

        // Add it to the list.
        classSpecifications.add(classSpecification);
    }


    // Ant task attributes.

    public void setAccess(String access)
    {
        this.access = access;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public void setExtends(String extends_)
    {
        this.extends_ = extends_;
    }


    public void setImplements(String implements_)
    {
        this.extends_ = implements_;
    }


    // Ant task nested elements.

    public void addConfiguredField(ClassMemberSpecificationElement classMemberSpecificationElement)
    {
        if (fieldSpecifications == null)
        {
            fieldSpecifications = new ArrayList();
        }

        classMemberSpecificationElement.appendTo(fieldSpecifications,
                                                 false,
                                                 false);
    }


    public void addConfiguredMethod(ClassMemberSpecificationElement classMemberSpecificationElement)
    {
        if (methodSpecifications == null)
        {
            methodSpecifications = new ArrayList();
        }

        classMemberSpecificationElement.appendTo(methodSpecifications,
                                                 true,
                                                 false);
    }


    public void addConfiguredConstructor(ClassMemberSpecificationElement classMemberSpecificationElement)
    {
        if (methodSpecifications == null)
        {
            methodSpecifications = new ArrayList();
        }

        classMemberSpecificationElement.appendTo(methodSpecifications,
                                                 true,
                                                 true);
    }


    // Small utility methods.

    private int requiredAccessFlags(boolean set,
                                    String  access,
                                    String  type)
    throws BuildException
    {
        int accessFlags = 0;

        if (access != null)
        {
            StringTokenizer tokenizer = new StringTokenizer(access, " ,");
            while (tokenizer.hasMoreTokens())
            {
                String token = tokenizer.nextToken();

                if (token.startsWith("!") ^ set)
                {
                    String strippedToken = token.startsWith("!") ?
                        token.substring(1) :
                        token;

                    int accessFlag =
                        strippedToken.equals(ClassConstants.EXTERNAL_ACC_PUBLIC)   ? ClassConstants.INTERNAL_ACC_PUBLIC   :
                        strippedToken.equals(ClassConstants.EXTERNAL_ACC_FINAL)    ? ClassConstants.INTERNAL_ACC_FINAL    :
                        strippedToken.equals(ClassConstants.EXTERNAL_ACC_ABSTRACT) ? ClassConstants.INTERNAL_ACC_ABSTRACT :
                        0;

                    if (accessFlag == 0)
                    {
                        throw new BuildException("Incorrect class access modifier ["+strippedToken+"]");
                    }

                    accessFlags |= accessFlag;
                }
            }
        }

        if (type != null && (type.startsWith("!") ^ set))
        {
            int accessFlag =
                type.equals(      ClassConstants.EXTERNAL_ACC_INTERFACE) ||
                type.equals("!" + ClassConstants.EXTERNAL_ACC_INTERFACE) ? ClassConstants.INTERNAL_ACC_INTERFACE :
                type.equals("class")                                     ? 0                                     :
                                                                           -1;
            if (accessFlag == -1)
            {
                throw new BuildException("Incorrect class type ["+type+"]");
            }

            accessFlags |= accessFlag;
        }

        return accessFlags;
    }
}
