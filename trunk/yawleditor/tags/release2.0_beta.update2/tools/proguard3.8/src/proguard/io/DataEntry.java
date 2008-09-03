/* $Id: DataEntry.java,v 1.3.2.2 2007/01/18 21:31:52 eric Exp $
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
 * This interface describes a data entry, e.g. a ZIP entry or a file.
 *
 * @author Eric Lafortune
 */
public interface DataEntry
{
    /**
     * Returns the name of this data entry.
     */
    public String getName();


    /**
     * Returns an input stream for reading the content of this data entry.
     */
    public InputStream getInputStream() throws IOException;


    /**
     * Closes the previously retrieved InputStream.
     */
    public void closeInputStream() throws IOException;


    /**
     * Returns the parent of this data entry, or <code>null</null> if it doesn't
     * have one.
     */
    public DataEntry getParent();
}
