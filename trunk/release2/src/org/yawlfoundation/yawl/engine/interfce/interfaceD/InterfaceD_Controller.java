/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine.interfce.interfaceD;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.jdom.Element;

/**
 * Performs workitem processing.
 *
 * 
 * @author Lachlan Aldred
 * Date: 16/09/2005
 * Time: 16:13:15
 */
public interface InterfaceD_Controller {

    /**
     * Handles a workitem being sent to it.
     * @param workitem a workitem
     * @return an element corresponding to the result of processing.
     */
    Element processWorkItem(WorkItemRecord workitem);
}
