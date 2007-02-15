/* $Id: MemberInfoNameCollector.java,v 1.3.2.2 2006/06/07 22:36:52 eric Exp $
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
package proguard.obfuscate;

import proguard.classfile.*;
import proguard.classfile.util.MethodInfoLinker;
import proguard.classfile.visitor.MemberInfoVisitor;

import java.util.Map;

/**
 * This MemberInfoVisitor collects all new (obfuscation) names of the members
 * that it visits.
 *
 * @see MethodInfoLinker
 * @see MemberInfoObfuscator
 *
 * @author Eric Lafortune
 */
public class MemberInfoNameCollector implements MemberInfoVisitor
{
    private boolean allowAggressiveOverloading;
    private Map     descriptorMap;


    /**
     * Creates a new MemberInfoNameCollector.
     * @param allowAggressiveOverloading a flag that specifies whether class
     *                                   members can be overloaded aggressively.
     * @param descriptorMap              the map of descriptors to
     *                                   [new name - old name] maps.
     */
    public MemberInfoNameCollector(boolean allowAggressiveOverloading,
                                   Map     descriptorMap)
    {
        this.allowAggressiveOverloading = allowAggressiveOverloading;
        this.descriptorMap              = descriptorMap;
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        collectName(programClassFile, programFieldInfo);
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        collectName(programClassFile, programMethodInfo);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
        collectName(libraryClassFile, libraryFieldInfo);
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        collectName(libraryClassFile, libraryMethodInfo);
    }


    /**
     * Inserts the new name of the given class member into the map.
     * @param classFile  the class file of the given member.
     * @param memberInfo the class member to be linked.
     */
    private void collectName(ClassFile classFile, MemberInfo memberInfo)
    {
        // Special cases: <clinit> and <init> are always kept unchanged.
        // We can ignore them here.
        String name = memberInfo.getName(classFile);
        if (name.equals(ClassConstants.INTERNAL_METHOD_NAME_CLINIT) ||
            name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
        {
            return;
        }

        // Get the member's new name.
        String newName = MemberInfoObfuscator.newMemberName(memberInfo);

        // Remember it, if it has already been set.
        if (newName != null)
        {
            // Get the member's descriptor.
            String descriptor = memberInfo.getDescriptor(classFile);

            // Check whether we're allowed to do aggressive overloading
            if (!allowAggressiveOverloading)
            {
                // Trim the return argument from the descriptor if not.
                // Works for fields and methods alike.
                descriptor = descriptor.substring(0, descriptor.indexOf(')')+1);
            }

            // Put the [descriptor - new name] in the map,
            // creating a new [new name - old name] map if necessary.
            Map nameMap = MemberInfoObfuscator.retrieveNameMap(descriptorMap, descriptor);

            // Is the other original name different from this original name?
            if (nameMap.get(newName) == null ||
                MemberInfoObfuscator.hasFixedNewMemberName(memberInfo))
            {
                // Remember not to use the new name again in this name space.
                nameMap.put(newName, name);
            }
        }
    }
}
