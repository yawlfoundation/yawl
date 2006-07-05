/* $Id: VariableClassFileVisitor.java,v 1.7 2004/08/15 12:39:30 eric Exp $
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
 * This ClassFileVisitor delegates all method calls to a ClassFileVisitor
 * that can be changed at any time.
 *
 * @author Eric Lafortune
 */
public class VariableClassFileVisitor implements ClassFileVisitor
{
    private ClassFileVisitor classFileVisitor;


    public VariableClassFileVisitor()
    {
        this(null);
    }


    public VariableClassFileVisitor(ClassFileVisitor classFileVisitor)
    {
        this.classFileVisitor = classFileVisitor;
    }


    public void setClassFileVisitor(ClassFileVisitor classFileVisitor)
    {
        this.classFileVisitor = classFileVisitor;
    }

    public ClassFileVisitor getClassFileVisitor()
    {
        return classFileVisitor;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        if (classFileVisitor != null)
        {
            classFileVisitor.visitProgramClassFile(programClassFile);
        }
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        if (classFileVisitor != null)
        {
            classFileVisitor.visitLibraryClassFile(libraryClassFile);
        }
    }
}
