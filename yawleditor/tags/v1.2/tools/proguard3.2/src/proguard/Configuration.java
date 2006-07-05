/* $Id: Configuration.java,v 1.14 2004/11/20 15:41:24 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2004 Eric Lafortune (eric@graphics.cornell.edu)
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

import java.util.*;


/**
 * The ProGuard configuration.
 *
 * @see ProGuard
 *
 * @author Eric Lafortune
 */
public class Configuration
{
    ///////////////////////////////////////////////////////////////////////////
    // Input and output options.
    ///////////////////////////////////////////////////////////////////////////

    /**
     * A list of input and output entries (jars, wars, ears, zips, and directories).
     */
    public ClassPath programJars;

    /**
     * A list of library entries (jars, wars, ears, zips, and directories).
     */
    public ClassPath libraryJars;

    /**
     * Specifies whether to skip non-public library classes while reading
     * library jars.
     */
    public boolean   skipNonPublicLibraryClasses = true;

    /**
     * Specifies whether to skip non-public library class members while reading
     * library classes.
     */
    public boolean   skipNonPublicLibraryClassMembers = true;

    ///////////////////////////////////////////////////////////////////////////
    // Keep options.
    ///////////////////////////////////////////////////////////////////////////

    /**
     * A list of {@link ClassSpecification} instances, whose class names and
     * class member names are to be kept from shrinking, optimization, and
     * obfuscation.
     */
    public List      keep;

    /**
     * A list of {@link ClassSpecification} instances, whose class names and
     * class member names are to be kept from obfuscation.
     */
    public List      keepNames;

    /**
     * An optional output file name for listing the kept seeds.
     * An empty string means the standard output.
     */
    public String    printSeeds;

    ///////////////////////////////////////////////////////////////////////////
    // Shrinking options.
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Specifies whether the code should be shrunk.
     */
    public boolean   shrink                      = true;

    /**
     * An optional output file name for listing the unused classes and class
     * members. An empty string means the standard output.
     */
    public String    printUsage;

    ///////////////////////////////////////////////////////////////////////////
    // Optimization options.
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Specifies whether the code should be optimized.
     */
    public boolean   optimize                    = true;

    /**
     * A list of {@link ClassSpecification} instances, whose methods are
     * assumed to have no side effects.
     */
    public List      assumeNoSideEffects;

    /**
     * Specifies whether the access of class members can be modified.
     */
    public boolean   allowAccessModification     = false;

    ///////////////////////////////////////////////////////////////////////////
    // Obfuscation options.
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Specifies whether the code should be obfuscated.
     */
    public boolean   obfuscate                   = true;

    /**
     * An optional output file name for listing the obfuscation mapping.
     * An empty string means the standard output.
     */
    public String    printMapping;

    /**
     * An optional input file name for reading an obfuscation mapping.
     */
    public String    applyMapping;

    /**
     * An optional name of a file containing obfuscated class member names.
     */
    public String    obfuscationDictionary;

    /**
     * Specifies whether to apply aggressive name overloading on class members.
     */
    public boolean   overloadAggressively        = false;

    /**
     * An optional default package to which all classes whose name is obfuscated
     * can be moved.
     */
    public String    defaultPackage;

    /**
     * Specifies whether to use mixed case class names.
     */
    public boolean   useMixedCaseClassNames      = true;

    /**
     * A list of <code>String</code>s specifying optional attributes to be kept.
     * A <code>null</code> list means no attributes. An empty list means all
     * attributes. The attribute names may contain "*" or "?" wildcards, and
     * they may be preceded by the "!" negator.
     */
    public List      keepAttributes;

    /**
     * An optional replacement for all SourceFile attributes.
     */
    public String    newSourceFileAttribute;

    ///////////////////////////////////////////////////////////////////////////
    // General options.
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Specifies whether to print verbose messages.
     */
    public boolean   verbose                     = false;

    /**
     * Specifies whether to print any notes.
     */
    public boolean   note                        = true;

    /**
     * Specifies whether to print any warnings.
     */
    public boolean   warn                        = true;

    /**
     * Specifies whether to ignore any warnings.
     */
    public boolean   ignoreWarnings              = false;

    /**
     * An optional output file name for printing out the processed code in a
     * more or less readable form. An empty string means the standard output.
     */
    public String    dump;
}
