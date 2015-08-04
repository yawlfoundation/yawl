/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.exceptions;

import au.edu.qut.yawl.elements.YExternalNetElement;


/**
 * 
 * @author Lachlan Aldred
 * 
 */
public class YConnectivityException extends YSyntaxException {


    public YConnectivityException(YExternalNetElement source, YExternalNetElement destination) {
        super("YAWL Syntax does not permit " + source
                + " to be connected with " + destination);
    }

    /**
     * Constructor YConnectivityException.
     * @param msg
     */
    public YConnectivityException(String msg) {
        super(msg);
    }


}
