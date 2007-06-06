/* $Id: DirectoryPump.java,v 1.4.2.3 2007/01/18 21:31:52 eric Exp $
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

import java.io.*;


/**
 * This class can read a given file or directory, recursively, applying a given
 * DataEntryReader to all files it comes across.
 *
 * @author Eric Lafortune
 */
public class DirectoryPump implements DataEntryPump
{
    private File directory;


    public DirectoryPump(File directory)
    {
        this.directory = directory;
    }


    // Implementations for DataEntryPump.

    public void pumpDataEntries(DataEntryReader dataEntryReader)
    throws IOException
    {
        if (!directory.exists())
        {
            throw new IOException("No such file or directory");
        }

        readFiles(directory, dataEntryReader);
    }


    /**
     * Reads the given subdirectory recursively, applying the given DataEntryReader
     * to all files that are encountered.
     */
    private void readFiles(File file, DataEntryReader dataEntryReader)
    throws IOException
    {
        if (file.isDirectory())
        {
            // Recurse into the subdirectory.
            File[] files = file.listFiles();

            for (int index = 0; index < files.length; index++)
            {
                readFiles(files[index], dataEntryReader);
            }
        }
        else
        {
            dataEntryReader.read(new FileDataEntry(directory, file));
        }
    }
}
