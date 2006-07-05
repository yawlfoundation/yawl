/* $Id: ClassPathElement.java,v 1.7 2004/12/18 20:21:11 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2004 Eric Lafortune (eric@graphics.cornell.edu)
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
package proguard.ant;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

import proguard.*;

import java.io.*;
import java.lang.ref.Reference;
import java.util.Stack;

/**
 * This FileSet represents a class path entry (or a set of class path entries)
 * in Ant.
 *
 * @author Eric Lafortune
 */
public class ClassPathElement extends Path
{
    private String filter;
    private String jarFilter;
    private String warFilter;
    private String earFilter;
    private String zipFilter;


    /**
     * @see Path#Path(Project)
     */
    public ClassPathElement(Project project)
    {
        super(project);
    }


    /**
     * Adds the contents of this class path element to the given class path.
     * @param classPath the class path to be extended.
     * @param output    specifies whether this is an output entry or not.
     */
    public void appendClassPathEntriesTo(ClassPath classPath, boolean output)
    {
        String   basePath = "";
        String[] files;

        if (isReference())
        {
            // Get the referenced path or file set.
            Object referencedObject = getCheckedRef(DataType.class,
                                                    DataType.class.getName());

            if (referencedObject instanceof Path)
            {
                Path path = (Path)referencedObject;

                // Get the names of the files in the referenced path.
                files = path.list();
            }
            else if (referencedObject instanceof AbstractFileSet)
            {
                AbstractFileSet fileSet = (AbstractFileSet)referencedObject;

                // Get the names of the existing input files in the referenced file set.
                DirectoryScanner scanner = fileSet.getDirectoryScanner(getProject());
                basePath = scanner.getBasedir().getPath() + File.separator;
                files    = scanner.getIncludedFiles();
            }
            else
            {
                throw new BuildException("The refid attribute doesn't point to a <path> element or a <fileset> element");
            }
        }
        else
        {
            // Get the names of the files in this path.
            files = list();
        }

        for (int index = 0; index < files.length; index++)
        {
            // Create a new class path entry, with the proper file name and
            // any filters.
            ClassPathEntry entry = new ClassPathEntry(basePath + files[index], output);
            entry.setFilter(filter);
            entry.setJarFilter(jarFilter);
            entry.setWarFilter(warFilter);
            entry.setEarFilter(earFilter);
            entry.setZipFilter(zipFilter);

            // Add it to the class path.
            classPath.add(entry);
        }
    }


    // Ant task attributes.

    /**
     * @deprecated Use {@link #setLocation(File)} instead.
     */
    public void setFile(File file)
    {
        setLocation(file);
    }


    /**
     * @deprecated Use {@link #setLocation(File)} instead.
     */
    public void setDir(File file)
    {
        setLocation(file);
    }


    /**
     * @deprecated Use {@link #setLocation(File)} instead.
     */
    public void setName(File file)
    {
        setLocation(file);
    }


    public void setFilter(String filter)
    {
        this.filter = filter;
    }


    public void setJarFilter(String jarFilter)
    {
        this.jarFilter = jarFilter;
    }


    public void setWarFilter(String warFilter)
    {
        this.warFilter = warFilter;
    }


    public void setEarFilter(String earFilter)
    {
        this.earFilter = earFilter;
    }


    public void setZipFilter(String zipFilter)
    {
        this.zipFilter = zipFilter;
    }
}
