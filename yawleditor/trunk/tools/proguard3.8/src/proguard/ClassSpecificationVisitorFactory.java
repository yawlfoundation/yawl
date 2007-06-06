/* $Id: ClassSpecificationVisitorFactory.java,v 1.7.2.1 2006/05/06 13:19:00 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2003 Eric Lafortune (eric@graphics.cornell.edu)
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

import proguard.classfile.visitor.*;

import java.util.*;


/**
 * This factory creates visitors to efficiently travel to specified classes and
 * class members.
 *
 * @author Eric Lafortune
 */
public class ClassSpecificationVisitorFactory
{
    /**
     * Creates a new ClassPoolVisitor to efficiently travel to the specified
     * classes and class members.
     *
     * @param classSpecifications the specifications of the classes and class
     *                            members to visit.
     * @param classFileVisitor    the ClassFileVisitor to be applied to matching
     *                            classes.
     * @param memberInfoVisitor   the MemberInfoVisitor to be applied to matching
     *                            class members.
     */
    public static ClassPoolVisitor createClassPoolVisitor(List              classSpecifications,
                                                          ClassFileVisitor  classFileVisitor,
                                                          MemberInfoVisitor memberInfoVisitor)
    {
        MultiClassPoolVisitor multiClassPoolVisitor = new MultiClassPoolVisitor();

        if (classSpecifications != null)
        {
            addClassPoolVisitors(classSpecifications,
                                 classFileVisitor,
                                 memberInfoVisitor,
                                 multiClassPoolVisitor);
        }

        return multiClassPoolVisitor;
    }


    /**
     * Adds new ClassPoolVisitor instances to the given MultiClassPoolVisitor,
     * to efficiently travel to the specified classes and class members.

     * @param classSpecifications   the specifications of the classes and class
     *                              members to visit.
     * @param classFileVisitor      the ClassFileVisitor to be applied to matching
     *                              classes.
     * @param memberInfoVisitor     the MemberInfoVisitor to be applied to matching
     *                              class members.
     * @param multiClassPoolVisitor the MultiClassPoolVisitor to which the new
     *                              visitors will be added.
     */
    private static void addClassPoolVisitors(List                  classSpecifications,
                                             ClassFileVisitor      classFileVisitor,
                                             MemberInfoVisitor     memberInfoVisitor,
                                             MultiClassPoolVisitor multiClassPoolVisitor)
    {
        for (int index = 0; index < classSpecifications.size(); index++)
        {
            multiClassPoolVisitor.addClassPoolVisitor(
                createClassPoolVisitor((ClassSpecification)classSpecifications.get(index),
                                       classFileVisitor,
                                       memberInfoVisitor));
        }
    }


