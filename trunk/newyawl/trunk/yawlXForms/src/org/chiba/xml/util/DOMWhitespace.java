/*
 *
 *    Artistic License
 *
 *    Preamble
 *
 *    The intent of this document is to state the conditions under which a Package may be copied, such that
 *    the Copyright Holder maintains some semblance of artistic control over the development of the
 *    package, while giving the users of the package the right to use and distribute the Package in a
 *    more-or-less customary fashion, plus the right to make reasonable modifications.
 *
 *    Definitions:
 *
 *    "Package" refers to the collection of files distributed by the Copyright Holder, and derivatives
 *    of that collection of files created through textual modification.
 *
 *    "Standard Version" refers to such a Package if it has not been modified, or has been modified
 *    in accordance with the wishes of the Copyright Holder.
 *
 *    "Copyright Holder" is whoever is named in the copyright or copyrights for the package.
 *
 *    "You" is you, if you're thinking about copying or distributing this Package.
 *
 *    "Reasonable copying fee" is whatever you can justify on the basis of media cost, duplication
 *    charges, time of people involved, and so on. (You will not be required to justify it to the
 *    Copyright Holder, but only to the computing community at large as a market that must bear the
 *    fee.)
 *
 *    "Freely Available" means that no fee is charged for the item itself, though there may be fees
 *    involved in handling the item. It also means that recipients of the item may redistribute it under
 *    the same conditions they received it.
 *
 *    1. You may make and give away verbatim copies of the source form of the Standard Version of this
 *    Package without restriction, provided that you duplicate all of the original copyright notices and
 *    associated disclaimers.
 *
 *    2. You may apply bug fixes, portability fixes and other modifications derived from the Public Domain
 *    or from the Copyright Holder. A Package modified in such a way shall still be considered the
 *    Standard Version.
 *
 *    3. You may otherwise modify your copy of this Package in any way, provided that you insert a
 *    prominent notice in each changed file stating how and when you changed that file, and provided that
 *    you do at least ONE of the following:
 *
 *        a) place your modifications in the Public Domain or otherwise make them Freely
 *        Available, such as by posting said modifications to Usenet or an equivalent medium, or
 *        placing the modifications on a major archive site such as ftp.uu.net, or by allowing the
 *        Copyright Holder to include your modifications in the Standard Version of the Package.
 *
 *        b) use the modified Package only within your corporation or organization.
 *
 *        c) rename any non-standard executables so the names do not conflict with standard
 *        executables, which must also be provided, and provide a separate manual page for each
 *        non-standard executable that clearly documents how it differs from the Standard
 *        Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    4. You may distribute the programs of this Package in object code or executable form, provided that
 *    you do at least ONE of the following:
 *
 *        a) distribute a Standard Version of the executables and library files, together with
 *        instructions (in the manual page or equivalent) on where to get the Standard Version.
 *
 *        b) accompany the distribution with the machine-readable source of the Package with
 *        your modifications.
 *
 *        c) accompany any non-standard executables with their corresponding Standard Version
 *        executables, giving the non-standard executables non-standard names, and clearly
 *        documenting the differences in manual pages (or equivalent), together with instructions
 *        on where to get the Standard Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    5. You may charge a reasonable copying fee for any distribution of this Package. You may charge
 *    any fee you choose for support of this Package. You may not charge a fee for this Package itself.
 *    However, you may distribute this Package in aggregate with other (possibly commercial) programs as
 *    part of a larger (possibly commercial) software distribution provided that you do not advertise this
 *    Package as a product of your own.
 *
 *    6. The scripts and library files supplied as input to or produced as output from the programs of this
 *    Package do not automatically fall under the copyright of this Package, but belong to whomever
 *    generated them, and may be sold commercially, and may be aggregated with this Package.
 *
 *    7. C or perl subroutines supplied by you and linked into this Package shall not be considered part of
 *    this Package.
 *
 *    8. The name of the Copyright Holder may not be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 *    9. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED
 *    WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 *    MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package org.chiba.xml.util;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * The DOM Whitespace helper class provides a set of methods that might
 * be helpful in implementing adjustable whitespace and comment handling
 * in DOM processing applications.
 *
 * @author <a href="mailto:unl@users.sourceforge.net">Ulrich Nicolas Liss&eacute;</a>
 * @version $Id: DOMWhitespace.java,v 1.7 2004/08/15 14:14:09 joernt Exp $
 */
