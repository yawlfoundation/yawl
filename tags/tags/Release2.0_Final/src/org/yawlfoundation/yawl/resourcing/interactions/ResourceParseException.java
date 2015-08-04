/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.interactions;


/**
 *  ResourceParseException Class.
 *
 *  An ResourceParseException is thrown when an attempt is made to parse
 *  the resourcing specification for a task and the spec does not match the
 *  expected content.
 *
 *  @author Michael Adams
 *  v2.0, 05/02/2008
 */

public class ResourceParseException extends Exception {
    
    public ResourceParseException() {
        super();
    }

    public ResourceParseException(String message) {
        super(message);
    }

}