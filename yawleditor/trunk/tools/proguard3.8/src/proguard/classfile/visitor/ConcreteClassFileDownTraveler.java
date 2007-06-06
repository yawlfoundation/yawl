/* $Id: ConcreteClassFileDownTraveler.java,v 1.8.2.2 2007/01/18 21:31:51 eric Exp $
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
 * travel to the first concrete subclasses down in its hierarchy of abstract
 * classes and concrete classes.
 *
 * @author Eric Lafortune
 */
public class ConcreteClassFileDownTraveler
  implements ClassFileVisitor
{
    private ClassFileVisitor classFileVisitor;


    /**
     * Creates a new ConcreteClassFileDownTraveler.
     * @param classFileVisitor the <code>ClassFileVisitor</code> to
     *                         which visits will be delegated.
     */
    public ConcreteClassFileDownTraveler(ClassFileVisitor classFileVisitor)
    {
        this.classFileVisitor = classFileVisitor;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Is this an abstract class or an interface?
        if ((programClassFile.getAccessFlags() &
             (ClassConstants.INTERNAL_ACC_INTERFACE |
              ClassConstants.INTERNAL_ACC_ABSTRACT)) != 0)
        {
            // Travel down the hierarchy.
            ClassFile[] subClasses = programClassFile.subClasses;
            if (subClasses != null)
            {
                for (int i = 0; i < subClasses.length; i++)
                {
                    subClasses[i].accept(this);
                }
            }
        }
        else
        {
            // Visit the class file. Don't descend any further.
            programClassFile.accept(classFileVisitor);
        }
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        // Is this an abstract class or interface?
        if ((libraryClassFile.getAccessFlags() &
             (ClassConstants.INTERNAL_ACC_INTERFACE |
              ClassConstants.INTERNAL_ACC_ABSTRACT)) != 0)
        {
            // Travel down the hierarchy.
            ClassFile[] subClasses = libraryClassFile.subClasses;
            if (subClasses != null)
            {
                for (int i = 0; i < subClasses.length; i++)
                {
                    subClasses[i].accept(this);
                }
            }
        }
        else
        {
            // Visit the class file. Don't descend any further.
            libraryClassFile.accept(classFileVisitor);
        }
    }
}
