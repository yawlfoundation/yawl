/* $Id: MemberInfoNameConflictFixer.java,v 1.1.2.2 2007/01/31 21:50:12 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2007 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.obfuscate;

import proguard.classfile.util.*;
import proguard.classfile.visitor.MemberInfoVisitor;
import proguard.classfile.*;

import java.util.*;

/**
 * This MemberInfoVisitor solves obfuscation naming conflicts in all class
 * members that it visits. It avoids names from the given descriptor map,
 * delegating to the given obfuscator in order to get a new name if necessary.
 *
 * @author Eric Lafortune
 */
public class MemberInfoNameConflictFixer implements MemberInfoVisitor
{
    private boolean              allowAggressiveOverloading;
    private Map                  descriptorMap;
    private WarningPrinter       warningPrinter;
    private MemberInfoObfuscator memberInfoObfuscator;


    /**
     * Creates a new MemberInfoNameConflictFixer.
     * @param allowAggressiveOverloading a flag that specifies whether class
     *                                   members can be overloaded aggressively.
     * @param descriptorMap              the map of descriptors to
     *                                   [new name - old name] maps.
     * @param warningPrinter             an optional warning printer to which
     *                                   warnings about conflicting name
     *                                   mappings can be printed.
     * @param memberInfoObfuscator       the obfuscator that can assign new
     *                                   names to members with conflicting
     *                                   names.
     */
    public MemberInfoNameConflictFixer(boolean              allowAggressiveOverloading,
                                       Map                  descriptorMap,
                                       WarningPrinter       warningPrinter,
                                       MemberInfoObfuscator memberInfoObfuscator)
    {
        this.allowAggressiveOverloading = allowAggressiveOverloading;
        this.descriptorMap              = descriptorMap;
        this.warningPrinter             = warningPrinter;
        this.memberInfoObfuscator       = memberInfoObfuscator;
    }




    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        visitMemberInfo(programClassFile, programFieldInfo, true);
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        // Special cases: <clinit> and <init> are always kept unchanged.
        // We can ignore them here.
        String name = programMethodInfo.getName(programClassFile);
        if (name.equals(ClassConstants.INTERNAL_METHOD_NAME_CLINIT) ||
            name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
        {
            return;
        }

        visitMemberInfo(programClassFile, programMethodInfo, false);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}
    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo) {}


    /**
     * Obfuscates the given class member.
     * @param classFile  the class file of the given member.
     * @param memberInfo the class member to be obfuscated.
     * @param isField    speficies whether the class member is a field.
     */
    private void visitMemberInfo(ClassFile  classFile,
                                 MemberInfo memberInfo,
                                 boolean    isField)
    {
        // Get the member's name and descriptor.
        String name       = memberInfo.getName(classFile);
        String descriptor = memberInfo.getDescriptor(classFile);

        // Check whether we're allowed to overload aggressively.
        if (!allowAggressiveOverloading)
        {
            // Trim the return argument from the descriptor if not.
            // Works for fields and methods alike.
            descriptor = descriptor.substring(0, descriptor.indexOf(')')+1);
        }

        // Get the name map, creating a new one if necessary.
        Map nameMap = MemberInfoObfuscator.retrieveNameMap(descriptorMap, descriptor);

        // Get the member's new name.
        String newName = MemberInfoObfuscator.newMemberName(memberInfo);

        String previousName = (String)nameMap.get(newName);
        if (!name.equals(previousName))
        {
            // There's a conflict! A member (with a given old name) in a
            // first namespace has received the same new name as this
            // member (with a different old name) in a second name space,
            // and now these two have to live together in this name space.
            if (MemberInfoObfuscator.hasFixedNewMemberName(memberInfo) &&
                warningPrinter != null)
            {
                descriptor = memberInfo.getDescriptor(classFile);
                warningPrinter.print("Warning: " + ClassUtil.externalClassName(classFile.getName()) +
                                     (isField ?
                                         ": field '" + ClassUtil.externalFullFieldDescription(0, name, descriptor) :
                                         ": method '" + ClassUtil.externalFullMethodDescription(classFile.getName(), 0, name, descriptor)) +
                                     "' can't be mapped to '" + newName +
                                     "' because it would conflict with " +
                                     (isField ?
                                         "field '" :
                                         "method '" ) + previousName +
                                     "', which is already being mapped to '" + newName + "'");
            }

            // Clear the conflicting name.
            MemberInfoObfuscator.setNewMemberName(memberInfo, null);

            // Assign a new name.
            memberInfo.accept(classFile, memberInfoObfuscator);
        }
    }
}
