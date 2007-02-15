/* $Id: Shrinker.java,v 1.1.2.3 2006/11/25 16:56:11 eric Exp $
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
package proguard.shrink;

import proguard.*;
import proguard.classfile.ClassPool;
import proguard.classfile.visitor.*;

import java.io.*;

/**
 * This class shrinks class pools according to a given configuration.
 *
 * @author Eric Lafortune
 */
public class Shrinker
{
    private Configuration configuration;


    /**
     * Creates a new Shrinker.
     */
    public Shrinker(Configuration configuration)
    {
        this.configuration = configuration;
    }


    /**
     * Performs obfuscation of the given program class pool.
     */
    public ClassPool execute(ClassPool programClassPool,
                             ClassPool libraryClassPool) throws IOException
    {
        // Clean up any old visitor info.
        programClassPool.classFilesAccept(new ClassFileCleaner());
        libraryClassPool.classFilesAccept(new ClassFileCleaner());

        // Create a visitor for marking the seeds.
        UsageMarker usageMarker = configuration.whyAreYouKeeping == null ?
            new UsageMarker() :
            new ShortestUsageMarker();

        ClassPoolVisitor classPoolvisitor =
            ClassSpecificationVisitorFactory.createClassPoolVisitor(configuration.keep,
                                                                    usageMarker,
                                                                    usageMarker);
        // Mark the seeds.
        programClassPool.accept(classPoolvisitor);
        libraryClassPool.accept(classPoolvisitor);

        // Mark interfaces that have to be kept.
        programClassPool.classFilesAccept(new InterfaceUsageMarker(usageMarker));

        // Mark the inner class information that has to be kept.
        programClassPool.classFilesAccept(new InnerUsageMarker(usageMarker));

        if (configuration.whyAreYouKeeping != null)
        {
            System.out.println();

            // Create a visitor for explaining classes and class members.
            ShortestUsagePrinter shortestUsagePrinter =
                new ShortestUsagePrinter((ShortestUsageMarker)usageMarker,
                                         configuration.verbose);

            ClassPoolVisitor whyClassPoolvisitor =
                ClassSpecificationVisitorFactory.createClassPoolVisitor(configuration.whyAreYouKeeping,
                                                                        shortestUsagePrinter,
                                                                        shortestUsagePrinter);

            // Mark the seeds.
            programClassPool.accept(whyClassPoolvisitor);
            libraryClassPool.accept(whyClassPoolvisitor);
        }

        if (configuration.printUsage != null)
        {
            PrintStream ps = isFile(configuration.printUsage) ?
                new PrintStream(new BufferedOutputStream(new FileOutputStream(configuration.printUsage))) :
                System.out;

            // Print out items that will be removed.
            programClassPool.classFilesAcceptAlphabetically(
                new UsagePrinter(usageMarker, true, ps));

            if (ps != System.out)
            {
                ps.close();
            }
        }

        // Discard unused program classes.
        ClassPool newProgramClassPool = new ClassPool();
        programClassPool.classFilesAccept(
            new UsedClassFileFilter(usageMarker,
            new MultiClassFileVisitor(
            new ClassFileVisitor[] {
                new ClassFileShrinker(usageMarker, 1024),
                new ClassPoolFiller(newProgramClassPool)
            })));

        return newProgramClassPool;
    }


    /**
     * Returns whether the given file is actually a file, or just a placeholder
     * for the standard output.
     */
    private boolean isFile(File file)
    {
        return file.getPath().length() > 0;
    }
}
