/* $Id: BasicMatcher.java,v 1.8 2005/11/05 19:29:25 eric Exp $
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
 * This StringMatcher tests whether strings match a given regular
 * expression. Supported wildcards are
 * <ul>
 * <li>'?'  for a single Java identifier character or other wildcard
 *          matching character,
 * <li>'*'  for any number of Java identifier characters or other wildcard
 *          matching characters, and
 * <li>'**' for any number of Java identifier characters or extended wildcard
 *          matching characters,
 * <li>'%'  for a single special wildcard matching character.
 * </ul>
 * The sets of wildcard characters, extended wildcard characters, and special
 * wildcard characters can be defined by the user.
 *
 * @author Eric Lafortune
 */
public class BasicMatcher implements StringMatcher
{
    private static final String SINGLE_CHARACTER_WILDCARD     = "?";
    private static final String MULTIPLE_CHARACTERS_WILDCARD1 = "*";
    private static final String MULTIPLE_CHARACTERS_WILDCARD2 = "**";
    private static final String SPECIAL_CHARACTER_WILDCARD    = "%";

    private String[] expressionParts;
    private char[]   wildcardCharacters;
    private char[]   extendedWildcardCharacters;
    private char[]   specialWildcardCharacters;


    /**
     * Creates a new BasicMatcher without extra wildcard matching
     * characters.
     * @param regularExpression the regular expression against which strings
     *                          will be matched.
     */
    public BasicMatcher(String regularExpression)
    {
        this(regularExpression, null, null, null);
    }


    /**
     * Creates a new BasicMatcher.
     * @param regularExpression          the regular expression against which
     *                                   strings will be matched.
     * @param wildcardCharacters         an optional extra list of wildcard
     *                                   matching characters.
     * @param extendedWildcardCharacters an optional extra list of extended
     *                                   wildcard matching characters.
     */
    public BasicMatcher(String regularExpression,
                        char[] wildcardCharacters,
                        char[] extendedWildcardCharacters,
                        char[] specialWildcardCharacters)
    {
        this.wildcardCharacters         = wildcardCharacters;
        this.extendedWildcardCharacters = extendedWildcardCharacters;
        this.specialWildcardCharacters  = specialWildcardCharacters;

        // Split the given regular expression into an array of parts: "?",
        // "*", "**", "%", and simple text strings.

        // A List to collect the subsequent regular expression parts.
        List expressionPartsList = new ArrayList();

        String wildcard      = null;
        int    previousIndex = 0;
        int    index         = 0;
        int    regularExpressionLength = regularExpression.length();
        while (index < regularExpressionLength)
        {
            wildcard =
                regularExpression.regionMatches(index, MULTIPLE_CHARACTERS_WILDCARD2, 0, MULTIPLE_CHARACTERS_WILDCARD2.length()) ? MULTIPLE_CHARACTERS_WILDCARD2 :
                regularExpression.regionMatches(index, MULTIPLE_CHARACTERS_WILDCARD1, 0, MULTIPLE_CHARACTERS_WILDCARD1.length()) ? MULTIPLE_CHARACTERS_WILDCARD1 :
                regularExpression.regionMatches(index, SINGLE_CHARACTER_WILDCARD,     0, SINGLE_CHARACTER_WILDCARD.length()) ?     SINGLE_CHARACTER_WILDCARD     :
                regularExpression.regionMatches(index, SPECIAL_CHARACTER_WILDCARD,    0, SINGLE_CHARACTER_WILDCARD.length()) ?     SPECIAL_CHARACTER_WILDCARD    :
                                                                                                                                   null;
            if (wildcard != null)
            {
                // Add the simple text string that we've skipped.
                if (previousIndex < index)
                {
                    expressionPartsList.add(regularExpression.substring(previousIndex, index));
                }

                // Add the wildcard that we've found.
                expressionPartsList.add(wildcard);

                // We'll continue parsing after this wildcard.
                index += wildcard.length();
                previousIndex = index;
            }
            else
            {
                // We'll continue parsing at the next character.
                index++;
            }
        }

        // Add the final simple text string that we've skipped, if any.
        if (wildcard == null)
        {
            expressionPartsList.add(regularExpression.substring(previousIndex));
        }

        // Copy the List into the array.
        expressionParts = new String[expressionPartsList.size()];
        expressionPartsList.toArray(expressionParts);
    }


    // Implementations for StringMatcher.

    public boolean matches(String string)
    {
        return matches(string, 0, 0);
    }


