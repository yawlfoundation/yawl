/* $Id: FileNameMatcher.java,v 1.7 2005/11/05 19:29:25 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002 Eric Lafortune (eric@graphics.cornell.edu)
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
package proguard.util;

import proguard.classfile.ClassConstants;

import java.io.*;

/**
 * This StringMatcher tests whether file names match a given regular
 * expression.
 * Supported wildcards are
 * '?'  for a single regular file name character,
 * '*'  for any number of regular file name characters, and
 * '**' for any number of regular file name characters or directory separator
 *      characters (always including '/').
 *
 * @author Eric Lafortune
 */
public class FileNameMatcher extends BasicMatcher
{
    private static final char[] FILE_NAME_CHARACTERS = new char[]
    {
        ' ',
        '-',
        '.'
    };

    private static final char[] EXTENDED_FILE_NAME_CHARACTERS = new char[]
    {
        ClassConstants.INTERNAL_PACKAGE_SEPARATOR,
        File.separatorChar
    };


    /**
     * Creates a new FileNameMatcher.
     * @param regularExpression the regular expression against which strings
     *                          will be matched.
     */
    public FileNameMatcher(String regularExpression)
    {
        super(regularExpression,
              FILE_NAME_CHARACTERS,
              EXTENDED_FILE_NAME_CHARACTERS,
              null);
    }


    /**
     * A main method for testing file name matching.
     */
    public static void main(String[] args)
    {
        try
        {
            System.out.println("Regular expression ["+args[0]+"]");
            FileNameMatcher matcher = new FileNameMatcher(args[0]);
            for (int index = 1; index < args.length; index++)
            {
                String string = args[index];
                System.out.print("String             ["+string+"]");
                System.out.println(" -> match = "+matcher.matches(args[index]));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
