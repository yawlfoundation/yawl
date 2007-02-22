/* $Id: MemberCounter.java,v 1.1.2.3 2006/02/13 00:20:43 eric Exp $
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
import proguard.classfile.visitor.*;

/**
 * This MemberInfoVisitor counts the number of class members that has been visited.
 *
 * @author Eric Lafortune
 */
public class MemberCounter implements MemberInfoVisitor
{
    private int count;


    /**
     * Returns the number of class members that has been visited so far.
     */
    public int getCount()
    {
        return count;
    }


    // Implementations for MemberInfoVisitor.

    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile,
                                      LibraryFieldInfo libraryFieldInfo)
    {
        count++;
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile,
                                       LibraryMethodInfo libraryMethodInfo)
    {
        count++;
    }


    public void visitProgramFieldInfo(ProgramClassFile programClassFile,
                                      ProgramFieldInfo programFieldInfo)
    {
        count++;
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile,
                                       ProgramMethodInfo programMethodInfo)
    {
        count++;
    }
}
