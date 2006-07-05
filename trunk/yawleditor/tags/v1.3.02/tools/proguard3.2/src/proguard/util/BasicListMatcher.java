/* $Id: BasicListMatcher.java,v 1.7 2004/08/15 12:39:30 eric Exp $
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

import java.util.*;


/**
 * This StringMatcher tests whether strings match an entry in a given list of
 * regular expressions. The list is given as a comma-separated string or as a
 * List of strings. An exclamation mark preceding a list entry acts as a
 * negator: if the expression matches, a negative match is returned, without
 * considering any subsequent entries. If none of the entries match, a positive
 * match is returned depending on whether the last regular expression had a
 * negator or not.
 * <p>
 * The individual regular expression matching is delegated to a StringMatcher
 * that is created by the {@link #createBasicMatcher(String}} method. If it is
 * not overridden, this method returns a BasicMatcher.
 *
 * @see BasicMatcher
 * @author Eric Lafortune
 */
public class BasicListMatcher implements StringMatcher
{
    private static final char REGULAR_EXPRESSION_SEPARATOR = ',';
    private static final char REGULAR_EXPRESSION_NEGATOR   = '!';

    private StringMatcher[] regularExpressionMatchers;
    private boolean[]       negatedRegularExpressions;


    /**
     * Creates a new BasicListMatcher.
     * @param regularExpression the comma-separated list of regular expressions
     *                          against which strings will be matched.
     */
    public BasicListMatcher(String regularExpression)
    {
        this(ListUtil.commaSeparatedList(regularExpression));
    }


    /**
     * Creates a new BasicListMatcher.
     * @param regularExpressionList the list of regular expressions against which
     *                              strings will be matched.
     */
    public BasicListMatcher(List regularExpressionList)
    {
        // Collect the regular expressions in arrays.
        int regularExpressionCount = regularExpressionList.size();

        regularExpressionMatchers = new StringMatcher[regularExpressionCount];
        negatedRegularExpressions = new boolean[regularExpressionCount];

        for (int index = 0; index < regularExpressionCount; index++)
        {
            String regularExpression = (String)regularExpressionList.get(index);

            // Does the regular expression start with an exclamation mark?
            if (regularExpression.length() > 0 &&
                regularExpression.charAt(0) == REGULAR_EXPRESSION_NEGATOR)
            {
                // Trim the regular expression.
                regularExpression = regularExpression.substring(1);

                // Remember the negator.
                negatedRegularExpressions[index] = true;
            }

            regularExpressionMatchers[index] =
                createBasicMatcher(regularExpression);
        }
    }


    /**
     * Creates a new StringMatcher for the given regular expression.
     */
    protected StringMatcher createBasicMatcher(String regularExpression)
    {
        return new BasicMatcher(regularExpression);
    }


    // Implementations for StringMatcher.

    public boolean matches(String string)
    {
        boolean result = true;

        for (int index = 0; index < regularExpressionMatchers.length; index++)
        {
            result = negatedRegularExpressions[index];

            if (regularExpressionMatchers[index].matches(string))
            {
                return !result;
            }
        }

        return result;
    }


    /**
     * A main method for testing string matching.
     */
    public static void main(String[] args)
    {
        try
        {
            System.out.println("Regular expression ["+args[0]+"]");
            BasicListMatcher matcher =
                new BasicListMatcher(args[0]);

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
