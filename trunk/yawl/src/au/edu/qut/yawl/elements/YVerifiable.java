/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.util.List;

import au.edu.qut.yawl.util.YVerificationMessage;

/**
 * 
 * @author Lachlan Aldred
 * Date: 27/10/2003
 * Time: 14:41:07
 * 
 */
public interface YVerifiable {
    /**
     * The verify method returns a list of YVerificationMessage objects
     * @see au.edu.qut.yawl.util.YVerificationMessage
     * @return List of YVerificationMessage
     */
    public List <YVerificationMessage> verify();
}
