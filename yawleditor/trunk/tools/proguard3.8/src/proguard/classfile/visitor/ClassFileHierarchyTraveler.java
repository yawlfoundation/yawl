/* $Id: ClassFileHierarchyTraveler.java,v 1.3.2.2 2007/01/18 21:31:51 eric Exp $
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
 * This <code>ClassFileVisitor</code> lets a given <code>ClassFileVisitor</code>
 * optionally travel to the visited class, its superclass, its interfaces, and
 * its subclasses.
 *
 * @author Eric Lafortune
 */
public class ClassFileHierarchyTraveler implements ClassFileVisitor
{
    private boolean visitThisClass;
    private boolean visitSuperClass;
    private boolean visitInterfaces;
    private boolean visitSubclasses;

    private ClassFileVisitor classFileVisitor;


    /**
     * Creates a new ClassFileHierarchyTraveler.
     * @param visitThisClass   specifies whether to visit the originally visited
     *                         classes.
     * @param visitSuperClass  specifies whether to visit the super classes of
     *                         the visited classes.
     * @param visitInterfaces  specifies whether to visit the interfaces of
     *                         the visited classes.
     * @param visitSubclasses  specifies whether to visit the subclasses of
     *                         the visited classes.
     * @param classFileVisitor the <code>ClassFileVisitor</code> to
     *                         which visits will be delegated.
     */
    public ClassFileHierarchyTraveler(boolean          visitThisClass,
                                      boolean          visitSuperClass,
                                      boolean          visitInterfaces,
                                      boolean          visitSubclasses,
                                      ClassFileVisitor classFileVisitor)
    {
        this.visitThisClass  = visitThisClass;
        this.visitSuperClass = visitSuperClass;
        this.visitInterfaces = visitInterfaces;
        this.visitSubclasses = visitSubclasses;

        this.classFileVisitor = classFileVisitor;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        programClassFile.hierarchyAccept(visitThisClass,
                                         visitSuperClass,
                                         visitInterfaces,
                                         visitSubclasses,
                                         classFileVisitor);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        libraryClassFile.hierarchyAccept(visitThisClass,
                                         visitSuperClass,
                                         visitInterfaces,
                                         visitSubclasses,
                                         classFileVisitor);
    }
}
