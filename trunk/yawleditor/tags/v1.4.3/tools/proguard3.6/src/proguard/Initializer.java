/* $Id: Initializer.java,v 1.2.2.1 2006/03/26 14:30:14 eric Exp $
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
        ClassFileHierarchyInitializer classFileHierarchyInitializer =
            new ClassFileHierarchyInitializer(programClassPool,
                                              libraryClassPool,
                                              configuration.warn);

        programClassPool.classFilesAccept(classFileHierarchyInitializer);

        // Initialize the class hierarchy for library classes.
        ClassFileHierarchyInitializer classFileHierarchyInitializer2 =
            new ClassFileHierarchyInitializer(programClassPool,
                                              libraryClassPool,
                                              false);

        libraryClassPool.classFilesAccept(classFileHierarchyInitializer2);

        // Initialize the Class.forName and .class references.
        ClassFileClassForNameReferenceInitializer classClassForNameReferenceInitializer =
            new ClassFileClassForNameReferenceInitializer(programClassPool,
                                                          libraryClassPool,
                                                          configuration.note,
                                                          createNoteExceptionMatcher(configuration.keep));

        programClassPool.classFilesAccept(
            new AllMethodVisitor(
            new AllAttrInfoVisitor(
            new AllInstructionVisitor(classClassForNameReferenceInitializer))));

        // Initialize the class references from program class members and attributes.
        ClassFileReferenceInitializer classFileReferenceInitializer =
            new ClassFileReferenceInitializer(programClassPool,
                                              libraryClassPool,
                                              configuration.warn);

        programClassPool.classFilesAccept(classFileReferenceInitializer);

        // Reconstruct a library class pool with only those library classes
        // whose hierarchies are referenced by the program classes.
        libraryClassPool.clear();
        programClassPool.classFilesAccept(
            new ReferencedClassFileVisitor(
            new LibraryClassFileFilter(
            new ClassFileHierarchyTraveler(true, true, true, false,
            new LibraryClassFileFilter(
            new ClassPoolFiller(libraryClassPool, false))))));

        // Initialize the class references from library class members.
        ClassFileReferenceInitializer classReferenceInitializer2 =
            new ClassFileReferenceInitializer(programClassPool,
                                          libraryClassPool,
                                          false);

        libraryClassPool.classFilesAccept(classReferenceInitializer2);

        int classForNameNoteCount = classClassForNameReferenceInitializer.getNoteCount();
        if (classForNameNoteCount > 0)
        {
            System.err.println("Note: there were " + classForNameNoteCount +
                               " class casts of dynamically created class instances.");
            System.err.println("      You might consider explicitly keeping the mentioned classes and/or");
            System.err.println("      their implementations (using '-keep').");
        }

        int hierarchyWarningCount = classFileHierarchyInitializer.getWarningCount();
        if (hierarchyWarningCount > 0)
        {
        // Check if we have at least some input classes.
        if (programClassPool.size() == 0)
        {
            throw new IOException("The input doesn't contain any classes. Did you specify the proper '-injars' options?");
        }

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
            System.err.println("         Your input classes appear to be inconsistent.");
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
}
