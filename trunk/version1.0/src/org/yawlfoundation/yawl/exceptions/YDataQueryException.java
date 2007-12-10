/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.exceptions;

import org.jdom.Element;

/**
 * 
 * @author Lachlan Aldred
 * Date: 1/09/2005
 * Time: 08:39:16
 * 
 */
public class YDataQueryException extends YDataStateException {


    public YDataQueryException(String queryString, Element data, String source, String message) {
        super(queryString, data, null, null, null, source, message);
    }

    public String getMessage() {
        return "The MI data accessing query (" + getQueryString() + ") " +
                "for the task (" + getSource() + ") " +
                "was applied over some data. " +
                "It failed to execute as expected.";
    }


    public String getQueryString() {
        return _queryString;
    }

    public Element getData() {
        return _queriedData;
    }
}
