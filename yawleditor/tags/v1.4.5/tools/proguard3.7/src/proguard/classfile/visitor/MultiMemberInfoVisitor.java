/* $Id: MultiMemberInfoVisitor.java,v 1.12.2.1 2006/01/16 22:57:55 eric Exp $
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
package proguard.classfile.visitor;

import proguard.classfile.*;


/**
 * This MemberInfoVisitor delegates all visits to each MemberInfoVisitor
 * in a given list.
 *
 * @author Eric Lafortune
 */
public class MultiMemberInfoVisitor implements MemberInfoVisitor
{
    private static final int ARRAY_SIZE_INCREMENT = 5;

    private MemberInfoVisitor[] memberInfoVisitors;
    private int                 memberInfoVisitorCount;


    public MultiMemberInfoVisitor()
    {
    }


    public MultiMemberInfoVisitor(MemberInfoVisitor[] memberInfoVisitors)
    {
        this.memberInfoVisitors     = memberInfoVisitors;
        this.memberInfoVisitorCount = memberInfoVisitors.length;
    }


    public void addMemberInfoVisitor(MemberInfoVisitor memberInfoVisitor)
    {
        ensureArraySize();

        memberInfoVisitors[memberInfoVisitorCount++] = memberInfoVisitor;
    }


    private void ensureArraySize()
    {
        if (memberInfoVisitors == null)
        {
            memberInfoVisitors = new MemberInfoVisitor[ARRAY_SIZE_INCREMENT];
        }
        else if (memberInfoVisitors.length == memberInfoVisitorCount)
        {
            MemberInfoVisitor[] newMemberInfoVisitors =
                new MemberInfoVisitor[memberInfoVisitorCount +
                                         ARRAY_SIZE_INCREMENT];
            System.arraycopy(memberInfoVisitors, 0,
                             newMemberInfoVisitors, 0,
                             memberInfoVisitorCount);
            memberInfoVisitors = newMemberInfoVisitors;
        }
    }


    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        for (int i = 0; i < memberInfoVisitorCount; i++)
        {
            memberInfoVisitors[i].visitProgramFieldInfo(programClassFile, programFieldInfo);
        }
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        for (int i = 0; i < memberInfoVisitorCount; i++)
        {
            memberInfoVisitors[i].visitProgramMethodInfo(programClassFile, programMethodInfo);
        }
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
        for (int i = 0; i < memberInfoVisitorCount; i++)
        {
            memberInfoVisitors[i].visitLibraryFieldInfo(libraryClassFile, libraryFieldInfo);
        }
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        for (int i = 0; i < memberInfoVisitorCount; i++)
        {
            memberInfoVisitors[i].visitLibraryMethodInfo(libraryClassFile, libraryMethodInfo);
        }
    }
}
