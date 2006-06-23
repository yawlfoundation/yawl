/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.util;

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
    public static void printMessages(List<YVerificationMessage> messages) {
        for (YVerificationMessage vm : messages) {
            System.out.println(vm.getStatus() + ":" + vm.getMessage());
        }
    }


    public static String getMessageString(List<YVerificationMessage> messages) {
        StringBuffer stringBuffer = new StringBuffer();
        for (YVerificationMessage message1 : messages) {
            stringBuffer
                    .append("\n").append(message1.getStatus())
                    .append(":").append(message1.getMessage());
        }
        return stringBuffer.toString();
    }
}
