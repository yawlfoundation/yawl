/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine;

/**
 * An interface to handle problems.  Implement this interface to handle any probelms. Details
 * of the problem are provided by the problem event.
 *
 *
 * 
 * @author Lachlan Aldred
 * Date: 15/10/2004
 * Time: 11:47:30
 * 
 */
public interface YProblemHandler {
    public void handleProblem(YProblemEvent problemEvent);
}
