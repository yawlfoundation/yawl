/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.util;

import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author Lachlan Aldred
 * Date: 30/04/2003
 * Time: 10:28:42
 * 
 */
public class YMessagePrinter {
    public static void printMessages(List messages) {
        Iterator iter = messages.iterator();
        while (iter.hasNext()) {
            Object o = null;
            try {
                o = iter.next();
                YVerificationMessage vm = (YVerificationMessage) o;
            } catch (ClassCastException cce) {
                cce.printStackTrace();
            }
        }
    }


    public static String getMessageString(List messages) {
        StringBuffer stringBuffer = new StringBuffer();
        Iterator iter = messages.iterator();
        while (iter.hasNext()) {
            YVerificationMessage message = (YVerificationMessage) iter.next();
            stringBuffer.append("\n" + message.getStatus() + ":" + message.getMessage());
        }
        return stringBuffer.toString();
    }
}
