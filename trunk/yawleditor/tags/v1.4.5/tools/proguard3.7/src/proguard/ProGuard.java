/* $Id: ProGuard.java,v 1.101.2.13 2006/12/11 21:57:04 eric Exp $
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
import proguard.classfile.editor.ConstantPoolSorter;
import proguard.classfile.visitor.*;
import proguard.obfuscate.Obfuscator;
import proguard.optimize.Optimizer;
import proguard.shrink.Shrinker;

import java.io.*;

/**
 * Tool for shrinking, optimizing, and obfuscating Java class files.
 *
 * @author Eric Lafortune
 */
public class ProGuard
{
    public static final String VERSION = "ProGuard, version 3.7";

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
     * Reads the input class files.
     */
    private void readInput() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Reading input...");
        }

        // Fill the program class pool and the library class pool.
        new InputReader(configuration).execute(programClassPool, libraryClassPool);
    }


    /**
     * Initializes the cross-references between all classes, performs some
     * basic checks, and shrinks the library class pool.
     */
    private void initialize() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Initializing...");
        }

        new Initializer(configuration).execute(programClassPool, libraryClassPool);
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
            System.out.println("Removing unused program classes and class elements...");
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
     * Writes the output claaa files.
     */
    private void writeOutput() throws IOException
    {
        if (configuration.verbose)
        {
            System.out.println("Writing output...");
        }

        // Write out the program class pool.
        new OutputWriter(configuration).execute(programClassPool);
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
