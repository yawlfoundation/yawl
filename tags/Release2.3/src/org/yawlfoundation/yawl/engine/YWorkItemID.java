/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.engine;

import org.yawlfoundation.yawl.elements.state.YIdentifier;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * 
 * @author Lachlan Aldred
 * Date: 23/05/2003
 * Time: 14:32:32
 * 
 */
public class YWorkItemID {
    private static final char[] _uniqifier = UniqueIDGenerator.newAlphas();
    private char[] _uniqueID;
    private YIdentifier _caseID;
    private String _taskID;


    public YWorkItemID(YIdentifier caseID, String taskID) {
        this(caseID, taskID, null);
    }

    public YWorkItemID(YIdentifier caseID, String taskID, String uniqueID) {
        _caseID = caseID;
        _taskID = taskID;

        if (uniqueID != null) {

            // increment counter as required
            while (compare(_uniqifier, uniqueID.toCharArray()) > 0) {
                UniqueIDGenerator.nextInOrder(_uniqifier);
            }
            _uniqueID = uniqueID.toCharArray();
        }
        else {
            _uniqueID = _uniqifier.clone();
            UniqueIDGenerator.nextInOrder(_uniqifier);
        }
    }


    public String toString() {
        return _caseID.toString() + ":" + _taskID;
    }

    public YIdentifier getCaseID() {
        return _caseID;
    }

    public String getTaskID() {
        return _taskID;
    }

    public String getUniqueID() {
        return new String(_uniqueID);
    }

    private static int compare(char[] first, char[] second) {
        BigInteger bigInt1 = new BigInteger(String.valueOf(first).getBytes());
        BigInteger bigInt2 = new BigInteger(String.valueOf(second).getBytes());

        return bigInt2.compareTo(bigInt1);
    }


    public boolean equals(Object other) {
        if (this == other) return true;
        if (other instanceof YWorkItemID) {
            YWorkItemID otherID = (YWorkItemID) other;
            return this.getCaseID().equals(otherID.getCaseID()) &&
                   this.getTaskID().equals(otherID.getTaskID()) &&
                    (((this.getUniqueID() == null) && (otherID.getUniqueID() == null)) ||
                    this.getUniqueID().equals(otherID.getUniqueID()));
        }
        return false;
    }

    public int hashCode() {
        int uCode = (getUniqueID() != null) ? getUniqueID().hashCode() : 31;
        return getCaseID().hashCode() + getTaskID().hashCode() + uCode;
    }
    
}

/*********************************************************************************/

class UniqueIDGenerator {

    static char[] newAlphas() {
        char[] alphas = new char[25];
        Arrays.fill(alphas, '0');
        return alphas;
    }

    /**
     *
     * @param chars
     * @return
     */
    static char[] nextInOrder(char[] chars) {
        for (int i = chars.length - 1; i >= 0; i--) {
            char aChar = chars[i];
            if (inRange(aChar)) {
                aChar = inc(aChar);
                chars[i] = aChar;
                break;
            } else {
                chars[i] = '0';
            }
        }
        return chars;
    }

    private static char inc(char aChar) {
        /**
         * 0 = 48
         * 9 = 57
         * A = 65
         * Z = 90
         * a = 97
         * z = 122 */
        if (aChar == '9') {
            return 'A';
        }
        if (aChar == 'Z') {
            return 'a';
        }
        if (aChar == 'z') {
            throw new IllegalArgumentException("Shouldn't be called with 'z'.");
        }
        return (++aChar);
    }

    private static boolean inRange(char aChar) {
        return ((int) aChar) < ((int) 'z');
    }

}
