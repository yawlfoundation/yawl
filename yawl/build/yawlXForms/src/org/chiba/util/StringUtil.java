/*
 * Artistic License
 *
 * Preamble
 *
 * The intent of this document is to state the conditions under which a
 * Package may be copied, such that the Copyright Holder maintains some
 * semblance of artistic control over the development of the package, while
 * giving the users of the package the right to use and distribute the
 * Package in a more-or-less customary fashion, plus the right to make
 * reasonable modifications.
 *
 * Definitions:
 *
 * "Package" refers to the collection of files distributed by the Copyright
 * Holder, and derivatives of that collection of files created through
 * textual modification.
 *
 * "Standard Version" refers to such a Package if it has not been modified,
 * or has been modified in accordance with the wishes of the Copyright
 * Holder.
 *
 * "Copyright Holder" is whoever is named in the copyright or copyrights
 * for the package.
 *
 * "You" is you, if you're thinking about copying or distributing this
 * Package.
 *
 * "Reasonable copying fee" is whatever you can justify on the basis of
 * media cost, duplication charges, time of people involved, and so
 * on. (You will not be required to justify it to the Copyright Holder, but
 * only to the computing community at large as a market that must bear the
 * fee.)
 *
 * "Freely Available" means that no fee is charged for the item itself,
 * though there may be fees involved in handling the item. It also means
 * that recipients of the item may redistribute it under the same
 * conditions they received it.
 *
 * 1. You may make and give away verbatim copies of the source form of the
 * Standard Version of this Package without restriction, provided that you
 * duplicate all of the original copyright notices and associated
 * disclaimers.
 *
 * 2. You may apply bug fixes, portability fixes and other modifications
 * derived from the Public Domain or from the Copyright Holder. A Package
 * modified in such a way shall still be considered the Standard Version.
 *
 * 3. You may otherwise modify your copy of this Package in any way,
 * provided that you insert a prominent notice in each changed file stating
 * how and when you changed that file, and provided that you do at least
 * ONE of the following:
 *
 * a) place your modifications in the Public Domain or otherwise make them
 * Freely Available, such as by posting said modifications to Usenet or an
 * equivalent medium, or placing the modifications on a major archive site
 * such as ftp.uu.net, or by allowing the Copyright Holder to include your
 * modifications in the Standard Version of the Package.
 *
 * b) use the modified Package only within your corporation or
 * organization.
 *
 * c) rename any non-standard executables so the names do not conflict with
 * standard executables, which must also be provided, and provide a
 * separate manual page for each non-standard executable that clearly
 * documents how it differs from the Standard Version.
 *
 * d) make other distribution arrangements with the Copyright Holder.
 *
 * 4. You may distribute the programs of this Package in object code or
 * executable form, provided that you do at least ONE of the following:
 *
 * a) distribute a Standard Version of the executables and library files,
 * together with instructions (in the manual page or equivalent) on where
 * to get the Standard Version.
 *
 * b) accompany the distribution with the machine-readable source of the
 * Package with your modifications.
 *
 * c) accompany any non-standard executables with their corresponding
 * Standard Version executables, giving the non-standard executables
 * non-standard names, and clearly documenting the differences in manual
 * pages (or equivalent), together with instructions on where to get the
 * Standard Version.
 *
 * d) make other distribution arrangements with the Copyright Holder.
 *
 * 5. You may charge a reasonable copying fee for any distribution of this
 * Package. You may charge any fee you choose for support of this
 * Package. You may not charge a fee for this Package itself.  However, you
 * may distribute this Package in aggregate with other (possibly
 * commercial) programs as part of a larger (possibly commercial) software
 * distribution provided that you do not advertise this Package as a
 * product of your own.
 *
 * 6. The scripts and library files supplied as input to or produced as
 * output from the programs of this Package do not automatically fall under
 * the copyright of this Package, but belong to whomever generated them,
 * and may be sold commercially, and may be aggregated with this Package.
 *
 * 7. C or perl subroutines supplied by you and linked into this Package
 * shall not be considered part of this Package.
 *
 * 8. The name of the Copyright Holder may not be used to endorse or
 * promote products derived from this software without specific prior
 * written permission.
 *
 * 9. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 * MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package org.chiba.util;

import java.util.StringTokenizer;

/**
 * This class implements some helper string method which may be used in
 * stylesheets as extensions.
 *
 * @author gregor klinke
 * @version $Revision$
 */