    /**
     * Creates a new ClassPoolVisitor to efficiently travel to the specified
     * classes and class members.
     *
     * @param classSpecification the specifications of the class(es) and class
     *                           members to visit.
     * @param classFileVisitor   the ClassFileVisitor to be applied to matching
     *                           classes.
     * @param memberInfoVisitor  the MemberInfoVisitor to be applied to matching
     *                           class members.
     */
    private static ClassPoolVisitor createClassPoolVisitor(ClassSpecification classSpecification,
                                                           ClassFileVisitor   classFileVisitor,
                                                           MemberInfoVisitor  memberInfoVisitor)
    {
        // The class file visitor for class files and their members.
        MultiClassFileVisitor multiClassFileVisitor = new MultiClassFileVisitor();

        // If specified, let the class file visitor visit the class file itself.
        if ((classSpecification.markClassFiles ||
             classSpecification.markConditionally) &&
            classFileVisitor != null)
        {
            multiClassFileVisitor.addClassFileVisitor(classFileVisitor);
        }

        // If specified, let the member info visitor visit the class members.
        if ((classSpecification.fieldSpecifications  != null ||
             classSpecification.methodSpecifications != null) &&
            memberInfoVisitor != null)
        {
            multiClassFileVisitor.addClassFileVisitor(
                createClassFileVisitor(classSpecification, memberInfoVisitor));
        }

        // This visitor is the starting point.
        ClassFileVisitor composedClassFileVisitor = multiClassFileVisitor;

        // If specified, let the marker visit the class file and its class
        // members conditionally.
        if (classSpecification.markConditionally)
        {
            composedClassFileVisitor =
                createClassFileMemberInfoTester(classSpecification,
                                                composedClassFileVisitor);
        }

        // By default, start visiting from the class name, if it's specified.
        String className = classSpecification.className;

        // If wildcarded, only visit class files with matching names.
        if (className != null &&
            containsWildCards(className))
        {
            composedClassFileVisitor =
                new ClassFileNameFilter(composedClassFileVisitor,
                                        className);

            // We'll have to visit all classes now.
            className = null;
        }

        // If specified, only visit class files with the right access flags.
        if (classSpecification.requiredSetAccessFlags   != 0 ||
            classSpecification.requiredUnsetAccessFlags != 0)
        {
            composedClassFileVisitor =
                new ClassFileAccessFilter(classSpecification.requiredSetAccessFlags,
                                          classSpecification.requiredUnsetAccessFlags,
                                          composedClassFileVisitor);
        }

        // If it's specified, start visiting from the extended class.
        String extendsClassName = classSpecification.extendsClassName;

        if (className        == null &&
            extendsClassName != null)
        {
            composedClassFileVisitor =
                new ClassFileHierarchyTraveler(false, false, false, true,
                                               composedClassFileVisitor);

            // If wildcarded, only visit class files with matching names.
            if (containsWildCards(extendsClassName))
            {
                composedClassFileVisitor =
                    new ClassFileNameFilter(composedClassFileVisitor,
                                            extendsClassName);
            }
            else
            {
                // Start visiting from the extended class name.
                className = extendsClassName;
            }
        }

        // If specified, visit a single named class, otherwise visit all classes.
        return className != null ?
            (ClassPoolVisitor)new NamedClassFileVisitor(composedClassFileVisitor, className) :
            (ClassPoolVisitor)new AllClassFileVisitor(composedClassFileVisitor);
    }


    /**
     * Creates a new ClassPoolVisitor to efficiently travel to the specified class
     * members.
     *
     * @param classSpecification the specifications of the class members to visit.
     * @param memberInfoVisitor   the MemberInfoVisitor to be applied to matching
     *                            class members.
     */
    private static ClassFileVisitor createClassFileVisitor(ClassSpecification classSpecification,
                                                           MemberInfoVisitor  memberInfoVisitor)
    {
        MultiClassFileVisitor multiClassFileVisitor = new MultiClassFileVisitor();

        addMemberInfoVisitors(classSpecification.fieldSpecifications,  true,  multiClassFileVisitor, memberInfoVisitor);
        addMemberInfoVisitors(classSpecification.methodSpecifications, false, multiClassFileVisitor, memberInfoVisitor);

        // Mark the class member in this class and in super classes.
        return new ClassFileHierarchyTraveler(true, true, false, false,
                                              multiClassFileVisitor);
    }


    /**
     * Adds elements to the given MultiClassFileVisitor, to apply the given
     * MemberInfoVisitor to all class members that match the given List
     * of options (of the given type).
     */
    private static void addMemberInfoVisitors(List                  classMemberSpecifications,
                                              boolean               isField,
                                              MultiClassFileVisitor multiClassFileVisitor,
                                              MemberInfoVisitor     memberInfoVisitor)
    {
        if (classMemberSpecifications != null)
        {
            for (int index = 0; index < classMemberSpecifications.size(); index++)
            {
                ClassMemberSpecification classMemberSpecification =
                    (ClassMemberSpecification)classMemberSpecifications.get(index);

                multiClassFileVisitor.addClassFileVisitor(
                    createClassFileVisitor(classMemberSpecification,
                                           isField,
                                           memberInfoVisitor));
            }
        }
    }


