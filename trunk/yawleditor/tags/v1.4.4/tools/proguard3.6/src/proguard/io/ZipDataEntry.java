/* $Id: ZipDataEntry.java,v 1.4.2.1 2006/01/16 22:57:56 eric Exp $
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
package proguard.io;

import java.io.*;
import java.util.zip.*;

import proguard.classfile.ClassConstants;

/**
 * This <code>DataEntry</code> represents a ZIP entry.
 *
 * @author Eric Lafortune
 */
public class ZipDataEntry implements DataEntry
{
    private DataEntry      parent;
    private ZipEntry       zipEntry;
    private ZipInputStream zipInputStream;


    public ZipDataEntry(DataEntry      parent,
                        ZipEntry       zipEntry,
                        ZipInputStream zipInputStream)
    {
        this.parent         = parent;
        this.zipEntry       = zipEntry;
        this.zipInputStream = zipInputStream;
    }


    // Implementations for DataEntry.

    public String getName()
    {
        // Chop the directory name from the file name and get the right separators.
        return zipEntry.getName()
            .replace(File.separatorChar, ClassConstants.INTERNAL_PACKAGE_SEPARATOR);
    }


    public InputStream getInputStream() throws IOException
    {
        return zipInputStream;
    }


    public void closeInputStream() throws IOException
    {
        zipInputStream.closeEntry();
        zipInputStream = null;
    }


    public DataEntry getParent()
    {
        return parent;
    }


    // Implementations for Object.

    public String toString()
    {
        return parent.toString() + ':' + getName();
    }
}
