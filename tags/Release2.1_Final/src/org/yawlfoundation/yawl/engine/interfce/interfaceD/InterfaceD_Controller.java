/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
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
