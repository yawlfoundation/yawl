/* $Id: ConfigurationWriter.java,v 1.18.2.1 2006/01/16 22:57:55 eric Exp $
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
package proguard;

import proguard.classfile.*;
import proguard.classfile.util.*;
import proguard.util.*;

import java.io.*;
import java.util.*;


/**
 * This class writes ProGuard configurations to a file.
 *
 * @author Eric Lafortune
 */
public class ConfigurationWriter
{
    private static final String[] KEEP_NAMES_OPTIONS = new String[]
    {
        ConfigurationConstants.KEEP_NAMES_OPTION,
        ConfigurationConstants.KEEP_CLASS_MEMBER_NAMES_OPTION,
        ConfigurationConstants.KEEP_CLASSES_WITH_MEMBER_NAMES_OPTION
    };

    private static final String[] KEEP_OPTIONS = new String[]
    {
        ConfigurationConstants.KEEP_OPTION,
        ConfigurationConstants.KEEP_CLASS_MEMBERS_OPTION,
        ConfigurationConstants.KEEP_CLASSES_WITH_MEMBERS_OPTION
    };

    private static final String[] WHY_ARE_YOU_KEEPING_OPTIONS = new String[]
    {
        ConfigurationConstants.WHY_ARE_YOU_KEEPING_OPTION,
        ConfigurationConstants.WHY_ARE_YOU_KEEPING_OPTION,
        ConfigurationConstants.WHY_ARE_YOU_KEEPING_OPTION
    };

    private static final String[] ASSUME_NO_SIDE_EFFECT_OPTIONS = new String[]
    {
        ConfigurationConstants.ASSUME_NO_SIDE_EFFECTS_OPTION,
        ConfigurationConstants.ASSUME_NO_SIDE_EFFECTS_OPTION,
        ConfigurationConstants.ASSUME_NO_SIDE_EFFECTS_OPTION
    };


    private PrintWriter writer;
    private File        baseDir;


    /**
     * Creates a new ConfigurationWriter for the given file name.
     */
    public ConfigurationWriter(File configurationFile) throws IOException
    {
        this(new PrintWriter(new FileWriter(configurationFile)));

        baseDir = configurationFile.getParentFile();
    }


    /**
     * Creates a new ConfigurationWriter for the given OutputStream.
     */
    public ConfigurationWriter(OutputStream outputStream) throws IOException
    {
        this(new PrintWriter(outputStream));
    }


    /**
     * Creates a new ConfigurationWriter for the given PrintWriter.
     */
    public ConfigurationWriter(PrintWriter writer) throws IOException
    {
        this.writer = writer;
    }


    /**
     * Closes this ConfigurationWriter.
     */
    public void close() throws IOException
    {
        writer.close();
    }


    /**
     * Writes the given configuration.
     * @param configuration the configuration that is to be written out.
     * @throws IOException if an IO error occurs while writing the configuration.
     */
    public void write(Configuration configuration) throws IOException
    {
        // Write the program class path (input and output entries).
        writeJarOptions(ConfigurationConstants.INJARS_OPTION,
                        ConfigurationConstants.OUTJARS_OPTION,
                        configuration.programJars);
        writer.println();

        // Write the library class path (output entries only).
        writeJarOptions(ConfigurationConstants.LIBRARYJARS_OPTION,
                        ConfigurationConstants.LIBRARYJARS_OPTION,
                        configuration.libraryJars);
        writer.println();

        // Write the other options.
        writeOption(ConfigurationConstants.DONT_SKIP_NON_PUBLIC_LIBRARY_CLASSES_OPTION,       !configuration.skipNonPublicLibraryClasses);
        writeOption(ConfigurationConstants.DONT_SKIP_NON_PUBLIC_LIBRARY_CLASS_MEMBERS_OPTION, !configuration.skipNonPublicLibraryClassMembers);

        writeOption(ConfigurationConstants.DONT_SHRINK_OPTION,                                !configuration.shrink);
        writeOption(ConfigurationConstants.PRINT_USAGE_OPTION,                                configuration.printUsage);

        writeOption(ConfigurationConstants.DONT_OPTIMIZE_OPTION ,                             !configuration.optimize);
        writeOption(ConfigurationConstants.ALLOW_ACCESS_MODIFICATION_OPTION,                  configuration.allowAccessModification);

        writeOption(ConfigurationConstants.DONT_OBFUSCATE_OPTION,                             !configuration.obfuscate);
        writeOption(ConfigurationConstants.PRINT_MAPPING_OPTION,                              configuration.printMapping);
        writeOption(ConfigurationConstants.APPLY_MAPPING_OPTION,                              configuration.applyMapping);
        writeOption(ConfigurationConstants.OBFUSCATION_DICTIONARY_OPTION,                     configuration.obfuscationDictionary);
        writeOption(ConfigurationConstants.OVERLOAD_AGGRESSIVELY_OPTION,                      configuration.overloadAggressively);
        writeOption(ConfigurationConstants.DEFAULT_PACKAGE_OPTION,                            configuration.defaultPackage);
        writeOption(ConfigurationConstants.DONT_USE_MIXED_CASE_CLASS_NAMES_OPTION,            !configuration.useMixedCaseClassNames);
        writeOption(ConfigurationConstants.KEEP_ATTRIBUTES_OPTION,                            ListUtil.commaSeparatedString(configuration.keepAttributes));
        writeOption(ConfigurationConstants.RENAME_SOURCE_FILE_ATTRIBUTE_OPTION,               configuration.newSourceFileAttribute);

        writeOption(ConfigurationConstants.VERBOSE_OPTION,                                    configuration.verbose);
        writeOption(ConfigurationConstants.DONT_NOTE_OPTION,                                  !configuration.note);
        writeOption(ConfigurationConstants.DONT_WARN_OPTION,                                  !configuration.warn);
        writeOption(ConfigurationConstants.IGNORE_WARNINGS_OPTION,                            configuration.ignoreWarnings);

        writeOption(ConfigurationConstants.PRINT_SEEDS_OPTION,                                configuration.printSeeds);
        writer.println();

        // Write the "why are you keeping" options.
        writeOptions(WHY_ARE_YOU_KEEPING_OPTIONS, configuration.whyAreYouKeeping);

        // Write the keep options.
        writeOptions(KEEP_OPTIONS, configuration.keep);

        // Write the keep names options.
        writeOptions(KEEP_NAMES_OPTIONS, configuration.keepNames);

        // Write the "no side effect methods" options.
        writeOptions(ASSUME_NO_SIDE_EFFECT_OPTIONS, configuration.assumeNoSideEffects);
}


