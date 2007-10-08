/* $Id: JarReader.java,v 1.3.2.2 2007/01/18 21:31:52 eric Exp $
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
import java.util.zip.*;

/**
 * This DataEntryReader lets a given DataEntryReader read all data entries of
 * the read jar/war/zip data entries.
 *
 * @author Eric Lafortune
 */
public class JarReader implements DataEntryReader
{
    DataEntryReader dataEntryReader;


    /**
     * Creates a new JarReader.
     */
    public JarReader(DataEntryReader dataEntryReader)
    {
        this.dataEntryReader = dataEntryReader;
    }


    // Implementation for DataEntryReader.

    public void read(DataEntry dataEntry) throws IOException
    {
        ZipInputStream zipInputStream = new ZipInputStream(dataEntry.getInputStream());

        try
        {
            // Get all entries from the input jar.
            while (true)
            {
                // Can we get another entry?
                ZipEntry zipEntry = zipInputStream.getNextEntry();
                if (zipEntry == null)
                {
                    break;
                }

                if (!zipEntry.isDirectory())
                {
                    // Delegate the actual reading to the data entry reader.
                    dataEntryReader.read(new ZipDataEntry(dataEntry,
                                                          zipEntry,
                                                          zipInputStream));
                }
            }
        }
        finally
        {
            dataEntry.closeInputStream();
        }
    }
}
