/* $Id: ProGuardTask.java,v 1.32.2.1 2006/01/16 22:57:55 eric Exp $
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
package proguard.ant;

import org.apache.tools.ant.*;
import proguard.*;
import proguard.classfile.util.*;

import java.io.*;

/**
 * This Task allows to configure and run ProGuard from Ant.
 *
 * @author Eric Lafortune
 */
public class ProGuardTask extends ConfigurationTask
{
    // Ant task attributes.

    public void setConfiguration(File configurationFile) throws BuildException
    {
        try
        {
            ConfigurationParser parser = new ConfigurationParser(configurationFile);

            try
            {
                parser.parse(configuration);
            }
            catch (ParseException ex)
            {
                throw new BuildException(ex.getMessage());
            }
            finally
            {
                parser.close();
            }
        }
        catch (IOException ex)
        {
            throw new BuildException(ex.getMessage());
        }
    }


    /**
     * @deprecated Use the nested outjar element instead.
     */
    public void setOutjar(String parameters)
    {
        throw new BuildException("Use the <outjar> nested element instead of the 'outjar' attribute");
    }


    public void setSkipnonpubliclibraryclasses(boolean skipNonPublicLibraryClasses)
    {
        configuration.skipNonPublicLibraryClasses = skipNonPublicLibraryClasses;
    }


    public void setSkipnonpubliclibraryclassmembers(boolean skipNonPublicLibraryClassMembers)
    {
        configuration.skipNonPublicLibraryClassMembers = skipNonPublicLibraryClassMembers;
    }


    public void setPrintseeds(File printSeeds)
    {
        configuration.printSeeds = optionalFile(printSeeds);
    }


    public void setShrink(boolean shrink)
    {
        configuration.shrink = shrink;
    }


    public void setPrintusage(File printUsage)
    {
        configuration.printUsage = optionalFile(printUsage);
    }


    public void setOptimize(boolean optimize)
    {
        configuration.optimize = optimize;
    }


    public void setAllowaccessmodification(boolean allowAccessModification)
    {
        configuration.allowAccessModification = allowAccessModification;
    }


    public void setObfuscate(boolean obfuscate)
    {
        configuration.obfuscate = obfuscate;
    }


    public void setPrintmapping(File printMapping)
    {
        configuration.printMapping = optionalFile(printMapping);
    }


    public void setApplymapping(File applyMapping)
    {
        configuration.applyMapping = resolvedFile(applyMapping);
    }


    public void setObfuscationdictionary(File obfuscationDictionary)
    {
        configuration.obfuscationDictionary = resolvedFile(obfuscationDictionary);
    }


    public void setOverloadaggressively(boolean overloadAggressively)
    {
        configuration.overloadAggressively = overloadAggressively;
    }


    public void setDefaultpackage(String defaultPackage)
    {
        configuration.defaultPackage = ClassUtil.internalClassName(defaultPackage);
    }


    public void setUsemixedcaseclassnames(boolean useMixedCaseClassNames)
    {
        configuration.useMixedCaseClassNames = useMixedCaseClassNames;
    }


    public void setRenamesourcefileattribute(String newSourceFileAttribute)
    {
        configuration.newSourceFileAttribute = newSourceFileAttribute;
    }


    public void setVerbose(boolean verbose)
    {
        configuration.verbose = verbose;
    }


    public void setNote(boolean note)
    {
        configuration.note = note;
    }


    public void setWarn(boolean warn)
    {
        configuration.warn = warn;
    }


    public void setIgnorewarnings(boolean ignoreWarnings)
    {
        configuration.ignoreWarnings = ignoreWarnings;
    }


    public void setDump(File dump)
    {
        configuration.dump = optionalFile(dump);
    }


    // Implementations for Task.

    public void execute() throws BuildException
    {
        try
        {
            ProGuard proGuard = new ProGuard(configuration);
            proGuard.execute();
        }
        catch (IOException ex)
        {
            throw new BuildException(ex.getMessage());
        }
    }


    // Small utility methods.

    /**
     * Returns a file that is properly resolved with respect to the project
     * directory, or <code>null</code> or empty if its name is actually a
     * boolean flag.
     */
    private File optionalFile(File file)
    {
        String fileName = file.getName();

        return
            fileName.equalsIgnoreCase("false") ||
            fileName.equalsIgnoreCase("no")    ||
            fileName.equalsIgnoreCase("off")    ? null :
            fileName.equalsIgnoreCase("true")  ||
            fileName.equalsIgnoreCase("yes")   ||
            fileName.equalsIgnoreCase("on")     ? new File("")   :
                                                  resolvedFile(file);
    }


    /**
     * Returns a file that is properly resolved with respect to the project
     * directory.
     */
    private File resolvedFile(File file)
    {
        return file.isAbsolute() ? file :
                                   new File(getProject().getBaseDir(),
                                            file.getName());
    }
}
