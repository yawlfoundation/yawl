/* $Id: MemberInfoLinker.java,v 1.9 2004/11/20 15:41:24 eric Exp $
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
package proguard.obfuscate;

import proguard.classfile.*;
import proguard.classfile.visitor.*;

import java.util.*;


/**
 * This ClassFileVisitor links all methods that should get the same names
 * in the name spaces of all visited class files. A class file's name space
 * encompasses all of its subclasses and interfaces. It is typically a class file
 * that is not being subclassed. Chains of links that have been created in
 * previous invocations are merged with new chains of links, in order to create
 * a consistent set of chains. Class initialization methods and constructors are
 * ignored.
 *
 * @see MemberInfoObfuscator
 *
 * @author Eric Lafortune
 */
public class MemberInfoLinker
  implements ClassFileVisitor,
             MemberInfoVisitor
{
    // An object that is reset and reused every time.
    // The map: [class member name+descriptor - class member info]
    private final Map methodInfoMap = new HashMap();


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Collect all members in this class's name space.
        programClassFile.hierarchyAccept(true, true, true, false,
                                         new AllMemberInfoVisitor(this));

        // Clean up for obfuscation of the next name space.
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
        MemberInfo thisLastMemberInfo = lastMemberInfo(methodInfo);

        // See if we've already come across a method with the same name and
        // descriptor.
        String key = name + descriptor;
        MethodInfo otherMethodInfo = (MethodInfo)methodInfoMap.get(key);

        if (otherMethodInfo == null)
        {
            // Store the new class method info in the map.
            methodInfoMap.put(key, thisLastMemberInfo);
        }
        else
        {
            // Get the last method in the other chain.
            MemberInfo otherLastMemberInfo = lastMemberInfo(otherMethodInfo);

            // Check if both link chains aren't already ending in the same element.
            if (thisLastMemberInfo != otherLastMemberInfo)
            {
                // Merge the two chains, making sure LibraryMethodInfo elements,
                // if any, are at the end of the resulting chain.
                if (thisLastMemberInfo instanceof LibraryMethodInfo)
                {
                    // This class method chain ends with a library class method.
                    // Link this chain to the end of the other one.
                    otherLastMemberInfo.setVisitorInfo(thisLastMemberInfo);
                }
                /* We can skip this test and go straight to the final case.
                else if (otherLastVisitorAccepter instanceof LibraryMethodInfo)
                {
                    // The other method chain ends with a library class method.
                    // Link the other chain to the end of this one.
                    thisLastVisitorAccepter.setVisitorInfo(otherLastVisitorAccepter);
                }
                */
                else
                {
                    // We have two non-library methods. Link their chains
                    // one way or another.
                    thisLastMemberInfo.setVisitorInfo(otherLastMemberInfo);
                }
            }
        }
    }


    // Small utility methods.

    /**
     * Finds the last class member in the linked list of class members.
     * @param memberInfo the given class member.
     * @return the last class member in the linked list.
     */
    static MemberInfo lastMemberInfo(MemberInfo memberInfo)
    {
        VisitorAccepter lastVisitorAccepter = memberInfo;
        while (lastVisitorAccepter.getVisitorInfo() != null &&
               lastVisitorAccepter.getVisitorInfo() instanceof VisitorAccepter)
        {
            lastVisitorAccepter = (VisitorAccepter)lastVisitorAccepter.getVisitorInfo();
        }

        return (MemberInfo)lastVisitorAccepter;
    }
}
