/* $Id: FileDataEntry.java,v 1.3.2.2 2007/01/18 21:31:52 eric Exp $
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

import proguard.classfile.ClassConstants;

import java.io.*;

/**
 * This <code>DataEntry</code> represents a file.
 *
 * @author Eric Lafortune
 */
public class FileDataEntry implements DataEntry
{
    private File        directory;
    private File        file;
    private InputStream inputStream;


    public FileDataEntry(File directory,
                         File file)
    {
        this.directory = directory;
        this.file      = file;
    }


    // Implementations for DataEntry.

    public String getName()
    {
        // Chop the directory name from the file name and get the right separators.
        return file.equals(directory) ?
            file.getName() :
            file.getPath()
            .substring(directory.getPath().length() + File.separator.length())
            .replace(File.separatorChar, ClassConstants.INTERNAL_PACKAGE_SEPARATOR);
    }


    // Implementations for InputEntry.

    public InputStream getInputStream() throws IOException
    {
        if (inputStream == null)
        {
            inputStream = new BufferedInputStream(new FileInputStream(file));
        }

        return inputStream;
    }


    public void closeInputStream() throws IOException
    {
        inputStream.close();
        inputStream = null;
    }


    public DataEntry getParent()
    {
        return null;
    }


    // Implementations for Object.

    public String toString()
    {
        return getName();
    }
}
