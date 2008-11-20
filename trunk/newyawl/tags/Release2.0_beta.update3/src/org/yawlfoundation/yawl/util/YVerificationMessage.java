/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.util;

import java.util.List;

/**
 * 
 * @author Lachlan Aldred
 * Date: 7/10/2003
 * Time: 14:21:11
 * 
 */
public class YVerificationMessage {
    private Object _source;
    private String _message;
    private String _status;
    public static final String ERROR_STATUS = "Error";
    public static final String WARNING_STATUS = "Warning";


    public YVerificationMessage(Object source, String message, String status) {
        _source = source;
        _message = message;
        _status = status;
    }


    public Object getSource() {
        return _source;
    }


    public String getMessage() {
        return _message;
    }


    public String getStatus() {
        return _status;
    }


    public static boolean containsNoErrors(List messages) {
        for (int i = 0; i < messages.size(); i++) {
            YVerificationMessage message = (YVerificationMessage) messages.get(i);
            if (message.getStatus() == ERROR_STATUS) {
                return false;
            }
        }
        return true;
    }

    public void setSource(Object source) {
        _source = source;
    }
}
