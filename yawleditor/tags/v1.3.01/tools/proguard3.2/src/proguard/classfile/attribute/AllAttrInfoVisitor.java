/* $Id: AllAttrInfoVisitor.java,v 1.1 2004/10/10 21:10:04 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2003 Eric Lafortune (eric@graphics.cornell.edu)
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
package proguard.classfile.attribute;

import proguard.classfile.*;
import proguard.classfile.visitor.*;


/**
 * This MemberInfoVisitor lets a given AttrInfoVisitor visit all AttrInfo
 * objects of the program class members it visits.
 *
 * @author Eric Lafortune
 */
public class AllAttrInfoVisitor implements MemberInfoVisitor
{
    private AttrInfoVisitor attrInfoVisitor;


    public AllAttrInfoVisitor(AttrInfoVisitor attrInfoVisitor)
    {
        this.attrInfoVisitor = attrInfoVisitor;
    }


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
    {
        programFieldInfo.attributesAccept(programClassFile, attrInfoVisitor);
    }


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        programMethodInfo.attributesAccept(programClassFile, attrInfoVisitor);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
    {
        // Library class file fields don't have attributes.
    }


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        // Library class file methods don't have attributes.
    }
}
