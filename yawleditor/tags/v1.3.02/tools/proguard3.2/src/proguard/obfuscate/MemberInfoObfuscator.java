/* $Id: MemberInfoObfuscator.java,v 1.12 2004/12/11 16:35:23 eric Exp $
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

import java.io.IOException;
import java.util.*;


/**
 * This ClassFileVisitor obfuscates all class members in the name spaces of all
 * visited class file. The class members must have been linked before applying this
 * visitor. The class file is typically a class file that is not being subclassed.
 *
 * @see MemberInfoLinker
 *
 * @author Eric Lafortune
 */
public class MemberInfoObfuscator implements ClassFileVisitor
{
    private static final char UNIQUE_SUFFIX = '_';

    private boolean allowAggressiveOverloading;

    private NameFactory nameFactory       = new SimpleNameFactory();
    private NameFactory uniqueNameFactory = new SimpleNameFactory();

    // Some objects that are reset and reused every time.

    // The main maps: [class member descriptor - new name - name]
    private final Map nonPrivateDescriptorMap = new HashMap();
    private final Map privateDescriptorMap    = new HashMap();



    /**
     * Creates a new MemberObfuscator.
     * @param allowAggressiveOverloading a flag that specifies whether class
     *                                   members can be overloaded aggressively.
     * @param obfuscationDictionary      the optional name of a file from which
     *                                   obfuscated method names can be read.
     */
    public MemberInfoObfuscator(boolean allowAggressiveOverloading,
                                String  obfuscationDictionary)
    throws IOException
    {
        this.allowAggressiveOverloading = allowAggressiveOverloading;

        // Get names from the obfuscation dictionary, if specified.
        if (obfuscationDictionary != null)
        {
            nameFactory = new ReadNameFactory(obfuscationDictionary, nameFactory);
        }
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Collect all preset new member names in this class's name space.
        // This actually includes preset new private member names.
        programClassFile.hierarchyAccept(true, true, true, false,
                                         new AllMemberInfoVisitor(
                                         new MyNewNameCollector(nonPrivateDescriptorMap)));

        // Assign new names to all non-private members in this class's name space.
        programClassFile.hierarchyAccept(true, true, true, false,
                                         new MyNonPrivateMemberInfoObfuscator());

        // Assign new names to all private members in this class's name space.
        programClassFile.hierarchyAccept(true, true, true, false,
                                         new MyPrivateMemberInfoObfuscator());

        // Clean up for obfuscation of the next name space.
        nonPrivateDescriptorMap.clear();
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
    }


    private class MyNonPrivateMemberInfoObfuscator implements ClassFileVisitor
    {
        // Implementations for ClassFileVisitor.

        public void visitProgramClassFile(ProgramClassFile programClassFile)
        {
            MemberInfoAccessFilter nonPrivateNewNameAssigner =
                new MemberInfoAccessFilter(
                0,
                ClassConstants.INTERNAL_ACC_PRIVATE,
                new MyNewNameAssigner(nonPrivateDescriptorMap));

            programClassFile.fieldsAccept(nonPrivateNewNameAssigner);
            programClassFile.methodsAccept(nonPrivateNewNameAssigner);
        }


        public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
        {
        }
    }


    private class MyPrivateMemberInfoObfuscator implements ClassFileVisitor
    {
        // Implementations for ClassFileVisitor.

        public void visitProgramClassFile(ProgramClassFile programClassFile)
        {
            MemberInfoAccessFilter privateNewNameAssigner =
                new MemberInfoAccessFilter(
                ClassConstants.INTERNAL_ACC_PRIVATE,
                0,
                new MyNewNameAssigner(privateDescriptorMap, nonPrivateDescriptorMap));

            programClassFile.fieldsAccept(privateNewNameAssigner);
            programClassFile.methodsAccept(privateNewNameAssigner);

            // Clean up for obfuscation of the next class.
            privateDescriptorMap.clear();
        }


        public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
        {
        }
    }


    private class MyNewNameCollector implements MemberInfoVisitor
    {
        private final Map descriptorMap;


        public MyNewNameCollector(Map descriptorMap)
        {
            this.descriptorMap = descriptorMap;
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
            // Make sure the library field keeps its name.
            String name = libraryFieldInfo.getName(libraryClassFile);
            setNewMemberName(libraryFieldInfo, name);

            visitMemberInfo(libraryClassFile, libraryFieldInfo);
        }