    private void writeJarOptions(String    inputEntryOptionName,
                                 String    outputEntryOptionName,
                                 ClassPath classPath)
    {
        if (classPath != null)
        {
            for (int index = 0; index < classPath.size(); index++)
            {
                ClassPathEntry entry = classPath.get(index);
                String optionName = entry.isOutput() ?
                     outputEntryOptionName :
                     inputEntryOptionName;

                writer.print(optionName + " " + relativeFileName(entry.getFile()));

                // Append the filters, if any.
                boolean filtered = false;

                filtered = writeFilter(filtered, entry.getZipFilter());
                filtered = writeFilter(filtered, entry.getEarFilter());
                filtered = writeFilter(filtered, entry.getWarFilter());
                filtered = writeFilter(filtered, entry.getJarFilter());
                filtered = writeFilter(filtered, entry.getFilter());

                if (filtered)
                {
                    writer.print(ConfigurationConstants.CLOSE_ARGUMENTS_KEYWORD);
                }

                writer.println();
            }
        }
    }


    private boolean writeFilter(boolean filtered, String filter)
    {
        if (filtered)
        {
            writer.print(ConfigurationConstants.SEPARATOR_KEYWORD);
        }

        if (filter != null)
        {
            if (!filtered)
            {
                writer.print(ConfigurationConstants.OPEN_ARGUMENTS_KEYWORD);
            }

            writer.print(quotedString(filter));

            filtered = true;
        }

        return filtered;
    }


    private void writeOption(String optionName, boolean flag)
    {
        if (flag)
        {
            writer.println(optionName);
        }
    }


    private void writeOption(String optionName, String arguments)
    {
        if (arguments != null)
        {
            writer.println(optionName + " " + quotedString(arguments));
        }
    }


    private void writeOption(String optionName, File file)
    {
        if (file != null)
        {
            if (file.getPath().length() > 0)
            {
                writer.println(optionName + " " + relativeFileName(file));
            }
            else
            {
                writer.println(optionName);
            }
        }
    }


    private void writeOptions(String[] optionNames,
                              List     classSpecifications)
    {
        if (classSpecifications != null)
        {
            for (int index = 0; index < classSpecifications.size(); index++)
            {
                writeOption(optionNames, (ClassSpecification)classSpecifications.get(index));
            }
        }
    }


    private void writeOption(String[]           optionNames,
                             ClassSpecification classSpecification)
    {
        writer.println();

        // Write out the comments for this option.
        writeComments(classSpecification.comments);

        // Write out the proper class specification option name.
        writer.print(optionNames[classSpecification.markConditionally ? 2 :
                                 classSpecification.markClassFiles    ? 0 :
                                                                        1]);

        writer.print(" ");

        // Write out the class access flags.
        writer.print(ClassUtil.externalClassAccessFlags(classSpecification.requiredUnsetAccessFlags,
                                                        ConfigurationConstants.NEGATOR_KEYWORD));

        writer.print(ClassUtil.externalClassAccessFlags(classSpecification.requiredSetAccessFlags));

        // Write out the class keyword, if we didn't write the interface
        // keyword earlier.
        if (((classSpecification.requiredSetAccessFlags |
              classSpecification.requiredUnsetAccessFlags) &
             ClassConstants.INTERNAL_ACC_INTERFACE) == 0)
        {
            writer.print("class");
        }

        writer.print(" ");

        // Write out the class name.
        writer.print(classSpecification.className != null ?
            ClassUtil.externalClassName(classSpecification.className) :
            ConfigurationConstants.ANY_CLASS_KEYWORD);

        // Write out the extends template, if any.
        if (classSpecification.extendsClassName != null)
        {
            writer.print(" extends " + ClassUtil.externalClassName(classSpecification.extendsClassName));
        }

        // Write out the keep field and keep method options, if any.
        if (classSpecification.fieldSpecifications  != null ||
            classSpecification.methodSpecifications != null)
        {
            writer.println(" {");

            writeFieldSpecification( classSpecification.fieldSpecifications);
            writeMethodSpecification(classSpecification.methodSpecifications);
            writer.println("}");
        }
        else
        {
            writer.println();
        }
    }



