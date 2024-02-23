/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 16/09/2014
 */
public class IdGenerator {

    private static final Set<String> _usedIDs = new HashSet<String>();


    protected static void clear() { _usedIDs.clear(); }


    protected static String uniquify(String id) {
        char[] idChars = id.toCharArray();
        StringBuilder cleanChars = new StringBuilder(idChars.length);

        // only letter, digit, underscore or dash allowed for an id
        for (char c : idChars) {
            if (Character.isJavaIdentifierPart(c) && c != '$') {
                cleanChars.append(c);
            }
        }
        String cleanid = cleanChars.toString();
        int suffix = 0;
        while (_usedIDs.contains(cleanid + ++suffix)) ;
        String result = cleanid + suffix;
        _usedIDs.add(result);
        return result;
    }




}