public class DOMWhitespace {
    // Utility methods.

    /**
     * Checks wether the given string consists of Unicode whitespace.
     *
     * @param string the string to be checked.
     * @return <CODE>true</CODE> if the given string consists of
     *         Unicode whitespace, otherwise <CODE>false</CODE>.
     */
    public static boolean isWhitespace(String string) {
        if (string != null) {
            for (int index = 0; index < string.length(); index++) {
                if (!Character.isWhitespace(string.charAt(index))) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returns the index of the next item in the node list being not a
     * comment node.
     *
     * @param list  the node list.
     * @param start the start index.
     * @return the index of the next item in the node list being not a
     *         comment node.
     */
    public static int skipComments(NodeList list, int start) {
        for (int index = start; index < list.getLength(); index++) {
            if (list.item(index).getNodeType() != Node.COMMENT_NODE) {
                return index;
            }
        }

        return list.getLength();
    }

    /**
     * Returns the index of the next item in the node list being not a
     * whitespace text node.
     *
     * @param list  the node list.
     * @param start the start index.
     * @return the index of the next item in the node list being not a
     *         whitespace text node.
     */
    public static int skipWhitespace(NodeList list, int start) {
        for (int index = start; index < list.getLength(); index++) {
            if (list.item(index).getNodeType() != Node.TEXT_NODE) {
                return index;
            }

            if (!isWhitespace(list.item(index).getNodeValue())) {
                return index;
            }
        }

        return list.getLength();
    }

    /**
     * Returns the index of the next item in the node list being neither a
     * whitespace text node nor a comment node.
     *
     * @param list  the node list.
     * @param start the start index.
     * @return the index of the next item in the node list being neither a
     *         whitespace text node nor a comment node.
     */
    public static int skipWhitespaceAndComments(NodeList list, int start) {
        for (int index = start; index < list.getLength(); index++) {
            if (list.item(index) == null) {
                return index;
            }

            if ((list.item(index).getNodeType() != Node.COMMENT_NODE) &&
                    (list.item(index).getNodeType() != Node.TEXT_NODE)) {
                return index;
            }

            if ((list.item(index).getNodeType() == Node.TEXT_NODE) &&
                    !isWhitespace(list.item(index).getNodeValue())) {
                return index;
            }
        }

        return list.getLength();
    }

    /**
     * Splits the specified text into whitespace and text portions.
     * <p/>
     * Any leading or trailing whitespace characters as well as any
     * two or more adjacent whitespace characters within text are
     * separated.
     *
     * @param text the text to denormalize.
     * @return a list of whitespace and text portions.
     */
    public static List denormalizeText(String text) {
        if (text == null) {
            return null;
        }

        List list = new ArrayList();
        if (text.length() == 0) {
            list.add(text);
            return list;
        }

        int start = 0;
        boolean whitespace = Character.isWhitespace(text.charAt(start));

        for (int index = 0; index < text.length(); index++) {
            if (whitespace && (!Character.isWhitespace(text.charAt(index)))) {
                // add whitespace and switch mode
                list.add(text.substring(start, index));
                start = index;
                whitespace = false;
            }
            if (!whitespace && Character.isWhitespace(text.charAt(index))) {
                // look ahead to skip a single whitespace character within text
                if ((index + 1 < text.length() && Character.isWhitespace(text.charAt(index + 1))) ||
                        index + 1 == text.length()) {
                    // add text and switch mode
                    list.add(text.substring(start, index));
                    start = index;
                    whitespace = true;
                }
            }
        }

        list.add(text.substring(start, text.length()));
        return list;
    }

}
