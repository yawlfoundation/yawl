/* $Id: Obfuscator.java,v 1.2.2.6 2007/01/18 21:31:52 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2007 Eric Lafortune (eric@graphics.cornell.edu)
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
package proguard.obfuscate;

import proguard.*;
import proguard.classfile.*;
import proguard.classfile.editor.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;

import java.io.*;
import java.util.*;

/**
 * This class can perform obfuscation of class pools according to a given
 * specification.
 *
 * @author Eric Lafortune
 */
public class Obfuscator
{
    private Configuration configuration;


    /**
     * Creates a new Obfuscator.
     */
    public Obfuscator(Configuration configuration)
    {
        this.configuration = configuration;
    }


    /**
     * Performs obfuscation of the given program class pool.
     */
    public void execute(ClassPool programClassPool,
                        ClassPool libraryClassPool) throws IOException
    {
        // Check if we have at least some keep commands.
        if (configuration.keep         == null &&
            configuration.keepNames    == null &&
            configuration.applyMapping == null &&
            configuration.printMapping == null)
        {
            throw new IOException("You have to specify '-keep' options for the obfuscation step.");
        }

        // Clean up any old visitor info.
        programClassPool.classFilesAccept(new ClassFileCleaner());
        libraryClassPool.classFilesAccept(new ClassFileCleaner());

        // If the class member names have to correspond globally,
        // link all class members in all classes, otherwise
        // link all non-private methods in all class hierarchies.
        ClassFileVisitor memberInfoLinker =
            configuration.useUniqueClassMemberNames ?
                (ClassFileVisitor)new AllMemberInfoVisitor(new MethodInfoLinker()) :
                (ClassFileVisitor)new BottomClassFileFilter(new MethodInfoLinker());

        programClassPool.classFilesAccept(memberInfoLinker);
        libraryClassPool.classFilesAccept(memberInfoLinker);

        // Create a visitor for marking the seeds.
        NameMarker nameMarker = new NameMarker();
        ClassPoolVisitor classPoolvisitor =
            new MultiClassPoolVisitor(new ClassPoolVisitor[]
            {
                ClassSpecificationVisitorFactory.createClassPoolVisitor(configuration.keep,
                                                                        nameMarker,
                                                                        nameMarker),
                ClassSpecificationVisitorFactory.createClassPoolVisitor(configuration.keepNames,
                                                                        nameMarker,
                                                                        nameMarker)
            });

        // Mark the seeds.
        programClassPool.accept(classPoolvisitor);
        libraryClassPool.accept(classPoolvisitor);

        // All library classes and library class members keep their names.
        libraryClassPool.classFilesAccept(nameMarker);
        libraryClassPool.classFilesAccept(new AllMemberInfoVisitor(nameMarker));

        // Apply the mapping, if one has been specified. The mapping can
        // override the names of library classes and of library class members.
        if (configuration.applyMapping != null)
        {
            WarningPrinter warningPrinter = configuration.warn ?
                new WarningPrinter(System.err) :
                null;

            MappingReader reader = new MappingReader(configuration.applyMapping);

            MappingProcessor keeper =
                new MultiMappingProcessor(new MappingProcessor[]
                {
                    new MappingKeeper(programClassPool, warningPrinter),
                    new MappingKeeper(libraryClassPool, null),
                });

            reader.pump(keeper);

            if (configuration.warn)
            {
                // Print out a summary of the warnings if necessary.
                int mappingWarningCount = warningPrinter.getWarningCount();
                if (mappingWarningCount > 0)
                {
                    System.err.println("Warning: there were " + mappingWarningCount +
                                       " kept classes and class members that were remapped anyway.");
                    System.err.println("         You should adapt your configuration or edit the mapping file.");

                    if (!configuration.ignoreWarnings)
                    {
                        System.err.println("         If you are sure this remapping won't hurt,");
                        System.err.println("         you could try your luck using the '-ignorewarnings' option.");
                        throw new IOException("Please correct the above warnings first.");
                    }
                }
            }
        }

        // Mark attributes that have to be kept.
        AttributeUsageMarker attributeUsageMarker = new AttributeUsageMarker();
        if (configuration.keepAttributes != null)
        {
            if (!configuration.keepAttributes.isEmpty())
            {
                attributeUsageMarker.setKeepAttributes(configuration.keepAttributes);
            }
            else
            {
                attributeUsageMarker.setKeepAllAttributes();
            }
        }
        programClassPool.classFilesAccept(attributeUsageMarker);

        // Remove the attributes that can be discarded.
        programClassPool.classFilesAccept(new AttributeShrinker());

        // Come up with new names for all classes.
        programClassPool.classFilesAccept(
            new ClassFileObfuscator(programClassPool,
                                    configuration.defaultPackage,
                                    configuration.useMixedCaseClassNames));

        // Come up with new names for all class members.
        NameFactory nameFactory = new SimpleNameFactory();

        if (configuration.obfuscationDictionary != null)
        {
            nameFactory = new DictionaryNameFactory(configuration.obfuscationDictionary,
                                                    nameFactory);
        }

        WarningPrinter warningPrinter = configuration.warn ?
            new WarningPrinter(System.err) :
            null;

        // Maintain a map of names to avoid [descriptor - new name - old name].
        Map descriptorMap = new HashMap();

        // Do the class member names have to be globally unique?
        if (configuration.useUniqueClassMemberNames)
        {
            // Collect all member names in all classes.
            programClassPool.classFilesAccept(
                new AllMemberInfoVisitor(
                new MemberInfoNameCollector(configuration.overloadAggressively,
                                            descriptorMap)));

            // Assign new names to all members in all classes.
            programClassPool.classFilesAccept(
                new AllMemberInfoVisitor(
                new MemberInfoObfuscator(configuration.overloadAggressively,
                                         nameFactory,
                                         descriptorMap)));
        }
        else
        {
            // Come up with new names for all non-private class members.
            programClassPool.classFilesAccept(
                new MultiClassFileVisitor(new ClassFileVisitor[]
                {
                    // Collect all private member names in this class and down
                    // the hierarchy.
                    new ClassFileHierarchyTraveler(true, false, false, true,
                    new AllMemberInfoVisitor(
                    new MemberInfoAccessFilter(ClassConstants.INTERNAL_ACC_PRIVATE, 0,
                    new MemberInfoNameCollector(configuration.overloadAggressively,
                                                descriptorMap)))),

                    // Collect all non-private member names anywhere in the hierarchy.
                    new ClassFileHierarchyTraveler(true, true, true, true,
                    new AllMemberInfoVisitor(
                    new MemberInfoAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                    new MemberInfoNameCollector(configuration.overloadAggressively,
                                                descriptorMap)))),

                    // Assign new names to all non-private members in this class.
                    new AllMemberInfoVisitor(
                    new MemberInfoAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                    new MemberInfoObfuscator(configuration.overloadAggressively,
                                             nameFactory,
                                             descriptorMap))),

                    // Clear the collected names.
                    new MapCleaner(descriptorMap)
                }));

