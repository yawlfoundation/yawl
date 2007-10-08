/* $Id: ProgramClassFileFilter.java,v 1.7.2.2 2007/01/18 21:31:52 eric Exp $
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
 * This <code>ClassFileVisitor</code> delegates its visits to another given
 * <code>ClassFileVisitor</code>, but only when visiting program class files.
 *
 * @author Eric Lafortune
 */
public class ProgramClassFileFilter implements ClassFileVisitor
{
    private ClassFileVisitor classFileVisitor;


    /**
     * Creates a new ProgramClassFileFilter.
     * @param classFileVisitor the <code>ClassFileVisitor</code> to which visits
     *                         will be delegated.
     */
    public ProgramClassFileFilter(ClassFileVisitor classFileVisitor)
    {
        this.classFileVisitor = classFileVisitor;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        classFileVisitor.visitProgramClassFile(programClassFile);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        // Don't delegate visits to library class files.
    }
}
