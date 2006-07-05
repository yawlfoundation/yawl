/* $Id: InputReader.java,v 1.1.2.1 2006/03/26 14:30:14 eric Exp $
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
import proguard.classfile.visitor.ClassPoolFiller;
import proguard.io.*;

import java.io.IOException;

/**
 * This class reads the input class files.
 *
 * @author Eric Lafortune
 */
public class InputReader
{
    private Configuration configuration;


    /**
     * Creates a new InputReader to read input class files as specified by the
     * given configuration.
     */
    public InputReader(Configuration configuration)
    {
        this.configuration = configuration;
    }


    /**
     * Fills the given program class pool and library class pool by reading
     * class files, based on the current configuration.
     */
    public void execute(ClassPool programClassPool,
                        ClassPool libraryClassPool) throws IOException
    {
        // Check if we have at least some program jars.
        if (configuration.programJars == null)
        {
            throw new IOException("The input is empty. You have to specify one or more '-injars' options.");
        }

        // Read the program class files.
        readInput("Reading program ",
                  configuration.programJars,
                  createDataEntryClassPoolFiller(programClassPool, false));

        // Check if we have at least some input classes.
        if (programClassPool.size() == 0)
        {
            throw new IOException("The input doesn't contain any classes. Did you specify the proper '-injars' options?");
        }

        // Read the library class files, if any.
        if (configuration.libraryJars != null)
        {
            readInput("Reading library ",
                      configuration.libraryJars,
                      createDataEntryClassPoolFiller(libraryClassPool, true));
        }
    }


    /**
     * Creates a DataEntryReader that will decode classes and put them in
     * the given class pool.
     */
    private DataEntryReader createDataEntryClassPoolFiller(ClassPool classPool,
                                                           boolean   isLibrary)
    {
        // Prepare a data entry reader to filter all classes,
        // which are then decoded to classes by a class reader,
        // which are then put in the class pool by a class pool filler.
        return
            new ClassFileFilter(
            new ClassFileReader(isLibrary,
                                configuration.skipNonPublicLibraryClasses,
                                configuration.skipNonPublicLibraryClassMembers,
                                configuration.note,
            new ClassPoolFiller(classPool, configuration.note)));
    }


    /**
     * Reads all input entries from the given class path.
     */
    private void readInput(String          messagePrefix,
                           ClassPath       classPath,
                           DataEntryReader reader) throws IOException
    {
        readInput(messagePrefix,
                  classPath,
                  0,
                  classPath.size(),
                  reader);
    }


    /**
     * Reads all input entries from the given section of the given class path.
     */
    public void readInput(String          messagePrefix,
                          ClassPath       classPath,
                          int             fromIndex,
                          int             toIndex,
                          DataEntryReader reader) throws IOException
    {
        for (int index = fromIndex; index < toIndex; index++)
        {
            ClassPathEntry entry = classPath.get(index);
            if (!entry.isOutput())
            {
                readInput(messagePrefix, entry, reader);
            }
        }
    }


    /**
     * Reads the given input class path entry.
     */
    private void readInput(String          messagePrefix,
                           ClassPathEntry  classPathEntry,
                           DataEntryReader dataEntryReader) throws IOException
    {
        try
        {
            // Create a reader that can unwrap jars, wars, ears, and zips.
            DataEntryReader reader =
                DataEntryReaderFactory.createDataEntryReader(messagePrefix,
                                                             classPathEntry,
                                                             dataEntryReader);

            // Create the data entry pump.
            DirectoryPump directoryPump =
                new DirectoryPump(classPathEntry.getFile());

            // Pump the data entries into the reader.
            directoryPump.pumpDataEntries(reader);
        }
        catch (IOException ex)
        {
            throw new IOException("Can't read [" + classPathEntry + "] (" + ex.getMessage() + ")");
        }
    }
}
