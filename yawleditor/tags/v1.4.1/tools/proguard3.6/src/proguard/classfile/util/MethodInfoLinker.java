/* $Id: MethodInfoLinker.java,v 1.2.2.1 2006/01/16 22:57:55 eric Exp $
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
import proguard.classfile.visitor.*;

import java.util.*;

/**
 * This ClassFileVisitor links all corresponding methods in the class hierarchies
 * of all visited class files. Visited class files are typically all class
 * files that are not being subclassed. Chains of links that have been created in
 * previous invocations are merged with new chains of links, in order to create
 * a consistent set of chains. Class initialization methods and constructors are
 * ignored.
 *
 * @author Eric Lafortune
 */
public class MethodInfoLinker
  implements ClassFileVisitor,
             MemberInfoVisitor
{
    // An object that is reset and reused every time.
    // The map: [class member name+descriptor - class member info]
    private final Map methodInfoMap = new HashMap();


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Collect all members in this class hierarchy.
        programClassFile.hierarchyAccept(true, true, true, false,
                                         new AllMemberInfoVisitor(this));

        // Clean up for the next class hierarchy.
        methodInfoMap.clear();
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        visitMethodInfo(programClassFile, programMethodInfo);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        visitMethodInfo(libraryClassFile, libraryMethodInfo);
    }


    /**
     * Links the given method into the chains of links. Class initialization
     * methods and constructors are ignored.
     * @param classFile  the class file of the given method.
     * @param methodInfo the method to be linked.
     */
    private void visitMethodInfo(ClassFile classFile, MethodInfo methodInfo)
    {
        // Private methods don't have to be linked.
        if ((methodInfo.getAccessFlags() & ClassConstants.INTERNAL_ACC_PRIVATE) != 0)
        {
            return;
        }

        // Get the method's original name and descriptor.
        String name       = methodInfo.getName(classFile);
        String descriptor = methodInfo.getDescriptor(classFile);

        // Special cases: <clinit> and <init> are always kept unchanged.
        // We can ignore them here.
        if (name.equals(ClassConstants.INTERNAL_METHOD_NAME_CLINIT) ||
            name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
        {
            return;
        }

        // Get the last method in the chain.
        MemberInfo thisLastMethodInfo = lastMethodInfo(methodInfo);

        // See if we've already come across a method with the same name and
        // descriptor.
        String key = name + descriptor;
        MethodInfo otherMethodInfo = (MethodInfo)methodInfoMap.get(key);

        if (otherMethodInfo == null)
        {
            // Store the new class method info in the map.
            methodInfoMap.put(key, thisLastMethodInfo);
        }
        else
        {
            // Get the last method in the other chain.
            MethodInfo otherLastMethodInfo = lastMethodInfo(otherMethodInfo);

            // Check if both link chains aren't already ending in the same element.
            if (!thisLastMethodInfo.equals(otherLastMethodInfo))
            {
                // Merge the two chains, with the library members last.
                if (otherLastMethodInfo instanceof LibraryMemberInfo)
                {
                    thisLastMethodInfo.setVisitorInfo(otherLastMethodInfo);
                }
                else
                {
                    otherLastMethodInfo.setVisitorInfo(thisLastMethodInfo);
                }
            }
        }
    }


    // Small utility methods.

    /**
     * Finds the last method in the linked list of related methods.
     * @param methodInfo the given method.
     * @return the last method in the linked list.
     */
    public static MethodInfo lastMethodInfo(MethodInfo methodInfo)
    {
        MethodInfo lastMethodInfo = methodInfo;
        while (lastMethodInfo.getVisitorInfo() != null &&
               lastMethodInfo.getVisitorInfo() instanceof MethodInfo)
        {
            lastMethodInfo = (MethodInfo)lastMethodInfo.getVisitorInfo();
        }

        return lastMethodInfo;
    }

    /**
     * Finds the last visitor accepter in the linked list of visitors.
     * @param visitorAccepter the given method.
     * @return the last method in the linked list.
     */
    public static VisitorAccepter lastVisitorAccepter(VisitorAccepter visitorAccepter)
    {
        VisitorAccepter lastVisitorAccepter = visitorAccepter;
        while (lastVisitorAccepter.getVisitorInfo() != null &&
               lastVisitorAccepter.getVisitorInfo() instanceof VisitorAccepter)
        {
            lastVisitorAccepter = (VisitorAccepter)lastVisitorAccepter.getVisitorInfo();
        }

        return lastVisitorAccepter;
    }
}
