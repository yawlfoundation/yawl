/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine.interfce.interfaceB;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;


/**
 * Receives event announcements from the engine and passes each of them to the
 *  custom service's appropriate handling method
 *
 * @author Lachlan Aldred
 * Date: 23/01/2004
 * Time: 13:26:04
 *
 * @author Michael Adams (refactored for v2.0, 12/2008)
 */

public class InterfaceB_EnvironmentBasedServer extends HttpServlet {
    private InterfaceBWebsideController _controller;
    private static final boolean _debug = false;
    private Logger _logger = Logger.getLogger(InterfaceB_EnvironmentBasedServer.class);


    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        ServletContext context = servletConfig.getServletContext();

        // get the name of the custom service implementing this interface
        // (i.e. the name of the class that extends InterfaceBWebSideController)
        String controllerClassName =
                context.getInitParameter("InterfaceBWebSideController");

        //If there is an auth proxy firewall and it has been configured it in the
        //web.xml file the settings be retrieved for use.
        String userName = context.getInitParameter("UserName");
        String password = context.getInitParameter("Password");
        String proxyHost = context.getInitParameter("ProxyHost");
        String proxyPort = context.getInitParameter("ProxyPort");
        try {
            Class controllerClass = Class.forName(controllerClassName);

            // If the class has a getInstance() method, call that method rather than
            // calling a constructor (& thus instantiating 2 instances of the class)
            try {
                Method instMethod = controllerClass.getDeclaredMethod("getInstance");
                _controller = (InterfaceBWebsideController) instMethod.invoke(null);
            }
            catch (NoSuchMethodException nsme) {
                _controller = (InterfaceBWebsideController) controllerClass.newInstance();
            }
            
            // retrieve the URL of the YAWL Engine from the web.xml file.
            String engineBackendAddress = context.getInitParameter("InterfaceB_BackEnd");
            _controller.setUpInterfaceBClient(engineBackendAddress);
            _controller.setRemoteAuthenticationDetails(
                    userName, password, proxyHost, proxyPort);
            context.setAttribute("controller", _controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        if (_debug) {
            _logger.debug("\nInterfaceB_EnvironmentBasedServer " +
                    "request.getRequestURL = " + request.getRequestURL());
            _logger.debug("InterfaceB_EnvironmentBasedServer::doGet() request.parameters = ");
            Enumeration paramNms = request.getParameterNames();
            while (paramNms.hasMoreElements()) {
                String name = (String) paramNms.nextElement();
                _logger.debug("\trequest.getParameter(" + name + ") = " +
                        request.getParameter(name));
            }
        }

        _controller.doGet(request, response);
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OutputStreamWriter outputWriter = ServletUtils.prepareResponse(response);
        StringBuffer output = new StringBuffer();
        output.append("<response>");
        output.append(processPostQuery(request));
        output.append("</response>");
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();

    }


    private String processPostQuery(HttpServletRequest request) {
        if (_debug) {
            _logger.debug("\nInterfaceB_Server_WebSide::doPost() " +
                    "request.getRequestURL = " + request.getRequestURL());
            _logger.debug("InterfaceB_EnvironmentBasedServer::doPost() request.parameters = ");
            Enumeration paramNms = request.getParameterNames();
            while (paramNms.hasMoreElements()) {
                String name = (String) paramNms.nextElement();
                _logger.debug("\trequest.getParameter(" + name + ") = " +
                        request.getParameter(name));
            }
        }

        String action = request.getParameter("action");
        String workItemXML = request.getParameter("workItem");
        WorkItemRecord workItem = Marshaller.unmarshalWorkItem(workItemXML);
        if ("handleEnabledItem".equals(action)) {
            _controller.handleEnabledWorkItemEvent(workItem);
        }
        else if ("cancelWorkItem".equals(action)) {
            _controller.handleCancelledWorkItemEvent(workItem);
        }
        else if ("timerExpiry".equals(action)) {
            _controller.handleTimerExpiryEvent(workItem);
        }
        else if ("announceCompletion".equals(action)) {
            String caseID = request.getParameter("caseID");
            String casedata = request.getParameter("casedata");
            _controller.handleCompleteCaseEvent(caseID, casedata);
        }
        else if ("announceCaseCancelled".equals(action)) {
            String caseID = request.getParameter("caseID");
            _controller.handleCancelledCaseEvent(caseID);
        }
        else if ("announceEngineInitialised".equals(action)) {
            _controller.handleEngineInitialisationCompletedEvent();
        }
        else if ("announceItemStatus".equals(action)) {
            String oldStatus = request.getParameter("oldStatus");
            String newStatus = request.getParameter("newStatus");
            _controller.handleWorkItemStatusChangeEvent(workItem, oldStatus, newStatus);
        }
        else if ("ParameterInfoRequest".equals(action)) {
            YParameter[] params = _controller.describeRequiredParams();
            StringBuffer output = new StringBuffer();
            for (YParameter param : params) {
                output.append(param.toXML());
            }
            return output.toString();
        }
        if (_debug) {
        }
        return "<success/>";
    }


}
