/* $Id: ClassFilePresenceFilter.java,v 1.1.2.2 2007/01/18 21:31:51 eric Exp $
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
 * This <code>ClassFileVisitor</code> delegates its visits to one of two
 * <code>ClassFileVisitor</code> instances, depending on whether the name of
 * the visited class file is present in a given <code>ClassPool</code> or not.
 *
 * @author Eric Lafortune
 */
public class ClassFilePresenceFilter implements ClassFileVisitor
{
    private ClassPool        classPool;
    private ClassFileVisitor presentClassFileVisitor;
    private ClassFileVisitor missingClassFileVisitor;


    /**
     * Creates a new ClassFilePresenceFilter.
     * @param classPool               the <code>ClassPool</code> in which the
     *                                presence will be tested.
     * @param presentClassFileVisitor the <code>ClassFileVisitor</code> to which
     *                                visits of present class files will be
     *                                delegated.
     * @param missingClassFileVisitor the <code>ClassFileVisitor</code> to which
     *                                visits of missing class files will be
     *                                delegated.
     */
    public ClassFilePresenceFilter(ClassPool        classPool,
                                   ClassFileVisitor presentClassFileVisitor,
                                   ClassFileVisitor missingClassFileVisitor)
    {
        this.classPool               = classPool;
        this.presentClassFileVisitor = presentClassFileVisitor;
        this.missingClassFileVisitor = missingClassFileVisitor;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        ClassFileVisitor classFileVisitor = classFileVisitor(programClassFile);

        if (classFileVisitor != null)
        {
            classFileVisitor.visitProgramClassFile(programClassFile);
        }
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        ClassFileVisitor classFileVisitor = classFileVisitor(libraryClassFile);

        if (classFileVisitor != null)
        {
            classFileVisitor.visitLibraryClassFile(libraryClassFile);
        }
    }


    // Small utility methods.

    /**
     * Returns the appropriate <code>ClassFileVisitor</code>.
     */
    private ClassFileVisitor classFileVisitor(ClassFile classFile)
    {
        return classPool.getClass(classFile.getName()) != null ?
            presentClassFileVisitor :
            missingClassFileVisitor;
    }
}
