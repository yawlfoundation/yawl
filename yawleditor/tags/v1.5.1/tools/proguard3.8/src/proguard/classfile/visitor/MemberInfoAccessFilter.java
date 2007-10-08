/* $Id: MemberInfoAccessFilter.java,v 1.7.2.2 2007/01/18 21:31:51 eric Exp $
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
package proguard.classfile.visitor;

import proguard.classfile.*;


/**
 * This <code>MemberInfoVisitor</code> delegates its visits to another given
 * <code>MemberInfoVisitor</code>, but only when the visited member
 * has the proper access flags.
 * <p>
 * If conflicting access flags (public/private/protected) are specified,
 * having one of them set will be considered sufficient.
 *
 * @see ClassConstants
 *
 * @author Eric Lafortune
 */
public class MemberInfoAccessFilter
  implements MemberInfoVisitor
{
    // A mask of conflicting access flags. These are interpreted in a special
    // way if more of them are required at the same time. In that case, one
    // of them being set is sufficient.
    private static final int ACCESS_MASK =
        ClassConstants.INTERNAL_ACC_PUBLIC  |
        ClassConstants.INTERNAL_ACC_PRIVATE |
        ClassConstants.INTERNAL_ACC_PROTECTED;

    private int               requiredSetAccessFlags;
    private int               requiredUnsetAccessFlags;
    private int               requiredOneSetAccessFlags;
    private MemberInfoVisitor memberInfoVisitor;


    /**
     * Creates a new MemberInfoAccessFilter.
     * @param requiredSetAccessFlags   the class access flags that should be
     *                                 set.
     * @param requiredUnsetAccessFlags the class access flags that should be
     *                                 unset.
     * @param memberInfoVisitor        the <code>MemberInfoVisitor</code> to
     *                                 which visits will be delegated.
     */
    public MemberInfoAccessFilter(int               requiredSetAccessFlags,
                                  int               requiredUnsetAccessFlags,
                                  MemberInfoVisitor memberInfoVisitor)
    {
        this.requiredSetAccessFlags    = requiredSetAccessFlags & ~ACCESS_MASK;
        this.requiredUnsetAccessFlags  = requiredUnsetAccessFlags;
        this.requiredOneSetAccessFlags = requiredSetAccessFlags &  ACCESS_MASK;
        this.memberInfoVisitor         = memberInfoVisitor;
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        if (accepted(programFieldInfo.getAccessFlags()))
        {
            memberInfoVisitor.visitProgramFieldInfo(programClassFile, programFieldInfo);
        }
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        if (accepted(programMethodInfo.getAccessFlags()))
        {
            memberInfoVisitor.visitProgramMethodInfo(programClassFile, programMethodInfo);
        }
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
        if (accepted(libraryFieldInfo.getAccessFlags()))
        {
            memberInfoVisitor.visitLibraryFieldInfo(libraryClassFile, libraryFieldInfo);
        }
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        if (accepted(libraryMethodInfo.getAccessFlags()))
        {
            memberInfoVisitor.visitLibraryMethodInfo(libraryClassFile, libraryMethodInfo);
        }
    }


    // Small utility methods.

    private boolean accepted(int accessFlags)
    {
        return (requiredSetAccessFlags    & ~accessFlags) == 0 &&
               (requiredUnsetAccessFlags  &  accessFlags) == 0 &&
               (requiredOneSetAccessFlags == 0                 ||
               (requiredOneSetAccessFlags &  accessFlags) != 0);
    }
}
