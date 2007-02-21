/* $Id: MultiClassFileVisitor.java,v 1.9.2.1 2006/01/16 22:57:55 eric Exp $
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


/**
 * This ClassFileVisitor delegates all visits to each ClassFileVisitor
 * in a given list.
 *
 * @author Eric Lafortune
 */
public class MultiClassFileVisitor implements ClassFileVisitor
{
    private static final int ARRAY_SIZE_INCREMENT = 5;

    private ClassFileVisitor[] classFileVisitors;
    private int                classFileVisitorCount;


    public MultiClassFileVisitor()
    {
    }


    public MultiClassFileVisitor(ClassFileVisitor[] classFileVisitors)
    {
        this.classFileVisitors     = classFileVisitors;
        this.classFileVisitorCount = classFileVisitors.length;
    }


    public void addClassFileVisitor(ClassFileVisitor classFileVisitor)
    {
        ensureArraySize();

        classFileVisitors[classFileVisitorCount++] = classFileVisitor;
    }


    private void ensureArraySize()
    {
        if (classFileVisitors == null)
        {
            classFileVisitors = new ClassFileVisitor[ARRAY_SIZE_INCREMENT];
        }
        else if (classFileVisitors.length == classFileVisitorCount)
        {
            ClassFileVisitor[] newClassFileVisitors =
                new ClassFileVisitor[classFileVisitorCount +
                                     ARRAY_SIZE_INCREMENT];
            System.arraycopy(classFileVisitors, 0,
                             newClassFileVisitors, 0,
                             classFileVisitorCount);
            classFileVisitors = newClassFileVisitors;
        }
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        for (int i = 0; i < classFileVisitorCount; i++)
        {
            classFileVisitors[i].visitProgramClassFile(programClassFile);
        }
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        for (int i = 0; i < classFileVisitorCount; i++)
        {
            classFileVisitors[i].visitLibraryClassFile(libraryClassFile);
        }
    }
}
