/* $Id: MemberInfoNameConflictFilter.java,v 1.3.2.2 2006/11/25 16:56:11 eric Exp $
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
import proguard.classfile.visitor.MemberInfoVisitor;

import java.util.*;

/**
 * This <code>MemberInfoVisitor</code> delegates its visits to another given
 * <code>MemberInfoVisitor</code>, but only when the visited member has been
 * marked as having a conflicting name.
 *
 * @see MemberInfoObfuscator
 *
 * @author Eric Lafortune
 */
public class MemberInfoNameConflictFilter implements MemberInfoVisitor
{
    private static final String CONFLICT = "_ CONFLICT _";


    private MemberInfoVisitor memberInfoVisitor;


    /**
     * Creates a new MemberInfoNameConflictFilter.
     * @param memberInfoVisitor the <code>MemberInfoVisitor</code> to which
     *                          visits will be delegated.
     */
    public MemberInfoNameConflictFilter(MemberInfoVisitor memberInfoVisitor)
    {
        this.memberInfoVisitor = memberInfoVisitor;
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        if (hasConflictingName(programFieldInfo))
        {
            memberInfoVisitor.visitProgramFieldInfo(programClassFile, programFieldInfo);
        }
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        if (hasConflictingName(programMethodInfo))
        {
            memberInfoVisitor.visitProgramMethodInfo(programClassFile, programMethodInfo);
        }
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}
    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo) {}


    // Small utility methods.

    /**
     * Marks the given class member as having a conflicting name.
     * @param memberInfo the class member.
     */
    static void markConflictingName(MemberInfo memberInfo)
    {
        MemberInfoObfuscator.setNewMemberName(memberInfo, CONFLICT);
    }


    /**
     * Returns whether the given class member has been marked as having a
     * conflicting name.
     * @param memberInfo the class member.
     */
    static boolean hasConflictingName(MemberInfo memberInfo)
    {
        String newName = MemberInfoObfuscator.newMemberName(memberInfo);

        return newName != null &&
               newName.equals(CONFLICT);
    }
}
