/* $Id: MemberInfoObfuscator.java,v 1.14.2.3 2006/11/25 16:56:11 eric Exp $
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
import proguard.classfile.util.*;
import proguard.classfile.visitor.MemberInfoVisitor;

import java.util.*;

/**
 * This MemberInfoVisitor obfuscates all class members that it visits.
 * It uses names from the given name factory. At the same time, it avoids names
 * from the given descriptor map.
 * <p>
 * The class members must have been linked before applying this visitor.
 *
 * @see MethodInfoLinker
 *
 * @author Eric Lafortune
 */
public class MemberInfoObfuscator implements MemberInfoVisitor
{
    private boolean        allowAggressiveOverloading;
    private NameFactory    nameFactory;
    private Map            descriptorMap;
    private WarningPrinter warningPrinter;


    /**
     * Creates a new MemberInfoObfuscator.
     * @param allowAggressiveOverloading a flag that specifies whether class
     *                                   members can be overloaded aggressively.
     * @param nameFactory                the factory that can produce
     *                                   obfuscated member names.
     * @param descriptorMap              the map of descriptors to
     *                                   [new name - old name] maps.
     * @param warningPrinter             an optional warning printer to which
     *                                   warnings about conflicting name
     *                                   mappings can be printed.
     */
    public MemberInfoObfuscator(boolean        allowAggressiveOverloading,
                                NameFactory    nameFactory,
                                Map            descriptorMap,
                                WarningPrinter warningPrinter)
    {
        this.allowAggressiveOverloading = allowAggressiveOverloading;
        this.nameFactory                = nameFactory;
        this.descriptorMap              = descriptorMap;
        this.warningPrinter             = warningPrinter;
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

        // Check whether we're allowed to do aggressive overloading
        if (!allowAggressiveOverloading)
        {
            // Trim the return argument from the descriptor if not.
            // Works for fields and methods alike.
            descriptor = descriptor.substring(0, descriptor.indexOf(')')+1);
        }

        // Get the name map, creating a new one if necessary.
        Map nameMap = retrieveNameMap(descriptorMap, descriptor);

        // Get the member's new name.
        String newName = newMemberName(memberInfo);

        // Assign a new one, if necessary.
        if (newName == null)
        {
            // Find an acceptable new name.
            nameFactory.reset();

            do
            {
                newName = nameFactory.nextName();
            }
            while (nameMap.containsKey(newName));

            // Remember not to use the new name again in this name space.
            nameMap.put(newName, name);

            // Assign the new name.
            setNewMemberName(memberInfo, newName);
        }
        else
        {
            String previousName = (String)nameMap.get(newName);
            if (!name.equals(previousName))
            {
                // There's a conflict! A member (with a given old name) in a
                // first namespace has received the same new name as this
                // member (with a different old name) in a second name space,
                // and now these two have to live together in this name space.
                if (hasFixedNewMemberName(memberInfo) &&
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

                // TODO: Preferentially keep the fixed name.

                // Mark the name as conflicting.
                MemberInfoNameConflictFilter.markConflictingName(memberInfo);
            }
        }
    }


    // Small utility methods.

    /**
     * Gets the name map, based on the given map and a given descriptor.
     * A new empty map is created if necessary.
     * @param descriptorMap the map of descriptors to [new name - old name] maps.
     * @param descriptor    the class member descriptor.
     * @return the corresponding name map.
     */
    static Map retrieveNameMap(Map descriptorMap, String descriptor)
    {
        // See if we can find the nested map with this descriptor key.
        Map nameMap = (Map)descriptorMap.get(descriptor);

        // Create a new one if not.
        if (nameMap == null)
        {
            nameMap = new HashMap();
            descriptorMap.put(descriptor, nameMap);
        }

        return nameMap;
    }


    /**
     * Assigns a fixed new name to the given class member.
     * @param memberInfo the class member.
     * @param name       the new name.
     */
    static void setFixedNewMemberName(MemberInfo memberInfo, String name)
    {
        VisitorAccepter lastVisitorAccepter = MethodInfoLinker.lastVisitorAccepter(memberInfo);

        if (!(lastVisitorAccepter instanceof LibraryMemberInfo) &&
            !(lastVisitorAccepter instanceof MyFixedName))
        {
            lastVisitorAccepter.setVisitorInfo(new MyFixedName(name));
        }
        else
        {
            lastVisitorAccepter.setVisitorInfo(name);
        }
    }


    /**
     * Assigns a new name to the given class member.
     * @param memberInfo the class member.
     * @param name       the new name.
     */
    static void setNewMemberName(MemberInfo memberInfo, String name)
    {
        MethodInfoLinker.lastVisitorAccepter(memberInfo).setVisitorInfo(name);
    }


    /**
     * Returns whether the new name of the given class member is fixed.
     * @param memberInfo the class member.
     * @return whether its new name is fixed.
     */
    static boolean hasFixedNewMemberName(MemberInfo memberInfo)
    {
        VisitorAccepter lastVisitorAccepter = MethodInfoLinker.lastVisitorAccepter(memberInfo);

        return lastVisitorAccepter instanceof LibraryMemberInfo ||
               lastVisitorAccepter instanceof MyFixedName;
    }


    /**
     * Retrieves the new name of the given class member.
     * @param memberInfo the given class member.
     * @return the class member's new name, or <code>null</code> if it doesn't
     *         have one yet.
     */
    static String newMemberName(MemberInfo memberInfo)
    {
        return (String)MethodInfoLinker.lastVisitorAccepter(memberInfo).getVisitorInfo();
    }


    /**
     * This VisitorAccepter can be used to wrap a name string, to indicate that
     * the name is fixed.
     */
    private static class MyFixedName implements VisitorAccepter
    {
        private String newName;


        public MyFixedName(String newName)
        {
            this.newName = newName;
        }


        // Implementations for VisitorAccepter.

        public Object getVisitorInfo()
        {
            return newName;
        }


        public void setVisitorInfo(Object visitorInfo)
        {
            newName = (String)visitorInfo;
        }
    }
}
