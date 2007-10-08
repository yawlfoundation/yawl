/* $Id: ConfigurationConstants.java,v 1.12.2.1 2006/06/07 22:36:52 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2003 Eric Lafortune (eric@graphics.cornell.edu)
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

/**
 * This class provides constants for parsing and writing ProGuard configurations.
 *
 * @author Eric Lafortune
 */
class ConfigurationConstants
{
    public static final String OPTION_PREFIX                                     = "-";
    public static final String AT_DIRECTIVE                                      = "@";
    public static final String INCLUDE_DIRECTIVE                                 = "-include";
    public static final String BASE_DIRECTORY_DIRECTIVE                          = "-basedirectory";

    public static final String INJARS_OPTION                                     = "-injars";
    public static final String OUTJARS_OPTION                                    = "-outjars";
    public static final String LIBRARYJARS_OPTION                                = "-libraryjars";
    public static final String RESOURCEJARS_OPTION                               = "-resourcejars";

    public static final String KEEP_OPTION                                       = "-keep";
    public static final String KEEP_CLASS_MEMBERS_OPTION                         = "-keepclassmembers";
    public static final String KEEP_CLASSES_WITH_MEMBERS_OPTION                  = "-keepclasseswithmembers";
    public static final String KEEP_NAMES_OPTION                                 = "-keepnames";
    public static final String KEEP_CLASS_MEMBER_NAMES_OPTION                    = "-keepclassmembernames";
    public static final String KEEP_CLASSES_WITH_MEMBER_NAMES_OPTION             = "-keepclasseswithmembernames";
    public static final String PRINT_SEEDS_OPTION                                = "-printseeds";

    public static final String DONT_SHRINK_OPTION                                = "-dontshrink";
    public static final String PRINT_USAGE_OPTION                                = "-printusage";
    public static final String WHY_ARE_YOU_KEEPING_OPTION                        = "-whyareyoukeeping";

    public static final String DONT_OPTIMIZE_OPTION                              = "-dontoptimize";
    public static final String ASSUME_NO_SIDE_EFFECTS_OPTION                     = "-assumenosideeffects";
    public static final String ALLOW_ACCESS_MODIFICATION_OPTION                  = "-allowaccessmodification";

    public static final String DONT_OBFUSCATE_OPTION                             = "-dontobfuscate";
    public static final String PRINT_MAPPING_OPTION                              = "-printmapping";
    public static final String APPLY_MAPPING_OPTION                              = "-applymapping";
    public static final String OBFUSCATION_DICTIONARY_OPTION                     = "-obfuscationdictionary";
    public static final String OVERLOAD_AGGRESSIVELY_OPTION                      = "-overloadaggressively";
    public static final String USE_UNIQUE_CLASS_MEMBER_NAMES_OPTION              = "-useuniqueclassmembernames";
    public static final String DEFAULT_PACKAGE_OPTION                            = "-defaultpackage";
    public static final String DONT_USE_MIXED_CASE_CLASS_NAMES_OPTION            = "-dontusemixedcaseclassnames";
    public static final String KEEP_ATTRIBUTES_OPTION                            = "-keepattributes";
    public static final String RENAME_SOURCE_FILE_ATTRIBUTE_OPTION               = "-renamesourcefileattribute";

    public static final String VERBOSE_OPTION                                    = "-verbose";
    public static final String DONT_NOTE_OPTION                                  = "-dontnote";
    public static final String DONT_WARN_OPTION                                  = "-dontwarn";
    public static final String IGNORE_WARNINGS_OPTION                            = "-ignorewarnings";
    public static final String DUMP_OPTION                                       = "-dump";
    public static final String DONT_SKIP_NON_PUBLIC_LIBRARY_CLASSES_OPTION       = "-dontskipnonpubliclibraryclasses";
    public static final String DONT_SKIP_NON_PUBLIC_LIBRARY_CLASS_MEMBERS_OPTION = "-dontskipnonpubliclibraryclassmembers";

    public static final String ANY_ATTRIBUTE_KEYWORD       = "*";
    public static final String ATTRIBUTE_SEPARATOR_KEYWORD = ",";

    public static final String JAR_SEPARATOR_KEYWORD   = System.getProperty("path.separator");

    public static final char   OPEN_SYSTEM_PROPERTY    = '<';
    public static final char   CLOSE_SYSTEM_PROPERTY   = '>';

    public static final String NEGATOR_KEYWORD         = "!";
    public static final String CLASS_KEYWORD           = "class";
    public static final String ANY_CLASS_KEYWORD       = "*";
    public static final String IMPLEMENTS_KEYWORD      = "implements";
    public static final String EXTENDS_KEYWORD         = "extends";
    public static final String OPEN_KEYWORD            = "{";
    public static final String ANY_CLASS_MEMBER_KEYWORD  = "*";
    public static final String ANY_FIELD_KEYWORD       = "<fields>";
    public static final String ANY_METHOD_KEYWORD      = "<methods>";
    public static final String OPEN_ARGUMENTS_KEYWORD  = "(";
    public static final String ARGUMENT_SEPARATOR_KEYWORD = ",";
    public static final String CLOSE_ARGUMENTS_KEYWORD = ")";
    public static final String SEPARATOR_KEYWORD       = ";";
    public static final String CLOSE_KEYWORD           = "}";
}
