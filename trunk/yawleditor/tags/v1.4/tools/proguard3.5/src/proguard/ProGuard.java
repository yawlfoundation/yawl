/* $Id: ProGuard.java,v 1.101.2.2 2006/01/16 22:57:55 eric Exp $
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
import proguard.classfile.attribute.AllAttrInfoVisitor;
import proguard.classfile.editor.ConstantPoolSorter;
import proguard.classfile.instruction.AllInstructionVisitor;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import proguard.io.*;
import proguard.obfuscate.Obfuscator;
import proguard.optimize.Optimizer;
import proguard.shrink.Shrinker;
import proguard.util.ClassNameListMatcher;

import java.io.*;
import java.util.*;

/**
 * Tool for shrinking, optimizing, and obfuscating Java class files.
 *
 * @author Eric Lafortune
 */
public class ProGuard
{
    public static final String VERSION = "ProGuard, version 3.5";

    private Configuration configuration;
    private ClassPool     programClassPool = new ClassPool();
    private ClassPool     libraryClassPool = new ClassPool();


    /**
     * Creates a new ProGuard object to process jars as specified by the given
     * configuration.
     */
    public ProGuard(Configuration configuration)
    {
        this.configuration = configuration;
    }


    /**
     * Performs all subsequent ProGuard operations.
     */
    public void execute() throws IOException
    {
        System.out.println(VERSION);

        GPL.check();

        readInput();

        // The defaultPackage option implies the allowAccessModification option.
        if (configuration.defaultPackage != null)
        {
            configuration.allowAccessModification = true;
        }

        if (configuration.shrink   ||
            configuration.optimize ||
            configuration.obfuscate)
        {
            initialize();
        }

        if (configuration.printSeeds != null)
        {
            printSeeds();
        }

        if (configuration.shrink)
        {
            shrink();
        }

        if (configuration.optimize)
        {
            optimize();

            // Shrink again, if we may.
            if (configuration.shrink)
            {
                // Don't print any usage this time around.
                configuration.printUsage       = null;
                configuration.whyAreYouKeeping = null;

                shrink();
            }
        }

        if (configuration.obfuscate)
        {
            obfuscate();
        }

        if (configuration.shrink   ||
            configuration.optimize ||
            configuration.obfuscate)
        {
            sortConstantPools();
        }

        if (configuration.programJars.hasOutput())
        {
            writeOutput();
        }

        if (configuration.dump != null)
        {
            dump();
        }
    }


