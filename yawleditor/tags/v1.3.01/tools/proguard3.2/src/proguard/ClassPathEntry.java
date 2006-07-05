/* $Id: ClassPathEntry.java,v 1.8 2004/08/15 12:39:30 eric Exp $
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
package proguard;


/**
 * This class represents an entry from a class path: a jar, a war, a zip, an
 * ear, or a directory, with a name and a flag to indicates whether the entry is
 * an input entry or an output entry. Optional filters can be specified for the
 * names of the contained resource/class files, jars, wars, ears, and zips.
 *
 * @author Eric Lafortune
 */
public class ClassPathEntry
{
    private String  name;
    private boolean output;
    private String  filter;
    private String  jarFilter;
    private String  warFilter;
    private String  earFilter;
    private String  zipFilter;


    /**
     * Creates a new ClassPathEntry with the given name and type.
     */
    public ClassPathEntry(String name, boolean isOutput)
    {
        this.name   = name;
        this.output = isOutput;
    }


    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }


    public boolean isOutput()
    {
        return output;
    }


    public void setOutput(boolean output)
    {
        this.output = output;
    }


    public String getFilter()
    {
        return filter;
    }

    public void setFilter(String filter)
    {
        this.filter = filter == null || filter.length() == 0 ? null : filter;
    }


    public String getJarFilter()
    {
        return jarFilter;
    }

    public void setJarFilter(String filter)
    {
        this.jarFilter = filter == null || filter.length() == 0 ? null : filter;
    }


    public String getWarFilter()
    {
        return warFilter;
    }

    public void setWarFilter(String filter)
    {
        this.warFilter = filter == null || filter.length() == 0 ? null : filter;
    }


    public String getEarFilter()
    {
        return earFilter;
    }

    public void setEarFilter(String filter)
    {
        this.earFilter = filter == null || filter.length() == 0 ? null : filter;
    }


    public String getZipFilter()
    {
        return zipFilter;
    }

    public void setZipFilter(String filter)
    {
        this.zipFilter = filter == null || filter.length() == 0 ? null : filter;
    }


    public String toString()
    {
        String string = name;

        if (filter    != null ||
            jarFilter != null ||
            warFilter != null ||
            earFilter != null ||
            zipFilter != null)
        {
            string +=
                ConfigurationConstants.OPEN_ARGUMENTS_KEYWORD +
                (zipFilter != null ? zipFilter : "")  +
                ConfigurationConstants.SEPARATOR_KEYWORD +
                (earFilter != null ? earFilter : "")  +
                ConfigurationConstants.SEPARATOR_KEYWORD +
                (warFilter != null ? warFilter : "")  +
                ConfigurationConstants.SEPARATOR_KEYWORD +
                (jarFilter != null ? jarFilter : "")  +
                ConfigurationConstants.SEPARATOR_KEYWORD +
                (filter    != null ? filter    : "")  +
                ConfigurationConstants.CLOSE_ARGUMENTS_KEYWORD;
        }

        return string;
    }
}
