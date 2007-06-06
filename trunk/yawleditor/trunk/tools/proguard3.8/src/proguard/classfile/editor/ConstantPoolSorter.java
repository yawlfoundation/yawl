/* $Id: ConstantPoolSorter.java,v 1.6.2.2 2007/01/18 21:31:51 eric Exp $
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
package proguard.classfile.editor;

import java.util.Arrays;

import proguard.classfile.*;
import proguard.classfile.instruction.*;
import proguard.classfile.visitor.*;


/**
 * This ClassFileVisitor sorts the constant pool entries of the classes that
 * it visits. The sorting is based on the types of the constant pool entries
 * in the first place, and on their contents in the second place.
 *
 * @author Eric Lafortune
 */
public class ConstantPoolSorter implements ClassFileVisitor
{
    private int[]                cpIndexMap;
    private ComparableCpInfo[]   comparableConstantPool;
    private ConstantPoolRemapper constantPoolRemapper;


    /**
     * Creates a new ConstantPoolSorter.
     * @param codeLength an estimate of the maximum length of all the code that
     *                   will be edited.
     */
    public ConstantPoolSorter(int codeLength)
    {
        constantPoolRemapper = new ConstantPoolRemapper(codeLength);
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Sort the constant pool and set up an index map.
        sortConstantPool(programClassFile,
                         programClassFile.constantPool,
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
     * Sorts the given constant pool.
     */
    private void sortConstantPool(ClassFile classFile, CpInfo[] constantPool, int length)
    {
        if (cpIndexMap == null ||
            cpIndexMap.length < length)
        {
            cpIndexMap             = new int[length];
            comparableConstantPool = new ComparableCpInfo[length];
        }

        // Initialize an array whose elements can be compared.
        for (int oldIndex = 1; oldIndex < length; oldIndex++)
        {
            CpInfo cpInfo = constantPool[oldIndex];

            // Long entries take up two entries, the second of which is null.
            if (cpInfo == null)
            {
                cpInfo = constantPool[oldIndex-1];
            }

            comparableConstantPool[oldIndex] = new ComparableCpInfo(classFile,
                                                                    oldIndex,
                                                                    cpInfo);
        }

        // Sort the array.
        Arrays.sort(comparableConstantPool, 1, length);

        // Save the sorted elements.
        CpInfo previousCpInfo = null;
        for (int newIndex = 1; newIndex < length; newIndex++)
        {
            ComparableCpInfo comparableCpInfo = comparableConstantPool[newIndex];

            // Fill out the map array.
            int oldIndex = comparableCpInfo.getIndex();
            cpIndexMap[oldIndex] = newIndex;

            // Copy the sorted constant pool entry over to the constant pool.
            // Long entries take up two entries, the second of which is null.
            CpInfo cpInfo = comparableCpInfo.getCpInfo();
            constantPool[newIndex] = cpInfo != previousCpInfo ?
                cpInfo :
                null;

            previousCpInfo = cpInfo;
        }
    }
}
