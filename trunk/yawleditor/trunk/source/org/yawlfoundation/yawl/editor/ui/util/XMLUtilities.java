/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.util;

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

public class XMLUtilities {

    private static final char[] XML_SPECIAL_CHARACTERS = {
            '<','>','\"','\'','\\','&'
    };

    /**
     * Returns a variant of the supplied string with all invalid XML name characters
     * removed. A special case is the the space character, which is converted to the '_'
     * character instead.
     * @param name A string that could be used as an XML name.
     * @return A valid XML name equivalent of name.
     */

    public static String toValidXMLName(String name) {
        if (name == null) return null;

        StringBuilder s = new StringBuilder(name.length());
        for (char c : name.toCharArray()) {
            if (c == ' ') {
                s.append('_');
            }
            else if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.') {
                s.append(c);
            }
        }
        return s.toString();
    }


    /**
     * Tests the supplied character to determine whether it is a
     * special XML Character. If so, it returns true.
     * @param character A character to be tested.
     * @return whether the character is a special XML character or not.
     */

    public static boolean isSpecialXMLCharacter(final char character) {

        for (char XML_SPECIAL_CHARACTER : XML_SPECIAL_CHARACTERS) {
            if (character == XML_SPECIAL_CHARACTER) {
                return true;
            }
        }
        return false;
    }



    public static String formatXML(String xml, boolean prettify, boolean wrap) {
        if ((xml != null) && (xml.trim().startsWith("<"))) {
            String temp = wrap ? StringUtil.wrap(xml, "temp") : xml;
            XNode node = new XNodeParser(true).parse(temp);
            if (node != null) {
                if (prettify) {
                    temp = node.toPrettyString(1,3);
                    return wrap ? StringUtil.unwrap(temp).substring(1) : temp;  // lead \n
                }
                else {
                    temp = node.toString();
                    return wrap ? StringUtil.unwrap(temp) : temp;
                }
            }
        }
        return xml;
    }

}
