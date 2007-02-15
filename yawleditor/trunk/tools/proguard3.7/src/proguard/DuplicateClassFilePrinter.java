/* $Id: DuplicateClassFilePrinter.java,v 1.1.2.1 2006/11/26 15:29:20 eric Exp $
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
package proguard;

import proguard.classfile.visitor.ClassFileVisitor;
import proguard.classfile.*;
import proguard.classfile.util.*;

/**
 * This ClassFileVisitor writes out notes about the class files that it visits
 * being duplicates.
 *
 * @author Eric Lafortune
 */
public class DuplicateClassFilePrinter implements ClassFileVisitor
{
    private WarningPrinter notePrinter;


    /**
     * Creates a new DuplicateClassFileVisitor.
     */
    public DuplicateClassFilePrinter(WarningPrinter notePrinter)
    {
        this.notePrinter = notePrinter;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        notePrinter.print("Note: duplicate definition of program class [" +
                          ClassUtil.externalClassName(programClassFile.getName()) + "]");
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        notePrinter.print("Note: duplicate definition of library class [" +
                          ClassUtil.externalClassName(libraryClassFile.getName()) + "]");
    }
}
