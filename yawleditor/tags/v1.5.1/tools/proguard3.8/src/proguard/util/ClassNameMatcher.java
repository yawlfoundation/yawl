/* $Id: ClassNameMatcher.java,v 1.6.2.1 2006/10/18 21:12:47 eric Exp $
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

/**
 * This StringMatcher tests whether internal class names match a
 * given regular expression.
 * Supported wildcards are
 * '?'  for a single Java identifier character,
 * '*'  for any number of regular Java identifier characters, and
 * '**' for any number of regular Java identifier characters or package separator
 *      characters.
 * '%'  for a single internal primitive type character (Z, B, C, S, I, F, J, or D),
 *
 * @author Eric Lafortune
 */
public class ClassNameMatcher extends BasicMatcher
{
    private static final char[] CLASS_NAME_CHARACTERS = new char[]
    {
        ClassConstants.INTERNAL_SPECIAL_CHARACTER
    };

    private static final char[] EXTENDED_CLASS_NAME_CHARACTERS = new char[]
    {
        ClassConstants.INTERNAL_PACKAGE_SEPARATOR
    };

    private static final char[] SPECIAL_PRIMITIVE_CHARACTERS = new char[]
    {
        ClassConstants.INTERNAL_TYPE_BOOLEAN,
        ClassConstants.INTERNAL_TYPE_BYTE,
        ClassConstants.INTERNAL_TYPE_CHAR,
        ClassConstants.INTERNAL_TYPE_SHORT,
        ClassConstants.INTERNAL_TYPE_INT,
        ClassConstants.INTERNAL_TYPE_FLOAT,
        ClassConstants.INTERNAL_TYPE_LONG,
        ClassConstants.INTERNAL_TYPE_DOUBLE
    };


    /**
     * Creates a new ClassNameMatcher.
     * @param regularExpression the regular expression against which strings
     *                          will be matched.
     */
    public ClassNameMatcher(String regularExpression)
    {
        super(regularExpression,
              CLASS_NAME_CHARACTERS,
              EXTENDED_CLASS_NAME_CHARACTERS,
              SPECIAL_PRIMITIVE_CHARACTERS);
    }


    /**
     * A main method for testing class name matching.
     */
    public static void main(String[] args)
    {
        try
        {
            System.out.println("Regular expression ["+args[0]+"]");
            ClassNameMatcher matcher = new ClassNameMatcher(args[0]);
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
