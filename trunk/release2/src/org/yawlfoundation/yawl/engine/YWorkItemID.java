/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
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