    /**
     * Constructs a ClassFileVisitor that conditionally applies the given
     * ClassFileVisitor to all classes that contain the given class members.
     */
    private static ClassFileVisitor createClassFileMemberInfoTester(ClassSpecification classSpecification,
                                                                    ClassFileVisitor   classFileVisitor)
    {
        // Create a linked list of conditional visitors, for fields and for
        // methods.
        return createClassFileMemberInfoTester(classSpecification.fieldSpecifications,
                                               true,
               createClassFileMemberInfoTester(classSpecification.methodSpecifications,
                                               false,
                                               classFileVisitor));
    }


    /**
     * Constructs a ClassFileVisitor that conditionally applies the given
     * ClassFileVisitor to all classes that contain the given List of class
     * members (of the given type).
     */
    private static ClassFileVisitor createClassFileMemberInfoTester(List             classMemberSpecifications,
                                                                    boolean          isField,
                                                                    ClassFileVisitor classFileVisitor)
    {
        // Create a linked list of conditional visitors.
        if (classMemberSpecifications != null)
        {
            for (int index = 0; index < classMemberSpecifications.size(); index++)
            {
                ClassMemberSpecification classMemberSpecification =
                    (ClassMemberSpecification)classMemberSpecifications.get(index);

                classFileVisitor =
                    createClassFileVisitor(classMemberSpecification,
                                           isField,
                                           new ClassFileMemberInfoVisitor(classFileVisitor));
            }
        }

        return classFileVisitor;
    }


    /**
     * Creates a new ClassFileVisitor to efficiently travel to the specified class
     * members.
     *
     * @param classMemberSpecification the specification of the class member(s)
     *                                 to visit.
     * @param memberInfoVisitor        the MemberInfoVisitor to be applied to
     *                                 matching class member(s).
     */
    private static ClassFileVisitor createClassFileVisitor(ClassMemberSpecification classMemberSpecification,
                                                           boolean                  isField,
                                                           MemberInfoVisitor        memberInfoVisitor)
    {
        String name       = classMemberSpecification.name;
        String descriptor = classMemberSpecification.descriptor;

        // If name or descriptor are not fully specified, only visit matching
        // class members.
        boolean fullySpecified =
            name       != null &&
            descriptor != null &&
            !containsWildCards(name) &&
            !containsWildCards(descriptor);

        if (!fullySpecified)
        {
            if (descriptor != null)
            {
                memberInfoVisitor =
                    new MemberInfoDescriptorFilter(descriptor, memberInfoVisitor);
            }

            if (name != null)
            {
                memberInfoVisitor =
                    new MemberInfoNameFilter(name, memberInfoVisitor);
            }
        }

        // If any access flags are specified, only visit matching class members.
        if (classMemberSpecification.requiredSetAccessFlags   != 0 ||
            classMemberSpecification.requiredUnsetAccessFlags != 0)
        {
            memberInfoVisitor =
                new MemberInfoAccessFilter(classMemberSpecification.requiredSetAccessFlags,
                                           classMemberSpecification.requiredUnsetAccessFlags,
                                           memberInfoVisitor);
        }

        // Depending on what's specified, visit a single named class member,
        // or all class members, filtering the matching ones.
        return isField ?
            fullySpecified ?
                (ClassFileVisitor)new NamedFieldVisitor(name, descriptor, memberInfoVisitor) :
                (ClassFileVisitor)new AllFieldVisitor(memberInfoVisitor) :
            fullySpecified ?
                (ClassFileVisitor)new NamedMethodVisitor(name, descriptor, memberInfoVisitor) :
                (ClassFileVisitor)new AllMethodVisitor(memberInfoVisitor);
    }


    // Small utility methods.

    private static boolean containsWildCards(String string)
    {
        return string != null &&
            (string.indexOf('*') >= 0 ||
             string.indexOf('?') >= 0 ||
             string.indexOf('%') >= 0 ||
             string.indexOf(',') >= 0);
    }
}
