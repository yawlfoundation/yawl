/* $Id: AllMethodVisitor.java,v 1.13.2.2 2007/01/18 21:31:51 eric Exp $
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
 * This ClassFileVisitor lets a given MemberInfoVisitor visit all MethodMemberInfo
 * objects of the class files it visits.
 *
 * @author Eric Lafortune
 */
public class AllMethodVisitor implements ClassFileVisitor
{
    private MemberInfoVisitor memberInfoVisitor;


    public AllMethodVisitor(MemberInfoVisitor memberInfoVisitor)
    {
        this.memberInfoVisitor = memberInfoVisitor;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        programClassFile.methodsAccept(memberInfoVisitor);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        libraryClassFile.methodsAccept(memberInfoVisitor);
    }
}
