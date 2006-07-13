/* $Id: ClassCounter.java,v 1.1.2.3 2006/02/13 00:20:43 eric Exp $
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
import proguard.classfile.visitor.*;

/**
 * This ClassFileVisitor counts the number of classes that has been visited.
 *
 * @author Eric Lafortune
 */
public class ClassCounter implements ClassFileVisitor
{
    private int count;


    /**
     * Returns the number of classes that has been visited so far.
     */
    public int getCount()
    {
        return count;
    }


    // Implementations for ClassFileVisitor.

    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        count++;
    }


    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        count++;
    }
}
