/* $Id: ConfigurationTask.java,v 1.3 2004/10/31 16:28:32 eric Exp $
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

import proguard.*;

import org.apache.tools.ant.*;

import java.io.*;
import java.util.*;

/**
 * This Task allows to define a ProGuard configuration from Ant.
 *
 * @author Eric Lafortune
 */
public class ConfigurationTask extends Task
{
    protected Configuration configuration = new Configuration();


    /**
     * Adds the contents of this configuration task to the given configuration.
     * @param configuration the configuration to be extended.
     */
    public void appendTo(Configuration configuration)
    {
        // Append all of these configuration entries to the given configuration.
        configuration.programJars = extendClassPath(configuration.programJars,
                                                    this.configuration.programJars);

        configuration.libraryJars = extendClassPath(configuration.libraryJars,
                                                    this.configuration.libraryJars);

        configuration.keep = extendClassSpecifications(configuration.keep,
                                                       this.configuration.keep);

        configuration.keepNames = extendClassSpecifications(configuration.keepNames,
                                                            this.configuration.keepNames);

        configuration.keepAttributes = extendAttributes(configuration.keepAttributes,
                                                        this.configuration.keepAttributes);
    }


    // Ant task nested elements.

    public void addConfiguredInjar(ClassPathElement classPathElement)
    {
        configuration.programJars = extendClassPath(configuration.programJars,
                                                    classPathElement,
                                                    false);
    }


    public void addConfiguredOutjar(ClassPathElement classPathElement)
    {
        configuration.programJars = extendClassPath(configuration.programJars,
                                                    classPathElement,
                                                    true);
    }


    public void addConfiguredLibraryjar(ClassPathElement classPathElement)
    {
        configuration.libraryJars = extendClassPath(configuration.libraryJars,
                                                    classPathElement,
                                                    false);
    }


    public void addConfiguredKeep(ClassSpecificationElement classSpecificationElement)
    {
        configuration.keep = extendClassSpecifications(configuration.keep,
                                                       classSpecificationElement,
                                                       true,
                                                       false);
    }


    public void addConfiguredKeepclassmembers(ClassSpecificationElement classSpecificationElement)
    {
        configuration.keep = extendClassSpecifications(configuration.keep,
                                                       classSpecificationElement,
                                                       false,
                                                       false);
    }


    public void addConfiguredKeepclasseswithmembers(ClassSpecificationElement classSpecificationElement)
    {
        configuration.keep = extendClassSpecifications(configuration.keep,
                                                       classSpecificationElement,
                                                       true,
                                                       true);
    }


    public void addConfiguredKeepnames(ClassSpecificationElement classSpecificationElement)
    {
        configuration.keepNames = extendClassSpecifications(configuration.keepNames,
                                                            classSpecificationElement,
                                                            true,
                                                            false);
    }


    public void addConfiguredKeepclassmembernames(ClassSpecificationElement classSpecificationElement)
    {
        configuration.keepNames = extendClassSpecifications(configuration.keepNames,
                                                            classSpecificationElement,
                                                            false,
                                                            false);
    }


    public void addConfiguredKeepclasseswithmembernames(ClassSpecificationElement classSpecificationElement)
    {
        configuration.keepNames = extendClassSpecifications(configuration.keepNames,
                                                            classSpecificationElement,
                                                            true,
                                                            true);
    }


    public void addConfiguredKeepattribute(KeepAttributeElement keepAttributeElement)
    {
        configuration.keepAttributes = extendAttributes(configuration.keepAttributes,
                                                        keepAttributeElement);
    }


    public void addConfiguredConfiguration(ConfigurationElement configurationElement)
    {
        configurationElement.appendTo(configuration);
    }


    // Implementations for Task.

    public void addText(String text) throws BuildException
    {
        try
        {
            String arg = getProject().replaceProperties(text);
            ConfigurationParser parser = new ConfigurationParser(new String[] { arg });
            parser.parse(configuration);
        }
        catch (IOException ex)
        {
            throw new BuildException(ex.getMessage());
        }
        catch (ParseException ex)
        {
            throw new BuildException(ex.getMessage());
        }
    }


    // Small utility methods.

    private String optionalFileName(String fileName)
    {
        return
            fileName.equalsIgnoreCase("false") ||
            fileName.equalsIgnoreCase("no")    ||
            fileName.equalsIgnoreCase("off")    ? null :
            fileName.equalsIgnoreCase("true")  ||
            fileName.equalsIgnoreCase("yes")   ||
            fileName.equalsIgnoreCase("on")     ? ""   :
                                                  fileName;
    }


    private ClassPath extendClassPath(ClassPath        classPath,
                                      ClassPathElement classPathElement,
                                      boolean          output)
    {
        if (classPath == null)
        {
            classPath = new ClassPath();
        }

        classPathElement.appendClassPathEntriesTo(classPath,
                                                  output);

        return classPath;
    }


    private ClassPath extendClassPath(ClassPath classPath,
                                      ClassPath additionalClassPath)
    {
        if (additionalClassPath != null)
        {
            if (classPath == null)
            {
                classPath = new ClassPath();
            }

            classPath.addAll(additionalClassPath);
        }

        return classPath;
    }


    private List extendClassSpecifications(List                      classSpecifications,
                                           ClassSpecificationElement classSpecificationElement,
                                           boolean                   markClassFiles,
                                           boolean                   markClassFilesConditionally)
    {
        if (classSpecifications == null)
        {
            classSpecifications = new ArrayList();
        }

        classSpecificationElement.appendTo(classSpecifications,
                                           markClassFiles,
                                           markClassFilesConditionally);

        return classSpecifications;
    }


    private List extendClassSpecifications(List classSpecifications,
                                           List additionalClassSpecifications)
    {
        if (additionalClassSpecifications != null)
        {
            if (classSpecifications == null)
            {
                classSpecifications = new ArrayList();
            }

            classSpecifications.addAll(additionalClassSpecifications);
        }

        return classSpecifications;
    }


    private List extendAttributes(List                 attributes,
                                  KeepAttributeElement keepAttributeElement)
    {
        if (attributes == null)
        {
            attributes = new ArrayList();
        }

        keepAttributeElement.appendTo(attributes);

        return attributes;
    }


    private List extendAttributes(List attributes,
                                  List additionalAttributes)
    {
        if (additionalAttributes != null)
        {
            if (attributes == null)
            {
                attributes = new ArrayList();
            }

            attributes.addAll(additionalAttributes);
        }

        return attributes;
    }
}
