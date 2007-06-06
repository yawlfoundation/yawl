/* $Id: DirectoryWriter.java,v 1.3.2.2 2007/01/18 21:31:52 eric Exp $
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
 * This DataEntryWriter writes sends data entries to individual files in a
 * given directory.
 *
 * @author Eric Lafortune
 */
public class DirectoryWriter implements DataEntryWriter
{
    private File    baseFile;
    private boolean isFile;

    private File         currentFile;
    private OutputStream currentOutputStream;
    private Finisher     currentFinisher;


    /**
     * Creates a new DirectoryWriter.
     * @param baseFile the base directory to which all files will be written.
     */
    public DirectoryWriter(File    baseFile,
                           boolean isFile)
    {
        this.baseFile = baseFile;
        this.isFile   = isFile;
    }


    // Implementations for DataEntryWriter.

    public OutputStream getOutputStream(DataEntry dataEntry) throws IOException
    {
        return getOutputStream(dataEntry,  null);
    }


    public OutputStream getOutputStream(DataEntry dataEntry,
                                        Finisher  finisher) throws IOException
    {
        // Should we close the current file?
        if (!isFile             &&
            currentFile != null &&
            !currentFile.equals(getFile(dataEntry)))
        {
            closeEntry();
        }

        // Do we need a new stream?
        if (currentOutputStream == null)
        {
            File file = getFile(dataEntry);

            // Make sure the parent directories exist.
            File parentDirectory = file.getParentFile();
            if (parentDirectory != null   &&
                !parentDirectory.exists() &&
                !parentDirectory.mkdirs())
            {
                throw new IOException("Can't create directory [" + parentDirectory.getPath() + "]");
            }

            // Open a new output stream for writing to the file.
            currentOutputStream =
                new BufferedOutputStream(
                new FileOutputStream(file));

            currentFinisher = finisher;
            currentFile     = file;
        }

        return currentOutputStream;
    }


    public void close() throws IOException
    {
        // Close the file stream, if any.
        closeEntry();
    }


    // Small utility methods.

    /**
     * Returns the file for the given data entry.
     */
    private File getFile(DataEntry dataEntry)
    {
        // Use the specified file, or construct a new file.
        return isFile ?
            baseFile :
            new File(baseFile,
                     dataEntry.getName().replace(ClassConstants.INTERNAL_PACKAGE_SEPARATOR,
                                                 File.separatorChar));
    }


    /**
     * Closes the previous file, if any.
     */
    private void closeEntry() throws IOException
    {
        // Close the file stream, if any.
        if (currentOutputStream != null)
        {
            // Let any finisher finish up first.
            if (currentFinisher != null)
            {
                currentFinisher.finish();
                currentFinisher = null;
            }

            currentOutputStream.close();
            currentOutputStream = null;
            currentFile         = null;
        }
    }
}
