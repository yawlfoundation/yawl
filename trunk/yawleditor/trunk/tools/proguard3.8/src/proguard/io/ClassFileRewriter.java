/* $Id: ClassFileRewriter.java,v 1.3.2.2 2007/01/18 21:31:52 eric Exp $
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
package proguard.io;

import proguard.classfile.*;

import java.io.*;


/**
 * This DataEntryReader reads class file entries and writes their corresponding
 * versions from the ClassPool to a given DataEntryWriter.
 *
 * @author Eric Lafortune
 */
public class ClassFileRewriter implements DataEntryReader
{
    private ClassPool       classPool;
    private DataEntryWriter dataEntryWriter;


    public ClassFileRewriter(ClassPool       classPool,
                             DataEntryWriter dataEntryWriter)
    {
        this.classPool       = classPool;
        this.dataEntryWriter = dataEntryWriter;
    }


    // Implementations for DataEntryReader.

    public void read(DataEntry dataEntry) throws IOException
    {
        String inputName = dataEntry.getName();
        String className = inputName.substring(0, inputName.length() - ClassConstants.CLASS_FILE_EXTENSION.length());

        // Find the modified class corrsponding to the input entry.
        ProgramClassFile programClassFile = (ProgramClassFile)classPool.getClass(className);
        if (programClassFile != null)
        {
            // Rename the data entry if necessary.
            String newClassName = programClassFile.getName();
            if (!className.equals(newClassName))
            {
                dataEntry = new RenamedDataEntry(dataEntry, newClassName + ClassConstants.CLASS_FILE_EXTENSION);
            }

            // Get the output entry corresponding to this input entry.
            OutputStream outputStream = dataEntryWriter.getOutputStream(dataEntry);
            if (outputStream != null)
            {
                // Write the class to the output entry.
                DataOutputStream classOutputStream = new DataOutputStream(outputStream);
                programClassFile.write(classOutputStream);
                classOutputStream.flush();
            }
        }
    }
}
