/* $Id: ExtensionFileFilter.java,v 1.6.2.1 2006/01/16 22:57:55 eric Exp $
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
package proguard.gui;

import java.io.File;
import javax.swing.filechooser.FileFilter;


/**
 * This <code>FileFilter</code> accepts files that end in one of the given
 * extensions.
 *
 * @author Eric Lafortune
 */
class ExtensionFileFilter extends FileFilter
{
    private String   description;
    private String[] extensions;


    /**
     * Creates a new ExtensionFileFilter.
     * @param description a description of the filter.
     * @param extensions  an array of acceptable extensions.
     */
    public ExtensionFileFilter(String description, String[] extensions)
    {
        this.description = description;
        this.extensions  = extensions;
    }


    // Implemntations for FileFilter

    public String getDescription()
    {
        return description;
    }


    public boolean accept(File file)
    {
        if (file.isDirectory())
        {
            return true;
        }

        String fileName = file.getName().toLowerCase();

        for (int index = 0; index < extensions.length; index++)
        {
            if (fileName.endsWith(extensions[index]))
            {
                return true;
            }
        }

        return false;
    }
}
