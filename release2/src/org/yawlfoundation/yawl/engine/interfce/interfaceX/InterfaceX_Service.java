/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.engine.interfce.interfaceX;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.YSpecificationID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;

/**
 *  The interface InterfaceX_Service defines the exception event methods that are passed
 *  from the engine to the exception service. It is designed to be implemented by the
 *  exception service class.
 *
 *  This interface def is a member of Interface X, which provides an interface
 *  between the YAWL Engine and a Custom YAWL Service that manages exception
 *  handling at the process level.
 *
 *  Schematic of Interface X:
 *                                          |
 *                           EXCEPTION      |                              INTERFACE X
 *                            GATEWAY       |                                SERVICE
 *                  (implements) |          |                       (implements) |
 *                               |          |                                    |
 *  +==========+   ----->   ENGINE-SIDE  ---|-->   SERVICE-SIDE  ----->   +=============+
 *  || YAWL   ||              CLIENT        |        SERVER               || EXCEPTION ||
 *  || ENGINE ||                            |                             ||  SERVICE  ||
 *  +==========+   <-----   ENGINE-SIDE  <--|---   SERVICE-SIDE  <-----   +=============+
 *                            SERVER        |         CLIENT
 *                                          |
 *  @author Michael Adams                   |
 *  @version 0.8, 07/06/2006
 */

public interface InterfaceX_Service {

     void handleCheckCaseConstraintEvent(YSpecificationID specID, String caseID,
                                         String data, boolean precheck);

     void handleCheckWorkItemConstraintEvent(WorkItemRecord wir, String data, boolean precheck);

     String handleWorkItemAbortException(WorkItemRecord wir, String caseData);

     void handleTimeoutEvent(WorkItemRecord wir, String taskList);

     void handleResourceUnavailableException(String resourceID, WorkItemRecord wir,
                                             String caseData, boolean primary);

     String handleConstraintViolationException(WorkItemRecord wir, String caseData);

     void handleCaseCancellationEvent(String caseID);

     void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException ;
}