    /**
     * Tries to match the given string, starting at the given index, with the
     * regular expression parts starting at the given index.
     */
    private boolean matches(String string,
                            int    stringStartIndex,
                            int    expressionIndex)
    {
        // Are we out of expression parts?
        if (expressionIndex == expressionParts.length)
        {
            // There's a match, at least if we're at the end of the string as well.
            return stringStartIndex == string.length();
        }

        String expressionPart = expressionParts[expressionIndex];

        // Did we get a wildcard of some sort?
        if (expressionPart.equals(SINGLE_CHARACTER_WILDCARD))
        {
            // Do we have any characters left to match?
            if (stringStartIndex == string.length())
            {
                // We've run out of characters.
                return false;
            }

            // Make sure we're matching an allowed character and then check if
            // the rest of the expression parts match.
            return
                matchesWildcard(string.charAt(stringStartIndex)) &&
                matches(string, stringStartIndex + 1, expressionIndex + 1);
        }
        else if (expressionPart.equals(MULTIPLE_CHARACTERS_WILDCARD1))
        {
            // Try out all possible matches for '*', not matching the package
            // separator.
            for (int stringEndIndex = stringStartIndex;
                 stringEndIndex <= string.length();
                 stringEndIndex++)
            {
                // Are we matching some characters already?
                if (stringEndIndex > stringStartIndex)
                {
                    // Make sure we don't start matching the wrong characters.
                    if (!matchesWildcard(string.charAt(stringEndIndex-1)))
                    {
                        // We can never get a match.
                        return false;
                    }
                }

                // Continue looking for a match of the next expression part,
                // starting from the end index.
                if (matches(string, stringEndIndex, expressionIndex + 1))
                {
                    return true;
                }
            }

            // We could get a match for '*', but not for the rest of the
            // expression parts.
            return false;
        }
        else if (expressionPart.equals(MULTIPLE_CHARACTERS_WILDCARD2))
        {
            // Try out all possible matches for '**'.
            for (int stringEndIndex = stringStartIndex;
                 stringEndIndex <= string.length();
                 stringEndIndex++)
            {
                // Are we matching some characters already?
                if (stringEndIndex > stringStartIndex)
                {
                    // Make sure we don't start matching the wrong characters.
                    if (!matchesExtendedWildcard(string.charAt(stringEndIndex-1)))
                    {
                        // We can never get a match.
                        return false;
                    }
                }

                // Continue looking for a match of the next expression part,
                // starting from this index.
                if (matches(string, stringEndIndex, expressionIndex + 1))
                {
                    return true;
                }
            }

            // We could get a match for '**', but not for the rest of the
            // expression parts.
            return stringStartIndex == string.length();
        }
        else if (expressionPart.equals(SPECIAL_CHARACTER_WILDCARD))
        {
            // Do we have any characters left to match?
            if (stringStartIndex == string.length())
            {
                // We've run out of characters.
                return false;
            }

            // Make sure we're matching an allowed character and then check if
            // the rest of the expression parts match.
            return
                matchesSpecialWildcard(string.charAt(stringStartIndex)) &&
                matches(string, stringStartIndex + 1, expressionIndex + 1);
        }
        else
        {
            // The expression part is a simple text string. Check if it matches,
            // and if the rest of the expression parts match.
            int expressionPartLength = expressionPart.length();
            return
                string.regionMatches(stringStartIndex, expressionPart, 0, expressionPartLength) &&
                matches(string, stringStartIndex + expressionPartLength, expressionIndex + 1);
        }
    }


    /**
     * Returns whether the given character matches a simple '?' or '*' wildcard.
     */
    private boolean matchesWildcard(char character)
    {
        if (Character.isJavaIdentifierPart(character))
        {
            return true;
        }

        if (wildcardCharacters != null)
        {
            for (int index = 0; index < wildcardCharacters.length; index++)
            {
                if (character == wildcardCharacters[index])
                {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * Returns whether the given character matches an extended '**' wildcard.
     */
    private boolean matchesExtendedWildcard(char character)
    {
        if (matchesWildcard(character))
        {
            return true;
        }

        if (extendedWildcardCharacters != null)
        {
            for (int index = 0; index < extendedWildcardCharacters.length; index++)
            {
                if (character == extendedWildcardCharacters[index])
                {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * Returns whether the given character matches a special '%' wildcard.
     */
    private boolean matchesSpecialWildcard(char character)
    {
        if (specialWildcardCharacters != null)
        {
            for (int index = 0; index < specialWildcardCharacters.length; index++)
            {
                if (character == specialWildcardCharacters[index])
                {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * A main method for testing string matching.
     */
    public static void main(String[] args)
    {
        try
        {
            System.out.println("Regular expression ["+args[0]+"]");
            BasicMatcher matcher =
                new BasicMatcher(args[0], null, new char[] {'/'}, null);

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
