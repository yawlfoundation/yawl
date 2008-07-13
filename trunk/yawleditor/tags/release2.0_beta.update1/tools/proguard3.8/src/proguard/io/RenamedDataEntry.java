/* $Id: RenamedDataEntry.java,v 1.3.2.2 2007/01/18 21:31:52 eric Exp $
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
 * This DataEntry wraps another data entry, returning a different name instead
 * of the wrapped data entry's name.
 *
 * @author Eric Lafortune
 */
public class RenamedDataEntry implements DataEntry
{
    private DataEntry dataEntry;
    private String    name;


    public RenamedDataEntry(DataEntry dataEntry,
                            String    name)
    {
        this.dataEntry = dataEntry;
        this.name      = name;
    }


    // Implementations for DataEntry.

    public String getName()
    {
        return name;
    }


    public InputStream getInputStream() throws IOException
    {
        return dataEntry.getInputStream();
    }


    public void closeInputStream() throws IOException
    {
        dataEntry.closeInputStream();
    }


    public DataEntry getParent()
    {
        return dataEntry.getParent();
    }


    // Implementations for Object.

    public String toString()
    {
        return name + " == " + dataEntry;
    }
}
