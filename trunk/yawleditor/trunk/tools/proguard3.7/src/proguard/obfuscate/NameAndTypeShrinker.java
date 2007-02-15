/* $Id: NameAndTypeShrinker.java,v 1.24.2.1 2006/01/16 22:57:56 eric Exp $
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
package proguard.obfuscate;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.editor.ConstantPoolRemapper;
import proguard.classfile.instruction.*;
import proguard.classfile.visitor.*;


/**
 * This ClassFileVisitor removes NameAndType constant pool entries
 * that are not marked as being used.
 *
 * @see NameAndTypeUsageMarker
 *
 * @author Eric Lafortune
 */
public class NameAndTypeShrinker implements ClassFileVisitor
{
    private int[]                cpIndexMap;
    private ConstantPoolRemapper constantPoolRemapper;


    /**
     * Creates a new NameAndTypeShrinker.
     * @param codeLength an estimate of the maximum length of all the code that
     *                   will be edited.
     */
    public NameAndTypeShrinker(int codeLength)
    {
        constantPoolRemapper = new ConstantPoolRemapper(codeLength);
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Shift the used constant pool entries together, filling out the
        // index map.
        programClassFile.u2constantPoolCount =
            shrinkConstantPool(programClassFile.constantPool,
                               programClassFile.u2constantPoolCount);


        // Remap all constant pool references.
        constantPoolRemapper.setCpIndexMap(cpIndexMap);
        constantPoolRemapper.visitProgramClassFile(programClassFile);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
    }


    // Small utility methods.

    /**
     * Removes all NameAndType entries that are not marked as being used
     * from the given constant pool.
     * @return the new number of entries.
     */
    private int shrinkConstantPool(CpInfo[] constantPool, int length)
    {
        // Create a new index map, if necessary.
        if (cpIndexMap == null ||
            cpIndexMap.length < length)
        {
            cpIndexMap = new int[length];
        }

        int     counter = 1;
        boolean isUsed  = false;

        // Shift the used constant pool entries together.
        for (int index = 1; index < length; index++)
        {
            cpIndexMap[index] = counter;

            CpInfo cpInfo = constantPool[index];

            // Don't update the flag if this is the second half of a long entry.
            if (cpInfo != null)
            {
                isUsed = cpInfo.getTag() != ClassConstants.CONSTANT_NameAndType ||
                         NameAndTypeUsageMarker.isUsed(cpInfo);
            }

            if (isUsed)
            {
                constantPool[counter++] = cpInfo;
            }
        }

        // Clear the remaining constant pool elements.
        for (int index = counter; index < length; index++)
        {
            constantPool[index] = null;
        }

        return counter;
    }
}
