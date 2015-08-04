/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.util.List;

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
     * @see org.yawlfoundation.yawl.util.YVerificationMessage
     * @return List of YVerificationMessage
     */
    public List<YVerificationMessage> verify();
}