    private void writeComments(String comments)
    {
        if (comments != null)
        {
            int index = 0;
            while (index < comments.length())
            {
                int breakIndex = comments.indexOf('\n', index);
                if (breakIndex < 0)
                {
                    breakIndex = comments.length();
                }

                writer.print('#');
                writer.println(comments.substring(index, breakIndex));

                index = breakIndex + 1;
            }
        }
    }


    private void writeFieldSpecification(List classMemberSpecifications)
    {
        if (classMemberSpecifications != null)
        {
            for (int index = 0; index < classMemberSpecifications.size(); index++)
            {
                ClassMemberSpecification classMemberSpecification =
                    (ClassMemberSpecification)classMemberSpecifications.get(index);

                writer.print("    ");

                // Write out the field access flags.
                writer.print(ClassUtil.externalFieldAccessFlags(classMemberSpecification.requiredUnsetAccessFlags,
                                                                ConfigurationConstants.NEGATOR_KEYWORD));

                writer.print(ClassUtil.externalFieldAccessFlags(classMemberSpecification.requiredSetAccessFlags));

                // Write out the field name and descriptor.
                String name       = classMemberSpecification.name;
                String descriptor = classMemberSpecification.descriptor;
                
                if (name == null)
                {
                    name = ConfigurationConstants.ANY_CLASS_MEMBER_KEYWORD;
                }
    
                writer.print(descriptor != null ?
                    ClassUtil.externalFullFieldDescription(0,
                                                           name,
                                                           descriptor) :
                    ConfigurationConstants.ANY_FIELD_KEYWORD);

                writer.println(";");
            }
        }
    }


    private void writeMethodSpecification(List classMemberSpecifications)
    {
        if (classMemberSpecifications != null)
        {
            for (int index = 0; index < classMemberSpecifications.size(); index++)
            {
                ClassMemberSpecification classMemberSpecification =
                    (ClassMemberSpecification)classMemberSpecifications.get(index);

                writer.print("    ");

                // Write out the method access flags.
                writer.print(ClassUtil.externalMethodAccessFlags(classMemberSpecification.requiredUnsetAccessFlags,
                                                                 ConfigurationConstants.NEGATOR_KEYWORD));

                writer.print(ClassUtil.externalMethodAccessFlags(classMemberSpecification.requiredSetAccessFlags));

                // Write out the method name and descriptor.
                String name       = classMemberSpecification.name;
                String descriptor = classMemberSpecification.descriptor;
                
                if (name == null)
                {
                    name = ConfigurationConstants.ANY_CLASS_MEMBER_KEYWORD;
                }
    
                writer.print(descriptor != null ?
                    ClassUtil.externalFullMethodDescription(ClassConstants.INTERNAL_METHOD_NAME_INIT,
                                                            0,
                                                            name,
                                                            descriptor) :
                    ConfigurationConstants.ANY_METHOD_KEYWORD);

                writer.println(";");
            }
        }
    }


    /**
     * Returns a relative file name of the given file, if possible.
     * The file name is also quoted, if necessary.
     */
    private String relativeFileName(File file)
    {
        String fileName = file.getAbsolutePath();

        // See if we can convert the file name into a relative file name.
        if (baseDir != null)
        {
            String baseDirName = baseDir.getAbsolutePath() + File.separator;
            if (fileName.startsWith(baseDirName))
            {
                fileName = fileName.substring(baseDirName.length());
            }
        }

        return quotedString(fileName);
    }


    /**
     * Returns a quoted version of the given string, if necessary.
     */
    private String quotedString(String string)
    {
        return string.length()     == 0 ||
               string.indexOf(' ') >= 0 ||
               string.indexOf('@') >= 0 ||
               string.indexOf('{') >= 0 ||
               string.indexOf('}') >= 0 ||
               string.indexOf('(') >= 0 ||
               string.indexOf(')') >= 0 ||
               string.indexOf(':') >= 0 ||
               string.indexOf(';') >= 0  ? ("'" + string + "'") :
                                           (      string      );
    }


    /**
     * A main method for testing configuration writing.
     */
    public static void main(String[] args) {
        try
        {
            ConfigurationWriter writer = new ConfigurationWriter(new File(args[0]));

            writer.write(new Configuration());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
