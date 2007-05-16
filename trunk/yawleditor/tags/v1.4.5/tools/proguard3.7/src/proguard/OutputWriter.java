/* $Id: OutputWriter.java,v 1.1.2.1 2006/03/26 14:30:14 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java bytecode.
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
package proguard;

import proguard.classfile.ClassPool;
import proguard.io.*;

import java.io.IOException;

/**
 * This class writes the output class files.
 *
 * @author Eric Lafortune
 */
public class OutputWriter
{
    private Configuration configuration;


    /**
     * Creates a new OutputWriter to write output class files as specified by
     * the given configuration.
     */
    public OutputWriter(Configuration configuration)
    {
        this.configuration = configuration;
    }


    /**
     * Writes the given class pool to class files, based on the current
     * configuration.
     */
    public void execute(ClassPool programClassPool) throws IOException
    {
        ClassPath programJars = configuration.programJars;

        // Perform a check on the first jar.
        ClassPathEntry firstEntry = programJars.get(0);
        if (firstEntry.isOutput())
        {
            throw new IOException("The output jar [" + firstEntry.getName() +
                                  "] must be specified after an input jar, or it will be empty.");
        }

        // Perform some checks on the output jars.
        for (int index = 0; index < programJars.size() - 1; index++)
        {
            ClassPathEntry entry = programJars.get(index);
            if (entry.isOutput())
            {
                // Check if all but the last output jars have filters.
                if (entry.getFilter()    == null &&
                    entry.getJarFilter() == null &&
                    entry.getWarFilter() == null &&
                    entry.getEarFilter() == null &&
                    entry.getZipFilter() == null &&
                    programJars.get(index + 1).isOutput())
                {
                    throw new IOException("The output jar [" + entry.getName() +
                                          "] must have a filter, or all subsequent jars will be empty.");
                }

                // Check if the output jar name is different from the input jar names.
                for (int inIndex = 0; inIndex < programJars.size(); inIndex++)
                {
                    ClassPathEntry otherEntry = programJars.get(inIndex);

                    if (!otherEntry.isOutput() &&
                        entry.getFile().equals(otherEntry.getFile()))
                    {
                        throw new IOException("The output jar [" + entry.getName() +
                                              "] must be different from all input jars.");
                    }
                }
            }
        }

        int firstInputIndex = 0;
        int lastInputIndex  = 0;

        // Go over all program class path entries.
        for (int index = 0; index < programJars.size(); index++)
        {
            // Is it an input entry?
            ClassPathEntry entry = programJars.get(index);
            if (!entry.isOutput())
            {
                // Remember the index of the last input entry.
                lastInputIndex = index;
            }
            else
            {
                // Check if this the last output entry in a series.
                int nextIndex = index + 1;
                if (nextIndex == programJars.size() ||
                    !programJars.get(nextIndex).isOutput())
                {
                    // Write the processed input entries to the output entries.
                    writeOutput(programClassPool,
                                programJars,
                                firstInputIndex,
                                lastInputIndex + 1,
                                nextIndex);

                    // Start with the next series of input entries.
                    firstInputIndex = nextIndex;
                }
            }
        }
    }


    /**
     * Transfers the specified input jars to the specified output jars.
     */
    private void writeOutput(ClassPool programClassPool,
                             ClassPath classPath,
                             int       fromInputIndex,
                             int       fromOutputIndex,
                             int       toOutputIndex)
    throws IOException
    {
        try
        {
            // Construct the writer that can write jars, wars, ears, zips, and
            // directories, cascading over the specified output entries.
            DataEntryWriter writer =
                DataEntryWriterFactory.createDataEntryWriter(classPath,
                                                             fromOutputIndex,
                                                             toOutputIndex);

            // Create the reader that can write classes and copy resource
            // files to the above writer.
            DataEntryReader reader =
                new ClassFileFilter(new ClassFileRewriter(programClassPool,
                                                          writer),
                                    new DataEntryCopier(writer));

            // Go over the specified input entries and write their processed
            // versions.
            new InputReader(configuration).readInput("  Copying resources from program ",
                                                     classPath,
                                                     fromInputIndex,
                                                     fromOutputIndex,
                                                     reader);

            // Close all output entries.
            writer.close();
        }
        catch (IOException ex)
        {
            throw new IOException("Can't write [" + classPath.get(fromOutputIndex).getName() + "] (" + ex.getMessage() + ")");
        }
    }
}