public class StringUtil {
    /**
     * __UNDOCUMENTED__
     *
     * @param idstr __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public static Boolean isValidID(String idstr) {
        String charsfirst = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String charsfollow = charsfirst + "0123456789_-";
        char[] idarray = idstr.toCharArray();

        if (idarray.length == 0) {
            return Boolean.FALSE;
        }

        if (charsfirst.indexOf(idarray[0]) == -1) {
            return Boolean.FALSE;
        }

        for (int c = 1; c < idarray.length; c++) {
            if (charsfollow.indexOf(idarray[c]) == -1) {
                return Boolean.FALSE;
            }
        }

        return Boolean.TRUE;
    }

    /**
     * Capitalize a string - i.e. Convert something like "purchase order" to "Purchase Order".
     *
     * @param value The value to capitalize - e.g. "purchase order".
     * @return The capitalized value - e.g. "Purchase Order".
     */
    public static String capitalize(String value) {
        if (value == null) {
            return null;
        }

        java.util.StringTokenizer tokenizer = new StringTokenizer(value, " ");
        StringBuffer result = new StringBuffer();

        while (tokenizer.hasMoreTokens()) {
            StringBuffer word = new StringBuffer(tokenizer.nextToken());

            // upper case first character
            word.replace(0, 1, word.substring(0, 1).toUpperCase());

            if (!tokenizer.hasMoreTokens()) {
                result.append(word);
            } else {
                result.append(word + " ");
            }
        }

        return result.toString();
    }

    /**
     * Capitalize an identifier - i.e. Convert something like "purchaseOrder" to "Purchase Order"
     * or "PURCHASE_ORDER" to "Purchase Order"
     *
     * @param value - The identifier to capitalize - e.g. "purchaseOrder" or "PURCHASE_ORDER".
     * @return The capitalized identifier - e.g. "Purchase Order".
     */
    public static String capitalizeIdentifier(String value) {
        if (value == null) {
            return null;
        }

        // if the word is all upper case, then set to lower case and continue
        if (value.equals(value.toUpperCase())) {
            value = value.toLowerCase();
        }

        java.util.StringTokenizer tokenizer = new StringTokenizer(value, "ABCDEFGHIJKLMNOPQRSTUVWXYZ_", true);
        StringBuffer result = new StringBuffer();

        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken();

            if ((word.equals(word.toUpperCase())) || (!tokenizer.hasMoreTokens())) {
                if (!word.equals("_")) {
                    result.append(word);
                }
            } else {
                result.append(word + " ");
            }
        }

        // upper case the first character
        //
        return capitalize(result.toString());
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param haystack __UNDOCUMENTED__
     * @param needle   __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public static Boolean endsWith(String haystack, String needle) {
        if ((haystack != null) && (needle != null)) {
            return new Boolean(haystack.endsWith(needle));
        }

        return Boolean.FALSE;
    }

    /**
     * scans a string and replaces all occurances of a given pattern with a
     * replacement string.  Thus replacePattern("hello world!", "o w",
     * "o/w"); result in "hello/world".
     */
    public static String replacePattern(String original, String pattern, String replacement) {
        StringTokenizer strtok = new StringTokenizer(original, pattern, true);
        StringBuffer result = new StringBuffer();

        if (replacement == null) {
            replacement = "";
        }

        while (strtok.hasMoreTokens()) {
            String token = strtok.nextToken();

            if (token.equals(pattern)) {
                result.append(replacement);
            } else {
                result.append(token);
            }
        }

        return result.toString();
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param str __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public static String toLowercase(String str) {
        return (str != null)
                ? str.toLowerCase()
                : null;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param str __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public static String toUppercase(String str) {
        return (str != null)
                ? str.toUpperCase()
                : null;
    }

    /* $Id$ */
    public static String trim(String str) {
        return (str != null)
                ? str.trim()
                : null;
    }

    /**
     * returns the value of the first parameter if set, otherwise a default value
     */
    public static String valueOf(String first, String defaultstr) {
        if ((first != null) && (first.length() > 0)) {
            return first;
        }

        return defaultstr;
    }
}


/*
   $Log$
   Revision 1.1  2006-02-27 17:28:02  maod
   *** empty log message ***

   Revision 1.4  2004/08/15 14:14:08  joernt
   preparing release...
   -reformatted sources to fix mixture of tabs and spaces
   -optimized imports on all files

   Revision 1.3  2003/10/02 15:15:50  joernt
   applied chiba jalopy settings to whole src tree

   Revision 1.2  2003/10/01 23:44:59  joernt
   fixed javadoc issues
   Revision 1.1.1.1  2003/05/23 14:54:06  unl
   no message
   Revision 1.3  2002/12/11 14:49:30  soframel
   added 2 methods in StringUtil (from chiba2 "StringUtils").
   added an ant task for the schema generator
   Revision 1.2  2001/09/26 09:39:19  gregor
   added a replacePattern method
   Revision 1.1  2001/09/20 18:31:59  gregor
   added (contains code for stylesheets extensions)
 */
