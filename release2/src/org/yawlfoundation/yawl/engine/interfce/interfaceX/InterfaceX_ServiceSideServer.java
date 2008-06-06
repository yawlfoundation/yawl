/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.engine.interfce.interfaceX;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

/**
 *  InterfaceX_ServiceSideServer passes exception event calls from the engine to the
 *  exception service.
 *
 *  This class is a member class of Interface X, which provides an interface
 *  between the YAWL Engine and a Custom YAWL Service that manages exception
 *  handling at the process level.
 *
 *  InterfaceB_EnvironmentBasedServer was used as a template for this class.
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
 *  @version 0.8, 04/07/2006
 */

public class InterfaceX_ServiceSideServer extends HttpServlet {

    private InterfaceX_Service _controller;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        ServletContext context = servletConfig.getServletContext();
        String controllerClassName =
                context.getInitParameter("InterfaceX_Service");
        try {
            Class controllerClass = Class.forName(controllerClassName);
            _controller = (InterfaceX_Service) controllerClass.newInstance();
            context.setAttribute("controller", _controller);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
                                                 throws IOException, ServletException {
        _controller.doGet(request, response);
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/xml");
        PrintWriter outputWriter = response.getWriter();
        StringBuffer output = new StringBuffer();
        output.append("<response>");
        output.append(processPostQuery(request));
        output.append("</response>");
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();
    }

    // receives the post and calls the specified Exception service method 
    private String processPostQuery(HttpServletRequest request) {

        // unpack the stringified parameters
        WorkItemRecord wir = null ;
        String workItemXML = request.getParameter("workItem");
        if (workItemXML != null) wir = Marshaller.unmarshalWorkItem(workItemXML);

        boolean preCheck = false ;
        String sPreCheck = request.getParameter("preCheck") ;
        if (sPreCheck != null) preCheck = sPreCheck.equalsIgnoreCase("TRUE");

        // unpack the strings
        String data = request.getParameter("data");
        String caseID = request.getParameter("caseID");
        String specID = request.getParameter("specID");
        String taskList = request.getParameter("taskList");

        switch (Integer.parseInt(request.getParameter("action"))) {
            case InterfaceX_EngineSideClient.NOTIFY_CHECK_CASE_CONSTRAINTS:
               _controller.handleCheckCaseConstraintEvent(specID, caseID, data, preCheck);
               break;
            case InterfaceX_EngineSideClient.NOTIFY_CHECK_ITEM_CONSTRAINTS:
                _controller.handleCheckWorkItemConstraintEvent(wir, data, preCheck);
               break;
            case InterfaceX_EngineSideClient.NOTIFY_WORKITEM_ABORT:
               _controller.handleWorkItemAbortException(wir);
               break;
            case InterfaceX_EngineSideClient.NOTIFY_TIMEOUT:
               _controller.handleTimeoutEvent(wir, taskList);
               break;
            case InterfaceX_EngineSideClient.NOTIFY_RESOURCE_UNAVAILABLE:
               _controller.handleResourceUnavailableException(wir);
               break;
            case InterfaceX_EngineSideClient.NOTIFY_CONSTRAINT_VIOLATION:
               _controller.handleConstraintViolationException(wir);
               break;
            case InterfaceX_EngineSideClient.NOTIFY_CANCELLED_CASE:
               _controller.handleCaseCancellationEvent(caseID);
               break;
        }
        return "<success/>";
    }


}
