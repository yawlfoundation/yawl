/* $Id: WriteOnlyFieldFilter.java,v 1.1.2.1 2006/01/16 22:57:56 eric Exp $
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
package proguard.optimize;

import proguard.classfile.*;
import proguard.classfile.visitor.MemberInfoVisitor;

/**
 * This <code>MemberInfoVisitor</code> delegates its visits to program fields to
 * another given <code>MemberInfoVisitor</code>, but only when the visited
 * field has been marked as write-only.
 *
 * @see WriteOnlyFieldMarker#isWriteOnly(VisitorAccepter)
 * @author Eric Lafortune
 */
public class WriteOnlyFieldFilter
  implements MemberInfoVisitor
{
    private MemberInfoVisitor memberInfoVisitor;


    /**
     * Creates a new WriteOnlyFieldFilter.
     * @param memberInfoVisitor the <code>MemberInfoVisitor</code> to which
     *                          visits will be delegated.
     */
    public WriteOnlyFieldFilter(MemberInfoVisitor memberInfoVisitor)
    {
        this.memberInfoVisitor = memberInfoVisitor;
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        if (WriteOnlyFieldMarker.isWriteOnly(programFieldInfo))
        {
            memberInfoVisitor.visitProgramFieldInfo(programClassFile, programFieldInfo);
        }
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo) {}
    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}
    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo) {}
}