    /**
     * Reads the input jars (or directories).
     */
    private void readInput() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Reading jars...");
        }

        // Check if we have at least some program jars.
        if (configuration.programJars == null)
        {
            throw new IOException("The input is empty. You have to specify one or more '-injars' options.");
        }

        // Read the input program jars.
        readInput("Reading program ",
                  configuration.programJars,
                  createDataEntryClassPoolFiller(false));

        // Check if we have at least some input class files.
        if (programClassPool.size() == 0)
        {
            throw new IOException("The input doesn't contain any class files. Did you specify the proper '-injars' options?");
        }

        // Read all library jars.
        if (configuration.libraryJars != null)
        {
            readInput("Reading library ",
                      configuration.libraryJars,
                      createDataEntryClassPoolFiller(true));
        }
    }


    /**
     * Creates a DataEntryReader that will decode class files and put them in
     * the proper class pool.
     */
    private DataEntryReader createDataEntryClassPoolFiller(boolean isLibrary)
    {
        // Get the proper class pool.
        ClassPool classPool = isLibrary ?
            libraryClassPool :
            programClassPool;

        // Prepare a data entry reader to filter all class files,
        // which are then decoded to class files by a class file reader,
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
    private void readInput(String          messagePrefix,
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


    /**
     * Initializes the cross-references between all class files.
     */
    private void initialize() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Initializing...");
        }

        int originalLibraryClassPoolSize = libraryClassPool.size();

        // Initialize the class hierarchy for program class files.
        ClassFileHierarchyInitializer classFileHierarchyInitializer =
            new ClassFileHierarchyInitializer(programClassPool,
                                              libraryClassPool,
                                              configuration.warn);

        programClassPool.classFilesAccept(classFileHierarchyInitializer);

        // Initialize the class hierarchy for library class files.
        ClassFileHierarchyInitializer classFileHierarchyInitializer2 =
            new ClassFileHierarchyInitializer(programClassPool,
                                              libraryClassPool,
                                              false);

        libraryClassPool.classFilesAccept(classFileHierarchyInitializer2);

        // Initialize the Class.forName and .class references.
        ClassFileClassForNameReferenceInitializer classFileClassForNameReferenceInitializer =
            new ClassFileClassForNameReferenceInitializer(programClassPool,
                                                          libraryClassPool,
                                                          configuration.note,
                                                          createNoteExceptionMatcher(configuration.keep));

        programClassPool.classFilesAccept(
            new AllMethodVisitor(
            new AllAttrInfoVisitor(
            new AllInstructionVisitor(classFileClassForNameReferenceInitializer))));

        // Initialize the class references from program class members and attributes.
        ClassFileReferenceInitializer classFileReferenceInitializer =
            new ClassFileReferenceInitializer(programClassPool,
                                              libraryClassPool,
                                              configuration.warn);

        programClassPool.classFilesAccept(classFileReferenceInitializer);

        // Reinitialize the library class pool with only those library classes
        // whose hierarchies are referenced by the program classes.
        ClassPool newLibraryClassPool = new ClassPool();
        programClassPool.classFilesAccept(
            new AllCpInfoVisitor(
            new ReferencedClassFileVisitor(
            new LibraryClassFileFilter(
            new ClassFileHierarchyTraveler(true, true, true, false,
            new LibraryClassFileFilter(
            new ClassPoolFiller(newLibraryClassPool, false)))))));

        libraryClassPool = newLibraryClassPool;

        // Initialize the class references from library class members.
        ClassFileReferenceInitializer classFileReferenceInitializer2 =
            new ClassFileReferenceInitializer(programClassPool,
                                              libraryClassPool,
                                              false);

        libraryClassPool.classFilesAccept(classFileReferenceInitializer2);

        int noteCount = classFileClassForNameReferenceInitializer.getNoteCount();
        if (noteCount > 0)
        {
            System.err.println("Note: there were " + noteCount +
                               " class casts of dynamically created class instances.");
            System.err.println("      You might consider explicitly keeping the mentioned classes and/or");
            System.err.println("      their implementations (using '-keep').");
        }

        int hierarchyWarningCount = classFileHierarchyInitializer.getWarningCount();
        if (hierarchyWarningCount > 0)
        {
            System.err.println("Warning: there were " + hierarchyWarningCount +
                               " unresolved references to superclasses or interfaces.");
            System.err.println("         You may need to specify additional library jars (using '-libraryjars'),");
            System.err.println("         or perhaps the '-dontskipnonpubliclibraryclasses' option.");
        }

        int referenceWarningCount = classFileReferenceInitializer.getWarningCount();
        if (referenceWarningCount > 0)
        {
            System.err.println("Warning: there were " + referenceWarningCount +
                               " unresolved references to program class members.");
            System.err.println("         Your input class files appear to be inconsistent.");
            System.err.println("         You may need to recompile them and try again.");
        }

        if ((hierarchyWarningCount > 0 ||
             referenceWarningCount > 0) &&
            !configuration.ignoreWarnings)
        {
            System.err.println("         If you are sure the mentioned classes are not used anyway,");
            System.err.println("         you could try your luck using the '-ignorewarnings' option.");
            throw new IOException("Please correct the above warnings first.");
        }

        // Discard unused library classes.
        if (configuration.verbose)
        {
            System.out.println("Removed unused library classes...");
            System.out.println("  Original number of library classes: " + originalLibraryClassPoolSize);
            System.out.println("  Final number of library classes:    " + libraryClassPool.size());
        }
    }


    /**
     * Extracts a list of exceptions for which not to print notes, from the
     * keep configuration.
     */
    private ClassNameListMatcher createNoteExceptionMatcher(List noteExceptions)
    {
        if (noteExceptions != null)
        {
            List noteExceptionNames = new ArrayList(noteExceptions.size());
            for (int index = 0; index < noteExceptions.size(); index++)
            {
                ClassSpecification classSpecification = (ClassSpecification)noteExceptions.get(index);
                if (classSpecification.markClassFiles)
                {
                    // If the class itself is being kept, it's ok.
                    String className = classSpecification.className;
                    if (className != null)
                    {
                        noteExceptionNames.add(className);
                    }

                    // If all of its extensions are being kept, it's ok too.
                    String extendsClassName = classSpecification.extendsClassName;
                    if (extendsClassName != null)
                    {
                        noteExceptionNames.add(extendsClassName);
                    }
                }
            }

            if (noteExceptionNames.size() > 0)
            {
                return new ClassNameListMatcher(noteExceptionNames);
            }
        }

        return null;
    }


    /**
     * Prints out classes and class members that are used as seeds in the
     * shrinking and obfuscation steps.
     */
    private void printSeeds() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Printing kept classes, fields, and methods...");
        }

        // Check if we have at least some keep commands.
        if (configuration.keep == null)
        {
            throw new IOException("You have to specify '-keep' options for the shrinking step.");
        }

        PrintStream ps = isFile(configuration.printSeeds) ?
            new PrintStream(new BufferedOutputStream(new FileOutputStream(configuration.printSeeds))) :
            System.out;

        // Create a visitor for printing out the seeds. Note that we're only
        // printing out the program elements that are preserved against shrinking.
        SimpleClassFilePrinter printer = new SimpleClassFilePrinter(false, ps);
        ClassPoolVisitor classPoolvisitor =
            ClassSpecificationVisitorFactory.createClassPoolVisitor(configuration.keep,
                                                                    new ProgramClassFileFilter(printer),
                                                                    new ProgramMemberInfoFilter(printer));

        // Print out the seeds.
        programClassPool.accept(classPoolvisitor);
        libraryClassPool.accept(classPoolvisitor);

        if (ps != System.out)
        {
            ps.close();
        }
    }


    /**
     * Performs the shrinking step.
     */
    private void shrink() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Shrinking...");

            // We'll print out some explanation, if requested.
            if (configuration.whyAreYouKeeping != null)
            {
                System.out.println("Explaining why classes and class members are being kept...");
            }

            // We'll print out the usage, if requested.
            if (configuration.printUsage != null)
            {
                System.out.println("Printing usage" +
                                   (isFile(configuration.printUsage) ?
                                       " to [" + configuration.printUsage.getAbsolutePath() + "]" :
                                       "..."));
            }
        }

        // Check if we have at least some keep commands.
        if (configuration.keep == null)
        {
            throw new IOException("You have to specify '-keep' options for the shrinking step.");
        }

        int originalProgramClassPoolSize = programClassPool.size();

        // Perform the actual shrinking.
        programClassPool = new Shrinker(configuration).execute(programClassPool, libraryClassPool);

        // Check if we have at least some output class files.
        int newProgramClassPoolSize = programClassPool.size();
        if (newProgramClassPoolSize == 0)
        {
            throw new IOException("The output jar is empty. Did you specify the proper '-keep' options?");
        }

        if (configuration.verbose)
        {
            System.out.println("Removed unused program classes and class elements...");
            System.out.println("  Original number of program classes: " + originalProgramClassPoolSize);
            System.out.println("  Final number of program classes:    " + newProgramClassPoolSize);
        }
    }


    /**
     * Performs the optimization step.
     */
    private void optimize() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Optimizing...");
        }

        // Check if we have at least some keep commands.
        if (configuration.keep         == null &&
            configuration.keepNames    == null &&
            configuration.applyMapping == null &&
            configuration.printMapping == null)
        {
            throw new IOException("You have to specify '-keep' options for the optimization step.");
        }

        // Perform the actual optimization.
        new Optimizer(configuration).execute(programClassPool, libraryClassPool);
    }


    /**
     * Performs the obfuscation step.
     */
    private void obfuscate() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Obfuscating...");

            // We'll apply a mapping, if requested.
            if (configuration.applyMapping != null)
            {
                System.out.println("Applying mapping [" + configuration.applyMapping.getAbsolutePath() + "]");
            }

            // We'll print out the mapping, if requested.
            if (configuration.printMapping != null)
            {
                System.out.println("Printing mapping" +
                                   (isFile(configuration.printMapping) ?
                                       " to [" + configuration.printMapping.getAbsolutePath() + "]" :
                                       "..."));
            }
        }

        // Perform the actual obfuscation.
        new Obfuscator(configuration).execute(programClassPool, libraryClassPool);
    }


    /**
     * Sorts the constant pools of all program class files.
     */
    private void sortConstantPools()
    {
        // TODO: Avoid duplicate constant pool entries.
        programClassPool.classFilesAccept(new ConstantPoolSorter(1024));
    }


    /**
     * Writes the output jars.
     */
    private void writeOutput() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Writing jars...");
        }

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
                    writeOutput(programJars,
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
    private void writeOutput(ClassPath classPath,
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

            // Create the reader that can write class files and copy resource
            // files to the above writer.
            DataEntryReader reader =
                new ClassFileFilter(new ClassFileRewriter(programClassPool,
                                                          writer),
                                    new DataEntryCopier(writer));

            // Read and handle the specified input entries.
            readInput("  Copying resources from program ",
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


    /**
     * Prints out the contents of the program class files.
     */
    private void dump() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Printing classes" +
                               (isFile(configuration.dump) ?
                                   " to [" + configuration.dump.getAbsolutePath() + "]" :
                                   "..."));
        }

        PrintStream ps = isFile(configuration.dump) ?
            new PrintStream(new BufferedOutputStream(new FileOutputStream(configuration.dump))) :
            System.out;

        programClassPool.classFilesAccept(new ClassFilePrinter(ps));

        if (isFile(configuration.dump))
        {
            ps.close();
        }
    }


    /**
     * Returns whether the given file is actually a file, or just a placeholder
     * for the standard output.
     */
    private boolean isFile(File file)
    {
        return file.getPath().length() > 0;
    }


    /**
     * The main method for ProGuard.
     */
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.println(VERSION);
            System.out.println("Usage: java proguard.ProGuard [options ...]");
            System.exit(1);
        }

        // Create the default options.
        Configuration configuration = new Configuration();

        try
        {
            // Parse the options specified in the command line arguments.
            ConfigurationParser parser = new ConfigurationParser(args);

            try
            {
                parser.parse(configuration);

                // Execute ProGuard with these options.
                new ProGuard(configuration).execute();
            }
            finally
            {
                parser.close();
            }
        }
        catch (Exception ex)
        {
            if (configuration.verbose)
            {
                // Print a verbose stack trace.
                ex.printStackTrace();
            }
            else
            {
                // Print just the stack trace message.
                System.err.println("Error: "+ex.getMessage());
            }

            System.exit(1);
        }

        System.exit(0);
    }
}