            // Come up with new names for all private class members.
            programClassPool.classFilesAccept(
                new MultiClassFileVisitor(new ClassFileVisitor[]
                {
                    // Collect all member names in this class.
                    new AllMemberInfoVisitor(
                    new MemberInfoNameCollector(configuration.overloadAggressively,
                                                descriptorMap)),

                    // Collect all non-private member names higher up the hierarchy.
                    new ClassFileHierarchyTraveler(false, true, true, false,
                    new AllMemberInfoVisitor(
                    new MemberInfoAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                    new MemberInfoNameCollector(configuration.overloadAggressively,
                                                descriptorMap)))),

                    // Assign new names to all private members in this class.
                    new AllMemberInfoVisitor(
                    new MemberInfoAccessFilter(ClassConstants.INTERNAL_ACC_PRIVATE, 0,
                    new MemberInfoObfuscator(configuration.overloadAggressively,
                                             nameFactory,
                                             descriptorMap))),

                    // Clear the collected names.
                    new MapCleaner(descriptorMap)
                }));
        }

        // Some class members may have ended up with conflicting names.
        // Come up with new, globally unique names for them.
        NameFactory specialNameFactory =
            new SpecialNameFactory(new SimpleNameFactory());

        // Collect a map of special names to avoid
        // [descriptor - new name - old name].
        Map specialDescriptorMap = new HashMap();

        programClassPool.classFilesAccept(
            new AllMemberInfoVisitor(
            new MemberInfoSpecialNameFilter(
            new MemberInfoNameCollector(configuration.overloadAggressively,
                                        specialDescriptorMap))));

        libraryClassPool.classFilesAccept(
            new AllMemberInfoVisitor(
            new MemberInfoSpecialNameFilter(
            new MemberInfoNameCollector(configuration.overloadAggressively,
                                        specialDescriptorMap))));

        // Replace conflicting non-private member names with special names.
        programClassPool.classFilesAccept(
            new MultiClassFileVisitor(new ClassFileVisitor[]
            {
                // Collect all private member names in this class and down
                // the hierarchy.
                new ClassFileHierarchyTraveler(true, false, false, true,
                new AllMemberInfoVisitor(
                new MemberInfoAccessFilter(ClassConstants.INTERNAL_ACC_PRIVATE, 0,
                new MemberInfoNameCollector(configuration.overloadAggressively,
                                            descriptorMap)))),

                // Collect all non-private member names anywhere in the hierarchy.
                new ClassFileHierarchyTraveler(true, true, true, true,
                new AllMemberInfoVisitor(
                new MemberInfoAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                new MemberInfoNameCollector(configuration.overloadAggressively,
                                            descriptorMap)))),

                // Assign new names to all conflicting non-private members in
                // this class.
                new AllMemberInfoVisitor(
                new MemberInfoAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                new MemberInfoNameConflictFixer(configuration.overloadAggressively,
                                                descriptorMap,
                                                warningPrinter,
                new MemberInfoObfuscator(configuration.overloadAggressively,
                                         specialNameFactory,
                                         specialDescriptorMap)))),

                // Clear the collected names.
                new MapCleaner(descriptorMap)
            }));

        // Replace conflicting private member names with special names.
        // This is only possible if those names were kept or mapped.
        programClassPool.classFilesAccept(
            new MultiClassFileVisitor(new ClassFileVisitor[]
            {
                // Collect all member names in this class.
                new AllMemberInfoVisitor(
                new MemberInfoNameCollector(configuration.overloadAggressively,
                                            descriptorMap)),

                // Collect all non-private member names higher up the hierarchy.
                new ClassFileHierarchyTraveler(false, true, true, false,
                new AllMemberInfoVisitor(
                new MemberInfoAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                new MemberInfoNameCollector(configuration.overloadAggressively,
                                            descriptorMap)))),

                // Assign new names to all conflicting private members in this
                // class.
                new AllMemberInfoVisitor(
                new MemberInfoAccessFilter(ClassConstants.INTERNAL_ACC_PRIVATE, 0,
                new MemberInfoNameConflictFixer(configuration.overloadAggressively,
                                                descriptorMap,
                                                warningPrinter,
                new MemberInfoObfuscator(configuration.overloadAggressively,
                                         specialNameFactory,
                                         specialDescriptorMap)))),

                // Clear the collected names.
                new MapCleaner(descriptorMap)
            }));

        // Print out any warnings about member name conflicts.
        if (configuration.warn)
        {
            int warningCount = warningPrinter.getWarningCount();
            if (warningCount > 0)
            {
                System.err.println("Warning: there were " + warningCount +
                                   " conflicting class member name mappings.");
                System.err.println("         Your configuration may be inconsistent.");

                if (!configuration.ignoreWarnings)
                {
                    System.err.println("         If you are sure the conflicts are harmless,");
                    System.err.println("         you could try your luck using the '-ignorewarnings' option.");
                    throw new IOException("Please correct the above warnings first.");
                }
            }
        }

        // Print out the mapping, if requested.
        if (configuration.printMapping != null)
        {
            PrintStream ps = isFile(configuration.printMapping) ?
                new PrintStream(new BufferedOutputStream(new FileOutputStream(configuration.printMapping))) :
                System.out;

            // Print out items that will be removed.
            programClassPool.classFilesAcceptAlphabetically(new MappingPrinter(ps));

            if (ps != System.out)
            {
                ps.close();
            }
        }

        // Actually apply the new names.
        programClassPool.classFilesAccept(new ClassFileRenamer());
        libraryClassPool.classFilesAccept(new ClassFileRenamer());

        // Update all references to these new names.
        programClassPool.classFilesAccept(new ClassFileReferenceFixer(false));
        libraryClassPool.classFilesAccept(new ClassFileReferenceFixer(false));
        programClassPool.classFilesAccept(new MemberReferenceFixer(1024));

        // Make package visible elements public, if necessary.
        if (configuration.defaultPackage != null)
        {
            programClassPool.classFilesAccept(new ClassFileOpener());
        }

        // Rename the source file attributes, if requested.
        if (configuration.newSourceFileAttribute != null)
        {
            programClassPool.classFilesAccept(new SourceFileRenamer(configuration.newSourceFileAttribute));
        }

        // Mark NameAndType constant pool entries that have to be kept
        // and remove the other ones.
        programClassPool.classFilesAccept(new NameAndTypeUsageMarker());
        programClassPool.classFilesAccept(new NameAndTypeShrinker(1024));

        // Mark Utf8 constant pool entries that have to be kept
        // and remove the other ones.
        programClassPool.classFilesAccept(new Utf8UsageMarker());
        programClassPool.classFilesAccept(new Utf8Shrinker(1024));
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
