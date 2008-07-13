/* $Id: MemberInfoClassFileAccessFilter.java,v 1.1.2.4 2007/01/18 21:31:51 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java bytecode.
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
import proguard.classfile.util.*;

/**
 * This <code>MemberInfoVisitor</code> delegates its visits to another given
 * <code>MemberInfoVisitor</code>, but only when the visited memberInfo is
 * accessible from the given referencing classFile.
 *
 * @author Eric Lafortune
 */
public class MemberInfoClassFileAccessFilter
implements   MemberInfoVisitor
{
    private ClassFile         referencingClassFile;
    private MemberInfoVisitor memberInfoVisitor;


    /**
     * Creates a new MemberInfoAccessFilter.
     * @param referencingClassFile the classFile that is accessing the member.
     * @param memberInfoVisitor    the <code>MemberInfoVisitor</code> to which
     *                             visits will be delegated.
     */
    public MemberInfoClassFileAccessFilter(ClassFile         referencingClassFile,
                                           MemberInfoVisitor memberInfoVisitor)
    {
        this.referencingClassFile = referencingClassFile;
        this.memberInfoVisitor    = memberInfoVisitor;
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        if (accepted(programClassFile, programFieldInfo.getAccessFlags()))
        {
            memberInfoVisitor.visitProgramFieldInfo(programClassFile, programFieldInfo);
        }
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        if (accepted(programClassFile, programMethodInfo.getAccessFlags()))
        {
            memberInfoVisitor.visitProgramMethodInfo(programClassFile, programMethodInfo);
        }
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
        if (accepted(libraryClassFile, libraryFieldInfo.getAccessFlags()))
        {
            memberInfoVisitor.visitLibraryFieldInfo(libraryClassFile, libraryFieldInfo);
        }
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        if (accepted(libraryClassFile, libraryMethodInfo.getAccessFlags()))
        {
            memberInfoVisitor.visitLibraryMethodInfo(libraryClassFile, libraryMethodInfo);
        }
    }


    // Small utility methodInfos.

    private boolean accepted(ClassFile classFile, int memberInfoAccessFlags)
    {
        int accessLevel = AccessUtil.accessLevel(memberInfoAccessFlags);

        return
            (accessLevel >= AccessUtil.PUBLIC                                                              ) ||
            (accessLevel >= AccessUtil.PRIVATE         && referencingClassFile.equals(classFile)                   ) ||
            (accessLevel >= AccessUtil.PACKAGE_VISIBLE && (ClassUtil.internalPackageName(referencingClassFile.getName()).equals(
                                                           ClassUtil.internalPackageName(classFile.getName())))) ||
            (accessLevel >= AccessUtil.PROTECTED       && (referencingClassFile.extends_(classFile)                  ||
                                                           referencingClassFile.implements_(classFile))            );
    }
}
