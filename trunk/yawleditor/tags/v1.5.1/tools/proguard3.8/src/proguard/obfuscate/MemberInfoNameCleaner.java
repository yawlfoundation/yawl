/* $Id: MemberInfoNameCleaner.java,v 1.3.2.2 2007/01/18 21:31:52 eric Exp $
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
package proguard.obfuscate;

import proguard.classfile.*;
import proguard.classfile.visitor.MemberInfoVisitor;

import java.util.*;

/**
 * This <code>MemberInfoVisitor</code> clears the new names of the class members
 * that it visits.
 *
 * @see MemberInfoLinker
 * @see MemberInfoObfuscator
 *
 * @author Eric Lafortune
 */
public class MemberInfoNameCleaner implements MemberInfoVisitor
{
    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        MemberInfoObfuscator.setNewMemberName(programFieldInfo, null);
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        MemberInfoObfuscator.setNewMemberName(programMethodInfo, null);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
        MemberInfoObfuscator.setNewMemberName(libraryFieldInfo, null);
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        MemberInfoObfuscator.setNewMemberName(libraryMethodInfo, null);
    }
}
