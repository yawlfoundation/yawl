/* $Id: MethodInfoLinker.java,v 1.2.2.4 2007/01/18 21:31:51 eric Exp $
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
package proguard.classfile.util;

import proguard.classfile.*;
import proguard.classfile.visitor.*;

import java.util.*;

/**
 * This ClassFileVisitor links all corresponding non-private methods in the class
 * hierarchies of all visited classes. Visited classes are typically all class
 * files that are not being subclassed. Chains of links that have been created
 * in previous invocations are merged with new chains of links, in order to
 * create a consistent set of chains.
 * <p>
 * As a MemberInfoVisitor, it links all corresponding methods that it ever
 * visits.
 *
 * @author Eric Lafortune
 */
public class MethodInfoLinker
  implements ClassFileVisitor,
             MemberInfoVisitor
{
    // An object that is reset and reused every time.
    // The map: [class member name+' '+descriptor - class member info]
    private final Map memberInfoMap = new HashMap();


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        visitClassFile(programClassFile);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        visitClassFile(libraryClassFile);
    }


    /**
     * Links all corresponding methods in the given class file and all of its
     * super classes and interfaces.
     */
    private void visitClassFile(ClassFile classFile)
    {
        // Collect all non-private members in this class hierarchy.
        classFile.hierarchyAccept(true, true, true, false,
            new AllMemberInfoVisitor(
            new MemberInfoAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
            this)));

        // Clean up for the next class hierarchy.
        memberInfoMap.clear();
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        visitMemberInfo(programClassFile, programFieldInfo);
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        visitMemberInfo(programClassFile, programMethodInfo);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
        visitMemberInfo(libraryClassFile, libraryFieldInfo);
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        visitMemberInfo(libraryClassFile, libraryMethodInfo);
    }


    /**
     * Links the given member into the chains of links. Class initialization
     * methods and constructors are ignored.
     * @param classFile  the class file of the given method.
     * @param memberInfo the method to be linked.
     */
    private void visitMemberInfo(ClassFile classFile, MemberInfo memberInfo)
    {
        // Get the method's original name and descriptor.
        String name       = memberInfo.getName(classFile);
        String descriptor = memberInfo.getDescriptor(classFile);

        // Special cases: <clinit> and <init> are always kept unchanged.
        // We can ignore them here.
        if (name.equals(ClassConstants.INTERNAL_METHOD_NAME_CLINIT) ||
            name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
        {
            return;
        }

        // Get the last member in the chain.
        MemberInfo thisLastMemberInfo = lastMemberInfo(memberInfo);

        // See if we've already come across a member with the same name and
        // descriptor.
        String key = name + ' ' + descriptor;
        MemberInfo otherMemberInfo = (MemberInfo)memberInfoMap.get(key);

        if (otherMemberInfo == null)
        {
            // Store the new class member info in the map.
            memberInfoMap.put(key, thisLastMemberInfo);
        }
        else
        {
            // Get the last member in the other chain.
            MemberInfo otherLastMemberInfo = lastMemberInfo(otherMemberInfo);

            // Check if both link chains aren't already ending in the same element.
            if (!thisLastMemberInfo.equals(otherLastMemberInfo))
            {
                // Merge the two chains, with the library members last.
                if (otherLastMemberInfo instanceof LibraryMemberInfo)
                {
                    thisLastMemberInfo.setVisitorInfo(otherLastMemberInfo);
                }
                else
                {
                    otherLastMemberInfo.setVisitorInfo(thisLastMemberInfo);
                }
            }
        }
    }


    // Small utility methods.

    /**
     * Finds the last member in the linked list of related members.
     * @param memberInfo the given member.
     * @return the last member in the linked list.
     */
    public static MemberInfo lastMemberInfo(MemberInfo memberInfo)
    {
        MemberInfo lastMemberInfo = memberInfo;
        while (lastMemberInfo.getVisitorInfo() != null &&
               lastMemberInfo.getVisitorInfo() instanceof MemberInfo)
        {
            lastMemberInfo = (MemberInfo)lastMemberInfo.getVisitorInfo();
        }

        return lastMemberInfo;
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
