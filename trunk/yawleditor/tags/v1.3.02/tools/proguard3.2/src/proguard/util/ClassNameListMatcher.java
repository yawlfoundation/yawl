/* $Id: ClassNameListMatcher.java,v 1.5 2004/08/15 12:39:30 eric Exp $
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

import java.util.List;


/**
 * This StringMatcher tests whether internal class names match any
 * entry in a given list of regular expressions.
 *
 * @see BasicListMatcher
 * @see ClassNameMatcher
 *
 * @author Eric Lafortune
 */
public class ClassNameListMatcher extends BasicListMatcher
{
    /**
     * Creates a new ClassNameListMatcher.
     * @param regularExpression the comma-separated list of regular expressions
     *                          against which strings will be matched.
     */
    public ClassNameListMatcher(String regularExpression)
    {
        super(regularExpression);
    }


    /**
     * Creates a new ClassNameListMatcher.
     * @param regularExpressionList the list of regular expressions against which
     *                              strings will be matched.
     */
    public ClassNameListMatcher(List regularExpressionList)
    {
        super(regularExpressionList);
    }


    // Overridden method of BasicListMatcher

    protected StringMatcher createBasicMatcher(String regularExpression)
    {
        return new ClassNameMatcher(regularExpression);
    }


    /**
     * A main method for testing file name matching.
     */
    public static void main(String[] args)
    {
        try
        {
            System.out.println("Regular expression ["+args[0]+"]");
            ClassNameListMatcher matcher = new ClassNameListMatcher(args[0]);
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
