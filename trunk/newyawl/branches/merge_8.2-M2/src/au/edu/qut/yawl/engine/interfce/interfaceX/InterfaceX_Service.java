/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */
package au.edu.qut.yawl.engine.interfce.interfaceX;

import au.edu.qut.yawl.worklist.model.WorkItemRecord;

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
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  @version 0.8, 07/06/2006
 */

public interface InterfaceX_Service {

     void handleCheckCaseConstraintEvent(String specID, String caseID, String data, boolean precheck);

     void handleCheckWorkItemConstraintEvent(WorkItemRecord wir, String data, boolean precheck);

     void handleWorkItemAbortException(WorkItemRecord wir);

     void handleTimeoutEvent(WorkItemRecord wir, String taskList);

     void handleResourceUnavailableException(WorkItemRecord wir);

     void handleConstraintViolationException(WorkItemRecord wir);

     void handleCaseCancellationEvent(String caseID);

     void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException ;
}
