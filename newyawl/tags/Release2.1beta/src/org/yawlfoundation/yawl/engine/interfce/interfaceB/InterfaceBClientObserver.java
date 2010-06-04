/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine.interfce.interfaceB;

import org.yawlfoundation.yawl.engine.YSpecificationID;

/**
 * Defines the 'B' interface into the YAWL Engine corresponding to WfMC interfaces 2+3 - Workflow client applications and invoke applications.
 *
 * @author Andrew Hastie
 *         Creation Date: 10-Jun-2005
 */
public interface InterfaceBClientObserver
{
    /**
     * Called by the engine when a new case is created.<P>
     *
     * @param specID
     * @param caseIDStr
     */
    void addCase(YSpecificationID specID, String caseIDStr);

    /**
     * Called by the engine when a previously executing case is removed.<P>
     *
     * @param caseIDStr
     */
    void removeCase(String caseIDStr);
}
