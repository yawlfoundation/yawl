/* $Id: ClassFileVersionCounter.java,v 1.1.2.1 2006/12/11 22:07:35 eric Exp $
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
 * This <code>ClassFileVisitor</code> counts the number of visited program class
 * files that have at least the given version number.
 *
 * @author Eric Lafortune
 */
public class ClassFileVersionCounter implements ClassFileVisitor
{
    private int majorVersionNumber;
    private int minorVersionNumber;

    private int count;


    /**
     * Creates a new ClassFileVersionChecker.
     * @param majorVersionNumber the major version number.
     * @param minorVersionNumber the minor version number.
     */
    public ClassFileVersionCounter(int majorVersionNumber,
                                   int minorVersionNumber)
    {
        this.majorVersionNumber = majorVersionNumber;
        this.minorVersionNumber = minorVersionNumber;
    }


    /**
     * Returns the number of visited program class files with the given minimum
     * version number.
     */
    public int getCount()
    {
        return count;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        if ((programClassFile.u2majorVersion == majorVersionNumber &&
             programClassFile.u2minorVersion >=  minorVersionNumber) ||
            programClassFile.u2majorVersion > majorVersionNumber)
        {
            count++;
        }
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        // Library class files don't store their version numbers.
    }
}