        public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
        {
            // Make sure the library method keeps its name.
            String name = libraryMethodInfo.getName(libraryClassFile);
            setNewMemberName(libraryMethodInfo, name);

            visitMemberInfo(libraryClassFile, libraryMethodInfo);
        }


        /**
         * Inserts the new name of the given class member into the main map.
         * @param classFile  the class file of the given member.
         * @param memberInfo the class member to be linked.
         */
        private void visitMemberInfo(ClassFile classFile, MemberInfo memberInfo)
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
            String newName = newMemberName(memberInfo);

            // Remember it, if it was set.
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
                // creating a new set of new names if necessary.
                Map newNameMap = retrieveNameMap(descriptorMap, descriptor);

                // Is the other original name different from this original
                // name?
                String otherName = (String)newNameMap.get(newName);
                if (otherName != null &&
                    !otherName.equals(name))
                {
                    // There's a conflict! A member (with a given old name) in a
                    // first namespace has received the same new name as this
                    // member (with a different old name) in a second name space,
                    // and now these two have to live together in this name space.
                    // Assign a truly unique new name to this member.
                    setNewMemberName(memberInfo, uniqueNameFactory.nextName() + UNIQUE_SUFFIX);
                }
                else
                {
                    // Remember not to use the new name again in this name space.
                    newNameMap.put(newName, name);
                }
            }
        }
    }


    private class MyNewNameAssigner implements MemberInfoVisitor
    {
        private final Map descriptorMap;
        private final Map secondaryDescriptorMap;


        public MyNewNameAssigner(Map descriptorMap)
        {
            this(descriptorMap, null);
        }


        public MyNewNameAssigner(Map descriptorMap,
                                 Map secondaryDescriptorMap)
        {
            this.descriptorMap          = descriptorMap;
            this.secondaryDescriptorMap = secondaryDescriptorMap;
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


        public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}
        public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo) {}


        /**
         * Inserts the given class member into the main map.
         * @param classFile  the class file of the given member.
         * @param memberInfo the class member to be linked.
         */
        private void visitMemberInfo(ClassFile classFile, MemberInfo memberInfo)
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
            String newName = newMemberName(memberInfo);

            // Assign a new one, if necessary.
            if (newName == null)
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

                // Retrieve the new [ new name - old name ] map for the given
                // descriptor, creating a new one if necessary.
                Map newNameMap          = retrieveNameMap(descriptorMap, descriptor);
                Map secondaryNewNameMap = secondaryDescriptorMap == null ? null :
                    (Map)secondaryDescriptorMap.get(descriptor);

                // Find a unique new name.
                nameFactory.reset();

                do
                {
                    newName = nameFactory.nextName();
                }
                while (newNameMap.containsKey(newName) ||
                       (secondaryNewNameMap != null &&
                        secondaryNewNameMap.containsKey(newName)));

                // Assign the new name.
                setNewMemberName(memberInfo, newName);

                // Remember not to use the new name again in this name space.
                newNameMap.put(newName, name);
            }
        }
    }


    // Small utility methods.

    /**
     * Gets the nested set of new names, based on the given map
     * [descriptor - nested set of new names] and a given descriptor.
     * A new empty set is created if necessary.
     * @param descriptorMap the map of descriptors to [ new name - member info ] maps.
     * @param descriptor    the class member descriptor.
     * @return the nested set of new names.
     */
    private Map retrieveNameMap(Map descriptorMap, String descriptor)
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
     * Assigns a new name to the given class member.
     * @param memberInfo the given class member.
     * @param name       the new name.
     */
    static void setNewMemberName(MemberInfo memberInfo, String name)
    {
        MemberInfoLinker.lastMemberInfo(memberInfo).setVisitorInfo(name);
    }


    /**
     * Retrieves the new name of the given class member.
     * @param memberInfo the given class member.
     * @return the class member's new name, or <code>null</code> if it doesn't
     *         have one yet.
     */
    static String newMemberName(MemberInfo memberInfo)
    {
        return (String)MemberInfoLinker.lastMemberInfo(memberInfo).getVisitorInfo();
    }
}
