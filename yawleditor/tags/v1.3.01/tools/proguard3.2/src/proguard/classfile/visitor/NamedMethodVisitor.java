/* $Id: NamedMethodVisitor.java,v 1.11 2004/12/11 16:35:23 eric Exp $
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
package proguard.classfile.visitor;

import proguard.classfile.*;


/**
 * This class visits ProgramMemberInfo objects referring to methods, identified by
 * a name and descriptor pair.
 *
 * @author Eric Lafortune
 */
public class NamedMethodVisitor implements ClassFileVisitor
{
    private String            name;
    private String            descriptor;
    private MemberInfoVisitor memberInfoVisitor;


    public NamedMethodVisitor(String            name,
                              String            descriptor,
                              MemberInfoVisitor memberInfoVisitor)
    {
        this.name              = name;
        this.descriptor        = descriptor;
        this.memberInfoVisitor = memberInfoVisitor;
    }


    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        programClassFile.methodAccept(name, descriptor, memberInfoVisitor);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        libraryClassFile.methodAccept(name, descriptor, memberInfoVisitor);
    }
}
