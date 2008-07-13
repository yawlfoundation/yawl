/* $Id: Initializer.java,v 1.2.2.11 2007/02/03 09:05:51 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java bytecode.
 *
 * Copyright (c) 2002-2007 Eric Lafortune (eric@graphics.cornell.edu)
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

import proguard.classfile.*;
import proguard.classfile.attribute.AllAttrInfoVisitor;
import proguard.classfile.instruction.AllInstructionVisitor;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import proguard.util.ClassNameListMatcher;

import java.io.IOException;
import java.util.*;

/**
 * This class initializes class pools.
 *
 * @author Eric Lafortune
 */
public class Initializer
{
    private Configuration configuration;


    /**
     * Creates a new Initializer to initialize classes according to the given
     * configuration.
     */
    public Initializer(Configuration configuration)
    {
        this.configuration = configuration;
    }


    /**
     * Initializes the classes in the given program class pool and library class
     * pool, performs some basic checks, and shrinks the library class pool.
     */
    public void execute(ClassPool programClassPool,
                        ClassPool libraryClassPool) throws IOException
    {
        int originalLibraryClassPoolSize = libraryClassPool.size();

        // Initialize the class hierarchy for program classes.
        WarningPrinter hierarchyWarningPrinter = configuration.warn ?
            new WarningPrinter(System.err) :
            null;

        programClassPool.classFilesAccept(
            new ClassFileHierarchyInitializer(programClassPool,
                                              libraryClassPool,
                                              hierarchyWarningPrinter));

        // Initialize the class hierarchy for library classes.
        libraryClassPool.classFilesAccept(
            new ClassFileHierarchyInitializer(programClassPool,
                                              libraryClassPool,
                                              null));

        // Initialize the Class.forName and .class references.
        WarningPrinter classForNameNotePrinter = configuration.note ?
            new WarningPrinter(System.out) :
            null;

        programClassPool.classFilesAccept(
            new AllMethodVisitor(
            new AllAttrInfoVisitor(
            new AllInstructionVisitor(
            new ClassFileClassForNameReferenceInitializer(programClassPool,
                                                          libraryClassPool,
                                                          classForNameNotePrinter,
                                                          createNoteExceptionMatcher(configuration.keep))))));

        // Initialize the class references from program class members and
        // attributes.
        WarningPrinter referenceWarningPrinter = configuration.warn ?
            new WarningPrinter(System.err) :
            null;

        programClassPool.classFilesAccept(
            new ClassFileReferenceInitializer(programClassPool,
                                              libraryClassPool,
                                              referenceWarningPrinter));

        if (configuration.applyMapping == null &&
            !configuration.useUniqueClassMemberNames)
        {
            // Reconstruct a library class pool with only those library classes
            // whose hierarchies are referenced by the program classes.
            libraryClassPool.clear();
            programClassPool.classFilesAccept(
                new ReferencedClassFileVisitor(
                new LibraryClassFileFilter(
                new ClassFileHierarchyTraveler(true, true, true, false,
                new LibraryClassFileFilter(
                new ClassPoolFiller(libraryClassPool))))));
        }

        // Initialize the class references from library class members.
        libraryClassPool.classFilesAccept(
            new ClassFileReferenceInitializer(programClassPool,
                                              libraryClassPool,
                                              null));

        // Print out a summary of the notes, if necessary.
        if (configuration.note)
        {
            int classForNameNoteCount = classForNameNotePrinter.getWarningCount();
            if (classForNameNoteCount > 0)
            {
                System.err.println("Note: there were " + classForNameNoteCount +
                                   " class casts of dynamically created class instances.");
                System.err.println("      You might consider explicitly keeping the mentioned classes and/or");
                System.err.println("      their implementations (using '-keep').");
            }

            if (configuration.optimize ||
                configuration.obfuscate)
            {
                // Check for Java 6 files.
                ClassFileVersionCounter classFileVersionCounter =
                    new ClassFileVersionCounter(ClassConstants.MAJOR_VERSION_MAX,
                                                ClassConstants.MINOR_VERSION_MAX);

                programClassPool.classFilesAccept(classFileVersionCounter);

                int java6count = classFileVersionCounter.getCount();
                if (java6count > 0)
                {
                    System.err.println("Note: there were " + java6count +
                                       " Java 6 program classes.");
                    System.err.println("      In order to obtain all of the improved start-up performance of Java 6,");
                    System.err.println("      they should be preverified after having been optimized or obfuscated.");
                    System.err.println("      Keep any eye on ProGuard version 4.0 for preverification support,");
                    System.err.println("      at http://proguard.sourceforge.net/");
                }
            }
        }

        // Print out a summary of the warnings, if necessary.
        if (configuration.warn)
        {
            int hierarchyWarningCount = hierarchyWarningPrinter.getWarningCount();
            if (hierarchyWarningCount > 0)
            {
                System.err.println("Warning: there were " + hierarchyWarningCount +
                                   " unresolved references to superclasses or interfaces.");
                System.err.println("         You may need to specify additional library jars (using '-libraryjars'),");
                System.err.println("         or perhaps the '-dontskipnonpubliclibraryclasses' option.");
            }

            int referenceWarningCount = referenceWarningPrinter.getWarningCount();
            if (referenceWarningCount > 0)
            {
                System.err.println("Warning: there were " + referenceWarningCount +
                                   " unresolved references to program class members.");
                System.err.println("         Your input classes appear to be inconsistent.");
                System.err.println("         You may need to recompile them and try again.");
                System.err.println("         Alternatively, you may have to specify the options ");
                System.err.println("         '-dontskipnonpubliclibraryclasses' and/or");
                System.err.println("         '-dontskipnonpubliclibraryclassmembers'.");
            }

            if ((hierarchyWarningCount > 0 ||
                 referenceWarningCount > 0) &&
                !configuration.ignoreWarnings)
            {
                System.err.println("         If you are sure the mentioned classes are not used anyway,");
                System.err.println("         you could try your luck using the '-ignorewarnings' option.");
                throw new IOException("Please correct the above warnings first.");
            }
        }

        // Discard unused library classes.
        if (configuration.verbose)
        {
            System.out.println("Ignoring unused library classes...");
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
}
